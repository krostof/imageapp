package org.example.projectaverage;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

public class ImageLoader {

    /**
     * Wczytuje obraz
     * @param path ścieżka do pliku
     * @return obiekt Mat w formacie CV_8U
     */
    public Mat loadImage(String path) {
        Mat image = Imgcodecs.imread(path);
        if (!image.empty()) {
            return image;
        }

        if (path.toLowerCase().endsWith(".tif")) {
            String pngPath = path.replaceAll("(?i)\\.tif$", ".png");
            File tifFile = new File(path);
            File pngFile = new File(pngPath);

            if (tifFile.exists() && tifFile.renameTo(pngFile)) {
                Mat converted = Imgcodecs.imread(pngPath);
                if (!converted.empty()) {
                    return converted;
                }
            }
        }

        throw new IllegalArgumentException("Cannot load image. Unsupported format or path: " + path);
    }
}
