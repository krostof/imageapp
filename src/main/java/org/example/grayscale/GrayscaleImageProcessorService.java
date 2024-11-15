package org.example.grayscale;

import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;

/**
 * Service for processing grayscale image operations.
 */
@AllArgsConstructor
public class GrayscaleImageProcessorService {

    private final GrayscaleImageProcessor grayscaleImageProcessor;

    /**
     * Negates the grayscale image.
     *
     * @param image The input grayscale image.
     * @return The negated image.
     */
    public BufferedImage negateImage(BufferedImage image) {
        return grayscaleImageProcessor.negateImage(image);
    }
}
