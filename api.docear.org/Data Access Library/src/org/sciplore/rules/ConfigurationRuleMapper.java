package org.sciplore.rules;

public class ConfigurationRuleMapper extends RuleMapper {
	
	public ConfigurationRuleMapper(ModificationRule defaultRule) {
		super(defaultRule);
	}

	public ModificationRule getRule(String attributeName) {
		ModificationRule rule = this.attributes.get(attributeName);
		if(rule != null) {
			return rule;
		}
		if (this.objectRule != null) {
			return this.objectRule;
		}
		return ModificationRule.DISCARD;
	}
}
