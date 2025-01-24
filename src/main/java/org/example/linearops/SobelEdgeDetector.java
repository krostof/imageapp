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

/*
    * kierunkowej detekcji krawędzi w oparciu o maski 8 kierunkowych masek Sobela
        (podstawowe 8 kierunków) przestawionych użytkownikowi do wyboru,
 */
public class SobelEdgeDetector {

    private final BorderFillProcessor borderFillProcessor;

    public SobelEdgeDetector(BorderFillProcessor borderFillProcessor) {
        this.borderFillProcessor = borderFillProcessor;
    }

    public BufferedImage applyDirectionalSobel(BufferedImage inputImage, String direction, int borderType, int constantValue) {
        Mat sourceMat = bufferedImageToMat(inputImage);

        if (sourceMat.channels() == 3) {
            Imgproc.cvtColor(sourceMat, sourceMat, Imgproc.COLOR_BGR2GRAY);
        }

        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        Map<String, int[]> sobelMasks = getSobelMasks();
        int[] mask = sobelMasks.getOrDefault(direction, sobelMasks.get("East"));

        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                kernel.put(i, j, (float) mask[i * 3 + j]);
            }
        }

        Mat sobelResult = borderFillProcessor.applyFilterWithBorder(sourceMat32F, kernel, borderType, constantValue);

        Mat absSobelResult = new Mat();
        Core.convertScaleAbs(sobelResult, absSobelResult);

        return matToBufferedImage(absSobelResult);
    }

    private Map<String, int[]> getSobelMasks() {
        Map<String, int[]> sobelMasks = new HashMap<>();
        sobelMasks.put("East", new int[]{-1, 0, 1, -2, 0, 2, -1, 0, 1});
        sobelMasks.put("South", new int[]{-1, -2, -1, 0, 0, 0, 1, 2, 1});
        sobelMasks.put("North East", new int[]{0, 1, 2, -1, 0, 1, -2, -1, 0});
        sobelMasks.put("South East", new int[]{-2, -1, 0, -1, 0, 1, 0, 1, 2});
        sobelMasks.put("West", new int[]{1, 0, -1, 2, 0, -2, 1, 0, -1});
        sobelMasks.put("North", new int[]{1, 2, 1, 0, 0, 0, -1, -2, -1});
        sobelMasks.put("North West", new int[]{2, 1, 0, 1, 0, -1, 0, -1, -2});
        sobelMasks.put("South West", new int[]{0, -2, -1, 1, 0, -1, 1, 2, 0});
        return sobelMasks;
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
