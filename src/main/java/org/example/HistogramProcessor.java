package org.example;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class HistogramProcessor {

    public BufferedImage linearStretch(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int minPixelValue = 255;
        int maxPixelValue = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = new Color(image.getRGB(x, y)).getRed();
                if (pixelValue < minPixelValue) {
                    minPixelValue = pixelValue;
                }
                if (pixelValue > maxPixelValue) {
                    maxPixelValue = pixelValue;
                }
            }
        }

        BufferedImage stretchedImage = new BufferedImage(width, height, image.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = new Color(image.getRGB(x, y)).getRed();
                int newPixelValue = (pixelValue - minPixelValue) * 255 / (maxPixelValue - minPixelValue);

                Color newColor = new Color(newPixelValue, newPixelValue, newPixelValue);
                stretchedImage.setRGB(x, y, newColor.getRGB());
            }
        }

        return stretchedImage;
    }
}

