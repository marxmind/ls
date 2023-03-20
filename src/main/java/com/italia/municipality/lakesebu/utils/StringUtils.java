package com.italia.municipality.lakesebu.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author mark italia
 * @since 09/28/2016
 *
 */

public class StringUtils {

	private static final String BACK_SLASH = "\\";
	private static final String SLASH = "/";
	private static final String SINGLE_QUOTE = "'";
	private static final String DOUBLE_QUOTE = "\"";
	
	public static String removeSpecialChar(String val){
		
		String chars[] = new String[]{"\\","/","'","\"",";"};
		
		
		for(int i=0; i<chars.length;i++){
			val = val.replace(chars[i], "");
		}
		
		/*val = val.replace(BACK_SLASH, "");
		val = val.replace(SLASH, "");
		val = val.replace(SINGLE_QUOTE, "");
		val = val.replace(DOUBLE_QUOTE, "");*/
		
		return val;
	}
	
	/**
	 * 
	 * @param str
	 * @return converted first char into uppercase
	 */
	public static String convertFirstCharToUpperCase(String str) {
		try {
		char ch[] = str.toCharArray();
		
		for(int i = 0; i < str.length(); i++) {
			if(i == 0 && ch[i] != ' ' || ch[i] !=' ' && ch[i-1] == ' ') {
				if(ch[i] >= 'a' && ch[i] <= 'z') {
					ch[i] = (char)(ch[i] - 'a' + 'A');
				}
			}else if(ch[i] >= 'A' && ch[i] <= 'Z') {
				ch[i] = (char) (ch[i] + 'a' - 'A');
			}
		}
		String st = new String(ch);
		
		return st;
		}catch(Exception e) {}
		
		return str;
	}
	
	public static String convertToUTF8(String raw){
		ByteBuffer buffer = StandardCharsets.UTF_8.encode(raw); 
		String utf8EncodedString = StandardCharsets.UTF_8.decode(buffer).toString();
		return utf8EncodedString;
	}
	
	public static void main(String[] args) {
		//System.out.println(convertFirstCharToUpperCase("geeek For The cause"));
		
		
		String val = "50000.0<@>0.0<@>0.0<@>5.0<@>35.0<@>0.0<@>0.0<@>40.0<@>2.1<@>42.1<@>1<@>2021-03-08<@>123-456-789-0000<@>5\"7'<@>75kg<@>filipino<@>1<@>houseman<@>FERDINAND L. LOPEZ<@>lake sebu<@>fils";
		String[] out = val.split("<@>");
		System.out.println(out[0]);
		
	}
	
}