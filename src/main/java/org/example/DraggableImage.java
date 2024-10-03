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
    private final BufferedImage originalImage; // Oryginalny obraz
    private BufferedImage scaledImage;         // Przeskalowany obraz
    private final ImageService imageService;
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;

    public DraggableImage(BufferedImage image, ImageService imageService, JPanel imagePanel, JFileChooser fileChooser) {
        super(new ImageIcon(image));
        this.originalImage = image;
        this.scaledImage = image;
        this.imageService = imageService;
        this.imagePanel = imagePanel;
        this.fileChooser = fileChooser;

        // Tworzenie menu kontekstowego
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

    // Tworzenie menu kontekstowego z opcjami: duplikuj, zapisz, zamknij, generuj histogram
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

    // Zmiana współczynnika skalowania
    public void setScaleFactor(double scaleFactor) {
        // Domyślny współczynnik skalowania
        int newWidth = (int) (originalImage.getWidth() * scaleFactor);
        int newHeight = (int) (originalImage.getHeight() * scaleFactor);

        // Skalowanie obrazu
        scaledImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // Aktualizacja ikony i rozmiaru
        setIcon(new ImageIcon(scaledImage));
        setSize(new Dimension(newWidth, newHeight));
        revalidate();
        repaint();
    }

    // Duplikowanie obrazu
    private void duplicateImage() {
        BufferedImage duplicated = imageService.duplicateImage(originalImage);
        DraggableImage duplicate = new DraggableImage(duplicated, imageService, imagePanel, fileChooser);
        imagePanel.add(duplicate);
        duplicate.setBounds(getX() + 20, getY() + 20, duplicated.getWidth(), duplicated.getHeight());
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    // Zapisz obraz do pliku
    private void saveImage() {
        int result = fileChooser.showSaveDialog(imagePanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imageService.saveImageToFile(originalImage, file);
        }
    }

    // Zamknij obraz
    private void closeImage() {
        imagePanel.remove(this);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    // Generowanie histogramu
    private void generateHistogram() {
        JFrame histogramFrame = new JFrame("Image Histogram");
        histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (originalImage != null) {
            HistogramPanel histogramPanel = new HistogramPanel(imageService);
            histogramPanel.setImage(originalImage);
            histogramFrame.add(histogramPanel);
            histogramFrame.pack();
            histogramFrame.setLocationRelativeTo(this);
            histogramFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No image loaded to generate histogram.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
