package com.italia.municipality.lakesebu.licensing.controller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import com.italia.municipality.lakesebu.enm.DocStyle;
import com.italia.municipality.lakesebu.enm.DocTypes;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;

/**
 * 
 * @author mark italia
 * @since 07/13/2018
 * @version 1.0
 */
public class DocumentPrinting {
	
	private static final String REPORT_PATH = ReadConfig.value(AppConf.REPORT_FOLDER);
	private static final String BUSINESS_REPORT_NAME = REPORT_PATH + "businesspermitV6.jrxml";
	private static final String BARANGAY_BUSINESS_PERMIT_NAME = REPORT_PATH + "businesspermitV6.jrxml";
	private static final String DOC_OPEN_TITLE_REPORT_NAME = "";//ReadXML.value(ReportTag.DOCUMENT_OPEN_TITLE);
	
	private final static String IMAGE_PATH = ReadConfig.value(AppConf.LICENSING_IMG) ;
	
	public static String copyPhoto(String photoId){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		
        String driveImage =  IMAGE_PATH + photoId + ".jpg";
        String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;
         
            String pathToSave = externalContext.getRealPath("") + contextImageLoc;
            File file = new File(driveImage);
            try{
    			Files.copy(file.toPath(), (new File(pathToSave + file.getName())).toPath(),
    			        StandardCopyOption.REPLACE_EXISTING);
    			}catch(IOException e){}
            
            return driveImage;
	}
	
	public static String getAppellation(Customer customer){
		String appellation="";
		
		if("1".equalsIgnoreCase(customer.getGender())){//male
			appellation = "Mr.";
		}else{//female
			if(CivilStatus.SINGLE.getId()==customer.getCivilStatus()){
				appellation = "Miss";
			}else{
				appellation = "Mrs.";
			}
		}
		
		return appellation.toUpperCase();
	}
	
	public static String documentNote(Clearance clr) {
		String note = "";
		
		if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_NEW.getId()== clr.getPurposeType()){
			note = "Valid Until December 31, "+clr.getIssuedDate().split("-")[0]+"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "New Business";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_RENEWAL.getId()== clr.getPurposeType()){
			note = "Valid Until December 31, "+clr.getIssuedDate().split("-")[0]+"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "Business Renewal";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_CERTIFICATION.getId()== clr.getPurposeType()){
			note = "Valid Until December 31, "+clr.getIssuedDate().split("-")[0]+"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "Certification";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_FOR_LOAN_REQUIREMENTS.getId()== clr.getPurposeType()){
			String valid = clr.getDocumentValidity()==0? "6 Months" : (clr.getDocumentValidity()==1? " 1 Month" : clr.getDocumentValidity() + " Months");
			note = "Valid Only for " + valid +"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "Business Loan";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.RETIREMENT_BUSINESS.getId()== clr.getPurposeType()){
			note = "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_PERMIT.getId()== clr.getPurposeType()){
			note = "Valid Until December 31, "+clr.getIssuedDate().split("-")[0]+"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "Business Permit";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.FISCH_CAGE.getId()== clr.getPurposeType()){
			note = "Valid Until December 31, "+clr.getIssuedDate().split("-")[0]+"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "Fish Cage Application";
		}else if(com.italia.municipality.lakesebu.enm.Purpose.FISH_CAGE_RENEWAL.getId()== clr.getPurposeType()){
			note = "Valid Until December 31, "+clr.getIssuedDate().split("-")[0]+"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			note += "Fish Cage Renewal";
		}else {
			String valid = clr.getDocumentValidity()==0? "6 Months" : (clr.getDocumentValidity()==1? " 1 Month" : clr.getDocumentValidity() + " Months");
			note = "Valid Only for " + valid +"\n";
			note += "Brgy. Document No " + clr.getId() +"\n";
			note += "Series of " + clr.getIssuedDate().split("-")[0]+"\n";
			if(DocTypes.CERTIFICATE_OPEN_TITLE.getId()==clr.getDocumentType()) {
				note += "Certificate";
			}else if(DocTypes.CLEARANCE_OPEN_TITLE.getId()==clr.getDocumentType()) {
				note += "Clearance";
			}else {
				note += DocTypes.typeName(clr.getDocumentType());
			}
		}
		
