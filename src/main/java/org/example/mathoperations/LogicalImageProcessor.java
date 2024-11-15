package org.example.mathoperations;

import java.awt.image.BufferedImage;

public class LogicalImageProcessor {

    /**
     * Wykonuje operację NOT na obrazie.
     * Algorytm:
     * - Dla każdego piksela stosuje inwersję bitową 255 - wartość piksela.
     *
     * @param image Obraz wejściowy w skali szarości lub binarny.
     * @return Obraz po operacji NOT.
     */
    public BufferedImage notOperation(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int result = 255 - pixel; // Inwersja bitowa
                resultImage.getRaster().setSample(x, y, 0, result); // Ustawienie nowej wartości
            }
        }

        return resultImage;
    }

    /**
     * Wykonuje operacje logiczne AND, OR, XOR na dwóch obrazach.
     * Algorytm:
     * - Dla każdego piksela pobiera wartości z obu obrazów.
     * - Stosuje operację logiczną (AND, OR, XOR) na poziomie bitowym.
     *
     * @param image1 Pierwszy obraz wejściowy.
     * @param image2 Drugi obraz wejściowy.
     * @param operation Typ operacji logicznej ("and", "or", "xor").
     * @return Obraz wynikowy po operacji logicznej.
     */
    public BufferedImage logicalOperation(BufferedImage image1, BufferedImage image2, String operation) {
        verifyImageCompatibility(image1, image2);

        int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getRaster().getSample(x, y, 0); // Piksel z pierwszego obrazu
                int pixel2 = image2.getRaster().getSample(x, y, 0); // Piksel z drugiego obrazu
                int result;

                // Wybór operacji logicznej
                switch (operation.toLowerCase()) {
                    case "and":
                        result = pixel1 & pixel2; // Operacja AND
                        break;
                    case "or":
                        result = pixel1 | pixel2; // Operacja OR
                        break;
                    case "xor":
                        result = pixel1 ^ pixel2; // Operacja XOR
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported logical operation: " + operation);
                }

                resultImage.getRaster().setSample(x, y, 0, result); // Ustawienie nowej wartości
            }
        }

        return resultImage;
    }

    /**
     * Konwertuje obraz do binarnej maski.
     * Algorytm:
     * - Porównuje wartość każdego piksela z progiem.
     * - Jeśli wartość > próg, piksel otrzymuje wartość 255, w przeciwnym razie 0.
     *
     * @param image Obraz wejściowy.
     * @param threshold Wartość progu (0-255).
     * @return Obraz binarny.
     */
    public BufferedImage convertToBinaryMask(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int binaryPixel = (pixel > threshold) ? 255 : 0; // Porównanie z progiem
                binaryImage.getRaster().setSample(x, y, 0, binaryPixel); // Ustawienie nowej wartości
            }
        }

        return binaryImage;
    }

    /**
     * Konwertuje obraz binarny na obraz monochromatyczny (8-bitowy).
     * Algorytm:
     * - Dla każdego piksela sprawdza, czy wartość > 0.
     * - Jeśli wartość > 0, piksel przyjmuje wartość 255, w przeciwnym razie 0.
     *
     * @param binaryImage Obraz binarny (1-bitowy).
     * @return Obraz monochromatyczny (8-bitowy).
     */
    public BufferedImage convertToMonochromeMask(BufferedImage binaryImage) {
        int width = binaryImage.getWidth();
        int height = binaryImage.getHeight();
        BufferedImage monochromeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = binaryImage.getRaster().getSample(x, y, 0); // Pobranie wartości piksela
                int grayPixel = (pixel > 0) ? 255 : 0; // Konwersja do 8-bitowej skali szarości
                monochromeImage.getRaster().setSample(x, y, 0, grayPixel); // Ustawienie nowej wartości
            }
        }

        return monochromeImage;
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
