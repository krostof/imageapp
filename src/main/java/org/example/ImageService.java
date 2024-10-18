package org.example;

import org.example.histogram.HistogramGenerator;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageService {

    private final HistogramGenerator histogramGenerator;
    private final ImageLoader imageLoader;
    private final ImageSaver imageSaver;
    private final ImageDuplicator imageDuplicator;
    private final LinearStretchProcessor linearStretchProcessor;

    public ImageService(HistogramGenerator histogramGenerator, ImageLoader imageLoader, ImageSaver imageSaver, ImageDuplicator imageDuplicator, LinearStretchProcessor linearStretchProcessor) {
        this.histogramGenerator = histogramGenerator;
        this.imageLoader = imageLoader;
        this.imageSaver = imageSaver;
        this.imageDuplicator = imageDuplicator;
        this.linearStretchProcessor = linearStretchProcessor;
    }

    public int[][] getHistogram(BufferedImage image) {
        return histogramGenerator.generateHistogram(image);
    }

    public BufferedImage loadImageFromFile(File file) {
        return imageLoader.loadImage(file);
    }

    public void saveImageToFile(BufferedImage image, File file) {
        imageSaver.saveImage(image, file);
    }

    public BufferedImage duplicateImage(BufferedImage image) {
        return imageDuplicator.duplicateImage(image);
    }

    public void applyLinearStretch(BufferedImage image) {
        linearStretchProcessor.applyLinearStretch(image); // Zastosowanie nowej klasy
    }
}
