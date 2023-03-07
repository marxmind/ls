package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.AgricultureConnect;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class FishCagePayment {
	
	private long id;
	private String paymentPaid;
	private int year;
	private String orNumber;
	private double amountPaid;
	private double payableAmount;
	private int fullpaid;
	private int isActive;
	
	private FishCage fishCage;
	
	public static List<FishCagePayment> retrieve(String sqlAdd, String[] params){
		List<FishCagePayment> pys = new ArrayList<FishCagePayment>();
		
		String tablePy="py";
		String tableOwner="own";
		
		String sql = "SELECT * FROM fishcagepayment "+ tablePy +", cageowner "+ tableOwner +" WHERE "+tablePy+".isactivecase=1  "
				+ "AND " + tablePy + ".cid=" + tableOwner + ".cid ";
		 sql = sql + sqlAdd;				
				
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		System.out.println("Fish cage payment SQL " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			FishCagePayment py = FishCagePayment.builder()
					.id(rs.getLong("fid"))
					.paymentPaid(rs.getString("paymentDate"))
					.year(rs.getInt("yearPaid"))
					.orNumber(rs.getString("orNumber"))
					.amountPaid(rs.getDouble("amountPaid"))
					.payableAmount(rs.getDouble("payableAmnt"))
					.fullpaid(rs.getInt("fullpaid"))
					.isActive(rs.getInt("isactivecase"))
					.build();
			
			FishCage cg = FishCage.builder()
					.id(rs.getLong("cid"))
					.dateRegister(rs.getString("datereg"))
					.waterSurveyNo(rs.getString("watersurveyno"))
					.ownerName(rs.getString("owner"))
					.tenantOwner(rs.getString("tenantowner"))
					.cageArea(rs.getString("cagearea"))
					.totalSquareArea(rs.getString("totalsquarearea"))
					.location(rs.getString("arealocation"))
					.remarks(rs.getString("ownerremarks"))
					.amountDue(rs.getDouble("amountdue"))
					.isActive(rs.getInt("isactiveowner"))
					.yearApplied(rs.getInt("yearapplied"))
					.motorizedBoat(rs.getInt("motorizedboat"))
					.nonMotorizedBoat(rs.getInt("nonmotorizedboat"))
					.cellphoneNo(rs.getString("cellphoneno"))
					.numberOfFishCages(rs.getInt("noofcages"))
					.noOfFunctional(rs.getInt("functional"))
					.noOfNonFunctional(rs.getInt("nonfunctional"))
					.noOfAnnualProduction(rs.getInt("annprod"))
					.noOfTotalStock(rs.getInt("totalstock"))
					.sizeCagePerModule(rs.getString("sizecagemodule"))
					.address(rs.getString("owneraddress"))
					.rsbsaFishR(rs.getString("rsbsa"))
					.build();
			py.setFishCage(cg);
			pys.add(py);
			
		}
		
		rs.close();
		ps.close();
		AgricultureConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return pys;
	}
	
	public static FishCagePayment save(FishCagePayment fs){
		if(fs!=null){
			
			long id = FishCagePayment.getInfo(fs.getId() ==0? FishCagePayment.getLatestId()+1 : fs.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				fs = FishCagePayment.insertData(fs, "1");
			}else if(id==2){
				LogU.add("update Data ");
				fs = FishCagePayment.updateData(fs);
			}else if(id==3){
				LogU.add("added new Data ");
				fs = FishCagePayment.insertData(fs, "3");
			}
			
		}
		return fs;
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
	
	public static FishCagePayment insertData(FishCagePayment fs, String type){
		String sql = "INSERT INTO fishcagepayment ("
				+ "fid,"
				+ "paymentDate,"
				+ "yearPaid,"
				+ "orNumber,"
				+ "amountPaid,"
				+ "payableAmnt,"
				+ "fullpaid,"
				+ "isactivecase,"
				+ "cid)" 
				+ "values(?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table fishcagepayment");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			fs.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			fs.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, fs.getPaymentPaid());
		ps.setInt(cnt++, fs.getYear());
		ps.setString(cnt++, fs.getOrNumber());
		ps.setDouble(cnt++, fs.getAmountPaid());
		ps.setDouble(cnt++, fs.getPayableAmount());
		ps.setInt(cnt++, fs.getFullpaid());
		ps.setInt(cnt++, fs.getIsActive());
		ps.setLong(cnt++, fs.getFishCage()==null? 0 : fs.getFishCage().getId());
		
		LogU.add(fs.getPaymentPaid());
		LogU.add(fs.getYear());
		LogU.add(fs.getOrNumber());
		LogU.add(fs.getAmountPaid());
		LogU.add(fs.getPayableAmount());
		LogU.add(fs.getFullpaid());
		LogU.add(fs.getIsActive());
		LogU.add(fs.getFishCage()==null? 0 : fs.getFishCage().getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to fishcagepayment : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return fs;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO fishcagepayment ("
				+ "fid,"
				+ "paymentDate,"
				+ "yearPaid,"
				+ "orNumber,"
				+ "amountPaid,"
				+ "payableAmnt,"
				+ "fullpaid,"
				+ "isactivecase,"
				+ "cid)" 
				+ "values(?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table fishcagepayment");
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
		
		ps.setString(cnt++, getPaymentPaid());
		ps.setInt(cnt++, getYear());
		ps.setString(cnt++, getOrNumber());
		ps.setDouble(cnt++, getAmountPaid());
		ps.setDouble(cnt++, getPayableAmount());
		ps.setInt(cnt++, getFullpaid());
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getFishCage()==null? 0 : getFishCage().getId());
		
		LogU.add(getPaymentPaid());
		LogU.add(getYear());
		LogU.add(getOrNumber());
		LogU.add(getAmountPaid());
		LogU.add(getPayableAmount());
		LogU.add(getFullpaid());
		LogU.add(getIsActive());
		LogU.add(getFishCage()==null? 0 : getFishCage().getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to fishcagepayment : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static FishCagePayment updateData(FishCagePayment fs){
		String sql = "UPDATE fishcagepayment SET "
				+ "paymentDate=?,"
				+ "yearPaid=?,"
				+ "orNumber=?,"
				+ "amountPaid=?,"
				+ "payableAmnt=?,"
				+ "fullpaid=?,"
				+ "cid=?" 
				+ " WHERE fid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table fishcagepayment");
		
		ps.setString(cnt++, fs.getPaymentPaid());
		ps.setInt(cnt++, fs.getYear());
		ps.setString(cnt++, fs.getOrNumber());
		ps.setDouble(cnt++, fs.getAmountPaid());
		ps.setDouble(cnt++, fs.getPayableAmount());
		ps.setInt(cnt++, fs.getFullpaid());
		ps.setLong(cnt++, fs.getFishCage()==null? 0 : fs.getFishCage().getId());
		ps.setLong(cnt++, fs.getId());
		
		LogU.add(fs.getPaymentPaid());
		LogU.add(fs.getYear());
		LogU.add(fs.getOrNumber());
		LogU.add(fs.getAmountPaid());
		LogU.add(fs.getPayableAmount());
		LogU.add(fs.getFullpaid());
		LogU.add(fs.getFishCage()==null? 0 : fs.getFishCage().getId());
		LogU.add(fs.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to fishcagepayment : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return fs;
	}
	
	public void updateData(){
		String sql = "UPDATE fishcagepayment SET "
				+ "paymentDate=?,"
				+ "yearPaid=?,"
				+ "orNumber=?,"
				+ "amountPaid=?,"
				+ "payableAmnt=?,"
				+ "fullpaid=?,"
				+ "cid=?" 
				+ " WHERE fid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table fishcagepayment");
		
		ps.setString(cnt++, getPaymentPaid());
		ps.setInt(cnt++, getYear());
		ps.setString(cnt++, getOrNumber());
		ps.setDouble(cnt++, getAmountPaid());
		ps.setDouble(cnt++, getPayableAmount());
		ps.setInt(cnt++, getFullpaid());
		ps.setLong(cnt++, getFishCage()==null? 0 : getFishCage().getId());
		ps.setLong(cnt++, getId());
		
		LogU.add(getPaymentPaid());
		LogU.add(getYear());
		LogU.add(getOrNumber());
		LogU.add(getAmountPaid());
		LogU.add(getPayableAmount());
		LogU.add(getFullpaid());
		LogU.add(getFishCage()==null? 0 : getFishCage().getId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to fishcagepayment : " + s.getMessage());
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
		sql="SELECT fid FROM fishcagepayment  ORDER BY fid DESC LIMIT 1";	
		conn = AgricultureConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("fid");
		}
		
		rs.close();
		prep.close();
		AgricultureConnect.close(conn);
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
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement("SELECT fid FROM fishcagepayment WHERE fid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		AgricultureConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		AgricultureConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE fishcagepayment set isactivecase=0 WHERE fid=?";
		
		String[] params = new String[1];
		params[0] = getId()+"";
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		AgricultureConnect.close(conn);
		}catch(SQLException s){}
		
	}
}
