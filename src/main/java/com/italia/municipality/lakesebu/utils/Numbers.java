package com.italia.municipality.lakesebu.utils;

/**
 * 
 * @author mark italia
 * @since 09/28/2016
 *
 */

import java.text.DecimalFormat;

public class Numbers {

	public Numbers(){}
	
	
	/**
	 * utils to return two digits e.g 87.69999694824219 become 87.70
	 * @param value
	 * @return
	 */
	public static double formatDouble(double value){
		try{
		DecimalFormat df = new DecimalFormat("####0.00");
		value = Double.valueOf(df.format(value));
		System.out.println("value formatDouble : " + value);
		}catch(Exception e){System.out.println("Error in formatDouble for value : " + value + " error : " + e.getMessage());}
		return value;
	}
	
	public static double formatDouble(String value){
		double val = 0d;
		try{
		val = Double.parseDouble(value.replace(",", ""));	
		DecimalFormat df = new DecimalFormat("####0.00");
		val = Double.valueOf(df.format(val));
		}catch(Exception e){System.out.println("Error in formatDouble for value : " + value + " error : " + e.getMessage());}
		return val;
	}
	
	public static double roundOf(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
}