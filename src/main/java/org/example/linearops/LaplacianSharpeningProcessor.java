package org.example.linearops;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class LaplacianSharpeningProcessor {

    public BufferedImage applyLaplacianSharpening(BufferedImage image, int[][] mask) {
        // Konwersja obrazu BufferedImage na obiekt Mat
        Mat matImage = bufferedImageToMat(image);
        Mat grayImage = new Mat();
        Mat laplacianResult = new Mat();

        // Jeśli obraz jest kolorowy, konwertuj go na skalę szarości
        if (matImage.channels() > 1) {
            Imgproc.cvtColor(matImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayImage = matImage.clone(); // Klonowanie obrazu, jeśli jest już w skali szarości
        }

        // Konwersja obrazu w skali szarości do formatu CV_32F (32-bitowe liczby zmiennoprzecinkowe)
        Mat grayImage32F = new Mat();
        grayImage.convertTo(grayImage32F, CvType.CV_32F);

        // Tworzenie macierzy maski na podstawie przekazanego parametru mask
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                kernel.put(i, j, (float) mask[i][j]);
            }
        }

        // Zastosowanie filtra Laplace'a przy użyciu macierzy maski
        Imgproc.filter2D(grayImage32F, laplacianResult, CvType.CV_32F, kernel);

        // Dodanie wyniku Laplace'a do oryginalnego obrazu, co powoduje jego wyostrzenie
        Mat sharpenedImage = new Mat();
        Core.add(grayImage32F, laplacianResult, sharpenedImage);

        // Normalizacja wartości pikseli do zakresu 0-255
        Core.normalize(sharpenedImage, sharpenedImage, 0, 255, Core.NORM_MINMAX);

        // Konwersja obrazu wyostrzonego do formatu 8-bitowego (CV_8U)
        sharpenedImage.convertTo(sharpenedImage, CvType.CV_8U);

        // Konwersja obrazu Mat z powrotem na BufferedImage
        return matToBufferedImage(sharpenedImage);
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        // Konwersja BufferedImage do Mat (tylko w skali szarości)
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (image.getType() != type) {
            // Jeśli obraz nie jest w skali szarości, konwertuj go
            BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), type);
            grayImage.getGraphics().drawImage(image, 0, 0, null);
            image = grayImage;
        }
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        // Konwersja Mat do BufferedImage (tylko w skali szarości)
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.cols() * mat.rows() * (int) mat.elemSize()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
