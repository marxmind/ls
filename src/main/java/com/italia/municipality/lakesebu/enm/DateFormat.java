package com.italia.municipality.lakesebu.enm;
/**
 * 
 * @author mark italia
 * @since 01/22/2017
 * @version 1.0
 *
 */
public enum DateFormat {

	YYYY_MM_DD,
	MM_DD_YYYY;
	
	
	public static String YYYY_MM_DD(){
		return "yyyy-MM-dd";
	}
	public static String MM_DD_YYYY(){
		return "MM-dd-yyyy";
	}
}

