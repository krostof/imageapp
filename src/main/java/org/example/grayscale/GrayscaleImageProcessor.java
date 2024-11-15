package org.example.grayscale;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GrayscaleImageProcessor {

    /**
     * Neguje obraz w skali szarości.
     * Algorytm:
     * - Dla każdego piksela wykonuje operację `255 - wartość piksela`.
     *
     * @param image Obraz wejściowy w skali szarości.
     * @return Obraz po negacji.
     */
    public BufferedImage negateImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage negatedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int negatedPixel = 255 - pixel; // Negacja
                negatedImage.getRaster().setSample(x, y, 0, negatedPixel); // Zmiana wartości
            }
        }

        return negatedImage;
    }

    /**
     * Redukuje liczbę poziomów szarości w obrazie.
     * Algorytm:
     * - Dzieli zakres 0-255 na `levels` równych przedziałów.
     * - Kwantyzuje wartość każdego piksela do odpowiedniego przedziału.
     *
     * @param image Obraz wejściowy w skali szarości.
     * @param levels Liczba poziomów szarości.
     * @return Obraz zredukowany do `levels` poziomów.
     */
    public BufferedImage quantizeImage(BufferedImage image, int levels) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Input image must be in grayscale.");
        }

        if (levels < 2 || levels > 256) {
            throw new IllegalArgumentException("Number of levels must be between 2 and 256.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int step = 256 / levels; // Rozmiar jednego przedziału

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int quantizedPixel = (pixel / step) * step; // Kwantyzacja
                quantizedImage.getRaster().setSample(x, y, 0, quantizedPixel); // Zmiana wartości
            }
        }

        return quantizedImage;
    }

    /**
     * Przeprowadza binarne progowanie obrazu.
     * Algorytm:
     * - Dla każdego piksela:
     *   - Jeśli wartość > próg, ustaw wartość na 255.
     *   - W przeciwnym razie ustaw wartość na 0.
     *
     * @param image Obraz wejściowy w skali szarości.
     * @param threshold Wartość progu (0-255).
     * @return Obraz binarny.
     */
    public BufferedImage binarizeImage(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binarizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int binaryPixel = (pixel > threshold) ? 255 : 0; // Porównanie z progiem
                binarizedImage.getRaster().setSample(x, y, 0, binaryPixel); // Zmiana wartości
            }
        }

        return binarizedImage;
    }

    /**
     * Przeprowadza progowanie obrazu z zachowaniem poziomów szarości.
     * Algorytm:
     * - Dla każdego piksela:
     *   - Jeśli wartość > próg, pozostaw wartość bez zmian.
     *   - W przeciwnym razie ustaw wartość na 0.
     *
     * @param image Obraz wejściowy w skali szarości.
     * @param threshold Wartość progu (0-255).
     * @return Obraz po progowaniu.
     */
    public BufferedImage thresholdWithGrayLevels(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage thresholdedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int grayLevelPixel = (pixel > threshold) ? pixel : 0; // Zachowanie poziomów szarości
                thresholdedImage.getRaster().setSample(x, y, 0, grayLevelPixel); // Zmiana wartości
            }
        }

        return thresholdedImage;
    }
}
