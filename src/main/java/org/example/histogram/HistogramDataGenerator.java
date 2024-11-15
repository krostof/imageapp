package org.example.histogram;

import java.awt.image.BufferedImage;

public class HistogramDataGenerator {

    private final LUTGenerator lutGenerator;

    public HistogramDataGenerator() {
        this.lutGenerator = new LUTGenerator();
    }

    public HistogramDataGenerator(LUTGenerator lutGenerator) {
        this.lutGenerator = lutGenerator;
    }

    public int[] generateOverallHistogram(BufferedImage image) {
        return lutGenerator.generateHistogramLUT(image); // Delegowanie do LUTGenerator
    }

    public int[][] generateColorHistograms(BufferedImage image) {
        return lutGenerator.generateColorHistogramsLUT(image); // Delegowanie do LUTGenerator
    }

    public int[] generateEqualizationLUT(BufferedImage image) {
        int[] histogram = generateOverallHistogram(image);
        int totalPixels = image.getWidth() * image.getHeight();
        return lutGenerator.generateEqualizationLUT(histogram, totalPixels); // Delegowanie do LUTGenerator
    }

    public int getMaxValue(int[] histogram) {
        int max = 0;
        for (int value : histogram) {
            max = Math.max(max, value);
        }
        return max;
    }
}