package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class PrewittEdgeDetector {

    private final BorderFillProcessor borderFillProcessor;

    public PrewittEdgeDetector(BorderFillProcessor borderFillProcessor) {
        this.borderFillProcessor = borderFillProcessor;
    }

    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage, int borderType, int constantValue) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Konwersja do skali szarości, jeśli obraz jest kolorowy
        if (sourceMat.channels() > 1) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // CV_32F (32-bitowe liczby zmiennoprzecinkowe)
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // Maska dla filtra poziomego Prewitta (Gx)
        Mat prewittKernelX = new Mat(3, 3, CvType.CV_32F);
        prewittKernelX.put(0, 0, new float[]{
                -1, 0, 1,
                -1, 0, 1,
                -1, 0, 1
        });

        // Maska dla filtra pionowego Prewitta (Gy)
        Mat prewittKernelY = new Mat(3, 3, CvType.CV_32F);
        prewittKernelY.put(0, 0, new float[]{
                -1, -1, -1,
                0,  0,  0,
                1,  1,  1
        });

        // Wykorzystanie BorderFillProcessor dla obu filtrów
        Mat gradX = borderFillProcessor.applyFilterWithBorder(sourceMat32F, prewittKernelX, borderType, constantValue);
        Mat gradY = borderFillProcessor.applyFilterWithBorder(sourceMat32F, prewittKernelY, borderType, constantValue);

        // Moduł gradientu
        Mat magnitude = new Mat();
        Core.magnitude(gradX, gradY, magnitude);

        // Normalizacja
        Core.normalize(magnitude, magnitude, 0, 255, Core.NORM_MINMAX);

        // 8-bitow (CV_8U)
        Mat absMagnitude = new Mat();
        magnitude.convertTo(absMagnitude, CvType.CV_8U);

        return matToBufferedImage(absMagnitude);
    }


    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.rows() * mat.cols() * mat.channels()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
