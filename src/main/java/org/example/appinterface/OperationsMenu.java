package org.example.appinterface;

import org.example.DraggableImage;
import javax.swing.*;

public class OperationsMenu {

    private final JMenu operationsMenu;
    private final MultiImageApp appContext;

    public OperationsMenu(MultiImageApp appContext) {
        this.appContext = appContext;
        this.operationsMenu = new JMenu("Operations");
        initializeMenu();
    }

    private void initializeMenu() {
        JMenuItem duplicateMenuItem = new JMenuItem("Duplicate Image");
        duplicateMenuItem.addActionListener(e -> {
            DraggableImage selectedImage = appContext.getSelectedImage();
            if (selectedImage != null) {
                appContext.duplicateImage(selectedImage, selectedImage.getImage());
            }
        });

        JMenuItem histogramMenuItem = new JMenuItem("Generate Histogram");
        histogramMenuItem.addActionListener(e -> {
            DraggableImage selectedImage = appContext.getSelectedImage();
            if (selectedImage != null) {
                appContext.generateHistogram(selectedImage.getImage());
            }
        });

        JMenuItem stretchMenuItem = new JMenuItem("Apply Linear Stretch (No Clipping)");
        stretchMenuItem.addActionListener(e -> {
            DraggableImage selectedImage = appContext.getSelectedImage();
            if (selectedImage != null) {
                appContext.applyLinearStretch(selectedImage, selectedImage.getImage(), false, 0);
            }
        });

        JMenuItem stretchWithClippingMenuItem = new JMenuItem("Apply Linear Stretch (5% Clipping)");
        stretchWithClippingMenuItem.addActionListener(e -> {
            DraggableImage selectedImage = appContext.getSelectedImage();
            if (selectedImage != null) {
                appContext.applyLinearStretch(selectedImage, selectedImage.getImage(), true, 0.05);
            }
        });

        JMenuItem equalizeHistogramMenuItem = new JMenuItem("Equalize Histogram");
        equalizeHistogramMenuItem.addActionListener(e -> {
            DraggableImage selectedImage = appContext.getSelectedImage();
            if (selectedImage != null) {
                appContext.applyHistogramEqualization(selectedImage, selectedImage.getImage());
            }
        });

        operationsMenu.add(duplicateMenuItem);
        operationsMenu.add(histogramMenuItem);
        operationsMenu.add(stretchMenuItem);
        operationsMenu.add(stretchWithClippingMenuItem);
        operationsMenu.add(equalizeHistogramMenuItem);
    }

    public JMenu getMenu() {
        return operationsMenu;
    }
}
