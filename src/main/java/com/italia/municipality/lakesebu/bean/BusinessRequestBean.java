package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.controller.BusinessRequest;
import com.italia.municipality.lakesebu.controller.FishCage;
import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.WaterRentalsPayment;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.BusinessRequestType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.BusinessEngaged;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named("bzreq")
@ViewScoped
@Data
public class BusinessRequestBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 356875342234566341L;
	private BusinessRequest businessData;
	private String searchBusinessRequestParam;
	private List<BusinessRequest> bzs;
	private List types;
	
	private boolean enableBusinessName;
	private boolean enableOwner;
	private boolean enableLocaton;
	private boolean enableStopOperationDate;
	
	private List<Livelihood> businessList;
	private String searchBusinessName;
	private Map<Integer, String> mapLineOfBusiness;
	
	private Date dateFrom;
	private Date dateTo;
	
	@PostConstruct
	public void init() {
		
		dateFrom = DateUtils.getDateToday();
		dateTo = dateFrom;
		types = new ArrayList<>();
		for(BusinessRequestType t : BusinessRequestType.values()) {
			types.add(new SelectItem(t.getId(),t.getName()));
		}
		
		
		clear();
		load();
		loadLineBusiness();
	}
	
	private void loadLineBusiness() {
		mapLineOfBusiness = new LinkedHashMap<Integer, String>();
		for(BusinessEngaged line : BusinessEngaged.readBusinessEngagedXML()){
			mapLineOfBusiness.put(line.getId(), line.getName());
		}
	}
	
	public void searchBusiness() {
		String sql = "";
		
		if(getSearchBusinessName()!=null && !getSearchBusinessName().isEmpty()) {
		
			sql += " AND live.isactivelive=1 AND live.livename like '%"+ getSearchBusinessName() +"%' ORDER BY live.livename";
		
		}else {
			sql += " AND live.isactivelive=1 ORDER BY live.livename DESC LIMIT 10";
		}
		businessList = new ArrayList<Livelihood>();
		for(Livelihood lv : Livelihood.retrieve(sql, new String[0])) {
			lv.setTypeLineName(mapLineOfBusiness.get(lv.getTypeLine()));
			businessList.add(lv);
		}
		
	}
	
	public void selectedBusiness(Livelihood lv) {
		BusinessRequest bz = new BusinessRequest();
		bz.setOldBusinessName(lv.getBusinessName());
		BusinessCustomer customer = lv.getTaxPayer();
		String ownerName = customer.getFirstname() + " " + customer.getMiddlename() + " " + customer.getLastname();
		
		//bz.setOldOwner(lv.getTaxPayer().getFullname());
		bz.setOldOwner(ownerName);
		String address = lv.getPurokName().isEmpty()? "" : lv.getPurokName() + ", ";
			   address += lv.getBarangay().getName()+", ";
			   address += lv.getMunicipality().getName() + ", ";
			   address += lv.getProvince().getName();
		bz.setOldAddress(address);
		bz.setDateFilingTmp(new Date());
		bz.setDateStartOperationTmp(DateUtils.convertDateString(lv.getDateRegistered(), "yyyy-MM-dd"));;
		bz.setIsActive(1);
		bz.setType(businessData.getType());
		bz.setGenderReq(Integer.valueOf(lv.getTaxPayer().getGender()));
		bz.setBusinessStart(lv.getDateRegistered());
		bz.setLineOfBusiness(mapLineOfBusiness.get(lv.getTypeLine()));
		businessData = bz;
		updateFlds();
	}
	
	public void clear() {
		businessData = BusinessRequest.builder()
				.dateFilingTmp(new Date())
				.type(BusinessRequestType.RETIREMENT.getId())
				.isActive(1)
				.build();
		
		updateFlds();
		
		
	}
	
	public void save() {
		boolean isOk = true;
		BusinessRequest req = getBusinessData();
		
		if(req.getId()==0) {
			req.setIsActive(1);
			req.setDateFilling(DateUtils.convertDate(req.getDateFilingTmp(), "yyyy-MM-dd"));
		}
		
		if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINES_NAME.getId()) {
			if(req.getNewBusinessName().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new business name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_OWNER.getId()) {
			if(req.getNewOwner().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new owner name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_ADDRESS.getId()) {
			if(req.getNewAddress().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new address name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINESS_NAME_OWNER_ADDRESS.getId()) {
			if(req.getNewBusinessName().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new business name");
				isOk = false;
			}
			if(req.getNewOwner().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new owner name");
				isOk = false;
			}
			if(req.getNewAddress().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new address name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINESS_NAME_OWNER.getId()) {
			if(req.getNewBusinessName().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new business name");
				isOk = false;
			}
			if(req.getNewOwner().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new owner name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINESS_NAME_ADDRESS.getId()) {
			if(req.getNewBusinessName().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new business name");
				isOk = false;
			}
			if(req.getNewAddress().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new address name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_OWNER_ADDRESS.getId()) {
			if(req.getNewOwner().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new owner name");
				isOk = false;
			}
			if(req.getNewAddress().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide new address name");
				isOk = false;
			}
		}else if(getBusinessData().getType()==BusinessRequestType.RETIREMENT.getId()) {
			
			if(req.getOfficer()==null || req.getOfficer().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide Verifier name");
				isOk = false;
			}
			
			if(req.getOfficerPosition()==null || req.getOfficerPosition().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide verifier position");
				isOk = false;
			}
			
			if(req.getBploApprover()==null || req.getBploApprover().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide bplo name of approver");
				isOk = false;
			}
			
			if(req.getBploPosition()==null || req.getBploPosition().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide Inspectorate");
				isOk = false;
			}
			
			if(req.getInspectorate()==null || req.getInspectorate().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide bplo name of approver");
				isOk = false;
			}
			
			if(req.getTreasurer()==null || req.getTreasurer().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide treasuer name");
				isOk = false;
			}
			
			if(req.getMayor()==null || req.getMayor().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide mayor name");
				isOk = false;
			}
			
			if(req.getDateStopOperationTmp()==null) {
				Application.addMessage(3, "Error", "Please select date of business ceased operated");
				isOk = false;
			}else {
				req.setDateStopOperation(DateUtils.convertDate(req.getDateStopOperationTmp(), "yyyy-MM-dd"));
			}
		}
		
		if(req.getDateApprovedTmp()!=null) {
			req.setDateApproved(DateUtils.convertDate(req.getDateApprovedTmp(), "yyyy-MM-dd"));
		}
		
		if(req.getDateStopOperationTmp()!=null) {
			req.setDateStopOperation(DateUtils.convertDate(req.getDateStopOperationTmp(), "yyyy-MM-dd"));
		}
		
		if(req.getDateEffectityTmp()!=null) {
			req.setEffectivityDate(DateUtils.convertDate(req.getDateEffectityTmp(), "yyyy-MM-dd"));
		}
		
		if(req.getDateStartOperationTmp()!=null) {
			req.setBusinessStart(DateUtils.convertDate(req.getDateStartOperationTmp(), "yyyy-MM-dd"));
		}
		
		if(isOk) {
			req.save();
			Application.addMessage(1, "Success", "Successfully saved.");
			load();
		}
		
	}
	
	public void updateFlds() {
		if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINES_NAME.getId()) {
			setEnableBusinessName(false);
			setEnableOwner(true);
			setEnableLocaton(true);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_OWNER.getId()) {
			setEnableBusinessName(true);
			setEnableOwner(false);
			setEnableLocaton(true);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_ADDRESS.getId()) {
			setEnableBusinessName(true);
			setEnableOwner(true);
			setEnableLocaton(false);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINESS_NAME_OWNER_ADDRESS.getId()) {
			setEnableBusinessName(false);
			setEnableOwner(false);
			setEnableLocaton(false);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINESS_NAME_OWNER.getId()) {
			setEnableBusinessName(false);
			setEnableOwner(false);
			setEnableLocaton(true);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_BUSINESS_NAME_ADDRESS.getId()) {
			setEnableBusinessName(false);
			setEnableOwner(true);
			setEnableLocaton(false);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.CHANGE_OWNER_ADDRESS.getId()) {
			setEnableBusinessName(true);
			setEnableOwner(false);
			setEnableLocaton(false);
			setEnableStopOperationDate(true);
		}else if(getBusinessData().getType()==BusinessRequestType.RETIREMENT.getId()) {
			setEnableBusinessName(true);
			setEnableOwner(true);
			setEnableLocaton(true);
			setEnableStopOperationDate(false);
			getBusinessData().setTreasurer(Words.getTagName("treasurer-name").toUpperCase());
			getBusinessData().setMayor(Words.getTagName("mayor").toUpperCase());
			getBusinessData().setBploApprover(Words.getTagName("oic").toUpperCase());
			getBusinessData().setBploPosition(Words.getTagName("license-officer-pos").toUpperCase());
			getBusinessData().setInspectorate(Words.getTagName("inspectorate-business-closure").toUpperCase());
			getBusinessData().setOfficer(Words.getTagName("verifier-name").toUpperCase());
			getBusinessData().setOfficerPosition(Words.getTagName("verifier-position").toUpperCase());
		}				
	}
	
	public void selectedReq(BusinessRequest req) {
		req.setDateFilingTmp(DateUtils.convertDateString(req.getDateFilling(), "yyyy-MM-dd"));
		if(req.getDateApproved()!=null) {
			req.setDateApprovedTmp(DateUtils.convertDateString(req.getDateFilling(), "yyyy-MM-dd"));
		}
		if(req.getDateApproved()!=null) {
			req.setDateApprovedTmp(DateUtils.convertDateString(req.getDateApproved(), "yyyy-MM-dd"));
		}
		if(req.getDateStopOperation()!=null) {
			req.setDateStopOperationTmp(DateUtils.convertDateString(req.getDateStopOperation(), "yyyy-MM-dd"));
		}
		if(req.getEffectivityDate()!=null) {
			req.setDateEffectityTmp(DateUtils.convertDateString(req.getEffectivityDate(), "yyyy-MM-dd"));
		}
		if(req.getDateStopOperation()!=null) {
			req.setDateStartOperationTmp(DateUtils.convertDateString(req.getDateStopOperation(), "yyyy-MM-dd"));
		}
		if(req.getBusinessStart()!=null){
			req.setDateStartOperationTmp(DateUtils.convertDateString(req.getBusinessStart(), "yyyy-MM-dd"));
		}
		
		
		setBusinessData(req);
		updateFlds();
	}
	
	public void deleteReq(BusinessRequest req) {
		req.delete();
		Application.addMessage(1, "Success", "Successfully saved.");
		load();
	}
	
	
	
	public void load() {
		bzs = new ArrayList<BusinessRequest>();
		
		String sql = "";
		
		String dateF = DateUtils.convertDate(getDateFrom(), "yyyy-MM-dd");
		String dateT = DateUtils.convertDate(getDateTo(), "yyyy-MM-dd");
		
		
		
		if(getSearchBusinessRequestParam()!=null && !getSearchBusinessRequestParam().isEmpty()) {
			sql += " AND st.oldbizname like '%"+ getSearchBusinessRequestParam() +"%'";
			sql += " OR st.newbizname like '%"+ getSearchBusinessRequestParam() +"%'";
			sql += " OR st.oldownername like '%"+ getSearchBusinessRequestParam() +"%'";
			sql += " OR st.newownername like '%"+ getSearchBusinessRequestParam() +"%'";
		}
		
		if(!dateF.equalsIgnoreCase(dateT)) {
			sql += " AND (st.datefiling>='"+ dateF +"' AND st.datefiling<='"+ dateT +"')";
		}else {
			sql += " ORDER BY reqid DESC LIMIT 20";
		}
		
		bzs = BusinessRequest.retrieve(sql, new String[0]);
	}
	
	public void printCert(BusinessRequest req, String type) {
		String path = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String reportName = GlobalVar.BUSINESS_CLOSURER_CERT;
		
		if("cert".equalsIgnoreCase(type)) {
			reportName = GlobalVar.BUSINESS_CLOSURER_CERT;
		}else if("app".equalsIgnoreCase(type)) {
			reportName = GlobalVar.BUSINESS_APPLIACTION_CLOSURE;
		}else if("letter".equalsIgnoreCase(type)) {
			reportName = GlobalVar.BUSINESS_APPLIACTION_CLOSURE_LETTER;
		}else if("loc".equalsIgnoreCase(type)) {
			reportName = GlobalVar.BUSINESS_CLOSURE_LOCATION;
		}
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(reportName, reportName, path);
		List<Reports> p = new ArrayList<Reports>();
		p.add(new Reports());
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(p);
		
		HashMap param = new HashMap();
		
		
		if("cert".equalsIgnoreCase(type)) {
		
		
		param.put("PARAM_TITLE", "C E R T I F I C A T I O N");
		String content = Words.getTagName("business-closure-certification");
		
		String[] issued = new String[0];
		if(req.getDateApproved()!=null && !req.getDateApproved().isEmpty()) {
			issued = req.getDateApproved().split("-");
		}else {
			issued = req.getDateFilling().split("-");
		}
		
		
		content = content.replace("<business>", req.getOldBusinessName().toUpperCase());
		content = content.replace("<owner>", req.getOldOwner().toUpperCase());
		content = content.replace("<location>", req.getOldAddress());
		
		if(BusinessRequestType.RETIREMENT.getId()==req.getType()) {
			content = content.replace("<mode>", "terminated");
			content = content.replace("<change>", "operation");
		}else if(BusinessRequestType.CHANGE_BUSINES_NAME.getId()==req.getType()) {
			content = content.replace("<mode>", "change");
			content = content.replace("<change>", "name to " + req.getNewBusinessName().toUpperCase());
		}else if(BusinessRequestType.CHANGE_OWNER.getId()==req.getType()) {
			content = content.replace("<mode>", "change");
			content = content.replace("<change>", "owner to " + req.getNewOwner().toUpperCase());
		}else if(BusinessRequestType.CHANGE_ADDRESS.getId()==req.getType()) {
			content = content.replace("<mode>", "change");
			content = content.replace("<change>", "location to " + req.getNewAddress().toUpperCase());
		}else if(BusinessRequestType.CHANGE_BUSINESS_NAME_ADDRESS.getId()==req.getType()) {
			content = content.replace("<mode>", "change");
			content = content.replace("<change>", "name and location to " + req.getNewBusinessName().toLowerCase() + " located at new location " + req.getNewAddress().toUpperCase());
		}else if(BusinessRequestType.CHANGE_BUSINESS_NAME_OWNER.getId()==req.getType()) {
			content = content.replace("<mode>", "change");
			content = content.replace("<change>", "name and owner to " + req.getNewBusinessName().toUpperCase() + " with a new owner name " + req.getNewOwner().toUpperCase());
		}else if(BusinessRequestType.CHANGE_BUSINESS_NAME_OWNER_ADDRESS.getId()==req.getType()) {
			content = content.replace("<mode>", "change");
			content = content.replace("<change>", "name, owner and location to " + req.getNewBusinessName().toUpperCase() + " with a new owner name " + req.getNewOwner().toUpperCase() + " located at new location " + req.getNewAddress().toUpperCase());
		}
		
		if(req.getEffectivityDate()!=null) {
			content = content.replace("<effectivitydate>",DateUtils.convertDateToMonthDayYear(req.getEffectivityDate()));
		}else {
			content = content.replace("<effectivitydate>","<Not Yet Specified>");
		}
		content = content.replace("<hisher>", req.getGenderReq()==1? "his" : "her");
		content = content.replace("<himher>", req.getGenderReq()==1? "him" : "her");
		content = content.replace("<day>", DateUtils.dayNaming(issued[2]));
		content = content.replace("<month>", DateUtils.getMonthName(Integer.valueOf(issued[1])));
		content = content.replace("<year>", issued[0]);
		
		
		param.put("PARAM_CONTENT", content);
		param.put("PARAM_TREASURER", req.getTreasurer().toUpperCase());
		DocumentFormatter doc = new DocumentFormatter();
		param.put("PARAM_TREASURER_POS", doc.getTagName("treasurer-position"));
		
		}else if("app".equalsIgnoreCase(type)) {
			String content = Words.getTagName("business-application-closure");
			content = content.replace("<startoperation>", DateUtils.convertDateToMonthDayYear(req.getBusinessStart()));
			param.put("PARAM_DATE", DateUtils.convertDateToMonthDayYear(req.getDateFilling()));
			param.put("PARAM_MAYOR", req.getMayor().toUpperCase());
			param.put("PARAM_BUSINESS_NAME", req.getOldBusinessName().toUpperCase());
			param.put("PARAM_OWNER", req.getOldOwner().toUpperCase());
			param.put("PARAM_LOCATION", req.getOldAddress().toUpperCase());
			param.put("PARAM_LINE_BUSINESS", req.getLineOfBusiness().toUpperCase());
			param.put("PARAM_REASON", req.getReason());
			
			param.put("PARAM_VERIFIER", req.getOfficer().toUpperCase());
			param.put("PARAM_VERIFIER_POS", req.getOfficerPosition());
			
			param.put("PARAM_BPLO", req.getBploApprover().toUpperCase());
			param.put("PARAM_BPLO_POS", req.getBploPosition());
			DocumentFormatter doc = new DocumentFormatter();
			param.put("PARAM_TREASURER", req.getTreasurer().toUpperCase());
			param.put("PARAM_TREASURER_POS", doc.getTagName("treasurer-position"));
			param.put("PARAM_INSPECTOR", req.getInspectorate().toUpperCase());
			param.put("PARAM_CONTENT", content);
		}else if("letter".equalsIgnoreCase(type)) {
			
			param.put("PARAM_DATE", DateUtils.convertDateToMonthDayYear(req.getDateFilling()));
			param.put("PARAM_MAYOR", req.getMayor().toUpperCase());
			
			String content = Words.getTagName("business-application-closure-letter");
			content = content.replace("<businessname>", req.getOldBusinessName().toUpperCase());
			content = content.replace("<location>", req.getOldAddress());
			content = content.replace("<ceased>", DateUtils.convertDateToMonthDayYear(req.getDateStopOperation()));
			content = content.replace("<reason>", req.getReason());
			param.put("PARAM_CONTENT", content);
		}else if("loc".equalsIgnoreCase(type)) {
			DocumentFormatter doc = new DocumentFormatter();
			param.put("PARAM_DATE", DateUtils.convertDateToMonthDayYear(req.getDateFilling()));
			param.put("PARAM_MAYOR", req.getMayor().toUpperCase());
			param.put("PARAM_TREASURER", req.getTreasurer().toUpperCase());
			param.put("PARAM_TREASURER_POS", doc.getTagName("treasurer-position").toUpperCase());
			param.put("PARAM_BPLO", req.getBploApprover().toUpperCase());
			param.put("PARAM_BPLO_POS", req.getBploPosition());
			param.put("PARAM_INSPECTOR", req.getInspectorate().toUpperCase());
			
			param.put("PARAM_BUSINESS_NAME", req.getOldBusinessName().toUpperCase());
			param.put("PARAM_OWNER", req.getOldOwner().toUpperCase());
			param.put("PARAM_LOCATION", req.getOldAddress().toUpperCase());
			param.put("PARAM_LINE_BUSINESS", req.getLineOfBusiness().toUpperCase());
		}	
		
		
		//logo
		//String logo = path + "logo.png";
		//try{File file = new File(logo);
		//FileInputStream loff = new FileInputStream(file);
		//param.put("PARAM_LOGO_LAKESEBU", loff);
		//}catch(Exception e){}

		//logo
		String officialLogo = path + "logotrans.png";
		try{File file = new File(officialLogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_SEALTRANSPARENT", off);
		}catch(Exception e){}
		
		String backlogo = path + "documentbg-gen.png";
		try{File file = new File(backlogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_BACKGROUND", off);
		}catch(Exception e){}
		
		try{
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ reportName +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
		
				try{
					System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + reportName);
		  		 File file = new File(path, reportName + ".pdf");
				 FacesContext faces = FacesContext.getCurrentInstance();
				 ExternalContext context = faces.getExternalContext();
				 HttpServletResponse response = (HttpServletResponse)context.getResponse();
					
			     BufferedInputStream input = null;
			     BufferedOutputStream output = null;
			     
			     try{
			    	 
			    	 // Open file.
			            input = new BufferedInputStream(new FileInputStream(file), GlobalVar.DEFAULT_BUFFER_SIZE);

			            // Init servlet response.
			            response.reset();
			            response.setHeader("Content-Type", "application/pdf");
			            response.setHeader("Content-Length", String.valueOf(file.length()));
			            response.setHeader("Content-Disposition", "inline; filename=\"" + reportName + ".pdf" + "\"");
			            output = new BufferedOutputStream(response.getOutputStream(), GlobalVar.DEFAULT_BUFFER_SIZE);

			            // Write file contents to response.
			            byte[] buffer = new byte[GlobalVar.DEFAULT_BUFFER_SIZE];
			            int length;
			            while ((length = input.read(buffer)) > 0) {
			                output.write(buffer, 0, length);
			            }

			            // Finalize task.
			            output.flush();
			    	 
			     }finally{
			    	// Gently close streams.
			            close(output);
			            close(input);
			     }
			     
			     // Inform JSF that it doesn't need to handle response.
			        // This is very important, otherwise you will get the following exception in the logs:
			        // java.lang.IllegalStateException: Cannot forward after response has been committed.
			        faces.responseComplete();
			        
				}catch(Exception ioe){
					ioe.printStackTrace();
				}
		
	}
	
	
	private void close(Closeable resource) {
	    if (resource != null) {
	        try {
	            resource.close();
	        } catch (IOException e) {
	            // Do your thing with the exception. Print it, log it or mail it. It may be useful to 
	            // know that this will generally only be thrown when the client aborted the download.
	            e.printStackTrace();
	        }
	    }
	}
}
