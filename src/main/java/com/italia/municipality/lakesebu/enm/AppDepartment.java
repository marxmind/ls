package com.italia.municipality.lakesebu.enm;

public enum AppDepartment {
	
	MAYOR(1,"MAYOR"),
	VICE_MAYOR(2, "VICE_MAYOR"),
	SB(3,"SB"),
	SK(4,"SK"),
	TREASURER(5,"TREASURER"),
	BUDGET(6,"BUDGET"),
	ACCOUNTING(7,"ACCOUNTING"),
	ADMIN(8,"ADMIN"),
	PERSONNEL(9,"PERSONNEL"),
	MSWD(10,"MSWD"),
	MAO(11,"MAO"),
	ASSESSOR(12,"ASSESSOR"),
	CIVIL_REGISTRAR(13,"CIVIL_REGISTRAR"),
	LICENSING(14,"LICENSING"),
	MPDC(15,"MPDC"),
	ENGINEERING(16,"ENGINEERING"),
	GSO(17,"GSO"),
	MDREAMS(18,"MDREAMS"),
	TOURISM(19,"TOURISM"),
	TOURISM_LODGE(20,"TOURISM_LODGE"),
	MOTORPOOL(21,"MOTORPOOL"),
	HOSPITAL(22,"HOSPITAL"),
	HEALTH(23,"HEALTH"),
	MENRO(24,"MENRO"),
	SENIOR(25,"SENIOR"),
	IPMR(26,"IPMR"),
	IPS(27,"IPS"),
	SECUIRTY(28,"SECUIRTY"),
	DILG(29,"DILG");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	
	private AppDepartment(int id,String name){
		this.id=id;
		this.name=name;
	}
	
	public static String getMonthName(int id){
		
		for(AppDepartment m : AppDepartment.values()){
			if(id==m.getId()){
				return m.getName();
			}
		}
		
		return AppDepartment.MAYOR.name;
	}
	
	public static int getMonthName(String name){
		
		for(Months m : Months.values()){
			if(name.equalsIgnoreCase(m.getName())){
				return m.getId();
			}
		}
		
		return AppDepartment.MAYOR.getId();
	}
	
	
}
