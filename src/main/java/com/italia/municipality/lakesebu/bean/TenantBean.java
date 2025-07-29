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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.Bills;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Tenant;
import com.italia.municipality.lakesebu.controller.TenantBilling;
import com.italia.municipality.lakesebu.controller.TenantContract;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.ClassType;
import com.italia.municipality.lakesebu.enm.TenantStatus;
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
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * @since 05/28/2025
 * @version 1.0
 * @author Mark Italia
 * 
 */

@Named("tenant")
@ViewScoped
@Data
public class TenantBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1324668657958965865L;
	
	//tenant
	private String search;
	private List<Tenant> tenants;
	private Tenant selectedData;
	
	//contract
	private String searchContract;
	private List<TenantContract> contracts;
	private TenantContract selectedContract;
	private List tenantContracts;
	private List status;
	private List types;
	
	//bills
	private String searchBill;
	private List<TenantBilling> bills;
	private TenantBilling billSelected;
	
	
	//billing
	private String searchBillCreated;
	private List<Tenant> billTenants;
	//private List<TenantBilling> billings;
	
	@PostConstruct
	public void init() {
		
		loadContactVal();
		
		clear();
		clearConract();
		clearBill();
		loadBillCreated();
	}
	
	public void onTabChange(TabChangeEvent event) {
		
		if("Billing".equalsIgnoreCase(event.getTab().getTitle())) {
			loadBillCreated();
		}else if("Tenant Information".equalsIgnoreCase(event.getTab().getTitle())) {
			clear();
			load();
		}else if("Contract Information".equalsIgnoreCase(event.getTab().getTitle())) {
			clearConract();
			loadContactVal();
		}else if("Billing Generation".equalsIgnoreCase(event.getTab().getTitle())) {
			clearBill();
			generateBilling();
		}
	}
	
	///////////////////////////////////BILLING//////////////////////////////////////////
	public void loadBillCreated() {
		System.out.println("loadBillCreated().....");
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		/*
		String sql = "";
		if(getSearchBillCreated()!=null) {
			//sql = " AND bill.mnt="+ month +" AND bill.yer=" + year;
			sql += " AND (ten.fullnamet like '%"+ getSearchBillCreated() +"%' OR bill.billno like '%"+ getSearchBillCreated() +"%')";
			//sql += " AND bill.ispaid=0 ";
			//sql += " ORDER BY ten.fullnamet";
		}
		
		sql += " AND bill.mnt="+ month +" AND bill.yer=" + year;
		sql += " AND bill.ispaid=0 ";
		sql += " ORDER BY ten.fullnamet";
		billings = TenantBilling.retrieve(sql, new String[0]);
		*/
		
		String name = null;
		if(getSearchBillCreated()!=null) {
			name = getSearchBillCreated();
		}
		billTenants = TenantBilling.getTenantBillLatest(year, month, name);
		System.out.println("end loadBillCreated().....");
	}
	
	public void printBill(Tenant bill) {
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = GlobalVar.MARKET_BILL;
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		
		String sql = "";
		//int year = DateUtils.getCurrentYear();
		//int month = DateUtils.getCurrentMonth();
		sql = " AND bill.tid=" + bill.getId();
		//sql += " AND bill.mnt="+ month +" AND bill.yer=" + year;
		sql += " AND bill.ispaid=0 ORDER BY bill.yer, bill.mnt";
		Map<Long, Map<Integer, List<Bills>>> billDataContract = new LinkedHashMap<Long, Map<Integer, List<Bills>>>();
		Map<Integer, List<Bills>> billDataYear = new LinkedHashMap<Integer, List<Bills>>();
		List<Bills> billList = new ArrayList<Bills>();
		for(TenantBilling py : TenantBilling.retrieve(sql, new String[0])) {
			
			//System.out.println("year/month: "+ py.getYear() + "/" + py.getMonth() +"\t bill:" + py.getBillNo() + "\t amount: " + py.getBillAmount());
			
			Bills bll = Bills.builder()
					.tenantNo(py.getTenant().getTenantNo())
					.accountNo(py.getContract().getAccountNo())
					.name(py.getTenant().getFullName())
					.contractId(py.getContract().getId())
					.billNo(py.getBillNo())
					.year(py.getYear())
					.month(py.getMonth())
					.principal(py.getBillAmount())
					.stall(py.getContract().getClassTypeName()+"/"+py.getContract().getLocationNumber())
					.surcharge(py.getSurcharge())
					.interest(py.getInterest())
					.unpaidPricipal(py.getUnpaidPrincipal())
					.total(py.getTotal())
					.build();
			
			int year = py.getYear();
			long contractId = py.getContract().getId();
			
			if(billDataContract!=null) {
				if(billDataContract.containsKey(contractId)) {
					if(billDataContract.get(contractId).containsKey(year)) {
						billDataContract.get(contractId).get(year).add(bll);
					}else {
						billList = new ArrayList<Bills>();
						billList.add(bll);
						billDataContract.get(contractId).put(year, billList);
					}
				}else {
					billDataYear = new LinkedHashMap<Integer, List<Bills>>();
					billList = new ArrayList<Bills>();
					billList.add(bll);
					billDataYear.put(year, billList);
					billDataContract.put(contractId, billDataYear);
				}
			}else {
				billList.add(bll);
				billDataYear.put(year, billList);
				billDataContract.put(contractId, billDataYear);
				
			}
			
		}
		
		//logo
		String officialLogo = REPORT_PATH + "logo.png";
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Bills> bbs = new ArrayList<Bills>();
		int count = 1;
		String s1 = "";
		String s2 = "";
		String s3 = "";
		
		String h1 = "";
		String h2 = "";
		String h3 = "";
		
		String dtls1 = "";
		String dtls2 = "";
		String dtls3 = "";
		
		String notes = "Note:\n\tPlease settle your account on or before ";
		notes += DateUtils.getMonthName(DateUtils.getCurrentMonth());
		notes += " 20 at Municipal Treasurer's Office. After the due date, a 25% surcharge of the amount due plus a monthly interest of 2% on the whole unpaid amount will be added to the total rent due."
				+ "\n\n\tPlease bring along with you your latest proof of payment(Official Receipt) when you settle this account or if payment has already been made, kindly disregard this bill."
				+ "\n\nPrepared by:";
		
		
		String preparedBy = Words.getTagName("market-preparedby");
		String preparedByPos = Words.getTagName("market_preparedby-pos");
		String treasname = Words.getTagName("market-treasname");
		String treasnamepos = Words.getTagName("market_prparedby-pos");
		
		for(long contract : billDataContract.keySet()) {
			
			Bills bz = null;
			for(int year : billDataContract.get(contract).keySet()) {
				bbs.addAll(billDataContract.get(contract).get(year));
				
				if(count==1) {
					s1 = "\n\nSTATEMENT OF ACCOUNT\nSTALL RENTAL("+ billDataContract.get(contract).get(year).get(0).getStall()+")";
					dtls1 += "\n" + year;
				}
				
				if(count==2) {
					s2 = "\n\nSTATEMENT OF ACCOUNT\nSTALL RENTAL("+ billDataContract.get(contract).get(year).get(0).getStall()+")";
					dtls2 += "\n" + year;
				}
				
				if(count==3) {
					s3 = "\n\nSTATEMENT OF ACCOUNT\nSTALL RENTAL("+ billDataContract.get(contract).get(year).get(0).getStall()+")";
					dtls3 += "\n" + year;
				}
				
				int index = 0;
				
				for(Bills b : billDataContract.get(contract).get(year)) {
					/*System.out.println("Name: " + b.getName());
					System.out.println("Year: " + b.getYear() + "\tMonth: " + b.getMonth() + "\tPrincipal: " + b.getPrincipal() + "\tSurcharge: " + b.getSurcharge() + "\tInterest: " + b.getInterest() + "\tTotal: " + b.getTotal());
					*/
					
					if(count==1) {
						dtls1 += "\n\t" + DateUtils.getMonthName(b.getMonth());
					}
					
					if(count==2) {
						dtls2 += "\n\t" + DateUtils.getMonthName(b.getMonth());
					}
					
					if(count==3) {
						dtls3 += "\n\t" + DateUtils.getMonthName(b.getMonth());
					}
					
					
					index++;
				}
				
				bz = billDataContract.get(contract).get(year).get(index-1);
				
			}
			
			
			if(bz!=null) {
			
			
			if(count==1) {
				s1 += "\nAs of " + DateUtils.getMonthName(bz.getMonth()) + " " + bz.getYear();
				s1 += "\n";
				
				h1 = "Tenant No: " + bz.getTenantNo() + "\n";
				h1 += "Account No: " + bz.getAccountNo() + "\n";
				h1 += "Lessee: " + bz.getName() + "\n";
				h1 += "Location: " + bz.getStall() + "\n";
				
				h1 += "\n";
				h1 += "Bill no: " + bz.getBillNo();
				h1 += "\nContract Amount: " + Currency.formatAmount(bz.getPrincipal());
				h1 += "\nSurcharge: " + Currency.formatAmount(bz.getSurcharge());
				h1 += "\nInterest: " + Currency.formatAmount(bz.getInterest());
				h1 += "\nUnpaid: " + Currency.formatAmount(bz.getUnpaidPricipal());
				//h1 += "\nTotal Bill: " + Currency.formatAmount(bz.getTotal());
				
				param.put("PARAM_TITLE1", "Republic of the Philippines\nProvinve of South Cotabato\nMunicipality of Lake Sebu\nOFFICE OF THE MUNICIPAL TREASURER" + s1);
				param.put("PARAM_HEADER1", h1 + "\n\nUnpaid Monht/s" + dtls1);
				try{File file = new File(officialLogo);
				FileInputStream off = new FileInputStream(file);
				param.put("PARAM_LOGO1", off);
				}catch(Exception e){e.printStackTrace();}
				
				param.put("PARAM_NOTES1", notes);
				param.put("PARAM_PREPAREDBY1", preparedBy);
				param.put("PARAM_POS1", preparedByPos);
				param.put("PARAM_NOTEDBY1", "Noted by:");
				param.put("PARAM_TREASNAME1", treasname);
				param.put("PARAM_TREASPOS1", treasnamepos);
				param.put("PARAM_TOTAL_PAYABLE1","AMOUNT TO PAY PHP: " + Currency.formatAmount(bz.getTotal()));
			}
			
			if(count==2) {
				s2 += "\nAs of " + DateUtils.getMonthName(bz.getMonth()) + " " + bz.getYear();
				s2 += "\n";
				
				h2 = "Tenant No: " + bz.getTenantNo() + "\n";
				h2 += "Account No: " + bz.getAccountNo() + "\n";
				h2 += "Lessee: " + bz.getName() + "\n";
				h2 += "Location: " + bz.getStall() + "\n";
				
				h2 += "\n";
				h2 += "Bill no: " + bz.getBillNo();
				h2 += "\nContract Amount: " + Currency.formatAmount(bz.getPrincipal());
				h2 += "\nSurcharge: " + Currency.formatAmount(bz.getSurcharge());
				h2 += "\nInterest: " + Currency.formatAmount(bz.getInterest());
				h2 += "\nUnpaid: " + Currency.formatAmount(bz.getUnpaidPricipal());
				//h2 += "\nTotal Bill: " + Currency.formatAmount(bz.getTotal());
				
				param.put("PARAM_TITLE2", "Republic of the Philippines\nProvinve of South Cotabato\nMunicipality of Lake Sebu\nOFFICE OF THE MUNICIPAL TREASURER" + s2);
				param.put("PARAM_HEADER2", h2 + "\n\nUnpaid Monht/s" + dtls2);
				try{File file = new File(officialLogo);
				FileInputStream off = new FileInputStream(file);
				param.put("PARAM_LOGO2", off);
				}catch(Exception e){e.printStackTrace();}
				
				param.put("PARAM_NOTES2", notes);
				param.put("PARAM_PREPAREDBY2", preparedBy);
				param.put("PARAM_POS2", preparedByPos);
				param.put("PARAM_NOTEDBY2", "Noted by:");
				param.put("PARAM_TREASNAME2", treasname);
				param.put("PARAM_TREASPOS2", treasnamepos);
				param.put("PARAM_TOTAL_PAYABLE2","AMOUNT TO PAY PHP: " + Currency.formatAmount(bz.getTotal()));
			}
			
			if(count==3) {
				s3 += "\nAs of " + DateUtils.getMonthName(bz.getMonth()) + " " + bz.getYear();
				s3 += "\n";
				
				h3 = "Tenant No: " + bz.getTenantNo() + "\n";
				h3 += "Account No: " + bz.getAccountNo() + "\n";
				h3 += "Lessee: " + bz.getName() + "\n";
				h3 += "Location: " + bz.getStall() + "\n";
				
				h3 += "\n";
				h3 += "Bill no: " + bz.getBillNo();
				h3 += "\nContract Amount: " + Currency.formatAmount(bz.getPrincipal());
				h3 += "\nSurcharge: " + Currency.formatAmount(bz.getSurcharge());
				h3 += "\nInterest: " + Currency.formatAmount(bz.getInterest());
				h3 += "\nUnpaid: " + Currency.formatAmount(bz.getUnpaidPricipal());
				//h3 += "\nTotal Bill: " + Currency.formatAmount(bz.getTotal());
				
				param.put("PARAM_TITLE3", "Republic of the Philippines\nProvinve of South Cotabato\nMunicipality of Lake Sebu\nOFFICE OF THE MUNICIPAL TREASURER" + s3);
				param.put("PARAM_HEADER3", h3 + "\n\nUnpaid Monht/s" + dtls3);
				try{File file = new File(officialLogo);
				FileInputStream off = new FileInputStream(file);
				param.put("PARAM_LOGO3", off);
				}catch(Exception e){e.printStackTrace();}
				
				param.put("PARAM_NOTES3", notes);
				param.put("PARAM_PREPAREDBY3", preparedBy);
				param.put("PARAM_POS3", preparedByPos);
				param.put("PARAM_NOTEDBY3", "Noted by:");
				param.put("PARAM_TREASNAME3", treasname);
				param.put("PARAM_TREASPOS3", treasnamepos);
				param.put("PARAM_TOTAL_PAYABLE3","AMOUNT TO PAY PHP: " + Currency.formatAmount(bz.getTotal()));
			}
			
			}
			
			count++;
			
		}
		
		

		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(bbs);
		
		
		
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
	
	public void printBillX(Tenant bill) {
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = GlobalVar.MARKET_BILL;
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		
		String sql = "";
		//int year = DateUtils.getCurrentYear();
		//int month = DateUtils.getCurrentMonth();
		sql = " AND bill.tid=" + bill.getId();
		//sql += " AND bill.mnt="+ month +" AND bill.yer=" + year;
		sql += " AND bill.ispaid=0 ORDER BY bill.yer, bill.mnt";
		Map<Integer, Map<Long, List<Bills>>> billDataYear = new LinkedHashMap<Integer, Map<Long, List<Bills>>>();
		Map<Long, List<Bills>> billDataContract = new LinkedHashMap<Long, List<Bills>>();
		List<Bills> billList = new ArrayList<Bills>();
		for(TenantBilling py : TenantBilling.retrieve(sql, new String[0])) {
			
			//System.out.println("year/month: "+ py.getYear() + "/" + py.getMonth() +"\t bill:" + py.getBillNo() + "\t amount: " + py.getBillAmount());
			
			Bills bll = Bills.builder()
					.tenantNo(py.getTenant().getTenantNo())
					.accountNo(py.getContract().getAccountNo())
					.name(py.getTenant().getFullName())
					.contractId(py.getContract().getId())
					.billNo(py.getBillNo())
					.year(py.getYear())
					.month(py.getMonth())
					.principal(py.getBillAmount())
					.stall(py.getContract().getClassTypeName()+"/"+py.getContract().getLocationNumber())
					.surcharge(py.getSurcharge())
					.interest(py.getInterest())
					.total(py.getTotal())
					.build();
			
			int year = py.getYear();
			long contractId = py.getContract().getId();
			
			if(billDataYear!=null) {
				if(billDataYear.containsKey(year)) {
					if(billDataYear.get(year).containsKey(contractId)) {
						billDataYear.get(year).get(contractId).add(bll);
					}else {
						billList = new ArrayList<Bills>();
						billList.add(bll);
						billDataYear.get(year).put(contractId, billList);
					}
				}else {
					billDataContract = new LinkedHashMap<Long, List<Bills>>();
					billList = new ArrayList<Bills>();
					billList.add(bll);
					billDataContract.put(contractId, billList);
					billDataYear.put(year, billDataContract);
				}
			}else {
				billList.add(bll);
				billDataContract.put(contractId, billList);
				billDataYear.put(year, billDataContract);
				
			}
			
		}
		
		//logo
		String officialLogo = REPORT_PATH + "logo.png";
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Bills> bbs = new ArrayList<Bills>();
		int count = 1;
		for(int year : billDataYear.keySet()) {
			count = 1;
			for(long contract : billDataYear.get(year).keySet()) {
				bbs.addAll(billDataYear.get(year).get(contract));
				
				String s1 = "";
				String s2 = "";
				String s3 = "";
				
				String h1 = "";
				String h2 = "";
				String h3 = "";
				
				
				if(count==1) {
					s1 = "\n\nSTATEMENT OF ACCOUNT\nSTALL RENTAL("+ billDataYear.get(year).get(contract).get(0).getStall()+")";
					
				}
				
				if(count==2) {
					s2 = "\n\nSTATEMENT OF ACCOUNT\nSTALL RENTAL("+ billDataYear.get(year).get(contract).get(0).getStall()+")";
					
				}
				
				if(count==3) {
					s3 = "\n\nSTATEMENT OF ACCOUNT\nSTALL RENTAL("+ billDataYear.get(year).get(contract).get(0).getStall()+")";
					
				}
				
				int index = 0;
				for(Bills b : billDataYear.get(year).get(contract)) {
					//System.out.println("Name: " + b.getName());
					//System.out.println("Year: " + b.getYear() + "\tMonth: " + b.getMonth() + "\tPrincipal: " + b.getPrincipal() + "\tSurcharge: " + b.getSurcharge() + "\tInterest: " + b.getInterest() + "\tTotal: " + b.getTotal());
					
					index++;
				}
				
				Bills bz = billDataYear.get(year).get(contract).get(index-1);
				
				if(count==1) {
					s1 += "\nAs of " + DateUtils.getMonthName(bz.getMonth()) + " " + bz.getYear();
					s1 += "\n";
					
					h1 = "Tenant No: " + bz.getTenantNo() + "\n";
					h1 += "Account No: " + bz.getAccountNo() + "\n";
					h1 += "Lessee:" + bz.getName() + "\n";
					h1 += "Location: " + bz.getStall() + "\n";
					
					h1 += "\n\n";
					h1 += "Bill no: " + bz.getBillNo();
					h1 += "\nContract Amount: " + Currency.formatAmount(bz.getPrincipal());
					h1 += "\nSurcharge: " + Currency.formatAmount(bz.getSurcharge());
					h1 += "\nInterest: " + Currency.formatAmount(bz.getInterest());
					h1 += "\nTotal Bill: " + Currency.formatAmount(bz.getTotal());
					
					param.put("PARAM_TITLE1", "Republic of the Philippines\nProvinve of South Cotabato\nMunicipality of Lake Sebu\nOFFICE OF THE MUNICIPAL TREASURER" + s1);
					param.put("PARAM_HEADER1", h1);
					try{File file = new File(officialLogo);
					FileInputStream off = new FileInputStream(file);
					param.put("PARAM_LOGO1", off);
					}catch(Exception e){e.printStackTrace();}
				}
				
				if(count==2) {
					s2 += "\nAs of " + DateUtils.getMonthName(bz.getMonth()) + " " + bz.getYear();
					s2 += "\n";
					
					h2 = "Tenant No: " + bz.getTenantNo() + "\n";
					h2 += "Account No: " + bz.getAccountNo() + "\n";
					h2 += "Lessee:" + bz.getName() + "\n";
					h2 += "Location: " + bz.getStall() + "\n";
					
					h2 += "\n\n";
					h2 += "Bill no: " + bz.getBillNo();
					h2 += "\nContract Amount: " + Currency.formatAmount(bz.getPrincipal());
					h2 += "\nSurcharge: " + Currency.formatAmount(bz.getSurcharge());
					h2 += "\nInterest: " + Currency.formatAmount(bz.getInterest());
					h2 += "\nTotal Bill: " + Currency.formatAmount(bz.getTotal());
					
					param.put("PARAM_TITLE2", "Republic of the Philippines\nProvinve of South Cotabato\nMunicipality of Lake Sebu\nOFFICE OF THE MUNICIPAL TREASURER" + s2);
					param.put("PARAM_HEADER2", h2);
					try{File file = new File(officialLogo);
					FileInputStream off = new FileInputStream(file);
					param.put("PARAM_LOGO2", off);
					}catch(Exception e){e.printStackTrace();}
				}
				
				if(count==3) {
					s3 += "\nAs of " + DateUtils.getMonthName(bz.getMonth()) + " " + bz.getYear();
					s3 += "\n";
					
					h3 = "Tenant No: " + bz.getTenantNo() + "\n";
					h3 += "Account No: " + bz.getAccountNo() + "\n";
					h3 += "Lessee:" + bz.getName() + "\n";
					h3 += "Location: " + bz.getStall() + "\n";
					
					h3 += "\n\n";
					h3 += "Bill no: " + bz.getBillNo();
					h3 += "\nContract Amount: " + Currency.formatAmount(bz.getPrincipal());
					h3 += "\nSurcharge: " + Currency.formatAmount(bz.getSurcharge());
					h3 += "\nInterest: " + Currency.formatAmount(bz.getInterest());
					h3 += "\nTotal Bill: " + Currency.formatAmount(bz.getTotal());
					
					param.put("PARAM_TITLE3", "Republic of the Philippines\nProvinve of South Cotabato\nMunicipality of Lake Sebu\nOFFICE OF THE MUNICIPAL TREASURER" + s3);
					param.put("PARAM_HEADER3", h3);
					try{File file = new File(officialLogo);
					FileInputStream off = new FileInputStream(file);
					param.put("PARAM_LOGO3", off);
					}catch(Exception e){e.printStackTrace();}
				}
				
				count++;
			}
		}
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(bbs);
		
		
		
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
	
	///////////////////////////////////TENANT INFORMATION///////////////////////////////
	
	public void save() {
		selectedData.setDate(DateUtils.convertDate(selectedData.getDateTrans(), "yyyy-MM-dd"));
		selectedData.save();
		load();
		clear();
		Application.addMessage(1, "Success", "Successfully saved");
	}
	
	public void clickItem(Tenant ten) {
		selectedData = ten;
		selectedData.setDateTrans(DateUtils.convertDateString(selectedData.getDate(), "yyyy-MM-dd"));
	}
	
	public void delete(Tenant ten) {
		ten.delete();
		load();
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void clear() {
		selectedData = Tenant.builder()
				.id(0)
				.tenantNo(Tenant.getNewTenantNumber())
				.dateTrans(new Date())
				.date(DateUtils.getCurrentDateYYYYMMDD())
				.address("Lake Sebu")
				.isActive(1)
				.build();
	}
	
	public void load() {
		String sql = "";
		if(getSearch()!=null) {
			sql = " AND fullnamet like '%"+ getSearch() +"%'";
			sql += " ORDER BY fullnamet";
		}else {
			sql += " ORDER BY tid DESC";
		}
		
		tenants = Tenant.retrieve(sql, new String[0]);
	}
	
	//////////////////////////////////////////////CONTRACT/////////////////////////////
	
	public void loadContactVal() {
		status = new ArrayList<>();
		for(TenantStatus s : TenantStatus.values()) {
			status.add(new SelectItem(s.getId(), s.getName()));
		}
		
		types = new ArrayList<>();
		for(ClassType c : ClassType.values()) {
			types.add(new SelectItem(c.getId(), c.getName()));
		}
		
		clearConract();
		
		tenantContracts = new ArrayList<>();
		for(Tenant t : Tenant.retrieve(" ORDER BY fullnamet", new String[0])) {
			tenantContracts.add(new SelectItem(t.getId(), t.getFullName()));
		}
		
		loadContract();
	}
	
	
	public void loadContract() {
		String sql = "";
		if(getSearchContract()!=null) {
			sql = " AND ten.fullnamet like '%"+ getSearchContract() +"%'";
			sql += " ORDER BY ten.fullnamet";
		}else {
			sql += " ORDER BY con.cid DESC";
		}
		contracts = TenantContract.retrieve(sql, new String[0]);
	}

	public void saveContract() {
		selectedContract.setDate(DateUtils.convertDate(selectedContract.getDateTrans(), "yyyy-MM-dd"));
		selectedContract.setContractStart(DateUtils.convertDate(selectedContract.getDateStart(), "yyyy-MM-dd"));
		selectedContract.setContractEnd(DateUtils.convertDate(selectedContract.getDateEnd(), "yyyy-MM-dd"));
		selectedContract.save();
		loadContract();
		clearConract();
		Application.addMessage(1, "Success", "Successfully saved");
	}
	
	
	public void clickItemContract(TenantContract ten) {
		selectedContract = ten;
		selectedContract.setDateTrans(DateUtils.convertDateString(selectedContract.getDate(), "yyyy-MM-dd"));
		selectedContract.setDateStart(DateUtils.convertDateString(selectedContract.getContractStart(), "yyyy-MM-dd"));
		selectedContract.setDateEnd(DateUtils.convertDateString(selectedContract.getContractEnd(), "yyyy-MM-dd"));
	}
	
	public void deleteContract(TenantContract ten) {
		ten.delete();
		loadContract();
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void clearConract() {
		selectedContract = TenantContract.builder()
				.id(0)
				.accountNo(TenantContract.getNewAccountNumber())
				.dateTrans(new Date())
				.date(DateUtils.getCurrentDateYYYYMMDD())
				.dateStart(new Date())
				.contractStart(DateUtils.getCurrentDateYYYYMMDD())
				.contractEnd("")
				.tenant(Tenant.builder().id(1).build())
				.classType(ClassType.AREA_1.getId())
				.status(TenantStatus.ACTIVE.getId())
				.isActive(1)
				.build();
	}
	
	////////////////////////////////////BILLING/////////////////////
	public void generateBilling() {
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		boolean isSuccess =  TenantBilling.createBill(year, month);
		if(isSuccess) {
			Application.addMessage(1, "Success", "Successfully billing generated");
		}else {
			Application.addMessage(2, "Error", "Error billing generation");
		}
		
		loadBill();
	}
	
	private UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
	public void saveBill() {
		UserDtls user = getUser();
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		
		if(user.getUserdtlsid()==1) {
			billSelected.setRemarks("Bill modefied by admin user:"+ getUser().getFirstname() +" on " + DateUtils.getCurrentDateMMDDYYYYTIME());
			billSelected.save();
			clearBill();
			loadBill();
			Application.addMessage(1, "Success", "Successfully modefied by admin");
		}else {
			if(billSelected!=null && billSelected.getIsPaid()==1) {
				clearBill();
				Application.addMessage(2, "Failed", "Paid transaction cannot be modefied");
			}else {
				if(year==billSelected.getYear() && month==billSelected.getMonth()) {
					billSelected.setRemarks("Bill modefied by "+ getUser().getFirstname() +" on " + DateUtils.getCurrentDateMMDDYYYYTIME());
					billSelected.save();
					clearBill();
					loadBill();
					Application.addMessage(1, "Success", "Successfully saved");
				}else {
					clearBill();
					Application.addMessage(2, "Failed", "Previous bill cannot be modefied");
				}
			}
		}
	}
	
	public void clearBill() {
		billSelected = TenantBilling
				.builder()
				.id(0)
				.billNo(TenantBilling.getNewBillNumber())
				.date(DateUtils.getCurrentDateYYYYMMDD())
				.tenant(Tenant.builder().fullName("No Tenant").build())
				.surcharge(0)
				.interest(0)
				.interestRate(0)
				.total(0)
				.isActive(1)
				.isPaid(0)
				.build();
	}
	
	public void loadBill() {
		String sql = "";
		if(getSearchBill()!=null) {
			sql = " AND (ten.fullnamet like '%"+ getSearchBill() +"%' OR bill.billno like '%"+ getSearchBill() +"%')";
		}
		bills = TenantBilling.retrieve(sql, new String[0]);
	}
	
	public void clickBillItem(TenantBilling bill) {
		billSelected = bill;
	}
	
	public void deleteBill(TenantBilling bill) {
		UserDtls user = getUser();
		
		if(user.getUserdtlsid()==1) {
			if(bill.getIsPaid()==0) {
				bill.delete();
				loadBill();
				Application.addMessage(1, "Success", "Successfully deleted bill");
			}else {
				Application.addMessage(2, "Error", "Bill is already paid");
			}
		}else {
			Application.addMessage(3, "Error", "Is not allowed to delete");
		}
	}
	
}
