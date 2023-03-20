package com.italia.municipality.lakesebu.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 03/15/2023
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class OfficeBudget {

	private long id;
	private String officeName;
	private String mooeName;
	private String mooeCode;
	private String mooeBudget;
	private String remainingBudget;
	private String usedBudget;
	private String dateCheck;
	private String fundName;
	private String checkNumber;
	private String paymentName;
	private String checkAmount;
	
}
