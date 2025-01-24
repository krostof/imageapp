package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class LaplacianSharpeningProcessor {

    /**
     * Wyostrzanie Laplace'a w skali szarości.
     */
    public BufferedImage applyLaplacianSharpening(BufferedImage image,
                                                  int[][] mask,
                                                  int borderType,
                                                  int constantValue) {
        Mat matImage = bufferedImageToMat(image);

        Mat grayImage32F = new Mat();
        matImage.convertTo(grayImage32F, CvType.CV_32F);

        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                kernel.put(i, j, (float) mask[i][j]);
            }
        }

        Mat borderedImage = createBorders(grayImage32F, borderType, constantValue);

        Mat laplacianResult = new Mat();
        Imgproc.filter2D(borderedImage, laplacianResult, CvType.CV_32F, kernel);

        Mat croppedResult = cropToOriginalSize(laplacianResult, grayImage32F.size());

        Mat sharpenedImage = new Mat();
        Core.add(grayImage32F, croppedResult, sharpenedImage);

        Core.normalize(sharpenedImage, sharpenedImage, 0, 255, Core.NORM_MINMAX);

        Mat sharpenedImage8U = new Mat();
        sharpenedImage.convertTo(sharpenedImage8U, CvType.CV_8U);

        return matToBufferedImage(sharpenedImage8U);
    }

    /**
     * Tworzenie marginesów
     */
    private Mat createBorders(Mat inputMat, int borderType, int constantValue) {
        Mat borderedImage = new Mat();
        if (borderType == Core.BORDER_CONSTANT) {
            Scalar borderVal = new Scalar(constantValue);
            Core.copyMakeBorder(inputMat, borderedImage,
                    1, 1, 1, 1,
                    Core.BORDER_CONSTANT, borderVal);
        } else {
            Core.copyMakeBorder(inputMat, borderedImage,
                    1, 1, 1, 1,
                    borderType);
        }
        return borderedImage;
    }

    private Mat cropToOriginalSize(Mat largeMat, Size originalSz) {
        int x = 1, y = 1;
        int w = (int) originalSz.width;
        int h = (int) originalSz.height;
        Rect roi = new Rect(x, y, w, h);
        return new Mat(largeMat, roi);
    }


    private Mat bufferedImageToMat(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Input image is null.");
        }
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            gray.getGraphics().drawImage(image, 0, 0, null);
            image = gray;
        }
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            throw new IllegalArgumentException("Input Mat is null or empty.");
        }
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage out = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.rows() * mat.cols() * (int)mat.elemSize()];
        mat.get(0, 0, data);
        out.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return out;
    }
}
