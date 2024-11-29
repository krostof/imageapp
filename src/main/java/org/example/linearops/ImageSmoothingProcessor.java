package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Class for performing smoothing operations on images using OpenCV.
 */
public class ImageSmoothingProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Apply smoothing to the input image based on the given method and kernel size.
     *
     * @param inputImage The input image as BufferedImage.
     * @param method     The smoothing method ("average", "weighted_average", "gaussian").
     * @param k          The weight for the weighted_average method (ignored for other methods).
     * @return The smoothed image as BufferedImage.
     */
    public BufferedImage applySmoothing(BufferedImage inputImage, String method, int k) {
        Mat src = bufferedImageToMat(inputImage);
        Mat dst = new Mat();

        // Generate kernel based on the method
        Mat kernel = createKernel(method, k);

        // Apply the kernel to the source image
        Imgproc.filter2D(src, dst, -1, kernel);

        return matToBufferedImage(dst);
    }

    /**
     * Creates the kernel (mask) based on the smoothing method and weight k.
     *
     * @param method The smoothing method ("average", "weighted_average", "gaussian").
     * @param k      The weight for the weighted_average method.
     * @return The generated kernel as Mat.
     */
    private Mat createKernel(String method, int k) {
        switch (method.toLowerCase()) {
            case "average":
                return createCustomAverageKernel(); // Custom kernel based on the image
            case "weighted_average":
                return createWeightedKernel(k);
            case "gaussian":
                return createGaussianKernel(); // Use a predefined Gaussian kernel
            default:
                throw new IllegalArgumentException("Unknown smoothing method: " + method);
        }
    }

    /**
     * Creates a custom average kernel based on the provided image.
     *
     * @return The custom average kernel as Mat.
     */
    private Mat createCustomAverageKernel() {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] maskValues = {
                0, 1, 0,
                1, 1, 1,
                0, 1, 0
        };
        float sum = 5; // Sum of all elements in the kernel
        for (int i = 0; i < maskValues.length; i++) {
            maskValues[i] /= sum; // Normalize the mask
        }
        kernel.put(0, 0, maskValues);
        return kernel;
    }

    /**
     * Creates a weighted kernel for the "weighted_average" method.
     *
     * @param k The weight for the center of the kernel.
     * @return The weighted kernel as Mat.
     */
    private Mat createWeightedKernel(int k) {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] weights = new float[]{
                1, 1, 1,
                1, k, 1,
                1, 1, 1
        };
        float sum = 8 + k; // Normalize weights
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }
        kernel.put(0, 0, weights);
        return kernel;
    }

    /**
     * Creates a predefined Gaussian kernel.
     *
     * @return The Gaussian kernel as Mat.
     */
    private Mat createGaussianKernel() {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] gaussianWeights = new float[]{
                1, 2, 1,
                2, 5, 2,
                1, 2, 1
        };
        float sum = 16; // Normalize weights
        for (int i = 0; i < gaussianWeights.length; i++) {
            gaussianWeights[i] /= sum;
        }
        kernel.put(0, 0, gaussianWeights);
        return kernel;
    }

    /**
     * Converts a BufferedImage to an OpenCV Mat.
     *
     * @param bi The input BufferedImage.
     * @return The corresponding Mat object.
     */
    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * Converts an OpenCV Mat to a BufferedImage.
     *
     * @param mat The input Mat object.
     * @return The corresponding BufferedImage.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage bi = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) bi.getRaster().getDataBuffer()).getData());
        return bi;
    }
}
