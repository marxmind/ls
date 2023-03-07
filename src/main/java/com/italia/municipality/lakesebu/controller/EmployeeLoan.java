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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 04/25/2022
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class EmployeeLoan {

	private long id;
	private String approvedDate;
	private String name;
	private String remarks;
	private double loanAmount;
	private double monthlyDeduction;
	private int isCompleted;
	private int isActive;
	private EmployeeMain employeeMain;
	
	private Date tempApprovedDate;
	private List completes;
	
	private String status;
	
	public static List<EmployeeLoan> retrieve(String sql, String[] params){
		List<EmployeeLoan> loans = new ArrayList<EmployeeLoan>();
		
		String tableLoan = "loan";
		String tableEmp = "emp";
		
		String sqlTemp = "SELECT * FROM emploan "+ tableLoan  +", employee "+ tableEmp +" WHERE " + tableLoan + ".isactiveloan=1 " +
				" AND " + tableLoan +".eid=" +  tableEmp + ".eid";
		
		
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
					.rateType(rs.getInt("ratetype"))
					.rate(rs.getDouble("rate"))
					.withHoldingTax(rs.getInt("taxable"))
					.build();
			
			EmployeeLoan loan = EmployeeLoan.builder()
					.id(rs.getLong("lid"))
					.name(rs.getString("loanname"))
					.approvedDate(rs.getString("dateapproved"))
					.remarks(rs.getString("remarks"))
					.loanAmount(rs.getDouble("loanamount"))
					.monthlyDeduction(rs.getDouble("monthlydeduction"))
					.isCompleted(rs.getInt("iscompleted"))
					.isActive(rs.getInt("isactiveloan"))
					.status(rs.getInt("iscompleted")==1? "In-Flight" : "Completed")
					.employeeMain(emp)
					.build();
			
			loans.add(loan);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return loans;
	}
	
	public static EmployeeLoan save(EmployeeLoan st){
		if(st!=null){
			
			long id = EmployeeLoan.getInfo(st.getId() ==0? EmployeeLoan.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = EmployeeLoan.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = EmployeeLoan.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = EmployeeLoan.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			EmployeeLoan.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			EmployeeLoan.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			EmployeeLoan.insertData(this, "3");
		}
		
 }
	
	public static EmployeeLoan insertData(EmployeeLoan st, String type){
		String sql = "INSERT INTO emploan ("
				+ "lid,"
				+ "loanname,"
				+ "dateapproved,"
				+ "remarks,"
				+ "loanamount,"
				+ "monthlydeduction,"
				+ "iscompleted,"
				+ "isactiveloan,"
				+ "eid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table emploanv");
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
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getApprovedDate());
		ps.setString(cnt++, st.getRemarks());
		ps.setDouble(cnt++, st.getLoanAmount());
		ps.setDouble(cnt++, st.getMonthlyDeduction());
		ps.setInt(cnt++, st.getIsCompleted());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getEmployeeMain().getId());
		
		LogU.add(st.getName());
		LogU.add(st.getApprovedDate());
		LogU.add(st.getRemarks());
		LogU.add(st.getLoanAmount());
		LogU.add(st.getMonthlyDeduction());
		LogU.add(st.getIsCompleted());
		LogU.add(st.getIsActive());
		LogU.add(st.getEmployeeMain().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to emploan : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static EmployeeLoan updateData(EmployeeLoan st){
		String sql = "UPDATE emploan SET "
				+ "loanname=?,"
				+ "dateapproved=?,"
				+ "remarks=?,"
				+ "loanamount=?,"
				+ "monthlydeduction=?,"
				+ "iscompleted=?,"
				+ "eid=?" 
				+ " WHERE lid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table emploan");
		
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getApprovedDate());
		ps.setString(cnt++, st.getRemarks());
		ps.setDouble(cnt++, st.getLoanAmount());
		ps.setDouble(cnt++, st.getMonthlyDeduction());
		ps.setInt(cnt++, st.getIsCompleted());
		ps.setLong(cnt++, st.getEmployeeMain().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getApprovedDate());
		LogU.add(st.getRemarks());
		LogU.add(st.getLoanAmount());
		LogU.add(st.getMonthlyDeduction());
		LogU.add(st.getIsCompleted());
		LogU.add(st.getEmployeeMain().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to emploan : " + s.getMessage());
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
		sql="SELECT lid FROM emploan ORDER BY lid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("lid");
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
		ps = conn.prepareStatement("SELECT lid FROM emploan WHERE lid=?");
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
		String sql = "UPDATE emploan set isactiveloan=0 WHERE lid=?";
		
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

