package org.example.linearstreach;

import lombok.extern.log4j.Log4j2;
import org.example.histogram.LUTGenerator;

import java.awt.image.BufferedImage;

@Log4j2
public class LinearStretchProcessor {

    private final LUTGenerator lutGenerator;

    public LinearStretchProcessor() {
        this.lutGenerator = new LUTGenerator();
    }

    /**
     * Przeprowadza liniowe rozciąganie histogramu obrazu z opcjonalnym obcinaniem wartości.
     *
     * @param image Obraz wejściowy.
     * @param withClipping Czy stosować obcinanie wartości pikseli.
     * @param clippingPercentage Procent pikseli do obcięcia (górny i dolny zakres histogramu).
     */
    public void applyLinearStretch(BufferedImage image, boolean withClipping, double clippingPercentage) {
        if (withClipping) {
            log.info("Applying linear stretch with clipping. Clipping percentage: {}%", clippingPercentage * 100);
            applyLinearStretchWithClipping(image, clippingPercentage);
        } else {
            log.info("Applying linear stretch without clipping.");
            applyLinearStretchWithoutClipping(image);
        }
    }

    /**
     * Wykonuje liniowe rozciąganie histogramu bez obcinania wartości.
     * Algorytm:
     * - Generuje histogram obrazu.
     * - Oblicza LUT dla rozciągania histogramu.
     * - Zastosowuje LUT do obrazu.
     *
     * @param image Obraz wejściowy.
     */
    private void applyLinearStretchWithoutClipping(BufferedImage image) {
        int[] histogram = lutGenerator.generateHistogramLUT(image); // Generowanie histogramu
        int totalPixels = image.getWidth() * image.getHeight();
        int[] lut = lutGenerator.generateEqualizationLUT(histogram, totalPixels); // Generowanie LUT

        transformImageWithLUT(image, lut); // Zastosowanie LUT
    }

    /**
     * Wykonuje liniowe rozciąganie histogramu z obcinaniem wartości.
     * Algorytm:
     * - Generuje histogram obrazu.
     * - Oblicza granice obcinania na podstawie procentu pikseli.
     * - Generuje LUT dla rozciągania z obcinaniem.
     * - Zastosowuje LUT do obrazu.
     *
     * @param image Obraz wejściowy.
     * @param clippingPercentage Procent pikseli do obcięcia.
     */
    private void applyLinearStretchWithClipping(BufferedImage image, double clippingPercentage) {
        int[] histogram = lutGenerator.generateHistogramLUT(image); // Generowanie histogramu
        int totalPixels = image.getWidth() * image.getHeight();

        int clipPixels = (int) (totalPixels * clippingPercentage); // Liczba pikseli do obcięcia
        int lowerBound = findClippingBound(histogram, clipPixels, true); // Dolna granica
        int upperBound = findClippingBound(histogram, clipPixels, false); // Górna granica

        int[] lut = generateClippedLUT(lowerBound, upperBound); // Generowanie LUT dla obcinania

        transformImageWithLUT(image, lut); // Zastosowanie LUT
    }

    /**
     * Znajduje granicę obcinania na podstawie liczby pikseli do obcięcia.
     *
     * @param histogram Histogram obrazu.
     * @param clipPixels Liczba pikseli do obcięcia.
     * @param isLower Czy szukać dolnej granicy.
     * @return Wartość granicy (0-255).
     */
    private int findClippingBound(int[] histogram, int clipPixels, boolean isLower) {
        int sum = 0;

        if (isLower) {
            for (int i = 0; i < histogram.length; i++) {
                sum += histogram[i];
                if (sum >= clipPixels) {
                    return i;
                }
            }
        } else {
            for (int i = histogram.length - 1; i >= 0; i--) {
                sum += histogram[i];
                if (sum >= clipPixels) {
                    return i;
                }
            }
        }

        return isLower ? 0 : 255;
    }

    /**
     * Generuje LUT dla rozciągania histogramu z obcinaniem wartości.
     * Algorytm:
     * - Piksele poniżej dolnej granicy otrzymują wartość 0.
     * - Piksele powyżej górnej granicy otrzymują wartość 255.
     * - Piksele w zakresie dolnej i górnej granicy są rozciągane liniowo.
     *
     * @param lowerBound Dolna granica zakresu.
     * @param upperBound Górna granica zakresu.
     * @return Tablica LUT.
     */
    private int[] generateClippedLUT(int lowerBound, int upperBound) {
        int[] lut = new int[256];

        for (int i = 0; i < 256; i++) {
            if (i < lowerBound) {
                lut[i] = 0;
            } else if (i > upperBound) {
                lut[i] = 255;
            } else {
                lut[i] = (int) (((i - lowerBound) * 255.0) / (upperBound - lowerBound)); // Rozciąganie liniowe
            }
        }

        return lut;
    }

    /**
     * Zastosowuje LUT do obrazu.
     * Algorytm:
     * - Dla każdego piksela w obrazie pobiera wartość LUT.
     * - Ustawia nową wartość piksela na podstawie LUT.
     *
     * @param image Obraz wejściowy.
     * @param lut Tablica LUT.
     */
    private void transformImageWithLUT(BufferedImage image, int[] lut) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int newPixel = lut[pixel]; // Zastosowanie LUT
                image.getRaster().setSample(x, y, 0, newPixel); // Ustawienie nowej wartości
            }
        }
    }
}
