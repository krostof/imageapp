package org.example;

import lombok.Getter;
import org.example.appinterface.MultiImageApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class DraggableImage extends JLabel {
    @Getter
    private final BufferedImage originalImage; // Oryginalny obraz
    private BufferedImage currentImage; // Obraz w aktualnym rozmiarze
    private Point initialClick;
    private final JPanel parentPanel;
    private final JPopupMenu popupMenu;
    private final ImageScaler imageScaler;

    public DraggableImage(BufferedImage image, JPanel parentPanel, MultiImageApp mainApp) {
        this.originalImage = image;
        this.currentImage = image;
        this.parentPanel = parentPanel;
        this.imageScaler = new ImageScaler();
        setIcon(new ImageIcon(currentImage));
        setSize(currentImage.getWidth(), currentImage.getHeight());

        // Inicjalizacja menu kontekstowego
        popupMenu = new JPopupMenu();

        // Opcja: Dopasuj do okna
        JMenuItem fitToWindowItem = new JMenuItem("Fit to Window");
        fitToWindowItem.addActionListener(e -> scaleImageToWindow());

        // Opcja: Pełny ekran
        JMenuItem fullScreenItem = new JMenuItem("Full Screen");
        fullScreenItem.addActionListener(e -> scaleImageToFullScreen());

        // Opcja: Naturalny rozmiar
        JMenuItem naturalSizeItem = new JMenuItem("Natural Size");
        naturalSizeItem.addActionListener(e -> scaleImageToNaturalSize());

        // Opcja: Zamknij obraz
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> closeImage());

        // Dodanie opcji do menu kontekstowego
        popupMenu.add(fitToWindowItem);
        popupMenu.add(fullScreenItem);
        popupMenu.add(naturalSizeItem);
        popupMenu.addSeparator();
        popupMenu.add(closeItem);

        // Obsługa zdarzeń myszy
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);

                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
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
            }
        });
    }

    public void updateImage(BufferedImage newImage) {
        setIcon(new ImageIcon(newImage));
        setSize(newImage.getWidth(), newImage.getHeight());
        revalidate();
        repaint();
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
        // Wywołanie metody z ImageScaler
        currentImage = imageScaler.scaleToFullScreen(originalImage);
        updateImage(currentImage);

        // Ustaw obraz w lewym górnym rogu
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
