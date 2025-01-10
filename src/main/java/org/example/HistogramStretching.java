package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HistogramStretching {

    /**
     * Applies manual histogram stretching in grayscale:
     * Source range [p1..p2] -> Target range [q3..q4].
     *
     * @param image The input image (will be internally converted to grayscale if not already).
     * @param p1    The lower bound of the source range (0..255).
     * @param p2    The upper bound of the source range (0..255), must be < p2.
     * @param q3    The lower bound of the target range (0..255).
     * @param q4    The upper bound of the target range (0..255), must be < q4.
     * @return A new BufferedImage (grayscale) with stretched histogram.
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

        // 1. Upewniamy się, że mamy obraz w skali szarości (8-bit)
        BufferedImage grayImage = ensureGray(image);

        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        // 2. Przygotowanie tablicy LUT (256-elementowej)
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

        // 3. Odczyt wszystkich pikseli „hurtem”
        //    Zwraca tablicę intensywności o rozmiarze width*height
        int[] pixels = grayImage.getRaster().getPixels(0, 0, width, height, (int[]) null);

        // 4. Zastosowanie LUT do każdej próby (wartości piksela)
        for (int i = 0; i < pixels.length; i++) {
            int oldVal = pixels[i];
            pixels[i] = lut[oldVal];
        }

        // 5. Tworzymy obraz wynikowy (również TYPE_BYTE_GRAY)
        BufferedImage stretchedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 6. Wpisujemy zmodyfikowane piksele do nowego obrazu
        stretchedImage.getRaster().setPixels(0, 0, width, height, pixels);

        return stretchedImage;
    }

    /**
     * Metoda pomocnicza: upewnia się, że BufferedImage jest typu TYPE_BYTE_GRAY.
     * Jeśli nie jest, tworzy nowy obraz i rysuje go w skali szarości.
     *
     * @param image Dowolny obraz wejściowy (może być np. RGB).
     * @return      Obraz w skali szarości (TYPE_BYTE_GRAY).
     */
    private BufferedImage ensureGray(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            // Już jest 8-bit Gray – nie musimy nic robić.
            return image;
        }
        // Konwersja do skali szarości
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return gray;
    }
}
