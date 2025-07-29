package com.italia.municipality.lakesebu.enm;
/**
 * @since 05/28/2025
 * @version 1.0
 * @author Mark Italia
 * 
 */
public enum TenantStatus {
	
	RETIRED(0, "Retired"),
	ACTIVE(1, "Active"),
	ON_HOLD(2, "On Hold"),
	TRANFERED_OWNER(3, "Transfered Owner");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private TenantStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public static TenantStatus val(int id) {
		for(TenantStatus g : TenantStatus.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return TenantStatus.ACTIVE;
	}
	public static TenantStatus val(String name) {
		for(TenantStatus g : TenantStatus.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return TenantStatus.ACTIVE;
	}
	
}
