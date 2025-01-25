package org.example.projectaverage;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAverageProcessor {

    private static ImageLoader imageLoader = new ImageLoader();
    private static VideoCreator videoCreator = new VideoCreator();
    private static ImageAveragingService averagingService = new ImageAveragingService();

    public ImageAverageProcessor() {
        this.imageLoader = new ImageLoader();
        this.videoCreator = new VideoCreator();
        this.averagingService = new ImageAveragingService();
    }

    /**
     * Przetwarza listę obrazów i zapisuje do wideo
     * w lokalizacji wskazanej przez użytkownika
     */
    public static String processImagesToCustomPath(List<File> imageFiles, int windowSize, String outputPath) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided for video creation.");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be greater than 0.");
        }
        if (outputPath == null || outputPath.isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty.");
        }
        /*
        Mat to podstawowy obiekt danych w OpenCV,
        który jest wykorzystywany do przechowywania obrazów,
        macierzy, oraz innych typów danych numerycznych.
         */
        List<Mat> floatFrames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image8U = imageLoader.loadImage(file.getAbsolutePath());
            Mat image32F = new Mat();
            int floatType = (image8U.channels() == 1) ? CvType.CV_32FC1 : CvType.CV_32FC3;
            image8U.convertTo(image32F, floatType);
            floatFrames.add(image32F);
        }

        // Format float (CV_32F) pozwala uniknąć przepełnienia wartości pikseli,
        // które może wystąpić podczas sumowania obrazów 8-bitowych.
        List<Mat> averagedFramesFloat = averagingService.calculateMovingAverage(floatFrames, windowSize);

        //  Konwersja każdej klatki do 8-bit i zapis do pliku wideo
        List<Mat> averagedFrames8U = new ArrayList<>();
        for (Mat floatFrame : averagedFramesFloat) {
            Mat frame8U = new Mat();
            floatFrame.convertTo(frame8U, CvType.CV_8U);
            averagedFrames8U.add(frame8U);
        }

        //  Tworzenie wideo
        videoCreator.createVideo(averagedFrames8U, outputPath);
        return outputPath;
    }

    /**
     * Oblicza obraz będący średnią ze wszystkich podanych plików i zapisuje go w
     * folderze pierwszego pliku jako 'overall_average.jpg'.
     */
    public static String calculateOverallAverage(List<File> imageFiles, String outputPath) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided for averaging.");
        }

        //  Wczytanie obrazów, konwersja do float
        List<Mat> floatFrames = new ArrayList<>();
        for (File file : imageFiles) {
            // Wczytanie obrazu
            Mat image8U = imageLoader.loadImage(file.getAbsolutePath());
            //Sprawdzenie, czy obraz został poprawnie wczytany
            if (image8U.empty()) {
                throw new RuntimeException("Could not read image: " + file.getAbsolutePath());
            }
            // Storzenie nowej macierzy image32F, która przechowuje obraz w formacie float (CV_32F)
            // CV_32FC1: Obraz jednokanałowy
            // CV_32FC3: Obraz trzykanałowy
            Mat image32F = new Mat();
            int floatType;
            if (image8U.channels() == 1) {
                floatType = CvType.CV_32FC1;
            } else {
                floatType = CvType.CV_32FC3;
            }

            // Konwersja obrazu 8-bitowego do float
            image8U.convertTo(image32F, floatType);
            floatFrames.add(image32F);
        }

        //  Wyliczenie uśrednionego obrazu w float
        Mat averageFloat = averagingService.calculateOverallAverage(floatFrames);

        //  Rzutowanie do 8-bit i zapis do pliku
        Mat average8U = new Mat();
        averageFloat.convertTo(average8U, CvType.CV_8U);

        // Zapis do wskazanej lokalizacji
        boolean success = Imgcodecs.imwrite(outputPath, average8U);
        if (!success) {
            throw new RuntimeException("Failed to save average image.");
        }

        return outputPath;
    }

}
