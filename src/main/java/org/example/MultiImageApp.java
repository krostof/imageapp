package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class MultiImageApp extends JFrame {
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;
    private final ImageService imageService;
    private DraggableImage draggableImage;

    public MultiImageApp() {
        super("Multi Image Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        fileChooser = new JFileChooser();
        imageService = new ImageService();

        // Panel do wyświetlania obrazów z obsługą przewijania
        imagePanel = new JPanel(null);
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        // Panel na dole z przyciskiem "Otwórz obraz"
        JPanel panel = new JPanel();
        JButton openButton = new JButton("Open Image");
        panel.add(openButton);
        add(panel, BorderLayout.SOUTH);

        // Menu rozwijane do wyboru powiększenia
        JComboBox<String> zoomComboBox = new JComboBox<>(new String[]{
                "Dopasowanie do szerokości", "100%", "50%", "25%", "20%", "10%", "150%", "200%"
        });
        panel.add(zoomComboBox);

        // Obsługa zmiany powiększenia
        zoomComboBox.addActionListener(e -> {
            String selectedZoom = (String) zoomComboBox.getSelectedItem();
            double scaleFactor = switch (Objects.requireNonNull(selectedZoom)) {
                case "Dopasowanie do szerokości" -> (double) imagePanel.getWidth() / draggableImage.getWidth();
                case "100%" -> 1.0;
                case "50%" -> 0.5;
                case "25%" -> 0.25;
                case "20%" -> 0.2;
                case "10%" -> 0.1;
                case "150%" -> 1.5;
                case "200%" -> 2.0;
                default -> 1.0;
            };
            draggableImage.setScaleFactor(scaleFactor);
        });

        // Obsługa przeciągania i upuszczania obrazów
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

        // Obsługa przycisku do otwierania obrazów
        openButton.addActionListener(e -> openImage());

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Funkcja otwierająca obraz
    private void openImage() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadImage(file);
        }
    }

    // Ładowanie obrazu i dodanie go do panelu
    private void loadImage(File file) {
        BufferedImage image = imageService.loadImageFromFile(file);
        if (image != null) {
            draggableImage = new DraggableImage(image, imageService, imagePanel, fileChooser);
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
