package com.italia.municipality.lakesebu.enm;

public enum StockStatus {

	NOT_HANDED(1, "NOT_HANDED"),
	ALL_ISSUED(2, "ALL ISSUED"),
	PARTIAL_ISSUED(3, "PARTIAL ISSUED"),
	LOSS(4, "LOSS");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private StockStatus(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		
		for(StockStatus type : StockStatus.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return StockStatus.NOT_HANDED.getName();
	}
	
	public static int idName(String name){
		
		for(StockStatus type : StockStatus.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return StockStatus.NOT_HANDED.getId();
	}
	
}