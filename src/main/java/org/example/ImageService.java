package org.example;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageService {

    // Generate histogram for an image
    public int[][] generateHistogram(BufferedImage image) {
        int[][] histogram = new int[3][256]; // 3 channels (R, G, B) and 256 intensity levels

        // Traverse each pixel of the image
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y));

                // Increment the intensity value for each channel
                histogram[0][pixelColor.getRed()]++;   // Red channel
                histogram[1][pixelColor.getGreen()]++; // Green channel
                histogram[2][pixelColor.getBlue()]++;  // Blue channel
            }
        }

        return histogram;
    }

    // Load image from file
    public BufferedImage loadImageFromFile(File file) {
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Save image to file
    public void saveImageToFile(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Duplicate a BufferedImage
    public BufferedImage duplicateImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        // Create a new BufferedImage with the same width, height, and image type
        BufferedImage duplicatedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        // Copy the original image to the new one
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                duplicatedImage.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return duplicatedImage;
    }
}
