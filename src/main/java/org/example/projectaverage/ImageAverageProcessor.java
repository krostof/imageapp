package org.example.projectaverage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa do przetwarzania obrazów przy użyciu OpenCV:
 *  - Tworzenie wideo z ruchomym uśrednianiem (moving average),
 *  - Obliczanie obrazu będącego średnią z podanych plików.
 */
public class ImageAverageProcessor {

    /**
     * Tworzy wideo z listy plików obrazów przy użyciu uśredniania ruchomego,
     * zapisując je do wskazanej ścieżki (outputPath).
     *
     * @param imageFiles  Lista plików obrazów
     * @param windowSize  Rozmiar okna do uśredniania (moving average)
     * @param outputPath  Docelowa ścieżka pliku wideo (np. "C:/video/output_video.avi")
     * @return            Ta sama ścieżka (outputPath), jeżeli zapis się powiedzie
     */
    public static String processImagesToCustomPath(
            List<File> imageFiles,
            int windowSize,
            String outputPath
    ) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided for video creation.");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be greater than 0.");
        }
        if (outputPath == null || outputPath.isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty.");
        }

        // 1. Wczytanie wszystkich klatek (Mat) z plików
        List<Mat> frames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image = loadImage(file.getAbsolutePath());
            if (image.empty()) {
                throw new RuntimeException("Could not read image: " + file.getAbsolutePath());
            }
            frames.add(image);
        }

        // 2. Przetwarzanie (uśrednianie ruchome)
        List<Mat> processedFrames = calculateMovingAverage(frames, windowSize);

        // 3. Utworzenie wideo w docelowej lokalizacji
        createVideo(processedFrames, outputPath);

        // Zwracamy ścieżkę, aby GUI mogło poinformować użytkownika
        return outputPath;
    }

    /**
     * Oblicza obraz będący średnią ze wszystkich podanych plików.
     * Zwraca ścieżkę do zapisanego obrazu (np. overall_average.jpg).
     *
     * @param imageFiles Lista plików obrazów
     * @return           Ścieżka do wygenerowanego obrazu,
     *                   lub napis "Failed to calculate overall average." w razie niepowodzenia
     */
    public static String calculateOverallAverage(List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return "Failed to calculate overall average.";
        }

        Mat sum = null;
        int imageCount = 0;

        // Sumowanie wszystkich obrazów
        for (File file : imageFiles) {
            Mat image = Imgcodecs.imread(file.getAbsolutePath());
            if (image.empty()) {
                System.err.println("Cannot read image: " + file.getAbsolutePath());
                continue; // Możesz też rzucić wyjątek, jeśli wolisz przerwać od razu
            }

            if (sum == null) {
                // Pierwszy obraz ustala rozmiar i typ
                sum = Mat.zeros(image.size(), image.type());
            } else {
                // (opcjonalnie) można sprawdzić, czy ma te same wymiary i typ
                if (!sum.size().equals(image.size()) || sum.type() != image.type()) {
                    return "Failed to calculate overall average. Inconsistent image sizes/types.";
                }
            }

            Core.add(sum, image, sum);
            imageCount++;
        }

        if (sum == null || imageCount == 0) {
            return "Failed to calculate overall average. No valid images.";
        }

        // Dzielenie przez liczbę obrazów
        Mat average = new Mat();
        Core.divide(sum, Scalar.all(imageCount), average);

        // Zapis do pliku
        File firstFile = imageFiles.get(0);
        File parentDir = firstFile.getParentFile();
        if (parentDir == null) {
            // jeśli plik jest w bieżącym folderze i getParentFile() zwraca null
            parentDir = new File(".");
        }
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        String outputPath = new File(parentDir, "overall_average.jpg").getAbsolutePath();
        boolean success = Imgcodecs.imwrite(outputPath, average);
        if (!success) {
            return "Failed to calculate overall average. Could not write file.";
        }
        return outputPath;
    }

    /**
     * Metoda pomocnicza wykonująca uśrednianie ruchome (moving average) na liście klatek.
     *
     * @param frames      Lista wczytanych klatek (Mat)
     * @param windowSize  Rozmiar okna
     * @return            Lista przetworzonych klatek
     */
    private static List<Mat> calculateMovingAverage(List<Mat> frames, int windowSize) {
        List<Mat> resultFrames = new ArrayList<>();
        if (frames.isEmpty()) {
            return resultFrames;
        }

        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());

        for (int i = 0; i < frames.size(); i++) {
            Core.add(sum, frames.get(i), sum);

            if (i >= windowSize - 1) {
                // Wyliczamy średnią dla aktualnego "okna"
                Mat average = calculateAverage(sum, windowSize);
                resultFrames.add(average);

                // Usuwamy "najstarszą" klatkę z sumy
                Core.subtract(sum, frames.get(i - windowSize + 1), sum);
            }
        }
        return resultFrames;
    }

    /**
     * Metoda pomocnicza do wyliczenia średniej z sumy pikseli (sum / windowSize).
     */
    private static Mat calculateAverage(Mat sum, int windowSize) {
        Mat average = new Mat();
        Core.divide(sum, Scalar.all(windowSize), average);
        return average;
    }

    /**
     * Wczytuje obraz jako Mat z podanej ścieżki.
     * Zawiera przykładowy workaround dla .TIF -> .png,
     * jeśli potrzebujesz obsługi plików TIFF.
     */
    private static Mat loadImage(String path) {
        Mat image = Imgcodecs.imread(path);
        if (!image.empty()) {
            return image;
        }

        // Ewentualna konwersja TIF -> PNG
        if (path.toLowerCase().endsWith(".tif")) {
            String pngPath = path.replaceAll("(?i)\\.tif$", ".png");
            File tifFile = new File(path);
            File pngFile = new File(pngPath);

            if (tifFile.exists() && tifFile.renameTo(pngFile)) {
                Mat converted = Imgcodecs.imread(pngPath);
                if (!converted.empty()) {
                    return converted;
                }
            }
        }

        // Jeśli nie udało się wczytać obrazu
        throw new IllegalArgumentException("Cannot load image. Unsupported format or path: " + path);
    }

    /**
     * Zapisuje listę klatek do pliku wideo (.avi) w zadanej ścieżce.
     *
     * @param frames      Lista klatek (Mat)
     * @param outputPath  Ścieżka docelowa
     */
    private static void createVideo(List<Mat> frames, String outputPath) {
        if (frames.isEmpty()) {
            System.err.println("No frames to write into video.");
            return;
        }

        File outFile = new File(outputPath);
        File parentDir = outFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        int width = frames.get(0).cols();
        int height = frames.get(0).rows();
        if (width <= 0 || height <= 0) {
            System.err.println("Invalid frame size: " + width + "x" + height);
            return;
        }

        Size frameSize = new Size(width, height);

        // Tworzymy VideoWriter (10 FPS, kodek MJPG)
        VideoWriter writer = new VideoWriter(
                outputPath,
                VideoWriter.fourcc('M','J','P','G'),
                10,
                frameSize,
                true
        );

        if (!writer.isOpened()) {
            System.err.println("Failed to open video writer for: " + outputPath);
            return;
        }

        for (Mat frame : frames) {
            writer.write(frame);
        }

        writer.release();
        System.out.println("Video saved at: " + outputPath);
    }
}
