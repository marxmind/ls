package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.primefaces.PrimeFaces;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import com.italia.municipality.lakesebu.controller.AppSetting;
import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.BudgetMonitoring;
import com.italia.municipality.lakesebu.controller.BusinessIndex;
import com.italia.municipality.lakesebu.controller.ChequeXML;
import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.NumberToWords;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.Signatories;
import com.italia.municipality.lakesebu.controller.VrTransaction;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.BudgetType;
import com.italia.municipality.lakesebu.enm.VrTransStatus;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.OfficeBudget;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.CheckInternetConnection;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.SendSMS;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named("chk")
@ViewScoped
@Data
public class ChkBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5435876855643541L;
	
	private int fundId;
	private List funds;
	private int searchFundId;
	private List searchFunds;
	private int fundBudgetId;
	private List fundBudgets;
	
	private String searchPayTo;
	private Date dateFrom;
	private Date dateTo;
	
	private long officeId;
	private List offices;
	private Map<Long, Offices> officeData;
	private List mooes;
	private Map<Long, String> mapMooeForOffice;
	private long mooeId;
	//private Map<Long, Mooe> mooeMap;
	private int treasPersonnelId;
	private List treasurerPersonnels;
	private int officialId;
	private List officials;
	private Map<Integer, BankAccounts> fundsData;
	private String checkSeriesNo;
	private Date dateTime;
	private String payTo;
	//private String natureOfPayment;
	private String inputAmount;
	private String remarks;
	private String words="0.00 Pesos Only";
	private int statusId;
	private List statusList;
	private Map<Integer, Signatories> signatoryData;
	private List<Chequedtls> cheques;
	private String grandTotal;
	private boolean includeDate;
	private int year;
	private List years;
	private Signatories primaryOfficial;
	private Signatories primaryOfficer;
	private Chequedtls selectedData;
	private List<OfficeBudget> budgets;
	private String totalBudgetRelease;
	private int offBudId;
	private List offBuds;
	private List<Chequedtls> selectedChecks;
	private List<Reports> fundData;
	private List<Reports> fundOfficeData;
	private int yearBudId;
	private List yearBuds;
	private int yearOfficeBudId;
	private List yearOfficeBuds;
	private String[] alertBudgetMsg;
	private int yearDataClick;
	
	private long officeHisId;
	private List officesHis;
	private int yearHisId;
	private List yearsHIs;
	private List<OfficeBudget> budgetsHis;
	
	private BarChartModel barModel;
	
	private BarChartModel barModel2;
	private Map<Double, Reports> mapPerOfficeData;
	private int fundBudgetPerOfficeId;
	private List fundBudgetPerOffices;
	private int perFundOfficeId;
	private List perFundOffices;
	
	private String printName=GlobalVar.EPSON_L3110;
	private boolean printMode;
	
	private List<BudgetMonitoring> buds;
	private int monYearId;
	private List monYears;
	private int monMonthId;
	private List monMonths;
	private int monAccId;
	private List monAccounts;
	private BudgetMonitoring budgetData;
	private double monAmount;
	private int type;
	private List types;
	private int cycle;
	private String budgetRemarks;
	
	private long searchOfficeId;
	private List searchOffices;
	
	private List<VrTransaction> vrTrans;
	private VrTransaction vrData;
	private String searchVr;
	private List vrStatus;
	private List vrOffices;
	private List vrMooes;
	private List vrAccounts;
	private VrTransaction selectedVrTransaction;
	
	public void clickItem(Chequedtls chk) {
		setSelectedData(chk);
		setDateTime(DateUtils.convertDateString(chk.getDate_disbursement(), "yyyy-MM-dd"));
		setFundId(chk.getFundTypeId());
		setOfficeId(chk.getOffice().getId());
		System.out.println("Office Id: " + chk.getOffice().getId());
		setMooeId(chk.getMoe().getId());
		setYearDataClick(Integer.valueOf(chk.getDate_disbursement().split("-")[0]));
		loadMooeForOffice();
		setCheckSeriesNo(chk.getCheckNo());
		setPayTo(chk.getPayToTheOrderOf());
		//setNatureOfPayment(getMooeMap().get(chk.getMoe().getId()).getDescription());
		setInputAmount(Currency.formatAmount(chk.getAmount()));
		setStatusId(chk.getStatus());
		setRemarks(chk.getRemarks());
		setTreasPersonnelId(chk.getSignatory1());
		setOfficialId(chk.getSignatory2());
		generateWords();//generate words
	}
	
	public void modeMsg() {
		Application.addMessage(1, "Printer Mode", "You have " + (isPrintMode()==true? "activated the printer " + GlobalVar.EPSON_L220 : "activated the printer " + GlobalVar.EPSON_L3110));
		//RCDReader.saveCollectorMode(isCollectorsMode()==true? "ON" : "OFF");
		setPrintName(isPrintMode()==false? GlobalVar.EPSON_L3110 : GlobalVar.EPSON_L220);
		AppSetting.updatePrintMode(isPrintMode());
	}
	
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		System.out.println("init.......");
		
		//vr transaction
				defaultVr();
				vrOffices = new ArrayList<>();
				vrAccounts = new ArrayList<>();
				
				vrStatus = new ArrayList<>();
				for(VrTransStatus s : VrTransStatus.values()){
					vrStatus.add(new SelectItem(s.getId(), s.getName()));
				}
		
		searchFundId=2;//General Fund
		//this for NTA
		loadMonBudgetDefault();
		
			String mode = AppSetting.getPrintMode();
			System.out.println("Checking printer mode: " + mode);
			if(GlobalVar.EPSON_L3110.equalsIgnoreCase(mode)){
				printMode = false;
				setPrintName(GlobalVar.EPSON_L3110);
			}else {
				printMode = true;
				setPrintName(GlobalVar.EPSON_L220);
			}
		
		
		setIncludeDate(true);//default set to true
		Date dateSource = DateUtils.getDateToday();
		selectedChecks = new ArrayList<Chequedtls>();
		yearDataClick=DateUtils.getCurrentYear();
		dateTime = dateSource;
		dateFrom = dateSource;
		dateTo = dateSource;
		offices = new ArrayList<>();
		officesHis = new ArrayList<>();
		searchFunds = new ArrayList<>();
		funds = new ArrayList<>();
		mooes = new ArrayList<>();
		officeId=0;//office
		officeHisId=0;
		fundId=2;//General Fund
		fundBudgetPerOfficeId=2;//General Fund
		fundBudgetId=2;//General Fund
		fundBudgets = new ArrayList<>();
		fundBudgets.add(new SelectItem(0, "All Fund"));
		loadNewCheckNo();//load new check no
		mooes.add(new SelectItem(0,"Please Select"));
		fundsData = new LinkedHashMap<Integer,BankAccounts>();
		fundBudgetPerOffices = new ArrayList<>();
		for(BankAccounts b : BankAccounts.retrieveAll()) {
			funds.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			searchFunds.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			fundBudgets.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			fundsData.put(b.getBankId(), b);
			fundBudgetPerOffices.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			vrAccounts.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
		}
		offBuds = new ArrayList<>();
		offBuds.add(new SelectItem(999, "All"));
		officeData = new LinkedHashMap<Long, Offices>();
		perFundOfficeId=0;//ALL
		perFundOffices=new ArrayList<>();
		perFundOffices.add(new SelectItem(0, "All Offices"));
		searchOffices = new ArrayList<>();
		for(Offices d : Offices.retrieve(" ORDER BY name", new String[0])) {
			offices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			officesHis.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			perFundOffices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			offBuds.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			officeData.put(d.getId(), d);
			searchOffices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			
			try {
			int len = d.getName().length();
				if(len>50) {
					vrOffices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName().substring(0, 46) + "..."));
				}else {
					vrOffices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
				}
			}catch(Exception e) {}
		}
		
		officials = new ArrayList<>();
		treasurerPersonnels = new ArrayList<>();
		signatoryData = Signatories.retrieveAll();
		officials.add(new SelectItem(0, "Select Official"));
		treasurerPersonnels.add(new SelectItem(0, "Select Personnel"));
		for(Signatories s : signatoryData.values()) {
			if(s.getIsOfficial()==1) {
				officials.add(new SelectItem(s.getId(), s.getName()));
				if(s.getPrimary()==1) {
					setOfficialId(s.getId());
					setPrimaryOfficial(s);
				}
			}else {
				treasurerPersonnels.add(new SelectItem(s.getId(), s.getName()));
				if(s.getPrimary()==1) {
					setTreasPersonnelId(s.getId());
					setPrimaryOfficer(s);
				}
			}
		}
		statusList = new ArrayList<>();
		statusId=1; 
		setRemarks("POSTED CHECK");
		statusList.add(new SelectItem(1,"Posted"));
		statusList.add(new SelectItem(2,"Cancelled"));
		
		
		year=DateUtils.getCurrentYear();//current year
		yearHisId=year;
		years = new ArrayList<>();
		yearsHIs = new ArrayList<>();
		yearBuds = new ArrayList<>();
		yearOfficeBuds = new ArrayList<>();
		yearBudId = DateUtils.getCurrentYear();
		yearOfficeBudId = yearBudId;
		for(int y=2016; y<=year; y++) {
			years.add(new SelectItem(y, y+""));
			yearsHIs.add(new SelectItem(y, y+""));
			yearBuds.add(new SelectItem(y, y+""));
			yearOfficeBuds.add(new SelectItem(y, y+""));
		}
		
		
		
		search();
		
		createBarModel();
		createBarModelPerOffice();
	}
	
	
	
	public void loadMooeForOffice() {
			int year = getYearDataClick();
			System.out.println("year selected: " + year);
			mooes = new ArrayList<>();
			mapMooeForOffice = new LinkedHashMap<Long, String>();
			List<Mooe> moes = Mooe.retrieve(" AND ofs.offid="+getOfficeId() + " AND moe.yearbudget=" + year, new String[0]);
			for(Mooe mo : moes) {
				double usedAmount = Chequedtls.mooeUsed(mo.getOffices().getId(), mo.getId(), year);
				String budget=Currency.formatAmount(mo.getBudgetAmount());
				String usedAm=Currency.formatAmount(usedAmount);
				mooes.add(new SelectItem(mo.getId(),mo.getCode() + "-" + mo.getDescription() + " (BUDGET:"+ budget +" USED:"+usedAm+")"));
				mapMooeForOffice.put(mo.getId(),budget+":"+usedAm+":"+mo.getDescription());
			}
			if(moes!=null && moes.size()==0) {mooes.add(new SelectItem(0,"No Assigned MOOE (0.00)"));}
	}
	
	public void loadMooeForOfficeHis() {
		budgetsHis = new ArrayList<OfficeBudget>();
		List<Mooe> moes = Mooe.retrieve(" AND ofs.offid="+getOfficeHisId() + " AND moe.yearbudget=" + getYearHisId(), new String[0]);
		for(Mooe mo : moes) {
			double usedAmount = Chequedtls.mooeUsed(mo.getOffices().getId(), mo.getId(), getYearHisId());
			String budget=Currency.formatAmount(mo.getBudgetAmount());
			String usedAm=Currency.formatAmount(usedAmount);
			double remaining = mo.getBudgetAmount() - usedAmount;
			OfficeBudget bud = OfficeBudget.builder()
					.mooeCode(mo.getCode())
					.officeName(mo.getOffices().getName())
					.mooeName(mo.getDescription())
					.mooeBudget(budget)
					.usedBudget(usedAm)
					.remainingBudget(Currency.formatAmount(remaining))
					.build();
			budgetsHis.add(bud);
		}
		
	}
	
	public void selectedMooeChecker() {
		if(mapMooeForOffice!=null) {
			String[] alert = new String[4];
			alert[0]="";
			alert[1]="";
			alert[2]="";
			alert[3]="";
			if(mapMooeForOffice.containsKey(getMooeId())) {
				String[] val = mapMooeForOffice.get(getMooeId()).split(":");
				double budget=Double.valueOf(val[0].replace(",", ""));
				double used=Double.valueOf(val[1].replace(",", ""));
				double remaining = budget - used;
				PrimeFaces pf = PrimeFaces.current();
				if(remaining<=0) {
					alert[0]="Budget: " + val[0];
					alert[1]="Used:" + val[1];
					alert[2]="You don't have remaining budget for this MOOE("+ val[2] +")";
					alert[3]="Remaining: " + Currency.formatAmount(remaining);
					setAlertBudgetMsg(alert);
					pf.executeScript("PF('dlgAlertBudget').show(1000)");;
				}else {
					if(remaining>=1000 && remaining <=2000) {
						alert[0]="Budget: " + val[0];
						alert[1]="Used:" + val[1];
						alert[2]="Please check below remaining budget for this MOOE("+ val[2] +")";
						alert[3]="Remaining: " + Currency.formatAmount(remaining);
						setAlertBudgetMsg(alert);
						pf.executeScript("PF('dlgAlertBudget').show(1000)");
					}else if(remaining>=10000 && remaining <=20000) {
						alert[0]="Budget: " + val[0];
						alert[1]="Used:" + val[1];
						alert[2]="Please check below remaining budget for this MOOE("+ val[2] +")";
						alert[3]="Remaining: " + Currency.formatAmount(remaining);
						setAlertBudgetMsg(alert);
						pf.executeScript("PF('dlgAlertBudget').show(1000)");
					}else {
						pf.executeScript("PF('dlgAlertBudget').hide(1000)");
					}
				}
				setAlertBudgetMsg(alert);
			}
		}
	}
	
	public void search() {
		cheques = new ArrayList<Chequedtls>();
		String sql = "";
		String val = getSearchPayTo();
		if(val!=null && !val.isEmpty()) {
			
			if("Posted".equalsIgnoreCase(val)){
				sql += " AND chkstatus=1";
			}else if("Cancelled".equalsIgnoreCase(val)){
				sql += " AND chkstatus=2";
			}else{
				sql += " AND (pay_to_the_order_of like '%"+val+"%' OR cheque_no like '%"+ val +"%'  )";
			}
			
		}
		
		if(getSearchFundId()>0) {
			sql += " AND accnt_no='"+ getSearchFundId() +"'";
		}
		
		if(isIncludeDate()) {
			String dFrom = DateUtils.convertDate(getDateFrom(), "yyyy-MM-dd");
			String dTo = DateUtils.convertDate(getDateTo(), "yyyy-MM-dd");
			sql += " AND (date_disbursement>='"+ dFrom +"' AND date_disbursement<='"+ dTo +"')";
		}
		
		if(getSearchOfficeId()>0) {
			sql += " AND offid=" + getSearchOfficeId();
		}
		
		sql += " ORDER BY cheque_id DESC";
		Object[] obj = Chequedtls.retrieveData(sql, new String[0]);
		setGrandTotal(Currency.formatAmount((Double)obj[0]));
		setCheques((List<Chequedtls>)obj[1]);
		
	}
	
	public void monthQuery(String name) {
		System.out.println("click month : "+name);
		String dateFrom = "";
		String dateTo = "";
		String year = getYear()+"";
		switch(name) {
			case "JANUARY" : 
				dateFrom = year + "-01-01";
				dateTo = year + "-01-31";
				break;
			case "FEBRUARY" : 
				dateFrom = year + "-02-01";
				dateTo = year + "-02-31";
				break;
			case "MARCH" : 
				dateFrom = year + "-03-01";
				dateTo = year + "-03-31";
				break;
			case "APRIL" : 
				dateFrom = year + "-04-01";
				dateTo = year + "-04-31";
				break;
			case "MAY" : 
				dateFrom = year + "-05-01";
				dateTo = year + "-05-31";
				break;
			case "JUNE" : 
				dateFrom = year + "-06-01";
				dateTo = year + "-06-31";
				break;
			case "JULY" : 
				dateFrom = year + "-07-01";
				dateTo = year + "-07-31";
				break;
			case "AUGUST" : 
				dateFrom = year + "-08-01";
				dateTo = year + "-08-31";
				break;
			case "SEPTEMBER" : 
				dateFrom = year + "-09-01";
				dateTo = year + "-09-31";
				break;
			case "OCTOBER" : 
				dateFrom = year + "-10-01";
				dateTo = year + "-10-31";
				break;
			case "NOVEMBER" : 
				dateFrom = year + "-11-01";
				dateTo = year + "-11-31";
				break;
			case "DECEMBER" :
				dateFrom = year + "-12-01";
				dateTo = year + "-12-31";
				break;
		}
		
		String sql = "";
		if(getSearchFundId()>0) {
			sql += " AND accnt_no='"+ getSearchFundId() +"'";
		}
		sql += " AND (date_disbursement>='"+ dateFrom +"' AND date_disbursement<='"+ dateTo +"') ORDER BY cheque_id DESC ";
		Object[] obj = Chequedtls.retrieveData(sql, new String[0]);
		setGrandTotal(Currency.formatAmount((Double)obj[0]));
		setCheques((List<Chequedtls>)obj[1]);
	}	
	
	
	
	public void save() {
		boolean isOk = true;
		String checkNo = getCheckSeriesNo();
		Chequedtls check = new Chequedtls();
		if(getSelectedData()!=null) {
			check = getSelectedData();
		}else {
			check.setIsActive(1);
			check.setDate_created(DateUtils.convertDate(getDateTime(), "yyyy-MM-dd"));
			
			if(!Chequedtls.checkIfCheckNumberExist(checkNo, getFundId()+"")) {
				isOk = false;
				Application.addMessage(3, "Error", "Check number already exist");
			}
			
		}
		if(getFundId()==0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please select fund");
		}
		if(getOfficeId()==0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please select office");
		}
		if(getMooeId()==0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please select mooe");
		}
		if(getCheckSeriesNo()==null || getCheckSeriesNo().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please input check number");
		}
		if(getPayTo()==null || getPayTo().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please input payment");
		}
		if(getInputAmount()==null || getInputAmount().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please input amount");
		}else {
			String amount = getInputAmount().replace(",", "");
			try {Double.valueOf(amount); setInputAmount(amount);}catch(NumberFormatException nume){
				isOk = false;
				Application.addMessage(3, "Error", "Please input correct amount");
			}
		}
		
		if(getWords()==null || getWords().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please input amount");
		}
		
		
		
		if(isOk) {
			
			HttpSession session = SessionBean.getSession();
			String proc_by = session.getAttribute("username").toString();
			
			check.setDate_disbursement(DateUtils.convertDate(getDateTime(), "yyyy-MM-dd"));

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			check.setDate_edited(dateFormat.format(date));
			
			check.setCheckNo(checkNo);
			check.setFundTypeId(getFundId());
			check.setProcessBy(proc_by);
			check.setSignatory1(getTreasPersonnelId());
			check.setSignatory2(getOfficialId());
			check.setStatus(getStatusId());
			check.setRemarks(getRemarks());
			check.setHasAdvice(0);
			check.setOffice(Offices.builder().id(getOfficeId()).build());
			check.setMoe(Mooe.builder().id(getMooeId()).build());
			check.setAmountInWOrds(getWords());
			check.setAmount(Double.valueOf(getInputAmount()));
			check.setPayToTheOrderOf(getPayTo().toUpperCase());
			
			BankAccounts acc = getFundsData().get(getFundId());
			check.setBankName(acc.getBankAccntBranch());
			check.setAccntName(acc.getBankAccntName());
			check.setAccntNumber(acc.getBankId()+"");
			
			check = Chequedtls.save(check);
			ChequeXML.saveCheckXMLForUpload(check);
			clearAll();
			setSearchPayTo(check.getCheckNo());
			setIncludeDate(false);
			setFundId(check.getFundTypeId());
			cheques.remove(check);
			cheques.add(check);
			reloadList();
			Collections.reverse(cheques);
			Application.addMessage(1, "Success", "Successfully saved.");
			
			if(getSelectedVrTransaction()!=null) {
				VrTransaction vr = getSelectedVrTransaction();
				vr.setCheckno(checkNo);
				vr.save();
				setSelectedVrTransaction(null);
			}
		}
		
	}
	
	public void assignRemarks(){
		if(getStatusId()==2){
			setRemarks("CANCELLED CHECK");
		}else{
			setRemarks("POSTED CHECK");
		}
	}
	
	public void createNew() {
		clearAll();
		fundId=2;
		loadNewCheckNo();
	}
	
	public void deleteRow(Chequedtls chk) {
		chk.delete();
		cheques.remove(chk);
		reloadList();
		Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	private void reloadList() {
		double amount = 0d;
		for(Chequedtls c : cheques) {
			if(c.getStatus()==1) {amount+=c.getAmount();}
		}
		setGrandTotal(Currency.formatAmount(amount));
	}
	
	public void clearAll() {
		setSelectedData(null);
		yearDataClick=DateUtils.getCurrentYear();
		dateTime = DateUtils.getDateToday();
		//setCheckSeriesNo(null);
		loadNewCheckNo();//increment new check number
		setPayTo(null);
		setInputAmount(null);
		setWords(null);
		setRemarks(null);
		//setTreasPersonnelId(0);
		//setOfficeId(0);
		//setTreasPersonnelId(getPrimaryOfficer().getId());
		//setOfficialId(getPrimaryOfficial().getId());
		setRemarks("POSTED CHECK");
		statusList = new ArrayList<>();
		statusList.add(new SelectItem(1,"Posted"));
		statusList.add(new SelectItem(2,"Cancelled"));
		words="0.00 Pesos Only";
		selectedChecks = new ArrayList<Chequedtls>();
	}
	
	public void onFundChange() {
		loadNewCheckNo();
	}
	
	private void loadNewCheckNo(){
		int series = 0;	
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT cheque_no FROM tbl_chequedtls WHERE isactive=1 AND accnt_no='"+ getFundId() +"' ORDER BY cheque_id DESC LIMIT 1");
		rs = ps.executeQuery();
		
		if(rs.next()){
			series= Integer.valueOf(rs.getString("cheque_no"));
		}
		
		rs.close();
		ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		BankChequeDatabaseConnect.close(conn);
		
		
		if(series>0) {	series +=1; }else {series=1;}
			
			String len = series+"";
			int ln = len.length();
			switch(ln){
			case 0 : checkSeriesNo="0000000000"; break;
			case 1 : checkSeriesNo="000000000"+series; break;
			case 2 : checkSeriesNo="00000000"+series; break;
			case 3 : checkSeriesNo="0000000"+series; break;
			case 4 : checkSeriesNo="000000"+series; break;
			case 5 : checkSeriesNo="00000"+series; break;
			case 6 : checkSeriesNo="0000"+series; break;
			case 7 : checkSeriesNo="000"+series; break;
			case 8 : checkSeriesNo="00"+series; break;
			case 9 : checkSeriesNo="0"+series; break;
			case 10 : checkSeriesNo=series+""; break;
			
			}
		
	}
	public List<String> completeBankCheckPayTo(String query){
		List<String> results = new ArrayList<String>();
		
		String sql = "SELECT DISTINCT pay_to_the_order_of FROM tbl_chequedtls WHERE pay_to_the_order_of like '%" + query.replace("--", "") + "%' LIMIT 20";
		results = Chequedtls.retrievePayOrderOf(sql, new String[0]);
		
		return results;
	}
	
	public void generateWords(){
		String result = "";
		try{
			inputAmount = inputAmount==null? "0.00" : inputAmount.replace(",", "");
		NumberToWords numberToWords = new NumberToWords();
		result = numberToWords.changeToWords(inputAmount).toUpperCase();
		
		String[] val = new String[2];
		if(inputAmount!=null){
			val[0] = inputAmount;
			val[1] = result;
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(result.equalsIgnoreCase(" PESOS ONLY.")){
			setWords("");
		}
		setWords(result.trim());
	}
	public List<String> autoPayToName(String query){
		String sql = "SELECT DISTINCT  pay_to_the_order_of from tbl_chequedtls WHERE  pay_to_the_order_of like '%" + query + "%' LIMIT 20";
		return Chequedtls.retrievePayOrderOf(sql, new String[0]);
	}
	
	public void printReportIndividual(Chequedtls chk) {
		String date = chk.getDate_disbursement();
		String tmpDate = date;
		//chk.setDate_disbursement(convertDateToMonthDayYear(date));
		chk.setDate_disbursement(convertDateToMonthDayYearNumeric(date));//new implementation as requested by bank mm-dd-yyyy
		String REPORT_NAME = getPrintName();
		Chequedtls.compileReport(chk,REPORT_NAME);
		chk.setDate_disbursement(tmpDate);
		try{
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		//String REPORT_NAME = ReadConfig.value(AppConf.CHEQUE_REPORT_NAME);
		
		
		File file = new File(REPORT_PATH, REPORT_NAME + ".pdf");
		 FacesContext faces = FacesContext.getCurrentInstance();
		 ExternalContext context = faces.getExternalContext();
		 HttpServletResponse response = (HttpServletResponse)context.getResponse();
		
		 System.out.println("File in printReportIndividual " + file.getName() + " " + file.getPath());
		 
	     BufferedInputStream input = null;
	     BufferedOutputStream output = null;
	     
	     try{
	    	 
	    	 // Open file.
	            input = new BufferedInputStream(new FileInputStream(file),GlobalVar.DEFAULT_BUFFER_SIZE);

	            // Init servlet response.
	            response.reset();
	            response.setBufferSize(GlobalVar.DEFAULT_BUFFER_SIZE);
	            response.setHeader("Content-Type", "application/pdf");
	            response.setHeader("Content-Length", String.valueOf(file.length()));
	            response.setHeader("Content-Disposition", "inline; filename=\"" + REPORT_NAME + ".pdf" + "\"");
	            output = new BufferedOutputStream(response.getOutputStream(), GlobalVar.DEFAULT_BUFFER_SIZE);
	            
	            // Write file contents to response.
	            byte[] buffer = new byte[GlobalVar.DEFAULT_BUFFER_SIZE];
	            int length;
	            while ((length = input.read(buffer)) > 0) {
	                output.write(buffer, 0, length);
	                System.out.println("printReportIndividual read : " + length);
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
	    
	       // com.italia.municipality.lakesebu.controller.Chequedtls.backupPrint();
	        
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
	/**
	 * 
	 * @param dateVal YYYY-MM-DD
	 * @return Month day, Year
	 */
	private String convertDateToMonthDayYear(String dateVal){
		//System.out.println("Date : " + dateVal);
		if(dateVal==null || dateVal.isEmpty()){
			dateVal = DateUtils.getCurrentDateYYYYMMDD();
		}
		int month = Integer.valueOf(dateVal.split("-")[1]); 
		String year = dateVal.split("-")[0];
		int day = Integer.valueOf(dateVal.split("-")[2]);
		
		if(day<10){
			dateVal = getMonthName(month) + " 0"+day + ", " + year;
		}else{
			dateVal = getMonthName(month) + " "+day + ", " + year;
		}
		//System.out.println("return date: "+ dateVal);
		return dateVal;
	}
	/**
	 * 
	 * @param dateVal YYYY-MM-DD
	 * @return Month day, Year
	 */
	private String convertDateToMonthDayYearNumeric(String dateVal){
		//System.out.println("Date : " + dateVal);
		if(dateVal==null || dateVal.isEmpty()){
			dateVal = DateUtils.getCurrentDateYYYYMMDD();
		}
		int month = Integer.valueOf(dateVal.split("-")[1]); 
		String year = dateVal.split("-")[0];
		int day = Integer.valueOf(dateVal.split("-")[2]);
		
		if(day<10){
			dateVal = (month<10? "0"+month: month) + "-0"+day + "-" + year;
		}else{
			dateVal = (month<10? "0"+month: month) + "-"+day + "-" + year;
		}
		//System.out.println("return date: "+ dateVal);
		return dateVal;
	}
	private String getMonthName(int month){
		switch(month){
			case 1: return "January";
			case 2 : return "February";
			case 3 : return "March";
			case 4 : return "April";
			case 5 : return "May";
			case 6 : return "June";
			case 7 : return "July";
			case 8 : return "August";
			case 9 : return "September";
			case 10 : return "October";
			case 11 : return "November";
			case 12 : return "December";
		}
		return "January";
	}
	@SuppressWarnings("unchecked")
	public void budgetMonitoring() {
		
		int year = DateUtils.getCurrentYear();
		String sql = " AND (date_disbursement>='"+ year +"-01-01' AND date_disbursement<='"+ year +"-12-31')";
		if(getOffBudId()!=999) {
			sql += " AND offid="+getOffBudId();
		}
		if(getFundBudgetId()>0) {
			sql += " AND accnt_no='"+ getFundBudgetId() +"'";
		}
		
		Map<Long,Map<Long, List<Chequedtls>>> offices = new  LinkedHashMap<Long, Map<Long, List<Chequedtls>>>();
		Map<Long,List<Chequedtls>> mooes = new LinkedHashMap<Long, List<Chequedtls>>();
		List<Chequedtls> checks = new ArrayList<Chequedtls>();
		Object[] objs = Chequedtls.retrieveData(sql, new String[0]);
		setTotalBudgetRelease(Currency.formatAmount((Double)objs[0]));
		for(Chequedtls chk : (List<Chequedtls>)objs[1]) {
			long officeId = chk.getOffice().getId();
			long moeId = chk.getMoe().getId();
			if(offices!=null && offices.containsKey(officeId)) {
				
				if(offices.get(officeId).containsKey(moeId)) {
					offices.get(officeId).get(moeId).add(chk);
					
				}else {
					checks = new ArrayList<Chequedtls>();
					checks.add(chk);
					offices.get(officeId).put(moeId, checks);
				}
			}else {
				checks = new ArrayList<Chequedtls>();
				mooes = new LinkedHashMap<Long, List<Chequedtls>>();
				checks.add(chk);
				mooes.put(moeId, checks);
				offices.put(officeId, mooes);
				
			}
		}
		
		Map<Long, Offices> ofis = Offices.retrieveAll();
		budgets = new ArrayList<OfficeBudget>();
		Map<Integer, BankAccounts> budget = BankAccounts.budgetAll();
		for(long officeId : offices.keySet()) {
			//System.out.println("officeId:"+officeId);
			OfficeBudget bud1 = new OfficeBudget();
			Map<Long, Mooe> mapMooe = Mooe.retrieveData(officeId,year);
			bud1.setId(officeId);
			bud1.setOfficeName(ofis.get(officeId).getName());
			
			bud1.setMooeCode("");
			bud1.setMooeName("");
			bud1.setMooeBudget("");
			bud1.setUsedBudget("");
			bud1.setRemainingBudget("");
			bud1.setDateCheck("");
			bud1.setFundName("");
			bud1.setCheckNumber("");
			bud1.setPaymentName("");
			bud1.setCheckAmount("");
			
			budgets.add(bud1);
			//List<OfficeBudget> rpts2 = new ArrayList<OfficeBudget>();
			for(long moeId : offices.get(officeId).keySet()) {
				//System.out.println("officeId:"+officeId + "\tmoeId:"+moeId);
				OfficeBudget bud2 = new OfficeBudget();
				Mooe mo = mapMooe.get(moeId);
				if(mo!=null) {
				bud2.setId(officeId);
				bud2.setOfficeName("");
				
				bud2.setMooeCode(mo.getCode());
				bud2.setMooeName(mo.getDescription());
				bud2.setMooeBudget(Currency.formatAmount(mo.getBudgetAmount()));
				
				bud2.setDateCheck("");
				bud2.setFundName("");
				bud2.setCheckNumber("");
				bud2.setPaymentName("");
				bud2.setCheckAmount("");
				double amountUsed=0d;
				List<OfficeBudget> rpts2 = new ArrayList<OfficeBudget>();
				for(Chequedtls c : offices.get(officeId).get(moeId)) {
					OfficeBudget bud3 = new OfficeBudget();
					
					bud3.setId(officeId);
					bud3.setOfficeName("");
					bud3.setMooeCode("");
					bud3.setMooeName("");
					bud3.setMooeBudget("");
					bud3.setUsedBudget("");
					bud3.setRemainingBudget("");
					
					bud3.setDateCheck(c.getDate_disbursement());
					bud3.setFundName(budget.get(c.getFundTypeId()).getBankAccntName());
					bud3.setCheckNumber(c.getCheckNo());
					bud3.setPaymentName(c.getPayToTheOrderOf());
					bud3.setCheckAmount(Currency.formatAmount(c.getAmount()));
					amountUsed+=c.getAmount();
					rpts2.add(bud3);
					//System.out.println("Fund: " + budget.get(c.getFundTypeId()).getBankAccntName());
				}
				
					bud2.setUsedBudget(Currency.formatAmount(amountUsed));
					bud2.setRemainingBudget(Currency.formatAmount(mo.getBudgetAmount()-amountUsed));
					budgets.add(bud2);
					budgets.addAll(rpts2);
				}
				
			}
			//rpts.addAll(rpts2);
		}
		/*
		 * for(OfficeBudget b : rpts) {
		 * System.out.println(b.getId()+"\t"+b.getOfficeName()+"\t"+b.getMooeName()+"\t"
		 * +b.getMooeBudget()+"\t"+b.getUsedBudget()+"\t"+b.getRemainingBudget()+"\t"+b.
		 * getDateCheck()+"\t"+b.getFundName()+"\t"+b.getCheckNumber()+"\t"+b.
		 * getPaymentName()+"\t"+Currency.formatAmount(b.getCheckAmount())); }
		 */
		
	}
	
	public void loadSelectedCheck() {
		if(getSelectedChecks()!=null && getSelectedChecks().size()>0){
			PrimeFaces pf = PrimeFaces.current();		
			pf.executeScript("PF('dlgCheckSelected').show(1000)");
		}else {
			Application.addMessage(3, "Error", "Please select check first.");
		}
	}
	
	public void deleteCheckSelected(Chequedtls chk) {
		getSelectedChecks().remove(chk);	
	}
	
	public void printVoucherCheck() {
		List<Reports> reports = new ArrayList<Reports>();
		double total = 0d;
		int year = Integer.valueOf(getSelectedChecks().get(0).getDate_disbursement().split("-")[0]);
		Map<Long, Mooe> mapMooe = Mooe.retrieveDataAll(year);
		List<Chequedtls> selectedData = getSelectedChecks();
		//Collections.reverse(selectedData);
		
		int size = selectedData.size() - 1;
		
		for(int i = size; i>=0; i--) {
			Chequedtls c = selectedData.get(i);
			Reports rpt = new Reports();
			rpt.setF1(c.getDate_disbursement());
			rpt.setF2(c.getCheckNo());
			//rpt.setF4(tran.getDepartmentCode());
			String stat = c.getRemarks();
			if("RECEIVED".equalsIgnoreCase(stat) || stat.isEmpty()) {
				rpt.setF3("Posted Check");
			}else {
				rpt.setF3(c.getRemarks());
			}
			try{rpt.setF4(getOfficeData().get(c.getOffice().getId()).getName());}catch(NullPointerException e) {}
			try{rpt.setF5(c.getPayToTheOrderOf());}catch(NullPointerException e) {}
			try{rpt.setF6(mapMooe.get(c.getMoe().getId()).getDescription());}catch(NullPointerException e) {}
			try{rpt.setF7(Currency.formatAmount(c.getAmount()));}catch(NullPointerException e) {}
			reports.add(rpt);
			total += c.getAmount();
		}
		
		//compiling report
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME =ReadConfig.value(AppConf.VOUCHER);
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
		
  		param.put("PARAM_REPORT_TITLE","REPORT OF CHECK ISSUED");
  		
  		param.put("PARAM_PRINTED_DATE","Printed: "+DateUtils.getCurrentDateMMDDYYYYTIME());
  		param.put("PARAM_RANGE_DATE",DateUtils.convertDate(getDateFrom(), "yyyy-MM-dd")+ " to " + DateUtils.convertDate(getDateTo(), "yyyy-MM-dd"));
  		
  		BankAccounts fund = new BankAccounts();
		if(getFundsData()!=null) {
			fund = getFundsData().get(getSearchFundId());
			param.put("PARAM_ACCOUNT_NAME","Bank Name/Account No. "+fund.getBankAccntNo() + "-" + fund.getBankAccntBranch());
		}else {
			param.put("PARAM_ACCOUNT_NAME","All Accounts");
		}
  		
		
  		param.put("PARAM_SUB_TOTAL",Currency.formatAmount(total));
  		
  		param.put("PARAM_RECEIVEDBY",Login.getUserLogin().getUserDtls().getFirstname().toUpperCase() + " " + Login.getUserLogin().getUserDtls().getLastname().toUpperCase());
  		
  		
  		DocumentFormatter doc = new DocumentFormatter();
		param.put("PARAM_TREASURER", doc.getTagName("treasurer-name"));
		param.put("PARAM_TREASURER_POS", doc.getTagName("treasurer-position"));
  		
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
	
	public void printVoucherCheckRecord() {
		List<Reports> reports = new ArrayList<Reports>();
		double total = 0d;
		int year = Integer.valueOf(getSelectedChecks().get(0).getDate_disbursement().split("-")[0]);
		Map<Long, Mooe> mapMooe = Mooe.retrieveDataAll(year);
		List<Chequedtls> selectedData = getSelectedChecks();
		//Collections.reverse(selectedData);
		
		int size = selectedData.size() - 1;
		
		for(int i = size; i>=0; i--) {
			Chequedtls c = selectedData.get(i);
			Reports rpt = new Reports();
			rpt.setF1(c.getDate_disbursement());
			rpt.setF2(c.getCheckNo());
			//rpt.setF4(tran.getDepartmentCode());
			String stat = c.getRemarks();
			if("RECEIVED".equalsIgnoreCase(stat) || stat.isEmpty()) {
				rpt.setF3("Posted Check");
			}else {
				rpt.setF3(c.getRemarks());
			}
			try{rpt.setF4(getOfficeData().get(c.getOffice().getId()).getName());}catch(NullPointerException e) {}
			try{rpt.setF5(c.getPayToTheOrderOf());}catch(NullPointerException e) {}
			try{rpt.setF6(mapMooe.get(c.getMoe().getId()).getDescription());}catch(NullPointerException e) {}
			try{rpt.setF7(Currency.formatAmount(c.getAmount()));}catch(NullPointerException e) {}
			reports.add(rpt);
			total += c.getAmount();
		}
		
		//compiling report
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = GlobalVar.VOUCHER_CHECK_RECORD;
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
		
  		param.put("PARAM_REPORT_TITLE","REPORT OF CHECK ISSUED");
  		
  		param.put("PARAM_PRINTED_DATE","Printed: "+DateUtils.getCurrentDateMMDDYYYYTIME());
  		param.put("PARAM_RANGE_DATE",DateUtils.convertDate(getDateFrom(), "yyyy-MM-dd")+ " to " + DateUtils.convertDate(getDateTo(), "yyyy-MM-dd"));
  		
  		BankAccounts fund = new BankAccounts();
		if(getFundsData()!=null) {
			fund = getFundsData().get(getSearchFundId());
			param.put("PARAM_ACCOUNT_NAME","Bank Name/Account No. "+fund.getBankAccntNo() + "-" + fund.getBankAccntBranch());
		}else {
			param.put("PARAM_ACCOUNT_NAME","All Accounts");
		}
  		
		
  		param.put("PARAM_SUB_TOTAL",Currency.formatAmount(total));
  		
  		param.put("PARAM_RECEIVEDBY",Login.getUserLogin().getUserDtls().getFirstname().toUpperCase() + " " + Login.getUserLogin().getUserDtls().getLastname().toUpperCase());
  		
  		DocumentFormatter doc = new DocumentFormatter();
		param.put("PARAM_TREASURER", doc.getTagName("treasurer-name"));
		param.put("PARAM_TREASURER_POS", doc.getTagName("treasurer-position"));
  		
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
	
	public void refresh() {
		mooes = new ArrayList<>();
		officeId=0;//office
		mooes.add(new SelectItem(0,"Please Select"));
		offices = new ArrayList<Offices>();
		offBuds = new ArrayList<>();
		offBuds.add(new SelectItem(999, "All"));
		officeData = new LinkedHashMap<Long, Offices>();
		for(Offices d : Offices.retrieve(" ORDER BY name", new String[0])) {
			offices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			offBuds.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			officeData.put(d.getId(), d);
		}
	}
	public void fundSearch() {
		PrimeFaces pf = PrimeFaces.current();
		
		fundData = new ArrayList<Reports>();
		Map<String, Double> data = Chequedtls.retrieveFund(yearBudId);
		double total = 0d;
		for(String s : data.keySet()) {
			double amount = Double.valueOf(data.get(s));
			Reports r = Reports.builder()
					.f1(s)
					.f2(Currency.formatAmount(amount))
					.build();
			fundData.add(r);
			total += amount;
		}
		Reports r = Reports.builder().f1("Total").f2(Currency.formatAmount(total)).build();
		fundData.add(r);
		createBarModel();
		pf.executeScript("PF('dlgFund').show(1000)");
	}
	
	public void createBarModel() {
		
		if(fundData==null || fundData.size()==0) {
			fundData = new ArrayList<Reports>();
			Reports r = Reports.builder().f1("Fund").f2("1000").build();
			fundData.add(r);
		}
		
        barModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet barDataSet = new BarChartDataSet();
        
        barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
        barDataSet.setBorderColor("rgb(255, 99, 132)");
        barDataSet.setBorderWidth(1);
        
        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        String amountTotal = "";
        for(Reports r : fundData) {
        	if(!"Total".equalsIgnoreCase(r.getF1())) {
	        	labels.add(r.getF1() + "("+ r.getF2() +")");
	        	values.add(Double.valueOf(r.getF2().replace(",", "")));
        	}else {
        		amountTotal = r.getF2();
        	}
        }
        barDataSet.setLabel("Graph Fund Chart("+ amountTotal +")");
        barDataSet.setData(values);
        data.addChartDataSet(barDataSet);
        data.setLabels(labels);
        barModel.setData(data);

        //Options
        BarChartOptions options = new BarChartOptions();
        //options.setMaintainAspectRatio(false);
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Fund Chart for year ("+ yearBudId +")");
        options.setTitle(title);

        barModel.setOptions(options);
}
	
	public void fundOfficeBudgetUsed() {
		mapPerOfficeData = new LinkedHashMap<Double, Reports>();
		PrimeFaces pf = PrimeFaces.current();
		
		fundOfficeData = new ArrayList<Reports>();
		Map<Long, Double> data = Chequedtls.retrievePerOffice(yearOfficeBudId, fundBudgetPerOfficeId, perFundOfficeId);
		
		Map<Long, Mooe> mapMooe = new LinkedHashMap<>();
		if(perFundOfficeId>0) {//this is for MOOE
			List<Mooe> moes = Mooe.retrieve(" AND ofs.offid="+perFundOfficeId + " AND moe.yearbudget=" + yearOfficeBudId, new String[0]);
			for(Mooe mo : moes) {
				mapMooe.put(mo.getId(), mo);
			}
		}
		
		double total = 0d;
		for(long s : data.keySet()) {
			if(perFundOfficeId==0) {//summary per office
				Offices off = officeData.get(s);
				double amount = Double.valueOf(data.get(s));
				Reports r = Reports.builder().f1(off.getName()).f2(Currency.formatAmount(amount)).build();
				mapPerOfficeData.put(amount, r);
				fundOfficeData.add(r);
				total += amount;
			}else {// office mooe summary
				Mooe mooe = mapMooe.get(s);
				double amount = Double.valueOf(data.get(s));
				Reports r = Reports.builder().f1(mooe.getDescription()).f2(Currency.formatAmount(amount)).f3(Currency.formatAmount(mooe.getBudgetAmount())).build();
				mapPerOfficeData.put(amount, r);
				fundOfficeData.add(r);
				total += amount;
			}
		}
		Reports r = Reports.builder().f1("Total").f2(Currency.formatAmount(total)).build();
		fundOfficeData.add(r);
		createBarModelPerOffice();
		pf.executeScript("PF('dlgMooeOffice').show(1000)");
	}
	
	public void createBarModelPerOffice() {
		Map<Double, Reports> rptData = new TreeMap<Double, Reports>();
		if(fundOfficeData==null || fundOfficeData.size()==0) {
			fundOfficeData = new ArrayList<Reports>();
			Reports r = Reports.builder().f1("Fund").f2("1000").build();
			fundOfficeData.add(r);
			//rptData = new TreeMap<Double, Reports>(mapPerOfficeData);
		}else {
			//has value
			rptData = new TreeMap<Double, Reports>(mapPerOfficeData);
		}
		
        barModel2 = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet barDataSet = new BarChartDataSet();
        
        
        
        barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
        barDataSet.setBorderColor("rgb(255, 99, 132)");
        barDataSet.setBorderWidth(1);
        
        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        
        BarChartDataSet barDataSet2 = new BarChartDataSet();
        barDataSet2.setBackgroundColor("rgba(100, 99, 132, 0.2)");
        barDataSet2.setBorderColor("rgb(100, 99, 132)");
        barDataSet2.setBorderWidth(1);
        
        List<String> labels2 = new ArrayList<>();
        List<Number> values2 = new ArrayList<>();
        
        List<Reports> rpts = new ArrayList<Reports>();
        for(Reports r : rptData.values()) {
        	rpts.add(r);
        }
        Collections.reverse(rpts);
        int count=1;
        for(Reports r : rpts) {
        	if(count<=20) {
		        //labels.add(r.getF1() + "("+ r.getF2() +")");
        		labels.add(r.getF1());
		        values.add(Double.valueOf(r.getF2().replace(",", "")));
		        
		        if(perFundOfficeId>0) {
			        labels2.add(r.getF1());
			        values2.add(Double.valueOf(r.getF3().replace(",", "")));
		        }
        	}
	        count++;
        }
        
                
       
        
        if(perFundOfficeId==0) {
        	barDataSet.setLabel("Graph Fund Chart");
            barDataSet.setData(values);
            data.addChartDataSet(barDataSet);
            data.setLabels(labels);
        }else {
        	 
        	 barDataSet2.setLabel("Budget");
             barDataSet2.setData(values2);
             data.addChartDataSet(barDataSet2);
             data.setLabels(labels2);
        	
             barDataSet.setLabel("Expense");
             barDataSet.setData(values);
             data.addChartDataSet(barDataSet);
             data.setLabels(labels);
             
             
        }
        
        
        barModel2.setData(data);

        //Options
        BarChartOptions options = new BarChartOptions();
        //options.setMaintainAspectRatio(false);
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Fund Chart Used for year ("+ yearOfficeBudId +") top 20 only");
        options.setTitle(title);

        barModel2.setOptions(options);
}
	
	
	
	public void loadBudget() {
		buds = BudgetMonitoring.retrieve(getMonYearId(), getMonMonthId(), getMonAccId());
		Collections.reverse(buds);
		}
	public void loadMonBudgetDefault() {
		
		setMonYearId(DateUtils.getCurrentYear());
		setMonMonthId(DateUtils.getCurrentMonth());
		
		monMonths = new ArrayList<>();
		for(int month=1; month<=12; month++) {
			monMonths.add(new SelectItem(month, DateUtils.getMonthName(month)));		
		}
		monYears = new ArrayList<>();
		for(int year=2024; year<=DateUtils.getCurrentYear(); year++) {
			monYears.add(new SelectItem(year, year+""));
		}
		
		monAccounts = new ArrayList<>();
		for(BankAccounts acc : BankAccounts.retrieveAll()) {
			monAccounts.add(new SelectItem(acc.getBankId(), acc.getBankAccntName() + "-" + acc.getBankAccntBranch()));
		}
		
		types = new ArrayList<>();
		type = 0;
		for(BudgetType t : BudgetType.values()) {
			types.add(new SelectItem(t.getId(), t.getName()));
		}
	}
	public void saveBudMonitoring() {
		boolean isOk = true;
		BudgetMonitoring mon = new BudgetMonitoring();
		if(getBudgetData()!=null) {
			mon = getBudgetData();
		}
		
		if(getMonAmount()==0) {
			Application.addMessage(3, "Error", "Provide amount");
			isOk = false;
		}
		
		if(getCycle()==0) {
			Application.addMessage(3, "Error", "Provide cycle");
			isOk = false;
		}
		
		if(isOk) {
			String date = getMonYearId() +"-" + (getMonMonthId()<10? "0"+getMonMonthId(): getMonMonthId());
			date += "-"+ (getCycle()<10? "0"+getCycle() : getCycle());
			mon.setAccounts(BankAccounts.builder().bankId(getMonAccId()).build());
			mon.setDateTrans(date);
			mon.setIsActive(1);
			mon.setAmount(getMonAmount());
			mon.setType(getType());
			mon.setCycle(getCycle());
			mon.setRemarks(getBudgetRemarks());
			mon.save();
			clearBudMonitoring();
			loadBudget();
			Application.addMessage(1, "Success", "Successfully saved");
		}
	}
	public void editMonBudget(BudgetMonitoring bud) {
		budgetData = bud;
		String[] date = bud.getDateTrans().split("-");
		int year = Integer.valueOf(date[0]);
		int month = Integer.valueOf(date[1]);
		int cycle = Integer.valueOf(date[2]);
		
		setMonYearId(year);
		setMonMonthId(month);
		setCycle(cycle);
		
		setMonAccId(bud.getAccounts().getBankId());
		setMonAmount(bud.getAmount());
		setType(bud.getType());
		setCycle(bud.getCycle());
		setBudgetRemarks(bud.getRemarks());
	}
	
	public void clearBudMonitoring() {
		setBudgetData(null);
		//setMonAccId(bud.getAccounts().getBankId());
		setMonAmount(0);
		setType(0);
		setCycle(DateUtils.getCurrentDay());
		setBudgetRemarks(null);
	}
	
	public void defaultVr() {
		vrData = VrTransaction.builder()
				.tmpDateTrans(new Date())
				.offices(Offices.builder().id(2).build())
				.mooe(Mooe.builder().id(16).build())
				.accounts(BankAccounts.builder().bankId(2).build())
				.status(1)
				.isActive(1)
				.build();
		
		loadVrMooe();
	
	}
	
	public void deleteMonBudget(BudgetMonitoring bud) {
		bud.delete();
		loadBudget();
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void saveVr() {
		VrTransaction tr = getVrData();
		tr.setDateTrans(DateUtils.convertDate(tr.getTmpDateTrans() , "yyyy-MM-dd"));
		tr.setIsActive(1);
		tr.setDeliveredDateTime(DateUtils.getCurrentDateMMDDYYYYTIMEPlain());
		if(tr.getCheckno().isEmpty()) {
			tr.setCheckno(null);
		}
		
		tr.save();
		defaultVr();
		loadLogVR();
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void loadLogVR() {
		String sql = "";
		vrData.setOffices(Offices.builder().id(2).build());
		if(getSearchVr()!=null && !getSearchVr().isEmpty()) {
			sql += " AND vr.payor LIKE '%"+ getSearchVr() +"%'";
			sql += " ORDER By vr.daterf DESC";
		}else {
			sql = " ORDER By vr.daterf DESC LIMIT 10";
		}
		
		
		
		vrTrans = VrTransaction.retrieve(sql, new String[0]);
	}
	public void clickVR(VrTransaction vr) {
			vr.setTmpDateTrans(DateUtils.convertDateString(vr.getDateTrans(),"yyyy-MM-dd"));
			setVrData(vr);
			loadVrMooe();
	}
	public void deleteVR(VrTransaction vr) {
		vr.delete();
		loadLogVR();
		Application.addMessage(1, "Success", "Successfully saved");	
	}
	
	public void sendCustomerSMS(VrTransaction rs) {
		if(rs!=null && rs.getId()>0 && !rs.getContactNo().isEmpty() && !rs.getCheckno().isEmpty() && VrTransStatus.RELEASING.getId()==rs.getStatus()) {
			if(CheckInternetConnection.isInternetPresent("https://semaphore.co/")) {
				System.out.println("The site is accessible...");
				String contactNo = rs.getContactNo();
				contactNo = contactNo.replace("+63", "");
				int len = contactNo.length();
				if(len==11) {
					String api_key = Words.getTagName("sms-api-key");
					String post_url_msg = Words.getTagName("sms-post-url-msg");
					String user_agent = Words.getTagName("sms-user-agent");
					String msg = GlobalVar.smsCheckMSG.replace("<recepient>", rs.getDeliveredBy());
					msg = GlobalVar.smsCheckMSG.replace("<checkno>", rs.getCheckno());
					
					System.out.println("sending info "+ msg + " number: " + contactNo);
					String[] response = SendSMS.sendSMS(api_key, contactNo, msg,post_url_msg, user_agent);
					//String[] response = {"SUCCESS"};
					//String[] response2 = SendSMS.sendSMS(api_key, contactNo, msg,post_url_msg, user_agent);
					//String[] response = {"SUCCESS"};
					if("SUCCESS".equalsIgnoreCase(response[0])) {
						Application.addMessage(1, "Success", "You have successfully sent the message to " + rs.getPayor() );
						rs.setPickUpBy("SENT");
						rs.save();
						loadLogVR();
						}else {
						Application.addMessage(3, "Error", "Sending sms was not successfully. Please try again.");
					}
				}else {
					Application.addMessage(3, "Error", "Please check the contact number of the client. It seems that is not correct.");
				}
			}else {
				System.out.println("Not accessible at this moment....");
				Application.addMessage(3, "Error", "Provider is not available or the internet is not present at this moment. Please re-send the notification to the user once online.");
			}
		}else {
			Application.addMessage(3, "Error", "Contact no is not available or check status is not yet change to for releasing");
		}
	}
	
	public void createCheck(VrTransaction rs) {
		setSelectedVrTransaction(rs);
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgVR').hide(1000)");
		;
	
		setFundId(rs.getAccounts().getBankId());
		setOfficeId(rs.getOffices().getId());
		//System.out.println("Office Id: " + chk.getOffice().getId());
		if(rs.getMooe()!=null) {
			setMooeId(rs.getMooe().getId());
		}
		//setYearDataClick(Integer.valueOf(chk.getDate_disbursement().split("-")[0]));
		loadMooeForOffice();
		//setCheckSeriesNo(chk.getCheckNo());
		setPayTo(rs.getPayor());
		//setNatureOfPayment(getMooeMap().get(chk.getMoe().getId()).getDescription());
		setInputAmount(Currency.formatAmount(rs.getAmount()));
		//setStatusId(chk.getStatus());
		setRemarks(rs.getRemarks());
		//setTreasPersonnelId(chk.getSignatory1());
		//setOfficialId(chk.getSignatory2());
		generateWords();//generate words
		
	}
	
	@SuppressWarnings("unchecked")
	public void loadVrMooe() {
		vrMooes = new ArrayList<>();
		for(Mooe mooe : Mooe.retrieveData(vrData.getOffices().getId(), DateUtils.getCurrentYear()).values()) {
			vrMooes.add(new SelectItem(mooe.getId(), mooe.getCode() + " " + mooe.getDescription()));
		}
	}
	
}
