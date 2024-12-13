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

    /**
     * Generuje histogram "overall" obrazu (jasność), delegując do LUTGenerator.
     */
    public int[] generateOverallHistogram(BufferedImage image) {
        return lutGenerator.generateHistogramLUT(image);
    }

    /**
     * Generuje histogramy dla kanałów R, G, B w kolorowym obrazie.
     */
    public int[][] generateColorHistograms(BufferedImage image) {
        return lutGenerator.generateColorHistogramsLUT(image);
    }

    /**
     * Generuje LUT do equalizacji histogramu (przydatne np. do wyrównywania kontrastu).
     */
    public int[] generateEqualizationLUT(BufferedImage image) {
        int[] histogram = generateOverallHistogram(image);
        int totalPixels = image.getWidth() * image.getHeight();
        return lutGenerator.generateEqualizationLUT(histogram, totalPixels);
    }

    /**
     * Zwraca maksymalną wartość w histogramie.
     */
    public int getMaxValue(int[] histogram) {
        int max = 0;
        for (int value : histogram) {
            max = Math.max(max, value);
        }
        return max;
    }
}
