package org.example.linearops;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BorderFillProcessor {

    // różne rodzaje wypełnienia marginesów do obrazu
    public BufferedImage applyBorderFill(BufferedImage inputImage, int borderType, int constantValue) {
        // BufferedImage do formatu Mat (używanego przez OpenCV)
        Mat sourceMat = bufferedImageToMat(inputImage);

        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        Mat resultMat = new Mat();

        int top = 10, bottom = 10, left = 10, right = 10;

        Scalar borderValue = new Scalar(constantValue);

        switch (borderType) {
            case Core.BORDER_CONSTANT:
                // Wypełnienie stałą wartością
                Core.copyMakeBorder(sourceMat, resultMat, top, bottom, left, right, Core.BORDER_CONSTANT, borderValue);
                break;

            case Core.BORDER_REFLECT:
                // Odbicie pikseli na brzegach
                Core.copyMakeBorder(sourceMat, resultMat, top, bottom, left, right, Core.BORDER_REFLECT);
                break;

            case Core.BORDER_REPLICATE:
                // Powielenie pikseli na brzegach
                Core.copyMakeBorder(sourceMat, resultMat, top, bottom, left, right, Core.BORDER_REPLICATE);
                break;

            default:
                throw new IllegalArgumentException("Invalid border type");
        }

        // Konwersja obiektu Mat z powrotem do BufferedImage
        return matToBufferedImage(resultMat);
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();

            return Imgcodecs.imdecode(new MatOfByte(imageInBytes), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            e.printStackTrace();
            return new Mat();
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);
        byte[] byteArray = mob.toArray();
        try {
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
