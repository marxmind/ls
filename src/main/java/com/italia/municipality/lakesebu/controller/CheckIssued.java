package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class CheckIssued {
	private long id;
	private int serialNo;
	private String dateTrans;
	private String dvPayroll;
	private String cafoaNo;
	private String centerOffice;
	private String payee;
	private String naturePay;
	private double amount;
	private int fundId;
	private int isActive;
	private CheckIssuedReport checkIssuedReport;
	
	private int number;
	
	public static List<CheckIssued> retrieveReportGroupCheckIssued(long reportId){
		List<CheckIssued> caz = new ArrayList<CheckIssued>();
		
		String sql = "SELECT * FROM checkissued  WHERE  isactiveck=1 AND cpid="+ reportId + " ORDER BY lineno";
			
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("Cash checkissued SQL " + ps.toString());
		rs = ps.executeQuery();
		int x = 1;
		while(rs.next()){
			

			CheckIssued cz = CheckIssued.builder()
					.number(x)
					.id(rs.getLong("ckid"))
					.dateTrans(rs.getString("datetrans"))
					.serialNo(rs.getInt("serialno"))
					.dvPayroll(rs.getString("dvpayroll"))
					.cafoaNo(rs.getString("cafoano"))
					.centerOffice(rs.getString("center"))
					.payee(rs.getString("payee"))
					.naturePay(rs.getString("naturepay"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactiveck"))
					.fundId(rs.getInt("fundid"))
					.checkIssuedReport(CheckIssuedReport.builder().id(rs.getLong("cpid")).build())
					.number(rs.getInt("lineno"))
					.build();
			
			caz.add(cz);
			x++;
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return caz;
	}
	
	public static List<CheckIssued> retrieveOne(long reportId){
		List<CheckIssued> caz = new ArrayList<CheckIssued>();
		
		String sql = "SELECT * FROM checkissued  WHERE  isactiveck=1 AND ckid="+ reportId;
			
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("Cash Disbursement SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			CheckIssued cz = CheckIssued.builder()
					.id(rs.getLong("ckid"))
					.dateTrans(rs.getString("datetrans"))
					.serialNo(rs.getInt("serialno"))
					.dvPayroll(rs.getString("dvpayroll"))
					.cafoaNo(rs.getString("cafoano"))
					.centerOffice(rs.getString("center"))
					.payee(rs.getString("payee"))
					.naturePay(rs.getString("naturepay"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactiveck"))
					.fundId(rs.getInt("fundid"))
					.checkIssuedReport(CheckIssuedReport.builder().id(rs.getLong("cpid")).build())
					.number(rs.getInt("lineno"))
					.build();
			caz.add(cz);
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return caz;
	}
	
	public static List<CheckIssued> retrieve(String sql, String[] params){
		List<CheckIssued> caz = new ArrayList<CheckIssued>();
		
		
		String tableCaz = "caz";
		String tableRpt = "rpt";
		String sqlAdd = "SELECT * FROM checkissued "+tableCaz+", checkissuedreport "+ tableRpt +"  WHERE  "+tableCaz+".isactiveck=1 AND "
				+ tableCaz + ".cpid=" + tableRpt + ".cpid ";
				
		
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
		System.out.println("Cash checkissued SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			CheckIssuedReport rpt = CheckIssuedReport.builder()
					.id(rs.getLong("cpid"))
					.reportNo(rs.getString("reportno"))
					.bankAccountNo(rs.getString("bankaccountno"))
					.periodCovered(rs.getString("periodcovered"))
					.dateReport(rs.getString("datereport"))
					.totalAmount(rs.getDouble("totalamount"))
					.disbursingOfficer(rs.getString("disbursingofficer"))
					.designation(rs.getString("designation"))
					.receivingOfficer(rs.getString("receivingofficer"))
					.receivePosition(rs.getString("receivedposition"))
					.isActive(rs.getInt("isactivecp"))
					.fundId(rs.getInt("fundid"))
					.build();
			
			CheckIssued cz = CheckIssued.builder()
					.id(rs.getLong("ckid"))
					.dateTrans(rs.getString("datetrans"))
					.serialNo(rs.getInt("serialno"))
					.dvPayroll(rs.getString("dvpayroll"))
					.cafoaNo(rs.getString("cafoano"))
					.centerOffice(rs.getString("center"))
					.payee(rs.getString("payee"))
					.naturePay(rs.getString("naturepay"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactiveck"))
					.fundId(rs.getInt("fundid"))
					.checkIssuedReport(rpt)
					.number(rs.getInt("lineno"))
					.build();
			caz.add(cz);
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return caz;
	}
	
	public static CheckIssued save(CheckIssued st){
		if(st!=null){
			
			long id = CheckIssued.getInfo(st.getId() ==0? CheckIssued.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = CheckIssued.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = CheckIssued.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = CheckIssued.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			CheckIssued.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			CheckIssued.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			CheckIssued.insertData(this, "3");
		}
		
 }
	
	public static CheckIssued insertData(CheckIssued st, String type){
		String sql = "INSERT INTO checkissued ("
				+ "ckid,"
				+ "datetrans,"
				+ "serialno,"
				+ "dvpayroll,"
				+ "cafoano,"
				+ "center,"
				+ "payee,"
				+ "naturepay,"
				+ "amount,"
				+ "isactiveck,"
				+ "fundid,"
				+ "cpid,"
				+ "lineno)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table checkissued");
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
		ps.setInt(cnt++, st.getSerialNo());
		ps.setString(cnt++, st.getDvPayroll());
		ps.setString(cnt++, st.getCafoaNo());
		ps.setString(cnt++, st.getCenterOffice());
		ps.setString(cnt++, st.getPayee());
		ps.setString(cnt++, st.getNaturePay());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getFundId());
		ps.setLong(cnt++, st.getCheckIssuedReport().getId());
		ps.setInt(cnt++, st.getNumber());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getSerialNo());
		LogU.add(st.getDvPayroll());
		LogU.add(st.getCafoaNo());
		LogU.add(st.getCenterOffice());
		LogU.add(st.getPayee());
		LogU.add(st.getNaturePay());
		LogU.add(st.getAmount());
		LogU.add(st.getIsActive());
		LogU.add(st.getFundId());
		LogU.add(st.getCheckIssuedReport().getId());
		LogU.add(st.getNumber());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to checkissued : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static CheckIssued updateData(CheckIssued st){
		String sql = "UPDATE checkissued SET "
				+ "datetrans=?,"
				+ "serialno=?,"
				+ "dvpayroll=?,"
				+ "cafoano=?,"
				+ "center=?,"
				+ "payee=?,"
				+ "naturepay=?,"
				+ "amount=?,"
				+ "fundid=?,"
				+ "cpid=?,"
				+ "lineno=?" 
				+ " WHERE ckid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table checkissued");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setInt(cnt++, st.getSerialNo());
		ps.setString(cnt++, st.getDvPayroll());
		ps.setString(cnt++, st.getCafoaNo());
		ps.setString(cnt++, st.getCenterOffice());
		ps.setString(cnt++, st.getPayee());
		ps.setString(cnt++, st.getNaturePay());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getFundId());
		ps.setLong(cnt++, st.getCheckIssuedReport().getId());
		ps.setInt(cnt++, st.getNumber());
		ps.setLong(cnt++, st.getId());
		
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getSerialNo());
		LogU.add(st.getDvPayroll());
		LogU.add(st.getCafoaNo());
		LogU.add(st.getCenterOffice());
		LogU.add(st.getPayee());
		LogU.add(st.getNaturePay());
		LogU.add(st.getAmount());
		LogU.add(st.getFundId());
		LogU.add(st.getCheckIssuedReport().getId());
		LogU.add(st.getNumber());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to checkissued : " + s.getMessage());
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
		sql="SELECT ckid FROM checkissued  ORDER BY ckid DESC LIMIT 1";	
		conn = BankChequeDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("ckid");
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
		ps = conn.prepareStatement("SELECT ckid FROM checkissued WHERE ckid=?");
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
		String sql = "UPDATE checkissued set isactiveck=0 WHERE ckid=?";
		
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
