package com.italia.municipality.lakesebu.utils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Validating URL if accessible or valid
 * @author Mark Italia
 * @version 1.0
 * @since 4/5/2024
 */
public class ValidateURL {
	
	public static void main(String[] args) {
		try {
			ValidateURL.isValidURL("http://mto-server:8081/ls/marxmind/portal.xhtml");
		} catch (MalformedURLException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isValidURL(String url) throws MalformedURLException, URISyntaxException{
		try {
			new URL(url).toURI();
			System.out.println("This url is valid " + url);
			return true;
		}catch(MalformedURLException me) {
			System.out.println("This url is not valid ");
			return false;
		}catch(URISyntaxException ue) {
			System.out.println("This url is not valid ");
			return false;
		}
	}
	
}
