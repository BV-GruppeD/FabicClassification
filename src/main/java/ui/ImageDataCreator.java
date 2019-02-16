package ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.bv_gruppe_d.imagej.ImageData;
import com.bv_gruppe_d.imagej.Lable;

import ij.IJ;
import ij.process.ByteProcessor;


/**
 * Provides utility methods to create labled and unlabled ImageData object from files in the file system.
 */
public class ImageDataCreator {

	/**
	 * Provides the directory names expected for automatic labling of images on the file system.
	 */
	@SuppressWarnings("serial")
	private static final ArrayList<String> expectedDirectories = new ArrayList<String>(){{
	    add("geschert");
	    add("keineDehnung");
	    add("mittlereDehnung");
	    add("maximaleDehnung");
	    add("stoerung");
	}};
	
	/**
	 * Given a valid upper directory this method iterates over the directories matching the expected
	 * names for automatic labling and creates a list of ImageData objects from the files in the directories.
	 * @param upperDirectory The directory that contains the folders named for automatic labling of the contained images.
	 * @return All ImageData objects that could be labled automaticly from the subdirectories.
	 */
	public static ArrayList<ImageData> getLabledImageData(File upperDirectory) {
		ArrayList<File> lableDirectories = new ArrayList<>();
		ArrayList<ImageData> labledImages = new ArrayList<>();
		
	    if (upperDirectory != null && upperDirectory.isDirectory()) {
	    	lableDirectories = getDirectoriesWithLabledImages(upperDirectory);
	    	for (File directory : lableDirectories) {
	    		try {
					labledImages.addAll(getLabledImagesFromDirectory(directory));
				} catch (Exception e) {
					IJ.showMessage(e.getMessage());
				}
			}
	    }
	    promptUserInformationForLoadingProcess(lableDirectories, labledImages);
	    
	    return labledImages;
	}
	
	/**
	 * Extracts the paths for directories contained in the given folder that can be used for automatic labling.
	 * @param folder The directory containing the sub-directories named for automatic labling.
	 * @return The paths to the sub-directories for automatic labling.
	 */
	private static ArrayList<File> getDirectoriesWithLabledImages(final File folder) {
	    ArrayList<File> directories = new ArrayList<>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory() && expectedDirectories.contains(fileEntry.getName())) {	           
	            directories.add(fileEntry);
	        }
	    }
		return directories;
	}
	
	/**
	 * Loads all images in the directory to ImageData objects and lables them depending on the name of the directory.
	 * @param directory The directory containig the files to be loaded. 
	 * @return The ImageData objects from the images in the directory.
	 * @throws Exception Throws exceptions occuring in the process of image loading.
	 */
	private static ArrayList<ImageData> getLabledImagesFromDirectory(File directory) throws Exception {
		Lable lable = determineLableFromDirectory(directory);
		
		//Load all images
		File[] files = directory.listFiles();
		ArrayList<ImageData> labledImages = new ArrayList<>();
		for (File file : files) {
			if (file.isFile()) {
				ByteProcessor bp = loadFileToImageProcessor(file);
				labledImages.add(new ImageData(bp, lable));
			}
		}
		return labledImages;
	}
	
	/**
	 * Maps the given directory to the enum values of the Lable enumeration.
	 * @param directory The directory to be decided upon.
	 * @return An enum-value from the Lable enumeration.
	 * @throws Exception Throws an exception if no mapping could be achieved.
	 */
	private static Lable determineLableFromDirectory(File directory) throws Exception {
		switch (directory.getName()) {
		case "geschert":
			return Lable.SHEARD;
		case "keineDehnung":
			return Lable.NO_STRETCH;
		case "mittlereDehnung":
			return Lable.MEDIUM_STRETCH;
		case "maximaleDehnung":
			return Lable.MAXIMUM_STRECH;
		case "stoerung":
			return Lable.DISTURBANCE;
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
	 * Prompts a notification for the user how many images where loaded and labeled. It also adds the paths to the directories used for automatic labling.
	 * @param lableDirectories The directories used for automatic labeling.
	 * @param labledImages The ImageData objects that where created and labeled in the process.
	 */
	private static void promptUserInformationForLoadingProcess(ArrayList<File> lableDirectories,
			ArrayList<ImageData> labledImages) {
		String countingMessage = labledImages.size() + " Bilder erfolgreich hinzugefügt.\r\n\r\n";
		
		String directoriesMessage = "Genutze Ordner:\r\n" + lableDirectories.toString();
		directoriesMessage = directoriesMessage.replaceAll(",", "\r\n");
		directoriesMessage = directoriesMessage.replace("[", " ");
		directoriesMessage = directoriesMessage.replace("]", " ");
		
		IJ.showMessage(countingMessage + directoriesMessage);
	}
	
	/**
	 * Creates an ImageData object from the given file.
	 * @param selectedFile The file containing an image to be processed.
	 * @return An ImageData object containing the image from the specified file with the UNKNOWN label.
	 * @throws IOException Throw exceptions from the loading process of the file.
	 */
	public static ImageData getImageData(File selectedFile) throws IOException {
		ByteProcessor bp = loadFileToImageProcessor(selectedFile);
		return new ImageData(bp, Lable.UNKNOWN);
	}	
}
