package com.italia.municipality.lakesebu.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Mark Italia
 * @since 08/19/2025
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class MarketSummaryReport {
	
	private TenantBilling billing;
	private int year;
	private String location;
	private String tenant;
	private double principal;
	private double arrears;
	private double jan;
	private double feb;
	private double mar;
	private double apr;
	private double may;
	private double jun;
	private double jul;
	private double aug;
	private double sep;
	private double oct;
	private double nov;
	private double dec;
	private double paid;
	private double balance;
	private double totalSurcharge;
	private double totalInterest;
	private double totalBill;
	private int sms;
	private String contactNo;
	
	private String style1;
	private String style2;
	private String style3;
	private String style4;
	private String style5;
	private String style6;
	private String style7;
	private String style8;
	private String style9;
	private String style10;
	private String style11;
	private String style12;
	
}
