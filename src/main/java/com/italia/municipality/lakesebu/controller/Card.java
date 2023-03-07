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
 * @since 03/15/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class Card {

	private long id;
	private String name;
	private String number;
	private String validFrom;
	private String validTo;
	private int isActive;
	private EmployeeMain employee;
	
	private Date tempDateFrom;
	private Date tempDateTo;
	
	public static List<Card> retrieve(String sql, String[] params){
		List<Card> cards = new ArrayList<Card>();
		
		String tableCard = "card";
		String tableEmp = "emp";
		
		String sqlTemp = "SELECT * FROM employeecard "+ tableCard  +", employee "+ tableEmp +" WHERE " + tableCard + ".isactivecard=1 " +
				" AND " + tableCard +".eid=" +  tableEmp + ".eid";
		
		
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
		System.out.println("Card SQL " + ps.toString());
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
			
			Card card = Card.builder()
					.id(rs.getLong("cardid"))
					.name(rs.getString("cardname"))
					.number(rs.getString("cardno"))
					.validFrom(rs.getString("validfrom"))
					.validTo(rs.getString("validto"))
					.isActive(rs.getInt("isactivecard"))
					.employee(emp)
					.build();
			
			cards.add(card);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return cards;
	}
	
	public static Card save(Card st){
		if(st!=null){
			
			long id = Card.getInfo(st.getId() ==0? Card.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Card.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Card.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Card.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Card.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Card.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Card.insertData(this, "3");
		}
		
 }
	
	public static Card insertData(Card st, String type){
		String sql = "INSERT INTO employeecard ("
				+ "cardid,"
				+ "cardname,"
				+ "cardno,"
				+ "validfrom,"
				+ "validto,"
				+ "isactivecard,"
				+ "eid)" 
				+ " VALUES(?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table employeecard");
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
		ps.setString(cnt++, st.getNumber());
		ps.setString(cnt++, st.getValidFrom());
		ps.setString(cnt++, st.getValidTo());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getEmployee().getId());
		
		LogU.add(st.getName());
		LogU.add(st.getNumber());
		LogU.add(st.getValidFrom());
		LogU.add(st.getValidTo());
		LogU.add(st.getIsActive());
		LogU.add(st.getEmployee().getId());
		
		
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
	
	public static Card updateData(Card st){
		String sql = "UPDATE employeecard SET "
				+ "cardname=?,"
				+ "cardno=?,"
				+ "validfrom=?,"
				+ "validto=?,"
				+ "eid=?" 
				+ " WHERE cardid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table employeecard");
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getNumber());
		ps.setString(cnt++, st.getValidFrom());
		ps.setString(cnt++, st.getValidTo());
		ps.setLong(cnt++, st.getEmployee().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getNumber());
		LogU.add(st.getValidFrom());
		LogU.add(st.getValidTo());
		LogU.add(st.getEmployee().getId());
		LogU.add(st.getId());
		
		
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
	
	/*
	 * public static EmployeeMain updateData(EmployeeMain st){ String sql =
	 * "UPDATE employee SET " + "regdate=?," + "employeeid=?," + "firstname=?," +
	 * "middlename=?," + "lastname=?," + "fullname=?," + "birthdate=?," +
	 * "civilstatus=?," + "empposition=?" + "departmentid=?," + "cctsid=?," +
	 * "employeetype=?," + "address=?," + "gender=?," + "contactno=?," +
	 * "signatureid=?," + "isresigned=?," + "emergencycontactdtls=?" +
	 * " WHERE eid=?";
	 * 
	 * PreparedStatement ps = null; Connection conn = null;
	 * 
	 * try{ conn = WebTISDatabaseConnect.getConnection(); ps =
	 * conn.prepareStatement(sql);
	 * 
	 * int cnt = 1;
	 * LogU.add("===========================START=========================");
	 * LogU.add("updatibg data into table employee");
	 * 
	 * 
	 * ps.setString(cnt++, st.getRegDate()); ps.setString(cnt++,
	 * st.getEmployeeId()); ps.setString(cnt++, st.getFirstName());
	 * ps.setString(cnt++, st.getMiddleName()); ps.setString(cnt++,
	 * st.getLastName()); ps.setString(cnt++, st.getFullName()); ps.setString(cnt++,
	 * st.getBirthDate()); ps.setInt(cnt++, st.getCivilStatus());
	 * ps.setString(cnt++, st.getPosition()); ps.setInt(cnt++,
	 * st.getDepartment().getDepid()); ps.setString(cnt++, st.getCctsId());
	 * ps.setInt(cnt++, st.getEmployeeType()); ps.setString(cnt++, st.getAddress());
	 * ps.setInt(cnt++, st.getGender()); ps.setString(cnt++, st.getContactNo());
	 * ps.setString(cnt++, st.getSignatureid()); ps.setInt(cnt++,
	 * st.getIsResigned()); ps.setString(cnt++, st.getEmergecnyContactDtls());
	 * ps.setLong(cnt++, st.getId());
	 * 
	 * LogU.add(st.getRegDate()); LogU.add(st.getEmployeeId());
	 * LogU.add(st.getFirstName()); LogU.add(st.getMiddleName());
	 * LogU.add(st.getLastName()); LogU.add(st.getFullName());
	 * LogU.add(st.getBirthDate()); LogU.add(st.getCivilStatus());
	 * LogU.add(st.getPosition()); LogU.add(st.getDepartment().getDepid());
	 * LogU.add(st.getCctsId()); LogU.add(st.getEmployeeType());
	 * LogU.add(st.getAddress()); LogU.add(st.getGender());
	 * LogU.add(st.getContactNo()); LogU.add(st.getSignatureid());
	 * LogU.add(st.getIsResigned()); LogU.add(st.getEmergecnyContactDtls());
	 * LogU.add(st.getId());
	 * 
	 * 
	 * LogU.add("executing for saving..."); ps.execute(); LogU.add("closing...");
	 * ps.close(); WebTISDatabaseConnect.close(conn);
	 * LogU.add("data has been successfully update..."); }catch(SQLException s){
	 * LogU.add("error inserting data to employee : " + s.getMessage()); }
	 * LogU.add("===========================END========================="); return
	 * st; }
	 */
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT cardid FROM employeecard ORDER BY cardid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("cardid");
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
		ps = conn.prepareStatement("SELECT cardid FROM employeecard WHERE cardid=?");
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
		String sql = "UPDATE employeecard set isactivecard=0 WHERE cardid=?";
		
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