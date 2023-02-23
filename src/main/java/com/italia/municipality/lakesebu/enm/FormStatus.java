package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author Mark Italia
 * @since 08-12-2018
 * @version 1.0
 *
 */

public enum FormStatus {

	HANDED(1, "HANDED"),
	ALL_ISSUED(2, "ALL ISSUED"),
	NOT_ALL_ISSUED(3, "NOT ALL ISSUED"),
	ENCODED(4, "ENCODED"),
	CANCELLED(5, "CANCELLED"),
	RTS(6,"RTS");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private FormStatus(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		
		for(FormStatus type : FormStatus.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return FormStatus.HANDED.getName();
	}
	
	public static int idName(String name){
		
		for(FormStatus type : FormStatus.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return FormStatus.HANDED.getId();
	}
	
}
