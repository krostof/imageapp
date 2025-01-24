package org.example.projectaverage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

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

        // Wczytywanie obrazów
        List<Mat> floatFrames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image8U = imageLoader.loadImage(file.getAbsolutePath());
            if (image8U.empty()) {
                throw new RuntimeException("Could not read image: " + file.getAbsolutePath());
            }
            int floatType = (image8U.channels() == 1) ? CvType.CV_32FC1 : CvType.CV_32FC3;
            Mat image32F = new Mat();
            image8U.convertTo(image32F, floatType);
            floatFrames.add(image32F);
        }

        // Przetwarzanie (moving average)
        List<Mat> processedFloatFrames = calculateMovingAverageFloat(floatFrames, windowSize);

        // Konwersja klatek do 8-bit
        List<Mat> processed8UFrames = new ArrayList<>();
        for (Mat floatFrame : processedFloatFrames) {
            Mat frame8U = new Mat();
            floatFrame.convertTo(frame8U, CvType.CV_8U);
            processed8UFrames.add(frame8U);
        }

        // Tworzenie wideo
        videoCreator.createVideo(processed8UFrames, outputPath);

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

        Mat sum32F = null;
        int imageCount = 0;

        // sumowanie obrazów
        for (File file : imageFiles) {
            Mat image8U = Imgcodecs.imread(file.getAbsolutePath());
            if (image8U.empty()) {
                System.err.println("Cannot read image: " + file.getAbsolutePath());
                continue;
            }

            // ustalanie, czy CV_32FC1 czy CV_32FC3
            int floatType = (image8U.channels() == 1) ? CvType.CV_32FC1 : CvType.CV_32FC3;
            Mat image32F = new Mat();
            image8U.convertTo(image32F, floatType);

            if (sum32F == null) {
                sum32F = Mat.zeros(image32F.size(), image32F.type());
            } else {
                // sprawdźanie, czy rozmiar/typ się zgadzają
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
}
