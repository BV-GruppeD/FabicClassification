package ui;

import ij.IJ;
import javafx.fxml.FXML;

public class UserInterfaceControler {

	@FXML
	private void handleButtonAction() {
		IJ.showMessage("Test");
	  System.out.println("Button Action\n");
	}
	
}
