package org.example.segmentaionlab5;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 Opracować algorytm i uruchomić funkcjonalność realizującą segmentację obrazów
 następującymi metodami:
 – Implementacja progowanie z dwoma progami wyznaczonymi przez użytkownika.
 – Implementacja progowanie z progiem wyznaczonym metodą Otsu,
 – Implementacja progowanie adaptacyjnego (adaptive threshold).
 */
public class SegmentationProcessor {

    /**
     * Progowanie z dwoma progami p1, p2
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
                    newVal = 127;
                }
                dst.put(y, x, newVal);
            }
        }
        return matToBufferedImage(dst);
    }

    /**
     * Progowanie Otsu (automatyczne wyznaczenie progu).
     */
    public BufferedImage otsuThreshold(BufferedImage input) {
        Mat src = bufferedImageToMat(input);
        Mat dst = new Mat();
        Imgproc.threshold(src, dst, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        return matToBufferedImage(dst);
    }

    /**
     * Progowanie adaptacyjne (z wykorzystaniem średniej ważonej).
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
