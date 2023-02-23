package com.italia.municipality.lakesebu.controller;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Requisition {

	private long id;
	private String stockNo;
	private String unit;
	private String description;
	private int formType;
	private int quantity;
	private double totalCost;
	private String remarks;
	private int isActive;
	private Stocks stocks;
	private RequisitionIssueSlip issueSlip;
	
	//temp data not included in database
	private boolean disableRow;
	private IssuedForm issuedFormData; 
	private CollectionInfo collectionInfoData;
	private List formTypes;
	
	public static List<Requisition> retrieve(String sql, String[] params){
		List<Requisition> reqs = new ArrayList<Requisition>();
		
		String tableStock = "st";
		String tableRis = "rx";
		String tableReq = "rq";
		
		String sqlTemp = "SELECT * FROM requesition "+ tableReq +", ris "+ tableRis +", stockreceipt "+ tableStock +" WHERE " + tableReq + ".isactiverq=1 AND " +
		   tableReq + ".rid=" + tableRis + ".rid AND " +
		   tableReq + ".stockid=" + tableStock + ".stockid ";
		
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
			
			RequisitionIssueSlip req = RequisitionIssueSlip.builder()
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
					.division(Division.builder().id(rs.getInt("divid")).build())
					.collector(Collector.builder().id(rs.getInt("isid")).build())
					.office(Offices.builder().id(rs.getInt("offid")).build())
					.fundName(FundType.typeName(rs.getInt("fundid")))
					.build();
			
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
			
			if(rs.getInt("statusstock")==1) {
				st.setStatusName("NOT ISSUED");
			}else {
				st.setStatusName("ISSUED");
			}
			st.setFormTypeName(FormType.val(rs.getInt("formType")).getDescription());
			
			Requisition rq = Requisition.builder()
					.id(rs.getLong("rqid"))
					.stockNo(rs.getString("stockno"))
					.unit(rs.getString("unit"))
					.description(rs.getString("description"))
					.formType(rs.getInt("formtype"))
					.quantity(rs.getInt("qty"))
					.totalCost(rs.getDouble("totalcost"))
					.remarks(rs.getString("remarks"))
					.isActive(rs.getInt("isactiverq"))
					.stocks(st)
					.issueSlip(req)
					.build();
			
			
			
			
			reqs.add(rq);
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return reqs;
	}
	
	public static Requisition save(Requisition st){
		if(st!=null){
			
			long id = Requisition.getInfo(st.getId() ==0? Requisition.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Requisition.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Requisition.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Requisition.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Requisition.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Requisition.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Requisition.insertData(this, "3");
		}
		
	}
	
	public static Requisition insertData(Requisition st, String type){
		String sql = "INSERT INTO requesition ("
				+ "rqid,"
				+ "stockno,"
				+ "unit,"
				+ "description,"
				+ "formtype,"
				+ "qty,"
				+ "totalcost,"
				+ "remarks,"
				+ "rid,"
				+ "isactiverq,"
				+ "stockid)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table requesition");
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
		
		ps.setString(cnt++, st.getStockNo());
		ps.setString(cnt++, st.getUnit());
		ps.setString(cnt++, st.getDescription());
		ps.setInt(cnt++, st.getFormType());
		ps.setInt(cnt++, st.getQuantity());
		ps.setDouble(cnt++, st.getTotalCost());
		ps.setString(cnt++, st.getRemarks());
		ps.setLong(cnt++, st.getIssueSlip().getId());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getStocks().getId());
		
		LogU.add(st.getStockNo());
		LogU.add(st.getUnit());
		LogU.add(st.getDescription());
		LogU.add(st.getFormType());
		LogU.add(st.getQuantity());
		LogU.add(st.getTotalCost());
		LogU.add(st.getRemarks());
		LogU.add(st.getIssueSlip().getId());
		LogU.add(st.getIsActive());
		LogU.add(st.getStocks().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to requesition : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static Requisition updateData(Requisition st){
		String sql = "UPDATE requesition SET "
				+ "stockno=?,"
				+ "unit=?,"
				+ "description=?,"
				+ "formtype=?,"
				+ "qty=?,"
				+ "totalcost=?,"
				+ "remarks=?,"
				+ "rid=?,"
				+ "stockid=?"
				+ " WHERE rqid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table requesition");
		
		
		ps.setString(cnt++, st.getStockNo());
		ps.setString(cnt++, st.getUnit());
		ps.setString(cnt++, st.getDescription());
		ps.setInt(cnt++, st.getFormType());
		ps.setInt(cnt++, st.getQuantity());
		ps.setDouble(cnt++, st.getTotalCost());
		ps.setString(cnt++, st.getRemarks());
		ps.setLong(cnt++, st.getIssueSlip().getId());
		ps.setLong(cnt++, st.getStocks().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getStockNo());
		LogU.add(st.getUnit());
		LogU.add(st.getDescription());
		LogU.add(st.getFormType());
		LogU.add(st.getQuantity());
		LogU.add(st.getTotalCost());
		LogU.add(st.getRemarks());
		LogU.add(st.getIssueSlip().getId());
		LogU.add(st.getStocks().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to requesition : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT rqid FROM requesition ORDER BY rqid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("rqid");
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
		ps = conn.prepareStatement("SELECT rqid FROM requesition WHERE rqid=?");
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
		System.out.println("Delete SQL : " + ps.toString());
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public  void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE requesition set isactiverq=0 WHERE rqid=?";
		
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