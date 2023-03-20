package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.AppSetting;
import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Form11Report;
import com.italia.municipality.lakesebu.controller.IssuedForm;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Stocks;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.Rcd;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Numbers;
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
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @since 03/05/2019
 * @version 1.0
 *
 */
@Named
@ViewScoped
public class LogformBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 464988556561L;
	
	private Date receivedDate;
	private int collectorId;
	private List collectors;
	
	private int formTypeId;
	private List formTypes = new ArrayList<>();
	
	private long issuedId;
	private List issueds;
	
	private long beginningNo;
	private long endingNo;
	private int quantity;
	
	private double amount;
	private String totalAmount;
	private int group;
	
	private int tmpQty;
	private IssuedForm issuedData;
	private List<CollectionInfo> newForms;
	
	private Map<String, CollectionInfo> maps;
	
	private int collectorMapId;
	private List collectorsMap;
	private Map<Integer, Collector> collectotData;
	
	private List<CollectionInfo> infos;
	
	private int monthId;
	private List months;
	
	private CollectionInfo selectedCollectionData;
	
	private String pujLabel = "PUJ";
	private String peddlerLabel = "PEDDLER";
	private String isdaLabel = "ISDA";
	private String skylabLabel = "SKYLAB";
	private String marketLablel = "MARKET";
	
	private String puj;
	private String pedller;
	private String isda;
	private String skylab;
	private String market;
	private String others;
	
	private int fundId;
	private List funds;
	private int fundSearchId;
	private List fundsSearch;
	
	private CollectionInfo cashTicketData;
	//private boolean hasTicket;
	
	private List<CollectionInfo> selectedCollection;
	
	private Date dateFrom;
	private Date dateTo;
	
	private String reportSeriesSummary;
	
	private Date summaryDate;
	private Date perReportDate;
	private CollectionInfo collectionPrint;
	private boolean useModifiedDate;
	private boolean currentDate;
	Map<Long, CollectionInfo> mapIssued;
	
	private int rtsId;
	private List rts;
	
	private String totalCashTicket;
	
	private boolean enableCalendarPrint=true;
	
	private String searchSeries;
	private List<IssuedForm> issuedForms;
	private List<CollectionInfo> issuedSeries;
	private List<CollectionInfo> fixIssuedSeries;
	private String availableSeries;
	
	private String changesNumbers;
	
	private boolean alreadyRetrieve=false;
	private int issuedCollectorId;
	
	private int formTypeIdSearch;
	private List formTypesSearch = new ArrayList<>();
	private List<CollectionInfo> formsInfo;
	private String totalAmountPerForms;
	private Date fromFormDate;
	private Date toFormDate;
	private int collectorIdSearch;
	private List collectorSearch;
	 
	public void loadFormDetails() {
		formTypeIdSearch = 0;
		formTypesSearch = new ArrayList<>();
		formTypesSearch.add(new SelectItem(0, "All Forms"));
		for(FormType type : FormType.values()) {
			formTypesSearch.add(new SelectItem(type.getId(), type.getName()));
		}
		
		collectorIdSearch = 0;
		collectorSearch = new ArrayList<>();
		collectorSearch.add(new SelectItem(0, "All Collector"));
		for(Collector col : Collector.retrieve(" AND cl.isactivecollector=1 ORDER BY cl.collectorname", new String[0])) {
			if(col.getId()!=0) {
				collectorSearch.add(new SelectItem(col.getId(), col.getName()));
			}
		}
		
		loadForms();
		
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgPerForms').show(1000);");
	}
	
	public void loadForms() {
		formsInfo = new ArrayList<CollectionInfo>();
		String sql = " AND frm.isactivecol=1 AND (frm.receiveddate>=? AND frm.receiveddate<=?)";
		String[] params = new String[2];
		
		params[0] =  DateUtils.convertDate(getFromFormDate(), "yyyy-MM-dd");
		params[1] =  DateUtils.convertDate(getToFormDate(), "yyyy-MM-dd");
		double amount = 0d;
		
		if(getFormTypeIdSearch()>0) {
			sql += " AND frm.formtypecol=" + getFormTypeIdSearch();
		}
		
		if(getCollectorIdSearch()>0) {
			sql += " AND cl.isid=" + getCollectorIdSearch();
		}
		
		for(CollectionInfo info : CollectionInfo.retrieve(sql, params)) {
			
			amount += info.getAmount();
			formsInfo.add(info);
			
		}
		
		setTotalAmountPerForms(Currency.formatAmount(amount));
	}
	
	public void changeBehaviorCalendarDatePrint() {
		if(isUseModifiedDate()) {
				setEnableCalendarPrint(false);
		}else {
				setEnableCalendarPrint(true);
		}
	}
	
	public void saveChangeSeries() {
		if(fixIssuedSeries!=null && fixIssuedSeries.size()>0) {
			int countTotal = fixIssuedSeries.size();
			List<CollectionInfo> tmp = fixIssuedSeries;
			
			IssuedForm tmpIssued = issuedForms.get(0);
			Stocks tmpStock = tmpIssued.getStock();
			
			int count = 0;
			for(CollectionInfo c : fixIssuedSeries) {
				c.save();
				count++;
			}
			
			if(count!=countTotal) {//rollback changes
				tmpIssued.save();
				tmpStock.save();
				for(CollectionInfo c : tmp) {
					c.save();
				}
			}else {
				IssuedForm issuedForm = issuedForms.get(0);
				Stocks stock = issuedForm.getStock();
				int start = issuedForms.get(0).getNewSeries();
				int end = issuedForms.get(0).getNewSeries() + (issuedForm.getPcs()-1);
				
				issuedForm.setBeginningNo(start);
				issuedForm.setEndingNo(end);
				issuedForm.save();
				
				String from = DateUtils.numberResult(issuedForm.getFormType(), start);
				String to = DateUtils.numberResult(issuedForm.getFormType(), end);
				stock.setSeriesFrom(from);
				stock.setSeriesTo(to);
				stock.save();
			}
			
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("$('#issuedId').hide();$('#fixId').hide();");
			setSearchSeries(fixIssuedSeries.get(0).getBeginningNo()+"");
			loadSearchSeries();
		}
	}
	
	public void loadSearchSeries() {
		String sql = " AND frm.isactivelog=1 ";
		String[] params = new String[0];
		issuedForms = new ArrayList<IssuedForm>();
		if(getSearchSeries()!=null) {
				sql += "  AND frm.beginningNoLog=" + getSearchSeries();
		}
		
		sql +=" ORDER BY frm.issueddate ASC";
		
		issuedForms = IssuedForm.retrieve(sql, params);
		PrimeFaces pf = PrimeFaces.current();
		if(issuedForms!=null && issuedForms.size()==1) {
			loadIssuedSeries(issuedForms.get(0));
		}else {
			pf.executeScript("$('#issuedId').hide();$('#fixId').hide()");
		}
	}
	
	public void loadIssuedSeries(IssuedForm issued) {
		PrimeFaces pf = PrimeFaces.current();
		
		issuedSeries = new ArrayList<CollectionInfo>();
		issuedSeries = CollectionInfo.retrieve(" AND sud.isactivelog=1 AND sud.logid=" + issued.getId(), new String[0]);
		if(issuedSeries!=null && issuedSeries.size()>0) {
			pf.executeScript("$('#issuedId').show();$('#fixId').hide()");
		}else {
			pf.executeScript("$('#issuedId').hide();$('#fixId').hide()");
		}
		int cnt = 0;
		for(CollectionInfo c : issuedSeries) {
			cnt += c.getPcs();
		}
		
		cnt = issued.getPcs() - cnt;
		setAvailableSeries("Available remaining form is " + cnt);
	}
	
	public void loadFixIssuedSeries(IssuedForm issued) {
		PrimeFaces pf = PrimeFaces.current();
			
			if(issued.getNewSeries()>0) {
				
				
				
				fixIssuedSeries = new ArrayList<CollectionInfo>();
				fixIssuedSeries = CollectionInfo.retrieve(" AND sud.isactivelog=1 AND sud.logid=" + issued.getId(), new String[0]);
				if(fixIssuedSeries!=null && fixIssuedSeries.size()>0) {
					
					
					List<CollectionInfo> ls = new ArrayList<CollectionInfo>();
					ls = fixIssuedSeries;
					fixIssuedSeries = new ArrayList<CollectionInfo>();
					int start = issued.getNewSeries();
					int end = 0;
					
					int count = 1;
					for(CollectionInfo s : ls) {
						if(count==1) {
							end = (s.getPcs() + start) - 1;
							s.setBeginningNo(start);
							s.setEndingNo(end);
							
							start = end;
						}else {
							
							end = start + s.getPcs();
							s.setBeginningNo(start+1);
							s.setEndingNo(end);
							
							start = end;
						}
						
						
						fixIssuedSeries.add(s);
						
						count++;
					}
					count -=1;
					setChangesNumbers(count>1?  "Below are the number of changes" : "Below is the changes");
					
					pf.executeScript("$('#fixId').show();$('#issuedId').hide()");
				}else {
					pf.executeScript("$('#fixId').hide()");
				}
			
			}else {
				Application.addMessage(2, "Wrong", "PLease provide new series for this accountable form");
				pf.executeScript("$('#fixId').hide()");
			}
		
	}
	
	public void updateSeriesInfo(CollectionInfo info) {
		info.save();
		Application.addMessage(1, "Success", "Successfully saved.");
	}
	
	public void onCellEditSeries(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        
        int index = event.getRowIndex();
        String column =  event.getColumn().getHeaderText();
        
        if("Status".equalsIgnoreCase(column) && !oldValue.equals(newValue)) {
        	int statusId = Integer.valueOf(newValue+"");
        	IssuedForm issuedForm  = getIssuedForms().get(index);
        	issuedForm.setStatus(statusId);
        	issuedForm.save();
        	//update list
        	getIssuedForms().get(index).setStatus(statusId);
        	getIssuedForms().get(index).setStatusName(FormStatus.nameId(statusId));
        	Application.addMessage(1, "Success", column + " has been changed to status " + FormStatus.nameId(statusId));
        }
        
        System.out.println("old Value: " + oldValue);
        System.out.println("new Value: " + newValue);
        
	}
	
	public void onCellIssued(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        
        System.out.println("old Value: " + oldValue);
        System.out.println("new Value: " + newValue);
        int row = event.getRowIndex();
        System.out.println("Row>> " + row );
        
	}
	
	public void calculateCashTicket() {
		double puj = getPuj().isEmpty()? 0 : Double.valueOf(getPuj());
		double ped = getPedller().isEmpty()? 0 : Double.valueOf(getPedller());
		double isda = getIsda().isEmpty()? 0 : Double.valueOf(getIsda());
		double skylab = getSkylab().isEmpty()? 0 : Double.valueOf(getSkylab());
		double market = getMarket().isEmpty()? 0 : Double.valueOf(getMarket());
		double total = puj + ped + isda + skylab + market;
		setTotalCashTicket(Currency.formatAmount(total));
	}
	
	public void onCellEdit(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        System.out.println("Row Index   "+ event.getRowIndex());
        if(newValue!=null) {
        	try {
        		
        		
        		CollectionInfo info = getNewForms().get(index);
        		
        		if(info.getFormType() == FormType.CT_2.getId() || info.getFormType() == FormType.CT_5.getId()) {
        			getNewForms().get(index).setAmount((Double)oldValue);
        			Application.addMessage(3, "Error", "Cash Ticket amount is not editable");
        		}else {
	        		double amount = (Double)newValue;
	        		if(amount==info.getAmount()) {
	        			if(info.getId()>0) {//check if already save on the database
	        				Date date= DateUtils.convertDateString(info.getReceivedDate(), "yyyy-MM-dd");
	        				setFundSearchId(info.getFundId());
	        				setCollectorMapId(info.getCollector().getId());
	        				setDateFrom(date);
	        				setDateTo(date);
	            			info.save();
	            			init();
	            			Application.addMessage(1, "Success", "Successfully updated");
	            		}
	        		}
        		}
        	}catch(NumberFormatException e) {}
        }
	}
	
	public void find() {
		clear();
		init();
		
	}
	
	@PostConstruct
	public void init() {
		selectedCollection = new ArrayList<CollectionInfo>();
		collectotData = new HashMap<Integer, Collector>();
		newForms = new ArrayList<CollectionInfo>();
		mapIssued = new HashMap<Long, CollectionInfo>();
		maps = new HashMap<String, CollectionInfo>();
		infos = new ArrayList<CollectionInfo>();
		
		if(!alreadyRetrieve) {
			issuedCollectorId = Login.getUserLogin().getCollectorId();
			setCollectorId(issuedCollectorId);
			setCollectorMapId(issuedCollectorId);
			alreadyRetrieve=true;
			loadIssuedForm();
		}
		
		String sql = "";
		String[] params = new String[4];
		int cnt = 0;
		if(getCollectorMapId()>0) {
			sql += "AND cl.isid=? ";
			params[cnt++] = getCollectorMapId()+""; 
		}else {
			sql += "AND cl.isid!=? ";
			params[cnt++] = "0";
		}
		
		if(getFundSearchId()>0) {
			sql += "AND frm.fundid=? ";
			params[cnt++] = getFundSearchId()+"";
		}else {
			sql += "AND frm.fundid!=? ";
			params[cnt++] = "0";
		}
		
		/*String dayStart = DateUtils.getCurrentYear()+"-"+ (DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+"") +"-01";
		String dayEnd = DateUtils.getCurrentYear()+"-"+ (DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+"") +"-31";
		
		
		if(getMonthId()==0) {
			dayStart = DateUtils.getCurrentYear()+"-01-01";
			dayEnd = DateUtils.getCurrentYear()+"-12-31";
		}else if(getMonthId()>0 && getMonthId()!=DateUtils.getCurrentMonth()) {
			dayStart = DateUtils.getCurrentYear()+"-"+ (getMonthId()<10? "0"+getMonthId() : getMonthId()+"") +"-01";
			dayEnd = DateUtils.getCurrentYear()+"-"+ (getMonthId()<10? "0"+getMonthId() : getMonthId()+"") +"-31";
		}*/
		sql += " AND frm.isactivecol=1 AND (frm.receiveddate>=? AND frm.receiveddate<=?) ORDER BY frm.receiveddate";
		params[cnt++] =  DateUtils.convertDate(getDateFrom(), "yyyy-MM-dd");
		params[cnt++] =  DateUtils.convertDate(getDateTo(), "yyyy-MM-dd");
		
		/*if(getCollectorMapId()>0) {
			params[cnt++] = getCollectorMapId()+"";
		}*/
		
		for(CollectionInfo in : CollectionInfo.retrieve(sql, params)){
			String key = in.getRptGroup() +"-"+ in.getFundId() +"-"+ in.getCollector().getId() +"";
			
			if(maps!=null && maps.containsKey(key)) {
					double newAmount = maps.get(key).getAmount() + in.getAmount();
					in.setAmount(newAmount);
					maps.put(key, in);
			}else {
				maps.put(key, in);
			}
		}
		
		for(CollectionInfo i : maps.values()) {
			String value = "";
			String len = i.getRptGroup()+"";
			int size = len.length();
			if(size==1) {
				String[] date = i.getReceivedDate().split("-");
				value = date[0] +"-"+date[1] + "-#00"+len;
			}else if(size==2) {
				String[] date = i.getReceivedDate().split("-");
				value = date[0] +"-"+date[1] + "-#0"+len;
			}else if(size==3) {
				String[] date = i.getReceivedDate().split("-");
				value = date[0] +"-"+date[1] + "-#"+len;
			}
			i.setRptFormat(value);
			double amnt = Numbers.roundOf(i.getAmount(), 2);
			i.setAmount(amnt);
			infos.add(i);
		}
		Collections.reverse(infos);
	}
	
	
	public void checkHasCashTicket(CollectionInfo info) {
		if(FormType.CT_2.getId()==info.getFormType() || FormType.CT_5.getId()==info.getFormType()) {
			PrimeFaces pm = PrimeFaces.current();
			pm.executeScript("PF('dlgCash').show();");
		}else {
			printRDC(info);
		}
	}
	
	public void loadIssuedForm() {
		
		//clear all above if changing collector
		clearBelowFormList();
		
		issueds = new ArrayList<>();
		
		String sql = " AND frm.fundid=? AND frm.formstatus=1 AND cl.isid=?";
		String[] params = new String[2];
		params[0] = getFundId()+"";
		params[1] = getCollectorId()+"";
		
		//assigned group
		if(getGroup()==0) {
			setGroup(CollectionInfo.getNewReportGroup(getCollectorId(),getFundId()));
		}
		
		List<IssuedForm> forms = IssuedForm.retrieve(sql, params);
		if(forms!=null && forms.size()>1) {
			int x=1;
			for(IssuedForm form : forms) {
				if(getMapIssued()!=null && getMapIssued().size()>0 && getMapIssued().containsKey(form.getId())) {
					// do not add to the list of available forms
				}else {
					String isNew = "";
					if(DateUtils.getCurrentDateYYYYMMDD().equalsIgnoreCase(form.getIssuedDate())) {
						isNew = "New";
					}
					issueds.add(new SelectItem(form.getId(),  isNew +" #"+form.getStabNo() + " " + form.getFormTypeName() +" " +form.getBeginningNo() +"-" + form.getEndingNo()));
					if(x==1) {
						setBeginningNo(form.getEndingNo());
						setFormTypeId(form.getFormType());
						setIssuedId(form.getId());
						loadLatestSeries();
					}
					x++;
				}
			}
		}else if(forms!=null && forms.size()==1) {
			
			if(getMapIssued()!=null && getMapIssued().size()>0 && getMapIssued().containsKey(forms.get(0).getId())) {
				//do not add to the available forms
			}else {
			
				String isNew = "";
				if(DateUtils.getCurrentDateYYYYMMDD().equalsIgnoreCase(forms.get(0).getIssuedDate())) {
					isNew = "New";
				}
				issueds.add(new SelectItem(forms.get(0).getId(),isNew +" #"+forms.get(0).getStabNo() + " " + forms.get(0).getFormTypeName() +" " + forms.get(0).getBeginningNo() +"-" + forms.get(0).getEndingNo()));
				setBeginningNo(forms.get(0).getEndingNo());
				setFormTypeId(forms.get(0).getFormType());
				setIssuedId(forms.get(0).getId());
				loadLatestSeries();
				
			}
		}else {
			issueds.add(new SelectItem(0, "No Issued Form"));
		}
		
	}
	
	public void loadLatestSeries() {
		String sql = " AND frm.fundid=? AND cl.isid=? AND sud.logid=? ORDER BY frm.colid DESC limit 1";
		String[] params = new String[3];
		params[0] = getFundId()+"";
		params[1] = getCollectorId()+"";
		params[2] = getIssuedId()+"";
		
		List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
		if(infos!=null && infos.size()>0) {
			CollectionInfo info = infos.get(0);
			setBeginningNo(info.getEndingNo()+1);
			setFormTypeId(info.getFormType());
			long qty = info.getIssuedForm().getEndingNo() - getBeginningNo();
			setQuantity(Integer.valueOf(qty+"")+1);
			setEndingNo(info.getIssuedForm().getEndingNo());
			setTmpQty(Integer.valueOf(qty+"")+1);
			setIssuedData(info.getIssuedForm());
			
			
			if(getFormTypeId()==9 || getFormTypeId()==10) {
				System.out.println("Retrieved in Collection Cash Ticket>> ");
				long beg =   info.getBeginningNo();
				long to =  info.getEndingNo();
				
				qty = 0;
				
				if(FormType.CT_2.getId()==info.getFormType()) {
					qty = beg / 2;
				}else if(FormType.CT_5.getId()==info.getFormType()) {
					qty = beg / 5;
				} 
				
				setTmpQty(Integer.valueOf(qty+""));
				setBeginningNo(beg);
				setEndingNo(to);
				setSelectedCollectionData(info);
				System.out.println("Set temp qty >> " + getTmpQty());
			}
			
		}else {
			System.out.println("Retrieved in LogIssuedForm Cash Ticket>> ");
			sql = " AND frm.fundid=? AND frm.formstatus=1 AND cl.isid=? AND frm.logid=?";
			params = new String[3];
			params[0] = getFundId()+"";
			params[1] = getCollectorId()+"";
			params[2] = getIssuedId()+"";
			List<IssuedForm> forms = IssuedForm.retrieve(sql, params);
			setBeginningNo(forms.get(0).getBeginningNo());
			setEndingNo(forms.get(0).getEndingNo());
			setQuantity(forms.get(0).getPcs());
			setFormTypeId(forms.get(0).getFormType());
			setIssuedId(forms.get(0).getId());
			setTmpQty(forms.get(0).getPcs());
			setIssuedData(forms.get(0));
			
			//if(getFormTypeId()>8) {
			if(getFormTypeId()==9 || getFormTypeId()==10) {
				long beg =   forms.get(0).getBeginningNo();
				long to =  forms.get(0).getEndingNo();
				
				long qty = 0;
				
				if(FormType.CT_2.getId()==forms.get(0).getFormType()) {
					qty = beg / 2;
				}else if(FormType.CT_5.getId()==forms.get(0).getFormType()) {
					qty = beg / 5;
				} 
				
				setTmpQty(Integer.valueOf(qty+""));
				setBeginningNo(beg);
				setEndingNo(to);
				
				CollectionInfo info = new CollectionInfo();
				info.setCollector(forms.get(0).getCollector());
				info.setIssuedForm(forms.get(0));
				info.setPcs(forms.get(0).getPcs());
				info.setBeginningNo(forms.get(0).getBeginningNo());
				info.setEndingNo(forms.get(0).getEndingNo());
				info.setFormType(forms.get(0).getFormType());
				setSelectedCollectionData(info);
				System.out.println("Set temp qty >> " + getTmpQty());
			}
		}
	}
	
	public void calculateEndingNo() {
			
		if(getFormTypeId()<=8 || getFormTypeId()>=11) {
			if(getQuantity()>getTmpQty()) {
				setQuantity(getTmpQty());
				Application.addMessage(3, "Error", "You have inputed more than the allowed quantity, series will now reset on default end series");
			}
			
			long ending = (getBeginningNo()) + (getQuantity()==0? 0 : getQuantity()-1);
			System.out.println("begin: " + getBeginningNo() + " pcs: " + getQuantity());
			System.out.println("ending: " + ending);
			setEndingNo(ending);
			
		}else { 
			if(getQuantity()>getTmpQty()) {
				setQuantity(getTmpQty());
				Application.addMessage(3, "Error", "You have inputed more than the allowed quantity, series will now reset on default end series");
			}
			
			int qty = getQuantity();
			//long prev = getSelectedCollectionData().getEndingNo() - getSelectedCollectionData().getBeginningNo();
			if(FormType.CT_2.getId()==getFormTypeId()) {
				qty *= 2;
			}else if(FormType.CT_5.getId()==getFormTypeId()) {
				qty *= 5;
			}
			
			setAmount(qty);
			
			long remainingAmount = getSelectedCollectionData().getBeginningNo() - qty;
			setBeginningNo(remainingAmount);
			
			if(getQuantity()==0) {
				setBeginningNo(getSelectedCollectionData().getBeginningNo());
			}
		}
		
			
		
	}
	
	public void addGroup() {
		setCurrentDate(true);
		CollectionInfo form = new CollectionInfo();
		
		boolean isOk = true;
		
		
		form.setIsActive(1);
		boolean isNotRts = getRtsId()==0? true : false;
		
		if(getBeginningNo()<=0) {
			
			if(FormType.CT_2.getId()==getFormTypeId() || FormType.CT_5.getId()==getFormTypeId()) {
				//do nothing it means all issued
			}else {
				isOk = false;
				Application.addMessage(3, "Error", "Please provide Serial From");
			}
		}
		
		if(getEndingNo()<=0 && isNotRts) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Serial To");
		}
		
		
		if(getQuantity()<=0 && isNotRts) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Quantity");
		}
		
		if(getCollectorId()<=0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Collector");
		}
		
		if(getAmount()==0 && isNotRts) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide amount");
		}
		
		if(getAmount()>0 && !isNotRts) {//if tag as RTS
			isOk = false;
			Application.addMessage(3, "Error", "Please remove amount and quantity as this stab tag as RTS");
		}
		
		
		form.setFundId(getFundId());
		form.setRptGroup(getGroup());
		form.setReceivedDate(DateUtils.convertDate(getReceivedDate(), "yyyy-MM-dd"));
		form.setFormType(getFormTypeId());
		form.setPcs(getQuantity());
		form.setBeginningNo(getBeginningNo());
		form.setEndingNo(getEndingNo());
		form.setAmount(getAmount());
		
		long pcs = getIssuedData().getEndingNo() - getBeginningNo();
		form.setPrevPcs(Integer.valueOf(pcs+""));
		
		Collector collector = new Collector();
		collector.setId(getCollectorId());
		form.setCollector(collector);
		
		IssuedForm issued = new IssuedForm();
		issued.setId(getIssuedId());
		form.setIssuedForm(issued);
		
		String start = DateUtils.numberResult(getFormTypeId(), getBeginningNo());
		String end = DateUtils.numberResult(getFormTypeId(), getEndingNo());
		
		form.setStartNo(start);
		form.setEndNo(end);
		
		//tag as all issued if the ending balance is match with the current collection ending series
		if(getIssuedData()!=null) {
			if(getEndingNo()==getIssuedData().getEndingNo()) {
				
				getIssuedData().setCollector(collector);
				
				getIssuedData().setStatus(FormStatus.ALL_ISSUED.getId());
				
				form.setStatus(FormStatus.ALL_ISSUED.getId());
				form.setStatusName(FormStatus.ALL_ISSUED.getName());
			}else {
				form.setStatus(FormStatus.NOT_ALL_ISSUED.getId());
				form.setStatusName(FormStatus.NOT_ALL_ISSUED.getName());
			}
		}else {
			form.setStatus(FormStatus.NOT_ALL_ISSUED.getId());
			form.setStatusName(FormStatus.NOT_ALL_ISSUED.getName());
		}
		
		//try{form.setFormTypeName(FormType.nameId(getFormTypeId()));}catch(NullPointerException e){}
		try{form.setFormTypeName(FormType.val(getFormTypeId()).getDescription());}catch(NullPointerException e){}
		
		//cash ticket
		if(FormType.CT_2.getId()==getFormTypeId() || FormType.CT_5.getId()==getFormTypeId()) {
			if(getBeginningNo()==0) {
				form.setStatus(FormStatus.ALL_ISSUED.getId());
				form.setStatusName(FormStatus.ALL_ISSUED.getName());
			}else {
				form.setStatus(FormStatus.NOT_ALL_ISSUED.getId());
				form.setStatusName(FormStatus.NOT_ALL_ISSUED.getName());
			}
		}
		
		if(getRtsId()==1) {
			form.setStatus(FormStatus.RTS.getId());
			form.setStatusName(FormStatus.RTS.getName());
			form.setIsRts(1);
		}
		
		
		if(newForms!=null && newForms.size()>0) {
			for(CollectionInfo in : newForms) {
				if(form.getIssuedForm().getId()==in.getIssuedForm().getId()) {
					System.out.println("Added form"+ form.getIssuedForm().getId() + " form already added " + in.getIssuedForm().getId());
					isOk = false;
					Application.addMessage(2, "Warning", "This item already added on the list. Re-adding is not allowed.");
				}
			}
		}
		
		
		if(isOk) {
			mapIssued.put(form.getIssuedForm().getId(), form);//add to list of issued form
			newForms.add(form);
			setQuantity(0);
			setBeginningNo(0);
			setEndingNo(0);
			setAmount(0);
			setRtsId(0);
			
			//recalculate
			recalCulateFormAmount();
			/*
			 * double amount = 0d; for(CollectionInfo i : newForms) { amount +=
			 * i.getAmount(); } setTotalAmount(Currency.formatAmount(amount));
			 */
			//cash ticket
			//checkCashTicketBeforeSaving();
			reloadAvailableForms();
		}
	}
	
	public void clear() {
		setMapIssued(new HashMap<Long, CollectionInfo>());
		setCurrentDate(false);
		//setHasTicket(false);
		setCashTicketData(null);
		setReceivedDate(null);
		setCollectorId(0);
		setFundId(1);
		setIssuedId(0);
		setRtsId(0);
		issueds = new ArrayList<>();
		issueds.add(new SelectItem(0, "No Selected Collector"));
		
		setQuantity(0);
		setBeginningNo(0);
		setEndingNo(0);
		setFormTypeId(0);
		setGroup(0);
		setAmount(0);
		
		newForms = new ArrayList<CollectionInfo>();//Collections.synchronizedList(new ArrayList<CollectionInfo>());
		
		setSelectedCollectionData(null);
		
		setPuj(null);
		setPedller(null);
		setIsda(null);
		setSkylab(null);
		setOthers(null);
		setTotalAmount("0.00");
		selectedCollection = new ArrayList<CollectionInfo>();//Collections.synchronizedList(new ArrayList<CollectionInfo>());
		setCollectionPrint(null);
	}
	
	public void clearBelowFormList() {
		setQuantity(0);
		setBeginningNo(0);
		setEndingNo(0);
		setGroup(0);
		setAmount(0);
		setTotalAmount("0.00");
		newForms = new ArrayList<CollectionInfo>();//Collections.synchronizedList(new ArrayList<CollectionInfo>());
		setPuj(null);
		setPedller(null);
		setIsda(null);
		setOthers(null);
		Application.addMessage(1, "Success", "Successfully delete forms listed");
		setSelectedCollectionData(null);
	}
	
	public void saveData() {
		
		if(newForms!=null && newForms.size()>0) {
			
			for(CollectionInfo form : newForms) {
				form.save();
				setCollectorMapId(form.getCollector().getId());
				
				if(FormStatus.ALL_ISSUED.getId()==form.getStatus()) {
					IssuedForm is = IssuedForm.retrieveId(form.getIssuedForm().getId());
					is.setStatus(FormStatus.ALL_ISSUED.getId());
					is.save();
				}else if(FormStatus.RTS.getId()==form.getStatus()) {
					IssuedForm is = IssuedForm.retrieveId(form.getIssuedForm().getId());
					is.setStatus(FormStatus.RTS.getId());
					is.setEndingNo(form.getBeginningNo()-1);
					is.save();
				}
			}
			
			
			if(checkCashTicketBeforeSaving()) {
				saveCashTicketFormDetails(newForms.get(0));
			}
			
			CollectionInfo in = newForms.get(0);
			newForms = new ArrayList<CollectionInfo>();//Collections.synchronizedList(new ArrayList<CollectionInfo>());
			clear();
			
			Date date= DateUtils.convertDateString(in.getReceivedDate(), "yyyy-MM-dd");
			setFundSearchId(in.getFundId());
			setCollectorMapId(in.getCollector().getId());
			setDateFrom(date);
			setDateTo(date);
			
			init();
			Application.addMessage(1, "Success", "Successfully saved");
		}else {
			Application.addMessage(3, "Error", "Please provide list in order to save");
		}
	}
	
	public boolean checkCashTicketBeforeSaving() {
		System.out.println("checkCashTicketBeforeSaving >> ");
		if(newForms!=null && newForms.size()>0) {
			System.out.println("checkCashTicketBeforeSaving with none zero");
			for(CollectionInfo i : newForms) {
				if(FormType.CT_2.getId()==i.getFormType() || FormType.CT_5.getId()==i.getFormType()) {
					//setHasTicket(true);
					System.out.println("with cash ticket>>>>");
					return true;
				}
			}
		}
		return false;
	}
	
	public void openCashTicket(CollectionInfo info) {
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();  
		System.out.println("save to folder cash ticket>> " + XML_FOLDER);
		
		Collector col = Collector.retrieve(info.getCollector().getId());
		
		String collector = col.getName();
		
		String value = "";
		String len = info.getRptGroup()+"";
		int size = len.length();
		if(size==1) {
			String[] date = info.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-00"+len;
		}else if(size==2) {
			String[] date = info.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-0"+len;
		}else if(size==3) {
			String[] date = info.getReceivedDate().split("-");
			value = date[0] +"-"+date[1] + "-"+len;
		}
		String fund = FundType.typeName(info.getFormType());
		RCDReader.readCashTicker(collector+"-"+value+"_"+fund+"_CT.xml");
	}
	
	public void saveCashTicketFormDetails(CollectionInfo info) {
		String fund = FundType.typeName(getFundId());
		
		List<RCDFormDetails> rs = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>());
		RCDFormDetails r = new RCDFormDetails();
		if(getPuj()!=null || !getPuj().isEmpty()) {
			r = new RCDFormDetails();
			r.setName(getPujLabel());
			r.setAmount(getPuj());
			rs.add(r);
		}
		if(getPedller()!=null || !getPedller().isEmpty()) {
			r = new RCDFormDetails();
			r.setName(getPeddlerLabel());
			r.setAmount(getPedller());
			rs.add(r);
		}
		if(getIsda()!=null || !getIsda().isEmpty()) {
			r = new RCDFormDetails();
			r.setName(getIsdaLabel());
			r.setAmount(getIsda());
			rs.add(r);
		}
		if(getSkylab()!=null || !getSkylab().isEmpty()) {
			r = new RCDFormDetails();
			r.setName(getSkylabLabel());
			r.setAmount(getSkylab());
			rs.add(r);
		}
		if(getMarket()!=null || !getMarket().isEmpty()) {
			r = new RCDFormDetails();
			r.setName(getMarketLablel());
			r.setAmount(getMarket());
			rs.add(r);
		}
		/*if(getOthers()!=null || !getOthers().isEmpty()) {
			r = new RCDFormDetails();
			r.setName("");
			r.setAmount(getOthers());
			rs.add(r);
		}*/
		//RCDReader.saveCashTicket(rs, fileName, fileSaveLocation)
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();  
		System.out.println("save to folder cash ticket>> " + XML_FOLDER);
		
		Collector col = Collector.retrieve(info.getCollector().getId());
		
		String collector = col.getName();
		
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
		
		
		RCDReader.saveCashTicket(rs, collector+"-"+value+"_"+fund+"_CT", XML_FOLDER);
		setPuj(null);
		setPedller(null);
		setIsda(null);
		setSkylab(null);
		setOthers(null);
	}
	
	
	public void clickItem(CollectionInfo in) {
		
		if(CollectionInfo.isGroupLatest(in.getRptGroup(), in.getCollector().getId(), in.getFundId())) {
			setCurrentDate(true);
		}else {
			setCurrentDate(false);
		}
		
		/*this lines of code was change by above codes
		 * Reason: Instead of current date can be edit, now allowing most recent transaction
		if(in.getReceivedDate().equalsIgnoreCase(DateUtils.getCurrentDateYYYYMMDD())) {
			setCurrentDate(true);
		}else {
			setCurrentDate(false);
		}*/
		
		setCashTicketData(in);
		String sql = " AND frm.isactivecol=1 AND cl.isid=? AND frm.rptgroup=? AND frm.receiveddate=? AND frm.fundid=?";
		String[] params = new String[4];
		params[0] = in.getCollector().getId()+"";
		params[1] = in.getRptGroup()+"";
		params[2] = in.getReceivedDate();
		params[3] = in.getFundId()+"";
		
		//loadIssuedForm();
		
		newForms = new ArrayList<CollectionInfo>();
		
		double totalAmount = 0d;
		setMapIssued(new HashMap<Long, CollectionInfo>());
		for(CollectionInfo i : CollectionInfo.retrieve(sql, params)){
			
			String start = DateUtils.numberResult(i.getFormType(), i.getBeginningNo());
			String end = DateUtils.numberResult(i.getFormType(), i.getEndingNo());
			
			i.setStartNo(start);
			i.setEndNo(end);
			
			newForms.add(i);
			totalAmount += i.getAmount();
			
			mapIssued.put(i.getIssuedForm().getId(), i);//store the issued form
		}
		
		
		//assigned group
		setGroup(in.getRptGroup());
		setFundId(in.getFundId());
		setCollectorId(in.getCollector().getId());
		reloadAvailableForms();
		setTotalAmount(Currency.formatAmount(totalAmount));
		setRtsId(in.getStatus()==FormStatus.RTS.getId()? 1 : 0);
	}
	
	private void reloadAvailableForms() {
		issueds = new ArrayList<>();
		setIssuedId(0);
		
		String sql = " AND frm.fundid=? AND frm.formstatus=1 AND cl.isid=?";
		String[] params = new String[2];
		params[0] = getFundId()+"";
		params[1] = getCollectorId()+"";
		
		
		List<IssuedForm> forms = IssuedForm.retrieve(sql, params);
		if(forms!=null && forms.size()>1) {
			issueds.add(new SelectItem(0, "Select Issued Form"));	
			//int x=1;
			for(IssuedForm form : forms) {
				if(!mapIssued.containsKey(form.getId())) {//check the form to load in serial issued is not yet issued
					issueds.add(new SelectItem(form.getId(), form.getFormTypeName() +" " +form.getBeginningNo() +"-" + form.getEndingNo()));
				}
			}
		}else if(forms!=null && forms.size()==1) {
			issueds.add(new SelectItem(0, "Select Issued Form"));
			//do not load
			//strick to do not double entry
			if(!mapIssued.containsKey(forms.get(0).getId())) {
				issueds.add(new SelectItem(forms.get(0).getId(), forms.get(0).getFormTypeName() +" " + forms.get(0).getBeginningNo() +"-" + forms.get(0).getEndingNo()));
			}	
		}else {
			issueds.add(new SelectItem(0, "No Issued Form"));
		}
	}
	
	public void clickItemForm(CollectionInfo in) {
		setReceivedDate(DateUtils.convertDateString(in.getReceivedDate(), "yyyy-MM-dd"));
		setCollectorId(in.getCollector().getId());
		setIssuedId(in.getIssuedForm().getFormType());
		setQuantity(in.getPcs());
		setBeginningNo(in.getBeginningNo());
		setEndingNo(in.getEndingNo());
		setFormTypeId(in.getFormType());
		setGroup(in.getRptGroup());
		setAmount(in.getAmount());
		setFormTypeId(in.getFormType());
		
		
		loadIssuedForm();
	}
	
	public void printDateModify(CollectionInfo info) {
		setUseModifiedDate(false);
		setPerReportDate(DateUtils.convertDateString(info.getReceivedDate(), "yyyy-MM-dd"));
		setCollectionPrint(info);
	}
	public void printChange() {
		if(getCollectionPrint()!=null) {
			printRDC(getCollectionPrint());
		}
	}
	
	
	public void printRDC(CollectionInfo info) {
		
		String today = DateUtils.getCurrentDateYYYYMMDD();
		String dbDate = info.getReceivedDate();
		
		if(CollectionInfo.isGroupLatest(info.getRptGroup(), info.getCollector().getId(), info.getFundId())) {
			/*System.out.println("Printing information from database");
			if(today.equalsIgnoreCase(dbDate)) {
				printForm(info);
			}else {
				System.out.println("Printing information from xml file parin");
				if(isXmlFileExist(info)) {
					printXML(info);
				}else {*/
					System.out.println("Creating xml file");
					System.out.println("Gathering information from database for data");
					printForm(info);
				/*}
			}*/
		}else {
			System.out.println("Printing information from xml file");
			if(isXmlFileExist(info)) {
				printXML(info);
			}else {
				System.out.println("Creating xml file");
				System.out.println("Gathering information from database for data");
				printForm(info);
			}
		}
	}
	
	
	private boolean isXmlFileExist(CollectionInfo in) {
		
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();
		
		Collector col = Collector.retrieve(in.getCollector().getId());
		String[] dates = in.getReceivedDate().split("-");
		
		String collector = col.getName();
		String virifiedDate = dates[1]+"/"+dates[2]+"/"+dates[0];
		
		String value = "";
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

		String fileName = collector+"-"+value+"_"+FundType.typeName(in.getFundId()) + ".xml";
		
		File xml = new File(XML_FOLDER + fileName);
		
		System.out.println("Checking xml file on location : " + XML_FOLDER + fileName);
		if(xml.exists()) {
			System.out.println("File is existing");
			return true;
		}else {
			System.out.println("File is not existing");
		}
		
		return false;
	}
	
	public void printXML(CollectionInfo info) {
		
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
  		RCDReader xml = RCDReader.readXML(xmlFolder,false);
  		
  		param.put("PARAM_FUND",xml.getFund());
  		param.put("PARAM_COLLECTOR_NAME",xml.getAccountablePerson().replace("-", "/").toUpperCase());
  		param.put("PARAM_RPT_GROUP",xml.getSeriesReport().replace("#", ""));
  		
  		String date = DateUtils.convertDateToMonthDayYear(DateUtils.convertDate(getPerReportDate(), "yyyy-MM-dd"));
  		
  		param.put("PARAM_PRINTED_DATE", useModifiedDate==true? date : xml.getDateCreated());
  		param.put("PARAM_VERIFIED_DATE", useModifiedDate==true? date : xml.getDateVerified());
  		param.put("PARAM_VERIFIED_PERSON", xml.getVerifierPerson());
  		param.put("PARAM_TREASURER", xml.getTreasurer());
  		
  		int cnt = 1;
  		for(RCDFormDetails d : xml.getRcdFormDtls()) {
  			param.put("PARAM_T"+cnt,d.getName());
	  		param.put("PARAM_FROM"+cnt,d.getSeriesFrom());
			param.put("PARAM_TO"+cnt,d.getSeriesTo());
			param.put("PARAM_A"+cnt,d.getAmount());
			cnt++;
  		}
		
  		cnt = 1;
		for(RCDFormSeries frm : xml.getRcdFormSeries()) {
			param.put("PARAM_F"+cnt,frm.getName());
			
	  		param.put("PARAM_BQ"+cnt,frm.getBeginningQty());
	  		param.put("PARAM_BF"+cnt,frm.getBeginningFrom());
	  		param.put("PARAM_BT"+cnt,frm.getBeginningTo());
	  		
	  		param.put("PARAM_RQ"+cnt,frm.getReceiptQty());
	  		param.put("PARAM_RF"+cnt,frm.getReceiptFrom());
	  		param.put("PARAM_RT"+cnt,frm.getReceiptTo());
	  		
	  		param.put("PARAM_IQ"+cnt,frm.getIssuedQty());
	  		param.put("PARAM_IF"+cnt,frm.getIssuedFrom());
	  		param.put("PARAM_IT"+cnt,frm.getIssuedTo());
	  		
	  		param.put("PARAM_EQ"+cnt,frm.getEndingQty());
	  		param.put("PARAM_EF"+cnt,frm.getEndingFrom());
	  		param.put("PARAM_ET"+cnt,frm.getEndingTo());
	  		cnt++;
		}
  		
  		
  		param.put("PARAM_TOTAL",xml.getAddAmount());
  		
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
	
	private RCDReader buildFormData(CollectionInfo in) {
		Collector col = Collector.retrieve(in.getCollector().getId());
		String[] dates = in.getReceivedDate().split("-");
		
		String collector = col.getName();
		String virifiedDate = dates[1]+"/"+dates[2]+"/"+dates[0];
		
		String value = "";
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
		
		RCDReader rcd = new RCDReader();
		rcd.setBrisFile("marxmind");
		rcd.setDateCreated(DateUtils.convertDateToMonthDayYear(in.getReceivedDate()));
		rcd.setFund(FundType.typeName(in.getFundId()));
		rcd.setAccountablePerson(collector);
		rcd.setSeriesReport(value);
		rcd.setDateVerified(virifiedDate);
		
		List<RCDFormDetails> dtls = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>());
		List<RCDFormSeries> srs = new ArrayList<RCDFormSeries>();//Collections.synchronizedList(new ArrayList<RCDFormSeries>());
		
		String sql = " AND frm.fundid=? AND frm.isactivecol=1 AND cl.isid=? AND frm.rptgroup=? AND frm.receiveddate=?";
		String[] params = new String[4];
		params[0] = in.getFundId()+"";
		params[1] = in.getCollector().getId()+"";
		params[2] = in.getRptGroup()+"";
		params[3] = in.getReceivedDate();
		
		double totalAmount = 0d;
		int cnt = 1;
		//forms with issuance
		//String tmpReceivedDate = DateUtils.getCurrentDateYYYYMMDD(); //to be use for no issuance
		Map<Long, IssuedForm> issuedMap = new HashMap<Long, IssuedForm>();//Collections.synchronizedMap(new HashMap<Long, IssuedForm>());
		boolean hasTicketIssued = false;
		for(CollectionInfo i : CollectionInfo.retrieve(sql, params)){
			//System.out.println("Finding... " + FormType.nameId(i.getFormType()));
			RCDFormDetails dt = new RCDFormDetails();
			RCDFormSeries sr = new RCDFormSeries();
			issuedMap.put(i.getIssuedForm().getId(), i.getIssuedForm());
			
			//tmpReceivedDate = i.getReceivedDate();//assigned date -- this will be use for no issuance
			//System.out.println("after issued map...");
			totalAmount += i.getAmount();
			String start = DateUtils.numberResult(i.getFormType(), i.getBeginningNo());
			String end = DateUtils.numberResult(i.getFormType(), i.getEndingNo());
			//System.out.println("after.... start and end " + start + " - " + end);
				Form11Report frm = reportCollectionInfo(i);
				String ctc = frm.getF1();
				//System.out.println("after form11..ctc= " + ctc);
				dt.setFormId(cnt+"");
				dt.setName(i.getFormTypeName());
				
				if(FormType.CT_2.getId()== i .getFormType() || FormType.CT_5.getId() == i .getFormType()) {
					dt.setSeriesFrom(Currency.formatAmount(i.getAmount()));
					dt.setSeriesTo("");
					
					hasTicketIssued = true;//set flag that there is new cash ticket issuance // this flag will be use for cash ticket xml file
					
				}else {
					dt.setSeriesFrom(start);
					dt.setSeriesTo(end);
				}
				//System.out.println("prepare for dtls hasTicketIssued="+hasTicketIssued);
				dt.setAmount(Currency.formatAmount(i.getAmount()));
				dtls.add(dt);
				//System.out.println("added to dtls");
				sr.setId(cnt+"");
				sr.setName(ctc);
				
				sr.setBeginningQty(frm.getF2());
		  		sr.setBeginningFrom(frm.getF3());
		  		sr.setBeginningTo(frm.getF4());
		  		
		  		sr.setReceiptQty(frm.getF5());
		  		sr.setReceiptFrom(frm.getF6());
		  		sr.setReceiptTo(frm.getF7());

		  		sr.setIssuedQty(frm.getF8());
		  		sr.setIssuedFrom(frm.getF9());
		  		sr.setIssuedTo(frm.getF10());
		  		
		  		sr.setEndingQty(frm.getF11());
		  		sr.setEndingFrom(frm.getF12());
		  		sr.setEndingTo(frm.getF13());
		  		srs.add(sr);
		  		
			cnt++;
			
		}
		
		//forms without issuance
		sql = " AND frm.fundid=? AND frm.isactivelog=1 AND frm.formstatus=1 AND cl.isid=?";
		params = new String[2];
		params[0] = in.getFundId()+"";
		params[1] = in.getCollector().getId()+"";
		
		if(issuedMap!=null && issuedMap.size()>0) {
			
			/*String[] date = tmpReceivedDate.split("-");
			int day = Integer.valueOf(date[2]);
			String dateRetrieved = date[0] + "-" + date[1] + "-" + (day<10? "0" + day  : day+"");*/
			
			for(IssuedForm is : IssuedForm.retrieve(sql, params)) {
				
				RCDFormSeries sr = new RCDFormSeries();
				
				long key = is.getId();
				if(issuedMap.containsKey(key)) {
					issuedMap.remove(key);//remove forms with issuance
				}else {
					issuedMap.put(key, is);
					//System.out.println("Multiple issued or has issued>>>");
					sql = " AND frm.fundid=? AND frm.rptgroup<? AND sud.logid=? AND cl.isid=? ORDER BY frm.colid DESC limit 1";
					params = new String[4];
					params[0] = is.getFundId()+"";
					params[1] = in.getRptGroup()+"";
					params[2] = is.getId()+"";
					params[3] = is.getCollector().getId()+"";
					//System.out.println("checking previous ");
					
					List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
					if(infos!=null && infos.size()>0) {
						Form11Report frm = reportLastCollectionInfo(infos.get(0));
						if(frm!=null) {	
								String ctc = "";
								ctc = frm.getF1();
								//System.out.println("Multiple issued or has issued more than one >>> " + ctc);
								sr.setId(cnt+"");
								sr.setName(ctc);
								
								sr.setBeginningQty(frm.getF2());
						  		sr.setBeginningFrom(frm.getF3());
						  		sr.setBeginningTo(frm.getF4());
						  		
						  		sr.setReceiptQty(frm.getF5());
						  		sr.setReceiptFrom(frm.getF6());
						  		sr.setReceiptTo(frm.getF7());

						  		sr.setIssuedQty(frm.getF8());
						  		sr.setIssuedFrom(frm.getF9());
						  		sr.setIssuedTo(frm.getF10());
						  		
						  		sr.setEndingQty(frm.getF11());
						  		sr.setEndingFrom(frm.getF12());
						  		sr.setEndingTo(frm.getF13());
						  		srs.add(sr);
						  		
						  		cnt++;
						}
					}else {
						Form11Report frm = reportIssued(is);
						
						String ctc = frm.getF1();
						//System.out.println("Multiple issued or has issued but will get in issued form reportIssued form >>> " + ctc);	
						sr.setId(cnt+"");
						sr.setName(ctc);
						
						sr.setBeginningQty(frm.getF2());
				  		sr.setBeginningFrom(frm.getF3());
				  		sr.setBeginningTo(frm.getF4());
				  		
				  		sr.setReceiptQty(frm.getF5());
				  		sr.setReceiptFrom(frm.getF6());
				  		sr.setReceiptTo(frm.getF7());

				  		sr.setIssuedQty(frm.getF8());
				  		sr.setIssuedFrom(frm.getF9());
				  		sr.setIssuedTo(frm.getF10());
				  		
				  		sr.setEndingQty(frm.getF11());
				  		sr.setEndingFrom(frm.getF12());
				  		sr.setEndingTo(frm.getF13());
				  		srs.add(sr);
					  		
					  		cnt++;
					}
					
				}
			}
			
		}else {
			//System.out.println("Totally no issued in collection info");
			//totally no issued in collectioninfo
			for(IssuedForm notissued : IssuedForm.retrieve(sql, params)) {
				RCDFormSeries sr = new RCDFormSeries();
				Form11Report frm = reportIssued(notissued);
				String ctc = frm.getF1();
				//System.out.println("Totally no issued in collection info form type>> " + ctc);
				sr.setId(cnt+"");
				sr.setName(ctc);
				
				sr.setBeginningQty(frm.getF2());
		  		sr.setBeginningFrom(frm.getF3());
		  		sr.setBeginningTo(frm.getF4());
		  		
		  		sr.setReceiptQty(frm.getF5());
		  		sr.setReceiptFrom(frm.getF6());
		  		sr.setReceiptTo(frm.getF7());

		  		sr.setIssuedQty(frm.getF8());
		  		sr.setIssuedFrom(frm.getF9());
		  		sr.setIssuedTo(frm.getF10());
		  		
		  		sr.setEndingQty(frm.getF11());
		  		sr.setEndingFrom(frm.getF12());
		  		sr.setEndingTo(frm.getF13());
		  		srs.add(sr);
			  		
			  		cnt++;
				
			}
		
		}
		//System.out.println("check dtls size " + (dtls!=null? dtls.size() : "zero" ) );
		rcd.setRcdFormDtls(dtls);
		rcd.setRcdFormSeries(srs);
		
		rcd.setBeginningBalancesAmount("0.00");
		rcd.setAddAmount(Currency.formatAmount(totalAmount));
		rcd.setLessAmount(Currency.formatAmount(totalAmount));
		rcd.setBalanceAmount("0.00");
		
		rcd.setCertificationPerson(collector);
		rcd.setVerifierPerson("HENRY E. MAGBANUA");
		rcd.setDateVerified(dates[1]+"/"+dates[2]+"/"+dates[0]);
		rcd.setTreasurer("FERDINAND L. LOPEZ");
		
		//force order do not remove
		List<RCDFormDetails> dets = rcd.getRcdFormDtls();
		List<RCDFormDetails> dts = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>());
		for(FormType form : FormType.values()) {
			for(RCDFormDetails s : dets) {
				//if(form.getName().trim().equalsIgnoreCase(s.getName())) {
				if(form.getDescription().equalsIgnoreCase(s.getName())) {
					System.out.println("Details >> " + s.getName());
					dts.add(s);
				}
			}
		}
		rcd.setRcdFormDtls(dts);
		
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();  
		System.out.println("save to folder>> " + XML_FOLDER);
		
		String fileName = collector+"-"+value+"_"+rcd.getFund();
		List<RCDFormDetails> rs = new ArrayList<RCDFormDetails>();
		if(hasTicketIssued) {	
			rs = RCDReader.readCashTicker(XML_FOLDER + fileName +"_CT.xml");
		}		
		
		if(rs!=null && rs.size()>0) {
			RCDFormDetails rx = new RCDFormDetails();
			rx.setName("");
			rx.setAmount("");
			rcd.getRcdFormDtls().add(rx);
			for(RCDFormDetails r : rs) {
				rx = new RCDFormDetails();
				if((r.getAmount()!=null || !r.getAmount().isEmpty()) && (r.getName()!=null || !r.getName().isEmpty())) {
					rx.setName(r.getName() + " ("+ Currency.formatAmount(r.getAmount()) +")");
					rx.setAmount("");
					rcd.getRcdFormDtls().add(rx);
				}else if((r.getAmount()!=null || !r.getAmount().isEmpty()) && (r.getName()==null || r.getName().isEmpty())) {
					int amount = 0;
					try {amount = Integer.valueOf(r.getAmount().replace(",", ""));}catch(Exception e) {}
					if(amount>0) {
						rx.setName("Others (" + r.getAmount() +")");
					}else {
						rx.setName(r.getAmount());
					}
					
					rx.setAmount("");
					rcd.getRcdFormDtls().add(rx);
				}
			}
		}
		
		
		
		//force order
		List<RCDFormSeries> series = rcd.getRcdFormSeries();
		List<RCDFormSeries> ss = new ArrayList<RCDFormSeries>();//Collections.synchronizedList(new ArrayList<RCDFormSeries>());
		for(FormType form : FormType.values()) {
			for(RCDFormSeries s : series) {
				if(form.getName().equalsIgnoreCase(s.getName())) {
					System.out.println("Series >> " + s.getName());
					ss.add(s);
				}
			}
		}
		rcd.setRcdFormSeries(ss);
		
		RCDReader.saveXML(rcd, fileName, XML_FOLDER,false);
		
		return rcd;
	}
	
	
	public void printForm(CollectionInfo in) {
		
		RCDReader rcd = buildFormData(in);
		
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
  		
  		String date = DateUtils.convertDateToMonthDayYear(DateUtils.convertDate(getPerReportDate(), "yyyy-MM-dd"));
  		
  		param.put("PARAM_PRINTED_DATE", useModifiedDate==true? date : rcd.getDateCreated());
  		param.put("PARAM_VERIFIED_DATE", useModifiedDate==true? date : rcd.getDateVerified());
  		param.put("PARAM_VERIFIED_PERSON", rcd.getVerifierPerson());
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
	
	@Deprecated
	public void printReport(CollectionInfo in) {
		//compiling report
				String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
						AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
				String REPORT_NAME ="rcd";
				System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
				ReportCompiler compiler = new ReportCompiler();
				String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
				
				List<Form11Report> reports = new ArrayList<Form11Report>();//Collections.synchronizedList(new ArrayList<Form11Report>());
				Form11Report rpt = new Form11Report();
				reports.add(rpt);
				JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
		  		HashMap param = new HashMap();
				
		  		Collector col = Collector.retrieve(in.getCollector().getId());
				param.put("PARAM_COLLECTOR_NAME",col.getName());
		  		
				String[] dates = in.getReceivedDate().split("-");
				
		  		param.put("PARAM_PRINTED_DATE", DateUtils.convertDateToMonthDayYear(in.getReceivedDate()));
		  		param.put("PARAM_VERIFIED_DATE", dates[1]+"/"+dates[2]+"/"+dates[0]);
		  		param.put("PARAM_VERIFIED_PERSON", "HENRY E. MAGBANUA");
		  		param.put("PARAM_TREASURER", "FERDINAND L. LOPEZ");
		  		
		  		String sql = " AND frm.isactivecol=1 AND cl.isid=? AND frm.rptgroup=?";
				String[] params = new String[2];
				params[0] = in.getCollector().getId()+"";
				params[1] = in.getRptGroup()+"";
				
				double totalAmount = 0d;
				int cnt = 1;
				//forms with issuance
				String tmpReceivedDate = DateUtils.getCurrentDateYYYYMMDD(); //to be use for no issuance
				Map<Long, IssuedForm> issuedMap = Collections.synchronizedMap(new HashMap<Long, IssuedForm>());
				for(CollectionInfo i : CollectionInfo.retrieve(sql, params)){
					issuedMap.put(i.getIssuedForm().getId(), i.getIssuedForm());
					
					tmpReceivedDate = i.getReceivedDate();//assigned date -- this will be use for no issuance
					
					totalAmount += i.getAmount();
					String start = DateUtils.numberResult(i.getFormType(), i.getBeginningNo());
					String end = DateUtils.numberResult(i.getFormType(), i.getEndingNo());
					
					Form11Report frm = reportCollectionInfo(i);
					/*String ctc = frm.getF1().replace("IND.", "");
				       ctc = ctc.replace("CORP.", "");*/
					String ctc = frm.getF1();
					
						param.put("PARAM_T"+cnt,i.getFormTypeName());
						if(FormType.CT_2.getId()== i .getFormType() || FormType.CT_5.getId() == i .getFormType()) {
							param.put("PARAM_FROM"+cnt,Currency.formatAmount(i.getAmount()));
							param.put("PARAM_TO"+cnt,"");
						}else {
							param.put("PARAM_FROM"+cnt,start);
							param.put("PARAM_TO"+cnt,end);
						}
						
						
						
						param.put("PARAM_A"+cnt,Currency.formatAmount(i.getAmount()));
						param.put("PARAM_F"+cnt,ctc);
				  		param.put("PARAM_BQ"+cnt,frm.getF2());
				  		
				  		param.put("PARAM_BF"+cnt,frm.getF3());
				  		param.put("PARAM_BT"+cnt,frm.getF4());
				  		param.put("PARAM_RQ"+cnt,frm.getF5());
				  		param.put("PARAM_RF"+cnt,frm.getF6());
				  		param.put("PARAM_RT"+cnt,frm.getF7());
				  		param.put("PARAM_IQ"+cnt,frm.getF8());
				  		param.put("PARAM_IF"+cnt,frm.getF9());
				  		param.put("PARAM_IT"+cnt,frm.getF10());
				  		param.put("PARAM_EQ"+cnt,frm.getF11());
				  		param.put("PARAM_EF"+cnt,frm.getF12());
				  		param.put("PARAM_ET"+cnt,frm.getF13());
				  		
					cnt++;
					
				}
				
				//forms without issuance
				sql = " AND frm.isactivelog=1 AND frm.formstatus=1 AND cl.isid=?";
				params = new String[1];
				params[0] = in.getCollector().getId()+"";// getCollectorMapId()+"";
				
				if(issuedMap!=null && issuedMap.size()>0) {
					
					String[] date = tmpReceivedDate.split("-");
					int day = Integer.valueOf(date[2]);
					String dateRetrieved = date[0] + "-" + date[1] + "-" + (day<10? "0" + day  : day+"");
					
					for(IssuedForm is : IssuedForm.retrieve(sql, params)) {
						long key = is.getId();
						if(issuedMap.containsKey(key)) {
							issuedMap.remove(key);//remove forms with issuance
						}else {
							issuedMap.put(key, is);
							
							sql = " AND frm.receiveddate<? AND sud.logid=? ORDER BY frm.colid DESC limit 1";
							params = new String[2];
							params[0] = dateRetrieved;
							params[1] = is.getId()+"";
							System.out.println("checking previous ");
							
							List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
							if(infos!=null && infos.size()>0) {
								
								Form11Report frm = reportLastCollectionInfo(infos.get(0));
								if(frm!=null) {	
										String ctc = "";
									   /*try{ctc = frm.getF1().replace("IND.", "");
								       ctc = ctc.replace("CORP.", "");}catch(NullPointerException e) {}*/
										ctc = frm.getF1();
										
										param.put("PARAM_F"+cnt,ctc);
								  		param.put("PARAM_BQ"+cnt,frm.getF2());
								  		
								  		param.put("PARAM_BF"+cnt,frm.getF3());
								  		param.put("PARAM_BT"+cnt,frm.getF4());
								  		param.put("PARAM_RQ"+cnt,frm.getF5());
								  		param.put("PARAM_RF"+cnt,frm.getF6());
								  		param.put("PARAM_RT"+cnt,frm.getF7());
								  		param.put("PARAM_IQ"+cnt,frm.getF8());
								  		param.put("PARAM_IF"+cnt,frm.getF9());
								  		param.put("PARAM_IT"+cnt,frm.getF10());
								  		param.put("PARAM_EQ"+cnt,frm.getF11());
								  		param.put("PARAM_EF"+cnt,frm.getF12());
								  		param.put("PARAM_ET"+cnt,frm.getF13());
								  		
								  		cnt++;
								}
							}else {
								Form11Report frm = reportIssued(is);
								
								String ctc = frm.getF1();
									
									param.put("PARAM_F"+cnt,ctc);
							  		param.put("PARAM_BQ"+cnt,frm.getF2());
							  		
							  		param.put("PARAM_BF"+cnt,frm.getF3());
							  		param.put("PARAM_BT"+cnt,frm.getF4());
							  		param.put("PARAM_RQ"+cnt,frm.getF5());
							  		param.put("PARAM_RF"+cnt,frm.getF6());
							  		param.put("PARAM_RT"+cnt,frm.getF7());
							  		param.put("PARAM_IQ"+cnt,frm.getF8());
							  		param.put("PARAM_IF"+cnt,frm.getF9());
							  		param.put("PARAM_IT"+cnt,frm.getF10());
							  		param.put("PARAM_EQ"+cnt,frm.getF11());
							  		param.put("PARAM_EF"+cnt,frm.getF12());
							  		param.put("PARAM_ET"+cnt,frm.getF13());
							  		
							  		cnt++;
							}
							
						}
					}
					
				}else {
					
					for(IssuedForm notissued : IssuedForm.retrieve(sql, params)) {
						
						Form11Report frm = reportIssued(notissued);
						String ctc = frm.getF1();
							
							param.put("PARAM_F"+cnt,ctc);
					  		param.put("PARAM_BQ"+cnt,frm.getF2());
					  		
					  		param.put("PARAM_BF"+cnt,frm.getF3());
					  		param.put("PARAM_BT"+cnt,frm.getF4());
					  		param.put("PARAM_RQ"+cnt,frm.getF5());
					  		param.put("PARAM_RF"+cnt,frm.getF6());
					  		param.put("PARAM_RT"+cnt,frm.getF7());
					  		param.put("PARAM_IQ"+cnt,frm.getF8());
					  		param.put("PARAM_IF"+cnt,frm.getF9());
					  		param.put("PARAM_IT"+cnt,frm.getF10());
					  		param.put("PARAM_EQ"+cnt,frm.getF11());
					  		param.put("PARAM_EF"+cnt,frm.getF12());
					  		param.put("PARAM_ET"+cnt,frm.getF13());
					  		
					  		cnt++;
						
					}
				
				}
		  		
				//if(getCollectorMapId()>0) {
					//param.put("PARAM_COLLECTOR_NAME",getCollectotData().get(getCollectorMapId()).getName());
					
				/*}else {
					param.put("PARAM_COLLECTOR_NAME","");
				}*/
		  		
		  		
		  		String value = "";
				String len = in.getRptGroup()+"";
				int size = len.length();
				if(size==1) {
					String[] date = in.getReceivedDate().split("-");
					value = date[0] +"-"+date[1] + "-00"+len;
				}else if(size==2) {
					String[] date = in.getReceivedDate().split("-");
					value = date[0] +"-"+date[1] + "-0"+len;
				}else if(size==3) {
					String[] date = in.getReceivedDate().split("-");
					value = date[0] +"-"+date[1] + "-"+len;
				}
		  		
		  		param.put("PARAM_RPT_GROUP",value);
		  		
		  		param.put("PARAM_TOTAL",Currency.formatAmount(totalAmount));
		  		
		  		
		  		
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
	
	public Form11Report reportCollectionInfo(CollectionInfo info){
		System.out.println("reportCollectionInfo>>>");
		Form11Report rpt = new Form11Report();
		
		rpt.setF1(FormType.nameId(info.getFormType()));
		System.out.println("checking Form11Report reportCollectionInfo f1="+rpt.getF1());
		int logmonth = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[1]);
		int logDay = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[2]);
		
		String f3 = "";
		String f4 = "";
		String f6 = "";
		String f7 = "";
		String f9 = "";
		String f10 = "";
		String f12 = "";
		String f13 = "";
		
		if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
			//write in receipt
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			
			String be1= info.getBeginningNo()+"";
			int be2 = be1.length();
			
			
			
			if(info.getPrevPcs()==49) {
				rpt.setF5("50");
				f6 = be2==7? "0"+be1 : be1;
				rpt.setF6(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				
				String en1= info.getIssuedForm().getEndingNo()+"";
				int en2 = en1.length();
				//rpt.setF7(en2==7? "0"+en1 : en1);
			
				f7 = en2==7? "0"+en1 : en1;
				rpt.setF7(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));
			}else {
				/*int qty = info.getPrevPcs()+1;
				rpt.setF5(qty+"");
				
				long begNo = info.getBeginningNo();
				be1= begNo+"";
				be2 = be1.length();
				
				f6 = be2==7? "0"+be1 : be1;
				rpt.setF6(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				
				String en1= info.getIssuedForm().getEndingNo()+"";
				int en2 = en1.length();
			
				f7 = en2==7? "0"+en1 : en1;
				rpt.setF7(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));*/
				
				int qty = info.getPrevPcs()+1;
				//temporary removed if qty not equal to 50 place it to beginning
				//rpt.setF5(qty+"");
				rpt.setF2(qty+"");
				
				long begNo = info.getBeginningNo();
				be1= begNo+"";
				be2 = be1.length();
				f6 = be2==7? "0"+be1 : be1;
				
				//temporary removed if qty not equal to 50 place it to beginning
				//rpt.setF6(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				rpt.setF6("");
				
				String en1= info.getIssuedForm().getEndingNo()+"";
				int en2 = en1.length();
				f7 = en2==7? "0"+en1 : en1;
				//temporary removed if qty not equal to 50 place it to beginning
				//rpt.setF7(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));
				rpt.setF4(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));
				rpt.setF7("");
			}
			
			
			
			
		}else {
		//Write in beginning balance
			String be1= info.getBeginningNo()+"";
			int be2 = be1.length();
			
			if(info.getPrevPcs()==49) {
				rpt.setF2("50");
				//rpt.setF3(be2==7? "0"+be1 : be1);
				
				f3 = be2==7? "0"+be1 : be1; 
				rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
			}else {
				
				/*String sql = " AND sud.logid=?";
				String params[]= new String[1];
				params[0]= info.getIssuedForm().getId()+"";
				List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
				
				if(infos!=null && infos.size()>1) {
					int qty = info.getPrevPcs()+1;
					rpt.setF2(qty+"");
					
					long begNo = info.getBeginningNo() - info.getPrevPcs();
					be1= begNo+"";
					be2 = be1.length();
					
					f3 = be2==7? "0"+be1 : be1; 
					rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
				}else {//correction for those who has a beginning quantity not equal to 49
					int qty = info.getPrevPcs()+1;
					rpt.setF2(qty+"");
					
					long begNo = info.getBeginningNo();
					be1= begNo+"";
					be2 = be1.length();
					
					f3 = be2==7? "0"+be1 : be1; 
					rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
				}*/
				System.out.println("Process on the new changes>>>>>");
				int qty = info.getPrevPcs()+1;
				rpt.setF2(qty+"");
				
				long begNo = info.getBeginningNo();
				be1= begNo+"";
				be2 = be1.length();
				
				f3 = be2==7? "0"+be1 : be1; 
				rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
				
				
				
				
			}
			
			
			String en1= info.getIssuedForm().getEndingNo()+"";
			int en2 = en1.length();
			//rpt.setF4(en2==7? "0"+en1 : en1);
			
			f4 = en2==7? "0"+en1 : en1;
			rpt.setF4(DateUtils.numberResult(info.getFormType(), Long.valueOf(f4)));
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
		}
		//issued
		rpt.setF8(info.getPcs()+"");
		
		String beg1= info.getBeginningNo()+"";
		int beg2 = beg1.length();
		//rpt.setF9(beg2==7? "0"+beg1 : beg1);
		
		f9 = beg2==7? "0"+beg1 : beg1;
		rpt.setF9(DateUtils.numberResult(info.getFormType(), Long.valueOf(f9)));
		
		//String en1= info.getIssuedForm().getEndingNo()+"";
		String en1= info.getEndingNo()+"";
		int en2 = en1.length();
		//rpt.setF10(en2==7? "0"+en1 : en1);
		
		f10 = en2==7? "0"+en1 : en1;
		rpt.setF10(DateUtils.numberResult(info.getFormType(), Long.valueOf(f10)));
		
		//ending balance
		
		long endingQty = info.getIssuedForm().getEndingNo() - info.getEndingNo();
		
		if(endingQty==0) {
			rpt.setF11("");
			rpt.setF12("All Issued");
			rpt.setF13("");
		}else {
			rpt.setF11(endingQty+"");
			long enNumber = info.getEndingNo() + 1;
			String enbeg1= enNumber+"";
			int enbeg2 = enbeg1.length();
			//rpt.setF12(enbeg2==7? "0"+enbeg1 : enbeg1);
			
			f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
			rpt.setF12(DateUtils.numberResult(info.getFormType(), Long.valueOf(f12)));
			
			String enen1= info.getIssuedForm().getEndingNo()+"";
			int enen2 = enen1.length();
			//rpt.setF13(enen2==7? "0"+enen1 : enen1);
			
			f13 = enen2==7? "0"+enen1 : enen1;
			rpt.setF13(DateUtils.numberResult(info.getFormType(), Long.valueOf(f13)));
		}
		//remarks
		rpt.setF14("");
		
		/*Collector col = Collector.retrieve(info.getCollector().getId());
		if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
			rpt.setF15(col.getName());
		}else {
			rpt.setF15(col.getDepartment().getDepartmentName());
		}*/
		
		//change the value if the form is Cash ticket
				if(FormType.CT_2.getId()==info.getFormType() || FormType.CT_5.getId()==info.getFormType()) {
					
					rpt.setF1(FormType.nameId(info.getFormType()));
					String allIssued = info.getBeginningNo()==0? "All Issued" : "";
					double amount = 0d;
					
					if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
						
						amount = info.getBeginningNo() + info.getAmount();
						
						if(amount==info.getEndingNo()) {
							//beginning
							rpt.setF2("");
							rpt.setF3("");
							rpt.setF4("");
							 
							//Receipt
							rpt.setF5("");
							rpt.setF6(Currency.formatAmount(amount));
							rpt.setF7((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
						}else {
							//beginning
							rpt.setF2("");
							rpt.setF3(Currency.formatAmount(amount));
							rpt.setF4((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
							 
							//Receipt
							rpt.setF5("");
							rpt.setF6("");
							rpt.setF7("");
						}
						
						//issued
						rpt.setF8("");
						rpt.setF9(Currency.formatAmount(info.getAmount()));
						rpt.setF10((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
						
						//ending balance
						rpt.setF11("");
						if(info.getBeginningNo()>0) {
							rpt.setF12(Currency.formatAmount(info.getBeginningNo()));
						}else {
							rpt.setF12(allIssued);
						}
						rpt.setF13("");
					}else {
						
						amount = info.getBeginningNo() + info.getAmount();
						
						//beginning
						rpt.setF2("");
						rpt.setF3(Currency.formatAmount(amount));
						rpt.setF4((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
						
						//Receipt
						rpt.setF5("");
						rpt.setF6("");
						rpt.setF7("");
						
						//issued
						rpt.setF8("");
						rpt.setF9(Currency.formatAmount(info.getAmount()));
						rpt.setF10((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
						
						//ending balance
						rpt.setF11("");
						if(info.getBeginningNo()>0) {
							rpt.setF12(Currency.formatAmount(info.getBeginningNo()));
						}else {
							rpt.setF12(allIssued);
						}
						rpt.setF13("");
					}
						
						
						//remarks
						rpt.setF14("");
					//}
				}
		
		return rpt;
	}
	
	public Form11Report reportLastCollectionInfo(CollectionInfo info){
		System.out.println("reportLastCollectionInfo>>>");
		Form11Report rpt = null;
		System.out.println("info.getIssuedForm().getEndingNo()=" + info.getIssuedForm().getEndingNo() + " - info.getEndingNo() " + info.getEndingNo());
		long endingQty = info.getIssuedForm().getEndingNo() - info.getEndingNo();
		System.out.println("endingQty>> " + endingQty);
		String f3 = "";
		String f4 = "";
		String f6 = "";
		String f7 = "";
		String f9 = "";
		String f10 = "";
		String f12 = "";
		String f13 = "";
		
		
		
		if(endingQty>0) {
		rpt = new Form11Report();	
		rpt.setF1(FormType.nameId(info.getFormType()));
		System.out.println("form type>> " + rpt.getF1());
		rpt.setF2(endingQty+"");
		
		long endTmp = info.getEndingNo() + 1;//added 1 if has previous issuance and no issuance on the next report 07/04/2019
		
		String enbeg1= endTmp+"";//info.getEndingNo()+"";
		int enbeg2 = enbeg1.length();
		
		f3 = enbeg2==7? "0"+enbeg1 : enbeg1;
		rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
		
		String enen1= info.getIssuedForm().getEndingNo()+"";
		int enen2 = enen1.length();
		
		f4 = enen2==7? "0"+enen1 : enen1;
		rpt.setF4(DateUtils.numberResult(info.getFormType(), Long.valueOf(f4)));
		//rpt.setF4(enen2==7? "0"+enen1 : enen1);
		
		System.out.println("Beginning from >> " + rpt.getF3());
		System.out.println("Beginning to >> " + rpt.getF4());
		
		//Receipt
		rpt.setF5("");
		rpt.setF6("");
		rpt.setF7("");
		
		//issued
		rpt.setF8("");
		rpt.setF9("No Iss.");
		rpt.setF10("");
		
		
		//ending balance
		rpt.setF11(endingQty+"");
		
		f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
		f13 = enen2==7? "0"+enen1 : enen1;
		rpt.setF12(DateUtils.numberResult(info.getFormType(), Long.valueOf(f12)));
		rpt.setF13(DateUtils.numberResult(info.getFormType(), Long.valueOf(f13)));
		//rpt.setF12(enbeg2==7? "0"+enbeg1 : enbeg1);
		//rpt.setF13(enen2==7? "0"+enen1 : enen1);
		
		//remarks
		rpt.setF14("");
		
		//change the value if the form is Cash ticket
		if(FormType.CT_2.getId()==info.getFormType() || FormType.CT_5.getId()==info.getFormType()) {
			if(info.getAmount()>0) {
				int logmonth = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[1]);
				int logDay = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[2]);
				
				rpt.setF1(FormType.nameId(info.getFormType()));
				if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
					//beginning
					rpt.setF2("");
					rpt.setF3("");
					rpt.setF4("");
					
					//Receipt
					rpt.setF5("");
					rpt.setF6(Currency.formatAmount(info.getAmount()));
					rpt.setF7((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
				}else {
					//beginning
					rpt.setF2("");
					rpt.setF3(Currency.formatAmount(info.getAmount()));
					rpt.setF4((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
					
					//Receipt
					rpt.setF5("");
					rpt.setF6("");
					rpt.setF7("");
				}
				
				
				//issued
				rpt.setF8("");
				rpt.setF9(Currency.formatAmount(info.getAmount()));
				rpt.setF10((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
				
				String allIssued = info.getBeginningNo()==0? "All Issued" : "";
				//ending balance
				rpt.setF11("");
				rpt.setF12(allIssued);
				rpt.setF13("");
				
				//remarks
				rpt.setF14("");
			}
		}
		
		}
		
		
		if(endingQty==0) {//this only for cash ticket if no issuance on the next collection report
			if(FormType.CT_2.getId()==info.getFormType() || FormType.CT_5.getId()==info.getFormType()) {
				if(info.getAmount()>0) {
					int logmonth = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[1]);
					int logDay = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[2]);
					rpt = new Form11Report();	
					rpt.setF1(FormType.nameId(info.getFormType()));
					if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
						//beginning
						rpt.setF2("");
						rpt.setF3("");
						rpt.setF4("");
						
						//Receipt
						rpt.setF5("");
						rpt.setF6(Currency.formatAmount(info.getBeginningNo()));
						rpt.setF7((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
						
						//change above value if ct2!=2000 or ct5!=10000
						if(info.getIssuedForm().getEndingNo()!=info.getBeginningNo()) {
							
							rpt.setF3(Currency.formatAmount(info.getBeginningNo()));
							rpt.setF4((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
							
							rpt.setF6("");
							rpt.setF7("");
						}
						
						
					}else {
						//beginning
						rpt.setF2("");
						rpt.setF3(Currency.formatAmount(info.getBeginningNo()));
						rpt.setF4((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
						
						//Receipt
						rpt.setF5("");
						rpt.setF6("");
						rpt.setF7("");
					}
					
					
					//issued
					rpt.setF8("");
					rpt.setF9("No Iss.");
					rpt.setF10("");
					
					
					//ending balance
					rpt.setF11("");
					rpt.setF12(Currency.formatAmount(info.getBeginningNo()));
					rpt.setF13((info.getIssuedForm().getStabNo()==0? "" : "#"+info.getIssuedForm().getStabNo()));
					
					//remarks
					rpt.setF14("");
				}
			}
		}
		
		return rpt;
	}
	
	private Form11Report collectLastYearFormIssued(IssuedForm isform, Form11Report rpt) {
		
		String[] params = new String[3];
		String sql = " AND frm.isactivecol=1 AND frm.fundid=? AND cl.isid=? AND sud.logid=? ORDER BY frm.rptgroup DESC LIMIT 1";	
		params[0] = isform.getFundId()+"";
		params[1] = isform.getCollector().getId()+"";
		params[2] = isform.getId()+"";
		
		rpt.setF1(FormType.nameId(isform.getFormType()));
		
		List<CollectionInfo> ins = CollectionInfo.retrieve(sql, params);
		
		if(isform.getFormType()<9) {
		
		if(ins.size()>0) {
			CollectionInfo info = ins.get(0);
			long newCountRem = isform.getEndingNo() - info.getEndingNo();
			
			rpt.setF2(newCountRem+"");
			long newEnding = info.getEndingNo() + 1;
			
			rpt.setF3(DateUtils.numberResult(isform.getFormType(),newEnding));
			rpt.setF4(DateUtils.numberResult(isform.getFormType(),isform.getEndingNo()));
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
			
			rpt.setF8("");
			rpt.setF9("No Iss.");
			rpt.setF10("");
			
			
			rpt.setF11(newCountRem+"");
			rpt.setF12(DateUtils.numberResult(isform.getFormType(),newEnding));
			rpt.setF13(DateUtils.numberResult(isform.getFormType(),isform.getEndingNo()));
			
		}else {
			rpt.setF2("50");
			rpt.setF3(DateUtils.numberResult(isform.getFormType(),isform.getBeginningNo()));
			rpt.setF4(DateUtils.numberResult(isform.getFormType(),isform.getEndingNo()));
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
			
			rpt.setF8("");
			rpt.setF9("No Iss.");
			rpt.setF10("");
			
			
			rpt.setF11("50");
			rpt.setF12(DateUtils.numberResult(isform.getFormType(),isform.getBeginningNo()));
			rpt.setF13(DateUtils.numberResult(isform.getFormType(),isform.getEndingNo()));
		}
		
		}else {
		//change the value if the form is Cash ticket
		//FormType.CT_2 || FormType.CT_5
			
			System.out.println("Cash ticket previous years >> " + isform.getFormTypeName());
			
			rpt.setF1(FormType.nameId(isform.getFormType()));
			double amount = 0d;
				
			if(ins.size()>0) {
				CollectionInfo in = ins.get(0);
				amount = in.getBeginningNo();
			}
			
			String f4 = isform.getStabNo()==0? "" : "#"+isform.getStabNo();
			
			//beginning
			rpt.setF2("");
			rpt.setF3(Currency.formatAmount(amount));
			rpt.setF4(f4);
								
			//Receipt
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");	
			
			//issued
			rpt.setF8("");
			rpt.setF9("No Iss.");
			rpt.setF10("");	
			
			//ending balance
			rpt.setF11("");
			rpt.setF12(Currency.formatAmount(amount));
			rpt.setF13(f4);
			
			//remarks
			rpt.setF14("");
		//}
		}
		
		
		return rpt;
	}
	
	public Form11Report reportIssued(IssuedForm isform) {
		
		System.out.println("reportIssued>>>");
		String[] dateForm = isform.getIssuedDate().split("-");
		int yearForm = Integer.valueOf(dateForm[0]);
		int month = Integer.valueOf(dateForm[1]);
		
		Form11Report rpt = new Form11Report();
		
		boolean currentYear = true;
		if(DateUtils.getCurrentYear()!=yearForm) { 
			System.out.println("year not match for form >> " + yearForm);
			rpt = collectLastYearFormIssued(isform, rpt);
			currentYear = false;
		}
		
		if(currentYear) {
		
		String f3 = "";
		String f4 = "";
		String f6 = "";
		String f7 = "";
		String f9 = "";
		String f10 = "";
		String f12 = "";
		String f13 = "";
		
		if("Stock".equalsIgnoreCase(isform.getCollector().getName())) {
			rpt.setF1(FormType.nameId(isform.getFormType()));
			
			int logmonth = Integer.valueOf(isform.getIssuedDate().split("-")[1]);
			
			if(logmonth==getMonthId()) {
				
				rpt.setF2("");
				rpt.setF3("");
				rpt.setF4("");
				
				String be1= isform.getBeginningNo()+"";
				int be2 = be1.length();
				rpt.setF5(isform.getPcs()+"");
				f6 = be2==7? "0"+be1 : be1;
				rpt.setF6(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f6)));
				//rpt.setF6(be2==7? "0"+be1 : be1);
				
				String en1= isform.getEndingNo()+"";
				int en2 = en1.length();
				f7 = en2==7? "0"+en1 : en1;
				rpt.setF7(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f7)));
				//rpt.setF7(en2==7? "0"+en1 : en1);
				
			}else {
			
				String be1= isform.getBeginningNo()+"";
				int be2 = be1.length();
				rpt.setF2(isform.getPcs()+"");
				f3 = be2==7? "0"+be1 : be1;
				rpt.setF3(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f3)));
				//rpt.setF3(be2==7? "0"+be1 : be1);
				
				String en1= isform.getEndingNo()+"";
				int en2 = en1.length();
				f4 = en2==7? "0"+en1 : en1;
				rpt.setF4(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f4)));
				//rpt.setF4(en2==7? "0"+en1 : en1);
				
				rpt.setF5("");
				rpt.setF6("");
				rpt.setF7("");
			
			}
			
			//issued
			rpt.setF8("");
			rpt.setF9("");
			rpt.setF10("");
			
			
			//ending balance
			
			
			rpt.setF11("");
			rpt.setF12("");
			rpt.setF13("");
			
			//remarks
			rpt.setF14("");
			
			Collector col = Collector.retrieve(isform.getCollector().getId());
			if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
				rpt.setF15(col.getName());
			}else {
				rpt.setF15(col.getDepartment().getDepartmentName());
			}
			
		}else {
		
		rpt.setF1(FormType.nameId(isform.getFormType()));
		
		int logmonth = Integer.valueOf(isform.getIssuedDate().split("-")[1]);
		int logDay = Integer.valueOf(isform.getIssuedDate().split("-")[2]);
		
		if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
			//write in receipt
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			
			String be1= isform.getBeginningNo()+"";
			int be2 = be1.length();
			rpt.setF5(isform.getPcs()+"");
			f6 = be2==7? "0"+be1 : be1;
			rpt.setF6(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f6)));
			//rpt.setF6(be2==7? "0"+be1 : be1);
				
			String en1= isform.getEndingNo()+"";
			int en2 = en1.length();
			f7 = en2==7? "0"+en1 : en1;
			rpt.setF7(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f7)));
			//rpt.setF7(en2==7? "0"+en1 : en1);
		
			
			
		}else {
		//Write in beginning balance
			String be1= isform.getBeginningNo()+"";
			int be2 = be1.length();
			rpt.setF2(isform.getPcs()+"");
			f3 = be2==7? "0"+be1 : be1;
			rpt.setF3(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f3)));
			//rpt.setF3(be2==7? "0"+be1 : be1);
			
			String en1= isform.getEndingNo()+"";
			int en2 = en1.length();
			f4 = en2==7? "0"+en1 : en1;
			rpt.setF4(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f4)));
			//rpt.setF4(en2==7? "0"+en1 : en1);
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
		}
		
		//issued
		rpt.setF8("");
		rpt.setF9("No Iss.");
		rpt.setF10("");
		
		
		//ending balance
		
		
		rpt.setF11(isform.getPcs()+"");
		
		String enbeg1= isform.getBeginningNo()+"";
		int enbeg2 = enbeg1.length();
		f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
		rpt.setF12(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f12)));
		//rpt.setF12(enbeg2==7? "0"+enbeg1 : enbeg1);
		
		String enending1= isform.getEndingNo()+"";
		int enending2 = enending1.length();
		f13 = enending2==7? "0"+enending1 : enending1;
		rpt.setF13(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f13)));
		//rpt.setF13(enending2==7? "0"+enending1 : enending1);
		
		//remarks
		rpt.setF14("");
		
		Collector col = Collector.retrieve(isform.getCollector().getId());
		if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
			rpt.setF15(col.getName());
		}else {
			rpt.setF15(col.getDepartment().getDepartmentName());
		}
		
		}
		
		//change the value if the form is Cash ticket
		if(FormType.CT_2.getId()==isform.getFormType() || FormType.CT_5.getId()==isform.getFormType()) {
			
			int logmonth = Integer.valueOf(isform.getIssuedDate().split("-")[1]);
			int logDay = Integer.valueOf(isform.getIssuedDate().split("-")[2]);
			
						rpt.setF1(FormType.nameId(isform.getFormType()));
						
						
						double amount = 0d;
						if(FormType.CT_2.getId()==isform.getFormType()) {
							amount = isform.getPcs() * 2;
						}else if(FormType.CT_5.getId()==isform.getFormType()) {
							amount = isform.getPcs() * 5;
						}
						
						if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
							//beginning
							rpt.setF2("");
							rpt.setF3("");
							rpt.setF4("");
							
							//Receipt
							rpt.setF5("");
							rpt.setF6(Currency.formatAmount(amount));
							rpt.setF7((isform.getStabNo()==0? "" : "#"+isform.getStabNo()));
						}else {
							//beginning
							rpt.setF2("");
							rpt.setF3(Currency.formatAmount(amount));
							rpt.setF4((isform.getStabNo()==0? "" : "#"+isform.getStabNo()));
							
							//Receipt
							rpt.setF5("");
							rpt.setF6("");
							rpt.setF7("");
						}
						
						
						//issued
						rpt.setF8("");
						rpt.setF9("No Iss.");
						rpt.setF10("");
						
						
						//ending balance
						rpt.setF11("");
						rpt.setF12(Currency.formatAmount(amount));
						rpt.setF13((isform.getStabNo()==0? "" : "#"+isform.getStabNo()));
						
						//remarks
						rpt.setF14("");
		}
		
		}
		
		return rpt;
	
	}
	
	public void deleteRow(CollectionInfo in) {
		
		if(in.getId()==0) {
			newForms.remove(in);
			recalCulateFormAmount();
			if(getMapIssued()!=null && getMapIssued().size()>0 && getMapIssued().containsKey(in.getIssuedForm().getId())) {
				getMapIssued().remove(in.getIssuedForm().getId());
			}
			reloadAvailableForms();
			Application.addMessage(1, "Success", "Successfully deleted");
		}else {
			if(in.getReceivedDate().equalsIgnoreCase(DateUtils.getCurrentDateYYYYMMDD())) {//check if form is in current date issued
				IssuedForm is = IssuedForm.retrieveId(in.getIssuedForm().getId());
				if(FormStatus.ALL_ISSUED.getId()==is.getStatus()) {
					is.setStatus(FormStatus.HANDED.getId());
					is.save();
				}
				
				
				Date date= DateUtils.convertDateString(in.getReceivedDate(), "yyyy-MM-dd");
				setFundSearchId(in.getFundId());
				setCollectorMapId(in.getCollector().getId());
				setDateFrom(date);
				setDateTo(date);
				
				in.delete();
				newForms.remove(in);
				recalCulateFormAmount();
				if(getMapIssued()!=null && getMapIssued().size()>0 && getMapIssued().containsKey(in.getIssuedForm().getId())) {
					getMapIssued().remove(in.getIssuedForm().getId());
				}
				reloadAvailableForms();
				init();
				Application.addMessage(1, "Success", "Successfully deleted");
			}else {
				Application.addMessage(3, "Error", "Sorry item cannot be deleted");
			}
		}
	}
	
	private void recalCulateFormAmount() {
		double amount = 0d;
		for(CollectionInfo i : newForms) {
			amount += i.getAmount();
		}
		setTotalAmount(Currency.formatAmount(amount));
	}
	
	
	public Date getReceivedDate() {
		if(receivedDate==null) {
			receivedDate = DateUtils.getDateToday();
		}
		return receivedDate;
	}
	
	public void printInternalCollectorSummary(String titleType) {
		if(getSelectedCollection()!=null && getSelectedCollection().size()>0) {
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME ="rcdallMonthly";
			
			if("all".equalsIgnoreCase(titleType)) {
				REPORT_NAME ="rcdperMonthly";
			}
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
			
			List<Rcd> reports = new ArrayList<Rcd>();
			double totalAmount = 0d;
			
			//combine all collector income per month
			Map<Long, CollectionInfo> mapColData = new HashMap<Long, CollectionInfo>();//Collections.synchronizedMap(new HashMap<Long, CollectionInfo>());
			for(CollectionInfo in : getSelectedCollection()) {
				long key = in.getCollector().getId();
				String[] date = in.getReceivedDate().split("-");
				String rptSeries = date[0]+"-"+date[1]+"-#"+(in.getRptGroup()<10? "00"+in.getRptGroup() : (in.getRptGroup()<100? "0"+in.getRptGroup(): in.getRptGroup()));
				if(mapColData!=null && mapColData.containsKey(key)) {
					double amount = mapColData.get(key).getAmount() + in.getAmount();
					String srs = mapColData.get(key).getRptFormat() + ", " + rptSeries;
					in.setRptFormat(srs);
					in.setAmount(amount);
					mapColData.put(key, in);
				}else {
					in.setRptFormat(rptSeries);
					mapColData.put(key, in);
				}
			}
			int cnt = 0;
			for(CollectionInfo in : mapColData.values()) {
				Rcd rpt = new Rcd();
				rpt.setF1(in.getCollector().getName().toUpperCase().replace("F.L LOPEZ-", ""));
				rpt.setF2(in.getRptFormat());
				rpt.setF3(Currency.formatAmount(in.getAmount()));
				totalAmount += in.getAmount();
				reports.add(rpt);
				cnt++;
			}
			
			
			for(int i=cnt; i<30; i++) {
				Rcd rpt = new Rcd();
				reports.add(rpt);
			}
			
			JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
	  		HashMap param = new HashMap();
	  		
	  		String title = "TOTAL COLLECTION FOR THE MONTH OF " + DateUtils.getMonthName(Integer.valueOf(getSelectedCollection().get(0).getReceivedDate().split("-")[1])).toUpperCase();
	  		
	  		if("collector".equalsIgnoreCase(titleType)) {
	  			title = "TOTAL COLLECTOR COLLECTION FOR THE MONTH OF " + DateUtils.getMonthName(Integer.valueOf(getSelectedCollection().get(0).getReceivedDate().split("-")[1])).toUpperCase();
	  		}else if("all".equalsIgnoreCase(titleType)) {
	  			//title = "TOTAL COLLECTION FOR THE MONTH OF " + DateUtils.getMonthName(Integer.valueOf(getSelectedCollection().get(0).getReceivedDate().split("-")[1])).toUpperCase();
	  			title = "For the month of " + DateUtils.getMonthName(Integer.valueOf(getSelectedCollection().get(0).getReceivedDate().split("-")[1])).toUpperCase();
	  		}
	  		
	  		System.out.println("report file : " + REPORT_NAME);
	  		
	  		title += ", " +  getSelectedCollection().get(0).getReceivedDate().split("-")[0];
	  		
	  		param.put("PARAM_TITLE", title);
			param.put("PARAM_TREASURER", "FERDINAND L. LOPEZ");
	  		param.put("PARAM_LIQUIDATING_OFFICER", "HENRY E. MAGBANUA");
	  		param.put("PARAM_TOTAL",Currency.formatAmount(totalAmount));
	  		
	  		String date = DateUtils.convertDateToMonthDayYear(DateUtils.getCurrentDateYYYYMMDD());
	  		
	  		param.put("PARAM_FUND", getSelectedCollection().get(0).getFundName().toUpperCase());
	  		param.put("PARAM_RPT_GROUP", getReportSeriesSummary().replace("#", ""));
	  		param.put("PARAM_PRINTED_DATE", date);
	  		param.put("PARAM_VERIFIED_DATE", date);
	  		
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
			            input = new BufferedInputStream(new FileInputStream(file),GlobalVar.DEFAULT_BUFFER_SIZE);

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
	}
	
	public void printAllRCD() {
		if(getSelectedCollection()!=null && getSelectedCollection().size()>0) {
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME ="rcdall";
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
			
			List<Rcd> reports = new ArrayList<Rcd>();//Collections.synchronizedList(new ArrayList<Rcd>());
			double totalAmount = 0d;
			
			int cnt = 0;
			int num = 1;
			for(CollectionInfo in : getSelectedCollection()) {
				Rcd rpt = new Rcd();
				
				rpt.setF1(num + ") "+in.getCollector().getName().toUpperCase().replace("F.L LOPEZ-", ""));
				rpt.setF2(in.getRptFormat());
				rpt.setF3(Currency.formatAmount(in.getAmount()));
				totalAmount += in.getAmount();
				reports.add(rpt);
				cnt++;
				num++;
			}
			
			for(int i=cnt; i<20; i++) {
				Rcd rpt = new Rcd();
				reports.add(rpt);
			}
			
			JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
	  		HashMap param = new HashMap();
	  		
			param.put("PARAM_TREASURER", "FERDINAND L. LOPEZ");
	  		param.put("PARAM_LIQUIDATING_OFFICER", "HENRY E. MAGBANUA");
	  		param.put("PARAM_TOTAL",Currency.formatAmount(totalAmount));
	  		
	  		String date = DateUtils.convertDateToMonthDayYear(DateUtils.convertDate(getSummaryDate(), "yyyy-MM-dd")); //DateUtils.convertDateToMonthDayYear(getSelectedCollection().get(0).getReceivedDate());
	  		//try{int index = getSelectedCollection().size()-1;
	  		//date = DateUtils.convertDateToMonthDayYear(getSelectedCollection().get(index).getReceivedDate());}catch(Exception e){ e.printStackTrace();}
	  		param.put("PARAM_FUND", getSelectedCollection().get(0).getFundName().toUpperCase());
	  		param.put("PARAM_RPT_GROUP", getReportSeriesSummary().replace("#", ""));
	  		param.put("PARAM_PRINTED_DATE", date);
	  		param.put("PARAM_VERIFIED_DATE", date);
	  		
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
	}
	public void printSummary() {
		if(getSelectedCollection()!=null && getSelectedCollection().size()>0) {
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME ="rcdsummary";
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
			
			List<Rcd> reports = new ArrayList<Rcd>();//Collections.synchronizedList(new ArrayList<Rcd>());
			double totalAmount = 0d;
			
			int cnt = 0;
			int num = 1;
			for(CollectionInfo in : getSelectedCollection()) {
				Rcd rpt = new Rcd();
				
				rpt.setF1(num + ") "+ in.getCollector().getName().toUpperCase().replace("F.L LOPEZ-", ""));
				rpt.setF2(in.getRptFormat());
				rpt.setF3(Currency.formatAmount(in.getAmount()));
				totalAmount += in.getAmount();
				reports.add(rpt);
				cnt++;
				num++;
			}
			
			for(int i=cnt; i<20; i++) {
				Rcd rpt = new Rcd();
				reports.add(rpt);
			}
			
			JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
	  		HashMap param = new HashMap();
	  		
			param.put("PARAM_TREASURER", "FERDINAND L. LOPEZ");
	  		param.put("PARAM_LIQUIDATOR_PERSON", "HENRY E. MAGBANUA");
	  		param.put("PARAM_TOTAL",Currency.formatAmount(totalAmount));
	  		
	  		String date = DateUtils.convertDateToMonthDayYear(DateUtils.convertDate(getSummaryDate(), "yyyy-MM-dd"));//DateUtils.convertDateToMonthDayYear(getSelectedCollection().get(0).getReceivedDate());
	  		//try{int index = getSelectedCollection().size()-1;
	  		//date = DateUtils.convertDateToMonthDayYear(getSelectedCollection().get(index).getReceivedDate());}catch(Exception e){}
	  		
	  		param.put("PARAM_FUND", getSelectedCollection().get(0).getFundName().toUpperCase());
	  		param.put("PARAM_RPT_GROUP", getReportSeriesSummary().replace("#", ""));
	  		param.put("PARAM_PRINTED_DATE", date);
	  		param.put("PARAM_VERIFIED_DATE", date);
	  		
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
	}
	
	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}
	
	public int getCollectorId() {
		return collectorId;
	}

	public void setCollectorId(int collectorId) {
		this.collectorId = collectorId;
	}

	public List getCollectors() {
		collectors = new ArrayList<>();
		collectors.add(new SelectItem(0, "Select Collector"));
		String sql = "";
		if(issuedCollectorId>0) {
			collectors = new ArrayList<>();
			sql = " AND cl.isid="+issuedCollectorId;
		}
		
		for(Collector col : Collector.retrieve(sql, new String[0])) {
			collectors.add(new SelectItem(col.getId(), col.getDepartment().getDepartmentName()+"/"+col.getName()));
		}
		
		
		
		return collectors;
	}

	public void setCollectors(List collectors) {
		this.collectors = collectors;
	}
	
	public int getFormTypeId() {
		if(formTypeId==0) {
			formTypeId=1;
		}
		return formTypeId;
	}
	public void setFormTypeId(int formTypeId) {
		this.formTypeId = formTypeId;
	}
	public List getFormTypes() {
		formTypes = new ArrayList<>();
		
		for(FormType form : FormType.values()) {
			formTypes.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
		}
		
		return formTypes;
	}
	public void setFormTypes(List formTypes) {
		this.formTypes = formTypes;
	}
	public long getIssuedId() {
		return issuedId;
	}

	public void setIssuedId(long issuedId) {
		this.issuedId = issuedId;
	}

	public List getIssueds() {
		return issueds;
	}

	public void setIssueds(List issueds) {
		this.issueds = issueds;
	}

	public long getBeginningNo() {
		return beginningNo;
	}

	public void setBeginningNo(long beginningNo) {
		this.beginningNo = beginningNo;
	}

	public long getEndingNo() {
		return endingNo;
	}

	public void setEndingNo(long endingNo) {
		this.endingNo = endingNo;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getTmpQty() {
		return tmpQty;
	}

	public void setTmpQty(int tmpQty) {
		this.tmpQty = tmpQty;
	}

	public List<CollectionInfo> getNewForms() {
		return newForms;
	}

	public void setNewForms(List<CollectionInfo> newForms) {
		this.newForms = newForms;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public IssuedForm getIssuedData() {
		return issuedData;
	}

	public void setIssuedData(IssuedForm issuedData) {
		this.issuedData = issuedData;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}


	public Map<String, CollectionInfo> getMaps() {
		return maps;
	}


	public void setMaps(Map<String, CollectionInfo> maps) {
		this.maps = maps;
	}


	public int getCollectorMapId() {
		return collectorMapId;
	}


	public void setCollectorMapId(int collectorMapId) {
		this.collectorMapId = collectorMapId;
	}

	
	public List getCollectorsMap() {
		collectorsMap = new ArrayList<>();
		collectorsMap.add(new SelectItem(0, "Select Collector"));
		collectotData = new HashMap<Integer, Collector>();//Collections.synchronizedMap(new HashMap<Integer, Collector>());
		
		Collector co = new Collector();
		co.setId(0);
		collectotData.put(0, co);
		String sql = "";
		if(issuedCollectorId>0) {
			collectorsMap = new ArrayList<>();
			sql = " AND cl.isid="+issuedCollectorId;
		}
		for(Collector col : Collector.retrieve(sql, new String[0])) {
			collectorsMap.add(new SelectItem(col.getId(), col.getDepartment().getDepartmentName()+"/"+col.getName()));
			collectotData.put(col.getId(), col);
		}
		return collectorsMap;
	}


	public void setCollectorsMap(List collectorsMap) {
		this.collectorsMap = collectorsMap;
	}


	public List<CollectionInfo> getInfos() {
		return infos;
	}


	public void setInfos(List<CollectionInfo> infos) {
		this.infos = infos;
	}
	
	public int getMonthId() {
		if(monthId==0) {
			monthId = DateUtils.getCurrentMonth();
		}
		return monthId;
	}

	public void setMonthId(int monthId) {
		this.monthId = monthId;
	}
	/*
	public List getMonths() {
		months = new ArrayList<>();
		months.add(new SelectItem(0, "All Months"));
		for(int m=1; m<=12;m++) {
			months.add(new SelectItem(m, DateUtils.getMonthName(m)));
		}
		return months;
	}

	public void setMonths(List months) {
		this.months = months;
	}*/


	public Map<Integer, Collector> getCollectotData() {
		return collectotData;
	}


	public void setCollectotData(Map<Integer, Collector> collectotData) {
		this.collectotData = collectotData;
	}


	public CollectionInfo getSelectedCollectionData() {
		return selectedCollectionData;
	}


	public void setSelectedCollectionData(CollectionInfo selectedCollectionData) {
		this.selectedCollectionData = selectedCollectionData;
	}


	public String getPuj() {
		return puj;
	}


	public void setPuj(String puj) {
		this.puj = puj;
	}


	public String getPedller() {
		return pedller;
	}


	public void setPedller(String pedller) {
		this.pedller = pedller;
	}


	public String getIsda() {
		return isda;
	}


	public void setIsda(String isda) {
		this.isda = isda;
	}


	public String getSkylab() {
		return skylab;
	}


	public void setSkylab(String skylab) {
		this.skylab = skylab;
	}


	public String getOthers() {
		return others;
	}


	public void setOthers(String others) {
		this.others = others;
	}
	public int getFundId() {
		if(fundId==0) {
			fundId = 1;
		}
		return fundId;
	}

	public void setFundId(int fundId) {
		this.fundId = fundId;
	}

	public List getFunds() {
		funds = new ArrayList<>();
		for(FundType f : FundType.values()) {
			funds.add(new SelectItem(f.getId(), f.getName()));
		}
		return funds;
	}

	public void setFunds(List funds) {
		this.funds = funds;
	}
	public int getFundSearchId() {
		return fundSearchId;
	}

	public void setFundSearchId(int fundSearchId) {
		this.fundSearchId = fundSearchId;
	}

	public List getFundsSearch() {
		fundsSearch = new ArrayList<>();
		fundsSearch.add(new SelectItem(0, "ALL FUNDS"));
		for(FundType f : FundType.values()) {
			fundsSearch.add(new SelectItem(f.getId(), f.getName()));
		}
		return fundsSearch;
	}

	public void setFundsSearch(List fundsSearch) {
		this.fundsSearch = fundsSearch;
	}


	public CollectionInfo getCashTicketData() {
		return cashTicketData;
	}


	public void setCashTicketData(CollectionInfo cashTicketData) {
		this.cashTicketData = cashTicketData;
	}


	/*
	 * public boolean isHasTicket() { return hasTicket; }
	 * 
	 * 
	 * public void setHasTicket(boolean hasTicket) { this.hasTicket = hasTicket; }
	 */


	public List<CollectionInfo> getSelectedCollection() {
		return selectedCollection;
	}


	public void setSelectedCollection(List<CollectionInfo> selectedCollection) {
		this.selectedCollection = selectedCollection;
	}


	public Date getDateFrom() {
		if(dateFrom==null) {
			dateFrom =  DateUtils.getDateToday();
		}	
		return dateFrom;
	}


	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}


	public Date getDateTo() {
		if(dateTo==null) {
			dateTo =  DateUtils.getDateToday();
		}
		return dateTo;
	}


	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	
	public void updateSeriesSummary() {
		AppSetting.updateSeries(getReportSeriesSummary());
	}
	
	public String getReportSeriesSummary() {
		if(reportSeriesSummary==null) {
			reportSeriesSummary=AppSetting.getReportSeries();
		}
		return reportSeriesSummary;
	}

	
	public void setReportSeriesSummary(String reportSeriesSummary) {
		this.reportSeriesSummary = reportSeriesSummary;
	}


	public String getPujLabel() {
		return pujLabel;
	}


	public void setPujLabel(String pujLabel) {
		this.pujLabel = pujLabel;
	}


	public String getPeddlerLabel() {
		return peddlerLabel;
	}


	public void setPeddlerLabel(String peddlerLabel) {
		this.peddlerLabel = peddlerLabel;
	}


	public String getIsdaLabel() {
		return isdaLabel;
	}


	public void setIsdaLabel(String isdaLabel) {
		this.isdaLabel = isdaLabel;
	}


	public String getSkylabLabel() {
		return skylabLabel;
	}


	public void setSkylabLabel(String skylabLabel) {
		this.skylabLabel = skylabLabel;
	}


	public Date getSummaryDate() {
		if(summaryDate==null) {
			summaryDate = DateUtils.getDateToday();
		}
		return summaryDate;
	}


	public void setSummaryDate(Date summaryDate) {
		this.summaryDate = summaryDate;
	}


	public Date getPerReportDate() {
		if(perReportDate==null) {
			perReportDate = DateUtils.getDateToday();
		}
		return perReportDate;
	}


	public void setPerReportDate(Date perReportDate) {
		this.perReportDate = perReportDate;
	}


	public CollectionInfo getCollectionPrint() {
		return collectionPrint;
	}


	public void setCollectionPrint(CollectionInfo collectionPrint) {
		this.collectionPrint = collectionPrint;
	}


	public boolean isUseModifiedDate() {
		return useModifiedDate;
	}


	public void setUseModifiedDate(boolean useModifiedDate) {
		this.useModifiedDate = useModifiedDate;
	}

	public boolean isCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(boolean currentDate) {
		this.currentDate = currentDate;
	}

	public Map<Long, CollectionInfo> getMapIssued() {
		return mapIssued;
	}

	public void setMapIssued(Map<Long, CollectionInfo> mapIssued) {
		this.mapIssued = mapIssued;
	}

	public int getRtsId() {
		return rtsId;
	}

	public void setRtsId(int rtsId) {
		this.rtsId = rtsId;
	}

	public List getRts() {
		rts = new ArrayList<>();
		
		rts.add(new SelectItem(0, "No"));
		rts.add(new SelectItem(1, "Yes"));
		
		return rts;
	}

	public void setRts(List rts) {
		this.rts = rts;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getMarketLablel() {
		return marketLablel;
	}

	public void setMarketLablel(String marketLablel) {
		this.marketLablel = marketLablel;
	}

	public String getTotalCashTicket() {
		return totalCashTicket;
	}

	public void setTotalCashTicket(String totalCashTicket) {
		this.totalCashTicket = totalCashTicket;
	}

	public boolean isEnableCalendarPrint() {
		return enableCalendarPrint;
	}

	public void setEnableCalendarPrint(boolean enableCalendarPrint) {
		this.enableCalendarPrint = enableCalendarPrint;
	}

	public String getSearchSeries() {
		return searchSeries;
	}

	public void setSearchSeries(String searchSeries) {
		this.searchSeries = searchSeries;
	}

	public List<IssuedForm> getIssuedForms() {
		return issuedForms;
	}

	public void setIssuedForms(List<IssuedForm> issuedForms) {
		this.issuedForms = issuedForms;
	}

	public List<CollectionInfo> getIssuedSeries() {
		return issuedSeries;
	}

	public void setIssuedSeries(List<CollectionInfo> issuedSeries) {
		this.issuedSeries = issuedSeries;
	}

	public List<CollectionInfo> getFixIssuedSeries() {
		return fixIssuedSeries;
	}

	public void setFixIssuedSeries(List<CollectionInfo> fixIssuedSeries) {
		this.fixIssuedSeries = fixIssuedSeries;
	}

	public String getAvailableSeries() {
		return availableSeries;
	}

	public void setAvailableSeries(String availableSeries) {
		this.availableSeries = availableSeries;
	}

	public String getChangesNumbers() {
		return changesNumbers;
	}

	public void setChangesNumbers(String changesNumbers) {
		this.changesNumbers = changesNumbers;
	}

	public boolean isAlreadyRetrieve() {
		return alreadyRetrieve;
	}

	public int getIssuedCollectorId() {
		return issuedCollectorId;
	}

	public void setAlreadyRetrieve(boolean alreadyRetrieve) {
		this.alreadyRetrieve = alreadyRetrieve;
	}

	public void setIssuedCollectorId(int issuedCollectorId) {
		this.issuedCollectorId = issuedCollectorId;
	}

	public int getFormTypeIdSearch() {
		return formTypeIdSearch;
	}

	public List getFormTypesSearch() {
		return formTypesSearch;
	}

	public void setFormTypeIdSearch(int formTypeIdSearch) {
		this.formTypeIdSearch = formTypeIdSearch;
	}

	public void setFormTypesSearch(List formTypesSearch) {
		this.formTypesSearch = formTypesSearch;
	}

	public List<CollectionInfo> getFormsInfo() {
		return formsInfo;
	}

	public void setFormsInfo(List<CollectionInfo> formsInfo) {
		this.formsInfo = formsInfo;
	}

	public String getTotalAmountPerForms() {
		return totalAmountPerForms;
	}

	public void setTotalAmountPerForms(String totalAmountPerForms) {
		this.totalAmountPerForms = totalAmountPerForms;
	}

	public Date getFromFormDate() {
		if(fromFormDate==null) {
			fromFormDate = DateUtils.getDateToday();
		}
		return fromFormDate;
	}

	public Date getToFormDate() {
		if(toFormDate==null) {
			toFormDate = DateUtils.getDateToday();
		}
		return toFormDate;
	}

	public void setFromFormDate(Date fromFormDate) {
		this.fromFormDate = fromFormDate;
	}

	public void setToFormDate(Date toFormDate) {
		this.toFormDate = toFormDate;
	}

	public int getCollectorIdSearch() {
		return collectorIdSearch;
	}

	public List getCollectorSearch() {
		return collectorSearch;
	}

	public void setCollectorIdSearch(int collectorIdSearch) {
		this.collectorIdSearch = collectorIdSearch;
	}

	public void setCollectorSearch(List collectorSearch) {
		this.collectorSearch = collectorSearch;
	}


	
}

