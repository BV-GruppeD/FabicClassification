package classification;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bv_gruppe_d.imagej.CsvInputOutput;

import libsvm.svm;
import libsvm.svm_parameter;
import libsvm.svm_problem;

/**
 * Provides methods to search for the parameters of a nu-SVM and holds informations about the last 
 * search procedure.
 */
public class SVMParameterSearch {
	
	private List<ClassifierTestMapping> parameterResultMap;
	private double optimalClassificationRate;
	
	
	/**
	 * Executes a parameter search on a 10 by 10 grid. By definition the nu-parameter is bounded 
	 * on the interval between 0 and 1. The recommended starting point for the gamma-parameter is
	 * 1/NumberOfFeatures and the search is executed on a logarithmic scale.
	 * @param trainingsData
	 * @return
	 */
	public svm_parameter parameterGridSearch(svm_problem trainingsData, int gridDepth) {		
		optimalClassificationRate = Double.MIN_VALUE;
		parameterResultMap = new ArrayList<ClassifierTestMapping>();
		
		muteLibSvmConsoleOutput();
		
		// Possible cause no sparse data is used in this application
		int numberOfFeatures = trainingsData.x[0].length;
		double maximalNu = calculateMaximalNu(trainingsData);
		svm_parameter optimalParameter = null;
		for (double nu = 0; nu < maximalNu; nu += maximalNu/20) {
			for (double gammaExp = -4; gammaExp < 15; gammaExp+=0.5) {
				double gamma = Math.pow(1.0/numberOfFeatures, gammaExp);
				svm_parameter parameter = createParametrizationForLearning(nu, gamma);
				if (isNewOptimalParameterSet(parameter, trainingsData)) {
					optimalParameter = parameter;
				}
			}
		}
		return optimalParameter;
	}

	/**
	 * ImageJ provides a separate Console with some Installations (on Windows in our case) which 
	 * runs on the UI-Thread of the Application. By default LibSVM prints the results of an 
	 * execution of the Learning and Cross-validation methods to this console.
	 * When executing the grid search from this class, that behavior causes the Application to hang
	 * and extends the execution time by round about 5000%!
	 * 
	 * Since the console with the output is not shown by default and the informations are not 
	 * processed any further, simple muting does the trick.
	 */
	private void muteLibSvmConsoleOutput() {
		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    @Override public void print(String s) {} // Disables svm output
		});
	}

	/**
	 * Calculates the upper limit of the admissible interval for the parameter nu.
	 * (Lower limit is equal to 0 in any case)
	 * 
	 * This depends upon how unbalanced the data set is that we deal with. For a 
	 * uniformly sized classes the value equals one and gets lower as the unbalance 
	 * in the data set gets greater.
	 * 
	 * (See: http://is.tuebingen.mpg.de/fileadmin/user_upload/files/publications/pdf3353.pdf 
	 * for a more detailed explanation)
	 * 
	 * @param trainingsData The data set upon which the calculation is executed.
	 * @return The upper limit of the admissible interval for the parameter nu.
	 */
	private double calculateMaximalNu(svm_problem trainingsData) {
		double[] labels = trainingsData.y;
		
		int[] labelOccurrences = countLabelOccurances(labels);
		double minimalNuMax = 1.0;
		for (int i = 0; i < labelOccurrences.length; i++) {
			for (int j = i+1; j < labelOccurrences.length; j++) {
				minimalNuMax = Math.min(minimalNuMax, calculateNuMax(labelOccurrences[i],labelOccurrences[j]));
			}
		}
		return minimalNuMax;
	}
	
	private int[] countLabelOccurances(double[] labels) {
		int[] labelOccurrences = new int[countNumberOfDifferentLabels(labels)];
		for (int i = 0; i < labels.length; i++) {
			labelOccurrences[(int) Math.round(labels[i]) - 1]++;
		}
		return labelOccurrences;	
	}
	
	private int countNumberOfDifferentLabels(double[] labels) {
		double numberOfDifferentLabels = Arrays.stream(labels).max().getAsDouble();
		return (int) Math.round(numberOfDifferentLabels);
	}
	
	private double calculateNuMax(int mI, int mJ) {
		return 2*Math.min(mI, mJ) / (mI + mJ);
	}
	
	/**
	 * Defines the parameter set used internally in the library. For descriptions of the parameters 
	 * the link mentioned above provides informations.
	 * @param gamma 
	 * @param nu 
	 * 
	 * @return The initialized parameter set for the training algorithm in LibSVM
	 */
	private svm_parameter createParametrizationForLearning(double nu, double gamma) {
		svm_parameter parameters = new svm_parameter();
		parameters.svm_type = svm_parameter.NU_SVC;
		parameters.kernel_type = svm_parameter.RBF;
		parameters.cache_size = 1000;
		parameters.eps = 0.00001;
		parameters.nu = nu;
		parameters.gamma = gamma;
		
		return parameters;
	}
	
	private boolean isNewOptimalParameterSet(svm_parameter parameter, svm_problem trainingsData) {
		
		double[] results = new double[trainingsData.y.length];
		svm.svm_cross_validation(trainingsData, parameter, 4, results);		
		double classificationRate = calculateClassificationRate(trainingsData.y,results);

		// Log process step
		parameterResultMap.add(new ClassifierTestMapping(parameter.gamma, parameter.nu, classificationRate));
		System.out.println("Nu: " + parameter.nu + " gammaExp: " + parameter.gamma + " classification rate: " + classificationRate);
		
		if (classificationRate > optimalClassificationRate) {
			optimalClassificationRate = classificationRate;
			return true;
		}		
		return false;
	}

	private double calculateClassificationRate(double[] targetResults, double[] acturalResults) {
		double sum = 0;
		for (int i = 0; i < targetResults.length; i++) {
			// We expected only near integer values, thereby no precise check is necessary
			if (Math.abs(targetResults[i]-acturalResults[i]) < 0.1) {
				sum++;
			}
		}
		return sum/targetResults.length;
	}
	
	/**
	 * Writes a csv-File to the home directory containing a mapping of SVM parameters to the 
	 * cross-validation classification rate from the last executed search. If non was done before,
	 * no file is added to the directory.
	 * 
	 * The purpose of this method is mainly the evaluation of the implemented method.
	 */
	public void saveParameterSearchLogToFile() {
		File logFile = new File(System.getProperty("user.home"), "parameterGridSearchResults.txt");
		CsvInputOutput.writeGridSearchLog(logFile.getAbsolutePath(), parameterResultMap);
	}
}
