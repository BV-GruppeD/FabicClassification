# Fabric classification by measuring the stretch of the material through image analysis

A Maven project implementing an ImageJ 1.x plugin.

The plugin takes a set of training samples classified as  no stretch, medium stretch or strong stretch and learns a classifier that can be tested on unknown data.
The classification is done based on the analysis of ellipses in the fabric images.

# Instructions to get the plugin going
## See the Code
To open the plugin in eclipse go to File->Import...->Maven->Existing Maven Project and click Next. Browse to the directory containing this project files an click open. The required .pom file should be visible and selected in the box below. Click Import to see the project structure.

## Get the plugin in ImageJ
To build the project see instructions here:


Place the .jar file in the plugins directory from your ImageJ application. The plugin can be found under Plugins->Stoff Klassifikation

## User instructions


## Required directory strucure
- geschert
- keineDehnung
- maximaleDehnung
- mittlereDehnung
- stoerung
