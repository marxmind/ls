package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 08/02/2019
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Signatories {
	
	private int id;
	private String name;
	private String position;
	private int isOfficial;
	
	
	public static List<Signatories> retrieve(String sql, String[] params){
		List<Signatories> sigs =  new ArrayList<Signatories>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		//System.out.println("SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Signatories sig = new Signatories();
			try{sig.setId(rs.getInt("sigid"));}catch(NullPointerException e){}
			try{sig.setName(rs.getString("signame"));}catch(NullPointerException e){}
			try{sig.setPosition(rs.getString("sigposition"));}catch(NullPointerException e){}
			try{sig.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			sigs.add(sig);
			
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sigs;
	}
	
	public static Signatories retrieve(int id){
		Signatories sig = new Signatories();
		
		String sql = "SELECT * FROM signatory WHERE sigid=" + id;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{sig.setId(rs.getInt("sigid"));}catch(NullPointerException e){}
			try{sig.setName(rs.getString("signame"));}catch(NullPointerException e){}
			try{sig.setPosition(rs.getString("sigposition"));}catch(NullPointerException e){}
			try{sig.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sig;
	}
	
}
