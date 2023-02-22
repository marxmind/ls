package com.italia.municipality.lakesebu.xml;

/**
 * 
 * @author mark italia
 * @since 02/08/2017
 * @version 1.0
 *
 */
public enum Check {

	DATE("cashDate"),
	DEPARTMENT_ID("departmentid"),
	OR_NO("orno"),
	CHECK_NO("checkno"),
	VOUCHER_NO("voucherno"),
	PAYEE("cashParticulars"),
	NATURE_PAYMENT("naturepayment"),
	IS_ACTIVE("cashisactive"),
	TRANSACTION_TYPE("cashtranstype"),
	ACCOUNT_TYPE("bankid"),
	AMOUNT("cashCredit"),
	USER("userdtlsid");
	
	private String tagName;
	
	public String getTagName(){
		return tagName;
	}
	
	private Check(String tagName){
		this.tagName = tagName;
	}
	
}
