package com.italia.municipality.lakesebu.enm;

public enum VrTransStatus {
	
	CHECKING_CASH_FLOW(1, "Checking Cash Flow"),
	CERTIFICATION(2, "For Certification"),
	VR_APPROVAL(3, "For Voucher Approval"),
	CHECK_CREATION(4, "Ready for Check Creation"),
	CHECK_SIGANTORY(5, "For Check Signatory"),
	RELEASING(6, "For Check Releasing"),
	CLAIMED(7, "Claimed"),
	CANCELLED(8, "Cancelled");
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private VrTransStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	
	public static VrTransStatus val(int id) {
		for(VrTransStatus g : VrTransStatus.values()) {
			if(id==g.getId()) {
				return g;
			}
		}
		return VrTransStatus.CHECKING_CASH_FLOW;
	}
	public static VrTransStatus val(String name) {
		for(VrTransStatus g : VrTransStatus.values()) {
			if(name.equalsIgnoreCase(g.getName())) {
				return g;
			}
		}
		return VrTransStatus.CHECKING_CASH_FLOW;
	}
	
}
