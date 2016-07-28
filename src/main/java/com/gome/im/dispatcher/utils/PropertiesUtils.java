package com.gome.im.dispatcher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

	public static Properties LoadProperties(String fileName) {
		Properties properties = new Properties();
		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			properties.load(in); //载入文件
			in.close();
			return properties;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}