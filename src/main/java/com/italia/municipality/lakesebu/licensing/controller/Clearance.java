package com.italia.municipality.lakesebu.licensing.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.italia.municipality.lakesebu.controller.Employee;
import com.italia.municipality.lakesebu.controller.MultiLivelihood;
import com.italia.municipality.lakesebu.controller.MultiPurpose;
import com.italia.municipality.lakesebu.controller.Purpose;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.DocTypes;
import com.italia.municipality.lakesebu.utils.DateUtils;
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
 *not in user
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Deprecated
public class Clearance {

	private long id;
	private String issuedDate;
	private String barcode;
	private String orNumber;
	private String cedulaNumber;
	private String notes;
	private String photoId;
	private double amountPaid;
	private int purposeType;
	private int status;
	private int isPayable;
	private int clearanceType;
	private int isActive;
	private Timestamp timestamp;
	private int documentType;
	private int documentValidity;
	
	private String customTitle;
	
	private Employee employee;
	private Customer taxPayer;
	private UserDtls userDtls;
	
	private List<MultiPurpose> multipurpose;
	private List<MultiLivelihood> multilivelihood;
	
	private String purposeName;
	private String typeName;
	
	public static String generateBarcode(){
		String barcode="";
		String lenId = (getLatestId() + 1) +"";
		int len = lenId.length();
		
		switch(len){
		case 1: barcode = "00000000000000" + lenId; break;
		case 2: barcode = "0000000000000" + lenId; break;
		case 3: barcode = "000000000000" + lenId; break;
		case 4: barcode = "00000000000" + lenId; break;
		case 5: barcode = "0000000000" + lenId; break;
		case 6: barcode = "000000000" + lenId; break;
		case 7: barcode = "00000000" + lenId; break;
		case 8: barcode = "0000000" + lenId; break;
		case 9: barcode = "000000" + lenId; break;
		case 10: barcode = "00000" + lenId; break;
		case 11: barcode = "0000" + lenId; break;
		case 12: barcode = "000" + lenId; break;
		case 13: barcode = "00" + lenId; break;
		case 14: barcode = "0" + lenId; break;
		case 15: barcode = "" + lenId; break;
		}
		
		return barcode;
	}
	
	public static List<String> retrieve(String param, String fieldName, String limit){
		
		String sql = "SELECT DISTINCT " + fieldName + " FROM clearance WHERE " + fieldName +" like '" + param + "%' " + limit;
		List<String> result = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			result.add(rs.getString(fieldName));
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return result;
	}
	
