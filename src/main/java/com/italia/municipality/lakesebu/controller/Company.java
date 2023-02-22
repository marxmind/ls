package com.italia.municipality.lakesebu.controller;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 09/27/2016
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Company {

	private Long compid;
	private String companyName;
	private String address;
	private String contactNo;
	private String ownername;
	private Timestamp timestamp;
	
}