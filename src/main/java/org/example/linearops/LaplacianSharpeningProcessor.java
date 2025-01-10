package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class LaplacianSharpeningProcessor {

    /**
     * Metoda główna: wyostrzanie Laplace'a w skali szarości.
     *
     * @param image         Wejściowy obraz (BufferedImage) – będzie przetwarzany w odcieniach szarości.
     * @param mask          Macierz (3x3) z wartościami operatora Laplace'a (np. -1, -1, 4, itp.).
     * @param borderType    Typ brzegów (np. Core.BORDER_DEFAULT, Core.BORDER_CONSTANT, itp.).
     * @param constantValue Wartość brzegów, gdy borderType = Core.BORDER_CONSTANT.
     * @return              Nowy obraz (BufferedImage) po wyostrzeniu (skala szarości).
     */
    public BufferedImage applyLaplacianSharpening(BufferedImage image,
                                                  int[][] mask,
                                                  int borderType,
                                                  int constantValue) {
        // 1. Konwersja BufferedImage -> Mat (8-bit, szarość)
        Mat matImage = bufferedImageToMat(image);

        // 2. Konwersja do float32, aby móc przechowywać wartości ujemne
        Mat grayImage32F = new Mat();
        matImage.convertTo(grayImage32F, CvType.CV_32F);

        // 3. Tworzenie jądra filtra (kernel) na podstawie maski 3x3
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                kernel.put(i, j, (float) mask[i][j]);
            }
        }

        // 4. Dodanie marginesów (1 piksel z każdej strony) przy użyciu metody pomocniczej
        Mat borderedImage = createBorders(grayImage32F, borderType, constantValue);

        // 5. Zastosowanie filtra 2D (laplace) na obrazie z marginesem
        Mat laplacianResult = new Mat();
        Imgproc.filter2D(borderedImage, laplacianResult, CvType.CV_32F, kernel);

        // 6. Wycięcie (crop) marginesu tak, by wynik miał rozmiar oryginalnego obrazu
        Mat croppedResult = cropToOriginalSize(laplacianResult, grayImage32F.size());

        // 7. Dodanie wyniku Laplace'a do oryginalnego obrazu
        Mat sharpenedImage = new Mat();
        Core.add(grayImage32F, croppedResult, sharpenedImage);

        // 8. Normalizacja do [0..255] (rozciąga min → 0, max → 255)
        Core.normalize(sharpenedImage, sharpenedImage, 0, 255, Core.NORM_MINMAX);

        // 9. Konwersja do formatu 8-bit (CV_8U)
        Mat sharpenedImage8U = new Mat();
        sharpenedImage.convertTo(sharpenedImage8U, CvType.CV_8U);

        // 10. Konwersja Mat -> BufferedImage (szarość) i zwrócenie
        return matToBufferedImage(sharpenedImage8U);
    }

    /**
     * Metoda do tworzenia marginesów (1 piksel z każdej strony) wokół obrazu float32.
     */
    private Mat createBorders(Mat inputMat, int borderType, int constantValue) {
        Mat borderedImage = new Mat();
        if (borderType == Core.BORDER_CONSTANT) {
            // Wartość pikseli brzegowych to constantValue
            Scalar borderVal = new Scalar(constantValue);
            Core.copyMakeBorder(inputMat, borderedImage,
                    1, 1, 1, 1,  // górny, dolny, lewy, prawy
                    Core.BORDER_CONSTANT, borderVal);
        } else {
            // Np. Core.BORDER_DEFAULT, Core.BORDER_REPLICATE, etc.
            Core.copyMakeBorder(inputMat, borderedImage,
                    1, 1, 1, 1,
                    borderType);
        }
        return borderedImage;
    }

    /**
     * Przycięcie obrazu powstałego po dodaniu ramek tak,
     * aby wrócić do pierwotnego rozmiaru.
     */
    private Mat cropToOriginalSize(Mat largeMat, Size originalSz) {
        int x = 1, y = 1;
        int w = (int) originalSz.width;
        int h = (int) originalSz.height;
        Rect roi = new Rect(x, y, w, h); // (startX, startY, width, height)
        return new Mat(largeMat, roi);
    }

    /**
     * Konwersja z BufferedImage (dowolny) do Mat (CV_8UC1), wymuszając skalę szarości.
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Input image is null.");
        }
        // Upewniamy się, że mamy TYPE_BYTE_GRAY
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            gray.getGraphics().drawImage(image, 0, 0, null);
            image = gray;
        }
        // Kopiujemy dane pikseli do macierzy OpenCV
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * Konwersja z Mat (CV_8UC1) do BufferedImage (TYPE_BYTE_GRAY).
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            throw new IllegalArgumentException("Input Mat is null or empty.");
        }
        // Założenie: mat jest 1-kanałowy (CV_8UC1).
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage out = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.rows() * mat.cols() * (int)mat.elemSize()];
        mat.get(0, 0, data);
        out.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return out;
    }
}
