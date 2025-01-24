package org.example.projectaverage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class GUIController {

    private static JFrame frame;
    private static DefaultListModel<File> imageListModel;
    private static JList<File> imageList;
    private static JTextField windowField;

    private static JButton addImagesButton;
    private static JButton removeImageButton;
    private static JButton moveUpButton;
    private static JButton moveDownButton;
    private static JButton processButton;
    private static JButton averageButton;

    public GUIController() {
        imageListModel = new DefaultListModel<>();
        imageList = new JList<>(imageListModel);
        windowField = new JTextField("3", 5);

        addImagesButton = new JButton("Add Images");
        removeImageButton = new JButton("Remove Selected Image");
        moveUpButton = new JButton("Move Up");
        moveDownButton = new JButton("Move Down");
        processButton = new JButton("Open New Window");
        averageButton = new JButton("Calculate Overall Average");
    }

    public static void createAndShowGUI() {
        frame = new JFrame("Image Sequence Processor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);

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
        panel.add(moveUpButton);
        panel.add(moveDownButton);
        panel.add(processButton);
        panel.add(averageButton);

        initListeners();
        return panel;
    }

    private static void initListeners() {
        addImagesButton.addActionListener(e -> addImages());
        removeImageButton.addActionListener(e -> removeSelectedImage());
        moveUpButton.addActionListener(e -> moveSelectedImageUp());
        moveDownButton.addActionListener(e -> moveSelectedImageDown());
        processButton.addActionListener(e -> new VideoPreviewWindow("path/to/your/video"));
        averageButton.addActionListener(e -> calculateOverallAverage());
    }

    private static void addImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png"));
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

    private static void calculateOverallAverage() {
        if (imageListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No images selected.");
            return;
        }

        List<File> selectedImages = Collections.list(imageListModel.elements());

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return ImageAverageProcessor.calculateOverallAverage(selectedImages);
            }

            @Override
            protected void done() {
                try {
                    String outputPath = get();
                    if (outputPath != null) {
                        JOptionPane.showMessageDialog(frame, "Overall average image saved at: " + outputPath);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to calculate overall average.");
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
}