package com.italia.municipality.lakesebu.enm;
/**
 * 
 * @author mark italia
 * @since 04/30/2018
 * @version 1.0
 *
 */
public enum Job {
	
	DEVELOPER(1,"Developer"),
	KAPITAN(2, "Kapitan"),
	KAGAWAD(3, "Kagawad"),
	TREASURER(4, "Treasurer"),
	SECRETARY(5, "Secretary"),
	ADMIN(6, "Administrator"),
	RECORD_KEEPER(7, "Record Keeper"),
	CLERK(8, "Clerk"),
	IPMR(9, "IPMR"),
	CLERK2(10, "Clerk 2"),
	DRIVER(11, "Driver"),
	TANOD(12, "Tanod"),
	DTEACHER(13, "Daycare Teacher"),
	LUPON(14, "Barangay Lupon"),
	LUPON_CHAIRMAN(15, "Lupon Chairman"),
	IPMR_CLERK(16, "IPMR Clerk");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private Job(int id, String name){
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
