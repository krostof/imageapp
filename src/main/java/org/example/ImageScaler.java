package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScaler {

    public BufferedImage scaleToWindow(BufferedImage originalImage, int panelWidth, int panelHeight) {
        return resizeImage(originalImage, panelWidth, panelHeight);
    }

    public BufferedImage scaleToFullScreen(BufferedImage originalImage) {
        // Pobierz wymiary pełnego ekranu
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Skaluj obraz dokładnie do rozmiaru ekranu
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
