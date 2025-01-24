package org.example.projectaverage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAverageProcessor {

    private static final ImageLoader imageLoader = new ImageLoader();
    private static final VideoCreator videoCreator = new VideoCreator();

    /**
     * Przetwarza listę obrazów (moving average), zapisując do określonej ścieżki (AVI).
     * Uśrednianie klatek odbywa się w formacie float (CV_32F), by uniknąć saturacji.
     *
     * @param imageFiles  lista plików obrazów
     * @param windowSize  rozmiar okna do ruchomego uśredniania
     * @param outputPath  docelowa ścieżka pliku wideo (np. "C:/video/output_video.avi")
     * @return            ścieżka zapisu wideo
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

        // 1. Wczytanie wszystkich obrazów jako float (CV_32F)
        List<Mat> floatFrames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image8U = loadImage(file.getAbsolutePath()); // CV_8U
            if (image8U.empty()) {
                throw new RuntimeException("Could not read image: " + file.getAbsolutePath());
            }
            // konwersja do float; jeżeli 1 kanał -> CV_32FC1, jeśli 3 kanały -> CV_32FC3
            int floatType = (image8U.channels() == 1) ? CvType.CV_32FC1 : CvType.CV_32FC3;
            Mat image32F = new Mat();
            image8U.convertTo(image32F, floatType);
            floatFrames.add(image32F);
        }

        // 2. Obliczenie uśredniania ruchomego (moving average) w float
        List<Mat> processedFloatFrames = calculateMovingAverageFloat(floatFrames, windowSize);

        // 3. Konwersja klatek z float -> 8U (dopiero teraz, po uśrednieniu)
        List<Mat> processed8UFrames = new ArrayList<>();
        for (Mat floatFrame : processedFloatFrames) {
            // Rzutowanie do 8-bit
            Mat frame8U = new Mat();
            floatFrame.convertTo(frame8U, CvType.CV_8U);
            processed8UFrames.add(frame8U);
        }

        // 4. Tworzymy wideo z klatek 8-bitowych
        createVideo(processed8UFrames, outputPath);

        return outputPath;
    }

    /**
     * Oblicza obraz będący średnią ze wszystkich plików, używając formatu float w trakcie sumowania.
     * Zwraca ścieżkę do zapisanego pliku (np. "overall_average.jpg").
     *
     * @param imageFiles  lista plików obrazów
     * @return            ścieżka do zapisanego uśrednionego obrazu albo komunikat o niepowodzeniu
     */
    public static String calculateOverallAverage(List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return "Failed to calculate overall average.";
        }

        Mat sum32F = null; // suma w formacie float
        int imageCount = 0;

        // Sumujemy obrazy w float (bez saturacji)
        for (File file : imageFiles) {
            Mat image8U = Imgcodecs.imread(file.getAbsolutePath());
            if (image8U.empty()) {
                System.err.println("Cannot read image: " + file.getAbsolutePath());
                continue;
            }

            // ustalamy, czy CV_32FC1 czy CV_32FC3
            int floatType = (image8U.channels() == 1) ? CvType.CV_32FC1 : CvType.CV_32FC3;
            Mat image32F = new Mat();
            image8U.convertTo(image32F, floatType);

            if (sum32F == null) {
                sum32F = Mat.zeros(image32F.size(), image32F.type());
            } else {
                // (opcjonalnie) sprawdź, czy rozmiar/typ się zgadza
                if (!sum32F.size().equals(image32F.size()) || sum32F.type() != image32F.type()) {
                    return "Failed to calculate overall average. Inconsistent sizes/types.";
                }
            }

            Core.add(sum32F, image32F, sum32F);
            imageCount++;
        }

        if (sum32F == null || imageCount == 0) {
            return "Failed to calculate overall average. No valid images.";
        }

        // Dzielenie float
        Core.divide(sum32F, Scalar.all(imageCount), sum32F);

        // Konwersja wyniku do 8-bit, aby zapisać normalnie
        Mat avg8U = new Mat();
        sum32F.convertTo(avg8U, CvType.CV_8U);

        // Zapis pliku
        File firstFile = imageFiles.get(0);
        File parentDir = firstFile.getParentFile();
        if (parentDir == null) {
            parentDir = new File(".");
        }
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        String outputPath = new File(parentDir, "overall_average.jpg").getAbsolutePath();
        boolean success = Imgcodecs.imwrite(outputPath, avg8U);
        if (!success) {
            return "Failed to calculate overall average. Could not write file.";
        }

        return outputPath;
    }

    /**
     * Funkcja obliczająca ruchome uśrednianie (moving average) w formacie float.
     * Zwraca listę klatek float (niesaturujących się).
     *
     * @param frames      lista klatek w formacie CV_32F
     * @param windowSize  rozmiar okna
     * @return            lista nowych klatek float po uśrednieniu
     */
    private static List<Mat> calculateMovingAverageFloat(List<Mat> frames, int windowSize) {
        List<Mat> resultFrames = new ArrayList<>();
        if (frames.isEmpty()) {
            return resultFrames;
        }

        // Suma w float
        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());

        for (int i = 0; i < frames.size(); i++) {
            Core.add(sum, frames.get(i), sum);

            if (i >= windowSize - 1) {
                // Obliczamy średnią w float: sum / windowSize
                Mat avgFloat = new Mat();
                Core.divide(sum, Scalar.all(windowSize), avgFloat);

                // Dodajemy do wyniku klatkę w formacie float (jeszcze nie 8-bit)
                resultFrames.add(avgFloat);

                // Odejmujemy najstarszą klatkę
                Core.subtract(sum, frames.get(i - windowSize + 1), sum);
            }
        }
        return resultFrames;
    }

    /**
     * Wczytuje obraz w formacie CV_8U (domyślnie BGR).
     * Zawiera przykład konwersji TIF -> PNG, jeśli to potrzebne.
     *
     * @param path  ścieżka do pliku
     * @return      obiekt Mat w CV_8U
     */
    private static Mat loadImage(String path) {
        Mat image = Imgcodecs.imread(path);
        if (!image.empty()) {
            return image;
        }

        // Przykład konwersji TIF -> PNG
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

        // Nie udało się wczytać
        throw new IllegalArgumentException("Cannot load image. Unsupported format or path: " + path);
    }

    /**
     * Zapisuje listę klatek 8-bitowych do pliku wideo (AVI).
     *
     * @param frames     lista klatek (już w CV_8U)
     * @param outputPath ścieżka zapisu (np. "C:/video/output.avi")
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

        // Wymiary klatek
        int width = frames.get(0).cols();
        int height = frames.get(0).rows();
        if (width <= 0 || height <= 0) {
            System.err.println("Invalid frame size: " + width + "x" + height);
            return;
        }

        Size frameSize = new Size(width, height);

        // Tworzymy VideoWriter (10 FPS, MJPG)
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

        // Zapis klatek
        for (Mat frame : frames) {
            writer.write(frame);
        }

        writer.release();
        System.out.println("Video saved at: " + outputPath);
    }
}
