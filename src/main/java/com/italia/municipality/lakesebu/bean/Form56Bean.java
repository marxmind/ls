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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

import com.italia.municipality.lakesebu.controller.Barangay;
import com.italia.municipality.lakesebu.controller.ILandType;
import com.italia.municipality.lakesebu.controller.ITaxPayerReceipt;
import com.italia.municipality.lakesebu.controller.ITaxPayor;
import com.italia.municipality.lakesebu.controller.ITaxPayorTrans;
import com.italia.municipality.lakesebu.controller.LandPayor;
import com.italia.municipality.lakesebu.controller.LandType;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.NumberToWords;
import com.italia.municipality.lakesebu.controller.PenaltyCalculation;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.TaxPayerReceipt;
import com.italia.municipality.lakesebu.controller.TaxPayor;
import com.italia.municipality.lakesebu.controller.TaxPayorTrans;
import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.PenalyMonth;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Numbers;
import com.italia.municipality.lakesebu.utils.Whitelist;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author mark
 * @since 11/17/2016
 * @version 1.0
 *
 */
@Named
@ViewScoped
public class Form56Bean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 154654676443L;

	private List<ITaxPayorTrans> trans;
	
	private String receiveFrom;
	private String transDate;
	private String amountInWords;
	private String basicTax;
	private String specialEducationFund;
	private String signed1;
	private String signed2;
	private String checkNo;
	private String checkDate;
	private String scNo;
	private Double grandTotal;
	
		
	private List<ITaxPayerReceipt> receipts;
	private ITaxPayerReceipt receiptSelected;
	private ITaxPayorTrans payorTransData;
	private List landTypes;
	private long payorId;
	private int landId;
	private int idFromYear;
	private List fromYear;
	private int idToYear;
	private List toYear;
	private String idPaymentType;
	private List paymentTypes;
	private String findName;
	private Date findFromDate;
	private Date findToDate;
	private String keyPress;
	private double transAmountGrandTotal;
	private Date tmpTransDate;
	
	private List statusList;
	private int statusId;
	private List paymentList;
	private int paymentId;
	
	private ITaxPayor payorSelected;
	private LandPayor landSelected;
	private List<ITaxPayor> payorLand;
	private List<LandPayor> lands;
	private List barangayListSearch;
	private int barangayIdSearch;
	private String searchParam;
	private ITaxPayerReceipt payerReceipt;
	
	private List<ITaxPayor> payorSearchData;
	private String payorSearchParam;
	
	private boolean specialCase;
	private boolean disAbleFullPenalty;
	private String signatory1;
	private String signatory2;
	
	private List<Boolean> listColumns;
	
	private String selectedOwner;
	
	private int yearSelectedFromId;
	private List yearSelectedFroms;
	private int yearSelectedToId;
	private List yearSelectedTos;
	private LandPayor landForPaymentData;
	
	public void onToggle(ToggleEvent e){
		int id = (Integer) e.getData();
		boolean visible = e.getVisibility() == Visibility.VISIBLE;
		System.out.println("columns id " + id + " visibility : " + visible);
		listColumns.set(id, visible);
	}
	
	public void specialCaseCheck(){
		if(getSpecialCase()){
			
			if(checkIsAdjustedRow()){
				setSpecialCase(false);
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Adjustment is activated. Please untick adjustment if you want to use the special case.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}else{	
				setDisAbleFullPenalty(false);
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Special case has been activated. Please note that adjustment is not allowed.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}else{
			setDisAbleFullPenalty(true);
			updateTotal();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Special case has been removed. Amount has been changed.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	private boolean checkIsAdjustedRow(){
		if(receipts!=null && receipts.size()>0){
		for(int i=0;i<receipts.size();i++){
			if(receipts.get(i).getIsAdjust()){
				return true;
			}
		}
			
		}
		return false;
	}
	
	public void dateSelected(){
		setTransDate(DateUtils.convertDate(getTmpTransDate(),"yyyy-MM-dd"));
	}
	
	public String getKeyPress() {
		keyPress = "findId";
		return keyPress;
	}
	public void setKeyPress(String keyPress){
		this.keyPress = keyPress;
	}
	
	@PostConstruct
	public void init(){
		
		System.out.println("initialize.....");
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		trans = new ArrayList<ITaxPayorTrans>();
		String sql = "SELECT * FROM "+ dbTax +".taxpayortrans trans, "+ dbTax +".taxpayor pay, "+ dbWeb +".userdtls u WHERE trans.paytransisactive=1 AND trans.payorid=pay.payorid AND trans.userdtlsid = u.userdtlsid AND (trans.payortransdate>=? AND trans.payortransdate<=?)";
		String[] params = new String[2];
		params[0] = DateUtils.convertDate(getFindFromDate(),"yyyy-MM-dd");
		params[1] = DateUtils.convertDate(getFindToDate(),"yyyy-MM-dd");
		
		sql += " AND trans.transtatus=1 Order by trans.payortransid DESC limit 10";
		
		for(ITaxPayorTrans tran : TaxPayorTrans.retrieve(sql, params)){
			tran.setLandPayor(paidFor(tran));
			trans.add(tran);
		}
		
		setTransAmountGrandTotal(Numbers.roundOf(TaxPayorTrans.retrieveTotal(sql, params),2));
		
		//create new form
		newForm();
	}
	
	private void saveLoad(String orNumber, String transDate) {
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		trans = new ArrayList<ITaxPayorTrans>();
		String sql = "SELECT * FROM "+ dbTax +".taxpayortrans trans, "+ dbTax +".taxpayor pay, "+ dbWeb +".userdtls u WHERE trans.paytransisactive=1 AND trans.payorid=pay.payorid AND trans.userdtlsid = u.userdtlsid AND (trans.payortransdate>=? AND trans.payortransdate<=?)";
		String[] params = new String[2];
		params[0] = transDate;
		params[1] = transDate;
		
		sql += " AND trans.payorscno like '%"+ orNumber +"%'";
		
		for(ITaxPayorTrans tran : TaxPayorTrans.retrieve(sql, params)){
			tran.setLandPayor(paidFor(tran));
			trans.add(tran);
		}
		
		sql += " AND trans.transtatus=1";
		setTransAmountGrandTotal(Numbers.roundOf(TaxPayorTrans.retrieveTotal(sql, params),2));
	}
	
	public void search(){
		System.out.println("initialize.....");
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		trans = new ArrayList<ITaxPayorTrans>();
		String sql = "SELECT * FROM "+ dbTax +".taxpayortrans trans, "+ dbTax +".taxpayor pay, "+ dbWeb +".userdtls u WHERE trans.paytransisactive=1 AND trans.payorid=pay.payorid AND trans.userdtlsid = u.userdtlsid AND (trans.payortransdate>=? AND trans.payortransdate<=?)";
		String[] params = new String[2];
		params[0] = DateUtils.convertDate(getFindFromDate(),"yyyy-MM-dd");
		params[1] = DateUtils.convertDate(getFindToDate(),"yyyy-MM-dd");
		
		if(getFindName()!=null && !getFindName().isEmpty()){
			int orno = 0;
			try{
				orno = Integer.valueOf(getFindName().trim());
				sql += " AND trans.payorscno like '%"+ getFindName().replaceAll("--", "") +"%'";
			}catch(Exception e){}
			
			if(orno==0){
				String val = getFindName().replaceAll("--", "").trim();
				
				if("POSTED".equalsIgnoreCase(val)){
					sql += " AND trans.transtatus=1 ";
				}else if("CANCELLED".equalsIgnoreCase(val)){
					sql += " AND trans.transtatus=2 ";
				}else if("FULL".equalsIgnoreCase(val)){
					sql += " AND trans.paytype=1 ";
				}else if("PARTIAL".equalsIgnoreCase(val)){
					sql += " AND trans.paytype=2 ";	
				}else{
					sql += " AND pay.payorname like '%"+ val +"%'";
				}	
			}
			
		}
		
		for(ITaxPayorTrans tran : TaxPayorTrans.retrieve(sql, params)){
			tran.setLandPayor(paidFor(tran));
			trans.add(tran);
		}
		Collections.reverse(trans);
		
		sql += " AND trans.transtatus=1";
		setTransAmountGrandTotal(Numbers.roundOf(TaxPayorTrans.retrieveTotal(sql, params),2));
	}
	
	private LandPayor paidFor(ITaxPayorTrans tran){
		LandPayor land = new LandPayor();
		try{
		String sql = "SELECT * FROM taxpayortransreceipt WHERE payortransid=? LIMIT 1";
		String[] params = new String[1];
		params[0] = tran.getId()+"";
		ITaxPayerReceipt rec = TaxPayerReceipt.retrieveLand(sql, params).get(0);
		land = rec.getLandPayor();
		}catch(Exception e){}
		return land;
	}
	
	private void generatingNewSCNo(){
		
		if(getScNo()==null || getScNo().isEmpty()){
		
		int scno = TaxPayorTrans.getLatestSCNo();
		scno = scno==0? 1 : scno + 1;
		String scnumber = scno+"";
		int cnt = scnumber.length();
		
		switch(cnt){
		//case 0 : setScNo("0000000"); break;
		case 1 : setScNo("000000"+scno); break;
		case 2 : setScNo("00000"+scno); break;
		case 3 : setScNo("0000"+scno); break;
		case 4 : setScNo("000"+scno); break;
		case 5 : setScNo("00"+scno); break;
		case 6 : setScNo("0"+scno); break;
		case 7 : setScNo(""+scno); break;
		}
		
		}
		
	}
	
	public List<String> autoPayorName(String query){
		String sql = "SELECT DISTINCT  payorname from taxpayor WHERE payisactive=1 AND  payorname like '%" + Whitelist.remove(query) + "%' LIMIT 20";
		String[] params = new String[0];
		List<String> result = new ArrayList<>();
		
		int len = query.length();
		
		if(!query.trim().isEmpty() && len>3){
			for(ITaxPayor payor : TaxPayor.retrievePayorName(sql, params)){
				System.out.println("Payor name : " + payor.getFullName());
				result.add(payor.getFullName());
			}
		}
		return result;
	}
	
	public List<String> autoLocation(String query){
		String sql = "SELECT DISTINCT  bgname from barangay WHERE  bgname like '%" + Whitelist.remove(query) + "%' LIMIT 20";
		String[] params = new String[0];
		List<String> result = new ArrayList<>();
		
		int len = query.length();
		
		if(!query.trim().isEmpty() && len>3){
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			try{
			conn = TaxDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			if(params!=null && params.length>0){
				
				for(int i=0; i<params.length; i++){
					ps.setString(i+1, params[i]);
				}
				
			}
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				result.add(rs.getString("bgname"));
			}
			
			
			rs.close();
			ps.close();
			TaxDatabaseConnect.close(conn);
			}catch(SQLException sl){}
		}
		return result;
	}
	
	
	public void onRowEdit(RowEditEvent event) {
		System.out.println("on row edit : " + ((ITaxPayerReceipt) event.getObject()).getLocation());
        FacesMessage msg = new FacesMessage("Transaction Edited", ((ITaxPayerReceipt) event.getObject()).getId()+"");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
     
    public void onRowCancel(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Transaction Cancelled", ((ITaxPayerReceipt) event.getObject()).getId()+"");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
     
    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        System.out.println("Row Key   "+ event.getRowKey());
        System.out.println("Row Index   "+ event.getRowIndex());
        System.out.println("land id " + getLandId());
        System.out.println("Column Field " +event.getColumn().getField());
        if(landId>0){
        String sql = "SELECT * FROM landtype WHERE landid=?";
        String[] params = new String[1];
        params[0] = getLandId()+"";
		for(ILandType type : LandType.retrieve(sql, params)){
			receipts.get(event.getRowIndex()).setLandType(type);
		}
		setLandId(0);
		
		
        }
        
        updateTotal();
        	
       
    }
	
    private ITaxPayerReceipt calculateAdjustment(ITaxPayerReceipt rpt, double overallTotal,double taxDue,  double fullpayment, double penAmount){
    	
    	double tmpFullPayment = Numbers.formatDouble(fullpayment);
		double tmpPenaltyAmount = Numbers.formatDouble(penAmount);
		double tmpOverAllTotal = tmpFullPayment + tmpPenaltyAmount;
		
		int retVal = Double.compare(tmpOverAllTotal, overallTotal);
		
		if(retVal > 0 ){
			
			penAmount = overallTotal - tmpFullPayment;
			
			rpt.setTaxDue(Numbers.formatDouble(taxDue));
			rpt.setFullPayment(Numbers.formatDouble(fullpayment));
			rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
			
		}else if( retVal < 0){
			
			rpt.setTaxDue(Numbers.formatDouble(taxDue));
			rpt.setFullPayment(Numbers.formatDouble(fullpayment));
			rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
			
		}else{
		
			rpt.setTaxDue(Numbers.formatDouble(taxDue));
			rpt.setFullPayment(Numbers.formatDouble(fullpayment));
			rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
			
		}
    	
    	return rpt;
    }
    
    @Deprecated
    private ITaxPayerReceipt calculateOnlyPenaltyTotal(ITaxPayerReceipt rpt, double tmpassTotal){
    	double taxDue = 0d;//tmpassTotal * 0.01;
		double penAmount = 0d;
		double fullpayment = 0d;
		int cntYear = 0;
		double overallTotal = 0d;
		double debtAmnt = 0d;
		double shareAmnt = 0d;
		double penaltyRate = 0d;
		double clientPaid = 0d;
		//year calculation
		int fromYear = DateUtils.getCurrentYear();
		int toYear = DateUtils.getCurrentYear();
		try{fromYear = Integer.valueOf(rpt.getFromYear());}catch(Exception e){}
		try{toYear = Integer.valueOf(rpt.getToYear());}catch(Exception e){}
		int month = Integer.valueOf(getTransDate().split("-")[1]); 
		int year = Integer.valueOf(getTransDate().split("-")[0]);
    	
		if(fromYear<year && toYear<year){
			penAmount = rpt.getPenaltyPercent();
			if(penAmount==0){
				for(int yer=fromYear; yer<=toYear; yer++ ){
					penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
				}
				cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
				taxDue = (tmpassTotal * 0.01);
				fullpayment = rpt.getFullPayment();
				penAmount = fullpayment * (penAmount/cntYear);
				
				rpt.setTaxDue(Numbers.formatDouble(taxDue));
				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
    			overallTotal = fullpayment + penAmount;
    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    			
    			//register actual amount
				setGrandTotal(getGrandTotal() + overallTotal);
			}else{

				penAmount = 0d;
				for(int yer=fromYear; yer<=toYear; yer++ ){
					penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
				}
				System.out.println("penalty: " + penAmount);
				cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
				taxDue = (tmpassTotal * 0.01);
				fullpayment = taxDue * cntYear;
				penAmount = fullpayment * (penAmount/cntYear);
				
				rpt.setTaxDue(Numbers.formatDouble(taxDue));
				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
    			overallTotal = fullpayment + penAmount;
    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    			
    			//register actual amount
    			setGrandTotal(getGrandTotal() + overallTotal);
			}
		}
		
		
		/**
		 * Manual
		 */
		
		//the same year
		if(fromYear==year && toYear==year){
			
			penAmount = rpt.getPenaltyPercent();
			if(penAmount==0){
				fullpayment = rpt.getFullPayment();
				taxDue = fullpayment;
				
				if(1==month || 2==month || 3==month){//January to March 10% discount if paying for current year
					penAmount = taxDue * 0.10;
					penAmount = -penAmount;
				}else{
					penAmount = taxDue * penAmount;
				}
				
				
				rpt.setTaxDue(Numbers.formatDouble(taxDue));
				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
    			overallTotal = fullpayment + penAmount;
    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    			System.out.println("same year...");
    			//register actual amount
    			setGrandTotal(getGrandTotal() + overallTotal);
			}else{
				
			}
		}
		
		//previous year to current year
		if(fromYear<year && toYear==year){
			
			penAmount = 0d;
				for(int yer=fromYear; yer<=toYear; yer++ ){
					penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
				}
				cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
				taxDue = (tmpassTotal * 0.01);
				fullpayment = rpt.getFullPayment();
				penAmount = fullpayment * (penAmount/cntYear);
				
				rpt.setTaxDue(Numbers.formatDouble(taxDue));
				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
    			overallTotal = fullpayment + penAmount;
    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    			
    			//register actual amount
    			setGrandTotal(getGrandTotal() + overallTotal);
		}
    	
		//advance payment
		if(fromYear>year && toYear>year && month<=12){
			penAmount = 0d;
			cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
				taxDue = (tmpassTotal * 0.01);
				fullpayment = rpt.getFullPayment();
				penAmount = fullpayment * 0.20;
				penAmount = -penAmount;
				
				rpt.setTaxDue(Numbers.formatDouble(taxDue));
				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
    			overallTotal = fullpayment + penAmount;
    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    			
    			//register actual amount
    			setGrandTotal(getGrandTotal() + overallTotal);
			}
		
		if(fromYear<=year && toYear>year){
			
			penAmount = 0d;
				int cntY = 0;
				int remY = 0;
				for(int yer=fromYear; yer<=year; yer++ ){
					penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
					cntY++;
				}
				double discount = 0d;
				cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
				taxDue = (tmpassTotal * 0.01);
				fullpayment = rpt.getFullPayment();
				
				remY = cntYear-cntY;
				discount = (taxDue * remY) * 0.20;
				penAmount = (taxDue * cntY) * (penAmount/cntY);
				penAmount = penAmount - discount;
				
				rpt.setTaxDue(Numbers.formatDouble(taxDue));
				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
    			overallTotal = fullpayment + penAmount;
    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    			
    			//register actual amount
    			setGrandTotal(getGrandTotal() + overallTotal);
		}
		
    	return rpt;
    }
    
    public void updateTotal(){
    	Double amnt = 0d;
    	Double tmpamnt = 0d;
    	List<ITaxPayerReceipt> tmpreceipts = new ArrayList<>();
    	setGrandTotal(amnt);
    	double actualTotalAmnt = 0d;
    	
    	if(receipts.size()>0){
    		for(ITaxPayerReceipt rpt : receipts){
    			
    			double tmpassTotal = rpt.getAssValueLand();// + rpt.getAssValueImprv();
    			if(rpt.getAssValueImprv()>0) {
    				tmpassTotal = rpt.getAssValueImprv();
    			}
    			rpt.setAssValueTotal(Numbers.formatDouble(tmpassTotal));
    			
    			double taxDue = 0d;//tmpassTotal * 0.01;
    			double penAmount = 0d;
    			double fullpayment = 0d;
    			int cntYear = 0;
    			double overallTotal = 0d;
    			double debtAmnt = 0d;
    			double shareAmnt = 0d;
    			double penaltyRate = 0d;
    			double clientPaid = 0d;
    			//year calculation
    			int fromYear = DateUtils.getCurrentYear();
    			int toYear = DateUtils.getCurrentYear();
    			try{fromYear = Integer.valueOf(rpt.getFromYear());}catch(Exception e){}
    			try{toYear = Integer.valueOf(rpt.getToYear());}catch(Exception e){}
    			int month = Integer.valueOf(getTransDate().split("-")[1]); 
    			int year = Integer.valueOf(getTransDate().split("-")[0]);
    			
    			/**
    			 * partial payment formula
    			 * 1 - stand for body total amount
    			 * .<month> e.g. 1.72 72 is stand for the month penalty when he paying for tax
    			 * 
    			 * amount paid by customer / 1.<month penalty rate> = fullpayment
    			 * amount paid by customer - fullpayment = penalty rate
    			 * fullpayment + penalty rate = total amount
    			 * 
    			 */
    			
    			//this apply only for special cases
    			if(getSpecialCase()){
    				
    				//this code apply if user input fullpayment and zeroing the penalty
    				//aim is to compute the penalty rate and total amount
    				/*if(rpt.getPenaltyPercent()==0){
    					
    					rpt = calculateOnlyPenaltyTotal(rpt,tmpassTotal);
    					
    				}else{*/
    				//these line of codes applicable if user provide value for fullpayment and penalty rate
    				taxDue = (tmpassTotal * 0.01);
					fullpayment = rpt.getFullPayment();
					penAmount = rpt.getPenaltyPercent();
					overallTotal = fullpayment + penAmount;
    						
    				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));		
    				
    				//}
        			
    			}else{
    			
    			boolean isAdjust = rpt.getIsAdjust();
    			boolean isCase = rpt.getIsCase();
    			if(fromYear<year && toYear<year){
    				penAmount = rpt.getPenaltyPercent();
    				//rpt.setRemarks(null);
    				if(isAdjust){
    					/**
    					 * Removed due to partial payment
    					 */
    					/*taxDue = (tmpassTotal * 0.01);
    					fullpayment = rpt.getFullPayment();*/
    					
    					penAmount = 0d;
    					for(int yer=fromYear; yer<=toYear; yer++ ){
    						penaltyRate += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
    					}
    					cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
    					//partial payment
    					
    					System.out.println("Penalty rate " + penaltyRate);
    					penaltyRate = penaltyRate / cntYear;
    					penaltyRate += 1; //base
    					System.out.println("new Penalty rate " + penaltyRate);
    					
    					clientPaid = rpt.getOverallTotal(); 
    					taxDue = (tmpassTotal * 0.01);
    					fullpayment = clientPaid / penaltyRate;
    					penAmount = clientPaid - fullpayment;
    					overallTotal = fullpayment + penAmount;
    					
    					/**
    					 * Below is the fixing for the issue if the the penalty rounded are not accurate to total amount
    					 */
    					rpt = calculateAdjustment(rpt, overallTotal, taxDue, fullpayment, penAmount);
    					
    					
    					//stable
    					/*rpt.setTaxDue(Numbers.formatDouble(taxDue));
                			rpt.setFullPayment(Numbers.formatDouble(fullpayment));
            				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
                			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));*/
            		
    				}else if(isCase){
    					
    					penAmount = rpt.getPenaltyPercent();
    					
    					if(penAmount==0){
    						
	    					for(int yer=fromYear; yer<=toYear; yer++ ){
	    						penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
	    					}
	    					
	    					cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
	    					taxDue = (tmpassTotal * 0.01);
	    					fullpayment = rpt.getFullPayment();
	    					penAmount = fullpayment * (penAmount/cntYear);
	    					
	    					taxDue = Numbers.formatDouble(taxDue);
	    					fullpayment = Numbers.formatDouble(fullpayment);
	    					penAmount = Numbers.formatDouble(penAmount);
	    					
	    					rpt.setTaxDue(taxDue);
	    					rpt.setFullPayment(fullpayment);
	    					rpt.setPenaltyPercent(penAmount);
	    	    			overallTotal = fullpayment + penAmount;
	    	    			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
	    	    			//register actual amount
	    	    			actualTotalAmnt +=overallTotal;
    					
    					}else{
    	    			
	    	    			taxDue = (tmpassTotal * 0.01);
	    					fullpayment = rpt.getFullPayment();
	    					penAmount = rpt.getPenaltyPercent();
	    					overallTotal = fullpayment + penAmount;
	        						
	        				rpt.setTaxDue(Numbers.formatDouble(taxDue));
	            			//rpt.setFullPayment(Numbers.formatDouble(fullpayment));
	        				//rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
	            			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));	
    					}
    				}else{
    					penAmount = 0d;
    					for(int yer=fromYear; yer<=toYear; yer++ ){
    						penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
    					}
    					System.out.println("penalty: " + penAmount);
	    				cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
	    				taxDue = (tmpassTotal * 0.01);
	    				fullpayment = taxDue * cntYear;
	    				penAmount = fullpayment * (penAmount/cntYear);
	    				
	    				fullpayment = Numbers.formatDouble(fullpayment);
	    				penAmount = Numbers.formatDouble(penAmount);
	    				
	    				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(fullpayment);
        				rpt.setPenaltyPercent(penAmount);
            			overallTotal = fullpayment + penAmount;
            			overallTotal = Numbers.formatDouble(overallTotal);
            			rpt.setOverallTotal(overallTotal);
            			
            			//register actual amount
        				actualTotalAmnt +=overallTotal;
    				}
    			}
    			
    			
    			/**
    			 * Manual
    			 */
    			
    			//the same year
    			if(fromYear==year && toYear==year){
    				
    				penAmount = rpt.getPenaltyPercent();
    				//rpt.setRemarks(null);	
    				if(isAdjust){
    					
    					//remove change implementation
        				//fullpayment = rpt.getFullPayment();
        				//taxDue = tmpassTotal * 0.01;
        				
    					
    					//partial payment
    					penaltyRate = PenaltyCalculation.monthPenalty(year, PenalyMonth.month(month));
    					penaltyRate = penaltyRate + 1;
    					clientPaid = rpt.getOverallTotal(); 
    					taxDue = (tmpassTotal * 0.01);
    					fullpayment = clientPaid / penaltyRate;
    					penAmount = clientPaid - fullpayment;
    					overallTotal = fullpayment + penAmount;
    					
    					/**
    					 * Below is the fixing for the issue if the the penalty rounded are not accurate to total amount
    					 */
    					rpt = calculateAdjustment(rpt, overallTotal, taxDue, fullpayment, penAmount);
    					
    				}else{
    					penAmount = PenaltyCalculation.monthPenalty(year, PenalyMonth.month(month));
        				fullpayment = tmpassTotal * 0.01;
        				taxDue = fullpayment;
        				
        				if(1==month || 2==month || 3==month){//January to March 10% discount if paying for current year
        					penAmount = taxDue * 0.10;
        					penAmount = -penAmount;
        				}else{
        					penAmount = taxDue * penAmount;
        				}
        				
        				
        				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
        				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
            			overallTotal = fullpayment + penAmount;
            			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
            			System.out.println("same year...");
            			//register actual amount
        				actualTotalAmnt +=overallTotal;
    				}
    				
    				
    				
    			}
    			//previous year to current year
    			if(fromYear<year && toYear==year){
    				penAmount = rpt.getPenaltyPercent();
    				//rpt.setRemarks(null);
    				if(isAdjust){
    					
    					//change implementation
    					//taxDue = (tmpassTotal * 0.01);
    					//fullpayment = rpt.getFullPayment();
    					
    					penAmount = 0d;
    					for(int yer=fromYear; yer<=toYear; yer++ ){
    						penaltyRate += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
    					}
    					
    					cntYear = (toYear - fromYear) + 1; // 2/17/2017
    					penaltyRate = penaltyRate / cntYear; //added only 2/17/2017 - to fix the issue in penalty
    					
    					penaltyRate = penaltyRate + 1;
    					clientPaid = rpt.getOverallTotal(); 
    					taxDue = (tmpassTotal * 0.01);
    					fullpayment = clientPaid / penaltyRate;
    					penAmount = clientPaid - fullpayment;
    					overallTotal = fullpayment + penAmount;
    					
    					/**
    					 * Below is the fixing for the issue if the the penalty rounded are not accurate to total amount
    					 */
    					rpt = calculateAdjustment(rpt, overallTotal, taxDue, fullpayment, penAmount);
    					
    				}else{
    					penAmount = 0d;
    					for(int yer=fromYear; yer<=toYear; yer++ ){
    						penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
    					}
	    				cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
	    				taxDue = (tmpassTotal * 0.01);
	    				fullpayment = taxDue * cntYear;
	    				penAmount = fullpayment * (penAmount/cntYear);
	    				
	    				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
        				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
            			overallTotal = fullpayment + penAmount;
            			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
            			
            			//register actual amount
        				actualTotalAmnt +=overallTotal;
    				}
    			}
    			
    			//advance payment
    			if(fromYear>year && toYear>year && month<=12){
    				penAmount = rpt.getPenaltyPercent();
    				//rpt.setRemarks(null);
    				if(isAdjust){
    					
    					cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
        				taxDue = (tmpassTotal * 0.01);
        				fullpayment = taxDue * cntYear;
        				penAmount = fullpayment * 0.20;
        				penAmount = -penAmount;
        				
        				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
        				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
        				
        				
        				
            			//rpt.setOverallTotal(Numbers.formatDouble(overallTotal)); //commented 2017-12-18
        				overallTotal = rpt.getOverallTotal();//added 2017-12-18
            			actualTotalAmnt +=overallTotal;//added 2017-12-18
            			
            			double tempTotal = fullpayment + penAmount;
            			int intVal = Double.compare(tempTotal, overallTotal);
            			
            			if(intVal > 0){
            				System.out.println("intVal > 0 = " + penAmount);
            			}else if(intVal < 0){
            				
            				penAmount =  overallTotal - fullpayment;
            				if(penAmount>0){
            					penAmount = -penAmount;
            				}
            				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
            				System.out.println("intVal < 0 = " + penAmount);
            			}else{
            				System.out.println("intVal == 0 " + penAmount);
            			}
            			
    				}else{
    					cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
        				taxDue = (tmpassTotal * 0.01);
        				fullpayment = taxDue * cntYear;
        				penAmount = fullpayment * 0.20;
        				penAmount = -penAmount;
        				
        				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
        				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
            			overallTotal = fullpayment + penAmount;
            			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
            			
            			//register actual amount
        				actualTotalAmnt +=overallTotal;
    				}
    				
    				
    			}
    			
    			if(fromYear<=year && toYear>year){
    				penAmount = rpt.getPenaltyPercent();
    				//rpt.setRemarks(null);
    				if(isAdjust){
    					
    					int cntY = 0;
    					int remY = 0;
    					for(int yer=fromYear; yer<=year; yer++ ){
    						penaltyRate += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
    						cntY++;
    					}
    					
    					
    					double discount = 0d;
    					cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
    					remY = cntYear-cntY;
    					discount = remY * 0.20;
    					
    					//Start added for penalty fixes 02/17/2017
    					double yearWithPenalty = 0d;
    					yearWithPenalty = (year - fromYear) ;
    					penaltyRate = penaltyRate / yearWithPenalty;
    					//end added for penalty fixes 02/17/2017
    					
    					penaltyRate = penaltyRate + 1;
    					clientPaid = rpt.getOverallTotal(); 
    					taxDue = (tmpassTotal * 0.01);
    					fullpayment = clientPaid / (penaltyRate - discount);
    					penAmount = clientPaid - fullpayment;
    					overallTotal = fullpayment + penAmount;
    					
        				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
        				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
            			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
    					
    				}else{
    					
    					penAmount = 0d;
    					int cntY = 0;
    					int remY = 0;
    					for(int yer=fromYear; yer<=year; yer++ ){
    						penAmount += PenaltyCalculation.monthPenalty(yer, PenalyMonth.month(month));
    						cntY++;
    					}
    					double discount = 0d;
    					cntYear = (toYear - fromYear) + 1; // plus one to get the actual count of the year
        				taxDue = (tmpassTotal * 0.01);
        				fullpayment = taxDue * cntYear;
        				
        				remY = cntYear-cntY;
        				discount = (taxDue * remY) * 0.20;
        				penAmount = (taxDue * cntY) * (penAmount/cntY);
        				penAmount = penAmount - discount;
        				
        				rpt.setTaxDue(Numbers.formatDouble(taxDue));
        				rpt.setFullPayment(Numbers.formatDouble(fullpayment));
        				rpt.setPenaltyPercent(Numbers.formatDouble(penAmount));
            			overallTotal = fullpayment + penAmount;
            			rpt.setOverallTotal(Numbers.formatDouble(overallTotal));
            			
            			//register actual amount
        				actualTotalAmnt +=overallTotal;
    				}
    					
    			}
    			
    			}
    			
    			setGrandTotal(getGrandTotal() + overallTotal);
    			
    			tmpreceipts.add(rpt);
    		}
    	}
    	receipts = new ArrayList<>();
    	receipts = tmpreceipts;
    	setGrandTotal(Numbers.formatDouble(getGrandTotal()));
    	generateWords();
    	
    }
    
    private double getPenaltyYear(int year){
    	
    	switch(year){
    	case 1986 : return 0.24;
    	case 1987 : return 0.24;
    	case 1988 : return 0.24;
    	case 1989 : return 0.24;
    	case 1990 : return 0.24;
    	case 1991 : return 0.24;
    	case 1992 : return 0.72;
    	case 1993 : return 0.72;
    	case 1994 : return 0.72;
    	case 1995 : return 0.72;
    	case 1996 : return 0.72;
    	case 1997 : return 0.72;
    	case 1998 : return 0.72;
    	case 1999 : return 0.72;
    	case 2000 : return 0.72;
    	case 2001 : return 0.72;
    	case 2002 : return 0.72;
    	case 2003 : return 0.72;
    	case 2004 : return 0.72;
    	case 2005 : return 0.72;
    	case 2006 : return 0.72;
    	case 2007 : return 0.72;
    	case 2008 : return 0.72;
    	case 2009 : return 0.72;
    	case 2010 : return 0.72;
    	case 2011 : return 0.72;
    	case 2012 : return 0.72;
    	case 2013 : return 0.72;
    	case 2014 : return 0.72;
    	case 2015 : return 0.48;
    	case 2016 : return 0.24;
    	case 2017 : return 0.24;
    	}
    	
    	return 0;
    }
    
    private double getPenaltyMonth(int month){
    	switch(month){
    	case 1 : return 0;
    	case 2 : return 0;
    	case 3 : return 0;
    	case 4 : return 0.08;
    	case 5 : return 0.10;
    	case 6 : return 0.12;
    	case 7 : return 0.14;
    	case 8 : return 0.16;
    	case 9 : return 0.18;
    	case 10 : return 0.20;
    	case 11 : return 0.22;
    	case 12 : return 0.24;
    	}
    	return 0;
    }
    
    private void generateWords(){
    	String result = "";
    	NumberToWords numberToWords = new NumberToWords();
		result = numberToWords.changeToWords(getGrandTotal()).toUpperCase();
		setAmountInWords(result);
    }
    
	public void saveData(){
		String recFrom="";
		String wordsAmount="";
		try{
		recFrom = getReceiveFrom().trim();
		}catch(NullPointerException e){
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide Received From", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);}
		
		try{
			wordsAmount = getAmountInWords().trim();
			}catch(NullPointerException e){
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount in words in not yet specify", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);}
		
		boolean error = false;
		if(receipts.size()>0){
			for(ITaxPayerReceipt chk : receipts){
				if(chk.getPayor()==null){
					error = true; 
					break;
				}
				
			}
			
			if(error){
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide declared owner", "");
	            FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
		}else{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide required data", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		if(!error){
		
		if(!recFrom.isEmpty() && !wordsAmount.isEmpty() && receipts.size()>0 && getPayorSelected()!=null){
			String sql = "SELECT * FROM taxpayor WHERE payorname=?";
			String[] params = new String[1];
			params[0] = recFrom;
			
			ITaxPayor pay = getPayorSelected();
			
			//saving on taxpayortrans table
			ITaxPayorTrans trans = new TaxPayorTrans();
			if(getPayorTransData()!=null){
				trans = getPayorTransData();
			}else{
				trans.setIsactive(1);
			}
			if(getStatusId()==2){
				trans.setAmount(0.00);
			}else{
				trans.setAmount(getGrandTotal());
			}
			
			trans.setIsSpecialCase(getSpecialCase()==true? 1 : 0);
			trans.setSigned1(getSigned1());
			trans.setSigned2(getSigned2());
			System.out.println("saving date: " + DateUtils.convertDate(getTmpTransDate(), "yyyy-MM-dd"));
			trans.setTransDate(DateUtils.convertDate(getTmpTransDate(), "yyyy-MM-dd"));
			trans.setAmountInWords(getAmountInWords());
			trans.setScNo(getScNo());
			trans.setAccountFormNo(56);
			trans.setCheckNo(getCheckNo());
			trans.setCheckDate(getCheckDate());
			trans.setTaxPayor(pay);
			trans.setStatus(getStatusId()==0? 1 : getStatusId());
			trans.setPaymentType(getPaymentId()==0? 1 : getPaymentId());
			trans.setUserDtls(Login.getUserLogin().getUserDtls());
			trans = TaxPayorTrans.save(trans); 
			String typeIns = "";
			String type = "";
			for(ITaxPayerReceipt rpt : receipts){
				
				if(rpt.getLandOwnerName()==null){
					rpt.setLandOwnerName(getReceiveFrom());
				}
				if("Last Name, First Name".equalsIgnoreCase(rpt.getLandOwnerName())){
					rpt.setLandOwnerName(getReceiveFrom());
				}
				
				rpt.setInstallmentRangeAndType(rpt.getFromYear()+":"+rpt.getToYear()+":"+rpt.getInstallmentType());
				rpt.setPayorTrans(trans);
				rpt.setIsActive(1);
				rpt.setUserDtls(Login.getUserLogin().getUserDtls());
				rpt.save();
				
			}
			
			String today = DateUtils.getCurrentDateYYYYMMDD();
			setTransDate(trans.getTransDate());
			if(!today.equalsIgnoreCase(getTransDate())) {
				setFindName(getReceiveFrom());
				setFindFromDate(DateUtils.convertDateString(trans.getTransDate(), "yyyy-MM-dd"));
				setFindToDate(DateUtils.convertDateString(trans.getTransDate(), "yyyy-MM-dd"));
				search();
			}else {
				saveLoad(getScNo(), getTransDate());
			}
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information has been successfully saved", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			newForm();
		}else{
			String str="";
			if(getReceiveFrom()==null){ str="Receive From is empty ";}
			if(getAmountInWords()==null){str += "Amount in words is empty";}
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please provide fill up fields", str);
            FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		}
	}
	
	public String graphChart(){
		return "graph56.xhtml";
	}
	
	private ITaxPayor savePayor(String payorName){
		ITaxPayor pay = new TaxPayor();
		pay.setFullName(payorName.toUpperCase());
		pay.setAddress("");
		pay.setIsactive(1);
		pay = TaxPayor.save(pay);
		return pay;
	}
	
	public void deleteRow(ITaxPayorTrans trans){
		System.out.println("delete row ");
		if(trans.getStatus()==1){
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Posted data is not allowed for deletion", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}else{
			trans.delete();
			init();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully deleted", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void deleteCell(){
		if(getReceiptSelected()!=null){
			receipts.remove(getReceiptSelected());
			getReceiptSelected().delete();
			updateTotal();
			//saveData();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully deleted", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void clearFields(){
		setScNo(null);
		setPayorTransData(null);
		setReceiptSelected(null);
		setGrandTotal(0d);
		setReceiveFrom(null);
		setAmountInWords(null);
		setIdFromYear(0);
		setIdToYear(0);
		setStatusId(1);
		setPaymentId(1);
		setSignatory1("ALVIN M. BATOL, CPA");
		setSignatory2("FERDINAND L. LOPEZ");
		setGrandTotal(0.0);
		setTransDate(DateUtils.getCurrentDateYYYYMMDD());
		setTmpTransDate(DateUtils.getDateToday());
		setPayorSearchParam(null);
		setSearchParam(null);
	}
	
	public void newForm(){
		
		listColumns = Arrays.asList(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
				
		listColumns.set(2, false);
		listColumns.set(3, false);
		listColumns.set(4, false);
		
		clearFields();
		receipts = new ArrayList<ITaxPayerReceipt>();
		ITaxPayerReceipt rec = createDefaultForm();
		rec.setCnt(1);
		receipts.add(rec);
		generatingNewSCNo();
		setSpecialCase(false);
		setDisAbleFullPenalty(true);
		setSelectedOwner(null);
	}
	
	private String cutName(String payorName){
		
		int cntName = payorName.length();
		
		if(cntName>=13){
			return  payorName.substring(0,13)+"...";
		}else{
			return  payorName;
		}
		
	}
	
	public void printReportIndividual(ITaxPayorTrans trans){
		receipts = new ArrayList<ITaxPayerReceipt>();
		String sql = " select * from taxpayortransreceipt p, taxpayortrans x, taxpayor t, landtype l "
				+ "WHERE p.recisactive=1 AND p.payortransid=x.payortransid AND p.payorid=t.payorid AND  p.landid=l.landid AND x.payortransid=?";
		String[] params = new String[1];
		params[0] = trans.getId()+"";
		List<ITaxPayerReceipt> pays = TaxPayerReceipt.retrieve(sql, params);
		
		if(pays.size()>0){
			int i=1;
			for(ITaxPayerReceipt pay : pays){
				pay.setCnt(i++);
				
				String fromYear = pay.getInstallmentRangeAndType().split(":")[0];
				String toYear = pay.getInstallmentRangeAndType().split(":")[1];
				String type = pay.getInstallmentRangeAndType().split(":")[2];
				pay.setFromYear(fromYear);
				pay.setToYear(toYear);
				pay.setInstallmentType(type);
				
				System.out.println("Click land " + pay.getLandType().getLandType());
				
				receipts.add(pay);
			}
		}
		
		int size = receipts.size();
		List<Reports> reports = new ArrayList<>();
		String payorName="";
		int cnt=0;
		int limit = 0;
		for(ITaxPayerReceipt rpt : receipts){
			Reports report = new Reports();
			if(cnt==0){
				payorName = rpt.getLandOwnerName();
				limit = payors(payorName);
			}
			if(limit >= 1){
				try{report.setF1(payorNames.get(cnt));}catch(Exception e){report.setF1(payorName);}
			}else if(limit == 0){
				try{report.setF1("");}catch(Exception e){}
			}
			limit -=1;
			report.setF2(rpt.getLocation());
			report.setF3(rpt.getLotBlockNo());
			report.setF4(rpt.getTaxDecNo());
			
			if(rpt.getAssValueImprv()==0) {
				report.setF5(Currency.formatAmount(rpt.getAssValueLand()+""));
				report.setF6("");
			}else {
				report.setF5("");
				report.setF6(Currency.formatAmount(rpt.getAssValueImprv()+""));
			}
			
			report.setF7(Currency.formatAmount(rpt.getAssValueTotal()+""));
			report.setF8(Currency.formatAmount(rpt.getTaxDue()+""));
			if(rpt.getFromYear().equalsIgnoreCase(rpt.getToYear())){
				report.setF9(rpt.getFromYear() + " " + (rpt.getInstallmentType().equalsIgnoreCase("BASIC")? "- B" : "- S") + " " + (rpt.getRemarks()==null? "" : rpt.getRemarks()));
			}else{
				report.setF9(rpt.getFromYear() + "-" + rpt.getToYear().substring(2, 4) + " " + (rpt.getInstallmentType().equalsIgnoreCase("BASIC")? "- B" : "- S") + " " + (rpt.getRemarks()==null? "" : rpt.getRemarks()));
			}
			report.setF10(Currency.formatAmount(rpt.getFullPayment()+""));
			report.setF11(Currency.formatAmount(rpt.getPenaltyPercent()+""));
			report.setF12(Currency.formatAmount(rpt.getOverallTotal()+""));
			reports.add(report);
			cnt++;
		}
		
		//add blank rows
		if(size<6){
			size+=1;
			for(int i=size; i<=6;i++){
				Reports report = new Reports();
				if(cnt<payorNames.size()){
					report.setF1(payorNames.get(cnt));
				}else{
					report.setF1("");
				}
				report.setF2("");
				report.setF3("");
				report.setF4("");
				report.setF5("");
				report.setF6("");
				report.setF7("");
				report.setF8("");
				report.setF9("");
				report.setF10("");
				report.setF11("");
				report.setF12("");
				reports.add(report);
				cnt++;
			}
		}
		
		//compiling report
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME =ReadConfig.value(AppConf.FORM_56);
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		param.put("PARAM_RECEIVE_FROM", trans.getTaxPayor().getFullName());
  		param.put("PARAM_AMOUNT_WORDS", trans.getAmountInWords());
  		param.put("PARAM_MONTH", DateUtils.convertDateToMonthDayYear(trans.getTransDate()));
  		param.put("PARAM_CHECK_NO", trans.getCheckNo());
  		if(trans.getCheckDate()!=null){
	  		param.put("PARAM_CHECK_MONTH", trans.getCheckDate().split("-")[1] + "/" + trans.getCheckDate().split("-")[2]);
	  		param.put("PARAM_CHECK_YEAR", trans.getCheckDate().split("-")[0].substring(2, 4));
  		}else{
  			param.put("PARAM_CHECK_MONTH", "");
  	  		param.put("PARAM_CHECK_YEAR", "");
  		}
  		param.put("PARAM_GRAND_TOTAL",Currency.formatAmount(trans.getAmount()+""));
  		
  		param.put("PARAM_SIGNED1",trans.getSigned1());
  		param.put("PARAM_SIGNED2",trans.getSigned2());
  		
  		
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
	public void addCell(){
		System.out.println("Add cell");
		int size = receipts.size();
		int year = Integer.valueOf(receipts.get(size-1).getToYear());
		//identify installment type
		int i=0;
		for(ITaxPayerReceipt chk : receipts){
			i = chk.getInstallmentType().equalsIgnoreCase("BASIC")? 2 : 1;
		}
		
		if(size<=5){
			ITaxPayerReceipt rec = createDefaultForm();
			if(i==1){
				rec.setInstallmentType("BASIC");
			}else{
				rec.setInstallmentType("SEF");
			}
			year +=1;
			rec.setFromYear(year+"");
			rec.setToYear(year+"");
			
			setYearSelectedFromId(year);
			setYearSelectedToId(year);
			
			rec.setCnt(size+1);
			receipts.add(rec);
			updateTotal();
			
		}
	}
	
	public void closePopup(){
		setFindFromDate(DateUtils.convertDateString(getTransDate(),"yyyy-MM-dd"));
		setFindToDate(DateUtils.convertDateString(getTransDate(),"yyyy-MM-dd"));
		init();
	}
	
	private String retrieveLotNumber(String name){
		Connection conn = null;
		ResultSet rs = null;
		String result = "";
		PreparedStatement ps = null;
		String sql = "SELECT lotno FROM taxpayor t, payorland p WHERE t.payorid=p.payorid AND t.payorname=?";
		String[] params = new String[1];
		params[0] = name;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		int i=1;
		while(rs.next()){
			if(i==1){
				result = rs.getString("lotno");
			}else{
				result += "," + rs.getString("lotno");
			}
			i++;
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(Exception e){}
		
		return result;
	}
	
	public void updateCopyPaste() {
		ITaxPayerReceipt pay =  getReceiptSelected();
		int yearFrom = Integer.valueOf(pay.getFromYear());
		int yearTo = Integer.valueOf(pay.getToYear());
		
		
		
		int index = getReceipts().size();
		
		switch(index) {
		case 1: //no movement on the year
			break;
		case 2: //adjust date by 1
			if(yearFrom!=yearTo) {//if not match year get the year to
				yearFrom = yearTo;
			}
			yearFrom +=1;
			yearTo +=1;
			break;
		case 3: //no movement on the year
			break;	
		case 4: //adjust date by 1
			if(yearFrom!=yearTo) {//if not match year get the year to
				yearFrom = yearTo;
			}
			yearFrom +=1;
			yearTo +=1;
			break;
		case 5: //no movement on the year
			break;
		case 6: //adjust date by 1
			if(yearFrom!=yearTo) {//if not match year get the year to
				yearFrom = yearTo;
			}
			yearFrom +=1;
			yearTo +=1;
			break;	
		}
		
		
		setIdFromYear(yearFrom);
		setIdToYear(yearTo);
		setIdPaymentType(pay.getInstallmentType().equalsIgnoreCase("BASIC")? "SEF" : "BASIC");
	}
	
	public void copyPasteNewCell(){
		System.out.println("copyPastecell");
		
		//identify installment type
		
		
		int size = receipts.size();
		if(size<=5){
			ITaxPayerReceipt copyrec = getReceiptSelected();
			ITaxPayerReceipt rec = new TaxPayerReceipt();
			rec.setCnt(size+1);
			try{rec.setLocation(copyrec.getLocation());}catch(NullPointerException e){}
			try{rec.setLotBlockNo(copyrec.getLotBlockNo());}catch(NullPointerException e){}
			try{rec.setTaxDecNo(copyrec.getTaxDecNo());}catch(NullPointerException e){}
			try{rec.setAssValueLand(copyrec.getAssValueLand());}catch(NullPointerException e){}
			try{rec.setAssValueImprv(copyrec.getAssValueImprv());}catch(NullPointerException e){}
			try{rec.setAssValueTotal(copyrec.getAssValueTotal());}catch(NullPointerException e){}
			try{rec.setTaxDue(copyrec.getTaxDue());}catch(NullPointerException e){}
			try{rec.setInstallmentRangeAndType(copyrec.getInstallmentRangeAndType());}catch(NullPointerException e){}
			try{rec.setFullPayment(copyrec.getFullPayment());}catch(NullPointerException e){}
			try{rec.setPenaltyPercent(copyrec.getPenaltyPercent());}catch(NullPointerException e){}
			try{rec.setOverallTotal(copyrec.getOverallTotal());}catch(NullPointerException e){}
			try{rec.setIsActive(1);}catch(NullPointerException e){}
			try{rec.setLandOwnerName(copyrec.getLandOwnerName());}catch(NullPointerException e){}
			try{rec.setLandType(copyrec.getLandType());}catch(NullPointerException e){}
			try{rec.setPayor(copyrec.getPayor());}catch(NullPointerException e){}
			try{rec.setRemarks(copyrec.getRemarks());}catch(NullPointerException e){}
			try{rec.setIsAdjust(copyrec.getIsAdjust());}catch(NullPointerException e){}
			try{rec.setIsCase(copyrec.getIsCase());}catch(NullPointerException e){}
			
			try{rec.setFromYear(copyrec.getFromYear());}catch(NullPointerException e){}
			try{rec.setToYear(copyrec.getToYear());}catch(NullPointerException e){}
			
				rec.setInstallmentType(getIdPaymentType());
				rec.setFromYear(getIdFromYear()+"");
				rec.setToYear(getIdToYear()+"");
				rec.setRemarks(copyrec.getRemarks());
				
			
			
			receipts.add(rec);
			updateTotal();
		}else {
			Application.addMessage(1, "Info", "More six of row is not allowed anymore");
		}
	}
	
	
	public void copyPasteCell(){
		System.out.println("copyPastecell");
		
		//identify installment type
		int i=0;
		for(ITaxPayerReceipt chk : receipts){
			i = chk.getInstallmentType().equalsIgnoreCase("BASIC")? 2 : 1;
		}
		
		int size = receipts.size();
		if(size<=5){
			ITaxPayerReceipt copyrec = getReceiptSelected();
			ITaxPayerReceipt rec = new TaxPayerReceipt();
			rec.setCnt(size+1);
			try{rec.setLocation(copyrec.getLocation());}catch(NullPointerException e){}
			try{rec.setLotBlockNo(copyrec.getLotBlockNo());}catch(NullPointerException e){}
			try{rec.setTaxDecNo(copyrec.getTaxDecNo());}catch(NullPointerException e){}
			try{rec.setAssValueLand(copyrec.getAssValueLand());}catch(NullPointerException e){}
			try{rec.setAssValueImprv(copyrec.getAssValueImprv());}catch(NullPointerException e){}
			try{rec.setAssValueTotal(copyrec.getAssValueTotal());}catch(NullPointerException e){}
			try{rec.setTaxDue(copyrec.getTaxDue());}catch(NullPointerException e){}
			try{rec.setInstallmentRangeAndType(copyrec.getInstallmentRangeAndType());}catch(NullPointerException e){}
			try{rec.setFullPayment(copyrec.getFullPayment());}catch(NullPointerException e){}
			try{rec.setPenaltyPercent(copyrec.getPenaltyPercent());}catch(NullPointerException e){}
			try{rec.setOverallTotal(copyrec.getOverallTotal());}catch(NullPointerException e){}
			try{rec.setIsActive(1);}catch(NullPointerException e){}
			try{rec.setLandOwnerName(copyrec.getLandOwnerName());}catch(NullPointerException e){}
			try{rec.setLandType(copyrec.getLandType());}catch(NullPointerException e){}
			try{rec.setPayor(copyrec.getPayor());}catch(NullPointerException e){}
			try{rec.setRemarks(copyrec.getRemarks());}catch(NullPointerException e){}
			try{rec.setIsAdjust(copyrec.getIsAdjust());}catch(NullPointerException e){}
			try{rec.setIsCase(copyrec.getIsCase());}catch(NullPointerException e){}
			
			try{rec.setFromYear(copyrec.getFromYear());}catch(NullPointerException e){}
			try{rec.setToYear(copyrec.getToYear());}catch(NullPointerException e){}
			if(i==1){
				rec.setInstallmentType("BASIC");
				
				//added to update year
				int toY = Integer.valueOf(copyrec.getToYear()) + 1;
				rec.setFromYear(toY+"");
				rec.setToYear(toY+"");
				rec.setRemarks("");
				
				setIdFromYear(toY);
				setIdToYear(toY);
				
			}else{
				rec.setInstallmentType("SEF");
			}
			
			
			receipts.add(rec);
			updateTotal();
		}
	}
	
	public void clickItem(ITaxPayorTrans trans){
		
		listColumns = Arrays.asList(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,true);
		
		//listColumns.set(1, true);
		listColumns.set(2, false);
		listColumns.set(3, false);
		listColumns.set(4, false);
		//listColumns.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
		
		
		setSearchParam(null);
		setPayorSelected(trans.getTaxPayor());
		setStatusId(trans.getStatus());
		setPaymentId(trans.getPaymentType());
		setPayorTransData(trans);
		setTmpTransDate(DateUtils.convertDateString(trans.getTransDate(),"yyyy-MM-dd"));
		setTransDate(trans.getTransDate());
		setScNo(trans.getScNo());
		setReceiveFrom(trans.getTaxPayor().getFullName());
		setSpecialCase(trans.getIsSpecialCase()==0? false : true);
		setDisAbleFullPenalty(trans.getIsSpecialCase()==0? true : false); //lock the field if 0 else unlock
		setSignatory1(trans.getSigned1());
		setSignatory2(trans.getSigned2());
		if(trans.getStatus()==2){
			setAmountInWords("ZERO PESOS ONLY.");
		}else{
			setAmountInWords(trans.getAmountInWords());
		}
		setGrandTotal(Double.valueOf(trans.getAmount()+""));
		receipts = new ArrayList<ITaxPayerReceipt>();// Collections.synchronizedList(new ArrayList<ITaxPayerReceipt>());
		String sql = " select * from taxpayortransreceipt p, taxpayortrans x, taxpayor t, landtype l "
				+ "WHERE p.payortransid=x.payortransid AND p.payorid=t.payorid AND  p.landid=l.landid AND x.payortransid=? AND p.recisactive=1";
		String[] params = new String[1];
		params[0] = trans.getId()+"";
		List<ITaxPayerReceipt> pays = TaxPayerReceipt.retrieve(sql, params);
		String typeIns="";
		if(pays.size()>0){
			int i=1;
			String owner = "";
			for(ITaxPayerReceipt pay : pays){
				pay.setCnt(i++);
				
				String fromYear = pay.getInstallmentRangeAndType().split(":")[0];
				String toYear = pay.getInstallmentRangeAndType().split(":")[1];
				String type = pay.getInstallmentRangeAndType().split(":")[2];
				pay.setFromYear(fromYear);
				pay.setToYear(toYear);
				owner = pay.getLandOwnerName();
				pay.setInstallmentType(type);//this the exchange of below commented code
				/**
				 * Temporary comment 04-25-2017
				 * reason there is instances only basic type can only settle for payment for the year
				 *
				if("".equalsIgnoreCase(typeIns)){
					pay.setInstallmentType(type);
					typeIns = type;
				}else if("BASIC".equalsIgnoreCase(typeIns)){
					pay.setInstallmentType("SEF");
					typeIns = "SEF";
				}else if("SEF".equalsIgnoreCase(typeIns)){
					pay.setInstallmentType("BASIC");
					typeIns = "BASIC";
				}*/
				
				
				System.out.println("Click land " + pay.getLandType().getLandType());
				
				receipts.add(pay);
			}
			setSelectedOwner(owner);
		}
	}
	
	private ITaxPayerReceipt createDefaultForm(){
		
		ITaxPayerReceipt rec = new TaxPayerReceipt();
		
		rec.setLocation("Location");
		rec.setLotBlockNo("Lot & Block No");
		rec.setTaxDecNo("Tax Dec");
		
		rec.setPayorTrans(getPayorTransData());
		ILandType land = new LandType();
		land.setId(1);
		land.setLandType("Agricultural");
		rec.setLandType(land);
		
		rec.setFromYear(DateUtils.getCurrentYear()+"");
		rec.setToYear(DateUtils.getCurrentYear()+"");
		
		rec.setInstallmentType("BASIC");
		rec.setIsAdjust(false);
		
		rec.setLandOwnerName("Last Name, First Name");

		
		return rec;
	}
	
	public void adjustmentCheck(ITaxPayerReceipt rec){
		System.out.println("check : " + rec.getIsAdjust());
		
		if(getSpecialCase()){
			receipts.get(rec.getCnt()-1).setIsAdjust(false);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "General Special case is activated. You can't use adjustment. Please untick special case to use adjustment.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}else{
			receipts.get(rec.getCnt()-1).setIsAdjust(rec.getIsAdjust());
		}
	}
	
	public void specialCheck(ITaxPayerReceipt rec){
		System.out.println("check : " + rec.getIsAdjust());
		
		if(getSpecialCase()){
			receipts.get(rec.getCnt()-1).setIsCase(false);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "General Special case is activated. You can't use specific special case. Please untick special case to use specific case.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}else{
			setDisAbleFullPenalty(false);
			receipts.get(rec.getCnt()-1).setIsCase(rec.getIsCase());
		}
	}
	
	public void printReportAll(){
		List<Reports> reports = new ArrayList<>();
		Collections.reverse(trans);
		for(ITaxPayorTrans rpt : trans){
			Reports report = new Reports();
			report.setF1(rpt.getScNo());
			report.setF2(rpt.getTransDate());
			report.setF3(rpt.getTaxPayor().getFullName());
			report.setF4(rpt.getLandPayor().getPayor().getFullName());
			report.setF5(rpt.getLandPayor().getTaxDeclarionNo() + "  /  " + rpt.getLandPayor().getLotNo());
			report.setF6("Php"+Currency.formatAmount(rpt.getAmount()+""));
			reports.add(report);
		}
		
		//compiling report
				String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
						AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
				String REPORT_NAME =ReadConfig.value(AppConf.FORM_56_ALL);
				System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
				ReportCompiler compiler = new ReportCompiler();
				String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
				
				JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
		  		HashMap param = new HashMap();
		  		
		  		param.put("PARAM_DATE","Printed Date: "+DateUtils.getCurrentDateMMMMDDYYYY());
		  		param.put("PARAM_DATE_RANGE", "From: " + DateUtils.convertDate(getFindFromDate(),"yyyy-MM-dd") + " To: " + DateUtils.convertDate(getFindToDate(),"yyyy-MM-dd"));
		  		param.put("PARAM_GRAND_TOTAL","Grand Total Php: "+Currency.formatAmount(getTransAmountGrandTotal()+""));
		  		param.put("PARAM_PREPAREDBY", "Prepared By: "+ Login.getUserLogin().getUserDtls().getFirstname() + " " + Login.getUserLogin().getUserDtls().getLastname());
		  		
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
	
	public List<ITaxPayorTrans> getTrans() {
		return trans;
	}

	public void setTrans(List<ITaxPayorTrans> trans) {
		this.trans = trans;
	}

	public String getReceiveFrom() {
		return receiveFrom;
	}

	public void setReceiveFrom(String receiveFrom) {
		this.receiveFrom = receiveFrom;
	}

	public String getTransDate() {
		if(transDate==null){
			transDate = DateUtils.getCurrentDateYYYYMMDD();
		}
		return transDate;
	}

	public void setTransDate(String transDate) {
		this.transDate = transDate;
	}

	public String getAmountInWords() {
		return amountInWords;
	}

	public void setAmountInWords(String amountInWords) {
		this.amountInWords = amountInWords;
	}

	public String getBasicTax() {
		return basicTax;
	}

	public void setBasicTax(String basicTax) {
		this.basicTax = basicTax;
	}

	public String getSpecialEducationFund() {
		return specialEducationFund;
	}

	public void setSpecialEducationFund(String specialEducationFund) {
		this.specialEducationFund = specialEducationFund;
	}

	public String getSigned1() {
		return signed1;
	}

	public void setSigned1(String signed1) {
		this.signed1 = signed1;
	}

	public String getSigned2() {
		return signed2;
	}

	public void setSigned2(String signed2) {
		this.signed2 = signed2;
	}

	public String getCheckNo() {
		return checkNo;
	}

	public void setCheckNo(String checkNo) {
		this.checkNo = checkNo;
	}

	public String getCheckDate() {
		return checkDate;
	}

	public void setCheckDate(String checkDate) {
		this.checkDate = checkDate;
	}

	public Double getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(Double grandTotal) {
		this.grandTotal = grandTotal;
	}

	public List<ITaxPayerReceipt> getReceipts() {
		return receipts;
	}

	public void setReceipts(List<ITaxPayerReceipt> receipts) {
		this.receipts = receipts;
	}

	public List getLandTypes() {
		landTypes = new ArrayList<>();
		String sql = "SELECT * FROM landtype";
		for(ILandType type : LandType.retrieve(sql, new String[0])){
			landTypes.add(new SelectItem(type.getId(),type.getLandType()));
		}
		return landTypes;
	}

	public void setLandTypes(List landTypes) {
		this.landTypes = landTypes;
	}

	public int getLandId() {
		return landId;
	}

	public void setLandId(int landId) {
		this.landId = landId;
	}

	public long getPayorId() {
		return payorId;
	}

	public void setPayorId(long payorId) {
		this.payorId = payorId;
	}

	public ITaxPayerReceipt getReceiptSelected() {
		return receiptSelected;
	}

	public void setReceiptSelected(ITaxPayerReceipt receiptSelected) {
		this.receiptSelected = receiptSelected;
	}

	public ITaxPayorTrans getPayorTransData() {
		return payorTransData;
	}

	public void setPayorTransData(ITaxPayorTrans payorTransData) {
		this.payorTransData = payorTransData;
	}

	public String getScNo() {
		return scNo;
	}

	public void setScNo(String scNo) {
		this.scNo = scNo;
	}

	public int getIdFromYear() {
		if(idFromYear==0){
			idFromYear = DateUtils.getCurrentYear();
		}
		return idFromYear;
	}

	public void setIdFromYear(int idFromYear) {
		this.idFromYear = idFromYear;
	}

	public List getFromYear() {
		fromYear = new ArrayList<>();
		for(int i=1985; i<=2030; i++){
			fromYear.add(new SelectItem(i,i+""));
		}
		
		return fromYear;
	}

	public void setFromYear(List fromYear) {
		this.fromYear = fromYear;
	}

	public int getIdToYear() {
		if(idToYear==0){
			idToYear = DateUtils.getCurrentYear();
		}
		return idToYear;
	}

	public void setIdToYear(int idToYear) {
		this.idToYear = idToYear;
	}

	public List getToYear() {
		toYear = new ArrayList<>();
		for(int i=1985; i<=2030; i++){
			toYear.add(new SelectItem(i,i+""));
		}
		return toYear;
	}

	public void setToYear(List toYear) {
		this.toYear = toYear;
	}

	public String getIdPaymentType() {
		return idPaymentType;
	}

	public void setIdPaymentType(String idPaymentType) {
		this.idPaymentType = idPaymentType;
	}

	public List getPaymentTypes() {
		paymentTypes = new ArrayList<>();
		paymentTypes.add(new SelectItem("BASIC","BASIC"));
		paymentTypes.add(new SelectItem("SEF","SEF"));
		return paymentTypes;
	}

	public void setPaymentTypes(List paymentTypes) {
		this.paymentTypes = paymentTypes;
	}

	public String getFindName() {
		return findName;
	}

	public void setFindName(String findName) {
		this.findName = findName;
	}

	public Date getFindFromDate() {
		if(findFromDate==null){
			findFromDate = DateUtils.getDateToday();
		}
		return findFromDate;
	}

	public void setFindFromDate(Date findFromDate) {
		this.findFromDate = findFromDate;
	}

	public Date getFindToDate() {
		if(findToDate==null){
			findToDate = DateUtils.getDateToday();
		}
		return findToDate;
	}

	public void setFindToDate(Date findToDate) {
		this.findToDate = findToDate;
	}
	public double getTransAmountGrandTotal() {
		return transAmountGrandTotal;
	}
	public void setTransAmountGrandTotal(double transAmountGrandTotal) {
		this.transAmountGrandTotal = transAmountGrandTotal;
	}

	public Date getTmpTransDate() {
		if(tmpTransDate==null){
			tmpTransDate = DateUtils.getDateToday();
		}
		return tmpTransDate;
	}

	public void setTmpTransDate(Date tmpTransDate) {
		this.tmpTransDate = tmpTransDate;
	}
	
	public static void main(String[] args) {
		
		double partialAmnt = 2690.69;
		double actualAmnt = 3005.18;
		double amntResult = 0.0d;
		amntResult = partialAmnt / actualAmnt;
		System.out.println("Amount percentage: " + Numbers.roundOf(amntResult, 2));
		double fullPayment = 1747.20;
		double totalPenaltyAmnt = 1257.98;
		
		double amntToSubtract = actualAmnt - partialAmnt;
		System.out.println("Less " + Numbers.roundOf(amntToSubtract,2));
		double amntpercentage = amntToSubtract / 2;
		double fullPaymentAmnt = fullPayment - amntpercentage;
		System.out.println("Full Payment: "+Numbers.roundOf(fullPaymentAmnt,2));
		double penaltyPayment = totalPenaltyAmnt - amntpercentage;
		System.out.println("Penalty Amount: "+Numbers.roundOf(penaltyPayment,2));
		double total = fullPaymentAmnt + penaltyPayment;
		System.out.println("Total: "+Numbers.roundOf(total,2));
	}
	
	private void partialCalculation(){
		double partAmnt = 123.00;
		
		
		
    }
	
	private List<String> payorNames = new ArrayList<>();
	public List<String> getPayorNames() {
		return payorNames;
	}

	public void setPayorNames(List<String> payorNames) {
		this.payorNames = payorNames;
	}

	public  int payors(String payorName){
		payorNames = new ArrayList<>();
		int size = payorName.length();
		int cnt = 0;
		if(size>15){
			String f1 = payorName.substring(0, 15);
			payorNames.add(f1);
			int new_s = size - 15;
			cnt = 1;
			if(new_s>15){
				String f3 = payorName.substring(15, 15*2);
				payorNames.add(f3);
				cnt = 2;
				new_s = new_s - 15;
				if(new_s>15){
					String f5 = payorName.substring(30, 30+13);
					payorNames.add(f5);
					cnt = 3;
				}else{
					String f4 = payorName.substring(30, 30+new_s);
					payorNames.add(f4);
					cnt = 3;
				}
				
			}else{
				String f2 = payorName.substring(15, 15+new_s);
				payorNames.add(f2);
				cnt = 2;
			}
			
			
		}else{
			payorNames.add(payorName);
			cnt = 1;
		}
		return cnt;
	}
	
	public void findLandOwner(){
		viewLandOwner(getPayerReceipt());
	}
	
	public void viewLandOwner(ITaxPayerReceipt payerReceipt){
		setPayerReceipt(payerReceipt);
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		String sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid order by p.payorid desc limit 10";
		String[] params = new String[0];
		
		if(getSearchParam()==null || getSearchParam().isEmpty()){
			if(payerReceipt.getPayor()!=null && !"Last Name, First Name".equalsIgnoreCase(payerReceipt.getPayor().getFullName())){
				setSearchParam(payerReceipt.getPayor().getFullName());
			}else if(getReceiveFrom()!=null){
				setSearchParam(getReceiveFrom());
			}
		}
		
		System.out.println("getSearchParam()>> " + getSearchParam());
		if(getSearchParam().contains("\'")) {
			setSearchParam(getSearchParam().replace("\'", "\\'"));
			System.out.println("contain ' >> " + getSearchParam());
		}
		
		/**
		 * both has a value
		 */
		if((getSearchParam()!=null && !getSearchParam().isEmpty()) && getBarangayIdSearch()!=0){
			params = new String[1];
			
			sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
					+ "WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid AND p.payorname like '%" + getSearchParam().replaceAll("--", "") + "%' AND p.bgid=? "
					+ "order by p.payorid desc limit 100";
			params[0] = getBarangayIdSearch()+"";
			
		/**
		 * only input text has a value	
		 */
		}else if((getSearchParam()!=null && !getSearchParam().isEmpty()) && getBarangayIdSearch()==0){
		
			sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
					+ "WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid AND p.payorname like '%" + getSearchParam().replaceAll("--", "") + "%'  "
					+ "order by p.payorid desc limit 100";
			
		/**
		 * Only barangay has a value	
		 */
		}else if((getSearchParam()==null || getSearchParam().isEmpty()) && getBarangayIdSearch()!=0){
			params = new String[1];
			sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
					+ "WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid AND  p.bgid=? "
					+ "order by p.payorid desc limit 100";
			params[0] = getBarangayIdSearch()+"";
		}
		
		payorLand = new ArrayList<ITaxPayor>();//Collections.synchronizedList(new ArrayList<ITaxPayor>());
		//payorLand = TaxPayor.retrieve(sql, params);
		
		for(ITaxPayor pay : TaxPayor.retrieve(sql, params)){
			sql = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid AND l.landstatus=1 AND l.payorid=? ORDER BY l.payorlandid DESC";
			params = new String[1];
			params[0] = pay.getId()+"";
			pay.setLandPayor(LandPayor.retrieve(sql, params));
			payorLand.add(pay);
		}
		
	}
	
	public void payorLookup(){
		
		try {
		String sql = "SELECT * FROM taxpayor WHERE payisactive=1 AND payorname like '%" + getPayorSearchParam().replaceAll("--", "") + "%' ORDER BY payorid DESC limit 100";
		String[] params = new String[0];
		payorSearchData = new ArrayList<ITaxPayor>();
		payorSearchData = TaxPayor.retrievePayor(sql, params);
		
		}catch(NullPointerException e) {e.printStackTrace();}
	}
	
	public void payorLookupSelected(ITaxPayor payor){
		setPayorSelected(payor);
		setReceiveFrom(payor.getFullName());
	}
	
	@Deprecated
	public void selectedPayor(ITaxPayor payor){
		System.out.println("selected payor");
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		String sql = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid AND p.payorname=? ORDER BY l.payorlandid DESC";
		String[] params = new String[1];
		params[0] = payor.getId()+"";
		lands = new ArrayList<LandPayor>();
		lands = LandPayor.retrieve(sql, params);
	}
	
	/**
	 * 
	 * @param change by selectedLand()
	 */
	@Deprecated
	public void landSelected(LandPayor pay){
		System.out.println("selected land");
		int index = receipts.indexOf(getPayerReceipt());
		receipts.get(index).setLandOwnerName(pay.getPayor().getFullName());
		receipts.get(index).setLocation(pay.getBarangay().getName());
		
		String tdno = pay.getTaxDeclarionNo();
		int size = tdno.length();
		receipts.get(index).setLotBlockNo(pay.getLotNo());
		try{receipts.get(index).setTaxDecNo(tdno.substring(0, 1) + "-" + tdno.substring(5,size));}catch(Exception e){receipts.get(index).setTaxDecNo(tdno);}
		receipts.get(index).setLandType(pay.getLandType());
		receipts.get(index).setAssValueLand(pay.getLandValue());
		receipts.get(index).setPayor(pay.getPayor());
		
		if(pay.getIsImprovment()==1) {
			receipts.get(index).setAssValueImprv(pay.getLandValue());
		}
		
		//new two lines added
		setReceiptSelected(receipts.get(index));
		copyPasteCell();
		
		updateTotal();
		setSearchParam(null);
		setSelectedOwner(pay.getPayor().getFullName());
		
		
	}
	
	
	
	public void passLandData(LandPayor pay) {
		setLandForPaymentData(pay);
	}
	
	public void selectedLand() {
		System.out.println("check selected Land method : " + getLandForPaymentData()==null? " is null" : " not null");
		if(getLandForPaymentData()!=null) {
			LandPayor pay = getLandForPaymentData();
			System.out.println("selected land");
			int index = receipts.indexOf(getPayerReceipt());
			
			receipts.get(index).setFromYear(getYearSelectedFromId()+"");
			receipts.get(index).setToYear(getYearSelectedToId()+"");
			
			receipts.get(index).setLandOwnerName(pay.getPayor().getFullName());
			receipts.get(index).setLocation(pay.getBarangay().getName());
			
			String tdno = pay.getTaxDeclarionNo();
			int size = tdno.length();
			receipts.get(index).setLotBlockNo(pay.getLotNo());
			try{receipts.get(index).setTaxDecNo(tdno.substring(0, 1) + "-" + tdno.substring(5,size));}catch(Exception e){receipts.get(index).setTaxDecNo(tdno);}
			receipts.get(index).setLandType(pay.getLandType());
			receipts.get(index).setAssValueLand(pay.getLandValue());
			receipts.get(index).setPayor(pay.getPayor());
			
			if(getSpecialCase()) {//partial amount
				double fullpayment = pay.getLandValue() * 0.01;
				//receipts.get(index).setFullPayment(fullpayment);
				
				int years = getYearSelectedToId() - getYearSelectedFromId();
				years += 1;
				double amount = years*fullpayment;
				System.out.println("caculated: " + amount + " for total number of year is " + years);
				receipts.get(index).setFullPayment(Numbers.formatDouble(amount));
				
			}
			
			if(pay.getIsImprovment()==1) {
				receipts.get(index).setAssValueImprv(pay.getLandValue());
			}
			
			//new two lines added
			setReceiptSelected(receipts.get(index));
			copyPasteCell();
			
			updateTotal();
			setSearchParam(null);
			setSelectedOwner(pay.getPayor().getFullName());
		}
	}
	
	public List getStatusList() {
		
		statusList = new ArrayList<>();
		statusList.add(new SelectItem(1,"POSTED"));
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

	public List getPaymentList() {
		
		paymentList = new ArrayList<>();
		paymentList.add(new SelectItem(1,"FULL"));
		paymentList.add(new SelectItem(2,"PARTIAL"));
		
		return paymentList;
	}

	public void setPaymentList(List paymentList) {
		this.paymentList = paymentList;
	}

	public int getPaymentId() {
		if(paymentId==0){
			paymentId = 1;
		}
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public ITaxPayor getPayorSelected() {
		return payorSelected;
	}

	public void setPayorSelected(ITaxPayor payorSelected) {
		this.payorSelected = payorSelected;
	}

	public LandPayor getLandSelected() {
		return landSelected;
	}

	public void setLandSelected(LandPayor landSelected) {
		this.landSelected = landSelected;
	}

	public List<ITaxPayor> getPayorLand() {
		return payorLand;
	}

	public void setPayorLand(List<ITaxPayor> payorLand) {
		this.payorLand = payorLand;
	}

	public List<LandPayor> getLands() {
		return lands;
	}

	public void setLands(List<LandPayor> lands) {
		this.lands = lands;
	}
	
	public List getBarangayListSearch() {
		barangayListSearch = new ArrayList<>();
		for(Barangay bar : Barangay.retrieve("SELECT * FROM barangay", new String[0])){
			barangayListSearch.add(new SelectItem(bar.getId(), bar.getName()));
		}
		
		return barangayListSearch;
	}
	public void setBarangayListSearch(List barangayListSearch) {
		this.barangayListSearch = barangayListSearch;
	}
	public int getBarangayIdSearch() {
		return barangayIdSearch;
	}
	public void setBarangayIdSearch(int barangayIdSearch) {
		this.barangayIdSearch = barangayIdSearch;
	}
	public String getSearchParam() {
		return searchParam;
	}
	public void setSearchParam(String searchParam) {
		this.searchParam = searchParam;
	}

	public ITaxPayerReceipt getPayerReceipt() {
		return payerReceipt;
	}

	public void setPayerReceipt(ITaxPayerReceipt payerReceipt) {
		this.payerReceipt = payerReceipt;
	}

	public List<ITaxPayor> getPayorSearchData() {
		return payorSearchData;
	}

	public void setPayorSearchData(List<ITaxPayor> payorSearchData) {
		this.payorSearchData = payorSearchData;
	}

	public String getPayorSearchParam() {
		return payorSearchParam;
	}

	public void setPayorSearchParam(String payorSearchParam) {
		this.payorSearchParam = payorSearchParam;
	}

	public boolean getSpecialCase() {
		return specialCase;
	}

	public void setSpecialCase(boolean specialCase) {
		this.specialCase = specialCase;
	}

	public boolean getDisAbleFullPenalty() {
		return disAbleFullPenalty;
	}

	public void setDisAbleFullPenalty(boolean disAbleFullPenalty) {
		this.disAbleFullPenalty = disAbleFullPenalty;
	}

	public String getSignatory1() {
		
		if(signatory1==null){
			signatory1 = "ALVIN M. BATOL, CPA";
		}
		
		return signatory1;
	}

	public void setSignatory1(String signatory1) {
		this.signatory1 = signatory1;
	}

	public String getSignatory2() {
		
		if(signatory2==null){
			signatory2 = "FERDINAND L. LOPEZ";
		}
		
		return signatory2;
	}

	public void setSignatory2(String signatory2) {
		this.signatory2 = signatory2;
	}

	public List<Boolean> getListColumns() {
		return listColumns;
	}

	public void setListColumns(List<Boolean> listColumns) {
		this.listColumns = listColumns;
	}

	public String getSelectedOwner() {
		return selectedOwner;
	}

	public void setSelectedOwner(String selectedOwner) {
		this.selectedOwner = selectedOwner;
	}

	public LandPayor getLandForPaymentData() {
		return landForPaymentData;
	}

	public void setLandForPaymentData(LandPayor landForPaymentData) {
		this.landForPaymentData = landForPaymentData;
	}

	public int getYearSelectedFromId() {
		if(yearSelectedFromId==0) {
			yearSelectedFromId = DateUtils.getCurrentYear();
		}
		return yearSelectedFromId;
	}

	public void setYearSelectedFromId(int yearSelectedFromId) {
		this.yearSelectedFromId = yearSelectedFromId;
	}

	public List getYearSelectedFroms() {
		int current = DateUtils.getCurrentYear() + 1;
		yearSelectedFroms = new ArrayList<>();
		for(int year=1985; year<=current; year++) {
			yearSelectedFroms.add(new SelectItem(year, year+""));
		}
		return yearSelectedFroms;
	}

	public void setYearSelectedFroms(List yearSelectedFroms) {
		this.yearSelectedFroms = yearSelectedFroms;
	}

	public int getYearSelectedToId() {
		if(yearSelectedToId==0) {
			yearSelectedToId = DateUtils.getCurrentYear();
		}
		return yearSelectedToId;
	}

	public void setYearSelectedToId(int yearSelectedToId) {
		this.yearSelectedToId = yearSelectedToId;
	}

	public List getYearSelectedTos() {
		int current = DateUtils.getCurrentYear() + 1;
		yearSelectedTos = new ArrayList<>();
		for(int year=1985; year<=current; year++) {
			yearSelectedTos.add(new SelectItem(year, year+""));
		}
		return yearSelectedTos;
	}

	public void setYearSelectedTos(List yearSelectedTos) {
		this.yearSelectedTos = yearSelectedTos;
	}
		
}
