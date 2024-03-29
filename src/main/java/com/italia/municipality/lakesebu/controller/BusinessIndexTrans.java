package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.BusinessQtrType;
import com.italia.municipality.lakesebu.enm.BusinessType;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Regional;
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
 * @since 05/17/2023
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class BusinessIndexTrans {
	
	private long id;
	private int year;
	private String dateTrans;
	private int isActive;
	private BusinessIndex businessIndex;
	private String ornumber;
	private String remarks;
	private int qtrPayment;
	private int typeOf;
	private double capital;
	private double gross;
	private double basicTax;
	
	private String quarterName;
	private double amountPaid;
	private String typeOfName;
	
	public static List<BusinessIndexTrans> retrieve(String sql, String[] params){
		List<BusinessIndexTrans> tns = new ArrayList<BusinessIndexTrans>();
		String bnt = "bnt";
		String bn = "bn";
		String sqlTemp = "SELECT * FROM businessindextrans "+ bnt +", businessindex "+ bn +"  WHERE "+ bnt +".isactivebnt=1 AND "
				+ bnt + ".bnid=" + bn + ".bnid ";
		
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
		System.out.println("businessindextrans SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			BusinessIndex index = BusinessIndex.builder()
					.id(rs.getLong("bnid"))
					.dateTrans(rs.getString("bndate"))
					.businessName(rs.getString("bnname"))
					.owner(rs.getString("bnowner"))
					.statusId(rs.getInt("bnstatus"))
					.isActive(rs.getInt("isactivebn"))
					.purok(rs.getString("purok"))
					.barangay(Barangay.builder().id(rs.getInt("bgid")).build())
					.municipal(Municipality.builder().id(rs.getInt("munid")).build())
					.provincial(Province.builder().id(rs.getInt("provid")).build())
					.regional(Regional.builder().id(rs.getInt("regid")).build())
					.build();
			
			BusinessIndexTrans tn = BusinessIndexTrans.builder()
					.id(rs.getLong("bntid"))
					.year(rs.getInt("bnyear"))
					.dateTrans(rs.getString("bntdate"))
					.businessIndex(index)
					.ornumber(rs.getString("ornumber"))
					.remarks(rs.getString("bnremarks"))
					.qtrPayment(rs.getInt("qtrype"))
					.quarterName(BusinessQtrType.typeName(rs.getInt("qtrype")))
					.typeOf(rs.getInt("bntype"))
					.typeOfName(BusinessType.val(rs.getInt("bntype")).getName())
					.capital(rs.getDouble("capital"))
					.gross(rs.getDouble("gross"))
					.basicTax(rs.getDouble("basictax"))
					.build();
			
			tns.add(tn);
			
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return tns;
	}
	
	public static BusinessIndexTrans save(BusinessIndexTrans st){
		if(st!=null){
			
			long id = BusinessIndexTrans.getInfo(st.getId() ==0? BusinessIndexTrans.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = BusinessIndexTrans.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = BusinessIndexTrans.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = BusinessIndexTrans.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			BusinessIndexTrans.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			BusinessIndexTrans.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			BusinessIndexTrans.insertData(this, "3");
		}
		
 }
	
	public static BusinessIndexTrans insertData(BusinessIndexTrans st, String type){
		String sql = "INSERT INTO businessindextrans ("
				+ "bntid,"
				+ "bnyear,"
				+ "bntdate,"
				+ "bnid,"
				+ "ornumber,"
				+ "isactivebnt,"
				+ "bnremarks,"
				+ "qtrype,"
				+ "bntype,"
				+ "capital,"
				+ "gross,"
				+ "basictax)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table businessindextrans");
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
		
		ps.setInt(cnt++, st.getYear());
		ps.setString(cnt++, st.getDateTrans());
		ps.setLong(cnt++, st.getBusinessIndex().getId());
		ps.setString(cnt++, st.getOrnumber().trim());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getQtrPayment());
		ps.setInt(cnt++, st.getTypeOf());
		ps.setDouble(cnt++, st.getCapital());
		ps.setDouble(cnt++, st.getGross());
		ps.setDouble(cnt++, st.getBasicTax());		
		
		LogU.add(st.getYear());
		LogU.add(st.getDateTrans());
		LogU.add(st.getBusinessIndex().getId());
		LogU.add(st.getOrnumber());
		LogU.add(st.getIsActive());
		LogU.add(st.getRemarks());
		LogU.add(st.getQtrPayment());
		LogU.add(st.getTypeOf());
		LogU.add(st.getCapital());
		LogU.add(st.getGross());
		LogU.add(st.getBasicTax());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to businessindextrans : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static BusinessIndexTrans updateData(BusinessIndexTrans st){
		String sql = "UPDATE businessindextrans SET "
				+ "bnyear=?,"
				+ "bntdate=?,"
				+ "bnid=?,"
				+ "ornumber=?,"
				+ "bnremarks=?,"
				+ "qtrype=?,"
				+ "bntype=?,"
				+ "capital=?,"
				+ "gross=?,"
				+ "basictax=?" 
				+ " WHERE bntid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table businessindextrans");
		
		ps.setInt(cnt++, st.getYear());
		ps.setString(cnt++, st.getDateTrans());
		ps.setLong(cnt++, st.getBusinessIndex().getId());
		ps.setString(cnt++, st.getOrnumber().trim());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getQtrPayment());
		ps.setInt(cnt++, st.getTypeOf());
		ps.setDouble(cnt++, st.getCapital());
		ps.setDouble(cnt++, st.getGross());
		ps.setDouble(cnt++, st.getBasicTax());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getYear());
		LogU.add(st.getDateTrans());
		LogU.add(st.getBusinessIndex().getId());
		LogU.add(st.getOrnumber());
		LogU.add(st.getRemarks());
		LogU.add(st.getQtrPayment());
		LogU.add(st.getTypeOf());
		LogU.add(st.getCapital());
		LogU.add(st.getGross());
		LogU.add(st.getBasicTax());
		LogU.add(st.getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to businessindextrans : " + s.getMessage());
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
		sql="SELECT bntid FROM businessindextrans ORDER BY bntid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("bntid");
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
		ps = conn.prepareStatement("SELECT bntid FROM businessindextrans WHERE bntid=?");
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
		String sql = "UPDATE businessindextrans set isactivebnt=0 WHERE bntid=?";
		
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
