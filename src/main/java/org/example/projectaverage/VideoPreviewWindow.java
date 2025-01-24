package org.example.projectaverage;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class VideoPreviewWindow extends JFrame {
    private final String videoPath;

    public VideoPreviewWindow(String videoPath) {
        super("Video Preview");
        this.videoPath = videoPath;

        JFXPanel jfxPanel = new JFXPanel(); // Wymaga JavaFX
        setLayout(new BorderLayout());
        add(jfxPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Video");
        saveButton.addActionListener(e -> saveVideo());
        add(saveButton, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        Platform.runLater(() -> initFX(jfxPanel)); // Uruchomienie JavaFX w Swing
    }

    private void initFX(JFXPanel jfxPanel) {
        try {
            File videoFile = new File(videoPath);

            // Jeśli plik istnieje w systemie plików
            if (videoFile.exists()) {
                Media media = new Media(videoFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);

                Scene scene = new Scene(new javafx.scene.layout.StackPane(mediaView), 800, 600);
                jfxPanel.setScene(scene);
                mediaPlayer.play();
            } else {
                // Jeśli plik wideo jest osadzony w JAR
                InputStream videoStream = getClass().getResourceAsStream(videoPath);
                if (videoStream != null) {
                    Path tempFile = Files.createTempFile("video", ".avi");
                    Files.copy(videoStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                    Media media = new Media(tempFile.toUri().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    MediaView mediaView = new MediaView(mediaPlayer);

                    Scene scene = new Scene(new javafx.scene.layout.StackPane(mediaView), 800, 600);
                    jfxPanel.setScene(scene);
                    mediaPlayer.play();
                } else {
                    throw new IllegalArgumentException("Video file not found: " + videoPath);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading video: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void saveVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("output_video.avi"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            File originalFile = new File(videoPath);

            try {
                // Kopiujemy (nadpisujemy, jeśli plik istnieje)
                Files.copy(originalFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Video saved at: " + targetFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error saving video: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

}
