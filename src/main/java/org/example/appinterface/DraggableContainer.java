package org.example.appinterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DraggableContainer extends JPanel {
    private Point initialClick;
    private JFrame parent;

    public DraggableContainer(JFrame parent) {
        this.parent = parent;
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Dodanie obwódki

        // Dodanie przeciągania
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Pobierz pozycję okna
                int thisX = getX();
                int thisY = getY();

                // Oblicz nowe współrzędne
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int newX = thisX + xMoved;
                int newY = thisY + yMoved;

                // Przesuń kontener
                setLocation(newX, newY);
            }
        });
    }
}
