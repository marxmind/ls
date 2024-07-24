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
import java.util.TreeMap;

import org.primefaces.PrimeFaces;

import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.DepositTransaction;
import com.italia.municipality.lakesebu.controller.RCDAllController;
import com.italia.municipality.lakesebu.controller.RCDDeposit;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.enm.Months;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.reports.Rcd;
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
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named("bankdp")
@ViewScoped
@Data
public class BankDepositBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4353645757546541L;
	private int yearDepositId;
	private List yearDeposits;
	private int monthDepositId;
	private List monthDeposits;
	private List<RCDDeposit> deposits;
	private int depositFundTypeId;
	private List depositFundTypes;
	private RCDDeposit rcdData;
	
	@PostConstruct
	public void initLoad() {
		rcdData = RCDDeposit.builder()
				.dateDeposited(new Date())
				.build();
		yearDepositId = DateUtils.getCurrentYear();
		yearDeposits = new ArrayList<>();
		monthDepositId = DateUtils.getCurrentMonth();
		monthDeposits = new ArrayList<>();
		deposits = new ArrayList<RCDDeposit>();
		for(int y=2023; y<=DateUtils.getCurrentYear(); y++) {
			yearDeposits.add(new SelectItem(y,y+""));
		}
		for(Months m : Months.values()) {
			if(m.getId()>0 && m.getId()<13) {
				monthDeposits.add(new SelectItem(m.getId(), m.getName()));
			}
		}
		
		depositFundTypes = new ArrayList<>();
		for(FundType f : FundType.values()) {
			depositFundTypes.add(new SelectItem(f.getId(), f.getName()));
		}
		
		loadBankTransaction();
		
	}
	
	public void openBankTransaction() {
		loadBankTransaction() ;
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgBankDeposit').show(1000)");
	}
	
	public void loadBankTransaction() {
		
		String paramFrom = getYearDepositId() + "-" + (getMonthDepositId()<10? "0" + getMonthDepositId() : getMonthDepositId()) + "-01";
		String paramTo = getYearDepositId() + "-" + (getMonthDepositId()<10? "0" + getMonthDepositId() : getMonthDepositId()) + "-31";
		deposits = RCDDeposit.retrieve(" AND ct.fundtype="+ getDepositFundTypeId() +" AND (ct.datetrans>='"+ paramFrom+"' AND ct.datetrans<='"+ paramTo +"') ORDER BY ct.did DESC", new String[0]);
		
	}
	
	public void clear() {
		rcdData = RCDDeposit.builder()
				.dateDeposited(new Date())
				.build();
		setRcdData(rcdData);
	}
	
	public void saveDeposit() {
		if(getRcdData()!=null && !getRcdData().getReference().isEmpty() && getRcdData().getAmount()>0) {
			RCDDeposit rcd = getRcdData();
			
			if(rcd.getId()==0) {
				int index = RCDDeposit.lastIndex(getDepositFundTypeId(), getYearDepositId(), getMonthDepositId());
				rcd.setIndexId(index+1);
			}
			
			rcd.setFundType(getDepositFundTypeId());
			rcd.setDateTrans(DateUtils.convertDate(rcd.getDateDeposited(), "yyyy-MM-dd"));
			//rcd.setRemarks("RCD-DEPOSIT");
			rcd.setIsActive(1);
			rcd.save();
			rcdData = RCDDeposit.builder()
					.dateDeposited(new Date())
					.build();
			loadBankTransaction();
			Application.addMessage(1, "Success", "Successfully Saved.");;
		}else {
			Application.addMessage(3, "Error", "Please add reference no");
		}
	}
	
	
	
	public void clickItem(RCDDeposit dp) {
		dp.setDateDeposited(DateUtils.convertDateString(dp.getDateTrans(), "yyyy-MM-dd"));
		setRcdData(dp);
	}
	
	public void deleteItem(RCDDeposit dp) {
		dp.delete();
		deposits.remove(dp);
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	private String group(String number) {
		String val = "0001";
		
		int count = number.length();
		switch(count) {
			case 1 : val = "000" + number; break;
			case 2 : val = "00" + number; break;
			case 3 : val = "0" + number; break;
			case 4 : val = number; break;
		}
		
		return val;
	}
	
	public void printDeposit(RCDDeposit dp) {
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = GlobalVar.BANK_DEPOSIT;
		
		String grp = "";
		if(FundType.GENERAL_FUND.getId()==dp.getFundType()) {
			grp += "GF-" + dp.getDateTrans().split("-")[0];
		}else if(FundType.TRUST_FUND.getId()==dp.getFundType()) {
			grp += "TF-" + dp.getDateTrans().split("-")[0];
		}else if(FundType.MOTORPOOL.getId()==dp.getFundType()) {
			grp += "MF-" + dp.getDateTrans().split("-")[0];
		}
		
		
		
		if(dp.getGroupNo()==0) {
			grp += "-0001";
		}else {
			grp += "-"+group(dp.getGroupNo()+"");
		}
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		List<Rcd> reports = new ArrayList<Rcd>();
		DocumentFormatter doc = new DocumentFormatter();
		/*reports.add(Rcd.builder().f1(doc.getTagName("treasurer-name").toUpperCase()).f2("").f3("").build());
		for(int i=1; i<=9; i++) {
			reports.add(Rcd.builder().f1("").f2("").f3("").build());
		}*/
		List<RCDDeposit> rcds = RCDDeposit.retrieve(" AND ct.datetrans='"+ dp.getDateTrans() +"' AND ct.fundtype="+ dp.getFundType() +" AND ct.groupno=" + dp.getGroupNo(), new String[0]);
		double total = 0d;
		if(rcds!=null && rcds.size()>0) {
			for(RCDDeposit r : rcds) {
				reports.add(Rcd.builder().f1(r.getRemarks()).f2(r.getReference()).f3(Currency.formatAmount(r.getAmount())).build());
				total += r.getAmount();
			}
			reports.add(Rcd.builder().f1("******************NOTHING FOLLOWS").f2("******************").f3("******************").build());
		}else {
			reports.add(Rcd.builder().f1("").f2("").f3("").build());
		}
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		
		param.put("PARAM_TREASURER", doc.getTagName("treasurer-name").toUpperCase());
		param.put("PARAM_TREASURER_POS", doc.getTagName("treasurer-position"));
  		param.put("PARAM_LIQUIDATING_OFFICER", doc.getTagName("verified-person"));
  		param.put("PARAM_VERIFIED_POSITION", doc.getTagName("verified-person-position"));
  		//param.put("PARAM_TOTAL",Currency.formatAmount(dp.getAmount()));
  		param.put("PARAM_TOTAL",Currency.formatAmount(total));
  		
  		String date = DateUtils.convertDateToMonthDayYear(dp.getDateTrans());
  		
  		param.put("PARAM_FUND", FundType.typeName(getDepositFundTypeId()));
  		param.put("PARAM_REFERENCENO_1", dp.getReference());
  		param.put("PARAM_AMOUNT_DEPOSITED_1", Currency.formatAmount(dp.getAmount()));
  		param.put("PARAM_RPT_GROUP", grp);
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
