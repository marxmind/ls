package com.italia.municipality.lakesebu.enm;

/**
 * @since 05/28/2025
 * @version 1.0
 * @author Mark Italia
 * 
 */
public enum ClassType {
	BUILDING_1(1, "Building 1"),
	BUILDING_2(2, "Building 2"),
	BUILDING_3(3, "Building 3"),
	BUILDING_4(4, "Building 4"),
	BUILDING_5(5, "Building 5"),
	BUILDING_6(6, "Building 6"),
	BUILDING_7(7, "Building 7"),
	BUILDING_8(8, "Building 8"),
	BUILDING_9(9, "Building 9"),
	BUILDING_10(10, "Building 10"),
	AREA_1(11, "Area 1"),
	AREA_2(12, "Area 2"),
	AREA_3(13, "Area 3"),
	AREA_4(14, "Area 4"),
	AREA_5(15, "Area 5"),
	AREA_6(16, "Area 6"),
	AREA_7(17, "Area 7"),
	AREA_8(18, "Area 8"),
	AREA_9(19, "Area 9"),
	AREA_10(20, "Area 10"),
	TERMINAL(21, "Terminal"),
	WETMARKET(22, "Wet Market"),
	DRIEDMARKET(23, "Dried Good Market");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private ClassType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public static ClassType val(int id) {
		for(ClassType g : ClassType.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return ClassType.AREA_1;
	}
	public static ClassType val(String name) {
		for(ClassType g : ClassType.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return ClassType.AREA_1;
	}
}
