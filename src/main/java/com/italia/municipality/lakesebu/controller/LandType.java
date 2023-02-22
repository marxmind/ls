package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;

public class LandType  implements ILandType{

	private int id;
	private String landType;
	private Timestamp timestamp;
	
	public LandType() {}
	
	public LandType(
			int id,
			String landType
			) {
		this.id = id;
		this.landType = landType;
	}
	
	public static List<ILandType> retrieve(String sql, String[] params){
		List<ILandType> types = new ArrayList<ILandType>();
		
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
			ILandType type = new LandType();
			type.setId(rs.getInt("landid"));
			type.setLandType(rs.getString("landtype"));
			type.setTimestamp(rs.getTimestamp("landtimestamp"));
			types.add(type);
		}
		
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return types;
	}
	
	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}
	@Override
	public void setId(int id) {
		// TODO Auto-generated method stub
		this.id = id;
	}
	@Override
	public String getLandType() {
		// TODO Auto-generated method stub
		return landType;
	}
	@Override
	public void setLandType(String landType) {
		// TODO Auto-generated method stub
		this.landType = landType;
	}

	@Override
	public Timestamp getTimestamp() {
		// TODO Auto-generated method stub
		return timestamp;
	}

	@Override
	public void setTimestamp(Timestamp timestamp) {
		// TODO Auto-generated method stub
		this.timestamp = timestamp;
	}
}
