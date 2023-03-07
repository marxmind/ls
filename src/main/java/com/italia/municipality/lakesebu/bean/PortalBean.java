package com.italia.municipality.lakesebu.bean;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.AppDepartment;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;


@Named
@ViewScoped
public class PortalBean implements Serializable {

	private static final long serialVersionUID = 456465876441L;
	private static final String PROPERTY_FILE = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +	
			AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() + "office.tis";
	
	@PostConstruct
	public void init() {
		String val = ReadConfig.value(AppConf.SERVER_LOCAL);
        HttpSession session = SessionBean.getSession();
		session.setAttribute("server-local", val);
		session.setAttribute("theme", "vela");
		System.out.println("assigning local: " + val);
	}
	
	public static String getPortal(AppDepartment department) {
		
		Properties prop = new Properties();
		
		try{
			prop.load(new FileInputStream(PROPERTY_FILE));
			return prop.getProperty(department.getName());
		}catch(Exception e) {}	
		
		return "login.xhtml";
	}
	
	public String mayor() {
		return getPortal(AppDepartment.MAYOR);
	}
	
	public String vice() {
		return getPortal(AppDepartment.VICE_MAYOR);
	}
	
	public String treas() {
		return "login"; //getPortal(Department.TREASURER);
	}
	
	public String da() {
		return "loginda";
	}
	
	public String licensing() {
		return "loginlic";
	}
	public String gso() {
		return getPortal(AppDepartment.GSO);
	}
	
	public String accounting() {
		return getPortal(AppDepartment.ACCOUNTING);
	}
	
	public String dilg() {
		return "dilg";//getPortal(Department.DILG);
	}
	
	public String personnel() {
		return "loginper";
	}
}