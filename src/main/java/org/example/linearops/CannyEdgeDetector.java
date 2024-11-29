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

    // Metoda główna do wykrywania krawędzi za pomocą algorytmu Canny'ego
    public BufferedImage applyCanny(BufferedImage inputImage, double threshold1, double threshold2, int apertureSize, boolean l2Gradient) {
        // Konwersja obrazu BufferedImage na obiekt Mat (format OpenCV)
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Jeśli obraz jest kolorowy, konwertuj go na skalę szarości
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Tworzenie obiektu Mat, który przechowa wykryte krawędzie
        Mat edges = new Mat();

        // Wykonywanie operacji Canny'ego z podanymi parametrami
        Imgproc.Canny(sourceMat, edges, threshold1, threshold2, apertureSize, l2Gradient);

        // Konwersja wyniku (Mat) z powrotem na BufferedImage
        return matToBufferedImage(edges);
    }

    // Prywatna metoda pomocnicza: konwersja BufferedImage na Mat
    private Mat bufferedImageToMat(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Zapisanie obrazu do strumienia bajtów w formacie PNG
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();

            // Konwersja strumienia bajtów na obiekt Mat
            return Imgcodecs.imdecode(new MatOfByte(imageInBytes), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            // Obsługa błędów wejścia/wyjścia
            e.printStackTrace();
            return new Mat(); // Zwrócenie pustego obiektu Mat w przypadku błędu
        }
    }

    // Prywatna metoda pomocnicza: konwersja Mat na BufferedImage
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte mob = new MatOfByte();
        // Kodowanie obrazu Mat do formatu PNG
        Imgcodecs.imencode(".png", mat, mob);
        byte[] byteArray = mob.toArray();
        try {
            // Odczyt obrazu z tablicy bajtów do BufferedImage
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            // Obsługa błędów wejścia/wyjścia
            e.printStackTrace();
            return null; // Zwrócenie wartości null w przypadku błędu
        }
    }
}
