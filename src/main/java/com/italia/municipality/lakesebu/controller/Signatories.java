package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	private int isActive;
	private int primary;
	
	
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
			
			Signatories sg = Signatories.builder()
					.id(rs.getInt("sigid"))
					.name(rs.getString("signame"))
					.position(rs.getString("sigposition"))
					.isOfficial(rs.getInt("isofficial"))
					.primary(rs.getInt("primaryper"))
					.isActive(rs.getInt("isactivesig"))
					.build();
			
			sigs.add(sg);
			
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sigs;
	}
	
	public static Map<String, Signatories> retrieveSig(String sql, String[] params){
		Map<String, Signatories> sigs = new LinkedHashMap<String, Signatories>();
		
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
			
			Signatories sg = Signatories.builder()
					.id(rs.getInt("sigid"))
					.name(rs.getString("signame"))
					.position(rs.getString("sigposition"))
					.isOfficial(rs.getInt("isofficial"))
					.isActive(rs.getInt("isactivesig"))
					.build();
			sigs.put(sg.getId()+"", sg);
			
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sigs;
	}
	
	public static Map<Integer, Signatories> retrieveAll(){
		Map<Integer, Signatories> sigs = new LinkedHashMap<Integer, Signatories>();
		String sql = "SELECT * FROM signatory WHERE isactivesig=1 ";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Signatories sg = Signatories.builder()
					.id(rs.getInt("sigid"))
					.name(rs.getString("signame"))
					.position(rs.getString("sigposition"))
					.isOfficial(rs.getInt("isofficial"))
					.primary(rs.getInt("primaryper"))
					.isActive(rs.getInt("isactivesig"))
					.build();
			sigs.put(sg.getId(), sg);
			
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sigs;
	}
	
	public static Signatories retrieve(int id){
		Signatories sg = new Signatories();
		
		String sql = "SELECT * FROM signatory WHERE sigid=" + id;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
					sg = Signatories.builder()
					.id(rs.getInt("sigid"))
					.name(rs.getString("signame"))
					.position(rs.getString("sigposition"))
					.isOfficial(rs.getInt("isofficial"))
					.primary(rs.getInt("primaryper"))
					.isActive(rs.getInt("isactivesig"))
					.build();
			
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return sg;
	}
	
}
