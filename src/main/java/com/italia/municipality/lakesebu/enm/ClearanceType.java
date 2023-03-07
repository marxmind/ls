package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 07/7/2017
 * @version 1.0
 *
 */

public enum ClearanceType {

	OTHERS(1, "OTHERS"),
	NEW(2,"NEW APPLICANT"),
	RENEWAL(3,"RENEWAL");
	
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private ClearanceType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(ClearanceType type : ClearanceType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return ClearanceType.OTHERS.getName();
	}
	public static int typeId(String name){
		for(ClearanceType type : ClearanceType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return ClearanceType.OTHERS.getId();
	}

	
}