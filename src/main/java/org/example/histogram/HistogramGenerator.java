package org.example.histogram;

import java.awt.image.BufferedImage;

public class HistogramGenerator {

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
}
