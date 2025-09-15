package com.italia.municipality.lakesebu.json;

/**
 * Encoding and Decoding character
 * This is use for json character
 */
public class CharacterLibrary {
	
	public static void main(String[] args) {
		
		String uname = readCharacterEncode("arnold");
		String password = readCharacterEncode("9632");
		String passcode = readCharacterEncode("9632");
		String fullName = readCharacterEncode("ARNOLD BANDALAN");
		
		System.out.println("Decode:"+ readCharacterDecode("2349"));
		
		System.out.println(uname + "\n" + password + "\n" + passcode +"\n" + fullName);
		
	}
	
	public static String readCharacterEncode(int word) {
		return readCharacterEncode(word+"");
	}
	public static String readCharacterEncode(String word) {
		//System.out.println("BEFORE: " + word);
		String val="";
		int count = word.length();
		String newVal="";
		for(int i=0; i<count; i++) {
			val = word.substring(i, i+1);
			newVal+=encodedChar(val);
		}
		//System.out.println("AFTER: " + newVal);
		return newVal;
	}
	public static String readCharacterDecode(int word) {
		return readCharacterDecode(word+"");
	}
	public static String readCharacterDecode(String word) {
		//System.out.println("BEFORE: " + word);
		String val="";
		int count = word.length();
		String newVal="";
		for(int i=0; i<count; i++) {
			val = word.substring(i, i+1);
			newVal+=decodeChar(val);
		}
		//System.out.println("AFTER: " + newVal);
		return newVal;
	}
	
	
	public static String encodedChar(String ch) {
		String newChar = ch;
		boolean isFound=false;
		switch(ch) {
		
		case "0" : newChar="9"; isFound=true; break;
		case "1" : newChar="8"; isFound=true; break;
		case "2" : newChar="7"; isFound=true; break;
		case "3" : newChar="6"; isFound=true; break;
		case "4" : newChar="5"; isFound=true; break;
		case "5" : newChar="0"; isFound=true; break;
		case "6" : newChar="1"; isFound=true; break;
		case "7" : newChar="2"; isFound=true; break;
		case "8" : newChar="3"; isFound=true; break;
		case "9" : newChar="4"; isFound=true; break;
		
		}
		
		if(!isFound) {// if not found in numbers this condition will process
		
			switch(ch) {
			
			case "a" : newChar="q"; isFound=true; break;
			case "b" : newChar="w"; isFound=true; break;
			case "c" : newChar="e"; isFound=true; break;
			case "d" : newChar="r"; isFound=true; break;
			case "e" : newChar="t"; isFound=true; break;
			case "f" : newChar="y"; isFound=true; break;
			case "g" : newChar="u"; isFound=true; break;
			case "h" : newChar="i"; isFound=true; break;
			case "i" : newChar="o"; isFound=true; break;
			case "j" : newChar="p"; isFound=true; break;
			case "k" : newChar="a"; isFound=true; break;
			case "l" : newChar="s"; isFound=true; break;
			case "m" : newChar="d"; isFound=true; break;
			case "n" : newChar="f"; isFound=true; break;
			case "o" : newChar="g"; isFound=true; break;
			case "p" : newChar="h"; isFound=true; break;
			case "q" : newChar="j"; isFound=true; break;
			case "r" : newChar="k"; isFound=true; break;
			case "s" : newChar="l"; isFound=true; break;
			case "t" : newChar="z"; isFound=true; break;
			case "u" : newChar="x"; isFound=true; break;
			case "v" : newChar="c"; isFound=true; break;
			case "w" : newChar="v"; isFound=true; break;
			case "x" : newChar="b"; isFound=true; break;
			case "y" : newChar="n"; isFound=true; break;
			case "z" : newChar="m"; isFound=true; break;
			
			case "A" : newChar="Q"; isFound=true; break;
			case "B" : newChar="W"; isFound=true; break;
			case "C" : newChar="E"; isFound=true; break;
			case "D" : newChar="R"; isFound=true; break;
			case "E" : newChar="T"; isFound=true; break;
			case "F" : newChar="Y"; isFound=true; break;
			case "G" : newChar="U"; isFound=true; break;
			case "H" : newChar="I"; isFound=true; break;
			case "I" : newChar="O"; isFound=true; break;
			case "J" : newChar="P"; isFound=true; break;
			case "K" : newChar="A"; isFound=true; break;
			case "L" : newChar="S"; isFound=true; break;
			case "M" : newChar="D"; isFound=true; break;
			case "N" : newChar="F"; isFound=true; break;
			case "O" : newChar="G"; isFound=true; break;
			case "P" : newChar="H"; isFound=true; break;
			case "Q" : newChar="J"; isFound=true; break;
			case "R" : newChar="K"; isFound=true; break;
			case "S" : newChar="L"; isFound=true; break;
			case "T" : newChar="Z"; isFound=true; break;
			case "U" : newChar="X"; isFound=true; break;
			case "V" : newChar="C"; isFound=true; break;
			case "W" : newChar="V"; isFound=true; break;
			case "X" : newChar="B"; isFound=true; break;
			case "Y" : newChar="N"; isFound=true; break;
			case "Z" : newChar="M"; isFound=true; break;
			
			case "ñ" : newChar="Ñ"; isFound=true; break;
			case "Ñ" : newChar="ñ"; isFound=true; break;
			
			
			}
		
		}
		
		
		return newChar;
	}
	
