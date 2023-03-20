package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 12/09/2022
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RequisitionIssueSlip {

	private long id;
	private String dateTrans;
	private String requestNo;
	private String codeNo;
	private String saiNo;
	private String saiDate;
	private String purspose;
	private String approvedBy;
	private String approvedPosition;
	private String issuedBy;
	private String issuedPosition;
	private String position;
	private int isActive;
	private int fundId;
	
	private Offices office;
	private Collector collector;
	private Division division;
	
	private List<Requisition> requisitions;
	
	private Date tmpDateTrans;
	private List divisions;
	private List offices;
	private List requestors;
	private List receivers;
	private List funds;
	
	private String fundName;
	
	public static Object[] checkAllFormsIsOkToEdit(RequisitionIssueSlip slip) {
		List<Requisition> reqs = new ArrayList<Requisition>();
		List<Stocks> stocks = new ArrayList<Stocks>();
		for(Requisition r : Requisition.retrieve(" AND  rx.rid="+ slip.getId(), new String[0])) {
			stocks.add(r.getStocks());
			reqs.add(r);
		}
		
		Object[] objs = new Object[4];
		List<CollectionInfo> infos = new ArrayList<CollectionInfo>();
		List<IssuedForm> iss = new ArrayList<IssuedForm>();
		int countStock = stocks.size();
		int countHanded = 0, countAllIssued=0, countRTS=0;
		for(Stocks s : stocks) {
			for(IssuedForm is : IssuedForm.retrieve(" AND st.stockid="+s.getId(), new String[0])) {
				
					System.out.println("Issued: id " + is.getId() + " stockid: " + is.getStock().getId());
				
					if(FormStatus.HANDED.getId()==is.getStatus()) {
						countHanded +=1;
					}else if(FormStatus.ALL_ISSUED.getId()==is.getStatus()) {
						countAllIssued +=1;
					}else if(FormStatus.RTS.getId()==is.getStatus()) {
						countRTS +=1;	
					}
					List<CollectionInfo> ins = CollectionInfo.retrieve(" AND frm.logid="+ is.getId(), new String[0]);
					if(ins!=null && ins.size()>0) {
							iss.add(is);
							infos.addAll(ins);
							System.out.println("infos>>> " + infos.size());
					}else {
							iss.add(is);
							System.out.println("iss>>> " + iss.size());
					}
			}
		}
		
		int totalForms = countHanded + countAllIssued + countRTS;
		System.out.println("Requested: " + countStock + " current: " + totalForms);
		if(totalForms==countStock) {objs[0] = true;} else {objs[0] = false;}
		objs[1] = infos;
		objs[2] = iss;
		objs[3] = reqs;
		
		System.out.println("Checking infos size : " + infos.size());
		System.out.println("Checking iss size : " + iss.size());
		System.out.println("Checking reqs size : " + reqs.size());
		return objs;
	}
	
	//this cannot be use in other module only in stock module
	//this function will delete data in logissuedform which is not yet all issued
	//requisition table
	//stockreceipt will rollback the collectorid =0 and status = handed/0
	public static boolean deleteFirstBeforeSavingRIS(long id) {
		//delete data in requesition table in database
		/*
		 * Connection conn = null; PreparedStatement ps = null; try{ conn =
		 * WebTISDatabaseConnect.getConnection(); ps =
		 * conn.prepareStatement("DELETE FROM requesition WHERE rid=" + id);
		 * ps.executeUpdate(); ps.close(); WebTISDatabaseConnect.close(conn); return
		 * true; }catch(Exception e){e.getMessage();}
		 */
		List<Requisition> reqs = Requisition.retrieve(" AND  rx.rid="+ id, new String[0]);
		int size = reqs.size();
		int count = 0;
		for(Requisition r : reqs) {
			
			IssuedForm.delete("DELETE FROM logissuedform WHERE stockid="+r.getStocks().getId() + " AND isactivelog =1 AND formstatus="+FormStatus.HANDED.getId(),new String[0]);//generic form deletion
			IssuedForm.delete("DELETE FROM logissuedform WHERE stockid="+r.getStocks().getId() + " AND formtypelog="+ FormType.AF_51.getId() +" AND isactivelog =1 AND formstatus="+FormStatus.ALL_ISSUED.getId() + " AND isbarangay=1",new String[0]);//this is for barangay for 51
			
			Stocks stock = r.getStocks();
			stock.setStatus(FormStatus.HANDED.getId());//rollback status from all issued to handed
			stock.setCollector(Collector.builder().id(0).build());
			stock.save();
			r.delete("DELETE FROM requesition WHERE rid="+id, new String[0]);
			count++;
		}
		if(size==count) {return true;}
		
		
		return false;
	}
	
	public static List<Stocks> stocksList(long risId){
		List<Stocks> stocks = new ArrayList<Stocks>();
		String sql = "SELECT * FROM requesition rq, stockreceipt st, issuedcollector cl WHERE rq.isactiverq=1 AND rq.stockid=st.stockid AND rq.rid="+risId + " AND st.isid=cl.isid";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		System.out.println("StockList : SQL: " + sql);
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		int count = 1;
		while(rs.next()){
			Stocks st = new Stocks();
			st.setId(rs.getLong("stockid"));
			st.setDateTrans(rs.getString("datetrans"));
			st.setSeriesFrom(rs.getString("seriesfrom"));
			st.setSeriesTo(rs.getString("seriesto"));
			st.setStatus(rs.getInt("statusstock"));
			st.setIsActive(rs.getInt("isactivestock"));
			st.setFormType(rs.getInt("formType"));
			st.setQuantity(rs.getInt("qty"));
			st.setStabNo(rs.getInt("stabno"));
			st.setCount(count++);
			
			if(rs.getInt("statusstock")==1) {
				st.setStatusName("NOT ISSUED");
			}else {
				st.setStatusName("ISSUED");
			}
			
			st.setFormTypeName(FormType.val(rs.getInt("formType")).getDescription());
			
			Collector col = new Collector();
			try{col.setId(rs.getInt("isid"));}catch(NullPointerException e){}
			try{col.setName(rs.getString("collectorname"));}catch(NullPointerException e){}
			try{col.setIsActive(rs.getInt("isactivecollector"));}catch(NullPointerException e){}
			try{col.setIsResigned(rs.getInt("isresigned"));}catch(NullPointerException e){}
			st.setCollector(col);
			
			stocks.add(st);
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		return stocks;
	}
	
	public static String generateNewSaiNo() {
		String year = DateUtils.getCurrentYear()+"";
		int mm = DateUtils.getCurrentMonth();
		String month = mm<10? "0"+mm : mm+"";
		String risNo="";
		String newSeries = "";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT risno FROM ris WHERE isactiver=1 ORDER BY risno DESC limit 1");
		rs = ps.executeQuery();
		String tmpRisNo = null;
		while(rs.next()){
			tmpRisNo = rs.getString("risno");
		}
		
		if(tmpRisNo!=null) {
			String[] vals = tmpRisNo.split("-");
			String tmpyear = vals[0];
			String tmpmonth = vals[1];
			String oldSeries = vals[2];
			
			if(!year.equalsIgnoreCase(tmpyear)) {
				tmpyear = year; // assign the cuurent year
				newSeries = "0001";
				if(!month.equalsIgnoreCase(tmpmonth)) {//not same month
					tmpmonth = month;
				}
				risNo = tmpyear + "-" + tmpmonth + "-" + newSeries;
			}else {//same year
				
				if(!month.equalsIgnoreCase(tmpmonth)) {//not same month
					tmpmonth = month;
				}
				
				int num = Integer.valueOf(oldSeries);
				num += 1; //increment one
				String newNum = num+"";
				int count = newNum.length();
				switch(count) {
					case 1 : risNo= tmpyear + "-" + tmpmonth + "-000"+num;  break;
					case 2 : risNo= tmpyear + "-" + tmpmonth + "-00"+num;  break;
					case 3 : risNo= tmpyear + "-" + tmpmonth + "-0"+num;  break;
					case 4 : risNo= tmpyear + "-" + tmpmonth + "-"+num;  break;
				}
			}
			
		}else {
			risNo = year + "-" + month + "-0001";
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		//System.out.println("generated new series : " + risNo);
		return risNo;
	}
	
	public static List<RequisitionIssueSlip> retrieve(String sql, String[] params){
		List<RequisitionIssueSlip> reqs = new ArrayList<RequisitionIssueSlip>();
		
		String tableColl = "cl";
		String tableOff = "off";
		String tableDiv = "dx";
		String tableRis = "rx";
		
		String sqlTemp = "SELECT * FROM ris "+ tableRis +", division "+ tableDiv +", offices "+ tableOff +", issuedcollector "+ tableColl +" WHERE " + tableRis + ".isactiver=1 AND " +
				tableRis + ".divid=" + tableDiv + ".divid AND " +
				tableRis + ".offid=" + tableOff + ".offid AND " +
				tableRis + ".isid=" + tableColl + ".isid ";
		
		sql = sqlTemp + sql;
		
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
		System.out.println("requesition SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Offices off = Offices.builder()
					.id(rs.getInt("offid"))
					.name(rs.getString("name"))
					.code(rs.getString("code"))
					.headOfOffice(rs.getString("headname"))
					.abr(rs.getString("abrname"))
					.isActive(rs.getInt("isactiveoff"))
					.build();
			
			Collector col = Collector.builder()
					.id(rs.getInt("isid"))
					.name(rs.getString("collectorname"))
					.build();
			
			Division dv = Division.builder()
					.id(rs.getInt("divid"))
					.name(rs.getString("divname"))
					.isActive(rs.getInt("isactivediv"))
					.build();
			
			RequisitionIssueSlip rq = RequisitionIssueSlip.builder()
					.id(rs.getLong("rid"))
					.requestNo(rs.getString("risno"))
					.dateTrans(rs.getString("datetrans"))
					.codeNo(rs.getString("code"))
					.saiNo(rs.getString("saino"))
					.saiDate(rs.getString("saidate"))
					.position(rs.getString("reqdesig"))
					.approvedBy(rs.getString("approvedby"))
					.approvedPosition(rs.getString("approvedpos"))
					.issuedBy(rs.getString("issuedby"))
					.issuedPosition(rs.getString("issuedpos"))
					.purspose(rs.getString("purpose"))
					.isActive(rs.getInt("isactiver"))
					.fundId(rs.getInt("fundid"))
					.division(dv)
					.collector(col)
					.office(off)
					.fundName(FundType.typeName(rs.getInt("fundid")))
					.build();
			
			
			reqs.add(rq);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return reqs;
	}
	
	public static RequisitionIssueSlip save(RequisitionIssueSlip st){
		if(st!=null){
			
			long id = RequisitionIssueSlip.getInfo(st.getId() ==0? RequisitionIssueSlip.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = RequisitionIssueSlip.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = RequisitionIssueSlip.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = RequisitionIssueSlip.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			RequisitionIssueSlip.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			RequisitionIssueSlip.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			RequisitionIssueSlip.insertData(this, "3");
		}
		
	}
	
	public static RequisitionIssueSlip insertData(RequisitionIssueSlip st, String type){
		String sql = "INSERT INTO ris ("
				+ "rid,"
				+ "risno,"
				+ "datetrans,"
				+ "code,"
				+ "saino,"
				+ "saidate,"
				+ "reqdesig,"
				+ "approvedby,"
				+ "approvedpos,"
				+ "issuedby,"
				+ "issuedpos,"
				+ "purpose,"
				+ "isactiver,"
				+ "divid,"
				+ "offid,"
				+ "isid,"
				+ "fundid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table ris");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			st.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			st.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, st.getRequestNo());
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getCodeNo());
		ps.setString(cnt++, st.getSaiNo());
		ps.setString(cnt++, st.getSaiDate());
		ps.setString(cnt++, st.getPosition());
		ps.setString(cnt++, st.getApprovedBy());
		ps.setString(cnt++, st.getApprovedPosition());
		ps.setString(cnt++, st.getIssuedBy());
		ps.setString(cnt++, st.getIssuedPosition());
		ps.setString(cnt++, st.getPurspose());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getDivision().getId());
		ps.setLong(cnt++, st.getOffice().getId());
		ps.setInt(cnt++, st.getCollector().getId());
		ps.setInt(cnt++, st.getFundId());
		
		LogU.add(st.getRequestNo());
		LogU.add(st.getDateTrans());
		LogU.add(st.getCodeNo());
		LogU.add(st.getSaiNo());
		LogU.add(st.getSaiDate());
		LogU.add(st.getPosition());
		LogU.add(st.getApprovedBy());
		LogU.add(st.getApprovedPosition());
		LogU.add(st.getIssuedBy());
		LogU.add(st.getIssuedPosition());
		LogU.add(st.getPurspose());
		LogU.add(st.getIsActive());
		LogU.add(st.getDivision().getId());
		LogU.add(st.getOffice().getId());
		LogU.add(st.getCollector().getId());
		LogU.add(st.getFundId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to ris : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static RequisitionIssueSlip updateData(RequisitionIssueSlip st){
		String sql = "UPDATE ris SET "
				+ "risno=?,"
				+ "datetrans=?,"
				+ "code=?,"
				+ "saino=?,"
				+ "saidate=?,"
				+ "reqdesig=?,"
				+ "approvedby=?,"
				+ "approvedpos=?,"
				+ "issuedby=?,"
				+ "issuedpos=?,"
				+ "purpose=?,"
				+ "divid=?,"
				+ "offid=?,"
				+ "isid=?,"
				+ "fundid=?" 
				+ " WHERE rid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table ris");
		
		ps.setString(cnt++, st.getRequestNo());
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getCodeNo());
		ps.setString(cnt++, st.getSaiNo());
		ps.setString(cnt++, st.getSaiDate());
		ps.setString(cnt++, st.getPosition());
		ps.setString(cnt++, st.getApprovedBy());
		ps.setString(cnt++, st.getApprovedPosition());
		ps.setString(cnt++, st.getIssuedBy());
		ps.setString(cnt++, st.getIssuedPosition());
		ps.setString(cnt++, st.getPurspose());
		ps.setInt(cnt++, st.getDivision().getId());
		ps.setLong(cnt++, st.getOffice().getId());
		ps.setInt(cnt++, st.getCollector().getId());
		ps.setInt(cnt++, st.getFundId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getRequestNo());
		LogU.add(st.getDateTrans());
		LogU.add(st.getCodeNo());
		LogU.add(st.getSaiNo());
		LogU.add(st.getSaiDate());
		LogU.add(st.getPosition());
		LogU.add(st.getApprovedBy());
		LogU.add(st.getApprovedPosition());
		LogU.add(st.getIssuedBy());
		LogU.add(st.getIssuedPosition());
		LogU.add(st.getPurspose());
		LogU.add(st.getDivision().getId());
		LogU.add(st.getOffice().getId());
		LogU.add(st.getCollector().getId());
		LogU.add(st.getFundId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to ris : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static int getLatestId(){
		int id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT rid FROM ris ORDER BY rid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("rid");
		}
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
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
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT rid FROM ris WHERE rid=?");
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
		String sql = "UPDATE ris set isactiver=0 WHERE rid=?";
		
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