package com.italia.municipality.lakesebu.enm;

public enum BudgetType {

	NTA(0,"NTA"),
	LOANS(1,"LOANS"),
	YEAR_BEGINNING_BALANCE(2, "YEAR BEGINNING BALANCE"),
	TRANSFER_TO_OTHER_FUND(3, "TRANSFER TO OTHER FUND");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private BudgetType(int id,String name){
		this.id = id;
		this.name = name;
	}
	public static String typeName(int id){
		for(BudgetType type : BudgetType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return BudgetType.NTA.getName();
	}
	public static int typeId(String name){
		for(BudgetType type : BudgetType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return BudgetType.NTA.getId();
	}
	
}