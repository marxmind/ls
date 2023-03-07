package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
 * @version 1.0
 * @since 03/15/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class EmployeeMain {

	private long id;
	private String regDate;
	private String employeeId;
	private String firstName;
	private String middleName;
	private String lastName;
	private String fullName;
	private String birthDate;
	private int civilStatus;
	private String position;
	private String cctsId;
	private int employeeType;
	private String address;
	private int gender;
	private String contactNo;
	private String signatureid;
	private int isResigned;
	private int isActiveEmployee;
	private String emergecnyContactDtls;
	private Department department;
	private String photoid;
	private int rateType;
	private double rate;
	private int withHoldingTax;
	private String bloodType;
	private String dateResigned;
	
	
	private Date tempRegDate;
	private Date tempResDate;
	private Date tempBirthDate;
	
	private String typeName;
	
	public static String getLatestEmloyeeId(EmployeeType type) {
		String employeeNo = null;
		int year = DateUtils.getCurrentYear();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT employeeid FROM employee WHERE employeetype=" + type.getId() + " AND isactiveemployee=1 ORDER BY employeeid DESC LIMIT 1");
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			employeeNo = rs.getString("employeeid");
		}
		
		if(employeeNo==null) {
			employeeNo = type.getCode() + "-" + year + "-00001";
		}else {
			
			String[] vals = employeeNo.split("-");
			int tmpYear = Integer.valueOf(vals[1]);
			//if(year.equalsIgnoreCase(vals[1])) {
			if(year == tmpYear) {	
				int number = Integer.valueOf(vals[2]);
				number += 1; //add 1
				
				String newSeries = number + "";
				int len = newSeries.length();
				
				switch(len) {
					case 1 : employeeNo = type.getCode() + "-" + year + "-0000" + newSeries;  break;
					case 2 : employeeNo = type.getCode() + "-" + year + "-000" + newSeries; break;
					case 3 : employeeNo = type.getCode() + "-" + year + "-00" + newSeries; break;
					case 4 : employeeNo = type.getCode() + "-" + year + "-0" + newSeries; break;
					case 5 : employeeNo = type.getCode() + "-" + year + "-" + newSeries; break;
				}
				
				
			}else {//not equal to current year
				employeeNo = type.getCode() + "-" + year + "-00001";
			}
			
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return employeeNo;
	}
	
	public static List<EmployeeMain> retrieve(String sql, String[] params){
		List<EmployeeMain> emps = new ArrayList<EmployeeMain>();
		
		String tableEmp = "emp";
		String tableDep = "dep";
		
		String sqlTemp = "SELECT * FROM employee "+ tableEmp + ", department "+ tableDep +" WHERE " + tableEmp + ".isactiveemployee=1 " + 
		 " AND " + tableEmp + ".departmentid=" + tableDep + ".departmentid ";
		
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
		System.out.println("ORLISTING SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Department dep = new Department();
			try{dep.setDepid(rs.getInt("departmentid"));}catch(NullPointerException e){}
			try{dep.setDepartmentName(rs.getString("departmentname"));}catch(NullPointerException e){}
			try{dep.setCode(rs.getString("code"));}catch(NullPointerException e){}
			try{dep.setDepartmentHead(rs.getString("dephead"));}catch(NullPointerException e){}
			
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
					.typeName(EmployeeType.nameValue(rs.getInt("employeetype")))
					.address(rs.getString("address"))
					.gender(rs.getInt("gender"))
					.contactNo(rs.getString("contactno"))
					.signatureid(rs.getString("signatureid"))
					.isResigned(rs.getInt("isresigned"))
					.isActiveEmployee(rs.getInt("isactiveemployee"))
					.emergecnyContactDtls(rs.getString("emergencycontactdtls"))
					.photoid(rs.getString("photoid"))
					.department(dep)
					.rateType(rs.getInt("ratetype"))
					.rate(rs.getDouble("rate"))
					.withHoldingTax(rs.getInt("taxable"))
					.bloodType(rs.getString("bloodtype"))
					.dateResigned(rs.getString("resigned"))
					.build();
			
			emps.add(emp);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	public static EmployeeMain save(EmployeeMain st){
		if(st!=null){
			
			long id = EmployeeMain.getInfo(st.getId() ==0? EmployeeMain.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = EmployeeMain.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = EmployeeMain.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = EmployeeMain.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			EmployeeMain.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			EmployeeMain.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			EmployeeMain.insertData(this, "3");
		}
		
 }
	
	public static EmployeeMain insertData(EmployeeMain st, String type){
		String sql = "INSERT INTO employee ("
				+ "eid,"
				+ "regdate,"
				+ "employeeid,"
				+ "firstname,"
				+ "middlename,"
				+ "lastname,"
				+ "fullname,"
				+ "birthdate,"
				+ "civilstatus,"
				+ "empposition," 
				+ "departmentid,"
				+ "cctsid,"
				+ "employeetype,"
				+ "address,"
				+ "gender,"
				+ "contactno,"
				+ "signatureid,"
				+ "isresigned,"
				+ "isactiveemployee,"
				+ "emergencycontactdtls,"
				+ "photoid,"
				+ "ratetype,"
				+ "rate,"
				+ "taxable,"
				+ "bloodtype,"
				+ "resigned)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table employee");
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
		
		ps.setString(cnt++, st.getRegDate());
		ps.setString(cnt++, st.getEmployeeId());
		ps.setString(cnt++, st.getFirstName().toUpperCase());
		ps.setString(cnt++, st.getMiddleName().toUpperCase());
		ps.setString(cnt++, st.getLastName().toUpperCase());
		ps.setString(cnt++, st.getFullName().toUpperCase());
		ps.setString(cnt++, st.getBirthDate());
		ps.setInt(cnt++, st.getCivilStatus());
		ps.setString(cnt++, st.getPosition().toUpperCase());
		ps.setInt(cnt++, st.getDepartment().getDepid());
		ps.setString(cnt++, st.getCctsId());
		ps.setInt(cnt++, st.getEmployeeType());
		ps.setString(cnt++, st.getAddress().toUpperCase());
		ps.setInt(cnt++, st.getGender());
		ps.setString(cnt++, st.getContactNo());
		ps.setString(cnt++, st.getSignatureid());
		ps.setInt(cnt++, st.getIsResigned());
		ps.setInt(cnt++, st.getIsActiveEmployee());
		ps.setString(cnt++, st.getEmergecnyContactDtls());
		ps.setString(cnt++, st.getPhotoid());
		ps.setInt(cnt++, st.getRateType());
		ps.setDouble(cnt++, st.getRate());
		ps.setInt(cnt++, st.getWithHoldingTax());
		ps.setString(cnt++, st.getBloodType());
		ps.setString(cnt++, st.getDateResigned());
		
		LogU.add(st.getRegDate());
		LogU.add(st.getEmployeeId());
		LogU.add(st.getFirstName());
		LogU.add(st.getMiddleName());
		LogU.add(st.getLastName());
		LogU.add(st.getFullName());
		LogU.add(st.getBirthDate());
		LogU.add(st.getCivilStatus());
		LogU.add(st.getPosition());
		LogU.add(st.getDepartment().getDepid());
		LogU.add(st.getCctsId());
		LogU.add(st.getEmployeeType());
		LogU.add(st.getAddress());
		LogU.add(st.getGender());
		LogU.add(st.getContactNo());
		LogU.add(st.getSignatureid());
		LogU.add(st.getIsResigned());
		LogU.add(st.getIsActiveEmployee());
		LogU.add(st.getEmergecnyContactDtls());
		LogU.add(st.getPhotoid());
		LogU.add(st.getRateType());
		LogU.add(st.getRate());
		LogU.add(st.getWithHoldingTax());
		LogU.add(st.getBloodType());
		LogU.add(st.getDateResigned());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to employee : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static EmployeeMain updateData(EmployeeMain st){
		String sql = "UPDATE employee SET "
				+ "regdate=?,"
				+ "employeeid=?,"
				+ "firstname=?,"
				+ "middlename=?,"
				+ "lastname=?,"
				+ "fullname=?,"
				+ "birthdate=?,"
				+ "civilstatus=?,"
				+ "empposition=?,"
				+ "departmentid=?,"
				+ "cctsid=?,"
				+ "employeetype=?,"
				+ "address=?,"
				+ "gender=?,"
				+ "contactno=?,"
				+ "signatureid=?,"
				+ "isresigned=?,"
				+ "emergencycontactdtls=?,"
				+ "photoid=?,"
				+ "ratetype=?,"
				+ "rate=?,"
				+ "taxable=?,"
				+ "bloodtype=?,"
				+ "resigned=?" 
				+ " WHERE eid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updatibg data into table employee");
		
		
		ps.setString(cnt++, st.getRegDate());
		ps.setString(cnt++, st.getEmployeeId());
		ps.setString(cnt++, st.getFirstName().toUpperCase());
		ps.setString(cnt++, st.getMiddleName().toUpperCase());
		ps.setString(cnt++, st.getLastName().toUpperCase());
		ps.setString(cnt++, st.getFullName().toUpperCase());
		ps.setString(cnt++, st.getBirthDate());
		ps.setInt(cnt++, st.getCivilStatus());
		ps.setString(cnt++, st.getPosition().toUpperCase());
		ps.setInt(cnt++, st.getDepartment().getDepid());
		ps.setString(cnt++, st.getCctsId());
		ps.setInt(cnt++, st.getEmployeeType());
		ps.setString(cnt++, st.getAddress().toUpperCase());
		ps.setInt(cnt++, st.getGender());
		ps.setString(cnt++, st.getContactNo());
		ps.setString(cnt++, st.getSignatureid());
		ps.setInt(cnt++, st.getIsResigned());
		ps.setString(cnt++, st.getEmergecnyContactDtls());
		ps.setString(cnt++, st.getPhotoid());
		ps.setInt(cnt++, st.getRateType());
		ps.setDouble(cnt++, st.getRate());
		ps.setInt(cnt++, st.getWithHoldingTax());
		ps.setString(cnt++, st.getBloodType());
		ps.setString(cnt++, st.getDateResigned());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getRegDate());
		LogU.add(st.getEmployeeId());
		LogU.add(st.getFirstName());
		LogU.add(st.getMiddleName());
		LogU.add(st.getLastName());
		LogU.add(st.getFullName());
		LogU.add(st.getBirthDate());
		LogU.add(st.getCivilStatus());
		LogU.add(st.getPosition());
		LogU.add(st.getDepartment().getDepid());
		LogU.add(st.getCctsId());
		LogU.add(st.getEmployeeType());
		LogU.add(st.getAddress());
		LogU.add(st.getGender());
		LogU.add(st.getContactNo());
		LogU.add(st.getSignatureid());
		LogU.add(st.getIsResigned());
		LogU.add(st.getEmergecnyContactDtls());
		LogU.add(st.getPhotoid());
		LogU.add(st.getRateType());
		LogU.add(st.getRate());
		LogU.add(st.getWithHoldingTax());
		LogU.add(st.getBloodType());
		LogU.add(st.getDateResigned());
		LogU.add(st.getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully update...");
		}catch(SQLException s){
			LogU.add("error inserting data to employee : " + s.getMessage());
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
		sql="SELECT eid FROM employee ORDER BY eid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("eid");
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
		ps = conn.prepareStatement("SELECT eid FROM employee WHERE eid=?");
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
		String sql = "UPDATE employee set isactiveemployee=0 WHERE eid=?";
		
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