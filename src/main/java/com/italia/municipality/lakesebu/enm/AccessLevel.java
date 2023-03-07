package com.italia.municipality.lakesebu.enm;

public enum AccessLevel {

	DEVELOPER(1, "DEVELOPER"),
	ADMIN(2, "ADMINISTRATIVE"),
	CASHIER_THREE(3, "CASHIER THREE"),
	CASHIER_ONE(4, "CASHIER ONE"),
	LIQUIDATION(5, "LIQUIDATION"),
	REVENUE_COLLECTION_OFFICECER_ONE(6, "REVENUE COLLECTION OFFICER ONE"),
	REVENUE_COLLECTION_CLERK(7, "REVENUE COLLECTION CLERK"),
	CLERK(8, "CLERK"),
	RECEIVING(9, "CLERK"),
	DEPARTMENT_DA(10, "DEPARTMENT OF DA"),
	DEPARTMENT_LICENSING(11, "DEPARTMENT OF LICENSING"),
	GSO(12, "GENERAL SERVICES"),
	ACCOUNTING_DEPARTMENT(13, "ACCOUNTING DEPARTMENT"),
	MTO_COLLECTOR(14, "MTO COLLECTOR"),
	MTO_LANDTAX_CLERK(15,"MTO LAND TAX CLERK");
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private int id;
	private String name;
	
	private AccessLevel(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		for(AccessLevel lvl : AccessLevel.values()){
			if(id==lvl.getId()){
				return lvl.getName();
			}
		}
		return AccessLevel.CLERK.getName();
	}
	
	public static int idName(String name){
		for(AccessLevel lvl : AccessLevel.values()){
			if(name.equalsIgnoreCase(lvl.getName())){
				return lvl.getId();
			}
		}
		return AccessLevel.CLERK.getId();
	}
	
}