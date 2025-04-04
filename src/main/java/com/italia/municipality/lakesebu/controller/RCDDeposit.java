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
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

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
public class RCDDeposit {
	
	private long id;
	private int indexId;
	private String dateTrans;
	private String remarks;
	private String reference;
	private double amount;
	private String accountNo;
	private int groupNo;
	private String bankName;
	private int fundType;
	private int isActive;
	
	private Date dateDeposited;
	
	public static double totalDeposit(int year, int fund) {
		
		String sql = "SELECT sum(amount) as total FROM rcddeposit where isactivercd=1 AND year(datetrans)=" + year + " AND fundtype=" + fund;
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		try {
			while(rs.next()) {
				return rs.getDouble("total");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public static Map<Integer, Double> depositedCollection(int year, int fund){
		Map<Integer, Double> mapData = new LinkedHashMap<Integer, Double>();
		String sql = "SELECT month(datetrans) as mnt, sum(amount) as total FROM rcddeposit where isactivercd=1 AND fundtype="+fund+" AND year(datetrans)=" + year;
		
		
		sql += " GROUP BY month(datetrans) ORDER BY month(datetrans)";
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				int mnt = rs.getInt("mnt");
				double amount = rs.getDouble("total");
				if(mapData!=null) {
					if(mapData.containsKey(mnt)) {
						double amnt = mapData.get(mnt) + amount;
						mapData.put(mnt, amnt);
					}else {
						mapData.put(mnt, amount);
					}
				}else {
					mapData.put(mnt, amount);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mapData;
	}
	
	public static Map<Integer, Double> depositedCollection(int year, int month, int fund){
		Map<Integer, Double> mapData = new LinkedHashMap<Integer, Double>();
		String sql = "SELECT month(datetrans) as mnt, sum(amount) as total FROM rcddeposit where isactivercd=1 AND fundtype="+fund+" AND year(datetrans)=" + year;
		
		if(month>0) {
			sql += " AND month(datetrans)=" + month;
		}
		
		sql += " GROUP BY month(datetrans) ORDER BY month(datetrans)";
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				int mnt = rs.getInt("mnt");
				double amount = rs.getDouble("total");
				if(mapData!=null) {
					if(mapData.containsKey(mnt)) {
						double amnt = mapData.get(mnt) + amount;
						mapData.put(mnt, amnt);
					}else {
						mapData.put(mnt, amount);
					}
				}else {
					mapData.put(mnt, amount);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mapData;
	}
	
	public static int lastIndex(int fundType, int year, int month) {
		
		ResultSet rs = OpenTableAccess.query("SELECT indexid FROM rcddeposit WHERE isactivercd=1 AND fundtype=" + fundType + " AND year(datetrans)=" + year + " AND month(datetrans)=" + month + " ORDER BY indexid DESC LIMIT 1", new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				return rs.getInt("indexid");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public static List<RCDDeposit> retrieve(String sql, String[] params){
		List<RCDDeposit> vrs = new ArrayList<RCDDeposit>();
		
		String tableController = "ct";
		String sqlTmp = "SELECT * FROM rcddeposit "+ tableController +" WHERE "+ 
				tableController +".isactivercd=1 ";
		
		sql = sqlTmp + sql;
		
		
		
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
			System.out.println("SQL retrieve >> " + ps.toString());
			rs = ps.executeQuery();
			
			while(rs.next()){
				
				RCDDeposit vr = RCDDeposit.builder()
						.id(rs.getLong("did"))
						.dateTrans(rs.getString("datetrans"))
						.reference(rs.getString("reference"))
						.remarks(rs.getString("remarks"))
						.accountNo(rs.getString("accountno"))
						.bankName(rs.getString("bankname"))
						.amount(rs.getDouble("amount"))
						.isActive(rs.getInt("isactivercd"))
						.fundType(rs.getInt("fundtype"))
						.indexId(rs.getInt("indexid"))
						.groupNo(rs.getInt("groupno"))
						.build();
				
				vrs.add(vr);
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return vrs;
	}
	
	public static RCDDeposit save(RCDDeposit st){
		if(st!=null){
			
			long id = RCDDeposit.getInfo(st.getId() ==0? RCDDeposit.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = RCDDeposit.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = RCDDeposit.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = RCDDeposit.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			RCDDeposit.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			RCDDeposit.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			RCDDeposit.insertData(this,"3");
		}
		
	}
	
	public static RCDDeposit insertData(RCDDeposit st, String type){
		String sql = "INSERT INTO rcddeposit ("
				+ "did,"
				+ "datetrans,"
				+ "reference,"
				+ "remarks,"
				+ "accountno,"
				+ "bankname,"
				+ "amount,"
				+ "isactivercd,"
				+ "fundtype,"
				+ "indexid,"
				+ "groupno)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table rcddeposit");
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
		ps.setString(cnt++, st.getReference());
		ps.setString(cnt++, st.getRemarks());
		ps.setString(cnt++, st.getAccountNo());
		ps.setString(cnt++, st.getBankName());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getFundType());
		ps.setInt(cnt++, st.getIndexId());
		ps.setInt(cnt++, st.getGroupNo());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getReference());
		LogU.add(st.getRemarks());
		LogU.add(st.getAccountNo());
		LogU.add(st.getBankName());
		LogU.add(st.getAmount());
		LogU.add(st.getIsActive());
		LogU.add(st.getFundType());
		LogU.add(st.getIndexId());
		LogU.add(st.getGroupNo());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to rcddeposit : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static RCDDeposit updateData(RCDDeposit st){
		String sql = "UPDATE rcddeposit SET "
				+ "datetrans=?,"
				+ "reference=?,"
				+ "remarks=?,"
				+ "accountno=?,"
				+ "bankname=?,"
				+ "amount=?,"
				+ "fundtype=?,"
				+ "indexid=?,"
				+ "groupno=?" 
				+ " WHERE did=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table rcddeposit");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getReference());
		ps.setString(cnt++, st.getRemarks());
		ps.setString(cnt++, st.getAccountNo());
		ps.setString(cnt++, st.getBankName());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getFundType());
		ps.setInt(cnt++, st.getIndexId());
		ps.setInt(cnt++, st.getGroupNo());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getReference());
		LogU.add(st.getRemarks());
		LogU.add(st.getAccountNo());
		LogU.add(st.getBankName());
		LogU.add(st.getAmount());
		LogU.add(st.getFundType());
		LogU.add(st.getIndexId());
		LogU.add(st.getGroupNo());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to rcddeposit : " + s.getMessage());
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
		sql="SELECT did FROM rcddeposit  ORDER BY did DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("did");
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
		ps = conn.prepareStatement("SELECT did FROM rcddeposit WHERE did=?");
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
		String sql = "UPDATE rcddeposit set isactivercd=0 WHERE did=?";
		
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
