package org.example.histogram;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LUTGenerator {

    /**
     * Generuje histogram obrazu, zwraca tablicę 256-elementową.
     * dla obrazu TYPE_BYTE_GRAY pobiera kanał 0 z rastra,
     * dla obrazu kolorowego stosuje formułę Y = 0.299R + 0.587G + 0.114B.
     */
    public int[] generateHistogramLUT(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[] histogram = new int[256];
        Arrays.fill(histogram, 0);

        int imageType = image.getType();
        boolean isGray = (imageType == BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (isGray) {
                    // Dla obrazu monochromatycznego (TYPE_BYTE_GRAY):
                    int gray = image.getRaster().getSample(x, y, 0);
                    histogram[gray]++;
                } else {
                    // Dla obrazu kolorowego wyliczamy luminancję:
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = (rgb) & 0xFF;

                    int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    // Upewniamy się, że mieści się w zakresie 0..255
                    if (gray < 0) gray = 0;
                    if (gray > 255) gray = 255;

                    histogram[gray]++;
                }
            }
        }
        return histogram;
    }

    /**
     * Generuje histogramy dla kanałów R, G, B.
     * Działąnie:
     * - Iteruje przez piksele obrazu.
     * - Zlicza intensywności dla kanałów R, G i B.
     */
    public int[][] generateColorHistogramsLUT(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[][] colorHistograms = new int[3][256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                int red   = (pixel >> 16) & 0xFF;
                int green = (pixel >>  8) & 0xFF;
                int blue  =  pixel        & 0xFF;

                colorHistograms[0][red]++;
                colorHistograms[1][green]++;
                colorHistograms[2][blue]++;
            }
        }

        return colorHistograms;
    }

    /**
     * Tablica LUT do equalizacji histogramu
     */
    public int[] generateEqualizationLUT(int[] histogram, int totalPixels) {
        if (histogram == null || histogram.length != 256) {
            throw new IllegalArgumentException("Histogram must be 256-length array.");
        }
        if (totalPixels <= 0) {
            throw new IllegalArgumentException("totalPixels must be > 0.");
        }

        int[] lut = new int[256];
        int cumulative = 0;

        for (int i = 0; i < histogram.length; i++) {
            cumulative += histogram[i];
            // Normalizacja do zakresu 0-255
            lut[i] = (int) ((cumulative * 255.0) / totalPixels);
        }

        return lut;
    }
}
