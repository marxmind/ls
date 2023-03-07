package com.italia.municipality.lakesebu.licensing.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.BFilter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class BusinessFilter {

	private int id;
	private String type;
	private String filters;
	private int isActive;
	
	/**
	 * 
	 * @param value
	 * @param filter
	 * @param filters
	 * @return
	 */
	public static boolean checkFilter(String value, String[] filters) {
		boolean hasFound = false;
		for(String ex : filters) {
			if(ex.equalsIgnoreCase(value.trim())) {
				hasFound = true;
			}
		}
		return hasFound;
	}
	
	/**
	 * If found return true
	 * @param value
	 * @param filter
	 * @return
	 */
	public static boolean checkFilter(String value, BFilter filter) {
		String[] filters = filter(filter);
		boolean hasFound = false;
		for(String ex : filters) {
			if(ex.equalsIgnoreCase(value.trim())) {
				hasFound = true;
			}
		}
		return hasFound;
	}
	
	public static String[] filter(BFilter type) {
		String[] filters = new String[0];
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT filters FROM businessfilter WHERE filisactive=1 AND typname='"+ type.getName() +"'");
		
		rs = ps.executeQuery();
		String val = null;
		while(rs.next()){
			val = rs.getString("filters");
		}		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		if(val!=null) {
			
			if(val.contains(",")) {
				List<String> ls = new ArrayList<String>();
				for(String x : val.split(",")) {
					ls.add(x);
				}
				filters = new String[ls.size()];
				for(int i=0; i<ls.size(); i++) {
					filters[i] = ls.get(i);
				}
			}else {
				filters = new String[1];
				filters[0] = val;
			}
			
		}
		
		}catch(Exception e){e.getMessage();}
		
		return filters;
	}
}