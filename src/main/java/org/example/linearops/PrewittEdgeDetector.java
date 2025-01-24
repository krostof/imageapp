package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.Map;

/**
 * Detektor krawędzi Prewitta w ośmiu kierunkach (E, SE, S, SW, W, NW, N, NE).
 * Automatycznie dodaje ramkę (1 piksel) i przycina wynik do oryginalnych wymiarów.
 */
public class PrewittEdgeDetector {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Mapa: "E" -> kernel, "SE" -> kernel itd.
    private final Map<String, Mat> prewittKernels;

    public PrewittEdgeDetector() {
        this.prewittKernels = generatePrewittKernels();
    }

    /**
     * Główna metoda do uruchomienia detekcji krawędzi Prewitta.
     * @param inputImage    Obraz wejściowy (BufferedImage).
     * @param direction     Kierunek Prewitta, np. "E", "SE", "S", ...
     * @param borderType    Typ brzegów (Core.BORDER_CONSTANT / BORDER_REFLECT / BORDER_REPLICATE itp.).
     * @param constantValue Wartość brzegów dla BORDER_CONSTANT (0..255).
     */
    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage,
                                                   String direction,
                                                   int borderType,
                                                   int constantValue) {
        // 1. Konwersja do skali szarości (8-bit)
        Mat sourceMat = bufferedImageToMatGray(inputImage);

        // 2. Zamiana na typ float (CV_32F)
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // 3. Pobranie odpowiedniego kernela z mapy
        Mat kernel = prewittKernels.get(direction.toUpperCase());
        if (kernel == null) {
            throw new IllegalArgumentException("Invalid Prewitt direction: " + direction);
        }

        // 4. Filtr z obsługą brzegów
        Mat result = applyFilterWithBorder(sourceMat32F, kernel, borderType, constantValue);

        // 5. Normalizacja do [0..255]
        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX);

        // 6. Konwersja do CV_8U i BufferedImage
        Mat result8U = new Mat();
        result.convertTo(result8U, CvType.CV_8U);
        return matToBufferedImage(result8U);
    }

    /**
     * Generuje osiem filtrów Prewitta na poszczególne kierunki (E, SE, S, SW, W, NW, N, NE).
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

    /**
     * Dodaje ramkę, stosuje filter2D, przycina do oryginalnych wymiarów.
     */
    private Mat applyFilterWithBorder(Mat source, Mat kernel, int borderType, int constantValue) {
        // A. Dodajemy ramkę 1-pikselową
        Mat bordered = createBorders(source, borderType, constantValue);

        // B. Filtr
        Mat filtered = new Mat();
        Imgproc.filter2D(bordered, filtered, CvType.CV_32F, kernel);

        // C. Crop do oryginalnych wymiarów
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

    /**
     * Konwertuje dowolny BufferedImage na Mat w skali szarości (CV_8UC1).
     */
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

    /**
     * Konwertuje Mat (CV_8UC1) do BufferedImage (TYPE_BYTE_GRAY).
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[mat.cols() * mat.rows()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
