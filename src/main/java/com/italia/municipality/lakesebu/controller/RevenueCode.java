package com.italia.municipality.lakesebu.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class RevenueCode {

	private String articleCode;
	private String articleName;
	private String subName;
	private String particular;
	private String description;
	private double fee;
	private double mpf;
	private double espf;
	private double emf;
	private double rmf;
	private double sf;
	private double ssf;

}