package com.italia.municipality.lakesebu.enm;

public enum GraphColor {
	
	COLOR1(1,"rgb(75, 192, 192)"),
	COLOR2(2,"rgb(255, 99, 132)"),
	COLOR3(3,"rgb(54, 162, 235)"),
	COLOR4(4,"rgb(255, 205, 86)"),
	COLOR5(5,"rgb(255, 99, 132)"),
	COLOR6(6,"rgb(255, 159, 64)"),
	COLOR7(7,"rgb(255, 205, 86)"),
	COLOR8(8,"rgb(75, 192, 192)"),
	COLOR9(9,"rgb(54, 162, 235)"),
	COLOR10(10,"rgb(153, 102, 255)"),
	COLOR11(11,"rgb(201, 203, 207)"),
	COLOR12(12,"rgb(255, 99, 132)"),
	COLOR13(13,"rgb(255, 159, 64)"),
	COLOR14(14,"rgb(255, 205, 86)"),
	COLOR15(15,"rgb(75, 192, 192)"),
	COLOR16(16,"rgb(54, 162, 235)"),
	COLOR17(17,"rgb(153, 102, 255)"),
	COLOR18(18,"rgb(201, 203, 207)"),
	COLOR19(19,"rgb(80, 192, 192)"),
	COLOR20(20,"rgb(153, 99, 132)"),
	COLOR21(21,"rgb(75, 162, 235)"),
	COLOR22(22,"rgb(102, 205, 86)"),
	COLOR23(23,"rgb(100, 162, 235)"),
	COLOR24(24,"rgb(200, 102, 255)"),
	COLOR25(25,"rgb(30, 203, 207)"),
	;
	
	private int id;
	private String value;
	
	public int getId(){
		return id;
	}
	
	public String getValue(){
		return value;
	}
	
	private GraphColor(int id, String value){
		this.id = id;
		this.value = value;
	}
	
	public static String color(int id){
		
		for(GraphColor type : GraphColor.values()){
			if(id==type.getId()){
				return type.getValue();
			}
		}
		
		return GraphColor.COLOR1.getValue();
	}
	
	
	public static String valueId(int id){
		
		for(GraphColor type : GraphColor.values()){
			if(id==type.getId()){
				return type.getValue();
			}
		}
		
		return GraphColor.COLOR1.getValue();
	}
	
	public static int idName(String value){
		
		for(GraphColor type : GraphColor.values()){
			if(value.equalsIgnoreCase(type.getValue())){
				return type.getId();
			}
		}
		
		return GraphColor.COLOR1.getId();
	}
	
}
