package org.example.appinterface;

import lombok.extern.log4j.Log4j2;
import org.example.*;
import org.example.grayscale.GrayscaleImageProcessor;
import org.example.grayscale.GrayscaleImageProcessorService;
import org.example.histogram.HistogramDataGenerator;
import org.example.histogram.HistogramDrawer;
import org.example.histogram.HistogramPanel;
import org.example.histogram.LUTGenerator;
import org.example.linearops.*;
import org.example.linearstreach.LinearStretchProcessor;
import org.example.mathoperations.LogicalImageProcessor;
import org.example.mathoperations.MultiArgumentImageProcessor;
import org.example.segmentaionlab5.MorphologyProcessor;
import org.example.segmentaionlab5.SegmentationProcessor;
import org.example.segmentaionlab5.SkeletonizationProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

@Log4j2
public class MultiImageApp extends JFrame {

    private final GrayscaleImageProcessorService grayscaleImageProcessorService;
    private final JPanel imagePanel;
    private final JFileChooser fileChooser;
    private final ImageService imageService;
    private DraggableImage selectedImage;
    private final HistogramStretching histogramStretching;
    private final MultiArgumentImageProcessor multiArgumentImageProcessor;
    private final LogicalImageProcessor logicalImageProcessor;
    private final SegmentationProcessor segmentationProcessor;
    private final MorphologyProcessor morphologyProcessor;
    private final SkeletonizationProcessor skeletonProcessor;
    private final JLabel selectedImageLabel;



