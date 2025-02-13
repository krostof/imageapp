package org.example.segmentaionlab5;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 Opracować algorytm i uruchomić funkcjonalność wykonywania podstawowych operacji
 morfologii matematycznej: erozji, dylacji, otwarcia i zamknięcia wykorzystując następujące
 elementy strukturalne 3x3 (tam gdzie możliwe jest rozróżnienie kształtu oddzielne dla krzyża i
 prostokąta).
 */
public class MorphologyProcessor {

    static {
        // Załadowanie natywnej biblioteki OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * erozja na obrazie wejściowym, używając elementu strukturalnego 3x3.
     */
    public BufferedImage erode(BufferedImage input, String shape) {
        Mat src = bufferedImageToMatGray(input);
        Mat kernel = createStructuringElement3x3(shape);
        Mat dst = new Mat();
        Imgproc.erode(src, dst, kernel);
        return matToBufferedImage(dst);
    }

    /**
     * Wykonuje dylację na obrazie wejściowym, używając elementu strukturalnego 3x3.
     */
    public BufferedImage dilate(BufferedImage input, String shape) {
        Mat src = bufferedImageToMatGray(input);
        Mat kernel = createStructuringElement3x3(shape);
        Mat dst = new Mat();
        Imgproc.dilate(src, dst, kernel);
        return matToBufferedImage(dst);
    }

    /**
     * Otwarcie = erozja + dylacja
     */
    public BufferedImage opening(BufferedImage input, String shape) {
        Mat src = bufferedImageToMatGray(input);
        Mat kernel = createStructuringElement3x3(shape);
        Mat dst = new Mat();
        // Morphological opening
        Imgproc.morphologyEx(src, dst, Imgproc.MORPH_OPEN, kernel);
        return matToBufferedImage(dst);
    }

    /**
     * Zamknięcie = dylacja + erozja
     */
    public BufferedImage closing(BufferedImage input, String shape) {
        Mat src = bufferedImageToMatGray(input);
        Mat kernel = createStructuringElement3x3(shape);
        Mat dst = new Mat();
        // Morphological closing
        Imgproc.morphologyEx(src, dst, Imgproc.MORPH_CLOSE, kernel);
        return matToBufferedImage(dst);
    }

    /**
     * Tworzy element strukturalny 3x3 w kształcie 'rectangle' (3x3 jedynek) lub 'cross' (3x3 krzyż).
     */
    private Mat createStructuringElement3x3(String shape) {
        if (shape == null) shape = "rectangle"; // Domyślnie prostokąt
        shape = shape.toLowerCase();

        if (shape.contains("cross")) {
            return Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
        } else {
            return Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        }
    }




    private Mat bufferedImageToMatGray(BufferedImage bi) {
        if (bi.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage gray = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = gray.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi = gray;
        }
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
            mat.get(0,0, data);
            out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        } else {
            // In case it's 3 channels
            out = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
            byte[] data = new byte[mat.width() * mat.height() * 3];
            mat.get(0,0, data);
            out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        }
        return out;
    }
}
