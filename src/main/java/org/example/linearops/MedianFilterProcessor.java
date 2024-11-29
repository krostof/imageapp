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
        // Konwersja obiektu BufferedImage na Mat (format OpenCV)
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Jeśli obraz jest kolorowy, konwertuj go na skalę szarości
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Dodanie marginesów do obrazu w celu zapewnienia poprawnego działania filtra
        Mat paddedMat = new Mat();
        Core.copyMakeBorder(sourceMat, paddedMat, kernelSize / 2, kernelSize / 2, kernelSize / 2, kernelSize / 2, borderTypeCode);

        // Zastosowanie filtra medianowego
        Mat resultMat = new Mat();
        Imgproc.medianBlur(paddedMat, resultMat, kernelSize);

        // Konwersja wyniku z Mat na BufferedImage
        return matToBufferedImage(resultMat);
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        // Konwersja obiektu BufferedImage na Mat
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Zapis obrazu do strumienia bajtów w formacie PNG
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();

            // Odczyt strumienia bajtów jako obiekt Mat
            return Imgcodecs.imdecode(new MatOfByte(imageInBytes), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            // Obsługa błędów wejścia/wyjścia
            e.printStackTrace();
            return new Mat(); // Zwrócenie pustego obiektu Mat w przypadku błędu
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        // Konwersja obiektu Mat na BufferedImage
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);
        byte[] byteArray = mob.toArray();
        try {
            // Odczyt obrazu z tablicy bajtów jako BufferedImage
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            // Obsługa błędów wejścia/wyjścia
            e.printStackTrace();
            return null; // Zwrócenie wartości null w przypadku błędu
        }
    }
}
