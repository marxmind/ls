package com.italia.municipality.lakesebu.enm;

public enum RateType {

	DAILYRATE(1, "Daily Rate"),
	FIXEDRATE(2, "Fixed Rate"),
	REGRATE(3, "Regular Rate");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private RateType(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public RateType val(int id) {
		for(RateType g : RateType.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return RateType.DAILYRATE;
	}
	public RateType val(String name) {
		for(RateType g : RateType.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return RateType.DAILYRATE;
	}

}
