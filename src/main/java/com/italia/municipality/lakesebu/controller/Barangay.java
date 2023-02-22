package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;

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
public class Barangay {

	private int id;
	private String name;
	
	public static List<Barangay> retrieve(String sql, String[] params){
		List<Barangay> bars = new ArrayList<>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			Barangay bar = new Barangay();
			bar.setId(rs.getInt("bgid"));
			bar.setName(rs.getString("bgname"));
			bars.add(bar);
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return bars;
	}
}
