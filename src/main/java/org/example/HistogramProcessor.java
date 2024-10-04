package org.example;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class HistogramProcessor {

    public BufferedImage linearStretch(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int minPixelValue = 255;
        int maxPixelValue = 0;

        // Znajdź minimalną i maksymalną wartość piksela
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = new Color(image.getRGB(x, y)).getRed();  // Przyjmujemy, że obraz jest w skali szarości
                if (pixelValue < minPixelValue) {
                    minPixelValue = pixelValue;
                }
                if (pixelValue > maxPixelValue) {
                    maxPixelValue = pixelValue;
                }
            }
        }

        // Przeprowadź rozciąganie liniowe
        BufferedImage stretchedImage = new BufferedImage(width, height, image.getType());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = new Color(image.getRGB(x, y)).getRed();
                int newPixelValue = (pixelValue - minPixelValue) * 255 / (maxPixelValue - minPixelValue);

                Color newColor = new Color(newPixelValue, newPixelValue, newPixelValue); // Przyjmujemy obraz w odcieniach szarości
                stretchedImage.setRGB(x, y, newColor.getRGB());
            }
        }

        return stretchedImage;
    }
}

