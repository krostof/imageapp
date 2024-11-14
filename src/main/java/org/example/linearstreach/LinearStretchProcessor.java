package org.example.linearstreach;

import lombok.extern.log4j.Log4j2;
import org.example.histogram.LUTGenerator;

import java.awt.image.BufferedImage;

@Log4j2
public class LinearStretchProcessor {

    private final LUTGenerator lutGenerator;

    public LinearStretchProcessor() {
        this.lutGenerator = new LUTGenerator();
    }

    public void applyLinearStretch(BufferedImage image, boolean withClipping, double clippingPercentage) {
        if (withClipping) {
            log.info("Applying linear stretch with clipping. Clipping percentage: {}%", clippingPercentage * 100);
            applyLinearStretchWithClipping(image, clippingPercentage);
        } else {
            log.info("Applying linear stretch without clipping.");
            applyLinearStretchWithoutClipping(image);
        }
    }

    private void applyLinearStretchWithoutClipping(BufferedImage image) {
        int[] histogram = lutGenerator.generateHistogramLUT(image);
        int totalPixels = image.getWidth() * image.getHeight();
        int[] lut = lutGenerator.generateEqualizationLUT(histogram, totalPixels);

        transformImageWithLUT(image, lut);
    }

    private void applyLinearStretchWithClipping(BufferedImage image, double clippingPercentage) {
        int[] histogram = lutGenerator.generateHistogramLUT(image);
        int totalPixels = image.getWidth() * image.getHeight();

        int clipPixels = (int) (totalPixels * clippingPercentage);
        int lowerBound = findClippingBound(histogram, clipPixels, true);
        int upperBound = findClippingBound(histogram, clipPixels, false);

        int[] lut = generateClippedLUT(lowerBound, upperBound);

        transformImageWithLUT(image, lut);
    }

    private int findClippingBound(int[] histogram, int clipPixels, boolean isLower) {
        int sum = 0;

        if (isLower) {
            for (int i = 0; i < histogram.length; i++) {
                sum += histogram[i];
                if (sum >= clipPixels) {
                    return i;
                }
            }
        } else {
            for (int i = histogram.length - 1; i >= 0; i--) {
                sum += histogram[i];
                if (sum >= clipPixels) {
                    return i;
                }
            }
        }

        return isLower ? 0 : 255;
    }

    private int[] generateClippedLUT(int lowerBound, int upperBound) {
        int[] lut = new int[256];

        for (int i = 0; i < 256; i++) {
            if (i < lowerBound) {
                lut[i] = 0;
            } else if (i > upperBound) {
                lut[i] = 255;
            } else {
                lut[i] = (int) (((i - lowerBound) * 255.0) / (upperBound - lowerBound));
            }
        }

        return lut;
    }

    private void transformImageWithLUT(BufferedImage image, int[] lut) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);

                int red = lut[(pixel >> 16) & 0xFF];
                int green = lut[(pixel >> 8) & 0xFF];
                int blue = lut[pixel & 0xFF];

                int newPixel = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }
    }
}
