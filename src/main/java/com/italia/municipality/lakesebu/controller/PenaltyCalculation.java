package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.enm.PenalyMonth;

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
public class PenaltyCalculation {

	private long id;
	private int year;
	private double january;
	private double february;
	private double march;
	private double april;
	private double may;
	private double june;
	private double july;
	private double august;
	private double september;
	private double october;
	private double november;
	private double december;
	
	public static List<PenaltyCalculation> retrieve(String sql, String[] params){
		List<PenaltyCalculation> calcs = new ArrayList<>();
		
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
		
		System.out.println("SQL " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			PenaltyCalculation cal = new PenaltyCalculation();
			cal.setId(rs.getLong("calid"));
			cal.setYear(rs.getInt("year"));
			cal.setJanuary(rs.getDouble("jana"));
			cal.setFebruary(rs.getDouble("feba"));
			cal.setMarch(rs.getDouble("mara"));
			cal.setApril(rs.getDouble("apra"));
			cal.setMay(rs.getDouble("maya"));
			cal.setJune(rs.getDouble("juna"));
			cal.setJuly(rs.getDouble("jula"));
			cal.setAugust(rs.getDouble("auga"));
			cal.setSeptember(rs.getDouble("sepa"));
			cal.setNovember(rs.getDouble("nova"));
			cal.setDecember(rs.getDouble("deca"));
		}
		
		TaxDatabaseConnect.close(conn);
		rs.close();
		ps.close();
		
		}catch(SQLException e){}
		
		return calcs;
	}
	
	public static double monthPenalty(int year, PenalyMonth month){
		String sql = "SELECT " + month.getName() + " FROM landtaxpenaltiescal WHERE year=?";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		ps.setInt(1, year);
		
		
		System.out.println("SQL " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return rs.getDouble(month.getName());
		}
		
		TaxDatabaseConnect.close(conn);
		rs.close();
		ps.close();
		
		}catch(SQLException e){}
		
		
		return 0;
	}
}
