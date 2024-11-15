package org.example.histogram;

import java.awt.image.BufferedImage;

public class LUTGenerator {

    /**
     * Generuje histogram obrazu w skali szarości.
     * Algorytm:
     * - Iteruje przez wszystkie piksele obrazu.
     * - Zlicza liczbę wystąpień każdej wartości szarości (0-255).
     *
     * @param image Obraz wejściowy w skali szarości.
     * @return Tablica histogramu.
     */
    public int[] generateHistogramLUT(BufferedImage image) {
        int[] histogram = new int[256]; // Tablica dla poziomów szarości

        // Iteracja po pikselach i zliczanie wartości
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości szarości
                histogram[pixel]++;
            }
        }
        return histogram;
    }

    /**
     * Generuje histogramy dla każdego kanału RGB.
     * Algorytm:
     * - Iteruje przez piksele obrazu.
     * - Zlicza intensywności dla kanałów R, G i B.
     *
     * @param image Obraz wejściowy w kolorze.
     * @return Tablica histogramów dla kanałów R, G, B.
     */
    public int[][] generateColorHistogramsLUT(BufferedImage image) {
        int[][] colorHistograms = new int[3][256]; // Histogramy dla kanałów R, G, B

        // Iteracja przez piksele obrazu
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);

                // Wyodrębnianie wartości kanałów
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

    /**
     * Generuje tablicę LUT do equalizacji histogramu.
     * Algorytm:
     * - Oblicza skumulowaną dystrybucję histogramu.
     * - Normalizuje wartości do zakresu 0-255.
     *
     * @param histogram Histogram obrazu.
     * @param totalPixels Liczba pikseli w obrazie.
     * @return Tablica LUT.
     */
    public int[] generateEqualizationLUT(int[] histogram, int totalPixels) {
        int[] lut = new int[256];
        int cumulative = 0;

        // Obliczenie skumulowanej dystrybucji
        for (int i = 0; i < histogram.length; i++) {
            cumulative += histogram[i];
            lut[i] = (int) ((cumulative * 255.0) / totalPixels); // Normalizacja do zakresu 0-255
        }

        return lut;
    }
}
