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
import org.primefaces.event.ReorderEvent;
import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.CashDisbursement;
import com.italia.municipality.lakesebu.controller.CashDisbursementReport;
import com.italia.municipality.lakesebu.controller.CheckIssued;
import com.italia.municipality.lakesebu.controller.CheckRpt;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.Words;
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

@Named("cashDv")
@ViewScoped
@Data
public class CashDVReportBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4364576864451L;
	
	private List funds;
	private CashDisbursementReport cashDvData;
	private List<CashDisbursementReport> rpts;
	private String searchParam;
	private boolean enabledTable;
	private Map<Integer, BankAccounts> mapBankAccounts;
	private Login userLogin;
	
	@PostConstruct
	public void init() {
		mapBankAccounts = new LinkedHashMap<Integer, BankAccounts>();
		funds = new ArrayList<>();
		for(BankAccounts a : BankAccounts.retrieveAll()) {
			funds.add(new SelectItem(a.getBankId(), a.getBankAccntName()));
			mapBankAccounts.put(a.getBankId(), a);
		}
		setUserLogin(Login.getUserLogin());
		defaultValue();
		load();
	}
	
	private void defaultValue() {
		CashDisbursementReport r = CashDisbursementReport.builder()
				.dateReportTmp(new Date())
				.disbursingOfficer(Words.getTagName("disbursing-officer"))
				.designation(Words.getTagName("disbursing-officer-designation"))
				.build();
		
		List<CashDisbursement> dvs = new ArrayList<CashDisbursement>();
		//int x=1;
		//while(x<=90) {
		//	dvs.add(CashDisbursement.builder().number(x).build());
		//	x++;
		//}
		//one at a time for now
		dvs.add(CashDisbursement.builder().number(1).build());
		
		r.setRpts(dvs);
		
		setCashDvData(r);
	}
	
	public void clickItemRpt(CashDisbursementReport rpt) {
		rpt.setDateReportTmp(DateUtils.convertDateString(rpt.getDateReport(), "yyyy-MM-dd"));
		
		rpt.setRpts(CashDisbursement.retrieveReportGroupDisbursement(rpt.getId()));
		if(rpt.getRpts().size()==0) {
			List<CashDisbursement> dvs = new ArrayList<CashDisbursement>();
			dvs.add(CashDisbursement.builder().number(1).build());
			rpt.setRpts(dvs);
		}
		setEnabledTable(true);
		setCashDvData(rpt);
	}
	
	public void deleteItem(CashDisbursement cv) {
		cv.delete();
		getCashDvData().getRpts().remove(cv);
	}
	
	public void onRowReorder(ReorderEvent event) {
		//Application.addMessage(1, "Move", "From: " + event.getFromIndex() + ", To:" + event.getToIndex());
		int index = 0;
		for(CashDisbursement rt : getCashDvData().getRpts()) {
			rt.setNumber(index++);
			rt.save();
		}
		
	}
	
	public void print(CashDisbursementReport rpt) {
		String REPORT_PATH = GlobalVar.REPORT_FOLDER;
		String REPORT_NAME = GlobalVar.CASH_DISBURSEMENT_NAME;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		List<CheckRpt> reports = new ArrayList<CheckRpt>();
		List<CashDisbursement> cashDisList = CashDisbursement.retrieveReportGroupDisbursement(rpt.getId());
		/*int count = 1;
		double amount = 0d;
		for(CashDisbursement cs : cashDisList) {
			if(count<30) {
			CheckRpt cpt = CheckRpt.builder()
					.f1(cs.getDateTrans())
					.f2(cs.getDvPayroll())
					.f3(cs.getCafoaNo())
					.f4(cs.getPayee())
					.f5(cs.getNaturePay())
					.f6(Currency.formatAmount(cs.getAmount()))
					.build();
			reports.add(cpt);
			amount += cs.getAmount();
			
			if(count==29) {
				cpt = CheckRpt.builder()
						.f1("")
						.f2("")
						.f3("")
						.f4("")
						.f5("Total")
						.f6(Currency.formatAmount(amount))
						.build();
				reports.add(cpt);
			}else {
				
			}
			
			}
			
			count++;
		}*/
		int count = 0;
		double amount = 0d;
		if(cashDisList.size()<32) {
			
			for(CashDisbursement cs : cashDisList) {
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			/*
			CheckRpt cpt = CheckRpt.builder()
					.visible("show")
					.f1("     ***")
					.f2("\t***")
					.f3("\t***")
					.f4("\tNOTHING FOLLOWS")
					.f5("\t***")
					.f6("***\t\t\t")
					.build();
			reports.add(cpt);*/
			
			count = 31 - count;
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
		
		if(cashDisList.size()>=32 && cashDisList.size()<62) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		
		if(cashDisList.size()>=62 && cashDisList.size()<92) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<61; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=61; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		
		if(cashDisList.size()>=92 && cashDisList.size()<122) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<61; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=61; i<91; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=91; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		
		if(cashDisList.size()>=122 && cashDisList.size()<152) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<61; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=61; i<91; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=91; i<121; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=121; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		/////////////////////////////////152//////////////////////182/////////////////////
		if(cashDisList.size()>=152 && cashDisList.size()<182) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<61; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=61; i<91; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=91; i<121; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=121; i<151; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			
			count = 0;
			
			for(int i=151; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		
		////////////////////////////////182/////////////////212//////////////
		if(cashDisList.size()>=182 && cashDisList.size()<212) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<61; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=61; i<91; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=91; i<121; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=121; i<151; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			
			count = 0;
			
			for(int i=151; i<181; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=181; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		
		/////////212///////242
		if(cashDisList.size()>=212 && cashDisList.size()<242) {
			
			for(int i=0; i<=30; i++) {
				CashDisbursement cs = cashDisList.get(i);
				CheckRpt cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			
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
			
			
			
			 cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=31; i<61; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=61; i<91; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=91; i<121; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=121; i<151; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			
			count = 0;
			
			for(int i=151; i<181; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=181; i<211; i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Total")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			cpt = CheckRpt.builder()
					.visible("hide")
					.f1("")
					.f2("")
					.f3("")
					.f4("")
					.f7("Sub-Total Carried Forward")
					.f8(Currency.formatAmount(amount))
					.build();
			reports.add(cpt);
			
			count = 0;
			
			for(int i=211; i<cashDisList.size(); i++) {
				CashDisbursement cs = cashDisList.get(i);
				 cpt = CheckRpt.builder()
						.visible("show")
						.f1(cs.getDateTrans())
						.f2(cs.getDvPayroll())
						.f3(cs.getCafoaNo())
						.f4(cs.getPayee())
						.f5(cs.getNaturePay())
						.f6(Currency.formatAmount(cs.getAmount()))
						.build();
				reports.add(cpt);
				count++;
				amount += cs.getAmount();
			}
			count = 30 - count;
			for(int i=1; i<=count; i++) {
				cpt = CheckRpt.builder()
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
			
			//total
			cpt = CheckRpt.builder()
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
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		
		param.put("PARAM_TITLE", "REPORT OF CASH DISBURSEMENTS");
		param.put("PARAM_SUB_TITLE", "Period Covered: "+ rpt.getPeriodCovered());
		param.put("PARAM_LGU_NAME", Words.getTagName("lgu-name"));
		param.put("PARAM_FUND_NAME", getMapBankAccounts().get(rpt.getFundId()).getBankAccntName());
		param.put("PARAM_REPORT_NO", rpt.getReportNo());
		//param.put("PARAM_SHEET_NO", "1");
		param.put("PARAM_SHEET_TOTAL", "1");
		param.put("PARAM_DISBURSING_OFFICER", rpt.getDisbursingOfficer());
		param.put("PARAM_DISBURSING_POSITION", rpt.getDesignation());
		param.put("PARAM_DATE", "");
		
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
	
	public void deleteRpt(CashDisbursementReport rpt) {
		rpt.delete();
		CashDisbursement.delete("UPDATE cashdisbursement SET isactived=0 WHERE zid="+ rpt.getId(), new String[0]);//only updating not deleting the data
		rpts.remove(rpt);
		
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
		int index = count - 2;
		index = index<0? 0 : index;
		CashDisbursement cash = getCashDvData().getRpts().get(index);
		getCashDvData().getRpts().add(
				CashDisbursement.builder()
				.number(count)
				.dvPayroll(newDV(cash.getDvPayroll()))
				.cafoaNo(cafoaSeries(cash.getCafoaNo()))
				.naturePay(cash.getNaturePay())
				.build()
				);
		setEnabledTable(true);
	}
	
	private String newDV(String val) {
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
	
	private String cafoaSeries(String val) {
		String cafoa="";
		
		if(val.contains("-")) {
			String[] cafs = val.split("-");
			int series = Integer.valueOf(cafs[3]);
			series += 1;
			cafoa = cafs[0] + "-" + cafs[1] + "-" + cafs[2] + "-" + series;
			return cafoa;
		}
		
		
		return val;
	}
	
	public void updateInfo() {
		if(getCashDvData()!=null) {
			getCashDvData().save();
			load();
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
		CashDisbursementReport cashDv = getCashDvData();
		
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
			cashDv = CashDisbursementReport.save(cashDv);
			
			List<CashDisbursement> items = new ArrayList<CashDisbursement>();
			for(CashDisbursement r : cashDv.getRpts()) {
				r.setIsActive(1);
				r.setReport(cashDv);
				r = CashDisbursement.save(r);
				items.add(r);
			}
			cashDv.setRpts(items);
			Application.addMessage(1, "Success", "Successfully saved.");
		}
	}
	
	
	
	public void load() {
		rpts = new ArrayList<CashDisbursementReport>();
		String[] params = new String[0];
		String sql = "";
		if(getUserLogin().getAccessLevel().getLevel()>1) {
			sql = " AND user.logid="+ getUserLogin().getLogid();
		}
		//if(getSearchParam()!=null) {
		//	sql = " AND cz.periodcovered like '%"+ getSearchParam() +"%'";
		//}
		
		sql += " ORDER BY cz.zid DESC LIMIT 10";
		//rpts = CashDisbursementReport.retrieve(sql, params);
		for(CashDisbursementReport cd : CashDisbursementReport.retrieve(sql, params)) {
			cd.setFundName(getMapBankAccounts().get(cd.getFundId()).getBankAccntName());
			rpts.add(cd);
		}
		if(rpts!=null && rpts.size()==1) {
			clickItemRpt(rpts.get(0));
		}
		
	}
	
	public void searchItem() {
		clear();
		rpts = new ArrayList<CashDisbursementReport>();
		String[] params = new String[0];
		String sql = "";
		if(getUserLogin().getAccessLevel().getLevel()>1) {
			sql = " AND user.logid="+ getUserLogin().getLogid();
		}
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			sql = " AND caz.payee like '%"+ getSearchParam() +"%'";
		
		
			List<CashDisbursement> chks = CashDisbursement.retrieve(sql, params);
			Map<Long, CashDisbursementReport> rptData = new LinkedHashMap<Long, CashDisbursementReport>();
			for(CashDisbursement c : chks) {
					rptData.put(c.getReport().getId(), c.getReport());
			}
			for(CashDisbursementReport cd : rptData.values()) {
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
		CashDisbursementReport cashDv = getCashDvData();
		
		if(cashDv.getPeriodCovered()==null || cashDv.getPeriodCovered().isEmpty()) {
			Application.addMessage(2, "Warning", "Please provide period covered details");
			isOk = false;
		}
		
		if(isOk) {
			cashDv.setDateReport(DateUtils.convertDate(cashDv.getDateReportTmp(), "yyyy-MM-dd"));
			cashDv.setIsActive(1);
			cashDv.setLoginUser(getUserLogin());
			cashDv = CashDisbursementReport.save(cashDv);
			List<CashDisbursement> dvs = new ArrayList<CashDisbursement>();
			dvs.add(CashDisbursement.builder().number(1).report(cashDv).build());
			/*
			 * int x=1; while(x<=90) {
			 * dvs.add(CashDisbursement.builder().number(x).report(cashDv).build()); x++; }
			 */
			getCashDvData().setRpts(dvs);
			setEnabledTable(true);
			load();
		}
	}
	
	public void onCellEdit(CellEditEvent event) {
		 try{
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        
	        System.out.println("old Value: " + oldValue);
	        System.out.println("new Value: " + newValue);
	        
	        int index = event.getRowIndex();
	        String column =  event.getColumn().getHeaderText();
	        //System.out.println("getCashDvData(): " + getCashDvData());
	        //System.out.println(getCashDvData().getRpts()!=null? getCashDvData().getRpts().size() : "No value");
	        
	        if(getCashDvData().getRpts()!=null && getCashDvData().getRpts().size()>0) {
	        	
	        	CashDisbursement cz = getCashDvData().getRpts().get(index);
	        	cz.setFundId(getCashDvData().getFundId());
	        	cz.setIsActive(1);
	        	cz.setReport(getCashDvData());
	        	//if(cz.getAmount()>0) {//only save in database if amount is greater than zero
		        	cz = CashDisbursement.save(cz);
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
