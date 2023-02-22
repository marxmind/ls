package com.italia.municipality.lakesebu.controller;

import java.sql.Timestamp;

public interface ITaxPayorTrans {

	long getId();
	void setId(long id);
	String getTransDate();
	void setTransDate(String transDate);
	double getAmount();
	void setAmount(double amount);
	String getAmountInWords();
	void setAmountInWords(String amountInWords);
	String getScNo();
	void setScNo(String scNo);
	int getAccountFormNo();
	void setAccountFormNo(int accountFormNo);
	int getIsactive();
	void setIsactive(int isactive);
	String getCheckNo();
	void setCheckNo(String checkNo);
	String getCheckDate();
	void setCheckDate(String checkDate);
	String getSigned1();
	void setSigned1(String signed1);
	String getSigned2();
	void setSigned2(String signed2);
	ITaxPayor getTaxPayor();
	void setTaxPayor(ITaxPayor taxPayor);
	Timestamp getTimestamp();
	void setTimestamp(Timestamp timestamp);
	int getStatus();
	void setStatus(int status);
	int getPaymentType();
	void setPaymentType(int paymentType);
	UserDtls getUserDtls();
	void setUserDtls(UserDtls userDtls); 
	int getIsSpecialCase();
	void setIsSpecialCase(int isSpecialCase);
	LandPayor getLandPayor();
	void setLandPayor(LandPayor landPayor);
	
	void save();
	static ITaxPayorTrans save(ITaxPayorTrans pay) {
		return pay;
	}
	static ITaxPayorTrans insertData(ITaxPayorTrans pay, String type){
		return pay;
	}
	static ITaxPayorTrans updateData(ITaxPayorTrans pay){
		return pay;
	}
	void delete();
}
