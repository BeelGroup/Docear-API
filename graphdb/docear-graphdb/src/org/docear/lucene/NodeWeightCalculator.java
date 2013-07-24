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
			
			if (parameterMetric.equals(ParameterMetric.RELATIVE)) 
				nodeWeights.put(node.getId(), parameterFilter(value, parameterMetric, (double)getMaxParameterValue(forNodes, parameterType)));
			else nodeWeights.put(node.getId(), parameterFilter(value, parameterMetric, null));
		}
		return nodeWeights;
	}
	
	/**
	 * @param nodesWithParameterWeights map containing the nodes and the corresponding weight for each node based on a parameter (e.g. node depth)
	 * @return a map with reversed values (1 / node weight value)
	 */
	public static Map<String, Double> reverseWeights(Map<String, Double> nodesWithParameterWeights) {
		for (Map.Entry<String, Double> entry : nodesWithParameterWeights.entrySet()) 
		   // reverse only non zero values
		   if (entry.getValue() != 0) entry.setValue(1 / entry.getValue());
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
			return parameterValue == 0 ? value : value * parameterValue;
		}
	}
	
	/**
	 * 
	 * @param parameterValue the value that needs to be adapted
	 * @param parameterMetric the metric to be used that can be either the absolute value of the parameter or its log, log10, sqrt or relative to the max value
	 * @param maxValue used only when calculating the relative value (relative = absolute / max value from the map)
	 * @return the modified value
	 */
	public static Double parameterFilter(Double parameterValue, ParameterMetric parameterMetric, Double maxValue) {
		switch (parameterMetric) {
		case LOG:
			// calculate only got non zero values
			return parameterValue == 0 ? 0 : Math.log(parameterValue);
		case LOG10:
			// calculate only got non zero values
			return parameterValue == 0 ? 0 : Math.log10(parameterValue);
		case SQRT:
			return Math.sqrt(parameterValue);
		case RELATIVE:
			// return the same value if max value is zero
			return maxValue == 0 ? parameterValue : parameterValue / maxValue;
		default:
			return parameterValue; // for the ABSOLUTE case
		}
	}
	
	/**
	 * @param forNodes
	 * @param parameterType
	 * @return the maximum value in the nodes list for the parameter type (e.g. node depth) used as input
	 */
	public static Integer getMaxParameterValue(List<NodeInfo> forNodes, ParameterType parameterType) {
		Integer maxValue = 0;
		Integer value = 0;
		for (NodeInfo nodeInfo : forNodes) {
			switch (parameterType) {
			case NODE_DEPTH:
				value = nodeInfo.getDepth();
				break;
			case NO_SIBLINGS:
				value = nodeInfo.getNoOfSiblings();
				break;
			case NO_CHILDREN:
				value = nodeInfo.getNoOfChildren();
				break;
			case WORD_COUNT:
				value = nodeInfo.getWordCount();
			}
		    if (value.compareTo(maxValue) > 0) maxValue = value;
		}
		return maxValue;
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
