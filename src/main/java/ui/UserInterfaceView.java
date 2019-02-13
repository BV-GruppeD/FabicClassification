package ui;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class UserInterfaceView {

	public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initializeAndShowUI();
            }
        });
    }
	
    private void initializeAndShowUI() {
        // This method is invoked on Swing thread
        JFrame frame = new JFrame("Stoffklassifikation");
        frame.setMinimumSize(new Dimension(1080,720));
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setVisible(true);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
        });
    }

    private void initFX(JFXPanel fxPanel) {
        // This method is invoked on JavaFX thread
        try {
        	FXMLLoader loader = new FXMLLoader();
        	loader.setLocation(getClass().getClassLoader().getResource("MainPage.fxml"));
        	Parent root = loader.load();
            
            Scene scene = new Scene(root, 450, 450);
            fxPanel.setScene(scene);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
