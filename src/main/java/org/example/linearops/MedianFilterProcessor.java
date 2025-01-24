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
Opracowanie algorytmu i uruchomienie aplikacji realizującej uniwersalną operację medianową
opartą na otoczeniu  3x3, 5x5, 7x7, 9x9 zadawanym w sposób interaktywny (wybór z list,
przesuwanie baru). Zastosować powyższych metod uzupełniania brzegowych pikselach obrazu,
dając użytkownikowi możliwość wyboru, jak w zadaniu 1.
 */
public class MedianFilterProcessor {

    public BufferedImage applyMedianFilter(BufferedImage inputImage, int kernelSize, int borderTypeCode) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        // do skali szarosci jeśli obraz jest kolorowy
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // dodawanie marginesów do obrazu
        Mat paddedMat = new Mat();
        Core.copyMakeBorder(sourceMat, paddedMat, kernelSize / 2, kernelSize / 2, kernelSize / 2, kernelSize / 2, borderTypeCode);

        // filtr medianowy
        Mat resultMat = new Mat();
        Imgproc.medianBlur(paddedMat, resultMat, kernelSize);

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