	public static List<Clearance> retrieve(String sqlAdd, String[] params){
		List<Clearance> clears = new ArrayList<>();
		
		String tableClear = "clz";
		String tableEmp = "emp";
		String tableCus = "cuz";
		String tableUser = "usr";
		
		String sql = "SELECT * FROM clearance " + tableClear + ", employee " + tableEmp + ", customer " + tableCus + ", userdtls " + tableUser + " WHERE " +
				tableClear +".empid=" + tableEmp + ".empid AND " +
				tableClear + ".customerid=" + tableCus + ".customerid AND " +
				tableClear + ".userdtlsid=" + tableUser + ".userdtlsid ";
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
		
		System.out.println("Clearance SQL " + ps.toString());
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
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
			try{cl.setTypeName(DocTypes.typeName(rs.getInt("doctype")));}catch(NullPointerException e){}
			try{cl.setDocumentType(rs.getInt("doctype"));}catch(NullPointerException e){}
			try{cl.setDocumentValidity(rs.getInt("doctvalid"));}catch(NullPointerException e){}
			try{cl.setCustomTitle(rs.getString("customtitle"));}catch(NullPointerException e){}
			
			Employee emp = new Employee();
			try{emp.setId(rs.getLong("empid"));}catch(NullPointerException e){}
			try{emp.setDateRegistered(rs.getString("datereg"));}catch(NullPointerException e){}
			try{emp.setDateResigned(rs.getString("dateend"));}catch(NullPointerException e){}
			try{emp.setFirstName(rs.getString("firstname"));}catch(NullPointerException e){}
			try{emp.setMiddleName(rs.getString("middlename"));}catch(NullPointerException e){}
			try{emp.setLastName(rs.getString("lastname"));}catch(NullPointerException e){}
			try{emp.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{emp.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{emp.setIsOfficial(rs.getInt("isofficial"));}catch(NullPointerException e){}
			try{emp.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			try{emp.setIsActiveEmployee(rs.getInt("isactiveemp"));}catch(NullPointerException e){}
			try{emp.setFullName(rs.getString("firstname") + " " +rs.getString("lastname"));}catch(NullPointerException e){}
			cl.setEmployee(emp);
			
			Customer cus = new Customer();
			try{cus.setId(rs.getLong("customerid"));}catch(NullPointerException e){}
			try{cus.setFirstname(rs.getString("cusfirstname"));}catch(NullPointerException e){}
			try{cus.setMiddlename(rs.getString("cusmiddlename"));}catch(NullPointerException e){}
			try{cus.setLastname(rs.getString("cuslastname"));}catch(NullPointerException e){}
			try{cus.setFullname(rs.getString("fullname"));}catch(NullPointerException e){}
			try{cus.setGender(rs.getString("cusgender"));}catch(NullPointerException e){}
			try{cus.setAge(rs.getInt("cusage"));}catch(NullPointerException e){}
			//try{cus.setAddress(rs.getString("cusaddress"));}catch(NullPointerException e){}
			try{cus.setContactno(rs.getString("cuscontactno"));}catch(NullPointerException e){}
			try{cus.setDateregistered(rs.getString("cusdateregistered"));}catch(NullPointerException e){}
			try{cus.setCardno(rs.getString("cuscardno"));}catch(NullPointerException e){}
			try{cus.setIsactive(rs.getInt("cusisactive"));}catch(NullPointerException e){}
			try{cus.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			try{Customer emergency = new Customer();
			emergency.setId(rs.getLong("emeperson"));
			cus.setEmergencyContactPerson(emergency);}catch(NullPointerException e){}
			try{cus.setRelationship(rs.getInt("relid"));}catch(NullPointerException e){}
			cl.setTaxPayer(cus);
			
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
			
			String sqlAdd_1 = " AND mu.isactivemultipurpose=1 AND mu.clearid="+ cl.getId();
			try{cl.setMultipurpose(MultiPurpose.retrieve(sqlAdd_1, new String[0]));}catch(Exception e){}
			
			sqlAdd_1 = " AND mu.isactivemultiliv=1 AND mu.clearid="+ cl.getId();
			try{cl.setMultilivelihood(MultiLivelihood.retrieve(sqlAdd_1, new String[0]));}catch(Exception e){}
			
			clears.add(cl);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return clears;
	}
	
	public static Clearance save(Clearance cl){
		if(cl!=null){
			
			long id = Clearance.getInfo(cl.getId() ==0? Clearance.getLatestId()+1 : cl.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				cl = Clearance.insertData(cl, "1");
			}else if(id==2){
				LogU.add("update Data ");
				cl = Clearance.updateData(cl);
			}else if(id==3){
				LogU.add("added new Data ");
				cl = Clearance.insertData(cl, "3");
			}
			
		}
		return cl;
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
	
	public static Clearance insertData(Clearance cl, String type){
		String sql = "INSERT INTO clearance ("
				+ "clearid,"
				+ "clearissueddate,"
				+ "clearancebarcode,"
				+ "ornumber,"
				+ "cedulanumber,"
				+ "clearnotes,"
				+ "photoid,"
				+ "amountpaid,"
				+ "purposetype,"
				+ "clearstatus,"
				+ "payable,"
				+ "cleartype,"
				+ "isactiveclearance,"
				+ "empid,"
				+ "customerid,"
				+ "userdtlsid,"
				+ "doctype,"
				+ "doctvalid,"
				+ "customtitle)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table clearance");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			cl.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			cl.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, cl.getIssuedDate());
		ps.setString(cnt++, cl.getBarcode());
		ps.setString(cnt++, cl.getOrNumber());
		ps.setString(cnt++, cl.getCedulaNumber());
		ps.setString(cnt++, cl.getNotes());
		ps.setString(cnt++, cl.getPhotoId());
		ps.setDouble(cnt++, cl.getAmountPaid());
		ps.setInt(cnt++, cl.getPurposeType());
		ps.setInt(cnt++, cl.getStatus());
		ps.setInt(cnt++, cl.getIsPayable());
		ps.setInt(cnt++, cl.getClearanceType());
		ps.setInt(cnt++, cl.getIsActive());
		ps.setLong(cnt++, cl.getEmployee()==null? 0 : cl.getEmployee().getId());
		ps.setLong(cnt++, cl.getTaxPayer()==null? 0 : cl.getTaxPayer().getId());
		ps.setLong(cnt++, cl.getUserDtls()==null? 0 : cl.getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, cl.getDocumentType());
		ps.setInt(cnt++, cl.getDocumentValidity());
		ps.setString(cnt++, cl.getCustomTitle());
		
		LogU.add(cl.getIssuedDate());
		LogU.add(cl.getBarcode());
		LogU.add(cl.getOrNumber());
		LogU.add(cl.getCedulaNumber());
		LogU.add(cl.getNotes());
		LogU.add(cl.getPhotoId());
		LogU.add(cl.getAmountPaid());
		LogU.add(cl.getPurposeType());
		LogU.add(cl.getStatus());
		LogU.add(cl.getIsPayable());
		LogU.add(cl.getClearanceType());
		LogU.add(cl.getIsActive());
		LogU.add(cl.getEmployee()==null? 0 : cl.getEmployee().getId());
		LogU.add(cl.getTaxPayer()==null? 0 : cl.getTaxPayer().getId());
		LogU.add(cl.getUserDtls()==null? 0 : cl.getUserDtls().getUserdtlsid());
		LogU.add(cl.getDocumentType());
		LogU.add(cl.getDocumentValidity());
		LogU.add(cl.getCustomTitle());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to clearance : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return cl;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO clearance ("
				+ "clearid,"
				+ "clearissueddate,"
				+ "clearancebarcode,"
				+ "ornumber,"
				+ "cedulanumber,"
				+ "clearnotes,"
				+ "photoid,"
				+ "amountpaid,"
				+ "purposetype,"
				+ "clearstatus,"
				+ "payable,"
				+ "cleartype,"
				+ "isactiveclearance,"
				+ "empid,"
				+ "customerid,"
				+ "userdtlsid,"
				+ "doctype,"
				+ "doctvalid,"
				+ "customtitle)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table clearance");
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
		
		ps.setString(cnt++, getIssuedDate());
		ps.setString(cnt++, getBarcode());
		ps.setString(cnt++, getOrNumber());
		ps.setString(cnt++, getCedulaNumber());
		ps.setString(cnt++, getNotes());
		ps.setString(cnt++, getPhotoId());
		ps.setDouble(cnt++, getAmountPaid());
		ps.setInt(cnt++, getPurposeType());
		ps.setInt(cnt++, getStatus());
		ps.setInt(cnt++, getIsPayable());
		ps.setInt(cnt++, getClearanceType());
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getEmployee()==null? 0 : getEmployee().getId());
		ps.setLong(cnt++, getTaxPayer()==null? 0 : getTaxPayer().getId());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, getDocumentType());
		ps.setInt(cnt++, getDocumentValidity());
		ps.setString(cnt++, getCustomTitle());
		
		LogU.add(getIssuedDate());
		LogU.add(getBarcode());
		LogU.add(getOrNumber());
		LogU.add(getCedulaNumber());
		LogU.add(getNotes());
		LogU.add(getPhotoId());
		LogU.add(getAmountPaid());
		LogU.add(getPurposeType());
		LogU.add(getStatus());
		LogU.add(getIsPayable());
		LogU.add(getClearanceType());
		LogU.add(getIsActive());
		LogU.add(getEmployee()==null? 0 : getEmployee().getId());
		LogU.add(getTaxPayer()==null? 0 : getTaxPayer().getId());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getDocumentType());
		LogU.add(getDocumentValidity());
		LogU.add(getCustomTitle());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to clearance : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Clearance updateData(Clearance cl){
		String sql = "UPDATE clearance SET "
				+ "clearissueddate=?,"
				+ "clearancebarcode=?,"
				+ "ornumber=?,"
				+ "cedulanumber=?,"
				+ "clearnotes=?,"
				+ "photoid=?,"
				+ "amountpaid=?,"
				+ "purposetype=?,"
				+ "clearstatus=?,"
				+ "payable=?,"
				+ "cleartype=?,"
				+ "empid=?,"
				+ "customerid=?,"
				+ "userdtlsid=?,"
				+ "doctype=?,"
				+ "doctvalid=?,"
				+ "customtitle=?" 
				+ " WHERE clearid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table clearance");
		
		
		ps.setString(cnt++, cl.getIssuedDate());
		ps.setString(cnt++, cl.getBarcode());
		ps.setString(cnt++, cl.getOrNumber());
		ps.setString(cnt++, cl.getCedulaNumber());
		ps.setString(cnt++, cl.getNotes());
		ps.setString(cnt++, cl.getPhotoId());
		ps.setDouble(cnt++, cl.getAmountPaid());
		ps.setInt(cnt++, cl.getPurposeType());
		ps.setInt(cnt++, cl.getStatus());
		ps.setInt(cnt++, cl.getIsPayable());
		ps.setInt(cnt++, cl.getClearanceType());
		ps.setLong(cnt++, cl.getEmployee()==null? 0 : cl.getEmployee().getId());
		ps.setLong(cnt++, cl.getTaxPayer()==null? 0 : cl.getTaxPayer().getId());
		ps.setLong(cnt++, cl.getUserDtls()==null? 0 : cl.getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, cl.getDocumentType());
		ps.setInt(cnt++, cl.getDocumentValidity());
		ps.setString(cnt++, cl.getCustomTitle());
		ps.setLong(cnt++, cl.getId());
		
		LogU.add(cl.getIssuedDate());
		LogU.add(cl.getBarcode());
		LogU.add(cl.getOrNumber());
		LogU.add(cl.getCedulaNumber());
		LogU.add(cl.getNotes());
		LogU.add(cl.getPhotoId());
		LogU.add(cl.getAmountPaid());
		LogU.add(cl.getPurposeType());
		LogU.add(cl.getStatus());
		LogU.add(cl.getIsPayable());
		LogU.add(cl.getClearanceType());
		LogU.add(cl.getEmployee()==null? 0 : cl.getEmployee().getId());
		LogU.add(cl.getTaxPayer()==null? 0 : cl.getTaxPayer().getId());
		LogU.add(cl.getUserDtls()==null? 0 : cl.getUserDtls().getUserdtlsid());
		LogU.add(cl.getDocumentType());
		LogU.add(cl.getDocumentValidity());
		LogU.add(cl.getCustomTitle());
		LogU.add(cl.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to clearance : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return cl;
	}
	
	public void updateData(){
		String sql = "UPDATE clearance SET "
				+ "clearissueddate=?,"
				+ "clearancebarcode=?,"
				+ "ornumber=?,"
				+ "cedulanumber=?,"
				+ "clearnotes=?,"
				+ "photoid=?,"
				+ "amountpaid=?,"
				+ "purposetype=?,"
				+ "clearstatus=?,"
				+ "payable=?,"
				+ "cleartype=?,"
				+ "empid=?,"
				+ "customerid=?,"
				+ "userdtlsid=?,"
				+ "doctype=?,"
				+ "doctvalid=?,"
				+ "customtitle=?" 
				+ " WHERE clearid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table clearance");
		
		
		ps.setString(cnt++, getIssuedDate());
		ps.setString(cnt++, getBarcode());
		ps.setString(cnt++, getOrNumber());
		ps.setString(cnt++, getCedulaNumber());
		ps.setString(cnt++, getNotes());
		ps.setString(cnt++, getPhotoId());
		ps.setDouble(cnt++, getAmountPaid());
		ps.setInt(cnt++, getPurposeType());
		ps.setInt(cnt++, getStatus());
		ps.setInt(cnt++, getIsPayable());
		ps.setInt(cnt++, getClearanceType());
		ps.setLong(cnt++, getEmployee()==null? 0 : getEmployee().getId());
		ps.setLong(cnt++, getTaxPayer()==null? 0 : getTaxPayer().getId());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, getDocumentType());
		ps.setInt(cnt++, getDocumentValidity());
		ps.setString(cnt++, getCustomTitle());
		ps.setLong(cnt++, getId());
		
		LogU.add(getIssuedDate());
		LogU.add(getBarcode());
		LogU.add(getOrNumber());
		LogU.add(getCedulaNumber());
		LogU.add(getNotes());
		LogU.add(getPhotoId());
		LogU.add(getAmountPaid());
		LogU.add(getPurposeType());
		LogU.add(getStatus());
		LogU.add(getIsPayable());
		LogU.add(getClearanceType());
		LogU.add(getEmployee()==null? 0 : getEmployee().getId());
		LogU.add(getTaxPayer()==null? 0 : getTaxPayer().getId());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getDocumentType());
		LogU.add(getDocumentValidity());
		LogU.add(getCustomTitle());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to clearance : " + s.getMessage());
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
		sql="SELECT clearid FROM clearance  ORDER BY clearid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("clearid");
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
		ps = conn.prepareStatement("SELECT clearid FROM clearance WHERE clearid=?");
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
		String sql = "UPDATE clearance set isactiveclearance=0,userdtlsid="+ getUserDtls().getUserdtlsid() +" WHERE clearid=?";
		
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
