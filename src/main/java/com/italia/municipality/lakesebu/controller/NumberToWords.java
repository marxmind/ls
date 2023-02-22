package com.italia.municipality.lakesebu.controller;

import java.math.BigDecimal;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NumberToWords {

		private static final String[] specialNames={
			" ",
			" thousand",
			" million",
			" billion",
			" trillion",
			" quadrillion",
			" quintillion"
		};
		
		private static final String[] tensNames={
			" ",
			" ten",
			" twenty",
			" thirty",
			" forty",
			" fifty",
			" sixty",
			" seventy",
			" eighty",
			" ninety"
		};
		
		private static final String[] numNames={
			" ",
			" one",
			" two",
			" three",
			" four",
			" five",
			" six",
			" seven",
			" eight",
			" nine",
			" ten",
			" eleven",
			" twelve",
			" thirteen",
			" fourteen",
			" fifteen",
			" sixteen",
			" seventeen",
			" eighteen",
			" nineteen"
		};
		
		private String convertLessTnanOneThousand(int number){
			String current;
			
			if(number % 100<20){
				current = numNames[number % 100];
				number /=100;
			}else{
				current = numNames[number % 10];
				number /= 10;
				
				current = tensNames[number % 10] + current;
				number /= 10;
			}
			
			if(number == 0) return current;
			return numNames[number] + " hundred" + current;
		}
		
		private String convert(int number){
			if(number == 0){return "zero";}
			
			String prefix = "";
			if(number < 0){
				number = -number;
				prefix = "negative";
			}
			
			String current = "";
			int place = 0;
			
			
			do{
				int n = number % 1000;
				if(n != 0){
					String s = convertLessTnanOneThousand(n);
					current = s + specialNames[place] + current;
				}
				place++;
				number /=1000;
			}while(number>0);
			return (prefix + current);
		}
		public String changeToWords(double val){
			return changeToWords(val+"");
		}
		
		public String changeToWords(BigDecimal val){
			return changeToWords(val+"");
		}
		
		public String changeToWords(String val){
			String results = "";
			
			System.out.println("changeToWOrds: " + val);
			try{
			
			String a = "";
			try{
			a = val.split("\\.")[1];
			
			System.out.println("aaaa : " + a);
			
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Index out of bound. Please check pass data.");
			}
			String first = "";
			int second = 0;
			if(a.isEmpty()){
				System.out.println("ok empty");
				first = convert(Integer.valueOf(val));
				System.out.println(first);
				results = first;
			}else{
				System.out.println("ok not empty");
				first = convert(Integer.valueOf(val.split("\\.")[0]));
				second = Integer.valueOf(val.split("\\.")[1]);
				if(second>0){
					
					int size = val.split("\\.")[1].length();
					if(size==2){
						String f_num = val.split("\\.")[1].substring(0, 1);
						String s_num = val.split("\\.")[1].substring(1, 2);
						if("0".equalsIgnoreCase(f_num)){
							results = first + " & " + f_num + s_num + "/100";
						}else{
							results = first + " & " + second + "/100";
						}
					}else{
						results = first + " & " + second + "/100";
					}
					
				}else{
					results = first;
				}
				System.out.println(results);
			}
			}catch(Exception e){
				e.getMessage();
			}
			return results + " pesos only.";
		}	
		
}
