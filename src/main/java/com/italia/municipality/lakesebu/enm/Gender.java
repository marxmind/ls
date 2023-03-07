package com.italia.municipality.lakesebu.enm;

public enum Gender {

	MALE(1, "MALE"),
	FEMALE(2, "FEMALE"),
	NON_BINARY(3, "NON-BINARY"),
	TRANSGENDER(4, "TRANSGENDER"),
	INTERSEX(5, "INTERSEX"),
	I_PREFER_NOT_TO_SAY(6, "I PREFER NOT TO SAY");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private Gender(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public Gender val(int id) {
		for(Gender g : Gender.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return Gender.MALE;
	}
	public Gender val(String name) {
		for(Gender g : Gender.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return Gender.MALE;
	}
}
