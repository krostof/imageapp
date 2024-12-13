package org.example.mathoperations;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LogicalImageProcessor {

    /**
     * Wykonuje operację NOT na obrazie jednokanałowym (grayscale lub binary).
     * Algorytm:
     * - Dla każdego piksela stosuje inwersję: result = 255 - pixel.
     *
     * @param image Obraz wejściowy w skali szarości lub binarny.
     * @return Obraz po operacji NOT.
     */
    public BufferedImage notOperation(BufferedImage image) {
        verifySingleChannelImage(image);

        int width = image.getWidth();
        int height = image.getHeight();

        // Wynik również w formacie TYPE_BYTE_GRAY
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int result = 255 - pixel;  // Inwersja
                resultImage.getRaster().setSample(x, y, 0, result);
            }
        }

        return resultImage;
    }

    /**
     * Wykonuje operacje logiczne AND, OR, XOR na dwóch obrazach jednokanałowych (grayscale).
     * Algorytm:
     * - Dla każdego piksela pobiera wartości z obu obrazów (pixel1, pixel2).
     * - Stosuje operację bitową & (AND), | (OR) lub ^ (XOR).
     *
     * @param image1 Pierwszy obraz wejściowy (grayscale).
     * @param image2 Drugi obraz wejściowy (grayscale).
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
                int pixel1 = image1.getRaster().getSample(x, y, 0);
                int pixel2 = image2.getRaster().getSample(x, y, 0);
                int result;

                switch (operation.toLowerCase()) {
                    case "and":
                        result = pixel1 & pixel2;
                        break;
                    case "or":
                        result = pixel1 | pixel2;
                        break;
                    case "xor":
                        result = pixel1 ^ pixel2;
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
     * Konwertuje obraz jednokanałowy (grayscale) do binarnej maski 0/255 w formacie TYPE_BYTE_GRAY.
     * Algorytm:
     * - Porównuje wartość każdego piksela z progiem (threshold).
     * - Jeśli wartość > próg, piksel otrzymuje wartość 255, w przeciwnym razie 0.
     *
     * @param image Obraz wejściowy (grayscale).
     * @param threshold Wartość progu (0-255).
     * @return Obraz binarny (w skali 0/255).
     */
    public BufferedImage convertToBinaryMask(BufferedImage image, int threshold) {
        verifySingleChannelImage(image);

        int width = image.getWidth();
        int height = image.getHeight();

        // Dla prostego binarnego maskowania używamy TYPE_BYTE_GRAY zamiast TYPE_BYTE_BINARY
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

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
     * Konwertuje obraz binarny (0/255) na obraz monochromatyczny (8-bitowy).
     * Algorytm:
     * - Dla każdego piksela sprawdza, czy wartość > 0.
     * - Jeśli wartość > 0, piksel przyjmuje wartość 255, w przeciwnym razie 0.
     *
     * @param binaryImage Obraz binarny (jednokanałowy, ale może być TYPE_BYTE_GRAY lub TYPE_BYTE_BINARY).
     * @return Obraz monochromatyczny (TYPE_BYTE_GRAY).
     */
    public BufferedImage convertToMonochromeMask(BufferedImage binaryImage) {
        if (binaryImage == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        // Krok 1. Konwersja z TYPE_BYTE_BINARY (1-bit / indeksowana paleta) do TYPE_BYTE_GRAY (8-bit)
        // tworzymy nowy obraz 8-bitowy w odcieniach szarości
        BufferedImage grayImage = new BufferedImage(binaryImage.getWidth(), binaryImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        // Kopiujemy piksele z binaryImage do grayImage (Java automatycznie odwzoruje paletę 1-bit na 0..255)
        Graphics2D g = grayImage.createGraphics();
        g.drawImage(binaryImage, 0, 0, null);
        g.dispose();

        // Krok 2. Tworzymy docelowy obraz (również TYPE_BYTE_GRAY) i
        // mapujemy piksele: (pixel>0) ? 255 : 0
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        BufferedImage monochromeMask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = grayImage.getRaster().getSample(x, y, 0);
                int maskValue = (pixelValue > 0) ? 255 : 0;
                monochromeMask.getRaster().setSample(x, y, 0, maskValue);
            }
        }

        return monochromeMask;
    }


    /**
     * Sprawdza zgodność dwóch obrazów jednokanałowych:
     * - Oba obrazy nie mogą być null.
     * - Muszą mieć te same wymiary.
     * - Muszą być w typie TYPE_BYTE_GRAY lub TYPE_BYTE_BINARY (choć docelowo działamy na grayscale).
     *
     * @param image1 Pierwszy obraz.
     * @param image2 Drugi obraz.
     */
    private void verifyImageCompatibility(BufferedImage image1, BufferedImage image2) {
        verifySingleChannelImage(image1);
        verifySingleChannelImage(image2);

        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            throw new IllegalArgumentException("Images must have the same dimensions.");
        }
    }

    /**
     * Sprawdza, czy obraz jest nie-null i czy jest w typie TYPE_BYTE_GRAY lub TYPE_BYTE_BINARY.
     *
     * @param image Obraz jednokanałowy do weryfikacji.
     */
    private void verifySingleChannelImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }
        int type = image.getType();
        if (type != BufferedImage.TYPE_BYTE_GRAY && type != BufferedImage.TYPE_BYTE_BINARY) {
            throw new IllegalArgumentException(
                    "Image must be TYPE_BYTE_GRAY or TYPE_BYTE_BINARY (single-channel)."
            );
        }
    }
}
