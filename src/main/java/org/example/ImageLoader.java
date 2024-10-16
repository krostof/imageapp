package org.example;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageLoader {
    public BufferedImage loadImageFromFile(File file) {
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
