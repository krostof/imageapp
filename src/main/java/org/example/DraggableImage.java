package org.example;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class DraggableImage extends JLabel {
    @Getter
    private final BufferedImage image;
    private Point initialClick;
    private final JPanel parentPanel;
    private final JPopupMenu popupMenu;

    public DraggableImage(BufferedImage image, JPanel parentPanel, MultiImageApp mainApp) {
        this.image = image;
        this.parentPanel = parentPanel;
        setIcon(new ImageIcon(image));

        popupMenu = new JPopupMenu();
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> closeImage());
        popupMenu.add(closeItem);

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

    private void closeImage() {
        parentPanel.remove(this);
        parentPanel.revalidate();
        parentPanel.repaint();
        System.out.println("Image closed.");
    }
}
