package com.italia.municipality.lakesebu.controller;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
 * @since 08/07/2019
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Responsibility {

	private long id;
	private String dateTrans;
	private String name;
	private Department department;
	private int isResign;
	private int isActive;
	
	public static List<Responsibility> retrieve(String sql, String[] params){
		List<Responsibility> rss = new ArrayList<Responsibility>();
		
		String tableVr = "rss";
		String tableDep = "dep";
		
		String sqlQ = "SELECT * FROM responsibility "+ tableVr +", department "+ tableDep +" WHERE "+ tableVr +".isactiver =1 AND " + tableVr +".departmentid=" + tableDep + ".departmentid ";
		
		sql = sqlQ + sql;
		
		System.out.println("SQL retrieve >> " + sql);
		
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
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				
				Responsibility r = new Responsibility();
				try{r.setId(rs.getLong("rid"));}catch(Exception e) {}
				try{r.setDateTrans(rs.getString("rrdate"));}catch(Exception e) {}
				try{r.setName(rs.getString("rname"));}catch(Exception e) {}
				try{r.setIsResign(rs.getInt("isresign"));}catch(Exception e) {}
				try{r.setIsActive(rs.getInt("isactiver"));}catch(Exception e) {}
				
				Department department = new Department();
				try{department.setDepid(rs.getInt("departmentid"));}catch(Exception e) {}
				try{department.setDepartmentName(rs.getString("departmentname"));}catch(Exception e) {}
				try{department.setCode(rs.getString("code"));}catch(Exception e) {}
				try{r.setDepartment(department);}catch(Exception e) {}
				
				rss.add(r);
				
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return rss;
	}
	
	public static Responsibility save(Responsibility st){
		if(st!=null){
			
			long id = Responsibility.getInfo(st.getId() ==0? Responsibility.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Responsibility.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Responsibility.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Responsibility.insertData(st, "3");
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
	
	public static Responsibility insertData(Responsibility rs, String type){
		String sql = "INSERT INTO responsibility ("
				+ "rid,"
				+ "rrdate,"
				+ "rname,"
				+ "departmentid,"
				+ "isresign,"
				+ "isactiver)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table responsibility");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			rs.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			rs.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, rs.getDateTrans());
		ps.setString(cnt++, rs.getName());
		ps.setInt(cnt++, rs.getDepartment().getDepid());
		ps.setInt(cnt++, rs.getIsResign());
		ps.setInt(cnt++, rs.getIsActive());
		
		LogU.add(rs.getDateTrans());
		LogU.add(rs.getName());
		LogU.add(rs.getDepartment().getDepid());
		LogU.add(rs.getIsResign());
		LogU.add(rs.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to responsibility : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return rs;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO responsibility ("
				+ "rid,"
				+ "rrdate,"
				+ "rname,"
				+ "departmentid,"
				+ "isresign,"
				+ "isactiver)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table responsibility");
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
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getDepartment().getDepid());
		ps.setInt(cnt++, getIsResign());
		ps.setInt(cnt++, getIsActive());
		
		LogU.add(getDateTrans());
		LogU.add(getName());
		LogU.add(getDepartment().getDepid());
		LogU.add(getIsResign());
		LogU.add(getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to responsibility : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Responsibility updateData(Responsibility rs){
		String sql = "UPDATE responsibility SET "
				+ "rrdate=?,"
				+ "rname=?,"
				+ "departmentid=?,"
				+ "isresign=? " 
				+ " WHERE rid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table responsibility");
		
		
		ps.setString(cnt++, rs.getDateTrans());
		ps.setString(cnt++, rs.getName());
		ps.setInt(cnt++, rs.getDepartment().getDepid());
		ps.setInt(cnt++, rs.getIsResign());
		ps.setLong(cnt++, rs.getId());
		
		LogU.add(rs.getDateTrans());
		LogU.add(rs.getName());
		LogU.add(rs.getDepartment().getDepid());
		LogU.add(rs.getIsResign());
		LogU.add(rs.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to responsibility : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return rs;
	}
	
	public void updateData(){
		String sql = "UPDATE responsibility SET "
				+ "rrdate=?,"
				+ "rname=?,"
				+ "departmentid=?,"
				+ "isresign=? " 
				+ " WHERE rid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table responsibility");
		
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getName());
		ps.setInt(cnt++, getDepartment().getDepid());
		ps.setInt(cnt++, getIsResign());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateTrans());
		LogU.add(getName());
		LogU.add(getDepartment().getDepid());
		LogU.add(getIsResign());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to responsibility : " + s.getMessage());
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
		sql="SELECT rid FROM responsibility  ORDER BY rid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("rid");
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
		ps = conn.prepareStatement("SELECT rid FROM responsibility WHERE rid=?");
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
		String sql = "UPDATE responsibility set isactiver=0 WHERE rid=?";
		
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
