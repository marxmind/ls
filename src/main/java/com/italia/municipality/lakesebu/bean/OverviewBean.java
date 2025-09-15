package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.BudgetMonitoring;
import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.controller.RCDDeposit;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.TaxAccountGroup;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AccessLevel;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;


@Named("ovBean")
@ViewScoped
@Data
public class OverviewBean implements Serializable {

	private static final long serialVersionUID = 54578253423231L;
	private String year;
	private int todayMonth;
	private String month;
	private String startDate;
	private String dateToday;
	private String last_Date;
	private Map<Long, Offices> mapOffices;
	private Map<Long, Mooe> mapMooe;
	private Map<Integer, Collector> mapCollector;
	
	private List<Reports> rptRecentChecksIssueds;
	private List<Reports> rptMultipleCheckIssueds;
	private List<Reports> rptMonthCheckIssuedsPerAccounts;
	private List<Reports> rptHighestIssuedChecks;
	private List<Reports> rcds;
	private List<Reports> collections;
	private List<Reports> collectionPrintData;
	
	private List<Reports> collectors;
	private String grandTotalRunningCollection;
	
	private List years;
	
	private String tabSelected;
	private int tabIndex;
	
	private double[] totalCancelledOR;
	private double[] totalCTCCollection;
	private double[] totalMTOCTCCollection;
	private double[] totalBarangayCTCCollection;
	
	private List<Reports> paymentViews;
	private Map<Long,PaymentName> payMapData;
	private String labelNamePayment;
	
	private List<Reports> todaysCollections;
	private List<Reports> accountableForms;
	
	private List<Reports> seriesHisDtls;
	private String remainingQty;
	
	private List<BusinessPermit> bzs;
	
	private List<Reports> cashbooks;
	private List cashBankAccounts;
	private String cashBankAccountsId;
	private Map<String, BankAccounts> mapAccounts;
	
	private int fundIdSearch;
	private List fundTypesSearch;
	
	@PostConstruct
	public void init() {
		
		//TaxAccountGroup.loadAcctountPayment();
		paymentViews = new ArrayList<Reports>();
		rptRecentChecksIssueds = new ArrayList<Reports>();
		rptMultipleCheckIssueds = new ArrayList<Reports>();
		rptMonthCheckIssuedsPerAccounts = new ArrayList<Reports>();
		rptHighestIssuedChecks = new ArrayList<Reports>();
		rcds = new ArrayList<Reports>();
		collections = new ArrayList<Reports>();
		
		year = DateUtils.getCurrentYear()+"";
		years = new ArrayList<>();
		
		for(int y=2018; y<=DateUtils.getCurrentYear(); y++) {
			years.add(new SelectItem(y+"", y+""));
		}

		todayMonth = DateUtils.getCurrentMonth();
		month = todayMonth<10? "0"+todayMonth : todayMonth+"";
		startDate = year + "-" + month + "-01";
		dateToday = DateUtils.getCurrentDateYYYYMMDD();
		last_Date= DateUtils.getLastDayOfTheMonth("yyyy-MM-dd",dateToday, Locale.TAIWAN);
		
		fundIdSearch = 0; //default search Fund Type which is all fund type
		fundTypesSearch = new ArrayList<>();
		fundTypesSearch.add(new SelectItem(0, "All Fund Types"));
		for(FundType type : FundType.values()){
			fundTypesSearch.add(new SelectItem(type.getId(), type.getName()));
		}
		
		
		//do not move these line of codes
		loadOffices();
		loadMooe();
		loadCollector();
		
		collections = new ArrayList<Reports>();
		loadCollection();
		
		payMapData = PaymentName.retrieveAllInMap();
		
		mapAccounts = new LinkedHashMap<String, BankAccounts>();
		cashBankAccounts = new ArrayList<>();
		setCashBankAccountsId("GENERAL FUND");
		for(BankAccounts b : BankAccounts.retrieveAll()) {
			cashBankAccounts.add(new SelectItem(b.getBankAccntName(), b.getBankAccntName()));
			mapAccounts.put(b.getBankAccntName(), b);
		}
	}
	
	public void reloadTab() {
		PrimeFaces pf = PrimeFaces.current();
		
		if("Recent Issued Check".equalsIgnoreCase(getTabSelected()) || "Multiple Payee Check Issued".equalsIgnoreCase(getTabSelected())) {
			rptRecentChecksIssueds = new ArrayList<Reports>();
			rptMultipleCheckIssueds = new ArrayList<Reports>();
			loadCheckWriting();
			pf.executeScript("PF('tabSelection').select(2)");
		}else if("Monthly Check Issued".equalsIgnoreCase(getTabSelected())) {
			rptMonthCheckIssuedsPerAccounts = new ArrayList<Reports>();
			monthlyCheckIssued();
			pf.executeScript("PF('tabSelection').select(4)");
		}else if("Highest Issued Check".equalsIgnoreCase(getTabSelected())) {
			rptHighestIssuedChecks = new ArrayList<Reports>();
			loadHighestIssuedChecks();
			pf.executeScript("PF('tabSelection').select(5)");
		}else if("RCD".equalsIgnoreCase(getTabSelected())) {
			rcds = new ArrayList<Reports>();
			loadRCD();
			pf.executeScript("PF('tabSelection').select(1)");
		}else if("Running Collections".equalsIgnoreCase(getTabSelected())) {
			collections = new ArrayList<Reports>();
			loadCollection();
			pf.executeScript("PF('tabSelection').select(0)");
		}else if("Monthly Collector Collection".equalsIgnoreCase(getTabSelected())) {
			collectors = new ArrayList<Reports>();
			loadCollectorCollection();
			pf.executeScript("PF('tabSelection').select(6)");
		}else if("Reported TO LO (Today)".equalsIgnoreCase(getTabSelected())) {
			todaysCollections = new ArrayList<Reports>();
			loadTodayReportedCollection();
		}else if("Accountable Form Status".equalsIgnoreCase(getTabSelected())) {
			accountableForms = new ArrayList<Reports>();
			loadAccountableForms();
		}else if("List Of No Business Renewal".equalsIgnoreCase(getTabSelected())) {
			loadNoBusinessRenewal();
		}else if("Cash Book".equalsIgnoreCase(getTabSelected())) {
			loadCashBook();	
		}
	}
	
	public void onTabChange(TabChangeEvent event) {
        
		if("Recent Issued Check".equalsIgnoreCase(event.getTab().getTitle()) || "Multiple Payee Check Issued".equalsIgnoreCase(event.getTab().getTitle())) {
			rptRecentChecksIssueds = new ArrayList<Reports>();
			rptMultipleCheckIssueds = new ArrayList<Reports>();
			loadCheckWriting();
		}else if("Monthly Check Issued".equalsIgnoreCase(event.getTab().getTitle())) {
			rptMonthCheckIssuedsPerAccounts = new ArrayList<Reports>();
			monthlyCheckIssued();
		}else if("Highest Issued Check".equalsIgnoreCase(event.getTab().getTitle())) {
			rptHighestIssuedChecks = new ArrayList<Reports>();
			loadHighestIssuedChecks();
		}else if("RCD".equalsIgnoreCase(event.getTab().getTitle())) {
			rcds = new ArrayList<Reports>();
			loadRCD();
		}else if("Running Collections".equalsIgnoreCase(event.getTab().getTitle())) {
			collections = new ArrayList<Reports>();
			loadCollection();
		}else if("Monthly Collector Collection".equalsIgnoreCase(event.getTab().getTitle())) {
			collectors = new ArrayList<Reports>();
			loadCollectorCollection();
		}else if("Reported TO LO (Today)".equalsIgnoreCase(event.getTab().getTitle())) {
			todaysCollections = new ArrayList<Reports>();
			loadTodayReportedCollection();
		}else if("Accountable Form Status".equalsIgnoreCase(event.getTab().getTitle())) {
			accountableForms = new ArrayList<Reports>();
			loadAccountableForms();
		}else if("List Of No Business Renewal".equalsIgnoreCase(event.getTab().getTitle())) {
			loadNoBusinessRenewal();
		}else if("Cash Book".equalsIgnoreCase(event.getTab().getTitle())) {
			loadCashBook();	
		}
		setTabSelected(event.getTab().getTitle());
    }
	
