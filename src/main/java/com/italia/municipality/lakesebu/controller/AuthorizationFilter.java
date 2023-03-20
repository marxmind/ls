package com.italia.municipality.lakesebu.controller;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(filterName = "AuthFilter", urlPatterns={"*.xhtml"})
public class AuthorizationFilter implements Filter{

	public AuthorizationFilter(){}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException{}
	
	@Override
	public void doFilter(ServletRequest request, 
			ServletResponse response, 
			FilterChain chain) throws IOException, ServletException{
		try{
			HttpServletRequest reqt = (HttpServletRequest)request;
			HttpServletResponse resp = (HttpServletResponse) response;
			HttpSession session = reqt.getSession(false);
			
			String reqURI = reqt.getRequestURI();
			if(reqURI.indexOf("/portal.xhtml")>=0 || reqURI.indexOf("/login.xhtml")>=0 || reqURI.indexOf("/chk.xhtml")>=0
					|| reqURI.indexOf("/loginda.xhtml")>=0 
					|| reqURI.indexOf("/loginlic.xhtml")>=0
					|| reqURI.indexOf("/logingso.xhtml")>=0
					|| reqURI.indexOf("/dilg.xhtml")>=0 
							|| reqURI.indexOf("/client.xhtml")>=0 
							|| reqURI.indexOf("/loginac.xhtml")>=0
									|| reqURI.indexOf("/citizenreg.xhtml")>=0
											|| reqURI.indexOf("/mobile.xhtml")>=0
													|| reqURI.indexOf("/loginper.xhtml")>=0
															|| reqURI.indexOf("/que.xhtml")>=0
					|| (session != null && session.getAttribute("username") !=null)
					|| reqURI.indexOf("/public/")>=0
					|| reqURI.contains("javax.faces.resource")){
				chain.doFilter(request, response);
			}else{
				resp.sendRedirect(reqt.getContextPath() + "/marxmind/portal.xhtml");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy(){}
}