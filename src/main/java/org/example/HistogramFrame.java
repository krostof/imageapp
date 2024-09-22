package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HistogramFrame extends JFrame {
    private HistogramPanel histogramPanel;

    public HistogramFrame(BufferedImage image, ImageService imageService) {
        super("Image Histogram");

        histogramPanel = new HistogramPanel(imageService);
        histogramPanel.setImage(image);

        // Dodajemy przycisk do zapisu histogramu
        JButton saveButton = new JButton("Save Histogram");
        saveButton.addActionListener(e -> saveHistogram());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);

        add(histogramPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // Funkcja do zapisu histogramu
    private void saveHistogram() {
        BufferedImage image = new BufferedImage(histogramPanel.getWidth(), histogramPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        histogramPanel.paint(g2d);
        g2d.dispose();

        // Użycie JFileChooser do wyboru lokalizacji zapisu
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(image, "png", file);
                JOptionPane.showMessageDialog(this, "Histogram saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving histogram.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
