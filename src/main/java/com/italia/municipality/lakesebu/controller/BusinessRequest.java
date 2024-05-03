package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.BusinessRequestType;
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
public class BusinessRequest {

	private long id;
	private String dateFilling;
	private String dateApproved;
	private String oldBusinessName;
	private String newBusinessName;
	private String oldOwner;
	private String newOwner;
	private String oldAddress;
	private String newAddress;
	private int type;
	private int isActive;
	private String reason;
	private String dateStopOperation;
	
	private String officer;
	private String officerPosition;
	private String bploApprover;
	private String bploPosition;
	private String inspectorate;
	private String treasurer;
	private String mayor;
	private String effectivityDate;
	private int genderReq;
	private String businessStart;
	private String lineOfBusiness;
	private String orNumber;
	private int isCompleted;
	
	private Date dateFilingTmp;
	private Date dateApprovedTmp;
	private Date dateStopOperationTmp;
	private Date dateEffectityTmp;
	private Date dateStartOperationTmp;
	private String typeName;
	
	public static List<BusinessRequest> retrieve(String sqlAdd, String[] params){
		List<BusinessRequest> taxes = new ArrayList<BusinessRequest>();
		
		String tableTax ="st";
		String sql = "SELECT * FROM businessrequest "+ tableTax + "  WHERE "+tableTax+".isactivereq=1 ";  
		
				
		sql = sql + sqlAdd;		
				
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
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		while(rs.next()){
			
			BusinessRequest rq = BusinessRequest.builder()
					.id(rs.getLong("reqid"))
					.dateFilling(rs.getString("datefiling"))
					.dateApproved(rs.getString("dateapproved"))
					.oldBusinessName(rs.getString("oldbizname"))
					.newBusinessName(rs.getString("newbizname"))
					.oldOwner(rs.getString("oldownername"))
					.newOwner(rs.getString("newownername"))
					.oldAddress(rs.getString("oldaddress"))
					.newAddress(rs.getString("newaddress"))
					.type(rs.getInt("trantype"))
					.reason(rs.getString("reason"))
					.isActive(rs.getInt("isactivereq"))
					.typeName(BusinessRequestType.val(rs.getInt("trantype")).getName())
					.dateStopOperation(rs.getString("datestop"))
					.officer(rs.getString("officer"))
					.officerPosition(rs.getString("officerpos"))
					.bploApprover(rs.getString("bploapprover"))
					.bploPosition(rs.getString("bplopos"))
					.inspectorate(rs.getString("inspector"))
					.treasurer(rs.getString("treasurer"))
					.mayor(rs.getString("mayor"))
					.effectivityDate(rs.getString("effectivity"))
					.genderReq(rs.getInt("genderreq"))
					.businessStart(rs.getString("businessstart"))
					.lineOfBusiness(rs.getString("lineofbusiness"))
					.orNumber(rs.getString("ornumber"))
					.isCompleted(rs.getInt("iscompleted"))
					.build();
			
			taxes.add(rq);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return taxes;
	}
	
	public static BusinessRequest save(BusinessRequest st){
		if(st!=null){
			
			long id = BusinessRequest.getInfo(st.getId() ==0? BusinessRequest.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = BusinessRequest.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = BusinessRequest.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = BusinessRequest.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			BusinessRequest.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			BusinessRequest.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			BusinessRequest.insertData(this, "3");
		}
		
 }
	
	
	public static BusinessRequest insertData(BusinessRequest st, String type){
		String sql = "INSERT INTO businessrequest ("
				+ "reqid,"
				+ "datefiling,"
				+ "dateapproved,"
				+ "oldbizname,"
				+ "newbizname,"
				+ "oldownername,"
				+ "newownername,"
				+ "oldaddress,"
				+ "newaddress,"
				+ "trantype,"
				+ "reason,"
				+ "isactivereq,"
				+ "datestop,"
				+ "officer,"
				+ "officerpos,"
				+ "bploapprover,"
				+ "bplopos,"
				+ "inspector,"
				+ "treasurer,"
				+ "mayor,"
				+ "effectivity,"
				+ "genderreq,"
				+ "businessstart,"
				+ "lineofbusiness,"
				+ "ornumber,"
				+ "iscompleted)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table businessrequest");
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
		
		ps.setString(cnt++, st.getDateFilling());
		ps.setString(cnt++, st.getDateApproved());
		ps.setString(cnt++, st.getOldBusinessName());
		ps.setString(cnt++, st.getNewBusinessName());
		ps.setString(cnt++, st.getOldOwner());
		ps.setString(cnt++, st.getNewOwner());
		ps.setString(cnt++, st.getOldAddress());
		ps.setString(cnt++, st.getNewAddress());
		ps.setInt(cnt++, st.getType());
		ps.setString(cnt++, st.getReason());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getDateStopOperation());
		ps.setString(cnt++, st.getOfficer());
		ps.setString(cnt++, st.getOfficerPosition());
		ps.setString(cnt++, st.getBploApprover());
		ps.setString(cnt++, st.getBploPosition());
		ps.setString(cnt++, st.getInspectorate());
		ps.setString(cnt++, st.getTreasurer());
		ps.setString(cnt++, st.getMayor());
		ps.setString(cnt++, st.getEffectivityDate());
		ps.setInt(cnt++, st.getGenderReq());
		ps.setString(cnt++, st.getBusinessStart());
		ps.setString(cnt++, st.getLineOfBusiness());
		ps.setString(cnt++, st.getOrNumber());
		ps.setInt(cnt++, st.getIsCompleted());
		
		LogU.add(st.getDateFilling());
		LogU.add(st.getDateApproved());
		LogU.add(st.getOldBusinessName());
		LogU.add(st.getNewBusinessName());
		LogU.add(st.getOldOwner());
		LogU.add(st.getNewOwner());
		LogU.add(st.getOldAddress());
		LogU.add(st.getNewAddress());
		LogU.add(st.getType());
		LogU.add(st.getReason());
		LogU.add(st.getIsActive());
		LogU.add(st.getDateStopOperation());
		LogU.add(st.getOfficer());
		LogU.add(st.getOfficerPosition());
		LogU.add(st.getBploApprover());
		LogU.add(st.getBploPosition());
		LogU.add(st.getInspectorate());
		LogU.add(st.getTreasurer());
		LogU.add(st.getMayor());
		LogU.add(st.getEffectivityDate());
		LogU.add(st.getGenderReq());
		LogU.add(st.getBusinessStart());
		LogU.add(st.getLineOfBusiness());
		LogU.add(st.getOrNumber());
		LogU.add(st.getIsCompleted());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to businessrequest : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static BusinessRequest updateData(BusinessRequest st){
		String sql = "UPDATE businessrequest SET "
				+ "datefiling=?,"
				+ "dateapproved=?,"
				+ "oldbizname=?,"
				+ "newbizname=?,"
				+ "oldownername=?,"
				+ "newownername=?,"
				+ "oldaddress=?,"
				+ "newaddress=?,"
				+ "trantype=?,"
				+ "reason=?,"
				+ "datestop=?," 
				+ "officer=?,"
				+ "officerpos=?,"
				+ "bploapprover=?,"
				+ "bplopos=?,"
				+ "inspector=?,"
				+ "treasurer=?,"
				+ "mayor=?,"
				+ "effectivity=?,"
				+ "genderreq=?,"
				+ "businessstart=?,"
				+ "lineofbusiness=?,"
				+ "ornumber=?,"
				+ "iscompleted=?" 
				+ " WHERE reqid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table businessrequest");
		
		
		ps.setString(cnt++, st.getDateFilling());
		ps.setString(cnt++, st.getDateApproved());
		ps.setString(cnt++, st.getOldBusinessName());
		ps.setString(cnt++, st.getNewBusinessName());
		ps.setString(cnt++, st.getOldOwner());
		ps.setString(cnt++, st.getNewOwner());
		ps.setString(cnt++, st.getOldAddress());
		ps.setString(cnt++, st.getNewAddress());
		ps.setInt(cnt++, st.getType());
		ps.setString(cnt++, st.getReason());
		ps.setString(cnt++, st.getDateStopOperation());
		ps.setString(cnt++, st.getOfficer());
		ps.setString(cnt++, st.getOfficerPosition());
		ps.setString(cnt++, st.getBploApprover());
		ps.setString(cnt++, st.getBploPosition());
		ps.setString(cnt++, st.getInspectorate());
		ps.setString(cnt++, st.getTreasurer());
		ps.setString(cnt++, st.getMayor());
		ps.setString(cnt++, st.getEffectivityDate());
		ps.setInt(cnt++, st.getGenderReq());
		ps.setString(cnt++, st.getBusinessStart());
		ps.setString(cnt++, st.getLineOfBusiness());
		ps.setString(cnt++, st.getOrNumber());
		ps.setInt(cnt++, st.getIsCompleted());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateFilling());
		LogU.add(st.getDateApproved());
		LogU.add(st.getOldBusinessName());
		LogU.add(st.getNewBusinessName());
		LogU.add(st.getOldOwner());
		LogU.add(st.getNewOwner());
		LogU.add(st.getOldAddress());
		LogU.add(st.getNewAddress());
		LogU.add(st.getType());
		LogU.add(st.getReason());
		LogU.add(st.getDateStopOperation());
		LogU.add(st.getOfficer());
		LogU.add(st.getOfficerPosition());
		LogU.add(st.getBploApprover());
		LogU.add(st.getBploPosition());
		LogU.add(st.getInspectorate());
		LogU.add(st.getTreasurer());
		LogU.add(st.getMayor());
		LogU.add(st.getEffectivityDate());
		LogU.add(st.getGenderReq());
		LogU.add(st.getBusinessStart());
		LogU.add(st.getLineOfBusiness());
		LogU.add(st.getOrNumber());
		LogU.add(st.getIsCompleted());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to businessrequest : " + s.getMessage());
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
		sql="SELECT reqid FROM businessrequest  ORDER BY reqid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("reqid");
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
		ps = conn.prepareStatement("SELECT reqid FROM businessrequest WHERE reqid=?");
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
		String sql = "UPDATE businessrequest set isactivereq=0 WHERE reqid=?";
		
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
