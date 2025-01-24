package org.example.mathoperations;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LogicalImageProcessor {

    /**
     * Operacja NOT na obrazie
     */
    public BufferedImage notOperation(BufferedImage image) {
        verifySingleChannelImage(image);

        int width = image.getWidth();
        int height = image.getHeight();

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
     * Operacje AND, OR, XOR na dwóch obrazach
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
     * Konwertuje obraz jednokanałowy
     */
    public BufferedImage convertToBinaryMask(BufferedImage image, int threshold) {
        verifySingleChannelImage(image);

        int width = image.getWidth();
        int height = image.getHeight();

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
     * Konwertuje obraz binarny na obraz monochromatyczny
     */
    public BufferedImage convertToMonochromeMask(BufferedImage binaryImage) {
        if (binaryImage == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        BufferedImage grayImage = new BufferedImage(binaryImage.getWidth(), binaryImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayImage.createGraphics();
        g.drawImage(binaryImage, 0, 0, null);
        g.dispose();

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
     * Sprawdza zgodność dwóch obrazów
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
