package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.ClearanceType;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.Clearance;
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
public class MultiLivelihood {

	private long id;
	private int isActive;
	private Timestamp timestamp;
	
	private Livelihood livelihood;
	private Clearance clearance;
	
	public static List<MultiLivelihood> retrieve(String sqlAdd, String[] params){
		List<MultiLivelihood> mults = new ArrayList<>();
		
		String tableMulti = "mu";
		String tableLv = "lv";
		String tableClr = "cl";
		String sql ="SELECT * FROM multiLivelihood " + tableMulti + ", livelihood " + tableLv + ", clearance " + tableClr + " WHERE " +
		tableMulti + ".livelihoodid=" + tableLv + ".livelihoodid AND " +
		tableMulti + ".clearid=" + tableClr + ".clearid ";
		
		sql = sql + sqlAdd;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL " + ps.toString());
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			MultiLivelihood li = new MultiLivelihood();
			try{li.setId(rs.getLong("multilivid"));}catch(NullPointerException e){}
			try{li.setIsActive(rs.getInt("isactivemultiliv"));}catch(NullPointerException e){}
			
			Clearance cl = new Clearance();
			try{cl.setId(rs.getLong("clearid"));}catch(NullPointerException e){}
			try{cl.setIssuedDate(rs.getString("clearissueddate"));}catch(NullPointerException e){}
			try{cl.setBarcode(rs.getString("clearancebarcode"));}catch(NullPointerException e){}
			try{cl.setOrNumber(rs.getString("ornumber"));}catch(NullPointerException e){}
			try{cl.setCedulaNumber(rs.getString("cedulanumber"));}catch(NullPointerException e){}
			try{cl.setNotes(rs.getString("clearnotes"));}catch(NullPointerException e){}
			try{cl.setPhotoId(rs.getString("photoid"));}catch(NullPointerException e){}
			try{cl.setAmountPaid(rs.getDouble("amountpaid"));}catch(NullPointerException e){}
			try{cl.setPurposeType(rs.getInt("purposetype"));}catch(NullPointerException e){}
			try{cl.setStatus(rs.getInt("clearstatus"));}catch(NullPointerException e){}
			try{cl.setIsPayable(rs.getInt("payable"));}catch(NullPointerException e){}
			try{cl.setClearanceType(rs.getInt("cleartype"));}catch(NullPointerException e){}
			try{cl.setIsActive(rs.getInt("isactiveclearance"));}catch(NullPointerException e){}
			try{cl.setTimestamp(rs.getTimestamp("timestampclear"));}catch(NullPointerException e){}
			try{cl.setPurposeName(com.italia.municipality.lakesebu.enm.Purpose.typeName(rs.getInt("purposetype")));}catch(NullPointerException e){}
			try{cl.setTypeName(ClearanceType.typeName(rs.getInt("cleartype")));}catch(NullPointerException e){}
			try{cl.setDocumentType(rs.getInt("doctype"));}catch(NullPointerException e){}
			try{cl.setDocumentValidity(rs.getInt("doctvalid"));}catch(NullPointerException e){}
			li.setClearance(cl);
			
			Livelihood live = new Livelihood();
			try{live.setId(rs.getLong("livelihoodid"));}catch(NullPointerException e){}
			try{live.setDateRegistered(rs.getString("livedatereg"));}catch(NullPointerException e){}
			try{live.setDateRetired(rs.getString("liveretireddate"));}catch(NullPointerException e){}
			try{live.setBusinessName(rs.getString("livename"));}catch(NullPointerException e){}
			try{live.setPurokName(rs.getString("livepurok"));}catch(NullPointerException e){}
			try{live.setAreaMeter(rs.getString("liveareameter"));}catch(NullPointerException e){}
			try{live.setSupportingDetails(rs.getString("livedetails"));}catch(NullPointerException e){}
			try{live.setTypeLine(rs.getInt("livelihoodtype"));}catch(NullPointerException e){}
			try{live.setStatus(rs.getInt("livestatus"));}catch(NullPointerException e){}
			try{live.setIsActive(rs.getInt("isactivelive"));}catch(NullPointerException e){}
			try{live.setTimestamp(rs.getTimestamp("timestamplive"));}catch(NullPointerException e){}
			
			BusinessCustomer cus = new BusinessCustomer();
			try{cus.setId(rs.getLong("customerid"));}catch(NullPointerException e){}
			live.setTaxPayer(cus);
			
			li.setLivelihood(live);
			
			mults.add(li);

		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return mults;
	}
	
