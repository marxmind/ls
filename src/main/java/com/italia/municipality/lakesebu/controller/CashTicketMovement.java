package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.CashTicketMovementType;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Mark Italia
 * @version 1.0
 * @since 04/29/2024
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class CashTicketMovement {
	
	//balance type: beginning=0, ending=1
	
	private long id;
	private int year;
	private int month;
	private int balanceType;
	private double balance;
	private int isActive;
	
	private Collector collector;
	
	private String balanceName;
	private String monthName;
	
	public static double getBalance(int balanceType, int collectorId, int month, int year) {
		double balance = 0d;
		
		ResultSet rs = OpenTableAccess.query("SELECT balance FROM cashticketmovement WHERE isactivemov=1 AND balancetype=" + balanceType + " AND isid="+ collectorId + " AND monthm="+month + " AND yearm=" + year, new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				balance = rs.getDouble("balance");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return balance;
	}
	
	public static List<String> getReceivedForms(int collectorId, int month, int year){
		List<String> vals = new ArrayList<String>();
		
		ResultSet rs = OpenTableAccess.query("SELECT issueddate,stabno,logpcs,formtypelog FROM logissuedform WHERE isactivelog=1  AND isid="+ 
		collectorId + " AND month(issueddate)="+month + " AND year(issueddate)=" + year + " AND ( formtypelog="+ FormType.CT_2.getId() +" OR formtypelog="+ FormType.CT_5.getId() +"  )  ORDER BY issueddate", new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				double amount = 0d;
				int pcs = rs.getInt("logpcs");
				if(FormType.CT_2.getId() == rs.getInt("formtypelog") ) {
					amount = pcs * 2;
				}else if(FormType.CT_5.getId() == rs.getInt("formtypelog") ) {
					amount = pcs * 5;
				}
				String[] issued = rs.getString("issueddate").split("-");
				String val = issued[1] + "/" + issued[2] + "<*>" + rs.getString("stabno") + "<*>" + amount;
				vals.add(val);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public static List<String> getIssuedForms(int collectorId, int month, int year){
		List<String> vals = new ArrayList<String>();
		
		ResultSet rs = OpenTableAccess.query("SELECT receiveddate,amount FROM collectioninfo WHERE isactivecol=1  AND isid="+ collectorId + " AND month(receiveddate)="+month + " AND year(receiveddate)=" + year + " AND (formtypecol="+ FormType.CT_2.getId() +" OR formtypecol="+FormType.CT_5.getId()+" )", new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				
				String[] issued = rs.getString("receiveddate").split("-");
				String val = issued[1] + "/" + issued[2] + "<*>"  + rs.getDouble("amount");
				vals.add(val);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public static List<CashTicketMovement> retrieve(String sql, String[] params){
		List<CashTicketMovement> movs = new ArrayList<CashTicketMovement>();
		
		String tableT = "ct";
		String tableC = "col";
		String sqlTmp = "SELECT * FROM cashticketmovement "+ tableT +", issuedcollector "+ tableC +" WHERE "+ 
				tableT +".isactivemov=1 AND " + tableT + ".isid=" + tableC + ".isid" ;
		
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
				
				Collector col = Collector.builder()
						.id(rs.getInt("isid"))
						.name(rs.getString("collectorname"))
						.isActive(rs.getInt("isactivecollector"))
						.isResigned(rs.getInt("isresigned"))
						.doNotTrace(rs.getInt("donottrace"))
						.build();
				
				CashTicketMovement mv = CashTicketMovement.builder()
						.id(rs.getLong("movid"))
						.year(rs.getInt("yearm"))
						.month(rs.getInt("monthm"))
						.balanceType(rs.getInt("balancetype"))
						.balance(rs.getDouble("balance"))
						.isActive(rs.getInt("isactivemov"))
						.collector(col)
						.balanceName(CashTicketMovementType.nameId(rs.getInt("balancetype")))
						.monthName(DateUtils.getMonthName(rs.getInt("monthm")))
						.build();
				
				movs.add(mv);
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return movs;
	}
	
	public static CashTicketMovement save(CashTicketMovement st){
		if(st!=null){
			
			long id = CashTicketMovement.getInfo(st.getId() ==0? CashTicketMovement.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = CashTicketMovement.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = CashTicketMovement.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = CashTicketMovement.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			CashTicketMovement.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			CashTicketMovement.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			CashTicketMovement.insertData(this,"3");
		}
		
	}
	
	public static CashTicketMovement insertData(CashTicketMovement st, String type){
		String sql = "INSERT INTO cashticketmovement ("
				+ "movid,"
				+ "yearm,"
				+ "monthm,"
				+ "isid,"
				+ "balancetype,"
				+ "balance,"
				+ "isactivemov)" 
				+ "values(?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cashticketmovement");
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
		
		ps.setInt(cnt++, st.getYear());
		ps.setInt(cnt++, st.getMonth());
		ps.setInt(cnt++, st.getCollector().getId());
		ps.setInt(cnt++, st.getBalanceType());
		ps.setDouble(cnt++, st.getBalance());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getYear());
		LogU.add(st.getMonth());
		LogU.add(st.getCollector().getId());
		LogU.add(st.getBalanceType());
		LogU.add(st.getBalance());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to cashticketmovement : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static CashTicketMovement updateData(CashTicketMovement st){
		String sql = "UPDATE cashticketmovement SET "
				+ "yearm=?,"
				+ "monthm=?,"
				+ "isid=?,"
				+ "balancetype=?,"
				+ "balance=?" 
				+ " WHERE movid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table cashticketmovement");
		
		
		ps.setInt(cnt++, st.getYear());
		ps.setInt(cnt++, st.getMonth());
		ps.setInt(cnt++, st.getCollector().getId());
		ps.setInt(cnt++, st.getBalanceType());
		ps.setDouble(cnt++, st.getBalance());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getYear());
		LogU.add(st.getMonth());
		LogU.add(st.getCollector().getId());
		LogU.add(st.getBalanceType());
		LogU.add(st.getBalance());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to cashticketmovement : " + s.getMessage());
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
		sql="SELECT movid FROM cashticketmovement  ORDER BY movid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("movid");
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
		ps = conn.prepareStatement("SELECT movid FROM cashticketmovement WHERE movid=?");
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
		String sql = "UPDATE cashticketmovement set isactivemov=0 WHERE movid=?";
		
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
