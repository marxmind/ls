package com.italia.municipality.lakesebu.enm;

public enum CashTicketMovementType {
	
	BEGINNING(0, "BEGINNING"),
	ENDING(1, "ENDING");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private CashTicketMovementType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		
		for(CashTicketMovementType type : CashTicketMovementType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return CashTicketMovementType.BEGINNING.getName();
	}
	
	public static int idName(String name){
		
		for(CashTicketMovementType type : CashTicketMovementType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return CashTicketMovementType.BEGINNING.getId();
	}
	
}
