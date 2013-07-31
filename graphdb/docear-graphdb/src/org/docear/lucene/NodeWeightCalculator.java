package org.docear.lucene;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.structs.NodeInfo;

/**
 * This class contains a number of help methods that assist in calculating the weight of a node
 * on a use mind map taking into account different parameters (e.g. node depth, number of siblings etc.)
 * 
 * @author gkapi
 */

public class NodeWeightCalculator {
	
	/** 
	 * 
	 * @param forNodes
	 * @param parameterType
	 * @return map that contains a weight for each node based on a specific parameter (e.g. node depth, no children etc.) with an applied metric (e.g. log, log10, etc.)
	 */
	public static Map<String, Double> calculateWeights(List<NodeInfo> forNodes, ParameterType parameterType, ParameterMetric parameterMetric) {
		Map<String, Double> nodeWeights = new HashMap<String, Double>();
		Double value = 0d;
		
		for (NodeInfo node: forNodes) {		
			switch (parameterType) {
			case NODE_DEPTH:
				value = (double)node.getDepth();
				break;
			case NO_SIBLINGS:
				value = (double)node.getNoOfSiblings();
				break;
			case NO_CHILDREN:
				value = (double)node.getNoOfChildren();
				break;
			case WORD_COUNT:
				value = (double)node.getWordCount();
			}
						
			nodeWeights.put(node.getId(), parameterFilter(value, parameterMetric));
		}
		return nodeWeights;
	}
	
	/**
	 * @param nodesWithParameterWeights map containing the nodes and the corresponding weight for each node based on a parameter (e.g. node depth)
	 * @return a map with reversed values (1 / node weight value)
	 */
	public static Map<String, Double> reverseWeights(Map<String, Double> nodesWithParameterWeights) {
		for (Map.Entry<String, Double> entry : nodesWithParameterWeights.entrySet()) 
			entry.setValue(1 / entry.getValue());
		return nodesWithParameterWeights;
	}
	
	/**
	 * @param nodesWeights
	 * @param value
	 * @return the map with its values divided by value
	 */
	public static Map<String, Double> divideWeightsWith(Map<String, Double> nodesWeights, Double valueToDivideWith) {
		if (valueToDivideWith != 0)
			for (Map.Entry<String, Double> entry : nodesWeights.entrySet()) 
			   entry.setValue(entry.getValue() / valueToDivideWith);
		return nodesWeights;
	}
	
	/**
	 * 
	 * @param value: the current weight value
	 * @param parameterValue: the value of the parameter that is going to be applied
	 * @param operator: the value will be either multiplied with or added with the parametervalue
	 * @return new weight value
	 */
	public static Double applyParameter(Double value, Double parameterValue, ParameterOperator operator) {
		switch (operator) {
		case ADD:
			return value + parameterValue;
		default: // covers case MULTIPLY
			return value * parameterValue;
		}
	}
	
	/**
	 * 
	 * @param parameterValue the value that needs to be adapted
	 * @param parameterMetric the metric to be used that can be either the absolute value of the parameter or its log, log10, sqrt or relative to the max value
	 * @return the modified value
	 */
	public static Double parameterFilter(Double parameterValue, ParameterMetric parameterMetric) {
		switch (parameterMetric) {
		case LOG:
			return Math.log(parameterValue + 2); // add 2 to avoid getting infinite or zero as result
		case LOG10:
			return Math.log10(parameterValue + 2); // add 2 to avoid getting infinite or zero as result
		case SQRT:
			return Math.sqrt(parameterValue + 2); // add 2 to avoid getting zero as result and keep this in consistency with log and log10 cases
		default: // for the ABSOLUTE case
			return parameterValue + 1; // add 1 to avoid getting zero as result
		}
	}

	/**
	 * @param nodesWithWeights
	 * @return the max value in the map
	 */
	public static Double getMaxValue(Map<String, Double> nodesWithWeights) {
		Double maxValue = 0d;
		for (Double value: nodesWithWeights.values())
			if (value > maxValue) maxValue = value;
		return maxValue;
	}
	
	public enum ParameterType {
		NODE_DEPTH(0), NO_SIBLINGS(1), NO_CHILDREN(2), WORD_COUNT(3);
		
		private final int value;
		private static final Map<Integer,ParameterType> lookup = new HashMap<Integer,ParameterType>();
	
		static {
			for(ParameterType s : EnumSet.allOf(ParameterType.class))
				lookup.put(s.getValue(), s);
		}
		   
        private ParameterType(int value) {
        	this.value = value;
        }
        
 	   public int getValue() { 
 		   return this.value; 
 	   }
	
	   public static ParameterType get(int value) { 
	        return lookup.get(value); 
	   }
	}
	
	public enum ParameterMetric {
		ABSOLUTE(0), LOG(1), LOG10(2), SQRT(3), RELATIVE(4);
		
		private final int value;
		private static final Map<Integer,ParameterMetric> lookup = new HashMap<Integer,ParameterMetric>();
	
		static {
			for(ParameterMetric s : EnumSet.allOf(ParameterMetric.class))
				lookup.put(s.getValue(), s);
		}
		   
        private ParameterMetric(int value) {
        	this.value = value;
        }
        
 	   public int getValue() { 
 		   return this.value; 
 	   }
	
	   public static ParameterMetric get(int value) { 
	        return lookup.get(value); 
	   }
	}
	
	public enum ParameterOperator {
		ADD(0), MULTIPLY(1);
		
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
	
}
