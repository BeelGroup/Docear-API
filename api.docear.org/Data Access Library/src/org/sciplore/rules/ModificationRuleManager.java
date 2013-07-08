package org.sciplore.rules;

import java.util.HashMap;

public class ModificationRuleManager {
	private static final HashMap<Class<?>, RuleMapper> entities = new HashMap<Class<?>, RuleMapper>();	
	
	public RuleMapper getRuleMapper(Class<?> clazz) {
		return entities.get(clazz);
	}
	
	protected void addRuleMapper(Class<?> clazz, RuleMapper mapper) {
		entities.put(clazz, mapper);
	}

}
