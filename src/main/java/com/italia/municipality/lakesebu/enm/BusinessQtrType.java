package com.italia.municipality.lakesebu.enm;

public enum BusinessQtrType {
	
	FIRST_QTR(1,"First Quarter"),
	SECOND_QTR(2,"Second Quarter"),
	THIRD_QTR(3,"Third Quarter"),
	FOURTH_QTR(4,"Fourth Quarter"),
	FIRST_SEMI_ANNUAL(5,"1st Semi Annual"),
	ANNUAL(6,"Annual"),
	SECOND_SEMI_ANNUAL(7, "2nd Semi Annual"),
	FIRST_TO_THIRD(8, "First to Third Quarter"),
	SECOND_TO_THIRD(9, "Second to Third Quarter"),
	SECOND_TO_FOURTH(10, "Second to Fourth Quarter");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private BusinessQtrType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(BusinessQtrType type : BusinessQtrType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return BusinessQtrType.FIRST_QTR.getName();
	}
	public static int typeId(String name){
		for(BusinessQtrType type : BusinessQtrType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return BusinessQtrType.FIRST_QTR.getId();
	}
	
}
