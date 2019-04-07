package ui;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * Provides methods to interact with he user through Pop-Up dialogs.
 */
public class UserDialogs {

	private File previouslySelectedFolder = null;
	
	/**
	 * Displays a directory chooser dialog for the user.
	 * 
	 * @return A File object to the selected directory or null when the dialog was canceled.
	 */
	public File getDirectoryFromUser() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		if(previouslySelectedFolder!=null)
			directoryChooser.setInitialDirectory(previouslySelectedFolder);
		previouslySelectedFolder = directoryChooser.showDialog(null);
		return previouslySelectedFolder;
	}
	

	/**
	 * Displays a file chooser dialog for the user where only .jpg and .png files
	 * can be selected.
	 * 
	 * @return A File object to the selected image or null when the dialog was canceled.
	 */
	public File getImageFileFromUser() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Bilddateien", "*.jpg", "*.png");
		fileChooser.getExtensionFilters().add(imageFilter);
		if(previouslySelectedFolder!=null)
			fileChooser.setInitialDirectory(previouslySelectedFolder);
		previouslySelectedFolder = fileChooser.showOpenDialog(null);
		return previouslySelectedFolder;
	}
	
	/**
	 * Shows an information for the user containing the specified message and an OK-Button for confirmation.
	 * @param message The message to show.
	 */
	public void showInformation(String message) {
		showDialog(AlertType.INFORMATION, message);
	}
	
	/**
	 * Shows an information for the user containing the specified message and an OK-Button for confirmation.
	 * Opens the dialog on the UI-Thread even when called from another thread.
	 * @param message The message to show.
	 */
	public void showInformationLater(String message) {
		Platform.runLater(() -> showDialog(AlertType.INFORMATION, message));
	}
	

	/**
	 * Shows an error message for the user and an OK-Button for confirmation.
	 * @param message The message to show.
	 */
	public void showError(String message) {
		showDialog(AlertType.ERROR, message);
	}
	
	/**
	 * Shows an error message for the user and an OK-Button for confirmation.
	 * Opens the dialog on the UI-Thread even when called from another thread.
	 * @param message The message to show.
	 */
	public void showErrorLater(String message) {
		Platform.runLater(() -> showDialog(AlertType.ERROR, message));
	}
	
	private void showDialog(AlertType type, String text) {
		System.out.println("[DIALOG] '" + text + "'");
		Alert alert = new Alert(type, text, ButtonType.OK);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
		alert.show();
	}
}
