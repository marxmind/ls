package com.italia.municipality.lakesebu.enm;

import java.io.File;
/**
 * 
 * @author mark italia
 * @since 02/01/2017
 * @version 1.0
 *
 */
public enum AppConf {
	
	DB_NAME_BANK("databaseBank"),
	DB_NAME_TAX("databaseTax"),
	DB_NAME_WEBTIS("databaseWebTis"),
	DB_AGRICULTURE("da"),
	DB_NAME_CASH("databaseCash"),
	DB_NAME_LICENSING("licensing"),
	DB_DRIVER("driver"),
	DB_URL("url"),
	DB_PORT("port"),
	DB_SSL("SSL"),
	USER_NAME("username"),
	USER_PASS("password"),
	APP_EXP("applicationExp"),
	APP_VER("applicationVersion"),
	APP_COPYRIGHT("copyright"),
	APP_OWNER("author"),
	APP_EMAIL("supportEamil"),
	APP_PHONE("supportNo"),
	SECURITY_ENCRYPTION_FORMAT("utf-8"),
	PRIMARY_DRIVE(System.getenv("SystemDrive")),
	APP_CONFIG_FOLDER_NAME("webtis"),
	//APP_CONFIG_FOLDER_NAME("webtis2"),
	APP_CONFIG_SETTING_FOLDER("conf"),
	APP_CONFIG_SETTING_FILE_NAME("application.xml"),
	APP_LICENSE_FILE_NAME("license.xml"),
	APP_LOG_INCLUDE("includeLog"),
	APP_LOG_PATH("logPath"),
	CHEQUE_REPORT_NAME("chequePdfName"),
	CHEQUE_REPORT_NAME_DISPENSE("dispenseChequePdfName"),
	REPORT_FOLDER("reports"),
	CHEQUE_JRXML_FILE("chequeJrxml"),
	FORM_56("form56"),
	FORM_56_ALL("form56all"),
	CHECK_ISSUED("checkIssued"),
	VOUCHER("voucher"),
	CASH_BOOK("cashbook"),
	SEPERATOR(File.separator),
	CHECKNOXMLS("checknoxmls"),
	LOADXML("loadxml"),
	BACKUPCASHBOOKBANKXML("backupcashbookinbankxml"),
	BACKUPCASHBOOKTREASURYXML("backupcashbookintreasuryxml"),
	LOADCASHBOOKBANKXML("cashbookinbankxml"),
	LOADCASHBOOKTREASURYXML("cashbookintreasuryxml"),
	THEME_STYLE("themeStyle"),
	DTR_REPORT("dtr"),
	FORM11_REPORT("form11"),
	LICENSING_IMG("license-img"),
	SERVER_LOCAL("server-local");
	
	private String value;
	
	private AppConf(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
	
}
