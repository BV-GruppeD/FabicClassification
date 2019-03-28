Fabric classification by measuring the stretch of the material through image analysis

A Maven project implementing an ImageJ 1.x plugin.

The plugin takes a set of training samples classified as no stretch, medium stretch, strong stretch, sheared or disturbed and learns a support vector machines (SVM) as a classifier that you can test on unknown data.
The classification is done based on the analysis of ellipses in the fabric images with the Hough-Transformation.

# Instructions to get the plugin going
## See the Code
To open the plugin in eclipse go to File->Import...->Maven->Existing Maven Project and click Next. Browse to the directory containing this project files and click “Open”. The required pom file should be visible and selected in the box below. Click Import to see the project structure.

## Get the plugin in ImageJ
There are several ways to build the project.
As an easy way in eclipse you can build the project with a maven build configuration.
After the build process finished a .jar file appears in the target directory of your project (if none exist it is created), copy this file in the plugins folder of your ImageJ installation.
Now start ImageJ, the Plugin appears under the Plugins-Menu->Stoff Klassifikation.

## User instructions
After the UI appears you can load a folder with training images. The selected directory has to contain sub-folders as listed below.
After selecting the upper directory, the application will autoload the images and label them according to the sub-folder they were found in. A Pop-Up displays the number of images that were successfully loaded and the directory path that were recognized.

Afterward the classifier can be trained by clicking the Button below. The Progress-Bar shows the number of images that were processed and a Pop-Up containing information about the classifier signals the end of the training process. The Chart at the bottom right corner will update automatically at this point and display the extracted feature from the trainings set.
At this point the “Speichern” Button can be used to store the values of the feature vectors in a .csv File. 
The File can be found under the __home directory__ which varies depending on the OS.
(C:\Users\<Username> in case of a standard Windows-System)

To test the trained classifier with a set of images select the Button to load the images in the area below. The same remarks as above hold true for this process. Only will the application present a detailed description of the classifiers performance on the test set at the end of the computation.
You can The feature vectors created now can also be saved in the respective home directory.

As an alternative to the procedure above a previously stored .csv file for training/testing can be loaded by clicking the “Laden” Button. When the application is now indicated to train or test the classifier it will take the feature vectors at hand. 
(Note: Should the Button to load images from a directory to the application have been clicked once since the start of the application, it will always execute the full image processing pipe an thereby create a new set of feature vectors.)

Last a single image can be classified by the application after a classifier was training by clicking the button in the lower left corner. The selected image has to be of the png or jpg format and will be shown in the area in the upper right corner of the software.

## Required directory strucure
- geschert
- keineDehnung
- maximaleDehnung
- mittlereDehnung
- stoerung