	public static MultiLivelihood save(MultiLivelihood mu){
		if(mu!=null){
			
			long id = MultiLivelihood.getInfo(mu.getId() ==0? MultiLivelihood.getLatestId()+1 : mu.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				mu = MultiLivelihood.insertData(mu, "1");
			}else if(id==2){
				LogU.add("update Data ");
				mu = MultiLivelihood.updateData(mu);
			}else if(id==3){
				LogU.add("added new Data ");
				mu = MultiLivelihood.insertData(mu, "3");
			}
			
		}
		return mu;
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
	
	public static MultiLivelihood insertData(MultiLivelihood mu, String type){
		String sql = "INSERT INTO multiLivelihood ("
				+ "multilivid,"
				+ "isactivemultiliv,"
				+ "livelihoodid,"
				+ "clearid)" 
				+ "values(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table multiLivelihood");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			mu.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			mu.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setInt(cnt++, mu.getIsActive());
		ps.setLong(cnt++, mu.getLivelihood()==null? 0 : mu.getLivelihood().getId());
		ps.setLong(cnt++, mu.getClearance()==null? 0 : mu.getClearance().getId());
		
		LogU.add(mu.getIsActive());
		LogU.add(mu.getLivelihood()==null? 0 : mu.getLivelihood().getId());
		LogU.add(mu.getClearance()==null? 0 : mu.getClearance().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to multiLivelihood : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return mu;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO multiLivelihood ("
				+ "multilivid,"
				+ "isactivemultiliv,"
				+ "livelihoodid,"
				+ "clearid)" 
				+ "values(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table multiLivelihood");
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
		
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getLivelihood()==null? 0 : getLivelihood().getId());
		ps.setLong(cnt++, getClearance()==null? 0 : getClearance().getId());
		
		LogU.add(getIsActive());
		LogU.add(getLivelihood()==null? 0 : getLivelihood().getId());
		LogU.add(getClearance()==null? 0 : getClearance().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to multiLivelihood : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static MultiLivelihood updateData(MultiLivelihood mu){
		String sql = "UPDATE multiLivelihood SET "
				+ "livelihoodid=?,"
				+ "clearid=?" 
				+ " WHERE multilivid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table multiLivelihood");
		
		
		
		ps.setLong(cnt++, mu.getLivelihood()==null? 0 : mu.getLivelihood().getId());
		ps.setLong(cnt++, mu.getClearance()==null? 0 : mu.getClearance().getId());
		ps.setLong(cnt++, mu.getId());
		
		LogU.add(mu.getLivelihood()==null? 0 : mu.getLivelihood().getId());
		LogU.add(mu.getClearance()==null? 0 : mu.getClearance().getId());
		LogU.add(mu.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to multiLivelihood : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return mu;
	}
	
	public void updateData(){
		String sql = "UPDATE multiLivelihood SET "
				+ "livelihoodid=?,"
				+ "clearid=?" 
				+ " WHERE multilivid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table multiLivelihood");
		
		
		
		ps.setLong(cnt++, getLivelihood()==null? 0 : getLivelihood().getId());
		ps.setLong(cnt++, getClearance()==null? 0 : getClearance().getId());
		ps.setLong(cnt++, getId());
		
		LogU.add(getLivelihood()==null? 0 : getLivelihood().getId());
		LogU.add(getClearance()==null? 0 : getClearance().getId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to multiLivelihood : " + s.getMessage());
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
		sql="SELECT multilivid FROM multiLivelihood  ORDER BY multilivid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("multilivid");
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
		ps = conn.prepareStatement("SELECT multilivid FROM multiLivelihood WHERE multilivid=?");
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
		String sql = "UPDATE multiLivelihood set isactivemultiliv=0 WHERE multilivid=?";
		
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
