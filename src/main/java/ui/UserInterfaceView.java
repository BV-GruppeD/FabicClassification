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


/**
 * Provides methods to create the user interface using JavaFX and the MainPage.fxml file
 */
public class UserInterfaceView {

	/**
	 * Create a process on a Thread containing the Java Swing components. 
	 */
	public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createSwingComponentsForUI();
            }
        });
    }
	
    /**
     *  -This method is invoked on the Swing thread-
     * Initializes the interface for the user and specifies the components needed for compatibility
     * between the java.awx based ImageJ interface and the modern JavaFX interface for this plugin.
     * Also starts the process handling the JavaFX user interactions
     */
    private void createSwingComponentsForUI() {
        JFrame frame = new JFrame("Stoffklassifikation");
        frame.setMinimumSize(new Dimension(1200,920));
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setVisible(true);

        // Thread for JavaFX interactions
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createFxComponentsForUI(fxPanel);
            }
        });
    }

    /**
     *  -This method is invoked on the JavaFX thread-
     * Specifies the informations about the user interface in the given Swing component as described
     * in the MainPage.fxml file. The file was created with JavaFX Scene Builder.
     * @param fxPanel The Swing components that holds the JavaFX user interface.
     */
    private void createFxComponentsForUI(JFXPanel fxPanel) {

        try {
        	FXMLLoader loader = new FXMLLoader();
        	loader.setLocation(getClass().getClassLoader().getResource("MainPage.fxml"));
        	loader.setController(new UserInterfaceControler());
        	Parent root = loader.load();
            
            Scene scene = new Scene(root, 450, 450);
            fxPanel.setScene(scene);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
