package org.example;

import java.awt.image.BufferedImage;

public class LinearStretchProcessor {

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

                min[0] = Math.min(min[0], red);
                min[1] = Math.min(min[1], green);
                min[2] = Math.min(min[2], blue);

                max[0] = Math.max(max[0], red);
                max[1] = Math.max(max[1], green);
                max[2] = Math.max(max[2], blue);
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

