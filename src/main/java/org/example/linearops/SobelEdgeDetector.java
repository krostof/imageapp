package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SobelEdgeDetector {

    public BufferedImage applyDirectionalSobel(BufferedImage inputImage, String direction) {
        // Konwersja BufferedImage do Mat
        Mat sourceMat = bufferedImageToMat(inputImage);

        // Sprawdź, czy obraz jest w skali szarości, jeśli nie, konwertuj
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // Konwersja sourceMat do CV_32F
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // Mapa kierunkowych masek Sobela
        Map<String, int[]> sobelMasks = getSobelMasks();

        // Pobierz maskę dla wybranego kierunku
        int[] mask = sobelMasks.getOrDefault(direction, sobelMasks.get("horizontal"));

        // Utwórz kernel dla wybranego kierunku
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                kernel.put(i, j, (float) mask[i * 3 + j]);
            }
        }

        // Aplikacja maski Sobela
        Mat sobelResult = new Mat();
        Imgproc.filter2D(sourceMat32F, sobelResult, CvType.CV_32F, kernel);

        // Konwersja do wartości bezwzględnych i skala do 8-bitowego obrazu
        Mat absSobelResult = new Mat();
        Core.convertScaleAbs(sobelResult, absSobelResult);

        // Konwersja Mat do BufferedImage
        return matToBufferedImage(absSobelResult);
    }


    private Map<String, int[]> getSobelMasks() {
        Map<String, int[]> sobelMasks = new HashMap<>();

        // Horizontal mask (0 degrees)
        sobelMasks.put("horizontal", new int[]{
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        });

        // Vertical mask (90 degrees)
        sobelMasks.put("vertical", new int[]{
                -1, -2, -1,
                0, 0, 0,
                1, 2, 1
        });

        // Diagonal mask (45 degrees, diagonal from bottom-left to top-right)
        sobelMasks.put("diagonal_left", new int[]{
                0, 1, 2,
                -1, 0, 1,
                -2, -1, 0
        });

        // Diagonal mask (135 degrees, diagonal from top-left to bottom-right)
        sobelMasks.put("diagonal_right", new int[]{
                -2, -1, 0,
                -1, 0, 1,
                0, 1, 2
        });

        // Left horizontal mask (270 degrees, reverse horizontal)
        sobelMasks.put("reverse_horizontal", new int[]{
                1, 0, -1,
                2, 0, -2,
                1, 0, -1
        });

        // Reverse vertical mask (180 degrees, reverse vertical)
        sobelMasks.put("reverse_vertical", new int[]{
                1, 2, 1,
                0, 0, 0,
                -1, -2, -1
        });

        // Reverse diagonal left (225 degrees, reverse diagonal left)
        sobelMasks.put("reverse_diagonal_left", new int[]{
                2, 1, 0,
                1, 0, -1,
                0, -1, -2
        });

        // Reverse diagonal right (315 degrees, reverse diagonal right)
        sobelMasks.put("reverse_diagonal_right", new int[]{
                0, -1, -2,
                1, 0, -1,
                2, 1, 0
        });

        return sobelMasks;
    }


    private Mat bufferedImageToMat(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();
            Mat mat = Imgcodecs.imdecode(new MatOfByte(imageInBytes), Imgcodecs.IMREAD_UNCHANGED);
            return mat;
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
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArray));
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
