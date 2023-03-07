package com.italia.municipality.lakesebu.licensing.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.controller.Employee;
import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.MultiLivelihood;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.DocTypes;
import com.italia.municipality.lakesebu.enm.Positions;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;

/**
 * 
 * @author mark italia
 * @since 07/14/2018
 * @version 1.0
 * @Description - version 6 layout of document
 */
public class DocumentPrintingV6 {
	
	private static final String REPORT_PATH =  ReadConfig.value(AppConf.REPORT_FOLDER);//AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() + AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
	private static final String BUSINESS_REPORT_MUNICIPAL = DocumentFormatter.getTagName("v6_business-municipal");
	private static final String BUSINESS_REPORT_CERTIFICATE = DocumentFormatter.getTagName("v6_business-municipal");
	private static final String BUSINESS_REPORT_PERMIT = DocumentFormatter.getTagName("v6_business-permit");
	private static final String FISHCAGE_REPORT_NAME = DocumentFormatter.getTagName("v6_fishcage-permit");
	private static final String DEATH_DOC = DocumentFormatter.getTagName("v6_death-document");
	private static final String BURIAL_ASS_DOC = DocumentFormatter.getTagName("v6_burial-indigent");
	private static final String FINANCIAL_ASS_DOC = DocumentFormatter.getTagName("v6_hospital-financial-document");
	private static final String GENERIC_DOC = DocumentFormatter.getTagName("v6_generic-document");
	private static final String DOC_OPEN_TITLE_REPORT_NAME = DocumentFormatter.getTagName("v6_generic-open-document");
	private static final String LARGE_CATTLE_DOC = DocumentFormatter.getTagName("v6_largecattle-document");
	private static final String LATE_BIRTH_DOC = DocumentFormatter.getTagName("v6_birth-document");
	private static final String RESIDENCY_DOC = DocumentFormatter.getTagName("v6_residency-document");
	private static final String COE_DOC = DocumentFormatter.getTagName("v6_coe-document");
	private static final String AUTHORIZATION_DOC = DocumentFormatter.getTagName("v6_authorization-document");
	private static final String LAND_DOC = DocumentFormatter.getTagName("v6_land-document");
	
public static Map<Integer, Object> printDocumentV6(Clearance clr) {
		
		Map<Integer, Object> mapObject = Collections.synchronizedMap(new HashMap<Integer, Object>());
		String BARANGAY = "Poblacion";
		String MUNICIPALITY = "Lake Sebu";
		String PROVINCE = "South Cotabato";
		
		HashMap param = new HashMap();
		Customer taxpayer =Customer.retrieve(clr.getTaxPayer().getId());
		clr.setTaxPayer(taxpayer);
		
		Employee employee = Employee.retrieve(clr.getEmployee().getId());//officer of the Day
		clr.setEmployee(employee);
		
		Employee kapitan = Employee.retrievePosition(Positions.CAPTAIN.getId());
		String kapNameSign = kapitan.getFirstName() + " "+ kapitan.getMiddleName().substring(0, 1) +". " + kapitan.getLastName(); 
		
		
		String REPORT_NAME = "";
		StringBuilder str = new StringBuilder();
		String detail_1 = "";
		String detail_2 = "";
		String purpose = "";
		String civilStatus = CivilStatus.typeName(clr.getTaxPayer().getCivilStatus());
		civilStatus = civilStatus.toLowerCase();
		
		String requestorPrintedName = clr.getTaxPayer().getFirstname() + " " + clr.getTaxPayer().getMiddlename().substring(0,1) + ". " + clr.getTaxPayer().getLastname();
		String requestor = DocumentPrinting.getAppellation(clr.getTaxPayer()) + " " + requestorPrintedName.toUpperCase();
		int reqage = taxpayer.getAge();
		if(reqage<=17) {
			requestor = clr.getTaxPayer().getFirstname().toUpperCase() + " " + clr.getTaxPayer().getMiddlename().substring(0,1).toUpperCase() + ". " + clr.getTaxPayer().getLastname().toUpperCase();
			
			if(reqage>=0 && reqage<=5){
				civilStatus = "Baby";
			}else if(reqage>=6 && reqage<=12){
				civilStatus = "Kid";
			}else{
				civilStatus = civilStatus+"/Teenager";
			}
			
		}
		
		String address = clr.getTaxPayer().getCompleteAddress();
		String municipal = MUNICIPALITY + ", " + PROVINCE;
				try{municipal = clr.getEmployee().getMunicipality().getName() + ", " + clr.getEmployee().getProvince().getName();}catch(Exception e) {}
		try{municipal = clr.getCedulaNumber().split("<:>")[2];}catch(Exception e){}
		String barangay = BARANGAY +"," + MUNICIPALITY + ", " + PROVINCE;
		try{barangay = clr.getEmployee().getBarangay().getName() + ", " + clr.getEmployee().getMunicipality().getName() + ", " + clr.getEmployee().getProvince().getName();}catch(Exception e) {}
		List<ClearanceRpt> reports = Collections.synchronizedList(new ArrayList<ClearanceRpt>());
		
		String path = REPORT_PATH ;
		
		if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_NEW.getId()== clr.getPurposeType() || 
				com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_RENEWAL.getId()== clr.getPurposeType() ||
						com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_CERTIFICATION.getId()== clr.getPurposeType() ||
								com.italia.municipality.lakesebu.enm.Purpose.RETIREMENT_BUSINESS.getId()== clr.getPurposeType() ||
										com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_FOR_LOAN_REQUIREMENTS.getId()== clr.getPurposeType()){ //business
			
			
			if(clr.getMultilivelihood()!=null && clr.getMultilivelihood().size()>0){
				for(MultiLivelihood mu : clr.getMultilivelihood()){
					ClearanceRpt rpt = new ClearanceRpt();
					Livelihood liv = mu.getLivelihood();
					try{
						liv = Livelihood.retrieve(" AND live.isactivelive AND live.livelihoodid=" + liv.getId(), new String[0]).get(0);
					}catch(Exception e){}
					
					String name = liv.getBusinessName()+"\n";
					       name += liv.getPurokName() + ", Brgy. " + liv.getBarangay().getName() + ", " + liv.getMunicipality().getName()+ ", " + liv.getProvince().getName();
					       rpt.setF1(name);
					//rpt.setF1(liv.getBusinessName());
					//rpt.setF2(liv.getPurokName() + ", Brgy. " + liv.getBarangay().getName() + ", " + liv.getMunicipality().getName()+ ", " + liv.getProvince().getName());
					            
					reports.add(rpt);
					
				}
			}
			
			String word = "";
			
			if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_NEW.getId()== clr.getPurposeType()){
				purpose="Business Application";
				REPORT_NAME = BUSINESS_REPORT_MUNICIPAL;
				str=new StringBuilder();
				
				str.append(requestor);
				detail_1 = str.toString();
				str=new StringBuilder();
				word = Words.getTagName("business-1");
				str.append(word);
				
				param.put("PARAM_ISSUED", Words.getTagName("business-5"));
				param.put("PARAM_REQUESTOR_POSITION", Words.getTagName("business-6"));
			}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_RENEWAL.getId()== clr.getPurposeType()){
				purpose="Business Renewal";
				REPORT_NAME = BUSINESS_REPORT_MUNICIPAL;
				str=new StringBuilder();
				
				str.append(requestor);
				detail_1 = str.toString();
				str=new StringBuilder();
				word = Words.getTagName("business-1");
				str.append(word);
				
				param.put("PARAM_ISSUED", Words.getTagName("business-5"));
				param.put("PARAM_REQUESTOR_POSITION", Words.getTagName("business-6"));
			}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_CERTIFICATION.getId()== clr.getPurposeType()){
				purpose="Business Certification";
				REPORT_NAME = BUSINESS_REPORT_CERTIFICATE;
				str=new StringBuilder();
				
				str.append(requestor);
				detail_1 = str.toString();
				str=new StringBuilder();
				word = Words.getTagName("business-3");
				word = word.replace("<barangayname>", "Brgy. " + BARANGAY);
				str.append(word);
				
				param.put("PARAM_ISSUED", Words.getTagName("business-9"));
				param.put("PARAM_REQUESTOR_POSITION", "Owner");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_FOR_LOAN_REQUIREMENTS.getId()== clr.getPurposeType()){
				purpose="Requirement for Loan Application";
				REPORT_NAME = BUSINESS_REPORT_CERTIFICATE;
				str=new StringBuilder();
				
				str.append(requestor);
				detail_1 = str.toString();
				str=new StringBuilder();
				word = Words.getTagName("business-3");
				word = word.replace("<barangayname>", "Brgy. " + BARANGAY);
				str.append(word);
				
				param.put("PARAM_ISSUED", Words.getTagName("business-7"));
				param.put("PARAM_REQUESTOR_POSITION", "Owner");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.RETIREMENT_BUSINESS.getId()== clr.getPurposeType()){
				purpose="Requirement for Retirement of Business";
				REPORT_NAME = BUSINESS_REPORT_CERTIFICATE;
				str=new StringBuilder();
				
				str.append(requestor);
				detail_1 = str.toString();
				str=new StringBuilder();
				word = Words.getTagName("business-8");
				word = word.replace("<barangayname>", "Brgy. " + BARANGAY);
				str.append(word);
				
				param.put("PARAM_ISSUED", Words.getTagName("business-7"));
				param.put("PARAM_REQUESTOR_POSITION", Words.getTagName("business-6"));
			}
			
			word = Words.getTagName("issued-date-office");
			word = word.replace("<day>", DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]));
			word = word.replace("<month>", DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])));
			word = word.replace("<year>", clr.getIssuedDate().split("-")[0]);
			word = word.replace("<barangayaddress>", barangay);
			str.append(word);
			
			word = Words.getTagName("business-4");
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				String[] ced = clr.getCedulaNumber().split("<:>");
				word = word.replace("<purpose>", purpose);
				word = word.replace("<ctcno>", ced[0]);
				word = word.replace("<ctcissueddate>", DateUtils.convertDateToMonthDayYear(ced[1]));
				word = word.replace("<ctcissuedaddress>", municipal);
				str.append(word);
			}else {
				word = word.replace("<purpose>", purpose);
				word = word.replace("<ctcno>", "N/A");
				word = word.replace("<ctcissueddate>", "N/A");
				word = word.replace("<ctcissuedaddress>", "N/A");
				str.append(word);
			}
			detail_2 = str.toString();
			
		}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_PERMIT.getId()== clr.getPurposeType()){ 	
			
			REPORT_NAME = BUSINESS_REPORT_PERMIT;
			
			ClearanceRpt rpt = new ClearanceRpt();
			reports.add(rpt);
			
			String[] notes = clr.getNotes().split("<:>");
			/**
			 * [0] - Control No
			 * [1] - NEW/RENEWAL
			 * [2] - MEMO
			 * [3] - Business Engaged
			 */
			String yearReg = DateUtils.getCurrentYear()+"";
			if(clr.getMultilivelihood()!=null && clr.getMultilivelihood().size()>0){
				for(MultiLivelihood mu : clr.getMultilivelihood()){
					
					Livelihood liv = mu.getLivelihood();
					try{
						liv = Livelihood.retrieve(" AND live.isactivelive AND live.livelihoodid=" + liv.getId(), new String[0]).get(0);
					}catch(Exception e){}
					
					param.put("PARAM_BUSINESS_NAME", liv.getBusinessName().toUpperCase());
					String businessaddress = liv.getPurokName() + ", " + liv.getBarangay().getName() + ", " + liv.getMunicipality().getName() + ", " + liv.getProvince().getName();
					param.put("PARAM_BUSINESS_ADDRESS", businessaddress.toUpperCase());
					
					String location = liv.getMunicipality().getName() + ", " + liv.getProvince().getName();
					param.put("PARAM_BUSINESS_MUNICIPALITY_ADDRESS", location + " "+Words.getTagName("business-permit-barangay-string-1"));
					yearReg = liv.getDateRegistered().split("-")[0];
				}
			}
			String issuedYear = clr.getIssuedDate().split("-")[0];
			if(issuedYear.equalsIgnoreCase(yearReg)) {
				purpose = "Business Permit Application";
			}else {
				purpose = "Business Permit Renewal";
			}
			
			param.put("PARAM_BUSINESS_TYPE", notes[3]);
			
			String[] ced = null;
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				ced = clr.getCedulaNumber().split("<:>");
			}
			
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				String word = Words.getTagName("business-permit-barangay-string-2");
				word = word.replace("<purpose>", purpose);
				word = word.replace("<ctcno>", ced[0]);
				word = word.replace("<ctcissueddate>", DateUtils.convertDateToMonthDayYear(ced[1]));
				word = word.replace("<ctcissuedaddress>", municipal);
				param.put("PARAM_CTAX", word);
			}else{
				String word = Words.getTagName("business-permit-barangay-string-2");
				word = word.replace("<purpose>", purpose);
				word = word.replace("<ctcno>", "N/A");
				word = word.replace("<ctcissueddate>", "N/A");
				word = word.replace("<ctcissuedaddress>", "N/A");
				param.put("PARAM_CTAX",word);
			}
			
			param.put("PARAM_DAY", DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]));
			param.put("PARAM_MONTH", DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])) + " " + clr.getIssuedDate().split("-")[0]);
		}else if(com.italia.municipality.lakesebu.enm.Purpose.FISCH_CAGE.getId()== clr.getPurposeType() || com.italia.municipality.lakesebu.enm.Purpose.FISH_CAGE_RENEWAL.getId()== clr.getPurposeType()){ //Fish cage
			REPORT_NAME = FISHCAGE_REPORT_NAME;
			if(clr.getMultilivelihood()!=null && clr.getMultilivelihood().size()>0){
				int cnt = 1;
				for(MultiLivelihood mu : clr.getMultilivelihood()){
					ClearanceRpt rpt = new ClearanceRpt();
					Livelihood liv = mu.getLivelihood();
					try{
						liv = Livelihood.retrieve(" AND live.isactivelive AND live.livelihoodid=" + liv.getId(), new String[0]).get(0);
					}catch(Exception e){}
					
					/*rpt.setF1(liv.getPurokName());
					rpt.setF2(liv.getAreaMeter());
					try{rpt.setF3(liv.getSupportingDetails().toLowerCase());}catch(Exception e) {}*/
					
					String details = cnt + ")" + liv.getTaxPayer().getFullname() + "/" +liv.getPurokName() + "/" +liv.getAreaMeter() + "/" + (liv.getSupportingDetails()!=null? liv.getSupportingDetails().toUpperCase() : "N/A");
					rpt.setF1(details);
					reports.add(rpt);
					cnt++;
				}
			}
			
			String word = "";
			word = Words.getTagName("fish-cage-string-1");
			word = word.replace("<requestor>", requestor);
			word = word.replace("<civilstatus>", civilStatus);
			word = word.replace("<requestoraddress>", address);
			str.append(word);
			
			str.append(Words.getTagName("fish-cage-string-2"));
			
			detail_1 = str.toString();
			
			str = new StringBuilder();
			if(com.italia.municipality.lakesebu.enm.Purpose.FISCH_CAGE.getId()== clr.getPurposeType()){
				purpose =  Words.getTagName("fish-cage-string-4");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.FISH_CAGE_RENEWAL.getId()== clr.getPurposeType()){
				purpose =  Words.getTagName("fish-cage-string-5");
			}
			
			word = Words.getTagName("fish-cage-string-6");
			str.append(word);
			
			word = Words.getTagName("issued-date-office");
			word = word.replace("<day>", DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]));
			word = word.replace("<month>", DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])));
			word = word.replace("<year>", clr.getIssuedDate().split("-")[0]);
			word = word.replace("<barangayaddress>", barangay);
			str.append(word);
			
			word = Words.getTagName("business-4");
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				String[] ced = clr.getCedulaNumber().split("<:>");
				word = word.replace("<purpose>", purpose);
				word = word.replace("<ctcno>", ced[0]);
				word = word.replace("<ctcissueddate>", DateUtils.convertDateToMonthDayYear(ced[1]));
				word = word.replace("<ctcissuedaddress>", municipal);
				str.append(word);
			}else {
				word = word.replace("<purpose>", purpose);
				word = word.replace("<ctcno>", "N/A");
				word = word.replace("<ctcissueddate>", "N/A");
				word = word.replace("<ctcissuedaddress>", "N/A");
				str.append(word);
			}
			
			detail_2 = str.toString();
		
		
		
	}else{
		
		REPORT_NAME = GENERIC_DOC;
		
		String words = Words.getTagName("other-string-1");
		if(DocTypes.CLEARANCE_OPEN_TITLE.getId()==clr.getDocumentType()) {
			words = words.replace("<docttype>", DocTypes.CLEARANCE.getName());
		}else if(DocTypes.CERTIFICATE_OPEN_TITLE.getId()==clr.getDocumentType()) {
			words = words.replace("<docttype>", DocTypes.CERTIFICATE.getName());
		}else {
			words = words.replace("<docttype>", DocTypes.typeName(clr.getDocumentType()));
		}
		str.append(words);
		
		supplyDetails(clr, reports, address, civilStatus, municipal);
		
		purpose = com.italia.municipality.lakesebu.enm.Purpose.typeName(clr.getPurposeType());
		
		ClearanceRpt rpt = new ClearanceRpt();
		rpt.setF1("PURPOSE:");rpt.setF2(purpose);
		reports.add(rpt);
		
		detail_1 = str.toString();
		str = new StringBuilder();
		
		String heshe = clr.getTaxPayer().getGender().equalsIgnoreCase("1")? "he" : "she";
		
		
		if(DocTypes.CLEARANCE.getId()==clr.getDocumentType() || DocTypes.CLEARANCE_OPEN_TITLE.getId()==clr.getDocumentType()){
			
			//reports = checkSummonRemarks(clr.getTaxPayer(), reports, Purpose.AFP_TRAINING);
			
			words = Words.getTagName("other-string-2");
			words = words.replace("<heshe>", heshe);
			int index = reports.size();
			index -=1;
			if(!"NO DEROGATORY RECORD ON FILE".equalsIgnoreCase(reports.get(index).getF2())) {
				words = words.replace("has no derogatory", "has a pending case");
				words = words.replace(" and a law abiding citizen in our locality.", ".");
			}
			str.append(words);
			
			
			/*rpt = new ClearanceRpt();
			rpt.setF1("REMARKS:");rpt.setF2(Words.getTagName("other-string-4"));
			reports.add(rpt);*/
			
		}else if(DocTypes.CERTIFICATE.getId()==clr.getDocumentType() || DocTypes.CERTIFICATE_OPEN_TITLE.getId()==clr.getDocumentType()){
			//reports = checkSummonRemarks(clr.getTaxPayer(), reports, Purpose.AFP_TRAINING);
			
			words = Words.getTagName("other-string-2");
			words = words.replace("<heshe>", heshe);
			int index = reports.size();
			index -=1;
			if(!"NO DEROGATORY RECORD ON FILE".equalsIgnoreCase(reports.get(index).getF2())) {
				words = words.replace("has no derogatory", "has a pending case");
				words = words.replace(" and a law abiding citizen in our locality.", ".");
			}
			str.append(words);
			
			/*rpt = new ClearanceRpt();
			rpt.setF1("REMARKS:");rpt.setF2(Words.getTagName("other-string-4"));
			reports.add(rpt);*/
		
		}else if(DocTypes.INDIGENT.getId()==clr.getDocumentType()){
			
			words = Words.getTagName("other-string-3");
			words = words.replace("<heshe>", heshe);
			str.append(words);
			
			rpt = new ClearanceRpt();
			rpt.setF1("REMARKS:");rpt.setF2(Words.getTagName("other-string-5"));
			reports.add(rpt);
			
		}
		
		
		
		words = Words.getTagName("issued-date-office-2");
		words = words.replace("<day>", DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]));
		words = words.replace("<month>", DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])));
		words = words.replace("<year>", clr.getIssuedDate().split("-")[0]);
		words = words.replace("<barangayaddress>", barangay);
		str.append(words);
		
		detail_2 = str.toString();
		
	}	
		
		
		
				//do not move this code this code use for custom title
				if(DocTypes.CERTIFICATE_OPEN_TITLE.getId()==clr.getDocumentType() || 
						DocTypes.CLEARANCE_OPEN_TITLE.getId()==clr.getDocumentType()){
					REPORT_NAME = DOC_OPEN_TITLE_REPORT_NAME;
					param.put("PARAM_DOC_OPEN_TITLE", clr.getCustomTitle().toUpperCase());
					
				}else if(DocTypes.LOW_INCOME.getId()==clr.getDocumentType()) {
					REPORT_NAME = DOC_OPEN_TITLE_REPORT_NAME;
					param.put("PARAM_DOC_OPEN_TITLE", "BARANGAY CERTIFICATE OF LOW INCOME");
					
				}	
				
				ReportCompiler compiler = new ReportCompiler();
				String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, path);
				
				JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
				UserDtls user = Login.getUserLogin().getUserDtls();
					
					
					param.put("PARAM_ISSUED_DATE", DateUtils.getCurrentDateMMMMDDYYYY());
					param.put("PARAM_REQUESTOR_NAME", clr.getTaxPayer().getFirstname().toUpperCase() + " " + clr.getTaxPayer().getMiddlename().substring(0,1).toUpperCase() + ". " + clr.getTaxPayer().getLastname().toUpperCase());
					//param.put("PARAM_BARCODE", clr.getTaxPayer().getFullname().toUpperCase());
					param.put("PARAM_CEDULA_NO", clr.getCedulaNumber());
					param.put("PARAM_ISSUED_LOCATION",  MUNICIPALITY + ", " + PROVINCE); // "Lake Sebu, South Cotabato");
					
					
					
					String signerOD="";
					String signerTitle="";
					
					try {
					signerOD=employee.getFirstName() + " " + employee.getMiddleName().substring(0,1) + ". " + employee.getLastName();
					
					if(employee.getPosition().getId()==1){
						signerTitle=clr.getEmployee().getPosition().getName();
					}else if(employee.getPosition().getId()==2){
						signerTitle="Barangay "+clr.getEmployee().getPosition().getName() +"\nOfficer of the day";
						signerTitle+="\nOn behalf of\n\n\n";
						signerTitle+=kapitan.getPosition().getName();
						signerOD +="\n\n\n\n"+kapNameSign.toUpperCase();
					}else{
						signerTitle="Barangay "+clr.getEmployee().getPosition().getName();
						signerTitle+="\nOn behalf of\n\n\n\n";
						signerTitle+=kapitan.getPosition().getName();
						signerOD +="\n\n\n\n"+kapNameSign.toUpperCase();
					}
					}catch(Exception e) {
						signerOD = kapNameSign;
						signerTitle=kapitan.getPosition().getName();
					}
					
					param.put("PARAM_OFFICER_DAY", signerOD.toUpperCase());
					param.put("PARAM_OFFICIAL_TITLE",signerTitle);
					
					int age= taxpayer.getAge();
					String legal = "";
					if(age==0){
						legal = "infant";
						detail_1 = detail_1.replace("of legal age", legal);
						detail_1 = detail_1.replace(" single,", "");
					}else if(age>=1 && age<=5){
						legal = age + " year old, baby";
						detail_1 = detail_1.replace("of legal age", legal);
						detail_1 = detail_1.replace(" single,", "");
					}else if(age>=6 && age<=9){
						legal = age + " year old, kid";
						detail_1 = detail_1.replace("of legal age", legal);
						detail_1 = detail_1.replace(" single,", "");
					}else if(age>=10 && age<=17){
						legal = age + " year old, teenager";
						detail_1 = detail_1.replace("of legal age", legal);
						detail_1 = detail_1.replace(" single,", "");
					}
					
					detail_1 =detail_1.replace("requested a INDIGENT CERTIFICATION", "requested an INDIGENT CERTIFICATION");
					
					param.put("PARAM_CLEARANCE_DETAILS", detail_1);
					param.put("PARAM_CLEARANCE_DETAILS2", detail_2);
					param.put("PARAM_PURPOSE", purpose);
					
					//Displayed photo
					boolean isOkToShowPicture = true;//com.italia.marxmind.bris.enm.Purpose.BURIAL.getId()==clr.getPurposeType() || 
					if(DocTypes.DEATH_CERT.getId()==clr.getDocumentType() || 
								DocTypes.LATE_DEATH_CERT.getId()==clr.getDocumentType() || 
									DocTypes.COE.getId()==clr.getDocumentType()){
						isOkToShowPicture = false;
					}
					
					if(isOkToShowPicture){				
						if(clr.getPhotoId()!=null && !clr.getPhotoId().isEmpty()){
							String picture = DocumentPrinting.copyPhoto(clr.getPhotoId()).replace("\\", "/");
							System.out.println("Images: " + picture);
							//InputStream img = this.getClass().getResourceAsStream("/"+clr.getPhotoId()+".jpg");
							File file = new File(picture);
							if(file.exists()){
								try{
								FileInputStream st = new FileInputStream(file);
								param.put("PARAM_PICTURE", st);
								}catch(Exception e){}
							}else{
								picture = DocumentPrinting.copyPhoto(taxpayer.getPhotoid()).replace("\\", "/");
								file = new File(picture);
								try{
									FileInputStream st = new FileInputStream(file);
									param.put("PARAM_PICTURE", st);
								}catch(Exception e){}
							}
						}
					
					}
					
					String word = Words.getTagName("province-line");
					param.put("PARAM_PROVINCE", word.replace("<province>", PROVINCE));
					word = Words.getTagName("municipality-line");
					param.put("PARAM_MUNICIPALITY", word.replace("<municipality>", MUNICIPALITY));
					word = Words.getTagName("barangay-line");
					param.put("PARAM_BARANGAY", word.replace("<barangay>", BARANGAY.toUpperCase()));
					
					//Officials
					String officialPicture = path + "officials.jpg";
					try{File file = new File(officialPicture);
					FileInputStream off = new FileInputStream(file);
					param.put("PARAM_OFFICIALS", off);
					}catch(Exception e){}
					
					//logo
					String officialLogo = path + "logo.png";
					try{File file = new File(officialLogo);
					FileInputStream off = new FileInputStream(file);
					param.put("PARAM_LOGO", off);
					}catch(Exception e){}
					
					
					if(DocTypes.CERTIFICATE.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "certificate.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.CLEARANCE.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "clearance.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
						
					}else if(DocTypes.DEATH_CERT.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "deathcert.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.LATE_DEATH_CERT.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "latedeathcert.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
							
					}else if(DocTypes.INDIGENT.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "indigency.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.LATE_BIRTH_REG.getId()==clr.getDocumentType()){
						
						//certificate
						String certificate = path + "latebirth.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
						
					}else if(DocTypes.LIVE_BIRTH_REG.getId()==clr.getDocumentType()){
						
						//certificate
						String certificate = path + "livebirth.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.BARANGAY_BUSINESS_PERMIT.getId()==clr.getDocumentType()){
						
						//certificate
						String certificate = path + "businesspermit.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.COE.getId()==clr.getDocumentType()){
						
						//certificate
						String certificate = path + "coe.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.AUTHORIZATION_LETTER.getId()==clr.getDocumentType()){
						
						//authorization
						String authorization = path + "authorization.png";
						try{File file1 = new File(authorization);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.RESIDENCY.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "residency.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}else if(DocTypes.INCOME.getId()==clr.getDocumentType()){
						//certificate
						String certificate = path + "income.png";
						try{File file1 = new File(certificate);
						FileInputStream off1 = new FileInputStream(file1);
						param.put("PARAM_CERTIFICATE", off1);
						}catch(Exception e){}
						
					}
		
		//officialseallakesebu
		String lakesebuofficialseal = path + "municipalseal.png";
		try{File file1 = new File(lakesebuofficialseal);
		FileInputStream off2 = new FileInputStream(file1);
		param.put("PARAM_LOGO_LAKESEBU", off2);
		}catch(Exception e){}
		
		//DILG
		String dilg = path + "dilg.png";
		try{File file1 = new File(dilg);
		FileInputStream off2 = new FileInputStream(file1);
		param.put("PARAM_DILG", off2);
		}catch(Exception e){}
		
		//logo
		String logo = path + "barangaylogotrans.png";
		try{File file = new File(logo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_SEALTRANSPARENT", off);
		}catch(Exception e){}
		
		//background
		String backlogo = path + "documentbg.png";
		try{File file = new File(backlogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_BACKGROUND", off);
		}catch(Exception e){}
		
		FileInputStream barPdf = null;
		try{
			Barcode barcode = null;
			
			//barcode = BarcodeFactory.createPDF417(tax.getFullname());
			barcode = BarcodeFactory.create3of9(clr.getTaxPayer().getCardno(), false);
			
			barcode.setDrawingText(false);
			File pdf = new File(clr.getTaxPayer().getCardno()+".png");
			
			BarcodeImageHandler.savePNG(barcode, pdf);
			barPdf = new FileInputStream(pdf);
			param.put("PARAM_BARCODE", barPdf);
		}catch(Exception e){e.printStackTrace();}
		
		if(clr.getIsPayable()==0){
			param.put("PARAM_PAID", "Amount Paid: FREE");
			param.put("PARAM_OR_NUMBER", "OR NO: N/A");
		}else{
			param.put("PARAM_PAID", "Amount Paid: Php "+Currency.formatAmount(clr.getAmountPaid()));
			param.put("PARAM_OR_NUMBER", "OR NO: "+clr.getOrNumber());
		}
		
		
		try{param.put("PARAM_DOCUMENT_NOTE", DocumentPrinting.documentNote(clr));}catch(NullPointerException e) {}
		try{param.put("PARAM_TAGLINE", Words.getTagName("tagline"));}catch(NullPointerException e) {}
		try{param.put("PARAM_REQUESTOR_PRINTED_NAME", requestorPrintedName.toUpperCase());}catch(NullPointerException e) {}
		
		//PATH
		mapObject.put(1, path);
		//REPORT NAME
		mapObject.put(2, REPORT_NAME);
		//JRXML FILE
		mapObject.put(3, jrxmlFile);
		//PARAMS
		mapObject.put(4, param);
		//JRBEAN collection
		mapObject.put(5, beanColl);
		
		return mapObject;
	}
	
	private static void supplyDetails(Clearance clr, List<ClearanceRpt> reports, String address, String civilStatus, String municipal) {
		Customer taxpayer = clr.getTaxPayer();
		String requestor = taxpayer.getLastname() +", " + taxpayer.getFirstname() + " " + taxpayer.getMiddlename();
		String gender = taxpayer.getGender().equalsIgnoreCase("1")? "MALE" : "FEMALE";
		
		ClearanceRpt rpt = new ClearanceRpt();
		rpt.setF1("NAME:");rpt.setF2(requestor.toUpperCase());
		reports.add(rpt);
		
		rpt = new ClearanceRpt();
		rpt.setF1("GENDER:");rpt.setF2(gender);
		reports.add(rpt);
		
		rpt = new ClearanceRpt();
		rpt.setF1("ADDRESS:");rpt.setF2(address.toUpperCase());
		reports.add(rpt);
		
		rpt = new ClearanceRpt();
		rpt.setF1("BIRTHDAY:");rpt.setF2(DateUtils.convertDateToMonthDayYear(taxpayer.getBirthdate()).toUpperCase());
		reports.add(rpt);
		
		rpt = new ClearanceRpt();
		rpt.setF1("CIVIL STATUS:");rpt.setF2(civilStatus.toUpperCase());
		reports.add(rpt);
		
		rpt = new ClearanceRpt();
		rpt.setF1("CITIZENSHIP:");rpt.setF2(Words.getTagName("citizenship"));
		reports.add(rpt);
		
		
		String[] ced = null;
		if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
			ced = clr.getCedulaNumber().split("<:>");
			rpt = new ClearanceRpt();
			rpt.setF1("CTC NO:");rpt.setF2(ced[0]);
			reports.add(rpt);
			
			rpt = new ClearanceRpt();
			rpt.setF1("DATE ISSUED:");rpt.setF2(DateUtils.convertDateToMonthDayYear(ced[1]).toUpperCase());
			reports.add(rpt);
			
			rpt = new ClearanceRpt();
			rpt.setF1("LOCATION:");rpt.setF2(municipal.toUpperCase());
			reports.add(rpt);
		}
	}
	
	private static String civilStatus(int age, String civilStatus) {
		if(age<=17){
			if(age==0){
				civilStatus = "infant";
			}else if(age>=1 && age<=5){
				civilStatus = "baby";
			}else if(age>=6 && age<=9){
				civilStatus = "kid";
			}else if(age>=10 && age<=17){
				civilStatus = "teenager";
			}
		}
		return civilStatus;
	}
	
	
	
	
	
}
