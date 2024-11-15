package org.example.mathoperations;

import java.awt.image.BufferedImage;

public class MultiArgumentImageProcessor {

    /**
     * Dodawanie dwóch obrazów z opcją wysycenia.
     * Algorytm:
     * - Dla każdego piksela oblicza sumę wartości z dwóch obrazów.
     * - Jeśli aktywne jest wysycenie, wartości powyżej 255 są obcinane do 255.
     *
     * @param image1 Pierwszy obraz wejściowy.
     * @param image2 Drugi obraz wejściowy.
     * @param withSaturation Czy stosować wysycenie.
     * @return Obraz wynikowy.
     */
    public BufferedImage addImages(BufferedImage image1, BufferedImage image2, boolean withSaturation) {
        verifyImageCompatibility(image1, image2);

        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getRaster().getSample(x, y, 0); // Piksel z pierwszego obrazu
                int pixel2 = image2.getRaster().getSample(x, y, 0); // Piksel z drugiego obrazu
                int result = pixel1 + pixel2;

                if (withSaturation) {
                    result = Math.min(255, result); // Wysycenie
                }

                resultImage.getRaster().setSample(x, y, 0, result); // Ustawienie nowej wartości
            }
        }

        return resultImage;
    }

    /**
     * Dodawanie, mnożenie i dzielenie obrazu przez liczbę całkowitą.
     * Algorytm:
     * - Dla każdego piksela stosuje operację (dodawanie, mnożenie, dzielenie).
     * - Jeśli aktywne jest wysycenie, wartości są obcinane do zakresu 0-255.
     *
     * @param image Obraz wejściowy.
     * @param scalar Liczba do operacji.
     * @param operation Typ operacji ("add", "multiply", "divide").
     * @param withSaturation Czy stosować wysycenie.
     * @return Obraz wynikowy.
     */
    public BufferedImage applyScalarOperation(BufferedImage image, int scalar, String operation, boolean withSaturation) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int result;

                // Wybór operacji
                switch (operation.toLowerCase()) {
                    case "add":
                        result = pixel + scalar;
                        break;
                    case "multiply":
                        result = pixel * scalar;
                        break;
                    case "divide":
                        result = (scalar != 0) ? pixel / scalar : pixel; // Uniknięcie dzielenia przez zero
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported operation: " + operation);
                }

                if (withSaturation) {
                    result = Math.min(255, Math.max(0, result)); // Wysycenie
                }

                resultImage.getRaster().setSample(x, y, 0, result);
            }
        }

        return resultImage;
    }

    /**
     * Różnica bezwzględna obrazów.
     * Algorytm:
     * - Oblicza moduł różnicy wartości pikseli z dwóch obrazów.
     *
     * @param image1 Pierwszy obraz wejściowy.
     * @param image2 Drugi obraz wejściowy.
     * @return Obraz wynikowy.
     */
    public BufferedImage absoluteDifference(BufferedImage image1, BufferedImage image2) {
        verifyImageCompatibility(image1, image2);

        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getRaster().getSample(x, y, 0); // Piksel z pierwszego obrazu
                int pixel2 = image2.getRaster().getSample(x, y, 0); // Piksel z drugiego obrazu
                int result = Math.abs(pixel1 - pixel2); // Różnica bezwzględna

                resultImage.getRaster().setSample(x, y, 0, result); // Ustawienie nowej wartości
            }
        }

        return resultImage;
    }

    /**
     * Sprawdza zgodność typów i rozmiarów obrazów wejściowych.
     *
     * @param image1 Pierwszy obraz.
     * @param image2 Drugi obraz.
     */
    private void verifyImageCompatibility(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            throw new IllegalArgumentException("Images must have the same dimensions.");
        }

        if (image1.getType() != BufferedImage.TYPE_BYTE_GRAY || image2.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Images must be in grayscale (TYPE_BYTE_GRAY).");
        }
    }
}
