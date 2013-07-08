package org.sciplore.xml;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ExternalizedStrings {
	private static final String BUNDLE_NAME = "org.sciplore.xml.ExternalizedStrings"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private ExternalizedStrings() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
