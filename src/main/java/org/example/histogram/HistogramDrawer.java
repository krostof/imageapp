package org.example.histogram;

import java.awt.*;

public class HistogramDrawer {

    /**
     * Rysowanie histogramu
     * Działąnie:
     * - Normalizowanie wartości histogramu na podstawie maxHistogramValue.
     * - Rysowanie słupków reprezentujących wartości histogramu.
     * - Dodawanie osi i opisy skali.
     */
    public void drawHistogram(Graphics2D g2d,
                              int[] histogram,
                              int maxHistogramValue,
                              int width,
                              int height,
                              int binWidth,
                              int marginLeft,
                              int marginBottom,
                              int marginTop,
                              Color color,
                              boolean useLogScale) {

        if (histogram == null || histogram.length != 256) return;
        if (maxHistogramValue <= 0) return;

        g2d.setColor(color);

        double logMax = Math.log(maxHistogramValue + 1);

        for (int i = 0; i < histogram.length; i++) {
            double normalizedValue;
            if (!useLogScale) {
                // Skalowanie liniowe
                normalizedValue = (double) histogram[i] / maxHistogramValue;
            } else {
                // Skalowanie logarytmiczne
                normalizedValue = Math.log(histogram[i] + 1) / logMax;
            }

            int barHeight = (int) (normalizedValue * (height - marginBottom - marginTop));

            int x = i * binWidth + marginLeft;
            int y = height - barHeight - marginBottom;

            g2d.fillRect(x, y, binWidth, barHeight);
        }

        // osie X i Y
        g2d.setColor(Color.BLACK);
        g2d.drawLine(marginLeft, height - marginBottom, width - marginLeft, height - marginBottom); // Oś X
        g2d.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom); // Oś Y

        // osie X (0 i 255)
        g2d.drawString("0", marginLeft - 10, height - marginBottom + 15);
        g2d.drawString("255", width - marginLeft - 20, height - marginBottom + 15);

        // osie Y (0 i maxHistogramValue)
        g2d.drawString("0", marginLeft - 25, height - marginBottom);
        g2d.drawString(String.valueOf(maxHistogramValue), marginLeft - 35, marginTop + 10);
    }
}
