package org.example;

import lombok.AllArgsConstructor;
import org.example.linearops.*;
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
    private final PrewittEdgeDetector prewittEdgeDetector;
    private final BorderFillProcessor borderFillProcessor;
    private final MedianFilterProcessor processor;
    private final CannyEdgeDetector detector;
    private final ShapeFeatureExtractor shapeFeatureExtractor;

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

    public BufferedImage applySmoothing(BufferedImage inputImage, String method, int k, int borderType, int constantValue) {
        return imageSmoothingProcessor.applySmoothing(inputImage, method, k, borderType, constantValue);
    }


    public BufferedImage applyLaplacianSharpening(BufferedImage image, int[][] mask) {
        return laplacianProcessor.applyLaplacianSharpening(image, mask);
    }

    public BufferedImage applyDirectionalSobel(BufferedImage image, String direction) {
        return sobelEdgeDetector.applyDirectionalSobel(image, direction);
    }

    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage) {
        return prewittEdgeDetector.applyPrewittEdgeDetection(inputImage);
    }

    public BufferedImage applyBorderFill(BufferedImage inputImage, int borderTypeCode, int constantValue) {
        return borderFillProcessor.applyBorderFill(inputImage, borderTypeCode, constantValue);
    }

    public BufferedImage applyMedianFilter(BufferedImage inputImage, int kernelSize, int borderTypeCode) {
        return processor.applyMedianFilter(inputImage, kernelSize, borderTypeCode);
    }

    public BufferedImage applyCanny(BufferedImage inputImage, double threshold1, double threshold2, int apertureSize, boolean l2Gradient) {
        return detector.applyCanny(inputImage, threshold1, threshold2, apertureSize, l2Gradient);
    }
    public String calculateShapeFeatures(BufferedImage image) {
        return shapeFeatureExtractor.calculateFeatures(OpenCVUtils.bufferedImageToMat(image));
    }
}
