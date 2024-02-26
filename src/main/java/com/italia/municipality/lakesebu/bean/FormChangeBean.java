package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.CheckIssued;
import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Form11Report;
import com.italia.municipality.lakesebu.controller.IssuedForm;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.RCDAllController;
import com.italia.municipality.lakesebu.controller.RCDSummary;
import com.italia.municipality.lakesebu.controller.RCDSummaryController;
import com.italia.municipality.lakesebu.controller.Stocks;
import com.italia.municipality.lakesebu.enm.AccessLevel;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormORTypes;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.xml.RCDFormDetails;
import com.italia.municipality.lakesebu.xml.RCDFormSeries;
import com.italia.municipality.lakesebu.xml.RCDReader;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Form;
import lombok.Data;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named("formChange")
@ViewScoped
@Data
public class FormChangeBean implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 789854534341L;
	private String reportDate;
	private String collectorName;
	private String reportSeriesNo;
	private String fundName;
	private String total;
	private List<RCDFormDetails> formsData;
	private List<RCDFormSeries> formSeriesData;
	private RCDReader xmlData;
	private CollectionInfo collectionData;
	private String fileName;
	private List<CollectionInfo> colData;
	private List<IssuedForm> issuedData;
	private List<Stocks> stocksData;
	private List<RCDSummary> summaryData;
	
	private List collectors;
	private List forms;
	private List statusList;
	private List fundList;
	
	private Map<Integer,CollectionInfo> collectionDataRemove;
	
	@PostConstruct
	public void init() {
		System.out.println("start......");
		Login in = Login.getUserLogin();
		if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() ||
				AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
			loadCollector();
			loadForms();
			loadStatusList();
			loadFundList();
		}
	}
	
	private void loadCollector() {
		collectors = new ArrayList<>();
		for(Collector c : Collector.retrieve("", new String[0])) {
			collectors.add(new SelectItem(c.getId(), c.getName()));
		}
	}
	
	private void loadStatusList() {
		statusList = new ArrayList<>();
		for(FormStatus f : FormStatus.values()) {
			statusList.add(new SelectItem(f.getId(), f.getName()));
		}
	}
	
	private void loadForms() {
		forms = new ArrayList<>();
		for(FormType f : FormType.values()) {
			forms.add(new SelectItem(f.getId(), f.getName()));
		}
	}
	
	private void loadFundList() {
		fundList = new ArrayList<>();
		for(FundType f : FundType.values()){
			fundList.add(new SelectItem(f.getId(), f.getName()));
		}
	}
	
	public void save(CollectionInfo info) {
		info  = CollectionInfo.save(info);
		loadFormDetails(info);
		Application.addMessage(1, "Success", "Successfully saved.");
	}
	
	private Stocks stockItem(long stockId) {
		Stocks stock = null;
		for(Stocks s : Stocks.retrieve(stockId)) {
			s.setCollectors(getCollectors());
			s.setForms(getForms());
			s.setStatusList(getStatusList());
			stock = s;
		}
		return stock;
	}
	
	private IssuedForm issuedItem(IssuedForm form) {
		
		form.setFundList(getFundList());
		
		return form;
	}
	
	public void reloadFormDetails() {
		loadFormDetails(getCollectionData());
	}
	
	public void loadFormDetails(CollectionInfo info) {
		System.out.println("................loadFormDetails");
		setCollectionData(info);
		
		summaryData = new ArrayList<RCDSummary>();
		summaryData = RCDSummary.retrieve(info.getCollector().getId()+"", info.getId()+"");
		
		colData = new ArrayList<CollectionInfo>();
		issuedData = new ArrayList<IssuedForm>();
		stocksData = new ArrayList<Stocks>();
		String sql = " AND frm.fundid=" + info.getFundId();
			   sql += " AND frm.isid=" + info.getCollector().getId();
			   sql += " AND frm.rptgroup=" + info.getRptGroup();
			   sql += " AND year(frm.receiveddate)='" + info.getReceivedDate().split("-")[0]+"'";
			   //sql += " AND frm.isactivecol=1";
		//colData = CollectionInfo.retrieve(sql, new String[0]);
	    Map<String, CollectionInfo> mapCol = new LinkedHashMap<String, CollectionInfo>();
		for(CollectionInfo col : CollectionInfo.retrieve(sql, new String[0]) ) {
			if("ALL ISSUED".equalsIgnoreCase(col.getStatusName())) {
				mapCol.put(correctingDigitOfSeries(col.getBeginningNo(),col.getFormType()), info);
			}
			
			
			int available = 0;
			if(FormType.AF_51.getId()==col.getFormType() || FormType.AF_52.getId()==col.getFormType() || FormType.AF_53.getId()==col.getFormType() || FormType.AF_54.getId()==col.getFormType() || FormType.AF_56.getId()==col.getFormType()
					|| FormType.CTC_INDIVIDUAL.getId()==col.getFormType() || FormType.CTC_CORPORATION.getId()==col.getFormType()) {
				available=(col.getPrevPcs() - col.getPcs()) + 1;
				
			}else if(FormType.CT_2.getId()==col.getFormType()) {
				long avail =(col.getEndingNo() - col.getPrevPcs());
				avail /=2;
				available = Integer.valueOf(avail+"");
				
			}else if(FormType.CT_5.getId()==col.getFormType()) {
				long avail =(col.getEndingNo() - col.getPrevPcs());
				avail /=5;
				available = Integer.valueOf(avail+"");
			}
			
			//include the issued qty
			//available += col.getPcs();
			
			col.setAvailable(available);			
			colData.add(col);
			
			issuedData.add(issuedItem(col.getIssuedForm()));
			
			try{stocksData.add(stockItem(col.getIssuedForm().getStock().getId()));}catch(IndexOutOfBoundsException io) {}
		}
		
		//PrimeFaces.current().ajax().update("listFormData");
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String collector = info.getCollector().getName();
		
		String value = "";
		String len = info.getRptGroup()+"";
		int size = len.length();
		if(size==1) {
			String[] date = info.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-#00"+len;
		}else if(size==2) {
			String[] date = info.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-#0"+len;
		}else if(size==3) {
			String[] date = info.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-#"+len;
		}
  		
		String xmlFolder = REPORT_PATH + "xml" + File.separator;  
		xmlFolder += collector +"-"+value +"_"+info.getFundName() +".xml";
		fileName = collector +"-"+value +"_"+info.getFundName();
		System.out.println("Collectorn Selected: " + collector);
  		RCDReader xml = RCDReader.readXML(xmlFolder,false);
  		setXmlData(xml);
  		collectorName = xml.getAccountablePerson().replace("-", "/").toUpperCase();
  		reportSeriesNo = xml.getSeriesReport().replace("#", "");
  		fundName = xml.getFund();
  		total = xml.getAddAmount();
  		int cnt = 1;
  		formsData = new ArrayList<RCDFormDetails>();
  		for(RCDFormDetails d : xml.getRcdFormDtls()) {
  			if(mapCol!=null && mapCol.size()>0 && mapCol.containsKey(d.getSeriesFrom())) {
  				d.setStyle("color:red");
  			}
  			formsData.add(d);
			cnt++;
  		}
  		for(int i = cnt; i<=13; i++) {
  			RCDFormDetails rcd = RCDFormDetails.builder()
  					.formId("")
  					.name("")
  					.seriesFrom("")
  					.seriesTo("")
  					.amount("")
  					.build();
  			formsData.add(rcd);
  		}
		
  		cnt = 1;
  		formSeriesData = new ArrayList<RCDFormSeries>();
  		for(RCDFormSeries frm : xml.getRcdFormSeries()) {
  			formSeriesData.add(frm);
	  		cnt++;
		}
  		for(int i = cnt; i<=13; i++) {
  			RCDFormSeries rc = RCDFormSeries.builder()
  					.name("")
  					.beginningQty("")
  					.beginningFrom("")
  					.beginningTo("")
  					.receiptQty("")
  					.receiptFrom("")
  					.receiptTo("")
  					.issuedQty("")
  					.issuedFrom("")
  					.issuedTo("")
  					.endingQty("")
  					.endingFrom("")
  					.endingTo("")
  					.build();
  			formSeriesData.add(rc);
  		}
  		
  		
  		
  		PrimeFaces pf = PrimeFaces.current();
  		pf.executeScript("PF('dlgFormChange').show(1000);buttonClick();");
	}
	
	public void saveDataXml() {
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();  
		String value = "";
		CollectionInfo in = getCollectionData();
		
		String len = in.getRptGroup()+"";
		int size = len.length();
		if(size==1) {
			String[] date = in.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-#00"+len;
		}else if(size==2) {
			String[] date = in.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-#0"+len;
		}else if(size==3) {
			String[] date = in.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-#"+len;
		}
		RCDReader xml = getXmlData();
		//String fileName = xml.getAccountablePerson().replace("/", "-")+"-"+value+"_"+getFundName()+"-Testing";
		//xml.setAccountablePerson(getCollectorName());
  		xml.setSeriesReport(getReportSeriesNo());
  		xml.setFund(getFundName());
  		xml.setAddAmount(getTotal());
  		xml.setRcdFormDtls(getFormsData());
  		xml.setRcdFormSeries(getFormSeriesData());
  		xml.setLessAmount(getTotal());
  		RCDReader.saveXML(xml, getFileName(), XML_FOLDER,false);
  		Application.addMessage(1, "Success", "Successfully saved");
	}
	
	public void onSummary(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        String column =  event.getColumn().getHeaderText();
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        System.out.println("Row Index   "+ event.getRowIndex());
        if(oldValue.equals(newValue)) {
        	//no changes
        }else {
        	if("Date".equalsIgnoreCase(column)){
        		getSummaryData().get(index).setDateTrans(newValue.toString());
        		objectSummary(getSummaryData().get(index));
        	}else if("Control Number".equalsIgnoreCase(column)){
        		getSummaryData().get(index).setControlNumber(newValue.toString());;
        		objectSummary(getSummaryData().get(index));
        	}else if("Col Ids".equalsIgnoreCase(column)){
        		getSummaryData().get(index).setColIds(newValue.toString());
        		objectSummary(getSummaryData().get(index));
        	}else if("Series".equalsIgnoreCase(column)){
        		getSummaryData().get(index).setIndividualSeries(newValue.toString());;
        		objectSummary(getSummaryData().get(index));
        	}else if("Amount".equalsIgnoreCase(column)){
        		double amount = Double.valueOf(newValue.toString());
        		getSummaryData().get(index).setAmount(amount);
        		objectSummary(getSummaryData().get(index));
        	}else if("CollectorId".equalsIgnoreCase(column)){
        		getSummaryData().get(index).setCollectorIDs(newValue.toString());
        		objectSummary(getSummaryData().get(index));
        	}
        }
	}
	
	private void objectSummary(RCDSummary rcd) {
		Object obj = rcd.getObjectData();
		if(obj instanceof RCDAllController) {
			RCDAllController all = (RCDAllController)obj;
					all.setId(rcd.getId());
					all.setDateTrans(rcd.getDateTrans());
					all.setControlNumber(rcd.getControlNumber());
					all.setCollectionIds(rcd.getColIds());
					all.setIsActive(rcd.getIsActive());
					all.setIndividualSeries(rcd.getIndividualSeries());
					all.setAmount(rcd.getAmount());
					all.setIsids(rcd.getCollectorIDs());
					all.save();
					Application.addMessage(1, "Success", "Successfully saved.");
		}else {
			RCDSummaryController sum = (RCDSummaryController)obj;
					sum.setId(rcd.getId());
					sum.setDateTrans(rcd.getDateTrans());
					sum.setControlNumber(rcd.getControlNumber());
					sum.setCollectionIds(rcd.getColIds());
					sum.setIsActive(rcd.getIsActive());
					sum.setIndividualSeries(rcd.getIndividualSeries());
					sum.setAmount(rcd.getAmount());
					sum.setIsids(rcd.getCollectorIDs());
					sum.save();
					Application.addMessage(1, "Success", "Successfully saved.");
		}
	}
	
	public void onCellEdit(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        System.out.println("Row Index   "+ event.getRowIndex());
        
	}  
	public void onCellEditSeries(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        System.out.println("Row Index   "+ event.getRowIndex());
	}  
	
	public void recalculateAmount() {
		//recalculate amount
		double total = 0d;
		for(CollectionInfo in : getColData()) {
			total += in.getAmount();
		}
		setTotal(Currency.formatAmount(total));
	}
	
	public void onStockInfo(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        String column =  event.getColumn().getHeaderText();
        if(oldValue.equals(newValue)) {
        	//no changes
        }else {
	        if("Collector ID".equalsIgnoreCase(column)) {
	        	int isId = getStocksData().get(index).getCollector().getId();
	        	getStocksData().get(index).setCollector(Collector.builder().id(isId).build());
	        	long stockId = getStocksData().get(index).getId();
	        	Stocks.openUpdate("UPDATE stockreceipt SET isid=" + isId +" WHERE stockid=" + stockId, new String[0]);
	        	Stocks.openUpdate("UPDATE logissuedform SET isid=" + isId +" WHERE stockid=" + stockId, new String[0]);
	        	Stocks.openUpdate("UPDATE collectioninfo SET isid=" + isId +" WHERE logid IN(SELECT logid FROM logissuedform WHERE stockid="+stockId+")", new String[0]);
	        	
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Date".equalsIgnoreCase(column)) {
	        	getStocksData().get(index).setDateTrans(newValue.toString());
	        	getStocksData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Stab No".equalsIgnoreCase(column)) {
	        	int stabNo = Integer.valueOf(newValue.toString());
	        	getStocksData().get(index).setStabNo(stabNo);
	        	
	        	long stockId = getStocksData().get(index).getId();
	        	Stocks.openUpdate("UPDATE stockreceipt SET stabno=" + stabNo +" WHERE stockid=" + stockId, new String[0]);
	        	Stocks.openUpdate("UPDATE logissuedform SET stabno=" + stabNo +" WHERE stockid=" + stockId, new String[0]);
	        	
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Form".equalsIgnoreCase(column)) {
	        	int formId = getStocksData().get(index).getFormType();
	        	getStocksData().get(index).setFormType(formId);
	        	
	        	long stockId = getStocksData().get(index).getId();
	        	Stocks.openUpdate("UPDATE stockreceipt SET formType=" + formId +" WHERE stockid=" + stockId, new String[0]);
	        	Stocks.openUpdate("UPDATE logissuedform SET formtypelog=" + formId +" WHERE stockid=" + stockId, new String[0]);
	        	Stocks.openUpdate("UPDATE collectioninfo SET formtypecol=" + formId +" WHERE logid IN(SELECT logid FROM logissuedform WHERE stockid="+stockId+")", new String[0]);
	        	
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Pieces".equalsIgnoreCase(column)) {
	        	int qty = Integer.valueOf(newValue.toString());
	        	getStocksData().get(index).setQuantity(qty);
	        	
	        	long stockId = getStocksData().get(index).getId();
	        	Stocks.openUpdate("UPDATE stockreceipt SET qty=" + qty +" WHERE stockid=" + stockId, new String[0]);
	        	Stocks.openUpdate("UPDATE logissuedform SET logpcs=" + qty +" WHERE stockid=" + stockId, new String[0]);
	        		        	
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("From".equalsIgnoreCase(column)) {
	        	//getStocksData().get(index).setSeriesFrom(newValue.toString());
	        	//getStocksData().get(index).save();
	        	//Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("To".equalsIgnoreCase(column)) {
	        	//getStocksData().get(index).setSeriesTo(newValue.toString());
	        	//getStocksData().get(index).save();
	        	//Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Deleted".equalsIgnoreCase(column)) {
	        	int del = Integer.valueOf(newValue.toString());
	        	getStocksData().get(index).setIsActive(del);
	        	
	        	if(del==0 || del==1) {
		        	long stockId = getStocksData().get(index).getId();
		        	Stocks.openUpdate("UPDATE stockreceipt SET isactivestock=" + del +" WHERE stockid=" + stockId, new String[0]);
		        	Stocks.openUpdate("UPDATE logissuedform SET isactivelog=" + del +" WHERE stockid=" + stockId, new String[0]);
		        	Stocks.openUpdate("UPDATE collectioninfo SET isactivecol=" + del +" WHERE logid IN(SELECT logid FROM logissuedform WHERE stockid="+stockId+")", new String[0]);
		        	Application.addMessage(1, "Success", "Successfully saved.");
	        	}else {
	        		Application.addMessage(2, "Error", "0 for Deletion 1 for active");
	        	}
	        }else if("Status".equalsIgnoreCase(column)) {
	        	int stat = Integer.valueOf(newValue.toString());
	        	getStocksData().get(index).setStatus(stat);
	        	getStocksData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }
	        reloadFormDetails();
        }
        
	} 
	
	public void onIssuedInfo(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        String column =  event.getColumn().getHeaderText();
        
        if(oldValue.equals(newValue)) {
        	//no changes
        }else {
        
	        if("Stock Id".equalsIgnoreCase(column)) {
	        	int id = Integer.valueOf(newValue.toString());
	        	getIssuedData().get(index).setStock(Stocks.builder().id(id).build());
	        	getIssuedData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Date".equalsIgnoreCase(column)) {
	        	getIssuedData().get(index).setIssuedDate(newValue.toString());
	        	getIssuedData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Form".equalsIgnoreCase(column)) {
	        	int id = Integer.valueOf(newValue.toString());
	        	getIssuedData().get(index).setFormType(id);
	        	getIssuedData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Pieces".equalsIgnoreCase(column)) {
	        	int qty = Integer.valueOf(newValue.toString());
	        	getIssuedData().get(index).setPcs(qty);
	        	getIssuedData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Beginning".equalsIgnoreCase(column)) {
	        	//long beg = Long.valueOf(newValue.toString());
	        	//getIssuedData().get(index).setBeginningNo(beg);
	        	//getIssuedData().get(index).save();
	        	//Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Ending".equalsIgnoreCase(column)) {
	        	//long end = Long.valueOf(newValue.toString());
	        	//getIssuedData().get(index).setEndingNo(end);
	        	//getIssuedData().get(index).save();
	        	//Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Deleted".equalsIgnoreCase(column)) {
	        	//int del = Integer.valueOf(newValue.toString());
	        	//getIssuedData().get(index).setIsActive(del);
	        	//getIssuedData().get(index).save();
	        	//Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Status".equalsIgnoreCase(column)) {
	        	//int stat = getIssuedData().get(index).getStatus();
	        	//getIssuedData().get(index).setStatus(stat);
	        	getIssuedData().get(index).save();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Fund".equalsIgnoreCase(column)) {
	        	int fundId = getIssuedData().get(index).getFundId();
	        	//getIssuedData().get(index).setFundId(id);
	        	//getIssuedData().get(index).save();
	        	long logId = getIssuedData().get(index).getId();
	        	Stocks.openUpdate("UPDATE logissuedform SET fundid=" + fundId +" WHERE isactivelog=1 AND logid=" + logId, new String[0]);
	        	Stocks.openUpdate("UPDATE collectioninfo SET fundid=" + fundId +" WHERE isactivecol=1 AND logid="+logId, new String[0]);
	        	
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        }	
	        reloadFormDetails();
        }
	}    
	
	public void deleteCollectionInfo(String type) {
		if(getCollectionDataRemove()!=null && getCollectionDataRemove().size()>0) {
			if("DELETE".equalsIgnoreCase(type)) {
				/*
				for(int index : getCollectionDataRemove().keySet()) {
					getColData().get(index).setIsActive(0);	
					getColData().get(index).save();
				}*/
				Application.addMessage(1, "Success", "Successfully deleted.");
			}else {
				for(int index : getCollectionDataRemove().keySet()) {
					getColData().get(index).setIsActive(1);	
					getColData().get(index).save();
				}
				Application.addMessage(1, "Success", "Successfully active.");
			}
			
		}else {
			Application.addMessage(3, "Error", "No item has been selected. Please select the item for deletion");
		}
	}
	
	public void onCollectionInfo(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        int formType = getColData().get(index).getFormType();
        String column =  event.getColumn().getHeaderText();
        System.out.println("Column: " + column + " index:"+ index + " old:"+ oldValue + " new:" + newValue);
        if(oldValue.equals(newValue)) {
        	//no changes
        }else {
        
	        if("Date".equalsIgnoreCase(column)) {
		        getColData().get(index).save();
		        Application.addMessage(1, "Success", "Successfully saved.");
		    }else if("Deleted".equalsIgnoreCase(column)) {
		    	int val = Integer.valueOf(newValue.toString());
		    	if(val==1) {
			    	getColData().get(index).setIsActive(val);	
		        	getColData().get(index).save();
		        	Application.addMessage(1, "Success", "Successfully saved.");
		    	}else if(val==0) {
		    		PrimeFaces pf = PrimeFaces.current();
		    		pf.executeScript("PF('dlgConfirmRemoving').show(1000)");
		    		setCollectionDataRemove(new LinkedHashMap<Integer, CollectionInfo>());
		    		getCollectionDataRemove().put(index, getColData().get(index));
		    	}else {
		    		Application.addMessage(3, "Error", "Only 0-deleted, 1-active is acceptable.");
		    	}
	        }else if("Amount".equalsIgnoreCase(column)) { 
	        	if(FormType.CT_2.getId()==formType || FormType.CT_5.getId()==formType) {
	        		//editing is not allowed for the amount. Because Cash Ticket is based on number of ticket
	        		Application.addMessage(2, "Error", "Editing for amount is not allowed. Ticket amount is based on quantity declared");
	        	}else {
	        		double amount = Double.valueOf(newValue.toString());
	        		getColData().get(index).setAmount(amount);
	        		getColData().get(index).save();
	        		Application.addMessage(1, "Success", "Successfully saved.");
	        		
	        		recalculateAmount();
	        		
	        	}
	        }else if("Group".equalsIgnoreCase(column)) { 
	        	int group = Integer.valueOf(newValue.toString());
	        	getColData().get(index).setRptGroup(group);
        		getColData().get(index).save();
        		Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("All".equalsIgnoreCase(column)) { 
	        	int all = Integer.valueOf(newValue.toString());
	        	getColData().get(index).setIsAll(all);
        		getColData().get(index).save();
        		Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("Summary".equalsIgnoreCase(column)) { 
	        	int summary = Integer.valueOf(newValue.toString());
	        	getColData().get(index).setIsSummary(summary);
        		getColData().get(index).save();
        		Application.addMessage(1, "Success", "Successfully saved.");
	        }else if("RTS?".equalsIgnoreCase(column)) {    
	        	int rts = Integer.valueOf(newValue.toString());
	        	int qty = getColData().get(index).getPcs();
	        	if(rts==0 || rts==1) {
	        		if(qty==0) {
	        			getColData().get(index).setIsRts(1);
	        			getColData().get(index).setAmount(0);
	        		}else {
	        			getColData().get(index).setIsRts(0);
	        		}
		        	getColData().get(index).save();
		        	IssuedForm iss = IssuedForm.retrieve(getColData().get(index).getIssuedForm().getId());
		        	iss.setStatus(FormStatus.RTS.getId());
		        	iss.save();
		        	Application.addMessage(1, "Success", "Successfully saved.");
	        	}else {
	        		Application.addMessage(2, "Error", "Only 0 for not RTS and 1 value is allowed to RTS this FORM");
	        	}
	        }else if("Request".equalsIgnoreCase(column)) {
	        	
	        	
	        	String statusName = getColData().get(index).getStatusName();
	        	long oldStart = getColData().get(index).getBeginningNo();
	        	long oldEnd = getColData().get(index).getEndingNo();
	        	double amount = getColData().get(index).getAmount();
	        	
	        	int reqQty = Integer.valueOf(newValue.toString());
	        	int available = getColData().get(index).getAvailable();
	        	int issued = getColData().get(index).getPcs(); //issued qty
	        	long newEnd = 0l;
	        	int newAvailQty = 0;
	        	if(FormType.CT_2.getId()==formType || FormType.CT_5.getId()==formType) {
	        		
	        		int divider = 5;
	        		if(FormType.CT_2.getId()==formType) {
	        			divider = 2;
	        		}
	        		
	        		newAvailQty = available + issued;
	        		double newAmount = reqQty*divider;
	        		double newAvailableAmount=0d;
	        		if(reqQty>0) {
	        			newAvailQty -= reqQty;
	        			newAvailableAmount = newAvailQty * divider;
	        			//	long newStart = reqQty * divider;
        		    	//getColData().get(index).setEndingNo(newStart);
	        			getColData().get(index).setAvailable(newAvailQty);
	        			getColData().get(index).setPcs(reqQty);
	        			getColData().get(index).setIsActive(1);
	        			
	        			supplyTicket(amount, newAmount, newAvailableAmount);
	        			getColData().get(index).setAmount(newAmount);
	        			
	        			CollectionInfo in = getColData().get(index);
	        			CollectionInfo info = CollectionInfo.retrieveLastTransaction(in.getIssuedForm().getId());
	        			if(info!=null) {
	        				String amnt = newAmount+"";
	        				amnt = amnt.replace(",", "");
	        				amnt = amnt.replace(".0", "");
	        				amnt = amnt.replace(".00", "");
	        				long newBeginning = info.getBeginningNo() - Long.valueOf(amnt);
	        				if(newBeginning<0) {newBeginning=0;}
	        				int newPrevPcs = Integer.valueOf(info.getEndingNo()+"") - Integer.valueOf(newBeginning+"") ;
	        				getColData().get(index).setBeginningNo(newBeginning);
	        				getColData().get(index).setPrevPcs(newPrevPcs);
	        			}else {
	        				String amnt = newAmount+"";
	        				amnt = amnt.replace(",", "");
	        				amnt = amnt.replace(".0", "");
	        				amnt = amnt.replace(".00", "");
	        				long newBeginning = in.getEndingNo() - Long.valueOf(amnt);
	        				if(newBeginning<0) {newBeginning=0;}
	        				int newPrevPcs = Integer.valueOf(in.getEndingNo()+"") - Integer.valueOf(newBeginning+"") ;
	        				getColData().get(index).setBeginningNo(newBeginning);
	        				getColData().get(index).setPrevPcs(newPrevPcs);
	        			}
	        				
	        		}
	        		
	        		System.out.println("Checking========================");
	        		System.out.println("Beginning: " + getColData().get(index).getBeginningNo() + " prevPcs: " +getColData().get(index).getPrevPcs());
	        		System.out.println("Checking========================");
	        		
	        	}else {
	        		
	        		newAvailQty = available + issued;
	        		    
	        		    if(reqQty>1) {
	        		    	newAvailQty -= reqQty;
	        		    	newEnd = oldStart + (reqQty-1);
	        		    	getColData().get(index).setEndingNo(newEnd);
		        			getColData().get(index).setAvailable(newAvailQty);
		        			getColData().get(index).setPcs(reqQty);
		        			getColData().get(index).setIsActive(1);
		        			//getColData().get(index).setIsAll(0);
		        			//getColData().get(index).setIsSummary(0);
		        			supplySeries(oldStart, oldEnd, oldStart, newEnd, reqQty, newAvailQty, formType, amount);
		        			
	        		    }else if(reqQty==1) {
	        		    	newAvailQty -= reqQty;
	        		    	newEnd = oldStart;
	        		    	getColData().get(index).setEndingNo(newEnd);
		        			getColData().get(index).setAvailable(newAvailQty);
		        			getColData().get(index).setPcs(reqQty);
		        			getColData().get(index).setIsActive(1);
		        			supplySeries(oldStart, oldEnd, oldStart, newEnd, reqQty, newAvailQty, formType, amount);
	        		    }else if(reqQty==0) {
	        		    	getColData().get(index).setEndingNo(newEnd);
		        			getColData().get(index).setAvailable(newAvailQty);
		        			getColData().get(index).setPcs(reqQty);
		        			getColData().get(index).setIsActive(0);
		        			//getColData().get(index).setIsAll(0);
		        			//getColData().get(index).setIsSummary(0);
		        			getColData().get(index).setStatus(FormStatus.CANCELLED.getId());
	        		    	getColData().get(index).setStatusName(FormStatus.CANCELLED.getName());
		        			supplySeries(oldStart, oldEnd, oldStart, oldEnd, reqQty, newAvailQty, formType, 0.00);
	        		    }
	        		    
	        		    
	        		    if(newAvailQty==0) {
	        		    	getColData().get(index).setStatus(FormStatus.ALL_ISSUED.getId());
	        		    	getColData().get(index).setStatusName(FormStatus.ALL_ISSUED.getName());;
	        		    }else {
	        		    	if(reqQty>0) {
		        		    	getColData().get(index).setStatus(FormStatus.NOT_ALL_ISSUED.getId());
		        		    	getColData().get(index).setStatusName(FormStatus.NOT_ALL_ISSUED.getName());
	        		    	}
	        		    }
	        	}
	        	
	        	recalculateAmount();
	        	Application.addMessage(1, "Success", "Successfully saved.");
	        
	        }
        }
	}
	
	private void supplyTicket(double oldAmount, double newAmount, double remAmount) {
		String amount = Currency.formatAmount(oldAmount);
		String newAmnt = Currency.formatAmount(newAmount);
		String newAvlAmnt = Currency.formatAmount(remAmount);
		int index = 0;
		for(RCDFormDetails rcd : formsData) {
			if(amount.equalsIgnoreCase(rcd.getSeriesFrom())) {
				formsData.get(index).setSeriesFrom(newAmnt);
				formsData.get(index).setAmount(newAmnt);
				if(remAmount==0) {
					formsData.get(index).setStyle("color:red");
				}else {
					formsData.get(index).setStyle("");
				}
			}
			index++;
		}
		
		index = 0;
		for(RCDFormSeries ser : formSeriesData) {
			if(amount.equalsIgnoreCase(ser.getIssuedFrom())) {
				formSeriesData.get(index).setIssuedFrom(newAmnt);
				if(remAmount==0) {
					formSeriesData.get(index).setEndingFrom("All Issued");
					formsData.get(index).setStyle("color:red");
				}else {
					formSeriesData.get(index).setEndingFrom(newAvlAmnt);
				}
			}
			index++;
		}
	}
	
	//to change for better performance
	private void supplySeries(long oldStart, long oldEnd, long newStart, long newEnd, int newIssuedQty, int newRemQty, int formType, double amountReported) {
		
		String valOldStart = correctingDigitOfSeries(oldStart, formType);
		String valOldEnd = correctingDigitOfSeries(oldEnd, formType);
		String valNewStart = correctingDigitOfSeries(newStart, formType);
		String valNewEnd = correctingDigitOfSeries(newEnd, formType);
		String valLatestEndStart = correctingDigitOfSeries(newEnd+1, formType);
		String newQty="";
		String newRemainintQty="";
		String amount="";
		if(newIssuedQty==0) {
			newQty="";
			valNewStart="No Iss.";
			valNewEnd="";
			valLatestEndStart = valOldStart;
			amount="";
		}else {
			newQty = newIssuedQty+"";
			newRemainintQty=newRemQty+"";
			amount = Currency.formatAmount(amountReported);
		}
		
		if(newRemQty==0) {
			valLatestEndStart="All Issued";
			newRemainintQty="";
		}
		
		updateSeries(valOldStart, valOldEnd, valNewStart, valNewEnd, newQty , newRemainintQty, valLatestEndStart,amount);
	}
	
	private String correctingDigitOfSeries(long num, int formType) {
		String val = "";
		String valNum = num+"";
		int len = valNum.length();
		if(FormType.CT_2.getId()==formType || FormType.CT_5.getId()==formType) {
			val = num+"";
		}else if(FormType.AF_51.getId()==formType || FormType.AF_56.getId()==formType || FormType.AF_53.getId()==formType) {
			
			switch(len) {
				case 1 : val = "000000"+num; break;
				case 2 : val = "00000"+num; break;
				case 3 : val = "0000"+num; break;
				case 4 : val = "000"+num; break;
				case 5 : val = "00"+num; break;
				case 6 : val = "0"+num; break;
				case 7 : val = ""+num; break;
			}
		

		}else if(FormType.CTC_INDIVIDUAL.getId()==formType || FormType.CTC_CORPORATION.getId()==formType) {
			
			switch(len) {
				case 1 : val = "0000000"+num; break;
				case 2 : val = "000000"+num; break;
				case 3 : val = "00000"+num; break;
				case 4 : val = "0000"+num; break;
				case 5 : val = "000"+num; break;
				case 6 : val = "00"+num; break;
				case 7 : val = "0"+num; break;
				case 8 : val = ""+num; break;
			}
		
		}
		
		
		return val;
	}
	
	private void updateSeries(String oldStart, String oldEnd, String newStart, String newEnd, String newIssuedQty, String newRemQty, String latestEndStart, String amount) {
		
		int index = 0;
		for(RCDFormDetails rcd : formsData) {
				if(rcd.getSeriesFrom().equalsIgnoreCase(oldStart) && rcd.getSeriesTo().equalsIgnoreCase(oldEnd)) {
					formsData.get(index).setSeriesFrom(newStart);
					formsData.get(index).setSeriesTo(newEnd);
					formsData.get(index).setAmount(amount);
					if("All Issued".equalsIgnoreCase(latestEndStart)) {
						formsData.get(index).setStyle("color:red");
					}else {
						formsData.get(index).setStyle("");
					}
				}
				index++;
		}
		
		index = 0;
		for(RCDFormSeries ser : formSeriesData) {
			
				if(ser.getIssuedFrom().equalsIgnoreCase(oldStart) && ser.getIssuedTo().equalsIgnoreCase(oldEnd)) {
					formSeriesData.get(index).setIssuedQty(newIssuedQty);
					formSeriesData.get(index).setIssuedFrom(newStart);
					formSeriesData.get(index).setIssuedTo(newEnd);
					formSeriesData.get(index).setEndingQty(newRemQty);
					formSeriesData.get(index).setEndingFrom(latestEndStart);
					if(newRemQty.isEmpty()) {
						formSeriesData.get(index).setEndingTo("");
					}else {
						formSeriesData.get(index).setEndingTo(formSeriesData.get(index).getBeginningTo());
					}
					if(newIssuedQty.isEmpty()) {
						formSeriesData.get(index).setEndingTo(formSeriesData.get(index).getBeginningTo());
						formSeriesData.get(index).setEndingQty(formSeriesData.get(index).getBeginningQty());
					}
				}
			
			index++;
			
		}
	}
	
public void printForm() {
	String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
			"xml" + AppConf.SEPERATOR.getValue();
		   XML_FOLDER += getFileName() + ".xml";
		RCDReader rcd = RCDReader.readXML(XML_FOLDER,false);
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME ="rcd";
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		List<Form11Report> reports = new ArrayList<Form11Report>();//Collections.synchronizedList(new ArrayList<Form11Report>());
		Form11Report rpt = new Form11Report();
		reports.add(rpt);
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		
  		param.put("PARAM_FUND",rcd.getFund());
  		param.put("PARAM_COLLECTOR_NAME",rcd.getAccountablePerson().replace("-", "/").toUpperCase());
  		
  		String date = rcd.getDateCreated();
  		param.put("PARAM_PRINTED_DATE", rcd.getDateCreated());
  		param.put("PARAM_VERIFIED_DATE", rcd.getDateVerified());
  		param.put("PARAM_VERIFIED_PERSON", rcd.getVerifierPerson());
  		DocumentFormatter doc = new DocumentFormatter();
  		param.put("PARAM_VERIFIED_POSITION", doc.getTagName("verified-person-position"));
  		param.put("PARAM_TREASURER", rcd.getTreasurer());
  		param.put("PARAM_RPT_GROUP",rcd.getSeriesReport().replace("#", ""));
  		param.put("PARAM_TOTAL",rcd.getAddAmount());
  		System.out.println("check before print the dtls " + (rcd.getRcdFormDtls()!=null? rcd.getRcdFormDtls().size() : "zero dtls"));
  		int cnt = 1;
  		String numberSeriesFrom = null;
  		for(RCDFormDetails d : rcd.getRcdFormDtls()) {
  			
  			if(!d.getAmount().equalsIgnoreCase("0.00")) {
	  			param.put("PARAM_T"+cnt,d.getName());
	  			param.put("PARAM_FROM"+cnt,d.getSeriesFrom());
				param.put("PARAM_TO"+cnt,d.getSeriesTo());
				param.put("PARAM_A"+cnt,d.getAmount());
				cnt++;
				
  			}else {
  				//for RTS
  				numberSeriesFrom = d.getSeriesFrom();
  			}
  		}
  		
  		cnt = 1;
  		for(RCDFormSeries s : rcd.getRcdFormSeries()) {
  			param.put("PARAM_F"+cnt,s.getName());
  			
	  		param.put("PARAM_BQ"+cnt,s.getBeginningQty());
	  		param.put("PARAM_BF"+cnt,s.getBeginningFrom());
	  		param.put("PARAM_BT"+cnt,s.getBeginningTo());
	  		
	  		param.put("PARAM_RQ"+cnt,s.getReceiptQty());
	  		param.put("PARAM_RF"+cnt,s.getReceiptFrom());
	  		param.put("PARAM_RT"+cnt,s.getReceiptTo());
	  		
	  		param.put("PARAM_IQ"+cnt,s.getIssuedQty());
	  		param.put("PARAM_IF"+cnt,s.getIssuedFrom());
	  		param.put("PARAM_IT"+cnt,s.getIssuedTo());
	  		
	  		param.put("PARAM_EQ"+cnt,s.getEndingQty());
	  		param.put("PARAM_EF"+cnt,s.getEndingFrom());
	  		param.put("PARAM_ET"+cnt,s.getEndingTo());
	  		
	  		if(numberSeriesFrom!=null && s.getBeginningFrom().equalsIgnoreCase(numberSeriesFrom)) {
	  			
	  			int qty = Integer.valueOf(s.getBeginningQty()) + Integer.valueOf(s.getBeginningTo());
	  				//qty -=1; 	
	  			param.put("PARAM_BQ"+cnt,s.getBeginningQty());
		  		param.put("PARAM_BF"+cnt,s.getBeginningFrom());
		  		param.put("PARAM_BT"+cnt,s.getEndingTo().replace(Integer.valueOf(s.getBeginningTo())+"", qty+""));
	  			
	  			param.put("PARAM_IQ"+cnt,"");
		  		param.put("PARAM_IF"+cnt,"No. Iss.");
		  		param.put("PARAM_IT"+cnt,"");
	  			
	  			param.put("PARAM_EQ"+cnt,"");
		  		param.put("PARAM_EF"+cnt,"RTS");
		  		param.put("PARAM_ET"+cnt,"");
	  		}
	  		
		cnt++;
  		}
  		
  		
  		
  		//logo
		String officialLogo = REPORT_PATH + "logo.png";
		try{File file = new File(officialLogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_LOGO", off);
		}catch(Exception e){e.printStackTrace();}
		
		//logo
		String officialLogotrans = REPORT_PATH + "logotrans.png";
		try{File file = new File(officialLogotrans);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_LOGO_TRANS", off);
		}catch(Exception e){e.printStackTrace();}
  		
  		try{
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
			
	  		try{
	  		File file = new File(REPORT_PATH, REPORT_NAME + ".pdf");
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
		            response.setHeader("Content-Disposition", "inline; filename=\"" + REPORT_NAME + ".pdf" + "\"");
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
