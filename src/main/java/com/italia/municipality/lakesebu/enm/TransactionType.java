package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 02/01/2017
 * @version 1.0
 *
 */
public enum TransactionType {
	
	IRA(1, "IRA"),
	DEPOSIT(2, "DEPOSIT"),
	LOAN(3, "LOAN PAYMENT"),
	SINKING(4, "SINKING FUND"),
	OTHER_INCOME(5, "OTHER INCOME"),
	OTHER_EXPENSES(6, "OTHER EXPENSES"),
	BOTH(7, "INCOME EXPENSES"),
	SHARE(8, "SHARE"),
	COLLECTION(9, "COLLECTION"),
	PAYMENT(10, "PAYMENT"),
	CHECK_ISSUED(11,"CHECK ISSUED"),
	CASH_ADVANCE(12, "CASH ADVANCE"),
	JEV(13,"JOURNAL ENTRY VOUCHER");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private TransactionType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		
		for(TransactionType type : TransactionType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return TransactionType.IRA.getName();
	}
	
	public static int idName(String name){
		
		for(TransactionType type : TransactionType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return TransactionType.IRA.getId();
	}
	
}