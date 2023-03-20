package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.Pages;
import com.italia.municipality.lakesebu.security.AppModule;
import com.italia.municipality.lakesebu.security.ClientInfo;
import com.italia.municipality.lakesebu.security.License;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import com.italia.municipality.lakesebu.utils.Whitelist;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

@Named
@ViewScoped
@Data
public class LoginBean implements Serializable{

	private static final long serialVersionUID = 1094801825228386363L;
	
	private String name;
	private String password;
	private String errorMessage;
	private String keyPress;
	
	private String themeId="vela";
	private List themes;
	
	private int moduleId;
	private List modules;
	
	@PostConstruct
	public void init() {
		//DailyReport.runReport();
	}
	
	//validate login
 public String validateUserNamePassword(){
		
		String sql = "SELECT * FROM login WHERE username=? and password=?";
		String[] params = new String[2];
		         params[0] = Whitelist.remove(name);
		         params[1] = Whitelist.remove(password);
		Login in = null;
		try{in = Login.retrieve(sql, params).get(0);}catch(Exception e){return "login";}
		
		/*boolean valid = Login.validate(sql, params);
		System.out.println("Valid: " + valid);*/
		
		String result="login";
		LogU.add("Guest with username : " + name + " and password ******** is trying to log in the system.");
		
		if(in!=null){
			String val = ReadConfig.value(AppConf.SERVER_LOCAL);
	        HttpSession session = SessionBean.getSession();
	        session.setAttribute("username", name);
			session.setAttribute("userid", in.getLogid());
			session.setAttribute("server-local", val);
			session.setAttribute("theme", getThemeId());
			
			if(getModuleId()>0) {
				result = assignModule();
			}else {
				result="main.xhtml";
				//result="dashboard.xhtml";//temporary
				
			}
			
			LogU.add("The user has been successfully login to the application with the username : " + name + " and password " + password);
			
			
			//license checker
			String det="";
			  switch(in.getAccessLevel().getLevel()){ 
			  	case 1:
			  		result="main.xhtml";
			  		if(in.getLogid()>1) {
				  		det = License.dbLicense(AppModule.MAIN_APP);
					  	if(checkdate(det)) {
					  		result="expired";
					  	}
			  		}
			  		break;
			    case 8:
			    	result="vr.xhtml"; 
			    	det = License.dbLicense(AppModule.VOUCHER_RECORDING);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
			    	break;
				case 9: result="main.xhtml"; break;
				case 10: 
					result="mainda.xhtml"; 
					det = License.dbLicense(AppModule.DA_FISHERY);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
					break;
				case 11:
					result="mainlic.xhtml"; 
					det = License.dbLicense(AppModule.LICENSING_CLERK);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
					break;
				case 12: result="maingso.xhtml"; break;
				
			  
			  case 14: {
				  result="orlisting.xhtml";
				  det = License.dbLicense(AppModule.GENERAL_COLLECTIONS);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
				  break;
			  }
			    
			    case 6: //same with 15
			    case 15:
			    	result="form56.xhtml"; 
			    	det = License.dbLicense(AppModule.LAND_TAX);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
			    	break;
				case 16: 
					result="mainpersonnel.xhtml"; 
					det = License.dbLicense(AppModule.PERSONNEL_CLERK);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
					break;
					
				case 13: //same with 17
				case 17: 
					result="mainacc.xhtml"; 
					det = License.dbLicense(AppModule.ACCOUNTING_CLERK);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
					break;
				case 18: result="checkissued.xhtml"; break;
			  
			  case 19: {
				  result="chk.xhtml";
				  det = License.dbLicense(AppModule.CHECK_WRITING);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
				  break;
			  }
			  
			  case 20: {
				  result="logform.xhtml";
				  det = License.dbLicense(AppModule.COLLECTORS_RECORDING);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
				  break;
			  }
			  
			  case 21: {
				  result="stocks.xhtml";
				  det = License.dbLicense(AppModule.STOCK_RECORDING);
				  	if(checkdate(det)) {
				  		result="expired";
				  	}
				  break;
			  }
			  
			  }
			 
			
			//Check application Expiration
			/*if(Copyright.checkLicenseExpiration()){	
				LogU.add("The application is expired. Please contact application owner.");
				result = "expired";
			}else{*/
				logUserIn(in);
			//}
		}else{
			FacesContext.getCurrentInstance().addMessage(
					null,new FacesMessage(
							FacesMessage.SEVERITY_WARN, 
							"Incorrect username and password", 
							"Please enter correct username and password"
							)
					);
//			/setErrorMessage("Incorrect username and password.");
			LogU.add("The user was not successfully login to the application with the username : " + name + " and password " + password);
			setName("");
			setPassword("");
			result= "login";
		}
		
		return result;
	}

