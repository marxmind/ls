package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 02/22/2017
 * @version 1.0
 *
 */
public enum Months {
	DEC0(0, "DECEMBER LAST YEAR"),
	JAN(1, "JANUARY"),
	FEB(2, "FEBRUARY"),
	MAR(3, "MARCH"),
	APR(4, "APRIL"),
	MAY(5, "MAY"),
	JUN(6, "JUNE"),
	JUL(7, "JULY"),
	AUG(8, "AUGUST"),
	SEP(9, "SEPTEMBER"),
	OCT(10, "OCTOBER"),
	NOV(11, "NOVEMBER"),
	DEC(12, "DECEMBER"),
	JAN0(13, "JANUARY");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	
	private Months(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String getMonthName(int id){
		
		for(Months m : Months.values()){
			if(id==m.getId()){
				return m.getName();
			}
		}
		
		return Months.JAN.getName();
	}
	
  public static Months getMonth(int id){
		
		for(Months m : Months.values()){
			if(id==m.getId()){
				return m;
			}
		}
		
		return Months.JAN;
	}
	
	public static int getMonthName(String name){
		
		for(Months m : Months.values()){
			if(name.equalsIgnoreCase(m.getName())){
				return m.getId();
			}
		}
		
		return Months.JAN.getId();
	}
	
}