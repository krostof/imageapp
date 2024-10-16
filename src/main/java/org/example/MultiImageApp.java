package org.example;

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
    private final ImageService imageService;
    private final ImageLoader imageLoader;  // Nowa instancja ImageLoader
    private final ImageSaver imageSaver;    // Nowa instancja ImageSaver
    private final ImageDuplicator imageDuplicator; // Nowa instancja ImageDuplicator
    private DraggableImage selectedImage;

    public MultiImageApp() {
        super("Multi Image Interface");

        imageService = new ImageService(); // Inicjalizacja ImageService
        imageLoader = new ImageLoader();   // Inicjalizacja ImageLoader
        imageSaver = new ImageSaver();     // Inicjalizacja ImageSaver
        imageDuplicator = new ImageDuplicator(); // Inicjalizacja ImageDuplicator
        fileChooser = new JFileChooser();
        imagePanel = new JPanel(null);
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        // Dodanie obsługi Drag & Drop
        enableDragAndDrop();

        // Dodanie menu
        createMenuBar();

        // Konfiguracja okna
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void enableDragAndDrop() {
        // Ustawienie DropTarget na panel obrazów
        imagePanel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    // Zaakceptowanie akcji przeciągnięcia i upuszczenia
                    dtde.acceptDrop(dtde.getDropAction());
                    // Pobranie plików przeciągniętych do aplikacji
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    // Przetwarzanie przeciągniętych plików
                    for (File file : droppedFiles) {
                        loadImage(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu "File"
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("Open Image");
        openMenuItem.addActionListener(e -> openImage());
        JMenuItem saveMenuItem = new JMenuItem("Save Image");
        saveMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                saveImage(selectedImage.getImage());
            }
        });
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);

        // Menu "Operations"
        JMenu operationsMenu = new JMenu("Operations");
        JMenuItem duplicateMenuItem = new JMenuItem("Duplicate Image");
        duplicateMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                duplicateImage(selectedImage, selectedImage.getImage());
            }
        });

        JMenuItem histogramMenuItem = new JMenuItem("Generate Histogram");
        histogramMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                generateHistogram(selectedImage.getImage());
            }
        });

        JMenuItem stretchMenuItem = new JMenuItem("Apply Linear Stretch");
        stretchMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                applyLinearStretch(selectedImage, selectedImage.getImage());
            }
        });

        operationsMenu.add(duplicateMenuItem);
        operationsMenu.add(histogramMenuItem);
        operationsMenu.add(stretchMenuItem);

        // Dodanie menu do paska menu
        menuBar.add(fileMenu);
        menuBar.add(operationsMenu);

        setJMenuBar(menuBar);
    }

    private void openImage() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadImage(file);
        }
    }

    private void loadImage(File file) {
        BufferedImage image = imageLoader.loadImageFromFile(file);
        if (image != null) {
            addImageToPanel(image);
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

    private void saveImage(BufferedImage image) {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imageSaver.saveImageToFile(image, file);
        }
    }

    private void duplicateImage(DraggableImage originalImage, BufferedImage image) {
        BufferedImage duplicatedImage = imageDuplicator.duplicateImage(image);
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

    private void generateHistogram(BufferedImage image) {
        int[][] histogram = imageService.generateHistogram(image);

        // Tworzenie panelu histogramu
        HistogramPanel histogramPanel = new HistogramPanel(histogram, image);

        // Wyświetlanie histogramu w nowym oknie
        JFrame histogramFrame = new JFrame("Histogram");
        histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        histogramFrame.getContentPane().add(histogramPanel);
        histogramFrame.pack();
        histogramFrame.setVisible(true);
    }




    private void applyLinearStretch(DraggableImage draggableImage, BufferedImage image) {
        imageService.applyLinearStretch(image);
        draggableImage.updateImage(image);  // Aktualizuj obraz w DraggableImage
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiImageApp::new);
    }
}
