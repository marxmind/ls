package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 06/28/2019
 * @version 1.0
 *
 */
public enum FundType {
	GENERAL_FUND(1, "GENERAL FUND"),
	MOTORPOOL(2,"MOTORPOOL"),
	TRUST_FUND(3,"TRUST FUND"),
	SPECIAL_EDUCATION_FUND(4,"SPECIAL EDUCATION FUND");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private FundType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(FundType type : FundType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return FundType.GENERAL_FUND.getName();
	}
	public static int typeId(String name){
		for(FundType type : FundType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return FundType.GENERAL_FUND.getId();
	}
}