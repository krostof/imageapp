package org.example.histogram;

import java.awt.*;

public class HistogramDrawer {

    /**
     * Rysuje histogram na podstawie dostarczonych danych.
     * Algorytm:
     * - Normalizuje wartości histogramu na podstawie maksymalnej wartości.
     * - Rysuje słupki reprezentujące wartości histogramu.
     * - Dodaje osie i opisy dla skali.
     *
     * @param g2d Kontekst graficzny do rysowania.
     * @param histogram Tablica z wartościami histogramu (0-255).
     * @param maxHistogramValue Maksymalna wartość histogramu.
     * @param width Szerokość obszaru rysowania.
     * @param height Wysokość obszaru rysowania.
     * @param binWidth Szerokość jednego słupka histogramu.
     * @param marginLeft Margines od lewej strony.
     * @param marginBottom Margines od dołu.
     * @param marginTop Margines od góry.
     * @param color Kolor słupków histogramu.
     */
    public void drawHistogram(Graphics2D g2d, int[] histogram, int maxHistogramValue, int width, int height, int binWidth, int marginLeft, int marginBottom, int marginTop, Color color) {
        g2d.setColor(color);

        // Iteracja po wartościach histogramu i rysowanie słupków
        for (int i = 0; i < histogram.length; i++) {
            double normalizedValue = (double) histogram[i] / maxHistogramValue; // Normalizacja
            int barHeight = (int) (normalizedValue * (height - marginBottom - marginTop)); // Wysokość słupka
            g2d.fillRect(i * binWidth + marginLeft, height - barHeight - marginBottom, binWidth, barHeight);
        }

        // Rysowanie osi X i Y
        g2d.setColor(Color.BLACK);
        g2d.drawLine(marginLeft, height - marginBottom, width - marginLeft, height - marginBottom); // Oś X
        g2d.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom); // Oś Y

        // Opis skali
        g2d.drawString("0", marginLeft - 10, height - marginBottom + 15);
        g2d.drawString("255", width - marginLeft - 20, height - marginBottom + 15);
        g2d.drawString("0", marginLeft - 25, height - marginBottom);
        g2d.drawString(String.valueOf(maxHistogramValue), marginLeft - 35, marginTop + 10);
    }
}
