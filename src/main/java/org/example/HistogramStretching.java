package org.example;

import java.awt.image.BufferedImage;

public class HistogramStretching {

    /**
     * Applies histogram stretching to an image in the given source range [p1, p2]
     * to the target range [q3, q4].
     *
     * @param image The input grayscale image.
     * @param p1    The lower bound of the source range.
     * @param p2    The upper bound of the source range.
     * @param q3    The lower bound of the target range.
     * @param q4    The upper bound of the target range.
     * @return The stretched image.
     */
    public BufferedImage stretchHistogram(BufferedImage image, int p1, int p2, int q3, int q4) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Input image must be in grayscale.");
        }
        if (p1 >= p2 || q3 >= q4) {
            throw new IllegalArgumentException("Invalid range values. Ensure p1 < p2 and q3 < q4.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage stretchedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF; // Extract grayscale value
                int newPixelValue;

                if (pixel < p1) {
                    newPixelValue = q3;
                } else if (pixel > p2) {
                    newPixelValue = q4;
                } else {
                    newPixelValue = q3 + ((pixel - p1) * (q4 - q3)) / (p2 - p1);
                }

                // Clamp the new pixel value to the target range
                newPixelValue = Math.max(q3, Math.min(q4, newPixelValue));

                // Build the new grayscale pixel
                int newPixel = (newPixelValue << 16) | (newPixelValue << 8) | newPixelValue;
                stretchedImage.setRGB(x, y, newPixel);
            }
        }

        return stretchedImage;
    }
}
