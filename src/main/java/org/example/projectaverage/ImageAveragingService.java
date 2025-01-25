package org.example.projectaverage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class ImageAveragingService {

    /**
     * Oblicza średni obraz ze wszystkich dostarczonych obrazów.
     */
    public Mat calculateOverallAverage(List<Mat> frames) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("No frames provided for averaging.");
        }
        // Macierz ktora ma przechowywać sumę wszystkich pixeli obrazów
        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());
        for (Mat frame : frames) {
            // Dodanie obrazu do sumy
            Core.add(sum, frame, sum);
        }
        // Macierz wynikowa przechowująca średnią z obrazów
        Mat average = new Mat();
        // Podzielenie sumy przez ilość obrazów
        Core.divide(sum, Scalar.all(frames.size()), average);
        return average;
    }

    /**
     * Oblicza średnią kroczącą na liście obrazów.
     */
    public List<Mat> calculateMovingAverage(List<Mat> frames, int windowSize) {
        // Lista wynikowych macierzy, które będą przechowywać obrazy średniej kroczącej
        List<Mat> resultFrames = new ArrayList<>();

        // Jeżeli lista obrazów jest pusta, nzwracana jest pusta lista
        if (frames.isEmpty()) {
            return resultFrames;
        }

        // Macierz która ma przechowywać sumę pikseli w bieżącym oknie
        Mat sum = Mat.zeros(frames.get(0).size(), frames.get(0).type());
        for (int i = 0; i < frames.size(); i++) {
            // Dodanie obrazu do sumy
            Core.add(sum, frames.get(i), sum);

            // Sprawdzenie warunku, czy została osiągnięta wielkość okna (windowSize)
            if (i >= windowSize - 1) {
                // Macierz przechowująca średnią z obrazów dla bieżącego okna
                Mat average = new Mat();
                // Podzielenie sumy przez wielkośc okna
                Core.divide(sum, Scalar.all(windowSize), average);
                // Dodanie wynikowego obrazu do listy wynikowej
                resultFrames.add(average);

                // Usunięcie najstarszego obraz z sumy, przesuwając okno
                Core.subtract(sum, frames.get(i - windowSize + 1), sum);
            }
        }
        // Zwrócenie listy wynikowych obrazów average
        return resultFrames;
    }
}
