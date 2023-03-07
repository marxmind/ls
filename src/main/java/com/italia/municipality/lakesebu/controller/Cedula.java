package com.italia.municipality.lakesebu.controller;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.Months;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 02/12/2020
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Cedula {

	private long id;
	private String cedulaNo;
	private String dateIssued;
	private int cedulaType;
	private double basicTax;
	private double grossTax;
	private double totalAmount;
	private String tinNumber;
	private String height;
	private String weight;
	private int isActive;
	private String issuedAddress;
	private int status;
	
	private double interestPenalty;
	private String citizenship;
	private int civilStatus;
	private int genderId;
	private String birthdate;
	private String birthPlace;
	private String occupationOrBusinessName;
	
	
	private Customer customer;
	private UserDtls userDtls;
	
	private String cedulaStatus;
	
	private int cnt;
	
	private static final String PROPERTY_FILE = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +	
			AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() + "ctc-interest-rate.tis";

public static double cedulaPenaltyRate(Months month) {
	double rate = 0;
	Properties prop = new Properties();
	try{
		System.out.println("month.getName()>> " + month.getName());
		prop.load(new FileInputStream(PROPERTY_FILE));
		rate = Double.valueOf(prop.getProperty(month.getName())); 
	}catch(Exception e) {e.printStackTrace();}	
	
	return rate;
}
	
public static String getLastCedulaNo() {
		
		String sql = "SELECT cedno FROM cedula WHERE cisactive=1 ORDER BY cid DESC LIMIT 1";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return rs.getString("cedno");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return "0";
	}
	
	public static String getNewCedulaNumber() {
		String cedulaNumber="";
			
		try {
			String cedNo = getLastCedulaNo();
			int incNo = Integer.valueOf(cedNo);
			incNo += 1;
			String newNo = incNo+"";
			int newSize = newNo.length();
			
			switch(newSize) {
				case 8: cedulaNumber=newNo; break;
				case 7: cedulaNumber="0"+newNo; break;
				case 6: cedulaNumber="00"+newNo; break;
				case 5: cedulaNumber="000"+newNo; break;
				case 4: cedulaNumber="0000"+newNo; break;
				case 3: cedulaNumber="00000"+newNo; break;
				case 2: cedulaNumber="000000"+newNo; break;
			}
		}catch(Exception e) {}	
		
		return cedulaNumber;
	}
	
	public static boolean isExistCedula(Customer cus){
		Cedula cedula = null;
		String sql = " AND (ced.cedate>=? AND ced.cedate<=?) AND ced.cisactive=1 AND cuz.customerid=" + cus.getId();
		String[] params = new String[2];
		params[0] = DateUtils.getCurrentYear() + "-01-01";
		params[1] = DateUtils.getCurrentYear() + "-12-31";
		
		try{
			cedula = Cedula.retrieve(sql, params).get(0);
			if(cedula!=null){
				return true;
			}
		}catch(Exception e){}
		
		return false;
	}
	
	public static Cedula checkSaveIfNotExist(Cedula cedula){
		
		String sql = "SELECT *  FROM cedula WHERE cisactive=1 AND cedno='" + cedula.getCedulaNo() +"' AND  cedate='" + cedula.getDateIssued() + "' ";
		boolean isNotExist = true;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			isNotExist = false;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		if(isNotExist){
			cedula = Cedula.save(cedula);
		}
		
		return cedula;
	}
	
	public static Cedula loadCedulaIfExist(Customer cus){
		Cedula cedula = null;
		String sql = " AND (ced.cedate>=? AND ced.cedate<=?) AND ced.cisactive=1 AND ced.cstatus=1 AND cuz.customerid=" + cus.getId();
		String[] params = new String[2];
		params[0] = DateUtils.getCurrentYear() + "-01-01";
		params[1] = DateUtils.getCurrentYear() + "-12-31";
		
		try{
			cedula = Cedula.retrieve(sql, params).get(0);
		}catch(Exception e){}
		
		return cedula;
	}
	
	public static List<Cedula> retrieve(String sqlAdd, String[] params){
		List<Cedula> ceds = new ArrayList<Cedula>();
		
		String tableCed = "ced";
		String tableCus = "cuz";
		String tableUser = "usr";
		
		String sql = "SELECT * FROM cedula " + tableCed + ", customer " + tableCus + ", userdtls " + tableUser + " WHERE " +
				tableCed + ".customerid=" + tableCus + ".customerid AND " +
				tableCed + ".userdtlsid=" + tableUser + ".userdtlsid ";
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
		
		System.out.println("CEDULA SQL " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Cedula cl = new Cedula();
			try{cl.setId(rs.getLong("cid"));}catch(NullPointerException e){}
			try{cl.setDateIssued(rs.getString("cedate"));}catch(NullPointerException e){}
			try{cl.setCedulaNo(rs.getString("cedno"));}catch(NullPointerException e){}
			try{cl.setTinNumber(rs.getString("tin"));}catch(NullPointerException e){}
			try{cl.setCedulaType(rs.getInt("cedtype"));}catch(NullPointerException e){}
			try{cl.setBasicTax(rs.getDouble("basictax"));}catch(NullPointerException e){}
			try{cl.setGrossTax(rs.getDouble("grosstax"));}catch(NullPointerException e){}
			try{cl.setTotalAmount(rs.getDouble("totalamount"));}catch(NullPointerException e){}
			try{cl.setHeight(rs.getString("chieght"));}catch(NullPointerException e){}
			try{cl.setWeight(rs.getString("cweight"));}catch(NullPointerException e){}
			try{cl.setIsActive(rs.getInt("cisactive"));}catch(NullPointerException e){}
			try{cl.setIssuedAddress(rs.getString("cedissuedaddress"));}catch(NullPointerException e){}
			try{cl.setStatus(rs.getInt("cstatus"));}catch(NullPointerException e){}
			
			try{cl.setCedulaStatus(cl.getStatus()==1? "Delivered" : "Cancelled");}catch(NullPointerException e){}
			
			Customer cus = new Customer();
			try{cus.setId(rs.getLong("customerid"));}catch(NullPointerException e){}
			try{cus.setFirstname(rs.getString("cusfirstname"));}catch(NullPointerException e){}
			try{cus.setMiddlename(rs.getString("cusmiddlename"));}catch(NullPointerException e){}
			try{cus.setLastname(rs.getString("cuslastname"));}catch(NullPointerException e){}
			try{cus.setFullname(rs.getString("fullname"));}catch(NullPointerException e){}
			try{cus.setGender(rs.getString("cusgender"));}catch(NullPointerException e){}
			cl.setCustomer(cus);
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setRegdate(rs.getString("regdate"));;}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			cl.setUserDtls(user);
			
			
			ceds.add(cl);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return ceds;
	}
	
	public static Cedula save(Cedula ced){
		if(ced!=null){
			
			long id = Cedula.getInfo(ced.getId() ==0? Cedula.getLatestId()+1 : ced.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				ced = Cedula.insertData(ced, "1");
			}else if(id==2){
				LogU.add("update Data ");
				ced = Cedula.updateData(ced);
			}else if(id==3){
				LogU.add("added new Data ");
				ced = Cedula.insertData(ced, "3");
			}
			
		}
		return ced;
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
	
	public static Cedula insertData(Cedula ced, String type){
		String sql = "INSERT INTO cedula ("
				+ "cid,"
				+ "cedate,"
				+ "cedno,"
				+ "tin,"
				+ "cedtype,"
				+ "basictax,"
				+ "grosstax,"
				+ "totalamount,"
				+ "chieght,"
				+ "cweight,"
				+ "cisactive,"
				+ "customerid,"
				+ "userdtlsid,"
				+ "cedissuedaddress,"
				+ "cstatus)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cedula");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			ced.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			ced.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, ced.getDateIssued());
		ps.setString(cnt++, ced.getCedulaNo());
		ps.setString(cnt++, ced.getTinNumber());
		ps.setInt(cnt++, ced.getCedulaType());
		ps.setDouble(cnt++, ced.getBasicTax());
		ps.setDouble(cnt++, ced.getGrossTax());
		ps.setDouble(cnt++, ced.getTotalAmount());
		ps.setString(cnt++, ced.getHeight());
		ps.setString(cnt++, ced.getWeight());
		ps.setInt(cnt++, ced.getIsActive());
		ps.setLong(cnt++, ced.getCustomer()==null? 0 : ced.getCustomer().getId());
		ps.setLong(cnt++, ced.getUserDtls()==null? 0 : ced.getUserDtls().getUserdtlsid());
		ps.setString(cnt++, ced.getIssuedAddress());
		ps.setInt(cnt++, ced.getStatus());
		
		LogU.add(ced.getDateIssued());
		LogU.add(ced.getCedulaNo());
		LogU.add(ced.getTinNumber());
		LogU.add(ced.getCedulaType());
		LogU.add(ced.getBasicTax());
		LogU.add(ced.getGrossTax());
		LogU.add(ced.getTotalAmount());
		LogU.add(ced.getHeight());
		LogU.add(ced.getWeight());
		LogU.add(ced.getIsActive());
		LogU.add(ced.getCustomer()==null? 0 : ced.getCustomer().getId());
		LogU.add(ced.getUserDtls()==null? 0 : ced.getUserDtls().getUserdtlsid());
		LogU.add(ced.getIssuedAddress());
		LogU.add(ced.getStatus());
				
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to cedula : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return ced;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO cedula ("
				+ "cid,"
				+ "cedate,"
				+ "cedno,"
				+ "tin,"
				+ "cedtype,"
				+ "basictax,"
				+ "grosstax,"
				+ "totalamount,"
				+ "chieght,"
				+ "cweight,"
				+ "cisactive,"
				+ "customerid,"
				+ "userdtlsid,"
				+ "cedissuedaddress,"
				+ "cstatus)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table cedula");
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
		
		ps.setString(cnt++, getDateIssued());
		ps.setString(cnt++, getCedulaNo());
		ps.setString(cnt++, getTinNumber());
		ps.setInt(cnt++, getCedulaType());
		ps.setDouble(cnt++, getBasicTax());
		ps.setDouble(cnt++, getGrossTax());
		ps.setDouble(cnt++, getTotalAmount());
		ps.setString(cnt++, getHeight());
		ps.setString(cnt++, getWeight());
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getCustomer()==null? 0 : getCustomer().getId());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setString(cnt++, getIssuedAddress());
		ps.setInt(cnt++, getStatus());
		
		LogU.add(getDateIssued());
		LogU.add(getCedulaNo());
		LogU.add(getTinNumber());
		LogU.add(getCedulaType());
		LogU.add(getBasicTax());
		LogU.add(getGrossTax());
		LogU.add(getTotalAmount());
		LogU.add(getHeight());
		LogU.add(getWeight());
		LogU.add(getIsActive());
		LogU.add(getCustomer()==null? 0 : getCustomer().getId());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getIssuedAddress());
		LogU.add(getStatus());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to cedula : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	
	}
	
	public static Cedula updateData(Cedula ced){
		String sql = "UPDATE cedula SET "
				+ "cedate=?,"
				+ "cedno=?,"
				+ "tin=?,"
				+ "cedtype=?,"
				+ "basictax=?,"
				+ "grosstax=?,"
				+ "totalamount=?,"
				+ "chieght=?,"
				+ "cweight=?,"
				+ "customerid=?,"
				+ "userdtlsid=?,"
				+ "cedissuedaddress=?,"
				+ "cstatus=? " 
				+ " WHERE cid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table cedula");
		
		ps.setString(cnt++, ced.getDateIssued());
		ps.setString(cnt++, ced.getCedulaNo());
		ps.setString(cnt++, ced.getTinNumber());
		ps.setInt(cnt++, ced.getCedulaType());
		ps.setDouble(cnt++, ced.getBasicTax());
		ps.setDouble(cnt++, ced.getGrossTax());
		ps.setDouble(cnt++, ced.getTotalAmount());
		ps.setString(cnt++, ced.getHeight());
		ps.setString(cnt++, ced.getWeight());
		ps.setLong(cnt++, ced.getCustomer()==null? 0 : ced.getCustomer().getId());
		ps.setLong(cnt++, ced.getUserDtls()==null? 0 : ced.getUserDtls().getUserdtlsid());
		ps.setString(cnt++, ced.getIssuedAddress());
		ps.setInt(cnt++, ced.getStatus());
		ps.setLong(cnt++, ced.getId());
		
		LogU.add(ced.getDateIssued());
		LogU.add(ced.getCedulaNo());
		LogU.add(ced.getTinNumber());
		LogU.add(ced.getCedulaType());
		LogU.add(ced.getBasicTax());
		LogU.add(ced.getGrossTax());
		LogU.add(ced.getTotalAmount());
		LogU.add(ced.getHeight());
		LogU.add(ced.getWeight());
		LogU.add(ced.getCustomer()==null? 0 : ced.getCustomer().getId());
		LogU.add(ced.getUserDtls()==null? 0 : ced.getUserDtls().getUserdtlsid());
		LogU.add(ced.getIssuedAddress());
		LogU.add(ced.getStatus());
		LogU.add(ced.getId());
				
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error upadating data to cedula : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return ced;
	}
	
	public void updateData(){
		String sql = "UPDATE cedula SET "
				+ "cedate=?,"
				+ "cedno=?,"
				+ "tin=?,"
				+ "cedtype=?,"
				+ "basictax=?,"
				+ "grosstax=?,"
				+ "totalamount=?,"
				+ "chieght=?,"
				+ "cweight=?,"
				+ "customerid=?,"
				+ "userdtlsid=?,"
				+ "cedissuedaddress=?,"
				+ "cstatus=? " 
				+ " WHERE cid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table cedula");
		
		ps.setString(cnt++, getDateIssued());
		ps.setString(cnt++, getCedulaNo());
		ps.setString(cnt++, getTinNumber());
		ps.setInt(cnt++, getCedulaType());
		ps.setDouble(cnt++, getBasicTax());
		ps.setDouble(cnt++, getGrossTax());
		ps.setDouble(cnt++, getTotalAmount());
		ps.setString(cnt++, getHeight());
		ps.setString(cnt++, getWeight());
		ps.setLong(cnt++, getCustomer()==null? 0 : getCustomer().getId());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setString(cnt++, getIssuedAddress());
		ps.setInt(cnt++, getStatus());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateIssued());
		LogU.add(getCedulaNo());
		LogU.add(getTinNumber());
		LogU.add(getCedulaType());
		LogU.add(getBasicTax());
		LogU.add(getGrossTax());
		LogU.add(getTotalAmount());
		LogU.add(getHeight());
		LogU.add(getWeight());
		LogU.add(getCustomer()==null? 0 : getCustomer().getId());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getIssuedAddress());
		LogU.add(getStatus());
		LogU.add(getId());
				
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error upadating data to cedula : " + s.getMessage());
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
		sql="SELECT cid FROM cedula  ORDER BY cid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("cid");
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
		ps = conn.prepareStatement("SELECT cid FROM cedula WHERE cid=?");
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
		String sql = "UPDATE cedula set cisactive=0,userdtlsid="+ getUserDtls().getUserdtlsid() +" WHERE cid=?";
		
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
