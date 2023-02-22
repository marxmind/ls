package com.italia.municipality.lakesebu.enm;
/**
 * 
 * @author mark italia
 * @since 04/30/2018
 * @version 1.0
 */

public enum EmailType {

	INBOX(1, "INBOX"),
	OUTBOX(2,"OUTBOX"),
	SEND(3,"SEND"),
	DRAFT(4,"DRAFT"),
	DELETED(5,"DELETED");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private EmailType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(EmailType type : EmailType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return EmailType.INBOX.getName();
	}
	public static int typeId(String name){
		for(EmailType type : EmailType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return EmailType.INBOX.getId();
	}
	
}