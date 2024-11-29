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

    // Metoda główna, która stosuje różne rodzaje wypełnienia marginesów do obrazu
    public BufferedImage applyBorderFill(BufferedImage inputImage, int borderType, int constantValue) {
        // Konwersja obiektu BufferedImage do formatu Mat (używanego przez OpenCV)
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Sprawdzenie, czy obraz jest w skali szarości; jeśli nie, wykonanie konwersji
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Tworzenie nowego obiektu Mat, który będzie zawierał wynik operacji
        Mat resultMat = new Mat();

        // Definiowanie wielkości marginesów do wypełnienia
        int top = 10, bottom = 10, left = 10, right = 10;

        // Tworzenie wartości wypełnienia dla typu BORDER_CONSTANT
        Scalar borderValue = new Scalar(constantValue);

        // Obsługa różnych typów wypełnienia marginesów
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
                // Wyrzucenie wyjątku, jeśli podano niepoprawny typ marginesu
                throw new IllegalArgumentException("Invalid border type");
        }

        // Konwersja obiektu Mat z powrotem do BufferedImage
        return matToBufferedImage(resultMat);
    }

    // Prywatna metoda pomocnicza: konwertuje BufferedImage do obiektu Mat
    private Mat bufferedImageToMat(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Zapis obrazu do strumienia bajtów w formacie PNG
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();

            // Odczyt strumienia bajtów do obiektu Mat za pomocą OpenCV
            return Imgcodecs.imdecode(new MatOfByte(imageInBytes), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            // Obsługa błędów wejścia/wyjścia
            e.printStackTrace();
            return new Mat(); // Zwrócenie pustego obiektu Mat w przypadku błędu
        }
    }

    // Prywatna metoda pomocnicza: konwertuje obiekt Mat do BufferedImage
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte mob = new MatOfByte();
        // Kodowanie obrazu Mat do formatu PNG
        Imgcodecs.imencode(".png", mat, mob);
        byte[] byteArray = mob.toArray();
        try {
            // Odczyt obrazu z tablicy bajtów do obiektu BufferedImage
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            // Obsługa błędów wejścia/wyjścia
            e.printStackTrace();
            return null; // Zwrócenie wartości null w przypadku błędu
        }
    }
}
