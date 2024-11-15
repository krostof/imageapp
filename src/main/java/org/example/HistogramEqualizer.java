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
        int[] histogram = lutGenerator.generateHistogramLUT(image); // Generowanie histogramu
        int totalPixels = image.getWidth() * image.getHeight();

        int[] equalizationLUT = lutGenerator.generateEqualizationLUT(histogram, totalPixels); // Generowanie LUT

        applyLUTToImage(image, equalizationLUT); // Zastosowanie LUT na obrazie
    }


    private void applyLUTToImage(BufferedImage image, int[] lut) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości szarości
                int newPixel = lut[pixel]; // Zastosowanie LUT
                image.getRaster().setSample(x, y, 0, newPixel); // Ustawienie nowej wartości
            }
        }
    }

}
