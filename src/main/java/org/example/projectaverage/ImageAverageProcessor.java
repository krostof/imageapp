package org.example.projectaverage;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAverageProcessor {

    private static final ImageLoader imageLoader = new ImageLoader();
    private static final VideoCreator videoCreator = new VideoCreator();
    private static final ImageAveragingService averagingService = new ImageAveragingService();

    /**
     * Przetwarza listę obrazów i zapisuje do wideo
     * w lokalizacji wskazanej przez użytkownika.
     */
    public static String processImagesToCustomPath(List<File> imageFiles, int windowSize, String outputPath) {
        validateInputs(imageFiles, windowSize, outputPath);

        // Konwersja obrazów do formatu float
        List<Mat> floatFrames = convertImagesToFloat(imageFiles);

        // Obliczenie średniej kroczącej
        List<Mat> averagedFramesFloat = averagingService.calculateMovingAverage(floatFrames, windowSize);

        // Konwersja każdej klatki z float do 8-bit
        List<Mat> averagedFrames8U = convertFramesTo8U(averagedFramesFloat);

        // Tworzenie wideo
        videoCreator.createVideo(averagedFrames8U, outputPath);
        return outputPath;
    }

    /**
     * Oblicza obraz będący średnią ze wszystkich podanych plików i umożliwia
     * zapisanie go w lokalizacji wybranej przez użytkownika.
     */
    public static String calculateOverallAverage(List<File> imageFiles, String outputPath) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided for averaging.");
        }

        // Konwersja obrazów do formatu float
        List<Mat> floatFrames = convertImagesToFloat(imageFiles);

        // Obliczenie uśrednionego obrazu
        Mat averageFloat = averagingService.calculateOverallAverage(floatFrames);

        // Rzutowanie do 8-bit
        Mat average8U = new Mat();
        averageFloat.convertTo(average8U, CvType.CV_8U);

        // Zapis do wskazanej lokalizacji
        boolean success = Imgcodecs.imwrite(outputPath, average8U);
        if (!success) {
            throw new RuntimeException("Failed to save average image.");
        }

        return outputPath;
    }

    /**
     * Konwertuje listę obrazów do formatu float (CV_32F).
     */
    private static List<Mat> convertImagesToFloat(List<File> imageFiles) {
        List<Mat> floatFrames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image8U = imageLoader.loadImage(file.getAbsolutePath());
            if (image8U.empty()) {
                throw new RuntimeException("Could not read image: " + file.getAbsolutePath());
            }
            Mat image32F = new Mat();
            int floatType;
            if (image8U.channels() == 1) {
                floatType = CvType.CV_32FC1;
            } else {
                floatType = CvType.CV_32FC3;
            }
            image8U.convertTo(image32F, floatType);
            floatFrames.add(image32F);
        }
        return floatFrames;
    }

    /**
     * Konwertuje listę obrazów float (CV_32F) do formatu 8-bitowego (CV_8U).
     */
    private static List<Mat> convertFramesTo8U(List<Mat> floatFrames) {
        List<Mat> frames8U = new ArrayList<>();
        for (Mat floatFrame : floatFrames) {
            Mat frame8U = new Mat();
            floatFrame.convertTo(frame8U, CvType.CV_8U);
            frames8U.add(frame8U);
        }
        return frames8U;
    }

    /**
     * Waliduje dane wejściowe dla funkcji przetwarzania obrazów.
     */
    private static void validateInputs(List<File> imageFiles, int windowSize, String outputPath) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided for processing.");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be greater than 0.");
        }
        if (outputPath == null || outputPath.isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty.");
        }
    }
}
