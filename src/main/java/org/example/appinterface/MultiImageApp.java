package org.example.appinterface;



import lombok.Getter;
import org.example.DraggableImage;
import org.example.HistogramEqualizer;
import org.example.histogram.HistogramPanel;
import org.example.linearstreach.LinearStretchProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class MultiImageApp extends JFrame {
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;
    @Getter
    private DraggableImage selectedImage;

    public MultiImageApp() {
        super("Multi Image Interface");

        fileChooser = new JFileChooser();
        imagePanel = new JPanel(null);
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        enableDragAndDrop();
        createMenuBar();

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void enableDragAndDrop() {
        imagePanel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : droppedFiles) {
                        loadImage(file);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MultiImageApp.this, "Failed to load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Dodanie menu operacji
        OperationsMenu operationsMenu = new OperationsMenu(this);
        menuBar.add(operationsMenu.getMenu());

        setJMenuBar(menuBar);
    }

    private void loadImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            addImageToPanel(image);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addImageToPanel(BufferedImage image) {
        DraggableImage draggableImage = new DraggableImage(image, imagePanel, this);
        draggableImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedImage(draggableImage);
            }
        });
        imagePanel.add(draggableImage);
        draggableImage.setBounds(0, 0, image.getWidth(), image.getHeight());
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void setSelectedImage(DraggableImage draggableImage) {
        this.selectedImage = draggableImage;
        System.out.println("Selected image at position: " + draggableImage.getX() + ", " + draggableImage.getY());
    }

    public void duplicateImage(DraggableImage originalImage, BufferedImage image) {
        BufferedImage duplicatedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics g = duplicatedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        DraggableImage newImage = new DraggableImage(duplicatedImage, imagePanel, this);
        newImage.setBounds(originalImage.getX() + 20, originalImage.getY() + 20, duplicatedImage.getWidth(), duplicatedImage.getHeight());
        newImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedImage(newImage);
            }
        });
        imagePanel.add(newImage);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    public void generateHistogram(BufferedImage image) {
        HistogramPanel histogramPanel = new HistogramPanel(image);

        JFrame histogramFrame = new JFrame("Histogram");
        histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        histogramFrame.getContentPane().add(histogramPanel);
        histogramFrame.pack();
        histogramFrame.setVisible(true);
    }

    public void applyLinearStretch(DraggableImage draggableImage, BufferedImage image, boolean withClipping, double clippingPercentage) {
        LinearStretchProcessor processor = new LinearStretchProcessor();
        processor.applyLinearStretch(image, withClipping, clippingPercentage);
        draggableImage.updateImage(image);
        JOptionPane.showMessageDialog(this, "Linear stretch applied successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void applyHistogramEqualization(DraggableImage draggableImage, BufferedImage image) {
        HistogramEqualizer equalizer = new HistogramEqualizer();
        equalizer.applyHistogramEqualization(image);
        draggableImage.updateImage(image);
        JOptionPane.showMessageDialog(this, "Histogram equalization applied successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiImageApp::new);
    }
}
