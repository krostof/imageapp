package org.example.linearops;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class LaplacianSharpeningProcessor {

    /**
     * Metoda główna: wyostrzanie Laplace'a w skali szarości.
     *
     * @param image         Wejściowy obraz typu BufferedImage (przetwarzany w odcieniach szarości).
     * @param mask          Macierz (3x3) z wartościami operatora Laplace'a (np. -1, -1, 4, itp.).
     * @param borderType    Typ brzegów (np. Core.BORDER_DEFAULT, Core.BORDER_CONSTANT, itd.).
     * @param constantValue Wartość brzegów dla BORDER_CONSTANT.
     * @return              Nowy obraz (BufferedImage) po wyostrzeniu (skala szarości).
     */
    public BufferedImage applyLaplacianSharpening(BufferedImage image,
                                                  int[][] mask,
                                                  int borderType,
                                                  int constantValue) {
        // 1. Konwersja BufferedImage -> Mat (skala szarości 8-bit)
        Mat matImage = bufferedImageToMat(image);

        // 2. Zamiana na typ float (32-bit), aby móc stosować filtry wrażliwe na wartości ujemne
        Mat grayImage32F = new Mat();
        matImage.convertTo(grayImage32F, CvType.CV_32F);

        // 3. Tworzenie macierzy jądra (kernel) na podstawie 'mask'
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                kernel.put(i, j, (float) mask[i][j]);
            }
        }

        // 4. Dodanie ramki wokół obrazu (1 piksel z każdej strony)
        Mat borderedImage = createBorders(grayImage32F, borderType, constantValue);

        // 5. Zastosowanie filtracji 2D na powiększonym obrazie
        Mat tempResult = new Mat();
        Imgproc.filter2D(borderedImage, tempResult, CvType.CV_32F, kernel);

        // 6. Przycięcie wyniku do oryginalnego rozmiaru (odcinamy 1-pikselową ramkę)
        Mat laplacianResult = cropToOriginalSize(tempResult, grayImage32F.size());

        // 7. Dodanie wyniku Laplace'a do oryginalnego obrazu
        Mat sharpenedImage = new Mat();
        Core.add(grayImage32F, laplacianResult, sharpenedImage);

        // 8. Normalizacja do zakresu [0,255] (NORM_MINMAX)
        //    UWAGA: może wzmocnić kontrast w zależności od wartości w sharpenedImage.
        Core.normalize(sharpenedImage, sharpenedImage, 0, 255, Core.NORM_MINMAX);

        // 9. Konwersja z powrotem do 8-bitowego obrazu (CV_8U)
        Mat sharpenedImage8U = new Mat();
        sharpenedImage.convertTo(sharpenedImage8U, CvType.CV_8U);

        // 10. Konwersja Mat -> BufferedImage i zwrócenie wyniku
        return matToBufferedImage(sharpenedImage8U);
    }

    /**
     * Pomocnicza metoda do tworzenia obramowania (bordera).
     *
     * @param inputMat      Wejściowy obraz (32-bit float).
     * @param borderType    Typ brzegów (np. Core.BORDER_CONSTANT).
     * @param constantValue Wartość do wypełnienia, gdy borderType = BORDER_CONSTANT.
     * @return              Nowy obraz z dołożonymi ramkami (1 piksel z każdej strony).
     */
    private Mat createBorders(Mat inputMat, int borderType, int constantValue) {
        Mat borderedImage = new Mat();
        if (borderType == Core.BORDER_CONSTANT) {
            Scalar borderValue = new Scalar(constantValue);
            Core.copyMakeBorder(inputMat, borderedImage, 1, 1, 1, 1, Core.BORDER_CONSTANT, borderValue);
        } else {
            Core.copyMakeBorder(inputMat, borderedImage, 1, 1, 1, 1, borderType);
        }
        return borderedImage;
    }

    /**
     * Przycięcie obrazu powstałego po dodaniu ramek tak,
     * aby wrócić do oryginalnego rozmiaru.
     *
     * @param largeMat   Obraz powiększony (np. o 1 piksel z każdej strony).
     * @param originalSz Oryginalny rozmiar (width x height).
     * @return           Wycinek (ROI) o wymiarach oryginalnego obrazu.
     */
    private Mat cropToOriginalSize(Mat largeMat, Size originalSz) {
        // Zakładamy, że poszerzyliśmy o 1 piksel wokół, zatem:
        int x = 1, y = 1;
        int w = (int) originalSz.width;
        int h = (int) originalSz.height;

        // Rect: (początek_x, początek_y, szerokość, wysokość)
        Rect roi = new Rect(x, y, w, h);
        return new Mat(largeMat, roi);
    }

    /**
     * Konwersja z BufferedImage do Mat (zakładamy, że docelowo chcemy 8-bitową skalę szarości).
     *
     * @param image Obraz wejściowy w postaci BufferedImage.
     * @return      Obiekt typu Mat (CV_8UC1).
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        // Jeśli obraz nie jest w TYPE_BYTE_GRAY, konwertujemy go do Gray:
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (image.getType() != type) {
            BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), type);
            grayImage.getGraphics().drawImage(image, 0, 0, null);
            image = grayImage;
        }

        // Pobranie surowych pikseli:
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Stworzenie macierzy (CV_8UC1) i wstawienie danych
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * Konwersja z Mat (CV_8UC1) do BufferedImage (TYPE_BYTE_GRAY).
     *
     * @param mat Obraz typu Mat (zakładamy 8-bit jednowarstwowy).
     * @return    BufferedImage w skali szarości.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);

        byte[] data = new byte[mat.cols() * mat.rows() * (int) mat.elemSize()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

        return image;
    }
}
