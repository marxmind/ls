package com.italia.municipality.lakesebu.security;

import java.util.Base64;

import com.italia.municipality.lakesebu.enm.AppConf;

/**
 * 
 * @author mark italia
 * @since 11/16/2016
 * @version 1.0
 * this class is use for encoding and decoding of character
 */
public class SecureChar {

	public static String encode(String val){
		
		try{
		// encode with padding
		String encoded = Base64.getEncoder().encodeToString(val.getBytes(AppConf.SECURITY_ENCRYPTION_FORMAT.getValue()));
		// encode without padding
		//String encoded = Base64.getEncoder().withoutPadding().encodeToString(val.getBytes(Ipos.SECURITY_ENCRYPTION_FORMAT.getName()));
		
		return encoded;
		}catch(Exception e){}
		return null;
	}
	public static String decode(String val){
		try{
			byte [] barr = Base64.getDecoder().decode(val);
			return new String(barr,AppConf.SECURITY_ENCRYPTION_FORMAT.getValue());
			}catch(Exception e){}
			return null;
	}
	
	
}