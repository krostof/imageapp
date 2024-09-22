package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HistogramPanel extends JPanel {
    private BufferedImage image;
    private final ImageService imageService;

    public HistogramPanel(ImageService imageService) {
        this.imageService = imageService;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) {
            return;
        }

        int[][] histogram = imageService.generateHistogram(image);
        drawHistogram(g, histogram);
    }

    private void drawHistogram(Graphics g, int[][] histogram) {
        int width = getWidth();
        int height = getHeight();
        int numBins = 256;
        int binWidth = width / numBins;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Zsumowanie wartości RGB do odcieni szarości
        int[] grayHistogram = new int[numBins];
        for (int i = 0; i < numBins; i++) {
            grayHistogram[i] = histogram[0][i] + histogram[1][i] + histogram[2][i];
        }

        // Znajdź maksymalną częstotliwość dla skalowania
        int maxFrequency = 0;
        for (int value : grayHistogram) {
            if (value > maxFrequency) {
                maxFrequency = value;
            }
        }

        // Rysowanie histogramu w jednolitym kolorze
        g2d.setColor(Color.BLUE); // Możesz zmienić kolor na czarny lub inny
        for (int i = 0; i < numBins; i++) {
            int barHeight = (int) ((double) grayHistogram[i] / maxFrequency * height);
            g2d.fillRect(i * binWidth, height - barHeight, binWidth, barHeight);
        }

        // Rysowanie etykiet osi
        g2d.setColor(Color.BLACK);
        g2d.drawString("0", 5, height - 5);
        g2d.drawString("255", width - 40, height - 5);

        g2d.dispose();
    }

}
