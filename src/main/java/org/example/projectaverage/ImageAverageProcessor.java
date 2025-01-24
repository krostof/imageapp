package org.example.projectaverage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAverageProcessor {

    public static String processImages(List<File> imageFiles, int windowSize) {
        List<Mat> frames = new ArrayList<>();
        for (File imageFile : imageFiles) {
            Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
            frames.add(image);
        }

        List<Mat> processedFrames = calculateMovingAverage(frames, windowSize);
        String outputPath = imageFiles.get(0).getParent() + "/output_video.avi";
        createVideo(processedFrames, outputPath);
        return outputPath;
    }

    public static String calculateOverallAverage(List<File> imageFiles) {
        Mat sum = null;

        for (File file : imageFiles) {
            Mat image = Imgcodecs.imread(file.getAbsolutePath());
            if (sum == null) {
                sum = Mat.zeros(image.size(), image.type());
            }
            Core.add(sum, image, sum);
        }

        if (sum != null) {
            Mat average = new Mat();
            Core.divide(sum, Scalar.all(imageFiles.size()), average);
            String outputPath = imageFiles.get(0).getParent() + "/overall_average.jpg";
            Imgcodecs.imwrite(outputPath, average);
            return outputPath;
        }

        return "Failed to calculate overall average.";
    }

    private static List<Mat> calculateMovingAverage(List<Mat> frames, int windowSize) {
        List<Mat> resultFrames = new ArrayList<>();
        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());

        for (int i = 0; i < frames.size(); i++) {
            Core.add(sum, frames.get(i), sum);

            // Once we have 'windowSize' frames summed, create an average frame
            if (i >= windowSize - 1) {
                Mat average = calculateAverage(sum, windowSize);
                resultFrames.add(average);

                // Subtract the oldest frame from the sum to move the window forward
                Core.subtract(sum, frames.get(i - windowSize + 1), sum);
            }
        }
        return resultFrames;
    }

    private static Mat calculateAverage(Mat sum, int windowSize) {
        Mat average = new Mat();
        Core.divide(sum, Scalar.all(windowSize), average);
        return average;
    }

    private static void createVideo(List<Mat> frames, String outputPath) {
        Size frameSize = new Size(frames.get(0).cols(), frames.get(0).rows());
        // Example uses MJPG with 10 fps; adjust as needed
        VideoWriter videoWriter = new VideoWriter(
                outputPath,
                VideoWriter.fourcc('M', 'J', 'P', 'G'),
                10,
                frameSize,
                true
        );

        if (!videoWriter.isOpened()) {
            System.out.println("Failed to open video writer.");
            return;
        }

        for (Mat frame : frames) {
            videoWriter.write(frame);
        }

        videoWriter.release();
        System.out.println("Video saved at " + outputPath);
    }
}
