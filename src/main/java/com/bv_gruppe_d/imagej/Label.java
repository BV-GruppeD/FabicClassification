package com.bv_gruppe_d.imagej;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the different labels that are used for data classification in this application.
 */
public enum Label {
	NO_STRETCH(1.0),
	MEDIUM_STRETCH(2.0),
	MAXIMUM_STRECH(3.0),
	SHEARD(4.0),
	DISTURBANCE(5.0),
	UNKNOWN(6.0);
	
    private double numericRepresentation;
    private static Map<Double, Label> representationMapping = new HashMap<>();

    Label(double numericRepresentation) {
        this.numericRepresentation = numericRepresentation;
    }

    /**
     * Provides a numeric representation for this label.
     * @return A double value that is mapped to this label.
     */
    public double getNumericRepresentation() {
        return numericRepresentation;
    }
    
    /**
     * Creates a dictionary for reverse mapping of the numeric representation.
     */
    static {
        for (Label label: Label.values()) {
            representationMapping.put(label.numericRepresentation, label);
        }
    }

    /**
     * Maps the provided numeric representation to the corresponding label.
     */
    public static Label valueOf(double numericRepresentation) {
        return (Label) representationMapping.get(numericRepresentation);
    }
    
    /**
     * Checks of two elements of this type are of the same value.
     * @param actualLabel First value to compare.
     * @param targetLabel Second value to compare.
     * @return True if the parameters contain the same value.
     */
    public static boolean areEqual(Label actualLabel, Label targetLabel) {
    	// Because this type only contains integer like values an unprecise check is sufficient.
    	if (Math.abs(targetLabel.getNumericRepresentation()-actualLabel.getNumericRepresentation())<0.1) {
			return true;
		}
    	return false;
    }
}
