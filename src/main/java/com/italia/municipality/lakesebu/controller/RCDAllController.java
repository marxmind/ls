package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.DateUtils;
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
public class RCDAllController {
	
	private long id;
	private int index;
	private String dateTrans;
	private String controlNumber;
	private String collectionIds;
	private int isActive;
	private int fundType;
	private String individualSeries;
	private double amount;
	private String isids;
	
	private String fundName;
	/**
	 * 
	 * year-#0000
	 */
	public static String getControlNum(int fundType){
		String num=null;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT controlnumber FROM rcdallcontroller WHERE isactiveall=1 AND fundtype="+ fundType +"  ORDER BY controlnumber DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			num = rs.getString("controlnumber");
			System.out.println("controlnumber:" + num);
		}
		String currentYear = DateUtils.getCurrentYear()+"";
		if(num==null) {
			num = currentYear + "-#0001";
			System.out.println("null controlnumber:" + num);
		}else {
			String[] cont = num.split("-");
			String year = cont[0];
			String series = cont[1].split("#")[1];
			int newSeries = Integer.valueOf(series);
			newSeries +=1;
			String countSeries = newSeries+"";
			int count = countSeries.length();
			

			if(currentYear.equalsIgnoreCase(year)) {
				num = currentYear + "-#";
			}else {
				num = year + "-#";
			}
			
			switch(count) {
				case 1: num += "000" + newSeries;   break;
				case 2: num += "00" + newSeries;   break;
				case 3: num += "0" + newSeries;   break;
				case 4: num += "" + newSeries;   break;
			}
			
			
			System.out.println("new controlnumber:" + num);
		}
		
		
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return num;
	}
	
	public static List<RCDAllController> retrieve(String sql, String[] params){
		List<RCDAllController> vrs = new ArrayList<RCDAllController>();
		
		String tableController = "ct";
		String sqlTmp = "SELECT * FROM rcdallcontroller "+ tableController +" WHERE "+ 
				tableController +".isactiveall=1 ";
		
		sql = sqlTmp + sql;
		
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			
			if(params!=null && params.length>0){
				
				for(int i=0; i<params.length; i++){
					ps.setString(i+1, params[i]);
				}
				
			}
			System.out.println("SQL retrieve >> " + ps.toString());
			rs = ps.executeQuery();
			
			while(rs.next()){
				String individualSeries = "";
				try { individualSeries = rs.getString("indrptseries").replace("<*>", ",");}catch(Exception e) {}
				RCDAllController vr = RCDAllController.builder()
						.id(rs.getLong("allid"))
						.index(rs.getInt("indexid"))
						.dateTrans(rs.getString("alldatetrans"))
						.controlNumber(rs.getString("controlnumber"))
						.collectionIds(rs.getString("colids"))
						.isActive(rs.getInt("isactiveall"))
						.fundType(rs.getInt("fundtype"))
						.individualSeries(individualSeries)
						.amount(rs.getDouble("amount"))
						.fundName(FundType.typeName(rs.getInt("fundtype")))
						.isids(rs.getString("isds"))
						.build();
				
				vrs.add(vr);
			}
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {e.printStackTrace();}
		return vrs;
	}
	
	public static RCDAllController save(RCDAllController st){
		if(st!=null){
			
			long id = RCDAllController.getInfo(st.getId() ==0? RCDAllController.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = RCDAllController.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = RCDAllController.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = RCDAllController.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			RCDAllController.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			RCDAllController.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			RCDAllController.insertData(this,"3");
		}
		
	}
	
	public static RCDAllController insertData(RCDAllController st, String type){
		String sql = "INSERT INTO rcdallcontroller ("
				+ "allid,"
				+ "alldatetrans,"
				+ "controlnumber,"
				+ "colids,"
				+ "isactiveall,"
				+ "fundtype,"
				+ "indrptseries,"
				+ "amount,"
				+ "indexid,"
				+ "isds)" 
				+ "values(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table rcdallcontroller");
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
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getControlNumber());
		ps.setString(cnt++, st.getCollectionIds());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getFundType());
		ps.setString(cnt++, st.getIndividualSeries());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getIndex());
		ps.setString(cnt++, st.getIsids());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getControlNumber());
		LogU.add(st.getCollectionIds());
		LogU.add(st.getIsActive());
		LogU.add(st.getFundType());
		LogU.add(st.getIndividualSeries());
		LogU.add(st.getAmount());
		LogU.add(st.getIndex());
		LogU.add(st.getIsids());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to rcdallcontroller : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static RCDAllController updateData(RCDAllController st){
		String sql = "UPDATE rcdallcontroller SET "
				+ "alldatetrans=?,"
				+ "controlnumber=?,"
				+ "colids=?,"
				+ "fundtype=?,"
				+ "indrptseries=?,"
				+ "amount=?,"
				+ "indexid=?,"
				+ "isds=?" 
				+ " WHERE allid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table rcdallcontroller");
		
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getControlNumber());
		ps.setString(cnt++, st.getCollectionIds());
		ps.setInt(cnt++, st.getFundType());
		ps.setString(cnt++, st.getIndividualSeries());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getIndex());
		ps.setString(cnt++, st.getIsids());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getControlNumber());
		LogU.add(st.getCollectionIds());
		LogU.add(st.getFundType());
		LogU.add(st.getIndividualSeries());
		LogU.add(st.getAmount());
		LogU.add(st.getIndex());
		LogU.add(st.getIsids());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to rcdallcontroller : " + s.getMessage());
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
		sql="SELECT allid FROM rcdallcontroller  ORDER BY allid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("allid");
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
		ps = conn.prepareStatement("SELECT allid FROM rcdallcontroller WHERE allid=?");
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
		String sql = "UPDATE rcdallcontroller set isactiveall=0 WHERE allid=?";
		
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