    public MultiImageApp() {
        super("Multi Image Interface");

        this.imageService = new ImageService(
                new ImageLoader(),
                new ImageSaver(),
                new ImageDuplicator(),
                new LinearStretchProcessor(),
                new HistogramEqualizer(new LUTGenerator()),
                new ImageSmoothingProcessor(new BorderFillProcessor()),
                new LaplacianSharpeningProcessor(),
                new SobelEdgeDetector(new BorderFillProcessor()),
                new PrewittEdgeDetector(new BorderFillProcessor() ),
                new BorderFillProcessor(),
                new MedianFilterProcessor(),
                new CannyEdgeDetector(),
                new ShapeFeatureExtractor()
        );
        this.segmentationProcessor = new SegmentationProcessor();
        this.logicalImageProcessor = new LogicalImageProcessor();
        this.morphologyProcessor = new MorphologyProcessor();
        this.skeletonProcessor = new SkeletonizationProcessor();
        this.grayscaleImageProcessorService = new GrayscaleImageProcessorService(new GrayscaleImageProcessor());
        this.histogramStretching = new HistogramStretching();
        this.multiArgumentImageProcessor = new MultiArgumentImageProcessor();
        this.fileChooser = new JFileChooser();
        this.imagePanel = new JPanel(null);
        selectedImageLabel = new JLabel("No image selected");
        selectedImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(selectedImageLabel, BorderLayout.SOUTH);
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
        JMenu smoothingMenu = createSmoothingMenu();
        menuBar.add(smoothingMenu);
        JMenu morphologyMenu = new JMenu("Morphology");
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
                    log.info("Adding images");
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
                log.info("NOT operation applied to image");
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

        JMenuItem sobelEdgeDetectionItem = new JMenuItem("Directional Sobel Edge Detection");
        sobelEdgeDetectionItem.addActionListener(e -> {
            if (selectedImage != null) {
                String[] directions = {
                        "East",         // 0° - poziomo
                        "South",        // 90° - pionowo
                        "North East",   // 45° - od lewego dolnego rogu do prawego górnego
                        "South East",   // 135° - od lewego górnego rogu do prawego dolnego
                        "North",        // 180° - poziomo, odwrócone
                        "West",         // 270° - pionowo, odwrócone
                        "North West",   // 225° - od prawego dolnego rogu do lewego górnego
                        "South West"    // 315° - od prawego górnego rogu do lewego dolnego
                };

                String selectedDirection = (String) JOptionPane.showInputDialog(
                        this,
                        "Select a Sobel direction:",
                        "Sobel Direction",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        directions,
                        directions[0]
                );

                if (selectedDirection != null) {
                    try {
                        // Prompt the user to select the border type
                        String[] borderOptions = {"Constant", "Reflect", "Replicate"};
                        String selectedBorder = (String) JOptionPane.showInputDialog(
                                this,
                                "Select border type:",
                                "Border Type",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                borderOptions,
                                "Constant"
                        );

                        if (selectedBorder == null) {
                            return; // User cancelled
                        }

                        int borderType;
                        int constantValue = 0;

                        switch (selectedBorder.toLowerCase()) {
                            case "constant":
                                borderType = Core.BORDER_CONSTANT;
                                String constantValueInput = JOptionPane.showInputDialog(
                                        this,
                                        "Enter constant value (0-255):",
                                        "128"
                                );
                                constantValue = Integer.parseInt(constantValueInput);
                                if (constantValue < 0 || constantValue > 255) {
                                    throw new IllegalArgumentException("Constant value must be between 0 and 255.");
                                }
                                break;
                            case "reflect":
                                borderType = Core.BORDER_REFLECT;
                                break;
                            case "replicate":
                                borderType = Core.BORDER_REPLICATE;
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid border type selected.");
                        }

                        // Apply the Sobel edge detection
                        BufferedImage sobelImage = imageService.applyDirectionalSobel(
                                selectedImage.getImage(),
                                selectedDirection,
                                borderType,
                                constantValue
                        );

                        selectedImage.updateImage(sobelImage);
                        imagePanel.repaint(); // Refresh the panel to display the updated image

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error applying Sobel edge detection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        // Tworzymy nowe menu "Segmentation"
        JMenu segmentationMenu = new JMenu("Segmentation");

        // 1. Double Threshold
        JMenuItem doubleThresholdItem = new JMenuItem("Double Threshold");
        doubleThresholdItem.addActionListener(e -> {
            if (selectedImage != null) {
                // Pytamy użytkownika o p1, p2
                String p1Input = JOptionPane.showInputDialog(this, "Enter lower threshold p1:");
                String p2Input = JOptionPane.showInputDialog(this, "Enter upper threshold p2:");
                if (p1Input != null && p2Input != null) {
                    try {
                        int p1 = Integer.parseInt(p1Input);
                        int p2 = Integer.parseInt(p2Input);
                        // Wywołujemy SegmentationProcessor
                        BufferedImage result = segmentationProcessor.doubleThreshold(selectedImage.getImage(), p1, p2);
                        selectedImage.updateImage(result);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid threshold values.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        segmentationMenu.add(doubleThresholdItem);

        // 2. Otsu Threshold
        JMenuItem otsuItem = new JMenuItem("Otsu Threshold");
        otsuItem.addActionListener(e -> {
            if (selectedImage != null) {
                BufferedImage result = segmentationProcessor.otsuThreshold(selectedImage.getImage());
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        segmentationMenu.add(otsuItem);

        // 3. Adaptive Threshold
        JMenuItem adaptiveItem = new JMenuItem("Adaptive Threshold");
        adaptiveItem.addActionListener(e -> {
            if (selectedImage != null) {
                String blockSizeStr = JOptionPane.showInputDialog(this, "Enter block size (odd number, e.g. 11):", "11");
                String cStr = JOptionPane.showInputDialog(this, "Enter constant C (e.g. 2):", "2");
                if (blockSizeStr != null && cStr != null) {
                    try {
                        int blockSize = Integer.parseInt(blockSizeStr);
                        int C = Integer.parseInt(cStr);
                        BufferedImage result = segmentationProcessor.adaptiveThreshold(selectedImage.getImage(), blockSize, C);
                        selectedImage.updateImage(result);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid blockSize/C.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        segmentationMenu.add(adaptiveItem);

        JMenuItem erosionItem = new JMenuItem("Erosion");
        erosionItem.addActionListener(e -> {
            if (selectedImage != null) {
                String shape = chooseStructElementShape();
                if (shape == null) return; // user canceled
                BufferedImage result = morphologyProcessor.erode(selectedImage.getImage(), shape);
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        morphologyMenu.add(erosionItem);

        // Dilation
        JMenuItem dilationItem = new JMenuItem("Dilation");
        dilationItem.addActionListener(e -> {
            if (selectedImage != null) {
                String shape = chooseStructElementShape();
                if (shape == null) return;
                BufferedImage result = morphologyProcessor.dilate(selectedImage.getImage(), shape);
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        morphologyMenu.add(dilationItem);

        // Opening
        JMenuItem openingItem = new JMenuItem("Opening");
        openingItem.addActionListener(e -> {
            if (selectedImage != null) {
                String shape = chooseStructElementShape();
                if (shape == null) return;
                BufferedImage result = morphologyProcessor.opening(selectedImage.getImage(), shape);
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        morphologyMenu.add(openingItem);

        // Closing
        JMenuItem closingItem = new JMenuItem("Closing");
        closingItem.addActionListener(e -> {
            if (selectedImage != null) {
                String shape = chooseStructElementShape();
                if (shape == null) return;
                BufferedImage result = morphologyProcessor.closing(selectedImage.getImage(), shape);
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        morphologyMenu.add(closingItem);

        // Zakładam, że jest w createMenuBar(), obok innych opcji
        JMenuItem convertTo8BitItem = new JMenuItem("Convert to 8-bit Grayscale");
        convertTo8BitItem.addActionListener(e -> {
            if (selectedImage != null) {
                BufferedImage converted = convertTo8BitGray(selectedImage.getImage());
                selectedImage.updateImage(converted);
                JOptionPane.showMessageDialog(this,
                        "Image converted to 8-bit Grayscale.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

// Następnie dodajemy convertTo8BitItem do odpowiedniego menu:
        pointOperationsMenu.add(convertTo8BitItem); // lub operationsMenu.add(convertTo8BitItem);

        JMenuItem skeletonItem = new JMenuItem("Skeletonize");
        skeletonItem.addActionListener(e -> {
            if (selectedImage != null) {
                // Wywołujemy skeletonize z nowej klasy
                BufferedImage result = skeletonProcessor.skeletonize(selectedImage.getImage());
                selectedImage.updateImage(result);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem extractShapeFeaturesMenuItem = new JMenuItem("Extract Shape Features");
        extractShapeFeaturesMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    String features = imageService.calculateShapeFeatures(selectedImage.getImage());
                    Object[] options = {"Save to File", "Close"};
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            features,
                            "Shape Features",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        String userHome = System.getProperty("user.home");
                        File downloadsDir = new File(userHome, "Downloads");
                        File file = new File(downloadsDir, "wynik.txt");

                        ShapeFeatureExtractor.saveResultsToFile(features, file.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Features saved to " + file.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error calculating shape features: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem grabCutMenuItem = new JMenuItem("Apply GrabCut Segmentation");
        grabCutMenuItem.addActionListener(e -> {
            if (selectedImage != null) {
                try {
                    // Wczytanie obrazu i inicjalizacja domyślnego prostokąta
                    Mat inputMat = OpenCVUtils.bufferedImageToMat(selectedImage.getImage());
                    int x = inputMat.cols() / 4;
                    int y = inputMat.rows() / 4;
                    int width = inputMat.cols() / 2;
                    int height = inputMat.rows() / 2;
                    Rect rect = new Rect(x, y, width, height);

                    // Domyślna liczba iteracji
                    int iterCount = 5;

                    // Zastosowanie GrabCut
                    GrabCutProcessor grabCutProcessor = new GrabCutProcessor();
                    Mat binaryMask = grabCutProcessor.applyGrabCut(inputMat, rect, iterCount);
                    Mat foreground = grabCutProcessor.extractForeground(inputMat, binaryMask);

                    // Konwersja wyniku na BufferedImage
                    BufferedImage segmentedImage = OpenCVUtils.matToBufferedImage(foreground);
                    selectedImage.updateImage(segmentedImage);

                    // Informacja o sukcesie
                    JOptionPane.showMessageDialog(this, "GrabCut applied successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error applying GrabCut: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

// Dodanie do menu "Operations"
        operationsMenu.add(grabCutMenuItem);




// Dodanie do menu "Operations"
        operationsMenu.add(grabCutMenuItem);


// Dodanie do menu "Operations"
        operationsMenu.add(grabCutMenuItem);


// Dodanie do menu "Operations"
        operationsMenu.add(grabCutMenuItem);





// Dodanie nowego elementu do istniejącego menu
        operationsMenu.add(extractShapeFeaturesMenuItem);


        morphologyMenu.add(skeletonItem);

        menuBar.add(morphologyMenu);

        // Dodaj "Morphology" do paska menu
        menuBar.add(morphologyMenu);

        menuBar.add(segmentationMenu);
        setJMenuBar(menuBar);

        smoothingMenu.add(sobelEdgeDetectionItem);
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


    private void addMedianFilterMenu(JMenu menu) {
        JMenuItem medianFilterItem = new JMenuItem("Apply Median Filter");
        medianFilterItem.addActionListener(e -> {
            if (selectedImage == null) {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Wybór rozmiaru jądra
            String[] kernelOptions = {"3x3", "5x5", "7x7", "9x9"};
            String kernelSizeOption = (String) JOptionPane.showInputDialog(
                    this,
                    "Select kernel size:",
                    "Median Filter",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    kernelOptions,
                    kernelOptions[0]
            );

            if (kernelSizeOption == null) return;

            int kernelSize = Integer.parseInt(kernelSizeOption.split("x")[0]);

            // Wybór metody uzupełniania marginesów
            String[] borderOptions = {"Constant", "Reflect", "Replicate"};
            String borderType = (String) JOptionPane.showInputDialog(
                    this,
                    "Select border type:",
                    "Border Fill",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    borderOptions,
                    borderOptions[0]
            );

            if (borderType == null) return;

            int borderTypeCode;
            int constantValue = 0;

            switch (borderType.toLowerCase()) {
                case "constant":
                    borderTypeCode = Core.BORDER_CONSTANT;
                    String input = JOptionPane.showInputDialog(this, "Enter constant value (0-255):", "128");
                    try {
                        constantValue = Integer.parseInt(input);
                        if (constantValue < 0 || constantValue > 255) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid constant value. Please enter a number between 0 and 255.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    break;

                case "reflect":
                    borderTypeCode = Core.BORDER_REFLECT;
                    break;

                case "replicate":
                    borderTypeCode = Core.BORDER_REPLICATE;
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Invalid border type selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            // Aplikacja filtra medianowego
            try {
                BufferedImage resultImage = imageService.applyMedianFilter(selectedImage.getImage(), kernelSize, borderTypeCode);
                selectedImage.updateImage(resultImage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error applying median filter: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        menu.add(medianFilterItem);
    }


    private void addPrewittEdgeDetectionMenu(JMenu smoothingMenu) {
        JMenuItem prewittEdgeDetectionItem = new JMenuItem("Prewitt Edge Detection");
        prewittEdgeDetectionItem.addActionListener(e -> {
            if (selectedImage == null) {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // Prompt the user to select the border type
                String[] borderOptions = {"Constant", "Reflect", "Replicate"};
                String selectedBorder = (String) JOptionPane.showInputDialog(
                        this,
                        "Select border type:",
                        "Border Type",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        borderOptions,
                        "Constant"
                );

                if (selectedBorder == null) {
                    return; // User cancelled
                }

                int borderType;
                int constantValue = 0;

                switch (selectedBorder.toLowerCase()) {
                    case "constant":
                        borderType = Core.BORDER_CONSTANT;
                        String constantValueInput = JOptionPane.showInputDialog(this, "Enter constant value (0-255):", "128");
                        constantValue = Integer.parseInt(constantValueInput);
                        if (constantValue < 0 || constantValue > 255) {
                            throw new IllegalArgumentException("Constant value must be between 0 and 255.");
                        }
                        break;
                    case "reflect":
                        borderType = Core.BORDER_REFLECT;
                        break;
                    case "replicate":
                        borderType = Core.BORDER_REPLICATE;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid border type selected.");
                }

                // Apply Prewitt edge detection with the selected border type
                BufferedImage processedImage = imageService.applyPrewittEdgeDetection(selectedImage.getImage(), borderType, constantValue);
                selectedImage.updateImage(processedImage);
                imagePanel.repaint(); // Refresh the panel to display the updated image

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        smoothingMenu.add(prewittEdgeDetectionItem);
    }



    private JMenu createSmoothingMenu() {
        JMenu smoothingMenu = new JMenu("Smoothing");

        // Average Smoothing
        JMenuItem averageItem = new JMenuItem("Average Smoothing");
        averageItem.addActionListener(e -> applySmoothing("average"));

        // Weighted Average Smoothing
        JMenuItem weightedItem = new JMenuItem("Weighted Average Smoothing");
        weightedItem.addActionListener(e -> applySmoothing("weighted_average"));

        // Gaussian Smoothing
        JMenuItem gaussianItem = new JMenuItem("Gaussian Smoothing");
        gaussianItem.addActionListener(e -> applySmoothing("gaussian"));

        // Laplacian Sharpening
        JMenuItem laplacianSharpeningItem = new JMenuItem("Laplacian Sharpening");
        laplacianSharpeningItem.addActionListener(e -> applyLaplacianSharpening());

        // Adding items to the menu
        smoothingMenu.add(averageItem);
        smoothingMenu.add(weightedItem);
        smoothingMenu.add(gaussianItem);
        smoothingMenu.add(laplacianSharpeningItem);

        // Additional methods (if implemented elsewhere)
        addPrewittEdgeDetectionMenu(smoothingMenu);
        addBorderFillMenu(smoothingMenu);
        addMedianFilterMenu(smoothingMenu);
        addCannyEdgeDetectionMenu(smoothingMenu);

        return smoothingMenu;
    }

    private void applySmoothing(String method) {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int k = 1; // Default value for `k` (only used for weighted_average)

            // Prompt the user for the weight `k` only if the method is "weighted_average"
            if ("weighted_average".equalsIgnoreCase(method)) {
                String kInput = JOptionPane.showInputDialog(this, "Enter value for k (positive integer):", "3");
                k = Integer.parseInt(kInput);
                if (k <= 0) {
                    throw new IllegalArgumentException("Value of k must be positive.");
                }
            }

            // Prompt the user to select the border type
            String[] borderOptions = {"Constant", "Reflect", "Replicate"};
            String selectedBorder = (String) JOptionPane.showInputDialog(
                    this,
                    "Select border type:",
                    "Border Type",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    borderOptions,
                    "Constant"
            );

            if (selectedBorder == null) {
                return; // User cancelled
            }

            int borderType;
            int constantValue = 0;

            switch (selectedBorder.toLowerCase()) {
                case "constant":
                    borderType = Core.BORDER_CONSTANT;
                    String constantValueInput = JOptionPane.showInputDialog(this, "Enter constant value (0-255):", "128");
                    constantValue = Integer.parseInt(constantValueInput);
                    if (constantValue < 0 || constantValue > 255) {
                        throw new IllegalArgumentException("Constant value must be between 0 and 255.");
                    }
                    break;
                case "reflect":
                    borderType = Core.BORDER_REFLECT;
                    break;
                case "replicate":
                    borderType = Core.BORDER_REPLICATE;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid border type selected.");
            }

            // Apply the selected smoothing method
            BufferedImage smoothedImage = imageService.applySmoothing(selectedImage.getImage(), method, k, borderType, constantValue);
            selectedImage.updateImage(smoothedImage);
            imagePanel.repaint(); // Refresh the panel to display the updated image

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void addCannyEdgeDetectionMenu(JMenu menu) {
        JMenuItem cannyEdgeDetectionItem = new JMenuItem("Apply Canny Edge Detection");
        cannyEdgeDetectionItem.addActionListener(e -> {
            if (selectedImage == null) {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String threshold1Input = JOptionPane.showInputDialog(this, "Enter threshold 1 (low threshold):", "100");
                double threshold1 = Double.parseDouble(threshold1Input);

                String threshold2Input = JOptionPane.showInputDialog(this, "Enter threshold 2 (high threshold):", "200");
                double threshold2 = Double.parseDouble(threshold2Input);

                String[] apertureOptions = {"3", "5", "7"};
                String apertureInput = (String) JOptionPane.showInputDialog(
                        this,
                        "Select aperture size (odd number):",
                        "Aperture Size",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        apertureOptions,
                        "3"
                );

                if (apertureInput == null) return;
                int apertureSize = Integer.parseInt(apertureInput);

                int l2GradientOption = JOptionPane.showConfirmDialog(
                        this,
                        "Use L2 Gradient?",
                        "L2 Gradient",
                        JOptionPane.YES_NO_OPTION
                );

                boolean l2Gradient = l2GradientOption == JOptionPane.YES_OPTION;

                BufferedImage resultImage = imageService.applyCanny(selectedImage.getImage(), threshold1, threshold2, apertureSize, l2Gradient);
                selectedImage.updateImage(resultImage);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error applying Canny Edge Detection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        menu.add(cannyEdgeDetectionItem);
    }


    private void addBorderFillMenu(JMenu menu) {
        JMenuItem borderFillItem = new JMenuItem("Apply Border Fill");
        borderFillItem.addActionListener(e -> {
            if (selectedImage == null) {
                JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] borderOptions = {"Constant", "Reflect", "Replicate"};
            String borderType = (String) JOptionPane.showInputDialog(
                    this,
                    "Select border type:",
                    "Border Fill",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    borderOptions,
                    borderOptions[0]
            );

            if (borderType == null) {
                return;
            }

            int borderTypeCode;
            int constantValue = 0;

            switch (borderType.toLowerCase()) {
                case "constant":
                    borderTypeCode = Core.BORDER_CONSTANT;
                    String input = JOptionPane.showInputDialog(this, "Enter constant value (0-255):", "128");
                    try {
                        constantValue = Integer.parseInt(input);
                        if (constantValue < 0 || constantValue > 255) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid constant value. Please enter a number between 0 and 255.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    break;

                case "reflect":
                    borderTypeCode = Core.BORDER_REFLECT;
                    break;

                case "replicate":
                    borderTypeCode = Core.BORDER_REPLICATE;
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Invalid border type selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            // Aplikacja wypełnienia marginesów
            try {
                BufferedImage resultImage = imageService.applyBorderFill(selectedImage.getImage(), borderTypeCode, constantValue);
                selectedImage.updateImage(resultImage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error applying border fill: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        menu.add(borderFillItem);
    }


    private void applyLaplacianSharpening() {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "No image selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Przedstawienie masek użytkownikowi
        String[] masks = {
                "Mask 1:\n 0 -1 0\n-1 4 -1\n 0 -1 0",
                "Mask 2:\n-1 -1 -1\n-1 8 -1\n-1 -1 -1",
                "Mask 3:\n 1 -2 1\n-2 4 -2\n 1 -2 1"
        };

        String selectedMask = (String) JOptionPane.showInputDialog(
                this,
                "Select a Laplacian mask:",
                "Laplacian Mask Selection",
                JOptionPane.PLAIN_MESSAGE,
                null,
                masks,
                masks[0]
        );

        if (selectedMask != null) {
            int[][] mask;
            if (selectedMask.startsWith("Mask 1")) {
                mask = new int[][]{
                        {0, -1, 0},
                        {-1, 4, -1},
                        {0, -1, 0}
                };
            } else if (selectedMask.startsWith("Mask 2")) {
                mask = new int[][]{
                        {-1, -1, -1},
                        {-1, 8, -1},
                        {-1, -1, -1}
                };
            } else if (selectedMask.startsWith("Mask 3")) {
                mask = new int[][]{
                        {1, -2, 1},
                        {-2, 4, -2},
                        {1, -2, 1}
                };
            } else {
                mask = new int[][]{
                        {0, -1, 0},
                        {-1, 4, -1},
                        {0, -1, 0}
                };
            }

            // Wybór rodzaju uzupełnienia marginesów
            String[] borderOptions = {"Constant", "Reflect", "Replicate"};
            String selectedBorder = (String) JOptionPane.showInputDialog(
                    this,
                    "Select border type:",
                    "Border Type",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    borderOptions,
                    "Constant"
            );

            if (selectedBorder == null) {
                return; // Anulowanie operacji
            }

            int borderType;
            int constantValue = 0;

            try {
                switch (selectedBorder.toLowerCase()) {
                    case "constant":
                        borderType = Core.BORDER_CONSTANT;
                        String constantValueInput = JOptionPane.showInputDialog(this, "Enter constant value (0-255):", "128");
                        constantValue = Integer.parseInt(constantValueInput);
                        if (constantValue < 0 || constantValue > 255) {
                            throw new IllegalArgumentException("Constant value must be between 0 and 255.");
                        }
                        break;
                    case "reflect":
                        borderType = Core.BORDER_REFLECT;
                        break;
                    case "replicate":
                        borderType = Core.BORDER_REPLICATE;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid border type selected.");
                }

                // Wywołanie logiki przetwarzania
                BufferedImage sharpenedImage = new LaplacianSharpeningProcessor().applyLaplacianSharpening(
                        selectedImage.getImage(),
                        mask,
                        borderType,
                        constantValue
                );

                selectedImage.updateImage(sharpenedImage);
                imagePanel.repaint(); // Odśwież panel obrazu
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error applying Laplacian sharpening: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
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
            addImageToPanel(image, file.getName());
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
        log.info("Appling linear stretch");
        draggableImage.updateImage(image);
    }

    private void applyHistogramEqualization(DraggableImage draggableImage, BufferedImage image) {
        imageService.applyHistogramEqualization(image);
        draggableImage.updateImage(image);
    }

    public void setSelectedImage(DraggableImage selectedImage) {
        this.selectedImage = selectedImage;
        if (selectedImage != null) {
            selectedImageLabel.setText("Selected image: " + selectedImage.toString());
        } else {
            selectedImageLabel.setText("No image selected");
        }
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

    private JMenu createEdgeDetectionMenu() {
        JMenu edgeDetectionMenu = new JMenu("Edge Detection");

        // Dodaj inne algorytmy, np. Sobel, Prewitt
        addCannyEdgeDetectionMenu(edgeDetectionMenu);

        return edgeDetectionMenu;
    }

    private String chooseStructElementShape() {
        String[] shapes = {"Rectangle", "Cross"};
        String shape = (String) JOptionPane.showInputDialog(
                this,
                "Select structuring element shape:",
                "Morphology Element",
                JOptionPane.PLAIN_MESSAGE,
                null,
                shapes,
                shapes[0]
        );
        if (shape == null) {
            // user cancelled
            return null;
        }
        return shape.toLowerCase();
    }

    private BufferedImage convertTo8BitGray(BufferedImage source) {
        log.info("Converting image to 8-bit grayscale AND applying binary threshold (0/255).");

        // Krok 1: Jeśli obraz ma już TYPE_BYTE_GRAY, zrób kopię
        // (ale dalej chcemy go zbinaryzować, więc i tak iterujemy piksele)
        BufferedImage grayImage;
        if (source.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            grayImage = deepCopy(source);
        } else {
            // Tworzymy nowy obraz w odcieniach szarości
            grayImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayImage.getGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
        }

        // Krok 2: Iteracja po pikselach, próg np. 128 (lub parametr)
        // Zamiast tylko odcieni szarości, wymuszamy 0 lub 255
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int threshold = 128; // stały próg, ewentualnie zdefiniuj jako parametr

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = grayImage.getRaster().getSample(x, y, 0);
                int newPixel = (pixelValue >= threshold) ? 0 : 255;
                binaryImage.getRaster().setSample(x, y, 0, newPixel);
            }
        }

        return binaryImage;
    }


    // Pomocnicza metoda do tworzenia kopii obrazu (opcjonalna)
    private BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return copy;
    }





    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiImageApp::new);
    }
}