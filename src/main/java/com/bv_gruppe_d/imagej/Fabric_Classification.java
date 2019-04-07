package com.bv_gruppe_d.imagej;

import ij.plugin.PlugIn;
import userinterface.UserInterfaceView;

/**
 * Starting point of the PlugIn invocation which triggers the creation of the user interface.
 */
public class Fabric_Classification implements PlugIn {

	@Override
	public void run(String arg0) {
		new UserInterfaceView().show();
	}
}
