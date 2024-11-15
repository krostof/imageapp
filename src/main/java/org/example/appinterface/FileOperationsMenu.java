//package org.example.appinterface;
//
//import org.example.DraggableImage;
//import org.example.ImageService;
//
//import javax.swing.*;
//import java.io.File;
//import java.awt.image.BufferedImage;
//
//public class FileOperationsMenu {
//
//    private final JMenu fileMenu;
//    private final MultiImageApp appContext;
//    private final ImageService imageService;
//
//    public FileOperationsMenu(MultiImageApp appContext, ImageService imageService) {
//        this.appContext = appContext;
//        this.imageService = imageService;
//        this.fileMenu = new JMenu("File");
//        initializeMenu();
//    }
//
//    private void initializeMenu() {
//        JMenuItem openImageMenuItem = new JMenuItem("Open Image");
//        openImageMenuItem.addActionListener(e -> openImage());
//        fileMenu.add(openImageMenuItem);
//
//        JMenuItem saveImageMenuItem = new JMenuItem("Save Image");
//        saveImageMenuItem.addActionListener(e -> saveImage());
//        fileMenu.add(saveImageMenuItem);
//    }
//
//    private void openImage() {
//        JFileChooser fileChooser = new JFileChooser();
//        int result = fileChooser.showOpenDialog(null);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File file = fileChooser.getSelectedFile();
//            BufferedImage image = imageService.loadImageFromFile(file);
//            if (image != null) {
//                appContext.addImageToPanel(image);
//            } else {
//                JOptionPane.showMessageDialog(null, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    private void saveImage() {
//        DraggableImage selectedImage = appContext.getSelectedImage();
//        if (selectedImage != null) {
//            JFileChooser fileChooser = new JFileChooser();
//            int result = fileChooser.showSaveDialog(null);
//            if (result == JFileChooser.APPROVE_OPTION) {
//                File file = fileChooser.getSelectedFile();
//                imageService.saveImageToFile(selectedImage.getImage(), file);
//            }
//        } else {
//            JOptionPane.showMessageDialog(null, "No image selected to save.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    public JMenu getMenu() {
//        return fileMenu;
//    }
//}
