# Fabric classification by measuring the stretch of the material through image analysis

This project was carried out as an examination for the course "Bildverarbeitung" at the [University of Applied Science Lemgo](https://www.hs-owl.de/en/campus/wir-ueber-uns.html).

Contributors:         Micha Bayer, Marcel Sandermann, Patrick Schlüter, Daniel Wolf\
Project Supervisors:  Dipl.-Inform. Jan Leif Hoffmann & Prof. Dr.-Ing. Volker Lohweg\
Project Kick-Off:     23.01.2019\
Submission Date:      08.04.2019

#TODO?
A Maven project implementing an [ImageJ](https://imagej.net/) 1.x plugin.

The plugin takes a set of training samples classified as no stretch, medium stretch, strong stretch, sheared or disturbed and learns a support vector machines (SVM) as a classifier that you can test on unknown data.
The classification is done based on the analysis of ellipses in the fabric images with the Hough-Transformation.

## Instructions to get the plugin going
### See the Code
To open the plugin in [Eclipse](https://www.eclipse.org/) go to File->Import...->Maven->Existing Maven Project and click Next. Browse to the directory containing this project and click “Open”. The required pom file should be visible and selected in the box below. Click Import to see the project structure.

### Get the plugin in ImageJ
There are several ways to build the project.
As an easy way in eclipse you can build the project with a maven build configuration.
Make sure to add the following parameter to the Run Configuration:
> name: imagej.app.directory\
value: /path/to/ImageJ.app/   (The path to the directory containing the ImageJ.exe on your system)

After the build process finished a .jar file appears in the plugins directory of your ImageJ installation. Additionally the .jar files of the external libraries are downloaded in the build process and copied to the plugins directory as well.
Now start ImageJ, the plugin appears under the Plugins menu as "Stoff Klassifikation".\
(__Note__: If the build process is executed behind a proxy server, this server has to be defined in a settings.xml file in the main project folder)

## User instructions
After the UI appears you can load a folder with training images. The selected directory has to contain sub-folders as listed below.
After selecting the root directory, the application will load the images and label them according to the sub-folder they were found in. A Pop-Up displays the number of images that were successfully loaded and the directory paths that were recognized.

Afterward the classifier can be trained by clicking the button below. The progress bar shows the number of images that were processed and a Pop-Up containing information about the classifier signals the end of the training process. The chart in the bottom right corner will update automatically at this point and display the extracted feature vectors from the trainings set.
At this point the “Speichern” Button can be used to store the values of the feature vectors in a .csv file.
The file can be found under the __home directory__ which varies depending on the OS.
(C:\Users\\\<Username> in case of a standard Windows-System, /home/\<username> on Linux)

To test the trained classifier with a set of images select the Button to load the images in the area below. The same remarks as above hold true for this process. Only will the application present a detailed description of the classifiers performance on the test set at the end of the computation.
The feature vectors created now can also be saved in the respective home directory.

As an alternative to the procedure above a previously stored .csv file for training/testing can be loaded by clicking the “Laden” button.
<!---When the application is now indicated to train or test the classifier it will take the feature vectors at hand.--->
(Note: Should the button to load images from a directory to the application have been clicked once since the start of the application, it will always execute the full image processing pipe an thereby create a new set of feature vectors.)

Last a single image can be classified by the application after a classifier was training by clicking the button in the lower left corner. The selected image has to be a .png or .jpg file and will be shown in the area in the upper right corner of the software.

### Required directory structure
- geschert
- keineDehnung
- maximaleDehnung
- mittlereDehnung
- stoerung

## Coding Guidelines
The code in this repository is designed by the following guidelines to increase readability and abide by Java standards.
- Notations
  - Class and interface names start with a capital letter and are nouns
  - Method names start with a lower case letter and are verbs
  - Variables and parameters start with a lower case letter
  - Variables with the final keyword are written in capital letters and underscores
  - Package names start with a lower case letter
  - All names are written in English and CamelCase
  - All names are meaningful, only iterator variables are allowed to be an abbreviation or a single character
- Comments
  - All comments are written in English
  - All public classes and methods (without getters and setters) have a JavaDoc comment
  - One line comments are replaced by extracting method with meaningful names if possible
- Statements
  - Only one statement per line#
  - Statement inside curly brackets {...} are indented with 4 Spaces or 1 Tab
  - Conditions and loops start in a separate line with an opening curly bracket at the end
  - Closing curly brackets get a separate line
  - No empty catch blocks exist

## External Libraries
The plugin uses four open source libraries which are listed below:
[LibSVM](https://github.com/cjlin1/libsvm) by C.-C. Chang [C.-J. Lin](https://github.com/cjlin1)\
License: [Copyright (c) 2000-2018 Chih-Chung Chang and Chih-Jen Lin](https://github.com/cjlin1/libsvm/blob/master/COPYRIGHT)

[confusion-matrix](https://github.com/habernal/confusion-matrix) by [Ivan Habernal](https://github.com/habernal)\
License: [Apache License 2.0](https://github.com/habernal/confusion-matrix/blob/master/LICENSE)

[super-csv](https://github.com/super-csv/super-csv) by [Kasper B. Graversen](https://github.com/kbilsted) et al.\
License: [Apache License 2.0](https://github.com/super-csv/super-csv/blob/master/LICENSE.txt)

[ImageJ](https://github.com/imagej/imagej)\
License:
[Copyright (c) 2009 - 2015, Board of Regents of the University of
Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
Institute of Molecular Cell Biology and Genetics.](https://github.com/imagej/imagej/blob/master/LICENSE.txt)
