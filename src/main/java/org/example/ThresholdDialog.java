package org.example;



import lombok.Getter;
import org.example.grayscale.GrayscaleImageProcessorService;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/*
Opracować algorytm i uruchomić funkcjonalności realizującą typowe
operacje punktowe jednoargumentowe na obrazach w odcieniach szarości:
• progowanie binarne z progiem wskazywanym przez użytkownika (w
kontekście histogramu wyświetlonego na ekranie),
• progowanie z zachowaniem poziomów szarości z progiem wskazywanym
przez użytkownika (w kontekście histogramu wyświetlonego na ekranie),
 */

public class ThresholdDialog extends JDialog {
    private final BufferedImage originalImage;
    @Getter
    private BufferedImage processedImage;
    private final JLabel imageLabel;
    private final GrayscaleImageProcessorService grayscaleService;

    public ThresholdDialog(JFrame parent, BufferedImage image, GrayscaleImageProcessorService grayscaleService) {
        super(parent, "Picture Thresholding", true);
        this.originalImage = image;
        this.processedImage = deepCopyImage(image);
        this.grayscaleService = grayscaleService;

        setLayout(new BorderLayout());

        imageLabel = new JLabel(new ImageIcon(processedImage));
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

        JSlider thresholdSlider = new JSlider(0, 255, 0);
        thresholdSlider.setMajorTickSpacing(50);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);

        JTextField thresholdField = new JTextField("0", 5);

        JCheckBox binaryModeCheckBox = new JCheckBox("Binary mode");
        binaryModeCheckBox.setSelected(false);

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
                JOptionPane.showMessageDialog(this, "Enter a valid number (0-255).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        binaryModeCheckBox.addActionListener(e -> {
            int threshold = thresholdSlider.getValue();
            updateImage(threshold, binaryModeCheckBox.isSelected());
        });

        JPanel sliderPanel = new JPanel(new FlowLayout());
        sliderPanel.add(new JLabel("Threshold:"));
        sliderPanel.add(thresholdSlider);
        sliderPanel.add(thresholdField);

        controlsPanel.add(sliderPanel);
        controlsPanel.add(binaryModeCheckBox);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            dispose();
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

        updateImage(0, false);
    }


    private void updateImage(int threshold, boolean binaryMode) {
        if (binaryMode) {
            processedImage = grayscaleService.binarizeImage(originalImage, threshold);
        } else {
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
