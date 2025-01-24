package org.example.segmentaionlab5;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 Opracować algorytm i uruchomić funkcjonalność wykonywania szkieletyzacji obiektu na mapie
 binarnej.
 */
public class SkeletonizationProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public BufferedImage skeletonize(BufferedImage binaryImage) {

        Mat src = bufferedImageToMatGray(binaryImage);
        Imgproc.threshold(src, src, 127, 255, Imgproc.THRESH_BINARY);

        Mat skeleton = Mat.zeros(src.size(), CvType.CV_8UC1);
        Mat temp = new Mat();
        Mat eroded = new Mat();

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));

        boolean done = false;
        while (!done) {
            // erozja
            Imgproc.erode(src, eroded, kernel);
            // dylacja erodowanego obrazu
            Imgproc.dilate(eroded, temp, kernel);
            Core.subtract(src, temp, temp);
            Core.bitwise_or(skeleton, temp, skeleton);

            // przekazanie erozji do kolejnej iteracji
            eroded.copyTo(src);

            // sprawdzanie zakończenia - flaga
            done = (Core.countNonZero(src) == 0);
        }

        return matToBufferedImage(skeleton);
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
        BufferedImage out = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[mat.width() * mat.height()];
        mat.get(0, 0, data);
        out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);
        return out;
    }
}
