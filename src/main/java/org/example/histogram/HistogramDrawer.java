package org.example.histogram;

import java.awt.*;

public class HistogramDrawer {

    public void drawHistogram(Graphics2D g2d, int[] histogram, int maxHistogramValue, int width, int height, int binWidth, int marginLeft, int marginBottom, int marginTop, Color color) {
        g2d.setColor(color);

        for (int i = 0; i < histogram.length; i++) {
            double normalizedValue = (double) histogram[i] / maxHistogramValue;
            int barHeight = (int) (normalizedValue * (height - marginBottom - marginTop));
            g2d.fillRect(i * binWidth + marginLeft, height - barHeight - marginBottom, binWidth, barHeight);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawLine(marginLeft, height - marginBottom, width - marginLeft, height - marginBottom); // Oś X
        g2d.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom); // Oś Y

        g2d.drawString("0", marginLeft - 10, height - marginBottom + 15);
        g2d.drawString("255", width - marginLeft - 20, height - marginBottom + 15);
        g2d.drawString("0", marginLeft - 25, height - marginBottom);
        g2d.drawString(String.valueOf(maxHistogramValue), marginLeft - 35, marginTop + 10);
    }
}
