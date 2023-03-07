package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.acc.controller.EmployeePayroll;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.EmployeeType;
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
 * @since 06/14/2022
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Payroll {

	private long id;
	private String series;
	private String dateTrans;
	private PayrollApprover supervisor;
	private PayrollApprover approver;
	private PayrollApprover treasurer;
	private PayrollApprover accountant;
	private PayrollApprover disbursing;
	private String ids;
	private double grossTotal;
	private double eeTotal;
	private double loanTotal;
	private double coopTotal;
	private double taxTotal;
	private double netTotal;
	private int isActive;
	private PayrollFund fund;
	
	public static String getLatestSeriesId(long fundType) {
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
		ps = conn.prepareStatement("SELECT pseries FROM payroll WHERE fid=" + fundType + " AND isactivep=1 ORDER BY pid DESC LIMIT 1");
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			employeeNo = rs.getString("pseries");
		}
		
		if(employeeNo==null) {
			employeeNo = payCode+fundType + "-" + year +"-"+ month +"-00001";
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
					case 1 : employeeNo = payCode+fundType + "-" + year +"-"+ month + "-0000" + newSeries;  break;
					case 2 : employeeNo = payCode+fundType + "-" + year +"-"+ month + "-000" + newSeries; break;
					case 3 : employeeNo = payCode+fundType + "-" + year +"-"+ month + "-00" + newSeries; break;
					case 4 : employeeNo = payCode+fundType + "-" + year +"-"+ month + "-0" + newSeries; break;
					case 5 : employeeNo = payCode+fundType + "-" + year +"-"+ month + "-" + newSeries; break;
				}
				
				
			}else {//not equal to current year
				employeeNo = payCode+fundType + "-" + year +"-"+ month +"-00001";
			}
			
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return employeeNo;
	}
	
	/**
	 * Rollback payroll created
	 * @param pay
	 * @return status 0 - draft
	 */
	public static boolean rollbackPayroll(Payroll pay) {
		boolean isSuccess = false;
		
		for(EmployeePayroll py : Payroll.retrieveAll(pay.getIds())) {
			py.setStatus(0);
			py.save();
			isSuccess = true;
		}
		
		return isSuccess;
	}
	
	
	public static List<EmployeePayroll> retrieveAll(String ids){
		List<EmployeePayroll> pays = new ArrayList<EmployeePayroll>();
		
		String sql = "";
		int cnt = 0;
		if(ids.contains(":")) {
			
			sql =	" AND (";
			
			for(String id : ids.split(":")) {
				if(cnt==0) {
					sql += " py.pyid="+id;
				}else {
					sql += " OR py.pyid="+id;
				}
				
				
				cnt++;
			}
			sql += " )";
			
		}else {
			sql += " AND py.pyid="+ids;
			cnt++;
		}
		
		
		if(cnt==0) {
			sql="";//reset
		}else {
			pays = EmployeePayroll.retrieve(sql, new String[0]);
		}
		
		return pays;
	}
	
	public static List<Payroll> retrieve(String sql, String[] params){
		List<Payroll> pays = new ArrayList<Payroll>();
		
		Map<Long, PayrollFund> fund = PayrollFund.retrieveAll();
		Map<Long, PayrollApprover> approver = PayrollApprover.retrieveAll();
		
		
		String sqlTemp = "SELECT * FROM payroll WHERE isactivep=1 ";
		 
		
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
		System.out.println("payrollapprover SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Payroll app = Payroll.builder()
					.id(rs.getLong("pid"))
					.series(rs.getString("pseries"))
					.dateTrans(rs.getString("pdate"))
					.supervisor(approver.get(rs.getLong("supervisor")))
					.approver(approver.get(rs.getLong("approver")))
					.treasurer(approver.get(rs.getLong("treasurer")))
					.disbursing(approver.get(rs.getLong("disbursing")))
					.accountant(approver.get(rs.getLong("accountant")))
					.ids(rs.getString("pyids"))
					.grossTotal(rs.getDouble("gross"))
					.eeTotal(rs.getDouble("ee"))
					.loanTotal(rs.getDouble("loan"))
					.taxTotal(rs.getDouble("tax"))
					.coopTotal(rs.getDouble("coop"))
					.netTotal(rs.getDouble("net"))
					.isActive(rs.getInt("isactivep"))
					.fund(fund.get(rs.getLong("fid")))
					.build();
			
			
			pays.add(app);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return pays;
	}
	
	public static Payroll save(Payroll st){
		if(st!=null){
			
			long id = Payroll.getInfo(st.getId() ==0? Payroll.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Payroll.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Payroll.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Payroll.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Payroll.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Payroll.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Payroll.insertData(this, "3");
		}
		
	}
	
	public static Payroll insertData(Payroll st, String type){
		String sql = "INSERT INTO payroll ("
				+ "pid,"
				+ "pseries,"
				+ "pdate,"
				+ "supervisor,"
				+ "approver,"
				+ "treasurer,"
				+ "disbursing,"
				+ "accountant,"
				+ "pyids,"
				+ "gross,"
				+ "ee,"
				+ "loan,"
				+ "tax,"
				+ "coop,"
				+ "net,"
				+ "isactivep,"
				+ "fid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table payroll");
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
		
		ps.setString(cnt++, st.getSeries());
		ps.setString(cnt++, st.getDateTrans());
		ps.setLong(cnt++, st.getSupervisor().getId());
		ps.setLong(cnt++, st.getApprover().getId());
		ps.setLong(cnt++, st.getTreasurer().getId());
		ps.setLong(cnt++, st.getDisbursing().getId());
		ps.setLong(cnt++, st.getAccountant().getId());
		ps.setString(cnt++, st.getIds());
		ps.setDouble(cnt++, st.getGrossTotal());
		ps.setDouble(cnt++, st.getEeTotal());
		ps.setDouble(cnt++, st.getLoanTotal());
		ps.setDouble(cnt++, st.getTaxTotal());
		ps.setDouble(cnt++, st.getCoopTotal());
		ps.setDouble(cnt++, st.getNetTotal());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getFund().getId());
		
		LogU.add(st.getSeries());
		LogU.add(st.getDateTrans());
		LogU.add(st.getSupervisor().getId());
		LogU.add(st.getApprover().getId());
		LogU.add(st.getTreasurer().getId());
		LogU.add(st.getDisbursing().getId());
		LogU.add(st.getAccountant().getId());
		LogU.add(st.getIds());
		LogU.add(st.getGrossTotal());
		LogU.add(st.getEeTotal());
		LogU.add(st.getLoanTotal());
		LogU.add(st.getTaxTotal());
		LogU.add(st.getCoopTotal());
		LogU.add(st.getNetTotal());
		LogU.add(st.getIsActive());
		LogU.add(st.getFund().getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to payroll : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static Payroll updateData(Payroll st){
		String sql = "UPDATE payroll SET "
				+ "pseries=?,"
				+ "pdate=?,"
				+ "supervisor=?,"
				+ "approver=?,"
				+ "treasurer=?,"
				+ "disbursing=?,"
				+ "accountant=?,"
				+ "pyids=?,"
				+ "gross=?,"
				+ "ee=?,"
				+ "loan=?,"
				+ "tax=?,"
				+ "coop=?,"
				+ "net=?,"
				+ "fid=?)" 
				+ " WHERE pid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table payroll");
		
		
		ps.setString(cnt++, st.getSeries());
		ps.setString(cnt++, st.getDateTrans());
		ps.setLong(cnt++, st.getSupervisor().getId());
		ps.setLong(cnt++, st.getApprover().getId());
		ps.setLong(cnt++, st.getTreasurer().getId());
		ps.setLong(cnt++, st.getDisbursing().getId());
		ps.setLong(cnt++, st.getAccountant().getId());
		ps.setString(cnt++, st.getIds());
		ps.setDouble(cnt++, st.getGrossTotal());
		ps.setDouble(cnt++, st.getEeTotal());
		ps.setDouble(cnt++, st.getLoanTotal());
		ps.setDouble(cnt++, st.getTaxTotal());
		ps.setDouble(cnt++, st.getCoopTotal());
		ps.setDouble(cnt++, st.getNetTotal());
		ps.setLong(cnt++, st.getFund().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getSeries());
		LogU.add(st.getDateTrans());
		LogU.add(st.getSupervisor().getId());
		LogU.add(st.getApprover().getId());
		LogU.add(st.getTreasurer().getId());
		LogU.add(st.getDisbursing().getId());
		LogU.add(st.getAccountant().getId());
		LogU.add(st.getIds());
		LogU.add(st.getGrossTotal());
		LogU.add(st.getEeTotal());
		LogU.add(st.getLoanTotal());
		LogU.add(st.getTaxTotal());
		LogU.add(st.getCoopTotal());
		LogU.add(st.getNetTotal());
		LogU.add(st.getFund().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to payroll : " + s.getMessage());
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
		sql="SELECT pid FROM payroll ORDER BY pid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("pid");
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
		ps = conn.prepareStatement("SELECT pid FROM payroll WHERE pid=?");
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
		String sql = "UPDATE payroll set isactivep=0 WHERE pid=?";
		
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
