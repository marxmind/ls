package com.italia.municipality.lakesebu.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.CashBookConnect;
import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;

public class OpenTableAccess {

	
	public static ResultSet query(String sql, String[] params, Object database) {
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
			
		if(database instanceof WebTISDatabaseConnect) {
			conn = WebTISDatabaseConnect.getConnection();
		}else if(database instanceof TaxDatabaseConnect) {
			conn = TaxDatabaseConnect.getConnection();
		}else if(database instanceof BankChequeDatabaseConnect) {
			conn = BankChequeDatabaseConnect.getConnection();
		}else if(database instanceof CashBookConnect) {
			conn = CashBookConnect.getConnection();
		}		
			
		
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();

		//rs.close();
		ps.close();
		
		if(database instanceof WebTISDatabaseConnect) {
			WebTISDatabaseConnect.close(conn);
		}else if(database instanceof TaxDatabaseConnect) {
			TaxDatabaseConnect.close(conn);
		}else if(database instanceof BankChequeDatabaseConnect) {
			BankChequeDatabaseConnect.close(conn);
		}else if(database instanceof CashBookConnect) {
			CashBookConnect.close(conn);
		}
		
		}catch(Exception e){e.getMessage();}
		
		return rs;
	}
	

}
