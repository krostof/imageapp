package org.example;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageService {

    public int[][] generateHistogram(BufferedImage image) {
        int[][] histogram = new int[3][256];

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y));

                histogram[0][pixelColor.getRed()]++;
                histogram[1][pixelColor.getGreen()]++;
                histogram[2][pixelColor.getBlue()]++;
            }
        }

        return histogram;
    }

    public BufferedImage loadImageFromFile(File file) {
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveImageToFile(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage duplicateImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        BufferedImage duplicatedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                duplicatedImage.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return duplicatedImage;
    }
}