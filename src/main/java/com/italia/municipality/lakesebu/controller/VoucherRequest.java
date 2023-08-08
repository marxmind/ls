package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class VoucherRequest {
	private long id;
	private String dateTrans;
	private String requestorName;
	private String contactNo;
	private String requestorRemarks;
	private String budgetRemarks;
	private String accountingRemarks;
	private String treasurerRemarks;
	private double amountRequested;
	private double amountBudgetApproved;
	private double amountAccountingApproved;
	private int status;
	private int isActive;
	private Offices office;
	private Mooe mooe;
	
	private String statusName;
	private Date tmpData;
	
	public static List<VoucherRequest> retrieve(String sql, String[] params){
		List<VoucherRequest> vrs = new ArrayList<VoucherRequest>();
		String vr = "vr";
		String ofs = "ofs";
		String mos = "moe";
		String sqlTemp = "SELECT * FROM voucherrequest "+ vr +", mooe "+ mos +", offices "+ ofs +" WHERE "+ vr +".isactivevr=1 AND "
				+ vr + ".offid=" + ofs + ".offid AND " 
				+ vr + ".offid=" + mos + ".moid ";
		
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
		System.out.println("mooe SQL " + ps.toString());
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
			
			Mooe moe = Mooe.builder()
				.id(rs.getLong("moid"))
				.code(rs.getString("code"))
				.dateTrans(rs.getString("encodedate"))
				.description(rs.getString("description"))
				.budgetAmount(rs.getDouble("budgetamount"))
				.isActive(rs.getInt("isactivem"))
				.yearBudget(rs.getInt("yearbudget"))
				.offices(off)
				.build();
			
			VoucherRequest vq = VoucherRequest.builder()
					.id(rs.getLong("vrid"))
					.dateTrans(rs.getString("datetrans"))
					.requestorName(rs.getString("requestor"))
					.contactNo(rs.getString("contactno"))
					.budgetRemarks(rs.getString("budgetremarks"))
					.accountingRemarks(rs.getString("accountingremarks"))
					.treasurerRemarks(rs.getString("treasurerremarks"))
					.requestorRemarks(rs.getString("reqremarks"))
					.amountRequested(rs.getDouble("amountreq"))
					.amountBudgetApproved(rs.getDouble("amountreq"))
					.amountAccountingApproved(rs.getDouble("amountreq"))
					.status(rs.getInt("status"))
					.isActive(rs.getInt("isactivevr"))
					.office(off)
					.mooe(moe)
					.build();
			
			vrs.add(vq);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return vrs;
	}
	
	public static VoucherRequest save(VoucherRequest st){
		if(st!=null){
			
			long id = VoucherRequest.getInfo(st.getId() ==0? Mooe.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = VoucherRequest.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = VoucherRequest.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = VoucherRequest.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			VoucherRequest.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			VoucherRequest.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			VoucherRequest.insertData(this, "3");
		}
		
 }
	
	public static VoucherRequest insertData(VoucherRequest st, String type){
		String sql = "INSERT INTO voucherrequest ("
				+ "vrid,"
				+ "datetrans,"
				+ "requestor,"
				+ "contactno,"
				+ "budgetremarks,"
				+ "accountingremarks,"
				+ "treasurerremarks,"
				+ "reqremarks,"
				+ "amountreq,"
				+ "amountbudgetapproved,"
				+ "amountaccountingapproved,"
				+ "status,"
				+ "offid,"
				+ "moid,"
				+ "isactivevr)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table voucherrequest");
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
		ps.setString(cnt++, st.getRequestorName());
		ps.setString(cnt++, st.getContactNo());
		ps.setString(cnt++, st.getBudgetRemarks());
		ps.setString(cnt++, st.getAccountingRemarks());
		ps.setString(cnt++, st.getTreasurerRemarks());
		ps.setString(cnt++, st.getRequestorRemarks());
		ps.setDouble(cnt++, st.getAmountRequested());
		ps.setDouble(cnt++, st.getAmountBudgetApproved());
		ps.setDouble(cnt++, st.getAmountAccountingApproved());
		ps.setInt(cnt++, st.getStatus());
		ps.setLong(cnt++, st.getOffice().getId());
		ps.setLong(cnt++, st.getMooe().getId());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getRequestorName());
		LogU.add(st.getContactNo());
		LogU.add(st.getBudgetRemarks());
		LogU.add(st.getAccountingRemarks());
		LogU.add(st.getTreasurerRemarks());
		LogU.add(st.getRequestorRemarks());
		LogU.add(st.getAmountRequested());
		LogU.add(st.getAmountBudgetApproved());
		LogU.add(st.getAmountAccountingApproved());
		LogU.add(st.getStatus());
		LogU.add(st.getOffice().getId());
		LogU.add(st.getMooe().getId());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to voucherrequest : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
		return st;
	}
	
	public static VoucherRequest updateData(VoucherRequest st){
		String sql = "UPDATE voucherrequest SET "
				+ "datetrans=?,"
				+ "requestor=?,"
				+ "contactno=?,"
				+ "budgetremarks=?,"
				+ "accountingremarks=?,"
				+ "treasurerremarks=?,"
				+ "reqremarks=?,"
				+ "amountreq=?,"
				+ "amountbudgetapproved=?,"
				+ "amountaccountingapproved=?,"
				+ "status=?,"
				+ "offid=?,"
				+ "moid=?" 
				+ " WHERE vrid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getRequestorName());
		ps.setString(cnt++, st.getContactNo());
		ps.setString(cnt++, st.getBudgetRemarks());
		ps.setString(cnt++, st.getAccountingRemarks());
		ps.setString(cnt++, st.getTreasurerRemarks());
		ps.setString(cnt++, st.getRequestorRemarks());
		ps.setDouble(cnt++, st.getAmountRequested());
		ps.setDouble(cnt++, st.getAmountBudgetApproved());
		ps.setDouble(cnt++, st.getAmountAccountingApproved());
		ps.setInt(cnt++, st.getStatus());
		ps.setLong(cnt++, st.getOffice().getId());
		ps.setLong(cnt++, st.getMooe().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getRequestorName());
		LogU.add(st.getContactNo());
		LogU.add(st.getBudgetRemarks());
		LogU.add(st.getAccountingRemarks());
		LogU.add(st.getTreasurerRemarks());
		LogU.add(st.getRequestorRemarks());
		LogU.add(st.getAmountRequested());
		LogU.add(st.getAmountBudgetApproved());
		LogU.add(st.getAmountAccountingApproved());
		LogU.add(st.getStatus());
		LogU.add(st.getOffice().getId());
		LogU.add(st.getMooe().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to voucherrequest : " + s.getMessage());
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
		sql="SELECT vrid FROM voucherrequest ORDER BY vrid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("vrid");
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
		ps = conn.prepareStatement("SELECT vrid FROM voucherrequest WHERE vrid=?");
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
		String sql = "UPDATE voucherrequest set isactivevr=0 WHERE vrid=?";
		
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
