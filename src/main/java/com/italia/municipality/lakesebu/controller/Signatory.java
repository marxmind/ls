package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * Please use the new Class
 * {@link}Signatories.class
 *
 */
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Signatory {

	private int sigId;
	private String sigName;
	private String sigPosition;
	private String sigCreated;
	
	public static List<Signatory> retrieve(String sql, String[] params){
		List<Signatory> sigs =  new ArrayList<Signatory>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Signatory sig = new Signatory();
			try{sig.setSigId(rs.getInt("sig_id"));}catch(NullPointerException e){}
			try{sig.setSigName(rs.getString("sig_name"));}catch(NullPointerException e){}
			try{sig.setSigPosition(rs.getString("sig_position"));}catch(NullPointerException e){}
			try{sig.setSigCreated(rs.getString("sig_created"));}catch(NullPointerException e){}
			sigs.add(sig);
			
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sigs;
	}
	
	public static Map<String, Signatory> retrieveSig(String sql, String[] params){
		Map<String,Signatory> sigs = new HashMap<String,Signatory>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Signatory sig = new Signatory();
			try{sig.setSigId(rs.getInt("sig_id"));}catch(NullPointerException e){}
			try{sig.setSigName(rs.getString("sig_name"));}catch(NullPointerException e){}
			try{sig.setSigPosition(rs.getString("sig_position"));}catch(NullPointerException e){}
			try{sig.setSigCreated(rs.getString("sig_created"));}catch(NullPointerException e){}
			sigs.put(sig.getSigId()+"", sig);
			
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sigs;
	}
}
