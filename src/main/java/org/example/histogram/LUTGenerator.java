package org.example.histogram;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LUTGenerator {

    public int[] generateHistogramLUT(BufferedImage image) {
        int[] histogram = new int[256]; // Tablica histogramu (LUT) dla intensywności

        Arrays.fill(histogram, 0);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int intensity = calculateIntensity(pixel); // Konwersja na skalę szarości
                histogram[intensity]++;
            }
        }

        return histogram;
    }

    public int[][] generateColorHistogramsLUT(BufferedImage image) {
        int[][] colorHistograms = new int[3][256]; // Kanały: R, G, B

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                colorHistograms[0][red]++;
                colorHistograms[1][green]++;
                colorHistograms[2][blue]++;
            }
        }

        return colorHistograms;
    }

    public int[] generateEqualizationLUT(int[] histogram, int totalPixels) {
        int[] lut = new int[256];
        double[] cumulativeDistribution = new double[256];

        // Obliczanie dystrybucji skumulowanej
        cumulativeDistribution[0] = (double) histogram[0] / totalPixels;
        for (int i = 1; i < 256; i++) {
            cumulativeDistribution[i] = cumulativeDistribution[i - 1] + (double) histogram[i] / totalPixels;
        }

        // Normalizacja dystrybucji skumulowanej
        double d0 = cumulativeDistribution[0];
        for (int i = 0; i < 256; i++) {
            lut[i] = (int) (((cumulativeDistribution[i] - d0) / (1 - d0)) * 255);
        }

        return lut;
    }

    private int calculateIntensity(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return (red + green + blue) / 3;
    }
}