	private void loadCashBook() {
		
		cashbooks = new ArrayList<Reports>();
		
		double beginningBalance = BudgetMonitoring.yearBeginningBalance(getCashBankAccountsId(), Integer.valueOf(getYear()));	
		cashbooks.add(Reports.builder()
				.f1("January " + getYear())
				.f2("Beginning Balance")
				.f5(Currency.formatAmount(beginningBalance)).build());
		
		int fundId = 0;
		if("GENERAL FUND".equalsIgnoreCase(getCashBankAccountsId())) {
			fundId = 1;
		}else if("MOTORPOOL".equalsIgnoreCase(getCashBankAccountsId())) {
			fundId = 2;
		}else if("TRUST FUND".equalsIgnoreCase(getCashBankAccountsId())) {
			fundId = 3;
		}
		
		double forwardedAmount = 0d;
		for(int month=1; month<=12; month++) {
			double cashInBank = 0d;
			Reports rpt = new Reports();
			double budget = BudgetMonitoring.amountBudgetOnly(Integer.valueOf(getYear()), month, getMapAccounts().get(getCashBankAccountsId()).getBankId());
			double loan = BudgetMonitoring.loanAmount(Integer.valueOf(getYear()), month, getMapAccounts().get(getCashBankAccountsId()).getBankId());
			double transferAmount = BudgetMonitoring.transferFundAmount(Integer.valueOf(getYear()), month, getMapAccounts().get(getCashBankAccountsId()).getBankId());
			
			if(budget>0) {
			
			if(month>1) {
				//beginning
				//rpt.setF1(DateUtils.getMonthName(month));
				//rpt.setF2("Beginning Balance");
				//rpt.setF5(Currency.formatAmount(forwardedAmount));
				//cashbooks.add(rpt);
				
				//NTA
				if(budget>0) {
					rpt = new Reports();
					rpt.setF1(DateUtils.getMonthName(month));
					rpt.setF2("National Tax Allocation");
					rpt.setF5(Currency.formatAmount(budget));
					cashbooks.add(rpt);
				}
				
			}else {
			
				//NTA
				if(budget>0) {
					rpt.setF1(DateUtils.getMonthName(month));
					rpt.setF2("National Tax Allocation");
					rpt.setF5(Currency.formatAmount(budget));
					cashbooks.add(rpt);
				}
			}
			//Deposit
			Map<Integer, Double> mapDep = RCDDeposit.depositedCollection(Integer.valueOf(getYear()), fundId);
			double deposit = 0;
			if(mapDep!=null && mapDep.size()>0 && mapDep.containsKey(month)) {
				deposit = mapDep.get(month);
			}
			
			if(deposit>0) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("Deposited Collection");
				rpt.setF3(Currency.formatAmount(deposit));
				cashbooks.add(rpt);
			}
			//Expenses
			if(loan>0) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("20% Development Fund + Loans");
				rpt.setF4(Currency.formatAmount(loan));
				cashbooks.add(rpt);
			}
			
			//Transfer Fund
			if(transferAmount>0) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("Transfer Fund");
				rpt.setF4(Currency.formatAmount(transferAmount));
				cashbooks.add(rpt);
			}
			
