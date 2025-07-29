package com.italia.municipality.lakesebu.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Mark Italia
 * @since 06/05/2025
 * @version 1.0
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Bills {
	
	private long contractId;
	private String tenantNo;
	private String accountNo;
	private String billNo;
	private String name;
	private String stall;
	private int year;
	private int month;
	private double principal;
	private double surcharge;
	private double interest;
	private double unpaidPricipal;
	private double total;
	
}
