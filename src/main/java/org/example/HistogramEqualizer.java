package org.example;

import org.example.histogram.LUTGenerator;

import java.awt.image.BufferedImage;


/*
Selektywnego wyrównanie histogramu przez equalizację
samodzielnie zaimplementowane według jednego z
algorytmów przedstawionych na wykładzie
 */
public class HistogramEqualizer {

    private final LUTGenerator lutGenerator;
    public HistogramEqualizer(LUTGenerator lutGenerator) {
        this.lutGenerator = lutGenerator;
    }

    public void applyHistogramEqualization(BufferedImage image) {
        int[] histogram = lutGenerator.generateHistogramLUT(image);
        int totalPixels = image.getWidth() * image.getHeight();

        int[] equalizationLUT = lutGenerator.generateEqualizationLUT(histogram, totalPixels);

        applyLUTToImage(image, equalizationLUT);
    }

    private void applyLUTToImage(BufferedImage image, int[] lut) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int newPixel = lut[pixel];
                image.getRaster().setSample(x, y, 0, newPixel);
            }
        }
    }
}
