package org.example.projectaverage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class GUIController {
    private static DefaultListModel<File> imageListModel = new DefaultListModel<>();

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Image Sequence Processor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel windowLabel = new JLabel("Set Moving Average Window:");
        JTextField windowField = new JTextField("3", 5);
        JButton addImagesButton = new JButton("Add Images");
        JButton removeImageButton = new JButton("Remove Selected Image");
        JButton moveUpButton = new JButton("Move Up");
        JButton moveDownButton = new JButton("Move Down");
        JButton processButton = new JButton("Process and Create Video");

        JList<File> imageList = new JList<>(imageListModel);
        JScrollPane imageScrollPane = new JScrollPane(imageList);
        imageScrollPane.setPreferredSize(new Dimension(400, 200));

        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.add(windowLabel);
        controlsPanel.add(windowField);
        controlsPanel.add(addImagesButton);
        controlsPanel.add(removeImageButton);
        controlsPanel.add(moveUpButton);
        controlsPanel.add(moveDownButton);
        controlsPanel.add(processButton);

        panel.add(imageScrollPane, BorderLayout.CENTER);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        frame.add(panel);

        addImagesButton.addActionListener(e -> addImages(frame));
        removeImageButton.addActionListener(e -> removeSelectedImage(imageList));
        moveUpButton.addActionListener(e -> moveSelectedImageUp(imageList));
        moveDownButton.addActionListener(e -> moveSelectedImageDown(imageList));
        processButton.addActionListener(e -> processImages(frame, windowField));

        frame.setVisible(true);
    }

    private static void addImages(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png"));
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            for (File file : fileChooser.getSelectedFiles()) {
                imageListModel.addElement(file);
            }
        }
    }

    private static void removeSelectedImage(JList<File> imageList) {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex != -1) {
            imageListModel.remove(selectedIndex);
        }
    }

    private static void moveSelectedImageUp(JList<File> imageList) {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex > 0) {
            File selectedFile = imageListModel.get(selectedIndex);
            imageListModel.remove(selectedIndex);
            imageListModel.add(selectedIndex - 1, selectedFile);
            imageList.setSelectedIndex(selectedIndex - 1);
        }
    }

    private static void moveSelectedImageDown(JList<File> imageList) {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex < imageListModel.size() - 1) {
            File selectedFile = imageListModel.get(selectedIndex);
            imageListModel.remove(selectedIndex);
            imageListModel.add(selectedIndex + 1, selectedFile);
            imageList.setSelectedIndex(selectedIndex + 1);
        }
    }

    private static void processImages(JFrame frame, JTextField windowField) {
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

        List<File> selectedImages = Collections.list(imageListModel.elements());
        ImageAverageProcessor.processImages(selectedImages, windowSize);
        JOptionPane.showMessageDialog(frame, "Video Created Successfully!");
    }
}
