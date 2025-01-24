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
Implementacji detekcji krawędzi operatorem Cannyego.
 */
public class CannyEdgeDetector {

    /*
    * obraz
    * macierz Mat
    * dolny próg detekcji
    * górny próg detekcji
    * rozmiar jądra do obliczania gradientu
    * l2Gradient(pozycyjna norma L2/normaL1)
    * */

    public BufferedImage applyCanny(BufferedImage inputImage, double threshold1, double threshold2, int apertureSize, boolean l2Gradient) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        Mat edges = new Mat();

        Imgproc.Canny(sourceMat, edges, threshold1, threshold2, apertureSize, l2Gradient);

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
