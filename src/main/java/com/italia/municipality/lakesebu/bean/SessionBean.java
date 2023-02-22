package com.italia.municipality.lakesebu.bean;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionBean {

	public static HttpSession getSession(){
		return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	}
	public static HttpServletRequest getRequest(){
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}
	public static String getUserName(){
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getAttribute("username").toString();
	}
	public static String getUserId(){
		HttpSession session = getSession();
		if(session != null){
			return (String)session.getAttribute("userid");
		}else{
			return null;
		}
	}
}
