package com.italia.municipality.lakesebu.controller;

import com.google.gson.Gson;
import com.italia.municipality.lakesebu.utils.DateUtils;

import lombok.Data;
@Data
public class NationalIDJsonData {

	private String DateIssued;
	private String Issuer;
	private String Suffix;
	private String lName;
	private String fName;
	private String mName;
	private String sex;
	private String BF;
	private String DOB;
	private String POB;
	private String PCN;
	private String alg;
	private String signature;

	public NationalIDJsonData(String jsonData) {
		
		jsonData = jsonData.replace("\"subject\":", "");
		jsonData = jsonData.replace("{", "");
		jsonData = jsonData.replace("}", "");
		jsonData = "{" + jsonData + "}";
		
		Gson gson = new Gson();
		NationalIDJsonData na = gson.fromJson(jsonData, NationalIDJsonData.class);
		
		String[] bod = na.getDOB().split(" ");
		String day = bod[1].replace(",", "");
		String month = DateUtils.getMonthNumber(bod[0]);
		
		this.DateIssued = na.getDateIssued();
		this.Issuer = na.getIssuer();
		this.Suffix = na.getSuffix();
		this.lName = na.getLName();
		this.fName = na.getFName();
		this.mName = na.getMName();
		this.sex = na.getSex();
		this.BF = na.getBF();
		this.DOB = bod[2] + "-" + month + "-" + day;
		this.POB = na.getPOB();
		this.PCN = na.getPCN();
		this.alg = na.getAlg();
		this.signature = na.getSignature();
		
	}
	
	
	
}