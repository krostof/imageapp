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
        int binWidth = (width - 80) / numBins;
        int marginLeft = 40;
        int marginBottom = 30;
        int marginTop = 20;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        int[] grayHistogram = new int[numBins];
        for (int i = 0; i < numBins; i++) {
            grayHistogram[i] = histogram[0][i] + histogram[1][i] + histogram[2][i];
        }

        int maxFrequency = 0;
        for (int value : grayHistogram) {
            if (value > maxFrequency) {
                maxFrequency = value;
            }
        }

        g2d.setColor(Color.BLACK);
        for (int i = 0; i < numBins; i++) {
            int barHeight = (int) ((double) grayHistogram[i] / maxFrequency * (height - marginBottom - marginTop));
            g2d.fillRect(i * binWidth + marginLeft, height - barHeight - marginBottom, binWidth, barHeight);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawLine(marginLeft, height - marginBottom, width - marginLeft, height - marginBottom);
        g2d.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom);

        g2d.drawString("0", marginLeft - 10, height - marginBottom + 15);
        g2d.drawString("255", width - marginLeft, height - marginBottom + 15);

        g2d.drawString("0", marginLeft - 25, height - marginBottom);
        g2d.drawString(Integer.toString(maxFrequency), marginLeft - 35, marginTop + 10);

        g2d.dispose();
    }
}
