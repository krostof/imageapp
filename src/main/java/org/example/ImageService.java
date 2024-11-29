package org.example;

import lombok.AllArgsConstructor;
import org.example.linearops.ImageSmoothingProcessor;
import org.example.linearops.LaplacianSharpeningProcessor;
import org.example.linearops.SobelEdgeDetector;
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
    private final ImageSmoothingProcessor imageSmoothingProcessor;
    private final LaplacianSharpeningProcessor laplacianProcessor;
    private final SobelEdgeDetector sobelEdgeDetector;

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

    public BufferedImage applyAverageSmoothing(BufferedImage image, int kernelSize) {
        return imageSmoothingProcessor.applyAverageSmoothing(image, kernelSize);
    }

    public BufferedImage applyGaussianSmoothing(BufferedImage image, int kernelSize) {
        return imageSmoothingProcessor.applyGaussianSmoothing(image, kernelSize);
    }

    public BufferedImage applyMedianSmoothing(BufferedImage image, int kernelSize) {
        return imageSmoothingProcessor.applyMedianSmoothing(image, kernelSize);
    }

    public BufferedImage applyLaplacianSharpening(BufferedImage image, int[][] mask) {
        return laplacianProcessor.applyLaplacianSharpening(image, mask);
    }
    public BufferedImage applyDirectionalSobel(BufferedImage image, String direction) {
        return sobelEdgeDetector.applyDirectionalSobel(image, direction);
    }
}