 private static boolean checkdate(String dbLicense){
		
		String systemDate = DateUtils.getCurrentDateMMDDYYYY();
		
		SimpleDateFormat dFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		try{
		Date dbDate = dFormat.parse(dbLicense);	
		Date sysDate = dFormat.parse(systemDate);
		
		System.out.println("dbDate = " + dbDate);
		System.out.println("sysDate = " + sysDate);
		
		if(dbDate.compareTo(sysDate)>0){
			System.out.println("Not expired");
		}else if(dbDate.compareTo(sysDate)<0){
			System.out.println("Expired...");
			return true;
		}else if(dbDate.compareTo(sysDate)==0){
			System.out.println("Expired...");
			return true;
		}else{
			System.out.println("Expired...");
			return true;
		}
		
		}catch(ParseException pre){}
		
		
		
		return false;
	}
 
private String assignModule() {
	
	switch(getModuleId()) {
		case 1 : return Pages.CheckWriting.getName();
		case 2 : return Pages.LandTax.getName();
		case 4 : return Pages.VoucherLoging.getName();
		case 5 : return Pages.Mooe.getName();
		case 6 : return Pages.CashInBank.getName();
		case 7 : return Pages.CashInTreasury.getName();
		case 8 : return Pages.Dtr.getName();
		case 9 : return Pages.Market.getName();
		case 10 : return Pages.StockRecording.getName();
		case 11 : return Pages.IssuedForm.getName();
		case 12 : return Pages.CollectorRecording.getName();
		case 13 : return Pages.ReportGraph.getName();
		case 14 : return Pages.Orlisting.getName();
		case 15 : return Pages.LandTax.getName();
		case 16 : return Pages.UploadRcd.getName();
		case 17 : return Pages.VoucherExpense.getName();
		case 18 : return Pages.WaterBilling.getName();
		case 19 : return Pages.LandTaxCertification.getName();
	}
	
	
	return "main";
}

private void logUserIn(Login in){
	if(in==null) in = new Login();
	ClientInfo cinfo = new ClientInfo();
	in.setLogintime(DateUtils.getCurrentDateMMDDYYYYTIME());
	in.setIsOnline(1);
	in.setClientip(cinfo.getClientIP());
	in.setClientbrowser(cinfo.getBrowserName());
	in.save();
}

private void logUserOut(){
	String sql = "SELECT * FROM login WHERE username=? and logid=?";
	HttpSession session = SessionBean.getSession();
	String userid = session.getAttribute("userid").toString();
	String username = session.getAttribute("username").toString();
	String[] params = new String[2];
	params[0] = username;
	params[1] = userid;
	Login in = null;
	try{in = Login.retrieve(sql, params).get(0);}catch(Exception e){}
	ClientInfo cinfo = new ClientInfo();
	if(in!=null){
		in.setLastlogin(DateUtils.getCurrentDateMMDDYYYYTIME());
		in.setIsOnline(0);
		in.setClientip(cinfo.getClientIP());
		in.setClientbrowser(cinfo.getBrowserName());
		in.save();
	}
	LogU.add("The user " + username + " was logging out to the application.");
	
	//Remove registered bean in session
	IBean.removeBean();
	
}

	
	//logout event, invalidate session
	public String logout(){
		logUserOut();
		//IBean.removeBean();
		//removeBean();
		/*HttpSession session = SessionBean.getSession();
		FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("loginBean");*/
		setName("");
		setPassword("");
		//session.invalidate();
		return "login.xhtml?faces-redirect=true";
	}
	
	//logout event, invalidate session
	@Deprecated
	private void refreshBean(){
		String[] beans = {
				"checkBean",
				"dBPCheckBean",
				"reportBean",
				"userBean"
				};
		for(String bean : beans){
			FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(bean);
		}
		}
	
	/**
	 * Remove and invalidate user session
	 */
	@Deprecated
	public static void removeBean(){
		HttpSession session = SessionBean.getSession();
		String[] beans = {
				"loginBean",
				"checkBean",
				"dBPCheckBean",
				"reportBean",
				"userBean"
				};
		for(String bean : beans){
			FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(bean);
		}
		session.invalidate();
	}
	
	public String getKeyPress() {
		keyPress = "logId";
		return keyPress;
	}


	public void setKeyPress(String keyPress) {
		this.keyPress = keyPress;
	}
	public int getModuleId() {
		return moduleId;
	}
	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}
	public List getModules() {
		modules = new ArrayList<>();
		modules.add(new SelectItem(0, "Select Module"));
		for(AppModule m : AppModule.values()) {
			modules.add(new SelectItem(m.getId(), m.getName()));
		}
		
		return modules;
	}
	public void setModules(List modules) {
		this.modules = modules;
	}
	
	public String getThemeId() {
		return themeId;
	}

	public void setThemeId(String themeId) {
		this.themeId = themeId;
	}

	public List getThemes() {
		themes = new ArrayList<>();
		themes.add(new SelectItem("arya", "ARYA"));
		themes.add(new SelectItem("saga", "SAGA"));
		themes.add(new SelectItem("vela", "VELA"));
		return themes;
	}

	public void setThemes(List themes) {
		this.themes = themes;
	}
}
