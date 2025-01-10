package org.example;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class GrabCutProcessor {

    /**
     * Zastosowanie algorytmu GrabCut do segmentacji obrazu.
     *
     * @param inputImage Obraz wejściowy w formacie Mat.
     * @param rect       Prostokąt otaczający obiekt (ROI).
     * @param iterCount  Liczba iteracji algorytmu.
     * @return           Obraz binarny z zsegmentowanym obiektem.
     */
    public Mat applyGrabCut(Mat inputImage, Rect rect, int iterCount) {
        // Inicjalizacja maski (wszystkie piksele jako tło)
        Mat mask = new Mat(inputImage.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_BGD));

        // Inicjalizacja modeli tła i pierwszego planu
        Mat bgdModel = new Mat();
        Mat fgdModel = new Mat();

        // Wywołanie algorytmu GrabCut
        Imgproc.grabCut(inputImage, mask, rect, bgdModel, fgdModel, iterCount, Imgproc.GC_INIT_WITH_RECT);

        // Zamiana maski na binarną (tło: 0, obiekt: 255)
        Mat binaryMask = new Mat();
        Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), binaryMask, Core.CMP_EQ);
        binaryMask.convertTo(binaryMask, CvType.CV_8UC1, 255);

        return binaryMask;
    }

    /**
     * Wyizolowanie pierwszego planu na podstawie maski.
     *
     * @param inputImage Obraz wejściowy w formacie Mat.
     * @param binaryMask Maska binarna (obiekt: 255, tło: 0).
     * @return           Obraz z wyizolowanym obiektem pierwszoplanowym.
     */
    public Mat extractForeground(Mat inputImage, Mat binaryMask) {
        Mat foreground = new Mat(inputImage.size(), inputImage.type(), new Scalar(0, 0, 0));
        inputImage.copyTo(foreground, binaryMask);
        return foreground;
    }
}
