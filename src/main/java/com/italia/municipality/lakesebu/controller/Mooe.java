package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class Mooe {
	private long id;
	private String code;
	private String dateTrans;
	private String description;
	private double budgetAmount;
	private int isActive;
	private int yearBudget;
	private Offices offices;
	
	private Date tmpDateTrans;
	
	public static Map<Long, Mooe> retrieveDataAll(int year){
		Map<Long, Mooe> mooes = new LinkedHashMap<Long, Mooe>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT * FROM mooe WHERE isactivem=1 AND yearbudget="+year);
		System.out.println("MOOE SQL " + ps.toString());
		rs = ps.executeQuery();
		while(rs.next()){
			Mooe moe = Mooe.builder()
					.id(rs.getLong("moid"))
					.code(rs.getString("code"))
					.dateTrans(rs.getString("encodedate"))
					.description(rs.getString("description"))
					.budgetAmount(rs.getDouble("budgetamount"))
					.isActive(rs.getInt("isactivem"))
					.yearBudget(rs.getInt("yearbudget"))
					.offices(Offices.builder().id(rs.getLong("offid")).build())
					.build();
			mooes.put(moe.getId(), moe);
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		return mooes;
	}
	
	public static Map<Long, Mooe> retrieveData(long officeId, int year){
		Map<Long, Mooe> mooes = new LinkedHashMap<Long, Mooe>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT * FROM mooe WHERE isactivem=1 AND offid="+officeId + " AND yearbudget="+year);
		System.out.println("MOOE SQL " + ps.toString());
		rs = ps.executeQuery();
		while(rs.next()){
			Mooe moe = Mooe.builder()
					.id(rs.getLong("moid"))
					.code(rs.getString("code"))
					.dateTrans(rs.getString("encodedate"))
					.description(rs.getString("description"))
					.budgetAmount(rs.getDouble("budgetamount"))
					.isActive(rs.getInt("isactivem"))
					.yearBudget(rs.getInt("yearbudget"))
					.offices(Offices.builder().id(rs.getLong("offid")).build())
					.build();
			mooes.put(moe.getId(), moe);
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		return mooes;
	}
	
	public static List<Mooe> retrieve(String sql, String[] params){
		List<Mooe> moes = new ArrayList<Mooe>();
		String ofs = "ofs";
		String mos = "moe";
		String sqlTemp = "SELECT * FROM mooe "+ mos +", offices "+ ofs +" WHERE "+ mos +".isactivem=1 AND "
				+ mos + ".offid=" + ofs + ".offid ";
		
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
		System.out.println("mooe SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Offices off = Offices.builder()
					.id(rs.getInt("offid"))
					.name(rs.getString("name"))
					.code(rs.getString("code"))
					.headOfOffice(rs.getString("headname"))
					.abr(rs.getString("abrname"))
					.isActive(rs.getInt("isactiveoff"))
					.departmentId(rs.getInt("departmentid"))
					.build();
			
		Mooe moe = Mooe.builder()
				.id(rs.getLong("moid"))
				.code(rs.getString("code"))
				.dateTrans(rs.getString("encodedate"))
				.description(rs.getString("description"))
				.budgetAmount(rs.getDouble("budgetamount"))
				.isActive(rs.getInt("isactivem"))
				.yearBudget(rs.getInt("yearbudget"))
				.offices(off)
				.build();
			
			moes.add(moe);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return moes;
	}
	
	public static Mooe save(Mooe st){
		if(st!=null){
			
			long id = Mooe.getInfo(st.getId() ==0? Mooe.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Mooe.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Mooe.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Mooe.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Mooe.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Mooe.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Mooe.insertData(this, "3");
		}
		
 }
	
	public static Mooe insertData(Mooe st, String type){
		String sql = "INSERT INTO mooe ("
				+ "moid,"
				+ "code,"
				+ "encodedate,"
				+ "description,"
				+ "budgetamount,"
				+ "isactivem,"
				+ "offid,"
				+ "yearbudget)" 
				+ " VALUES(?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table mooe");
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
		
		ps.setString(cnt++, st.getCode());
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getDescription());
		ps.setDouble(cnt++, st.getBudgetAmount());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getOffices().getId());
		ps.setInt(cnt++, st.getYearBudget());
		
		LogU.add(st.getCode());
		LogU.add(st.getDateTrans());
		LogU.add(st.getDescription());
		LogU.add(st.getBudgetAmount());
		LogU.add(st.getIsActive());
		LogU.add(st.getOffices().getId());
		LogU.add(st.getYearBudget());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to mooe : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static Mooe updateData(Mooe st){
		String sql = "UPDATE mooe SET "
				+ "code=?,"
				+ "encodedate=?,"
				+ "description=?,"
				+ "budgetamount=?,"
				+ "offid=?,"
				+ "yearbudget=?" 
				+ " WHERE moid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table mooe");
		
		ps.setString(cnt++, st.getCode());
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getDescription());
		ps.setDouble(cnt++, st.getBudgetAmount());
		ps.setLong(cnt++, st.getOffices().getId());
		ps.setInt(cnt++, st.getYearBudget());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getCode());
		LogU.add(st.getDateTrans());
		LogU.add(st.getDescription());
		LogU.add(st.getBudgetAmount());
		LogU.add(st.getOffices().getId());
		LogU.add(st.getYearBudget());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to mooe : " + s.getMessage());
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
		sql="SELECT moid FROM mooe ORDER BY moid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("moid");
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
		ps = conn.prepareStatement("SELECT moid FROM mooe WHERE moid=?");
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
		String sql = "UPDATE mooe set isactivem=0 WHERE moid=?";
		
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
