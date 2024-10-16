package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageService {

    // Generowanie histogramu dla kanałów RGB
    public int[][] generateHistogram(BufferedImage image) {
        int[][] histogram = new int[3][256]; // [0] - Red, [1] - Green, [2] - Blue

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                histogram[0][red]++;
                histogram[1][green]++;
                histogram[2][blue]++;
            }
        }

        return histogram;
    }

    // Zastosowanie rozciągania liniowego
    public void applyLinearStretch(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] min = {255, 255, 255};  // minimalne wartości dla kanałów R, G, B
        int[] max = {0, 0, 0};        // maksymalne wartości dla kanałów R, G, B

        // Znalezienie minimalnych i maksymalnych wartości dla każdego kanału
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                if (red < min[0]) min[0] = red;
                if (green < min[1]) min[1] = green;
                if (blue < min[2]) min[2] = blue;

                if (red > max[0]) max[0] = red;
                if (green > max[1]) max[1] = green;
                if (blue > max[2]) max[2] = blue;
            }
        }

        // Zastosowanie rozciągania liniowego
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                red = stretchValue(red, min[0], max[0]);
                green = stretchValue(green, min[1], max[1]);
                blue = stretchValue(blue, min[2], max[2]);

                int newPixel = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }
    }

    // Funkcja rozciągająca wartość z danego zakresu do zakresu 0-255
    private int stretchValue(int value, int min, int max) {
        if (max == min) {
            return value; // Unikamy dzielenia przez zero
        }
        return (int) (((value - min) * 255.0) / (max - min)); // Normalizacja wartości do zakresu 0-255
    }
}


//    public BufferedImage generateHistogram(BufferedImage image) {
//        int[][] histogram = calculateHistogram(image);
//
//        int width = 256;
//        int height = 100;
//        BufferedImage histogramImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2d = histogramImage.createGraphics();
//
//        g2d.setColor(Color.WHITE);
//        g2d.fillRect(0, 0, width, height);
//
//        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE};
//
//        int max = 0;
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 3; j++) {
//                if (histogram[j][i] > max) {
//                    max = histogram[j][i];
//                }
//            }
//        }
//
//        for (int channel = 0; channel < 3; channel++) {
//            g2d.setColor(colors[channel]);
//            for (int i = 0; i < 256; i++) {
//                int value = histogram[channel][i];
//                int barHeight = (int) ((value / (float) max) * height);
//                g2d.drawLine(i, height, i, height - barHeight);
//            }
//        }
//
//        g2d.dispose();
//
//        return histogramImage;
//    }

