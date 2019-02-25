package com.bv_gruppe_d.imagej;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the different labels that are used for data classification in this application.
 */
public enum Lable {
	NO_STRETCH(1.0),
	MEDIUM_STRETCH(2.0),
	MAXIMUM_STRECH(3.0),
	SHEARD(4.0),
	DISTURBANCE(5.0),
	UNKNOWN(6.0);
	
    private double numericRepresentation;
    private static Map<Double, Lable> representationMapping = new HashMap<>();

    Lable(double numericRepresentation) {
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
        for (Lable label: Lable.values()) {
            representationMapping.put(label.numericRepresentation, label);
        }
    }

    /**
     * Maps the provided numeric representation to the corresponding label.
     */
    public static Lable valueOf(double numericRepresentation) {
        return (Lable) representationMapping.get(numericRepresentation);
    }
}
