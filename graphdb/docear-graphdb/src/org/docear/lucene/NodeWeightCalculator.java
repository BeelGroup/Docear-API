package org.docear.lucene;

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
	 * @param operator: the value will be either multiplied with or divided by the parametervalue
	 * @return new weight value
	 */
	public static Double applyParameter(Double value, Double parameterValue, ParameterOperator operator) {
		return operator.equals(ParameterOperator.MULTIPLY)? value * parameterValue : value / parameterValue;
	}
	
	public enum ParameterOperator {
		DIVIDE, MULTIPLY
	}

}
