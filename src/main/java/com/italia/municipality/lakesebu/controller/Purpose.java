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
 * @author mark italia
 * @version 1.0
 * @since 07/05/2017
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Purpose {

	private int id;
	private String name;
	private int isActivePurpose;
	
	public static List<Purpose> retrieve(String sql, String[] params){
		List<Purpose> purs = new ArrayList<>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		System.out.println("SQL " + ps.toString());
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Purpose pur = new Purpose();
			try{pur.setId(rs.getInt("purposeid"));}catch(NullPointerException e){}
			try{pur.setName(rs.getString("purname"));}catch(NullPointerException e){}
			try{pur.setIsActivePurpose(rs.getInt("isactivepurpose"));}catch(NullPointerException e){}
			purs.add(pur);

		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return purs;
	}
	
	public static Purpose save(Purpose pur){
		if(pur!=null){
			
			long id = Position.getInfo(pur.getId() ==0? Purpose.getLatestId()+1 : pur.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				pur = Purpose.insertData(pur, "1");
			}else if(id==2){
				LogU.add("update Data ");
				pur = Purpose.updateData(pur);
			}else if(id==3){
				LogU.add("added new Data ");
				pur = Purpose.insertData(pur, "3");
			}
			
		}
		return pur;
	}
	
	public void save(){
			
			int id = getInfo(getId() ==0? getLatestId()+1 : getId());
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
	
	public static Purpose insertData(Purpose pur, String type){
		String sql = "INSERT INTO purpose ("
				+ "purposeid,"
				+ "purname,"
				+ "isactivepurpose)" 
				+ "values(?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table purpose");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			pur.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			pur.setId(id);
			LogU.add("id: " + id);
		}
		ps.setString(cnt++, pur.getName());
		ps.setInt(cnt++, pur.getIsActivePurpose());
		
		LogU.add(pur.getName());
		LogU.add(pur.getIsActivePurpose());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to purpose : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pur;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO purpose ("
				+ "purposeid,"
				+ "purname,"
				+ "isactivepurpose)" 
				+ "values(?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table purpose");
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
		ps.setInt(cnt++, getIsActivePurpose());
		
		LogU.add(getName());
		LogU.add(getIsActivePurpose());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to purpose : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Purpose updateData(Purpose pur){
		String sql = "UPDATE purpose SET "
				+ "purname=?" 
				+ " WHERE posid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table purpose");
		
		ps.setString(cnt++, pur.getName());
		ps.setInt(cnt++, pur.getId());
		
		LogU.add(pur.getName());
		LogU.add(pur.getId());
		
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to purpose : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pur;
	}
	
	public void updateData(){
		String sql = "UPDATE purpose SET "
				+ "purname=?" 
				+ " WHERE posid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table purpose");
		
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getId());
		
		LogU.add(getName());
		LogU.add(getId());
		
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to purpose : " + s.getMessage());
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
		sql="SELECT purposeid FROM purpose  ORDER BY purposeid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("purposeid");
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
	public static boolean isIdNoExist(long id){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT purposeid FROM purpose WHERE purposeid=?");
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
		String sql = "UPDATE purpose set isactivepurpose=0 WHERE purposeid=?";
		
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
