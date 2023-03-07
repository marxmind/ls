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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @since 1/2/2019
 * @version 1.0
 * 
 * @apiNote This is the fish cage owner class
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class FishCage {

	private long id;
	private String waterSurveyNo;
	private String dateRegister;
	private String ownerName;
	private String address;
	private String tenantOwner;
	private String cageArea;
	private String totalSquareArea;
	private String location;
	private int isActive;
	private String remarks;
	private double amountDue;
	private int yearApplied;
	private String rsbsaFishR;
	
	private int motorizedBoat;
	private int nonMotorizedBoat;
	private String cellphoneNo;
	private int numberOfFishCages;
	private int noOfFunctional;
	private int noOfNonFunctional;
	private String sizeCagePerModule;
	private int noOfAnnualProduction;
	private int noOfTotalStock;
	private String barangay;
	
	public static List<FishCage> retrieve(String sqlAdd, String[] params){
		List<FishCage> fs = new ArrayList<FishCage>();
		
		String sql = "SELECT * FROM cageowner WHERE isactiveowner=1  ";
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
		
		System.out.println("Fish cage SQL " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
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
					.barangay(rs.getString("barangay"))
					.build();
			fs.add(cg);
			
		}
		
		rs.close();
		ps.close();
		AgricultureConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return fs;
	}
	
	public static FishCage save(FishCage fs){
		if(fs!=null){
			
			long id = FishCage.getInfo(fs.getId() ==0? FishCage.getLatestId()+1 : fs.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				fs = FishCage.insertData(fs, "1");
			}else if(id==2){
				LogU.add("update Data ");
				fs = FishCage.updateData(fs);
			}else if(id==3){
				LogU.add("added new Data ");
				fs = FishCage.insertData(fs, "3");
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
	
	public static FishCage insertData(FishCage fs, String type){
		String sql = "INSERT INTO cageowner ("
				+ "cid,"
				+ "watersurveyno,"
				+ "datereg,"
				+ "owner,"
				+ "tenantowner,"
				+ "cagearea,"
				+ "totalsquarearea,"
				+ "arealocation,"
				+ "ownerremarks,"
				+ "amountdue,"
				+ "isactiveowner,"
				+ "yearapplied,"
				+ "motorizedboat,"
				+ "nonmotorizedboat,"
				+ "cellphoneno,"
				+ "noofcages,"
				+ "functional,"
				+ "nonfunctional,"
				+ "annprod,"
				+ "totalstock,"
				+ "sizecagemodule,"
				+ "owneraddress,"
				+ "rsbsa,"
				+ "barangay)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cageowner");
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
		
		ps.setString(cnt++, fs.getWaterSurveyNo());
		ps.setString(cnt++, fs.getDateRegister());
		ps.setString(cnt++, fs.getOwnerName());
		ps.setString(cnt++, fs.getTenantOwner());
		ps.setString(cnt++, fs.getCageArea());
		ps.setString(cnt++, fs.getTotalSquareArea());
		ps.setString(cnt++, fs.getLocation());
		ps.setString(cnt++, fs.getRemarks());
		ps.setDouble(cnt++, fs.getAmountDue());
		ps.setInt(cnt++, fs.getIsActive());
		ps.setInt(cnt++, fs.getYearApplied());
		ps.setInt(cnt++, fs.getMotorizedBoat());
		ps.setInt(cnt++, fs.getNonMotorizedBoat());
		ps.setString(cnt++, fs.getCellphoneNo());
		ps.setInt(cnt++, fs.getNumberOfFishCages());
		ps.setInt(cnt++, fs.getNoOfFunctional());
		ps.setInt(cnt++, fs.getNoOfNonFunctional());
		ps.setInt(cnt++, fs.getNoOfAnnualProduction());
		ps.setInt(cnt++, fs.getNoOfTotalStock());
		ps.setString(cnt++, fs.getSizeCagePerModule());
		ps.setString(cnt++, fs.getAddress());
		ps.setString(cnt++, fs.getRsbsaFishR());
		ps.setString(cnt++, fs.getBarangay());
		
		LogU.add(fs.getId());
		LogU.add(fs.getWaterSurveyNo());
		LogU.add(fs.getDateRegister());
		LogU.add(fs.getOwnerName());
		LogU.add(fs.getTenantOwner());
		LogU.add(fs.getCageArea());
		LogU.add(fs.getTotalSquareArea());
		LogU.add(fs.getLocation());
		LogU.add(fs.getRemarks());
		LogU.add(fs.getAmountDue());
		LogU.add(fs.getIsActive());
		LogU.add(fs.getYearApplied());
		LogU.add(fs.getMotorizedBoat());
		LogU.add(fs.getNonMotorizedBoat());
		LogU.add(fs.getCellphoneNo());
		LogU.add(fs.getNumberOfFishCages());
		LogU.add(fs.getNoOfFunctional());
		LogU.add(fs.getNoOfNonFunctional());
		LogU.add(fs.getNoOfAnnualProduction());
		LogU.add(fs.getNoOfTotalStock());
		LogU.add(fs.getSizeCagePerModule());
		LogU.add(fs.getAddress());
		LogU.add(fs.getRsbsaFishR());
		LogU.add(fs.getBarangay());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to cageowner : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return fs;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO cageowner ("
				+ "cid,"
				+ "watersurveyno,"
				+ "datereg,"
				+ "owner,"
				+ "tenantowner,"
				+ "cagearea,"
				+ "totalsquarearea,"
				+ "arealocation,"
				+ "ownerremarks,"
				+ "amountdue,"
				+ "isactiveowner,"
				+ "yearapplied,"
				+ "motorizedboat,"
				+ "nonmotorizedboat,"
				+ "cellphoneno,"
				+ "noofcages,"
				+ "functional,"
				+ "nonfunctional,"
				+ "annprod,"
				+ "totalstock,"
				+ "sizecagemodule,"
				+ "owneraddress,"
				+ "rsbsa,"
				+ "barangay)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cageowner");
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
		
		ps.setString(cnt++, getWaterSurveyNo());
		ps.setString(cnt++, getDateRegister());
		ps.setString(cnt++, getOwnerName());
		ps.setString(cnt++, getTenantOwner());
		ps.setString(cnt++, getCageArea());
		ps.setString(cnt++, getTotalSquareArea());
		ps.setString(cnt++, getLocation());
		ps.setString(cnt++, getRemarks());
		ps.setDouble(cnt++, getAmountDue());
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getYearApplied());
		ps.setInt(cnt++, getMotorizedBoat());
		ps.setInt(cnt++, getNonMotorizedBoat());
		ps.setString(cnt++, getCellphoneNo());
		ps.setInt(cnt++, getNumberOfFishCages());
		ps.setInt(cnt++, getNoOfFunctional());
		ps.setInt(cnt++, getNoOfNonFunctional());
		ps.setInt(cnt++, getNoOfAnnualProduction());
		ps.setInt(cnt++, getNoOfTotalStock());
		ps.setString(cnt++, getSizeCagePerModule());
		ps.setString(cnt++, getAddress());
		ps.setString(cnt++, getRsbsaFishR());
		ps.setString(cnt++, getBarangay());
		
		LogU.add(getId());
		LogU.add(getWaterSurveyNo());
		LogU.add(getDateRegister());
		LogU.add(getOwnerName());
		LogU.add(getTenantOwner());
		LogU.add(getCageArea());
		LogU.add(getTotalSquareArea());
		LogU.add(getLocation());
		LogU.add(getRemarks());
		LogU.add(getAmountDue());
		LogU.add(getIsActive());
		LogU.add(getYearApplied());
		LogU.add(getMotorizedBoat());
		LogU.add(getNonMotorizedBoat());
		LogU.add(getCellphoneNo());
		LogU.add(getNumberOfFishCages());
		LogU.add(getNoOfFunctional());
		LogU.add(getNoOfNonFunctional());
		LogU.add(getNoOfAnnualProduction());
		LogU.add(getNoOfTotalStock());
		LogU.add(getSizeCagePerModule());
		LogU.add(getAddress());
		LogU.add(getRsbsaFishR());
		LogU.add(getBarangay());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to cageowner : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static FishCage updateData(FishCage fs){
		String sql = "UPDATE cageowner SET "
				+ "watersurveyno=?,"
				+ "datereg=?,"
				+ "owner=?,"
				+ "tenantowner=?,"
				+ "cagearea=?,"
				+ "totalsquarearea=?,"
				+ "arealocation=?,"
				+ "ownerremarks=?,"
				+ "amountdue=?,"
				+ "yearapplied=?,"
				+ "motorizedboat=?,"
				+ "nonmotorizedboat=?,"
				+ "cellphoneno=?,"
				+ "noofcages=?,"
				+ "functional=?,"
				+ "nonfunctional=?,"
				+ "annprod=?,"
				+ "totalstock=?,"
				+ "sizecagemodule=?,"
				+ "owneraddress=?,"
				+ "rsbsa=?,"
				+ "barangay=? "
				
				+ " WHERE cid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cageowner");
		
		ps.setString(cnt++, fs.getWaterSurveyNo());
		ps.setString(cnt++, fs.getDateRegister());
		ps.setString(cnt++, fs.getOwnerName());
		ps.setString(cnt++, fs.getTenantOwner());
		ps.setString(cnt++, fs.getCageArea());
		ps.setString(cnt++, fs.getTotalSquareArea());
		ps.setString(cnt++, fs.getLocation());
		ps.setString(cnt++, fs.getRemarks());
		ps.setDouble(cnt++, fs.getAmountDue());
		ps.setInt(cnt++, fs.getYearApplied());
		ps.setInt(cnt++, fs.getMotorizedBoat());
		ps.setInt(cnt++, fs.getNonMotorizedBoat());
		ps.setString(cnt++, fs.getCellphoneNo());
		ps.setInt(cnt++, fs.getNumberOfFishCages());
		ps.setInt(cnt++, fs.getNoOfFunctional());
		ps.setInt(cnt++, fs.getNoOfNonFunctional());
		ps.setInt(cnt++, fs.getNoOfAnnualProduction());
		ps.setInt(cnt++, fs.getNoOfTotalStock());
		ps.setString(cnt++, fs.getSizeCagePerModule());
		ps.setString(cnt++, fs.getAddress());
		ps.setString(cnt++, fs.getRsbsaFishR());
		ps.setString(cnt++, fs.getBarangay());
		ps.setLong(cnt++, fs.getId());
		
		
		LogU.add(fs.getWaterSurveyNo());
		LogU.add(fs.getDateRegister());
		LogU.add(fs.getOwnerName());
		LogU.add(fs.getTenantOwner());
		LogU.add(fs.getCageArea());
		LogU.add(fs.getTotalSquareArea());
		LogU.add(fs.getLocation());
		LogU.add(fs.getRemarks());
		LogU.add(fs.getAmountDue());
		LogU.add(fs.getYearApplied());
		LogU.add(fs.getMotorizedBoat());
		LogU.add(fs.getNonMotorizedBoat());
		LogU.add(fs.getCellphoneNo());
		LogU.add(fs.getNumberOfFishCages());
		LogU.add(fs.getNoOfFunctional());
		LogU.add(fs.getNoOfNonFunctional());
		LogU.add(fs.getNoOfAnnualProduction());
		LogU.add(fs.getNoOfTotalStock());
		LogU.add(fs.getSizeCagePerModule());
		LogU.add(fs.getAddress());
		LogU.add(fs.getRsbsaFishR());
		LogU.add(fs.getBarangay());
		LogU.add(fs.getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to cageowner : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return fs;
	}
	
	public void updateData(){
		String sql = "UPDATE cageowner SET "
				+ "watersurveyno=?,"
				+ "datereg=?,"
				+ "owner=?,"
				+ "tenantowner=?,"
				+ "cagearea=?,"
				+ "totalsquarearea=?,"
				+ "arealocation=?,"
				+ "ownerremarks=?,"
				+ "amountdue=?,"
				+ "yearapplied=?,"
				+ "motorizedboat=?,"
				+ "nonmotorizedboat=?,"
				+ "cellphoneno=?,"
				+ "noofcages=?,"
				+ "functional=?,"
				+ "nonfunctional=?,"
				+ "annprod=?,"
				+ "totalstock=?,"
				+ "sizecagemodule=?,"
				+ "owneraddress=?,"
				+ "rsbsa=?,"
				+ "barangay=? "
				
				+ " WHERE cid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = AgricultureConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cageowner");
		
		ps.setString(cnt++, getWaterSurveyNo());
		ps.setString(cnt++, getDateRegister());
		ps.setString(cnt++, getOwnerName());
		ps.setString(cnt++, getTenantOwner());
		ps.setString(cnt++, getCageArea());
		ps.setString(cnt++, getTotalSquareArea());
		ps.setString(cnt++, getLocation());
		ps.setString(cnt++, getRemarks());
		ps.setDouble(cnt++, getAmountDue());
		ps.setInt(cnt++, getYearApplied());
		ps.setInt(cnt++, getMotorizedBoat());
		ps.setInt(cnt++, getNonMotorizedBoat());
		ps.setString(cnt++, getCellphoneNo());
		ps.setInt(cnt++, getNumberOfFishCages());
		ps.setInt(cnt++, getNoOfFunctional());
		ps.setInt(cnt++, getNoOfNonFunctional());
		ps.setInt(cnt++, getNoOfAnnualProduction());
		ps.setInt(cnt++, getNoOfTotalStock());
		ps.setString(cnt++, getSizeCagePerModule());
		ps.setString(cnt++, getAddress());
		ps.setString(cnt++, getRsbsaFishR());
		ps.setString(cnt++, getBarangay());
		ps.setLong(cnt++, getId());
		
		
		LogU.add(getWaterSurveyNo());
		LogU.add(getDateRegister());
		LogU.add(getOwnerName());
		LogU.add(getTenantOwner());
		LogU.add(getCageArea());
		LogU.add(getTotalSquareArea());
		LogU.add(getLocation());
		LogU.add(getRemarks());
		LogU.add(getAmountDue());
		LogU.add(getYearApplied());
		LogU.add(getMotorizedBoat());
		LogU.add(getNonMotorizedBoat());
		LogU.add(getCellphoneNo());
		LogU.add(getNumberOfFishCages());
		LogU.add(getNoOfFunctional());
		LogU.add(getNoOfNonFunctional());
		LogU.add(getNoOfAnnualProduction());
		LogU.add(getNoOfTotalStock());
		LogU.add(getSizeCagePerModule());
		LogU.add(getAddress());
		LogU.add(getRsbsaFishR());
		LogU.add(getBarangay());
		LogU.add(getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		AgricultureConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to cageowner : " + s.getMessage());
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
		sql="SELECT cid FROM cageowner  ORDER BY cid DESC LIMIT 1";	
		conn = AgricultureConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("cid");
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
		ps = conn.prepareStatement("SELECT cid FROM cageowner WHERE cid=?");
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
		String sql = "UPDATE cageowner set isactiveowner=0 WHERE cid=?";
		
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