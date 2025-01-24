package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HistogramStretching {

    /**
     Rozciąganie histogramu w zadanym przez użytkownika zakresie: p1-p2 (obraz źródłowy) do
     zakresu q3-q4 (obraz wynikowy).
     */
    public BufferedImage stretchHistogram(BufferedImage image, int p1, int p2, int q3, int q4) {
        if (image == null) {
            throw new IllegalArgumentException("Input image cannot be null.");
        }
        // Walidacja parametrów rozciągania
        if (p1 < 0 || p2 > 255 || p1 >= p2) {
            throw new IllegalArgumentException("Invalid source range [p1..p2]. Must be within [0..255], p1 < p2.");
        }
        if (q3 < 0 || q4 > 255 || q3 >= q4) {
            throw new IllegalArgumentException("Invalid target range [q3..q4]. Must be within [0..255], q3 < q4.");
        }

        BufferedImage grayImage = ensureGray(image);

        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            if (i <= p1) {
                lut[i] = q3;
            } else if (i >= p2) {
                lut[i] = q4;
            } else {
                double fraction = (double) (i - p1) / (p2 - p1);
                lut[i] = (int) (q3 + fraction * (q4 - q3));
            }
        }

        int[] pixels = grayImage.getRaster().getPixels(0, 0, width, height, (int[]) null);

        for (int i = 0; i < pixels.length; i++) {
            int oldVal = pixels[i];
            pixels[i] = lut[oldVal];
        }

        BufferedImage stretchedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        stretchedImage.getRaster().setPixels(0, 0, width, height, pixels);

        return stretchedImage;
    }

    private BufferedImage ensureGray(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return image;
        }
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return gray;
    }
}
