package com.italia.municipality.lakesebu.controller;

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
public class DepositTransaction {
	
	private long id;
	private int index;
	private String day;
	private String particular;
	private String reference;
	private double credit;
	private double debit;
	private double balance;
	private Object data;
}