			//Expenses
			double issuedAmount = Chequedtls.monthlyIssued(getMapAccounts().get(getCashBankAccountsId()).getBankId(), Integer.valueOf(getYear()), month);
			if(issuedAmount>0) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("Issued Checks");
				rpt.setF4(Currency.formatAmount(issuedAmount));
				cashbooks.add(rpt);
			}
			
			//Cash In Bank
			
			if(month==1) {
				cashInBank = (budget + beginningBalance + deposit) - (loan + issuedAmount + transferAmount);
			}else {
				cashInBank = (budget + forwardedAmount + deposit) - (loan + issuedAmount + transferAmount);
			}
			
			if(month<12) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("Ending Balance");
				rpt.setF5(Currency.formatAmount(cashInBank));
				cashbooks.add(rpt);
			}
			
			forwardedAmount = cashInBank;
			
			}
		}
		Reports rpt = new Reports();
		rpt.setF1("Current Cash In Bank");
		rpt.setF2("");
		rpt.setF5(Currency.formatAmount(forwardedAmount));
		cashbooks.add(rpt);
		
	}
	
	public void loadAccountableForms() {
		ResultSet rs = OpenTableAccess.query("SELECT issueddate,formtypelog,beginningNoLog,endingNoLog,logpcs,isid,fundid,logid FROM logissuedform WHERE isactivelog=1 AND formstatus=" + FormStatus.HANDED.getId() +" ORDER BY isid", new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				
				boolean isDoNotTrack = getMapCollector().get(rs.getInt("isid")).getDoNotTrace()==0? true : false; 
				if(isDoNotTrack) {
					String collector = getMapCollector().get(rs.getInt("isid")).getName();
					String days = DateUtils.getNumberyDaysNow(rs.getString("issueddate"))+"";
					String dateIssued = DateUtils.convertDateToMonthDayYear(rs.getString("issueddate"));
					String formName = FormType.nameId(rs.getInt("formtypelog"));
					long start = rs.getLong("beginningNoLog");
					long last = rs.getLong("endingNoLog");
					int qty = rs.getInt("logpcs");
					
					String fund = FundType.typeName(rs.getInt("fundid"));
					
					String beg = correctingDigitOfSeries(start, rs.getInt("formtypelog"));
					String end = correctingDigitOfSeries(last, rs.getInt("formtypelog"));
					
					int issQty = CollectionInfo.remainingQty(rs.getInt("formtypelog"), rs.getInt("isid"), rs.getLong("logid"));
					int remQty = qty - issQty;
					
					String style="";
					int numOfDays = Integer.valueOf(days);
					if(numOfDays>50) {
						style = "background-color: red; font-color: black";
					}
					
					Reports rpt = Reports.builder()
							.f1(collector)
							.f2(formName)
							.f3(dateIssued)
							.f4(beg+"-"+end)
							.f5(qty+"")
							.f6(fund)
							.f7(days)
							.f8(rs.getInt("isid")+"")
							.f9(rs.getInt("formtypelog")+"")
							.f10(rs.getLong("logid")+"")
							.f11(issQty+"")
							.f12(remQty+"")
							.f13(style)
							.build();
					
					accountableForms.add(rpt);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String correctingDigitOfSeries(long num, int formType) {
		String val = "";
		String valNum = num+"";
		int len = valNum.length();
		if(FormType.CT_20.getId()==formType || FormType.CT_5.getId()==formType) {
			val = num+"";
		}else if(FormType.AF_51.getId()==formType || FormType.AF_52.getId()==formType || FormType.AF_56.getId()==formType || FormType.AF_53.getId()==formType) {
			
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
	
	public void loadPayment(String labelName,String taxAmount, String month,String id) {
		paymentViews = new ArrayList<Reports>();
		
		setLabelNamePayment(labelName + " "+ DateUtils.getMonthName(Integer.valueOf(month)) + " " + getYear() + " : " + taxAmount);
		
		
		if("Land Tax Collection".equalsIgnoreCase(labelName)) {
			
			double taxes = Double.valueOf(taxAmount.replace(",", ""));
			double basic = taxes/2;
			double prov = 0.35 * basic;
			double mun = 0.40 * basic;
			double brgy = 0.25 * basic;
			
			Reports r = Reports.builder()
					.f1("Basic (50%)")
					.f2(Currency.formatAmount(basic))
					.build();
			paymentViews.add(r);
			
			r = Reports.builder()
					.f1("SEF (50%)")
					.f2(Currency.formatAmount(basic))
					.build();
			paymentViews.add(r);
			
			r = Reports.builder()
					.f1("Below are the sharing")
					.f2("BASIC")
					.build();
			paymentViews.add(r);
			
			r = Reports.builder()
					.f1("Province (35%)")
					.f2(Currency.formatAmount(prov))
					.build();
			paymentViews.add(r);
			
			r = Reports.builder()
					.f1("Municipal (40%)")
					.f2(Currency.formatAmount(mun))
					.build();
			paymentViews.add(r);
			
			r = Reports.builder()
					.f1("Barangay (25%)")
					.f2(Currency.formatAmount(brgy))
					.build();
			paymentViews.add(r);
		}else {
		
		
		String sql = "SELECT o.olamount as amount,o.pyid as id FROM ornamelist o, orlisting p WHERE o.isactiveol=1 AND p.orstatus=4 AND  p.isactiveor=1 and o.orid=p.orid AND month(p.ordatetrans)=" + month + " AND year(p.ordatetrans)=" + getYear() + " AND o.accid="+id;
		
		if("0".equalsIgnoreCase(month)) {
			sql = "SELECT o.olamount as amount,o.pyid as id FROM ornamelist o, orlisting p WHERE o.isactiveol=1 AND p.orstatus=4 AND  p.isactiveor=1 and o.orid=p.orid AND year(p.ordatetrans)=" + getYear() + " AND o.accid="+id;
		}
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		Map<Long, Double> data = new LinkedHashMap<Long, Double>();
		try {
			while(rs.next()) {
				long pyid = rs.getLong("id");
				double amount = rs.getDouble("amount");
				if(data!=null && data.size()>0) {
					if(data.containsKey(pyid)) {
						double newAmount = data.get(pyid) + amount;
						data.put(pyid, newAmount);
					}else {
						data.put(pyid, amount);
					}
				}else {
					data.put(pyid, amount);
				}
			}
			
			double total = 0d;
			for(long i : data.keySet()) {
				String name = payMapData.get(i).getName();
				double amnt = data.get(i);
				total += amnt;
				Reports r = Reports.builder()
						.f1(name)
						.f2(Currency.formatAmount(amnt))
						.build();
				paymentViews.add(r);
			}
			/*Reports r = Reports.builder()
					.f1("Total")
					.f2(Currency.formatAmount(total))
					.build();
			paymentViews.add(r);*/
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
	}
	
	public void loadTodayReportedCollection() {
		String sql = "SELECT day(receiveddate) as day,sum(amount) as amount,formtypecol,isid "
				+ "FROM collectioninfo WHERE isactivecol=1 AND receiveddate>='"+ dateToday +"'"
						+ " GROUP BY day,formtypecol,isid ";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		//collector form day amount
		Map<Integer, Map<Integer, Map<Integer, Double>>> collector = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<Integer, Double>> form = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> days = new LinkedHashMap<Integer, Double>();
		try {
			while(rs.next()) {
				
				int col = rs.getInt("isid");
				int formType = rs.getInt("formtypecol");
				int day = rs.getInt("day");
				double amount = rs.getDouble("amount");
				
				if(collector!=null && collector.size()>0) {
					if(collector.containsKey(col)) {
						if(collector.get(col).containsKey(formType)) {
							if(collector.get(col).get(formType).containsKey(day)){
								double newAmount = collector.get(col).get(formType).get(day) + amount;
								collector.get(col).get(formType).put(day, newAmount);	
							}else {
								collector.get(col).get(formType).put(day, amount);							}
						}else {
							days = new LinkedHashMap<Integer, Double>();
							days.put(day, amount);
							collector.get(col).put(formType, days);
						}
					}else {
						form = new LinkedHashMap<Integer, Map<Integer, Double>>();
						days = new LinkedHashMap<Integer, Double>();
						days.put(day, amount);
						form.put(formType, days);
						collector.put(col, form);
					}
				}else {
					days.put(day, amount);
					form.put(formType, days);
					collector.put(col, form);
				}
			}
			
			if(collector!=null && collector.size()>0) {
				for(int id : collector.keySet()) {
					String dep = "";
					Collector col = getMapCollector().get(id);
					try {dep = col.getDepartment().getDepartmentName();}catch(Exception e) {}
					Reports rss = Reports.builder()
							.f1(col.getName())
							.f15(dep)
							.build();
					
					todaysCollections.add(rss);
					
						for(int frm : collector.get(id).keySet()) {
							rss = Reports.builder()
									.f2(FormType.nameId(frm))
									.build();
							
							for(int m=1; m<=31; m++) {
								String amnt = "";
								if(collector.get(id).get(frm).containsKey(m)) {
									amnt = Currency.formatAmount(collector.get(id).get(frm).get(m));
									rss.setF3(amnt);
								}
							}
							todaysCollections.add(rss);
						}
					
					}
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadCollectorCollection() {
		String sql = "SELECT month(receiveddate) as month,sum(amount) as amount,formtypecol,isid "
				+ "FROM collectioninfo WHERE isactivecol=1 AND (receiveddate>='"+ year +"-01-01' AND receiveddate<='"+ year +"-12-31')"
						+ " GROUP BY month,formtypecol,isid ";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		//collector form month amount
		Map<Integer, Map<Integer, Map<Integer, Double>>> collector = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<Integer, Double>> form = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> month = new LinkedHashMap<Integer, Double>();
		try {
			while(rs.next()) {
				
				int col = rs.getInt("isid");
				int formType = rs.getInt("formtypecol");
				int mnth = rs.getInt("month");
				double amount = rs.getDouble("amount");
				
				if(collector!=null && collector.size()>0) {
					if(collector.containsKey(col)) {
						if(collector.get(col).containsKey(formType)) {
							if(collector.get(col).get(formType).containsKey(mnth)){
								double newAmount = collector.get(col).get(formType).get(mnth) + amount;
								collector.get(col).get(formType).put(mnth, newAmount);	
							}else {
								collector.get(col).get(formType).put(mnth, amount);							}
						}else {
							month = new LinkedHashMap<Integer, Double>();
							month.put(mnth, amount);
							collector.get(col).put(formType, month);
						}
					}else {
						form = new LinkedHashMap<Integer, Map<Integer, Double>>();
						month = new LinkedHashMap<Integer, Double>();
						month.put(mnth, amount);
						form.put(formType, month);
						collector.put(col, form);
					}
				}else {
					month.put(mnth, amount);
					form.put(formType, month);
					collector.put(col, form);
				}
			}
			
			if(collector!=null && collector.size()>0) {
				for(int id : collector.keySet()) {
					String dep = "";
					Collector col = getMapCollector().get(id);
					try {dep = col.getDepartment().getDepartmentName();}catch(Exception e) {}
					Reports rss = Reports.builder()
							.f1(col.getName())
							.f15(dep)
							.build();
					
					collectors.add(rss);
					
						for(int frm : collector.get(id).keySet()) {
							rss = Reports.builder()
									.f2(FormType.nameId(frm))
									.build();
							
							for(int m=1; m<=12; m++) {
								String amnt = "";
								if(collector.get(id).get(frm).containsKey(m)) {
									amnt = Currency.formatAmount(collector.get(id).get(frm).get(m));
								}
								
								//months
								switch(m) {
									case 1: rss.setF3(amnt); break;
									case 2: rss.setF4(amnt); break;
									case 3: rss.setF5(amnt); break;
									case 4: rss.setF6(amnt); break;
									case 5: rss.setF7(amnt); break;
									case 6: rss.setF8(amnt); break;
									case 7: rss.setF9(amnt); break;
									case 8: rss.setF10(amnt); break;
									case 9: rss.setF11(amnt); break;
									case 10: rss.setF12(amnt); break;
									case 11: rss.setF13(amnt); break;
									case 12: rss.setF14(amnt); break;
								}
								
								
							}
							collectors.add(rss);
						}
					
					}
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Map<Integer, String> accountingGroup(){
		Map<Integer, String> data = new LinkedHashMap<Integer, String>();
		
		ResultSet rs = OpenTableAccess.query("SELECT accid,accname FROM taxaccntgroup WHERE accisactive=1", new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				int id = rs.getInt("accid");
				String name = rs.getString("accname");
				data.put(id, name);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	public void loadCollection() {
		
		totalCancelledOR = new double[13];
		totalCTCCollection = new double[13];
		totalMTOCTCCollection = new double[13];
		totalBarangayCTCCollection = new double[13];
		
		
		totalCancelledOR[0]=0;
		totalCTCCollection[0]=0;
		totalMTOCTCCollection[0]=0;
		totalBarangayCTCCollection[0]=0;
		
		
		Map<Integer, String> mapAccount =  accountingGroup();
		
		//String sql = "SELECT month(b.timestampol) as month, a.accname as name, b.olamount as amount FROM taxaccntgroup a,ornamelist b WHERE b.accid=a.accid AND b.orid IN(select o.orid from orlisting o where o.isactiveor=1 AND (o.ordatetrans>='"+ year +"-01-01' AND o.ordatetrans<='"+ year +"-12-31')) ORDER BY amount DESC";
		//o.isactiveol=1 and remove to inclued cancelled
		String sql = "SELECT o.isactiveol as stat,p.orstatus, o.olamount as amount,o.accid as id,month(p.ordatetrans) as month,p.aform,p.isid from ornamelist o, orlisting p WHERE  p.isactiveor=1 and o.orid=p.orid AND (p.ordatetrans>='"+ year +"-01-01' AND p.ordatetrans<='"+ year +"-12-31')";
		
		if(getFundIdSearch()>0) {
			sql += " AND p.fundid=" + getFundIdSearch();
		}
		
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		Map<Integer, Map<Integer, Double>> mapColl = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> mapMonth = new LinkedHashMap<Integer, Double>();
		double grandTotal = 0d;
		//double totalcancelled = 0d;
		//double barangayctcamount = 0d;
		//double mtoctcamount = 0d;
		//double ctctotal = 0d;
		try {
			while(rs.next()) {
				//String name =  mapAccount.get(rs.getInt("id"));
				int name = rs.getInt("id");
				int month = rs.getInt("month");
				int formType = rs.getInt("aform");
				int status = rs.getInt("orstatus"); //==FormStatus.CANCELLED.getId()? true : false;
				boolean isCancelledPayment = rs.getInt("stat")==0? true : false; 
				double amount = rs.getDouble("amount");
				int depId = getMapCollector().get(rs.getInt("isid")).getDepartment().getDepid();
				
				boolean isCTC = false;
				boolean isbarangayCtc = false;
				
				if(formType==FormType.CTC_INDIVIDUAL.getId()) {
					isCTC = true;
				}
				
				if(depId>1) {
					isbarangayCtc = true;
				}
				
				//if(isCancelled) {
				if(status==FormStatus.CANCELLED.getId()) {
					//isCancelledPayment) {
					if(isCancelledPayment) {
						//totalcancelled += amount;
						
						totalCancelledOR[month] += amount;
					}
				}else if(status==FormStatus.ENCODED.getId()){
					//not cancelled or
					grandTotal += amount;
				
					if(isCTC) {
						//ctctotal += amount;
						totalCTCCollection[month] += amount;
						if(isbarangayCtc) {
							//barangayctcamount += amount;
							totalBarangayCTCCollection[month] += amount;
						}else {
							//mtoctcamount += amount;
							totalMTOCTCCollection[month] += amount;
						}
					}
					
				
					if(mapColl!=null && mapColl.size()>0) {
						if(mapColl.containsKey(name)) {
							if(mapColl.get(name).containsKey(month)) {
								double newAmount = mapColl.get(name).get(month) + amount;
								mapColl.get(name).put(month, newAmount);
								/*if(name==8 && month==1) {
									System.out.println("iv Cedula:\t" + newAmount);
								}*/
							}else {
								mapColl.get(name).put(month, amount);
								/*if(name==8 && month==1) {
									System.out.println("xxx Cedula:\t" + amount);
								}*/
							}
						}else {
							mapMonth = new LinkedHashMap<Integer, Double>();
							mapMonth.put(month, amount);
							mapColl.put(name, mapMonth);
							/*if(name==8 && month==1) {
								System.out.println("xx Cedula:\t" + amount);
							}*/
						}
					}else {
						mapMonth.put(month, amount);
						mapColl.put(name, mapMonth);
						/*if(name==8 && month==1) {
							System.out.println("x Cedula:\t" + amount);
						}*/
					}
				
				}
				
			}
			
			//setGrandTotalRunningCollection(Currency.formatAmount(grandTotal));
			
			String[][] data = new String[mapColl.size()][15];
			int x = 0;
			double[] totalPerPayment = new double[12];
			//int monthNow = DateUtils.getCurrentMonth();
			for(int name : mapColl.keySet()) {
				
				data[x][0] =  mapAccount.get(name);//name;
				int month = 1;
				double total = 0d;
				for(int m=1; m<=12; m++) {
					
					//boolean isMonthBelowOrEqualNow = monthNow<=m? true : false;
					
					if(mapColl!=null && mapColl.size()>0) {
						if(mapColl.containsKey(name)) {
							if(mapColl.get(name).containsKey(m)) {
								double amount = mapColl.get(name).get(m);
								try{total += amount;
								data[x][month] = Currency.formatAmount(amount);}catch(Exception e){System.out.println("Empty: " + amount);}
								totalPerPayment[m-1] += amount;
							}else {
								//if(isMonthBelowOrEqualNow) {data[x][month] = "0.00";}
								//totalPerPayment[m-1] = 0.00;
							}
						}else {
							//if(isMonthBelowOrEqualNow) {data[x][month] = "0.00";}
							//totalPerPayment[m-1] = 0.00;
						}
					}else {
						//if(isMonthBelowOrEqualNow) {data[x][month] = "0.00";}
						//totalPerPayment[m-1] = 0.00;
					}
					month++;
				}
				//add total
				data[x][13]= Currency.formatAmount(total);
				
				data[x][14]=name+"";
				x++;
			}
			
			//add total
			Reports rss = Reports.builder()
					.f1("PRINT REPORT")
					.f2("PRINT")
					.f3("PRINT")
					.f4("PRINT")
					.f5("PRINT")
					.f6("PRINT")
					.f7("PRINT")
					.f8("PRINT")
					.f9("PRINT")
					.f10("PRINT")
					.f11("PRINT")
					.f12("PRINT")
					.f13("PRINT")
					.f14("PRINT")
					.f15("")
					.build();
			collections.add(rss);
		
			
			Map<Integer, Double> taxData = new LinkedHashMap<Integer, Double>();
			if(getFundIdSearch()==FundType.GENERAL_FUND.getId()) {
			
			//add land tax reported collection amount
			 taxData = CollectionInfo.monthlyIncomeRealPropertyTax(Integer.valueOf(getYear()));
			}
			 
			if(taxData!=null && taxData.size()>0) {
				for(int mx : taxData.keySet()) {
					totalPerPayment[mx-1] += taxData.get(mx);
				}
			}
			
			
			
			collectionPrintData = new ArrayList<Reports>();
				
			//add land tax label
			double landTaxTotal = 0d;
			List<Reports> tmpRpt = new ArrayList<Reports>();
			if(taxData!=null && taxData.size()>0) {
					
					Reports r = new Reports();
					r.setF1("Land Tax Collection");
					for(int mo : taxData.keySet()) {
						landTaxTotal += taxData.get(mo);
						switch (mo) {
							case 1: r.setF2(Currency.formatAmount(taxData.get(mo))); break;
							case 2: r.setF3(Currency.formatAmount(taxData.get(mo))); break;
							case 3: r.setF4(Currency.formatAmount(taxData.get(mo))); break;
							case 4: r.setF5(Currency.formatAmount(taxData.get(mo))); break;
							case 5: r.setF6(Currency.formatAmount(taxData.get(mo))); break;
							case 6: r.setF7(Currency.formatAmount(taxData.get(mo))); break;
							case 7: r.setF8(Currency.formatAmount(taxData.get(mo))); break;
							case 8: r.setF9(Currency.formatAmount(taxData.get(mo))); break;
							case 9: r.setF10(Currency.formatAmount(taxData.get(mo))); break;
							case 10: r.setF11(Currency.formatAmount(taxData.get(mo))); break;
							case 11: r.setF12(Currency.formatAmount(taxData.get(mo))); break;
							case 12: r.setF13(Currency.formatAmount(taxData.get(mo))); break;
						}
					
					}
					r.setF14(Currency.formatAmount(landTaxTotal));;
					collectionPrintData.add(r);
					tmpRpt.add(r);
				
			}
			
			//add total
			rss = Reports.builder()
					.f1("TOTAL")
					.f2(totalPerPayment[0]==0? "" : Currency.formatAmount(totalPerPayment[0]))
					.f3(totalPerPayment[1]==0? "" : Currency.formatAmount(totalPerPayment[1]))
					.f4(totalPerPayment[2]==0? "" : Currency.formatAmount(totalPerPayment[2]))
					.f5(totalPerPayment[3]==0? "" : Currency.formatAmount(totalPerPayment[3]))
					.f6(totalPerPayment[4]==0? "" : Currency.formatAmount(totalPerPayment[4]))
					.f7(totalPerPayment[5]==0? "" : Currency.formatAmount(totalPerPayment[5]))
					.f8(totalPerPayment[6]==0? "" : Currency.formatAmount(totalPerPayment[6]))
					.f9(totalPerPayment[7]==0? "" : Currency.formatAmount(totalPerPayment[7]))
					.f10(totalPerPayment[8]==0? "" : Currency.formatAmount(totalPerPayment[8]))
					.f11(totalPerPayment[9]==0? "" : Currency.formatAmount(totalPerPayment[9]))
					.f12(totalPerPayment[10]==0? "" : Currency.formatAmount(totalPerPayment[10]))
					.f13(totalPerPayment[11]==0? "" : Currency.formatAmount(totalPerPayment[11]))
					.f14(grandTotal==0? "" : Currency.formatAmount(grandTotal + landTaxTotal))
					.f15("font-weight: bold")
					.build();
			collections.add(rss);
			collections.addAll(tmpRpt);
			
			
			
			setGrandTotalRunningCollection(Currency.formatAmount(grandTotal + landTaxTotal));
			
			
				for(int b=0; b<mapColl.size(); b++) {
					
				Reports r = Reports.builder()
						.f1(data[b][0])
						.f2(data[b][1])
						.f3(data[b][2])
						.f4(data[b][3])
						.f5(data[b][4])
						.f6(data[b][5])
						.f7(data[b][6])
						.f8(data[b][7])
						.f9(data[b][8])
						.f10(data[b][9])
						.f11(data[b][10])
						.f12(data[b][11])
						.f13(data[b][12])
						.f14(data[b][13])
						.f16(data[b][14])
						.build();
				
				collectionPrintData.add(r);
				collections.add(r);
			}
			
				
			
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void loadRCD() {
		Map<String, Map<Integer, Double>> mapBeginningBal = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapBegMonth = new LinkedHashMap<Integer, Double>();
		
		Map<String, Map<Integer, Double>> mapCollection = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapCollMonth = new LinkedHashMap<Integer, Double>();
		
		Map<String, Map<Integer, Double>> mapDeposit = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapDepMonth = new LinkedHashMap<Integer, Double>();
		
		ResultSet rs = OpenTableAccess.query("SELECT  month(datetrans) as month,fundtype, amount,remarks FROM rcddeposit WHERE isactivercd=1 AND (datetrans>='"+  year  +"-01-01' AND datetrans<='"+  year +"-12-31') ", new String[0],new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				int month = rs.getInt("month");
				double amount = rs.getDouble("amount");
				String fund = FundType.typeName(rs.getInt("fundtype"));
				String remarks = rs.getString("remarks");
				
				
				if("BEGINNING BALANCE".equalsIgnoreCase(remarks)) {
					if(mapBeginningBal!=null && mapBeginningBal.size()>0) {
						if(mapBeginningBal.containsKey(fund)) {
							if(mapBeginningBal.get(fund).containsKey(month)) {
								double newAmount = mapBeginningBal.get(fund).get(month) + amount;
								mapBeginningBal.get(fund).put(month, newAmount);
							}else {
								mapBeginningBal.get(fund).put(month, amount);
							}
						}else {
							mapBegMonth = new LinkedHashMap<Integer, Double>();
							mapBegMonth.put(month, amount);
							mapBeginningBal.put(fund, mapBegMonth);
						}
					}else {
						mapBegMonth.put(month, amount);
						mapBeginningBal.put(fund, mapBegMonth);
					}
				}else {
					//deposit
					if(mapDeposit!=null && mapDeposit.size()>0) {
						if(mapDeposit.containsKey(fund)) {
							if(mapDeposit.get(fund).containsKey(month)) {
								double newAmount = mapDeposit.get(fund).get(month) + amount;
								mapDeposit.get(fund).put(month, newAmount);
							}else {
								mapDeposit.get(fund).put(month, amount);
							}
						}else {
							mapDepMonth = new LinkedHashMap<Integer, Double>();
							mapDepMonth.put(month, amount);
							mapDeposit.put(fund, mapDepMonth);
						}
					}else {
						mapDepMonth.put(month, amount);
						mapDeposit.put(fund, mapDepMonth);
					}
				}
				
			}
			
			rs = OpenTableAccess.query("select month(alldatetrans) as month, fundtype,amount from rcdallcontroller WHERE isactiveall=1 AND (alldatetrans>='"+  year +"-01-01' AND alldatetrans<='"+  year +"-12-31')", new String[0],new WebTISDatabaseConnect());
			
			while(rs.next()) {
				int month = rs.getInt("month");
				double amount = rs.getDouble("amount");
				String fund = FundType.typeName(rs.getInt("fundtype"));
				
				if(mapCollection!=null && mapCollection.size()>0) {
					if(mapCollection.containsKey(fund)) {
						if(mapCollection.get(fund).containsKey(month)) {
							double newAmount = mapCollection.get(fund).get(month) + amount;
							mapCollection.get(fund).put(month, newAmount);
						}else {
							mapCollection.get(fund).put(month, amount);
						}
					}else {
						mapCollMonth = new LinkedHashMap<Integer, Double>();
						mapCollMonth.put(month, amount);
						mapCollection.put(fund, mapCollMonth);
					}
				}else {
					mapCollMonth.put(month, amount);
					mapCollection.put(fund, mapCollMonth);
				}
				
			}
			int size = (FundType.values().length*FundType.values().length) + FundType.values().length;
			
			String[][] data = new String[size][15];
			/*data[0][0] = "Fund";
			data[0][1] = "Description";
			int x = 2;
			for(int m=1; m<=12; m++) {
				data[0][x] = DateUtils.getMonthName(m);
				x++;		
			}*/
			
			int xx=0;
			double beginning [] = new double[12];
			double collection [] = new double[12];
			double deposit [] = new double[12];
			
			double totalYearlyCollection = 0d;
			double totalYearlyDeposit = 0d;
			
			for(FundType f : FundType.values()) {
				
				//xx += 1;
				
				data[xx][0] = f.getName();
				
				for(int i=1; i<13;i++) {
					data[xx][i] = "";
				}
				
				
				
				//beginning
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "BEGINNING BALANCE";
				int a=2;
				int index = 0;
				double totalYearlyBeginning = 0d;
				for(int m=1; m<=12; m++) {//month
					
					if(mapBeginningBal!=null && mapBeginningBal.size()>0) {
						if(mapBeginningBal.containsKey(f.getName())) {
							if(mapBeginningBal.get(f.getName()).containsKey(m)) {
								data[xx][a] = Currency.formatAmount(mapBeginningBal.get(f.getName()).get(m));
								beginning[index] = mapBeginningBal.get(f.getName()).get(m);
								if(m==1) {
									totalYearlyBeginning = mapBeginningBal.get(f.getName()).get(m);
								}
							}else {
								data[xx][a] = "";
								beginning[index] = 0.00;
							}
						}else {
							data[xx][a] = "";
							beginning[index] = 0.00;
						}
					}else {
						data[xx][a] = "";
						beginning[index] = 0.00;
					}
					a++;
					index++;
				}
				data[xx][14] = Currency.formatAmount(totalYearlyBeginning);
				
				
				
				//collection
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "COLLECTION";
				a=2;
				index=0;
				
				for(int m=1; m<=12; m++) {//month
					if(mapCollection!=null && mapCollection.size()>0) {
						if(mapCollection.containsKey(f.getName())) {
							if(mapCollection.get(f.getName()).containsKey(m)) {
								data[xx][a] = Currency.formatAmount(mapCollection.get(f.getName()).get(m));
								collection[index] = mapCollection.get(f.getName()).get(m);
								totalYearlyCollection += mapCollection.get(f.getName()).get(m);
							}else {
								data[xx][a] = "";
								collection[index] = 0.00;
							}
						}else {
							data[xx][a] = "";
							collection[index] = 0.00;
						}
					}else {
						data[xx][a] = "";
						collection[index] = 0.00;
					}
					a++;
					index++;
				}
				
				//total collection
				data[xx][14] = totalYearlyCollection==0? "" : Currency.formatAmount(totalYearlyCollection);
				
				
				//deposit
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "DEPOSIT";
				a=2;
				index=0;
				
				for(int m=1; m<=12; m++) {//month
					if(mapDeposit!=null && mapDeposit.size()>0) {
						if(mapDeposit.containsKey(f.getName())) {
							if(mapDeposit.get(f.getName()).containsKey(m)) {
								data[xx][a] = Currency.formatAmount(mapDeposit.get(f.getName()).get(m));
								deposit[index] = mapDeposit.get(f.getName()).get(m);
								totalYearlyDeposit += mapDeposit.get(f.getName()).get(m);
							}else {
								data[xx][a] = "";
								deposit[index] = 0.00;
							}
						}else {
							data[xx][a] = "";
							deposit[index] = 0.00;
						}
					}else {
						data[xx][a] = "";
						deposit[index] = 0.00;
					}
					a++;
					index++;
				}
				
				data[xx][14] = totalYearlyDeposit==0? "" : Currency.formatAmount(totalYearlyDeposit);
				
				
				
				//Ending
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "ENDING BALANCE";
				a=2;
				index=0;
				double totalYearlyEnding = 0d;
				for(int i=0; i<12; i++) {//month
					boolean isNotZero=false;
					isNotZero = collection[i]>0? true : false;
					if(!isNotZero) {
						isNotZero = deposit[i]>0? true : false;
					}
					double amount = (beginning[i] + collection[i]) - deposit[i];
					data[xx][a] = isNotZero==false? "" : Currency.formatAmount(amount);
					a++;
				}
				totalYearlyEnding = (totalYearlyBeginning + totalYearlyCollection) - totalYearlyDeposit;
				data[xx][14] = totalYearlyEnding==0? "" : Currency.formatAmount(totalYearlyEnding);
				totalYearlyBeginning = 0d;
				totalYearlyEnding = 0d;
				totalYearlyDeposit = 0d;
				totalYearlyCollection = 0d;
				xx++;
			}
			
			
			
			for(int b=0; b<size; b++) {
				
					Reports r = Reports.builder()
							.f1(data[b][0])
							.f2(data[b][1])
							.f3(data[b][2])
							.f4(data[b][3])
							.f5(data[b][4])
							.f6(data[b][5])
							.f7(data[b][6])
							.f8(data[b][7])
							.f9(data[b][8])
							.f10(data[b][9])
							.f11(data[b][10])
							.f12(data[b][11])
							.f13(data[b][12])
							.f14(data[b][13])
							.f15(data[b][14])
							.build();
					rcds.add(r);
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
	}
	
	public void loadHighestIssuedChecks() {
		
	ResultSet	rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,c.date_disbursement,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND c.chkstatus='1' AND (c.date_disbursement>='"+  startDate +"' AND c.date_disbursement<='"+  last_Date +"') ORDER BY c.cheque_amount DESC LIMIT 10",new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				
				String mode = rs.getString("bank_account_name");
				String checkNo = rs.getString("cheque_no");
				double amnt = rs.getDouble("amount");
				String disBurseDate = rs. getString("date_disbursement");
				String mooe = mapMooe.get(rs.getLong("moid")).getDescription();
				String opis = mapOffices.get(rs.getLong("offid")).getName();
				String payee = rs.getString("payee");
				
				Reports rr = Reports.builder()
						.f1(DateUtils.convertDateToMonthDayYear(disBurseDate))
						.f2(checkNo)
						.f3(payee)
						.f4(Currency.formatAmount(amnt))
						.f5(mode)
						.f6(opis)
						.f7(mooe)
						.build();
				rptHighestIssuedChecks.add(rr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void monthlyCheckIssued() {
		
		ResultSet rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,MONTH(c.date_disbursement) as month,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND c.chkstatus=1 AND (c.date_disbursement>='"+  year +"-01-01" +"' AND c.date_disbursement<='"+  year+"-12-31" +"')", new String[0],new BankChequeDatabaseConnect());
		
		Map<String, Map<Integer, Double>> mapMonth = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapMode = new LinkedHashMap<Integer, Double>();
		try {
			while(rs.next()) {
				String mode = rs.getString("bank_account_name");
				double amount = rs.getDouble("amount");
				int month = rs.getInt("month");
				if(mapMonth!=null && mapMonth.size()>0) {
					if(mapMonth.containsKey(mode)) {
						if(mapMonth.get(mode).containsKey(month)) {
							double newAmount = mapMonth.get(mode).get(month) + amount;
							mapMonth.get(mode).put(month, newAmount);
						}else {
							mapMonth.get(mode).put(month, amount);
						}
					}else {
						mapMode = new LinkedHashMap<Integer, Double>();
						mapMode.put(month, amount);
						mapMonth.put(mode, mapMode);
					}
				}else {
					mapMode.put(month, amount);
					mapMonth.put(mode, mapMode);
				}
			}
			
			Map<String, Map<Integer, Double>> mapOrg = new TreeMap<String, Map<Integer, Double>>(mapMonth);
			//rptMonthCheckIssuedsPerAccounts
			
			//budget
			Map<String, Double> budgetData = BudgetMonitoring.amountBudget(Integer.valueOf(getYear()),0);
			
			//int size = mapOrg.size() + 1;
			int size = mapOrg.size();
			String[][] data = new String[size][16];
			
			//data[0][0] = "Name";
			
			//for(int m=1; m<=12; m++) {
				//data[0][m] = DateUtils.getMonthName(m);
			//}
			
			
			//int sz = 1;
			int sz = 0;
			for(String fund : mapOrg.keySet()) {
				data[sz][0] = fund;
				double totalamount = 0d;
				for(int month=1; month<=12; month++) {
					if(mapOrg.get(fund).containsKey(month)) {
						data[sz][month] = Currency.formatAmount(mapOrg.get(fund).get(month));
						totalamount += mapOrg.get(fund).get(month);
					}else {
						data[sz][month] = "";
					}
				}
				//this for budget
				if(budgetData!=null && budgetData.containsKey(fund)) {
					
					
					//program static
					//STATIC STATIC STATIC
					double deposit = 0d;
					double fundAmount = budgetData.get(fund);
					if("GENERAL FUND".equalsIgnoreCase(fund)) {
						deposit = RCDDeposit.totalDeposit(Integer.valueOf(year),1);
					}else if("MOTORPOOL".equalsIgnoreCase(fund)) {
						deposit = RCDDeposit.totalDeposit(Integer.valueOf(year), 2);
					}else if("TRUST FUND".equalsIgnoreCase(fund)) {
						deposit = RCDDeposit.totalDeposit(Integer.valueOf(year), 3);
					}
					double yearBeginningBalance = BudgetMonitoring.yearBeginningBalance(fund, Integer.valueOf(year));
					data[sz][13] = Currency.formatAmount(fundAmount + deposit + yearBeginningBalance);//budget
					
					//totalamount += yearBeginningBalance;//adding year beginning balance
					
					
					fundAmount = (fundAmount + deposit) -totalamount;
					fundAmount += yearBeginningBalance;
					
					
					data[sz][14] = Currency.formatAmount(totalamount);//used
					data[sz][15] = Currency.formatAmount(fundAmount);//remaining
				}else {
					data[sz][13] = "00.00";//budget
					data[sz][14] = Currency.formatAmount(totalamount);//used
					data[sz][15] = "00.00";//Currency.formatAmount(-totalamount);//remaining
				}
				sz++;
			}
			
			//data[0][13] = "Budget";
			//data[0][14] = "Used";
			//data[0][15] = "Remaining";
			
			//int y=0;
			for(int x=0; x<size; x++) {
					Reports name = new Reports();
					name.setF1(data[x][0]);
					name.setF2(data[x][1]);
					name.setF3(data[x][2]);
					name.setF4(data[x][3]);
					name.setF5(data[x][4]);
					name.setF6(data[x][5]);
					name.setF7(data[x][6]);
					name.setF8(data[x][7]);
					name.setF9(data[x][8]);
					name.setF10(data[x][9]);
					name.setF11(data[x][10]);
					name.setF12(data[x][11]);
					name.setF13(data[x][12]);
					
					name.setF14(data[x][13]);//budget
					name.setF15(data[x][14]);//used
					name.setF16(data[x][15]);//remaining
					
					rptMonthCheckIssuedsPerAccounts.add(name);
					
				//System.out.println(data[x][0] + " = " + data[x][1] + " = " + data[x][2]);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private List<Reports> chks;
	public void loadCheckIssued(String fundName, int month) {
		chks = Chequedtls.retrieveByFundName(fundName, Integer.valueOf(getYear()), month);
		
	}
	private List<Reports> budRpts;
	private String yearBeginningBalance;
	public void loadMonthlyBudget(String fundName) {
		double yearBeginningBalance = BudgetMonitoring.yearBeginningBalance(fundName,Integer.valueOf(getYear()));
		setYearBeginningBalance(Currency.formatAmount(yearBeginningBalance));
		budRpts = BudgetMonitoring.amountBudget(fundName, Integer.valueOf(getYear()) ,0);
	}
	
	public void loadCheckWriting() {
		ResultSet rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,c.date_disbursement,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND (c.date_disbursement>='"+  year +"-"+ month +"-01' AND c.date_disbursement<='"+  year +"-"+ month +"-31')", new String[0],new BankChequeDatabaseConnect());
		double amountToday=0d;
		int totalCancelledCheck=0;
		double amountMonth=0d;
		String lastTransaction="";
		double lastPayeeAmount=0d;
		String payee = "";
		String disBurseDate="";
		String remarks = "";
		String mooe = "";
		String opis = "";
		String mode="";
		String checkNo="";
		
		List<Reports> rpt = new ArrayList<Reports>();
		Map<String, List<Reports>> mapMul = new LinkedHashMap<String, List<Reports>>();
		List<Reports> mul = new ArrayList<Reports>();
		try {
			while(rs.next()) {
				mode = rs.getString("bank_account_name");
				checkNo = rs.getString("cheque_no");
				double amnt = rs.getDouble("amount");
				String stat = rs. getString("chkstatus");
				disBurseDate = rs. getString("date_disbursement");
				remarks = rs.getString("chkremarks");
				mooe = mapMooe.get(rs.getLong("moid")).getDescription();
				opis = mapOffices.get(rs.getLong("offid")).getName();
				payee = rs.getString("payee");
				
				Reports rr = Reports.builder()
						.f1(DateUtils.convertDateToMonthDayYear(disBurseDate))
						.f2(checkNo)
						.f3(payee)
						.f4(Currency.formatAmount(amnt))
						.f5(mode)
						.f6(opis)
						.f7(mooe)
						.f10(remarks)
						.build();
				
				//get the total amount today
				if("1".equalsIgnoreCase(stat)) {//issued
					
					
					
					//monthly
					amountMonth += amnt;
					
					//today
					if(dateToday.equalsIgnoreCase(disBurseDate)) {
						amountToday += amnt;
					}
					
					//last transaction
					//lastTransaction = rs.getString("payee");
					lastPayeeAmount = amnt;
					
					
					if(mapMul!=null && mapMul.size()>0) {
						if(mapMul.containsKey(payee)) {
							mul = mapMul.get(payee);
							mul.add(rr);
							mapMul.put(payee, mul);
						}else {
							mul = new ArrayList<Reports>();
							mul.add(rr);
							mapMul.put(payee, mul);
						}
					}else {
						mul.add(rr);
						mapMul.put(payee, mul);
					}
					
					
				}else {
					//get total cancelled check
					totalCancelledCheck +=1;
					rr.setF9("CANCELLED");
					
					
				}
				rpt.add(rr);
				
			}
			
			Collections.reverse(rpt);//reverse the order the last will be the first
			int cnt = 1;
			for(Reports r : rpt) {
				
				if(cnt<=10) {
					r.setF8(r.getF3());
					rptRecentChecksIssueds.add(r);
				}else if(cnt>10) {
					break;
				}
				
				//do not add 1 as it is the last transaction
				/*if(cnt>1 && cnt<12) {
					r.setF8(r.getF3());
					rptRecentChecksIssueds.add(r);
				}else if(cnt==12) {
					break;
				}*/
				cnt++;
			}
			
			
			
			//this for multiple check issued list
			if(mapMul!=null && mapMul.size()>0) {
				
				for(String name : mapMul.keySet()) {
					int size = mapMul.get(name).size();
					if(size>1) {
						Reports rv = Reports.builder().f3(name).build();
						rptMultipleCheckIssueds.add(rv);
						for(Reports v : mapMul.get(name)) {
							v.setF3("");
							rptMultipleCheckIssueds.add(v);
						}
					}
				}
				
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	private void loadOffices() {
		mapOffices = new LinkedHashMap<Long, Offices>();
		ResultSet rs = OpenTableAccess.query("SELECT * FROM offices WHERE isactiveoff=1",new String[0], new WebTISDatabaseConnect());
		try {
			while(rs.next()) {
				Offices off = Offices.builder()
						.id(rs.getInt("offid"))
						.name(rs.getString("name"))
						.code(rs.getString("code"))
						.headOfOffice(rs.getString("headname"))
						.abr(rs.getString("abrname"))
						.isActive(rs.getInt("isactiveoff"))
						.departmentId(rs.getInt("departmentid"))
						.build();
				mapOffices.put(off.getId(), off);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadMooe() {
		mapMooe = new LinkedHashMap<Long, Mooe>();
		ResultSet rs = OpenTableAccess.query("SELECT * FROM mooe WHERE isactivem=1",new String[0], new WebTISDatabaseConnect());
		try {
			while(rs.next()) {
				Mooe moe = Mooe.builder()
						.id(rs.getLong("moid"))
						.code(rs.getString("code"))
						.dateTrans(rs.getString("encodedate"))
						.description(rs.getString("description"))
						.budgetAmount(rs.getDouble("budgetamount"))
						.isActive(rs.getInt("isactivem"))
						.yearBudget(rs.getInt("yearbudget"))
						.offices(Offices.builder().id(rs.getLong("offid")).build())
						.build();
				mapMooe.put(moe.getId(), moe);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadCollector() {
		mapCollector = new LinkedHashMap<Integer, Collector>();
		for(Collector c : Collector.retrieve("", new String[0])) {
			mapCollector.put(c.getId(), c);
		}
	}
	
	public void printCollection(int month) {
		try{
			List<Reports> rss = new ArrayList<Reports>();
			
			int size = getCollectionPrintData().size();
			
			List<Reports> data = new ArrayList<Reports>();
			
			double totalAmount = 0d;
			for(Reports r : getCollectionPrintData()) {
				Reports a = new Reports();
				double amount = 0d;
				String val = "0.00";
				switch(month) {
					case 0 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF14()); 
						try {val = r.getF14().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 1 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF2()); 
						try {val = r.getF2().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 2 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF3()); 
						try {val = r.getF3().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 3 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF4()); 
						try {val = r.getF4().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 4 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF5()); 
						try {val = r.getF5().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 5 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF6()); 
						try {val = r.getF6().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 6 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF7()); 
						try {val = r.getF7().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 7 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF8()); 
						try {val = r.getF8().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 8 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF9()); 
						try {val = r.getF9().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 9 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF10()); 
						try {val = r.getF10().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 10 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF11()); 
						try {val = r.getF11().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 11 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF12()); 
						try {val = r.getF12().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 12 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF13()); 
						try {val = r.getF13().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
				}
				
				
				data.add(a);
			}
			
			
			if(month==0) {
				//summary report
				double cancelledOR = 0d;
				double ctcCollection = 0d;
				double mtoCtcCollection = 0d;
				double barangayCTCCollection = 0d;
				for(int m=1; m<=12; m++) {
					cancelledOR +=totalCancelledOR[m];
					ctcCollection += totalCTCCollection[m];
					mtoCtcCollection += totalMTOCTCCollection[m];
					barangayCTCCollection += totalBarangayCTCCollection[m];
				}
				totalCancelledOR[month] = cancelledOR;
				totalCTCCollection[month] = ctcCollection;
				totalMTOCTCCollection[month] = mtoCtcCollection;
				totalBarangayCTCCollection[month] = barangayCTCCollection;
			}
			
			///ADDED INFO
			Reports aa = new Reports();
			aa.setF1("Total Cancelled OR");
			aa.setF2("("+Currency.formatAmount(totalCancelledOR[month])+")");
			data.add(aa);
			
			aa = new Reports();
			aa.setF1("CTC Collection" + "("+Currency.formatAmount(totalCTCCollection[month])+")");
			aa.setF2("");
			data.add(aa);
			
			aa = new Reports();
			aa.setF1("MTO CTC Collection" + "("+Currency.formatAmount(totalMTOCTCCollection[month])+")");
			aa.setF2("");
			data.add(aa);
			
			aa = new Reports();
			aa.setF1("Barangay CTC Collection" + "("+Currency.formatAmount(totalBarangayCTCCollection[month])+")");
			aa.setF2("");
			data.add(aa);
			
			double sharedAmount = totalBarangayCTCCollection[month] * 0.50;
			aa = new Reports();
			aa.setF1("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
			aa.setF2("");
			data.add(aa);
			
			
			if(size>35) {
				int cnt=1;
				List<Reports> r1 = new ArrayList<Reports>();
				List<Reports> r2 = new ArrayList<Reports>();
				int cntRem = 1;
				for(Reports r : data) {
					if(cnt<36) {
						r1.add(r);
					}else {
						r2.add(r);
						cntRem++;
					}
					cnt++;
				}
				cntRem -=1;
				
				//this for r2 only data
				for(int i=cntRem; i<36; i++) {//supply the remaining fields
					r2.add(new Reports());
				}
				
				
				//combine the two list
				//this is for the first page only
				//which is less than 35 rows
				for(int i=0; i<35;i++) {
					Reports r = new Reports();
					r.setF1(r1.get(i).getF1());
					r.setF2(r1.get(i).getF2());
					
					r.setF3(r2.get(i).getF1());
					r.setF4(r2.get(i).getF2());
					
					rss.add(r);
				}
				
				//adding for more than
				//this code is for more than one page
				if(cntRem>=36) {
					for(int i=35; i<cntRem;i++) {
						Reports r = new Reports();
						r.setF1(r2.get(i).getF1());
						r.setF2(r2.get(i).getF2());
						i+=1;
						try{r.setF3(r2.get(i).getF1());}catch(IndexOutOfBoundsException ie) {}
						try{r.setF4(r2.get(i).getF2());}catch(IndexOutOfBoundsException ie) {}
						
						rss.add(r);
					}
				}
				
			}else {
				rss = data;
			}
			
			/*for(Reports r : rss) {
				System.out.println("f1: " + r.getF1() + " f2: " + r.getF2() + " f3: " + r.getF3() + " f4: " + r.getF4());
			}*/
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME = "summaryonly";
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rss);
	  		HashMap param = new HashMap();
	  		UserDtls user = Login.getUserLogin().getUserDtls();
	  		if(month==0) {
	  			param.put("PARAM_TITLE", "SUMMARY OF COLLECTION FOR THE YEAR " + getYear() + " AS OF " + DateUtils.getMonthName(DateUtils.getCurrentMonth())	.toUpperCase());
	  		}else {	
	  			param.put("PARAM_TITLE", "SUMMARY OF COLLECTION FOR THE MONTH OF "+DateUtils.getMonthName(month).toUpperCase() + " " + getYear());
	  		}
	  		param.put("PARAM_TOTAL", "Php"+Currency.formatAmount(totalAmount));
	  		param.put("PARAM_PRINTED_DATE", DateUtils.getCurrentDateMMMMDDYYYY());
	  		
	  		
	  		param.put("PARAM_PREPAREDBY", user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
	  		
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
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
	  	    
	  	    
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
		                //System.out.println("printReportAll read : " + length);
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
		     
		       // backupPrintDispense();
		        
			
		
			System.out.println("pdf successfully created...");
			}catch(Exception e){
				e.printStackTrace();
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
	
	
	
	public void seriesTransactionHistory(Reports rt) {
		String valSeries = rt.getF4();
		String collectorId = rt.getF8();
		String formTypeId = rt.getF9();
		String issuedQty = rt.getF5();
		String logId = rt.getF10();
		
		seriesHisDtls = new ArrayList<Reports>();
		if(valSeries!=null && !valSeries.isEmpty() && valSeries.contains("-")) {
			
			String[] val = valSeries.split("-");
			long start = Long.valueOf(val[0]);
			long end = Long.valueOf(val[1]);
			
			String st = "SELECT receiveddate,beginningNoCol,endingNoCol,colpcs,colstatus,formtypecol,amount,rptgroup,fundid,isid FROM collectioninfo WHERE isactivecol=1 AND formtypecol="+formTypeId + " AND isid="+ collectorId + " AND logid=" + logId;
			int count = 1;
			for(long i=start; i<=end; i++) {
				if(count==1) {
					st += " AND (beginningNoCol=" + i + " OR endingNoCol=" + i ;
				}else {
					st += " OR beginningNoCol=" + i + " OR endingNoCol=" + i ;
				}
				count++;
			}
			st += ")";
			
			
			ResultSet rs = OpenTableAccess.query(st, new String[0], new WebTISDatabaseConnect());
			int issued = Integer.valueOf(issuedQty);
			
			try {
				while(rs.next()) {
					
					String name = mapCollector.get(rs.getInt("isid")).getName();
					String fund = FundType.typeName(rs.getInt("fundid"));
					String status = rs.getInt("colstatus")==FormStatus.NOT_ALL_ISSUED.getId()? "ISSUED" : FormStatus.nameId(rs.getInt("colstatus"));
					
					int pcs = rs.getInt("colpcs");
					
					issued -= pcs;
					
					String formName = FormType.nameId(rs.getInt("formtypecol"));		
							seriesHisDtls.add(
							Reports.builder()
							.f1(rs.getString("receiveddate"))
							.f2(name)
							.f3(formName)
							.f4(fund)
							.f5(rs.getString("beginningNoCol")+"-" + rs.getString("endingNoCol"))
							.f6(pcs+"")
							.f7(status)
							.f8(Currency.formatAmount(rs.getDouble("amount")))
							.f9(rs.getString("rptgroup"))
							.build()						
							);	
				}
				setRemainingQty(issued+"");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	
	public void loadNoBusinessRenewal() {
		bzs = new ArrayList<BusinessPermit>();
		Map<String, BusinessPermit> mapData = new LinkedHashMap<String, BusinessPermit>();
		int yearNow = Integer.valueOf(getYear());
		int prevYear = yearNow - 1;
		
		List<BusinessPermit> ps = BusinessPermit.retrieve(" AND YEAR(bz.datetrans)="+prevYear + " ORDER BY bz.businessname", new String[0]);
		for(BusinessPermit p : ps) {
				String val  = StringUtils.normalizeSpace(p.getBusinessName());
				mapData.put(val, p);
		}
		 ps = BusinessPermit.retrieve(" AND YEAR(bz.datetrans)="+yearNow + " ORDER BY bz.businessname", new String[0]);
		
		for(BusinessPermit s : ps) {
			if(mapData!=null && mapData.size()>0) {
				String val  = StringUtils.normalizeSpace(s.getBusinessName());
				if(mapData.containsKey(val)) {
					mapData.remove(val);//removing renewed business
				}
			}
		}
		
		for(BusinessPermit bz : mapData.values()) {
			bzs.add(bz);
		}
		
	}
	
}































