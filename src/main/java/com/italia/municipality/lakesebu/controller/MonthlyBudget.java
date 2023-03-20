package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class MonthlyBudget {

	private long id;
	private String dateTrans;
	private String description;
	private double budgetAmount;
	private int budgetType;
	private int isActive;
	private int month;
	private int year;
	private Offices offices;
	
	public static MonthlyBudget save(MonthlyBudget st){
		if(st!=null){
			
			long id = MonthlyBudget.getInfo(st.getId() ==0? MonthlyBudget.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = MonthlyBudget.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = MonthlyBudget.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = MonthlyBudget.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			MonthlyBudget.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			MonthlyBudget.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			MonthlyBudget.insertData(this, "3");
		}
		
 }
	
	public static MonthlyBudget updateData(MonthlyBudget st){
		String sql = "UPDATE monthlybudget SET "
				+ "encodedate=?,"
				+ "description=?,"
				+ "budgetamount=?,"
				+ "budgettype=?,"
				+ "montbudget=?,"
				+ "yearbudget=?,"
				+ "offid=?" 
				+ " WHERE buid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table monthlybudget");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getDescription());
		ps.setDouble(cnt++, st.getBudgetAmount());
		ps.setInt(cnt++, st.getMonth());
		ps.setInt(cnt++, st.getYear());
		ps.setLong(cnt++, st.getOffices().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getDescription());
		LogU.add(st.getBudgetAmount());
		LogU.add(st.getMonth());
		LogU.add(st.getYear());
		LogU.add(st.getOffices().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to moomonthlybudgete : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static MonthlyBudget insertData(MonthlyBudget st, String type){
		String sql = "INSERT INTO monthlybudget ("
				+ "buid,"
				+ "encodedate,"
				+ "description,"
				+ "budgetamount,"
				+ "budgettype,"
				+ "isactiveb,"
				+ "montbudget,"
				+ "yearbudget,"
				+ "offid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table monthlybudget");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			st.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			st.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getDescription());
		ps.setDouble(cnt++, st.getBudgetAmount());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getMonth());
		ps.setInt(cnt++, st.getYear());
		ps.setLong(cnt++, st.getOffices().getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getDescription());
		LogU.add(st.getBudgetAmount());
		LogU.add(st.getIsActive());
		LogU.add(st.getMonth());
		LogU.add(st.getYear());
		LogU.add(st.getOffices().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to moomonthlybudgete : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT buid FROM monthlybudget ORDER BY buid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("buid");
		}
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static long getInfo(long id){
		boolean isNotNull=false;
		long result =0;
		//id no data retrieve.
		//application will insert a default no which 1 for the first data
		long val = getLatestId();	
		if(val==0){
			isNotNull=true;
			result= 1; // add first data
			System.out.println("First data");
		}
		
		//check if empId is existing in table
		if(!isNotNull){
			isNotNull = isIdNoExist(id);
			if(isNotNull){
				result = 2; // update existing data
				System.out.println("update data");
			}else{
				result = 3; // add new data
				System.out.println("add new data");
			}
		}
		
		
		return result;
	}
	public static boolean isIdNoExist(long id){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT buid FROM monthlybudget WHERE buid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE monthlybudget set isactiveb=0 WHERE buid=?";
		
		String[] params = new String[1];
		params[0] = getId()+"";
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	
}
