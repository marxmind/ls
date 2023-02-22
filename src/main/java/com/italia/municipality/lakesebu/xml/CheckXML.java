package com.italia.municipality.lakesebu.xml;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.UserDtls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class CheckXML {

	private int id;
	private String dateTrans;
	private String checkNo;
	private String orNumber;
	private String voucherNo;
	private String payee;
	private String naturePayment;
	private int transactionType;
	private int isActive;
	private double debitAmount;
	private double creditAmount;
	private double balance;
	private BankAccounts accounts;
	private Department department;
	private UserDtls userDtls;
}
