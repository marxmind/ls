package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.ClassType;
import com.italia.municipality.lakesebu.enm.TenantStatus;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @since 05/27/2025
 * @version 1.0
 * @author Mark Italia
 * 
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class TenantContract {

	private long id;
	private String accountNo;
	private String date;
	private String contractStart;
	private String contractEnd;
	private double amount;
	private String locationNumber;
	private int classType;
	private int status;
	private int isActive;
	
	private double arrears;
	private int monthStarted;
	private int yearStarted;
	private double penaltyRate;
	
	private Tenant tenant;
	private String classTypeName;
	private String statusName;
	private Date dateTrans;
	private Date dateStart;
	private Date dateEnd;
	
	public static boolean hasExistingContract(long tenantId) {
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement("SELECT tid FROM tenantcontract WHERE isactivec=1 AND tid=" + tenantId);
			rs = ps.executeQuery();
			
			while(rs.next()){
				return true;
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
	
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static String getNewAccountNumber() {
		String series = null;
		String date = DateUtils.getCurrentDateYYYYMMDD();
		String yearNow = date.split("-")[0];
		String yearLast = "";
		int number = 0;
		int size = 0;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT accountno FROM tenantcontract ORDER BY cid DESC LIMIT 1");
		rs = ps.executeQuery();
		
		while(rs.next()){
			String[] val = rs.getString("accountno").split("-");
 			yearLast = val[0];
 			number = Integer.valueOf(val[1]);
		}
		
		number += 1;
		String len = number + "";
		size = len.length();
		
		if(yearNow.equalsIgnoreCase(yearLast)) {
			switch(size) {
			case 1 : series = yearLast + "-000000000" + number; break;
			case 2 : series = yearLast + "-00000000" + number; break;
			case 3 : series = yearLast + "-0000000" + number; break;
			case 4 : series = yearLast + "-000000" + number; break;
			case 5 : series = yearLast + "-00000" + number; break;
			case 6 : series = yearLast + "-0000" + number; break;
			case 7 : series = yearLast + "-000" + number; break;
			case 8 : series = yearLast + "-00" + number; break;
			case 9 : series = yearLast + "-0" + number; break;
			case 10 : series = yearLast + "-" + number; break;
			}
			
			
		}else {
			series = null; //reset series
		}
		
		
		if(series==null) {
			series = yearNow + "-0000000001";
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		System.out.println("account contract: " + series);
		return series;
	} 
	
	public static List<TenantContract> retrieve(String sql, String[] params){
		List<TenantContract> tns = new ArrayList<TenantContract>();
		
		String tblTenant = "ten";
		String tblCon = "con";
		String sqlTemp = "SELECT * FROM tenantcontract "+ tblCon +", tenant "+ tblTenant +" WHERE "+ tblCon +".isactivec=1 AND "
				+ tblCon + ".tid=" + tblTenant + ".tid ";
		
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
		System.out.println("tenantcontract SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Tenant tn = Tenant.builder()
					.id(rs.getLong("tid"))
					.tenantNo(rs.getString("tenantno"))
					.date(rs.getString("tdate"))
					.fullName(rs.getString("fullnamet"))
					.contactNumber(rs.getString("contractnot"))
					.address(rs.getString("addresst"))
					.isActive(rs.getInt("isactivet"))
					.build();
			
			TenantContract con = builder()
					.id(rs.getLong("cid"))
					.accountNo(rs.getString("accountno"))
					.date(rs.getString("cdate"))
					.contractStart(rs.getString("contractstart"))
					.contractEnd(rs.getString("contractend"))
					.amount(rs.getDouble("amountc"))
					.locationNumber(rs.getString("locationno"))
					.classType(rs.getInt("classtype"))
					.classTypeName(ClassType.val(rs.getInt("classtype")).getName())
					.status(rs.getInt("statusc"))
					.statusName(TenantStatus.val(rs.getInt("statusc")).getName())
					.isActive(rs.getInt("isactivec"))
					.arrears(rs.getDouble("arrears"))
					.monthStarted(rs.getInt("monthstarted"))
					.yearStarted(rs.getInt("yearstarted"))
					.penaltyRate(rs.getDouble("penaltyrate"))
					.tenant(tn)
					.build();
			
			
			
			tns.add(con);
			
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return tns;
	}
	
	public static TenantContract save(TenantContract st){
		if(st!=null){
			
			long id = TenantContract.getInfo(st.getId() ==0? TenantContract.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = TenantContract.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = TenantContract.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = TenantContract.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save() {
		TenantContract.save(this);
	}
	
	public static TenantContract insertData(TenantContract st, String type){
		String sql = "INSERT INTO tenantcontract ("
				+ "cid,"
				+ "cdate,"
				+ "tid,"
				+ "contractstart,"
				+ "contractend,"
				+ "amountc,"
				+ "locationno,"
				+ "classtype,"
				+ "statusc,"
				+ "isactivec,"
				+ "accountno,"
				+ "arrears,"
				+ "monthstarted,"
				+ "yearstarted,"
				+ "penaltyrate)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table tenantcontract");
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
		
		ps.setString(cnt++, st.getDate());
		ps.setLong(cnt++, st.getTenant().getId());
		ps.setString(cnt++, st.getContractStart());
		ps.setString(cnt++, st.getContractEnd());
		ps.setDouble(cnt++, st.getAmount());
		ps.setString(cnt++, st.getLocationNumber());
		ps.setInt(cnt++, st.getClassType());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getAccountNo());
		ps.setDouble(cnt++, st.getArrears());
		ps.setInt(cnt++, st.getMonthStarted());
		ps.setInt(cnt++, st.getYearStarted());
		ps.setDouble(cnt++, st.getPenaltyRate());
		
		LogU.add(st.getDate());
		LogU.add(st.getTenant().getId());
		LogU.add(st.getContractStart());
		LogU.add(st.getContractEnd());
		LogU.add(st.getAmount());
		LogU.add(st.getLocationNumber());
		LogU.add(st.getClassType());
		LogU.add(st.getStatus());
		LogU.add(st.getIsActive());
		LogU.add(st.getAccountNo());
		LogU.add(st.getArrears());
		LogU.add(st.getMonthStarted());
		LogU.add(st.getYearStarted());
		LogU.add(st.getPenaltyRate());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to tenantcontract : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static TenantContract updateData(TenantContract st){
		String sql = "UPDATE tenantcontract SET "
				+ "cdate=?,"
				+ "tid=?,"
				+ "contractstart=?,"
				+ "contractend=?,"
				+ "amountc=?,"
				+ "locationno=?,"
				+ "classtype=?,"
				+ "statusc=?,"
				+ "isactivec=?,"
				+ "accountno=?,"
				+ "arrears=?,"
				+ "monthstarted=?,"
				+ "yearstarted=?,"
				+ "penaltyrate=?" 
				+ " WHERE cid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table tenantcontract");
		
		ps.setString(cnt++, st.getDate());
		ps.setLong(cnt++, st.getTenant().getId());
		ps.setString(cnt++, st.getContractStart());
		ps.setString(cnt++, st.getContractEnd());
		ps.setDouble(cnt++, st.getAmount());
		ps.setString(cnt++, st.getLocationNumber());
		ps.setInt(cnt++, st.getClassType());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getAccountNo());
		ps.setDouble(cnt++, st.getArrears());
		ps.setInt(cnt++, st.getMonthStarted());
		ps.setInt(cnt++, st.getYearStarted());
		ps.setDouble(cnt++, st.getPenaltyRate());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDate());
		LogU.add(st.getTenant().getId());
		LogU.add(st.getContractStart());
		LogU.add(st.getContractEnd());
		LogU.add(st.getAmount());
		LogU.add(st.getLocationNumber());
		LogU.add(st.getClassType());
		LogU.add(st.getStatus());
		LogU.add(st.getIsActive());
		LogU.add(st.getAccountNo());
		LogU.add(st.getArrears());
		LogU.add(st.getMonthStarted());
		LogU.add(st.getYearStarted());
		LogU.add(st.getPenaltyRate());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to tenantcontract : " + s.getMessage());
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
		sql="SELECT cid FROM tenantcontract ORDER BY cid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("cid");
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
		ps = conn.prepareStatement("SELECT cid FROM tenantcontract WHERE cid=?");
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
		System.out.println("Delete SQL : " + ps.toString());
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public  void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE tenantcontract set isactivec=0 WHERE cid=?";
		
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
