package org.example.histogram;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class HistogramPanel extends JPanel {
    private BufferedImage image;

    private final HistogramDataGenerator dataGenerator;
    private final HistogramDrawer drawer;

    private int[][] colorHistograms;   // [3][256] -> R, G, B
    private int[] overallHistogram;    // [256] -> jasność
    private int maxHistogramValue;
    private boolean isColorImage;

    // Marginesy i parametry wizualne
    private static final int MARGIN_LEFT = 40;
    private static final int MARGIN_BOTTOM = 30;
    private static final int MARGIN_TOP = 20;

    // Flaga do przełączania log scale w rysowaniu
    private boolean useLogScale = false;

    public HistogramPanel(LUTGenerator lutGenerator, HistogramDataGenerator dataGenerator, HistogramDrawer drawer) {
        this.dataGenerator = dataGenerator;
        this.drawer = drawer;
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton showLUTButton = new JButton("Show LUT");
        showLUTButton.addActionListener(e -> showLUT());
        buttonPanel.add(showLUTButton);

        JButton showRGBButton = new JButton("Show RGB Histograms");
        showRGBButton.addActionListener(e -> showRGBHistograms());
        buttonPanel.add(showRGBButton);

        JButton showDataButton = new JButton("Show Histogram Data");
        showDataButton.addActionListener(e -> showHistogramData());
        buttonPanel.add(showDataButton);

        // Przycisk do przełączania logarytmicznej skali
        JButton toggleLogScale = new JButton("Toggle Log Scale");
        toggleLogScale.addActionListener(e -> {
            useLogScale = !useLogScale;
            repaint();
        });
        buttonPanel.add(toggleLogScale);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        if (image != null) {
            generateHistograms();
        }
        repaint();
    }

    private void generateHistograms() {
        isColorImage = isColorImage(image);

        // Overall (grayscale-luminance) histogram
        overallHistogram = dataGenerator.generateOverallHistogram(image);

        if (isColorImage) {
            // Kanały R, G, B
            colorHistograms = dataGenerator.generateColorHistograms(image);
        }

        if (isColorImage) {
            int maxR = dataGenerator.getMaxValue(colorHistograms[0]);
            int maxG = dataGenerator.getMaxValue(colorHistograms[1]);
            int maxB = dataGenerator.getMaxValue(colorHistograms[2]);
            int maxOverall = dataGenerator.getMaxValue(overallHistogram);
            maxHistogramValue = Math.max(maxOverall, Math.max(maxR, Math.max(maxG, maxB)));
        } else {
            maxHistogramValue = dataGenerator.getMaxValue(overallHistogram);
        }
    }

    /**
     * Prosta metoda do wykrywania, czy obraz jest "kolorowy".
     * Sprawdza, czy znajdzie się piksel, w którym R != G lub G != B.
     */
    private boolean isColorImage(BufferedImage image) {
        if (image == null) return false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int red   = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue  =  pixel        & 0xFF;
                if (red != green || green != blue) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showLUT() {
        if (overallHistogram == null) {
            JOptionPane.showMessageDialog(this, "LUT data is not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame lutFrame = new JFrame("LUT Table");
        lutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea lutTextArea = new JTextArea();
        lutTextArea.setEditable(false);

        StringBuilder lutText = new StringBuilder("LUT Table:\n");
        lutText.append("Intensity | Count\n");
        lutText.append("----------|------\n");
        for (int i = 0; i < overallHistogram.length; i++) {
            lutText.append(String.format("    %3d   | %d%n", i, overallHistogram[i]));
        }
        lutTextArea.setText(lutText.toString());

        JScrollPane scrollPane = new JScrollPane(lutTextArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        lutFrame.add(scrollPane);
        lutFrame.setSize(300, 400);
        lutFrame.setLocationRelativeTo(this);
        lutFrame.setVisible(true);
    }

    private void showRGBHistograms() {
        if (!isColorImage || colorHistograms == null) {
            JOptionPane.showMessageDialog(this, "RGB histograms are only available for color images.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame rgbFrame = new JFrame("RGB Histograms");
        rgbFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        rgbFrame.setLayout(new GridLayout(3, 1));

        rgbFrame.add(createHistogramPanel(colorHistograms[0], Color.RED,   "Red Histogram"));
        rgbFrame.add(createHistogramPanel(colorHistograms[1], Color.GREEN, "Green Histogram"));
        rgbFrame.add(createHistogramPanel(colorHistograms[2], Color.BLUE,  "Blue Histogram"));

        rgbFrame.setSize(800, 600);
        rgbFrame.setLocationRelativeTo(this);
        rgbFrame.setVisible(true);
    }

    private JPanel createHistogramPanel(int[] histogram, Color color, String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int w = getWidth();
                int h = getHeight();

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int binWidth = Math.max(1, (w - 80) / histogram.length);

                drawer.drawHistogram(
                        g2d,
                        histogram,
                        maxHistogramValue,
                        w,
                        h,
                        binWidth,
                        MARGIN_LEFT,
                        MARGIN_BOTTOM,
                        MARGIN_TOP,
                        color,
                        useLogScale
                );

                g2d.dispose();
            }
        };

        panel.setPreferredSize(new Dimension(800, 200));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

//    private void showHistogramData() {
//        if (overallHistogram == null) {
//            JOptionPane.showMessageDialog(this, "No histogram data available.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        int totalPixels = IntStream.of(overallHistogram).sum();
//        int maxIndex = IntStream.range(0, overallHistogram.length)
//                .reduce((i, j) -> overallHistogram[i] > overallHistogram[j] ? i : j)
//                .orElse(0);
//        int minIndex = IntStream.range(0, overallHistogram.length)
//                .filter(i -> overallHistogram[i] > 0)
//                .findFirst()
//                .orElse(0);
//
//        double mean = IntStream.range(0, overallHistogram.length)
//                .mapToDouble(i -> i * overallHistogram[i])
//                .sum() / totalPixels;
//
//        JFrame dataFrame = new JFrame("Histogram Data");
//        dataFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//
//        JTextArea dataTextArea = new JTextArea();
//        dataTextArea.setEditable(false);
//
//        StringBuilder dataText = new StringBuilder();
//        dataText.append("Total Pixels: ").append(totalPixels).append("\n");
//        dataText.append("Mean Intensity: ").append(String.format("%.2f", mean)).append("\n");
//        dataText.append("Min Intensity: ").append(minIndex).append("\n");
//        dataText.append("Max Intensity: ").append(maxIndex).append("\n");
//
//        dataTextArea.setText(dataText.toString());
//        dataFrame.add(new JScrollPane(dataTextArea));
//        dataFrame.setSize(300, 200);
//        dataFrame.setLocationRelativeTo(this);
//        dataFrame.setVisible(true);
//    }

    private void showHistogramData() {
        if (overallHistogram == null) {
            JOptionPane.showMessageDialog(this, "No histogram data available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Oblicz dane statystyczne
        HistogramDataGenerator.HistogramStatistics stats = dataGenerator.calculateStatistics(image);

        int totalPixels = IntStream.of(overallHistogram).sum();
        int maxIndex = IntStream.range(0, overallHistogram.length)
                .reduce((i, j) -> overallHistogram[i] > overallHistogram[j] ? i : j)
                .orElse(0);
        int minIndex = IntStream.range(0, overallHistogram.length)
                .filter(i -> overallHistogram[i] > 0)
                .findFirst()
                .orElse(0);

        JFrame dataFrame = new JFrame("Histogram Data");
        dataFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea dataTextArea = new JTextArea();
        dataTextArea.setEditable(false);

        StringBuilder dataText = new StringBuilder();
        dataText.append("Total Pixels: ").append(totalPixels).append("\n");
        dataText.append("Mean Intensity: ").append(String.format("%.2f", stats.getMean())).append("\n");
        dataText.append("Standard Deviation: ").append(String.format("%.2f", stats.getStandardDeviation())).append("\n");
        dataText.append("Median Intensity: ").append(stats.getMedian()).append("\n");
        dataText.append("Min Intensity: ").append(minIndex).append("\n");
        dataText.append("Max Intensity: ").append(maxIndex).append("\n");

        dataTextArea.setText(dataText.toString());
        dataFrame.add(new JScrollPane(dataTextArea));
        dataFrame.setSize(300, 200);
        dataFrame.setLocationRelativeTo(this);
        dataFrame.setVisible(true);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (overallHistogram == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight() - 100; // Miejsce na panel z przyciskami

        int binWidth = Math.max(1, (w - 80) / overallHistogram.length);

        drawer.drawHistogram(
                g2d,
                overallHistogram,
                maxHistogramValue,
                w,
                h,
                binWidth,
                MARGIN_LEFT,
                MARGIN_BOTTOM,
                MARGIN_TOP,
                Color.BLACK,
                useLogScale
        );
        g2d.dispose();
    }
}
