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
public class ReportFields {
	private int id;
	private String accntNumber;
	private String checkNo;
	private String date_disbursement;
	private String bankName;
	private String accntName;
	private String amount;
	private String payToTheOrderOf;
	private String amountInWOrds;
	private String signatory1;
	private String signatory2;
	private String processBy;
	private String date_edited;
	private String date_created;
	private String status;
	private String remarks;
}
