package com.italia.municipality.lakesebu.enm;

public enum FormORTypes {
	
	NEW(0, "NEW","All Types"),
	NEW_CEDULE(1, "NEW CEDULA","NEW CEDULA RELEASE"),
	NEW_OR(2, "NEW OR","NEW OFFICIAL RECEIPT"),
	NEW_BUSINESS_OR(3, "NEW BUSINESS OR","NEW BUSINESS OFFICIAL RECEIPT"),
	NEW_FISHCAGE_OR(4, "NEW FISHCAGE OR","NEW FISHCAGE OFFICIAL RECEIPT"),
	NEW_SKYLAB_PERMIT(5, "NEW SKYLAB PERMIT","NEW SKYLAB PERMIT OFFICIAL RECEIPT");
	
	private int id;
	private String name;
	private String description;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	private FormORTypes(int id, String name,String description){
		this.id = id;
		this.name = name;
		this.description = description;	
	}
	
	public static String nameId(int id){
		
		for(FormORTypes type : FormORTypes.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return FormORTypes.NEW.getName();
	}
	
	public static int idName(String name){
		
		for(FormORTypes type : FormORTypes.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return FormORTypes.NEW.getId();
	}
	
	public static FormORTypes val(String name) {
		
		for(FormORTypes type : FormORTypes.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type;
			}
		}
		
		return null;
	}
	
	public static FormORTypes val(int id) {
		
		for(FormORTypes type : FormORTypes.values()){
			if(id==type.getId()){
				return type;
			}
		}
		
		return null;
	}
	
}
