package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class CashDisbursementReport {
	private long id;
	private String periodCovered;
	private String dateReport;
	private double totalAmount;
	private String disbursingOfficer;
	private String designation;
	private int isActive;
	private int fundId=1;
	private String reportNo;
	private Login loginUser;
	
	private Date dateReportTmp;
	private List<CashDisbursement> rpts;
	private String fundName;
	
	public static CashDisbursementReport retrieveOne(long id, long userId){
		CashDisbursementReport cz = new CashDisbursementReport();
		String tableDep="cz";
		String tableUser="user";
		String sql = "SELECT cz.*,user.logid,user.useraccesslevelid FROM cashdisbursementreport "+ tableDep +", webtis.login "+ tableUser +"  WHERE  "+ tableDep +".isactivez=1 AND "
				+ " AND " + tableDep + ".logid.=" + tableUser + ".logid AND "
				+ tableDep +".zid=" + id + " AND " + tableUser + ".logid="+userId;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("Cash Disbursement report SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Login user = Login.builder()
					.logid(rs.getLong("logid"))
					.accessLevel(UserAccessLevel.builder().useraccesslevelid(rs.getInt("useraccesslevelid")).build())
					.build();
			
			cz = CashDisbursementReport.builder()
					.id(rs.getLong("zid"))
					.periodCovered(rs.getString("periodcovered"))
					.dateReport(rs.getString("datereport"))
					.totalAmount(rs.getDouble("totalamount"))
					.disbursingOfficer(rs.getString("disbursingofficer"))
					.designation(rs.getString("designation"))
					.isActive(rs.getInt("isactivez"))
					.fundId(rs.getInt("fundid"))
					.reportNo(rs.getString("reportno"))
					.loginUser(user)
					.build();
			
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return cz;
	}
	
	public static List<CashDisbursementReport> retrieve(String sql, String[] params){
		List<CashDisbursementReport> caz = new ArrayList<CashDisbursementReport>();
		
		
		String tableCaz = "cz";
		String tableUser="user";
		String sqlAdd = "SELECT cz.*,user.logid,user.useraccesslevelid,(select sum(amount) as total from cashdisbursement c where c.isactived=1 AND c.zid = cz.zid) as total FROM cashdisbursementreport "+tableCaz+", webtis.login "+ tableUser +"  WHERE  "+tableCaz+".isactivez=1 "
				+ " AND " + tableCaz + ".logid=" + tableUser + ".logid ";
				
		
		sql = sqlAdd + sql;
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		System.out.println("Cash Disbursement report SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Login user = Login.builder()
					.logid(rs.getLong("logid"))
					.accessLevel(UserAccessLevel.builder().useraccesslevelid(rs.getInt("useraccesslevelid")).build())
					.build();
			
			CashDisbursementReport cz = CashDisbursementReport.builder()
					.id(rs.getLong("zid"))
					.periodCovered(rs.getString("periodcovered"))
					.dateReport(rs.getString("datereport"))
					.totalAmount(rs.getDouble("total"))
					.disbursingOfficer(rs.getString("disbursingofficer"))
					.designation(rs.getString("designation"))
					.isActive(rs.getInt("isactivez"))
					.fundId(rs.getInt("fundid"))
					.reportNo(rs.getString("reportno"))
					.loginUser(user)
					.build();
			caz.add(cz);
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return caz;
	}
	
	public static CashDisbursementReport save(CashDisbursementReport st){
		if(st!=null){
			
			long id = CashDisbursementReport.getInfo(st.getId() ==0? CashDisbursementReport.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = CashDisbursementReport.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = CashDisbursementReport.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = CashDisbursementReport.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			CashDisbursementReport.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			CashDisbursementReport.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			CashDisbursementReport.insertData(this, "3");
		}
		
 }
	
	public static CashDisbursementReport insertData(CashDisbursementReport st, String type){
		String sql = "INSERT INTO cashdisbursementreport ("
				+ "zid,"
				+ "periodcovered,"
				+ "datereport,"
				+ "totalamount,"
				+ "disbursingofficer,"
				+ "designation,"
				+ "isactivez,"
				+ "fundid,"
				+ "reportno,"
				+ "logid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cashdisbursementreport");
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
		
		ps.setString(cnt++, st.getPeriodCovered());
		ps.setString(cnt++, st.getDateReport());
		ps.setDouble(cnt++, st.getTotalAmount());
		ps.setString(cnt++, st.getDisbursingOfficer());
		ps.setString(cnt++, st.getDesignation());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getFundId());
		ps.setString(cnt++, st.getReportNo());
		ps.setLong(cnt++, st.getLoginUser().getLogid());
		
		LogU.add(st.getPeriodCovered());
		LogU.add(st.getDateReport());
		LogU.add(st.getTotalAmount());
		LogU.add(st.getDisbursingOfficer());
		LogU.add(st.getDesignation());
		LogU.add(st.getIsActive());
		LogU.add(st.getFundId());
		LogU.add(st.getReportNo());
		LogU.add(st.getLoginUser().getLogid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to cashdisbursementreport : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static CashDisbursementReport updateData(CashDisbursementReport st){
		String sql = "UPDATE cashdisbursementreport SET "
				+ "periodcovered=?,"
				+ "datereport=?,"
				+ "totalamount=?,"
				+ "disbursingofficer=?,"
				+ "designation=?,"
				+ "fundid=?,"
				+ "reportno=?,"
				+ "logid=?" 
				+ " WHERE zid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table cashdisbursementreport");
		
		ps.setString(cnt++, st.getPeriodCovered());
		ps.setString(cnt++, st.getDateReport());
		ps.setDouble(cnt++, st.getTotalAmount());
		ps.setString(cnt++, st.getDisbursingOfficer());
		ps.setString(cnt++, st.getDesignation());
		ps.setInt(cnt++, st.getFundId());
		ps.setString(cnt++, st.getReportNo());
		ps.setLong(cnt++, st.getLoginUser().getLogid());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getPeriodCovered());
		LogU.add(st.getDateReport());
		LogU.add(st.getTotalAmount());
		LogU.add(st.getDisbursingOfficer());
		LogU.add(st.getDesignation());
		LogU.add(st.getFundId());
		LogU.add(st.getReportNo());
		LogU.add(st.getLoginUser().getLogid());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to cashdisbursementreport : " + s.getMessage());
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
		sql="SELECT zid FROM cashdisbursementreport  ORDER BY zid DESC LIMIT 1";	
		conn = BankChequeDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("zid");
		}
		
		rs.close();
		prep.close();
		BankChequeDatabaseConnect.close(conn);
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
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT zid FROM cashdisbursementreport WHERE zid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE cashdisbursementreport set isactivez=0 WHERE zid=?";
		
		String[] params = new String[1];
		params[0] = getId()+"";
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
}