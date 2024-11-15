package org.example;

import lombok.AllArgsConstructor;
import org.example.linearstreach.LinearStretchProcessor;

import java.awt.image.BufferedImage;
import java.io.File;

@AllArgsConstructor
public class ImageService {

    private final ImageLoader imageLoader;
    private final ImageSaver imageSaver;
    private final ImageDuplicator imageDuplicator;
    private final LinearStretchProcessor linearStretchProcessor;
    private final HistogramEqualizer histogramEqualizer;

    public BufferedImage loadImageFromFile(File file) {
        return imageLoader.loadImage(file);
    }

    public void saveImageToFile(BufferedImage image, File file) {
        imageSaver.saveImage(image, file);
    }

    public BufferedImage duplicateImage(BufferedImage image) {
        return imageDuplicator.duplicateImage(image);
    }

    public void applyLinearStretch(BufferedImage image, boolean withClipping, double clippingPercentage) {
        linearStretchProcessor.applyLinearStretch(image, withClipping, clippingPercentage);
    }
    public void applyHistogramEqualization(BufferedImage image) {
        histogramEqualizer.applyHistogramEqualization(image);
    }
}
