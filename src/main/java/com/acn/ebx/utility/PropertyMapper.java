package com.acn.ebx.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class PropertyMapper {

	public TreeMap<String, String> getPropertyMap() throws URISyntaxException, IOException {

		TreeMap<String, String> propertyMap = new TreeMap<String, String>();

		Properties prop = new Properties();
		// load a properties file
		File propertiesFile = new File(
				"C:\\Users\\pooja.hd.jain\\DeepBlueAutomation\\DeepBlue\\Novartis DeepBlue\\DeepBlue\\Reference File\\Application.properties");

		InputStream inputStream = new FileInputStream(propertiesFile);

		prop.load(inputStream);
		Set<Object> keys = prop.keySet();

		for (Object k : keys) {
			String propertyName = (String) k;
			String propertyValue = prop.getProperty(propertyName);
			propertyMap.put(propertyName, propertyValue);
		}

		return propertyMap;
	}
}
