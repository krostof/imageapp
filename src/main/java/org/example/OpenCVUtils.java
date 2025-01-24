package org.example;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class OpenCVUtils {

    public static Mat bufferedImageToMat(BufferedImage image) {
        int type = image.getType();
        Mat mat;

        if (type == BufferedImage.TYPE_BYTE_GRAY) {
            mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        } else {
            mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        }

        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);

        return mat;
    }

    public static BufferedImage matToBufferedImage(Mat mat) {
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            throw new IllegalArgumentException("Unsupported Mat channel count: " + mat.channels());
        }

        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        byte[] data = new byte[mat.channels() * mat.width() * mat.height()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);

        return image;
    }
}
