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
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.ReorderEvent;
import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.CheckIssued;
import com.italia.municipality.lakesebu.controller.CheckIssuedReport;
import com.italia.municipality.lakesebu.controller.CheckRpt;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @since 07/26/2022
 * @version 1.0
 *
 */
@Named("chkIssued")
@ViewScoped
@Data
public class CheckIssuedReportBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 456879769769761L;
	private List funds;
	private CheckIssuedReport cashDvData;
	private List<CheckIssuedReport> rpts;
	private String searchParam;
	private boolean enabledTable;
	private Map<Integer, BankAccounts> mapBankAccounts;
	private List pageSizes;
	private Login userLogin;
	
	@PostConstruct
	public void init() {
		mapBankAccounts = new LinkedHashMap<Integer, BankAccounts>();
		funds = new ArrayList<>();
		for(BankAccounts a : BankAccounts.retrieveAll()) {
			funds.add(new SelectItem(a.getBankId(), a.getBankAccntName() + " " + a.getBankAccntNo()));
			mapBankAccounts.put(a.getBankId(), a);
		}
		
		pageSizes = new ArrayList<>();
		pageSizes.add(new SelectItem(0, "Short"));
		pageSizes.add(new SelectItem(1, "Long"));
		pageSizes.add(new SelectItem(2, "Xtra-Long"));
		
		setUserLogin(Login.getUserLogin());
		defaultValue();
		load();
	}
	
	private void defaultValue() {
		BankAccounts acc = getMapBankAccounts().get(2);
		CheckIssuedReport r = CheckIssuedReport.builder()
				.dateReportTmp(new Date())
				.bankAccountNo(acc.getBankAccntBranch() + " " + acc.getBankAccntNo())
				.disbursingOfficer(Words.getTagName("treasurer-name"))
				.designation(Words.getTagName("official-designation"))
				.receivingOfficer(Words.getTagName("receiving-person"))
				.receivePosition(Words.getTagName("receiving-position"))
				.build();
		
		List<CheckIssued> dvs = new ArrayList<CheckIssued>();
		dvs.add(CheckIssued.builder().number(1).build());
		setEnabledTable(false);
		r.setRpts(dvs);
		
		setCashDvData(r);
	}
	
	public void clickItemRpt(CheckIssuedReport rpt) {
		rpt.setDateReportTmp(DateUtils.convertDateString(rpt.getDateReport(), "yyyy-MM-dd"));
		
		rpt.setRpts(CheckIssued.retrieveReportGroupCheckIssued(rpt.getId()));
		if(rpt.getRpts().size()==0) {
			List<CheckIssued> dvs = new ArrayList<CheckIssued>();
			dvs.add(CheckIssued.builder().number(1).build());
			rpt.setRpts(dvs);
		}
		setEnabledTable(true);
		setCashDvData(rpt);
	}
	
	public void deleteItem(CheckIssued ck) {
		ck.delete();
		getCashDvData().getRpts().remove(ck);
	}
	
	public void print(CheckIssuedReport rpt) {
		String REPORT_PATH = GlobalVar.REPORT_FOLDER;
		String REPORT_NAME = GlobalVar.CHECK_ISSUED_REPORT_SHORT_NAME;
		int startSeries=0,endSeries=0;
		List<CheckRpt> reports = new ArrayList<CheckRpt>();
		List<CheckIssued> cashDisList = CheckIssued.retrieveReportGroupCheckIssued(rpt.getId());
		
		//testing purposes only
		/**List<CheckIssued> cashDisList = new ArrayList<CheckIssued>();
		for(int x=1; x<=47; x++) {
			cashDisList.add(
					CheckIssued.builder()
					.number(x)
					.serialNo(76026822)
					.dvPayroll("101-22-05-1717")
					.cafoaNo("101-22-043249")
					.centerOffice("1299")
					.payee("El galong lake resort and accommodation center")
					.naturePay("Loan payment JO")
					.dateTrans("12/29/2022")
					.amount(1000 + x)
					.build()
					);
		}*/
		
		
		//int count = 0;
		double amount = 0d;
		String payeeNature="\t-";
		List<Integer> series = new ArrayList<Integer>();
		//if(cashDisList.size()<37) {
		if(rpt.getPageSize()==0) {	//short page
			for(CheckIssued cs : cashDisList) {
				//if(count==0) {startSeries=cs.getSerialNo();}else {endSeries=cs.getSerialNo();}
				
				if(cs.getSerialNo()>0) {
					series.add(cs.getSerialNo());
				}
				
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee()==null? payeeNature : cs.getPayee())
						.f5(cs.getNaturePay()==null? payeeNature : cs.getNaturePay())
						.f6(cs.getAmount()==0? "-" : Currency.formatAmount(cs.getAmount()))
						.f9(cs.getSerialNo()==0? "-" : cs.getSerialNo()+"")
						.f10(cs.getCenterOffice())
						.build();
				
				reports.add(cpt);
				//count++;
				amount += cs.getAmount();
			}
			/*
			count = 36 - count;
			for(int i=1; i<=count; i++) {
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1("")
						.f2("")
						.f3("")
						.f4("")
						.f5("")
						.f6("")
						.build();
				reports.add(cpt);
			}*/
			
			//total
			CheckRpt cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
		//}else if(cashDisList.size()>37 && cashDisList.size()<48){//if more than 37 long
		}else if(rpt.getPageSize()==1) {//long page	
			REPORT_NAME = GlobalVar.CHECK_ISSUED_REPORT_LONG_NAME;
			for(CheckIssued cs : cashDisList) {
				//if(count==0) {startSeries=cs.getSerialNo();}else {endSeries=cs.getSerialNo();}
				
				if(cs.getSerialNo()>0) {
					series.add(cs.getSerialNo());
				}
				
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee()==null? payeeNature : cs.getPayee())
						.f5(cs.getNaturePay()==null? payeeNature : cs.getNaturePay())
						.f6(cs.getAmount()==0? "-" : Currency.formatAmount(cs.getAmount()))
						.f9(cs.getSerialNo()==0? "-" : cs.getSerialNo()+"")
						.f10(cs.getCenterOffice())
						.build();
				
				if(cs.getSerialNo()==0) {
					cpt.setF9("-");
				}
				
				if(cs.getAmount()==0) {//modify if zero
					cpt.setF6("-");
				}
				
				reports.add(cpt);
				//count++;
				amount += cs.getAmount();
			}
			/**
			count = 47 - count;
			for(int i=1; i<=count; i++) {
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1("")
						.f2("")
						.f3("")
						.f4("")
						.f5("")
						.f6("")
						.build();
				reports.add(cpt);
			}
			*/
			//total
			CheckRpt cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
		}else {//extra long page
			REPORT_NAME = GlobalVar.CHECK_ISSUED_REPORT_LONG_EXTENDED_NAME;
			for(CheckIssued cs : cashDisList) {
				//if(count==0) {startSeries=cs.getSerialNo();}else {endSeries=cs.getSerialNo();}
				
				if(cs.getSerialNo()>0) {
					series.add(cs.getSerialNo());
				}
				
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee()==null? payeeNature : cs.getPayee())
						.f5(cs.getNaturePay()==null? payeeNature : cs.getNaturePay())
						.f6(cs.getAmount()==0? "-" : Currency.formatAmount(cs.getAmount()))
						.f9(cs.getSerialNo()==0? "-" : cs.getSerialNo()+"")
						.f10(cs.getCenterOffice())
						.build();
				
				if(cs.getSerialNo()==0) {
					cpt.setF9("-");
				}
				
				if(cs.getAmount()==0) {//modify if zero
					cpt.setF6("-");
				}
				
				reports.add(cpt);
				//count++;
				amount += cs.getAmount();
			}
			/*
			count = 58 - count;
			for(int i=1; i<=count; i++) {
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1("")
						.f2("")
						.f3("")
						.f4("")
						.f5("")
						.f6("")
						.build();
				reports.add(cpt);
			}
			*/
			//total
			CheckRpt cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
		}
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		if(series!=null && series.size()>0) {
			startSeries = series.get(0);
			endSeries = series.get(series.size()-1);
		}
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		
		param.put("PARAM_TITLE", "REPORT OF CHECK ISSUED");
		param.put("PARAM_SUB_TITLE", rpt.getPeriodCovered());
		param.put("PARAM_LGU_NAME", Words.getTagName("lgu-name"));
		param.put("PARAM_FUND_NAME", getMapBankAccounts().get(rpt.getFundId()).getBankAccntName());
		param.put("PARAM_REPORT_NO", rpt.getReportNo());
		param.put("PARAM_SHEET_NO", rpt.getSheetNo());
		param.put("PARAM_RECEIVED_BY", rpt.getReceivingOfficer());
		param.put("PARAM_RECEIVED_POSITION", rpt.getReceivePosition());
		param.put("PARAM_DISBURSING_OFFICER", rpt.getDisbursingOfficer());
		param.put("PARAM_DISBURSING_POSITION", rpt.getDesignation());
		param.put("PARAM_DATE", rpt.getDateReport());
		param.put("PARAM_BANK_ACCOUNT", rpt.getBankAccountNo());
		param.put("PARAM_CHECK_NOS", startSeries + " to " + endSeries);
		//logo
		//String officialLogo = REPORT_PATH + "logo.png";
		//try{File file = new File(officialLogo);
		//FileInputStream off = new FileInputStream(file);
		//param.put("PARAM_LOGO", off);
		//}catch(Exception e){e.printStackTrace();}
		
		//logo
		//String officialLogotrans = REPORT_PATH + "logotrans.png";
		//try{File file = new File(officialLogotrans);
		//FileInputStream off = new FileInputStream(file);
		//param.put("PARAM_LOGO_TRANS", off);
		//}catch(Exception e){e.printStackTrace();}
  		
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
	
	public void deleteRpt(CheckIssuedReport rpt) {
		rpt.delete();
		CheckIssued.delete("UPDATE checkissued SET isactiveck=0 WHERE ckid="+ rpt.getId(), new String[0]);//only updating not deleting the data
		rpts.remove(rpt);
		clear();
	}
	
	public void clear() {
		defaultValue();
	}
	
	/*
	 * public void deleteItem(CashDisbursement item) { item.delete();
	 * getCashDvData().getRpts().remove(item); }
	 */
	
	public void addItem() {
		int count = getCashDvData().getRpts().size() + 1;
		int index = count==0? 0 : count-2;
		if(getCashDvData().getRpts().get(index).getId()>0) {
			CheckIssued cash = getCashDvData().getRpts().get(index);//get the latest data info
			getCashDvData().getRpts().add(
					CheckIssued.builder()
					.number(count)
					.dateTrans("-DO-")
					.serialNo(cash.getSerialNo()+1)
					.dvPayroll(newDV(cash))
					.cafoaNo(cash.getCafoaNo())
					.centerOffice(cash.getCenterOffice())
					.naturePay(cash.getNaturePay())
					.payee(cash.getPayee())
					.amount(cash.getAmount())
					.build()
					);
			setEnabledTable(true);
		}
	}
	
	@Deprecated
	private String dvSeriesGen(CheckIssued cash) {
		
		if(cash.getDvPayroll().contains("-")) {
			
			String[] dv = cash.getDvPayroll().split("-");
			int number = 0;
			
			try{number = Integer.valueOf(dv[3]);}catch(NumberFormatException num) {}
			
			number += 1;//increment by 1
			
			return dv[0] + "-" + dv[1] + "-" + dv[2] + "-" + number;
			
		}
		
		
		return cash.getDvPayroll();
	}
	
	private String newDV(CheckIssued cash) {
		String val = cash.getDvPayroll();
		String dv="";
		
		if(val.contains("-")) {
			
			String[] dvs = val.split("-");
			int len = dvs[3].length();
			int series = Integer.valueOf(dvs[3].substring(1,len));
			series += 1;
			
			String v = "";
			try {Integer.valueOf(dvs[3].substring(0, 1)); series = Integer.valueOf(dvs[3]); series += 1;}catch(Exception e){v=dvs[3].substring(0, 1);}
			
			dv = dvs[0] + "-" + dvs[1] + "-" + dvs[2] + "-" + v + series;
			return dv;
			
		}
		
		return val;
	}
	
	public void updateDate() {
		if(getCashDvData()!=null && getCashDvData().getId()!=0) {
			CheckIssuedReport rpt = getCashDvData();
			rpt.setDateReport(DateUtils.convertDate(rpt.getDateReportTmp(), "yyyy-MM-dd"));
			rpt.save();
		}
	}
	
	public void updateInfo() {
		if(getCashDvData()!=null && getCashDvData().getId()!=0) {
			BankAccounts acc = getMapBankAccounts().get(getCashDvData().getFundId());
			getCashDvData().setBankAccountNo(acc.getBankAccntBranch() + " " + acc.getBankAccntNo());
			System.out.println("Bank: "+ acc.getBankAccntBranch() + " " + acc.getBankAccntNo());
			getCashDvData().save();
			load();
		}
	}
	
	public void changeFund() {
		if(getCashDvData()!=null && getCashDvData().getId()!=0) {
			BankAccounts acc = getMapBankAccounts().get(getCashDvData().getFundId());
			getCashDvData().setBankAccountNo(acc.getBankAccntBranch() + " " + acc.getBankAccntNo());
			System.out.println("Bank: "+ acc.getBankAccntBranch() + " " + acc.getBankAccntNo());
			getCashDvData().save();
			load();
		}else {
			BankAccounts acc = getMapBankAccounts().get(getCashDvData().getFundId());
			getCashDvData().setBankAccountNo(acc.getBankAccntBranch() + " " + acc.getBankAccntNo());
			System.out.println("Bank: "+ acc.getBankAccntBranch() + " " + acc.getBankAccntNo());
		}
	}
	
	public void autoSave() {
		
	}
	
	/**
	 * See create item
	 */
	@Deprecated
	public void save() {
		boolean isOk = true;
		CheckIssuedReport cashDv = getCashDvData();
		
		if(cashDv.getPeriodCovered()==null) {
			Application.addMessage(2, "Warning", "Please provide period covered details");
			isOk = false;
		}
		
		if(cashDv!=null && cashDv.getRpts().size()==0) {
			Application.addMessage(2, "Warning", "Please provide voucher details");
			isOk = false;
		}
		
		if(isOk) {
			cashDv.setDateReport(DateUtils.convertDate(cashDv.getDateReportTmp(), "yyyy-MM-dd"));
			cashDv.setIsActive(1);
			cashDv = CheckIssuedReport.save(cashDv);
			
			List<CheckIssued> items = new ArrayList<CheckIssued>();
			for(CheckIssued r : cashDv.getRpts()) {
				r.setIsActive(1);
				r.setCheckIssuedReport(cashDv);
				r = CheckIssued.save(r);
				items.add(r);
			}
			cashDv.setRpts(items);
			Application.addMessage(1, "Success", "Successfully saved.");
		}
	}
	
	
	
	public void load() {
		rpts = new ArrayList<CheckIssuedReport>();
		String[] params = new String[0];
		String sql = "";
		
		if(getUserLogin().getAccessLevel().getLevel()>1) {
			sql = " AND user.logid="+ getUserLogin().getLogid();
		}
		
		if(getSearchParam()!=null) {
			sql = " AND cz.periodcovered like '%"+ getSearchParam() +"%'";
		}
		
		sql += " ORDER BY cz.cpid DESC LIMIT 10";
		//rpts = CashDisbursementReport.retrieve(sql, params);
		for(CheckIssuedReport cd : CheckIssuedReport.retrieve(sql, params)) {
			cd.setFundName(getMapBankAccounts().get(cd.getFundId()).getBankAccntName());
			rpts.add(cd);
		}
		if(rpts!=null && rpts.size()==1) {
			clickItemRpt(rpts.get(0));
		}
		
	}
	
	public void searchItem() {
		clear();
		rpts = new ArrayList<CheckIssuedReport>();
		String[] params = new String[0];
		String sql = "";
		if(getUserLogin().getAccessLevel().getLevel()>1) {
			sql = " AND user.logid="+ getUserLogin().getLogid();
		}
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			sql = " AND caz.payee like '%"+ getSearchParam() +"%'";
		
		
			List<CheckIssued> chks = CheckIssued.retrieve(sql, params);
			Map<Long, CheckIssuedReport> rptData = new LinkedHashMap<Long, CheckIssuedReport>();
			for(CheckIssued c : chks) {
					rptData.put(c.getCheckIssuedReport().getId(), c.getCheckIssuedReport());
			}
			for(CheckIssuedReport cd : rptData.values()) {
				cd.setFundName(getMapBankAccounts().get(cd.getFundId()).getBankAccntName());
				rpts.add(cd);
			}
			if(rpts!=null && rpts.size()==1) {
				clickItemRpt(rpts.get(0));
			}
		
		}else {
			load();
		}
	}
	
	public void createItem() {
		
		boolean isOk = true;
		CheckIssuedReport cashDv = getCashDvData();
		
		if(cashDv.getPeriodCovered()==null || cashDv.getPeriodCovered().isEmpty()) {
			Application.addMessage(2, "Warning", "Please provide period covered details");
			isOk = false;
		}
		
		if(cashDv.getReportNo()==null || cashDv.getReportNo().isEmpty()) {
			Application.addMessage(2, "Warning", "Please provide period report no details");
			isOk = false;
		}
		
		if(cashDv.getSheetNo()==null || cashDv.getSheetNo().isEmpty()) {
			Application.addMessage(2, "Warning", "Please provide period sheet no details");
			isOk = false;
		}
		
		if(isOk) {
			cashDv.setDateReport(DateUtils.convertDate(cashDv.getDateReportTmp(), "yyyy-MM-dd"));
			cashDv.setIsActive(1);
			cashDv.setLoginUser(getUserLogin());
			cashDv = CheckIssuedReport.save(cashDv);
			List<CheckIssued> dvs = new ArrayList<CheckIssued>();
			dvs.add(CheckIssued.builder().number(1).checkIssuedReport(cashDv).build());
			/*
			 * int x=1; while(x<=90) {
			 * dvs.add(CashDisbursement.builder().number(x).report(cashDv).build()); x++; }
			 */
			getCashDvData().setRpts(dvs);
			setEnabledTable(true);
			load();
		}
	}
	
	public void onRowReorder(ReorderEvent event) {
		//Application.addMessage(1, "Move", "From: " + event.getFromIndex() + ", To:" + event.getToIndex());
		int index = 0;
		for(CheckIssued rt : getCashDvData().getRpts()) {
			rt.setNumber(index++);
			rt.save();
		}
		
	}
	
	public void onCellEdit(CellEditEvent event) {
		 try{
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        
	        System.out.println("old Value: " + oldValue);
	        System.out.println("new Value: " + newValue);
	        
	        int index = event.getRowIndex();
	       // String column =  event.getColumn().getHeaderText();
	        //System.out.println("getCashDvData(): " + getCashDvData());
	        //System.out.println(getCashDvData().getRpts()!=null? getCashDvData().getRpts().size() : "No value");
	        
	        if(getCashDvData().getRpts()!=null && getCashDvData().getRpts().size()>0) {
	        	
	        	CheckIssued cz = getCashDvData().getRpts().get(index);
	        	cz.setFundId(getCashDvData().getFundId());
	        	cz.setIsActive(1);
	        	cz.setCheckIssuedReport(getCashDvData());
	        	//if(cz.getAmount()>0) {//only save in database if amount is greater than zero
		        	cz = CheckIssued.save(cz);
		        	getCashDvData().getRpts().get(index).setId(cz.getId());//update item id
	        	//}
	        	
	        	if(getCashDvData().getRpts().get(index).getId()>0) {
	        		load();
	        	}
	        	
	        }
	        
		 }catch(Exception e){
			 e.printStackTrace();
		 }  
	 }
}
