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

import org.primefaces.PrimeFaces;

import com.google.gson.internal.LinkedTreeMap;
import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.ChequeXML;
import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.NumberToWords;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.Signatories;
import com.italia.municipality.lakesebu.controller.Voucher;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.TransactionType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.OfficeBudget;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.xml.BookCheck;
import com.italia.municipality.lakesebu.xml.CheckXML;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
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
	
	public void clickItem(Chequedtls chk) {
		setSelectedData(chk);
		setDateTime(DateUtils.convertDateString(chk.getDate_disbursement(), "yyyy-MM-dd"));
		setFundId(chk.getFundTypeId());
		setOfficeId(chk.getOffice().getId());
		System.out.println("Office Id: " + chk.getOffice().getId());
		setMooeId(chk.getMoe().getId());
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
	
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		System.out.println("init.......");
		setIncludeDate(true);//default set to true
		Date dateSource = DateUtils.getDateToday();
		selectedChecks = new ArrayList<Chequedtls>();
		dateTime = dateSource;
		dateFrom = dateSource;
		dateTo = dateSource;
		offices = new ArrayList<>();
		searchFunds = new ArrayList<>();
		funds = new ArrayList<>();
		mooes = new ArrayList<>();
		officeId=0;//office
		fundId=2;//General Fund
		fundBudgetId=2;//General Fund
		fundBudgets = new ArrayList<>();
		fundBudgets.add(new SelectItem(0, "All Fund"));
		loadNewCheckNo();//load new check no
		mooes.add(new SelectItem(0,"Please Select"));
		fundsData = new LinkedHashMap<Integer,BankAccounts>();
		for(BankAccounts b : BankAccounts.retrieveAll()) {
			funds.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			searchFunds.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			fundBudgets.add(new SelectItem(b.getBankId(), b.getBankAccntName() + "-" + b.getBankAccntNo()));
			fundsData.put(b.getBankId(), b);
		}
		offBuds = new ArrayList<>();
		offBuds.add(new SelectItem(999, "All"));
		officeData = new LinkedHashMap<Long, Offices>();
		for(Offices d : Offices.retrieve(" ORDER BY name", new String[0])) {
			offices.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			offBuds.add(new SelectItem(d.getId(), d.getCode() + "-" + d.getName()));
			officeData.put(d.getId(), d);
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
		years = new ArrayList<>();
		for(int y=2016; y<=year; y++) {
			years.add(new SelectItem(y, y+""));
		}
		search();
	}
	
	public void loadMooeForOffice() {
		mooes = new ArrayList<>();
		List<Mooe> moes = Mooe.retrieve(" AND ofs.offid="+getOfficeId(), new String[0]);
		for(Mooe mo : moes) {
			double amount = Chequedtls.mooeUsed(mo.getOffices().getId(), mo.getId(), DateUtils.getCurrentYear());
			mooes.add(new SelectItem(mo.getId(),mo.getDescription() + " (BUDGET:"+ Currency.formatAmount(mo.getBudgetAmount()) +" REM:"+Currency.formatAmount(amount)+")"));
			//mooeMap.put(mo.getId(), mo);
		}
		if(moes!=null && moes.size()==0) {mooes.add(new SelectItem(0,"No Assigned MOOE (0.00)"));}
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
		String sql = " AND (date_disbursement>='"+ dateFrom +"' AND date_disbursement<='"+ dateTo +"')";
		Object[] obj = Chequedtls.retrieveData(sql, new String[0]);
		setGrandTotal(Currency.formatAmount((Double)obj[0]));
		setCheques((List<Chequedtls>)obj[1]);
	}	
	
	
	
	public void save() {
		boolean isOk = true;
		Chequedtls check = new Chequedtls();
		if(getSelectedData()!=null) {
			check = getSelectedData();
		}else {
			check.setIsActive(1);
			check.setDate_created(DateUtils.convertDate(getDateTime(), "yyyy-MM-dd"));
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
			
			check.setCheckNo(getCheckSeriesNo());
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
		chk.setDate_disbursement(convertDateToMonthDayYear(date));
		
		Chequedtls.compileReport(chk);
		chk.setDate_disbursement(tmpDate);
		try{
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = ReadConfig.value(AppConf.CHEQUE_REPORT_NAME);
		
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
		for(Chequedtls c : getSelectedChecks()) {
			
			Reports rpt = new Reports();
			rpt.setF1(c.getDate_disbursement());
			rpt.setF2(c.getCheckNo());
			//rpt.setF4(tran.getDepartmentCode());
			rpt.setF4(getOfficeData().get(c.getOffice().getId()).getName());
			rpt.setF5(c.getPayToTheOrderOf());
			rpt.setF6(mapMooe.get(c.getMoe().getId()).getDescription());
			rpt.setF7(Currency.formatAmount(c.getAmount()));
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
		
  		param.put("PARAM_REPORT_TITLE","CHECK ISSUED REPORT");
  		
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
}
