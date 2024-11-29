package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MedianFilterProcessor {

    public BufferedImage applyMedianFilter(BufferedImage inputImage, int kernelSize, int borderTypeCode) {
        // Konwersja BufferedImage do Mat
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Sprawdź, czy obraz jest w skali szarości, jeśli nie, konwertuj
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Dodanie marginesów
        Mat paddedMat = new Mat();
        Core.copyMakeBorder(sourceMat, paddedMat, kernelSize / 2, kernelSize / 2, kernelSize / 2, kernelSize / 2, borderTypeCode);

        // Aplikacja filtra medianowego
        Mat resultMat = new Mat();
        Imgproc.medianBlur(paddedMat, resultMat, kernelSize);

        // Konwersja Mat do BufferedImage
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
