package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.DateUtils;
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
 * @since 1/13/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class GenCollection {

	private long id;
	private String dateTrans;
	private String monthYear;
	private int isActive;
	
	public static String getMonthYearLatest() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String val = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT monthyear FROM gencollection WHERE genisactive=1 ORDER BY genid DESC LIMIT 1");
		
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		while(rs.next()){
			val = rs.getString("monthyear");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		int month = DateUtils.getCurrentMonth();
		int year = DateUtils.getCurrentYear();
		if(val==null) {
			val = month>9? month+"" : "0"+month;
			val += "-" + year;
		}else {
			String[] vals = val.split("-");
			String monthVal = vals[0];
			String yearVal = vals[1];
			if(!yearVal.equalsIgnoreCase(year+"") && !monthVal.equalsIgnoreCase(month+"")) {
				val = month>9? month+"" : "0"+month;
				val += "-" + year;
			}
		}
		
		
		}catch(Exception e){e.getMessage();}
		
		return val;
	}
	
	public static GenCollection retrieveSelected(String monthYear) {
		GenCollection tax = new GenCollection();
		String tableTax ="st";
		String sql = "SELECT * FROM gencollection "+ tableTax + "  WHERE "+tableTax+".genisactive=1 AND datetrans='"+ monthYear +"'";  
				
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		while(rs.next()){
			
			 tax = GenCollection.builder()
					.id(rs.getLong("genid"))
					.dateTrans(rs.getString("datetrans"))
					.monthYear(rs.getString("monthyear"))
					.isActive(rs.getInt("genisactive"))
					.build();
			
			
		}
		
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		
		}catch(Exception e){e.getMessage();}
		
		
		return tax;
	}
	
	public static GenCollection saveGen(GenCollection genCol) {
		List<GenCollection> gen = GenCollection.retrieve(" AND monthyear='"+ genCol.getMonthYear() +"'", new String[0]);
		if(gen!=null && gen.size()>0) {
			//save(gen.get(0)); saving is not allowed
		}else {
			genCol = GenCollection.save(genCol);
		}
		return genCol;
	}
	
	public static List<GenCollection> retrieve(String sqlAdd, String[] params){
		List<GenCollection> taxes = new ArrayList<GenCollection>();
		
		String tableTax ="st";
		String sql = "SELECT * FROM gencollection "+ tableTax + "  WHERE "+tableTax+".genisactive=1 ";  
		
				
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
			
			GenCollection tax = GenCollection.builder()
					.id(rs.getLong("genid"))
					.dateTrans(rs.getString("datetrans"))
					.monthYear(rs.getString("monthyear"))
					.isActive(rs.getInt("genisactive"))
					.build();
			
			taxes.add(tax);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return taxes;
	}
	
	private static GenCollection save(GenCollection st){
		if(st!=null){
			
			long id = GenCollection.getInfo(st.getId() ==0? GenCollection.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = GenCollection.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = GenCollection.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = GenCollection.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	private void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			GenCollection.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			GenCollection.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			GenCollection.insertData(this, "3");
		}
		
	}
	
	public static GenCollection insertData(GenCollection st, String type){
		String sql = "INSERT INTO gencollection ("
				+ "genid,"
				+ "datetrans,"
				+ "monthyear,"
				+ "genisactive)" 
				+ " VALUES(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table gencollection");
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
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getMonthYear());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getMonthYear());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to gencollection : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static GenCollection updateData(GenCollection st){
		String sql = "UPDATE gencollection SET "
				+ "datetrans=?,"
				+ "monthyear=?"
				+ " WHERE genid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table gencollection");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getMonthYear());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getMonthYear());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to gencollection : " + s.getMessage());
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
		sql="SELECT genid FROM gencollection  ORDER BY genid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("genid");
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
		ps = conn.prepareStatement("SELECT genid FROM gencollection WHERE genid=?");
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
		String sql = "UPDATE gencollection set genisactive=0 WHERE genid=?";
		
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