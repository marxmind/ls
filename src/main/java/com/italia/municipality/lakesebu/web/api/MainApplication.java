package com.italia.municipality.lakesebu.web.api;

import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class MainApplication extends Application {
	
	@Override
	public Set<Class<?>> getClasses(){
		return Set.of(OfficialReceipt.class);
	} 
	
}
