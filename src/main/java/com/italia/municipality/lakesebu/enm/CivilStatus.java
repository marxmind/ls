package com.italia.municipality.lakesebu.enm;
public enum CivilStatus {

	SINGLE(1, "Single"),
	MARRIED(2,"Married"),
	DIVORCED(3, "Divorced"),
	WIDOWED(4, "Widowed"),
	LIVEIN(5, "Live In"),
	SEPERATED(6,"Seperated"),
	COMMON_LAW(7, "Common Law"),
	UNKNOWN(8, "Unknown");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private CivilStatus(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(CivilStatus type : CivilStatus.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return CivilStatus.SINGLE.getName();
	}
	public static int typeId(String name){
		for(CivilStatus type : CivilStatus.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return CivilStatus.SINGLE.getId();
	}
	
}
