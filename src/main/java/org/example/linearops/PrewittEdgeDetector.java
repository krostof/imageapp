package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Klasa demonstrująca wykrywanie krawędzi metodą Prewitta z obsługą ramek (borders).
 */
public class PrewittEdgeDetector {

    /**
     * Główna metoda do wykrywania krawędzi Prewitta.
     *  - Konwertuje obraz do szarości (jeśli to konieczne).
     *  - Dodaje ramkę (1 piksel).
     *  - Stosuje maski Prewitta (Gx, Gy).
     *  - Oblicza moduł gradientu i normalizuje w zakresie [0..255].
     *
     * @param inputImage    Obraz wejściowy (BufferedImage).
     * @param borderType    Typ brzegów (np. Core.BORDER_CONSTANT, Core.BORDER_REPLICATE, itp.).
     * @param constantValue Wartość piksela w brzegach, jeśli borderType = BORDER_CONSTANT.
     * @return              Obraz wyjściowy (BufferedImage) w skali szarości, pokazujący krawędzie.
     */
    public BufferedImage applyPrewittEdgeDetection(BufferedImage inputImage,
                                                   int borderType,
                                                   int constantValue) {
        // 1. Konwersja BufferedImage -> Mat w skali szarości (8-bit)
        Mat sourceMat = bufferedImageToMatGray(inputImage);

        // 2. Konwersja na typ float (CV_32F), bo Prewitt może generować wartości ujemne
        Mat sourceMat32F = new Mat();
        sourceMat.convertTo(sourceMat32F, CvType.CV_32F);

        // 3. Definicje masek Prewitta
        Mat prewittKernelX = createPrewittKernelX();
        Mat prewittKernelY = createPrewittKernelY();

        // 4. Zastosowanie filtra Prewitta z obsługą ramki
        Mat gradX = applyFilterWithBorder(sourceMat32F, prewittKernelX, borderType, constantValue);
        Mat gradY = applyFilterWithBorder(sourceMat32F, prewittKernelY, borderType, constantValue);

        // 5. Obliczenie modułu gradientu: sqrt(gradX^2 + gradY^2)
        Mat magnitude = new Mat();
        Core.magnitude(gradX, gradY, magnitude);

        // 6. Normalizacja do [0..255]
        Core.normalize(magnitude, magnitude, 0, 255, Core.NORM_MINMAX);

        // 7. Konwersja do 8-bit (CV_8U) i BufferedImage
        Mat absMagnitude = new Mat();
        magnitude.convertTo(absMagnitude, CvType.CV_8U);

        return matToBufferedImage(absMagnitude);
    }

    /**
     * Metoda pomocnicza – tworzy ramkę, filtruje obraz jądrem (kernel), przycina do oryginalnego rozmiaru.
     */
    private Mat applyFilterWithBorder(Mat source, Mat kernel, int borderType, int constantValue) {
        // A. Dodajemy ramkę 1-pikselową wokół obrazu
        Mat bordered = createBorders(source, borderType, constantValue);

        // B. Filtrujemy (splot 2D)
        Mat filtered = new Mat();
        Imgproc.filter2D(bordered, filtered, CvType.CV_32F, kernel);

        // C. Wycinamy (crop) ramkę, by wrócić do oryginalnego rozmiaru
        Mat cropped = cropToOriginalSize(filtered, source.size());
        return cropped;
    }

    /**
     * Dodanie ramki 1-pikselowej wokół obrazu.
     */
    private Mat createBorders(Mat inputMat, int borderType, int constantValue) {
        Mat borderedImage = new Mat();
        if (borderType == Core.BORDER_CONSTANT) {
            Scalar borderVal = new Scalar(constantValue);
            Core.copyMakeBorder(inputMat, borderedImage,
                    1, 1, 1, 1,  // górny, dolny, lewy, prawy margines
                    Core.BORDER_CONSTANT, borderVal);
        } else {
            Core.copyMakeBorder(inputMat, borderedImage,
                    1, 1, 1, 1,
                    borderType);
        }
        return borderedImage;
    }

    /**
     * Przycinanie (crop) obrazu z marginesami do pierwotnego rozmiaru.
     */
    private Mat cropToOriginalSize(Mat largeMat, Size originalSize) {
        int x = 1, y = 1;
        int w = (int) originalSize.width;
        int h = (int) originalSize.height;
        Rect roi = new Rect(x, y, w, h);
        return new Mat(largeMat, roi);
    }

    /**
     * Tworzy jądro Prewitta w kierunku X.
     *  -1  0  1
     *  -1  0  1
     *  -1  0  1
     */
    private Mat createPrewittKernelX() {
        Mat kernelX = new Mat(3, 3, CvType.CV_32F);
        kernelX.put(0, 0, new float[]{
                -1, 0, 1,
                -1, 0, 1,
                -1, 0, 1
        });
        return kernelX;
    }

    /**
     * Tworzy jądro Prewitta w kierunku Y.
     *  -1 -1 -1
     *   0  0  0
     *   1  1  1
     */
    private Mat createPrewittKernelY() {
        Mat kernelY = new Mat(3, 3, CvType.CV_32F);
        kernelY.put(0, 0, new float[]{
                -1, -1, -1,
                0,  0,  0,
                1,  1,  1
        });
        return kernelY;
    }

    // ---------------------------------------------------------------------------------------------
    // Pomocnicze metody do konwersji BufferedImage <--> Mat (1-kanałowe, skala szarości)
    // ---------------------------------------------------------------------------------------------

    /**
     * Konwertuje dowolny BufferedImage do Mat w odcieniach szarości (CV_8UC1).
     */
    private Mat bufferedImageToMatGray(BufferedImage bi) {
        if (bi == null) {
            throw new IllegalArgumentException("Input image is null.");
        }
        // Jeśli typ jest inny niż TYPE_BYTE_GRAY, konwertujemy do Gray przez Graphics
        if (bi.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage gray = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            gray.getGraphics().drawImage(bi, 0, 0, null);
            bi = gray;
        }
        // Odczyt pikseli do tablicy
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        // Tworzymy macierz OpenCV (CV_8UC1)
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * Konwertuje Mat (1-kanałowy CV_8UC1) do BufferedImage (TYPE_BYTE_GRAY).
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        if (mat.empty()) {
            throw new IllegalArgumentException("Input Mat is empty.");
        }
        int width = mat.cols();
        int height = mat.rows();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[width * height];
        mat.get(0, 0, data);
        out.getRaster().setDataElements(0, 0, width, height, data);
        return out;
    }
}
