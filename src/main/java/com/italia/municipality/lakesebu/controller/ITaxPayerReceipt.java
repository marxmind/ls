package com.italia.municipality.lakesebu.controller;

import java.sql.Timestamp;

public interface ITaxPayerReceipt {

	long getId();
	void setId(long id);
	String getLocation();
	void setLocation(String location);
	String getLotBlockNo();
	void setLotBlockNo(String lotBlockNo);
	String getTaxDecNo();
	void setTaxDecNo(String taxDecNo);
	double getAssValueLand();
	void setAssValueLand(double assValueLand);
	double getAssValueImprv();
	void setAssValueImprv(double assValueImprv);
	double getAssValueTotal();
	void setAssValueTotal(double assValueTotal);
	double getTaxDue();
	void setTaxDue(double taxDue);
	String getInstallmentRangeAndType();
	void setInstallmentRangeAndType(String installmentRangeAndType);
	double getFullPayment();
	void setFullPayment(double fullPayment);
	double getPenaltyPercent();
	void setPenaltyPercent(double penaltyPercent);
	double getOverallTotal();
	void setOverallTotal(double overallTotal);
	int getIsActive();
	void setIsActive(int isActive);
	ITaxPayor getPayor();
	void setPayor(ITaxPayor payor);
	ITaxPayorTrans getPayorTrans();
	void setPayorTrans(ITaxPayorTrans payorTrans);
	Timestamp getTimestamp();
	void setTimestamp(Timestamp timestamp);
	boolean getIsAdjust();
	void setIsAdjust(boolean isAdjust);
	boolean getIsCase();
	void setIsCase(boolean isCase);
	String getRemarks();
	void setRemarks(String remarks);
	UserDtls getUserDtls();
	void setUserDtls(UserDtls userDtls);
	String getLandOwnerName();
	void setLandOwnerName(String landOwnerName);
	
	ILandType getLandType();
	void setLandType(ILandType landType);
	int getCnt();
	void setCnt(int cnt);
	
	String getFromYear();
	void setFromYear(String fromYear);
	String getToYear();
	void setToYear(String toYear);
	String getInstallmentType();
	void setInstallmentType(String installmentType);
	
	LandPayor getLandPayor();
	void setLandPayor(LandPayor landPayor);
	
	void save();
	static ITaxPayerReceipt save(ITaxPayerReceipt pay) {
		return pay;
	}
	static ITaxPayerReceipt insertData(ITaxPayerReceipt pay, String type){
		return pay;
	}
	static ITaxPayerReceipt updateData(ITaxPayerReceipt pay){
		return pay;
	}
	void delete();
	
}