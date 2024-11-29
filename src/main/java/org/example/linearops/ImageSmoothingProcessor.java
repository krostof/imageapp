package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageSmoothingProcessor {

    static {
        // Ładowanie natywnej biblioteki OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public BufferedImage applySmoothing(BufferedImage inputImage, String method, int k) {
        // Konwersja obrazu BufferedImage na obiekt Mat (format OpenCV)
        Mat src = bufferedImageToMat(inputImage);
        Mat dst = new Mat();

        // Tworzenie jądra splotu na podstawie wybranej metody
        Mat kernel = createKernel(method, k);

        // Zastosowanie jądra splotu do obrazu źródłowego
        Imgproc.filter2D(src, dst, -1, kernel);

        // Konwersja wyniku operacji (Mat) na BufferedImage
        return matToBufferedImage(dst);
    }

    private Mat createKernel(String method, int k) {
        // Wybór metody wygładzania na podstawie parametru
        switch (method.toLowerCase()) {
            case "average":
                // Tworzenie jądra splotu dla średniej
                return createCustomAverageKernel();
            case "weighted_average":
                // Tworzenie jądra splotu dla ważonej średniej
                return createWeightedKernel(k);
            case "gaussian":
                // Tworzenie jądra splotu dla filtru Gaussa
                return createGaussianKernel();
            default:
                // Rzucenie wyjątku w przypadku nieznanej metody
                throw new IllegalArgumentException("Unknown smoothing method: " + method);
        }
    }

    private Mat createCustomAverageKernel() {
        // Tworzenie jądra splotu (3x3) dla średniej ważonej
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] maskValues = {
                0, 1, 0,
                1, 1, 1,
                0, 1, 0
        };
        float sum = 5; // Suma wszystkich wartości w masce
        for (int i = 0; i < maskValues.length; i++) {
            maskValues[i] /= sum; // Normalizacja wartości
        }
        kernel.put(0, 0, maskValues);
        return kernel;
    }

    private Mat createWeightedKernel(int k) {
        // Tworzenie jądra splotu dla metody "weighted_average"
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] weights = new float[]{
                1, 1, 1,
                1, k, 1,
                1, 1, 1
        };
        float sum = 8 + k; // Normalizacja wag
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }
        kernel.put(0, 0, weights);
        return kernel;
    }

    private Mat createGaussianKernel() {
        // Tworzenie jądra splotu dla filtru Gaussa (3x3)
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        float[] gaussianWeights = new float[]{
                1, 2, 1,
                2, 5, 2,
                1, 2, 1
        };
        float sum = 16; // Normalizacja wag
        for (int i = 0; i < gaussianWeights.length; i++) {
            gaussianWeights[i] /= sum;
        }
        kernel.put(0, 0, gaussianWeights);
        return kernel;
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        // Konwersja obiektu BufferedImage na Mat
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        // Konwersja obiektu Mat na BufferedImage
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage bi = new BufferedImage(mat.cols(), mat.rows(), type);
        mat.get(0, 0, ((DataBufferByte) bi.getRaster().getDataBuffer()).getData());
        return bi;
    }
}
