package com.italia.municipality.lakesebu.controller;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.italia.municipality.lakesebu.enm.AppConf;

public class ReadConfig {
	
	private final String sep = File.separator;
	private final String PATH = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() + 
								AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +
								AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
								//"C:" + sep + "CheckSystem" + sep + "conf" + sep;
	private final String FILE_NAME = AppConf.APP_CONFIG_SETTING_FILE_NAME.getValue();// "database.xml";
	
	private static final String APPLICATION_FILE = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_CONFIG_SETTING_FILE_NAME.getValue();
	
	
	
	
	public static String value(AppConf conf){
		
		File xmlFile = new File(APPLICATION_FILE);
		if(xmlFile.exists()){
			try {
			
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node node = document.selectSingleNode("/application/" + conf.getValue());
			return node.getText();// node.selectSingleNode(conf.getValue()).getText();
			
			}catch(DocumentException e) {}
		}
		
		return null;
	}
	
}

