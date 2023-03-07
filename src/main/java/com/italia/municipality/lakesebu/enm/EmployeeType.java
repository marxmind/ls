package com.italia.municipality.lakesebu.enm;

public enum EmployeeType {

	REGULAR(1, "REGULAR", "ME"),
	CO_TERMINOUS(2, "CO-TERMINOUS", "CT"),
	CASUAL(3, "CASUAL", "CS"),
	CONTRACTUAL(4, "CONTRACTUAL", "CO"),
	JO(5, "JOB ORDER", "JO"),
	CRO(6, "CRO", "RO");
	
	
	private int id;
	private String name;
	private String code;
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	private EmployeeType(int id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}
	
	public static String nameValue(int id) {
		for(EmployeeType t : EmployeeType.values()) {
			if(t.getId()==id) {
				return t.getName();
			}
		}
		return EmployeeType.REGULAR.getName();
	}
	public static int idValue(String name) {
		for(EmployeeType t : EmployeeType.values()) {
			if(t.getName().equalsIgnoreCase(name)) {
				return t.getId();
			}
		}
		return EmployeeType.REGULAR.getId();
	}
	public static EmployeeType value(int id) {
		for(EmployeeType t : EmployeeType.values()) {
			if(t.getId()==id) {
				return t;
			}
		}
		return EmployeeType.REGULAR;
	}
	public static EmployeeType value(String name) {
		for(EmployeeType t : EmployeeType.values()) {
			if(t.getName().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return EmployeeType.REGULAR;
	}
	
}