package org.example;

import java.awt.image.BufferedImage;

public class ImageDuplicator {
    public BufferedImage duplicateImage(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                newImage.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return newImage;
    }
}
