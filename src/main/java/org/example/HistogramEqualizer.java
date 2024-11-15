package org.example;

import org.example.histogram.LUTGenerator;

import java.awt.image.BufferedImage;

public class HistogramEqualizer {

    private final LUTGenerator lutGenerator;

    /**
     * Konstruktor z argumentem. Pozwala przekazać zewnętrzny LUTGenerator.
     *
     * @param lutGenerator Instancja klasy LUTGenerator.
     */
    public HistogramEqualizer(LUTGenerator lutGenerator) {
        this.lutGenerator = lutGenerator;
    }

    /**
     * Wyrównuje histogram obrazu za pomocą LUT.
     * Algorytm:
     * - Generuje histogram obrazu.
     * - Oblicza skumulowany histogram (LUT) dla wyrównania.
     * - Zastosowuje LUT do obrazu, aby znormalizować poziomy szarości.
     *
     * @param image Obraz wejściowy (w skali szarości).
     */
    public void applyHistogramEqualization(BufferedImage image) {
        int[] histogram = lutGenerator.generateHistogramLUT(image); // Generowanie histogramu
        int totalPixels = image.getWidth() * image.getHeight(); // Obliczenie liczby pikseli

        int[] equalizationLUT = lutGenerator.generateEqualizationLUT(histogram, totalPixels); // Generowanie LUT

        applyLUTToImage(image, equalizationLUT); // Zastosowanie LUT do obrazu
    }

    /**
     * Zastosowuje LUT (Look-Up Table) do obrazu.
     * Algorytm:
     * - Dla każdego piksela obrazu:
     * - Pobiera wartość LUT.
     * - Zastępuje starą wartość nową na podstawie LUT.
     *
     * @param image Obraz wejściowy.
     * @param lut   Tablica LUT.
     */
    private void applyLUTToImage(BufferedImage image, int[] lut) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int newPixel = lut[pixel]; // Pobranie nowej wartości z LUT
                image.getRaster().setSample(x, y, 0, newPixel); // Zmiana wartości piksela
            }
        }
    }
}
