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
import lombok.Data;
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
public class TaxPayorTrans implements ITaxPayorTrans {

	private long id;
	private String transDate;
	private double amount;
	private String amountInWords;
	private String scNo;
	private int accountFormNo;
	private int isactive;
	private String checkNo;
	private String checkDate;
	private String signed1;
	private String signed2;
	private ITaxPayor taxPayor;
	private Timestamp timestamp;
	private int status;
	private int paymentType;
	UserDtls userDtls;
	private int isSpecialCase;
	
	private LandPayor landPayor;
	
	/**
	 * Required to inner join taxpayor, taxpayortrans
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static List<ITaxPayorTrans> retrieve(String sql, String[] params){
		List<ITaxPayorTrans> pays = new ArrayList<ITaxPayorTrans>();
		
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
		
		System.out.println("ITaxPayorTrans SQL: " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			ITaxPayorTrans pay = new TaxPayorTrans();
			
			try{pay.setId(rs.getLong("payortransid"));}catch(NullPointerException e){}
			try{pay.setTransDate(rs.getString("payortransdate"));}catch(NullPointerException e){}
			try{pay.setAmount(rs.getDouble("payortransamount"));}catch(NullPointerException e){}
			try{pay.setAmountInWords(rs.getString("payortransamountinwords"));}catch(NullPointerException e){}
			try{pay.setScNo(rs.getString("payorscno"));}catch(NullPointerException e){}
			try{pay.setAccountFormNo(rs.getInt("payoraccntform"));}catch(NullPointerException e){}
			try{pay.setIsactive(rs.getInt("paytransisactive"));}catch(NullPointerException e){}
			try{pay.setCheckNo(rs.getString("paycheckno"));}catch(NullPointerException e){}
			try{pay.setCheckDate(rs.getString("paycheckdate"));}catch(NullPointerException e){}
			try{pay.setSigned1(rs.getString("paysigned1"));}catch(NullPointerException e){}
			try{pay.setSigned2(rs.getString("paysigned2"));}catch(NullPointerException e){}
			try{pay.setTimestamp(rs.getTimestamp("payortranstimestamp"));}catch(NullPointerException e){}
			try{pay.setStatus(rs.getInt("transtatus"));}catch(NullPointerException e){}
			try{pay.setPaymentType(rs.getInt("paytype"));}catch(NullPointerException e){}
			try{pay.setIsSpecialCase(rs.getInt("isSpecialCase"));}catch(NullPointerException e){} 
			
			ITaxPayor payor = new TaxPayor();
			try{payor.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{payor.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{payor.setAddress(rs.getString("payoraddress"));}catch(NullPointerException e){}
			try{payor.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			try{payor.setTimestamp(rs.getTimestamp("paytimestamp"));}catch(NullPointerException e){}
			try{pay.setTaxPayor(payor);}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			pay.setUserDtls(user);
			
			pays.add(pay);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pays;
	}
	
	public static double retrieveTotal(String sql, String[] params){
		double amnt = 0d;
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
		
		//System.out.println("SQL: " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			try{
				amnt += rs.getDouble("payortransamount");
			}catch(NullPointerException n){
			}catch(NumberFormatException ex){}
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return amnt;
	}
	
	public static int getLatestSCNo(){
		int result = 0;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT payorscno FROM taxpayortrans order by payortransid desc limit 1");
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			try{result = Integer.valueOf(rs.getString("payorscno"));}catch(Exception e){result=0;}
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException se){}
		return result;
	}
	
	public static ITaxPayorTrans save(ITaxPayorTrans pay){
		if(pay!=null){
			long id = getInfo(pay.getId()==0? getLatestId()+1 : pay.getId());
			if(id==1){
				pay = TaxPayorTrans.insertData(pay, "1");
			}else if(id==2){
				pay = TaxPayorTrans.updateData(pay);
			}else if(id==3){
				pay = TaxPayorTrans.insertData(pay, "3");
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
	
public static ITaxPayorTrans insertData(ITaxPayorTrans pay, String type){
		
		String sql = "INSERT INTO taxpayortrans ("
				+ "payortransid,"
				+ "payortransdate,"
				+ "payortransamount,"
				+ "payortransamountinwords,"
				+ "payorscno,"
				+ "payoraccntform,"
				+ "paytransisactive,"
				+ "paycheckno,"
				+ "paycheckdate,"
				+ "paysigned1,"
				+ "paysigned2,"
				+ "payorid,"
				+ "transtatus,"
				+ "paytype,"
				+ "userdtlsid,"
				+ "isSpecialCase) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table taxpayortrans");	
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
		ps.setString(2, pay.getTransDate());
		ps.setDouble(3, pay.getAmount());
		ps.setString(4, pay.getAmountInWords());
		ps.setString(5, pay.getScNo());
		ps.setInt(6, pay.getAccountFormNo());
		ps.setInt(7, pay.getIsactive());
		ps.setString(8, pay.getCheckNo());
		ps.setString(9, pay.getCheckDate());
		ps.setString(10, pay.getSigned1());
		ps.setString(11, pay.getSigned2());
		ps.setLong(12, pay.getTaxPayor()==null? 0 : pay.getTaxPayor().getId());
		ps.setInt(13, pay.getStatus());
		ps.setInt(14, pay.getPaymentType());
		ps.setLong(15, pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		ps.setInt(16, pay.getIsSpecialCase());
		
		LogU.add(pay.getTransDate());
		LogU.add(pay.getAmount());
		LogU.add(pay.getAmountInWords());
		LogU.add(pay.getScNo());
		LogU.add(pay.getAccountFormNo());
		LogU.add(pay.getIsactive());
		LogU.add(pay.getCheckNo());
		LogU.add(pay.getCheckDate());
		LogU.add(pay.getSigned1());
		LogU.add(pay.getSigned2());
		LogU.add(pay.getTaxPayor()==null? 0 : pay.getTaxPayor().getId());
		LogU.add(pay.getStatus());
		LogU.add(pay.getPaymentType());
		LogU.add(pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
		LogU.add(pay.getIsSpecialCase());
		
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table taxpayortrans successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table taxpayortrans.");
		}
		
		return pay;
	}

public void insertData(String type){
	
	String sql = "INSERT INTO taxpayortrans ("
			+ "payortransid,"
			+ "payortransdate,"
			+ "payortransamount,"
			+ "payortransamountinwords,"
			+ "payorscno,"
			+ "payoraccntform,"
			+ "paytransisactive,"
			+ "paycheckno,"
			+ "paycheckdate,"
			+ "paysigned1,"
			+ "paysigned2,"
			+ "payorid,"
			+ "transtatus,"
			+ "paytype,"
			+ "userdtlsid,"
			+ "isSpecialCase) " 
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	PreparedStatement ps = null;
	Connection conn = null;
	try{
	LogU.add("Insert into table taxpayortrans");	
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
	ps.setString(2, getTransDate());
	ps.setDouble(3, getAmount());
	ps.setString(4, getAmountInWords());
	ps.setString(5, getScNo());
	ps.setInt(6, getAccountFormNo());
	ps.setInt(7, getIsactive());
	ps.setString(8, getCheckNo());
	ps.setString(9, getCheckDate());
	ps.setString(10, getSigned1());
	ps.setString(11, getSigned2());
	ps.setLong(12, getTaxPayor()==null? 0 : getTaxPayor().getId());
	ps.setInt(13, getStatus());
	ps.setInt(14, getPaymentType());
	ps.setLong(15, getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
	ps.setInt(16, getIsSpecialCase());
	
	LogU.add(getTransDate());
	LogU.add(getAmount());
	LogU.add(getAmountInWords());
	LogU.add(getScNo());
	LogU.add(getAccountFormNo());
	LogU.add(getIsactive());
	LogU.add(getCheckNo());
	LogU.add(getCheckDate());
	LogU.add(getSigned1());
	LogU.add(getSigned2());
	LogU.add(getTaxPayor()==null? 0 : getTaxPayor().getId());
	LogU.add(getStatus());
	LogU.add(getPaymentType());
	LogU.add(getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
	LogU.add(getIsSpecialCase());
	
	ps.execute();
	ps.close();
	TaxDatabaseConnect.close(conn);
	LogU.add("Insert into table taxpayortrans successfully.");
	}catch(SQLException s){
		s.getMessage();
		LogU.add("Error Insert into table taxpayortrans.");
	}
	
}


public static ITaxPayorTrans updateData(ITaxPayorTrans pay){
	
	String sql = "UPDATE taxpayortrans SET "
			+ "payortransdate=?,"
			+ "payortransamount=?,"
			+ "payortransamountinwords=?,"
			+ "payorscno=?,"
			+ "payoraccntform=?,"
			+ "paycheckno=?,"
			+ "paycheckdate=?,"
			+ "paysigned1=?,"
			+ "paysigned2=?,"
			+ "payorid=?,"
			+ "transtatus=?,"
			+ "paytype=?,"
			+ "userdtlsid=?,"
			+ "isSpecialCase=? " 
			+ " WHERE payortransid=?";
	
	PreparedStatement ps = null;
	Connection conn = null;
	try{
	LogU.add("updating into table taxpayortrans");	
	conn = TaxDatabaseConnect.getConnection();
	ps = conn.prepareStatement(sql);
	
	ps.setString(1, pay.getTransDate());
	ps.setDouble(2, pay.getAmount());
	ps.setString(3, pay.getAmountInWords());
	ps.setString(4, pay.getScNo());
	ps.setInt(5, pay.getAccountFormNo());
	ps.setString(6, pay.getCheckNo());
	ps.setString(7, pay.getCheckDate());
	ps.setString(8, pay.getSigned1());
	ps.setString(9, pay.getSigned2());
	ps.setLong(10, pay.getTaxPayor()==null? 0 : pay.getTaxPayor().getId());
	ps.setInt(11, pay.getStatus());
	ps.setInt(12, pay.getPaymentType());
	ps.setLong(13, pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
	ps.setInt(14, pay.getIsSpecialCase());
	ps.setLong(15, pay.getId());
	
	LogU.add(pay.getTransDate());
	LogU.add(pay.getAmount());
	LogU.add(pay.getAmountInWords());
	LogU.add(pay.getScNo());
	LogU.add(pay.getAccountFormNo());
	LogU.add(pay.getIsactive());
	LogU.add(pay.getCheckNo());
	LogU.add(pay.getCheckDate());
	LogU.add(pay.getSigned1());
	LogU.add(pay.getSigned2());
	LogU.add(pay.getTaxPayor()==null? 0 : pay.getTaxPayor().getId());
	LogU.add(pay.getStatus());
	LogU.add(pay.getPaymentType());
	LogU.add(pay.getUserDtls()==null? 1 : pay.getUserDtls().getUserdtlsid());
	LogU.add(pay.getIsSpecialCase());
	LogU.add(pay.getId());
	
	ps.execute();
	ps.close();
	TaxDatabaseConnect.close(conn);
	LogU.add("updating into table taxpayortrans successfully.");
	}catch(SQLException s){
		s.getMessage();
		LogU.add("Error updating into table taxpayortrans.");
	}
	
	return pay;
}

public void updateData(){
	
	String sql = "UPDATE taxpayortrans SET "
			+ "payortransdate=?,"
			+ "payortransamount=?,"
			+ "payortransamountinwords=?,"
			+ "payorscno=?,"
			+ "payoraccntform=?,"
			+ "paycheckno=?,"
			+ "paycheckdate=?,"
			+ "paysigned1=?,"
			+ "paysigned2=?,"
			+ "payorid=?,"
			+ "transtatus=?,"
			+ "paytype=?,"
			+ "userdtlsid=?,"
			+ "isSpecialCase=? " 
			+ " WHERE payortransid=?";
	
	PreparedStatement ps = null;
	Connection conn = null;
	try{
	LogU.add("updating into table taxpayortrans");	
	conn = TaxDatabaseConnect.getConnection();
	ps = conn.prepareStatement(sql);
	
	ps.setString(1, getTransDate());
	ps.setDouble(2, getAmount());
	ps.setString(3, getAmountInWords());
	ps.setString(4, getScNo());
	ps.setInt(5, getAccountFormNo());
	ps.setString(6, getCheckNo());
	ps.setString(7, getCheckDate());
	ps.setString(8, getSigned1());
	ps.setString(9, getSigned2());
	ps.setLong(10, getTaxPayor()==null? 0 : getTaxPayor().getId());
	ps.setInt(11, getStatus());
	ps.setInt(12, getPaymentType());
	ps.setLong(13, getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
	ps.setInt(14, getIsSpecialCase());
	ps.setLong(15, getId());
	
	LogU.add(getTransDate());
	LogU.add(getAmount());
	LogU.add(getAmountInWords());
	LogU.add(getScNo());
	LogU.add(getAccountFormNo());
	LogU.add(getIsactive());
	LogU.add(getCheckNo());
	LogU.add(getCheckDate());
	LogU.add(getSigned1());
	LogU.add(getSigned2());
	LogU.add(getTaxPayor()==null? 0 : getTaxPayor().getId());
	LogU.add(getStatus());
	LogU.add(getPaymentType());
	LogU.add(getUserDtls()==null? 1 : getUserDtls().getUserdtlsid());
	LogU.add(getIsSpecialCase());
	LogU.add(getId());
	
	ps.execute();
	ps.close();
	TaxDatabaseConnect.close(conn);
	LogU.add("updating into table taxpayortrans successfully.");
	}catch(SQLException s){
		s.getMessage();
		LogU.add("Error updating into table taxpayortrans.");
	}
	
}

	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT payortransid FROM taxpayortrans  ORDER BY payortransid DESC LIMIT 1";	
		conn = TaxDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("payortransid");
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
		ps = conn.prepareStatement("SELECT payortransid FROM taxpayortrans WHERE payortransid=?");
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
		String sql = "update taxpayortrans set paytransisactive=0, transtatus=0, userdtlsid=? WHERE payortransid=?";
		String params[] = new String[2];
		params[0] = Login.getUserLogin().getUserDtls().getUserdtlsid()+"";
		params[1] = getId()+"";
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			LogU.add("Deleting data in taxpayortrans where id="+getId());	
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
	public String getTransDate() {
		// TODO Auto-generated method stub
		return transDate;
	}

	@Override
	public void setTransDate(String transDate) {
		// TODO Auto-generated method stub
		this.transDate = transDate;
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return amount;
	}

	@Override
	public void setAmount(double amount) {
		// TODO Auto-generated method stub
		this.amount = amount;
	}

	@Override
	public String getAmountInWords() {
		// TODO Auto-generated method stub
		return amountInWords;
	}

	@Override
	public void setAmountInWords(String amountInWords) {
		// TODO Auto-generated method stub
		this.amountInWords = amountInWords;
	}

	@Override
	public String getScNo() {
		// TODO Auto-generated method stub
		return scNo;
	}

	@Override
	public void setScNo(String scNo) {
		// TODO Auto-generated method stub
		this.scNo = scNo;
	}

	@Override
	public int getAccountFormNo() {
		// TODO Auto-generated method stub
		return accountFormNo;
	}

	@Override
	public void setAccountFormNo(int accountFormNo) {
		// TODO Auto-generated method stub
		this.accountFormNo = accountFormNo;
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
	public String getCheckNo() {
		// TODO Auto-generated method stub
		return checkNo;
	}

	@Override
	public void setCheckNo(String checkNo) {
		// TODO Auto-generated method stub
		this.checkNo = checkNo;
	}

	@Override
	public String getCheckDate() {
		// TODO Auto-generated method stub
		return checkDate;
	}

	@Override
	public void setCheckDate(String checkDate) {
		// TODO Auto-generated method stub
		this.checkDate = checkDate;
	}

	@Override
	public String getSigned1() {
		// TODO Auto-generated method stub
		return signed1;
	}

	@Override
	public void setSigned1(String signed1) {
		// TODO Auto-generated method stub
		this.signed1 = signed1;
	}

	@Override
	public String getSigned2() {
		// TODO Auto-generated method stub
		return signed2;
	}

	@Override
	public void setSigned2(String signed2) {
		// TODO Auto-generated method stub
		this.signed2 = signed2;
	}

	@Override
	public ITaxPayor getTaxPayor() {
		// TODO Auto-generated method stub
		return taxPayor;
	}

	@Override
	public void setTaxPayor(ITaxPayor taxPayor) {
		// TODO Auto-generated method stub
		this.taxPayor = taxPayor;
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
	
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return status;
	}

	@Override
	public void setStatus(int status) {
		// TODO Auto-generated method stub
		this.status = status;
	}

	@Override
	public int getPaymentType() {
		// TODO Auto-generated method stub
		return paymentType;
	}

	@Override
	public void setPaymentType(int paymentType) {
		// TODO Auto-generated method stub
		this.paymentType = paymentType;
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
	public LandPayor getLandPayor() {
		// TODO Auto-generated method stub
		return landPayor;
	}

	@Override
	public void setLandPayor(LandPayor landPayor) {
		// TODO Auto-generated method stub
		this.landPayor = landPayor;
	}
	@Override
	public int getIsSpecialCase() {
		return isSpecialCase;
	}
	@Override
	public void setIsSpecialCase(int isSpecialCase) {
		this.isSpecialCase = isSpecialCase;
	}
}
