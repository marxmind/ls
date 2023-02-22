package com.italia.municipality.lakesebu.enm;

public enum GraphColorWithBorder {
	COLOR1(1,"rgba(75, 192, 192, 0.2)"),
	COLOR2(2,"rgba(255, 99, 132, 0.2)"),
	COLOR3(3,"rgba(54, 162, 235, 0.2)"),
	COLOR4(4,"rgba(255, 205, 86, 0.2)"),
	COLOR5(5,"rgba(255, 99, 132, 0.2)"),
	COLOR6(6,"rgba(255, 159, 64, 0.2)"),
	COLOR7(7,"rgba(255, 205, 86, 0.2)"),
	COLOR8(8,"rgba(75, 192, 192, 0.2)"),
	COLOR9(9,"rgba(54, 162, 235, 0.2)"),
	COLOR10(10,"rgba(153, 102, 255, 0.2)"),
	COLOR11(11,"rgba(201, 203, 207, 0.2)"),
	COLOR12(12,"rgba(255, 99, 132, 0.2)"),
	COLOR13(13,"rgba(255, 159, 64, 0.2)"),
	COLOR14(14,"rgba(255, 205, 86, 0.2)"),
	COLOR15(15,"rgba(75, 192, 192, 0.2)"),
	COLOR16(16,"rgba(54, 162, 235, 0.2)"),
	COLOR17(17,"rgba(153, 102, 255, 0.2)"),
	COLOR18(18,"rgba(201, 203, 207, 0.2)"),
	COLOR19(19,"rgba(80, 192, 192, 0.2)"),
	COLOR20(20,"rgba(153, 99, 132, 0.2)"),
	COLOR21(21,"rgba(75, 162, 235, 0.2)"),
	COLOR22(22,"rgba(102, 205, 86, 0.2)"),
	COLOR23(23,"rgba(100, 162, 235, 0.2)"),
	COLOR24(24,"rgba(200, 102, 255, 0.2)"),
	COLOR25(25,"rgba(30, 203, 207, 0.2)"),
	;
	
	private int id;
	private String value;
	
	public int getId(){
		return id;
	}
	
	public String getValue(){
		return value;
	}
	
	private GraphColorWithBorder(int id, String value){
		this.id = id;
		this.value = value;
	}
	
	public static String color(int id){
		
		for(GraphColorWithBorder type : GraphColorWithBorder.values()){
			if(id==type.getId()){
				return type.getValue();
			}
		}
		
		return GraphColorWithBorder.COLOR1.getValue();
	}
	
	
	public static String valueId(int id){
		
		for(GraphColorWithBorder type : GraphColorWithBorder.values()){
			if(id==type.getId()){
				return type.getValue();
			}
		}
		
		return GraphColorWithBorder.COLOR1.getValue();
	}
	
	public static int idName(String value){
		
		for(GraphColorWithBorder type : GraphColorWithBorder.values()){
			if(value.equalsIgnoreCase(type.getValue())){
				return type.getId();
			}
		}
		
		return GraphColorWithBorder.COLOR1.getId();
	}
}
