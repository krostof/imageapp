package org.example.grayscale;

import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;

/**
 * Service for processing grayscale image operations.
 */
@AllArgsConstructor
public class GrayscaleImageProcessorService {

    private final GrayscaleImageProcessor grayscaleImageProcessor;

    public BufferedImage negateImage(BufferedImage image) {
        return grayscaleImageProcessor.negateImage(image);
    }

    public BufferedImage quantizeImage(BufferedImage image, int levels) {
        return grayscaleImageProcessor.quantizeImage(image, levels);
    }

    public BufferedImage binarizeImage(BufferedImage image, int threshold) {
        return grayscaleImageProcessor.binarizeImage(image, threshold);
    }
}
