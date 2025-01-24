package org.example.projectaverage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class ImageAverageInterface {

    private static JFrame frame;
    private static DefaultListModel<File> imageListModel;
    private static JList<File> imageList;
    private static JTextField windowField;

    private static JButton addImagesButton;
    private static JButton removeImageButton;
    private static JButton moveUpButton;
    private static JButton moveDownButton;
    private static JButton removeAllButton;
    private static JButton saveVideoButton;
    private static JButton averageButton;

    public ImageAverageInterface() {
        imageListModel = new DefaultListModel<>();
        imageList = new JList<>(imageListModel);
        windowField = new JTextField("3", 5);

        addImagesButton = new JButton("Add Images");
        removeImageButton = new JButton("Remove Selected Image");
        removeAllButton = new JButton("Remove All Images");
        moveUpButton = new JButton("Move Up");
        moveDownButton = new JButton("Move Down");
        saveVideoButton = new JButton("Save video");
        averageButton = new JButton("Calculate Overall Average");
    }

    public static void createAndShowGUI() {
        frame = new JFrame("Image Sequence Processor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 400);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createListPanel(), BorderLayout.CENTER);
        mainPanel.add(createControlsPanel(), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(imageList);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JLabel windowLabel = new JLabel("Set Moving Average Window:");
        panel.add(windowLabel);
        panel.add(windowField);

        panel.add(addImagesButton);
        panel.add(removeImageButton);
        panel.add(removeAllButton);
        panel.add(moveUpButton);
        panel.add(moveDownButton);
        panel.add(saveVideoButton);
        panel.add(averageButton);

        initListeners();
        return panel;
    }

    private static void initListeners() {
        addImagesButton.addActionListener(e -> addImages());
        removeImageButton.addActionListener(e -> removeSelectedImage());
        moveUpButton.addActionListener(e -> moveSelectedImageUp());
        moveDownButton.addActionListener(e -> moveSelectedImageDown());
        removeAllButton.addActionListener(e -> removeAllImages()); // Listener dla nowego przycisku
        saveVideoButton.addActionListener(e -> saveVideo());
        averageButton.addActionListener(e -> calculateOverallAverage());
    }

    private static void addImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png")
        );

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            for (File file : fileChooser.getSelectedFiles()) {
                imageListModel.addElement(file);
            }
        }
    }

    private static void removeSelectedImage() {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex != -1) {
            imageListModel.remove(selectedIndex);
        }
    }

    private static void moveSelectedImageUp() {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex > 0) {
            File selectedFile = imageListModel.get(selectedIndex);
            imageListModel.remove(selectedIndex);
            imageListModel.add(selectedIndex - 1, selectedFile);
            imageList.setSelectedIndex(selectedIndex - 1);
        }
    }

    private static void moveSelectedImageDown() {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex < imageListModel.size() - 1) {
            File selectedFile = imageListModel.get(selectedIndex);
            imageListModel.remove(selectedIndex);
            imageListModel.add(selectedIndex + 1, selectedFile);
            imageList.setSelectedIndex(selectedIndex + 1);
        }
    }

    private static void removeAllImages() {
        if (imageListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No images to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to remove all images?",
                "Confirm Remove All",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            imageListModel.clear();
            JOptionPane.showMessageDialog(frame, "All images have been removed.");
        }
    }

    private static void saveVideo() {
        if (imageListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No images selected.");
            return;
        }

        int windowSize;
        try {
            windowSize = Integer.parseInt(windowField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid window size.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("output_video.avi"));
        int result = fileChooser.showSaveDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File targetFile = fileChooser.getSelectedFile();

        List<File> selectedImages = Collections.list(imageListModel.elements());

        // 3. Tworzymy wideo w tle (SwingWorker)
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                // Tutaj musimy mieć metodę w ImageAverageProcessor,
                // która pozwala określić docelową ścieżkę
                // np. processImages(List<File>, int, String) albo
                // processImagesCustomPath(selectedImages, windowSize, targetFile.getAbsolutePath())
                return ImageAverageProcessor.processImagesToCustomPath(
                        selectedImages,
                        windowSize,
                        targetFile.getAbsolutePath()
                );
            }

            @Override
            protected void done() {
                try {
                    String outputPath = get();
                    // Wyświetlamy informację o zapisie
                    JOptionPane.showMessageDialog(frame,
                            "Video saved at: " + outputPath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Error creating video: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    /**
     * Obliczanie obrazu średniego i wyświetlanie go w nowym oknie.
     */
    private static void calculateOverallAverage() {
        if (imageListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No images selected.");
            return;
        }

        List<File> selectedImages = Collections.list(imageListModel.elements());

        // Okno wyboru lokalizacji zapisu
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("overall_average.jpg")); // domyślna nazwa pliku
        int result = fileChooser.showSaveDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // Użytkownik anulował wybór
        }
        File outputFile = fileChooser.getSelectedFile();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return ImageAverageProcessor.calculateOverallAverage(selectedImages, outputFile.getAbsolutePath());
            }

            @Override
            protected void done() {
                try {
                    String outputPath = get();
                    if (outputPath != null) {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Overall average image saved at: " + outputPath
                        );
                        showAverageImage(outputPath);
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Failed to calculate overall average."
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Error calculating overall average: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }


    /**
     * Wyświetla wskazany obraz (np. uśredniony) w nowym oknie.
     */
    private static void showAverageImage(String imagePath) {
        JFrame previewFrame = new JFrame("Average Image Preview");
        previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon icon = new ImageIcon(imagePath);
        JLabel imageLabel = new JLabel(icon);
        previewFrame.add(imageLabel, BorderLayout.CENTER);

        previewFrame.pack();
        previewFrame.setLocationRelativeTo(null);
        previewFrame.setVisible(true);
    }
}
