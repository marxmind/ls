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
public class BusinessApproval {

	private String dateFile;
	private String type;
	private String name;
	private String owner;
	private String address;
	private String lineofbusiness;
	private String datestop;
	private BusinessRequest requestData;
	private Livelihood businessData;
	
}
