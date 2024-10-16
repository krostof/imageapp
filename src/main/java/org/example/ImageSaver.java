package org.example;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageSaver {
    public void saveImageToFile(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "png", file); // Możesz zmienić format na inny, np. "jpg"
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
