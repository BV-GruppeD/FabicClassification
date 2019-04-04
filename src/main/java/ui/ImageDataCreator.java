package ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.bv_gruppe_d.imagej.ImageData;
import com.bv_gruppe_d.imagej.Label;

import ij.IJ;
import ij.process.ByteProcessor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;


/**
 * Provides utility methods to create labeled and unlabeled ImageData object from files in the file system.
 */
public class ImageDataCreator {

	/**
	 * Provides the directory names expected for automatic labeling of images on the file system.
	 */
	@SuppressWarnings("serial") // This class will not be serialized.
	private final static ArrayList<String> expectedDirectories = new ArrayList<String>(){{
	    add("geschert");
	    add("keineDehnung");
	    add("mittlereDehnung");
	    add("maximaleDehnung");
	    add("stoerung");
	}};
	
	/*//TODO I suggest something like 
	static {
	expectedDirectories = new ArrayList<String>();
	expectedDirectories.add("geschert");
	...
	}
	//to prevent this @SuppressWarnings("serial"). It is the first time I ever saw an anonymous subclass to initialize a list
	*/
	/**
	 * Given a valid upper directory this method iterates over the directories matching the expected
	 * names for automatic labeling and creates a list of ImageData objects from the files in the directories.
	 * @param upperDirectory The directory that contains the folders named for automatic labeling of the contained images.
	 * @return All ImageData objects that could be labeled automatically from the sub-directories.
	 */
	public static ArrayList<ImageData> getLabeldImageData(File upperDirectory) {
		ArrayList<File> labelDirectories = new ArrayList<>();
		ArrayList<ImageData> labeldImages = new ArrayList<>();
		
	    if (upperDirectory != null && upperDirectory.isDirectory()) {
	    	labelDirectories = getDirectoriesWithLabeldImages(upperDirectory);
	    	for (File directory : labelDirectories) {
	    		try {
					labeldImages.addAll(getLabeldImagesFromDirectory(directory));
				} catch (Exception e) {
					IJ.showMessage(e.getMessage());
				}
			}
	    }
	    promptUserInformationForLoadingProcess(labelDirectories, labeldImages);
	    
	    return labeldImages;
	}
	
	/**
	 * Extracts the paths for directories contained in the given folder that can be used for automatic labeling.
	 * @param folder The directory containing the sub-directories named for automatic labeling.
	 * @return The paths to the sub-directories for automatic labeling.
	 */
	private static ArrayList<File> getDirectoriesWithLabeldImages(final File folder) {
	    ArrayList<File> directories = new ArrayList<>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory() && expectedDirectories.contains(fileEntry.getName())) {	           
	            directories.add(fileEntry);
	        }
	    }
		return directories;
	}
	
	/**
	 * Loads all images in the directory to ImageData objects and labels them depending on the name of the directory.
	 * @param directory The directory containing the files to be loaded. 
	 * @return The ImageData objects from the images in the directory.
	 * @throws Exception Throws exceptions occurring in the process of image loading.
	 */
	private static ArrayList<ImageData> getLabeldImagesFromDirectory(File directory) throws Exception {
		Label label = determineLabelFromDirectory(directory);
		
		//Load all images
		File[] files = directory.listFiles();
		ArrayList<ImageData> labeldImages = new ArrayList<>();
		for (File file : files) {
			if (file.isFile()) {
				ByteProcessor bp = loadFileToImageProcessor(file);
				labeldImages.add(new ImageData(bp, label));
			}
		}
		return labeldImages;
	}
	
	/**
	 * Maps the given directory to the enum values of the Label enumeration.
	 * @param directory The directory to be decided upon.
	 * @return An enum-value from the Label enumeration.
	 * @throws Exception Throws an exception if no mapping could be achieved.
	 */
	private static Label determineLabelFromDirectory(File directory) throws Exception {
		switch (directory.getName()) {
		case "geschert":
			return Label.SHEARD;
		case "keineDehnung":
			return Label.NO_STRETCH;
		case "mittlereDehnung":
			return Label.MEDIUM_STRETCH;
		case "maximaleDehnung":
			return Label.MAXIMUM_STRECH;
		case "stoerung":
			return Label.DISTURBANCE;
		default:
			throw new Exception("Beim Labeln der Daten ist leider ein Fehler aufgetreten.");
		}
	}	
	
	/**
	 * Creates a ByteProcessor from the given file.
	 * @param file The file containing an image to be loaded.
	 * @return A ByteProcessor for the given file.
	 * @throws IOException Throws exceptions from the IO operation.
	 */
	private static ByteProcessor loadFileToImageProcessor(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		return new ByteProcessor(image);
	}
	
	/*
	 * TDOD: move to ui controller
	 */
	/**
	 * Prompts a notification for the user how many images where loaded and labeled. It also adds 
	 * the paths to the directories used for automatic labeling.
	 * @param labelDirectories The directories used for automatic labeling.
	 * @param labeldImages The ImageData objects that where created and labeled in the process.
	 */
	private static void promptUserInformationForLoadingProcess(ArrayList<File> labelDirectories,
			ArrayList<ImageData> labeldImages) {
		String countingMessage = labeldImages.size() + " Bilder erfolgreich hinzugefügt.\r\n\r\n";
		
		String directoriesMessage = "Genutze Ordner:\r\n" + labelDirectories.toString();
		directoriesMessage = directoriesMessage.replaceAll(",", System.lineSeparator());
		directoriesMessage = directoriesMessage.replace("[", " ");
		directoriesMessage = directoriesMessage.replace("]", " ");
		
		new Alert(AlertType.INFORMATION, countingMessage + directoriesMessage, ButtonType.OK)
			.showAndWait();
	}
	
	/**
	 * Creates an ImageData object from the given file.
	 * @param selectedFile The file containing an image to be processed.
	 * @return An ImageData object containing the image from the specified file with the UNKNOWN label.
	 * @throws IOException Throw exceptions from the loading process of the file.
	 */
	public static ImageData getImageData(File selectedFile) throws IOException {
		ByteProcessor bp = loadFileToImageProcessor(selectedFile);
		return new ImageData(bp, Label.UNKNOWN);
	}	
}
