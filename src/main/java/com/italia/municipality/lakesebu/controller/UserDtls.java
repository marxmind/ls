package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.bean.SessionBean;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 09/27/2016
 * @version 1.0
 * 
 * @since 10/14/2021
 * @version 2.0
 * @apiNote adapting lombok annotation
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class UserDtls {

	private long userdtlsid;
	private String regdate;
	private String firstname;
	private String middlename;
	private String lastname;
	private String address;
	private String contactno;
	private int age;
	private int gender;
	private Login login;
	private Job job;
	private Department department;
	private UserDtls userDtls;
	private Timestamp timestamp;
	private int isActive;
	
	
	public static String userSQL(String tablename,UserDtls user){
		String sql= " AND "+ tablename +".isactive=" + user.getIsActive();
		if(user!=null){
			
			if(user.getUserdtlsid()!=0){
				sql += " AND "+ tablename +".userdtlsid=" + user.getUserdtlsid();
			}
			if(user.getRegdate()!=null){
				sql += " AND "+ tablename +".regdate like '%" + user.getRegdate()+"%'";
			}
			if(user.getFirstname()!=null){
				sql += " AND "+ tablename +".firstname like '%" + user.getFirstname()+"%'";
			}
			if(user.getMiddlename()!=null){
				sql += " AND "+ tablename +".middlename like '%" + user.getMiddlename()+"%'";
			}
			if(user.getLastname()!=null){
				sql += " AND "+ tablename +".lastname like '%" + user.getLastname()+"%'";
			}
			if(user.getAddress()!=null){
				sql += " AND "+ tablename +".address like '%" + user.getAddress()+"%'";
			}
			if(user.getContactno()!=null){
				sql += " AND "+ tablename +".contactno like'%" + user.getContactno()+"%'";
			}
			if(user.getAge()!=0){
				sql += " AND "+ tablename +".age=" + user.getAge();
			}
			if(user.getGender()!=0){
				sql += " AND "+ tablename +".gender=" + user.getGender();
			}
			
		}
		
		return sql;
	}
	
	public static List<UserDtls> retrieve(String sql, String[] params){
		List<UserDtls> users = new ArrayList<UserDtls>();
		
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
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setRegdate(rs.getString("regdate"));;}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setLogin(Login.login(rs.getString("logid")));}catch(NullPointerException e){}
			try{user.setJob(Job.job(rs.getString("jobtitleid")));}catch(NullPointerException e){}
			try{user.setDepartment(Department.department(rs.getString("departmentid")));}catch(NullPointerException e){}
			try{user.setUserDtls(UserDtls.addedby(rs.getString("addedby")));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			users.add(user);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return users;
	}
	
	public static UserDtls retrieveUserPositon(int jobId){
		UserDtls user = new UserDtls();
		String sql = "SELECT * FROM userdtls WHERE  isactive=1 AND jobtitleid=?";
		String[] params = new String[1];
		params[0] = jobId+"";
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
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setRegdate(rs.getString("regdate"));;}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setLogin(Login.login(rs.getString("logid")));}catch(NullPointerException e){}
			try{user.setJob(Job.job(rs.getString("jobtitleid")));}catch(NullPointerException e){}
			try{user.setDepartment(Department.department(rs.getString("departmentid")));}catch(NullPointerException e){}
			try{user.setUserDtls(UserDtls.addedby(rs.getString("addedby")));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return user;
	}
	
	public static UserDtls addedby(String userdtlsid){
		UserDtls user = new UserDtls();
		String sql = "SELECT * FROM userdtls WHERE userdtlsid=?";
		String[] params = new String[1];
		params[0] = userdtlsid;
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
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
				try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
				try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
				try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
				try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
				try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
				try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
				try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
				try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
				try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
				try{user.setJob(Job.job(rs.getString("jobtitleid")));}catch(NullPointerException e){}
				
				
			}
			
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e){}
		return user;
	}
	
	@Deprecated
	public static UserDtls user(String userdtlsid){
		UserDtls user = new UserDtls();
		String sql = "SELECT * FROM userdtls WHERE userdtlsid=?";
		String[] params = new String[1];
		params[0] = userdtlsid;
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
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
				try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
				try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
				try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
				try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
				try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
				try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
				try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
				try{user.setLogin(Login.login(rs.getString("logid")));}catch(NullPointerException e){}
				try{user.setJob(Job.job(rs.getString("jobtitleid")));}catch(NullPointerException e){}
				try{user.setDepartment(Department.department(rs.getString("departmentid")));}catch(NullPointerException e){}
				try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
				try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			}
			
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e){}
		return user;
	}
	
	public static UserDtls getUser(String userdtlsid){
		UserDtls user = new UserDtls();
		String sql = "SELECT * FROM userdtls WHERE userdtlsid=?";
		String[] params = new String[1];
		params[0] = userdtlsid;
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
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
				try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
				try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
				try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
				try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
				try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
				try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
				try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
				try{user.setLogin(Login.login(rs.getString("logid")));}catch(NullPointerException e){}
				try{user.setJob(Job.job(rs.getString("jobtitleid")));}catch(NullPointerException e){}
				try{user.setDepartment(Department.department(rs.getString("departmentid")));}catch(NullPointerException e){}
				try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
				try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			}
			
			
		}catch(Exception e){}
		return user;
	}
	
	public static void save(UserDtls user){
		if(user!=null){
			
			long id = UserDtls.getInfo(user.getUserdtlsid() ==0? UserDtls.getLatestId()+1 : user.getUserdtlsid());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				UserDtls.insertData(user, "1");
			}else if(id==2){
				LogU.add("update Data ");
				UserDtls.updateData(user);
			}else if(id==3){
				LogU.add("added new Data ");
				UserDtls.insertData(user, "3");
			}
			
		}
	}
	
	public void save(){
			
			long id = UserDtls.getInfo(getUserdtlsid() ==0? getLatestId()+1 : getUserdtlsid());
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
	
	public static UserDtls insertData(UserDtls user, String type){
		String sql = "INSERT INTO userdtls ("
				+ "userdtlsid,"
				+ "firstname,"
				+ "middlename,"
				+ "lastname,"
				+ "address,"
				+ "contactno,"
				+ "age,"
				+ "gender,"
				+ "logid,"
				+ "jobtitleid,"
				+ "departmentid,"
				+ "addedby,"
				+ "isactive,"
				+ "regdate)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table userdtls");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			user.setUserdtlsid(Long.valueOf(id));
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			user.setUserdtlsid(Long.valueOf(id));
			LogU.add("id: " + id);
		}
		
		ps.setString(2, user.getFirstname());
		ps.setString(3, user.getMiddlename());
		ps.setString(4, user.getLastname());
		ps.setString(5, user.getAddress());
		ps.setString(6, user.getContactno());
		ps.setInt(7, user.getAge());
		ps.setInt(8, user.getGender());
		ps.setLong(9, user.getLogin().getLogid());
		ps.setInt(10, user.getJob()==null? 0 : (user.getJob().getJobid()==0? 0 : user.getJob().getJobid()));
		ps.setInt(11, user.getDepartment()==null? 0 : (user.getDepartment().getDepid()==0? 0 : user.getDepartment().getDepid()));
		ps.setLong(12, user.getUserDtls()==null? 0l : (user.getUserDtls().getUserdtlsid()==0l? 0l : user.getUserDtls().getUserdtlsid()));
		ps.setInt(13, user.getIsActive());
		ps.setString(14, user.getRegdate());
		
		LogU.add( user.getFirstname());
		LogU.add( user.getMiddlename());
		LogU.add(user.getLastname());
		LogU.add(user.getAddress());
		LogU.add(user.getContactno());
		LogU.add(user.getAge());
		LogU.add(user.getGender());
		LogU.add(user.getLogin().getLogid());
		LogU.add(user.getJob()==null? 0 : (user.getJob().getJobid()==0? 0 : user.getJob().getJobid()));
		LogU.add(user.getDepartment()==null? 0 : (user.getDepartment().getDepid()==0? 0 : user.getDepartment().getDepid()));
		LogU.add(user.getUserDtls()==null? 0l : (user.getUserDtls().getUserdtlsid()==0? 0l : user.getUserDtls().getUserdtlsid()));
		LogU.add(user.getIsActive());
		LogU.add(user.getRegdate());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to userdtls : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return user;
	}

	public void insertData(String type){
		String sql = "INSERT INTO userdtls ("
				+ "userdtlsid,"
				+ "firstname,"
				+ "middlename,"
				+ "lastname,"
				+ "address,"
				+ "contactno,"
				+ "age,"
				+ "gender,"
				+ "logid,"
				+ "jobtitleid,"
				+ "departmentid,"
				+ "addedby,"
				+ "isactive,"
				+ "regdate)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table userdtls");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			setUserdtlsid(Long.valueOf(id));
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			setUserdtlsid(Long.valueOf(id));
			LogU.add("id: " + id);
		}
		
		ps.setString(2, getFirstname());
		ps.setString(3, getMiddlename());
		ps.setString(4, getLastname());
		ps.setString(5, getAddress());
		ps.setString(6, getContactno());
		ps.setInt(7, getAge());
		ps.setInt(8, getGender());
		ps.setLong(9, getLogin().getLogid());
		ps.setInt(10, getJob()==null? 0 : (getJob().getJobid()==0? 0 : getJob().getJobid()));
		ps.setInt(11, getDepartment()==null? 0 : (getDepartment().getDepid()==0? 0 : getDepartment().getDepid()));
		ps.setLong(12, getUserDtls()==null? 0l : (getUserDtls().getUserdtlsid()==0? 0l : getUserDtls().getUserdtlsid()));
		ps.setInt(13, getIsActive());
		ps.setString(14, getRegdate());
		
		LogU.add( getFirstname());
		LogU.add( getMiddlename());
		LogU.add(getLastname());
		LogU.add(getAddress());
		LogU.add(getContactno());
		LogU.add(getAge());
		LogU.add(getGender());
		LogU.add(getLogin().getLogid());
		LogU.add(getJob()==null? 0 : (getJob().getJobid()==0? 0 : getJob().getJobid()));
		LogU.add(getDepartment()==null? 0 : (getDepartment().getDepid()==0? 0 : getDepartment().getDepid()));
		LogU.add(getUserDtls()==null? 0l : (getUserDtls().getUserdtlsid()==0? 0l : getUserDtls().getUserdtlsid()));
		LogU.add(getIsActive());
		LogU.add(getRegdate());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to userdtls : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static UserDtls updateData(UserDtls user){
		String sql = "UPDATE userdtls SET "
				+ "firstname=?,"
				+ "middlename=?,"
				+ "lastname=?,"
				+ "address=?,"
				+ "contactno=?,"
				+ "age=?,"
				+ "gender=?,"
				+ "logid=?,"
				+ "jobtitleid=?,"
				+ "departmentid=?,"
				+ "addedby=?,"
				+ "isactive=?" 
				+ " WHERE userdtlsid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		LogU.add("===========================START=========================");
		LogU.add("update data into table userdtls");
		
		ps.setString(1, user.getFirstname());
		ps.setString(2, user.getMiddlename());
		ps.setString(3, user.getLastname());
		ps.setString(4, user.getAddress());
		ps.setString(5, user.getContactno());
		ps.setInt(6, user.getAge());
		ps.setInt(7, user.getGender());
		ps.setLong(8, user.getLogin().getLogid());
		ps.setInt(9, user.getJob()==null? 0 : (user.getJob().getJobid()==0? 0 : user.getJob().getJobid()));
		ps.setInt(10, user.getDepartment()==null? 0 : (user.getDepartment().getDepid()==0? 0 : user.getDepartment().getDepid()));
		ps.setLong(11, user.getUserDtls()==null? 0l : (user.getUserDtls().getUserdtlsid()==0? 0l : user.getUserDtls().getUserdtlsid()));
		ps.setInt(12, user.getIsActive());
		ps.setLong(13, user.getUserdtlsid());
		
		LogU.add(user.getFirstname());
		LogU.add(user.getMiddlename());
		LogU.add(user.getLastname());
		LogU.add(user.getAddress());
		LogU.add(user.getContactno());
		LogU.add(user.getAge());
		LogU.add(user.getGender());
		LogU.add(user.getLogin().getLogid());
		LogU.add(user.getJob()==null? 0 : (user.getJob().getJobid()==0? 0 : user.getJob().getJobid()));
		LogU.add(user.getDepartment()==null? 0 : (user.getDepartment().getDepid()==0? 0 : user.getDepartment().getDepid()));
		LogU.add(user.getUserDtls()==null? 0l : (user.getUserDtls().getUserdtlsid()==0? 0l : user.getUserDtls().getUserdtlsid()));
		LogU.add(user.getIsActive());
		LogU.add(user.getUserdtlsid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to userdtls : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return user;
	}
	
	public void updateData(){
		String sql = "UPDATE userdtls SET "
				+ "firstname=?,"
				+ "middlename=?,"
				+ "lastname=?,"
				+ "address=?,"
				+ "contactno=?,"
				+ "age=?,"
				+ "gender=?,"
				+ "logid=?,"
				+ "jobtitleid=?,"
				+ "departmentid=?,"
				+ "addedby=?,"
				+ "isactive=?" 
				+ " WHERE userdtlsid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		LogU.add("===========================START=========================");
		LogU.add("update data into table userdtls");
		
		ps.setString(1, getFirstname());
		ps.setString(2, getMiddlename());
		ps.setString(3, getLastname());
		ps.setString(4, getAddress());
		ps.setString(5, getContactno());
		ps.setInt(6, getAge());
		ps.setInt(7, getGender());
		ps.setLong(8, getLogin().getLogid());
		ps.setInt(9, getJob()==null? 0 : (getJob().getJobid()==0? 0 : getJob().getJobid()));
		ps.setInt(10, getDepartment()==null? 0 : (getDepartment().getDepid()==0? 0 : getDepartment().getDepid()));
		ps.setLong(11, getUserDtls()==null? 0l : (getUserDtls().getUserdtlsid()==0? 0l : getUserDtls().getUserdtlsid()));
		ps.setInt(12, getIsActive());
		ps.setLong(13, getUserdtlsid());
		
		LogU.add(getFirstname());
		LogU.add(getMiddlename());
		LogU.add(getLastname());
		LogU.add(getAddress());
		LogU.add(getContactno());
		LogU.add(getAge());
		LogU.add(getGender());
		LogU.add(getLogin().getLogid());
		LogU.add(getJob()==null? 0 : (getJob().getJobid()==0? 0 : getJob().getJobid()));
		LogU.add(getDepartment()==null? 0 : (getDepartment().getDepid()==0? 0 : getDepartment().getDepid()));
		LogU.add(getUserDtls()==null? 0l : (getUserDtls().getUserdtlsid()==0? 0l : getUserDtls().getUserdtlsid()));
		LogU.add(getIsActive());
		LogU.add(getUserdtlsid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to userdtls : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	private static String processBy(){
		String proc_by = "error";
		try{
			HttpSession session = SessionBean.getSession();
			proc_by = session.getAttribute("username").toString();
		}catch(Exception e){}
		return proc_by;
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT userdtlsid FROM userdtls  ORDER BY userdtlsid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("userdtlsid");
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
		ps = conn.prepareStatement("SELECT userdtlsid FROM userdtls WHERE userdtlsid=?");
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
	
	public void delete(boolean retain){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE userdtls set isactive=0 WHERE userdtlsid=?";
		
		if(!retain){
			sql = "DELETE FROM userdtls WHERE userdtlsid=?";
		}
		
		String[] params = new String[1];
		params[0] = getUserdtlsid()+"";
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