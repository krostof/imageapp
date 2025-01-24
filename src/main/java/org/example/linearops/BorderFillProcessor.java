package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
/*
Proszę zaimplementować wybór sposobu uzupełnienie marginesów/brzegów w operacjach
sąsiedztwa według zasady wybranej spośród następujących zasad:
• wypełnienie ramki wybraną wartością stałą n narzuconą przez użytkownika:
BORDER_CONSTANT
• wypełnienie wyniku wybraną wartością stałą n narzuconą przez użytkownika
• wyliczenie ramki według BORDER_REFLECT
 */
public class BorderFillProcessor {

    // Wypełnienia marginesów i filtracji
    public Mat applyFilterWithBorder(Mat inputMat, Mat kernel, int borderType, int constantValue) {
        Mat extendedMat = new Mat();

        if (borderType == Core.BORDER_CONSTANT) {
            Scalar borderValue = new Scalar(constantValue);
            Core.copyMakeBorder(inputMat, extendedMat, 1, 1, 1, 1, Core.BORDER_CONSTANT, borderValue);
        } else {
            Core.copyMakeBorder(inputMat, extendedMat, 1, 1, 1, 1, borderType);
        }

        Mat outputMat = new Mat();
        Imgproc.filter2D(extendedMat, outputMat, -1, kernel);

        Rect roi = new Rect(1, 1, inputMat.cols(), inputMat.rows());
        return new Mat(outputMat, roi);
    }

    public BufferedImage applyBorderFill(BufferedImage inputImage, int borderType, int constantValue) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        Mat resultMat = new Mat();
        int top = 10, bottom = 10, left = 10, right = 10;

        Scalar borderValue = new Scalar(constantValue);

        switch (borderType) {
            case Core.BORDER_CONSTANT:
                Core.copyMakeBorder(sourceMat, resultMat, top, bottom, left, right, Core.BORDER_CONSTANT, borderValue);
                break;
            case Core.BORDER_REFLECT:
                Core.copyMakeBorder(sourceMat, resultMat, top, bottom, left, right, Core.BORDER_REFLECT);
                break;
            case Core.BORDER_REPLICATE:
                Core.copyMakeBorder(sourceMat, resultMat, top, bottom, left, right, Core.BORDER_REPLICATE);
                break;
            default:
                throw new IllegalArgumentException("Invalid border type");
        }

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
