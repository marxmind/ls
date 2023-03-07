package com.italia.municipality.lakesebu.licensing.controller;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * 
 * @author mark italia
 * @since 12/26/2017
 * @version 1.0
 *
 */
public class Words {

	private static String wordContentSource() {
		return DocumentFormatter.getTagName("documentContentSource");
	}
	
	public static String getTagName(String tagName){
		Properties prop = new Properties();
		try{
			prop.load(new FileInputStream(wordContentSource()));
			return prop.getProperty(tagName);
		}catch(Exception e){}
		return "";
	}
	
}
