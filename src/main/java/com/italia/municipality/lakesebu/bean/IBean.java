package com.italia.municipality.lakesebu.bean;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author mark italia
 * @since 10/01/2016
 * @version 1.0
 *
 */
public class IBean {

	/**
	 * Remove and invalidate user session
	 */
	public static void removeBean(){
		HttpSession session = SessionBean.getSession();
		String[] beans = {
				"versionBean",
				"checkBean",
				"budgetBean",
				"dBPCheckBean",
				"graphBean",
				"form56Bean",
				"reportBean",
				"userBean"
				};
		for(String bean : beans){
			FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(bean);
		}
		session.invalidate();
	}
	
}
