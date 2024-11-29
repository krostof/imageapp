package org.example.linearops;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageSmoothingProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    // Wygładzanie uśredniające
    public BufferedImage applyAverageSmoothing(BufferedImage inputImage, int kernelSize) {
        Mat src = bufferedImageToMat(inputImage);
        Mat dst = new Mat();
        Size kernel = new Size(kernelSize, kernelSize);
        Imgproc.blur(src, dst, kernel); // Funkcja uśredniania OpenCV
        return matToBufferedImage(dst);
    }

    // Wygładzanie Gaussa
    public BufferedImage applyGaussianSmoothing(BufferedImage inputImage, int kernelSize) {
        Mat src = bufferedImageToMat(inputImage);
        Mat dst = new Mat();
        Size kernel = new Size(kernelSize, kernelSize);
        Imgproc.GaussianBlur(src, dst, kernel, 0); // Gaussowskie wygładzanie OpenCV
        return matToBufferedImage(dst);
    }

    // Wygładzanie uśredniające z wagami (np. maska medianowa)
    public BufferedImage applyMedianSmoothing(BufferedImage inputImage, int kernelSize) {
        Mat src = bufferedImageToMat(inputImage);
        Mat dst = new Mat();
        Imgproc.medianBlur(src, dst, kernelSize); // Medianowe wygładzanie OpenCV
        return matToBufferedImage(dst);
    }

    // Pomocnicza metoda do konwersji BufferedImage -> Mat
    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    // Pomocnicza metoda do konwersji Mat -> BufferedImage
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
