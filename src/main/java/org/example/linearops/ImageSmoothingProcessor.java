package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageSmoothingProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public BufferedImage applySmoothing(BufferedImage inputImage, String method, int k) {
        Mat src = bufferedImageToMat(inputImage);
        Mat dst = new Mat();

        Mat kernel = createKernel(method, k);

        Imgproc.filter2D(src, dst, -1, kernel);

        return matToBufferedImage(dst);
    }

    private Mat createKernel(String method, int k) {
        switch (method.toLowerCase()) {
            case "average":
                return createCustomAverageKernel();
            case "weighted_average":
                return createWeightedKernel(k);
            case "gaussian":
                return createGaussianKernel();
            default:
                throw new IllegalArgumentException("Unknown smoothing method: " + method);
        }
    }

    private Mat createCustomAverageKernel() {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] maskValues = {
                0, 1, 0,
                1, 1, 1,
                0, 1, 0
        };
        float sum = 5;
        for (int i = 0; i < maskValues.length; i++) {
            maskValues[i] /= sum;
        }
        kernel.put(0, 0, maskValues);
        return kernel;
    }

    private Mat createWeightedKernel(int k) {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] weights = new float[]{
                1, 1, 1,
                1, k, 1,
                1, 1, 1
        };
        float sum = 8 + k;
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }
        kernel.put(0, 0, weights);
        return kernel;
    }

    private Mat createGaussianKernel() {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] gaussianWeights = new float[]{
                1, 2, 1,
                2, 5, 2,
                1, 2, 1
        };
        float sum = 16;
        for (int i = 0; i < gaussianWeights.length; i++) {
            gaussianWeights[i] /= sum;
        }
        kernel.put(0, 0, gaussianWeights);
        return kernel;
    }

//    private Mat bufferedImageToMat(BufferedImage bi) {
//        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
//        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//        mat.put(0, 0, data);
//        return mat;
//    }
//
//    private BufferedImage matToBufferedImage(Mat mat) {
//        int type = BufferedImage.TYPE_BYTE_GRAY;
//        if (mat.channels() > 1) {
//            type = BufferedImage.TYPE_3BYTE_BGR;
//        }
//        BufferedImage bi = new BufferedImage(mat.cols(), mat.rows(), type);
//        mat.get(0, 0, ((DataBufferByte) bi.getRaster().getDataBuffer()).getData());
//        return bi;
//    }

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
