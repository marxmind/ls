package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.primefaces.PrimeFaces;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.optionconfig.title.Title;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Budget;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.IBudget;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.NumberToWords;
import com.italia.municipality.lakesebu.controller.PrintFormat;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.ReportFields;
import com.italia.municipality.lakesebu.controller.Signatories;
import com.italia.municipality.lakesebu.controller.Voucher;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.BudgetType;
import com.italia.municipality.lakesebu.enm.GraphColor;
import com.italia.municipality.lakesebu.enm.GraphColorWithBorder;
import com.italia.municipality.lakesebu.enm.TransactionType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.xml.BookCheck;
import com.italia.municipality.lakesebu.xml.CheckXML;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * Please check the new implementation in chk.xhtml and ChkBean.java
 *
 */
@Deprecated
@Named
@ViewScoped
public class CheckBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6548798090879675641L;
	private String bankCheckName;
	private String bankCheckNo;
	private String bankCheckDate;
	private String bankCheckAccntName;
	private String bankCheckAmount;
	private String bankCheckPayTo;
	private String bankCheckInWords;
	private String bankCheckAccountNumber;
	private String inputAmount;
	private String words;
	private Date dateTime;
	private final Path PATH_APP = Paths.get("");
	private String sig1Label;
	private String sig2Label;
	private String accountLabel;
	private List accountList;
	private List accountNameList;
	private Map<Integer,BankAccounts> accounts;
	private String userNotification="";
	//private List<Chequedtls> chequedtls;
	private String printFormat;
	private List formatPrint;
	private Map<Integer,PrintFormat> mapprint;
	private List<com.italia.municipality.lakesebu.controller.Chequedtls> cheques;
	private String searchPayTo;
	private List<Date> rangeDate;
	private String searchBankAccountId;
	private com.italia.municipality.lakesebu.controller.Chequedtls chequedtlsData;
	private String grandTotal;
	private String keyPress;
	private byte[] reportBytesCheque;
	private List statusList;
	private int statusId;
	private String remarks;
	private boolean enableRemarks;
	
	private String natureOfPayment;
	private int departmentId;
	private List department;
	private Map<Integer, Department> departmentData;
	
	private String buttonSizeName="Expand Table";
	
	@Setter @Getter private BarChartModel barModel;
	@Setter @Getter private List years;
	@Setter @Getter private List selectedYear;
	
	
	public int getDepartmentId() {
		if(departmentId==0){
			departmentId = 49;
		}
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}
	
	public List getDepartment() {
		departmentData = new HashMap<Integer, Department>();
		department = new ArrayList<>();
		for(Department dep : Department.retrieve("SELECT * FROM department order by departmentname", new String[0])){
			department.add(new SelectItem(dep.getDepid(), dep.getDepartmentName()));
			departmentData.put(dep.getDepid(), dep);
		}
		return department;
	}

	public void setDepartment(List department) {
		this.department = department;
	}

	public Map<Integer, Department> getDepartmentData() {
		return departmentData;
	}

	public void setDepartmentData(Map<Integer, Department> departmentData) {
		this.departmentData = departmentData;
	}
	
	private boolean checkLimit(){
		
		double amountToDispense = Double.valueOf(getInputAmount().replace(",", ""));
		
		if(amountToDispense>0){
		IBudget bud = Budget.retrieveBudget("SELECT * FROM budget WHERE bank_id='" + getBankCheckAccountNumber()+"'", new String[0]).get(0);
		
		if(bud.getIsActivated()){
		
		System.out.println("Check if activated " + bud.getIsActivated());
		System.out.println("Amount limit " + bud.getLimitAmount());
		System.out.println("Budget Amount " + bud.getAmount());
		com.italia.municipality.lakesebu.controller.Chequedtls chk = null;
		try{chk = com.italia.municipality.lakesebu.controller.Chequedtls.retrieve("SELECT * FROM  tbl_chequedtls WHERE isactive=1 AND chkstatus=1 AND cheque_no='"+getBankCheckNo().trim()+"'", new String[0]).get(0);}catch(IndexOutOfBoundsException e){} 
		String dateFrom = DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		String dateTo = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		if(chk==null){
		
			int month = DateUtils.getCurrentMonth(); 
			int year = DateUtils.getCurrentYear();
			
			int dateToday = Integer.valueOf(DateUtils.getCurrentDateYYYYMMDD().split("-")[2]);
			if(bud.getCycleDate()>dateToday){
				month -=1;
			}
			
			dateFrom = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
			
			if(month==12){
				year +=1;
				month=1; 
				
				dateTo = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
				
			}else{
				month +=1; 
				int cycleDate = bud.getCycleDate() - 1;
				if(cycleDate==0){
					cycleDate=31;
					month -=1;
				}
				dateTo = year + "-" + (month<10? "0"+month : month) + "-" + (cycleDate<10? "0" + cycleDate : cycleDate);
			}		
			
			
		String sql = "SELECT sum(cheque_amount) as total FROM tbl_chequedtls WHERE  accnt_no=? AND (date_disbursement>=? and date_disbursement<=?) and isactive=1 and chkstatus=1";
		String[] params = new String[3];
		params[0] = bud.getAccounts().getBankId()+"";
		params[1] = dateFrom;//DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);//DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		params[2] = dateTo;//DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		BigDecimal totalDispenseAmount = com.italia.municipality.lakesebu.controller.Chequedtls.sum(sql, params, "total");
		if(totalDispenseAmount==null) {totalDispenseAmount = new BigDecimal("0.00");}
		double remainingAmount = bud.getAmount().doubleValue() - totalDispenseAmount.doubleValue();
		System.out.println("total dispense: " + totalDispenseAmount.doubleValue());
		System.out.println("Remaining amount: " + remainingAmount);
		
		if(remainingAmount>=bud.getLimitAmount()){
			remainingAmount -= bud.getLimitAmount();
			if(remainingAmount>=amountToDispense){
				return true;
			}
		}
		
		}else{
			return true;
		}
		
		}else{
			return true;
		}
		
		}else{
			return true;
		}
		
		return false;
	}
	
	public void assignRemarks(){
		if(getStatusId()==2){
			setEnableRemarks(false);
			setRemarks("CANCELLED CHECK");
		}else{
			setEnableRemarks(true);
			setRemarks("RECEIVED");
		}
	}
	
	public String graphChart(){
		return "cheque.xhtml";
	}
	
	public List<String> autoPayToName(String query){
		String sql = "SELECT DISTINCT  pay_to_the_order_of from tbl_chequedtls WHERE  pay_to_the_order_of like '%" + query + "%' LIMIT 20";
		String[] params = new String[0];
		List<String> result = new ArrayList<>();
		result = com.italia.municipality.lakesebu.controller.Chequedtls.retrievePayOrderOf(sql, params);
		return result;
	}
	
	public void printReportAll(){
		try{
			
			String sql = "SELECT * FROM tbl_chequedtls WHERE isactive=1 ";
			String[] params = new String[0];
			
			
			int cnt = 0;
			
			if(getSearchBankAccountId()!=null && !getSearchBankAccountId().isEmpty()){
				cnt++; 
			}
			
			if(getSearchPayTo()!=null && !getSearchPayTo().isEmpty()){
				int checkno = 0;
				
				try{
					//checker only
					checkno = Integer.valueOf(getSearchPayTo());
					cnt++;
				}catch(NumberFormatException e){
					
				}
			}
			
			if(getRangeDate()!=null && getRangeDate().size()>1) {
				cnt +=2;
			}else {
				cnt++;
			}
			
			params = new String[cnt];
			int x = 0;
			if(getSearchBankAccountId()!=null && !getSearchBankAccountId().isEmpty()){
				sql += " AND accnt_no=?";
				params[x] = getSearchBankAccountId();
				x++;
			}
			
			if(getSearchPayTo()!=null && !getSearchPayTo().isEmpty()){
				int checkno = 0;
				
				try{
					//checker only
					checkno = Integer.valueOf(getSearchPayTo());
					
					sql += " AND cheque_no=?";
					params[x] = getSearchPayTo().trim();
					x++;
				}catch(NumberFormatException e){
					sql += " AND pay_to_the_order_of like '%"+getSearchPayTo().replace("--", "")+"%'";
				}
			}
			
			if(getRangeDate()!=null && getRangeDate().size()>1) {
				sql += " AND (date_disbursement>=? AND date_disbursement<=? )";
				params[x] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");//convertDateToMonthDayYear(getDateFrom());
				x++;
				params[x] = DateUtils.convertDate(getRangeDate().get(1),"yyyy-MM-dd");//convertDateToMonthDayYear(getDateTo());
			}else {
				sql += " AND date_disbursement=?";
				params[x] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");// convertDateToMonthDayYear(getDateTo());
				x++;
			}
			
			
			if(cnt==0){
				params = new String[1];
				sql += " AND date_disbursement=? ORDER BY cheque_no LIMIT 100";
				params[0] =  DateUtils.getCurrentDateYYYYMMDD();//convertDateToMonthDayYear(DateUtils.getCurrentDateMMMMDDYYYY());
			}else{
				sql += " ORDER BY cheque_no";
			}
			
			
			
			
		List<ReportFields> 	reportFields = new ArrayList<>();
		int i=1;
		double amount = 0d;
		for(com.italia.municipality.lakesebu.controller.Chequedtls chk : com.italia.municipality.lakesebu.controller.Chequedtls.retrieve(sql, params)){
			ReportFields rpt = new ReportFields();
			
			rpt.setId(i++);
			
			if(chk.getStatus()==1){
				amount += chk.getAmount();
			}
			
			rpt.setAccntNumber(bankAccountNum(chk.getAccntNumber()));
			rpt.setCheckNo(chk.getCheckNo());
			rpt.setDate_disbursement(chk.getDate_disbursement());
			rpt.setBankName(chk.getBankName());
			rpt.setAccntName(chk.getAccntName());
			rpt.setAmount(Currency.formatAmount(chk.getAmount()));
			rpt.setPayToTheOrderOf(chk.getPayToTheOrderOf());
			rpt.setAmountInWOrds(chk.getAmountInWOrds());
			rpt.setSignatory1(chk.getSignatoryName1());
			rpt.setSignatory2(chk.getSignatoryName2());
			rpt.setProcessBy(chk.getProcessBy());
			rpt.setDate_edited(chk.getDate_edited());
			rpt.setDate_created(chk.getDate_created());
			rpt.setStatus(chk.getStatus()==1? "RECEIVED" : "CANCELLED");
			rpt.setRemarks(chk.getRemarks());
			reportFields.add(rpt);
		}
		setGrandTotal(formatAmount(amount+""));	
		
		compileReportList(reportFields);
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = ReadConfig.value(AppConf.CHEQUE_REPORT_NAME_DISPENSE);
		
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
	        
		}catch(Exception ioe){
			ioe.printStackTrace();
		}
	}
	
	private void backupPrintDispense(){
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = AppConf.CHEQUE_REPORT_NAME_DISPENSE.getValue();
		String REPORT_PATH_FILE = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() +  AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String dll = "rundll32 url.dll,FileProtocolHandler";
		String fileName = REPORT_PATH + REPORT_NAME + ".pdf"; 
		File file = new File(fileName);
		try{
			if(file.exists()){
				//Process process = Runtime.getRuntime().exec(dll + " " + fileName);
				Process process = Runtime.getRuntime().exec("cmd /c start /wait " + REPORT_PATH + "copyReportDispense.bat");
				process = Runtime.getRuntime().exec(dll + " " + REPORT_PATH_FILE + REPORT_NAME + ".pdf");
				process.waitFor();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Compute dispense and remaining amount
	 * @param bud
	 * @return
	 */
	private BigDecimal[] computeAmount(BankAccounts accnt){
		
		IBudget bud = null;
		try{bud=Budget.retrieveBudget("SELECT * FROM budget WHERE bank_id="+accnt.getBankId() + " AND budtypeid="+BudgetType.MONTHLY.getId(), new String[0]).get(0);}catch(Exception e){}
		String dateFrom = DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		String dateTo = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		if(bud!=null){
			
			int month = DateUtils.getCurrentMonth(); 
			int year = DateUtils.getCurrentYear();
			
			int dateToday = Integer.valueOf(DateUtils.getCurrentDateYYYYMMDD().split("-")[2]);
			if(bud.getCycleDate()>dateToday){
				month -=1;
			}
			
			dateFrom = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
			
			if(month==12){
				year +=1;
				month=1; 
				
				dateTo = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
				
			}else{
				month +=1; 
				int cycleDate = bud.getCycleDate() - 1;
				if(cycleDate==0){
					cycleDate=31;
					month -=1;
				}
				dateTo = year + "-" + (month<10? "0"+month : month) + "-" + (cycleDate<10? "0" + cycleDate : cycleDate);
			}
		}
		
		System.out.println("Compute amount");
		String fieldName = "amount";
		BigDecimal[] value= new BigDecimal[2];
		String sql = "SELECT sum(cheque_amount) as "+ fieldName +" FROM tbl_chequedtls WHERE  accnt_no=? AND (date_disbursement>=? and date_disbursement<=?) and isactive=1 and chkstatus=1";
		String[] params = new String[3];
		params[0] = accnt.getBankId()+"";
		params[1] = dateFrom;//DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);//DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		params[2] = dateTo;//DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		BigDecimal dispense_amnt = com.italia.municipality.lakesebu.controller.Chequedtls.sum(sql, params, fieldName); 
		value[0] = dispense_amnt; // value of total dispense of the current month
		System.out.println("Compute amount dispense amount : " + dispense_amnt);
		sql = "SELECT * FROM budget b, budgettype t, tbl_bankaccounts a WHERE b.bank_id = a.bank_id AND b.budtypeid=t.budtypeid AND a.bank_id=? AND t.budtypeid=? AND budisactive=1 ";
		params = new String[2];
		params[0] = accnt.getBankId()+"";
		params[1] = BudgetType.MONTHLY.getId()+"";
		List<IBudget> buds = Budget.retrieve(sql, params);
		if(buds.size()>0){
			try{value[1] = buds.get(0).getAmount().subtract(dispense_amnt); }catch(Exception e){value[1]=new BigDecimal("0.00");}//value of remaining amount
			System.out.println("Compute amount remaining amount : " + value[1]);
		}
		
		return value;
	}
	
	private  void compileReportList(List<ReportFields> reportFields){
		try{
			Date dateFrom = DateUtils.getDateToday();
			Date dateTo = dateFrom;
			if(getRangeDate()!=null && getRangeDate().size()>1) {
				dateFrom = getRangeDate().get(0);
				dateTo = getRangeDate().get(1);
			}else {
				dateFrom = getRangeDate().get(0);
				dateTo = dateFrom;
			}
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME =ReadConfig.value(AppConf.CHEQUE_REPORT_NAME_DISPENSE);
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		ArrayList<ReportFields> rpts = new ArrayList<ReportFields>();
	  		
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reportFields);
	  		HashMap param = new HashMap();
	  		
	  		DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
			Date date = new Date();
			String _date = dateFormat.format(date);
			param.put("PARAM_DATE_RANGE", "From: "+ DateUtils.convertDate(dateFrom,"yyyy-MM-dd") + " to " + DateUtils.convertDate(dateTo,"yyyy-MM-dd"));
	  		param.put("PARAM_DATE", "Printed: "+_date);
	  		param.put("PARAM_GRAND_TOTAL","Total: Php "+getGrandTotal());
	  		param.put("PARAM_PREPAREDBY", "Prepared By: "+ Login.getUserLogin().getUserDtls().getFirstname() + " " + Login.getUserLogin().getUserDtls().getLastname());
	  		if(getSearchBankAccountId()!=null && !getSearchBankAccountId().isEmpty()){
	  			BankAccounts accnt = new BankAccounts();
	  			accnt.setBankId(Integer.valueOf(getSearchBankAccountId()));
	  			BigDecimal[] amnt = computeAmount(accnt);
	  			IBudget bud = null;
	  			try{bud=Budget.retrieveBudget("SELECT * FROM budget WHERE bank_id="+accnt.getBankId() + " AND budtypeid="+BudgetType.MONTHLY.getId(), new String[0]).get(0);
	  			System.out.println("pasok ako dito");}catch(Exception e){System.out.println("ERROR AKO :" +e.getMessage());}
	  			int month = DateUtils.getCurrentMonth(); 
  				int year = DateUtils.getCurrentYear();
  				
  				int dateToday = Integer.valueOf(DateUtils.getCurrentDateYYYYMMDD().split("-")[2]);
  				if(bud.getCycleDate()>dateToday){
  					month -=1;
  				}
  				
  				String firstDayOfTheMonth = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
	  			
	  			if(amnt[0]!=null){
	  				double dis = Double.valueOf(amnt[0]+"");
	  				param.put("PARAM_TOTAL_DISPENSE","Total Disbursement from " + firstDayOfTheMonth + " To " + DateUtils.convertDate(dateTo,"yyyy-MM-dd") + ": Php "+Currency.formatAmount(dis+""));
	  			}else{
	  				param.put("PARAM_TOTAL_DISPENSE","Total Disbursement from " + firstDayOfTheMonth + " To " + DateUtils.convertDate(dateTo,"yyyy-MM-dd") + ": Php 0.00");
	  			}
	  			
	  			if(amnt[1]!=null){
	  				double bal = Double.valueOf(amnt[1]+"");
	  				param.put("PARAM_BALANCE","Balance: Php "+Currency.formatAmount(bal+""));
	  			}else{
	  				param.put("PARAM_BALANCE","Balance: Php 0.00");
	  			}
	  			
	  			
	  			
	  			
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
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
		
		
			System.out.println("pdf successfully created...");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
	public List getFormatPrint() {
		
		formatPrint= new ArrayList<>();
		
		for(PrintFormat prnt : mapprint.values()){
		formatPrint.add(new SelectItem(prnt.getId(),prnt.getName()));
		}
		
		return formatPrint;
	}
	public void savePrintFormat(){
		System.out.println("Print format processing..");
	}
	
	private void getFormatList(){
		mapprint = java.util.Collections.synchronizedMap(new HashMap<Integer,PrintFormat>());
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM tbl_chequeprintformat";
		try{
		
			conn=BankChequeDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			System.out.println("Search SQL: "+ ps.toString());
			rs = ps.executeQuery();
			
			while(rs.next()){
				PrintFormat frmt = new PrintFormat();
				frmt.setId(rs.getInt("id"));
				frmt.setName(rs.getString("name"));
				boolean isSelected = rs.getInt("isSelected")==1? true : false;
				frmt.setSelected(isSelected);
				frmt.setFileName(rs.getString("fileName"));
				mapprint.put(frmt.getId(), frmt);
			}
			rs.close();
			ps.close();
			BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void setFormatPrint(List formatPrint) {
		this.formatPrint = formatPrint;
	}
	public String getPrintFormat() {
		return printFormat;
	}
	public void setPrintFormat(String printFormat) {
		this.printFormat = printFormat;
	}
	/*
	public List<Chequedtls> getChequedtls() {
		
		
		setBankCheckPayTo(getBankCheckPayTo()==null? "a" : getBankCheckPayTo());
		
		chequedtls = new ArrayList<>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT DISTINCT pay_to_the_order_of FROM tbl_chequedtls ORDER BY pay_to_the_order_of LIMIT 100";// WHERE pay_to_the_order_of like '%"+ getBankCheckPayTo() +"%'";
		try{
		
			conn=BankChequeDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while(rs.next()){
				Chequedtls cheqdtls = new Chequedtls();
				cheqdtls.setPayToTheOrderOf(rs.getString("pay_to_the_order_of"));
				chequedtls.add(cheqdtls);
			}
			rs.close();
			ps.close();
			BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return chequedtls;
	}*/
	/*
	public void setChequedtls(List<Chequedtls> chequedtls) {
		this.chequedtls = chequedtls;
	}*/
	public String getUserNotification() {
		return userNotification;
	}
	public void setUserNotification(String userNotification) {
		this.userNotification = userNotification;
	}
	public List getAccountList() {
		
		accountList = new ArrayList<>();
		for(BankAccounts a : accounts.values()){
			accountList.add(new SelectItem(a.getBankId(),a.getBankAccntNo() + " " + a.getBankAccntName()));
		}
		
		return accountList;
	}
	public void setAccountList(List accountList) {
		this.accountList = accountList;
	}
	public String getAccountLabel() {
		return accountLabel;
	}
	public void setAccountLabel(String accountLabel) {
		this.accountLabel = accountLabel;
	}
	
	
	public void saveData(){
		
		boolean isOk = true;
		
		if(getBankCheckAccountNumber()==null || getBankCheckAccountNumber().isEmpty()) {
			Application.addMessage(3, "Error", "Please select Account No");
			isOk = false;
		}
		
		if(getBankCheckPayTo()==null || getBankCheckPayTo().isEmpty()) {
			Application.addMessage(3, "Error", "Please provide Pay To The Order Of");
			isOk = false;
		}
		
		if(getInputAmount()==null || getInputAmount().isEmpty()) {
			Application.addMessage(3, "Error", "Please provide amount");
			isOk = false;
		}
		
		if(getNatureOfPayment()==null || getNatureOfPayment().isEmpty()) {
			Application.addMessage(3, "Error", "Please provide nature of payment");
			isOk = false;
		}
		
		if(getBankCheckName()==null || getBankCheckName().isEmpty()) {
			Application.addMessage(3, "Error", "Please select Account No");
			isOk = false;
		}
		
		if(getBankCheckAccntName()==null || getBankCheckAccntName().isEmpty()) {
			Application.addMessage(3, "Error", "Please select Account No");
			isOk = false;
		}
		
		if(isOk) {
		
		com.italia.municipality.lakesebu.controller.Chequedtls chk = new com.italia.municipality.lakesebu.controller.Chequedtls();
		if(getChequedtlsData()!=null){
			chk = getChequedtlsData();
		}else{
			/**
			 * Date created
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			chk.setDate_edited(dateFormat.format(date));
			chk.setIsActive(1);
		}
		
		if(getDateTime()==null) setDateTime(DateUtils.getDateToday());
		
		chk.setCheckNo(getBankCheckNo());
		String date_dis = getDateTime()==null? DateUtils.getCurrentDateYYYYMMDD() : DateUtils.convertDate(getDateTime(),"yyyy-MM-dd");
		date_dis = date_dis.isEmpty()? DateUtils.getCurrentDateYYYYMMDD() : date_dis;
		chk.setDate_disbursement(date_dis);//convertDateToMonthDayYear(getDateTime())
		chk.setBankName(getBankCheckName());
		chk.setAccntName(getBankCheckAccntName());
		chk.setPayToTheOrderOf(getBankCheckPayTo().toUpperCase());
		
		if(getInputAmount()==null){
			chk.setAmount(0.00);
			chk.setAmountInWOrds("ZERO PESOS ONLY.");
		}else{
			
			chk.setAmount(Double.valueOf(getInputAmount().replace(",", "")));
			chk.setAmountInWOrds(getNumberInToWords());
		}
		
		
		chk.setSignatory1(Integer.valueOf(getSig1()));
		chk.setSignatory2(Integer.valueOf(getSig2()));
		
		chk.setStatus(getStatusId()==0? 1: getStatusId());
		chk.setRemarks(getRemarks()==null? (getStatusId()==2? "CANCELLED CHECK" : "RECEIVED") : getRemarks() );
		
		if(getStatusId()==2){
			chk.setAmount(0.00);
			setInputAmount("0.00");
			chk.setAmountInWOrds("ZERO PESOS ONLY.");
		}
		chk.setAccntNumber(getBankCheckAccountNumber());
		
		HttpSession session = SessionBean.getSession();
		String proc_by = session.getAttribute("username").toString();
		if(proc_by!=null){
			chk.setProcessBy(proc_by);
		}else{
			chk.setProcessBy("Error");
		}
		
		if(checkLimit()){
		
			chk = chk.save(chk);
		
		//create xml copy
		saveXML(chk);
		
		rangeDate = new ArrayList<Date>();
		rangeDate.add(getDateTime());
		rangeDate.add(getDateTime());
		
		saveInit();
		setBankCheckName(chk.getBankName());
		setBankCheckAccntName(chk.getAccntName());
		setBankCheckAccountNumber(chk.getAccntNumber());
		loadNewCheckNo();
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Saved.", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		
		}else{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Limit amount has been reached.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		}
	}
	
	private void saveInit() {
		
		getSignatories();
		getAccounts();
		
		cheques = new ArrayList<com.italia.municipality.lakesebu.controller.Chequedtls>();
		com.italia.municipality.lakesebu.controller.Chequedtls chk = new com.italia.municipality.lakesebu.controller.Chequedtls();
		String sql = "SELECT * FROM tbl_chequedtls WHERE isactive=1 AND (date_disbursement>=? AND date_disbursement<=? ) ORDER BY date_edited DESC LIMIT 50";
		String[] params = new String[2];
		
		if(getRangeDate()!=null && getRangeDate().size()>1) {
			params[0] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");
			params[1] = DateUtils.convertDate(getRangeDate().get(1),"yyyy-MM-dd");
		}else {
			params[0] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");
			params[1] = params[0];
		}
		
		
		
		double amount = 0d;
		for(com.italia.municipality.lakesebu.controller.Chequedtls chq : chk.retrieve(sql, params)){
			if(chq.getStatus()==1){
				amount += chq.getAmount();
			}
			chq.setAccntNumber(bankAccountNum(chq.getAccntNumber()));
			cheques.add(chq);
		}
		setGrandTotal(formatAmount(amount+""));
		
		if(getSig1()==null || "".equalsIgnoreCase(getSig1())){
			setSig1Label("Please Select...");
		}
		if(getSig2()==null || "".equalsIgnoreCase(getSig2())){
			setSig2Label("Please Select...");
		}
		if(getBankCheckAccountNumber()==null || "".equalsIgnoreCase(getBankCheckAccountNumber())){
			setAccountLabel("Please Select...");
		}
		
		
		//clearing fields
		setBankCheckAccountNumber(null);
		setBankCheckNo(null);
		setDateTime(DateUtils.getDateToday());
		setBankCheckName(null);
		setBankCheckAccntName(null);
		setBankCheckPayTo(null);
		setInputAmount(null);
		setNumberInToWords(null);
		setSig1(null);
		setSig2(null);
		setChequedtlsData(null);
		setStatusId(1);
		setRemarks("RECEIVED");
		setEnableRemarks(false);
		setNatureOfPayment(null);
		setDepartmentId(0);
	}
	
	private boolean checkAmount() {
		try {
			double amount = Double.valueOf(getInputAmount().replace(",", ""));
			return true;
		}catch(NumberFormatException nu) {
			return false;
		}
		
	}
	
	public void checkingFieldBeforeSave() {
		PrimeFaces pf = PrimeFaces.current();
		String scs = "";
		boolean isOk = true;
		if(getBankCheckAccountNumber()==null || getBankCheckAccountNumber().isEmpty()) {
			isOk = false;
			scs += "$('#accId').css('border', '3px solid red');";
			scs += "$('#checkNoId').css('border', '3px solid red');";
		}else {
			scs += "$('#accId').css('border-top', 'none');$('#accId').css('border-left', 'none');$('#accId').css('border-right', 'none');$('#accId').css('border-bottom', '3px solid black');$('#accId').css('color', 'black');";
			scs += "$('#checkNoId').css('border-top', 'none');$('#checkNoId').css('border-left', 'none');$('#checkNoId').css('border-right', 'none');$('#checkNoId').css('border-bottom', '3px solid black');$('#checkNoId').css('color', 'black');";
		}
		
		if(getBankCheckPayTo()==null || getBankCheckPayTo().isEmpty()) {
			isOk = false;
			scs += "$('#idpayto').css('border', '3px solid red');";
		}else {
			scs += "$('#idpayto').css('border-top', 'none');$('#idpayto').css('border-left', 'none');$('#idpayto').css('border-right', 'none');$('#idpayto').css('border-bottom', '3px solid black');$('#idpayto').css('color', 'black');";
		}
		
		if(getInputAmount()==null || getInputAmount().isEmpty()) {
			isOk = false;
			scs += "$('#amntId').css('border', '3px solid red');";
		}else {
			if(checkAmount()) {
				scs += "$('#amntId').css('border-top', 'none');$('#amntId').css('border-left', 'none');$('#amntId').css('border-right', 'none');$('#amntId').css('border-bottom', '3px solid black');$('#amntId').css('color', 'black');";
			}else {
				scs += "$('#amntId').css('border', '3px solid red');";
				isOk = false;
			}
		}
		
		if(getNatureOfPayment()==null || getNatureOfPayment().isEmpty()) {
			isOk = false;
			scs += "$('#idnature').css('border', '3px solid red');";
		}else {
			scs += "$('#idnature').css('border-top', 'none');$('#idnature').css('border-left', 'none');$('#idnature').css('border-right', 'none');$('#idnature').css('border-bottom', '3px solid black');$('#idnature').css('color', 'black');";
		}
		
		if(getBankCheckName()==null || getBankCheckName().isEmpty()) {
			isOk = false;
		}
		
		if(getBankCheckAccntName()==null || getBankCheckAccntName().isEmpty()) {
			isOk = false;
		}
		
		if(getNumberInToWords()!=null && "PESOS ONLY.".equalsIgnoreCase(getNumberInToWords().trim())) {
			scs += "$('#amntId').css('border', '3px solid red');";
			isOk = false;
		}
		
		if(isOk) {
			
			pf.executeScript("PF('dlgAlert').show(100)");
		}else {
			pf.executeScript(scs);
		}
		
	}
	
	
	public void savePrint() {
		
		
		com.italia.municipality.lakesebu.controller.Chequedtls chk = new com.italia.municipality.lakesebu.controller.Chequedtls();
		if(getChequedtlsData()!=null){
			chk = getChequedtlsData();
		}else{
			/**
			 * Date created
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			chk.setDate_edited(dateFormat.format(date));
			chk.setIsActive(1);
		}
		
		if(getDateTime()==null) setDateTime(DateUtils.getDateToday());
		
		chk.setCheckNo(getBankCheckNo());
		String date_dis = getDateTime()==null? DateUtils.getCurrentDateYYYYMMDD() : DateUtils.convertDate(getDateTime(),"yyyy-MM-dd");
		date_dis = date_dis.isEmpty()? DateUtils.getCurrentDateYYYYMMDD() : date_dis;
		chk.setDate_disbursement(date_dis);
		chk.setBankName(getBankCheckName());
		chk.setAccntName(getBankCheckAccntName());
		chk.setPayToTheOrderOf(getBankCheckPayTo().toUpperCase());
		
		if(getInputAmount()==null){
			chk.setAmount(0.00);
			chk.setAmountInWOrds("ZERO PESOS ONLY.");
		}else{
			
			chk.setAmount(Double.valueOf(getInputAmount().replace(",", "")));
			chk.setAmountInWOrds(getNumberInToWords());
		}
		
		
		chk.setSignatory1(Integer.valueOf(getSig1()));
		chk.setSignatory2(Integer.valueOf(getSig2()));
		
		chk.setStatus(getStatusId()==0? 1: getStatusId());
		chk.setRemarks(getRemarks()==null? (getStatusId()==2? "CANCELLED CHECK" : "RECEIVED") : getRemarks() );
		
		if(getStatusId()==2){
			chk.setAmount(0.00);
			setInputAmount("0.00");
			chk.setAmountInWOrds("ZERO PESOS ONLY.");
		}
		
		chk.setAccntNumber(getBankCheckAccountNumber());
		
		HttpSession session = SessionBean.getSession();
		String proc_by = session.getAttribute("username").toString();
		if(proc_by!=null){
			chk.setProcessBy(proc_by);
		}else{
			chk.setProcessBy("Error");
		}
		
		if(checkLimit()){
		
			chk = chk.save(chk);
		
		//create xml copy
		saveXML(chk);
		
		rangeDate = new ArrayList<Date>();
		rangeDate.add(getDateTime());
		rangeDate.add(getDateTime());
		
		saveInit();
		
		setBankCheckName(chk.getBankName());
		setBankCheckAccntName(chk.getAccntName());
		setBankCheckAccountNumber(chk.getAccntNumber());
		loadNewCheckNo();
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Saved.", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		printReportIndividual(chk);
		}else{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Limit amount has been reached.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		
	}
	
	private void saveXML(com.italia.municipality.lakesebu.controller.Chequedtls chk){
		try{
			
			Voucher voucher = null;
			try{voucher = Voucher.retrieve("SELECT * FROM voucher WHERE checkno="+ chk.getCheckNo(), new String[0]).get(0);}catch(Exception e){}
			
			if(voucher!=null){
				System.out.println("check is not null");
				if(getStatusId()==2){
					voucher.setPayee("CANCELLED CHECK " + chk.getPayToTheOrderOf());
				}else{
					voucher.setPayee(chk.getPayToTheOrderOf()+"");
				}
			}else{
				System.out.println("check is null");
				voucher = new Voucher();
				voucher.setPayee(chk.getPayToTheOrderOf());
			}
			voucher.setNaturePayment(getNatureOfPayment()!=null? getNatureOfPayment().toUpperCase() : "");
			voucher.setDateTrans(chk.getDate_disbursement());
			voucher.setCheckNo(chk.getCheckNo());
			voucher.setIsActive(1);
			voucher.setTransType(TransactionType.CHECK_ISSUED.getId());
			voucher.setAmount(chk.getAmount());
			
			
			BankAccounts accounts = new BankAccounts();
			accounts.setBankId(Integer.valueOf(chk.getAccntNumber()));
			voucher.setAccounts(accounts);
			
			Department department = new Department();
			if(getDepartmentId()==0){
				setDepartmentId(49);
			}
			department = getDepartmentData().get(getDepartmentId());
			/*if(voucher==null){
			department.setDepid(1);
			department.setCode("1091");
			}else{
				department = voucher.getDepartment();
			}*/
			voucher.setDepartment(department);
			
			voucher.save();
				
			CheckXML xml = new CheckXML();
			xml.setDateTrans(chk.getDate_disbursement());
			xml.setCheckNo(chk.getCheckNo());
			xml.setIsActive(chk.getIsActive());
			xml.setCreditAmount(chk.getAmount());
			xml.setUserDtls(Login.getUserLogin().getUserDtls());
			xml.setTransactionType(TransactionType.CHECK_ISSUED.getId());
			
			xml.setPayee(getStatusId()==2? "CANCELLED CHECK "+chk.getPayToTheOrderOf() : chk.getPayToTheOrderOf());
			if(voucher==null){
				xml.setNaturePayment(getNatureOfPayment()+"");
				xml.setVoucherNo("");
				xml.setOrNumber("");
			}else{
				xml.setNaturePayment(getNatureOfPayment());
				xml.setVoucherNo("");
				xml.setOrNumber("");
			}
			xml.setDepartment(department);
			
			xml.setAccounts(accounts);
			BookCheck.createXML(xml, chk.getCheckNo()+".xml");
			System.out.println("successfully created xml file");
			}catch(Exception e){}
	}
	
	public List<String> completeBankCheckPayTo(String query){
		List<String> results = new ArrayList<String>();
		
		String sql = "SELECT DISTINCT pay_to_the_order_of FROM tbl_chequedtls WHERE pay_to_the_order_of like '%" + query.replace("--", "") + "%' LIMIT 20";
		results = com.italia.municipality.lakesebu.controller.Chequedtls.retrievePayOrderOf(sql, new String[0]);
		
		return results;
	}
	
	
	public List<String> completeNaturePayment(String query){
		List<String> results = new ArrayList<String>();
		
		String sql = "SELECT DISTINCT naturepayment FROM voucher WHERE naturepayment like '%" + query.replace("--", "") + "%' LIMIT 20";
		results = Voucher.retrieveNature(sql, new String[0]);
		
		return results;
	}
	
	public void createNew(){
		//loadDepartment();
		clearFields();
	}
	
	public void clearFields(){
		setBankCheckAccountNumber(null);
		setBankCheckNo(null);
		setDateTime(DateUtils.getDateToday());
		setBankCheckName(null);
		setBankCheckAccntName(null);
		setBankCheckPayTo(null);
		setInputAmount(null);
		setNumberInToWords(null);
		setSig1(null);
		setSig2(null);
		setChequedtlsData(null);
		setStatusId(1);
		setRemarks("RECEIVED");
		setEnableRemarks(false);
		Date date = DateUtils.getDateToday();
		rangeDate = new ArrayList<Date>();
		rangeDate.add(date);
		rangeDate.add(date);
		setNatureOfPayment(null);
		setDepartmentId(0);
		
		
	}
	
	
	@PostConstruct
	public void init(){
		Date date = DateUtils.getDateToday();
		rangeDate = new ArrayList<>();
		rangeDate.add(date);
		rangeDate.add(date);
		
		getSignatories();
		getAccounts();
		cheques = new ArrayList<com.italia.municipality.lakesebu.controller.Chequedtls>();
		com.italia.municipality.lakesebu.controller.Chequedtls chk = new com.italia.municipality.lakesebu.controller.Chequedtls();
		String sql = "SELECT * FROM tbl_chequedtls WHERE isactive=1 AND (date_disbursement>=? AND date_disbursement<=? ) ORDER BY date_edited DESC LIMIT 50";
		String[] params = new String[2];
		
		if(getRangeDate()!=null && getRangeDate().size()>1) {
			params[0] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");
			params[1] = DateUtils.convertDate(getRangeDate().get(1),"yyyy-MM-dd");
		}else {
			params[0] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");
			params[1] = params[0];
		}
		
		
		
		double amount = 0d;
		for(com.italia.municipality.lakesebu.controller.Chequedtls chq : chk.retrieve(sql, params)){
			if(chq.getStatus()==1){
				amount += chq.getAmount();
			}
			chq.setAccntNumber(bankAccountNum(chq.getAccntNumber()));
			cheques.add(chq);
		}
		setGrandTotal(formatAmount(amount+""));
		if(getSig1()==null || "".equalsIgnoreCase(getSig1())){
			setSig1Label("Please Select...");
		}
		if(getSig2()==null || "".equalsIgnoreCase(getSig2())){
			setSig2Label("Please Select...");
		}
		if(getBankCheckAccountNumber()==null || "".equalsIgnoreCase(getBankCheckAccountNumber())){
			setAccountLabel("Please Select...");
		}
		
		dummyBarModel();
	}
	
	private String bankAccountNum(String id){
		String result = "";
		String sql = "SELECT * FROM tbl_bankaccounts WHERE bank_id=?";
		String[] params = new String[1];
		params[0] = id+"";
		List<BankAccounts> accounts = BankAccounts.retrieve(sql, params);
		
		if(accounts.size()>0){
			BankAccounts acc = accounts.get(0);
			result = acc.getBankAccntNo();
		}
		
		
		return result;
	}
	
	private void loadNewCheckNo(){
		if(getChequedtlsData()==null){
			
			int i = 0;
			
			if(getBankCheckAccountNumber()!=null && !getBankCheckAccountNumber().isEmpty()){
				i = getLastAccountCheckNo(getBankCheckAccountNumber());
				i +=1; //increment 1
			}else{
				 i = getLastCheckNo()!=0?  getLastCheckNo() + 1 : 0;
			}
			
			String len = i+"";
			int ln = len.length();
			switch(ln){
			case 0 : setBankCheckNo("0000000000"); break;
			case 1 : setBankCheckNo("000000000"+i); break;
			case 2 : setBankCheckNo("00000000"+i); break;
			case 3 : setBankCheckNo("0000000"+i); break;
			case 4 : setBankCheckNo("000000"+i); break;
			case 5 : setBankCheckNo("00000"+i); break;
			case 6 : setBankCheckNo("0000"+i); break;
			case 7 : setBankCheckNo("000"+i); break;
			case 8 : setBankCheckNo("00"+i); break;
			case 9 : setBankCheckNo("0"+i); break;
			case 10 : setBankCheckNo(""+i); break;
			
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
	 * @param dateVal Month day, Year 
	 * @return YYYY-MM-DD
	 */
	private String convertDateToYearMontyDay(String dateVal){
		//System.out.println("Date : " + dateVal);
		if(dateVal==null || dateVal.isEmpty()){
			dateVal = DateUtils.getCurrentDateMMMMDDYYYY();
		}
		String tmp = dateVal.split(",")[0];
		String month = tmp.split(" ")[0];
		String day = tmp.split(" ")[1];
		String year = dateVal.split(",")[1].trim();
		dateVal = year + "-" + getMonthNumber(month) + "-" + day;
		return dateVal;
	}
	
	private String getMonthNumber(String month){
		switch(month){
			case "January": return "01";
			case "February" : return "02";
			case "March" : return "03";
			case "April" : return "04";
			case "May" : return "05";
			case "June" : return "06";
			case "July" : return "07";
			case "August" : return "08";
			case "September" :  return "09";
			case "October" : return "10";
			case "November" : return "11";
			case "December" : return "12";
		}
		return "January";
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
	
	
	public void clickItem(com.italia.municipality.lakesebu.controller.Chequedtls chk){
		//loadDepartment();
		Voucher voucher = null;
		try{voucher = Voucher.retrieve("SELECT * FROM voucher WHERE checkno="+ chk.getCheckNo(), new String[0]).get(0);}catch(Exception e){}
		try{setNatureOfPayment(voucher.getNaturePayment());}catch(Exception e){}
		Department dep = null;
		try{dep = Department.department(voucher.getDepartment().getDepid()+"");}catch(Exception e) {}
		try{setDepartmentId(dep.getDepid());}catch(Exception e) {}
		
		setChequedtlsData(chk);
		setBankCheckNo(chk.getCheckNo());
		System.out.println("chk.getDate_disbursement() = " + chk.getDate_disbursement());
		setDateTime(DateUtils.convertDateString(chk.getDate_disbursement(),"yyyy-MM-dd"));
		System.out.println("click check date is " + getDateTime());
		setBankCheckName(chk.getBankName());
		setBankCheckAccntName(chk.getAccntName());
		setBankCheckPayTo(chk.getPayToTheOrderOf());
		//System.out.println("clickItem Input amount : " + chk.getAmount());
		setInputAmount(Currency.formatAmount(chk.getAmount()));
		setNumberInToWords(chk.getAmountInWOrds());
		setSig1(chk.getSignatory1()+"");
		setSig2(chk.getSignatory2()+"");
		setRemarks(chk.getRemarks());
		setStatusId(chk.getStatus());
		
		if(chk.getStatus()==1){
			setEnableRemarks(false);
		}else{
			setEnableRemarks(true);
		}
		
		for(BankAccounts acc : accounts.values()){
			if(chk.getAccntNumber().equalsIgnoreCase(acc.getBankAccntNo())){
				setBankCheckAccountNumber(acc.getBankId()+"");
			}
		}
		
	}
	
	public void setSig1Label(String sig1Label){
		this.sig1Label = sig1Label;
	}
	public String getSig1Label(){
		return sig1Label;
	}
	
	public void setSig2Label(String sig2Label){
		this.sig2Label = sig2Label;
	}
	public String getSig2Label(){
		return sig2Label;
	}
	
	private Map<String,Signatories> signatories = new HashMap<String,Signatories>();
	
	
	public void getSignatories(){
		signatories = new HashMap<String,Signatories>();
		
		String sql = "SELECT * FROM signatory";
		String params[] = new String[0];
		signatories = Signatories.retrieveSig(sql, params);	
	}
	public void processAccnt(AjaxBehaviorEvent e){
		try{
		String id = getBankCheckAccountNumber();
		if(accounts!=null && !"".equalsIgnoreCase(id)){
			int i = Integer.valueOf(id);
			BankAccounts account = accounts.get(i);
			setBankCheckName(account.getBankAccntBranch());
			setBankCheckAccntName(account.getBankAccntName());
			loadNewCheckNo();
			
		}
		}catch(Exception es){
			es.printStackTrace();
		}
	}
	public void getAccounts(){
		accounts = java.util.Collections.synchronizedMap(new HashMap<Integer,BankAccounts>());
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM tbl_bankaccounts";
		try{
		
			conn=BankChequeDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);		
			rs = ps.executeQuery();
			while(rs.next()){
				BankAccounts account = new BankAccounts();
				account.setBankId(rs.getInt("bank_id"));
				account.setBankAccntNo(rs.getString("bank_account_no"));
				account.setBankAccntName(rs.getString("bank_account_name"));
				account.setBankAccntBranch(rs.getString("bank_branch"));
				accounts.put(account.getBankId(), account);
				
			}
			rs.close();
			ps.close();
			BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public String getBankCheckName() {
		return bankCheckName;
	}
	public void setBankCheckName(String bankCheckName) {
		this.bankCheckName = bankCheckName;
	}
	public String getBankCheckAccountNumber() {
		return bankCheckAccountNumber;
	}
	public void setBankCheckAccountNumber(String bankCheckAccountNumber) {
		this.bankCheckAccountNumber = bankCheckAccountNumber;
	}
	public String getBankCheckNo() {
		return bankCheckNo;
	}
	public void setBankCheckNo(String bankCheckNo) {
		this.bankCheckNo = bankCheckNo;
	}
	public String getBankCheckDate() {
		return bankCheckDate;
	}
	public void setBankCheckDate(String bankCheckDate) {
		this.bankCheckDate = bankCheckDate;
	}
	public String getBankCheckAccntName() {
		return bankCheckAccntName;
	}
	public void setBankCheckAccntName(String bankCheckAccntName) {
		this.bankCheckAccntName = bankCheckAccntName;
	}
	public String getBankCheckAmount() {
		return bankCheckAmount;
	}
	public void setBankCheckAmount(String bankCheckAmount) {
		this.bankCheckAmount = bankCheckAmount;
	}
	public String getBankCheckPayTo() {
		return bankCheckPayTo;
	}
	public void setBankCheckPayTo(String bankCheckPayTo) {
		this.bankCheckPayTo = bankCheckPayTo;
	}
	public String getBankCheckInWords() {
		return bankCheckInWords;
	}
	public void setBankCheckInWords(String bankCheckInWords) {
		this.bankCheckInWords = bankCheckInWords;
	}
	
	private String sig1;
	private String sig2;
	
	public void setSig1(String sig1){
		this.sig1 = sig1;
	}
	public String getSig1(){
		//System.out.println(sig1);
		if(sig1==null){
			sig1 = "1";
		}
		return sig1;
	}
	public void setSig2(String sig2){
		this.sig2 = sig2;
	}
	public String getSig2(){
		//System.out.println(sig2);
		if(sig2==null){
			sig2 = "3";
		}
		return sig2;
	}
	
	
	private List data = new ArrayList<>();
	public List getData(){
		data = new ArrayList<>();
		
		Signatories sig = new Signatories();
		sig = signatories.get("1");
		data.add(new SelectItem(sig.getId()+"",sig.getName()));
		sig = signatories.get("2");
		data.add(new SelectItem(sig.getId()+"",sig.getName()));
		
		
		
		return data;
	}
	public void setData(List data1){
		this.data1 = data1;
	}
	private List data1 = new ArrayList<>();
	public List getData1(){
		data1= new ArrayList<>();
		
		Signatories sig = new Signatories();
		sig = signatories.get("3");
		data1.add(new SelectItem(sig.getId()+"",sig.getName()));
		sig = signatories.get("4");
		data1.add(new SelectItem(sig.getId()+"",sig.getName()));
		
		
		
		return data1;
	}
	public void setData1(List data1){
		this.data1 = data1;
	}
	
	public void setInputAmount(String inputAmount){
		this.inputAmount = inputAmount;
	}
	public String getInputAmount(){
		return inputAmount;
	}
	public void setDateTime(Date dateTime){
		this.dateTime = dateTime;
	}
	public Date getDateTime(){
		
		if(dateTime==null){
			dateTime = DateUtils.getDateToday();
		}
		
		return dateTime;
	}
	
	public void generateWords(){
		String result = "";
		try{
			inputAmount = inputAmount==null? "0.00" : inputAmount.replace(",", "");
		NumberToWords numberToWords = new NumberToWords();
		result = numberToWords.changeToWords(inputAmount).toUpperCase();
		
		String[] val = new String[2];
		//if(inputAmount!=null){
		if(inputAmount!=null){
			val[0] = inputAmount;
			val[1] = result;
			
			//System.out.println(val[0] + "----" + val[1]);
			
			//compileReport(val);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(result.equalsIgnoreCase(" PESOS ONLY.")){
			setNumberInToWords("");
		}
		setNumberInToWords(result.trim());
	}
	
	public String getNumberInToWords(){
		 
		return words;
	}
	public void setNumberInToWords(String words){
		this.words = words;
	}
	
	//@ManagedProperty(value="#{loginBean}")
	//private LoginBean loginData;
	@Deprecated
	public String saveCheque(){
		System.out.println("Save");
		setBankCheckDate(DateUtils.convertDate(dateTime,"yyyy-MM-dd"));
		setBankCheckAmount(inputAmount+"");
		setBankCheckInWords(words);
		System.out.println(
				"Account No: " + getBankCheckAccountNumber() +
				"\nCheck No: " + getBankCheckNo() +
				"\nDate: " + getBankCheckDate() +
				"\nBank Name: " + getBankCheckName() +
				"\nAccount Name: " + getBankCheckAccntName() +
				"\nAmount: " + getBankCheckAmount() +
				"\nPAY TO THE ORDER OF: " + getBankCheckPayTo() +
				"\nPESOS: " + getBankCheckInWords() +
				"\nSignature 1 : " + getSig1() +
				"\nSignature 2 : " + getSig2()
				);
		
		if(checkFields()){
			return "Failed saving data...";
		}
		
		
		List data = new ArrayList();
				
				data.add(getBankCheckAccountNumber());
				data.add(getBankCheckAccntName());
				data.add(getBankCheckName());
				data.add(getBankCheckDate());
				data.add(getBankCheckAmount());
				data.add(getBankCheckPayTo().toUpperCase());
				data.add(getBankCheckInWords());
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				
				HttpSession session = SessionBean.getSession();
				String proc_by = session.getAttribute("username").toString();
				
				
				String _sig1 = !getSig1().isEmpty()? getSig1() : "1";
				String _sig2 = !getSig2().isEmpty()? getSig2() : "3";
				
				
				//use for printing
				
				/*reportFields = new ReportFields();
				reportFields.setCheckNo(getBankCheckNo());
				reportFields.setAccntNumber(getBankCheckAccountNumber());
				reportFields.setAccntName(getBankCheckAccntName());
				reportFields.setBankName(getBankCheckName());
				
				double money = Double.valueOf(getBankCheckAmount());
				NumberFormat format = NumberFormat.getCurrencyInstance();
				String formatted = format.format(money).replace("$", "");
				reportFields.setAmount((formatted));
				
				reportFields.setPayToTheOrderOf(getBankCheckPayTo());
				reportFields.setAmountInWOrds(getBankCheckInWords());
				reportFields.setDate_disbursement(getBankCheckDate());
				Signatory sig = signatories.get(_sig1);
				reportFields.setSignatory1(sig.getSigName());
						  sig = signatories.get(_sig2);
				reportFields.setSignatory2(sig.getSigName());*/
				
		String chkNo = getChequeInfo(getBankCheckNo());

		if(chkNo.equalsIgnoreCase("1")){
			//insert first data
			data.add(getBankCheckNo());
			data.add(dateFormat.format(date));
			data.add(dateFormat.format(date));
			data.add(Integer.valueOf(1));
			data.add(proc_by);
			
			data.add(_sig1);
			data.add(_sig2);
			
			insertData(data, "1");
			setUserNotification("Check information has been save.");
		}else if(chkNo.equalsIgnoreCase("2")){
			//update data
			data.add(dateFormat.format(date));
			data.add(proc_by);
			
			data.add(_sig1);
			data.add(_sig2);
			data.add(getBankCheckNo());
			
			updateData(data);
			setUserNotification("Check information has been updated.");
		}else if(chkNo.equalsIgnoreCase("3")){
			//add new data
			data.add(getBankCheckNo());
			data.add(dateFormat.format(date));
			data.add(dateFormat.format(date));
			int latestId = getLastCheckId() + 1;
			data.add(latestId);
			data.add(proc_by);
			
			data.add(_sig1);
			data.add(_sig2);
			
			insertData(data, "3");
			setUserNotification("Check information has been save.");
		}
		
	
		
		
		
		System.out.println("Preparing to create pdf file");
		
		return "Save";
	}
	private void insertData(List data, String type){
		String sql = "INSERT INTO tbl_chequedtls ("
				+ "cheque_id,"
				+ "accnt_no,"
				+ "accnt_name,"
				+ "bank_name,"
				+ "date_disbursement,"
				+ "cheque_amount,"
				+ "pay_to_the_order_of,"
				+ "amount_in_words,"
				+ "cheque_no,"
				+ "date_created,"
				+ "date_edited,"
				+ "proc_by,"
				+ "sig1_id,"
				+ "sig2_id)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
		//String sql = "INSERT INTO tbl_chequedtls (cheque_id) values(?)";
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		if("1".equalsIgnoreCase(type)){
			ps.setInt(1, 1);
		}else if("3".equalsIgnoreCase(type)){
			ps.setInt(1, Integer.valueOf(data.get(10).toString()));
		}
		ps.setString(2, data.get(0).toString());
		ps.setString(3, data.get(1).toString().toUpperCase());
		ps.setString(4, data.get(2).toString().toUpperCase());
		ps.setString(5, data.get(3).toString());
		ps.setString(6, data.get(4).toString());
		ps.setString(7, data.get(5).toString());
		ps.setString(8, data.get(6).toString().toUpperCase());
		ps.setString(9, data.get(7).toString().toUpperCase());
		ps.setTimestamp(10, Timestamp.valueOf(data.get(8).toString()));
		ps.setTimestamp(11, Timestamp.valueOf(data.get(9).toString()));
		ps.setString(12, data.get(11).toString());
		ps.setString(13, data.get(12).toString());
		ps.setString(14, data.get(13).toString());
		System.out.println("SQL ADD : " + ps.toString());
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	private void updateData(List data){
		String sql = "UPDATE tbl_chequedtls SET "
				+ "accnt_no=?,"
				+ "accnt_name=?,"
				+ "bank_name=?,"
				+ "date_disbursement=?,"
				+ "cheque_amount=?,"
				+ "pay_to_the_order_of=?,"
				+ "amount_in_words=?,"
				+ "date_edited=?,"
				+ "proc_by=?,"
				+ "sig1_id=?,"
				+ "sig2_id=?"
				+ " WHERE cheque_no=?";
	
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		ps.setString(1, data.get(0).toString());
		ps.setString(2, data.get(1).toString());
		ps.setString(3, data.get(2).toString());
		ps.setString(4, data.get(3).toString());
		ps.setString(5, data.get(4).toString());
		ps.setString(6, data.get(5).toString());
		ps.setString(7, data.get(6).toString());
		ps.setTimestamp(8, Timestamp.valueOf(data.get(7).toString()));
		ps.setString(9, data.get(8).toString());
		ps.setString(10, data.get(9).toString());
		ps.setString(11, data.get(10).toString());
		ps.setString(12, data.get(11).toString());
		System.out.println("SQL UPDATE : " + ps.toString());
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	private String getChequeInfo(String chkNo){
		
		boolean isNotNull=false;
		String result ="0";
		//id no data retrieve.
		//application will insert a default no which 1 for the first data
		int val = getLastCheckId();	
		if(val==0){
			isNotNull=true;
			result= "1"; // add first data
			System.out.println("First data");
		}
		
		//check if check_no is existing in table
		if(!isNotNull){
			isNotNull = isCheckNoExist(chkNo);
			if(isNotNull){
				result = "2"; // update existing data
				System.out.println("update data");
			}else{
				result = "3"; // add new data
				System.out.println("add new data");
			}
		}
		
		
		return result;
	}
	@Deprecated
	private void retrieveData(String val){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = ""; 
			sql = "SELECT * FROM tbl_chequedtls WHERE cheque_no=?";
		
		try{
			conn = BankChequeDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, val);
				
			System.out.println("Search SQL: "+ ps.toString());
			rs = ps.executeQuery();
			
			while(rs.next()){
				double amount = rs.getDouble("cheque_amount");
				setBankCheckAccountNumber(rs.getString("accnt_no"));
				setBankCheckNo(rs.getString("cheque_no"));
				setBankCheckAccntName(rs.getString("accnt_name").toUpperCase());
				setBankCheckName(rs.getString("bank_name").toUpperCase());
				setDateTime(DateUtils.convertDateString(rs.getString("date_disbursement"),"yyyy-MM-dd"));
				//setInputAmount(formatAmount(amount).replaceAll(",", ""));
				setInputAmount(amount+"");
				setBankCheckPayTo(rs.getString("pay_to_the_order_of").toUpperCase());
				setNumberInToWords(rs.getString("amount_in_words").toUpperCase());
				String sig1 = "";
				String sig2 = "";
				if(signatories!=null){
					sig1 = ""+rs.getInt("sig1_id");
					sig2 = ""+rs.getInt("sig2_id");
					setSig1(sig1);
					setSig2(sig2);
				}
				if(accounts!=null){
					setBankCheckAccountNumber(rs.getString("accnt_no"));
				}
			}
			
			rs.close();
			ps.close();
			BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	private String formatAmount(String amount){
		double money = Double.valueOf(amount);
		NumberFormat format = NumberFormat.getCurrencyInstance();
		amount = format.format(money).replace("$", "");
		amount = amount.replace("Php", "");
		return amount;
	}
	private int getLastCheckId(){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int result = 0;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT cheque_id FROM tbl_chequedtls ORDER BY cheque_id DESC LIMIT 1");
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=rs.getInt("cheque_id");
		}
		
		rs.close();
		ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		BankChequeDatabaseConnect.close(conn);
		return result;
	}
	private int getLastCheckNo(){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int result = 0;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT cheque_no FROM tbl_chequedtls ORDER BY cheque_id DESC LIMIT 1");
		rs = ps.executeQuery();
		
		if(rs.next()){
			result= Integer.valueOf(rs.getString("cheque_no"));
		}
		
		rs.close();
		ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		BankChequeDatabaseConnect.close(conn);
		return result;
	}
	
	private int getLastAccountCheckNo(String accountId){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int result = 0;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT cheque_no FROM tbl_chequedtls WHERE accnt_no="+ accountId +" ORDER BY cheque_id DESC LIMIT 1");
		rs = ps.executeQuery();
		
		if(rs.next()){
			result= Integer.valueOf(rs.getString("cheque_no"));
		}
		
		rs.close();
		ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		BankChequeDatabaseConnect.close(conn);
		return result;
	}
	
	private boolean isCheckNoExist(String chkNo){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT cheque_no FROM tbl_chequedtls WHERE cheque_no=?");
		ps.setString(1, chkNo);
		System.out.println("Is exist sql: " + ps.toString());
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		BankChequeDatabaseConnect.close(conn);
		return result;
	}
	
	public void deleteRow(com.italia.municipality.lakesebu.controller.Chequedtls chk){
		if(chk.getStatus()==1){
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Received data is not allowed for deletion", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}else{	
		chk.delete();
		
		Date date = DateUtils.getDateToday();
		rangeDate = new ArrayList<Date>();
		rangeDate.add(date);
		rangeDate.add(date);
		//setDateFrom(DateUtils.getDateToday());
		//setDateTo(DateUtils.getDateToday());
		init();
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully deleted", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	private ReportFields reportFields=new ReportFields();
	
	public void showPrintPdf(com.italia.municipality.lakesebu.controller.Chequedtls chk) {
		String date = chk.getDate_disbursement();
		String tmpDate = date;
		chk.setDate_disbursement(convertDateToMonthDayYear(date));
		
		chk.compileReport(chk);
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
	     
		}catch(Exception e) {}
	}
	
	public  void printReportIndividual(com.italia.municipality.lakesebu.controller.Chequedtls chk){
		String date = chk.getDate_disbursement();
		String tmpDate = date;
		chk.setDate_disbursement(convertDateToMonthDayYear(date));
		
		chk.compileReport(chk);
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
	
	/**
	 * 
	 * Removed in v3.1
	 */
	@Deprecated
	public String printReport(){
		try{
			
			String sep = File.separator;
			String REPORT_PATH = "C:" + sep + "CheckSystem" + sep + "reports" + sep;
					//FacesContext.getCurrentInstance().getExternalContext().getRealPath(sep +"resources"+sep+"reports")+sep;
			 String REPORT_NAME = "BankChequeReport";
			String JRXMLFILE="Format1";
			
			//use for printing
			setUserNotification("Please wait while application is generating pdf file for this check.");
			String _sig1 = getSig1()!=null? getSig1() : "1";
			String _sig2 = getSig2()!=null? getSig2() : "3";
			
			reportFields = new ReportFields();
			reportFields.setCheckNo(getBankCheckNo());
			reportFields.setAccntNumber(getBankCheckAccountNumber());
			reportFields.setAccntName(getBankCheckAccntName());
			reportFields.setBankName(getBankCheckName());
			
			double money = Double.valueOf(getInputAmount());
			NumberFormat format = NumberFormat.getCurrencyInstance();
			String formatted = format.format(money).replace("$", "");
			formatted = formatted.replace("Php", "");
			reportFields.setAmount((formatted));
			
			reportFields.setPayToTheOrderOf(getBankCheckPayTo());
			reportFields.setAmountInWOrds(getNumberInToWords());
			reportFields.setDate_disbursement(DateUtils.convertDate(getDateTime(),"yyyy-MM-dd"));
			Signatories sig = signatories.get(_sig1);
			reportFields.setSignatory1(sig.getName());
					  sig = signatories.get(_sig2);
			reportFields.setSignatory2(sig.getName());			
			
		compileReport(reportFields);	
			
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
	    
	        //backupPrint();
	        
		}catch(Exception ioe){
			ioe.printStackTrace();
		}
		return "Print";
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
 * Removed in v3.1
 */
@Deprecated	
private void compileReport(ReportFields reportFields){
	String sep = File.separator;
	String REPORT_PATH = "C:" + sep + "CheckSystem" + sep + "reports" + sep;
			//FacesContext.getCurrentInstance().getExternalContext().getRealPath(sep +"resources"+sep+"reports")+sep;
	 String REPORT_NAME = "BankChequeReport";
	String JRXMLFILE="Format1";
		System.out.println("CheckReport path: " + REPORT_PATH);
		HashMap paramMap = new HashMap();
		ReportFields rpt = reportFields;
		ReportCompiler compiler = new ReportCompiler();
		System.out.println("REPORT_NAME: " +REPORT_NAME + " REPORT_PATH: " + REPORT_PATH);
		String reportLocation = compiler.compileReport(JRXMLFILE, REPORT_NAME, REPORT_PATH);
		System.out.println("Check report path: " + reportLocation);
		HashMap params = new HashMap();
		
		params.put("PARAM_ACCOUNT_NUMBER", rpt.getAccntNumber());
		params.put("PARAM_CHECK_NUMBER", rpt.getCheckNo());
		params.put("PARAM_DATE_DISBURSEMENT", rpt.getDate_disbursement());
		params.put("PARAM_BANK_NAME", rpt.getBankName().toUpperCase());
		params.put("PARAM_ACCOUNT_NAME", rpt.getAccntName().toUpperCase());
		params.put("PARAM_AMOUNT", rpt.getAmount());
		params.put("PARAM_PAYTOORDEROF", rpt.getPayToTheOrderOf().toUpperCase());
		params.put("PARAM_AMOUNT_INWORDS", rpt.getAmountInWOrds().toUpperCase());
		params.put("PARAM_SIGNATORY1", rpt.getSignatory1().toUpperCase());
		params.put("PARAM_SIGNATORY2", rpt.getSignatory2().toUpperCase());
		
		
		
		JasperPrint print = compiler.report(reportLocation, params);
		File pdf = null;
		try{
		pdf = new File(REPORT_PATH+REPORT_NAME+".pdf");
		pdf.createNewFile();
		JasperExportManager.exportReportToPdfStream(print, new FileOutputStream(pdf));
		System.out.println("pdf successfully created...");
		System.out.println("Creating a backup copy....");
		pdf = new File(REPORT_PATH+REPORT_NAME+"_copy"+".pdf");
		pdf.createNewFile();
		JasperExportManager.exportReportToPdfStream(print, new FileOutputStream(pdf));
		System.out.println("Done creating a backup copy....");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
public String reportPage(){
	System.out.println("Reports called");
	return "reports";
}
private void backupPrint(){
	String sep = File.separator;
	String REPORT_PATH = "C:" + sep + "CheckSystem" + sep + "reports" + sep;
	String REPORT_PATH_FILE = "C:" + sep  + "reports" + sep;
	String REPORT_NAME = "BankChequeReport";
	String dll = "rundll32 url.dll,FileProtocolHandler";
	String fileName = REPORT_PATH + REPORT_NAME + ".pdf"; 
	File file = new File(fileName);
	try{
		if(file.exists()){
			//Process process = Runtime.getRuntime().exec(dll + " " + fileName);
			Process process = Runtime.getRuntime().exec("cmd /c start /wait " + REPORT_PATH + "copyReport.bat");
			process = Runtime.getRuntime().exec(dll + " " + REPORT_PATH_FILE + REPORT_NAME + ".pdf");
			process.waitFor();
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}

private boolean checkFields(){
	boolean isFields=false;
	String msg="",msg1="",msg2="",msg3="",msg4="",msg5="",msg6="",msg7="",_sig1="",_sig2="";
	msg1 =  !getBankCheckAccountNumber().isEmpty()? "" : "Account Number ";
	msg2 = !getBankCheckAccntName().isEmpty()? "" : "Account Name ";
	msg3 = !getBankCheckName().isEmpty()? "" : "Branch Name ";
	msg4 = !getBankCheckDate().isEmpty()? "" : "Date ";
	msg5 = !getBankCheckAmount().isEmpty()? "" : "Amount ";
	msg6 = !getBankCheckPayTo().isEmpty()? "" : "PAY TO THE ORDER OF ";
	msg7 = !getBankCheckInWords().isEmpty()? "" : "AMOUNT IN WORDS (Php) ";
	_sig1 = !getSig1().isEmpty()? "" : "Signatory 1 ";
	_sig2 = !getSig2().isEmpty()? "" : "Signatory 2 ";
	msg = (!msg1.isEmpty()? msg1+"," : "") + (!msg2.isEmpty()? msg2+"," : "") + (!msg3.isEmpty()? msg3+"," : "") + (!msg4.isEmpty()? msg4+"," : "") + (!msg5.isEmpty()? msg5+"," : "") + (!msg6.isEmpty()? msg6+"," : "") + (!msg7.isEmpty()? msg7+"," : "") + (!_sig1.isEmpty()? _sig1+"," : "") +  _sig2;
	
	if(!"".equalsIgnoreCase(msg)){
		setUserNotification("Please provide details: " + msg);
		isFields=true;
	}
	
	return isFields;
}


public void monthQuery(String name) {
	System.out.println("click month : "+name);
	String dateFrom = "";
	String dateTo = "";
	String year = DateUtils.getCurrentYear()+"";
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
	
	
	cheques = java.util.Collections.synchronizedList(new ArrayList<com.italia.municipality.lakesebu.controller.Chequedtls>());
	com.italia.municipality.lakesebu.controller.Chequedtls chk = new com.italia.municipality.lakesebu.controller.Chequedtls();
	String sql = "SELECT * FROM tbl_chequedtls WHERE isactive=1 AND (date_disbursement>=? AND date_disbursement<=? ) ORDER BY date_edited";
	String[] params = new String[2];
	params[0] = dateFrom;
	params[1] = dateTo;
	
	
	double amount = 0d;
	for(com.italia.municipality.lakesebu.controller.Chequedtls chq : chk.retrieve(sql, params)){
		if(chq.getStatus()==1){
			amount += chq.getAmount();
		}
		chq.setAccntNumber(bankAccountNum(chq.getAccntNumber()));
		cheques.add(chq);
	}
	setGrandTotal(formatAmount(amount+""));
	
}

public String userpage(){
	return "userMaintenance";
}
	public List<com.italia.municipality.lakesebu.controller.Chequedtls> getCheques() {
	return cheques;
}
public void setCheques(List<com.italia.municipality.lakesebu.controller.Chequedtls> cheques) {
	this.cheques = cheques;
}
	public String getSearchPayTo() {
	return searchPayTo;
}
public void setSearchPayTo(String searchPayTo) {
	this.searchPayTo = searchPayTo;
}

public String getSearchBankAccountId() {
	return searchBankAccountId;
}
public void setSearchBankAccountId(String searchBankAccountId) {
	this.searchBankAccountId = searchBankAccountId;
}
public List getAccountNameList() {
	accountNameList = new ArrayList<>();
	for(BankAccounts a : accounts.values()){
		accountNameList.add(new SelectItem(a.getBankId()+"",a.getBankAccntName() + " " + a.getBankAccntBranch()));
	}
	
	return accountNameList;
}
public void setAccountNameList(List accountNameList) {
	this.accountNameList = accountNameList;
}
	public com.italia.municipality.lakesebu.controller.Chequedtls getChequedtlsData() {
	return chequedtlsData;
}
public void setChequedtlsData(com.italia.municipality.lakesebu.controller.Chequedtls chequedtlsData) {
	this.chequedtlsData = chequedtlsData;
}
	public String getGrandTotal() {
	return grandTotal;
}

public void setGrandTotal(String grandTotal) {
	this.grandTotal = grandTotal;
}

public void find(){
			//This will execute first in this class
			//retrieve the details of signatory in the database
			getSignatories();
			getAccounts();
			
			/*
			 * Version v3.1 start
			 */
			cheques = new ArrayList<com.italia.municipality.lakesebu.controller.Chequedtls>();
			com.italia.municipality.lakesebu.controller.Chequedtls chk = new com.italia.municipality.lakesebu.controller.Chequedtls();
			String sql = "SELECT * FROM tbl_chequedtls WHERE isactive=1 ";
			String[] params = new String[0];
			
			
			int cnt = 0;
			
			if(getSearchBankAccountId()!=null && !getSearchBankAccountId().isEmpty()){
				cnt++; 
			}
			
			if(getSearchPayTo()!=null && !getSearchPayTo().isEmpty()){
				
				int checkno = 0;
				
				try{
					//checker only
					checkno = Integer.valueOf(getSearchPayTo());
					cnt++;
				}catch(NumberFormatException e){
					
				}
				
				//cnt++;
			}
			
			
			if(getRangeDate()!=null && getRangeDate().size()>1) {
				cnt+=2;
			}else {
				cnt++;
			}
			
			params = new String[cnt];
			int x = 0;
			if(getSearchBankAccountId()!=null && !getSearchBankAccountId().isEmpty()){
				sql += " AND accnt_no=?";
				params[x] = getSearchBankAccountId();
				x++;
			}
			
			if(getSearchPayTo()!=null && !getSearchPayTo().isEmpty()){
				
				int checkno = 0;
				
				try{
					//checker only
					checkno = Integer.valueOf(getSearchPayTo());
					
					sql += " AND cheque_no=?";
					params[x] = getSearchPayTo().trim();
					x++;
				}catch(NumberFormatException e){}
				
				if(checkno==0){
					
					String val = getSearchPayTo().replace("--", "").trim();
					
					if("RECEIVED".equalsIgnoreCase(val)){
						sql += " AND chkstatus=1";
					}else if("CANCELLED".equalsIgnoreCase(val)){
						sql += " AND chkstatus=2";
					}else{
						sql += " AND pay_to_the_order_of like '%"+val+"%'";
					}
					
				}
				
			}
			
			if(getRangeDate()!=null && getRangeDate().size()>1) {
				sql += " AND (date_disbursement>=? AND date_disbursement<=? )";
				params[x] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");//convertDateToMonthDayYear(getDateFrom());
				x++;
				params[x] = DateUtils.convertDate(getRangeDate().get(1),"yyyy-MM-dd");//convertDateToMonthDayYear(getDateTo());
				
			}else {
				sql += " AND date_disbursement=?";
				params[x] = DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd");// convertDateToMonthDayYear(getDateTo());
				x++;
			}
			
			
			if(cnt==0){
				params = new String[1];
				sql += " AND date_disbursement=? ORDER BY date_edited LIMIT 10";
				params[0] =  DateUtils.getCurrentDateYYYYMMDD();//convertDateToMonthDayYear(DateUtils.getCurrentDateMMMMDDYYYY());
			}else{
				sql += " ORDER BY date_edited";
			}
			double amount = 0d;
			for(com.italia.municipality.lakesebu.controller.Chequedtls chq : chk.retrieve(sql, params)){
				if(chq.getStatus()==1){
					amount += chq.getAmount();
				}
				chq.setAccntNumber(bankAccountNum(chq.getAccntNumber()));
				cheques.add(chq);
			}
			setGrandTotal(formatAmount(amount+""));
			java.util.Collections.reverse(cheques);
			
			
			/*
			 * Version v3.1 end
			 */
			
			if(getSig1()==null || "".equalsIgnoreCase(getSig1())){
				setSig1Label("Please Select...");
			}
			if(getSig2()==null || "".equalsIgnoreCase(getSig2())){
				setSig2Label("Please Select...");
			}
			if(getBankCheckAccountNumber()==null || "".equalsIgnoreCase(getBankCheckAccountNumber())){
				setAccountLabel("Please Select...");
			}
			
			
}

public void changeTableSize() {
	PrimeFaces pf = PrimeFaces.current();
	System.out.println("getButtonSizeName()" + getButtonSizeName());
	String scs = "";
	
	if("Expand Table".equalsIgnoreCase(getButtonSizeName())) {
		scs = "$('#idLeft').hide(1000);";
		scs += "$('#idCenter').removeClass('col-5');";
		scs +="$('#idCenter').addClass('col-12');";
		setButtonSizeName("Minimize Table");
	}else {
		setButtonSizeName("Expand Table");
		scs = "$('#idLeft').show(1000);";
		scs += "$('#idCenter').removeClass('col-12');";
		scs +="$('#idCenter').addClass('col-5');";
	}
	
	pf.executeScript(scs);
}

public void loadGraph() {
	System.out.println("loading graph default value");
	//dummyBarModel();
	selectedYear = new ArrayList<>();
	years = new ArrayList<>();
	for(int year=2021; year<=DateUtils.getCurrentYear(); year++) {
		years.add(new SelectItem(year, year+""));
	}
}

public void dummyBarModel() {
    barModel = new BarChartModel();
    ChartData data = new ChartData();

    BarChartDataSet barDataSet = new BarChartDataSet();
    barDataSet.setLabel("Year 1");
    barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
    barDataSet.setBorderColor("rgb(255, 99, 132)");
    barDataSet.setBorderWidth(1);
    List<Number> values = new ArrayList<>();
    values.add(65);
    values.add(59);
    values.add(80);
    values.add(81);
    values.add(56);
    values.add(55);
    values.add(40);
    barDataSet.setData(values);

    BarChartDataSet barDataSet2 = new BarChartDataSet();
    barDataSet2.setLabel("Year 2");
    barDataSet2.setBackgroundColor("rgba(255, 159, 64, 0.2)");
    barDataSet2.setBorderColor("rgb(255, 159, 64)");
    barDataSet2.setBorderWidth(1);
    List<Number> values2 = new ArrayList<>();
    values2.add(85);
    values2.add(69);
    values2.add(20);
    values2.add(51);
    values2.add(76);
    values2.add(75);
    values2.add(10);
    barDataSet2.setData(values2);
    

    data.addChartDataSet(barDataSet);
    data.addChartDataSet(barDataSet2);

    List<String> labels = new ArrayList<>();
    labels.add("January");
    labels.add("February");
    labels.add("March");
    labels.add("April");
    labels.add("May");
    labels.add("June");
    labels.add("July");
    data.setLabels(labels);
    barModel.setData(data);

    //Options
    BarChartOptions options = new BarChartOptions();
    CartesianScales cScales = new CartesianScales();
    CartesianLinearAxes linearAxes = new CartesianLinearAxes();
    linearAxes.setOffset(true);
    CartesianLinearTicks ticks = new CartesianLinearTicks();
    //ticks.setBeginAtZero(true);
    linearAxes.setTicks(ticks);
    cScales.addYAxesData(linearAxes);
    options.setScales(cScales);

    Title title = new Title();
    title.setDisplay(true);
    title.setText("Bar Chart");
    options.setTitle(title);
    
    
    Legend legend = new Legend();
    legend.setDisplay(true);
    legend.setPosition("bottom");
    LegendLabel legendLabels = new LegendLabel();
    //legendLabels.setFontStyle("bold");
    legendLabels.setFontColor("#2980B9");
    legendLabels.setFontSize(12);
    legend.setLabels(legendLabels);
    options.setLegend(legend);
    
	/*
	 * Tooltip tooltip = new Tooltip(); tooltip.setEnabled(true);
	 * tooltip.setMode("index"); tooltip.setIntersect(false);
	 * options.setTooltip(tooltip);
	 */
    
    barModel.setOptions(options);
}

public void loadBar() {
	System.out.println("Selected year: " + getSelectedYear().toString());
	barModel = new BarChartModel();
    ChartData data = new ChartData();
    int color = 1;
    boolean isMorethanOneYearSelected=false;
    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
    	isMorethanOneYearSelected=true;
    }
    Map<Integer,Map<Integer, Double>> mapYear = new LinkedHashMap<Integer, Map<Integer, Double>>();
    Map<Integer, Double> mapMonth = new LinkedHashMap<Integer, Double>();
    
    List<String> labels = new ArrayList<>();
    BarChartDataSet barDataSet = new BarChartDataSet();
	List<Number> values = new ArrayList<>();
    
    for(Object o : getSelectedYear()) {
    	int year = Integer.valueOf(o.toString());
    	Map<Integer, Double> years= com.italia.municipality.lakesebu.controller.Chequedtls.getByMonth(year);
    	for(int month : years.keySet()) {
    		values.add(years.get(month));
    		barDataSet.setLabel(DateUtils.getMonthName(month));
    	}
    	barDataSet.setData(values);
	    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
	    barDataSet.setBorderColor(GraphColor.valueId(color));
	    barDataSet.setBorderWidth(1);
        data.addChartDataSet(barDataSet);
    	color++;
    }
 
    
    
}


public String getKeyPress() {
	keyPress = "findId";
	System.out.println("Im searching.... keypress");
	return keyPress;
}

public void setKeyPress(String keyPress) {
	this.keyPress = keyPress;
}

	public byte[] getReportBytesCheque() {
	return reportBytesCheque;
}

public void setReportBytesCheque(byte[] reportBytesCheque) {
	this.reportBytesCheque = reportBytesCheque;
}

public List getStatusList() {
	
	statusList = new ArrayList<>();
	statusList.add(new SelectItem(1,"RECEIVED"));
	statusList.add(new SelectItem(2,"CANCELLED"));
	
	return statusList;
}

public void setStatusList(List statusList) {
	this.statusList = statusList;
}

public int getStatusId() {
	if(statusId==0){
		statusId = 1;
	}
	return statusId;
}

public void setStatusId(int statusId) {
	this.statusId = statusId;
}

public String getRemarks() {
	return remarks;
}

public void setRemarks(String remarks) {
	this.remarks = remarks;
}

	public boolean isEnableRemarks() {
	return enableRemarks;
}

public void setEnableRemarks(boolean enableRemarks) {
	this.enableRemarks = enableRemarks;
}

	public String getNatureOfPayment() {
	return natureOfPayment;
}

public void setNatureOfPayment(String natureOfPayment) {
	this.natureOfPayment = natureOfPayment;
}



	public static void main(String[] args) {
		
		CheckBean b = new CheckBean();
		b.convertDateToYearMontyDay(DateUtils.getCurrentDateMMMMDDYYYY());
		
	}

	public List<Date> getRangeDate() {
		return rangeDate;
	}

	public void setRangeDate(List<Date> rangeDate) {
		this.rangeDate = rangeDate;
	}

	public String getButtonSizeName() {
		return buttonSizeName;
	}

	public void setButtonSizeName(String buttonSizeName) {
		this.buttonSizeName = buttonSizeName;
	}
}

