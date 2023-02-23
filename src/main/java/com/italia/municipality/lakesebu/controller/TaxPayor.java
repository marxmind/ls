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
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 11/17/2013
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TaxPayor implements ITaxPayor{
	
	private long id;
	private String fullName; 
	private String address;
	private int isactive;
	private Timestamp timestamp;
	private Barangay barangay;
	private UserDtls userDtls;
	
	private List<LandPayor> landPayor;

	public static List<ITaxPayor> retrieve(String sql, String[] params){
		List<ITaxPayor> pays = new ArrayList<ITaxPayor>();
		
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
		
		//System.out.println("SQL Payor : " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			ITaxPayor payor = new TaxPayor();
			String address = null;
			try{payor.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{address = rs.getString("payoraddress");}catch(NullPointerException e){}
			try{payor.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			try{payor.setTimestamp(rs.getTimestamp("paytimestamp"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			payor.setUserDtls(user);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			
			if(address==null){
				payor.setAddress(rs.getString("bgname") + ", Lake Sebu, South Cotabato");
			}else if("null".equalsIgnoreCase(address)){
				payor.setAddress(rs.getString("bgname") + ", Lake Sebu, South Cotabato");
			}else{
				payor.setAddress(rs.getString("payoraddress"));
			}
			
			payor.setBarangay(bar);
			
			pays.add(payor);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pays;
	}
	
	public static List<ITaxPayor> retrievePayorName(String sql, String[] params){
		List<ITaxPayor> pays = new ArrayList<ITaxPayor>();
		
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
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			ITaxPayor payor = new TaxPayor();
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			pays.add(payor);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pays;
	}
	
	public static List<ITaxPayor> retrievePayor(String sql, String[] params){
		List<ITaxPayor> pays = new ArrayList<ITaxPayor>();
		
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
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			ITaxPayor payor = new TaxPayor();
			payor.setId(rs.getLong("payorid"));
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			payor.setAddress(rs.getString("payoraddress"));
			pays.add(payor);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pays;
	}
	
	public static TaxPayor retrievePayor(String name){
		String sql="SELECT * FROM taxpayor WHERE payisactive=1 AND payorname=" + name;
		TaxPayor payor = new TaxPayor();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			payor.setId(rs.getLong("payorid"));
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			payor.setAddress(rs.getString("payoraddress"));
			payor.setIsactive(rs.getInt("payisactive"));
			
			Barangay barangay = new Barangay();
			barangay.setId(rs.getInt("bgid"));
			payor.setBarangay(barangay);
			
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return payor;
	}
	
	public static ITaxPayor save(ITaxPayor pay){
		if(pay!=null){
			long id = getInfo(pay.getId()==0? getLatestId()+1 : pay.getId());
			if(id==1){
				pay = TaxPayor.insertData(pay, "1");
			}else if(id==2){
				pay = TaxPayor.updateData(pay);
			}else if(id==3){
				pay = TaxPayor.insertData(pay, "3");
			}
			
		}
		return pay;
	}
	
	@Override
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
	
	
	public static ITaxPayor insertData(ITaxPayor pay, String type){
		
		String sql = "INSERT INTO taxpayor ("
				+ "payorid,"
				+ "payorname,"
				+ "payoraddress,"
				+ "payisactive,"
				+ "bgid,"
				+ "userdtlsid) " 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table taxpayor");	
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
		ps.setString(2, pay.getFullName());
		ps.setString(3, pay.getAddress());
		ps.setInt(4, pay.getIsactive());
		ps.setInt(5, pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		ps.setLong(6, pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		
		LogU.add(pay.getFullName());
		LogU.add(pay.getAddress());
		LogU.add(pay.getIsactive());
		LogU.add(pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		LogU.add(pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table taxpayor successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table taxpayor.");
		}
		
		return pay;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO taxpayor ("
				+ "payorid,"
				+ "payorname,"
				+ "payoraddress,"
				+ "payisactive,"
				+ "bgid,"
				+ "userdtlsid) " 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table taxpayor");	
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
		ps.setString(2, getFullName());
		ps.setString(3, getAddress());
		ps.setInt(4, getIsactive());
		ps.setInt(5, getBarangay()==null? 1 : getBarangay().getId());
		ps.setLong(6, getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		
		LogU.add(getFullName());
		LogU.add(getAddress());
		LogU.add(getIsactive());
		LogU.add(getBarangay()==null? 1 : getBarangay().getId());
		LogU.add(getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table taxpayor successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table taxpayor.");
		}
		
	}
	
	public static ITaxPayor updateData(ITaxPayor pay){
		String sql = "UPDATE taxpayor SET "
				+ "payorname=?,"
				+ "payoraddress=?,"
				+ "bgid=?,"
				+ "userdtlsid=? "
				+ " WHERE payorid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Update into table taxpayor");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		ps.setString(1, pay.getFullName());
		ps.setString(2, pay.getAddress());
		ps.setInt(3, pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		ps.setLong(4, pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		ps.setLong(5, pay.getId());
		
		System.out.println("SQL uPDATE : " + ps.toString());
		
		LogU.add(pay.getFullName());
		LogU.add(pay.getAddress());
		LogU.add(pay.getBarangay()==null? 1 : pay.getBarangay().getId());
		LogU.add(pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		LogU.add(pay.getId());
		
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Update into table taxpayor successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error update into table taxpayor.");
		}
		
		return pay;
	}
	
	public void updateData(){
		String sql = "UPDATE taxpayor SET "
				+ "payorname=?,"
				+ "payoraddress=?,"
				+ "bgid=?,"
				+ "userdtlsid=? "
				+ " WHERE payorid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Update into table taxpayor");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		ps.setString(1, getFullName());
		ps.setString(2, getAddress());
		ps.setInt(3, getBarangay()==null? 1 : getBarangay().getId());
		ps.setLong(4, getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		ps.setLong(5, getId());
		
		LogU.add(getFullName());
		LogU.add(getAddress());
		LogU.add(getBarangay()==null? 1 : getBarangay().getId());
		LogU.add(getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
		LogU.add(getId());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Update into table taxpayor successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error update into table taxpayor.");
		}
		
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT payorid FROM taxpayor  ORDER BY payorid DESC LIMIT 1";	
		conn = TaxDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("payorid");
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
		ps = conn.prepareStatement("SELECT payorid FROM taxpayor WHERE payorid=?");
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
	
	@Override
	public void delete(){
		String sql = "update taxpayor set payisactive=0, userdtlsid=? WHERE payorid=?";
		String params[] = new String[2];
		params[0] = Login.getUserLogin().getUserDtls().getUserdtlsid()+"";
		params[1] = getId()+"";
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		LogU.add("Deleting data in taxpayor where id="+getId());	
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

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub
		this.id = id;
	}

	@Override
	public String getFullName() {
		// TODO Auto-generated method stub
		return fullName;
	}

	@Override
	public void setFullName(String fullName) {
		// TODO Auto-generated method stub
		this.fullName = fullName;
	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return address;
	}

	@Override
	public void setAddress(String address) {
		// TODO Auto-generated method stub
		this.address = address;
	}

	@Override
	public int getIsactive() {
		// TODO Auto-generated method stub
		return isactive;
	}

	@Override
	public void setIsactive(int isactive) {
		// TODO Auto-generated method stub
		this.isactive = isactive;
	}

	@Override
	public Timestamp getTimestamp() {
		// TODO Auto-generated method stub
		return timestamp;
	}

	@Override
	public void setTimestamp(Timestamp timestamp) {
		// TODO Auto-generated method stub
		this.timestamp = timestamp;
	}
	
	public static void main(String[] args) {
		ITaxPayor pay = new TaxPayor();
		pay.setId(5);
		pay.setFullName("mark italia 13");
		pay.setAddress("lake sebu");
		pay.setIsactive(1);
		pay.delete();
		//pay = TaxPayor.save(pay);
		//System.out.println(pay.getFullName());
		
		String sql = "SELECT * FROM taxpayor";
		String[] params = new String[0];
		  for(ITaxPayor tax : TaxPayor.retrieve(sql, params)){
			 System.out.println(tax.getAddress());
			 System.out.println(tax.getTimestamp());
		  }
	}

	@Override
	public Barangay getBarangay() {
		// TODO Auto-generated method stub
		return barangay;
	}

	@Override
	public void setBarangay(Barangay barangay) {
		// TODO Auto-generated method stub
		this.barangay = barangay;
				
	}

	@Override
	public UserDtls getUserDtls() {
		// TODO Auto-generated method stub
		return userDtls;
	}

	@Override
	public void setUserDtls(UserDtls userDtls) {
		// TODO Auto-generated method stub
		this.userDtls = userDtls;
	}
	
	@Override
	public List<LandPayor> getLandPayor() {
		return landPayor;
	}
	@Override
	public void setLandPayor(List<LandPayor> landPayor) {
		this.landPayor = landPayor;
	}
}
