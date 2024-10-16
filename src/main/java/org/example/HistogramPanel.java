package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

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
        int numBins = 256; // Ustalamy liczbę binów (256 dla obrazów 8-bitowych)
        int binWidth = (width - 80) / numBins; // Dodajemy margines z każdej strony (lewy i prawy) o 40px
        int marginLeft = 40;
        int marginBottom = 30;
        int marginTop = 20; // Nowy margines od góry

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sumujemy wartości RGB, aby uzyskać histogram szarości
        int[] grayHistogram = new int[numBins];
        for (int i = 0; i < numBins; i++) {
            grayHistogram[i] = histogram[0][i] + histogram[1][i] + histogram[2][i];
        }

        // Szukamy maksymalnej wartości w histogramie
        int maxFrequency = 0;
        for (int value : grayHistogram) {
            if (value > maxFrequency) {
                maxFrequency = value;
            }
        }

        // Rysowanie słupków histogramu (czarny kolor)
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < numBins; i++) {
            int barHeight = (int) ((double) grayHistogram[i] / maxFrequency * (height - marginBottom - marginTop)); // Uwzględniamy margines górny
            g2d.fillRect(i * binWidth + marginLeft, height - barHeight - marginBottom, binWidth, barHeight);
        }

        // Rysowanie osi X i Y (kolor czarny)
        g2d.setColor(Color.BLACK);
        g2d.drawLine(marginLeft, height - marginBottom, width - marginLeft, height - marginBottom); // Oś X
        g2d.drawLine(marginLeft, marginTop, marginLeft, height - marginBottom); // Oś Y

        // Opisy osi X (0 i 255)
        g2d.drawString("0", marginLeft - 10, height - marginBottom + 15); // Etykieta osi X dla 0
        g2d.drawString("255", width - marginLeft, height - marginBottom + 15); // Etykieta osi X dla 255

        // Opis osi Y (wartości minimalna i maksymalna)
        g2d.drawString("0", marginLeft - 25, height - marginBottom); // Y etykieta
        g2d.drawString(Integer.toString(maxFrequency), marginLeft - 35, marginTop + 10); // Max wartość na osi Y

        g2d.dispose();
    }
}
