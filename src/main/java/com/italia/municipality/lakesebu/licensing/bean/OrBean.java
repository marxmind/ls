package com.italia.municipality.lakesebu.licensing.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.controller.Email;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.DateFormat;
import com.italia.municipality.lakesebu.enm.EmailType;
import com.italia.municipality.lakesebu.enm.Job;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.BusinessORTransaction;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class OrBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6878976901L;
	
	private String searchName;
	private Date calendarFrom;
	private Date calendarTo;
	private List<BusinessORTransaction> orNumbers = new ArrayList<BusinessORTransaction>();
	private double grandTotal;
	
	private Date issuedDate;
	private String orNumber;
	private double amount;
	private String issuedAddress;
	private String purpose;
	private double grossAmount;
	private BusinessCustomer customerSelected;
	private String citizenName;
	
	private String BARANGAY = "Poblacion";
	private String MUNICIPALITY = "Lake Sebu";
	private String PROVINCE = "South Cotabato";
	
	private List<BusinessCustomer> taxpayers = new ArrayList<BusinessCustomer>();
	private String searchTaxpayerName;
	
	private BusinessORTransaction orData;
	
	private int statId;
	private List stats;
	
	private List<String> checks;
	
	private boolean capital;
	
	@PostConstruct
	public void init(){
		loadORs();
	}
	
	public void loadORs(){
		orNumbers = new ArrayList<BusinessORTransaction>();
		
		String sql = " AND (orl.ordate>=? AND orl.ordate<=?) AND orl.oractive=1 AND cuz.cusisactive=1 ";
		String[] params = new String[2];
		grandTotal = 0.0d;
		params[0] = DateUtils.convertDate(getCalendarFrom(), DateFormat.YYYY_MM_DD());
		params[1] = DateUtils.convertDate(getCalendarTo(), DateFormat.YYYY_MM_DD());
		
		if(getSearchName()!=null && !getSearchName().isEmpty()){
			sql += " AND ( cuz.fullname like '%"+ getSearchName().replace("--", "") +"%'";
			sql += " OR orl.orno like '%"+ getSearchName().replace("--", "") +"%' )";
		}
		
		sql += " ORDER BY orl.orid DESC";
		
		int cnt = 1;
		for(BusinessORTransaction ort : BusinessORTransaction.retrieve(sql, params)){
			ort.setCnt(cnt++);
			orNumbers.add(ort);
			grandTotal += ort.getAmount();
		}
		
		BusinessORTransaction tr = new BusinessORTransaction();
		
		tr.setId(0);
		tr.setCnt(cnt++);
		tr.setDateTrans("Grand Total");
		tr.setOrNumber("");
		tr.setAmount(grandTotal);
		BusinessCustomer cus = new BusinessCustomer();
		cus.setFullname("");
		tr.setCustomer(cus);
		orNumbers.add(tr);
	}
	
	public void loadTaxpayer(){
		
		taxpayers = new ArrayList<BusinessCustomer>();
		
		String sql = " AND cus.cusisactive=1 ";
		
		if(getSearchTaxpayerName()!=null && !getSearchTaxpayerName().isEmpty()){
			//customer.setFullname(Whitelist.remove(getSearchCustomer()));
			sql += " AND cus.fullname like '%" + getSearchTaxpayerName() +"%'";
		}else{
			sql += " order by cus.customerid DESC limit 100;";
		}
		
		taxpayers = BusinessCustomer.retrieve(sql, new String[0]);
		
		if(taxpayers!=null && taxpayers.size()==1) {
			setSearchTaxpayerName("");
			clickItemOwner(taxpayers.get(0));
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("PF('multiDialogOwner').hide();");
		}
		
	}
	
	public void saveOR(){
		BusinessORTransaction or = new BusinessORTransaction();
		boolean isOk = true;
		if(getOrData()!=null){
			or = getOrData();
		}else{
			or.setIsActive(1);
		}
		
		if(getOrNumber()==null || getOrNumber().isEmpty()){
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Or Number");
		}else {
			
			try {
				Integer.valueOf(getOrNumber());
			}catch(NumberFormatException ex){
				isOk = false;
				Application.addMessage(3, "Error", "Please check your number it contain non numeric number");
			}
			
		}
		
		if(getAmount()<=0){
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Amount");
		}
		
		if(getCustomerSelected()==null){
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Citizen Name");
		}
		
		if(getIssuedAddress()==null || getIssuedAddress().isEmpty()){
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Address	");
		}
		
		if(getOrNumber()!=null && getOrNumber().contains(".")) {
			isOk = false;
			Application.addMessage(3, "Error", "Please remove period on OR series");
		}
		
		if(isOk){
			
			if(or.getId()==0) {
				sendSystemMail();
			}
			
			or.setStatus(getStatId());
			or.setDateTrans(DateUtils.convertDate(getIssuedDate(), DateFormat.YYYY_MM_DD()));
			or.setOrNumber(getOrNumber().trim());
			or.setAmount(getAmount());
			or.setAddress(getIssuedAddress());
			or.setCustomer(getCustomerSelected());
			or.setPurpose(getPurpose().toUpperCase());
			or.setUserDtls(Login.getUserLogin().getUserDtls());
			or.setGrossAmount(getGrossAmount());
			or.setIscapital(isCapital()==true? 1 : 0);
			or.save();
			
			clearFlds();
			init();
			Application.addMessage(1, "Success", "Successfully saved");
		}
		
		
	}
	
	private void sendSystemMail() {
		
		int cnt=1;
		String toMailUser = "";
		boolean isCheckNote=false;
		for(String chk : getChecks()) {
			
			isCheckNote=true;
			
			if("Secretary".equalsIgnoreCase(chk)) {
				UserDtls toUser = UserDtls.retrieveUserPositon(Job.SECRETARY.getId());
				if(cnt>1) {
					toMailUser += ":"+toUser.getUserdtlsid()+"";
				}else {
					toMailUser = toUser.getUserdtlsid()+"";
				}
			}else if("Treasurer".equalsIgnoreCase(chk)) {
				UserDtls toUser = UserDtls.retrieveUserPositon(Job.TREASURER.getId());
				if(cnt>1) {
					toMailUser += ":"+toUser.getUserdtlsid()+"";
				}else {
					toMailUser = toUser.getUserdtlsid()+"";
				}
			}else if("Clerk".equalsIgnoreCase(chk)) {
					String sql = "SELECT * FROM userdtls WHERE jobtitleid=?";
					String[] params = new String[1];
					params[0] = Job.CLERK.getId()+"";
					List<UserDtls> toUsers = UserDtls.retrieve(sql, params);
					
					if(toUsers.size()>1) {
						String toM = "";
						int cntM=1;
						for(UserDtls user : toUsers) {
							if(cntM>1) {
								toM += ":"+user.getUserdtlsid()+"";
							}else {
								toM = user.getUserdtlsid()+"";
							}
							cntM++;	
						}
						if(cnt>1) {
							toMailUser += ":"+toM;
						}else {
							toMailUser = toM;
						}
						
					}else {
						if(cnt>1) {
							toMailUser += ":"+toUsers.get(0).getUserdtlsid()+"";
						}else {
							toMailUser = toUsers.get(0).getUserdtlsid()+"";
						}
					}
					
				}
			
			cnt++;
		}
		
		System.out.println("send to " + toMailUser);
		
		if(isCheckNote && !toMailUser.isEmpty()) {
			
			boolean isMore = false;
			try {
				String[] em = toMailUser.split(":");
				isMore=true;
		    }catch(Exception ex) {}
			if(isMore) {
				for(String sendTo : toMailUser.split(":")) {
					Email e = new Email();
					e.setSendDate(DateUtils.getCurrentDateYYYYMMDD());
					e.setTitle("New Official Receipt Created for " + getCustomerSelected().getFullname());
					
					e.setType(EmailType.INBOX.getId());
					e.setIsOpen(0);
					e.setIsDeleted(0);
					e.setIsActive(1);
					
					e.setToEmail(toMailUser);
					e.setPersonCopy(Long.valueOf(sendTo));
					e.setFromEmail("0");
					
					String msg = "";
					msg = "<p><strong>Hi</strong></p>";
					msg += "<br/>";
					msg += "<p>Please see below details for the new created official receipt.</p>";
					msg += "<br/>";
					msg += "<p>Official Receipt No: <strong>" + getOrNumber()+"</strong></p>";
					msg += "<p>Name : <strong>" + getCustomerSelected().getFullname()+"</strong></p>";
					msg += "<p>Purpose: <strong>" + getPurpose().toUpperCase() + "</strong></p>";
					msg += "<br/>";
					msg += "<p><strong>This is a system generated email. Please do not reply.</strong></p>";
					String fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + sendTo;
					Email.emailSavePath(msg, fileName);
					e.setContendId(fileName);
					e.save();
				}
			}else {
				Email e = new Email();
				e.setSendDate(DateUtils.getCurrentDateYYYYMMDD());
				e.setTitle("New Official Receipt Created for " + getCustomerSelected().getFullname());
				
				e.setType(EmailType.INBOX.getId());
				e.setIsOpen(0);
				e.setIsDeleted(0);
				e.setIsActive(1);
				
				e.setToEmail(toMailUser);
				e.setPersonCopy(Long.valueOf(toMailUser));
				e.setFromEmail("0");
				
				String msg = "";
				msg = "<p><strong>Hi</strong></p>";
				msg += "<br/>";
				msg += "<p>Please see below details for the new created official receipt.</p>";
				msg += "<br/>";
				msg += "<p>Official Receipt No: <strong>" + getOrNumber()+"</strong></p>";
				msg += "<p>Name : <strong>" + getCustomerSelected().getFullname()+"</strong></p>";
				msg += "<p>Purpose: <strong>" + getPurpose().toUpperCase() + "</strong></p>";
				msg += "<br/>";
				msg += "<p><strong>This is a system generated email. Please do not reply.</strong></p>";
				String fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + toMailUser;
				Email.emailSavePath(msg, fileName);
				e.setContendId(fileName);
				e.save();
			}
		}
		
	}
	
	public void deleteRow(BusinessORTransaction or){
		or.delete();
		Application.addMessage(1, "Success", "Successfully deleted");
		clearFlds();
		init();
	}
	
	public void clickItem(BusinessORTransaction or){
		
		setOrData(or);
		setOrNumber(or.getOrNumber());
		setAmount(or.getAmount());
		setIssuedAddress(or.getAddress());
		setPurpose(or.getPurpose());
		setIssuedDate(DateUtils.convertDateString(or.getDateTrans(),DateFormat.YYYY_MM_DD()));
		setCustomerSelected(or.getCustomer());
		setCitizenName(or.getCustomer().getFullname());
		setStatId(or.getStatus());
		setGrossAmount(or.getGrossAmount());
		setCapital(or.getIscapital()==0? false : true);
	}
	
	public void clickItemOwner(BusinessCustomer cus){
		setCustomerSelected(cus);
		setCitizenName(cus.getFullname());
	}
	
	public void clearFlds(){
		setOrData(null);
		setCustomerSelected(null);
		setCitizenName(null);
		
		setIssuedDate(null);
		setOrNumber(null);
		setAmount(0);
		setIssuedAddress(null);
		setPurpose(null);
		
		setStatId(1);
		
		checks = new ArrayList<String>();
		setCapital(false);
		setGrossAmount(0);
	}
	
	public Date getCalendarFrom() {
		if(calendarFrom==null){
			
			String date = DateUtils.getCurrentYear()+"";
			date += "-"+(DateUtils.getCurrentMonth()<=9? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+"");
			date += "-01";
			calendarFrom = DateUtils.convertDateString(date, DateFormat.YYYY_MM_DD());
			
		}
		return calendarFrom;
	}

	public void setCalendarFrom(Date calendarFrom) {
		this.calendarFrom = calendarFrom;
	}

	public Date getCalendarTo() {
		if(calendarTo==null){
			calendarTo = DateUtils.convertDateString(DateUtils.getEndOfMonthDate(1, Locale.TAIWAN), DateFormat.YYYY_MM_DD());
			calendarTo = DateUtils.getDateToday();
		}
		return calendarTo;
	}

	public void setCalendarTo(Date calendarTo) {
		this.calendarTo = calendarTo;
	}

	public List<BusinessORTransaction> getOrNumbers() {
		return orNumbers;
	}

	public void setOrNumbers(List<BusinessORTransaction> orNumbers) {
		this.orNumbers = orNumbers;
	}

	public double getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(double grandTotal) {
		this.grandTotal = grandTotal;
	}

	public String getSearchName() {
		return searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}

	public BusinessCustomer getCustomerSelected() {
		return customerSelected;
	}

	public void setCustomerSelected(BusinessCustomer customerSelected) {
		this.customerSelected = customerSelected;
	}

	public String getCitizenName() {
		return citizenName;
	}

	public void setCitizenName(String citizenName) {
		this.citizenName = citizenName;
	}

	public Date getIssuedDate() {
		if(issuedDate==null){
			issuedDate = DateUtils.getDateToday();
		}
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	public String getOrNumber() {
		if(orNumber==null) {
			String orNo =  BusinessORTransaction.getLastORNumber();
			
			int incNo = Integer.valueOf(orNo);
			incNo += 1;
			String newNo = incNo+"";
			int newSize = newNo.length();
			
			switch(newSize) {
				
				case 7: orNumber=newNo; break;
				case 6: orNumber="0"+newNo; break;
				case 5: orNumber="00"+newNo; break;
				case 4: orNumber="000"+newNo; break;
				case 3: orNumber="0000"+newNo; break;
				case 2: orNumber="00000"+newNo; break;
			}
		}
		return orNumber;
	}

	public void setOrNumber(String orNumber) {
		this.orNumber = orNumber;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getIssuedAddress() {
		if(issuedAddress==null){
			issuedAddress = BARANGAY + ", " + MUNICIPALITY + ", " + PROVINCE;
		}
		return issuedAddress;
	}

	public void setIssuedAddress(String issuedAddress) {
		this.issuedAddress = issuedAddress;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public List<BusinessCustomer> getTaxpayers() {
		return taxpayers;
	}

	public void setTaxpayers(List<BusinessCustomer> taxpayers) {
		this.taxpayers = taxpayers;
	}

	public String getSearchTaxpayerName() {
		return searchTaxpayerName;
	}

	public void setSearchTaxpayerName(String searchTaxpayerName) {
		this.searchTaxpayerName = searchTaxpayerName;
	}

	public BusinessORTransaction getOrData() {
		return orData;
	}

	public void setOrData(BusinessORTransaction orData) {
		this.orData = orData;
	}

	public int getStatId() {
		if(statId==0){
			statId=1;
		}
		return statId;
	}

	public void setStatId(int statId) {
		this.statId = statId;
	}

	public List getStats() {
		stats = new ArrayList<>();
		stats.add(new SelectItem(1, "Delivered"));
		stats.add(new SelectItem(2, "Cancelled"));
		
		return stats;
	}

	public void setStats(List stats) {
		this.stats = stats;
	}

	public List<String> getChecks() {
		return checks;
	}

	public void setChecks(List<String> checks) {
		this.checks = checks;
	}

	public double getGrossAmount() {
		return grossAmount;
	}

	public void setGrossAmount(double grossAmount) {
		this.grossAmount = grossAmount;
	}

	public boolean isCapital() {
		return capital;
	}

	public void setCapital(boolean capital) {
		this.capital = capital;
	}
	
}
