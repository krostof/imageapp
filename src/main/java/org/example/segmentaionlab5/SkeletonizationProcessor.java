package org.example.segmentaionlab5;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Klasa realizująca szkieletyzację (thinning) binarnych obiektów w Javie z użyciem OpenCV.
 * Założenie: wejściowy obraz to mapa binarna (0=czarny, 255=biały) w TYPE_BYTE_GRAY.
 */
public class SkeletonizationProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Szkieletyzacja obrazu binarnego.
     *
     * @param binaryImage Obraz wejściowy (binarna mapa obiektów, 0 = czarny, 255 = biały).
     * @return Obraz zawierający szkielet obiektów.
     */
    public BufferedImage skeletonize(BufferedImage binaryImage) {
        // Binaryzacja obrazu (upewniamy się, że obraz jest binarny)
        Mat src = bufferedImageToMatGray(binaryImage);
        Imgproc.threshold(src, src, 127, 255, Imgproc.THRESH_BINARY);

        // Inicjalizacja macierzy
        Mat skeleton = Mat.zeros(src.size(), CvType.CV_8UC1);
        Mat temp = new Mat();
        Mat eroded = new Mat();

        // Testowanie różnych elementów strukturalnych
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
        // Alternatywnie:
        // Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        // Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));

        boolean done = false;
        while (!done) {
            // Erozja
            Imgproc.erode(src, eroded, kernel);
            // Dylacja erodowanego obrazu
            Imgproc.dilate(eroded, temp, kernel);
            // Różnica między src a temp -> część szkieletu
            Core.subtract(src, temp, temp);
            Core.bitwise_or(skeleton, temp, skeleton);

            // Przekazanie erozji do kolejnej iteracji
            eroded.copyTo(src);

            // Sprawdzanie zakończenia
            done = (Core.countNonZero(src) == 0);
        }

        return matToBufferedImage(skeleton);
    }

    /**
     * Konwersja BufferedImage do Mat w skali szarości.
     *
     * @param bi Obraz wejściowy.
     * @return Obraz w formacie Mat.
     */
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

    /**
     * Konwersja Mat do BufferedImage w skali szarości.
     *
     * @param mat Obraz wejściowy w formacie Mat.
     * @return Obraz w formacie BufferedImage.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        BufferedImage out = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[mat.width() * mat.height()];
        mat.get(0, 0, data);
        out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        return out;
    }
}
