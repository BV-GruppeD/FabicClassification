package classification;

import java.io.File;
import java.util.ArrayList;
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
	 * 1/NumberOfFeatures and the search is executed on a logarithmic scale
	 * @param trainingsData
	 * @return
	 */
	public svm_parameter parameterGridSearch(svm_problem trainingsData, int gridDepth) {		
		optimalClassificationRate = Double.MIN_VALUE;
		parameterResultMap = new ArrayList<ClassifierTestMapping>();
		
		muteLibSvmConsoleOutput();
		
		svm_parameter optimalParameter = null;
		double promisingGammaExp = 0;
		double promisingNU = 0.5;
		for (int i = 0; i < gridDepth; i++) {
			for (int nuIndex = -4; nuIndex < 5; nuIndex++) {
				for (int gammaIndex = -5; gammaIndex < 5; gammaIndex++) {
					double nu = promisingNU + Math.pow(10, -(i+1)) * nuIndex;
					// TODO: Remove magic number
					double gammaExp = promisingGammaExp + Math.pow(10, -i) * gammaIndex;
					double gamma = Math.pow(0.3, gammaExp);
					
					svm_parameter parameter = createParametrizationForLearning(nu, gamma);
					
					if (isNewOptimalParameterSet(parameter, trainingsData)) {
						optimalParameter = parameter;
						promisingNU = nu;
						promisingGammaExp = gammaExp;
					}
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
