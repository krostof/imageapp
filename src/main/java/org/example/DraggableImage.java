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
    private final BufferedImage originalImage;  // Tylko do ewentualnego resetu
    private BufferedImage currentImage;         // Aktualny stan obrazu

    private Point initialClick;
    private final JPanel parentPanel;
    private final JPopupMenu popupMenu;
    private final ImageScaler imageScaler;
    private final String fileName;

    public DraggableImage(BufferedImage image, JPanel parentPanel, MultiImageApp mainApp, String fileName) {
        this.originalImage = image;    // Zachowujemy oryginał
        this.currentImage = image;     // Na start = oryginał
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

                // Ustawienie obrazu jako wybranego w MultiImageApp
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

                // Log aktualnej pozycji
                System.out.println("Image moved to: X = " + X + ", Y = " + Y);
            }
        });
    }

    /**
     * Aktualizuje obraz w DraggableImage – ustawia jako currentImage i odświeża etykietę.
     */
    public void updateImage(BufferedImage newImage) {
        this.currentImage = newImage;
        setIcon(new ImageIcon(newImage));
        setSize(newImage.getWidth(), newImage.getHeight());
        revalidate();
        repaint();
    }

    /**
     * Zwraca aktualny stan obrazu (po przekształceniach).
     */
    public BufferedImage getImage() {
        return currentImage;
    }

    /**
     * Dopasowanie obrazu do rozmiaru okna rodzica (bazując na bieżącym stanie currentImage).
     */
    private void scaleImageToWindow() {
        currentImage = imageScaler.scaleToWindow(currentImage, parentPanel.getWidth(), parentPanel.getHeight());
        updateImage(currentImage);
        setLocation(0, 0);
    }

    /**
     * Skalowanie do pełnego ekranu (bazując na bieżącym stanie currentImage).
     */
    private void scaleImageToFullScreen() {
        currentImage = imageScaler.scaleToFullScreen(currentImage);
        updateImage(currentImage);
        setLocation(0, 0);
    }

    /**
     * Przywraca naturalny rozmiar – w tym przypadku proponuję:
     * - Albo bierzemy oryginalny obraz (originalImage),
     * - Albo bierzemy aktualny i nic nie zmieniamy.
     *
     * Jeśli chcemy powrót do stanu surowego, użyjemy originalImage:
     */
    private void scaleImageToNaturalSize() {
        currentImage = imageScaler.scaleToNaturalSize(originalImage);
        updateImage(currentImage);
    }

    /**
     * Usuwa etykietę z panelu rodzica.
     */
    private void closeImage() {
        parentPanel.remove(this);
        parentPanel.revalidate();
        parentPanel.repaint();
        System.out.println("Image closed.");
    }
}
