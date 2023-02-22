package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.TaxPayorTrans;
import com.italia.municipality.lakesebu.controller.UserAccessLevel;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author mark
 * @since 02/11/2017
 * @version 1.0
 *
 */
@Named
@ViewScoped
@Data
public class MainBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 65568785234541L;
	
	
	private String userName;
	private String greetings;
	private String checkWritingData;
	private String timeCheck;
	private String landTaxData;
	private String[] accounts;
	
	private String firstName;
	private String middleName;
	private String lastName;
	private String age;
	
	private String oldUserName;
	private String newUserName;
	private String oldPassword;
	private String newPassword;
	
	public void savePersonal(){
		try{
		UserDtls user = getUserProperites();
		if(user!=null){
			boolean isOk= false;
			if(getFirstName()!=null && !getFirstName().isEmpty()){
				user.setFirstname(getFirstName());
				isOk= true;
			}
			if(getMiddleName()!=null && !getMiddleName().isEmpty()){
				user.setMiddlename(getMiddleName());
				isOk= true;
			}
			if(getLastName()!=null && !getLastName().isEmpty()){
				user.setLastname(getLastName());
				isOk= true;
			}
			if(getAge()!=null && !getAge().isEmpty()){
				try{user.setAge(Integer.valueOf(getAge()));}catch(Exception e){}
				isOk= true;
			}
			
			if(isOk){
				user.save();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Information has been successfully updated", "");
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}else{
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "No information was updated", "");
		        FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			
		}
		}catch(Exception e){
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Something went wrong. Please logout and login again", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveLogin(){
		try{
			Login user = getUserProperites().getLogin();
			if(user!=null){
				String sql = "SELECT * FROM login WHERE ";
				String[] params = new String[1]; 
				boolean isOk = false;
				if(getOldUserName()!=null && !getOldUserName().isEmpty()){
				
					if(getNewUserName()!=null && !getNewUserName().isEmpty()){
						
						sql += " username=?";
						params[0] = getNewUserName().replace("--", "");
						Login in = null;
						try{in = Login.retrieve(sql, params).get(0);}catch(IndexOutOfBoundsException e){}
						if(in!=null){
							FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "The new username that you've provided is already in use. Please select another username", "");
					        FacesContext.getCurrentInstance().addMessage(null, msg);
						}else{
							user.setUsername(getNewUserName());
							isOk = true;
						}
						
					}
					
				}
				
				if(getOldPassword()!=null && !getOldPassword().isEmpty()){
					
					if(getNewPassword()!=null && !getNewPassword().isEmpty()){
						
						sql += " password=?";
						params[0] = getNewPassword().replace("--", "");
						Login in = null;
						try{in = Login.retrieve(sql, params).get(0);}catch(IndexOutOfBoundsException e){}
						if(in!=null){
							FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "The new password that you've provided is already in use. Please select another password", "");
					        FacesContext.getCurrentInstance().addMessage(null, msg);
						}else{
							user.setPassword(getNewPassword());
							isOk = true;
						}
						
					}
					
				}
				
				if(isOk){
					
					sql = "SELECT * FROM login WHERE username=? AND password=?";
					params = new String[2];
					params[0] = getOldUserName().replace("--", "");
					params[1] = getOldPassword().replace("--", "");
					Login in = null;
					try{in = Login.retrieve(sql, params).get(0);}catch(IndexOutOfBoundsException e){}
					
					if(in !=null ){
						in = user;
						in.save();
						FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Login credentials have been successfully changed", "");
				        FacesContext.getCurrentInstance().addMessage(null, msg);
					}else{
						FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "The username and password you've provided to change the login credential is incorrect", "");
				        FacesContext.getCurrentInstance().addMessage(null, msg);
					}
				}
				
			}
		}catch(Exception e){
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Something went wrong. Please logout and login again", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public UserDtls getUserProperites(){
		UserDtls dtls = new UserDtls();
		Login in = new Login();
		dtls = Login.getUserLogin().getUserDtls();
		in = Login.getUserLogin();
		String sql = "SELECT * FROM useraccesslevel WHERE useraccesslevelid=?";
		String[] params = new String[1];
		params[0] = in.getAccessLevel().getUseraccesslevelid()+"";
		
		UserAccessLevel lvl = UserAccessLevel.retrieve(sql, params).get(0);
		in.setAccessLevel(lvl);
		
		dtls = UserDtls.retrieveUserPositon(dtls.getUserDtls().getJob().getJobid());//UserDtls.retrieve(user).get(0);
		
		dtls.setLogin(in);
		
		return dtls;
	}
	
	public String getUserName() {
		userName = Login.getUserLogin().getUserDtls().getFirstname();
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getGreetings() {
		
		String[] greets = {"Top of the", "Good", "Hey there, hope you're having a fine", "Hope, you are having a wonderful","Cool"};
		greetings = greets[(int) (Math.random() * greets.length)];
		
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		if(timeOfDay >= 0 && timeOfDay <12){
			greetings = greetings + " morning";
		}else if(timeOfDay >= 12 && timeOfDay < 16){
			greetings = greetings + " afternoon";
		}else if(timeOfDay >= 16 && timeOfDay < 21){
			greetings = greetings + " evening";
		}else if(timeOfDay >= 21 && timeOfDay < 24){
			greetings = greetings + " night";
		}
		
		return greetings;
	}


	public void setGreetings(String greetings) {
		this.greetings = greetings;
	}


	public String getCheckWritingData() {
		
		String sql = "SELECT * FROM tbl_chequedtls WHERE date_disbursement=?";
		String[] params = new String[1];
		params[0] = DateUtils.getCurrentDateYYYYMMDD();
		List<Chequedtls> dtls = Chequedtls.retrieve(sql, params);
		
		if(dtls.size()>0){
			double amount = 0d;
			for(Chequedtls d : dtls){
				amount+= d.getAmount();
			}
			checkWritingData = "Php " + Currency.formatAmount(amount);
		}else{
			checkWritingData = "Php 0.00";
		}
		
		
		return checkWritingData;
	}


	public void setCheckWritingData(String checkWritingData) {
		this.checkWritingData = checkWritingData;
	}


	public String getTimeCheck() {
		timeCheck = DateUtils.getCurrentDateMMDDYYYYTIME();
		return timeCheck;
	}


	public void setTimeCheck(String timeCheck) {
		this.timeCheck = timeCheck;
	}


	public String getLandTaxData() {
		
		String sql = "SELECT * FROM taxpayortrans WHERE payortransdate=?";
		String[] params = new String[1];
		params[0] = DateUtils.getCurrentDateYYYYMMDD();
		double amount = TaxPayorTrans.retrieveTotal(sql, params);
		landTaxData = "Php "+Currency.formatAmount(amount);
		return landTaxData;
	}


	public void setLandTaxData(String landTaxData) {
		this.landTaxData = landTaxData;
	}


	public String[] getAccounts() {
		
		String sql = "SELECT * FROM tbl_chequedtls WHERE date_disbursement=?";
		String[] params = new String[1];
		params[0] = DateUtils.getCurrentDateYYYYMMDD();
		double accntSEF = 0d, accntGen=0d, accntTrust1=0d, accntTrust2=0d, accntMotor = 0d;
		for(Chequedtls dtls : Chequedtls.retrieve(sql, params)){
			
			if("1".equalsIgnoreCase(dtls.getAccntNumber())){
				accntSEF += dtls.getAmount();
			}else if("2".equalsIgnoreCase(dtls.getAccntNumber())){
				accntGen += dtls.getAmount();
			}else if("3".equalsIgnoreCase(dtls.getAccntNumber())){
				accntTrust1 += dtls.getAmount();	
			}else if("4".equalsIgnoreCase(dtls.getAccntNumber())){
				accntMotor += dtls.getAmount();
			}else if("5".equalsIgnoreCase(dtls.getAccntNumber())){
				accntTrust2 += dtls.getAmount();
			}	
		}
		
		accounts = new String[5];
		accounts[0] = Currency.formatAmount(accntSEF);
		accounts[1] = Currency.formatAmount(accntGen);
		accounts[2] = Currency.formatAmount(accntTrust1);
		accounts[3] = Currency.formatAmount(accntMotor);
		accounts[4] = Currency.formatAmount(accntTrust2);
		
		return accounts;
	}

}
