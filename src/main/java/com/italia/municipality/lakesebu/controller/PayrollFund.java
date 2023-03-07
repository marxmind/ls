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
public class PayrollFund {

	private long id;
	private String name;
	private double amount;
	private int isActive;
	
	public static Map<Long,PayrollFund> retrieveAll(){
		Map<Long,PayrollFund> emps = new LinkedHashMap<Long,PayrollFund>();
		
		
		
		String sql = "SELECT * FROM payrollfund WHERE isactivef=1 ";
		 
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("payrollfundAll SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			PayrollFund fund = PayrollFund.builder()
					.id(rs.getLong("fid"))
					.name(rs.getString("fundname"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactivef"))
					.build();
			
			
			emps.put(fund.getId(), fund);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	public static List<PayrollFund> retrieve(String sql, String[] params){
		List<PayrollFund> emps = new ArrayList<PayrollFund>();
		
		
		
		String sqlTemp = "SELECT * FROM payrollfund WHERE isactivef=1 ";
		 
		
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
		System.out.println("payrollfund SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			PayrollFund fund = PayrollFund.builder()
					.id(rs.getLong("fid"))
					.name(rs.getString("fundname"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactivef"))
					.build();
			
			
			emps.add(fund);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	public static PayrollFund save(PayrollFund st){
		if(st!=null){
			
			long id = PayrollFund.getInfo(st.getId() ==0? PayrollFund.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = PayrollFund.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = PayrollFund.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = PayrollFund.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			PayrollFund.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			PayrollFund.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			PayrollFund.insertData(this, "3");
		}
		
	}
	
	public static PayrollFund insertData(PayrollFund st, String type){
		String sql = "INSERT INTO payrollfund ("
				+ "fid,"
				+ "fundname,"
				+ "amount,"
				+ "isactivef)" 
				+ " VALUES(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table payrollfund");
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
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getName());
		LogU.add(st.getAmount());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to payrollfund : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static PayrollFund updateData(PayrollFund st){
		String sql = "UPDATE payrollfund SET "
				+ "fid=?,"
				+ "fundname=?,"
				+ "amount=?" 
				+ " WHERE fid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table payrollfund");
		
		
		ps.setString(cnt++, st.getName());
		ps.setDouble(cnt++, st.getAmount());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getAmount());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to payrollfund : " + s.getMessage());
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
		sql="SELECT fid FROM payrollfund ORDER BY fid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("fid");
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
		ps = conn.prepareStatement("SELECT fid FROM payrollfund WHERE fid=?");
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
		String sql = "UPDATE payrollfund set isactivef=0 WHERE fid=?";
		
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
