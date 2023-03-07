package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @Descrition: use for document style
 * @see DocumentFormatter.java
 *
 */
public enum DocStyle {
	V5(1, "V5"),
	V6(2, "V6");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private DocStyle(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(DocStyle type : DocStyle.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return DocStyle.V5.getName();
	}
	public static int typeId(String name){
		for(DocStyle type : DocStyle.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return DocStyle.V5.getId();
	}
}
