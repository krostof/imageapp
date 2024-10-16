package org.example;

import java.awt.image.BufferedImage;

public class ImageService {

    public int[][] generateHistogram(BufferedImage image) {
        int[][] histogram = new int[3][256];

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

    public void applyLinearStretch(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] min = {255, 255, 255};
        int[] max = {0, 0, 0};

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

    private int stretchValue(int value, int min, int max) {
        if (max == min) {
            return value;
        }
        return (int) (((value - min) * 255.0) / (max - min));
    }
}



