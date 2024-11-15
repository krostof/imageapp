package org.example;



import lombok.Getter;
import org.example.grayscale.GrayscaleImageProcessorService;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ThresholdDialog extends JDialog {
    private final BufferedImage originalImage;
    @Getter
    private BufferedImage processedImage;
    private final JLabel imageLabel;
    private final GrayscaleImageProcessorService grayscaleService;

    public ThresholdDialog(JFrame parent, BufferedImage image, GrayscaleImageProcessorService grayscaleService) {
        super(parent, "Picture Thresholding", true);
        this.originalImage = image;
        this.processedImage = deepCopyImage(image); // Aby zachować oryginał
        this.grayscaleService = grayscaleService;

        setLayout(new BorderLayout());

        // Wyświetlanie obrazu
        imageLabel = new JLabel(new ImageIcon(processedImage));
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        // Panel kontroli
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

        // Suwak dla progu (domyślnie na 0)
        JSlider thresholdSlider = new JSlider(0, 255, 0);
        thresholdSlider.setMajorTickSpacing(50);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);

        // Pole tekstowe do wprowadzania wartości (domyślnie na 0)
        JTextField thresholdField = new JTextField("0", 5);

        // Tryb binarny (domyślnie wyłączony)
        JCheckBox binaryModeCheckBox = new JCheckBox("Binary mode");
        binaryModeCheckBox.setSelected(false);

        // Aktualizacja obrazu w zależności od suwaka
        thresholdSlider.addChangeListener(e -> {
            int threshold = thresholdSlider.getValue();
            thresholdField.setText(String.valueOf(threshold));
            updateImage(threshold, binaryModeCheckBox.isSelected());
        });

        thresholdField.addActionListener(e -> {
            try {
                int threshold = Integer.parseInt(thresholdField.getText());
                threshold = Math.max(0, Math.min(255, threshold));
                thresholdSlider.setValue(threshold);
                updateImage(threshold, binaryModeCheckBox.isSelected());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number (0-255).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        binaryModeCheckBox.addActionListener(e -> {
            int threshold = thresholdSlider.getValue();
            updateImage(threshold, binaryModeCheckBox.isSelected());
        });

        // Dodanie elementów do panelu kontrolnego
        JPanel sliderPanel = new JPanel(new FlowLayout());
        sliderPanel.add(new JLabel("Threshold:"));
        sliderPanel.add(thresholdSlider);
        sliderPanel.add(thresholdField);

        controlsPanel.add(sliderPanel);
        controlsPanel.add(binaryModeCheckBox);

        // Przyciski OK/Cancel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            dispose(); // Zamyka okno
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            processedImage = originalImage; // Przywróć oryginał
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        controlsPanel.add(buttonPanel);

        add(controlsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);

        // Ustawienie obrazu początkowego
        updateImage(0, false); // Domyślnie binary mode wyłączony i threshold = 0
    }


    private void updateImage(int threshold, boolean binaryMode) {
        if (binaryMode) {
            // Progowanie binarne
            processedImage = grayscaleService.binarizeImage(originalImage, threshold);
        } else {
            // Progowanie z zachowaniem poziomów szarości
            processedImage = grayscaleService.thresholdWithGrayLevels(originalImage, threshold);
        }
        imageLabel.setIcon(new ImageIcon(processedImage));
    }


    private BufferedImage deepCopyImage(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }
}
