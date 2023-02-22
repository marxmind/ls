package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author mark italia
 * @since 04/09/2017
 * @version 1.0
 *
 */

@Named
@ViewScoped
public class ThemeBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 658674386854751L;

	public String getApplicationTheme(){
		String value = "saga";
		try{
			//value = ReadConfig.value(AppConf.THEME_STYLE);
			HttpSession session = SessionBean.getSession();
			value = session.getAttribute("theme").toString();
		}catch(Exception e){}
		return value;
	}
	
}
