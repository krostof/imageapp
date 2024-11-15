package org.example.grayscale;

import java.awt.image.BufferedImage;

public class GrayscaleImageProcessor {

    /**
     * Applies negation to a grayscale image.
     *
     * @param image The input grayscale image.
     * @return The negated image.
     */
    public BufferedImage negateImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Input image must be in grayscale.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage negatedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF; // Extract grayscale value
                int negatedPixel = 255 - pixel;        // Negation formula
                int newPixel = (negatedPixel << 16) | (negatedPixel << 8) | negatedPixel; // Rebuild grayscale pixel
                negatedImage.setRGB(x, y, newPixel);
            }
        }
        return negatedImage;
    }

    /**
     * Reduces the number of grayscale levels in the image.
     *
     * @param image The input grayscale image.
     * @param levels The number of grayscale levels to reduce to.
     * @return The quantized image.
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

        int step = 256 / levels; // Size of each grayscale step

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF; // Extract grayscale value
                int quantizedPixel = (pixel / step) * step; // Quantize pixel value
                int newPixel = (quantizedPixel << 16) | (quantizedPixel << 8) | quantizedPixel; // Rebuild grayscale pixel
                quantizedImage.setRGB(x, y, newPixel);
            }
        }

        return quantizedImage;
    }

    /**
     * Binarizes a grayscale image based on a user-defined threshold.
     *
     * @param image The input grayscale image.
     * @param threshold The threshold value (0-255).
     * @return The binarized image.
     */
    public BufferedImage binarizeImage(BufferedImage image, int threshold) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Input image must be in grayscale.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binarizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF; // Pobierz wartość szarości
                int binarizedPixel = pixel > threshold ? 255 : 0; // Progowanie
                int newPixel = (binarizedPixel << 16) | (binarizedPixel << 8) | binarizedPixel; // Budowa piksela
                binarizedImage.setRGB(x, y, newPixel);
            }
        }

        return binarizedImage;
    }

}
