package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
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
public class LandPayor {

	private long id;
	private String taxDeclarionNo;
	private String address;
	private double landValue;
	private String lotNo;
	private String remarks;
	private int isActive;
	private Barangay barangay;
	private int status;
	private ITaxPayor payor;
	private ILandType landType;
	private UserDtls userDtls;
	private Timestamp timestamp;
	private int isImprovment;
	
	public static List<LandPayor> retrieve(String sql, String[] params){
		List<LandPayor> lands = new ArrayList<LandPayor>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		System.out.println("SQL land: " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			LandPayor pay = new LandPayor();
			try{pay.setId(rs.getLong("payorlandid"));}catch(NullPointerException e){}
			try{pay.setTaxDeclarionNo(rs.getString("landtd"));}catch(NullPointerException e){}
			try{pay.setLandValue(rs.getDouble("landvalue"));}catch(NullPointerException e){}
			try{pay.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{pay.setLotNo(rs.getString("lotno"));}catch(NullPointerException e){}
			try{pay.setRemarks(rs.getString("landremarks"));}catch(NullPointerException e){}
			try{pay.setIsActive(rs.getInt("isactiveland"));}catch(NullPointerException e){}
			try{pay.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{pay.setStatus(rs.getInt("landstatus"));}catch(NullPointerException e){}
			try{pay.setIsImprovment(rs.getInt("isimprov"));}catch(NullPointerException e){}
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			pay.setBarangay(bar);
			
			TaxPayor payor = new TaxPayor();
			try{payor.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{payor.setAddress(rs.getString("payoraddress"));}catch(NullPointerException e){}
			try{payor.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			pay.setPayor(payor);
			
			ILandType type = new LandType();
			try{type.setId(rs.getInt("landid"));}catch(NullPointerException e){}
			try{type.setLandType(rs.getString("landtype"));}catch(NullPointerException e){}
			try{type.setTimestamp(rs.getTimestamp("landtimestamp"));}catch(NullPointerException e){}
			pay.setLandType(type);
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			pay.setUserDtls(user);
			
			lands.add(pay);
			
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return lands;
	}
	
	public static List<LandPayor> load(String sql, String[] params){
		List<LandPayor> lands = new ArrayList<LandPayor>();
		
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		String st = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
				+ "WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid ";
				sql = st + sql;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		System.out.println("SQL land: " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			LandPayor pay = new LandPayor();
			try{pay.setId(rs.getLong("payorlandid"));}catch(NullPointerException e){}
			try{pay.setTaxDeclarionNo(rs.getString("landtd"));}catch(NullPointerException e){}
			try{pay.setLandValue(rs.getDouble("landvalue"));}catch(NullPointerException e){}
			try{pay.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{pay.setLotNo(rs.getString("lotno"));}catch(NullPointerException e){}
			try{pay.setRemarks(rs.getString("landremarks"));}catch(NullPointerException e){}
			try{pay.setIsActive(rs.getInt("isactiveland"));}catch(NullPointerException e){}
			try{pay.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{pay.setStatus(rs.getInt("landstatus"));}catch(NullPointerException e){}
			try{pay.setIsImprovment(rs.getInt("isimprov"));}catch(NullPointerException e){}
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			pay.setBarangay(bar);
			
			TaxPayor payor = new TaxPayor();
			try{payor.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{payor.setAddress(rs.getString("payoraddress"));}catch(NullPointerException e){}
			try{payor.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			pay.setPayor(payor);
			
			ILandType type = new LandType();
			try{type.setId(rs.getInt("landid"));}catch(NullPointerException e){}
			try{type.setLandType(rs.getString("landtype"));}catch(NullPointerException e){}
			try{type.setTimestamp(rs.getTimestamp("landtimestamp"));}catch(NullPointerException e){}
			pay.setLandType(type);
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			pay.setUserDtls(user);
			
			lands.add(pay);
			
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return lands;
	}
	
	public static LandPayor retrieveLand(String tdNo, String owner){
		LandPayor pay = new LandPayor();
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		String sql = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
				+ "WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid " +
				"AND l.landtd=" + tdNo + " AND p.payorname=" + owner;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{pay.setId(rs.getLong("payorlandid"));}catch(NullPointerException e){}
			try{pay.setTaxDeclarionNo(rs.getString("landtd"));}catch(NullPointerException e){}
			try{pay.setLandValue(rs.getDouble("landvalue"));}catch(NullPointerException e){}
			try{pay.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{pay.setLotNo(rs.getString("lotno"));}catch(NullPointerException e){}
			try{pay.setRemarks(rs.getString("landremarks"));}catch(NullPointerException e){}
			try{pay.setIsActive(rs.getInt("isactiveland"));}catch(NullPointerException e){}
			try{pay.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{pay.setStatus(rs.getInt("landstatus"));}catch(NullPointerException e){}
			try{pay.setIsImprovment(rs.getInt("isimprov"));}catch(NullPointerException e){}
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			pay.setBarangay(bar);
			
			TaxPayor payor = new TaxPayor();
			try{payor.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{payor.setAddress(rs.getString("payoraddress"));}catch(NullPointerException e){}
			try{payor.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			pay.setPayor(payor);
			
			ILandType type = new LandType();
			try{type.setId(rs.getInt("landid"));}catch(NullPointerException e){}
			try{type.setLandType(rs.getString("landtype"));}catch(NullPointerException e){}
			try{type.setTimestamp(rs.getTimestamp("landtimestamp"));}catch(NullPointerException e){}
			pay.setLandType(type);
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			pay.setUserDtls(user);
			
		
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pay;
	}
	
	public static LandPayor save(LandPayor pay){
		if(pay!=null){
			long id = getInfo(pay.getId()==0? getLatestId()+1 : pay.getId());
			if(id==1){
				pay = LandPayor.insertData(pay, "1");
			}else if(id==2){
				pay = LandPayor.updateData(pay);
			}else if(id==3){
				pay = LandPayor.insertData(pay, "3");
			}
			
		}
		return pay;
	}
	
	
	public void save(){
		
			long id = getInfo(getId()==0? getLatestId()+1 : getId());
			if(id==1){
				insertData("1");
			}else if(id==2){
				updateData();
			}else if(id==3){
				insertData("3");
			}
			
		
	}
	
	public static LandPayor insertData(LandPayor pay, String type){
		
		String sql = "INSERT INTO payorland ("
				+ "payorlandid,"
				+ "landtd,"
				+ "address,"
				+ "lotno,"
				+ "landvalue,"
				+ "landremarks,"
				+ "isactiveland,"
				+ "landid,"
				+ "payorid,"
				+ "bgid,"
				+ "userdtlsid,"
				+ "landstatus,"
				+ "isimprov) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table payorland");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			pay.setId(id);
			LogU.add(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			pay.setId(id);
			LogU.add(id);
		}
		ps.setString(2, pay.getTaxDeclarionNo());
		ps.setString(3, pay.getAddress());
		ps.setString(4, pay.getLotNo());
		ps.setDouble(5, pay.getLandValue());
		ps.setString(6, pay.getRemarks());
		ps.setInt(7, pay.getIsActive());
		ps.setInt(8, pay.getLandType()==null? 1 : pay.getLandType().getId());
		ps.setLong(9, pay.getPayor()==null? 1 : pay.getPayor().getId());
		ps.setInt(10, pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		ps.setLong(11, pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		ps.setInt(12, pay.getStatus());
		ps.setInt(13, pay.getIsImprovment());
		
		LogU.add(pay.getTaxDeclarionNo());
		LogU.add(pay.getAddress());
		LogU.add(pay.getLotNo());
		LogU.add(pay.getLandValue());
		LogU.add(pay.getRemarks());
		LogU.add(pay.getIsActive());
		LogU.add(pay.getLandType()==null? 1 : pay.getLandType().getId());
		LogU.add(pay.getPayor()==null? 1 : pay.getPayor().getId());
		LogU.add(pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		LogU.add(pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		LogU.add(pay.getStatus());
		LogU.add(pay.getIsImprovment());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table payorland successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table payorland.");
		}
		
		return pay;
	}
	
 public void insertData(String type){
		
		String sql = "INSERT INTO payorland ("
				+ "payorlandid,"
				+ "landtd,"
				+ "address,"
				+ "lotno,"
				+ "landvalue,"
				+ "landremarks,"
				+ "isactiveland,"
				+ "landid,"
				+ "payorid,"
				+ "bgid,"
				+ "userdtlsid,"
				+ "landstatus,"
				+ "isimprov) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table payorland");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			setId(id);
			LogU.add(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			setId(id);
			LogU.add(id);
		}
		ps.setString(2, getTaxDeclarionNo());
		ps.setString(3, getAddress());
		ps.setString(4, getLotNo());
		ps.setDouble(5, getLandValue());
		ps.setString(6, getRemarks());
		ps.setInt(7, getIsActive());
		ps.setInt(8, getLandType()==null? 1 : getLandType().getId());
		ps.setLong(9, getPayor()==null? 1 : getPayor().getId());
		ps.setInt(10, getBarangay()==null? 1 : getBarangay().getId());
		ps.setLong(11, getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		ps.setInt(12, getStatus());
		ps.setInt(13, getIsImprovment());
		
		LogU.add(getTaxDeclarionNo());
		LogU.add(getAddress());
		LogU.add(getLotNo());
		LogU.add(getLandValue());
		LogU.add(getRemarks());
		LogU.add(getIsActive());
		LogU.add(getLandType()==null? 1 : getLandType().getId());
		LogU.add(getPayor()==null? 1 : getPayor().getId());
		LogU.add(getBarangay()==null? 1 : getBarangay().getId());
		LogU.add(getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		LogU.add(getStatus());
		LogU.add(getIsImprovment());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table payorland successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table payorland.");
		}
		
	}
	
 public static LandPayor updateData(LandPayor pay){
		
		String sql = "UPDATE payorland SET "
				+ "landtd=?,"
				+ "address=?,"
				+ "lotno=?,"
				+ "landvalue=?,"
				+ "landremarks=?,"
				+ "landid=?,"
				+ "payorid=?,"
				+ "bgid=?,"
				+ "userdtlsid=?,"
				+ "landstatus=?,"
				+ "isimprov=? " 
				+ " WHERE payorlandid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("update into table payorland");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		ps.setString(1, pay.getTaxDeclarionNo());
		ps.setString(2, pay.getAddress());
		ps.setString(3, pay.getLotNo());
		ps.setDouble(4, pay.getLandValue());
		ps.setString(5, pay.getRemarks());
		ps.setInt(6, pay.getLandType()==null? 1 : pay.getLandType().getId());
		ps.setLong(7, pay.getPayor()==null? 1 : pay.getPayor().getId());
		ps.setInt(8, pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		ps.setLong(9, pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		ps.setInt(10, pay.getStatus());
		ps.setInt(11, pay.getIsImprovment());
		ps.setLong(12, pay.getId());
		
		LogU.add(pay.getTaxDeclarionNo());
		LogU.add(pay.getAddress());
		LogU.add(pay.getLotNo());
		LogU.add(pay.getLandValue());
		LogU.add(pay.getRemarks());
		LogU.add(pay.getLandType()==null? 1 : pay.getLandType().getId());
		LogU.add(pay.getPayor()==null? 1 : pay.getPayor().getId());
		LogU.add(pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		LogU.add(pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		LogU.add(pay.getStatus());
		LogU.add(pay.getIsImprovment());
		LogU.add(pay.getId());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("update into table payorland successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error update into table payorland.");
		}
		
		return pay;
	}
 
 public void updateData(){
		
		String sql = "UPDATE payorland SET "
				+ "landtd=?,"
				+ "address=?,"
				+ "lotno=?,"
				+ "landvalue=?,"
				+ "landremarks=?,"
				+ "landid=?,"
				+ "payorid=?,"
				+ "bgid=?,"
				+ "userdtlsid=?,"
				+ "landstatus=?,"
				+ "isimprov=? " 
				+ " WHERE payorlandid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("update into table payorland");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		ps.setString(1, getTaxDeclarionNo());
		ps.setString(2, getAddress());
		ps.setString(3, getLotNo());
		ps.setDouble(4, getLandValue());
		ps.setString(5, getRemarks());
		ps.setInt(6, getLandType()==null? 1 : getLandType().getId());
		ps.setLong(7, getPayor()==null? 1 : getPayor().getId());
		ps.setInt(8, getBarangay()==null? 1 : getBarangay().getId());
		ps.setLong(9, getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		ps.setInt(10, getStatus());
		ps.setInt(11, getIsImprovment());
		ps.setLong(12, getId());
		
		LogU.add(getTaxDeclarionNo());
		LogU.add(getAddress());
		LogU.add(getLotNo());
		LogU.add(getLandValue());
		LogU.add(getRemarks());
		LogU.add(getLandType()==null? 1 : getLandType().getId());
		LogU.add(getPayor()==null? 1 : getPayor().getId());
		LogU.add(getBarangay()==null? 1 : getBarangay().getId());
		LogU.add(getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		LogU.add(getStatus());
		LogU.add(getIsImprovment());
		LogU.add(getId());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("update into table payorland successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error update into table payorland.");
		}
		
	}
 
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT payorlandid FROM payorland  ORDER BY payorlandid DESC LIMIT 1";	
		conn = TaxDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("payorlandid");
		}
		
		rs.close();
		prep.close();
		TaxDatabaseConnect.close(conn);
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
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT payorlandid FROM payorland WHERE payorlandid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
			
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
	
	
	public void delete(){
		String sql = "update payorland set isactiveland=0, userdtlsid=? WHERE payorlandid=?";
		String params[] = new String[2];
		params[0] = Login.getUserLogin().getUserDtls().getUserdtlsid()+"";
		params[1] = getId()+"";
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		LogU.add("Deleting data in payorland where id="+getId());	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
}
