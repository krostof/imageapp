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
    private final HistogramProcessor histogramProcessor;

    public DraggableImage(BufferedImage image, ImageService imageService, JPanel imagePanel, JFileChooser fileChooser,HistogramProcessor histogramProcessor) {
        super(new ImageIcon(image));
        this.image = image;
        this.imageService = imageService;
        this.imagePanel = imagePanel;
        this.fileChooser = fileChooser;
        this.histogramProcessor = histogramProcessor;

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
        JMenuItem linearStretchItem = new JMenuItem("Linear Stretch");

        duplicateItem.addActionListener(e -> duplicateImage());
        saveItem.addActionListener(e -> saveImage());
        closeItem.addActionListener(e -> closeImage());
        histogramItem.addActionListener(e -> generateHistogram());
        linearStretchItem.addActionListener(e -> applyLinearStretch());

        popupMenu.add(duplicateItem);
        popupMenu.add(saveItem);
        popupMenu.add(closeItem);
        popupMenu.add(histogramItem);
        popupMenu.add(linearStretchItem);
    }

    private void generateHistogram() {
        JFrame histogramFrame = new JFrame("Image Histogram");
        histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (image != null) {
            HistogramPanel histogramPanel = new HistogramPanel(imageService);
            histogramPanel.setImage(image);
            histogramFrame.add(histogramPanel);
            histogramFrame.pack();
            histogramFrame.setLocationRelativeTo(this);
            histogramFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No image loaded to generate histogram.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void duplicateImage() {
        BufferedImage duplicated = imageService.duplicateImage(image);
        DraggableImage duplicate = new DraggableImage(duplicated, imageService, imagePanel, fileChooser, new HistogramProcessor());
        imagePanel.add(duplicate);
        duplicate.setBounds(getX() + 20, getY() + 20, duplicated.getWidth(), duplicated.getHeight());
        imagePanel.revalidate();
        imagePanel.repaint();
    }


    private void saveImage() {
        int result = fileChooser.showSaveDialog(imagePanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imageService.saveImageToFile(image, file);
        }
    }

    private void closeImage() {
        imagePanel.remove(this);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    public void setScaleFactor(double scaleFactor) {
        int newWidth = (int) (image.getWidth() * scaleFactor);
        int newHeight = (int) (image.getHeight() * scaleFactor);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        setIcon(new ImageIcon(scaledImage));
        setSize(new Dimension(newWidth, newHeight));
        revalidate();
        repaint();
    }

    private void applyLinearStretch() {
        BufferedImage stretchedImage = histogramProcessor.linearStretch(image);
        DraggableImage stretchedDraggableImage = new DraggableImage(stretchedImage, imageService, imagePanel, fileChooser, histogramProcessor);
        imagePanel.add(stretchedDraggableImage);
        stretchedDraggableImage.setBounds(getX() + 20, getY() + 20, stretchedImage.getWidth(), stretchedImage.getHeight());
        imagePanel.revalidate();
        imagePanel.repaint();
    }



}
