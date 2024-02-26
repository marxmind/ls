package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;

public class PenaltyTable {

	private long id;
	private int year;
	private double january;
	private double february;
	private double march;
	private double april;
	private double may;
	private double june;
	private double july;
	private double august;
	private double september;
	private double october;
	private double november;
	private double december;
	private int isActive;
	private UserDtls userDtls;
	private Timestamp timestamp;
	
	private int cnt;
	
	public PenaltyTable(){}
	
	public PenaltyTable(
			long id,
			int year,
			double january,
			double february,
			double march,
			double april,
			double may,
			double june,
			double july,
			double august,
			double september,
			double october,
			double november,
			double december,
			int isActive,
			UserDtls userDtls
			){
				this.id = id;
				this.year = year;
				this.january = january;
				this.february = february;
				this.march = march;
				this.april = april;
				this.may = may;
				this.june = june;
				this.july = july;
				this.august = august;
				this.september = september;
				this.october = october;
				this.november = november;
				this.december = december;
				this.isActive = isActive;
				this.userDtls = userDtls;
	}
	
	public static List<PenaltyTable> retrieve(String sql, String[] params){
		List<PenaltyTable> pens = Collections.synchronizedList(new ArrayList<>());
		
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
		
		System.out.println("SQL " + ps.toString());
		
		rs = ps.executeQuery();
		int cnt = 1;
		while(rs.next()){
			PenaltyTable pen = new PenaltyTable();
			pen.setCnt(cnt++);
			pen.setId(rs.getLong("calid"));
			pen.setYear(rs.getInt("year"));
			pen.setJanuary(rs.getDouble("jana"));
			pen.setFebruary(rs.getDouble("feba"));
			pen.setMarch(rs.getDouble("mara"));
			pen.setApril(rs.getDouble("apra"));
			pen.setMay(rs.getDouble("maya"));
			pen.setJune(rs.getDouble("juna"));
			pen.setJuly(rs.getDouble("jula"));
			pen.setAugust(rs.getDouble("auga"));
			pen.setSeptember(rs.getDouble("sepa"));
			pen.setOctober(rs.getDouble("octa"));
			pen.setNovember(rs.getDouble("nova"));
			pen.setDecember(rs.getDouble("deca"));
			pen.setTimestamp(rs.getTimestamp("timestamp"));
			pens.add(pen);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(Exception e){}
		
		return pens;
	}
	
	public static PenaltyTable save(PenaltyTable pen){
		if(pen!=null){
			
			long id = PenaltyTable.getInfo(pen.getId()==0? PenaltyTable.getLatestId()+1 : pen.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				pen = PenaltyTable.insertData(pen, "1");
			}else if(id==2){
				LogU.add("update Data ");
				pen = PenaltyTable.updateData(pen);
			}else if(id==3){
				LogU.add("added new Data ");
				pen = PenaltyTable.insertData(pen, "3");
			}
			
		}
		return pen;
	}
	
	public void save(){
			
			long id = getInfo(getId()==0? getLatestId()+1 : getId());
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
	
	public static PenaltyTable insertData(PenaltyTable pen, String type){
		String sql = "INSERT INTO landtaxpenaltiescal ("
				+ "calid,"
				+ "year,"
				+ "jana,"
				+ "feba,"
				+ "mara,"
				+ "apra,"
				+ "maya,"
				+ "juna,"
				+ "jula,"
				+ "auga,"
				+ "sepa,"
				+ "octa,"
				+ "nova,"
				+ "deca,"
				+ "isactiverate,"
				+ "userdtlsid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table landtaxpenaltiescal");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			pen.setId(Long.valueOf(id));
			LogU.add("Logid: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			pen.setId(Long.valueOf(id));
			LogU.add("logid: " + id);
		}
		
		ps.setInt(2,  pen.getYear());
		ps.setDouble(3, pen.getJanuary());
		ps.setDouble(4, pen.getFebruary());
		ps.setDouble(5, pen.getMarch());
		ps.setDouble(6, pen.getApril());
		ps.setDouble(7, pen.getMay());
		ps.setDouble(8, pen.getJune());
		ps.setDouble(9, pen.getJuly());
		ps.setDouble(10, pen.getAugust());
		ps.setDouble(11, pen.getSeptember());
		ps.setDouble(12, pen.getOctober());
		ps.setDouble(13, pen.getNovember());
		ps.setDouble(14, pen.getDecember());
		ps.setInt(15, pen.getIsActive());
		ps.setLong(16, pen.getUserDtls()==null? 0 : pen.getUserDtls().getUserdtlsid());
		
		System.out.println("SQL: " + ps.toString());
		
		LogU.add(pen.getYear());
		LogU.add(pen.getJanuary());
		LogU.add(pen.getFebruary());
		LogU.add(pen.getMarch());
		LogU.add(pen.getApril());
		LogU.add(pen.getMay());
		LogU.add(pen.getJune());
		LogU.add(pen.getJuly());
		LogU.add(pen.getAugust());
		LogU.add(pen.getSeptember());
		LogU.add(pen.getOctober());
		LogU.add(pen.getNovember());
		LogU.add(pen.getDecember());
		LogU.add(pen.getIsActive());
		LogU.add(pen.getUserDtls()==null? 0 : pen.getUserDtls().getUserdtlsid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("error inserting data to landtaxpenaltiescal : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pen;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO landtaxpenaltiescal ("
				+ "calid,"
				+ "year,"
				+ "jana,"
				+ "feba,"
				+ "mara,"
				+ "apra,"
				+ "maya,"
				+ "juna,"
				+ "jula,"
				+ "auga,"
				+ "sepa,"
				+ "octa,"
				+ "nova,"
				+ "deca,"
				+ "isactiverate,"
				+ "userdtlsid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table landtaxpenaltiescal");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			setId(Long.valueOf(id));
			LogU.add("Logid: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			setId(Long.valueOf(id));
			LogU.add("logid: " + id);
		}
		
		ps.setInt(2,  getYear());
		ps.setDouble(3, getJanuary());
		ps.setDouble(4, getFebruary());
		ps.setDouble(5, getMarch());
		ps.setDouble(6, getApril());
		ps.setDouble(7, getMay());
		ps.setDouble(8, getJune());
		ps.setDouble(9, getJuly());
		ps.setDouble(10, getAugust());
		ps.setDouble(11, getSeptember());
		ps.setDouble(12, getOctober());
		ps.setDouble(13, getNovember());
		ps.setDouble(14, getDecember());
		ps.setInt(15, getIsActive());
		ps.setLong(16, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		
		LogU.add(getYear());
		LogU.add(getJanuary());
		LogU.add(getFebruary());
		LogU.add(getMarch());
		LogU.add(getApril());
		LogU.add(getMay());
		LogU.add(getJune());
		LogU.add(getJuly());
		LogU.add(getAugust());
		LogU.add(getSeptember());
		LogU.add(getOctober());
		LogU.add(getNovember());
		LogU.add(getDecember());
		LogU.add(getIsActive());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to landtaxpenaltiescal : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static PenaltyTable updateData(PenaltyTable pen){
		String sql = "UPDATE landtaxpenaltiescal SET "
				+ "year=?,"
				+ "jana=?,"
				+ "feba=?,"
				+ "mara=?,"
				+ "apra=?,"
				+ "maya=?,"
				+ "juna=?,"
				+ "jula=?,"
				+ "auga=?,"
				+ "sepa=?,"
				+ "octa=?,"
				+ "nova=?,"
				+ "deca=?,"
				+ "userdtlsid=? " 
				+ " WHERE calid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		LogU.add("===========================START=========================");
		LogU.add("updating data into table landtaxpenaltiescal");
		
		
		ps.setInt(1,  pen.getYear());
		ps.setDouble(2, pen.getJanuary());
		ps.setDouble(3, pen.getFebruary());
		ps.setDouble(4, pen.getMarch());
		ps.setDouble(5, pen.getApril());
		ps.setDouble(6, pen.getMay());
		ps.setDouble(7, pen.getJune());
		ps.setDouble(8, pen.getJuly());
		ps.setDouble(9, pen.getAugust());
		ps.setDouble(10, pen.getSeptember());
		ps.setDouble(11, pen.getOctober());
		ps.setDouble(12, pen.getNovember());
		ps.setDouble(13, pen.getDecember());
		ps.setLong(14, pen.getUserDtls()==null? 0 : pen.getUserDtls().getUserdtlsid());
		ps.setLong(15, pen.getId());
		
		LogU.add(pen.getYear());
		LogU.add(pen.getJanuary());
		LogU.add(pen.getFebruary());
		LogU.add(pen.getMarch());
		LogU.add(pen.getApril());
		LogU.add(pen.getMay());
		LogU.add(pen.getJune());
		LogU.add(pen.getJuly());
		LogU.add(pen.getAugust());
		LogU.add(pen.getSeptember());
		LogU.add(pen.getOctober());
		LogU.add(pen.getNovember());
		LogU.add(pen.getDecember());
		LogU.add(pen.getUserDtls()==null? 0 : pen.getUserDtls().getUserdtlsid());
		LogU.add(pen.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to landtaxpenaltiescal : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return pen;
	}
	
	public void updateData(){
		String sql = "UPDATE landtaxpenaltiescal SET "
				+ "year=?,"
				+ "jana=?,"
				+ "feba=?,"
				+ "mara=?,"
				+ "apra=?,"
				+ "maya=?,"
				+ "juna=?,"
				+ "jula=?,"
				+ "auga=?,"
				+ "sepa=?,"
				+ "octa=?,"
				+ "nova=?,"
				+ "deca=?,"
				+ "userdtlsid=? " 
				+ " WHERE calid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		LogU.add("===========================START=========================");
		LogU.add("updating data into table landtaxpenaltiescal");
		
		
		ps.setInt(1,  getYear());
		ps.setDouble(2, getJanuary());
		ps.setDouble(3, getFebruary());
		ps.setDouble(4, getMarch());
		ps.setDouble(5, getApril());
		ps.setDouble(6, getMay());
		ps.setDouble(7, getJune());
		ps.setDouble(8, getJuly());
		ps.setDouble(9, getAugust());
		ps.setDouble(10, getSeptember());
		ps.setDouble(11, getOctober());
		ps.setDouble(12, getNovember());
		ps.setDouble(13, getDecember());
		ps.setLong(14, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setLong(15, getId());
		
		LogU.add(getYear());
		LogU.add(getJanuary());
		LogU.add(getFebruary());
		LogU.add(getMarch());
		LogU.add(getApril());
		LogU.add(getMay());
		LogU.add(getJune());
		LogU.add(getJuly());
		LogU.add(getAugust());
		LogU.add(getSeptember());
		LogU.add(getOctober());
		LogU.add(getNovember());
		LogU.add(getDecember());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to landtaxpenaltiescal : " + s.getMessage());
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
		sql="SELECT calid FROM landtaxpenaltiescal  ORDER BY calid DESC LIMIT 1";	
		conn = TaxDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("calid");
		}
		
		rs.close();
		prep.close();
		TaxDatabaseConnect.close(conn);
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
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT calid FROM landtaxpenaltiescal WHERE calid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
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
		}catch(SQLException s){}
		
	}
	
	public void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE landtaxpenaltiescal set isactiverate=0,userdtlsid="+ Login.getUserLogin().getUserDtls().getUserdtlsid() +" WHERE calid=?";
		
		
		String[] params = new String[1];
		params[0] = getId()+"";
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
		}catch(SQLException s){}
		
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public double getJanuary() {
		return january;
	}
	public void setJanuary(double january) {
		this.january = january;
	}
	public double getFebruary() {
		return february;
	}
	public void setFebruary(double february) {
		this.february = february;
	}
	public double getMarch() {
		return march;
	}
	public void setMarch(double march) {
		this.march = march;
	}
	public double getApril() {
		return april;
	}
	public void setApril(double april) {
		this.april = april;
	}
	public double getMay() {
		return may;
	}
	public void setMay(double may) {
		this.may = may;
	}
	public double getJune() {
		return june;
	}
	public void setJune(double june) {
		this.june = june;
	}
	public double getJuly() {
		return july;
	}
	public void setJuly(double july) {
		this.july = july;
	}
	public double getAugust() {
		return august;
	}
	public void setAugust(double august) {
		this.august = august;
	}
	public double getSeptember() {
		return september;
	}
	public void setSeptember(double september) {
		this.september = september;
	}
	public double getOctober() {
		return october;
	}
	public void setOctober(double october) {
		this.october = october;
	}
	public double getNovember() {
		return november;
	}
	public void setNovember(double november) {
		this.november = november;
	}
	public double getDecember() {
		return december;
	}
	public void setDecember(double december) {
		this.december = december;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public int getIsActive() {
		return isActive;
	}

	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}

	public UserDtls getUserDtls() {
		return userDtls;
	}

	public void setUserDtls(UserDtls userDtls) {
		this.userDtls = userDtls;
	}
	
	public static void main(String[] args) {
		PenaltyTable pen = new PenaltyTable();
		pen.setId(33);
		pen.setYear(2017);
		pen.setJanuary(1);
		pen.setFebruary(0);
		pen.setMarch(0);
		pen = PenaltyTable.save(pen);
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	
}
