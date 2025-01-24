package org.example.projectaverage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class ImageAveragingService {

    /**
     * Oblicza średni obraz ze wszystkich dostarczonych *floatowych* obrazów.
     * Zakładamy, że klatki są w CV_32F (1 lub 3 kanały).
     */
    public Mat calculateOverallAverage(List<Mat> frames) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("No frames provided for averaging.");
        }

        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());
        for (Mat frame : frames) {
            Core.add(sum, frame, sum);
        }

        Mat average = new Mat();
        Core.divide(sum, Scalar.all(frames.size()), average);
        return average; // pozostaje w float
    }

    /**
     * Oblicza ruchome uśrednianie (moving average) na liście *floatowych* obrazów.
     * frames -> CV_32F
     */
    public List<Mat> calculateMovingAverage(List<Mat> frames, int windowSize) {
        List<Mat> resultFrames = new ArrayList<>();
        if (frames.isEmpty()) {
            return resultFrames;
        }

        // Tworzymy 'sum' w tym samym formacie float
        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());

        for (int i = 0; i < frames.size(); i++) {
            Core.add(sum, frames.get(i), sum);

            if (i >= windowSize - 1) {
                Mat average = new Mat();
                Core.divide(sum, Scalar.all(windowSize), average);
                resultFrames.add(average);

                Core.subtract(sum, frames.get(i - windowSize + 1), sum);
            }
        }
        return resultFrames;
    }
}
