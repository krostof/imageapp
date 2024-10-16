package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

public class HistogramPanel extends JPanel {
    private int[][] histogram;
    private BufferedImage image;

    public HistogramPanel(int[][] histogram, BufferedImage image) {
        this.histogram = histogram;
        this.image = image;
        setPreferredSize(new Dimension(400, 300)); // Dopasowanie rozmiaru panelu
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int height = panelHeight - 100; // Zostawiamy miejsce na statystyki pod wykresem
        int marginLeft = 40;
        int marginRight = 40;
        int marginTop = 20;
        int marginBottom = 30; // Zmniejszamy dolny margines, aby histogram był bliżej dolnej krawędzi

        // Rysowanie ramki i osi
        g2d.setColor(Color.BLACK);
        g2d.drawRect(marginLeft, marginTop, panelWidth - marginLeft - marginRight, height - marginTop - marginBottom); // Ramka wokół histogramu

        // Rysowanie osi X (0 - 255)
        g2d.drawString("0", marginLeft, height - marginBottom + 15); // Umieszczamy 0 bliżej dolnej osi
        g2d.drawString("255", panelWidth - marginRight, height - marginBottom + 15); // 255 po prawej

        // Obliczenia do rysowania histogramu
        int maxValue = 0;
        for (int i = 0; i < 256; i++) {
            for (int channel = 0; channel < 3; channel++) {
                if (histogram[channel][i] > maxValue) {
                    maxValue = histogram[channel][i];
                }
            }
        }

        // Skaluje histogram do dostępnej wysokości (uwzględnia marginesy)
        if (maxValue > 0) {
            double scalingFactor = (height - marginTop - marginBottom) / (double) maxValue;

            // Rysowanie histogramu w czarnym kolorze
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < 256; i++) {
                int totalValue = histogram[0][i] + histogram[1][i] + histogram[2][i]; // Sumujemy wartości z RGB
                int barHeight = (int) (totalValue * scalingFactor); // Zastosowanie współczynnika skalowania
                g2d.fillRect(marginLeft + i * (panelWidth - marginLeft - marginRight) / 256,
                        height - marginBottom - barHeight,
                        (panelWidth - marginLeft - marginRight) / 256,
                        barHeight); // Rysowanie słupków od dołu ramki
            }
        }

        // Wyliczenie statystyk obrazu
        int totalPixels = image.getWidth() * image.getHeight();
        double mean = calculateMean();
        double stdDev = calculateStdDev(mean);
        int min = calculateMin();
        int max = calculateMax();
        int mode = calculateMode();

        // Wyświetlenie statystyk
        DecimalFormat df = new DecimalFormat("#.###");
        g2d.drawString("N: " + totalPixels, marginLeft, height + 20);
        g2d.drawString("Mean: " + df.format(mean), marginLeft, height + 40);
        g2d.drawString("StdDev: " + df.format(stdDev), marginLeft, height + 60);
        g2d.drawString("Min: " + min, panelWidth - marginRight - 80, height + 20);
        g2d.drawString("Max: " + max, panelWidth - marginRight - 80, height + 40);
        g2d.drawString("Mode: " + mode, panelWidth - marginRight - 80, height + 60);
    }

    // Funkcja do obliczania średniej
    private double calculateMean() {
        long sum = 0;
        long count = 0;
        for (int i = 0; i < 256; i++) {
            for (int channel = 0; channel < 3; channel++) {
                sum += histogram[channel][i] * i;
                count += histogram[channel][i];
            }
        }
        return sum / (double) count;
    }

    // Funkcja do obliczania odchylenia standardowego
    private double calculateStdDev(double mean) {
        long sum = 0;
        long count = 0;
        for (int i = 0; i < 256; i++) {
            for (int channel = 0; channel < 3; channel++) {
                long diff = i - (long) mean;
                sum += histogram[channel][i] * diff * diff;
                count += histogram[channel][i];
            }
        }
        return Math.sqrt(sum / (double) count);
    }

    // Funkcja do obliczania minimalnej wartości
    private int calculateMin() {
        for (int i = 0; i < 256; i++) {
            for (int channel = 0; channel < 3; channel++) {
                if (histogram[channel][i] > 0) {
                    return i;
                }
            }
        }
        return 0;
    }

    // Funkcja do obliczania maksymalnej wartości
    private int calculateMax() {
        for (int i = 255; i >= 0; i--) {
            for (int channel = 0; channel < 3; channel++) {
                if (histogram[channel][i] > 0) {
                    return i;
                }
            }
        }
        return 255;
    }

    // Funkcja do obliczania wartości modalnej (najczęstsza wartość)
    private int calculateMode() {
        int mode = 0;
        int maxCount = 0;
        for (int i = 0; i < 256; i++) {
            int totalValue = histogram[0][i] + histogram[1][i] + histogram[2][i];
            if (totalValue > maxCount) {
                maxCount = totalValue;
                mode = i;
            }
        }
        return mode;
    }
}
