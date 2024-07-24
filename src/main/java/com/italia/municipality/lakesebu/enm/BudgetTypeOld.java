package com.italia.municipality.lakesebu.enm;

public enum BudgetTypeOld {
	DAILY(1,"DAILY"),
	WEEKLY(2,"WEEKLY"),
	MONTHLY(3,"MONTHLY"),
	YEARLY(4,"YEARLY");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private BudgetTypeOld(int id,String name){
		this.id = id;
		this.name = name;
	}
	public static String typeName(int id){
		for(BudgetTypeOld type : BudgetTypeOld.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return BudgetTypeOld.DAILY.getName();
	}
	public static int typeId(String name){
		for(BudgetTypeOld type : BudgetTypeOld.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return BudgetTypeOld.DAILY.getId();
	}
}
