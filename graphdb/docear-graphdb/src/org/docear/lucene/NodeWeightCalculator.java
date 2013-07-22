package org.docear.lucene;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains a number of help methods that assist in calculating the weight of a node
 * on a use mind map taking into account different parameters (e.g. node depth, number of siblings etc.)
 * 
 * @author gkapi
 */

public class NodeWeightCalculator {
	
	/**
	 * 
	 * @param value: the current weight value
	 * @param parameterValue: the value of the parameter that is going to be applied
	 * @param parameterMetric: specifies the metric for which parametervalue will be considered as in ParameterMetric enumeration
	 * @param operator: the value will be either multiplied with or divided by the parametervalue
	 * @return new weight value
	 */
	public static Double applyParameter(Double value, Double parameterValue, ParameterMetric parameterMetric, ParameterOperator operator) {
		return operator.equals(ParameterOperator.MULTIPLY)? 
				value * parameterFilter(parameterValue, parameterMetric) : value / parameterFilter(parameterValue, parameterMetric);
	}
	
	public static Double parameterFilter(Double parameterValue, ParameterMetric parameterMetric) {
		switch (parameterMetric) {
		case LOG:
			return !new Double(1.0).equals(parameterValue) ? Math.log(parameterValue) : 1.0;
		case LOG10:
			return !new Double(1.0).equals(parameterValue) ? Math.log10(parameterValue) : 1.0;
		case SQRT:
			return Math.sqrt(parameterValue);
		}
		return parameterValue; // for the ABSOLUTE case
	}
	
	public enum ParameterOperator {
		DIVIDE(1), MULTIPLY(2);
		
		private final int value;
		private static final Map<Integer,ParameterOperator> lookup = new HashMap<Integer,ParameterOperator>();
	
		static {
			for(ParameterOperator s : EnumSet.allOf(ParameterOperator.class))
				lookup.put(s.getValue(), s);
		}
		   
        private ParameterOperator(int value) {
        	this.value = value;
        }
        
 	   public int getValue() { 
 		   return this.value; 
 		   }
	
	   public static ParameterOperator get(int value) { 
	        return lookup.get(value); 
	   }
	}
	
	public enum ParameterMetric {
		ABSOLUTE(0), LOG(1), LOG10(2), SQRT(3);
		
		private final int metricValue;

        private ParameterMetric(int metricValue) {
        	this.metricValue = metricValue;                
        }
	}
	
}
