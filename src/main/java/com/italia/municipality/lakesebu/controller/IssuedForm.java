package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.LogU;

import jakarta.faces.model.SelectItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author Mark Italia
 * @since 08-11-2018
 * @version 1.0
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class IssuedForm {

	private long id;
	private String issuedDate;
	private int formType;
	private long beginningNo;
	private long endingNo;
	private int pcs;
	private int isActive;
	private int status;
	private int fundId;
	private Collector collector;
	private Stocks stock;
	private int stabNo;
	private int isForBarangayOR;
	
	private String formTypeName;
	private String statusName;
	private String fundName;
	
	private int newSeries;//use for updating series
	
	private List formStatus;
	
	public List getFormStatus() {
		formStatus = new ArrayList<>();
		for(FormStatus s : FormStatus.values()) {
			formStatus.add(new SelectItem(s.getId(), s.getName()));
		}
		return formStatus;
	}
	
	public static List<IssuedForm> retrieve(String sqlAdd, String[] params){
		List<IssuedForm> forms = new ArrayList<IssuedForm>();
		
		String tableForm = "frm";
		String tableCol = "cl";
		String tableSt = "st";
		String sql = "SELECT * FROM logissuedform "+ tableForm +", issuedcollector "+ tableCol  + ", stockreceipt "+ tableSt +" WHERE  ";
		sql += tableForm + ".isid=" + tableCol + ".isid AND " + tableForm +".isactivelog=1 AND " + tableForm +".stockid=" + tableSt + ".stockid ";
		
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
			
			IssuedForm form = new IssuedForm();
			try{form.setId(rs.getLong("logid"));}catch(NullPointerException e){}
			try{form.setIssuedDate(rs.getString("issueddate"));}catch(NullPointerException e){}
			try{form.setFormType(rs.getInt("formtypelog"));}catch(NullPointerException e){}
			try{form.setBeginningNo(rs.getLong("beginningNoLog"));}catch(NullPointerException e){}
			try{form.setEndingNo(rs.getLong("endingNoLog"));}catch(NullPointerException e){}
			try{form.setPcs(rs.getInt("logpcs"));}catch(NullPointerException e){}
			try{form.setIsActive(rs.getInt("isactivelog"));}catch(NullPointerException e){}
			try{form.setStatus(rs.getInt("formstatus"));}catch(NullPointerException e){}
			//try{form.setFormTypeName(FormType.nameId(rs.getInt("formtypelog")));}catch(NullPointerException e){}
			try{form.setFormTypeName(FormType.val(rs.getInt("formtypelog")).getDescription());}catch(NullPointerException e){}
			try{form.setStatusName(FormStatus.nameId(rs.getInt("formstatus")));}catch(NullPointerException e){}
			try{form.setFundId(rs.getInt("fundid"));}catch(NullPointerException e){}
			try{form.setFundName(FundType.typeName(rs.getInt("fundid")));}catch(NullPointerException e){}
			try{form.setStabNo(rs.getInt("stabno"));}catch(NullPointerException e){}
			try {form.setIsForBarangayOR(rs.getInt("isbarangay"));}catch(Exception e) {}
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			form.setCollector(col);
			
			Stocks st = new Stocks();
			try{st.setId(rs.getLong("stockid"));}catch(NullPointerException e){}
			try{st.setDateTrans(rs.getString("datetrans"));}catch(NullPointerException e){}
			try{st.setSeriesFrom(rs.getString("seriesfrom"));}catch(NullPointerException e){}
			try{st.setSeriesTo(rs.getString("seriesto"));}catch(NullPointerException e){}
			try{st.setStatus(rs.getInt("statusstock"));}catch(NullPointerException e){}
			try{st.setIsActive(rs.getInt("isactivestock"));}catch(NullPointerException e){}
			try{st.setFormType(rs.getInt("formType"));}catch(NullPointerException e){}
			try{st.setQuantity(rs.getInt("qty"));}catch(NullPointerException e){}
			
			try{
			if(rs.getInt("statusstock")==1) {
				st.setStatusName("NOT ISSUED");
			}else {
				st.setStatusName("ISSUED");
			}
			}catch(NullPointerException e){}
			
			try{
			st.setFormTypeName(FormType.nameId(rs.getInt("formType")));
			//	st.setFormTypeName(FormType.val(rs.getInt("formType")).getDescription());
			form.setStock(st);
			}catch(NullPointerException e){}
			
			forms.add(form);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return forms;
	}
	
	public static IssuedForm retrieve(long id){
		IssuedForm form = new IssuedForm();
		
		String tableForm = "frm";
		String tableCol = "cl";
		String tableSt = "st";
		String sql = "SELECT * FROM logissuedform "+ tableForm +", issuedcollector "+ tableCol  + ", stockreceipt "+ tableSt +" WHERE  ";
		sql += tableForm + ".isid=" + tableCol + ".isid AND " + tableForm +".isactivelog=1 AND " + tableForm +".stockid=" + tableSt + ".stockid AND " + tableForm +".logid=" + id;
				
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{form.setId(rs.getLong("logid"));}catch(NullPointerException e){}
			try{form.setIssuedDate(rs.getString("issueddate"));}catch(NullPointerException e){}
			try{form.setFormType(rs.getInt("formtypelog"));}catch(NullPointerException e){}
			try{form.setBeginningNo(rs.getLong("beginningNoLog"));}catch(NullPointerException e){}
			try{form.setEndingNo(rs.getLong("endingNoLog"));}catch(NullPointerException e){}
			try{form.setPcs(rs.getInt("logpcs"));}catch(NullPointerException e){}
			try{form.setIsActive(rs.getInt("isactivelog"));}catch(NullPointerException e){}
			try{form.setStatus(rs.getInt("formstatus"));}catch(NullPointerException e){}
			try{form.setFormTypeName(FormType.nameId(rs.getInt("formtypelog")));}catch(NullPointerException e){}
			try{form.setStatusName(FormStatus.nameId(rs.getInt("formstatus")));}catch(NullPointerException e){}
			try{form.setFundId(rs.getInt("fundid"));}catch(NullPointerException e){}
			try{form.setFundName(FundType.typeName(rs.getInt("fundid")));}catch(NullPointerException e){}
			try{form.setStabNo(rs.getInt("stabno"));}catch(NullPointerException e){}
			try {form.setIsForBarangayOR(rs.getInt("isbarangay"));}catch(Exception e) {}
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			form.setCollector(col);
			
			Stocks st = new Stocks();
			try{st.setId(rs.getLong("stockid"));}catch(NullPointerException e){}
			try{st.setDateTrans(rs.getString("datetrans"));}catch(NullPointerException e){}
			try{st.setSeriesFrom(rs.getString("seriesfrom"));}catch(NullPointerException e){}
			try{st.setSeriesTo(rs.getString("seriesto"));}catch(NullPointerException e){}
			try{st.setStatus(rs.getInt("statusstock"));}catch(NullPointerException e){}
			try{st.setIsActive(rs.getInt("isactivestock"));}catch(NullPointerException e){}
			try{st.setFormType(rs.getInt("formType"));}catch(NullPointerException e){}
			try{st.setQuantity(rs.getInt("qty"));}catch(NullPointerException e){}
			
			try{
			if(rs.getInt("statusstock")==1) {
				st.setStatusName("NOT ISSUED");
			}else {
				st.setStatusName("ISSUED");
			}
			}catch(NullPointerException e){}
			
			try{
			st.setFormTypeName(FormType.nameId(rs.getInt("formType")));
			form.setStock(st);
			}catch(NullPointerException e){}
			
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return form;
	}
	
	public static IssuedForm retrieveId(long id){
		IssuedForm form = new IssuedForm();
		
		String sql = "SELECT * FROM logissuedform WHERE logid=" + id;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			try{form.setId(rs.getLong("logid"));}catch(NullPointerException e){}
			try{form.setIssuedDate(rs.getString("issueddate"));}catch(NullPointerException e){}
			try{form.setFormType(rs.getInt("formtypelog"));}catch(NullPointerException e){}
			try{form.setBeginningNo(rs.getLong("beginningNoLog"));}catch(NullPointerException e){}
			try{form.setEndingNo(rs.getLong("endingNoLog"));}catch(NullPointerException e){}
			try{form.setPcs(rs.getInt("logpcs"));}catch(NullPointerException e){}
			try{form.setIsActive(rs.getInt("isactivelog"));}catch(NullPointerException e){}
			try{form.setStatus(rs.getInt("formstatus"));}catch(NullPointerException e){}
			try{form.setFormTypeName(FormType.nameId(rs.getInt("formtypelog")));}catch(NullPointerException e){}
			try{form.setStatusName(FormStatus.nameId(rs.getInt("formstatus")));}catch(NullPointerException e){}
			try{form.setFundId(rs.getInt("fundid"));}catch(NullPointerException e){}
			try{form.setFundName(FundType.typeName(rs.getInt("fundid")));}catch(NullPointerException e){}
			try{form.setStabNo(rs.getInt("stabno"));}catch(NullPointerException e){}
			try {form.setIsForBarangayOR(rs.getInt("isbarangay"));}catch(Exception e) {}
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			form.setCollector(col);
			
			Stocks st = new Stocks();
			try{st.setId(rs.getLong("stockid"));}catch(NullPointerException e){}
			form.setStock(st);
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return form;
	}
	
	public static boolean isExistInCollection(long logid) {
		
		String sql = "SELECT * FROM collectioninfo WHERE isactivecol=1 AND logid=" + logid;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("Check existing SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static IssuedForm save(IssuedForm is){
		if(is!=null){
			
			long id = IssuedForm.getInfo(is.getId() ==0? IssuedForm.getLatestId()+1 : is.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				is = IssuedForm.insertData(is, "1");
			}else if(id==2){
				LogU.add("update Data ");
				is = IssuedForm.updateData(is);
			}else if(id==3){
				LogU.add("added new Data ");
				is = IssuedForm.insertData(is, "3");
			}
			
		}
		return is;
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
	
	public static IssuedForm insertData(IssuedForm col, String type){
		String sql = "INSERT INTO logissuedform ("
				+ "logid,"
				+ "issueddate,"
				+ "formtypelog,"
				+ "beginningNoLog,"
				+ "endingNoLog,"
				+ "logpcs,"
				+ "isactivelog,"
				+ "isid,"
				+ "formstatus,"
				+ "stockid,"
				+ "fundid,"
				+ "stabno,"
				+ "isbarangay)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table logissuedform");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			col.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			col.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, col.getIssuedDate());
		ps.setInt(cnt++, col.getFormType());
		ps.setLong(cnt++, col.getBeginningNo());
		ps.setLong(cnt++, col.getEndingNo());
		ps.setInt(cnt++, col.getPcs());
		ps.setInt(cnt++, col.getIsActive());
		ps.setInt(cnt++, col.getCollector().getId());
		ps.setInt(cnt++, col.getStatus());
		ps.setLong(cnt++, col.getStock().getId());
		ps.setInt(cnt++, col.getFundId());
		ps.setInt(cnt++, col.getStabNo());
		ps.setInt(cnt++, col.getIsForBarangayOR());
		
		LogU.add(col.getIssuedDate());
		LogU.add(col.getFormType());
		LogU.add(col.getBeginningNo());
		LogU.add(col.getEndingNo());
		LogU.add(col.getPcs());
		LogU.add(col.getIsActive());
		LogU.add(col.getCollector().getId());
		LogU.add(col.getStatus());
		LogU.add(col.getStock().getId());
		LogU.add(col.getFundId());
		LogU.add(col.getStabNo());
		LogU.add(col.getIsForBarangayOR());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to logissuedform : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return col;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO logissuedform ("
				+ "logid,"
				+ "issueddate,"
				+ "formtypelog,"
				+ "beginningNoLog,"
				+ "endingNoLog,"
				+ "logpcs,"
				+ "isactivelog,"
				+ "isid,"
				+ "formstatus,"
				+ "stockid,"
				+ "fundid,"
				+ "stabno,"
				+ "isbarangay)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table logissuedform");
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
		ps.setInt(cnt++, getFormType());
		ps.setLong(cnt++, getBeginningNo());
		ps.setLong(cnt++, getEndingNo());
		ps.setInt(cnt++, getPcs());
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getCollector().getId());
		ps.setInt(cnt++, getStatus());
		ps.setLong(cnt++, getStock().getId());
		ps.setInt(cnt++, getFundId());
		ps.setInt(cnt++, getStabNo());
		ps.setInt(cnt++, getIsForBarangayOR());
		
		LogU.add(getIssuedDate());
		LogU.add(getFormType());
		LogU.add(getBeginningNo());
		LogU.add(getEndingNo());
		LogU.add(getPcs());
		LogU.add(getIsActive());
		LogU.add(getCollector().getId());
		LogU.add(getStatus());
		LogU.add(getStock().getId());
		LogU.add(getFundId());
		LogU.add(getStabNo());
		LogU.add(getIsForBarangayOR());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to logissuedform : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static IssuedForm updateData(IssuedForm col){
		String sql = "UPDATE logissuedform SET "
				+ "issueddate=?,"
				+ "formtypelog=?,"
				+ "beginningNoLog=?,"
				+ "endingNoLog=?,"
				+ "logpcs=?,"
				+ "isid=?,"
				+ "formstatus=?,"
				+ "stockid=?,"
				+ "fundid=?,"
				+ "stabno=?,"
				+ "isbarangay=? " 
				+ " WHERE logid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table logissuedform");
		
		ps.setString(cnt++, col.getIssuedDate());
		ps.setInt(cnt++, col.getFormType());
		ps.setLong(cnt++, col.getBeginningNo());
		ps.setLong(cnt++, col.getEndingNo());
		ps.setInt(cnt++, col.getPcs());
		ps.setInt(cnt++, col.getCollector().getId());
		ps.setInt(cnt++, col.getStatus());
		ps.setLong(cnt++, col.getStock().getId());
		ps.setInt(cnt++, col.getFundId());
		ps.setInt(cnt++, col.getStabNo());
		ps.setInt(cnt++, col.getIsForBarangayOR());
		ps.setLong(cnt++, col.getId());
		
		LogU.add(col.getIssuedDate());
		LogU.add(col.getFormType());
		LogU.add(col.getBeginningNo());
		LogU.add(col.getEndingNo());
		LogU.add(col.getPcs());
		LogU.add(col.getCollector().getId());
		LogU.add(col.getStatus());
		LogU.add(col.getStock().getId());
		LogU.add(col.getFundId());
		LogU.add(col.getStabNo());
		LogU.add(col.getIsForBarangayOR());
		LogU.add(col.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to logissuedform : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return col;
	}
	
	public void updateData(){
		String sql = "UPDATE logissuedform SET "
				+ "issueddate=?,"
				+ "formtypelog=?,"
				+ "beginningNoLog=?,"
				+ "endingNoLog=?,"
				+ "logpcs=?,"
				+ "isid=?,"
				+ "formstatus=?,"
				+ "stockid=?,"
				+ "fundid=?,"
				+ "stabno=?,"
				+ "isbarangay=? " 
				+ " WHERE logid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table logissuedform");
		
		ps.setString(cnt++, getIssuedDate());
		ps.setInt(cnt++, getFormType());
		ps.setLong(cnt++, getBeginningNo());
		ps.setLong(cnt++, getEndingNo());
		ps.setInt(cnt++, getPcs());
		ps.setInt(cnt++, getCollector().getId());
		ps.setInt(cnt++, getStatus());
		ps.setLong(cnt++, getStock().getId());
		ps.setInt(cnt++, getFundId());
		ps.setInt(cnt++, getStabNo());
		ps.setInt(cnt++, getIsForBarangayOR());
		ps.setLong(cnt++, getId());
		
		LogU.add(getIssuedDate());
		LogU.add(getFormType());
		LogU.add(getBeginningNo());
		LogU.add(getEndingNo());
		LogU.add(getPcs());
		LogU.add(getCollector().getId());
		LogU.add(getStatus());
		LogU.add(getStock().getId());
		LogU.add(getFundId());
		LogU.add(getStabNo());
		LogU.add(getIsForBarangayOR());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to logissuedform : " + s.getMessage());
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
		sql="SELECT logid FROM logissuedform  ORDER BY logid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("logid");
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
		ps = conn.prepareStatement("SELECT logid FROM logissuedform WHERE logid=?");
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
		System.out.println("Delete: SQL " + ps.toString());
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE logissuedform set isactivelog=0 WHERE logid=?";
		
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