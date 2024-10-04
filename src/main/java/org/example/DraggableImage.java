package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class DraggableImage extends JLabel {
    private Point initialClick;
    private final JPopupMenu popupMenu;
    private final BufferedImage image;
    private final ImageService imageService;
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;

    public DraggableImage(BufferedImage image, ImageService imageService, JPanel imagePanel, JFileChooser fileChooser) {
        super(new ImageIcon(image));
        this.image = image;
        this.imageService = imageService;
        this.imagePanel = imagePanel;
        this.fileChooser = fileChooser;

        // Create context menu
        popupMenu = new JPopupMenu();
        createContextMenu();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });
    }

    private void createContextMenu() {
        JMenuItem duplicateItem = new JMenuItem("Duplicate");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem closeItem = new JMenuItem("Close");
        JMenuItem histogramItem = new JMenuItem("Generate Histogram");

        duplicateItem.addActionListener(e -> duplicateImage());
        saveItem.addActionListener(e -> saveImage());
        closeItem.addActionListener(e -> closeImage());
        histogramItem.addActionListener(e -> generateHistogram());

        popupMenu.add(duplicateItem);
        popupMenu.add(saveItem);
        popupMenu.add(closeItem);
        popupMenu.add(histogramItem);
    }

    // Generate and display the histogram for this image
    private void generateHistogram() {
        // Create a new JFrame for displaying the histogram
        JFrame histogramFrame = new JFrame("Image Histogram");
        histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Ensure imageService and image are properly set
        if (image != null) {
            HistogramPanel histogramPanel = new HistogramPanel(imageService);
            histogramPanel.setImage(image); // Set the image for the panel
            histogramFrame.add(histogramPanel);
            histogramFrame.pack();
            histogramFrame.setLocationRelativeTo(this);
            histogramFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No image loaded to generate histogram.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Duplicate image and add to panel
    private void duplicateImage() {
        BufferedImage duplicated = imageService.duplicateImage(image);
        DraggableImage duplicate = new DraggableImage(duplicated, imageService, imagePanel, fileChooser);
        imagePanel.add(duplicate);
        duplicate.setBounds(getX() + 20, getY() + 20, duplicated.getWidth(), duplicated.getHeight());
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    // Save image to a selected file
    private void saveImage() {
        int result = fileChooser.showSaveDialog(imagePanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imageService.saveImageToFile(image, file);
        }
    }

    // Remove image from panel
    private void closeImage() {
        imagePanel.remove(this);
        imagePanel.revalidate();
        imagePanel.repaint();
    }
}
