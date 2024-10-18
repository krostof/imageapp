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

    public int getPeakValue(int[][] histogram) {
        int maxIndex = 0;
        int maxValue = 0;
        for (int i = 0; i < histogram[0].length; i++) {
            int currentValue = histogram[0][i] + histogram[1][i] + histogram[2][i];
            if (currentValue > maxValue) {
                maxValue = currentValue;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public double getMaximumValue(int[][] histogram) {
        int maxValue = 0;
        for (int i = 0; i < histogram[0].length; i++) {
            int currentValue = histogram[0][i] + histogram[1][i] + histogram[2][i];
            maxValue = Math.max(maxValue, currentValue);
        }
        return maxValue;
    }
}
