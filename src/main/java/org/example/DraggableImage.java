package org.example;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.example.appinterface.MultiImageApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

@Log4j2
public class DraggableImage extends JLabel {
    @Override
    public String toString() {
        return fileName;
    }
    @Getter
    private final BufferedImage originalImage;
    private BufferedImage currentImage;
    private Point initialClick;
    private final JPanel parentPanel;
    private final JPopupMenu popupMenu;
    private final ImageScaler imageScaler;
    private final String fileName;

    public DraggableImage(BufferedImage image, JPanel parentPanel, MultiImageApp mainApp, String fileName) {
        this.originalImage = image;
        this.currentImage = image;
        this.parentPanel = parentPanel;
        this.fileName = fileName;
        this.imageScaler = new ImageScaler();
        setIcon(new ImageIcon(currentImage));
        setSize(currentImage.getWidth(), currentImage.getHeight());

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

                // Ustawienie obrazu jako wybranego
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mainApp.setSelectedImage(DraggableImage.this); // Ustawienie wybranego obrazu w MultiImageApp
//                    System.out.println("Selected image updated. Location: X = " + getX() + ", Y = " + getY());
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

//                System.out.println("Image moved to: X = " + X + ", Y = " + Y);
            }
        });
    }

    public void updateImage(BufferedImage newImage) {
        setIcon(new ImageIcon(newImage));
        setSize(newImage.getWidth(), newImage.getHeight());
        revalidate();
        repaint();
        String string = newImage.toString();
        System.out.println("Image updated " + string);
    }

    public BufferedImage getImage() {
        return currentImage;
    }

    private void scaleImageToWindow() {
        currentImage = imageScaler.scaleToWindow(originalImage, parentPanel.getWidth(), parentPanel.getHeight());
        updateImage(currentImage);
        setLocation(0, 0);
    }

    private void scaleImageToFullScreen() {
        currentImage = imageScaler.scaleToFullScreen(originalImage);
        updateImage(currentImage);
        setLocation(0, 0);
    }

    private void scaleImageToNaturalSize() {
        currentImage = imageScaler.scaleToNaturalSize(originalImage);
        updateImage(currentImage);
    }

    private void closeImage() {
        parentPanel.remove(this);
        parentPanel.revalidate();
        parentPanel.repaint();
        System.out.println("Image closed.");
    }
}