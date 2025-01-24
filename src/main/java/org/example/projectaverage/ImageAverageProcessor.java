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

        // Wczytanie obrazów jako float
        List<Mat> floatFrames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image = imageLoader.loadImage(file.getAbsolutePath());
            Mat floatImage = new Mat();
            int floatType = (image.channels() == 1) ? CvType.CV_32FC1 : CvType.CV_32FC3;
            image.convertTo(floatImage, floatType);
            floatFrames.add(floatImage);
        }

        // Obliczenie ruchomego uśredniania
        List<Mat> averagedFrames = averagingService.calculateMovingAverage(floatFrames, windowSize);

        // Konwersja do 8-bit
        List<Mat> resultFrames = new ArrayList<>();
        for (Mat floatFrame : averagedFrames) {
            Mat frame8U = new Mat();
            floatFrame.convertTo(frame8U, CvType.CV_8U);
            resultFrames.add(frame8U);
        }

        // Tworzenie wideo
        videoCreator.createVideo(resultFrames, outputPath);
        return outputPath;
    }

    public static String calculateOverallAverage(List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No image files provided for averaging.");
        }

        List<Mat> frames = new ArrayList<>();
        for (File file : imageFiles) {
            Mat image = imageLoader.loadImage(file.getAbsolutePath());
            frames.add(image);
        }

        Mat average = averagingService.calculateOverallAverage(frames);

        String outputPath = imageFiles.get(0).getParent() + "/overall_average.jpg";
        boolean success = Imgcodecs.imwrite(outputPath, average);
        if (!success) {
            throw new RuntimeException("Failed to save average image.");
        }

        return outputPath;
    }
}
