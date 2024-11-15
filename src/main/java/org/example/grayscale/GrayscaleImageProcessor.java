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
}
