package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageSmoothingProcessor {

    private final BorderFillProcessor borderFillProcessor;

    public ImageSmoothingProcessor(BorderFillProcessor borderFillProcessor) {
        this.borderFillProcessor = borderFillProcessor;
    }

    public BufferedImage applySmoothing(BufferedImage inputImage, String method, int k, int borderType, int constantValue) {
        // Dodanie marginesów przy użyciu BorderFillProcessor
        BufferedImage imageWithBorders = borderFillProcessor.applyBorderFill(inputImage, borderType, constantValue);

        // Konwersja BufferedImage do Mat
        Mat src = bufferedImageToMat(imageWithBorders);
        Mat dst = new Mat();

        // Tworzenie jądra na podstawie wybranej metody
        Mat kernel;
        if ("gaussian".equalsIgnoreCase(method)) {
            // W przypadku filtra gaussowskiego używamy Imgproc.GaussianBlur zamiast filter2D
            Imgproc.GaussianBlur(src, dst, new Size(3, 3), 0);
        } else {
            kernel = createKernel(method, k);
            // Zastosowanie filtra wygładzającego
            Imgproc.filter2D(src, dst, -1, kernel);
        }

        // Konwersja wyniku z powrotem do BufferedImage
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
            default:
                throw new IllegalArgumentException("Unsupported image type: " + bi.getType());
        }
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.rows() * mat.cols() * mat.channels()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
