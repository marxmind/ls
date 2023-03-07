package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 07/09/2017
 * @version 1.0
 *
 */

public enum Relationships {
	NA(0, "N/A"),
	FATHER(1, "Father"),
	MOTHER(2, "Mother"),
	BROTHER(3, "Brother"),
	SISTER(4, "Sister"),
	BROTHER_IN_LAW(5, "Brother-In-Law"),
	SISTER_IN_LAW(6, "Sister-In-Law"),
	SPOUSE(7, "Spouse"),
	NICE(8, "Niece"),
	NEPHEW(9, "Nephew"),
	RELATIVES(10, "Relatives"),
	GIRLFRIEND(11, "Girlfriend"),
	BOYFRIEND(12, "Boyfriend"),
	GRAND_FATHER(13, "Grand Father"),
	GRAND_MOTHER(14, "Grand Mother"),
	MOTHER_IN_LAW(15, "Mother-In-Law"),
	FATHER_IN_LAW(16, "Father-In-Law"),
	FRIEND(17, "Friend"),
	STEP_FATHER(18, "Step Father"),
	STEP_MOTHER(19, "Step Mother"),
	STEP_BROTHER(20, "Step Brother"),
	STEP_SISTER(21, "Step Sister"),
	LIVE_IN_PARTNER(22, "Live-In-Partner"),
	UNCLE(23, "Uncle"),
	AUNTIE(24, "Auntie"),
	SON(25, "Son"),
	DAUGHTER(26, "Daughter"),
	SON_LAW(27, "Son-In-Law"),
	DAUGHTER_LAW(28, "Daughter-In-Law"),
	HUSBAND(29,"Husband"),
	WIFE(30, "Wife"),
	GRAND_SON(31, "Grand Son"),
	GRAND_DAUGHTER(32, "Grand Daughter"),
	COUSIN(33,"Cousin");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private Relationships(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(Relationships type : Relationships.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return Relationships.MOTHER.getName();
	}
	public static int typeId(String name){
		for(Relationships type : Relationships.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return Relationships.MOTHER.getId();
	}
	
}