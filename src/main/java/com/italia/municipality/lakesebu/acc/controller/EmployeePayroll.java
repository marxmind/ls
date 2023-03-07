package com.italia.municipality.lakesebu.acc.controller;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.EmployeeMain;
import com.italia.municipality.lakesebu.controller.Payroll;
import com.italia.municipality.lakesebu.controller.PayrollFund;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@Builder
@ToString
public class EmployeePayroll {

	private long id;
	private String dateTrans;
	private String designation;
	private double rate;
	private double gross;
	private double net;
	private double ee;
	private double er;
	private double tax;
	private double coop;
	private double numberOfWork;
	private int month;
	private int year;
	private int isActive;
	private int status;
	private PayrollFund fund;
	//private PayrollGroupSeries group;
	private EmployeeMain employeeMain;
	private Payroll payroll;
	
	/**
	 * Check if the employee has already payroll created for the specific month
	 * @param month
	 * @param year
	 * @param employeeId
	 * @return
	 */
	public static boolean isExistPayroll(int month, int year, long employeeId) {
		boolean isExist = false;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT pid FROM employeepayroll WHERE isactivepay=1 AND monthperiod="+month + " AND yearperiod="+year + " AND eid="+employeeId);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return isExist;
	}
	
	public static List<EmployeePayroll> retrieve(String sql, String[] params){
		List<EmployeePayroll> emps = new ArrayList<EmployeePayroll>();
		
		String tableEmp = "emp";
		String tablePay = "py";
		String tableFund = "fn";
		
		String sqlTemp = "SELECT * FROM employeepayroll  "+ tablePay + ", employee "+ tableEmp +", payrollfund "+ tableFund +" WHERE " + tablePay + ".isactivepay=1 " + 
		 " AND " + tablePay + ".eid=" + tableEmp + ".eid AND "
		 		+ tablePay + ".fid=" + tableFund + ".fid ";
		
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
		System.out.println("employeepayroll SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			/*PayrollGroupSeries gp = PayrollGroupSeries.builder()
					.id(rs.getLong("gid"))
					.dateTrans(rs.getString("gdate"))
					.series(rs.getString("gseries"))
					.isActive(rs.getInt("isactiveg"))
					.status(rs.getInt("gstatus"))
					.build();*/
			PayrollFund fund = PayrollFund.builder()
					.id(rs.getLong("fid"))
					.name(rs.getString("fundname"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactivef"))
					.build();
			
			EmployeeMain emp = EmployeeMain.builder()
					.id(rs.getLong("eid"))
					.regDate(rs.getString("regdate"))
					.employeeId(rs.getString("employeeid"))
					.firstName(rs.getString("firstname"))
					.middleName(rs.getString("middlename"))
					.lastName(rs.getString("lastname"))
					.fullName(rs.getString("fullname"))
					.birthDate(rs.getString("birthdate"))
					.civilStatus(rs.getInt("civilstatus"))
					.position(rs.getString("empposition"))
					.cctsId(rs.getString("cctsid"))
					.employeeType(rs.getInt("employeetype"))
					.address(rs.getString("address"))
					.gender(rs.getInt("gender"))
					.contactNo(rs.getString("contactno"))
					.signatureid(rs.getString("signatureid"))
					.isResigned(rs.getInt("isresigned"))
					.isActiveEmployee(rs.getInt("isactiveemployee"))
					.emergecnyContactDtls(rs.getString("emergencycontactdtls"))
					.photoid(rs.getString("photoid"))
					.department(Department.builder().depid(rs.getInt("departmentid")).build())
					.rateType(rs.getInt("ratetype"))
					.rate(rs.getDouble("rate"))
					.withHoldingTax(rs.getInt("taxable"))
					.build();
			
			EmployeePayroll py = EmployeePayroll.builder()
					.id(rs.getLong("pyid"))
					.dateTrans(rs.getString("pydate"))
					.designation(rs.getString("designation"))
					.rate(rs.getDouble("pyrate"))
					.gross(rs.getDouble("pygross"))
					.net(rs.getDouble("pynet"))
					.ee(rs.getDouble("pyee"))
					.er(rs.getDouble("pyer"))
					.tax(rs.getDouble("pytax"))
					.coop(rs.getDouble("pycoop"))
					.numberOfWork(rs.getDouble("dayswork"))
					.month(rs.getInt("monthperiod"))
					.year(rs.getInt("yearperiod"))
					.isActive(rs.getInt("isactivepay"))
					.status(rs.getInt("pystatus"))
					.employeeMain(emp)
					.fund(fund)
					.payroll(Payroll.builder().id(rs.getLong("pid")).build())
					.build();
			
			emps.add(py);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	/**
	 * 
	 * @param sql
	 * @param params
	 * @return obj[tax],obj[gross],obj[net],obj[ee],obj[er],obj[coop] obj[List<EmployeePayroll>]
	 */
	public static Object[] retrieveData(String sql, String[] params){
		List<EmployeePayroll> emps = new ArrayList<EmployeePayroll>();
		Object[] obj = new Object[7];
		double tax=0d, gross=0d, net=0d,ee=0d,er=0d,coop=0d;
		String tableEmp = "emp";
		String tablePay = "py";
		String tableFund = "fn";
		
		String sqlTemp = "SELECT * FROM employeepayroll  "+ tablePay + ", employee "+ tableEmp +", payrollfund "+ tableFund +" WHERE " + tablePay + ".isactivepay=1 " + 
		 " AND " + tablePay + ".eid=" + tableEmp + ".eid AND "
		 		+ tablePay + ".fid=" + tableFund + ".fid ";
		
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
		System.out.println("employeepayroll SQL " + ps.toString());
		rs = ps.executeQuery();
		
		
		
		
		while(rs.next()){
			
			PayrollFund fund = PayrollFund.builder()
					.id(rs.getLong("fid"))
					.name(rs.getString("fundname"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("isactivef"))
					.build();
			
			EmployeeMain emp = EmployeeMain.builder()
					.id(rs.getLong("eid"))
					.regDate(rs.getString("regdate"))
					.employeeId(rs.getString("employeeid"))
					.firstName(rs.getString("firstname"))
					.middleName(rs.getString("middlename"))
					.lastName(rs.getString("lastname"))
					.fullName(rs.getString("fullname"))
					.birthDate(rs.getString("birthdate"))
					.civilStatus(rs.getInt("civilstatus"))
					.position(rs.getString("empposition"))
					.cctsId(rs.getString("cctsid"))
					.employeeType(rs.getInt("employeetype"))
					.address(rs.getString("address"))
					.gender(rs.getInt("gender"))
					.contactNo(rs.getString("contactno"))
					.signatureid(rs.getString("signatureid"))
					.isResigned(rs.getInt("isresigned"))
					.isActiveEmployee(rs.getInt("isactiveemployee"))
					.emergecnyContactDtls(rs.getString("emergencycontactdtls"))
					.photoid(rs.getString("photoid"))
					.department(Department.builder().depid(rs.getInt("departmentid")).build())
					.rateType(rs.getInt("ratetype"))
					.rate(rs.getDouble("rate"))
					.withHoldingTax(rs.getInt("taxable"))
					.build();
			
			EmployeePayroll py = EmployeePayroll.builder()
					.id(rs.getLong("pyid"))
					.dateTrans(rs.getString("pydate"))
					.designation(rs.getString("designation"))
					.rate(rs.getDouble("pyrate"))
					.gross(rs.getDouble("pygross"))
					.net(rs.getDouble("pynet"))
					.ee(rs.getDouble("pyee"))
					.er(rs.getDouble("pyer"))
					.tax(rs.getDouble("pytax"))
					.coop(rs.getDouble("pycoop"))
					.numberOfWork(rs.getDouble("dayswork"))
					.month(rs.getInt("monthperiod"))
					.year(rs.getInt("yearperiod"))
					.isActive(rs.getInt("isactivepay"))
					.status(rs.getInt("pystatus"))
					.employeeMain(emp)
					.fund(fund)
					.payroll(Payroll.builder().id(rs.getLong("pid")).build())
					.build();
			
			tax += rs.getDouble("pytax");
			gross += rs.getDouble("pygross");
			net += rs.getDouble("pynet");
			ee += rs.getDouble("pyee");
			er += rs.getDouble("pyer");
			coop += rs.getDouble("pycoop");
			
			emps.add(py);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		obj[0] = tax;
		obj[1] = gross;
		obj[2] = net;
		obj[3] = ee;
		obj[4] = er;
		obj[5] = coop;
		obj[6] = emps;
		
		return obj;
	}
	
	public static EmployeePayroll save(EmployeePayroll st){
		if(st!=null){
			
			long id = EmployeePayroll.getInfo(st.getId() ==0? EmployeePayroll.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = EmployeePayroll.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = EmployeePayroll.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = EmployeePayroll.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			EmployeePayroll.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			EmployeePayroll.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			EmployeePayroll.insertData(this, "3");
		}
		
	}
	
	public static EmployeePayroll insertData(EmployeePayroll st, String type){
		String sql = "INSERT INTO employeepayroll ("
				+ "pyid,"
				+ "pydate,"
				+ "designation,"
				+ "pyrate,"
				+ "pygross,"
				+ "pynet,"
				+ "pyee,"
				+ "pyer,"
				+ "pytax,"
				+ "pycoop," 
				+ "dayswork,"
				+ "monthperiod,"
				+ "yearperiod,"
				+ "isactivepay,"
				+ "pystatus,"
				+ "fid,"
				+ "eid,"
				+ "pid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table employeepayroll");
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
		ps.setString(cnt++, st.getDesignation());
		ps.setDouble(cnt++, st.getRate());
		ps.setDouble(cnt++, st.getGross());
		ps.setDouble(cnt++, st.getNet());
		ps.setDouble(cnt++, st.getEe());
		ps.setDouble(cnt++, st.getEr());
		ps.setDouble(cnt++, st.getTax());
		ps.setDouble(cnt++, st.getCoop());
		ps.setDouble(cnt++, st.getNumberOfWork());
		ps.setInt(cnt++, st.getMonth());
		ps.setInt(cnt++, st.getYear());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getStatus());
		ps.setLong(cnt++, st.getFund().getId());
		ps.setLong(cnt++, st.getEmployeeMain().getId());
		ps.setLong(cnt++, st.getPayroll().getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getDesignation());
		LogU.add(st.getRate());
		LogU.add(st.getGross());
		LogU.add(st.getNet());
		LogU.add(st.getEe());
		LogU.add(st.getEr());
		LogU.add(st.getTax());
		LogU.add(st.getCoop());
		LogU.add(st.getNumberOfWork());
		LogU.add(st.getMonth());
		LogU.add(st.getYear());
		LogU.add(st.getIsActive());
		LogU.add(st.getStatus());
		LogU.add(st.getFund().getId());
		LogU.add(st.getEmployeeMain().getId());
		LogU.add(st.getPayroll().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to employeepayroll : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static EmployeePayroll updateData(EmployeePayroll st){
		String sql = "UPDATE employeepayroll SET "
				+ "pydate=?,"
				+ "designation=?,"
				+ "pyrate=?,"
				+ "pygross=?,"
				+ "pynet=?,"
				+ "pyee=?,"
				+ "pyer=?,"
				+ "pytax=?,"
				+ "pycoop=?," 
				+ "dayswork=?,"
				+ "monthperiod=?,"
				+ "yearperiod=?,"
				+ "pystatus=?,"
				+ "fid=?,"
				+ "eid=?,"
				+ "pid=?" 
				+ " WHERE pyid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table employeepayroll");
		
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getDesignation());
		ps.setDouble(cnt++, st.getRate());
		ps.setDouble(cnt++, st.getGross());
		ps.setDouble(cnt++, st.getNet());
		ps.setDouble(cnt++, st.getEe());
		ps.setDouble(cnt++, st.getEr());
		ps.setDouble(cnt++, st.getTax());
		ps.setDouble(cnt++, st.getCoop());
		ps.setDouble(cnt++, st.getNumberOfWork());
		ps.setInt(cnt++, st.getMonth());
		ps.setInt(cnt++, st.getYear());
		ps.setInt(cnt++, st.getStatus());
		ps.setLong(cnt++, st.getFund().getId());
		ps.setLong(cnt++, st.getEmployeeMain().getId());
		ps.setLong(cnt++, st.getPayroll().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getDesignation());
		LogU.add(st.getRate());
		LogU.add(st.getGross());
		LogU.add(st.getNet());
		LogU.add(st.getEe());
		LogU.add(st.getEr());
		LogU.add(st.getTax());
		LogU.add(st.getCoop());
		LogU.add(st.getNumberOfWork());
		LogU.add(st.getMonth());
		LogU.add(st.getYear());
		LogU.add(st.getIsActive());
		LogU.add(st.getStatus());
		LogU.add(st.getFund().getId());
		LogU.add(st.getEmployeeMain().getId());
		LogU.add(st.getPayroll().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to employeepayroll : " + s.getMessage());
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
		sql="SELECT pyid FROM employeepayroll ORDER BY pyid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("pyid");
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
		ps = conn.prepareStatement("SELECT pyid FROM employeepayroll WHERE pyid=?");
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
		String sql = "UPDATE employeepayroll set isactivepay=0 WHERE pyid=?";
		
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