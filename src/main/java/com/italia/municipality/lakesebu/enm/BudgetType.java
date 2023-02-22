package com.italia.municipality.lakesebu.enm;

public enum BudgetType {

	DAILY(1,"DAILY"),
	WEEKLY(2,"WEEKLY"),
	MONTHLY(3,"MONTHLY"),
	YEARLY(4,"YEARLY");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private BudgetType(int id,String name){
		this.id = id;
		this.name = name;
	}
	
}