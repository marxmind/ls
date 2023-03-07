package com.italia.municipality.lakesebu.licensing.controller;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @since 10/14/2017
 * @version 1.0
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Purok {

	private long id;
	private String dateTrans;
	private String purokName;
	private int isActive;
	private Timestamp timestamp;
	
	private Barangay barangay;
	private Municipality municipality;
	
	public static String purokSQL(String tablename,Purok pur){
		String sql= " AND "+ tablename +".isactivepurok=" + pur.getIsActive();
		if(pur!=null){
			
			sql += " AND "+ tablename +".purid=" + pur.getId();
			
			if(pur.getPurokName()!=null){
				
				sql += " AND "+ tablename +".purokname like '%"+ pur.getPurokName() +"%'";
				
			}
			
		}
		
		return sql;
	}	
	
	public static List<Purok> retrieve(String sqlAdd, String[] params){
		List<Purok> purs = new ArrayList<Purok>();
		
		String tablePur = "pur";
		String tableBar = "bgy";
		String tableMun = "mun";
		
		String sql = "SELECT * FROM purok "+ tablePur +", barangay " + tableBar + ", municipality " + tableMun + 
				" WHERE " + 
				tablePur + ".bgid=" + tableBar +".bgid AND " +
				tablePur + ".munid=" + tableMun +".munid ";
						
		
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
		System.out.println("SQL : " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			try{pur.setTimestamp(rs.getTimestamp("timestamppur"));}catch(NullPointerException e){}
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			pur.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			pur.setMunicipality(mun);
			
			
			purs.add(pur);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return purs;
	}
	
	public static Purok retrieve(long id){
		Purok pur = new Purok();
		
		String tablePur = "pur";
		String tableBar = "bgy";
		String tableMun = "mun";
		
		String sql = "SELECT * FROM purok "+ tablePur +", barangay " + tableBar + ", municipality " + tableMun + 
				" WHERE " + 
				tablePur + ".bgid=" + tableBar +".bgid AND " +
				tablePur + ".munid=" + tableMun +".munid AND " + tablePur + ".isactivepurok=1 AND " + tablePur + ".purid=" + id;
						
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			try{pur.setTimestamp(rs.getTimestamp("timestamppur"));}catch(NullPointerException e){}
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			pur.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			pur.setMunicipality(mun);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return pur;
	}
	
	public static Purok save(Purok pur){
		if(pur!=null){
			
			long id = Purok.getInfo(pur.getId() ==0? Purok.getLatestId()+1 : pur.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				pur = Purok.insertData(pur, "1");
			}else if(id==2){
				LogU.add("update Data ");
				pur = Purok.updateData(pur);
			}else if(id==3){
				LogU.add("added new Data ");
				pur = Purok.insertData(pur, "3");
			}
			
		}
		return pur;
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
	
	public static Purok insertData(Purok pur, String type){
		String sql = "INSERT INTO purok ("
				+ "purid,"
				+ "purdate,"
				+ "purokname,"
				+ "isactivepurok,"
				+ "bgid,"
				+ "munid)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table purok");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			pur.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			pur.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, pur.getDateTrans());
		ps.setString(cnt++, pur.getPurokName());
		ps.setInt(cnt++, pur.getIsActive());
		ps.setInt(cnt++, pur.getBarangay().getId());
		ps.setInt(cnt++, pur.getMunicipality().getId());
		
		LogU.add(pur.getDateTrans());
		LogU.add(pur.getPurokName());
		LogU.add(pur.getIsActive());
		LogU.add(pur.getBarangay().getId());
		LogU.add(pur.getMunicipality().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to purok : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pur;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO purok ("
				+ "purid,"
				+ "purdate,"
				+ "purokname,"
				+ "isactivepurok,"
				+ "bgid,"
				+ "munid)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table purok");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getPurokName());
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getBarangay().getId());
		ps.setInt(cnt++, getMunicipality().getId());
		
		LogU.add(getDateTrans());
		LogU.add(getPurokName());
		LogU.add(getIsActive());
		LogU.add(getBarangay().getId());
		LogU.add(getMunicipality().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to purok : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Purok updateData(Purok pur){
		String sql = "UPDATE purok SET "
				+ "purdate=?,"
				+ "purokname=?,"
				+ "bgid=?,"
				+ "munid=?" 
				+ " WHERE purid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table purok");
		
		
		ps.setString(cnt++, pur.getDateTrans());
		ps.setString(cnt++, pur.getPurokName());
		ps.setInt(cnt++, pur.getBarangay().getId());
		ps.setInt(cnt++, pur.getMunicipality().getId());
		ps.setLong(cnt++, pur.getId());
		
		LogU.add(pur.getDateTrans());
		LogU.add(pur.getPurokName());
		LogU.add(pur.getBarangay().getId());
		LogU.add(pur.getMunicipality().getId());
		LogU.add(pur.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to purok : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pur;
	}
	
	public void updateData(){
		String sql = "UPDATE purok SET "
				+ "purdate=?,"
				+ "purokname=?,"
				+ "bgid=?,"
				+ "munid=?" 
				+ " WHERE purid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table purok");
		
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getPurokName());
		ps.setInt(cnt++, getBarangay().getId());
		ps.setInt(cnt++, getMunicipality().getId());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateTrans());
		LogU.add(getPurokName());
		LogU.add(getBarangay().getId());
		LogU.add(getMunicipality().getId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to purok : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT purid FROM purok  ORDER BY purid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("purid");
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
		ps = conn.prepareStatement("SELECT purid FROM purok WHERE purid=?");
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
		String sql = "UPDATE purok set isactivepurok=0 WHERE purid=?";
		
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