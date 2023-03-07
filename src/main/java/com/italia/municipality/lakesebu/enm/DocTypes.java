package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 08/17/2017
 * @version 1.0
 *
 */
public enum DocTypes {

	CERTIFICATE(1, "CERTIFICATE"),
	CLEARANCE(2, "CLEARANCE"),
	LATE_BIRTH_REG(3, "LATE BIRTH CERTIFICATE"),
	INDIGENT(4, "INDIGENT CERTIFICATION"),
	DEATH_CERT(5, "DEATH CERTIFICATE"),
	LATE_DEATH_CERT(6, "LATE DEATH CERTIFICATE"),
	BARANGAY_BUSINESS_PERMIT(7, "BUSINESS PERMIT"),
	COE(8, "COE CERTIFICATION"),
	AUTHORIZATION_LETTER(9,"AUTHORIZATION LETTER"),
	LIVE_BIRTH_REG(10, "LIVE BIRTH CERTIFICATE"),
	RESIDENCY(11, "RESIDENCY CERTIFICATE"),
	INCOME(12, "INCOME CERTIFICATE"),
	CERTIFICATE_OPEN_TITLE(13, "CERTIFICATE OPEN TITLE"),
	CLEARANCE_OPEN_TITLE(14, "CLEARANCE OPEN TITLE"),
	LOW_INCOME(15, "LOW INCOME CERTIFICATION");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private DocTypes(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(DocTypes type : DocTypes.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return DocTypes.CERTIFICATE.getName();
	}
	public static int typeId(String name){
		for(DocTypes type : DocTypes.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return DocTypes.CERTIFICATE.getId();
	}
	
}