	public static String decodeChar(String ch) {
		String newChar = ch;
		boolean isFound=false;
		switch(ch) {
		
		case "9" : newChar="0"; isFound=true; break;
		case "8" : newChar="1"; isFound=true; break;
		case "7" : newChar="2"; isFound=true; break;
		case "6" : newChar="3"; isFound=true; break;
		case "5" : newChar="4"; isFound=true; break;
		case "0" : newChar="5"; isFound=true; break;
		case "1" : newChar="6"; isFound=true; break;
		case "2" : newChar="7"; isFound=true; break;
		case "3" : newChar="8"; isFound=true; break;
		case "4" : newChar="9"; isFound=true; break;
		
		}
		
		if(!isFound) {// if not found in numbers this condition will process
		
			switch(ch) {
			
			case "q" : newChar="a"; isFound=true; break;
			case "w" : newChar="b"; isFound=true; break;
			case "e" : newChar="c"; isFound=true; break;
			case "r" : newChar="d"; isFound=true; break;
			case "t" : newChar="e"; isFound=true; break;
			case "y" : newChar="f"; isFound=true; break;
			case "u" : newChar="g"; isFound=true; break;
			case "i" : newChar="h"; isFound=true; break;
			case "o" : newChar="i"; isFound=true; break;
			case "p" : newChar="j"; isFound=true; break;
			case "a" : newChar="k"; isFound=true; break;
			case "s" : newChar="l"; isFound=true; break;
			case "d" : newChar="m"; isFound=true; break;
			case "f" : newChar="n"; isFound=true; break;
			case "g" : newChar="o"; isFound=true; break;
			case "h" : newChar="p"; isFound=true; break;
			case "j" : newChar="q"; isFound=true; break;
			case "k" : newChar="r"; isFound=true; break;
			case "l" : newChar="s"; isFound=true; break;
			case "z" : newChar="t"; isFound=true; break;
			case "x" : newChar="u"; isFound=true; break;
			case "c" : newChar="v"; isFound=true; break;
			case "v" : newChar="w"; isFound=true; break;
			case "b" : newChar="x"; isFound=true; break;
			case "n" : newChar="y"; isFound=true; break;
			case "m" : newChar="z"; isFound=true; break;
			
			case "Q" : newChar="A"; isFound=true; break;
			case "W" : newChar="B"; isFound=true; break;
			case "E" : newChar="C"; isFound=true; break;
			case "R" : newChar="D"; isFound=true; break;
			case "T" : newChar="E"; isFound=true; break;
			case "Y" : newChar="F"; isFound=true; break;
			case "U" : newChar="G"; isFound=true; break;
			case "I" : newChar="H"; isFound=true; break;
			case "O" : newChar="I"; isFound=true; break;
			case "P" : newChar="J"; isFound=true; break;
			case "A" : newChar="K"; isFound=true; break;
			case "S" : newChar="L"; isFound=true; break;
			case "D" : newChar="M"; isFound=true; break;
			case "F" : newChar="N"; isFound=true; break;
			case "G" : newChar="O"; isFound=true; break;
			case "H" : newChar="P"; isFound=true; break;
			case "J" : newChar="Q"; isFound=true; break;
			case "K" : newChar="R"; isFound=true; break;
			case "L" : newChar="S"; isFound=true; break;
			case "Z" : newChar="T"; isFound=true; break;
			case "X" : newChar="U"; isFound=true; break;
			case "C" : newChar="V"; isFound=true; break;
			case "V" : newChar="W"; isFound=true; break;
			case "B" : newChar="X"; isFound=true; break;
			case "N" : newChar="Y"; isFound=true; break;
			case "M" : newChar="Z"; isFound=true; break;
			
			case "Ñ" : newChar="ñ"; isFound=true; break;
			case "ñ" : newChar="Ñ"; isFound=true; break;
			
			
			}
		
		}
		
		
		return newChar;
	}
	
}
