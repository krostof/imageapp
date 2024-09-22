package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class MultiImageApp extends JFrame {
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;
    private final ImageService imageService;

    public MultiImageApp() {
        super("Multi Image Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        fileChooser = new JFileChooser();
        imageService = new ImageService();

        // Panel to display draggable images
        imagePanel = new JPanel(null);
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for open image button
        JPanel panel = new JPanel();
        JButton openButton = new JButton("Open Image");
        panel.add(openButton);
        add(panel, BorderLayout.SOUTH);

        // Adding drag and drop support for images
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
                    ex.printStackTrace();
                }
            }
        });

        // Button action to open images
        openButton.addActionListener(e -> openImage());

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Function to open an image from file chooser
    private void openImage() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadImage(file);
        }
    }

    // Load image and add to panel
    private void loadImage(File file) {
        BufferedImage image = imageService.loadImageFromFile(file);
        if (image != null) {
            DraggableImage draggableImage = new DraggableImage(image, imageService, imagePanel, fileChooser);
            imagePanel.add(draggableImage);
            draggableImage.setBounds(0, 0, image.getWidth(), image.getHeight());
            imagePanel.revalidate();
            imagePanel.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiImageApp::new);
    }
}
