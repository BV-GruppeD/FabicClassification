package com.bv_gruppe_d.imagej;

import ij.IJ;
import ij.plugin.PlugIn;
import ui.UserInterfaceView;

public class Fabric_Classification implements PlugIn {
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		IJ.showStatus("Work in Progress");
		
		new UserInterfaceView().show();
	}
}
