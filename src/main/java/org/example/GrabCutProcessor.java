package org.example;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class GrabCutProcessor {

    /**
     * Zastosowanie algorytmu GrabCut do segmentacji obrazu.
     */
    public Mat applyGrabCut(Mat inputImage, Rect rect, int iterCount) {
        Mat mask = new Mat(inputImage.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_BGD));

        Mat bgdModel = new Mat();
        Mat fgdModel = new Mat();

        Imgproc.grabCut(inputImage, mask, rect, bgdModel, fgdModel, iterCount, Imgproc.GC_INIT_WITH_RECT);

        Mat binaryMask = new Mat();
        Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), binaryMask, Core.CMP_EQ);
        binaryMask.convertTo(binaryMask, CvType.CV_8UC1, 255);

        return binaryMask;
    }

    public Mat extractForeground(Mat inputImage, Mat binaryMask) {
        Mat foreground = new Mat(inputImage.size(), inputImage.type(), new Scalar(0, 0, 0));
        inputImage.copyTo(foreground, binaryMask);
        return foreground;
    }
}
