package com.italia.municipality.lakesebu.enm;

public enum VoucherStatus {

	FOR_BUDGET_APPROVAL(1,5, "FOR BUDGET APPROVAL"),
	FOR_ACCOUNTING_APPROVAL(2,4,"ACCOUNTING APPROVAL"),
	FOR_TREASURER_APPROVAL(3,1,"TREASURER APPROVAL"),
	FOR_CHECK_CREATION(4, 1,"FOR CHECK CREATION"),
	FOR_SIGBATORIES(5, 1,"FOR SIGNATORIES"),
	WAITING_FOR_BANK_ADVICE(6,1, "WAITING FOR ADVICE"),
	READY_FOR_RELEASE(6, 1,"READY FOR RELEASE"),
	CHECK_ISSUED_TO_PAYOR(7,1, "CHECK ISSUED TO PAYOR"),
	BUDGET_DENIED(8,5,"BUDGET DENIED"),
	BUDGET_HOLD(9, 5,"BUDGET HOLD"),
	ACCOUNTING_DENIED(10, 4,"ACCOUNTING DENIED"),
	ACCOUNTING_HOLD(11, 4,"ACCOUNTING HOLD"),
	TREASURER_DENIED(12, 1,"TREASURER DENIED"),
	TREASURER_HOLD(13, 1,"TREASURER HOLD"),
	VOUCHER_CANCELLED(14, 0,"VOUCHER CANCELLED");
	
	private int id;
	private long departmentId;
	private String name;
	
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public long getDepartmentId() {
		return departmentId;
	} 
	
	private VoucherStatus(int id, int departmentId, String name){
		this.id = id;
		this.departmentId = departmentId;
		this.name = name;
	}
	
	public static String typeName(int id){
		for(VoucherStatus type : VoucherStatus.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		return VoucherStatus.FOR_BUDGET_APPROVAL.getName();
	}
	public static int typeId(String name){
		for(VoucherStatus type : VoucherStatus.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		return VoucherStatus.FOR_BUDGET_APPROVAL.getId();
	}
	
	public static VoucherStatus value(long departmentId){
		for(VoucherStatus type : VoucherStatus.values()){
			if(departmentId==type.getDepartmentId()){
				return type;
			}
		}
		return VoucherStatus.FOR_BUDGET_APPROVAL;
	}
	
}
