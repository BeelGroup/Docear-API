package org.sciplore.database;

public class HibernateConfigurationInfo {
	private static String configFile = null;
	
	public static String getConfigurationFile() {
		return configFile;
	}
	
	public HibernateConfigurationInfo() {
	}
	
	public void setConfigPath(String path) {
		configFile = path;
	}
}
