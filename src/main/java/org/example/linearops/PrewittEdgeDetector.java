package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PrewittEdgeDetector {

    /**
     * Applies Prewitt edge detection using horizontal and vertical masks.
     *
     * @param inputImage BufferedImage input image
     * @return BufferedImage with edges detected
     */
    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage) {
        // Convert BufferedImage to Mat
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Convert to grayscale if necessary
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Convert Mat to CV_32F for filtering
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // Define Prewitt horizontal and vertical kernels
        Mat prewittHorizontal = new Mat(3, 3, CvType.CV_32F);
        prewittHorizontal.put(0, 0, -1, 0, 1);
        prewittHorizontal.put(1, 0, -1, 0, 1);
        prewittHorizontal.put(2, 0, -1, 0, 1);

        Mat prewittVertical = new Mat(3, 3, CvType.CV_32F);
        prewittVertical.put(0, 0, -1, -1, -1);
        prewittVertical.put(1, 0, 0, 0, 0);
        prewittVertical.put(2, 0, 1, 1, 1);

        // Apply the horizontal Prewitt filter
        Mat prewittHorizontalResult = new Mat();
        Imgproc.filter2D(sourceMat32F, prewittHorizontalResult, CvType.CV_32F, prewittHorizontal);

        // Apply the vertical Prewitt filter
        Mat prewittVerticalResult = new Mat();
        Imgproc.filter2D(sourceMat32F, prewittVerticalResult, CvType.CV_32F, prewittVertical);

        // Combine results using the magnitude of the gradient
        Mat magnitude = new Mat();
        Core.magnitude(prewittHorizontalResult, prewittVerticalResult, magnitude);

        // Convert to absolute values and scale to 8-bit
        Mat absMagnitude = new Mat();
        Core.convertScaleAbs(magnitude, absMagnitude);

        // Convert Mat back to BufferedImage
        return matToBufferedImage(absMagnitude);
    }

    /**
     * Converts a BufferedImage to an OpenCV Mat.
     *
     * @param bi BufferedImage
     * @return Mat
     */
    private Mat bufferedImageToMat(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();
            return Imgcodecs.imdecode(new MatOfByte(imageInBytes), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert BufferedImage to Mat", e);
        }
    }

    /**
     * Converts an OpenCV Mat to a BufferedImage.
     *
     * @param mat Mat
     * @return BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);
        byte[] byteArray = mob.toArray();
        try {
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Mat to BufferedImage", e);
        }
    }
}
