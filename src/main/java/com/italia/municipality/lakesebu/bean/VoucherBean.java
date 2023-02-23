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
import java.util.List;
import java.util.Locale;

import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.Voucher;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.TransactionType;
import com.italia.municipality.lakesebu.global.GlobalVar;
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
import lombok.Data;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author mark italia
 * @since 02/10/2017
 * @version 1.0
 *
 */
@Named
@ViewScoped
@Data
public class VoucherBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877904552413546L;

	private List accountNameList;
	private int accountNameId;
	private String searchParticulars;
	private List<Date> rangeDate;
	private List<Voucher> trans;
	private Voucher selectedData;
	private int transId;
	private List transType;
	private String total;
	private int departmentId;
	private List departments;
	
	public void welcomeListener() {
	    Application.addMessage(1, "Welcome Back", "Continue your works.");
	}
	
	@PostConstruct
	public void init(){
		
		if(transId==0){
			transId = TransactionType.CHECK_ISSUED.getId();
		}
		
		transType = new ArrayList<>();
		transType.add(new SelectItem(TransactionType.CHECK_ISSUED.getId(), TransactionType.CHECK_ISSUED.getName()));
		
		departments = new ArrayList<>();
		for(Department dep : Department.retrieve("SELECT * FROM department order by departmentname", new String[0])){
			departments.add(new SelectItem(dep.getDepid(), dep.getDepartmentName()));
		}
		
		String dateFrom = DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd",DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		String dateTo = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd",DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN); 
		
		if(rangeDate==null || rangeDate.size()==0) {
			rangeDate = new ArrayList<Date>();
			rangeDate.add(DateUtils.convertDateString(dateFrom, "yyyy-MM-dd"));
			rangeDate.add(DateUtils.convertDateString(dateTo, "yyyy-MM-dd"));
		}else {
			
			if(rangeDate!=null && rangeDate.size()>1) {
				dateFrom = DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd");
				dateTo = DateUtils.convertDate(getRangeDate().get(1), "yyyy-MM-dd");
			}else {
				dateFrom = DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd");
				dateTo = dateFrom;
			}
		}
		
		String sql = "SELECT * FROM voucher WHERE ";
		
		String[] params = new String[3];
		
		if(getAccountNameId()==0){
			params = new String[2];
			params[0] = dateFrom;
			params[1] = dateTo;
			sql +="vDate>=? AND vDate<=?"; 
		}else{
			params[0] = dateFrom;
			params[1] = dateTo;
			params[2] = getAccountNameId()+"";
			sql +="vDate>=? AND vDate<=? AND bank_id=? ";
		}
		
		int checkNo = 0;
				try{checkNo = Integer.valueOf(getSearchParticulars());}catch(Exception e){}
		if(checkNo!=0){
			sql += " AND checkno=" + getSearchParticulars().replace("--", "");
		}else{
			
		
		if(getSearchParticulars()!=null && !getSearchParticulars().isEmpty()){
			sql += " AND vpayee like '%" + getSearchParticulars().replace("--", "") + "%'";
		}
		
		}
		
		sql +=" ORDER BY checkno";
		
		trans = new ArrayList<Voucher>();
		int cnt = 1;
		double amount=0d;
		for(Voucher tran : Voucher.retrieve(sql, params)){
			tran.setCnt(cnt++);
			tran.setDAmount(Currency.formatAmount(tran.getAmount()));
			tran.setTransactionName(TransactionType.nameId(tran.getTransType()));
			Department dep = null;
			try{dep = Department.retrieve("SELECT * FROM department WHERE departmentid=" + tran.getDepartment().getDepid(), new String[0]).get(0);
			tran.setDepartmentCode(dep.getCode());
			tran.setDepartmentName(dep.getDepartmentName());
			tran.setDepartment(dep);
			}catch(IndexOutOfBoundsException e){tran.setDepartmentCode("");}
			
			BankAccounts acc = null;
			try{acc = acc.retrieve("SELECT * FROM tbl_bankaccounts WHERE bank_id="+tran.getAccounts().getBankId(), new String[0]).get(0);}catch(Exception  e){}
			if(acc!=null){
				tran.setAccounts(acc);
			}else{
				tran.setAccounts(new BankAccounts());
			}
			
			trans.add(tran);
			amount += tran.getAmount();
		}
		
		if(trans.size()==0){ //load default data if there is no data retrieve
			Voucher tran = cashField();
			tran.setCnt(cnt);
			trans.add(tran);
		}
		
		//Collections.reverse(trans);
		setTotal(Currency.formatAmount(amount));
	}
	
	public void onCellEdit(CellEditEvent event) {
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue());
        System.out.println("Index   "+ event.getRowIndex());
        int index = event.getRowIndex();
        
        if(getDepartmentId()!=0){
        	Department department = Department.retrieve("SELECT * FROM department WHERE departmentid="+ getDepartmentId(), new String[0]).get(0);
        	trans.get(index).setDepartment(department);
        	trans.get(index).setDepartmentName(department.getDepartmentName());
        	trans.get(index).setDepartmentCode(department.getCode());
        }
        
        
        saveChanges(index);
        
	}
	
	private void saveChanges(int index){
		 if(trans.size()>0){
			
			 Voucher tran = trans.get(index);
			 tran.setUserDtls(Login.getUserLogin().getUserDtls());
			 tran.save();
			 
			 xmlSave(tran);
		 }
	 }
	
	private void xmlSave(Voucher v){
		try{
		System.out.println("saving xml...");	
		CheckXML xml = new CheckXML();
		xml.setDateTrans(v.getDateTrans());
		xml.setCheckNo(v.getCheckNo());
		xml.setIsActive(v.getIsActive());
		xml.setCreditAmount(v.getAmount());
		xml.setUserDtls(Login.getUserLogin().getUserDtls());
		xml.setTransactionType(TransactionType.CHECK_ISSUED.getId());
		
		xml.setPayee(v.getPayee());
		xml.setNaturePayment(v.getNaturePayment());
		xml.setVoucherNo("");
		xml.setOrNumber("");
		
		xml.setDepartment(v.getDepartment());
		xml.setAccounts(v.getAccounts());
		BookCheck.createXML(xml, v.getCheckNo()+".xml");
		System.out.println("xml successfully saved...");
		}catch(Exception e){}
	}
	
	public void addNew(){
		int cnt = 0;
		Voucher tran = cashField();
		if(trans!=null && trans.size()>0){
			cnt = trans.size() + 1;
			try{tran.setDateTrans(trans.get(trans.size()-1).getDateTrans());}catch(IndexOutOfBoundsException e){}
		}
		
		tran.setCnt(cnt);
		setTransId(0);
		trans.add(tran);
	}
	 
	private Voucher cashField(){
		
		Voucher cash = new Voucher();
		cash.setDateTrans(DateUtils.getCurrentDateYYYYMMDD());
		cash.setPayee("payee");
		cash.setNaturePayment("nature of payment");
		cash.setCheckNo("");
		cash.setIsActive(1);
		cash.setTransType(TransactionType.CHECK_ISSUED.getId());
		cash.setTransactionName(TransactionType.CHECK_ISSUED.getName());
		cash.setAmount(0);
		cash.setDAmount("0");
		BankAccounts accounts = new BankAccounts();
		accounts.setBankId(getAccountNameId());
		cash.setAccounts(accounts);
		return cash;
	}
	
	public List getAccountNameList() {
		Login user = Login.getUserLogin();
		accountNameList = new ArrayList<>();
		for(BankAccounts a : BankAccounts.retrieve("SELECT * FROM tbl_bankaccounts", new String[0])){
			accountNameList.add(new SelectItem(a.getBankId(),a.getBankAccntName() + " " + a.getBankAccntBranch()));
			
		}
		return accountNameList;
	}
	
	public void print(){ //check issued only
		if(trans.size()>0){
			List<Reports> reports = new ArrayList<Reports>();
			double total = 0d;
			for(Voucher tran : trans){
				if(tran.getTransType()==TransactionType.CHECK_ISSUED.getId()){
					Reports rpt = new Reports();
					rpt.setF1(tran.getDateTrans());
					rpt.setF2(tran.getCheckNo().equalsIgnoreCase("0")? "" : tran.getCheckNo());
					//rpt.setF4(tran.getDepartmentCode());
					rpt.setF4(tran.getDepartmentName());
					rpt.setF5(tran.getPayee());
					rpt.setF6(tran.getNaturePayment());
					rpt.setF7(tran.getDAmount());
					reports.add(rpt);
					total += tran.getAmount();
				}
			}
			
			String dateFrom = DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd",DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
			String dateTo = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd",DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN); 
			
			if(getRangeDate()!=null && getRangeDate().size()>1) {
				dateFrom = DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd");
				dateTo = DateUtils.convertDate(getRangeDate().get(1), "yyyy-MM-dd");
			}else {
				dateFrom = DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd");
				dateTo = dateFrom;
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
	  		param.put("PARAM_RANGE_DATE",dateFrom + " to " + dateTo);
	  		
	  		if(getAccountNameId()==0){
		  		param.put("PARAM_ACCOUNT_NAME","All Accounts");
	  		}else{
		  		BankAccounts accnt = BankAccounts.retrieve("SELECT * FROM tbl_bankaccounts WHERE bank_id=" + getAccountNameId(), new String[0]).get(0);
		  		param.put("PARAM_ACCOUNT_NAME","Bank Name/Account No. "+accnt.getBankAccntNo() + "-" + accnt.getBankAccntBranch());
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