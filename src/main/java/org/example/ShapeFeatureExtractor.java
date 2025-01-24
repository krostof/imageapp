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
 * Zadanie 1.
 * Opracować algorytm i uruchomić funkcjonalność realizującą wyznaczanie następujących składowych
 * wektora cech obiektu binarnego:
 * a) Momenty
 * b) Pole powierzchni i obwód
 * c) Współczynniki kształtu: aspectRatio, extent, solidity, equivalentDiameter
 * Przygotować zapis wyników w postaci pliku tekstowego do wczytanie do oprogramowania Excel
 * Program przetestować na podstawowych figurach znakach graficznych (gwiazdka, wykrzyknik,
 * dwukropek, przecinek, średnik, itp.).
 */
public class ShapeFeatureExtractor {

    /**
     * Metoda wyszukuje największy kontur w obrazie binarnym.
     */
    public static MatOfPoint findLargestContour(Mat binaryImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(
                binaryImage,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        MatOfPoint largestContour = null;
        double maxArea = 0.0;

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
     *  - momenty
     *  - pole i obwód
     *  - aspectRatio, extent, solidity, equivalentDiameter
     */
    public static String calculateFeatures(Mat binaryImage) {
        MatOfPoint contour = findLargestContour(binaryImage);
        if (contour == null) {
            return "No contours found.";
        }


        Moments moments = Imgproc.moments(contour);

        Mat huMomentsMat = new Mat(1, 7, CvType.CV_64F);
        Imgproc.HuMoments(moments, huMomentsMat);
        double[] huMoments = new double[7];
        huMomentsMat.get(0, 0, huMoments);

        double area = Imgproc.contourArea(contour);
        double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);

        Rect boundingRect = Imgproc.boundingRect(contour);
        double aspectRatio = (double) boundingRect.width / boundingRect.height; // W/H

        double rectArea = boundingRect.width * boundingRect.height;
        double extent = (rectArea > 0) ? area / rectArea : 0.0;

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

        double equivalentDiameter = Math.sqrt(4 * area / Math.PI);

        StringBuilder result = new StringBuilder();

        result.append(String.format(
                "Area: %.4f, Perimeter: %.4f\n",
                area, perimeter
        ));
        result.append(String.format(
                "AspectRatio: %.4f, Extent: %.4f, Solidity: %.4f, EquivalentDiameter: %.4f\n",
                aspectRatio, extent, solidity, equivalentDiameter
        ));
        for (int i = 0; i < huMoments.length; i++) {
            result.append(
                    String.format("HuMoment[%d]: %.6e\n", i, huMoments[i])
            );
        }

        return result.toString();
    }

    /**
     * metoda do zapisu wyników do pliku.
     */
    public static void saveResultsToFile(String features, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            String[] lines = features.split("\\r?\\n");

            for (String line : lines) {
                String[] pairs = line.split(",");

                StringBuilder csvRow = new StringBuilder();
                for (int i = 0; i < pairs.length; i++) {
                    String pair = pairs[i].trim();
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        String name = parts[0].trim();
                        String value = parts[1].trim();
                        csvRow.append(name).append(",").append(value);
                    } else {
                        csvRow.append(pair);
                    }
                    if (i < pairs.length - 1) {
                        csvRow.append(",");
                    }
                }
                writer.write(csvRow.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveResultsToCsvFile(String features, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            String[] lines = features.split("\\r?\\n");

            for (String line : lines) {
                String[] pairs = line.split(",");

                StringBuilder csvRow = new StringBuilder();
                for (int i = 0; i < pairs.length; i++) {
                    String pair = pairs[i].trim();
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        String name = parts[0].trim();
                        String value = parts[1].trim();
                        csvRow.append(name).append(",").append(value);
                    } else {
                        csvRow.append(pair);
                    }
                    if (i < pairs.length - 1) {
                        csvRow.append(",");
                    }
                }
                writer.write(csvRow.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
