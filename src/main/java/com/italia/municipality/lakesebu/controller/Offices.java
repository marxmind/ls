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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 12/12/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Offices {

	private long id;
	private String code;
	private String name;
	private String abr;
	private String headOfOffice;
	private int departmentId;
	private int isActive;
	
	public static Map<Long, Offices> retrieveAll(){
		Map<Long, Offices> offices = new LinkedHashMap<Long, Offices>();
		
		String sql = "SELECT * FROM offices WHERE isactiveoff=1 ORDER BY name";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("offices SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Offices off = Offices.builder()
					.id(rs.getInt("offid"))
					.name(rs.getString("name"))
					.code(rs.getString("code"))
					.headOfOffice(rs.getString("headname"))
					.abr(rs.getString("abrname"))
					.isActive(rs.getInt("isactiveoff"))
					.departmentId(rs.getInt("departmentid"))
					.build();
			
			offices.put(off.getId(), off);

		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return offices;
	}
	
	public static List<Offices> retrieve(String sql, String[] params){
		List<Offices> offcs = new ArrayList<Offices>();
		
		String sqlTemp = "SELECT * FROM offices WHERE isactiveoff=1 ";
		
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
		System.out.println("offices SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Offices off = Offices.builder()
					.id(rs.getInt("offid"))
					.name(rs.getString("name"))
					.code(rs.getString("code"))
					.headOfOffice(rs.getString("headname"))
					.abr(rs.getString("abrname"))
					.isActive(rs.getInt("isactiveoff"))
					.departmentId(rs.getInt("departmentid"))
					.build();
			
			offcs.add(off);

		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return offcs;
	}
	
	public static Offices save(Offices st){
		if(st!=null){
			
			long id = Offices.getInfo(st.getId() ==0? Offices.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Offices.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Offices.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Offices.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Offices.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Offices.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Offices.insertData(this, "3");
		}
		
	}
	
	public static Offices insertData(Offices st, String type){
		String sql = "INSERT INTO offices ("
				+ "offid,"
				+ "name,"
				+ "code,"
				+ "headname,"
				+ "abrname,"
				+ "isactiveoff,"
				+ "departmentid)" 
				+ " VALUES(?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table offices");
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
		ps.setString(cnt++, st.getCode());
		ps.setString(cnt++, st.getHeadOfOffice());
		ps.setString(cnt++, st.getAbr());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getDepartmentId());
		
		LogU.add(st.getName());
		LogU.add(st.getCode());
		LogU.add(st.getHeadOfOffice());
		LogU.add(st.getAbr());
		LogU.add(st.getIsActive());
		LogU.add(st.getDepartmentId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to offices : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static Offices updateData(Offices st){
		String sql = "UPDATE offices SET "
				+ "name=?,"
				+ "code=?,"
				+ "headname=?,"
				+ "abrname=?,"
				+ "departmentid=?" 
				+ " WHERE offid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table offices");
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getCode());
		ps.setString(cnt++, st.getHeadOfOffice());
		ps.setString(cnt++, st.getAbr());
		ps.setInt(cnt++, st.getDepartmentId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getCode());
		LogU.add(st.getHeadOfOffice());
		LogU.add(st.getAbr());
		LogU.add(st.getDepartmentId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to offices : " + s.getMessage());
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
		sql="SELECT offid FROM offices  ORDER BY offid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("offid");
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
		ps = conn.prepareStatement("SELECT offid FROM offices WHERE offid=?");
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
		String sql = "UPDATE offices set isactiveoff=0 WHERE offid=?";
		
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