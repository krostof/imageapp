package org.example.histogram;

import java.awt.*;

public class HistogramDrawer {

    /**
     * Rysuje histogram na podstawie dostarczonych danych.
     * Algorytm:
     * - Normalizuje wartości histogramu (liniowo lub logarytmicznie) na podstawie maxHistogramValue.
     * - Rysuje słupki reprezentujące wartości histogramu.
     * - Dodaje osie i opisy skali.
     *
     * @param g2d              Kontekst graficzny do rysowania.
     * @param histogram        Tablica z wartościami histogramu (0-255).
     * @param maxHistogramValue Maksymalna wartość histogramu.
     * @param width            Szerokość obszaru rysowania.
     * @param height           Wysokość obszaru rysowania.
     * @param binWidth         Szerokość jednego słupka histogramu.
     * @param marginLeft       Margines od lewej strony.
     * @param marginBottom     Margines od dołu.
     * @param marginTop        Margines od góry.
     * @param color            Kolor słupków histogramu.
     * @param useLogScale      Czy używać skali logarytmicznej (opcjonalnie).
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

        // Rysowanie osi X i Y
        g2d.setColor(Color.BLACK);
        g2d.drawLine(marginLeft, height - marginBottom, width - marginLeft, height - marginBottom); // Oś X
        g2d.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom); // Oś Y

        // Etykiety osi X (0 i 255)
        g2d.drawString("0", marginLeft - 10, height - marginBottom + 15);
        g2d.drawString("255", width - marginLeft - 20, height - marginBottom + 15);

        // Etykiety osi Y (0 i maxHistogramValue)
        g2d.drawString("0", marginLeft - 25, height - marginBottom);
        g2d.drawString(String.valueOf(maxHistogramValue), marginLeft - 35, marginTop + 10);
    }
}
