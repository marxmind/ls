package com.italia.municipality.lakesebu.acc.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.EmployeeType;
import com.italia.municipality.lakesebu.utils.DateUtils;
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
 * @since 05/13/2022
 * @version 1.0
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class PayrollGroupSeries {

	private long id;
	private String dateTrans;
	private String series;
	private int status;
	private int isActive;
	private int type;
	
	public static String getLatestSeriesId(EmployeeType type) {
		String employeeNo = null;
		String payCode = "PAY|";
		int year = DateUtils.getCurrentYear();
		int mnt = DateUtils.getCurrentMonth();
		String month = (mnt<10? "0"+mnt : ""+mnt);
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT gseries FROM payrollgroup WHERE emtype=" + type.getId() + " AND isactiveg=1 ORDER BY gid DESC LIMIT 1");
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			employeeNo = rs.getString("gseries");
		}
		
		if(employeeNo==null) {
			employeeNo = payCode+type.getCode() + "-" + year +"-"+ month +"-00001";
		}else {
			
			String[] vals = employeeNo.split("-");
			int tmpYear = Integer.valueOf(vals[1]);
			//if(year.equalsIgnoreCase(vals[1])) {
			if(year == tmpYear) {	
				int number = Integer.valueOf(vals[3]);
				
				//if same year and month no movement on series
				if(month.equalsIgnoreCase(vals[2])) {
					
				}else {
					number += 1; //add 1
				}
				
				
				
				String newSeries = number + "";
				int len = newSeries.length();
				
				switch(len) {
					case 1 : employeeNo = payCode+type.getCode() + "-" + year +"-"+ month + "-0000" + newSeries;  break;
					case 2 : employeeNo = payCode+type.getCode() + "-" + year +"-"+ month + "-000" + newSeries; break;
					case 3 : employeeNo = payCode+type.getCode() + "-" + year +"-"+ month + "-00" + newSeries; break;
					case 4 : employeeNo = payCode+type.getCode() + "-" + year +"-"+ month + "-0" + newSeries; break;
					case 5 : employeeNo = payCode+type.getCode() + "-" + year +"-"+ month + "-" + newSeries; break;
				}
				
				
			}else {//not equal to current year
				employeeNo = payCode+type.getCode() + "-" + year +"-"+ month +"-00001";
			}
			
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return employeeNo;
	}
	
	public static PayrollGroupSeries retrieveSeries(String series){
		
		PayrollGroupSeries gp = null;
		String sql = "SELECT * FROM payrollgroup WHERE isactiveg=1 AND gseries='"+ series.trim() + "'";
		 
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("retrieveSeries SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			 gp = PayrollGroupSeries.builder()
					.id(rs.getLong("gid"))
					.dateTrans(rs.getString("gdate"))
					.series(rs.getString("gseries"))
					.isActive(rs.getInt("isactiveg"))
					.status(rs.getInt("gstatus"))
					.type(rs.getInt("emtype"))
					.build();
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return gp;
	}
	
	public static PayrollGroupSeries retrieveGroup(long groupId, int employeeType){
		
		PayrollGroupSeries gp = null;
		String sql = "SELECT * FROM payrollgroup WHERE isactiveg=1 AND gid="+ groupId + " AND emtype="+employeeType;
		 
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("retrieveGroup SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			 gp = PayrollGroupSeries.builder()
					.id(rs.getLong("gid"))
					.dateTrans(rs.getString("gdate"))
					.series(rs.getString("gseries"))
					.isActive(rs.getInt("isactiveg"))
					.status(rs.getInt("gstatus"))
					.type(rs.getInt("emtype"))
					.build();
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return gp;
	}
	
	public static List<PayrollGroupSeries> retrieve(String sql, String[] params){
		List<PayrollGroupSeries> emps = new ArrayList<PayrollGroupSeries>();
		
		
		String tableGp = "gp";
		
		String sqlTemp = "SELECT * FROM payrollgroup "+ tableGp +" WHERE " + tableGp + ".isactiveg=1 ";
		 
		
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
		System.out.println("payrollgroup SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			PayrollGroupSeries gp = PayrollGroupSeries.builder()
					.id(rs.getLong("gid"))
					.dateTrans(rs.getString("gdate"))
					.series(rs.getString("gseries"))
					.isActive(rs.getInt("isactiveg"))
					.status(rs.getInt("gstatus"))
					.type(rs.getInt("emtype"))
					.build();
			
			
			
			emps.add(gp);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	public static PayrollGroupSeries save(PayrollGroupSeries st){
		if(st!=null){
			
			long id = PayrollGroupSeries.getInfo(st.getId() ==0? PayrollGroupSeries.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = PayrollGroupSeries.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = PayrollGroupSeries.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = PayrollGroupSeries.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			PayrollGroupSeries.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			PayrollGroupSeries.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			PayrollGroupSeries.insertData(this, "3");
		}
		
	}
	
	public static PayrollGroupSeries insertData(PayrollGroupSeries st, String type){
		String sql = "INSERT INTO payrollgroup ("
				+ "gid,"
				+ "gdate,"
				+ "gseries,"
				+ "isactiveg,"
				+ "gstatus,"
				+ "emtype)" 
				+ " VALUES(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table payrollgroup");
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
		ps.setString(cnt++, st.getSeries());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getType());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getSeries());
		LogU.add(st.getIsActive());
		LogU.add(st.getStatus());
		LogU.add(st.getType());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to payrollgroup : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static PayrollGroupSeries updateData(PayrollGroupSeries st){
		String sql = "INSERT INTO payrollgroup ("
				+ "gdate=?,"
				+ "gseries=?,"
				+ "gstatus=?,"
				+ "emtype=?" 
				+ " WHERE gid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table payrollgroup");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getSeries());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getType());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getSeries());
		LogU.add(st.getStatus());
		LogU.add(st.getType());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to payrollgroup : " + s.getMessage());
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
		sql="SELECT gid FROM payrollgroup ORDER BY gid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("gid");
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
		ps = conn.prepareStatement("SELECT gid FROM payrollgroup WHERE gid=?");
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
		String sql = "UPDATE payrollgroup set isactiveg=0 WHERE gid=?";
		
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
