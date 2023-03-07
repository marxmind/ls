package com.italia.municipality.lakesebu.enm;

public enum CalendarType {
	
	REMARKS(1, "Remarks"),
	HOLIDAY(2,"Holiday"),
	SPECIAL_HOLIDAY(3, "Special Holiday"),
	SPECIAL_NON_WORKING_DAY(4, "Special Non Working Day");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private CalendarType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(CalendarType type : CalendarType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return CalendarType.REMARKS.getName();
	}
	public static int typeId(String name){
		for(CalendarType type : CalendarType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return CalendarType.REMARKS.getId();
	}
}
