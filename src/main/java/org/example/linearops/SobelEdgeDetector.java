package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SobelEdgeDetector {

    public BufferedImage applyDirectionalSobel(BufferedImage inputImage, String direction) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        // do skali szarosci jeśli obraz jest kolorowy
        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        // CV_32F (32-bitowe liczby zmiennoprzecinkowe)
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // maski
        Map<String, int[]> sobelMasks = getSobelMasks();

        // pobranie maski, domyślnie Eest
        int[] mask = sobelMasks.getOrDefault(direction, sobelMasks.get("East"));

        // jądro na podstawie wybranej maski
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                kernel.put(i, j, (float) mask[i * 3 + j]);
            }
        }

        //  filtr Sobela z wybraną maską
        Mat sobelResult = new Mat();
        Imgproc.filter2D(sourceMat32F, sobelResult, CvType.CV_32F, kernel);

        // wynik i skalowanie do obrazu 8-bitowego
        Mat absSobelResult = new Mat();
        Core.convertScaleAbs(sobelResult, absSobelResult);

        return matToBufferedImage(absSobelResult);
    }

    private Map<String, int[]> getSobelMasks() {
        // Tworzenie mapy masek Sobela dla różnych kierunków
        Map<String, int[]> sobelMasks = new HashMap<>();

        // Maska dla poziomego kierunku (0 stopni)
        sobelMasks.put("East", new int[]{
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        });

        // Maska dla pionowego kierunku (90 stopni)
        sobelMasks.put("South", new int[]{
                -1, -2, -1,
                0, 0, 0,
                1, 2, 1
        });

        // Maska dla kierunku diagonalnego (45 stopni, z dolnego lewego do górnego prawego)
        sobelMasks.put("North East", new int[]{
                0, 1, 2,
                -1, 0, 1,
                -2, -1, 0
        });

        // Maska dla kierunku diagonalnego (135 stopni, z górnego lewego do dolnego prawego)
        sobelMasks.put("South East", new int[]{
                -2, -1, 0,
                -1, 0, 1,
                0, 1, 2
        });

        // Odwrócona maska pozioma (270 stopni)
        sobelMasks.put("West", new int[]{
                1, 0, -1,
                2, 0, -2,
                1, 0, -1
        });

        // Odwrócona maska pionowa (180 stopni)
        sobelMasks.put("North", new int[]{
                1, 2, 1,
                0, 0, 0,
                -1, -2, -1
        });

        // Odwrócona maska diagonalna (225 stopni)
        sobelMasks.put("North West", new int[]{
                2, 1, 0,
                1, 0, -1,
                0, -1, -2
        });

        // Odwrócona maska diagonalna (315 stopni)
        sobelMasks.put("South West", new int[]{
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
