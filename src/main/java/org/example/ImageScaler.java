package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

/*
Zapewnić możliwość zmiany wielkości wyświetlanego obrazu
(zminimalizowany do wielkości okna, zajmujący pełny ekran, wyświetlony
w naturalnej rozdzielczości, itp.)
 */

public class ImageScaler {

    public BufferedImage scaleToWindow(BufferedImage originalImage, int panelWidth, int panelHeight) {
        return resizeImage(originalImage, panelWidth, panelHeight);
    }

    public BufferedImage scaleToFullScreen(BufferedImage originalImage) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        return resizeImage(originalImage, screenWidth, screenHeight);
    }

    public BufferedImage scaleToNaturalSize(BufferedImage originalImage) {
        return originalImage;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight) {
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }
}
