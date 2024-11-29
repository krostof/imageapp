package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CannyEdgeDetector {

    public BufferedImage applyCanny(BufferedImage inputImage, double threshold1, double threshold2, int apertureSize, boolean l2Gradient) {
        // Konwersja BufferedImage do Mat
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Konwertuj obraz na skalę szarości, jeśli jest w kolorze
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Aplikacja operatora Canny'ego
        Mat edges = new Mat();
        Imgproc.Canny(sourceMat, edges, threshold1, threshold2, apertureSize, l2Gradient);

        // Konwersja Mat do BufferedImage
        return matToBufferedImage(edges);
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
