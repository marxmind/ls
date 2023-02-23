package com.italia.municipality.lakesebu.enm;

public enum PenalyMonth {

	JANUARY("jana"),
	FEBRUARY("feba"),
	MARCH("mara"),
	APRIL("apra"),
	MAY("maya"),
	JUNE("juna"),
	JULY("jula"),
	AUGUST("auga"),
	SEPTEMBER("sepa"),
	OCTOBER("octa"),
	NOVEMBER("nova"),
	DECEMBER("deca");
	
	private String name;
	
	public String getName(){
		return name;
	}
	
	private PenalyMonth(String name){
		this.name = name;
	}
	
	public static PenalyMonth month(int month){
		switch(month){
		case 1 : return PenalyMonth.JANUARY;
		case 2 : return PenalyMonth.FEBRUARY;
		case 3 : return PenalyMonth.MARCH;
		case 4 : return PenalyMonth.APRIL;
		case 5 : return PenalyMonth.MAY;
		case 6 : return PenalyMonth.JUNE;
		case 7 : return PenalyMonth.JULY;
		case 8 : return PenalyMonth.AUGUST;
		case 9 : return PenalyMonth.SEPTEMBER;
		case 10 : return PenalyMonth.OCTOBER;
		case 11 : return PenalyMonth.NOVEMBER;
		case 12 : return PenalyMonth.DECEMBER;
		}
		return PenalyMonth.JANUARY;
	}
}