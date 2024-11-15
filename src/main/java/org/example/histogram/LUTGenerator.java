package org.example.histogram;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LUTGenerator {

    // Generowanie histogramu intensywności (skala szarości)
    public int[] generateHistogramLUT(BufferedImage image) {
        int[] histogram = new int[256]; // LUT histogramu dla 256 poziomów jasności

        // Inicjalizacja histogramu
        Arrays.fill(histogram, 0);

        // Iteracja przez wszystkie piksele obrazu i zliczanie intensywności
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int intensity = calculateIntensity(pixel); // Konwersja do jasności
                histogram[intensity]++;
            }
        }

        return histogram;
    }

    // Generowanie histogramów dla każdego kanału RGB
    public int[][] generateColorHistogramsLUT(BufferedImage image) {
        int[][] colorHistograms = new int[3][256]; // Histogramy dla R, G, B

        // Iteracja przez piksele i zliczanie intensywności dla każdego kanału
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

    // Generowanie tablicy LUT do equalizacji histogramu
    public int[] generateEqualizationLUT(int[] histogram, int totalPixels) {
        int[] lut = new int[256]; // Tablica LUT dla wyrównania histogramu
        double[] cumulativeDistribution = new double[256]; // Skumulowana dystrybucja prawdopodobieństwa

        // Obliczenie skumulowanej dystrybucji histogramu (cumulative distribution function, CDF)
        cumulativeDistribution[0] = (double) histogram[0] / totalPixels;
        for (int i = 1; i < 256; i++) {
            cumulativeDistribution[i] = cumulativeDistribution[i - 1] + (double) histogram[i] / totalPixels;
        }

        // Normalizacja CDF w celu wygenerowania LUT
        double d0 = cumulativeDistribution[0]; // Pierwsza niezerowa wartość dystrybucji
        for (int i = 0; i < 256; i++) {
            // LUT[i] = ((CDF[i] - d0) / (1 - d0)) * (M - 1)
            lut[i] = (int) (((cumulativeDistribution[i] - d0) / (1 - d0)) * 255);
        }

        return lut;
    }

    // Obliczanie jasności piksela jako średniej wartości R, G, B
    private int calculateIntensity(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return (red + green + blue) / 3;
    }
}
