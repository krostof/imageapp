package org.example.segmentaionlab5;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Klasa realizująca segmentację (progowanie) za pomocą OpenCV:
 * - Double threshold (dwa progi)
 * - Otsu threshold
 * - Adaptive threshold
 *
 * Założenie: obraz wejściowy to BufferedImage typu TYPE_BYTE_GRAY (lub TYPE_3BYTE_BGR).
 * Ewentualnie konwertujemy do odcieni szarości, jeśli jest kolorowy.
 */
public class SegmentationProcessor {

    static {
        // Załaduj natywną bibliotekę OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Progowanie z dwoma progami p1, p2 (zwracamy obraz 3-wartościowy: <p1=0, p1..p2=127, >p2=255).
     */
    public BufferedImage doubleThreshold(BufferedImage input, int p1, int p2) {
        Mat src = bufferedImageToMat(input);

        Mat dst = new Mat(src.size(), CvType.CV_8UC1);
        for (int y = 0; y < src.rows(); y++) {
            for (int x = 0; x < src.cols(); x++) {
                double pixVal = src.get(y, x)[0];
                double newVal;
                if (pixVal < p1) {
                    newVal = 0;
                } else if (pixVal > p2) {
                    newVal = 255;
                } else {
                    newVal = 127; // lub 255, w zależności od definicji
                }
                dst.put(y, x, newVal);
            }
        }
        return matToBufferedImage(dst);
    }

    /**
     * Progowanie metodą Otsu - OpenCV automatycznie wylicza próg.
     * Zwracamy obraz binarny (0/255).
     */
    public BufferedImage otsuThreshold(BufferedImage input) {
        Mat src = bufferedImageToMat(input);
        Mat dst = new Mat();
        // threshold z flagą THRESH_OTSU
        Imgproc.threshold(src, dst, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        return matToBufferedImage(dst);
    }

    /**
     * Progowanie adaptacyjne (np. blockSize=11, C=2).
     * Zwraca obraz binarny (0/255).
     */
    public BufferedImage adaptiveThreshold(BufferedImage input, int blockSize, int C) {
        Mat src = bufferedImageToMat(input);
        Mat dst = new Mat();
        Imgproc.adaptiveThreshold(src, dst, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                blockSize,
                C
        );
        return matToBufferedImage(dst);
    }


    private Mat bufferedImageToMat(BufferedImage bi) {
        // Sprawdź, czy obraz jest w odcieniach szarości:
        if (bi.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            // W razie potrzeby konwertuj do GRAY:
            BufferedImage grayImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayImage.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi = grayImage;
        }
        // Konwersja do Mat:
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        BufferedImage out;
        if (mat.channels() == 1) {
            out = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
            byte[] data = new byte[mat.width() * mat.height()];
            mat.get(0, 0, data);
            out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        } else {
            // tu ewentualnie obsługa obrazu 3-kanałowego
            out = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
            byte[] data = new byte[mat.width() * mat.height() * 3];
            mat.get(0, 0, data);
            out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        }
        return out;
    }
}
