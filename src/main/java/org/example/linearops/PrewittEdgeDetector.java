package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Class for detecting edges using the Prewitt operator.
 */
public class PrewittEdgeDetector {

    /**
     * Applies Prewitt edge detection using standard Prewitt masks.
     *
     * @param inputImage BufferedImage input image
     * @return BufferedImage with edges detected
     */
    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage) {
        // Convert BufferedImage to Mat
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Convert to grayscale if necessary
        if (sourceMat.channels() > 1) {
            if (sourceMat.channels() == 4) {
                // If image has alpha channel
                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGRA2GRAY);
            } else {
                Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
            }
        }

        // Convert image to CV_32F for filtering
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // Define Prewitt horizontal kernel (Gx)
        Mat prewittKernelX = new Mat(3, 3, CvType.CV_32F);
        float[] dataX = {
                -1, 0, 1,
                -1, 0, 1,
                -1, 0, 1
        };
        prewittKernelX.put(0, 0, dataX);

        // Define Prewitt vertical kernel (Gy)
        Mat prewittKernelY = new Mat(3, 3, CvType.CV_32F);
        float[] dataY = {
                -1, -1, -1,
                0,  0,  0,
                1,  1,  1
        };
        prewittKernelY.put(0, 0, dataY);

        // Apply the horizontal Prewitt filter
        Mat gradX = new Mat();
        Imgproc.filter2D(sourceMat32F, gradX, CvType.CV_32F, prewittKernelX);

        // Apply the vertical Prewitt filter
        Mat gradY = new Mat();
        Imgproc.filter2D(sourceMat32F, gradY, CvType.CV_32F, prewittKernelY);

        // Compute the magnitude of the gradient
        Mat magnitude = new Mat();
        Core.magnitude(gradX, gradY, magnitude);

        // Normalize the result to the range 0-255
        Core.normalize(magnitude, magnitude, 0, 255, Core.NORM_MINMAX);

        // Convert to 8-bit image
        Mat absMagnitude = new Mat();
        magnitude.convertTo(absMagnitude, CvType.CV_8U);

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
                // Convert from ABGR to BGRA
                byte[] dataBGRA = new byte[dataABGR.length];
                for (int i = 0; i < dataABGR.length; i += 4) {
                    dataBGRA[i]     = dataABGR[i + 3]; // Blue
                    dataBGRA[i + 1] = dataABGR[i + 2]; // Green
                    dataBGRA[i + 2] = dataABGR[i + 1]; // Red
                    dataBGRA[i + 3] = dataABGR[i];     // Alpha
                }
                mat.put(0, 0, dataBGRA);
                break;
            default:
                // Convert to a known format
                BufferedImage converted = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                converted.getGraphics().drawImage(bi, 0, 0, null);
                return bufferedImageToMat(converted);
        }
        return mat;
    }

    /**
     * Converts an OpenCV Mat to a BufferedImage.
     *
     * @param mat Mat
     * @return BufferedImage
     */
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
            // Convert from BGRA to ABGR
            byte[] dataABGR = new byte[data.length];
            for (int i = 0; i < data.length; i += 4) {
                dataABGR[i]     = data[i + 3]; // Alpha
                dataABGR[i + 1] = data[i + 2]; // Red
                dataABGR[i + 2] = data[i + 1]; // Green
                dataABGR[i + 3] = data[i];     // Blue
            }
            image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dataABGR);
        } else {
            image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        }

        return image;
    }
}
