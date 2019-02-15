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
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;

public class UserInterfaceControler {

	private final ArrayList<String> expectedDirectories = new ArrayList<String>(){{
	    add("geschert");
	    add("keineDehnung");
	    add("mittlereDehnung");
	    add("maximaleDehnung");
	    add("stoerung");
	}};
	
	private ArrayList<ImageData> trainingsData;
	private ArrayList<ImageData> testData;
	@FXML
	private void readLabledTrainingsData() {
		trainingsData = getLabledImageData();
	}
	
	@FXML
	private void readLabledTestData() {
		testData = getLabledImageData();
	}

	private ArrayList<ImageData> getLabledImageData() {
		ArrayList<File> lableDirectories = new ArrayList<>();
		ArrayList<ImageData> labledImages = new ArrayList<>();
		
		final File selectedDirectory = getDirectoryFromUser();
	    if (selectedDirectory != null) {
	    	lableDirectories = getDictionariesWithLabledImages(selectedDirectory);
	    	for (File directory : lableDirectories) {
	    		try {
					labledImages.addAll(getLabledImagesFromDictionary(directory));
				} catch (Exception e) {
					IJ.showMessage(e.getMessage());
				}
			}
	    }
	    promptUserInformationForLoadingProcess(lableDirectories, labledImages);
	    
	    return labledImages;
	}

	private void promptUserInformationForLoadingProcess(ArrayList<File> lableDirectories,
			ArrayList<ImageData> labledImages) {
		String countingMessage = labledImages.size() + " Bilder erfolgreich hinzugefügt.\r\n\r\n";
		
		String directoriesMessage = "Genutze Ordner:\r\n" + lableDirectories.toString();
		directoriesMessage = directoriesMessage.replaceAll(",", "\r\n");
		directoriesMessage = directoriesMessage.replace("[", " ");
		directoriesMessage = directoriesMessage.replace("]", " ");
		
		IJ.showMessage(countingMessage + directoriesMessage);
	}

	private File getDirectoryFromUser() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
	    return directoryChooser.showDialog(null);
	}
	
	private ArrayList<File> getDictionariesWithLabledImages(final File folder) {
	    ArrayList<File> directories = new ArrayList<>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory() && expectedDirectories.contains(fileEntry.getName())) {	           
	            directories.add(fileEntry);
	        }
	    }
		return directories;
	}
	
	
	private ArrayList<ImageData> getLabledImagesFromDictionary(File dictionary) throws Exception {
		Lable lable = determineLableFromDictionary(dictionary);
		
		//Load all images
		File[] files = dictionary.listFiles();
		ArrayList<ImageData> labledImages = new ArrayList<>();
		for (File file : files) {
			if (file.isFile()) {
				ByteProcessor bp = loadFileToImageProcessor(file);
				labledImages.add(new ImageData(bp, lable));
			}
		}
		return labledImages;
	}

	private ByteProcessor loadFileToImageProcessor(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		return new ByteProcessor(image);
	}

	private Lable determineLableFromDictionary(File dictionary) throws Exception {
		switch (dictionary.getName()) {
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
}
