package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.enm.VrTransStatus;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/26/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class VrTransaction {

	private long id;
	private String dateTrans;
	private String payor;
	private double amount;
	private String deliveredBy;
	private String deliveredDateTime;
	private String pickUpBy;
	private String pickUpDateTime;
	private int isActive;
	private String contactNo;
	private String remarks;
	private int status;
	
	private BankAccounts accounts;
	private Offices offices;
	
	private String statusName;
	private Date tmpDateTrans;
	
	
	public static List<VrTransaction> retrieve(String sql, String[] params){
		List<VrTransaction> vrs = new ArrayList<VrTransaction>();
		
		Map<Long, Offices> offices = Offices.retrieveAll();
		Map<Integer, BankAccounts> banks = BankAccounts.budgetAll();
		
		String tableVR = "vr";
		
		String sqlTemp = "SELECT * FROM vrtransaction "+ tableVR +" WHERE " + tableVR + ".isactiverf=1 ";
		
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
		System.out.println("requesition SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			VrTransaction vr = VrTransaction.builder()
					.id(rs.getLong("rfid"))
					.dateTrans(rs.getString("daterf"))
					.payor(rs.getString("payor"))
					.amount(rs.getDouble("amount"))
					.deliveredBy(rs.getString("deliverdby"))
					.deliveredDateTime(rs.getString("delivereddt"))
					.pickUpBy(rs.getString("pickupby"))
					.pickUpDateTime(rs.getString("pickupdt"))
					.isActive(rs.getInt("isactiverf"))
					.contactNo(rs.getString("contactno"))
					.remarks(rs.getString("remarks"))
					.status(rs.getInt("status"))
					.accounts(banks.get(rs.getInt("bankid")))
					.offices(offices.get(rs.getLong("offid")))
					.statusName(VrTransStatus.val(rs.getInt("status")).getName())
					.build();
			
			vrs.add(vr);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return vrs;
	}
	
	public static VrTransaction save(VrTransaction st){
		if(st!=null){
			
			long id = VrTransaction.getInfo(st.getId() ==0? VrTransaction.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = VrTransaction.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = VrTransaction.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = VrTransaction.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			VrTransaction.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			VrTransaction.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			VrTransaction.insertData(this, "3");
		}
		
	}
	
	public static VrTransaction insertData(VrTransaction st, String type){
		String sql = "INSERT INTO vrtransaction ("
				+ "rfid,"
				+ "daterf,"
				+ "payor,"
				+ "amount,"
				+ "deliverdby,"
				+ "delivereddt,"
				+ "pickupby,"
				+ "pickupdt,"
				+ "isactiverf,"
				+ "contactno,"
				+ "remarks,"
				+ "status,"
				+ "bankid,"
				+ "offid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table vrtransaction");
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
		ps.setString(cnt++, st.getPayor());
		ps.setDouble(cnt++, st.getAmount());
		ps.setString(cnt++, st.getDeliveredBy());
		ps.setString(cnt++, st.getDeliveredDateTime());
		ps.setString(cnt++, st.getPickUpBy());
		ps.setString(cnt++, st.getPickUpDateTime());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getContactNo());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getAccounts().getBankId());
		ps.setLong(cnt++, st.getOffices().getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getPayor());
		LogU.add(st.getAmount());
		LogU.add(st.getDeliveredBy());
		LogU.add(st.getDeliveredDateTime());
		LogU.add(st.getPickUpBy());
		LogU.add(st.getPickUpDateTime());
		LogU.add(st.getIsActive());
		LogU.add(st.getContactNo());
		LogU.add(st.getRemarks());
		LogU.add(st.getStatus());
		LogU.add(st.getAccounts().getBankId());
		LogU.add(st.getOffices().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to vrtransaction : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static VrTransaction updateData(VrTransaction st){
		String sql = "UPDATE vrtransaction SET "
				+ "daterf=?,"
				+ "payor=?,"
				+ "amount=?,"
				+ "deliverdby=?,"
				+ "delivereddt=?,"
				+ "pickupby=?,"
				+ "pickupdt=?,"
				+ "contactno=?,"
				+ "remarks=?,"
				+ "status=?,"
				+ "bankid=?,"
				+ "offid=?" 
				+ " WHERE rfid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table vrtransaction");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getPayor());
		ps.setDouble(cnt++, st.getAmount());
		ps.setString(cnt++, st.getDeliveredBy());
		ps.setString(cnt++, st.getDeliveredDateTime());
		ps.setString(cnt++, st.getPickUpBy());
		ps.setString(cnt++, st.getPickUpDateTime());
		ps.setString(cnt++, st.getContactNo());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getAccounts().getBankId());
		ps.setLong(cnt++, st.getOffices().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getPayor());
		LogU.add(st.getAmount());
		LogU.add(st.getDeliveredBy());
		LogU.add(st.getDeliveredDateTime());
		LogU.add(st.getPickUpBy());
		LogU.add(st.getPickUpDateTime());
		LogU.add(st.getContactNo());
		LogU.add(st.getRemarks());
		LogU.add(st.getStatus());
		LogU.add(st.getAccounts().getBankId());
		LogU.add(st.getOffices().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to vrtransaction : " + s.getMessage());
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
		sql="SELECT rfid FROM vrtransaction ORDER BY rfid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("rfid");
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
		ps = conn.prepareStatement("SELECT rfid FROM vrtransaction WHERE rfid=?");
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
		System.out.println("Delete SQL : " + ps.toString());
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public  void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE vrtransaction set isactiverf=0 WHERE rfid=?";
		
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
