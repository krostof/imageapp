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

        // Find the maximum frequency for scaling
        int maxFrequency = 0;
        for (int i = 0; i < numBins; i++) {
            for (int c = 0; c < 3; c++) {
                if (histogram[c][i] > maxFrequency) {
                    maxFrequency = histogram[c][i];
                }
            }
        }

        // Draw histogram bars
        for (int i = 0; i < numBins; i++) {
            int redHeight = (int) ((double) histogram[0][i] / maxFrequency * height);
            int greenHeight = (int) ((double) histogram[1][i] / maxFrequency * height);
            int blueHeight = (int) ((double) histogram[2][i] / maxFrequency * height);

            // Draw bars for each channel
            g2d.setColor(Color.RED);
            g2d.fillRect(i * binWidth, height - redHeight, binWidth, redHeight);

            g2d.setColor(Color.GREEN);
            g2d.fillRect(i * binWidth + binWidth / 3, height - greenHeight, binWidth / 3, greenHeight);

            g2d.setColor(Color.BLUE);
            g2d.fillRect(i * binWidth + 2 * binWidth / 3, height - blueHeight, binWidth / 3, blueHeight);
        }

        // Draw axis labels
        g2d.setColor(Color.BLACK);
        g2d.drawString("0", 5, height - 5);
        g2d.drawString("255", width - 40, height - 5);

        // Draw Y-axis labels
        for (int y = 0; y <= height; y += 10) {
            g2d.drawString(String.valueOf((height - y) * maxFrequency / height), 5, y);
        }

        g2d.dispose();
    }
}
