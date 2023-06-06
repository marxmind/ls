package com.italia.municipality.lakesebu.enm;

public enum BusinessType {
	//PLEASE DO NOT ADD 3 THIS IS RESERVE FOR all USE IN bUSINESS
	//BusinessIndexBean loadBusinessTrans() for reference
	NEW(0, "NEW"),
	RENEW(1, "RENEW"),
	ADDED(2, "ADDED"),//3 is reserve
	OTHERS(4, "OTHERS");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private BusinessType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public static BusinessType val(int id) {
		for(BusinessType g : BusinessType.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return BusinessType.NEW;
	}
	public static BusinessType val(String name) {
		for(BusinessType g : BusinessType.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return BusinessType.NEW;
	}
	
}
