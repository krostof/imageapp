package org.example.projectaverage;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VideoPreviewWindow extends JFrame {
    private final String videoPath;

    public VideoPreviewWindow(String videoPath) {
        super("Video Preview");
        this.videoPath = videoPath;

        setLayout(new BorderLayout());
        JLabel label = new JLabel("Video preview is not implemented yet (placeholder).");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Video");
        saveButton.addActionListener(e -> saveVideo());

        add(saveButton, BorderLayout.SOUTH);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void saveVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("output_video.avi"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            new File(videoPath).renameTo(targetFile);
            JOptionPane.showMessageDialog(this, "Video saved at: " + targetFile.getAbsolutePath());
        }
    }
}
