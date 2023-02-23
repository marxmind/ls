package com.italia.municipality.lakesebu.controller;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
public class VRData {

	private long id;
	private String date;
	private String monthGroup;
	private Department department;
	private BankAccounts accounts;
	private String checkNo;
	private String payee;
	private String paymentDesc;
	private double gross;
	private double net;
	private int signatory1;
	private int signatory2;
	private String voucherSeries;
	private int isActive;
	
	private Responsibility responsibility;
	
	private Signatories signature1;
	private Signatories signature2;
	
	public static boolean isDuplicate(String checkNo, int departmentId) {
		
		String sql = "SELECT checkno FROM vrdata WHERE isactivevr=1  AND checkno='"+ checkNo.trim() +"' AND departmentid=" + departmentId;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
		
			rs = ps.executeQuery();
			
			while(rs.next()){
				return true;
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		
		return false;
	}
	
	public static String newCheckNo(int accountId) {
		
		
		String sql = "SELECT checkno FROM vrdata WHERE bankid=" + accountId + " ORDER BY vrid DESC LIMIT 1";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String oldCheckNo=null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
		
			rs = ps.executeQuery();
			
			while(rs.next()){
				oldCheckNo = rs.getString("checkno");
				System.out.println("Old Check No: " + oldCheckNo);
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		
		String checkNo = "0000000000";
		long num = 0l;
		if(oldCheckNo!=null) {
			num = Long.valueOf(oldCheckNo);
			num += 1; //increment by one
			
			String len = num+"";
			int ln = len.length();
			switch(ln){
				case 0 : checkNo="0000000000"; break;
				case 1 : checkNo="000000000"+num; break;
				case 2 : checkNo="00000000"+num; break;
				case 3 : checkNo="0000000"+num; break;
				case 4 : checkNo="000000"+num; break;
				case 5 : checkNo="00000"+num; break;
				case 6 : checkNo="0000"+num; break;
				case 7 : checkNo="000"+num; break;
				case 8 : checkNo="00"+num; break;
				case 9 : checkNo="0"+num; break;
				case 10 : checkNo=""+num; break;
			}
		}
		System.out.println("New Check No: " + checkNo);
		
		return checkNo;
	}
	
	public static double amountSuggest(int departmentId, String payee, String nature) {
		double amount = 0d;
		
		String sql = "SELECT gross FROM vrdata  WHERE isactivevr=1 AND departmentid="+ departmentId + " AND payee like '%"  + payee.trim() +"%' AND paymentdesc like '%"+ nature.trim() +"%' ORDER BY vrid DESC LIMIT 1";
				
		System.out.println("SQL retrieve >> " + sql);
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				return rs.getDouble("gross");
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return amount;
	}
	
	public static List<String> retrieve(String query, String fld, int limit){
		List<String> result = new ArrayList<String>();
		
		String sql = "SELECT DISTINCT "+ fld +" FROM vrdata  WHERE isactivevr=1 AND "+ fld + " like '%"  + query +"%' ORDER BY "+ fld +" LIMIT " + limit;
				
		if(limit==0) {
			sql = "SELECT DISTINCT "+ fld +" FROM vrdata  WHERE isactivevr=1 AND "+ fld + " like '%"  + query +"%' ORDER BY "+ fld;
		}
		
		//System.out.println("SQL retrieve >> " + sql);
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				result.add(rs.getString(fld));
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static List<String> retrieve(int year, int month, String fldDistinct, int limit){
		List<String> result = new ArrayList<String>();
		
		String range = " (vrdate>='"+ year +"-"+ (month<10? "0"+month: month) +"-01' AND vrdate<='"+ year +"-"+ (month<10? "0"+month: month) +"-31') ";
		String sql = "SELECT DISTINCT "+ fldDistinct +" FROM vrdata  WHERE isactivevr=1 AND "+ range +" ORDER BY "+ fldDistinct +" LIMIT " + limit;
				
		if(limit==0) {
			sql = "SELECT DISTINCT "+ fldDistinct +" FROM vrdata  WHERE isactivevr=1 AND "+ range +" ORDER BY "+ fldDistinct;
		}
		
		//System.out.println("SQL retrieve >> " + sql);
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				result.add(rs.getString(fldDistinct));
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return result;
	}
	
	public static List<VRData> retrieve(String sql, String[] params){
		List<VRData> vrs = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		
		String tableVr = "vr";
		String tableDep = "dep";
		String tableR = "rs";
		String sqlQ = "SELECT * FROM vrdata "+ tableVr +", responsibility "+ tableR  +", department "+ tableDep +" WHERE "+ 
		tableVr +".isactivevr=1 AND " + tableVr +".departmentid=" + tableDep + ".departmentid AND " +
		tableVr +".rid=" + tableR+".rid ";
		
		sql = sqlQ + sql;
		
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			if(params!=null && params.length>0){
				
				for(int i=0; i<params.length; i++){
					ps.setString(i+1, params[i]);
				}
				
			}
			System.out.println("SQL retrieve >> " + ps.toString());
			rs = ps.executeQuery();
			
			while(rs.next()){
				
				VRData d = new VRData();
				d.setId(rs.getLong("vrid"));
				d.setDate(rs.getString("vrdate"));
				d.setMonthGroup(rs.getString("vrmonthgroup"));
				
				Department department = new Department();
				department.setDepid(rs.getInt("departmentid"));
				department.setDepartmentName(rs.getString("departmentname"));
				department.setDepartmentHead(rs.getString("dephead"));
				department.setCode(rs.getString("code"));
				 
				d.setDepartment(department);
				
				BankAccounts acc = new BankAccounts();
				acc.setBankId(rs.getInt("bankid"));
				d.setAccounts(acc);
				
				d.setCheckNo(rs.getString("checkno"));
				d.setPayee(rs.getString("payee"));
				d.setPaymentDesc(rs.getString("paymentdesc"));
				
				d.setGross(rs.getDouble("gross"));
				d.setNet(rs.getDouble("net"));
				
				Signatories sig = Signatories.retrieve(rs.getInt("sig1"));
				d.setSignature1(sig);
				
				sig = Signatories.retrieve(rs.getInt("sig2"));
				d.setSignature2(sig);
			
				d.setVoucherSeries(rs.getString("vrcode"));
				d.setIsActive(rs.getInt("isactivevr"));
				
				Responsibility r = new Responsibility();
				try{r.setId(rs.getLong("rid"));}catch(Exception e) {}
				try{r.setDateTrans(rs.getString("rrdate"));}catch(Exception e) {}
				try{r.setName(rs.getString("rname"));}catch(Exception e) {}
				try{r.setIsResign(rs.getInt("isresign"));}catch(Exception e) {}
				try{r.setIsActive(rs.getInt("isactiver"));}catch(Exception e) {}
				d.setResponsibility(r);
				vrs.add(d);
				
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return vrs;
	}
	
	public static VRData save(VRData st){
		if(st!=null){
			
			long id = VRData.getInfo(st.getId() ==0? VRData.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = VRData.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = VRData.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = VRData.insertData(st, "3");
			}
			
		}
		return st;
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
	
	public static VRData insertData(VRData st, String type){
		String sql = "INSERT INTO vrdata ("
				+ "vrid,"
				+ "vrdate,"
				+ "vrmonthgroup,"
				+ "departmentid,"
				+ "bankid,"
				+ "checkno,"
				+ "payee,"
				+ "paymentdesc,"
				+ "gross,"
				+ "net,"
				+ "sig1,"
				+ "sig2,"
				+ "vrcode,"
				+ "isactivevr,"
				+ "rid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table vrdata");
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
		ps.setString(cnt++, st.getMonthGroup());
		ps.setInt(cnt++, st.getDepartment().getDepid());
		ps.setInt(cnt++, st.getAccounts().getBankId());
		ps.setString(cnt++, st.getCheckNo());
		ps.setString(cnt++, st.getPayee());
		ps.setString(cnt++, st.getPaymentDesc());
		ps.setDouble(cnt++, st.getGross());
		ps.setDouble(cnt++, st.getNet());
		ps.setInt(cnt++, st.getSignatory1());
		ps.setInt(cnt++, st.getSignatory2());
		ps.setString(cnt++, st.getVoucherSeries());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getResponsibility().getId());
		
		LogU.add(st.getDate());
		LogU.add(st.getMonthGroup());
		LogU.add(st.getDepartment().getDepid());
		LogU.add(st.getAccounts().getBankId());
		LogU.add(st.getCheckNo());
		LogU.add(st.getPayee());
		LogU.add(st.getPaymentDesc());
		LogU.add(st.getGross());
		LogU.add(st.getNet());
		LogU.add(st.getSignatory1());
		LogU.add(st.getSignatory2());
		LogU.add(st.getVoucherSeries());
		LogU.add(st.getIsActive());
		LogU.add(st.getResponsibility().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to vrdata : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO vrdata ("
				+ "vrid,"
				+ "vrdate,"
				+ "vrmonthgroup,"
				+ "departmentid,"
				+ "bankid,"
				+ "checkno,"
				+ "payee,"
				+ "paymentdesc,"
				+ "gross,"
				+ "net,"
				+ "sig1,"
				+ "sig2,"
				+ "vrcode,"
				+ "isactivevr,"
				+ "rid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table vrdata");
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
		
		ps.setString(cnt++, getDate());
		ps.setString(cnt++, getMonthGroup());
		ps.setInt(cnt++, getDepartment().getDepid());
		ps.setInt(cnt++, getAccounts().getBankId());
		ps.setString(cnt++, getCheckNo());
		ps.setString(cnt++, getPayee());
		ps.setString(cnt++, getPaymentDesc());
		ps.setDouble(cnt++, getGross());
		ps.setDouble(cnt++, getNet());
		ps.setInt(cnt++, getSignatory1());
		ps.setInt(cnt++, getSignatory2());
		ps.setString(cnt++, getVoucherSeries());
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getResponsibility().getId());
		
		LogU.add(getDate());
		LogU.add(getMonthGroup());
		LogU.add(getDepartment().getDepid());
		LogU.add(getAccounts().getBankId());
		LogU.add(getCheckNo());
		LogU.add(getPayee());
		LogU.add(getPaymentDesc());
		LogU.add(getGross());
		LogU.add(getNet());
		LogU.add(getSignatory1());
		LogU.add(getSignatory2());
		LogU.add(getVoucherSeries());
		LogU.add(getIsActive());
		LogU.add(getResponsibility().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to vrdata : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static VRData updateData(VRData st){
		String sql = "UPDATE vrdata SET "
				+ "vrdate=?,"
				+ "vrmonthgroup=?,"
				+ "departmentid=?,"
				+ "bankid=?,"
				+ "checkno=?,"
				+ "payee=?,"
				+ "paymentdesc=?,"
				+ "gross=?,"
				+ "net=?,"
				+ "sig1=?,"
				+ "sig2=?,"
				+ "vrcode=?,"
				+ "rid=? " 
				+ " WHERE vrid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table vrdata");
		
		
		ps.setString(cnt++, st.getDate());
		ps.setString(cnt++, st.getMonthGroup());
		ps.setInt(cnt++, st.getDepartment().getDepid());
		ps.setInt(cnt++, st.getAccounts().getBankId());
		ps.setString(cnt++, st.getCheckNo());
		ps.setString(cnt++, st.getPayee());
		ps.setString(cnt++, st.getPaymentDesc());
		ps.setDouble(cnt++, st.getGross());
		ps.setDouble(cnt++, st.getNet());
		ps.setInt(cnt++, st.getSignatory1());
		ps.setInt(cnt++, st.getSignatory2());
		ps.setString(cnt++, st.getVoucherSeries());
		ps.setLong(cnt++, st.getResponsibility().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDate());
		LogU.add(st.getMonthGroup());
		LogU.add(st.getDepartment().getDepid());
		LogU.add(st.getAccounts().getBankId());
		LogU.add(st.getCheckNo());
		LogU.add(st.getPayee());
		LogU.add(st.getPaymentDesc());
		LogU.add(st.getGross());
		LogU.add(st.getNet());
		LogU.add(st.getSignatory1());
		LogU.add(st.getSignatory2());
		LogU.add(st.getVoucherSeries());
		LogU.add(st.getResponsibility().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to vrdata : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public void updateData(){
		String sql = "UPDATE vrdata SET "
				+ "vrdate=?,"
				+ "vrmonthgroup=?,"
				+ "departmentid=?,"
				+ "bankid=?,"
				+ "checkno=?,"
				+ "payee=?,"
				+ "paymentdesc=?,"
				+ "gross=?,"
				+ "net=?,"
				+ "sig1=?,"
				+ "sig2=?,"
				+ "vrcode=?,"
				+ "rid=? " 
				+ " WHERE vrid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table vrdata");
		
		
		ps.setString(cnt++, getDate());
		ps.setString(cnt++, getMonthGroup());
		ps.setInt(cnt++, getDepartment().getDepid());
		ps.setInt(cnt++, getAccounts().getBankId());
		ps.setString(cnt++, getCheckNo());
		ps.setString(cnt++, getPayee());
		ps.setString(cnt++, getPaymentDesc());
		ps.setDouble(cnt++, getGross());
		ps.setDouble(cnt++, getNet());
		ps.setInt(cnt++, getSignatory1());
		ps.setInt(cnt++, getSignatory2());
		ps.setString(cnt++, getVoucherSeries());
		ps.setLong(cnt++, getResponsibility().getId());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDate());
		LogU.add(getMonthGroup());
		LogU.add(getDepartment().getDepid());
		LogU.add(getAccounts().getBankId());
		LogU.add(getCheckNo());
		LogU.add(getPayee());
		LogU.add(getPaymentDesc());
		LogU.add(getGross());
		LogU.add(getNet());
		LogU.add(getSignatory1());
		LogU.add(getSignatory2());
		LogU.add(getVoucherSeries());
		LogU.add(getResponsibility().getId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to vrdata : " + s.getMessage());
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
		sql="SELECT vrid FROM vrdata  ORDER BY vrid DESC LIMIT 1";	
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
		ps = conn.prepareStatement("SELECT vrid FROM vrdata WHERE vrid=?");
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
		String sql = "UPDATE vrdata set isactivevr=0 WHERE vrid=?";
		
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
