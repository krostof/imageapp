package org.example.histogram;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class HistogramDataGenerator {

    private final LUTGenerator lutGenerator;

    public HistogramDataGenerator() {
        this.lutGenerator = new LUTGenerator();
    }

    public HistogramDataGenerator(LUTGenerator lutGenerator) {
        this.lutGenerator = lutGenerator;
    }

    /**
     * Generowanie histogramu obrazu
     */
    public int[] generateOverallHistogram(BufferedImage image) {
        return lutGenerator.generateHistogramLUT(image);
    }

    /**
     * Generowanie histogramu dla kanałów R, G, B w kolorowym obrazie.
     */
    public int[][] generateColorHistograms(BufferedImage image) {
        return lutGenerator.generateColorHistogramsLUT(image);
    }

    /**
     * Generowanie LUT do equalizacji histogramu
     */
    public int[] generateEqualizationLUT(BufferedImage image) {
        int[] histogram = generateOverallHistogram(image);
        int totalPixels = image.getWidth() * image.getHeight();
        return lutGenerator.generateEqualizationLUT(histogram, totalPixels);
    }

    public int getMaxValue(int[] histogram) {
        return Arrays.stream(histogram).max().orElse(0);
    }

    public double calculateMean(int[] histogram) {
        int totalPixels = Arrays.stream(histogram).sum();
        if (totalPixels == 0) return 0.0;

        double sum = 0;
        for (int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
        }
        return sum / totalPixels;
    }

    public double calculateStandardDeviation(int[] histogram, double mean) {
        int totalPixels = Arrays.stream(histogram).sum();
        if (totalPixels == 0) return 0.0;

        double varianceSum = 0;
        for (int i = 0; i < histogram.length; i++) {
            varianceSum += histogram[i] * Math.pow(i - mean, 2);
        }
        return Math.sqrt(varianceSum / totalPixels);
    }

    public int calculateMedian(int[] histogram) {
        int totalPixels = Arrays.stream(histogram).sum();
        if (totalPixels == 0) return 0;

        int cumulativeSum = 0;
        int medianValue = 0;

        for (int i = 0; i < histogram.length; i++) {
            cumulativeSum += histogram[i];
            if (cumulativeSum >= totalPixels / 2) {
                medianValue = i;
                break;
            }
        }
        return medianValue;
    }

    public HistogramStatistics calculateStatistics(BufferedImage image) {
        int[] histogram = generateOverallHistogram(image);
        double mean = calculateMean(histogram);
        double stdDev = calculateStandardDeviation(histogram, mean);
        int median = calculateMedian(histogram);

        return new HistogramStatistics(mean, stdDev, median);
    }

    public static class HistogramStatistics {
        private final double mean;
        private final double standardDeviation;
        private final int median;

        public HistogramStatistics(double mean, double standardDeviation, int median) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
            this.median = median;
        }

        public double getMean() {
            return mean;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public int getMedian() {
            return median;
        }

        @Override
        public String toString() {
            return String.format("Mean: %.2f, Standard Deviation: %.2f, Median: %d", mean, standardDeviation, median);
        }
    }
}
