package org.example.grayscale;

import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;

/**
 * Service for processing grayscale image operations.
 */
@AllArgsConstructor
public class GrayscaleImageProcessorService {

    private final GrayscaleImageProcessor operations;

    public BufferedImage negateImage(BufferedImage image) {
        return operations.negateImage(image);
    }

    public BufferedImage quantizeImage(BufferedImage image, int levels) {
        return operations.quantizeImage(image, levels);
    }

    public BufferedImage binarizeImage(BufferedImage image, int threshold) {
        return operations.binarizeImage(image, threshold);
    }

    public BufferedImage thresholdWithGrayLevels(BufferedImage image, int threshold) {
        return operations.thresholdWithGrayLevels(image, threshold);
    }
}
