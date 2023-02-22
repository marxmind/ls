package com.italia.municipality.lakesebu.security;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.primefaces.context.PrimeFacesContext;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author mark italia
 * @since 09/28/2016
 * @version 1.0
 *
 */
public class ClientInfo {

	public static String getBrowserName() {
		ExternalContext externalContext = PrimeFacesContext.getCurrentInstance().getExternalContext();
	    String userAgent = externalContext.getRequestHeaderMap().get("User-Agent");

	    if(userAgent.contains("MSIE")){ 
	        return "Internet Explorer";
	    }
	    if(userAgent.contains("Firefox")){ 
	        return "Firefox";
	    }
	    if(userAgent.contains("Chrome")){ 
	        return "Chrome";
	    }
	    if(userAgent.contains("Opera")){ 
	        return "Opera";
	    }
	    if(userAgent.contains("Safari")){ 
	        return "Safari";
	    }
	    return "Unknown";
	}
	
	public static String getLocalHost(){
		InetAddress ip;
        try{
        	ip = InetAddress.getLocalHost();
        	return ip.getHostAddress();
        }catch(UnknownHostException e){
        	return "host unknown : " +e.getMessage();
        }
        
	}
	public static String getLocalHostName(){
		InetAddress ip;
        try{
        	ip = InetAddress.getLocalHost();
        	return ip.getHostName();
        }catch(UnknownHostException e){
        	return "host unknown : " +e.getMessage();
        }
	}
	
	public static String getClientIP(){
		String clientIp=null;
		try{
	        HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
	        clientIp = req.getHeader("X-FORWARDED-FOR");
	        if(clientIp==null){
	        	clientIp = req.getRemoteAddr();
	        }
			}catch(Exception e){
	        	clientIp = "host unknown : " +e.getMessage();
	        }
		return clientIp;
	}
}
