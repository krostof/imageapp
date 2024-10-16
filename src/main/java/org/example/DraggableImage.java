package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class DraggableImage extends JLabel {
    private final BufferedImage image;
    private Point initialClick;
    private final JPanel parentPanel;
    private final MultiImageApp mainApp;
    private final JPopupMenu popupMenu;

    public DraggableImage(BufferedImage image, JPanel parentPanel, MultiImageApp mainApp) {
        this.image = image;
        this.parentPanel = parentPanel;
        this.mainApp = mainApp;
        setIcon(new ImageIcon(image));

        // Inicjalizacja menu kontekstowego
        popupMenu = new JPopupMenu();
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> closeImage());
        popupMenu.add(closeItem);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);

                // Jeśli kliknięto prawym przyciskiem myszy, pokaż menu kontekstowe
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

    public BufferedImage getImage() {
        return image;
    }

    public void updateImage(BufferedImage newImage) {
        setIcon(new ImageIcon(newImage));
        setSize(newImage.getWidth(), newImage.getHeight());
        revalidate();
        repaint();
    }

    // Funkcja zamykająca (usuwająca) obraz z panelu
    private void closeImage() {
        parentPanel.remove(this);
        parentPanel.revalidate();
        parentPanel.repaint();
        System.out.println("Image closed.");
    }
}
