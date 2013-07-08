package org.sciplore.rules;

import java.util.HashMap;

public class RuleMapper {
	protected final ModificationRule objectRule;
	protected HashMap<String, ModificationRule> attributes = new HashMap<String, ModificationRule>();
	
	public RuleMapper(ModificationRule defaultRule) {
		objectRule = defaultRule;
	}
	
	public ModificationRule getRule(String attributeName) {
		return ModificationRule.DISCARD;
	}
	
	public void addAttributeRule(String attributeName, ModificationRule rule) {
		attributes.put(attributeName, rule);
	}
	
	
}
