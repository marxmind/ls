package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 * @author mark italia
 * @since 09/27/2016
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
/*
 * 
 * Please use offices for the future
 *
 */
public class Department {

	private int depid;
	private String departmentName;
	private String code;
	private UserDtls userDtls;
	private Company company;
	private Timestamp timestamp;
	private int isActive;
	private String departmentHead;
	private String abrevation;
	
	public static String departmentSQL(String tablename,Department dep){
		String sql="";
		if(dep!=null){
			
			if(dep.getDepid()!=0){
				sql += " AND "+ tablename +".departmentid=" + dep.getDepid();
			}
			if(dep.getDepartmentName()!=null){
				sql += " AND "+ tablename +".departmentname='" + dep.getDepartmentName()+"'";
			}
			if(dep.getCode()!=null){
				sql += " AND "+ tablename +".code='" + dep.getCode()+"'";
			}
		}
		
		return sql;
	}
	
	public static List<Department> retrieve(String sql, String[] params){
		List<Department> deps = new ArrayList<Department>();
		
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
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Department dp = Department.builder()
					.depid(rs.getInt("departmentid"))
					.departmentName(rs.getString("departmentname"))
					.code(rs.getString("code"))
					.departmentHead(rs.getString("dephead"))
					.abrevation(rs.getString("abr"))
					.build();
			
			deps.add(dp);
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return deps;
	}
	
	public static Department department(String departmentid){
		Department dep = new Department();
		String sql = "SELECT * FROM department WHERE departmentid=?";
		String[] params = new String[1];
		params[0] = departmentid;
		try{
			dep = Department.retrieve(sql, params).get(0);
		}catch(Exception e){}
		return dep;
	}
	
	public static Department save(Department dep){
		if(dep!=null){
			
			long id = Department.getInfo(dep.getDepid() ==0? Department.getLatestId()+1 : dep.getDepid());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				dep = Department.insertData(dep, "1");
			}else if(id==2){
				LogU.add("update Data ");
				dep = Department.updateData(dep);
			}else if(id==3){
				LogU.add("added new Data ");
				dep = Department.insertData(dep, "3");
			}
			
		}
		return dep;
	}
	
	public void save(){
		
		int id = getInfo(getDepid()==0? getLatestId()+1 : getDepid());
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
	
	public static Department insertData(Department dep, String type){
		String sql = "INSERT INTO department ("
				+ "departmentid,"
				+ "departmentname,"
				+ "code,"
				+ "isactivedep,"
				+ "dephead,"
				+ "abr)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table department");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			dep.setDepid(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			dep.setDepid(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, dep.getDepartmentName());
		ps.setString(cnt++, dep.getCode());
		ps.setInt(cnt++, dep.getIsActive());
		ps.setString(cnt++, dep.getDepartmentHead());
		ps.setString(cnt++, dep.getAbrevation());
		
		LogU.add(dep.getDepartmentName());
		LogU.add(dep.getCode());
		LogU.add(dep.getIsActive());
		LogU.add(dep.getDepartmentHead());
		LogU.add(dep.getAbrevation());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to department : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return dep;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO department ("
				+ "departmentid,"
				+ "departmentname,"
				+ "code,"
				+ "isactivedep,"
				+ "dephead,"
				+ "abr)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table department");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			setDepid(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			setDepid(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, getDepartmentName());
		ps.setString(cnt++, getCode());
		ps.setInt(cnt++, getIsActive());
		ps.setString(cnt++, getDepartmentHead());
		ps.setString(cnt++, getAbrevation());
		
		LogU.add(getDepartmentName());
		LogU.add(getCode());
		LogU.add(getIsActive());
		LogU.add(getDepartmentHead());
		LogU.add(getAbrevation());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to department : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Department updateData(Department dep){
		String sql = "UPDATE department SET "
				+ " departmentname=?,"
				+ " code=?,"
				+ "dephead=?,"
				+ "abr=? " 
				+ " WHERE departmentid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table department");
		
		ps.setString(cnt++, dep.getDepartmentName());
		ps.setString(cnt++, dep.getCode());
		ps.setString(cnt++, dep.getDepartmentHead());
		ps.setString(cnt++, dep.getAbrevation());
		ps.setInt(cnt++, dep.getDepid());
		
		LogU.add(dep.getDepartmentName());
		LogU.add(dep.getCode());
		LogU.add(dep.getDepartmentHead());
		LogU.add(dep.getAbrevation());
		LogU.add(dep.getDepid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to department : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return dep;
	}
	
	public void updateData(){
		String sql = "UPDATE department SET "
				+ " departmentname=?,"
				+ " code=?,"
				+ "dephead=?,"
				+ "abr=? " 
				+ " WHERE departmentid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table department");
		
		ps.setString(cnt++, getDepartmentName());
		ps.setString(cnt++, getCode());
		ps.setString(cnt++, getDepartmentHead());
		ps.setString(cnt++, getAbrevation());
		ps.setInt(cnt++, getDepid());
		
		LogU.add(getDepartmentName());
		LogU.add(getCode());
		LogU.add(getDepartmentHead());
		LogU.add(getAbrevation());
		LogU.add(getDepid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to department : " + s.getMessage());
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
		sql="SELECT departmentid FROM department  ORDER BY departmentid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("departmentid");
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
		ps = conn.prepareStatement("SELECT departmentid FROM department WHERE departmentid=?");
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
		String sql = "UPDATE department set isactivedep=0 WHERE departmentid=?";
		
		String[] params = new String[1];
		params[0] = getDepid()+"";
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
