package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.utils.LogU;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @version 1.0
 * @since 07/05/2017
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Employee {

	private long id;
	private String dateRegistered;
	private String dateResigned;
	private String firstName;
	private String middleName;
	private String lastName;
	private int age;
	private int gender;
	private int isOfficial;
	private int isResigned;
	private int isActiveEmployee;
	private Timestamp timestamp;
	
	private String contactNo;
	private String purok;
	
	private Position position;
	private Committee committee;
	
	private com.italia.municipality.lakesebu.licensing.controller.Barangay barangay;
	private Municipality municipality;
	private Province province;
	
	private String genderName;
	
	private String fullName;
	
	public static Employee retrievePosition(int positionId){
		Employee emp = new Employee();
		
		String tableEmp = "emp";
		String tablePos = "pos";
		String tableCom = "com";
		String tableBg = "bg";
		String tableMun = "mu";
		String tablePrv = "prv";
		String sql = "SELECT * FROM employee " + tableEmp + ", empposition " + tablePos + ", committee " + tableCom + ", barangay " + tableBg + ", municipality " + tableMun + ", province " + tablePrv + " WHERE " +
				tableEmp +".posid=" + tablePos + ".posid AND " + 
				tableEmp + ".committeid="+ tableCom + ".committeid AND " +
				tableEmp + ".bgid=" + tableBg + ".bgid AND " +
				tableEmp + ".munid=" + tableMun + ".munid AND " +
				tableEmp + ".provid=" + tablePrv + ".provid AND emp.isactiveemp=1 AND emp.isresigned=0 AND pos.posid="+ positionId;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL: " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{emp.setId(rs.getLong("empid"));}catch(NullPointerException e){}
			try{emp.setDateRegistered(rs.getString("datereg"));}catch(NullPointerException e){}
			try{emp.setDateResigned(rs.getString("dateend"));}catch(NullPointerException e){}
			try{emp.setFirstName(rs.getString("firstname"));}catch(NullPointerException e){}
			try{emp.setMiddleName(rs.getString("middlename"));}catch(NullPointerException e){}
			try{emp.setLastName(rs.getString("lastname"));}catch(NullPointerException e){}
			try{emp.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{emp.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{emp.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			try{emp.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			try{emp.setContactNo(rs.getString("contactno"));}catch(NullPointerException e){}
			try{emp.setPurok(rs.getString("purok"));}catch(NullPointerException e){}
			try{emp.setIsActiveEmployee(rs.getInt("isactiveemp"));}catch(NullPointerException e){}
			try{emp.setFullName(rs.getString("firstname") + " " +rs.getString("lastname"));}catch(NullPointerException e){}
			
			emp.setGenderName(emp.getGender()==1? "Male" : "Female");
			
			Position pos = new Position();
			try{pos.setId(rs.getInt("posid"));}catch(NullPointerException e){}
			try{pos.setName(rs.getString("posname"));}catch(NullPointerException e){}
			try{pos.setIsAcativePosition(rs.getInt("isactivepos"));}catch(NullPointerException e){}
			emp.setPosition(pos);
			
			Committee com = new Committee();
			try{com.setId(rs.getInt("committeid"));}catch(NullPointerException e){}
			try{com.setName(rs.getString("comname"));}catch(NullPointerException e){}
			try{com.setIsActiveCommittee(rs.getInt("isactivecom"));}catch(NullPointerException e){}
			emp.setCommittee(com);
			
			com.italia.municipality.lakesebu.licensing.controller.Barangay bar = new com.italia.municipality.lakesebu.licensing.controller.Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			emp.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			emp.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			emp.setProvince(prov);
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return emp;
	}
	
	public static List<Employee> retrieve(String sqlAdd, String[] params){
		List<Employee> emps = new ArrayList<>();
		
		String tableEmp = "emp";
		String tablePos = "pos";
		String tableCom = "com";
		String tableBg = "bg";
		String tableMun = "mu";
		String tablePrv = "prv";
		String sql = "SELECT * FROM employee " + tableEmp + ", empposition " + tablePos + ", committee " + tableCom + ", barangay " + tableBg + ", municipality " + tableMun + ", province " + tablePrv + " WHERE " +
				tableEmp +".posid=" + tablePos + ".posid AND " + 
				tableEmp + ".committeid="+ tableCom + ".committeid AND " +
				tableEmp + ".bgid=" + tableBg + ".bgid AND " +
				tableEmp + ".munid=" + tableMun + ".munid AND " +
				tableEmp + ".provid=" + tablePrv + ".provid ";
		
		sql = sql + sqlAdd;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL: " + ps.toString());
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Employee emp = new Employee();
			try{emp.setId(rs.getLong("empid"));}catch(NullPointerException e){}
			try{emp.setDateRegistered(rs.getString("datereg"));}catch(NullPointerException e){}
			try{emp.setDateResigned(rs.getString("dateend"));}catch(NullPointerException e){}
			try{emp.setFirstName(rs.getString("firstname"));}catch(NullPointerException e){}
			try{emp.setMiddleName(rs.getString("middlename"));}catch(NullPointerException e){}
			try{emp.setLastName(rs.getString("lastname"));}catch(NullPointerException e){}
			try{emp.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{emp.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{emp.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			try{emp.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			try{emp.setContactNo(rs.getString("contactno"));}catch(NullPointerException e){}
			try{emp.setPurok(rs.getString("purok"));}catch(NullPointerException e){}
			try{emp.setIsActiveEmployee(rs.getInt("isactiveemp"));}catch(NullPointerException e){}
			try{emp.setFullName(rs.getString("firstname") + " " +rs.getString("lastname"));}catch(NullPointerException e){}
			emp.setGenderName(emp.getGender()==1? "Male" : "Female");
			
			Position pos = new Position();
			try{pos.setId(rs.getInt("posid"));}catch(NullPointerException e){}
			try{pos.setName(rs.getString("posname"));}catch(NullPointerException e){}
			try{pos.setIsAcativePosition(rs.getInt("isactivepos"));}catch(NullPointerException e){}
			emp.setPosition(pos);
			
			Committee com = new Committee();
			try{com.setId(rs.getInt("committeid"));}catch(NullPointerException e){}
			try{com.setName(rs.getString("comname"));}catch(NullPointerException e){}
			try{com.setIsActiveCommittee(rs.getInt("isactivecom"));}catch(NullPointerException e){}
			emp.setCommittee(com);
			
			com.italia.municipality.lakesebu.licensing.controller.Barangay bar = new com.italia.municipality.lakesebu.licensing.controller.Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			emp.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			emp.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			emp.setProvince(prov);
			
			emps.add(emp);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return emps;
	}
	
	public static Employee retrieve(long emplyeeId){
		Employee emp = new Employee();
		
		String tableEmp = "emp";
		String tablePos = "pos";
		String tableCom = "com";
		String tableBg = "bg";
		String tableMun = "mu";
		String tablePrv = "prv";
		String sql = "SELECT * FROM employee " + tableEmp + ", empposition " + tablePos + ", committee " + tableCom + ", barangay " + tableBg + ", municipality " + tableMun + ", province " + tablePrv + " WHERE " +
				tableEmp +".posid=" + tablePos + ".posid AND " + 
				tableEmp + ".committeid="+ tableCom + ".committeid AND " +
				tableEmp + ".bgid=" + tableBg + ".bgid AND " +
				tableEmp + ".munid=" + tableMun + ".munid AND " +
				tableEmp + ".provid=" + tablePrv + ".provid AND emp.isactiveemp=1 AND emp.empid="+ emplyeeId;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL: " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{emp.setId(rs.getLong("empid"));}catch(NullPointerException e){}
			try{emp.setDateRegistered(rs.getString("datereg"));}catch(NullPointerException e){}
			try{emp.setDateResigned(rs.getString("dateend"));}catch(NullPointerException e){}
			try{emp.setFirstName(rs.getString("firstname"));}catch(NullPointerException e){}
			try{emp.setMiddleName(rs.getString("middlename"));}catch(NullPointerException e){}
			try{emp.setLastName(rs.getString("lastname"));}catch(NullPointerException e){}
			try{emp.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{emp.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{emp.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			try{emp.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			try{emp.setContactNo(rs.getString("contactno"));}catch(NullPointerException e){}
			try{emp.setPurok(rs.getString("purok"));}catch(NullPointerException e){}
			try{emp.setIsActiveEmployee(rs.getInt("isactiveemp"));}catch(NullPointerException e){}
			try{emp.setFullName(rs.getString("firstname") + " " +rs.getString("lastname"));}catch(NullPointerException e){}
			emp.setGenderName(emp.getGender()==1? "Male" : "Female");
			
			Position pos = new Position();
			try{pos.setId(rs.getInt("posid"));}catch(NullPointerException e){}
			try{pos.setName(rs.getString("posname"));}catch(NullPointerException e){}
			try{pos.setIsAcativePosition(rs.getInt("isactivepos"));}catch(NullPointerException e){}
			emp.setPosition(pos);
			
			Committee com = new Committee();
			try{com.setId(rs.getInt("committeid"));}catch(NullPointerException e){}
			try{com.setName(rs.getString("comname"));}catch(NullPointerException e){}
			try{com.setIsActiveCommittee(rs.getInt("isactivecom"));}catch(NullPointerException e){}
			emp.setCommittee(com);
			
			com.italia.municipality.lakesebu.licensing.controller.Barangay bar = new com.italia.municipality.lakesebu.licensing.controller.Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			emp.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			emp.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			emp.setProvince(prov);
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return emp;
	}
	
	public static Employee employeeNamePosition(long id) {
		String sql = "SELECT * FROM employee e, empposition p WHERE e.posid=p.posid AND e.empid="+id+" AND e.isactiveemp=1";
		String[] params = new String[0];
		Employee emp = new Employee();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("employeePosition SQL: " + ps.toString());
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			try{emp.setId(rs.getLong("empid"));}catch(NullPointerException e){}
			try{emp.setDateRegistered(rs.getString("datereg"));}catch(NullPointerException e){}
			try{emp.setDateResigned(rs.getString("dateend"));}catch(NullPointerException e){}
			try{emp.setFirstName(rs.getString("firstname"));}catch(NullPointerException e){}
			try{emp.setMiddleName(rs.getString("middlename"));}catch(NullPointerException e){}
			try{emp.setLastName(rs.getString("lastname"));}catch(NullPointerException e){}
			try{emp.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{emp.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{emp.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			try{emp.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			try{emp.setContactNo(rs.getString("contactno"));}catch(NullPointerException e){}
			try{emp.setPurok(rs.getString("purok"));}catch(NullPointerException e){}
			try{emp.setIsActiveEmployee(rs.getInt("isactiveemp"));}catch(NullPointerException e){}
			try{emp.setFullName(rs.getString("firstname") + " " +rs.getString("lastname"));}catch(NullPointerException e){}
			emp.setGenderName(emp.getGender()==1? "Male" : "Female");
			
			Position pos = new Position();
			try{pos.setId(rs.getInt("posid"));}catch(NullPointerException e){}
			try{pos.setName(rs.getString("posname"));}catch(NullPointerException e){}
			try{pos.setIsAcativePosition(rs.getInt("isactivepos"));}catch(NullPointerException e){}
			emp.setPosition(pos);
			
			Committee com = new Committee();
			try{com.setId(rs.getInt("committeid"));}catch(NullPointerException e){}
			emp.setCommittee(com);
			
			com.italia.municipality.lakesebu.licensing.controller.Barangay bar = new com.italia.municipality.lakesebu.licensing.controller.Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			emp.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			emp.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			emp.setProvince(prov);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return emp;
	}
	
	public static Employee save(Employee emp){
		if(emp!=null){
			
			long id = Employee.getInfo(emp.getId() ==0? Employee.getLatestId()+1 : emp.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				emp = Employee.insertData(emp, "1");
			}else if(id==2){
				LogU.add("update Data ");
				emp = Employee.updateData(emp);
			}else if(id==3){
				LogU.add("added new Data ");
				emp = Employee.insertData(emp, "3");
			}
			
		}
		return emp;
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
	
	public static Employee insertData(Employee emp, String type){
		String sql = "INSERT INTO employee ("
				+ "empid,"
				+ "datereg,"
				+ "dateend,"
				+ "firstname,"
				+ "middlename,"
				+ "lastname,"
				+ "age,"
				+ "gender,"
				+ "isofficial,"
				+ "isresigned,"
				+ "isactiveemp,"
				+ "posid,"
				+ "committeid,"
				+ "contactno,"
				+ "purok,"
				+ "bgid,"
				+ "munid,"
				+ "provid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
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
			emp.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			emp.setId(id);
			LogU.add("id: " + id);
		}
		ps.setString(cnt++, emp.getDateRegistered());
		ps.setString(cnt++, emp.getDateResigned());
		ps.setString(cnt++, emp.getFirstName());
		ps.setString(cnt++, emp.getMiddleName());
		ps.setString(cnt++, emp.getLastName());
		ps.setInt(cnt++, emp.getAge());
		ps.setInt(cnt++, emp.getGender());
		ps.setInt(cnt++, emp.getIsOfficial());
		ps.setInt(cnt++, emp.getIsResigned());
		ps.setInt(cnt++, emp.getIsActiveEmployee());
		ps.setInt(cnt++, emp.getPosition()==null? 0 : emp.getPosition().getId());
		ps.setInt(cnt++, emp.getCommittee()==null? 0 : emp.getCommittee().getId());
		ps.setString(cnt++, emp.getContactNo());
		ps.setString(cnt++, emp.getPurok());
		ps.setInt(cnt++, emp.getBarangay()==null? 0 : emp.getBarangay().getId());
		ps.setInt(cnt++, emp.getMunicipality()==null? 0 : emp.getMunicipality().getId());
		ps.setInt(cnt++, emp.getProvince()==null? 0 : emp.getProvince().getId());
		
		LogU.add(emp.getDateRegistered());
		LogU.add(emp.getDateResigned());
		LogU.add(emp.getFirstName());
		LogU.add(emp.getMiddleName());
		LogU.add(emp.getLastName());
		LogU.add(emp.getAge());
		LogU.add(emp.getGender());
		LogU.add(emp.getIsOfficial());
		LogU.add(emp.getIsResigned());
		LogU.add(emp.getIsActiveEmployee());
		LogU.add(emp.getPosition()==null? 0 : emp.getPosition().getId());
		LogU.add(emp.getCommittee()==null? 0 : emp.getCommittee().getId());
		LogU.add(emp.getContactNo());
		LogU.add(emp.getPurok());
		LogU.add(emp.getBarangay()==null? 0 : emp.getBarangay().getId());
		LogU.add(emp.getMunicipality()==null? 0 : emp.getMunicipality().getId());
		LogU.add(emp.getProvince()==null? 0 : emp.getProvince().getId());
		
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
		return emp;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO employee ("
				+ "empid,"
				+ "datereg,"
				+ "dateend,"
				+ "firstname,"
				+ "middlename,"
				+ "lastname,"
				+ "age,"
				+ "gender,"
				+ "isofficial,"
				+ "isresigned,"
				+ "isactiveemp,"
				+ "posid,"
				+ "committeid,"
				+ "contactno,"
				+ "purok,"
				+ "bgid,"
				+ "munid,"
				+ "provid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
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
			setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			setId(id);
			LogU.add("id: " + id);
		}
		ps.setString(cnt++, getDateRegistered());
		ps.setString(cnt++, getDateResigned());
		ps.setString(cnt++, getFirstName());
		ps.setString(cnt++, getMiddleName());
		ps.setString(cnt++, getLastName());
		ps.setInt(cnt++, getAge());
		ps.setInt(cnt++, getGender());
		ps.setInt(cnt++, getIsOfficial());
		ps.setInt(cnt++, getIsResigned());
		ps.setInt(cnt++, getIsActiveEmployee());
		ps.setInt(cnt++, getPosition()==null? 0 : getPosition().getId());
		ps.setInt(cnt++, getCommittee()==null? 0 : getCommittee().getId());
		ps.setString(cnt++, getContactNo());
		ps.setString(cnt++, getPurok());
		ps.setInt(cnt++, getBarangay()==null? 0 : getBarangay().getId());
		ps.setInt(cnt++, getMunicipality()==null? 0 : getMunicipality().getId());
		ps.setInt(cnt++, getProvince()==null? 0 : getProvince().getId());
		
		LogU.add(getDateRegistered());
		LogU.add(getDateResigned());
		LogU.add(getFirstName());
		LogU.add(getMiddleName());
		LogU.add(getLastName());
		LogU.add(getAge());
		LogU.add(getGender());
		LogU.add(getIsOfficial());
		LogU.add(getIsResigned());
		LogU.add(getIsActiveEmployee());
		LogU.add(getPosition()==null? 0 : getPosition().getId());
		LogU.add(getCommittee()==null? 0 : getCommittee().getId());
		LogU.add(getContactNo());
		LogU.add(getPurok());
		LogU.add(getBarangay()==null? 0 : getBarangay().getId());
		LogU.add(getMunicipality()==null? 0 : getMunicipality().getId());
		LogU.add(getProvince()==null? 0 : getProvince().getId());
		
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
		
	}
	
	public static Employee updateData(Employee emp){
		String sql = "UPDATE employee SET "
				+ "datereg=?,"
				+ "dateend=?,"
				+ "firstname=?,"
				+ "middlename=?,"
				+ "lastname=?,"
				+ "age=?,"
				+ "gender=?,"
				+ "isofficial=?,"
				+ "isresigned=?,"
				+ "posid=?,"
				+ "committeid=?,"
				+ "contactno=?,"
				+ "purok=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=? " 
				+ " WHERE empid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table employee");
		
		ps.setString(cnt++, emp.getDateRegistered());
		ps.setString(cnt++, emp.getDateResigned());
		ps.setString(cnt++, emp.getFirstName());
		ps.setString(cnt++, emp.getMiddleName());
		ps.setString(cnt++, emp.getLastName());
		ps.setInt(cnt++, emp.getAge());
		ps.setInt(cnt++, emp.getGender());
		ps.setInt(cnt++, emp.getIsOfficial());
		ps.setInt(cnt++, emp.getIsResigned());
		ps.setInt(cnt++, emp.getPosition()==null? 0 : emp.getPosition().getId());
		ps.setInt(cnt++, emp.getCommittee()==null? 0 : emp.getCommittee().getId());
		ps.setString(cnt++, emp.getContactNo());
		ps.setString(cnt++, emp.getPurok());
		ps.setInt(cnt++, emp.getBarangay()==null? 0 : emp.getBarangay().getId());
		ps.setInt(cnt++, emp.getMunicipality()==null? 0 : emp.getMunicipality().getId());
		ps.setInt(cnt++, emp.getProvince()==null? 0 : emp.getProvince().getId());
		ps.setLong(cnt++, emp.getId());
		
		LogU.add(emp.getDateRegistered());
		LogU.add(emp.getDateResigned());
		LogU.add(emp.getFirstName());
		LogU.add(emp.getMiddleName());
		LogU.add(emp.getLastName());
		LogU.add(emp.getAge());
		LogU.add(emp.getGender());
		LogU.add(emp.getIsOfficial());
		LogU.add(emp.getIsResigned());
		LogU.add(emp.getPosition()==null? 0 : emp.getPosition().getId());
		LogU.add(emp.getCommittee()==null? 0 : emp.getCommittee().getId());
		LogU.add(emp.getContactNo());
		LogU.add(emp.getPurok());
		LogU.add(emp.getBarangay()==null? 0 : emp.getBarangay().getId());
		LogU.add(emp.getMunicipality()==null? 0 : emp.getMunicipality().getId());
		LogU.add(emp.getProvince()==null? 0 : emp.getProvince().getId());
		LogU.add(emp.getId());
		
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to employee : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return emp;
	}
	
	public void updateData(){
		String sql = "UPDATE employee SET "
				+ "datereg=?,"
				+ "dateend=?,"
				+ "firstname=?,"
				+ "middlename=?,"
				+ "lastname=?,"
				+ "age=?,"
				+ "gender=?,"
				+ "isofficial=?,"
				+ "isresigned=?,"
				+ "posid=?,"
				+ "committeid=?,"
				+ "contactno=?,"
				+ "purok=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=? " 
				+ " WHERE empid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table employee");
		
		ps.setString(cnt++, getDateRegistered());
		ps.setString(cnt++, getDateResigned());
		ps.setString(cnt++, getFirstName());
		ps.setString(cnt++, getMiddleName());
		ps.setString(cnt++, getLastName());
		ps.setInt(cnt++, getAge());
		ps.setInt(cnt++, getGender());
		ps.setInt(cnt++, getIsOfficial());
		ps.setInt(cnt++, getIsResigned());
		ps.setInt(cnt++, getPosition()==null? 0 : getPosition().getId());
		ps.setInt(cnt++, getCommittee()==null? 0 : getCommittee().getId());
		ps.setString(cnt++, getContactNo());
		ps.setString(cnt++, getPurok());
		ps.setInt(cnt++, getBarangay()==null? 0 : getBarangay().getId());
		ps.setInt(cnt++, getMunicipality()==null? 0 : getMunicipality().getId());
		ps.setInt(cnt++, getProvince()==null? 0 : getProvince().getId());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateRegistered());
		LogU.add(getDateResigned());
		LogU.add(getFirstName());
		LogU.add(getMiddleName());
		LogU.add(getLastName());
		LogU.add(getAge());
		LogU.add(getGender());
		LogU.add(getIsOfficial());
		LogU.add(getIsResigned());
		LogU.add(getPosition()==null? 0 : getPosition().getId());
		LogU.add(getCommittee()==null? 0 : getCommittee().getId());
		LogU.add(getContactNo());
		LogU.add(getPurok());
		LogU.add(getBarangay()==null? 0 : getBarangay().getId());
		LogU.add(getMunicipality()==null? 0 : getMunicipality().getId());
		LogU.add(getProvince()==null? 0 : getProvince().getId());
		LogU.add(getId());
		
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to employee : " + s.getMessage());
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
		sql="SELECT empid FROM employee  ORDER BY empid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("empid");
		}
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static Long getInfo(long id){
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
		ps = conn.prepareStatement("SELECT empid FROM employee WHERE empid=?");
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
		String sql = "UPDATE employee set isactiveemp=0 WHERE empid=?";
		
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