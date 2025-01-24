package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.Map;

/**
 * Detektor krawędzi Prewitta w ośmiu kierunkach (E, SE, S, SW, W, NW, N, NE).
 */
public class PrewittEdgeDetector {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final Map<String, Mat> prewittKernels;

    public PrewittEdgeDetector() {
        this.prewittKernels = generatePrewittKernels();
    }

    /**
     * Metoda do uruchomienia detekcji krawędzi Prewitta.
     */
    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage,
                                                   String direction,
                                                   int borderType,
                                                   int constantValue) {
        Mat sourceMat = bufferedImageToMatGray(inputImage);

        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        Mat kernel = prewittKernels.get(direction.toUpperCase());
        if (kernel == null) {
            throw new IllegalArgumentException("Invalid Prewitt direction: " + direction);
        }
        Mat result = applyFilterWithBorder(sourceMat32F, kernel, borderType, constantValue);

        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX);

        Mat result8U = new Mat();
        result.convertTo(result8U, CvType.CV_8U);
        return matToBufferedImage(result8U);
    }

    /**
     * Osiem filtrów Prewitta na poszczególne kierunki (E, SE, S, SW, W, NW, N, NE).
     */
    private Map<String, Mat> generatePrewittKernels() {
        Map<String, Mat> kernels = new HashMap<>();

        kernels.put("E", createKernel(new float[]{
                -1, 0, 1,
                -1, 0, 1,
                -1, 0, 1
        }));

        kernels.put("SE", createKernel(new float[]{
                -1, -1, 0,
                -1,  0,  1,
                0,  1,  1
        }));

        kernels.put("S", createKernel(new float[]{
                -1, -1, -1,
                0,  0,  0,
                1,  1,  1
        }));

        kernels.put("SW", createKernel(new float[]{
                0, -1, -1,
                1,  0, -1,
                1,  1,  0
        }));

        kernels.put("W", createKernel(new float[]{
                1,  0, -1,
                1,  0, -1,
                1,  0, -1
        }));

        kernels.put("NW", createKernel(new float[]{
                1,  1,  0,
                1,  0, -1,
                0, -1, -1
        }));

        kernels.put("N", createKernel(new float[]{
                1,  1,  1,
                0,  0,  0,
                -1, -1, -1
        }));

        kernels.put("NE", createKernel(new float[]{
                0,  1,  1,
                -1,  0,  1,
                -1, -1,  0
        }));

        return kernels;
    }

    private Mat createKernel(float[] values) {
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0, values);
        return kernel;
    }

    private Mat applyFilterWithBorder(Mat source, Mat kernel, int borderType, int constantValue) {
        Mat bordered = createBorders(source, borderType, constantValue);

        Mat filtered = new Mat();
        Imgproc.filter2D(bordered, filtered, CvType.CV_32F, kernel);

        return cropToOriginalSize(filtered, source.size());
    }

    private Mat createBorders(Mat inputMat, int borderType, int constantValue) {
        Mat borderedImage = new Mat();
        if (borderType == Core.BORDER_CONSTANT) {
            Scalar borderVal = new Scalar(constantValue);
            Core.copyMakeBorder(inputMat, borderedImage, 1, 1, 1, 1, Core.BORDER_CONSTANT, borderVal);
        } else {
            Core.copyMakeBorder(inputMat, borderedImage, 1, 1, 1, 1, borderType);
        }
        return borderedImage;
    }

    private Mat cropToOriginalSize(Mat largeMat, Size originalSize) {
        Rect roi = new Rect(1, 1, (int) originalSize.width, (int) originalSize.height);
        return new Mat(largeMat, roi);
    }

    private Mat bufferedImageToMatGray(BufferedImage bi) {
        if (bi == null) {
            throw new IllegalArgumentException("Input image is null.");
        }
        if (bi.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage gray = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            gray.getGraphics().drawImage(bi, 0, 0, null);
            bi = gray;
        }
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[mat.cols() * mat.rows()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
