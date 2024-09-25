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
	SPECIAL_EDUCATION_FUND(4,"SPECIAL EDUCATION FUND"),
	PROFESSIONAL_FEE(5, "PROFESSIONAL FEE"),
	PHILHEALTH(6, "PHILHEALTH"),
	UNIVERSAL_HEALTH_CARE(7, "UNIVERSAL HEALTH CARE"),
	MAIP_TRUST_FUND(8, "MAIP TRUST FUND"),
	CALAMITY_FUND(9, "CALAMITY FUND"),
	TWENTY_PERCENT_DEVELOPMENT_FUND(10, "20% DEVELOPMENT FUND");
	
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