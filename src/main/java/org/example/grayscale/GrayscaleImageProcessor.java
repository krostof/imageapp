package org.example.grayscale;

import java.awt.image.BufferedImage;

/*
redukcja poziomów szarości przez powtórną kwantyzację z liczbą poziomów
szarości wskazaną przez użytkownika,
 */
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
                int pixel = image.getRaster().getSample(x, y, 0);
                int negatedPixel = 255 - pixel;
                negatedImage.getRaster().setSample(x, y, 0, negatedPixel);
            }
        }
        String string = image.toString();
        System.out.println("Image negation: " + string);
        return negatedImage;
    }

    /**
     * Redukcja liczby poziomów szarości w obrazie
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

        int step = 256 / levels;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int quantizedPixel = (pixel / step) * step;
                quantizedImage.getRaster().setSample(x, y, 0, quantizedPixel);
            }
        }

        return quantizedImage;
    }

    public BufferedImage binarizeImage(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binarizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int binaryPixel = (pixel > threshold) ? 255 : 0;
                binarizedImage.getRaster().setSample(x, y, 0, binaryPixel);
            }
        }

        return binarizedImage;
    }

    /**
     * Progowanie obrazu z zachowaniem poziomów szarości
     */
    public BufferedImage thresholdWithGrayLevels(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage thresholdedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int grayLevelPixel = (pixel > threshold) ? pixel : 0;
                thresholdedImage.getRaster().setSample(x, y, 0, grayLevelPixel);
            }
        }

        return thresholdedImage;
    }
}
