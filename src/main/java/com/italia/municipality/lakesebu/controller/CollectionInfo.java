package com.italia.municipality.lakesebu.controller;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @since 08-11-2018
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class CollectionInfo {

	private long id;
	private String receivedDate;
	private int formType;
	private long beginningNo;
	private long endingNo;
	private int pcs;
	private int isActive;
	private int status;
	private int fundId;
	private Collector collector;
	private IssuedForm issuedForm;
	private int prevPcs;
	
	private double amount;
	private int rptGroup;
	
	private String formTypeName;
	private String statusName;
	
	private String startNo;
	private String endNo;
	
	private String rptFormat;
	private String fundName;
	private boolean hasCashTicket;
	
	private int isRts;
	
	public static Map<Integer, Map<Integer, Double>> graph(List years) {
		
		Map<Integer, Map<Integer, Double>> mapData = new LinkedHashMap<Integer, Map<Integer, Double>>();
		
		for(Object year : years) {
			int yer = Integer.valueOf(year.toString());
			mapData.put(yer, CollectionInfo.retrieveYear(yer));
		}
		
		return mapData;
		
	}
	
	public static Map<Integer, Map<Integer, Map<Integer, Double>>> graph(List years, List collectors, int form) {
		Map<Integer, Map<Integer, Map<Integer, Double>>> mapYear = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		
		for(Object collector : collectors) {
			int col = Integer.valueOf(collector.toString());
			for(Object year : years) {
				int yer = Integer.valueOf(year.toString());
				mapYear = CollectionInfo.retrieveYear(yer,col,form,mapYear);
			}
		}
		
		
		return mapYear;
		
	}
	
	/*
	 * public static Map<Integer,Map<Integer, Map<Integer, Map<Integer, Double>>>>
	 * graph(List years, List collectors, List forms) { Map<Integer, Map<Integer,
	 * Map<Integer, Map<Integer, Double>>>> mapYear = new LinkedHashMap<Integer,
	 * Map<Integer, Map<Integer, Map<Integer, Double>>>>();
	 * 
	 * for(Object form : forms) { int fm = Integer.valueOf(form.toString());
	 * for(Object collector : collectors) { int col =
	 * Integer.valueOf(collector.toString()); for(Object year : years) { int yer =
	 * Integer.valueOf(year.toString()); mapYear =
	 * CollectionInfo.retrieveYear(yer,col,fm,mapYear); } } }
	 * 
	 * 
	 * return mapYear;
	 * 
	 * }
	 */
	
	public static Map<Integer, Double> retrieveYear(int year){
		Map<Integer, Double> mapData = new LinkedHashMap<Integer, Double>();
		
		String[] params = new String[2];
		params[0] = year + "-01-01";
		params[1] = year + "-12-31";
		String sql = "SELECT DATE_FORMAT(receiveddate,'%m') as month, sum(amount) as amount FROM collectioninfo WHERE isactivecol=1 and (receiveddate>=? AND receiveddate<=?) GROUP BY MONTH(receiveddate)";
		
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
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				mapData.put(rs.getInt("month"), rs.getDouble("amount"));
			}
		
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
			}catch(Exception e){e.getMessage();}
		
		return mapData;
	}
	
	public static Map<Integer, Map<Integer, Map<Integer, Double>>> retrieveYear(int year, int collector,int form,Map<Integer, Map<Integer, Map<Integer, Double>>> mapYear){
		//Map<Integer, Map<Integer, Map<Integer, Double>>> mapYear = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<Integer, Double>> mapCollector = new LinkedHashMap<Integer, Map<Integer, Double>>();
		 Map<Integer, Double> mapMonth = new LinkedHashMap<Integer, Double>();
		String[] params = new String[3];
		params[0] = year + "-01-01";
		params[1] = year + "-12-31";
		params[2] = collector+"";
		String sql = "SELECT DATE_FORMAT(receiveddate,'%Y') as year,DATE_FORMAT(receiveddate,'%m') as month,isid as collector, sum(amount) as amount FROM collectioninfo WHERE isactivecol=1 and (receiveddate>=? AND receiveddate<=?) AND isid=?";
		
		if(form>0) {
			sql += " AND formtypecol=" + form;
		}
		
		sql += " GROUP BY MONTH(receiveddate)";
		
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
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				//mapData.put(rs.getInt("month"), rs.getDouble("amount"));
				int yr = rs.getInt("year");
				int mt = rs.getInt("month");
				int col = rs.getInt("collector");
				double amount = rs.getDouble("amount");
				
				if(mapYear!=null && mapYear.containsKey(yr)) {
					if(mapYear.get(yr).containsKey(col)) {
						if(mapYear.get(yr).get(col).containsKey(mt)) {
							double amnt = mapYear.get(yr).get(col).get(mt) + amount;
							mapYear.get(yr).get(col).put(mt, amnt);
						}else {
							mapYear.get(yr).get(col).put(mt, amount);
						}
					}else {
						mapMonth = new LinkedHashMap<Integer, Double>();
						mapMonth.put(mt, amount);
						mapYear.get(yr).put(col, mapMonth);
					}
				}else {
					mapMonth.put(mt, amount);
					mapCollector.put(col, mapMonth);
					mapYear.put(yr, mapCollector);
				}
			}
		
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
			}catch(Exception e){e.getMessage();}
		
		return mapYear;
	}
	
	/*
	 * public static Map<Integer, Map<Integer, Map<Integer, Map<Integer, Double>>>>
	 * retrieveYear(int year, int collector,int form, Map<Integer, Map<Integer,
	 * Map<Integer, Map<Integer, Double>>>> mapYear){ Map<Integer, Map<Integer,
	 * Map<Integer, Double>>> mapForm = new LinkedHashMap<Integer, Map<Integer,
	 * Map<Integer, Double>>>(); Map<Integer, Map<Integer, Double>> mapCollector =
	 * new LinkedHashMap<Integer, Map<Integer, Double>>(); Map<Integer, Double>
	 * mapMonth = new LinkedHashMap<Integer, Double>(); String[] params = new
	 * String[4]; params[0] = year + "-01-01"; params[1] = year + "-12-31";
	 * params[2] = collector+""; params[3] = form+""; String sql =
	 * "SELECT formtypecol as form,DATE_FORMAT(receiveddate,'%Y') as year,DATE_FORMAT(receiveddate,'%m') as month,isid as collector, sum(amount) as amount FROM collectioninfo WHERE isactivecol=1 and (receiveddate>=? AND receiveddate<=?) AND isid=? AND formtypecol=? GROUP BY MONTH(receiveddate)"
	 * ;
	 * 
	 * Connection conn = null; ResultSet rs = null; PreparedStatement ps = null;
	 * 
	 * try{ conn = WebTISDatabaseConnect.getConnection(); ps =
	 * conn.prepareStatement(sql);
	 * 
	 * if(params!=null && params.length>0){
	 * 
	 * for(int i=0; i<params.length; i++){ ps.setString(i+1, params[i]); }
	 * 
	 * }
	 * 
	 * rs = ps.executeQuery();
	 * 
	 * while(rs.next()){ //mapData.put(rs.getInt("month"), rs.getDouble("amount"));
	 * int frm = rs.getInt("form"); int yr = rs.getInt("year"); int mt =
	 * rs.getInt("month"); int col = rs.getInt("collector"); double amount =
	 * rs.getDouble("amount");
	 * 
	 * if(mapYear!=null && mapYear.containsKey(yr)) {
	 * if(mapYear.get(yr).containsKey(frm)) {
	 * if(mapYear.get(yr).get(frm).containsKey(col)) {
	 * if(mapYear.get(yr).get(frm).get(col).containsKey(mt)) { double newAmount =
	 * mapYear.get(yr).get(frm).get(col).get(mt) + amount;
	 * mapYear.get(yr).get(frm).get(col).put(mt, newAmount); }else {
	 * mapYear.get(yr).get(frm).get(col).put(mt, amount); } }else { mapMonth = new
	 * LinkedHashMap<Integer, Double>(); mapMonth.put(mt,amount); mapCollector = new
	 * LinkedHashMap<Integer, Map<Integer, Double>>(); mapCollector.put(col,
	 * mapMonth); mapYear.get(yr).put(frm, mapCollector); } }else { mapMonth = new
	 * LinkedHashMap<Integer, Double>(); mapMonth.put(mt,amount); mapCollector = new
	 * LinkedHashMap<Integer, Map<Integer, Double>>(); mapCollector.put(col,
	 * mapMonth); mapForm = new LinkedHashMap<Integer, Map<Integer, Map<Integer,
	 * Double>>>(); mapYear.get(yr).put(frm, mapCollector); } }else {
	 * mapMonth.put(mt, amount); mapCollector.put(col, mapMonth); mapForm.put(frm,
	 * mapCollector); mapYear.put(yr, mapForm); }
	 * 
	 * }
	 * 
	 * rs.close(); ps.close(); WebTISDatabaseConnect.close(conn); }catch(Exception
	 * e){e.getMessage();}
	 * 
	 * return mapYear; }
	 */
	
	public static int getNewReportGroup(long colloctorId, int fundId) {
		int rpt=0;
		
		String sql = "SELECT rptgroup FROM collectioninfo WHERE isactivecol=1 AND isid="+ colloctorId +" AND fundid="+ fundId +" ORDER BY colid DESC limit 1";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			rpt = rs.getInt("rptgroup");
		}
		
		rpt +=1;
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}	
		
		System.out.println("new group id >> " + rpt);
		
		return rpt;
	}
	
	public static boolean isGroupLatest(int groupNumber, long collectorId, int fundId) {
		int dbGroup=0;
		
		String sql = "SELECT rptgroup FROM collectioninfo WHERE isactivecol=1 AND isid="+ collectorId +" AND fundid="+ fundId +" ORDER BY colid DESC limit 1";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			dbGroup = rs.getInt("rptgroup");
			
			//System.out.println("Access group >> " + groupNumber);
			//System.out.println("dbGroup group >> " + dbGroup);
			
			if(groupNumber>=dbGroup) {
				return true;
			}else {
				return false;
			}
			
		}
		
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}	
		
		
		return false;
	}
	
	public static List<CollectionInfo> retrieve(String sqlAdd, String[] params){
		List<CollectionInfo> forms = new ArrayList<CollectionInfo>();
		
		String tableForm = "frm";
		String tableCol = "cl";
		String tableIssued = "sud";
		String sql = "SELECT * FROM collectioninfo "+ tableForm +", issuedcollector "+ tableCol  + ", logissuedform "+ tableIssued +"  WHERE  ";
		sql += tableForm + ".isid=" + tableCol + ".isid AND " + tableForm + ".logid=" + tableIssued + ".logid AND " + tableForm +".isactivecol=1 ";
		
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
		System.out.println("SQL Retrieve: " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			CollectionInfo form = new CollectionInfo();
			try{form.setId(rs.getLong("colid"));}catch(NullPointerException e){}
			try{form.setReceivedDate(rs.getString("receiveddate"));}catch(NullPointerException e){}
			try{form.setFormType(rs.getInt("formtypecol"));}catch(NullPointerException e){}
			try{form.setBeginningNo(rs.getLong("beginningNoCol"));}catch(NullPointerException e){}
			try{form.setEndingNo(rs.getLong("endingNoCol"));}catch(NullPointerException e){}
			try{form.setPcs(rs.getInt("colpcs"));}catch(NullPointerException e){}
			try{form.setIsActive(rs.getInt("isactivecol"));}catch(NullPointerException e){}
			try{form.setStatus(rs.getInt("colstatus"));}catch(NullPointerException e){}
			//try{form.setFormTypeName(FormType.nameId(rs.getInt("formtypecol")));}catch(NullPointerException e){}
			try{form.setFormTypeName(FormType.val(rs.getInt("formtypecol")).getDescription());}catch(NullPointerException e){}
			try{form.setStatusName(FormStatus.nameId(rs.getInt("colstatus")));}catch(NullPointerException e){}
			try{form.setPrevPcs(rs.getInt("prevPcs"));}catch(NullPointerException e){}
			try{form.setFundId(rs.getInt("fundid"));}catch(NullPointerException e){}
			try{form.setFundName(FundType.typeName(rs.getInt("fundid")));}catch(NullPointerException e){}
			
			try{form.setAmount(rs.getDouble("amount"));}catch(NullPointerException e){}
			try{form.setRptGroup(rs.getInt("rptgroup"));}catch(NullPointerException e){}
			try{form.setIsRts(rs.getInt("rts"));}catch(NullPointerException e){}
			
			IssuedForm issued = new IssuedForm();
			try{issued.setId(rs.getLong("logid"));}catch(NullPointerException e){}
			try{issued.setIssuedDate(rs.getString("issueddate"));}catch(NullPointerException e){}
			try{issued.setFormType(rs.getInt("formtypelog"));}catch(NullPointerException e){}
			try{issued.setBeginningNo(rs.getLong("beginningNoLog"));}catch(NullPointerException e){}
			try{issued.setEndingNo(rs.getLong("endingNoLog"));}catch(NullPointerException e){}
			try{issued.setPcs(rs.getInt("logpcs"));}catch(NullPointerException e){}
			try{issued.setIsActive(rs.getInt("isactivelog"));}catch(NullPointerException e){}
			try{issued.setStatus(rs.getInt("formstatus"));}catch(NullPointerException e){}
			try{issued.setFormTypeName(FormType.nameId(rs.getInt("formtypelog")));}catch(NullPointerException e){}
			try{issued.setStatusName(FormStatus.nameId(rs.getInt("formstatus")));}catch(NullPointerException e){}
			form.setIssuedForm(issued);
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			form.setCollector(col);
			
			forms.add(form);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return forms;
	}
	
	public CollectionInfo retrieve(long id){
		CollectionInfo form = new CollectionInfo();
		
		String tableForm = "frm";
		String tableCol = "cl";
		String tableIssued = "sud";
		String sql = "SELECT * FROM collectioninfo "+ tableForm +", issuedcollector "+ tableCol  + ", logissuedform "+ tableIssued +"  WHERE  ";
		sql += tableForm + ".isid=" + tableCol + ".isid AND " + tableForm + ".logid=" + tableIssued + ".logid AND " + tableForm +".isactivecol=1 AND " + tableForm +".colid="+id;
		
		
				
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			try{form.setId(rs.getLong("colid"));}catch(NullPointerException e){}
			try{form.setReceivedDate(rs.getString("receiveddate"));}catch(NullPointerException e){}
			try{form.setFormType(rs.getInt("formtypecol"));}catch(NullPointerException e){}
			try{form.setBeginningNo(rs.getLong("beginningNoCol"));}catch(NullPointerException e){}
			try{form.setEndingNo(rs.getLong("endingNoCol"));}catch(NullPointerException e){}
			try{form.setPcs(rs.getInt("colpcs"));}catch(NullPointerException e){}
			try{form.setIsActive(rs.getInt("isactivecol"));}catch(NullPointerException e){}
			try{form.setStatus(rs.getInt("colstatus"));}catch(NullPointerException e){}
			try{form.setFormTypeName(FormType.nameId(rs.getInt("formtypecol")));}catch(NullPointerException e){}
			try{form.setStatusName(FormStatus.nameId(rs.getInt("colstatus")));}catch(NullPointerException e){}
			try{form.setPrevPcs(rs.getInt("prevPcs"));}catch(NullPointerException e){}
			try{form.setFundId(rs.getInt("fundid"));}catch(NullPointerException e){}
			try{form.setFundName(FundType.typeName(rs.getInt("fundid")));}catch(NullPointerException e){}
			try{form.setAmount(rs.getDouble("amount"));}catch(NullPointerException e){}
			try{form.setRptGroup(rs.getInt("rptgroup"));}catch(NullPointerException e){}
			try{form.setIsRts(rs.getInt("rts"));}catch(NullPointerException e){}
			
			IssuedForm issued = new IssuedForm();
			try{issued.setId(rs.getLong("logid"));}catch(NullPointerException e){}
			try{issued.setIssuedDate(rs.getString("issueddate"));}catch(NullPointerException e){}
			try{issued.setFormType(rs.getInt("formtypelog"));}catch(NullPointerException e){}
			try{issued.setBeginningNo(rs.getLong("beginningNoLog"));}catch(NullPointerException e){}
			try{issued.setEndingNo(rs.getLong("endingNoLog"));}catch(NullPointerException e){}
			try{issued.setPcs(rs.getInt("logpcs"));}catch(NullPointerException e){}
			try{issued.setIsActive(rs.getInt("isactivelog"));}catch(NullPointerException e){}
			try{issued.setStatus(rs.getInt("formstatus"));}catch(NullPointerException e){}
			try{issued.setFormTypeName(FormType.nameId(rs.getInt("formtypelog")));}catch(NullPointerException e){}
			try{issued.setStatusName(FormStatus.nameId(rs.getInt("formstatus")));}catch(NullPointerException e){}
			form.setIssuedForm(issued);
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			form.setCollector(col);
			
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return form;
	}
	
	public static CollectionInfo save(CollectionInfo is){
		if(is!=null){
			
			long id = CollectionInfo.getInfo(is.getId() ==0? CollectionInfo.getLatestId()+1 : is.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				is = CollectionInfo.insertData(is, "1");
			}else if(id==2){
				LogU.add("update Data ");
				is = CollectionInfo.updateData(is);
			}else if(id==3){
				LogU.add("added new Data ");
				is = CollectionInfo.insertData(is, "3");
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
	
	public static CollectionInfo insertData(CollectionInfo col, String type){
		String sql = "INSERT INTO collectioninfo ("
				+ "colid,"
				+ "receiveddate,"
				+ "formtypecol,"
				+ "beginningNoCol,"
				+ "endingNoCol,"
				+ "colpcs,"
				+ "isactivecol,"
				+ "isid,"
				+ "logid,"
				+ "colstatus,"
				+ "prevPcs,"
				+ "amount,"
				+ "rptgroup,"
				+ "fundid,"
				+ "rts)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table collectioninfo");
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
		
		ps.setString(cnt++, col.getReceivedDate());
		ps.setInt(cnt++, col.getFormType());
		ps.setLong(cnt++, col.getBeginningNo());
		ps.setLong(cnt++, col.getEndingNo());
		ps.setInt(cnt++, col.getPcs());
		ps.setInt(cnt++, col.getIsActive());
		ps.setInt(cnt++, col.getCollector().getId());
		ps.setLong(cnt++, col.getIssuedForm().getId());
		ps.setInt(cnt++, col.getStatus());
		ps.setInt(cnt++, col.getPrevPcs());
		ps.setDouble(cnt++, col.getAmount());
		ps.setInt(cnt++, col.getRptGroup());
		ps.setInt(cnt++, col.getFundId());
		ps.setInt(cnt++, col.getIsRts());
		
		LogU.add(col.getReceivedDate());
		LogU.add(col.getFormType());
		LogU.add(col.getBeginningNo());
		LogU.add(col.getEndingNo());
		LogU.add(col.getPcs());
		LogU.add(col.getIsActive());
		LogU.add(col.getCollector().getId());
		LogU.add(col.getIssuedForm().getId());
		LogU.add(col.getStatus());
		LogU.add(col.getPrevPcs());
		LogU.add(col.getAmount());
		LogU.add(col.getRptGroup());
		LogU.add(col.getFundId());
		LogU.add(col.getIsRts());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to collectioninfo : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return col;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO collectioninfo ("
				+ "colid,"
				+ "receiveddate,"
				+ "formtypecol,"
				+ "beginningNoCol,"
				+ "endingNoCol,"
				+ "colpcs,"
				+ "isactivecol,"
				+ "isid,"
				+ "logid,"
				+ "colstatus,"
				+ "prevPcs,"
				+ "amount,"
				+ "rptgroup,"
				+ "fundid,"
				+ "rts)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table collectioninfo");
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
		
		ps.setString(cnt++, getReceivedDate());
		ps.setInt(cnt++, getFormType());
		ps.setLong(cnt++, getBeginningNo());
		ps.setLong(cnt++, getEndingNo());
		ps.setInt(cnt++, getPcs());
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getCollector().getId());
		ps.setLong(cnt++, getIssuedForm().getId());
		ps.setInt(cnt++, getStatus());
		ps.setInt(cnt++, getPrevPcs());
		ps.setDouble(cnt++, getAmount());
		ps.setInt(cnt++, getRptGroup());
		ps.setInt(cnt++, getFundId());
		ps.setInt(cnt++, getIsRts());
		
		LogU.add(getReceivedDate());
		LogU.add(getFormType());
		LogU.add(getBeginningNo());
		LogU.add(getEndingNo());
		LogU.add(getPcs());
		LogU.add(getIsActive());
		LogU.add(getCollector().getId());
		LogU.add(getIssuedForm().getId());
		LogU.add(getStatus());
		LogU.add(getPrevPcs());
		LogU.add(getAmount());
		LogU.add(getRptGroup());
		LogU.add(getFundId());
		LogU.add(getIsRts());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to collectioninfo : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static CollectionInfo updateData(CollectionInfo col){
		String sql = "UPDATE collectioninfo SET "
				+ "receiveddate=?,"
				+ "formtypecol=?,"
				+ "beginningNoCol=?,"
				+ "endingNoCol=?,"
				+ "colpcs=?,"
				+ "isid=?,"
				+ "logid=?,"
				+ "colstatus=?,"
				+ "prevPcs=?,"
				+ "amount=?,"
				+ "rptgroup=?,"
				+ "fundid=?,"
				+ "rts=? " 
				+ " WHERE colid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table collectioninfo");
		
		
		ps.setString(cnt++, col.getReceivedDate());
		ps.setInt(cnt++, col.getFormType());
		ps.setLong(cnt++, col.getBeginningNo());
		ps.setLong(cnt++, col.getEndingNo());
		ps.setInt(cnt++, col.getPcs());
		ps.setInt(cnt++, col.getCollector().getId());
		ps.setLong(cnt++,col.getIssuedForm().getId());
		ps.setInt(cnt++, col.getStatus());
		ps.setInt(cnt++, col.getPrevPcs());
		ps.setDouble(cnt++, col.getAmount());
		ps.setInt(cnt++, col.getRptGroup());
		ps.setInt(cnt++, col.getFundId());
		ps.setInt(cnt++, col.getIsRts());
		ps.setLong(cnt++, col.getId());
		
		LogU.add(col.getReceivedDate());
		LogU.add(col.getFormType());
		LogU.add(col.getBeginningNo());
		LogU.add(col.getEndingNo());
		LogU.add(col.getPcs());
		LogU.add(col.getCollector().getId());
		LogU.add(col.getIssuedForm().getId());
		LogU.add(col.getStatus());
		LogU.add(col.getPrevPcs());
		LogU.add(col.getAmount());
		LogU.add(col.getRptGroup());
		LogU.add(col.getFundId());
		LogU.add(col.getIsRts());
		LogU.add(col.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to collectioninfo : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return col;
	}
	
	public void updateData(){
		String sql = "UPDATE collectioninfo SET "
				+ "receiveddate=?,"
				+ "formtypecol=?,"
				+ "beginningNoCol=?,"
				+ "endingNoCol=?,"
				+ "colpcs=?,"
				+ "isid=?,"
				+ "logid=?,"
				+ "colstatus=?,"
				+ "prevPcs=?,"
				+ "amount=?,"
				+ "rptgroup=?,"
				+ "fundid=?,"
				+ "rts=? " 
				+ " WHERE colid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table collectioninfo");
		
		
		ps.setString(cnt++, getReceivedDate());
		ps.setInt(cnt++, getFormType());
		ps.setLong(cnt++, getBeginningNo());
		ps.setLong(cnt++, getEndingNo());
		ps.setInt(cnt++, getPcs());
		ps.setInt(cnt++, getCollector().getId());
		ps.setLong(cnt++,getIssuedForm().getId());
		ps.setInt(cnt++, getStatus());
		ps.setInt(cnt++, getPrevPcs());
		ps.setDouble(cnt++, getAmount());
		ps.setInt(cnt++, getRptGroup());
		ps.setInt(cnt++, getFundId());
		ps.setInt(cnt++, getIsRts());
		ps.setLong(cnt++, getId());
		
		LogU.add(getReceivedDate());
		LogU.add(getFormType());
		LogU.add(getBeginningNo());
		LogU.add(getEndingNo());
		LogU.add(getPcs());
		LogU.add(getCollector().getId());
		LogU.add(getIssuedForm().getId());
		LogU.add(getStatus());
		LogU.add(getPrevPcs());
		LogU.add(getAmount());
		LogU.add(getRptGroup());
		LogU.add(getFundId());
		LogU.add(getIsRts());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to collectioninfo : " + s.getMessage());
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
		sql="SELECT colid FROM collectioninfo  ORDER BY colid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("colid");
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
		ps = conn.prepareStatement("SELECT colid FROM collectioninfo WHERE colid=?");
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
		String sql = "UPDATE collectioninfo set isactivecol=0 WHERE colid=?";
		
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
