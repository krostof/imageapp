package org.example.mathoperations;

import java.awt.image.BufferedImage;

public class LogicalImageProcessor {

    /**
     * Operacja NOT na obrazie.
     */
    public BufferedImage notOperation(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int result = 255 - pixel; // Inwersja bitowa
                resultImage.getRaster().setSample(x, y, 0, result);
            }
        }

        return resultImage;
    }

    /**
     * Operacje logiczne AND, OR, XOR.
     */
    public BufferedImage logicalOperation(BufferedImage image1, BufferedImage image2, String operation) {
        verifyImageCompatibility(image1, image2);

        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getRaster().getSample(x, y, 0);
                int pixel2 = image2.getRaster().getSample(x, y, 0);
                int result;

                switch (operation.toLowerCase()) {
                    case "and":
                        result = pixel1 & pixel2; // Operacja bitowa AND
                        break;
                    case "or":
                        result = pixel1 | pixel2; // Operacja bitowa OR
                        break;
                    case "xor":
                        result = pixel1 ^ pixel2; // Operacja bitowa XOR
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported logical operation: " + operation);
                }

                resultImage.getRaster().setSample(x, y, 0, result);
            }
        }

        return resultImage;
    }

    /**
     * Konwersja obrazu z 8-bitowego (monochromatycznego) na 1-bitowy (binarna maska).
     */
    public BufferedImage convertToBinaryMask(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int binaryPixel = (pixel > threshold) ? 255 : 0;
                binaryImage.getRaster().setSample(x, y, 0, binaryPixel);
            }
        }

        return binaryImage;
    }

    /**
     * Konwersja obrazu z maski binarnej (1-bitowej) na obraz 8-bitowy.
     */
    public BufferedImage convertToMonochromeMask(BufferedImage binaryImage) {
        int width = binaryImage.getWidth();
        int height = binaryImage.getHeight();
        BufferedImage monochromeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = binaryImage.getRaster().getSample(x, y, 0);
                int grayPixel = (pixel > 0) ? 255 : 0; // Konwersja na 8-bitową skalę szarości
                monochromeImage.getRaster().setSample(x, y, 0, grayPixel);
            }
        }

        return monochromeImage;
    }

    /**
     * Weryfikacja zgodności obrazów.
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
