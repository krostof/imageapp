package org.example;

import org.example.histogram.LUTGenerator;

import java.awt.image.BufferedImage;

public class HistogramEqualizer {

    private final LUTGenerator lutGenerator;

    public HistogramEqualizer() {
        this.lutGenerator = new LUTGenerator();
    }

    public HistogramEqualizer(LUTGenerator lutGenerator) {
        this.lutGenerator = lutGenerator;
    }

    public void applyHistogramEqualization(BufferedImage image) {
        // Generowanie histogramu obrazu
        int[] histogram = lutGenerator.generateHistogramLUT(image);

        // Obliczenie liczby pikseli w obrazie
        int totalPixels = image.getWidth() * image.getHeight();

        // Generowanie tablicy LUT dla equalizacji
        int[] equalizationLUT = lutGenerator.generateEqualizationLUT(histogram, totalPixels);

        // Zastosowanie LUT na obrazie
        applyLUTToImage(image, equalizationLUT);
    }

    private void applyLUTToImage(BufferedImage image, int[] lut) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);

                int red = lut[(pixel >> 16) & 0xFF];
                int green = lut[(pixel >> 8) & 0xFF];
                int blue = lut[pixel & 0xFF];

                int newPixel = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }
    }
}
