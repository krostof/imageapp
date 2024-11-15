package org.example.appinterface;

import org.example.*;
import org.example.grayscale.GrayscaleImageProcessor;
import org.example.grayscale.GrayscaleImageProcessorService;
import org.example.histogram.HistogramDataGenerator;
import org.example.histogram.HistogramDrawer;
import org.example.histogram.HistogramPanel;
import org.example.histogram.LUTGenerator;
import org.example.linearstreach.LinearStretchProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class MultiImageApp extends JFrame {

    private final GrayscaleImageProcessorService grayscaleImageProcessorService;
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;
    private final ImageService imageService;
    private DraggableImage selectedImage;
    private final HistogramStretching histogramStretching;


    public MultiImageApp() {
        super("Multi Image Interface");

        this.imageService = new ImageService(
                new ImageLoader(),
                new ImageSaver(),
                new ImageDuplicator(),
                new LinearStretchProcessor(),
                new HistogramEqualizer(new LUTGenerator())
        );

        this.grayscaleImageProcessorService = new GrayscaleImageProcessorService(new GrayscaleImageProcessor());
        this.histogramStretching = new HistogramStretching();
        this.fileChooser = new JFileChooser();
        this.imagePanel = new JPanel(null);
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

        JMenuItem stretchMenuItem = new JMenuItem("Apply Linear Stretch (No Clipping)");
        stretchMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                applyLinearStretch(selectedImage, selectedImage.getImage(), false, 0);
            }
        });

        JMenuItem stretchWithClippingMenuItem = new JMenuItem("Apply Linear Stretch (5% Clipping)");
        stretchWithClippingMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                applyLinearStretch(selectedImage, selectedImage.getImage(), true, 0.05);
            }
        });

        JMenuItem equalizeHistogramMenuItem = new JMenuItem("Equalize Histogram");
        equalizeHistogramMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                applyHistogramEqualization(selectedImage, selectedImage.getImage());
            }
        });

        JMenuItem negateMenuItem = new JMenuItem("Negate Image");
        negateMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    BufferedImage negatedImage = grayscaleImageProcessorService.negateImage(selectedImage.getImage());
                    selectedImage.updateImage(negatedImage);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Image must be in grayscale for negation.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenu pointOperationsMenu = new JMenu("Point Operations");
        JMenuItem quantizeMenuItem = new JMenuItem("Reduce Grayscale Levels");
        quantizeMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    // Pobranie liczby poziomów szarości od użytkownika
                    String input = JOptionPane.showInputDialog(this, "Enter the number of grayscale levels (2-256):",
                            "Reduce Grayscale Levels", JOptionPane.PLAIN_MESSAGE);

                    if (input != null) {
                        int levels = Integer.parseInt(input); // Konwersja do liczby
                        if (levels < 2 || levels > 256) {
                            throw new IllegalArgumentException("Number of levels must be between 2 and 256.");
                        }

                        // Wywołanie operacji redukcji poziomów szarości
                        BufferedImage quantizedImage = grayscaleImageProcessorService.quantizeImage(selectedImage.getImage(), levels);
                        selectedImage.updateImage(quantizedImage); // Aktualizacja obrazu
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number between 2 and 256.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem binarizeMenuItem = new JMenuItem("Binary Thresholding");
        binarizeMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter the threshold value (0-255):",
                            "Binary Thresholding", JOptionPane.PLAIN_MESSAGE);

                    if (input != null) {
                        int threshold = Integer.parseInt(input); // Konwersja do liczby
                        if (threshold < 0 || threshold > 255) {
                            throw new IllegalArgumentException("Threshold must be between 0 and 255.");
                        }

                        // Wywołanie metody progowania binarnego w serwisie
                        BufferedImage binarizedImage = grayscaleImageProcessorService.binarizeImage(selectedImage.getImage(), threshold);
                        selectedImage.updateImage(binarizedImage);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number between 0 and 255.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenuItem grayLevelThresholdMenuItem = new JMenuItem("Threshold with Gray Levels");
        grayLevelThresholdMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter the threshold value (0-255):",
                            "Threshold with Gray Levels", JOptionPane.PLAIN_MESSAGE);

                    if (input != null) {
                        int threshold = Integer.parseInt(input); // Konwersja do liczby
                        if (threshold < 0 || threshold > 255) {
                            throw new IllegalArgumentException("Threshold must be between 0 and 255.");
                        }

                        // Wywołanie metody progowania w serwisie
                        BufferedImage thresholdedImage = grayscaleImageProcessorService.thresholdWithGrayLevels(selectedImage.getImage(), threshold);
                        selectedImage.updateImage(thresholdedImage); // Aktualizacja obrazu
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number between 0 and 255.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem stretchHistogramMenuItem = new JMenuItem("Stretch Histogram");
        stretchHistogramMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    // Pobranie zakresów od użytkownika
                    String p1Input = JOptionPane.showInputDialog(this, "Enter the lower bound of source range (p1):", "Stretch Histogram", JOptionPane.PLAIN_MESSAGE);
                    String p2Input = JOptionPane.showInputDialog(this, "Enter the upper bound of source range (p2):", "Stretch Histogram", JOptionPane.PLAIN_MESSAGE);
                    String q3Input = JOptionPane.showInputDialog(this, "Enter the lower bound of target range (q3):", "Stretch Histogram", JOptionPane.PLAIN_MESSAGE);
                    String q4Input = JOptionPane.showInputDialog(this, "Enter the upper bound of target range (q4):", "Stretch Histogram", JOptionPane.PLAIN_MESSAGE);

                    if (p1Input != null && p2Input != null && q3Input != null && q4Input != null) {
                        int p1 = Integer.parseInt(p1Input);
                        int p2 = Integer.parseInt(p2Input);
                        int q3 = Integer.parseInt(q3Input);
                        int q4 = Integer.parseInt(q4Input);

                        // Sprawdzenie poprawności zakresów
                        if (p1 >= p2 || q3 >= q4) {
                            throw new IllegalArgumentException("Ensure p1 < p2 and q3 < q4.");
                        }

                        // Wywołanie metody rozciągania histogramu
                        BufferedImage stretchedImage = histogramStretching.stretchHistogram(selectedImage.getImage(), p1, p2, q3, q4);
                        selectedImage.updateImage(stretchedImage); // Aktualizacja obrazu
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

// Dodanie do menu operacji
        operationsMenu.add(stretchHistogramMenuItem);

        operationsMenu.add(stretchHistogramMenuItem);
        operationsMenu.add(duplicateMenuItem);
        operationsMenu.add(histogramMenuItem);
        operationsMenu.add(stretchMenuItem);
        operationsMenu.add(stretchWithClippingMenuItem);
        operationsMenu.add(equalizeHistogramMenuItem);
        pointOperationsMenu.add(grayLevelThresholdMenuItem);
        pointOperationsMenu.add(binarizeMenuItem);
        pointOperationsMenu.add(quantizeMenuItem);
        pointOperationsMenu.add(negateMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(operationsMenu);
        menuBar.add(pointOperationsMenu);

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
        BufferedImage image = imageService.loadImageFromFile(file);
        if (image != null) {
            addImageToPanel(image);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addImageToPanel(BufferedImage image) {
        DraggableImage draggableImage = new DraggableImage(image, imagePanel, this);
        draggableImage.setBounds(0, 0, image.getWidth(), image.getHeight());
        imagePanel.add(draggableImage);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void saveImage(BufferedImage image) {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imageService.saveImageToFile(image, file);
        }
    }

    private void duplicateImage(DraggableImage originalImage, BufferedImage image) {
        BufferedImage duplicatedImage = imageService.duplicateImage(image);
        DraggableImage newImage = new DraggableImage(duplicatedImage, imagePanel, this);
        newImage.setBounds(originalImage.getX() + 20, originalImage.getY() + 20, duplicatedImage.getWidth(), duplicatedImage.getHeight());
        imagePanel.add(newImage);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void generateHistogram(BufferedImage image) {
        // Tworzenie zależności dla HistogramPanel
        LUTGenerator lutGenerator = new LUTGenerator();
        HistogramDataGenerator dataGenerator = new HistogramDataGenerator();
        HistogramDrawer drawer = new HistogramDrawer();

        // Inicjalizacja HistogramPanel z wymaganymi zależnościami
        HistogramPanel histogramPanel = new HistogramPanel(lutGenerator, dataGenerator, drawer);
        histogramPanel.setImage(image);

        // Tworzenie i konfiguracja okna dla histogramu
        JFrame histogramFrame = new JFrame("Histogram");
        histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        histogramFrame.getContentPane().add(histogramPanel);
        histogramFrame.pack();
        histogramFrame.setSize(800, 600); // Ustawienie rozmiaru
        histogramFrame.setLocationRelativeTo(null); // Wyśrodkowanie okna
        histogramFrame.setVisible(true);
    }


    private void applyLinearStretch(DraggableImage draggableImage, BufferedImage image, boolean withClipping, double clippingPercentage) {
        imageService.applyLinearStretch(image, withClipping, clippingPercentage);
        draggableImage.updateImage(image);
    }

    private void applyHistogramEqualization(DraggableImage draggableImage, BufferedImage image) {
        imageService.applyHistogramEqualization(image);
        draggableImage.updateImage(image);
    }

    public void setSelectedImage(DraggableImage selectedImage) {
        this.selectedImage = selectedImage;
        System.out.println("Selected image set: " + selectedImage);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiImageApp::new);
    }
}
