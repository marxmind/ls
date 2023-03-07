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
public class Barangay {

	private int id;
	private String name;
	private int isActive;
	
	private Province province;
	private Municipality municipality;
	private Regional regional;
	
	public static String barangaySQL(String tablename,Barangay bg){
		String sql= " AND "+ tablename +".bgisactive=" + bg.getIsActive();
		if(bg!=null){
			
			sql += " AND "+ tablename +".bgid=" + bg.getId();
			
			if(bg.getName()!=null){
				
				sql += " AND "+ tablename +".bgname like '%"+ bg.getName() +"%'";
				
			}
			
		}
		
		return sql;
	}	
	
	public static List<String> retrieve(String param, String fieldName, String limit){
		
		String sql = "SELECT DISTINCT " + fieldName + " FROM barangay WHERE " + fieldName +" like '" + param + "%' " + limit;
		List<String> result = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			result.add(rs.getString(fieldName));
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return result;
	}
	
	public static List<Barangay> retrieve(String sqlAdd, String[] params){
		List<Barangay> bars = new ArrayList<Barangay>();
		
		String tableBar = "bgy";
		String tableMun = "mun";
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sql = "SELECT * FROM barangay " + tableBar + ", municipality " + tableMun + ", province " + tableProv + ", regional " + tableReg +
				" WHERE " + tableBar + ".munid=" + tableMun +".munid AND "
						+ tableBar + ".provid=" + tableProv+".provid AND "
						+ tableBar + ".regid=" + tableReg +".regid ";
		
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
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			bar.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			bar.setProvince(prov);
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			bar.setRegional(reg);
			
			bars.add(bar);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return bars;
	}
	
	public static Barangay retrieve(int id){
		Barangay bar = new Barangay();
		
		String tableBar = "bgy";
		String tableMun = "mun";
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sql = "SELECT * FROM barangay " + tableBar + ", municipality " + tableMun + ", province " + tableProv + ", regional " + tableReg +
				" WHERE " + tableBar + ".munid=" + tableMun +".munid AND "
						+ tableBar + ".provid=" + tableProv+".provid AND "
						+ tableBar + ".regid=" + tableReg +".regid AND bgy.bgisactive=1 AND bgy.bgid="+id;
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			bar.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			bar.setProvince(prov);
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			bar.setRegional(reg);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return bar;
	}
	
	public static Barangay save(Barangay bar){
		if(bar!=null){
			
			int id = Barangay.getInfo(bar.getId() ==0? Barangay.getLatestId()+1 : bar.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				bar = Barangay.insertData(bar, "1");
			}else if(id==2){
				LogU.add("update Data ");
				bar = Barangay.updateData(bar);
			}else if(id==3){
				LogU.add("added new Data ");
				bar = Barangay.insertData(bar, "3");
			}
			
		}
		return bar;
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
	
	public static Barangay insertData(Barangay bar, String type){
		String sql = "INSERT INTO barangay ("
				+ "bgid,"
				+ "bgname,"
				+ "bgisactive,"
				+ "munid,"
				+ "provid,"
				+ "regid)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table barangay");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			bar.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			bar.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, bar.getName());
		ps.setInt(cnt++, bar.getIsActive());
		ps.setInt(cnt++, bar.getMunicipality().getId());
		ps.setInt(cnt++, bar.getProvince().getId());
		ps.setInt(cnt++, bar.getRegional().getId());
		
		LogU.add(bar.getName());
		LogU.add(bar.getIsActive());
		LogU.add(bar.getMunicipality().getId());
		LogU.add(bar.getProvince().getId());
		LogU.add(bar.getRegional().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to barangay : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return bar;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO barangay ("
				+ "bgid,"
				+ "bgname,"
				+ "bgisactive,"
				+ "munid,"
				+ "provid,"
				+ "regid)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table barangay");
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
		ps.setInt(cnt++, getMunicipality().getId());
		ps.setInt(cnt++, getProvince().getId());
		ps.setInt(cnt++, getRegional().getId());
		
		LogU.add(getName());
		LogU.add(getIsActive());
		LogU.add(getMunicipality().getId());
		LogU.add(getProvince().getId());
		LogU.add(getRegional().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to barangay : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Barangay updateData(Barangay bar){
		String sql = "UPDATE barangay SET "
				+ "bgname=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "regid=?" 
				+ " WHERE bgid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table barangay");
		
		
		ps.setString(cnt++, bar.getName());
		ps.setInt(cnt++, bar.getMunicipality().getId());
		ps.setInt(cnt++, bar.getProvince().getId());
		ps.setInt(cnt++, bar.getRegional().getId());
		ps.setInt(cnt++, bar.getId());
		
		LogU.add(bar.getName());
		LogU.add(bar.getMunicipality().getId());
		LogU.add(bar.getProvince().getId());
		LogU.add(bar.getRegional().getId());
		LogU.add(bar.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to barangay : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return bar;
	}
	public void updateData(){
		String sql = "UPDATE barangay SET "
				+ "bgname=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "regid=?" 
				+ " WHERE bgid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table barangay");
		

		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getMunicipality().getId());
		ps.setInt(cnt++, getProvince().getId());
		ps.setInt(cnt++, getRegional().getId());
		ps.setInt(cnt++, getId());
		
		LogU.add(getName());
		LogU.add(getMunicipality().getId());
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
			LogU.add("error updating data to barangay : " + s.getMessage());
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
		sql="SELECT bgid FROM barangay  ORDER BY bgid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("bgid");
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
		ps = conn.prepareStatement("SELECT bgid FROM barangay WHERE bgid=?");
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
		String sql = "UPDATE barangay set bgisactive=0 WHERE bgid=?";
		
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