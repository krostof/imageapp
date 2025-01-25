package org.example.projectaverage;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.util.List;

public class VideoCreator {

    /**
     * Tworzy plik wideo z listy klatek w formacie.
     * @param frames lista klatek wideo
     * @param outputPath ścieżka docelowa pliku wideo
     */
    public void createVideo(List<Mat> frames, String outputPath) {
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

        VideoWriter writer = new VideoWriter(
                outputPath,
                VideoWriter.fourcc('M', 'J', 'P', 'G'),
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
