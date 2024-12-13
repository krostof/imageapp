package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class PrewittEdgeDetector {

    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        // do skali szarosci jeśli obraz jest kolorowy
        if (sourceMat.channels() > 1) {
            if (sourceMat.channels() == 4) {
                // BGRA do skali szarości (obrazy z kanałem alfa)
                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGRA2GRAY);
            } else {
                // BGR do skali szarości
                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
            }
        }

        // CV_32F (32-bitowe liczby zmiennoprzecinkowe)
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // maska dla filtra poziomego Prewitta (Gx)
        Mat prewittKernelX = new Mat(3, 3, CvType.CV_32F);
        float[] dataX = {
                -1, 0, 1,
                -1, 0, 1,
                -1, 0, 1
        };
        prewittKernelX.put(0, 0, dataX);

        // maska dla filtra pionowego Prewitta (Gy)
        Mat prewittKernelY = new Mat(3, 3, CvType.CV_32F);
        float[] dataY = {
                -1, -1, -1,
                0,  0,  0,
                1,  1,  1
        };
        prewittKernelY.put(0, 0, dataY);

        // poziomy filtra Prewitta
        Mat gradX = new Mat();
        Imgproc.filter2D(sourceMat32F, gradX, CvType.CV_32F, prewittKernelX);

        // pionowy filtra Prewitta
        Mat gradY = new Mat();
        Imgproc.filter2D(sourceMat32F, gradY, CvType.CV_32F, prewittKernelY);

        // moduł gradientu
        Mat magnitude = new Mat();
        Core.magnitude(gradX, gradY, magnitude);

        // normalizacja
        Core.normalize(magnitude, magnitude, 0, 255, Core.NORM_MINMAX);

        // 8-bitow (CV_8U)
        Mat absMagnitude = new Mat();
        magnitude.convertTo(absMagnitude, CvType.CV_8U);

        return matToBufferedImage(absMagnitude);
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat;
        switch (bi.getType()) {
            case BufferedImage.TYPE_BYTE_GRAY:
                mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
                byte[] dataGray = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                mat.put(0, 0, dataGray);
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
                byte[] dataBGR = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                mat.put(0, 0, dataBGR);
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC4);
                byte[] dataABGR = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                byte[] dataBGRA = new byte[dataABGR.length];
                for (int i = 0; i < dataABGR.length; i += 4) {
                    dataBGRA[i]     = dataABGR[i + 3];
                    dataBGRA[i + 1] = dataABGR[i + 2];
                    dataBGRA[i + 2] = dataABGR[i + 1];
                    dataBGRA[i + 3] = dataABGR[i];
                }
                mat.put(0, 0, dataBGRA);
                break;
            default:
                BufferedImage converted = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                converted.getGraphics().drawImage(bi, 0, 0, null);
                return bufferedImageToMat(converted);
        }
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else if (mat.channels() == 4) {
            type = BufferedImage.TYPE_4BYTE_ABGR;
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.rows() * mat.cols() * mat.channels()];
        mat.get(0, 0, data);

        if (mat.channels() == 4) {
            byte[] dataABGR = new byte[data.length];
            for (int i = 0; i < data.length; i += 4) {
                dataABGR[i]     = data[i + 3];
                dataABGR[i + 1] = data[i + 2];
                dataABGR[i + 2] = data[i + 1];
                dataABGR[i + 3] = data[i];
            }
            image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dataABGR);
        } else {
            image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        }

        return image;
    }
}
