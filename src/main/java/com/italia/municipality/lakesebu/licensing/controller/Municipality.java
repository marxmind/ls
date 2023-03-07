package com.italia.municipality.lakesebu.licensing.controller;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @since 04/09/2017
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Municipality {

	private int id;
	private String name;
	private int isActive;
	
	private Province province;
	private Regional regional;
	
	public static String municipalitySQL(String tablename,Municipality bg){
		String sql= " AND "+ tablename +".munisactive=" + bg.getIsActive();
		if(bg!=null){
			
			sql += " AND "+ tablename +".munid=" + bg.getId();
			
			if(bg.getName()!=null){
				
				sql += " AND "+ tablename +".munname like '%"+ bg.getName() +"%'";
				
			}
			
		}
		
		return sql;
	}	
	
	public static List<Municipality> retrieve(String sqlAdd, String[] params){
		List<Municipality> muns = new ArrayList<Municipality>();
		
		String tableMun = "mun";
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sql = "SELECT * FROM  municipality " + tableMun + ", province " + tableProv + ", regional " + tableReg +
				" WHERE " + tableMun + ".provid=" + tableProv+".provid AND "
						+ tableMun + ".regid=" + tableReg +".regid ";
		
		sql += sqlAdd;
		
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
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			mun.setProvince(prov);
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			mun.setRegional(reg);
			
			muns.add(mun);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return muns;
	}
	
	public static Municipality retrieve(int id){
		
		Municipality mun = new Municipality();
		
		String tableMun = "mun";
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sql = "SELECT * FROM  municipality " + tableMun + ", province " + tableProv + ", regional " + tableReg +
				" WHERE " + tableMun + ".provid=" + tableProv+".provid AND "
						+ tableMun + ".regid=" + tableReg +".regid AND mun.munisactive=1 AND mun.munid="+id;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			mun.setProvince(prov);
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			mun.setRegional(reg);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return mun;
	}
	
	public static Municipality save(Municipality mun){
		if(mun!=null){
			
			int id = Municipality.getInfo(mun.getId() ==0? Municipality.getLatestId()+1 : mun.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				mun = Municipality.insertData(mun, "1");
			}else if(id==2){
				LogU.add("update Data ");
				mun = Municipality.updateData(mun);
			}else if(id==3){
				LogU.add("added new Data ");
				mun = Municipality.insertData(mun, "3");
			}
			
		}
		return mun;
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
	
	public static Municipality insertData(Municipality mun, String type){
		String sql = "INSERT INTO municipality ("
				+ "munid,"
				+ "munname,"
				+ " munisactive,"
				+ "provid,"
				+ "regid)" 
				+ "values(?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table municipality");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			mun.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			mun.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, mun.getName());
		ps.setInt(cnt++, mun.getIsActive());
		ps.setInt(cnt++, mun.getProvince().getId());
		ps.setInt(cnt++, mun.getRegional().getId());
		
		LogU.add(mun.getName());
		LogU.add(mun.getIsActive());
		LogU.add(mun.getProvince().getId());
		LogU.add(mun.getRegional().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to municipality : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return mun;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO municipality ("
				+ "munid,"
				+ "munname,"
				+ " munisactive,"
				+ "provid,"
				+ "regid)" 
				+ "values(?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table municipality");
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
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getProvince().getId());
		ps.setInt(cnt++, getRegional().getId());
		
		LogU.add(getName());
		LogU.add(getIsActive());
		LogU.add(getProvince().getId());
		LogU.add(getRegional().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to municipality : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Municipality updateData(Municipality mun){
		String sql = "UPDATE municipality SET "
				+ "munname=?,"
				+ "provid=?,"
				+ "regid=?" 
				+ " WHERE munid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table municipality");
		
		
		ps.setString(cnt++, mun.getName());
		ps.setInt(cnt++, mun.getProvince().getId());
		ps.setInt(cnt++, mun.getRegional().getId());
		ps.setInt(cnt++, mun.getId());
		
		LogU.add(mun.getName());
		LogU.add(mun.getProvince().getId());
		LogU.add(mun.getRegional().getId());
		LogU.add(mun.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to municipality : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return mun;
	}
	
	public void updateData(){
		String sql = "UPDATE municipality SET "
				+ "munname=?,"
				+ "provid=?,"
				+ "regid=?" 
				+ " WHERE munid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table municipality");
		
		
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getProvince().getId());
		ps.setInt(cnt++, getRegional().getId());
		ps.setInt(cnt++, getId());
		
		LogU.add(getName());
		LogU.add(getProvince().getId());
		LogU.add(getRegional().getId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to municipality : " + s.getMessage());
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
		sql="SELECT munid FROM municipality  ORDER BY munid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("munid");
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
	public static boolean isIdNoExist(int id){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT munid FROM municipality WHERE munid=?");
		ps.setInt(1, id);
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
		String sql = "UPDATE municipality set munisactive=0 WHERE munid=?";
		
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
