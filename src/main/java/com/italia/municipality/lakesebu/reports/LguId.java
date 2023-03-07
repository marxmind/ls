package com.italia.municipality.lakesebu.reports;

import java.io.InputStream;

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
public class LguId {
	
	private String idNumber;
	private String dateIssued;
	private String validUntil;
	private String provinceName;
	private String municipalityName;
	private InputStream lguLogo;
	private InputStream employeePicture;
	private InputStream employeeSignature;
	private String address;
	private String contactNo;
	private String birthDate;
	private String civilStatus;
	private String bloodType;
	private String emergencyContactDtls;
	private String departmentName;
	private String employeeName;
	private String desiganation;
	private InputStream mayorSignature;
	private String mayorName;
	private InputStream idBgFront;
	private InputStream idBgBack;
	private InputStream bgBig;
	private InputStream qrcode;
	private InputStream provLogo;
	
	private String card1;
	private String card2;
	private String card3;
	private String card4;
	private String card5;
	private String card6;
	
	
}