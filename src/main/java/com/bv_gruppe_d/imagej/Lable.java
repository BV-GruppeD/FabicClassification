package com.bv_gruppe_d.imagej;

import java.util.HashMap;
import java.util.Map;

public enum Lable {
	NO_STRETCH(1.0),
	MEDIUM_STRETCH(2.0),
	MAXIMUM_STRECH(3.0),
	SHEARD(4.0),
	DISTURBANCE(5.0),
	UNKNOWN(6.0);
	
    private double numericRepresentation;
    private static Map representationMapping = new HashMap<>();

    Lable(double numericRepresentation) {
        this.numericRepresentation = numericRepresentation;
    }

    public double getNumericRepresentation() {
        return numericRepresentation;
    }
    
    static {
        for (Lable label: Lable.values()) {
            representationMapping.put(label.numericRepresentation, label);
        }
    }

    public static Lable valueOf(double label) {
        return (Lable) representationMapping.get(label);
    }
}
