package org.example;

import java.awt.image.BufferedImage;

public class HistogramStretching {

    /**
     * Applies manual histogram stretching:
     * Source range [p1..p2] -> Target range [q3..q4].
     *
     * @param image The input grayscale image (TYPE_BYTE_GRAY).
     * @param p1    The lower bound of the source range.
     * @param p2    The upper bound of the source range.
     * @param q3    The lower bound of the target range.
     * @param q4    The upper bound of the target range.
     * @return A new BufferedImage with stretched histogram.
     */
    public BufferedImage stretchHistogram(BufferedImage image, int p1, int p2, int q3, int q4) {
        if (image == null) {
            throw new IllegalArgumentException("Input image cannot be null.");
        }
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Input image must be TYPE_BYTE_GRAY.");
        }
        // Sprawdzenie poprawności zakresów
        if (p1 < 0 || p2 > 255 || p1 >= p2) {
            throw new IllegalArgumentException("Invalid source range [p1..p2]. Must be within [0..255], p1 < p2.");
        }
        if (q3 < 0 || q4 > 255 || q3 >= q4) {
            throw new IllegalArgumentException("Invalid target range [q3..q4]. Must be within [0..255], q3 < q4.");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Tworzymy LUT (256 elementów) mapującą [p1..p2] -> [q3..q4]
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            if (i <= p1) {
                lut[i] = q3;
            } else if (i >= p2) {
                lut[i] = q4;
            } else {
                // Liniowe skalowanie:
                // fraction = (i - p1) / (p2 - p1)
                double fraction = (double)(i - p1) / (p2 - p1);
                lut[i] = (int)(q3 + fraction * (q4 - q3));
            }
        }

        // Tworzymy nowy obraz wynikowy (TYPE_BYTE_GRAY)
        BufferedImage stretchedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Zastosowanie LUT do każdego piksela
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Odczyt piksela jako grayscale (0..255)
                int oldPixel = image.getRaster().getSample(x, y, 0);
                int newPixelValue = lut[oldPixel];

                // Ustawienie nowej wartości piksela w obrazie wynikowym
                stretchedImage.getRaster().setSample(x, y, 0, newPixelValue);
            }
        }

        return stretchedImage;
    }
}
