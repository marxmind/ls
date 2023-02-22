package com.italia.municipality.lakesebu.controller;

import java.math.BigDecimal;

public interface IBudget {

	long getId();
	void setId(long id);
	String getBudgetDate();
	void setBudgetDate(String budgetDate);
	BigDecimal getAmount();
	void setAmount(BigDecimal amount);
	String getProcessBy();
	void setProcessBy(String processBy);
	int getIsActive();
	void setIsActive(int isActive);
	IBudgetType getBudgetType();
	void setBudgetType(IBudgetType budgetType);
	BankAccounts getAccounts();
	void setAccounts(BankAccounts accounts);
	double getLimitAmount();
	void setLimitAmount(double limitAmount);
	
	int getCycleDate();
	void setCycleDate(int cycleDate);
	
	
	String getUsedAmount();
	void setUsedAmount(String usedAmount);
	String getRemainingAmount();
	void setRemainingAmount(String remainingAmount);
	String getBudgetAmount();
	void setBudgetAmount(String budgetAmount);
	BigDecimal getAddedAmount();
	void setAddedAmount(BigDecimal addedAmount);
	boolean getIsActivated();
	void setIsActivated(boolean isActivated);
	
	String getAddedAmountTmp();
	void setAddedAmountTmp(String addedAmountTmp);
	String getLimitAmountTmp();
	void setLimitAmountTmp(String limitAmountTmp);
	
	void save();
}