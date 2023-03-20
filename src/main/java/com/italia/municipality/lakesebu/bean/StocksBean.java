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

import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.Division;
import com.italia.municipality.lakesebu.controller.Form11Report;
import com.italia.municipality.lakesebu.controller.IssuedForm;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.controller.Requisition;
import com.italia.municipality.lakesebu.controller.RequisitionIssueSlip;
import com.italia.municipality.lakesebu.controller.Stocks;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.enm.StockStatus;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.reports.RisRpt;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @Since 03/08/2019
 * @version 1.0
 *
 */
@Named
@ViewScoped
public class StocksBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1546876970435L;
	
	private Date recordedDate;
	private int numberOfStab;
	private String seriesFrom;
	private String seriesTo;
	private int stabNo;
	private Stocks stockData;
	private List<Stocks> stocks;
	
	private int formTypeId;
	private List formType;
	
	private int formTypeIdSearch;
	private List formTypeSearch;
	
	@Getter @Setter private int fundId;
	@Getter @Setter private List funds;
	@Getter @Setter private int collectorId;
	@Getter @Setter private List collectors;
	@Getter @Setter List<Form11Report> forms;
	@Getter @Setter private String searchSeries;
	
	//////////////////////////////////////////////////CREATE REQUISITION//////////////////////////////////////////
	@Getter @Setter private RequisitionIssueSlip slip;
	@Getter @Setter private List<Stocks> stockSelected;
	@Getter @Setter private Map<Long, Offices> mapDepCode;
	@Getter @Setter private Map<Integer, Offices> mapDeps;
	@Getter @Setter private int tabSelected;
	@Getter @Setter private List<RequisitionIssueSlip> slips;
	@Getter @Setter private List<Requisition> requesitionData;
	@Getter @Setter private Map<Integer, Collector> mapCollector;
	
	@PostConstruct
	public void init() {
		setTabSelected(0);
		stockInit();
		initCreateSlip();
	}
	
	public void stockInit() {
		stocks = new ArrayList<Stocks>();
		forms = new ArrayList<Form11Report>();
		
		String sql = " AND cl.isid=0 AND st.isactivestock=1 AND (st.statusstock="+ StockStatus.NOT_HANDED.getId() +" OR st.statusstock="+ StockStatus.PARTIAL_ISSUED.getId() + ")";
		String[] params = new String[0];
		
		if(getFormTypeIdSearch()>0 && getTabSelected()==0) {
				sql += "  AND st.formType="+getFormTypeIdSearch();
		}
		
		sql +=" ORDER BY st.datetrans ASC";
		
		stocks = Stocks.retrieve(sql, params);
		loadOthers();
	}
	
	private void loadOthers() {
		collectors = new ArrayList<>();
		//collectors.add(new SelectItem(0, "Select Collector"));
		String sql = "";
		for(Collector col : Collector.retrieve(sql, new String[0])) {
			collectors.add(new SelectItem(col.getId(), col.getDepartment().getDepartmentName()+"/"+col.getName()));
		}
		
		funds = new ArrayList<>();
		setFundId(1);
		//funds.add(new SelectItem(0, "All Funds"));
		for(FundType f : FundType.values()) {
			funds.add(new SelectItem(f.getId(), f.getName()));
		}
	}
	
	public void loadIssuedForm() {
		String sql = " AND frm.fundid=? AND frm.formstatus=1 AND cl.isid=?";
		String[] params = new String[2];
		params[0] = getFundId()+"";
		params[1] = getCollectorId()+"";
		
		/*
		 * if(getCollectorId()==0) { sql = " AND frm.fundid=? AND frm.formstatus=1 ";
		 * params = new String[1]; params[0] = getFundId()+""; }
		 */
		
		if(getSearchSeries()!=null && !getSearchSeries().isEmpty()) {
			sql += " AND ( frm.beginningNoLog like '%"+ getSearchSeries() +"%' OR frm.endingNoLog like '%"+ getSearchSeries() +"%' OR cl.collectorname like '%"+ getSearchSeries() +"%')";
		}
		
		forms = new ArrayList<Form11Report>();
		List<IssuedForm> iss = IssuedForm.retrieve(sql, params);
		if(iss!=null && iss.size()>1) {
			for(IssuedForm form : iss) {
				loadLatestSeries(form.getId(),form.getEndingNo(), forms);
			}
		}else if(iss!=null && iss.size()==1) {
				IssuedForm form = iss.get(0);
				loadLatestSeries(form.getId(),form.getEndingNo(),forms);
		}		
	}
	
	public void loadLatestSeries(long issuedId, long beginnngNo,List<Form11Report> forms) {
		String sql = " AND frm.fundid=? AND cl.isid=? AND sud.logid=? ORDER BY frm.colid DESC limit 1";
		
		String[] params = new String[3];
		params[0] = getFundId()+"";
		params[1] = getCollectorId()+"";
		params[2] = issuedId+"";
		Form11Report form11 = new Form11Report();
		List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
		if(infos!=null && infos.size()>0) {
			CollectionInfo info = infos.get(0);
			form11.setF6(info.getIssuedForm().getIssuedDate() + " (" + DateUtils.getNumberyDaysNow(info.getIssuedForm().getIssuedDate()) + " days)");
			form11.setF1(info.getCollector().getName());
			form11.setF2(FormType.nameId(info.getFormType()));
			long start = info.getEndingNo()+1;
			form11.setF3(start+"");
			form11.setF4(info.getIssuedForm().getEndingNo()+"");
			long qty =  info.getIssuedForm().getEndingNo() - start;  //info.getIssuedForm().getEndingNo() - beginnngNo;
			qty +=1;
			form11.setF5(qty+"");
			
			if(getFormTypeId()==9 || getFormTypeId()==10) {
				
				long beg =   info.getBeginningNo();
				long to =  info.getEndingNo();
				
				qty = 0;
				
				if(FormType.CT_2.getId()==info.getFormType()) {
					qty = beg / 2;
				}else if(FormType.CT_5.getId()==info.getFormType()) {
					qty = beg / 5;
				} 
				
				form11.setF3(beg+"");
				form11.setF4(to+"");
				form11.setF5(qty+"");
			}
			
		}else {
			
			sql = " AND frm.fundid=? AND frm.formstatus=1 AND cl.isid=? AND frm.logid=?";
			params = new String[3];
			params[0] = getFundId()+"";
			params[1] = getCollectorId()+"";
			params[2] = issuedId+"";
			
			List<IssuedForm> isforms = IssuedForm.retrieve(sql, params);
			
			IssuedForm isfrm = isforms.get(0);
			form11.setF6(isfrm.getIssuedDate() + " (" + DateUtils.getNumberyDaysNow(isfrm.getIssuedDate()) + " days)");
			form11.setF1(isfrm.getCollector().getName());
			form11.setF2(FormType.nameId(isfrm.getFormType()));
			
			form11.setF3(isfrm.getBeginningNo()+"");
			form11.setF4(isfrm.getEndingNo()+"");
			form11.setF5(isfrm.getPcs()+"");
			
			//if(getFormTypeId()>8) {
			if(getFormTypeId()==9 || getFormTypeId()==10) {
				long beg =   isfrm.getBeginningNo();
				long to =  isfrm.getEndingNo();
				
				long qty = 0;
				
				if(FormType.CT_2.getId()==isfrm.getFormType()) {
					qty = beg / 2;
				}else if(FormType.CT_5.getId()==isfrm.getFormType()) {
					qty = beg / 5;
				} 
				
				form11.setF3(beg+"");
				form11.setF4(to+"");
				form11.setF5(qty+"");
				
			}
		}
		forms.add(form11);
	}
	
	public void generateSeries() {
		
		long series = 0;
		
		try{series = Long.valueOf(getSeriesFrom().replace(",", ""));}catch(Exception e) {}
		System.out.println("Form type " + FormType.nameId(getFormTypeId()));
		System.out.println(">>> stab " + getNumberOfStab());
		if(getFormTypeId()<=8 || getFormTypeId()>=11) {
			System.out.println("If " + getFormTypeId());
			if(getNumberOfStab()>1) {
				
				series += 50 * getNumberOfStab();
				series -=1;
			}else {
				series += 49;
			}
			
			String newSeries = DateUtils.numberResult(getFormTypeId(), series);
			
			setSeriesTo(newSeries);
		
			
		}else {// for Cash ticket
			setSeriesFrom(1+"");
			System.out.println("if else " + getFormTypeId());
			if(getNumberOfStab()>1) {
				
				series += 2000 * getNumberOfStab();
				series -= 1;
				
				System.out.println("series>>> " + series);
			}else {
				setNumberOfStab(1);
				//series += 1999;
				series = 2000;
				
				System.out.println("else series>>> " + series);
			}
			
			setSeriesTo(series+"");
		}
		
		
		
		/*long seriesFrom = 0;
		long seriesTo = 0;
		
		try {
		seriesFrom = Long.valueOf(getSeriesFrom());
		seriesTo = Long.valueOf(getSeriesTo());
		}catch(Exception e) {}*/
		
	}
	
	public void saveData() {
		
		Stocks st = new Stocks();
		
		if(getStockData()!=null) {
			st = getStockData();
			setNumberOfStab(1);
		}
		
		boolean isOk = true;
		
		if(getNumberOfStab()==0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide number of stab");
		}
		
		if(getFormTypeId()<=8) {
		
			if(getSeriesFrom()==null || getSeriesFrom().isEmpty()) {
				isOk = false;
				Application.addMessage(3, "Error", "Please provide start of series");
			}
		
		}
		
		if(isOk && getStockData()==null && Stocks.isExistedSeries(getSeriesFrom(), getFormTypeId())) {
			isOk = false;
			Application.addMessage(3, "Error", "Series is already existed");
		}
		
		if(isOk) {
		
		if(FormType.CT_2.getId()==getFormTypeId() || FormType.CT_5.getId()==getFormTypeId()) {
			
			if(getNumberOfStab()>1) {
				 
				int stab = getStabNo();
				for(long i=1; i<=getNumberOfStab(); i++) {
					
						st = new Stocks();
						st.setIsActive(1);
						st.setDateTrans(DateUtils.convertDate(getRecordedDate(), "yyyy-MM-dd"));
						st.setSeriesFrom("1");
						st.setSeriesTo("2000");
						st.setFormType(getFormTypeId());
						st.setStatus(1);//not issued
						st.setCollector(null);
						st.setQuantity(2000);
						st.setStabNo(stab);
						st.save();
						stab++;
				}
			}else {
				st.setIsActive(1);
				st.setDateTrans(DateUtils.convertDate(getRecordedDate(), "yyyy-MM-dd"));
				st.setSeriesFrom(getSeriesFrom());
				st.setSeriesTo(getSeriesTo());
				st.setFormType(getFormTypeId());
				st.setStatus(1);//not issued
				st.setCollector(null);
				st.setQuantity(Integer.valueOf(getSeriesTo()));
				st.setStabNo(getStabNo());
				st.save();
			}
					setFormTypeIdSearch(getFormTypeId());
					createNew();
					//init();
					stockInit();
			Application.addMessage(1, "Success", "Successfully saved.");
			
		}else {
		
		long seriesFrom = Long.valueOf(getSeriesFrom());
		long seriesTo = Long.valueOf(getSeriesTo());
			
		if(getNumberOfStab()>1) {
			
			
			int cnt = 1;
			long from = 0;
			int stab = getStabNo();
			for(long i=seriesFrom; i<=seriesTo; i++) {
				
				//System.out.println("cnt>>> " + cnt);
				
				if(cnt==1) {
					from = i;
				}
				
				if(cnt==50) {
					
					//System.out.println(DateUtils.numberResult(getFormTypeId(), from) + "-" + DateUtils.numberResult(getFormTypeId(), i));
					st = new Stocks();
					st.setIsActive(1);
					st.setDateTrans(DateUtils.convertDate(getRecordedDate(), "yyyy-MM-dd"));
					st.setSeriesFrom(DateUtils.numberResult(getFormTypeId(), from));
					st.setSeriesTo(DateUtils.numberResult(getFormTypeId(), i));
					st.setFormType(getFormTypeId());
					st.setStatus(1);//not issued
					st.setCollector(null);
					st.setQuantity(50);
					st.setStabNo(stab);
					st.save();
					
					cnt=1;
					stab++;
				}else {
					cnt++;
					
				}
				
				
			}
			
			
			setFormTypeIdSearch(getFormTypeId());
			createNew();
			//init();
			stockInit();
			Application.addMessage(1, "Success", "Successfully saved.");
		}else {
			
			long is49 = seriesTo - seriesFrom;
			
			if(is49==49) {
			
			st.setIsActive(1);
			st.setDateTrans(DateUtils.convertDate(getRecordedDate(), "yyyy-MM-dd"));
			st.setSeriesFrom(getSeriesFrom());
			st.setSeriesTo(getSeriesTo());
			st.setFormType(getFormTypeId());
			st.setStatus(1);//not issued
			st.setCollector(null);
			st.setQuantity(50);
			st.setStabNo(getStabNo());
			st.save();
			
			setFormTypeIdSearch(getFormTypeId());
			createNew();
			//init();
			stockInit();
			Application.addMessage(1, "Success", "Successfully saved.");
			}else {
				Application.addMessage(3, "Error", "Please input the correct range of of series which is corresponding to 50 pieces");
			}
		}
		
		}
		}
	}
	
	public void clickItem(Stocks st) {
		setStockData(st);
		setFormTypeId(st.getFormType());
		setRecordedDate(DateUtils.convertDateString(st.getDateTrans(), "yyyy-MM-dd"));
		setStabNo(st.getStabNo());
		if(st.getFormType()<=8) {
			setNumberOfStab(1);
			setSeriesFrom(st.getSeriesFrom());
			setSeriesTo(st.getSeriesTo());
		}else {
			int qty = Integer.valueOf(st.getSeriesTo()) / 2000;
			setNumberOfStab(qty);
			setSeriesFrom(st.getSeriesFrom());
			setSeriesTo(st.getSeriesTo());
		}
		
		setFormTypeId(st.getFormType());
	}
	
	public void deleteRow(Stocks st) {
		st.delete();
		//init();
		stockInit();
		Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	public void createNew() {
		setStockData(null);
		setRecordedDate(null);
		setNumberOfStab(0);
		setSeriesFrom("");
		setSeriesTo("");
		setFormTypeId(1);
		setStabNo(0);
	}
	
	public Date getRecordedDate() {
		if(recordedDate==null) {
			recordedDate = DateUtils.getDateToday();
		}
		return recordedDate;
	}
	public void setRecordedDate(Date recordedDate) {
		this.recordedDate = recordedDate;
	}
	public int getNumberOfStab() {
		if(numberOfStab==0) {
			numberOfStab=1;
		}
		return numberOfStab;
	}
	public void setNumberOfStab(int numberOfStab) {
		this.numberOfStab = numberOfStab;
	}
	public String getSeriesFrom() {
		return seriesFrom;
	}
	public void setSeriesFrom(String seriesFrom) {
		this.seriesFrom = seriesFrom;
	}
	public String getSeriesTo() {
		return seriesTo;
	}
	public void setSeriesTo(String seriesTo) {
		this.seriesTo = seriesTo;
	}

	public List<Stocks> getStocks() {
		return stocks;
	}

	public void setStocks(List<Stocks> stocks) {
		this.stocks = stocks;
	}

	public Stocks getStockData() {
		return stockData;
	}

	public void setStockData(Stocks stockData) {
		this.stockData = stockData;
	}

	public List getFormType() {
		
		formType = new ArrayList<>();
		
		for(FormType form : FormType.values()) {
			formType.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
		}
		
		return formType;
	}

	public void setFormType(List formType) {
		this.formType = formType;
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

	public int getFormTypeIdSearch() {
		return formTypeIdSearch;
	}

	public void setFormTypeIdSearch(int formTypeIdSearch) {
		this.formTypeIdSearch = formTypeIdSearch;
	}

	public List getFormTypeSearch() {
		
		formTypeSearch = new ArrayList<>();
		formTypeSearch.add(new SelectItem(0, "All Forms"));
		for(FormType form : FormType.values()) {
			formTypeSearch.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
		}
		
		return formTypeSearch;
	}

	public void setFormTypeSearch(List formTypeSearch) {
		this.formTypeSearch = formTypeSearch;
	}

	public int getStabNo() {
		return stabNo;
	}

	public void setStabNo(int stabNo) {
		this.stabNo = stabNo;
	}
	
	
	
	//////////////////////////////////////////////////CREATE REQUISITION//////////////////////////////////////////
	private void initCreateSlip() {
		
		List divs = new ArrayList<>();
		divs.add(new SelectItem(0, "Select Division"));
		List reqs = new ArrayList<>();
		reqs.add(new SelectItem(0, "Select Requestor"));
		List deps = new ArrayList<>();
		deps.add(new SelectItem(0, "Select Office"));
		String series = slip.generateNewSaiNo();
		
		List funds = new ArrayList<>();
		funds.add(new SelectItem(0, "Select Item"));
		
		slip = RequisitionIssueSlip.builder()
				.requestNo(series)
				.saiNo(series)
				.tmpDateTrans(new Date())
				.requestors(reqs)
				.collector(Collector.builder().id(0).build())
				.divisions(divs)
				.division(Division.builder().id(0).build())
				.offices(deps)
				.office(Offices.builder().id(0).build())
				.approvedBy("Ferdinand L. Lopez")
				.approvedPosition("Municipal Treasurer")
				.issuedBy("Anita B. Sanang")
				.issuedPosition("Releasing Officer")
				.purspose("Collection")
				.isActive(1)
				.fundId(0)
				.funds(funds)
				.build();
	}
	
	public void positionUpdate() {
		
		Department dep = getMapCollector().get(slip.getCollector().getId()).getDepartment();
		slip.setCodeNo(getMapDeps().get(dep.getDepid()).getCode());
		slip.setOffice(Offices.builder().id(getMapDeps().get(dep.getDepid()).getId()).build());
		if(slip!=null && dep.getDepid()>1) {//((slip.getCollector().getId()>=1 && slip.getCollector().getId()<=20) || (slip.getCollector().getId()==38) || (slip.getCollector().getId()==41) )) {
			slip.setPosition("Barangay Treasurer");
			slip.setDivision(Division.builder().id(9).build());
		}else{
			slip.setPosition("Municipal Collector");
			slip.setDivision(Division.builder().id(1).build());
		};
	}
	
	public void selectedDep() {
		slip.setCodeNo(getMapDepCode().get(slip.getOffice().getId()).getCode());
		System.out.println("Department code: " + getMapDepCode().get(slip.getOffice().getId()).getCode());
	}
	
	 public void onTabChange(TabChangeEvent event) {
		 
		 System.out.println("current tab value: " + getTabSelected());
		 setStockSelected(new ArrayList<Stocks>());
		 if("Stocks".equalsIgnoreCase(event.getTab().getTitle())) {
			 stockInit();
			 setTabSelected(0);
		 }else if("Create Requisition Slip".equalsIgnoreCase(event.getTab().getTitle())) {
			 if(getTabSelected()==2) {//if tab from history
				 setTabSelected(1);
				 if(slip!=null && slip.getId()>0) {
					 RequisitionIssueSlip rq = slip;//put in temporary variable since below code will create default value//do not move this code
					 loadValueCreateSlip();
					 stockInit();//load form which is not yet turn over to collector
					 List<Stocks> tmpStocks = stocks;//list of stock not yet added to collector
					 stocks = new ArrayList<Stocks>();//clear the list
					 List<Stocks> stockFormOfCollector = RequisitionIssueSlip.stocksList(rq.getId());
					 setStockSelected(stockFormOfCollector);//check the already added form to collector
					 int num = 1;
					 for(Stocks s : stockFormOfCollector) {
						 s.setCount(num++);
						 stocks.add(s);
					 }
					 for(Stocks s : tmpStocks) {
						 s.setCount(num++);
						 stocks.add(s);
					 }
					// stocks.addAll(sotckFormOfCollector);//added to not yet added form list
					 
					 //slip = rq;
					 slip.setFundId(rq.getFundId());
					 slip.setCollector(rq.getCollector());
					 slip.setDivision(rq.getDivision());
					 slip.setOffice(rq.getOffice());
					 slip.setCodeNo(rq.getOffice().getCode());
					 slip.setIssuedBy(rq.getIssuedBy());
					 slip.setIssuedPosition(rq.getIssuedPosition());
					 slip.setApprovedBy(rq.getApprovedBy());
					 slip.setApprovedPosition(rq.getApprovedPosition());
					 slip.setPurspose(rq.getPurspose());
					 slip.setRequestNo(rq.getRequestNo());
					 slip.setSaiNo(rq.getSaiNo());
					 slip.setTmpDateTrans(DateUtils.convertDateString(rq.getDateTrans(), "yyyy-MM-dd"));
					 slip.setId(rq.getId());
					 slip.setIsActive(rq.getIsActive());
					 
				 }else {
					 loadValueCreateSlip();
					 stockInit();
				 }
			 }else {
				 setTabSelected(1);
				 loadValueCreateSlip();
				 stockInit();
			 }
		 }else if("RIS History".equalsIgnoreCase(event.getTab().getTitle())) {
			 setTabSelected(2);
			 loadHistory();
		 }
		 
	 }
	
	 private void loadValueCreateSlip() {
		 String series = slip.generateNewSaiNo();
		 
		 List funds = new ArrayList<>();
		 for(FundType f : FundType.values()) {
				funds.add(new SelectItem(f.getId(), f.getName()));
		 }
		 
		 List reqs = new ArrayList<>();
		 mapCollector = new LinkedHashMap<Integer, Collector>();
		 for(Collector c : Collector.retrieve(" ORDER BY cl.collectorname", new String[0])) {
			 reqs.add(new SelectItem(c.getId(), c.getName()));
			 mapCollector.put(c.getId(), c);
		 }
		 List divs = new ArrayList<>();
		 for(Division d : Division.retrieve(" ORDER BY divid", new String[0])) {
			 divs.add(new SelectItem(d.getId(), d.getName()));
		 }
		 List deps = new ArrayList<>();
		 mapDepCode = new LinkedHashMap<Long, Offices>();
		 mapDeps = new LinkedHashMap<Integer, Offices>();
		 boolean found=false;
		 String codeDefault="";
		 for(Offices dp : Offices.retrieve(" ORDER BY offid", new String[0])) {
			 deps.add(new SelectItem(dp.getId(), dp.getCode() +"-"+ dp.getName()));
			 mapDepCode.put(dp.getId(), dp);
			 mapDeps.put(dp.getDepartmentId(), dp);
			 if(!found) {
				 found=true;
				 codeDefault=dp.getCode(); //default code
			 }
		 }
		 
		 slip = RequisitionIssueSlip.builder()
					.requestNo(series)
					.codeNo(codeDefault)
					.saiNo(series)
					.tmpDateTrans(new Date())
					.requestors(reqs)
					.collector(Collector.builder().id(0).build())
					.position("Collector")
					.division(Division.builder().id(1).build())
					.divisions(divs)
					.division(Division.builder().id(0).build())
					.offices(deps)
					.office(Offices.builder().id(0).build())
					.approvedBy("Ferdinand L. Lopez")
					.approvedPosition("Municipal Treasurer")
					.issuedBy("Anita B. Sanang")
					.issuedPosition("Releasing Officer")
					.isActive(1)
					.purspose("Collection")
					.fundId(1)
					.funds(funds)
					.build();
	 }
	
	
	
	public void onCellEdit(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
	}
	
	public void onCellEditHis(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        if(event.getColumn().getHeaderText().equalsIgnoreCase("AF Form Type")) {
        	System.out.println("AF Form Type......");
        	getRequesitionData().get(index).getStocks().setFormTypeName(FormType.nameDescriptionId(Integer.valueOf(newValue+"")));
        }
	}
	
	public void saveRIS() {
		boolean isOk = true;
		//boolean isOldData = slip.getId()>0? true : false;
		if(slip.getCodeNo()==null && slip.getCodeNo().isEmpty()) {
			Application.addMessage(3, "Error", "Please provide code");
			isOk = false;
		}
		
		if(slip.getCollector().getId()==0) {
			Application.addMessage(3, "Error", "Please provide requestor");
			isOk = false;
		}
		
		if(slip.getPurspose()==null && slip.getPurspose().isEmpty()) {
			Application.addMessage(3, "Error", "Please provide purpose");
			isOk = false;
		}
		
		if(slip.getIssuedBy()==null && slip.getIssuedBy().isEmpty()) {
			Application.addMessage(3, "Error", "Please provide issued by");
			isOk = false;
		}
		
		if(getStockSelected()!=null && getStockSelected().size()==0) {
			Application.addMessage(3, "Error", "Please check form to release");
			isOk = false;
		}
		
		if(slip.getPosition()!=null && slip.getPosition().isBlank()) {
			Application.addMessage(3, "Error", "Please provide requestor position");
			isOk = false;
		}
		
		if(isOk) {
			//slip.save();
			//delete existing data first in requesition table before saving
			if(RequisitionIssueSlip.deleteFirstBeforeSavingRIS(slip.getId())) {
				String dateTrans = DateUtils.convertDate(slip.getTmpDateTrans(), "yyyy-MM-dd");
				boolean isForBarangay = false;
				/*if(slip.getDivision().getId()==1) {//MTO Collector
					slip.setPosition("Collector");
				}else if(slip.getDivision().getId()==9) {//Barangay Treasurer
					slip.setPosition("Barangay Treasurer");
					isForBarangay = true;
				}else {
					slip.setPosition("Collector");
				}*/
				//change above codes
				if(getMapCollector()!=null) {
					int depId = getMapCollector().get(slip.getCollector().getId()).getDepartment().getDepid();
					if(depId>1) {//1=Municipal LGU Collector else barangay treasurer
						isForBarangay = true;
					}
				}
		
				
				slip.setDateTrans(dateTrans);
				slip.setSaiDate(dateTrans);
				slip = RequisitionIssueSlip.save(slip);
				boolean isSuccess = false;
				
				for(Stocks s : getStockSelected()) {
					
					double totalCost =0d;
					if(FormType.CT_5.getId()==s.getFormType()) {
						totalCost = s.getQuantity() * 5;
					}else if(FormType.CT_2.getId()==s.getFormType()) {
						totalCost = s.getQuantity() * 2;
					}	
					
					Requisition.builder()
							.stockNo(s.getStabNo()+"")
							.unit("STUBS")
							.description("Accountable " + s.getFormTypeName() + " "+ s.getSeriesFrom()+"-"+s.getSeriesTo())
							.formType(s.getFormType())
							.quantity(s.getQuantity())
							.totalCost(totalCost)
							.remarks(s.getRemarks())
							.isActive(1)
							.issueSlip(slip)
							.stocks(s)
							.build().save();
					isSuccess = true;
				}
				
				if(isSuccess) {
					isSuccess = false;
					for(Stocks s : getStockSelected()) {
						s.setCollector(slip.getCollector());//update stockreceipt table with collector assigned	
					}
					List<IssuedForm> forms = new ArrayList<IssuedForm>();
					for(Stocks s : getStockSelected()) {
						//create logform data
						int status = FormStatus.HANDED.getId();
						boolean barangayOR = false;
						if(isForBarangay) {
							if(FormType.AF_51.getId()==s.getFormType()) {
								status = FormStatus.ALL_ISSUED.getId();//automatic change status to all issued. this means all 51 accountable form 51 is issued to the barangay treasurer
								barangayOR = true;
							}
						}
						
						//if(FormType.AF_51.getId()==s.getFormType()) {
							IssuedForm form = IssuedForm.builder()
							.issuedDate(dateTrans)
							.formType(s.getFormType())
							.beginningNo(Long.valueOf(s.getSeriesFrom()))
							.endingNo(Long.valueOf(s.getSeriesTo()))
							.pcs(s.getQuantity())//this will create issue in RTS // will fix in LogIssueForm module
							.isActive(1)
							.status(status)
							.fundId(slip.getFundId())
							.collector(slip.getCollector())
							.stock(s)
							.stabNo(s.getStabNo())
							.build();
							 
							long amount = 0l;
							if(FormType.CT_2.getId()==s.getFormType()) { 
								amount = 2*2000;
								form.setBeginningNo(amount);
								form.setEndingNo(amount);
							}else if(FormType.CT_5.getId()==s.getFormType()) {
								amount = 5*2000;
								form.setBeginningNo(amount);
								form.setEndingNo(amount);
							}
							
							if(barangayOR) {
								form.setIsForBarangayOR(1);//add this filter if for barangay Official Receipt to reflect all issued in CRAAF Report
							}
							
							form = IssuedForm.save(form);
							forms.add(form);
						//}
						
						//update stocksreceipt information
						s.setCollector(slip.getCollector());
						s.setStatus(FormStatus.ALL_ISSUED.getId());//change the status of the form to all issued. It means the form is issued to the collector
						s.save();
						
						isSuccess = true;
					}
					if(!isSuccess) {
						//rollback changes
						if(forms!=null && forms.size()>0) {
							for(IssuedForm f : forms) {
								f.delete();
							}
						}
						//Requisition.delete("DELETE FROM requesition WHERE rqid="+slip.getId(), new String[0]);
						RequisitionIssueSlip.deleteFirstBeforeSavingRIS(slip.getId());
						slip.delete();
						Application.addMessage(3, "Error", "Cannot proceed. Rollback all changes");
						clearCreateStocks();
					}
				}else {
					//rollback changes
					//Requisition.delete("DELETE FROM requesition WHERE rqid="+slip.getId(), new String[0]);
					RequisitionIssueSlip.deleteFirstBeforeSavingRIS(slip.getId());
					slip.delete();
					Application.addMessage(3, "Error", "Cannot proceed. Rollback all first level");
					clearCreateStocks();
				}
				
			}
			Application.addMessage(1, "Success", "Successfully saved.");
			clearCreateStocks();
			stockInit();
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("PF('tabSel').select(2)");
		}
	}
	
	public void clearCreateStocks() {
		setStockSelected(null);
		setTabSelected(1);
		loadValueCreateSlip();
	}
	
	public void loadHistory(){
		String sql = " ORDER BY rx.rid DESC LIMIT 20";
		String[] params = new String[0];
		slips = RequisitionIssueSlip.retrieve(sql, params);
	}
	
	public void clickReq(RequisitionIssueSlip st) {
		PrimeFaces pf = PrimeFaces.current();
		Object[] objs = RequisitionIssueSlip.checkAllFormsIsOkToEdit(st);
		boolean isOkToEdit = (boolean)objs[0];
		List<CollectionInfo> infos = (List<CollectionInfo>) objs[1];
		List<IssuedForm> iss = (List<IssuedForm>) objs[2];
		List<Requisition> reqs = (List<Requisition>) objs[3];
		System.out.println("Check is Ok: " + isOkToEdit);
		if(isOkToEdit && infos.size()==0) {
			st.setTmpDateTrans(DateUtils.convertDateString(st.getDateTrans(), "yyyy-MM-dd"));
			setSlip(st);
			pf.executeScript("PF('tabSel').select(1)");
		}else {
			System.out.println("Editing is no longer allowed but able to change some data");
			pf.executeScript("PF('dlgView').show(1000)");
			
			List forms = new ArrayList<>();
			for(FormType type : FormType.values()) {
				forms.add(new SelectItem(type.getId(), type.getDescription()));
			}
			Map<Long, Requisition> mapData = new LinkedHashMap<Long, Requisition>();
			requesitionData = new ArrayList<Requisition>();
			for(Requisition r : reqs) {
				for(IssuedForm is : iss) {
					if(mapData!=null && mapData.containsKey(is.getId())) {
						
					}else {
						mapData.put(is.getId(), r);
						if(r.getStocks().getId()==is.getStock().getId()) {
							r.setIssuedFormData(is);
						}
						for(CollectionInfo in : infos) {
							System.out.println(" col id: "+in.getIssuedForm().getId() + " == " + is.getId());
							if(in.getIssuedForm().getId()==is.getId()) {
								r.setCollectionInfoData(in);
								r.setDisableRow(true);
							}
						}
					}
				}
				System.out.println("Collection Info: " + r.isDisableRow());
				r.setFormTypes(forms);
				requesitionData.add(r);
			}
			
			
		}
		
	}
	
	public void deleteHis(RequisitionIssueSlip st) {
		PrimeFaces pf = PrimeFaces.current();
		Object[] objs = RequisitionIssueSlip.checkAllFormsIsOkToEdit(st);
		boolean isOkToDelete = (boolean)objs[0];
		List<CollectionInfo> infos = (List<CollectionInfo>) objs[1];
		List<IssuedForm> iss = (List<IssuedForm>) objs[2];
		List<Requisition> reqs = (List<Requisition>) objs[3];
		if(isOkToDelete && infos.size()==0) {
			RequisitionIssueSlip.deleteFirstBeforeSavingRIS(st.getId());
			st.delete();
			Application.addMessage(1, "Success", "Successfully deleted");
			loadHistory();
		}else {
			pf.executeScript("PF('dlgView').show(1000)");
			Map<Long, Requisition> mapData = new LinkedHashMap<Long, Requisition>();
			requesitionData = new ArrayList<Requisition>();
			for(Requisition r : reqs) {
				for(IssuedForm is : iss) {
					if(mapData!=null && mapData.containsKey(is.getId())) {
						
					}else {
						mapData.put(is.getId(), r);
						if(r.getStocks().getId()==is.getStock().getId()) {
							r.setIssuedFormData(is);
							
						}
						for(CollectionInfo in : infos) {
							if(in.getIssuedForm().getId()==is.getId()) {
								r.setCollectionInfoData(in);
								r.setDisableRow(true);
							}
						}
					}
				}
				requesitionData.add(r);
			}
			
		}
	}
	
	public void saveHisData(Requisition req) {
		
		Stocks stock = req.getStocks();
		stock.save();
		
		double totalCost =0d;
		if(FormType.CT_5.getId()==stock.getFormType()) {
			totalCost = stock.getQuantity() * 5;
		}else if(FormType.CT_2.getId()==stock.getFormType()) {
			totalCost = stock.getQuantity() * 2;
		}	
		
		Requisition res = req;
		res.setFormType(stock.getFormType());
		res.setStockNo(stock.getStabNo()+"");
		res.setDescription("Accountable " + stock.getFormTypeName() + " "+ stock.getSeriesFrom()+"-"+stock.getSeriesTo());
		res.setQuantity(stock.getQuantity());
		res.setTotalCost(totalCost);
		res.setRemarks(stock.getRemarks());
		res.save();
		
		IssuedForm iss = req.getIssuedFormData();
		iss.setFormType(stock.getFormType());
		iss.setStabNo(stock.getStabNo());
		
		long amount = 0l;
		if(FormType.CT_2.getId()==res.getFormType()) { 
			amount = 2*res.getQuantity();
			iss.setBeginningNo(amount);
			iss.setEndingNo(4000);
		}else if(FormType.CT_5.getId()==res.getFormType()) {
			amount = 5*res.getQuantity();
			iss.setBeginningNo(amount);
			iss.setEndingNo(10000);
		}
		
		iss.setBeginningNo(Long.valueOf(stock.getSeriesFrom()));
		iss.setEndingNo(Long.valueOf(stock.getSeriesTo()));
		iss.setPcs(stock.getQuantity());
		iss.save();
		Application.addMessage(1, "Success", "Successfully saved.");
		
		
	}
	
	public void deleteHisRow(Requisition req) {
		IssuedForm.delete("DELETE FROM logissuedform WHERE stockid="+req.getStocks().getId() + " AND isactivelog =1 AND formstatus="+FormStatus.HANDED.getId(),new String[0]);
		
		Stocks stock = req.getStocks();
		stock.setStatus(FormStatus.HANDED.getId());//rollback status from all issued to handed
		stock.setCollector(Collector.builder().id(0).build());
		stock.save();
		Requisition.delete("DELETE FROM requesition WHERE rqid="+req.getId(), new String[0]);
		requesitionData.remove(req);//remove from the list
		Application.addMessage(1, "Success", "Successfully saved.");
	}
	
	public void printRIS(RequisitionIssueSlip slip) {
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = GlobalVar.RIS_SLIP_RPT;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		List<RisRpt> reports = new ArrayList<RisRpt>();
		
		for(Requisition r : Requisition.retrieve(" AND  rx.rid="+ slip.getId(), new String[0])) {
			if(r.getTotalCost()>0) {
				r.setRemarks(Currency.formatAmount(r.getTotalCost()));
			}
			reports.add(
					RisRpt.builder()
					.f1(r.getStockNo())
					.f2(r.getUnit())
					.f3(r.getDescription())
					.f4(r.getQuantity()+"")
					.f5(r.getRemarks())
					.build()
					);
		}
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		
  		param.put("PARAM_MUNICIPALITY", "MUNICIPALITY OF "+GlobalVar.MUNICIPALITY.toUpperCase());
  		
  		param.put("PARAM_DIVISION", slip.getDivision().getName());
  		param.put("PARAM_OFFICES", slip.getOffice().getName());
  		param.put("PARAM_CODE", slip.getCodeNo());
  		param.put("PARAM_RISNO", slip.getRequestNo());
  		param.put("PARAM_RISDATE", slip.getDateTrans());
  		param.put("PARAM_SAINO", slip.getSaiNo());
  		param.put("PARAM_SAIDATE", slip.getDateTrans());
  		
  		param.put("PARAM_REQUESTEDBY", slip.getCollector().getName().toUpperCase());
		param.put("PARAM_REQUESTEDBY_DESIG", slip.getPosition().toUpperCase());
		
		param.put("PARAM_APPROVEDBY", "FERDINAND L. LOPEZ");
		param.put("PARAM_APPROVEDBY_DESIG", "MUNICIPAL TREASURER");
		
		param.put("PARAM_ISSUEDBY", slip.getIssuedBy().toUpperCase());
		param.put("PARAM_ISSUED_DESIG", slip.getIssuedPosition().toUpperCase());
		
		param.put("PARAM_PURPOSE", slip.getPurspose());
		
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