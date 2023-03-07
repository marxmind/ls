package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 07/12/2017
 * @version 1.0
 *
 */

public enum Positions {

	CAPTAIN(1, "Punong Barangay"),
	KAGAWAD(2,"Kagawad"),
	TREASURER(3, "Treasurer"),
	SECRETARY(4, "Secretary"),
	ADMIN(5, "Administrator"),
	RECORD_KEEPER(6, "Record Keeper"),
	CLERK(7, "Clerk"),
	IPMR(8,"Tribal Chieftain"),
	CLERK_2(9, "Clerk 2"),
	DRIVER(10, "Driver"),
	TANOD(11, "Tanod"),
	DAYCARE_TEACHER(12, "Daycare Teacher"),
	BARANGAY_LUPON(13, "Barangay Lupon"),
	BARANGAY_LUPON_CHAIRMAN(14, "Barangay Lupon Chairman"),
	BARANGAY_LUPON_SECRETARY(15, "Barangay Lupon Secretary"),
	BARANGAY_LUPON_MEMBER(16, "Barangay Lupon Member"),
	BARANGAY_PANGKAT_SECRETARY(17, "Barangay Pangkat Secretary"),
	SK_CHAIRMAN(18, "SK CHAIRMAN"),
	SK_CHAIRWOMAN(19, "SK CHAIRWOMAN"),
	SK_MEMBER(20, "SK MEMBER");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private Positions(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(Positions type : Positions.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return Positions.CAPTAIN.getName();
	}
	public static int typeId(String name){
		for(Positions type : Positions.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return Positions.CAPTAIN.getId();
	}
	
}
