package org.example.mathoperations;

import java.awt.image.BufferedImage;

public class MultiArgumentImageProcessor {

    /**
     * Dodawanie obrazów z opcją wysycenia.
     */
    public BufferedImage addImages(BufferedImage image1, BufferedImage image2, boolean withSaturation) {
        verifyImageCompatibility(image1, image2);

        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getRaster().getSample(x, y, 0);
                int pixel2 = image2.getRaster().getSample(x, y, 0);
                int result = pixel1 + pixel2;

                if (withSaturation) {
                    result = Math.min(255, result); // Wysycenie
                }

                resultImage.getRaster().setSample(x, y, 0, result);
            }
        }

        return resultImage;
    }

    /**
     * Dodawanie, mnożenie i dzielenie obrazu przez liczbę całkowitą z opcją wysycenia.
     */
    public BufferedImage applyScalarOperation(BufferedImage image, int scalar, String operation, boolean withSaturation) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int result;

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
     */
    public BufferedImage absoluteDifference(BufferedImage image1, BufferedImage image2) {
        verifyImageCompatibility(image1, image2);

        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getRaster().getSample(x, y, 0);
                int pixel2 = image2.getRaster().getSample(x, y, 0);
                int result = Math.abs(pixel1 - pixel2);

                resultImage.getRaster().setSample(x, y, 0, result);
            }
        }

        return resultImage;
    }

    /**
     * Weryfikacja zgodności obrazów pod kątem typów i rozmiarów.
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
