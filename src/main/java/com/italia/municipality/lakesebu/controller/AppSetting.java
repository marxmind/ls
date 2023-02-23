package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AppSetting {
	private long id;
	private String name;
	private String value;
	private int isActive;
	private UserDtls userDtls;
	
	
	public static String getCollectorMode(UserDtls user) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		String sql = "SELECT settingvalue FROM appsettings WHERE isactivesett=1 AND namesetting='COLLECTOR MODE' AND userdtlsid="+ user.getUserdtlsid() +" ORDER BY settingvalue DESC LIMIT 1";
		String[] params = new String[0];
		
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
			return rs.getString("settingvalue");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return "OFF";
	}
	
	public static List<AppSetting> getTargetBudget() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		List<AppSetting> apps = new ArrayList<AppSetting>();
		String sql = "SELECT namesetting,settingvalue FROM appsettings WHERE isactivesett=1 AND userdtlsid=0 ORDER BY settingvalue";
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			AppSetting app = AppSetting.builder()
					.name(rs.getString("namesetting"))
					.value(rs.getString("settingvalue"))
					.build();
			apps.add(app);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return apps;
	}
	
	public static String updateCollectorMode(boolean isOn ,UserDtls user) {
		System.out.println("updateCollectorMode..." + isOn);
		//isOn = isOn==true? false : true;
		if(isExistCollectorMode(user)) {
			Connection conn = null;
			PreparedStatement ps = null;
			
			String sql = "UPDATE appsettings SET settingvalue='"+  (isOn==true? "ON" : "OFF") +"' WHERE isactivesett=1 AND namesetting='COLLECTOR MODE' AND userdtlsid="+ user.getUserdtlsid();
			String[] params = new String[0];
			System.out.println(sql);
			try{
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			System.out.println("updating collector..." + isOn);
			ps.executeUpdate();
			
			ps.close();
			WebTISDatabaseConnect.close(conn);
			}catch(Exception e){e.getMessage();}
		
		}else {
			
			AppSetting app = AppSetting.builder()
					.name("COLLECTOR MODE")
					.value(isOn==true? "ON" : "OFF")
					.isActive(1)
					.userDtls(user)
					.build();
			
			app.save();
			
			return isOn==true? "ON" : "OFF";
		}
		
		return "OFF";
	}
	
	private static boolean isExistCollectorMode(UserDtls user) {
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		String sql = "SELECT settingvalue FROM appsettings WHERE isactivesett=1 AND namesetting='COLLECTOR MODE' AND userdtlsid="+ user.getUserdtlsid() +" ORDER BY settingvalue DESC LIMIT 1";
		String[] params = new String[0];
		
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
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static String getReportSeries() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		String sql = "SELECT settingvalue FROM appsettings WHERE isactivesett=1 AND namesetting='SERIES' ORDER BY settingvalue DESC LIMIT 1";
		String[] params = new String[0];
		
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
			return rs.getString("settingvalue");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		String year = DateUtils.getCurrentYear()+"";
		String month = DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+""; 
		
		return year + "-" + month + "-#000";
	}
	
	public static void updateSeries(String value) {
		
		if(checkSeriesExist()) {// if existed
			
			Connection conn = null;
			PreparedStatement ps = null;
			
			String sql = "UPDATE appsettings SET settingvalue='"+ value +"' WHERE isactivesett=1 AND namesetting='SERIES'";
			String[] params = new String[0];
			
			try{
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			ps.executeUpdate();
			
			 
			ps.close();
			WebTISDatabaseConnect.close(conn);
			}catch(Exception e){e.getMessage();}
		
		} else {
			UserDtls user = new UserDtls();
			user.setUserdtlsid(1l);
			
			String year = DateUtils.getCurrentYear()+"";
			String month = DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+""; 
			AppSetting app = AppSetting.builder()
					.name("SERIES")
					.value(year + "-" + month + "-#001")
					.isActive(1)
					.userDtls(user)
					.build();
			
			app.save();
		}
		
	}
	
	private static boolean checkSeriesExist() {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		String sql = "SELECT * FROM appsettings WHERE isactivesett=1 AND namesetting='SERIES'";
		String[] params = new String[0];
		
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
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static List<AppSetting> retrieve(String sql, String[] params){
		List<AppSetting> apps = new ArrayList<AppSetting>();
		
		String tableApp="apt";
		String tableUser="usr";
		String sq = "SELECT * FROM appsettings " + tableApp + ", userdtls " + tableUser + " WHERE " + tableApp + ".isactivesett=1 AND " +
		tableApp + ".userdtlsid=" + tableUser + ".userdtlsid ";
		
		sql  = sq + sql;
		
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
			AppSetting app = AppSetting.builder()
					.id(rs.getLong("sid"))
					.name(rs.getString("namesetting"))
					.value(rs.getString("settingvalue"))
					.isActive(rs.getInt("isactivesett"))
					.build();
			
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
			//try{user.setLogin(Login.login(rs.getString("logid")));}catch(NullPointerException e){}
			//try{user.setJob(Job.job(rs.getString("jobtitleid")));}catch(NullPointerException e){}
			//try{user.setDepartment(Department.department(rs.getString("departmentid")));}catch(NullPointerException e){}
			//try{user.setUserDtls(UserDtls.addedby(rs.getString("addedby")));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			
			app.setUserDtls(user);
			apps.add(app);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return apps;
	}
	
	public static AppSetting save(AppSetting pos){
		if(pos!=null){
			
			long id = AppSetting.getInfo(pos.getId() ==0? AppSetting.getLatestId()+1 : pos.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				pos = AppSetting.insertData(pos, "1");
			}else if(id==2){
				LogU.add("update Data ");
				pos = AppSetting.updateData(pos);
			}else if(id==3){
				LogU.add("added new Data ");
				pos = AppSetting.insertData(pos, "3");
			}
			
		}
		return pos;
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
	
	public static AppSetting insertData(AppSetting pos, String type){
		String sql = "INSERT INTO appsettings ("
				+ "sid,"
				+ "namesetting,"
				+ "settingvalue,"
				+ "isactivesett,"
				+ "userdtlsid)" 
				+ "values(?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table appsettings");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			pos.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			pos.setId(id);
			LogU.add("id: " + id);
		}
		ps.setString(cnt++, pos.getName());
		ps.setString(cnt++, pos.getValue());
		ps.setInt(cnt++, pos.getIsActive());
		ps.setLong(cnt++, pos.getUserDtls()==null? 0l : pos.getUserDtls().getUserdtlsid());
		
		LogU.add(pos.getName());
		LogU.add(pos.getValue());
		LogU.add(pos.getIsActive());
		LogU.add(pos.getUserDtls()==null? 0l : pos.getUserDtls().getUserdtlsid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to appsettings : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pos;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO appsettings ("
				+ "sid,"
				+ "namesetting,"
				+ "settingvalue,"
				+ "isactivesett,"
				+ "userdtlsid)" 
				+ "values(?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table appsettings");
		if("1".equalsIgnoreCase(type)){
			ps.setInt(cnt++, id);
			setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setInt(cnt++, id);
			setId(id);
			LogU.add("id: " + id);
		}
		ps.setString(cnt++, getName());
		ps.setString(cnt++, getValue());
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getUserDtls()==null? 0l : getUserDtls().getUserdtlsid());
		
		LogU.add(getName());
		LogU.add(getValue());
		LogU.add(getIsActive());
		LogU.add(getUserDtls()==null? 0l : getUserDtls().getUserdtlsid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to appsettings : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static AppSetting updateData(AppSetting pos){
		String sql = "UPDATE appsettings SET "
				+ "namesetting=?,"
				+ "settingvalue=?,"
				+ "userdtlsid=?" 
				+ " WHERE sid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table appsettings");
		
		ps.setString(cnt++, pos.getName());
		ps.setString(cnt++, pos.getValue());
		ps.setLong(cnt++, pos.getUserDtls()==null? 0l : pos.getUserDtls().getUserdtlsid());
		ps.setLong(cnt++, pos.getId());
		
		LogU.add(pos.getName());
		LogU.add(pos.getValue());
		LogU.add(pos.getUserDtls()==null? 0l : pos.getUserDtls().getUserdtlsid());
		LogU.add(pos.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to appsettings : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pos;
	}
	
	public void updateData(){
		String sql = "UPDATE appsettings SET "
				+ "namesetting=?,"
				+ "settingvalue=?,"
				+ "userdtlsid=?" 
				+ " WHERE sid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table appsettings");
		
		ps.setString(cnt++, getName());
		ps.setString(cnt++, getValue());
		ps.setLong(cnt++, getUserDtls()==null? 0l : getUserDtls().getUserdtlsid());
		ps.setLong(cnt++, getId());
		
		LogU.add(getName());
		LogU.add(getValue());
		LogU.add(getUserDtls()==null? 0l : getUserDtls().getUserdtlsid());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to appsettings : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	
	public static int getLatestId(){
		int id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT sid FROM appsettings  ORDER BY sid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("sid");
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
		ps = conn.prepareStatement("SELECT sid FROM appsettings WHERE sid=?");
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
		String sql = "UPDATE appsettings set isactivesett=0 WHERE sid=?";
		
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