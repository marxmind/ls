package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 09/27/2022
 *
 */
public enum ClientStatus {
	
	QUEUE(0, "QUEUE"),
	SERVING(1, "SERVING"),
	COMPLETED(2, "COMPLETED"),
	CANCELLED(3, "CANCELLED"),
	DELETED(4,"DELETED");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private ClientStatus(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		
		for(ClientStatus type : ClientStatus.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return ClientStatus.QUEUE.getName();
	}
	
	public static int idName(String name){
		
		for(ClientStatus type : ClientStatus.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return ClientStatus.QUEUE.getId();
	}
	
}