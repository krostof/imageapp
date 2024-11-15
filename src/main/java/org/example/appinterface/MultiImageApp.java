package org.example.appinterface;

import org.example.*;
import org.example.grayscale.GrayscaleImageProcessor;
import org.example.grayscale.GrayscaleImageProcessorService;
import org.example.histogram.HistogramDataGenerator;
import org.example.histogram.HistogramDrawer;
import org.example.histogram.HistogramPanel;
import org.example.histogram.LUTGenerator;
import org.example.linearstreach.LinearStretchProcessor;
import org.example.mathoperations.LogicalImageProcessor;
import org.example.mathoperations.MultiArgumentImageProcessor;

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
    private final MultiArgumentImageProcessor multiArgumentImageProcessor;
    private final LogicalImageProcessor logicalImageProcessor;


    public MultiImageApp() {
        super("Multi Image Interface");

        this.imageService = new ImageService(
                new ImageLoader(),
                new ImageSaver(),
                new ImageDuplicator(),
                new LinearStretchProcessor(),
                new HistogramEqualizer(new LUTGenerator())
        );
        this.logicalImageProcessor = new LogicalImageProcessor();
        this.grayscaleImageProcessorService = new GrayscaleImageProcessorService(new GrayscaleImageProcessor());
        this.histogramStretching = new HistogramStretching();
        this.multiArgumentImageProcessor = new MultiArgumentImageProcessor();
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
        JMenu mathMenu = new JMenu("Math");
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

        JMenuItem thresholdMenuItem = new JMenuItem("Threshold");
        thresholdMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                ThresholdDialog dialog = new ThresholdDialog(this, selectedImage.getImage(), grayscaleImageProcessorService);
                dialog.setVisible(true);

                BufferedImage processedImage = dialog.getProcessedImage();
                if (processedImage != null) {
                    selectedImage.updateImage(processedImage);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem addImagesMenuItem = new JMenuItem("Add Images");
        addImagesMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                // Wybierz drugi obraz z załadowanych w aplikacji
                DraggableImage secondImage = selectImage("Select the second image for addition:");
                if (secondImage != null) {
                    try {
                        boolean withSaturation = JOptionPane.showConfirmDialog(
                                this,
                                "Apply saturation?",
                                "Saturation",
                                JOptionPane.YES_NO_OPTION
                        ) == JOptionPane.YES_OPTION;

                        BufferedImage resultImage = multiArgumentImageProcessor.addImages(
                                selectedImage.getImage(),
                                secondImage.getImage(),
                                withSaturation
                        );
                        selectedImage.updateImage(resultImage); // Aktualizacja obrazu wybranego
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem scalarOperationMenuItem = new JMenuItem("Scalar Operation");
        scalarOperationMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                String[] operations = {"Add", "Multiply", "Divide"};
                String selectedOperation = (String) JOptionPane.showInputDialog(
                        this,
                        "Choose an operation:",
                        "Scalar Operation",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        operations,
                        "Add"
                );

                if (selectedOperation != null) {
                    String input = JOptionPane.showInputDialog(this, "Enter scalar value:");
                    try {
                        int scalar = Integer.parseInt(input);
                        boolean withSaturation = JOptionPane.showConfirmDialog(
                                this,
                                "Apply saturation?",
                                "Saturation",
                                JOptionPane.YES_NO_OPTION
                        ) == JOptionPane.YES_OPTION;

                        BufferedImage resultImage = multiArgumentImageProcessor.applyScalarOperation(
                                selectedImage.getImage(),
                                scalar,
                                selectedOperation.toLowerCase(),
                                withSaturation
                        );
                        selectedImage.updateImage(resultImage);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid scalar value.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem absoluteDifferenceMenuItem = new JMenuItem("Absolute Difference");
        absoluteDifferenceMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                DraggableImage secondImage = selectImage("Select the second image for absolute difference:");
                if (secondImage != null) {
                    try {
                        BufferedImage resultImage = multiArgumentImageProcessor.absoluteDifference(
                                selectedImage.getImage(),
                                secondImage.getImage()
                        );
                        selectedImage.updateImage(resultImage);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });



// NOT
        JMenuItem notMenuItem = new JMenuItem("NOT Operation");
        notMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                BufferedImage result = logicalImageProcessor.notOperation(selectedImage.getImage());
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

// AND, OR, XOR
        String[] logicalOps = {"AND", "OR", "XOR"};
        for (String op : logicalOps) {
            JMenuItem menuItem = new JMenuItem(op + " Operation");
            menuItem.addActionListener(e -> {
                if (selectedImage != null) {
                    DraggableImage secondImage = selectImage("Select the second image for " + op + " operation:");
                    if (secondImage != null) {
                        try {
                            BufferedImage result = logicalImageProcessor.logicalOperation(
                                    selectedImage.getImage(),
                                    secondImage.getImage(),
                                    op.toLowerCase()
                            );
                            selectedImage.updateImage(result);
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            mathMenu.add(menuItem);
        }

// Convert to Binary Mask
        JMenuItem toBinaryMenuItem = new JMenuItem("Convert to Binary Mask");
        toBinaryMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                String input = JOptionPane.showInputDialog(this, "Enter threshold (0-255):");
                try {
                    int threshold = Integer.parseInt(input);
                    BufferedImage binaryImage = logicalImageProcessor.convertToBinaryMask(selectedImage.getImage(), threshold);
                    selectedImage.updateImage(binaryImage);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid threshold value.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

// Convert to Monochrome Mask
        JMenuItem toMonochromeMenuItem = new JMenuItem("Convert to Monochrome Mask");
        toMonochromeMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                BufferedImage monochromeImage = logicalImageProcessor.convertToMonochromeMask(selectedImage.getImage());
                selectedImage.updateImage(monochromeImage);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });




        operationsMenu.add(stretchHistogramMenuItem);
        operationsMenu.add(duplicateMenuItem);
        operationsMenu.add(histogramMenuItem);
        operationsMenu.add(stretchMenuItem);
        operationsMenu.add(stretchWithClippingMenuItem);
        operationsMenu.add(equalizeHistogramMenuItem);
        pointOperationsMenu.add(quantizeMenuItem);
        pointOperationsMenu.add(negateMenuItem);
        pointOperationsMenu.add(thresholdMenuItem);
        mathMenu.add(scalarOperationMenuItem);
        mathMenu.add(addImagesMenuItem);
        mathMenu.add(absoluteDifferenceMenuItem);
        mathMenu.add(notMenuItem);
        mathMenu.add(toBinaryMenuItem);
        mathMenu.add(toMonochromeMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(operationsMenu);
        menuBar.add(pointOperationsMenu);
        menuBar.add(mathMenu);

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
            addImageToPanel(image, file.getName()); // Przekazujemy nazwę pliku
        } else {
            JOptionPane.showMessageDialog(this, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addImageToPanel(BufferedImage image, String fileName) {
        DraggableImage draggableImage = new DraggableImage(image, imagePanel, this, fileName);
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
        DraggableImage newImage = new DraggableImage(duplicatedImage, imagePanel, this,originalImage.getName());
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

    private DraggableImage selectImage(String message) {
        Object[] images = imagePanel.getComponents();
        if (images.length == 0) {
            JOptionPane.showMessageDialog(this, "No images available.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        DraggableImage[] draggableImages = new DraggableImage[images.length];
        for (int i = 0; i < images.length; i++) {
            draggableImages[i] = (DraggableImage) images[i];
        }

        return (DraggableImage) JOptionPane.showInputDialog(
                this,
                message,
                "Select Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                draggableImages,
                draggableImages[0]
        );
    }


    private DraggableImage[] selectTwoImages(String message) {
        Object[] images = imagePanel.getComponents(); // Pobierz wszystkie obrazy w panelu
        if (images.length < 2) {
            JOptionPane.showMessageDialog(this, "At least two images must be loaded.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        DraggableImage[] draggableImages = new DraggableImage[images.length];
        for (int i = 0; i < images.length; i++) {
            draggableImages[i] = (DraggableImage) images[i];
        }

        // Okno dialogowe do wyboru obrazów
        DraggableImage firstImage = (DraggableImage) JOptionPane.showInputDialog(
                this,
                "Select the first image (left):",
                "Select First Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                draggableImages,
                draggableImages[0]
        );

        if (firstImage == null) return null;

        // Usuń wybrany obraz z opcji wyboru dla drugiego
        DraggableImage[] remainingImages = java.util.Arrays.stream(draggableImages)
                .filter(image -> image != firstImage)
                .toArray(DraggableImage[]::new);

        DraggableImage secondImage = (DraggableImage) JOptionPane.showInputDialog(
                this,
                "Select the second image (right):",
                "Select Second Image",
                JOptionPane.PLAIN_MESSAGE,
                null,
                remainingImages,
                remainingImages[0]
        );

        if (secondImage == null) return null;

        return new DraggableImage[]{firstImage, secondImage};
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiImageApp::new);
    }
}
