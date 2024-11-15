package org.example.histogram;

import java.awt.image.BufferedImage;


public class LUTGenerator {

    public int[] generateHistogramLUT(BufferedImage image) {
        int[] histogram = new int[256];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości szarości
                histogram[pixel]++;
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


    // Obliczanie jasności piksela jako średniej wartości R, G, B
    private int calculateIntensity(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return (red + green + blue) / 3;
    }
}
