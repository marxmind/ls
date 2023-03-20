package com.italia.municipality.lakesebu.bean;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;

import org.primefaces.PrimeFaces;

import com.italia.municipality.lakesebu.controller.Email;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserAccessLevel;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
* 
* @author mark italia
* @since 01/24/2017
* @version 1.0
*
*/
@Named
@ViewScoped
public class MenuBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 157680978085668L;
	
	private String userFullName;
	private boolean displayMsg;
	private String totalMsg;
	private String total;
	private String styleButton;
	private boolean hasUpdate;
	
	private static final String REPORT_FOLDER = AppConf.PRIMARY_DRIVE.getValue() + File.separator + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + File.separator + "dailyreport" + File.separator;
	
	@PostConstruct
	public void init() {
		loadCountEmailNote();
	}
	
	public UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
	public void updateOff() {
		Login in = Login.getUserLogin();
		in.setHasUpdate(0);
		in.save();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgUpdate').hide(1000);");
	}
	
	public boolean getHasUpdate() {
		return Login.getUserLogin().getHasUpdate()==1? true : false;
	}
	
	public void setHasUpdate(boolean hasUpdate) {
		this.hasUpdate = hasUpdate;
	}
	
	private void runReport() {
		try {
		System.out.println("Run report....");
		
		String pathFile = REPORT_FOLDER;
		
		//explorer C:\bris\bris-runner\bris-report.jar
		File file = new File(pathFile +  "run-report.bat");
		String bat = "java -jar "+ REPORT_FOLDER +"dailyreport.jar";
		 PrintWriter pw = new PrintWriter(new FileWriter(file));
		    pw.println(bat);
	        pw.flush();
	        pw.close();
		
		
		Runtime.getRuntime().exec(pathFile +  "run-report.bat");
		System.out.println("End run report....");
		
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public void onActiveMsg() {
		checkEmail();
	}
	
	public void onIdleMsg() {
		checkEmail();
	}
	
	private void checkEmail() {
		String sql = " AND msgtype=1 AND isopen=0 AND isdeleted=0 AND personcpy=?";
		String[] params = new String[1];
		params[0] = getUser().getUserdtlsid()+"";
		int count = Email.countNewEmail(sql, params);
		if(count>0) {
			//Application.addMessage(1, "Email Received", "You have " + (count>1? count + " emails received." : count + " email received."));
			setTotalMsg("You have " + (count>1? count + " emails received." : count + " email received."));
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("PF('dlgEmailNot').show(1000)");
		}
	}
	
	public void loadCountEmailNote() {
		String sql = " AND msgtype=1 AND isopen=0 AND isdeleted=0 AND personcpy=?";
		String[] params = new String[1];
		params[0] = getUser().getUserdtlsid()+"";
		int count = Email.countNewEmail(sql, params);
		if(count>0) {
			displayMsg = true;
			setDisplayMsg(true);
			setTotalMsg(count+" messages.");
			setTotal(count+"");
			setStyleButton("color:red;");
		}else {
			displayMsg = false;
			setStyleButton("color:black;");
			setTotalMsg(count+" message.");
			setTotal(count+"");
		}
	}
	
	public String stocks() {
		return "stocks";
	}
	
	public String logform() {
		return "logform";
	}
	public String reportsgraph() {
		return "reportsgraph";
	}
	public String orlisting() {
		return "orlisting";
	}
	public String uploadrcd() {
		return "uploadrcd";
	}
	public String vr() {
		return "vr";
	}
	public String waterowner() {
		return "waterowner";
	}
	public String citizenreg() {
		return "citizenreg";
	}
	public String bm() {
		return "bm";
	}
	public String collectionForm() {
		return "collectionform";
	}
	
	public String issuedForm() {
		return "issuedform";
	}
	
	public String market() {
		return "market.xhtml";
	}
	
	public String dtrReport() {
		return "dtr.xhtml";
	}
	
	public String form56(){
		return "form56.xhtml";
	}
	
	public String writing(){
		return "chk.xhtml";
	}
	
	public String funds(){
		return "funds.xhtml";
	}
	
	public String funds2(){
		return "funds2.xhtml";
	}
	
	public String voucher(){
		return "voucher.xhtml";
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
		
		dtls = UserDtls.retrieveUserPositon(dtls.getUserDtls().getJob().getJobid());
		
		dtls.setLogin(in);
		
		return dtls;
	}
	public String cashDv() {
		return "cashdv";
	}
	
	public String checkIssued() {
		return "checkissued";
	}
	
	public String collectionDeposit() {
		return "collection-deposit";
	}
	
	public String getUserFullName() {
		
		UserDtls user = Login.getUserLogin().getUserDtls();
		userFullName = user.getFirstname() + " " + user.getLastname();
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String landTaxCert() {
		return "history";
	}
	
	public String moereport(){
		return "moereport.xhtml";
	}

	public String graphGen(){
		return "graphgen";
	}

	public String departmentexp(){
		return "departmentexp";
	}

	public boolean isDisplayMsg() {
		return displayMsg;
	}

	public void setDisplayMsg(boolean displayMsg) {
		this.displayMsg = displayMsg;
	}

	public String getTotalMsg() {
		return totalMsg;
	}

	public void setTotalMsg(String totalMsg) {
		this.totalMsg = totalMsg;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getStyleButton() {
		return styleButton;
	}

	public void setStyleButton(String styleButton) {
		this.styleButton = styleButton;
	}
}
