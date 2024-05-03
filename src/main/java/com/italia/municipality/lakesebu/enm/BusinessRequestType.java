package com.italia.municipality.lakesebu.enm;

public enum BusinessRequestType {
	
	RETIREMENT(1, "Retirement"),
	CHANGE_BUSINES_NAME(2, "Change Business Name"),
	CHANGE_OWNER(3, "Change Owner"),
	CHANGE_ADDRESS(4, "Change Address"),
	CHANGE_BUSINESS_NAME_OWNER_ADDRESS(5, "Change Business Name, Owner and Address"),
	CHANGE_BUSINESS_NAME_OWNER(6, "Change Business Name and Owner"),
	CHANGE_BUSINESS_NAME_ADDRESS(7, "Change Business Name and Address"),
	CHANGE_OWNER_ADDRESS(8, "Change Owner and Address");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private BusinessRequestType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public static BusinessRequestType val(int id) {
		for(BusinessRequestType g : BusinessRequestType.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return BusinessRequestType.RETIREMENT;
	}
	public static BusinessRequestType val(String name) {
		for(BusinessRequestType g : BusinessRequestType.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return BusinessRequestType.RETIREMENT;
	}
	
}