		return note;
	}
	
	public static Map<Integer, Object> printRequestedDocument(Clearance clr){
		String docStyle = DocumentFormatter.getTagName("documentLayout");
		if(DocStyle.V5.getName().equalsIgnoreCase(docStyle)) {
			return printDocument(clr);
		}else if(DocStyle.V6.getName().equalsIgnoreCase(docStyle)) {
			return DocumentPrintingV6.printDocumentV6(clr);	
		}else {
			return new HashMap<Integer, Object>();
		}
	}
	
	private static Map<Integer, Object> printDocument(Clearance clr) {
		
		String BARANGAY = "Poblacion";
		String MUNICIPALITY = "Lake Sebu";
		String PROVINCE = "South Cotabato";
		
		HashMap param = new HashMap();
		Customer taxpayer =Customer.retrieve(clr.getTaxPayer().getId());
		clr.setTaxPayer(taxpayer);
		
		Employee employee = Employee.retrieve(clr.getEmployee().getId());
		clr.setEmployee(employee);
		
		
		String REPORT_NAME = "";
		StringBuilder str = new StringBuilder();
		String detail_1 = "";
		String detail_2 = "";
		String purpose = "";
		String civilStatus = CivilStatus.typeName(clr.getTaxPayer().getCivilStatus());
		civilStatus = civilStatus.toLowerCase();
		
		String requestor = getAppellation(clr.getTaxPayer()) + " " + clr.getTaxPayer().getFirstname().toUpperCase() + " " + clr.getTaxPayer().getMiddlename().substring(0,1).toUpperCase() + ". " + clr.getTaxPayer().getLastname().toUpperCase();
		//String address = clr.getTaxPayer().getPurok().getPurokName() + ", " + clr.getTaxPayer().getBarangay().getName() + ", " + clr.getTaxPayer().getMunicipality().getName() + ", " + clr.getTaxPayer().getProvince().getName();
		String address = clr.getTaxPayer().getCompleteAddress();
		String municipal = clr.getEmployee().getMunicipality().getName() + ", " + clr.getEmployee().getProvince().getName();
		try{municipal = clr.getCedulaNumber().split("<:>")[2];}catch(Exception e){}
		String barangay = clr.getEmployee().getBarangay().getName() + ", " + clr.getEmployee().getMunicipality().getName() + ", " + clr.getEmployee().getProvince().getName();
		List<ClearanceRpt> reports = Collections.synchronizedList(new ArrayList<ClearanceRpt>());
		
		String path = REPORT_PATH ;
		
		if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_NEW.getId()== clr.getPurposeType() || 
				com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_RENEWAL.getId()== clr.getPurposeType() ||
						com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_CERTIFICATION.getId()== clr.getPurposeType() ||
								com.italia.municipality.lakesebu.enm.Purpose.RETIREMENT_BUSINESS.getId()== clr.getPurposeType() ||
										com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_FOR_LOAN_REQUIREMENTS.getId()== clr.getPurposeType()){ //business
			REPORT_NAME = BUSINESS_REPORT_NAME;
			
			if(clr.getMultilivelihood()!=null && clr.getMultilivelihood().size()>0){
				for(MultiLivelihood mu : clr.getMultilivelihood()){
					ClearanceRpt rpt = new ClearanceRpt();
					Livelihood liv = mu.getLivelihood();
					try{
						liv = Livelihood.retrieve(" AND live.isactivelive AND live.livelihoodid=" + liv.getId(), new String[0]).get(0);
					}catch(Exception e){}
					
					rpt.setF1(liv.getBusinessName());
					rpt.setF2(liv.getPurokName() + ", " + liv.getBarangay().getName() + ", " + liv.getMunicipality().getName()+ ", " + liv.getProvince().getName());
					//rpt.setF3(com.italia.marxmind.bris.enm.Purpose.BUSINESS.getName());
					reports.add(rpt);
					
				}
			}
			
			String[] ced = null;
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				ced = clr.getCedulaNumber().split("<:>");
			}
			
			
			String word = "";
			word = Words.getTagName("business-permit-municipal-string-1");
			word = word.replace("<requestor>", requestor);
			word = word.replace("<civilstatus>", civilStatus);
			word = word.replace("<requestoraddress>", address);
			str.append(word);
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				word = Words.getTagName("business-permit-municipal-string-2");
				word = word.replace("<ctcno>", ced[0]);
				word = word.replace("<ctcissueddate>", DateUtils.convertDateToMonthDayYear(ced[1]));
				word = word.replace("<ctcissuedaddress>", municipal);
				str.append(word);
			}
			
			String businessIdentity=Words.getTagName("business-permit-municipal-string-3");
			
			if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_CERTIFICATION.getId()== clr.getPurposeType() ||
					com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_FOR_LOAN_REQUIREMENTS.getId()== clr.getPurposeType()){
				businessIdentity = Words.getTagName("business-permit-municipal-string-4");
			}
			
			word = Words.getTagName("business-permit-municipal-string-5");
			word = word.replace("<ownerrep>", businessIdentity);
			str.append(word);
			
			
			
			detail_1 = str.toString();
			str = new StringBuilder();
			if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_NEW.getId()== clr.getPurposeType()){
				purpose = Words.getTagName("business-permit-municipal-string-7");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_RENEWAL.getId()== clr.getPurposeType()){
				purpose = Words.getTagName("business-permit-municipal-string-8");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_CERTIFICATION.getId()== clr.getPurposeType()){
				purpose = Words.getTagName("business-permit-municipal-string-9");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_FOR_LOAN_REQUIREMENTS.getId()== clr.getPurposeType()){
				purpose = Words.getTagName("business-permit-municipal-string-10");
			}else if(com.italia.municipality.lakesebu.enm.Purpose.RETIREMENT_BUSINESS.getId()== clr.getPurposeType()){
				purpose = Words.getTagName("business-permit-municipal-string-11");
			}
			
			word = Words.getTagName("business-permit-municipal-string-12");
			str.append(word);
			
			word = Words.getTagName("business-permit-municipal-string-6");
			word = word.replace("<day>", DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]));
			word = word.replace("<month>", DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])));
			word = word.replace("<year>", clr.getIssuedDate().split("-")[0]);
			word = word.replace("<barangayaddress>", barangay);
			str.append(word);
			
			detail_2 = str.toString();
			
		}else if(com.italia.municipality.lakesebu.enm.Purpose.BUSINESS_PERMIT.getId()== clr.getPurposeType()){ 	
			
			REPORT_NAME = BARANGAY_BUSINESS_PERMIT_NAME;
			
			ClearanceRpt rpt = new ClearanceRpt();
			reports.add(rpt);
			
			String[] notes = clr.getNotes().split("<:>");
			/**
			 * [0] - Control No
			 * [1] - NEW/RENEWAL
			 * [2] - MEMO
			 * [3] - Business Engaged
			 */
			
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
					
				}
			}
			
			param.put("PARAM_BUSINESS_TYPE", notes[3]);
			
			String[] ced = null;
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				ced = clr.getCedulaNumber().split("<:>");
			}
			
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				param.put("PARAM_CTAX", Words.getTagName("business-permit-barangay-string-2") + ced[0] +"/" + DateUtils.convertDateToMonthDayYear(ced[1]));
			}else{
				param.put("PARAM_CTAX","N/A");
			}
			
			param.put("PARAM_DAY", DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]));
			param.put("PARAM_MONTH", DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])) + " " + DateUtils.getCurrentYear());
			
		
		}else{
			//REPORT_NAME = OTHERS_REPORT_NAME;
			
			REPORT_NAME = "";//ASSISTANCE_REPORT_NAME;
			
			ClearanceRpt rpt = new ClearanceRpt();
			reports.add(rpt);
			
			String[] ced = null;
			
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
				ced = clr.getCedulaNumber().split("<:>");
			}
			
			str.append(Words.getTagName("other-string-1") + requestor +Words.getTagName("other-string-2")+ civilStatus + Words.getTagName("other-string-3") + address);
			if(clr.getCedulaNumber()!=null && !clr.getCedulaNumber().isEmpty()){
			str.append(" "+ Words.getTagName("other-string-4") + ced[0] + " "+ Words.getTagName("other-string-5") + DateUtils.convertDateToMonthDayYear(ced[1]) + " "+ Words.getTagName("other-string-6") + municipal +".");
			}else{
				str.append(".");
			}
			String gender = clr.getTaxPayer().getGender().equalsIgnoreCase("1")? "he" : "she";
			String gender1 = clr.getTaxPayer().getGender().equalsIgnoreCase("1")? "his" : "her";
			String words="";
			if(DocTypes.CLEARANCE.getId()==clr.getDocumentType() || DocTypes.CLEARANCE_OPEN_TITLE.getId()==clr.getDocumentType()){
				words = Words.getTagName("other-string-7");
				words = words.replace("he/she", gender);
				words = words.replace("his/her", gender1);
				str.append(words);
				
			}else if(DocTypes.CERTIFICATE.getId()==clr.getDocumentType() || DocTypes.CERTIFICATE_OPEN_TITLE.getId()==clr.getDocumentType()){
				words = Words.getTagName("other-string-13");
				words = words.replace("he/she", gender);
				words = words.replace("his/her", gender1);
				str.append(words);
			
			}else if(DocTypes.INDIGENT.getId()==clr.getDocumentType()){
				words = Words.getTagName("other-string-14");
				words = words.replace("he/she", gender);
				words = words.replace("his/her", gender1);
				str.append(words);
			}
			
			detail_1 = str.toString();
			str = new StringBuilder();
			
			purpose = Words.getTagName("other-string-8") + com.italia.municipality.lakesebu.enm.Purpose.typeName(clr.getPurposeType());
			
			
			
			str.append(Words.getTagName("other-string-9") + DateUtils.dayNaming(clr.getIssuedDate().split("-")[2]) + " " + Words.getTagName("other-string-10") + DateUtils.getMonthName(Integer.valueOf(clr.getIssuedDate().split("-")[1])) + " " + clr.getIssuedDate().split("-")[0] + " " + Words.getTagName("other-string-11"));
			str.append(" " + Words.getTagName("other-string-12") + barangay +".");
			detail_2 = str.toString();
			
		}
		
		//System.out.println("Report type " + REPORT_NAME + " purpose type " + clr.getPurposeType());
		
		//do not move this code this code use for custom title
		if(DocTypes.CERTIFICATE_OPEN_TITLE.getId()==clr.getDocumentType() || 
				DocTypes.CLEARANCE_OPEN_TITLE.getId()==clr.getDocumentType()){
			REPORT_NAME = DOC_OPEN_TITLE_REPORT_NAME;
			param.put("PARAM_DOC_OPEN_TITLE", clr.getCustomTitle().toUpperCase());
			//document validity
			if(clr.getDocumentValidity()>0){
					param.put("PARAM_VALIDITY_NOTE", "Note: This Document is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
			}
		}else if(DocTypes.LOW_INCOME.getId()==clr.getDocumentType()) {
			REPORT_NAME = DOC_OPEN_TITLE_REPORT_NAME;
			param.put("PARAM_DOC_OPEN_TITLE", "BARANGAY CERTIFICATE OF LOW INCOME");
			//document validity
			if(clr.getDocumentValidity()>0){
					param.put("PARAM_VALIDITY_NOTE", "Note: This Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
			}
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
			
			param.put("PARAM_OFFICER_DAY", employee.getFirstName().toUpperCase() + " " + employee.getMiddleName().substring(0,1).toUpperCase() + ". " + employee.getLastName().toUpperCase());
			
			if(employee.getPosition().getId()==1){
				param.put("PARAM_OFFICIAL_TITLE",clr.getEmployee().getPosition().getName());
			}else if(employee.getPosition().getId()==2){
				param.put("PARAM_OFFICIAL_TITLE","Barangay "+clr.getEmployee().getPosition().getName() +"\nOfficer of the day");
			}else{
				param.put("PARAM_OFFICIAL_TITLE","Barangay "+clr.getEmployee().getPosition().getName());
			}
			
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
			
			param.put("PARAM_CLEARANCE_DETAILS", detail_1);
			param.put("PARAM_CLEARANCE_DETAILS2", detail_2);
			param.put("PARAM_PURPOSE", purpose);
			
			//Displayed photo
			boolean isOkToShowPicture = true;
			if(DocTypes.DEATH_CERT.getId()==clr.getDocumentType() || 
					com.italia.municipality.lakesebu.enm.Purpose.BURIAL.getId()==clr.getPurposeType() || 
						DocTypes.LATE_DEATH_CERT.getId()==clr.getDocumentType() || 
							DocTypes.COE.getId()==clr.getDocumentType()){
				isOkToShowPicture = false;
			}
			
			if(isOkToShowPicture){				
				if(clr.getPhotoId()!=null && !clr.getPhotoId().isEmpty()){
					String picture = copyPhoto(clr.getPhotoId()).replace("\\", "/");
					System.out.println("Images: " + picture);
					//InputStream img = this.getClass().getResourceAsStream("/"+clr.getPhotoId()+".jpg");
					File file = new File(picture);
					if(file.exists()){
						try{
						FileInputStream st = new FileInputStream(file);
						param.put("PARAM_PICTURE", st);
						}catch(Exception e){}
					}else{
						picture = copyPhoto(taxpayer.getPhotoid()).replace("\\", "/");
						file = new File(picture);
						try{
							FileInputStream st = new FileInputStream(file);
							param.put("PARAM_PICTURE", st);
						}catch(Exception e){}
					}
				}
			
			}
			
			
			param.put("PARAM_PROVINCE", "Province of " + PROVINCE);
			param.put("PARAM_MUNICIPALITY", "Municipality of " + MUNICIPALITY);
			param.put("PARAM_BARANGAY", "BARANGAY " + BARANGAY.toUpperCase());
			
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
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
				
			}else if(DocTypes.CLEARANCE.getId()==clr.getDocumentType()){
				//certificate
				String certificate = path + "clearance.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Clearance is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
			}else if(DocTypes.DEATH_CERT.getId()==clr.getDocumentType()){
				//certificate
				String certificate = path + "deathcert.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Death Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
			}else if(DocTypes.LATE_DEATH_CERT.getId()==clr.getDocumentType()){
				//certificate
				String certificate = path + "latedeathcert.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Late Death Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}	
			}else if(DocTypes.INDIGENT.getId()==clr.getDocumentType()){
				//certificate
				String certificate = path + "indigency.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Indigency Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}	
				
			}else if(DocTypes.LATE_BIRTH_REG.getId()==clr.getDocumentType()){
				
				//certificate
				String certificate = path + "latebirth.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
			
			}else if(DocTypes.LIVE_BIRTH_REG.getId()==clr.getDocumentType()){
				
				//certificate
				String certificate = path + "livebirth.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}	
				
			}else if(DocTypes.BARANGAY_BUSINESS_PERMIT.getId()==clr.getDocumentType()){
				
				//certificate
				String certificate = path + "businesspermit.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Barangay Business Permit is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
				
			}else if(DocTypes.COE.getId()==clr.getDocumentType()){
				
				//certificate
				String certificate = path + "coe.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This COE is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
				
			}else if(DocTypes.AUTHORIZATION_LETTER.getId()==clr.getDocumentType()){
				
				//authorization
				String authorization = path + "authorization.png";
				try{File file1 = new File(authorization);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This authorization letter is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
			}else if(DocTypes.RESIDENCY.getId()==clr.getDocumentType()){
				//certificate
				String certificate = path + "residency.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Residency Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
			}else if(DocTypes.INCOME.getId()==clr.getDocumentType()){
				//certificate
				String certificate = path + "income.png";
				try{File file1 = new File(certificate);
				FileInputStream off1 = new FileInputStream(file1);
				param.put("PARAM_CERTIFICATE", off1);
				}catch(Exception e){}
				
				//document validity
				if(clr.getDocumentValidity()>0){
						param.put("PARAM_VALIDITY_NOTE", "Note: This Income Certificate is valid for " + (clr.getDocumentValidity()==1? "1 month.": clr.getDocumentValidity()+" months."));
				}
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
			
			
			try{param.put("PARAM_DOCUMENT_NOTE", documentNote(clr));}catch(NullPointerException e) {}
			try{param.put("PARAM_TAGLINE", Words.getTagName("tagline"));}catch(NullPointerException e) {}
			
			Map<Integer, Object> mapObject = Collections.synchronizedMap(new HashMap<Integer, Object>());
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
	
}