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
public class Province {

	private int id;
	private String name;
	private int isActive;
	private Regional regional;
	
	public static String provinceSQL(String tablename,Province bg){
		String sql= " AND "+ tablename +".provisactive=" + bg.getIsActive();
		if(bg!=null){
			
			sql += " AND "+ tablename +".provid=" + bg.getId();
			
			if(bg.getName()!=null){
				
				sql += " AND "+ tablename +".provname like '%"+ bg.getName() +"%'";
				
			}
			
		}
		
		return sql;
	}	
	
	public static List<Province> retrieve(String sqlAdd, String[] params){
		List<Province> bars = new ArrayList<>();
		
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sql = "SELECT * FROM  province " + tableProv + ", regional " + tableReg +
				" WHERE " + tableProv + ".regid=" + tableReg +".regid ";
		
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
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			prov.setRegional(reg);
			
			bars.add(prov);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return bars;
	}
	
	public static Province retrieve(int id){
		
		Province prov = new Province();
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sql = "SELECT * FROM  province " + tableProv + ", regional " + tableReg +
				" WHERE " + tableProv + ".regid=" + tableReg +".regid AND prv.provisactive=1 AND prv.provid=" + id;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			prov.setRegional(reg);
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return prov;
	}
	
	
	public static Province save(Province prov){
		if(prov!=null){
			
			int id = Province.getInfo(prov.getId() ==0? Province.getLatestId()+1 : prov.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				prov = Province.insertData(prov, "1");
			}else if(id==2){
				LogU.add("update Data ");
				prov = Province.updateData(prov);
			}else if(id==3){
				LogU.add("added new Data ");
				prov = Province.insertData(prov, "3");
			}
			
		}
		return prov;
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
	
	public static Province insertData(Province prov, String type){
		String sql = "INSERT INTO province ("
				+ "provid,"
				+ "provname,"
				+ "provisactive,"
				+ "regid)" 
				+ "values(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table province");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			prov.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			prov.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, prov.getName());
		ps.setInt(cnt++, prov.getIsActive());
		ps.setInt(cnt++, prov.getRegional().getId());
		
		LogU.add(prov.getName());
		LogU.add(prov.getIsActive());
		LogU.add(prov.getRegional().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to province : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return prov;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO province ("
				+ "provid,"
				+ "provname,"
				+ "provisactive,"
				+ "regid)" 
				+ "values(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table province");
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
		ps.setInt(cnt++, getRegional().getId());
		
		LogU.add(getName());
		LogU.add(getIsActive());
		LogU.add(getRegional().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to province : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Province updateData(Province prov){
		String sql = "UPDATE province SET "
				+ "provname=?,"
				+ "regid=?" 
				+ " WHERE provid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table province");
		
		
		ps.setString(cnt++, prov.getName());
		ps.setInt(cnt++, prov.getRegional().getId());
		ps.setInt(cnt++, prov.getId());
		
		LogU.add(prov.getName());
		LogU.add(prov.getRegional().getId());
		LogU.add(prov.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to province : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return prov;
	}
	
	public void updateData(){
		String sql = "UPDATE province SET "
				+ "provname=?,"
				+ "regid=?" 
				+ " WHERE provid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table province");
		
		
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getRegional().getId());
		ps.setInt(cnt++, getId());
		
		LogU.add(getName());
		LogU.add(getRegional().getId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to province : " + s.getMessage());
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
		sql="SELECT provid FROM province  ORDER BY provid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("provid");
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
		ps = conn.prepareStatement("SELECT provid FROM province WHERE provid=?");
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
		String sql = "UPDATE province set provisactive=0 WHERE provid=?";
		
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
