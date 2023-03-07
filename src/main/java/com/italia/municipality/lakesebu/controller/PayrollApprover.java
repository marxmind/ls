package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/14/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class PayrollApprover {

	private long id;
	private String name;
	private String position;
	private int isActive;
	
	public static Map<Long, PayrollApprover> retrieveAll(){
		Map<Long, PayrollApprover>  apps = new LinkedHashMap<Long, PayrollApprover>();
		
		
		
		String sql = "SELECT * FROM payrollapprover WHERE isactivea=1 ";
		 
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		


		System.out.println("payrollapprover all SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			PayrollApprover app = PayrollApprover.builder()
					.id(rs.getLong("aid"))
					.name(rs.getString("aname"))
					.position(rs.getString("aposition"))
					.isActive(rs.getInt("isactivea"))
					.build();
			
			
			apps.put(app.getId(), app);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return apps;
	}
	
	public static List<PayrollApprover> retrieve(String sql, String[] params){
		List<PayrollApprover> emps = new ArrayList<PayrollApprover>();
		
		
		
		String sqlTemp = "SELECT * FROM payrollapprover WHERE isactivea=1 ";
		 
		
		sql = sqlTemp + sql;
		
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
		System.out.println("payrollapprover SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			PayrollApprover app = PayrollApprover.builder()
					.id(rs.getLong("aid"))
					.name(rs.getString("aname"))
					.position(rs.getString("aposition"))
					.isActive(rs.getInt("isactivea"))
					.build();
			
			
			emps.add(app);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	public static PayrollApprover save(PayrollApprover st){
		if(st!=null){
			
			long id = PayrollApprover.getInfo(st.getId() ==0? PayrollApprover.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = PayrollApprover.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = PayrollApprover.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = PayrollApprover.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			PayrollApprover.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			PayrollApprover.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			PayrollApprover.insertData(this, "3");
		}
		
	}
	
	public static PayrollApprover insertData(PayrollApprover st, String type){
		String sql = "INSERT INTO payrollapprover ("
				+ "aid,"
				+ "aname,"
				+ "aposition,"
				+ "isactivea)" 
				+ " VALUES(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table payrollapprover");
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
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getPosition());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getName());
		LogU.add(st.getPosition());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to payrollapprover : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static PayrollApprover updateData(PayrollApprover st){
		String sql = "UPDATE payrollapprover SET "
				+ "aid=?,"
				+ "aname=?,"
				+ "aposition=?" 
				+ " WHERE fid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table payrollapprover");
		
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getPosition());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getPosition());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to payrollapprover : " + s.getMessage());
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
		sql="SELECT aid FROM payrollapprover ORDER BY aid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("aid");
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
		ps = conn.prepareStatement("SELECT aid FROM payrollapprover WHERE aid=?");
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
		String sql = "UPDATE payrollapprover set isactivea=0 WHERE aid=?";
		
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
