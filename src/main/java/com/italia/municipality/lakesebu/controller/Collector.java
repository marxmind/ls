package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @since 08-11-2018
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Collector {

	private int id;
	private String name;
	private int isActive;
	private int isResigned;
	private Department department;
	
	public static List<Collector> retrieve(String sqlAdd, String[] params){
		List<Collector> cols = new ArrayList<Collector>();
		
		String tableCol="cl";
		String tableDep="dp";
		String sql = "SELECT * FROM issuedcollector "+tableCol + ", department " + tableDep +"  WHERE "+tableCol+".isactivecollector=1 "
				+ "AND " + tableCol + ".departmentid=" + tableDep +".departmentid ";
		sql = sql + sqlAdd;		
				
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
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			
			Department dep = new Department();
			try{dep.setDepid(rs.getInt("departmentid"));}catch(NullPointerException e){}
			try{dep.setDepartmentName(rs.getString("departmentname"));}catch(NullPointerException e){}
			try{dep.setCode(rs.getString("code"));}catch(NullPointerException e){}
			col.setDepartment(dep);
			
			cols.add(col);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return cols;
	}
	
	public static Collector retrieve(int id){
	
		Collector col = new Collector();
		String tableCol="cl";
		String tableDep="dp";
		String sql = "SELECT * FROM issuedcollector "+tableCol + ", department " + tableDep +"  WHERE "+tableCol+".isactivecollector=1 "
				+ "AND " + tableCol + ".departmentid=" + tableDep +".departmentid AND " + tableCol + ".isid="+id;
			
				
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			
			Department dep = new Department();
			try{dep.setDepid(rs.getInt("departmentid"));}catch(NullPointerException e){}
			try{dep.setDepartmentName(rs.getString("departmentname"));}catch(NullPointerException e){}
			try{dep.setCode(rs.getString("code"));}catch(NullPointerException e){}
			col.setDepartment(dep);
			
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return col;
	}
	
	public static Collector save(Collector col){
		if(col!=null){
			
			int id = Collector.getInfo(col.getId() ==0? Collector.getLatestId()+1 : col.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				col = Collector.insertData(col, "1");
			}else if(id==2){
				LogU.add("update Data ");
				col = Collector.updateData(col);
			}else if(id==3){
				LogU.add("added new Data ");
				col = Collector.insertData(col, "3");
			}
			
		}
		return col;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			insertData("1");
		}else if(id==2){
			LogU.add("update Data ");
			updateData();
		}else if(id==3){
			LogU.add("added new Data ");
			insertData("3");
		}
		
 }
	
	public static Collector insertData(Collector col, String type){
		String sql = "INSERT INTO issuedcollector ("
				+ "isid,"
				+ "collectorname,"
				+ "departmentid,"
				+ "isactivecollector,"
				+ "isresigned)" 
				+ "values(?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table issuedcollector");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			col.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			col.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, col.getName());
		ps.setInt(cnt++, col.getDepartment().getDepid());
		ps.setInt(cnt++, col.getIsActive());
		ps.setInt(cnt++, col.getIsResigned());
		
		LogU.add(col.getName());
		LogU.add(col.getDepartment().getDepid());
		LogU.add(col.getIsActive());
		LogU.add(col.getIsResigned());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to issuedcollector : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return col;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO issuedcollector ("
				+ "isid,"
				+ "collectorname,"
				+ "departmentid,"
				+ "isactivecollector,"
				+ "isresigned)" 
				+ "values(?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table issuedcollector");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getDepartment().getDepid());
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getIsResigned());
		
		LogU.add(getName());
		LogU.add(getDepartment().getDepid());
		LogU.add(getIsActive());
		LogU.add(getIsResigned());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to issuedcollector : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	
	}
	
	public static Collector updateData(Collector col){
		String sql = "UPDATE issuedcollector SET "
				+ "collectorname=?,"
				+ "departmentid=?,"
				+ "isresigned=? " 
				+ " WHERE isid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("udating data into table issuedcollector");
		
		
		ps.setString(cnt++, col.getName());
		ps.setInt(cnt++, col.getDepartment().getDepid());
		ps.setInt(cnt++, col.getIsResigned());
		ps.setInt(cnt++, col.getId());
		
		LogU.add(col.getName());
		LogU.add(col.getDepartment().getDepid());
		LogU.add(col.getIsResigned());
		LogU.add(col.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to issuedcollector : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return col;
	}
	
	public void updateData(){
		String sql = "UPDATE issuedcollector SET "
				+ "collectorname=?,"
				+ "departmentid=?,"
				+ "isresigned=? " 
				+ " WHERE isid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("udating data into table issuedcollector");
		
		
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getDepartment().getDepid());
		ps.setInt(cnt++, getIsResigned());
		ps.setInt(cnt++, getId());
		
		LogU.add(getName());
		LogU.add(getDepartment().getDepid());
		LogU.add(getIsResigned());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to issuedcollector : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	
	}
	
	public static int getLatestId(){
		int id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT isid FROM issuedcollector  ORDER BY isid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("isid");
		}
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static int getInfo(int id){
		boolean isNotNull=false;
		int result =0;
		//id no data retrieve.
		//application will insert a default no which 1 for the first data
		int val = getLatestId();	
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
	public static boolean isIdNoExist(int id){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT isid FROM issuedcollector WHERE isid=?");
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
		String sql = "UPDATE issuedcollector set isactivecollector=0 WHERE isid=?";
		
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
