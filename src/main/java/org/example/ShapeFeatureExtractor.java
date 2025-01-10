package org.example;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa obliczająca wektor cech obiektów binarnych:
 *  1) Moment zwykłe i HuMoments
 *  2) Pole powierzchni (area) i obwód (perimeter)
 *  3) Współczynniki kształtu: aspectRatio, extent, solidity, equivalentDiameter
 *
 * Informacje bazują na slajdach:
 *  - momenty (funkcja cv::moments)
 *  - współczynniki kształtu (większa czułość na zniekształcenia niż momenty,
 *    mają określone zakresy, np. 0.01–100.0 w zależności od definicji)
 */
public class ShapeFeatureExtractor {

    /**
     * Metoda wyszukuje największy kontur w obrazie binarnym.
     *
     * @param binaryImage Obraz binarny (0 - tło, >0 - obiekt)
     * @return Największy znaleziony kontur lub null, jeśli brak konturów
     */
    public static MatOfPoint findLargestContour(Mat binaryImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // Znajdowanie wszystkich konturów
        Imgproc.findContours(
                binaryImage,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        MatOfPoint largestContour = null;
        double maxArea = 0.0;

        // Szukamy konturu o największym polu
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }
        return largestContour;
    }

    /**
     * Metoda oblicza cechy kształtu obiektu binarnego:
     *  - momenty (zwykłe + HuMoments)
     *  - pole i obwód
     *  - aspectRatio, extent, solidity, equivalentDiameter
     *
     * @param binaryImage Obraz binarny (musi zawierać przynajmniej 1 obiekt)
     * @return Tekst z wynikami lub komunikat "No contours found." jeśli brak obiektów
     */
    public static String calculateFeatures(Mat binaryImage) {
        MatOfPoint contour = findLargestContour(binaryImage);
        if (contour == null) {
            return "No contours found.";
        }

        // ----- 1) Obliczanie momentów -----
        Moments moments = Imgproc.moments(contour);
        // Obliczanie Hu Moments
        Mat huMomentsMat = new Mat(1, 7, CvType.CV_64F);
        Imgproc.HuMoments(moments, huMomentsMat);
        double[] huMoments = new double[7];
        huMomentsMat.get(0, 0, huMoments);

        // ----- 2) Pole i obwód -----
        double area = Imgproc.contourArea(contour);
        double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);

        // ----- 3) Wyznaczanie współczynników kształtu -----

        // 3a) Bounding box i aspectRatio
        Rect boundingRect = Imgproc.boundingRect(contour);
        double aspectRatio = (double) boundingRect.width / boundingRect.height; // W/H

        // 3b) Extent (jaką część boundingRect wypełnia kontur)
        double rectArea = boundingRect.width * boundingRect.height;
        double extent = (rectArea > 0) ? area / rectArea : 0.0;

        // 3c) Solidity: area / hullArea
        // Wyznaczamy otoczkę wypukłą (convex hull)
        MatOfInt hullIndices = new MatOfInt();
        Imgproc.convexHull(contour, hullIndices);
        MatOfPoint hullPoints = new MatOfPoint();
        hullPoints.create((int) hullIndices.size().height, 1, CvType.CV_32SC2);

        for (int i = 0; i < hullIndices.size().height; i++) {
            int index = (int) hullIndices.get(i, 0)[0];
            double[] point = contour.get(index, 0);
            hullPoints.put(i, 0, point);
        }

        double hullArea = Imgproc.contourArea(hullPoints);
        double solidity = (hullArea > 0) ? area / hullArea : 0.0;

        // 3d) Equivalent Diameter
        // Średnica okręgu o polu równym polu konturu
        double equivalentDiameter = Math.sqrt(4 * area / Math.PI);

        // ----- 4) Formatowanie wyników -----
        StringBuilder result = new StringBuilder();

        // Pole, obwód
        result.append(String.format(
                "Area: %.4f, Perimeter: %.4f\n",
                area, perimeter
        ));
        // Współczynniki kształtu
        result.append(String.format(
                "AspectRatio: %.4f, Extent: %.4f, Solidity: %.4f, EquivalentDiameter: %.4f\n",
                aspectRatio, extent, solidity, equivalentDiameter
        ));
        // Moment Hu
        for (int i = 0; i < huMoments.length; i++) {
            result.append(
                    String.format("HuMoment[%d]: %.6e\n", i, huMoments[i])
            );
        }

        return result.toString();
    }

    /**
     * Metoda pomocnicza do zapisu wyników do pliku.
     *
     * @param features Ciąg znaków z wynikiem
     * @param filePath Ścieżka do pliku
     */
    public static void saveResultsToFile(String features, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(features);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
