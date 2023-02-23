package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class PayorPayment implements IPayorPayment{
	
	private long id;
	private String tdNo;
	private String lotNo;
	private String owner;
	private String barangay;
	private double assessedValue;
	private String address;//is not part of the table
	private String orNumber;
	private String paidDate;
	private int fromQtr;
	private int toQtr;
	private int fromYear;
	private int toYear;
	private String kind;
	private String actualUse;
	private double principal;
	private double penalty;
	private double total;
	private String collector;
	private String type;
	private int isActive;
	
	
	public  static boolean isExistingPayment(PayorPayment py) {
		
		String sql = "SELECT * FROM paymenthistory WHERE isactivepayment=1 AND tdNo=? AND paidDate=? AND orNumber=? AND typepay=?";
		String[] params = new String[4];
		try{params[0] = py.getTdNo().strip();}catch(NullPointerException e) {params[0]="";}
		try{params[1] = py.getPaidDate().strip();}catch(NullPointerException e) {params[1]="";}
		try{params[2] = py.getOrNumber().strip();}catch(NullPointerException e) {params[2]="";}
		try{params[3] = py.getType().strip();}catch(NullPointerException e) {params[2]="";}
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		//System.out.println("SQL history payment exist " + ps.toString());
		
		rs = ps.executeQuery();
		
		return rs.next()==true? true : false;
		
		}catch(SQLException e) {}
		
		return false;
	}
	
	public static List<PayorPayment> retrieve(String sql, String[] params){
		
		List<PayorPayment> pays = new ArrayList<PayorPayment>();
		String stasql = "SELECT * FROM paymenthistory WHERE isactivepayment=1 " + sql;
				sql = stasql;
				Connection conn = null;
				ResultSet rs = null;
				PreparedStatement ps = null;
				try{
				conn = TaxDatabaseConnect.getConnection();
				ps = conn.prepareStatement(sql);
				
				if(params!=null && params.length>0){
					
					for(int i=0; i<params.length; i++){
						ps.setString(i+1, params[i]);
					}
					
				}
				
				System.out.println("SQL history payment: " + ps.toString());
				
				rs = ps.executeQuery();
				
				while(rs.next()){
					PayorPayment py = PayorPayment.builder()
							.id(rs.getLong("pid"))
							.paidDate(rs.getString("paidDate"))
							.tdNo(rs.getString("tdNo"))
							.lotNo(rs.getString("lotNo"))
							.owner(rs.getString("owner"))
							.barangay(rs.getString("barangay"))
							.assessedValue(rs.getDouble("assessedValue"))
							.orNumber(rs.getString("orNumber"))
							.fromQtr(rs.getInt("fromQtr"))
							.toQtr(rs.getInt("toQtr"))
							.fromYear(rs.getInt("fromYear"))
							.toYear(rs.getInt("toYear"))
							.kind(rs.getString("kind"))
							.actualUse(rs.getString("actualUse"))
							.principal(rs.getDouble("principal"))
							.penalty(rs.getDouble("penalty"))
							.total(rs.getDouble("total"))
							.collector(rs.getString("collector"))
							.type(rs.getString("typepay"))
							.isActive(rs.getInt("isactivepayment"))
							.build();
					pays.add(py);
				}
				
				rs.close();
				ps.close();
				TaxDatabaseConnect.close(conn);
				}catch(SQLException sl){}	
				
		
		return pays;
	}
	
	public static PayorPayment save(PayorPayment pay) { 
		if(pay!=null){
			long id = getInfo(pay.getId()==0? getLatestId()+1 : pay.getId());
			if(id==1){
				pay = PayorPayment.insertData(pay, "1");
			}else if(id==2){
				pay = PayorPayment.updateData(pay);
			}else if(id==3){
				pay = PayorPayment.insertData(pay, "3");
			}
			
		}
		return pay;
	}
	
	@Override
	public void save() {
		// TODO Auto-generated method stub
		long id = getInfo(getId()==0? getLatestId()+1 : getId());
		if(id==1){
			insertData("1");
		}else if(id==2){
			updateData();
		}else if(id==3){
			insertData("3");
		}
	}
	@Override
	public void insertData(String type) {
		String sql = "INSERT INTO paymenthistory ("
				+ "pid,"
				+ "paidDate,"
				+ "tdNo,"
				+ "lotNo,"
				+ "owner,"
				+ "barangay,"
				+ "assessedValue,"
				+ "orNumber,"
				+ "fromQtr,"
				+ "toQtr,"
				+ "fromYear,"
				+ "toYear,"
				+ "kind,"
				+ "actualUse,"
				+ "principal,"
				+ "penalty,"
				+ "total,"
				+ "collector,"
				+ "typepay,"
				+ "isactivepayment) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table paymenthistory");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		long id =1;
		if("1".equalsIgnoreCase(type)){
			setId(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
		}
		
		ps.setLong(cnt++, id);
		ps.setString(cnt++, getPaidDate());
		ps.setString(cnt++, getTdNo());
		ps.setString(cnt++, getLotNo());
		ps.setString(cnt++, getOwner());
		ps.setString(cnt++, getBarangay());
		ps.setDouble(cnt++, getAssessedValue());
		ps.setString(cnt++, getOrNumber());
		ps.setInt(cnt++, getFromQtr());
		ps.setInt(cnt++, getToQtr());
		ps.setInt(cnt++, getFromYear());
		ps.setInt(cnt++, getToYear());
		ps.setString(cnt++, getKind());
		ps.setString(cnt++, getActualUse());
		ps.setDouble(cnt++, getPrincipal());
		ps.setDouble(cnt++, getPenalty());
		ps.setDouble(cnt++, getTotal());
		ps.setString(cnt++, getCollector());
		ps.setString(cnt++, getType());
		ps.setInt(cnt++, getIsActive());
		
		LogU.add("id "+id);
		LogU.add("date" + getPaidDate());
		LogU.add("tdNO "+getTdNo());
		LogU.add("lot "+getLotNo());
		LogU.add("owner "+getOwner());
		LogU.add("barangay"+getBarangay());
		LogU.add("ass val "+getAssessedValue());
		LogU.add("OR No "+getOrNumber());
		LogU.add("from qtr "+getFromQtr());
		LogU.add("to qtr "+getToQtr());
		LogU.add("from year "+getFromYear());
		LogU.add("to year "+getToYear());
		LogU.add("kind "+getKind());
		LogU.add("actual use "+getActualUse());
		LogU.add("Principal "+getPrincipal());
		LogU.add("penalty "+getPenalty());
		LogU.add("total "+getTotal());
		LogU.add("collector "+getCollector());
		LogU.add("type "+getType());
		LogU.add("active "+getIsActive());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table paymenthistory successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table paymenthistory.");
		}
	}
	
	static PayorPayment insertData(PayorPayment pay, String type) {
		String sql = "INSERT INTO paymenthistory ("
				+ "pid,"
				+ "paidDate,"
				+ "tdNo,"
				+ "lotNo,"
				+ "owner,"
				+ "barangay,"
				+ "assessedValue,"
				+ "orNumber,"
				+ "fromQtr,"
				+ "toQtr,"
				+ "fromYear,"
				+ "toYear,"
				+ "kind,"
				+ "actualUse,"
				+ "principal,"
				+ "penalty,"
				+ "total,"
				+ "collector,"
				+ "typepay,"
				+ "isactivepayment) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table paymenthistory");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		long id =1;
		if("1".equalsIgnoreCase(type)){
			pay.setId(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
		}
		
		ps.setLong(cnt++, id); 
		ps.setString(cnt++, pay.getPaidDate());
		ps.setString(cnt++, pay.getTdNo());
		ps.setString(cnt++, pay.getLotNo());
		ps.setString(cnt++, pay.getOwner());
		ps.setString(cnt++, pay.getBarangay());
		ps.setDouble(cnt++, pay.getAssessedValue());
		ps.setString(cnt++, pay.getOrNumber());
		ps.setInt(cnt++, pay.getFromQtr());
		ps.setInt(cnt++, pay.getToQtr());
		ps.setInt(cnt++, pay.getFromYear());
		ps.setInt(cnt++, pay.getToYear());
		ps.setString(cnt++, pay.getKind());
		ps.setString(cnt++, pay.getActualUse());
		ps.setDouble(cnt++, pay.getPrincipal());
		ps.setDouble(cnt++, pay.getPenalty());
		ps.setDouble(cnt++, pay.getTotal());
		ps.setString(cnt++, pay.getCollector());
		ps.setString(cnt++, pay.getType());
		ps.setInt(cnt++, pay.getIsActive());
		
		LogU.add(id);
		LogU.add(pay.getPaidDate());
		LogU.add(pay.getTdNo());
		LogU.add(pay.getLotNo());
		LogU.add(pay.getOwner());
		LogU.add(pay.getBarangay());
		LogU.add(pay.getAssessedValue());
		LogU.add(pay.getOrNumber());
		LogU.add(pay.getFromQtr());
		LogU.add(pay.getToQtr());
		LogU.add(pay.getFromYear());
		LogU.add(pay.getToYear());
		LogU.add(pay.getKind());
		LogU.add(pay.getActualUse());
		LogU.add(pay.getPrincipal());
		LogU.add(pay.getPenalty());
		LogU.add(pay.getTotal());
		LogU.add(pay.getCollector());
		LogU.add(pay.getType());
		LogU.add(pay.getIsActive());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table paymenthistory successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table paymenthistory.");
		}
		return pay;
	}
	
	@Override
	public void updateData() {
		String sql = "UPDATE paymenthistory SET "
				+ "paidDate=?,"
				+ "tdNo=?,"
				+ "lotNo=?,"
				+ "owner=?,"
				+ "barangay=?,"
				+ "assessedValue=?,"
				+ "orNumber=?,"
				+ "fromQtr=?,"
				+ "toQtr=?,"
				+ "fromYear=?,"
				+ "toYear=?,"
				+ "kind=?,"
				+ "actualUse=?,"
				+ "principal=?,"
				+ "penalty=?,"
				+ "total=?,"
				+ "collector=?,"
				+ "typepay=?"
				+ " WHERE pid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Update table paymenthistory");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		
		ps.setString(cnt++, getPaidDate());
		ps.setString(cnt++, getTdNo());
		ps.setString(cnt++, getLotNo());
		ps.setString(cnt++, getOwner());
		ps.setString(cnt++, getBarangay());
		ps.setDouble(cnt++, getAssessedValue());
		ps.setString(cnt++, getOrNumber());
		ps.setInt(cnt++, getFromQtr());
		ps.setInt(cnt++, getToQtr());
		ps.setInt(cnt++, getFromYear());
		ps.setInt(cnt++, getToYear());
		ps.setString(cnt++, getKind());
		ps.setString(cnt++, getActualUse());
		ps.setDouble(cnt++, getPrincipal());
		ps.setDouble(cnt++, getPenalty());
		ps.setDouble(cnt++, getTotal());
		ps.setString(cnt++, getCollector());
		ps.setString(cnt++, getType());
		ps.setLong(cnt++, getId());
		
		LogU.add(getPaidDate());
		LogU.add(getTdNo());
		LogU.add(getLotNo());
		LogU.add(getOwner());
		LogU.add(getBarangay());
		LogU.add(getAssessedValue());
		LogU.add(getOrNumber());
		LogU.add(getFromQtr());
		LogU.add(getToQtr());
		LogU.add(getFromYear());
		LogU.add(getToYear());
		LogU.add(getKind());
		LogU.add(getActualUse());
		LogU.add(getPrincipal());
		LogU.add(getPenalty());
		LogU.add(getTotal());
		LogU.add(getCollector());
		LogU.add(getType());
		LogU.add(getId());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("update table paymenthistory successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error update table paymenthistory.");
		}
	}
	
	public static PayorPayment updateData(PayorPayment pay) {
		String sql = "UPDATE paymenthistory SET "
				+ "paidDate=?,"
				+ "tdNo=?,"
				+ "lotNo=?,"
				+ "owner=?,"
				+ "barangay=?,"
				+ "assessedValue=?,"
				+ "orNumber=?,"
				+ "fromQtr=?,"
				+ "toQtr=?,"
				+ "fromYear=?,"
				+ "toYear=?,"
				+ "kind=?,"
				+ "actualUse=?,"
				+ "principal=?,"
				+ "penalty=?,"
				+ "total=?,"
				+ "collector=?,"
				+ "typepay=?"
				+ " WHERE pid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Update table paymenthistory");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		
		ps.setString(cnt++, pay.getPaidDate());
		ps.setString(cnt++, pay.getTdNo());
		ps.setString(cnt++, pay.getLotNo());
		ps.setString(cnt++, pay.getOwner());
		ps.setString(cnt++, pay.getBarangay());
		ps.setDouble(cnt++, pay.getAssessedValue());
		ps.setString(cnt++, pay.getOrNumber());
		ps.setInt(cnt++, pay.getFromQtr());
		ps.setInt(cnt++, pay.getToQtr());
		ps.setInt(cnt++, pay.getFromYear());
		ps.setInt(cnt++, pay.getToYear());
		ps.setString(cnt++, pay.getKind());
		ps.setString(cnt++, pay.getActualUse());
		ps.setDouble(cnt++, pay.getPrincipal());
		ps.setDouble(cnt++, pay.getPenalty());
		ps.setDouble(cnt++, pay.getTotal());
		ps.setString(cnt++, pay.getCollector());
		ps.setString(cnt++, pay.getType());
		ps.setLong(cnt++, pay.getId());
		
		LogU.add(pay.getPaidDate());
		LogU.add(pay.getTdNo());
		LogU.add(pay.getLotNo());
		LogU.add(pay.getOwner());
		LogU.add(pay.getBarangay());
		LogU.add(pay.getAssessedValue());
		LogU.add(pay.getOrNumber());
		LogU.add(pay.getFromQtr());
		LogU.add(pay.getToQtr());
		LogU.add(pay.getFromYear());
		LogU.add(pay.getToYear());
		LogU.add(pay.getKind());
		LogU.add(pay.getActualUse());
		LogU.add(pay.getPrincipal());
		LogU.add(pay.getPenalty());
		LogU.add(pay.getTotal());
		LogU.add(pay.getCollector());
		LogU.add(pay.getType());
		LogU.add(pay.getId());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("update table paymenthistory successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error update table paymenthistory.");
		}
		return pay;
	}
	
	@Override
	public void delete() {
		String sql = "update paymenthistory set isactivepayment=0 WHERE pid=?";
		String params[] = new String[2];
		params[0] = Login.getUserLogin().getUserDtls().getUserdtlsid()+"";
		params[1] = getId()+"";
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		LogU.add("Deleting data in payorland where id="+getId());	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
	}
	
	static void delete(String sql, String[] params) {
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
	}
	
	public static long getLatestId() {
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT pid FROM paymenthistory  ORDER BY pid DESC LIMIT 1";	
		conn = TaxDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("pid");
		}
		
		rs.close();
		prep.close();
		TaxDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static long getInfo(long id) {
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
	
	public static boolean isIdNoExist(long id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT pid FROM paymenthistory WHERE pid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}