package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BudgetType implements IBudgetType{

	private int id;
	private String name;
	private int isActive;
	
	public static List<IBudgetType> retrieve(String sql, String[] params){
		List<IBudgetType> types =  new ArrayList<IBudgetType>();
		
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
			
			IBudgetType type = new BudgetType();
			type.setId(rs.getInt("budtypeid"));
			type.setName(rs.getString("typename"));
			type.setIsActive(rs.getInt("typeisactive"));
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
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name = name;
	}

	@Override
	public int getIsActive() {
		// TODO Auto-generated method stub
		return isActive;
	}

	@Override
	public void setIsActive(int isActive) {
		// TODO Auto-generated method stub
		this.isActive = isActive;
	}
}