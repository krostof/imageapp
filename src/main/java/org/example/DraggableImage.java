package org.example;

import lombok.Getter;
import org.example.appinterface.MultiImageApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class DraggableImage extends JLabel {

    @Override
    public String toString() {
        return fileName;
    }

    @Getter
    private final BufferedImage originalImage;  // Original image for possible reset
    private BufferedImage currentImage;         // Current state of the image

    private Point initialClick;
    private final JPanel parentPanel;
    private final JPopupMenu popupMenu;
    private final ImageScaler imageScaler;
    private final String fileName;
    private final JLabel nameLabel; // Label to display the image name

    public DraggableImage(BufferedImage image, JPanel parentPanel, MultiImageApp mainApp, String fileName) {
        this.originalImage = image;    // Save the original image
        this.currentImage = image;     // Initial state = original image
        this.parentPanel = parentPanel;
        this.fileName = fileName;
        this.imageScaler = new ImageScaler();

        setIcon(new ImageIcon(currentImage));
        setSize(currentImage.getWidth(), currentImage.getHeight());

        // Initialize the name label
        this.nameLabel = new JLabel(fileName);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setSize(getWidth(), 20);
        nameLabel.setLocation(getX(), getY() + getHeight());
        parentPanel.add(nameLabel);

        popupMenu = new JPopupMenu();

        JMenuItem fitToWindowItem = new JMenuItem("Fit to Window");
        fitToWindowItem.addActionListener(e -> scaleImageToWindow());

        JMenuItem fullScreenItem = new JMenuItem("Full Screen");
        fullScreenItem.addActionListener(e -> scaleImageToFullScreen());

        JMenuItem naturalSizeItem = new JMenuItem("Natural Size");
        naturalSizeItem.addActionListener(e -> scaleImageToNaturalSize());

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> closeImage());

        popupMenu.add(fitToWindowItem);
        popupMenu.add(fullScreenItem);
        popupMenu.add(naturalSizeItem);
        popupMenu.addSeparator();
        popupMenu.add(closeItem);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);

                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }

                // Set the image as selected in MultiImageApp
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mainApp.setSelectedImage(DraggableImage.this);
                    System.out.println("Selected image updated. Location: X = " + getX() + ", Y = " + getY());
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

                int X = thisX + xMoved;
                int Y = thisY + yMoved;

                setLocation(X, Y);

                // Update the position of the name label
                nameLabel.setLocation(X, Y + getHeight());

                // Log the current position
                System.out.println("Image moved to: X = " + X + ", Y = " + Y);
            }
        });
    }

    /**
     * Updates the image in DraggableImage - sets it as currentImage and refreshes the label.
     */
    public void updateImage(BufferedImage newImage) {
        this.currentImage = newImage;
        setIcon(new ImageIcon(newImage));
        setSize(newImage.getWidth(), newImage.getHeight());
        nameLabel.setLocation(getX(), getY() + getHeight()); // Update label position
        revalidate();
        repaint();
    }

    /**
     * Returns the current state of the image (after transformations).
     */
    public BufferedImage getImage() {
        return currentImage;
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        // Update the position of the name label
        if (nameLabel != null) {
            nameLabel.setLocation(x, y + getHeight());
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        // Update the width of the name label
        if (nameLabel != null) {
            nameLabel.setSize(width, 20);
        }
    }

    /**
     * Scales the image to fit the parent panel's size (based on currentImage).
     */
    private void scaleImageToWindow() {
        currentImage = imageScaler.scaleToWindow(currentImage, parentPanel.getWidth(), parentPanel.getHeight());
        updateImage(currentImage);
        setLocation(0, 0);
    }

    /**
     * Scales the image to full screen (based on currentImage).
     */
    private void scaleImageToFullScreen() {
        currentImage = imageScaler.scaleToFullScreen(currentImage);
        updateImage(currentImage);
        setLocation(0, 0);
    }

    /**
     * Restores the image to its natural size using the original image.
     */
    private void scaleImageToNaturalSize() {
        currentImage = imageScaler.scaleToNaturalSize(originalImage);
        updateImage(currentImage);
    }

    /**
     * Removes the label and the image from the parent panel.
     */
    private void closeImage() {
        parentPanel.remove(this);
        parentPanel.remove(nameLabel);
        parentPanel.revalidate();
        parentPanel.repaint();
        System.out.println("Image closed.");
    }
}
