package com.italia.municipality.lakesebu.licensing.controller;

import java.io.FileInputStream;
import java.util.Properties;

import com.italia.municipality.lakesebu.enm.AppConf;

/**
 * 
 * @author mark italia
 * @since 07/13/2018
 * @version 1.0
 *
 */
public class DocumentFormatter {

	private static final String PROPERTY_FILE = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() + AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() + "documentFormatter.bris";
	
	public static String getTagName(String tagName){
		Properties prop = new Properties();
		try{
			prop.load(new FileInputStream(PROPERTY_FILE));
			System.out.println(tagName+"="+prop.getProperty(tagName));
			return prop.getProperty(tagName);
		}catch(Exception e){}
		return "";
	}
	
}