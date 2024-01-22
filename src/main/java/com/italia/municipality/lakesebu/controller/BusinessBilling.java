package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Regional;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Mark Italia
 * @version 1.0
 * @since 12/12/2023
 * 
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class BusinessBilling {

	private long id;
	private String billSeries;
	private String billDate;
	private int billType;
	private String regulatories;
	private double basicTax;
	private int isActive;
	
	private double regulatoryFee;
	private double grossEssential;
	private double grossNonEssential;
	private double grandTotal;
	private double capital;
	
	private BusinessIndex businessIndex;
	private List<String> regList;
	
	public static List<String> regulatoriesData(String bz){
		List<String> regs = new ArrayList<String>();
		
		if(bz.length()>0) {
			String[] data = bz.split("<*>");
			for(String d : data) {
				regs.add(d);
			}
		}
		
		return regs;
	}
	
	public static String regulatoriesCollection(List<PaymentName> data) {
		String regs = "";
		if(data==null || data.isEmpty()) return "";
		
		int count = 1;
		for(PaymentName s : data) {
			if("Add here".equalsIgnoreCase(s.getName())) {
				//do nothing
			}else {
				if(count==1) {
					regs = s.getId() + "-" + Currency.formatAmount(s.getAmount());
				}else {
					regs += "<*>" + s.getId() + "-" + Currency.formatAmount(s.getAmount());
				}
				count++;
			}
		}
		
		return regs;
	}
	
	public static String getLatestBillSeries() {
		String year = DateUtils.getCurrentYear()+"";
		String tmpSeries = null;
		String billSeries="BILL-"+ year + "-00001";
		String sql = "SELECT billseries FROM bzbilling WHERE isactivebill=1 ORDER BY billid DESC LIMIT 1";

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL latest series " + ps.toString());
		
		rs = ps.executeQuery();
		
			while(rs.next()){
				tmpSeries = rs.getString("billseries");
			}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e) {}
		
		if(tmpSeries!=null) {
		
			String[] data = tmpSeries.split("-");
			String tmpYear = data[1];
			String series = data[2];
			
			if(year.equalsIgnoreCase(tmpYear)) {
				billSeries = "BILL-" + tmpYear + "-";
			
				int tmpNum = Integer.valueOf(series);
				int newNum = tmpNum + 1;
				String tmpSize = newNum+"";
				int len = tmpSize.length();
				
				switch(len) {
					case 1 : billSeries+="0000" + newNum;  break;
					case 2 : billSeries+="000" + newNum;  break;
					case 3 : billSeries+="00" + newNum;  break;
					case 4 : billSeries+="0" + newNum;  break;
					case 5 : billSeries+="" + newNum;  break;
				}
				
			}else {
				billSeries = "BILL-" + year + "-00001";
			}
			
		}
		
		return billSeries;
	}
	
	public static List<BusinessBilling> retrieve(String sql, String[] params){
		List<BusinessBilling> bns = new ArrayList<BusinessBilling>();
		
		String bill = "bl";
		String bn = "bn";
		
		
		String sqlTemp = "SELECT * FROM bzbilling "+ bill +",businessindex "+ bn +" WHERE " + bill + ".bnid=" + bn +".bnid AND " + bill + ".isactivebill=1 ";
						
		
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
		System.out.println("bzbilling SQL " + ps.toString());
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
					.address("")
					.map(BusinessMapping.builder().id(rs.getLong("bzid")).build())
					.isEssential(rs.getInt("isessentail"))
					.category(rs.getInt("category"))
					.build();
			
			BusinessBilling bil = BusinessBilling.builder()
					.id(rs.getLong("billid"))
					.billSeries(rs.getString("billseries"))
					.billDate(rs.getString("billdate"))
					.billType(rs.getInt("billtype"))
					.regulatories(rs.getString("regulatories"))
					.regList(regulatoriesData(rs.getString("regulatories")))
					.basicTax(rs.getDouble("basictax"))
					.isActive(rs.getInt("isactivebill"))
					.grossEssential(rs.getDouble("grossEssential"))
					.grossNonEssential(rs.getDouble("grossNonEssential"))
					.regulatoryFee(rs.getDouble("regulatoryFee"))
					.capital(rs.getDouble("capital"))
					.grandTotal(rs.getDouble("grandTotal"))
					.businessIndex(index)
					.build();
			
			bns.add(bil);
			
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return bns;
	}
	
	public static BusinessBilling save(BusinessBilling st){
		if(st!=null){
			
			long id = BusinessBilling.getInfo(st.getId() ==0? BusinessBilling.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = BusinessBilling.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = BusinessBilling.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = BusinessBilling.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			BusinessBilling.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			BusinessBilling.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			BusinessBilling.insertData(this, "3");
		}
		
 }
	
	public static BusinessBilling insertData(BusinessBilling st, String type){
		String sql = "INSERT INTO bzbilling ("
				+ "billid,"
				+ "billseries,"
				+ "billdate,"
				+ "billtype,"
				+ "regulatories,"
				+ "basictax,"
				+ "isactivebill,"
				+ "bnid,"
				+ "regulatoryFee,"
				+ "grossEssential,"
				+ "grossNonEssential,"
				+ "grandTotal,"
				+ "capital)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table bzbilling");
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
		
		ps.setString(cnt++, getLatestBillSeries());
		ps.setString(cnt++, st.getBillDate());
		ps.setInt(cnt++, st.getBillType());
		ps.setString(cnt++, st.getRegulatories());
		ps.setDouble(cnt++, st.getBasicTax());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getBusinessIndex().getId());
		ps.setDouble(cnt++, st.getRegulatoryFee());
		ps.setDouble(cnt++, st.getGrossEssential());
		ps.setDouble(cnt++, st.getGrossNonEssential());
		ps.setDouble(cnt++, st.getGrandTotal());
		ps.setDouble(cnt++, st.getCapital());
		
		LogU.add(getLatestBillSeries());
		LogU.add(st.getBillDate());
		LogU.add(st.getBillType());
		LogU.add(st.getRegulatories());
		LogU.add(st.getBasicTax());
		LogU.add(st.getIsActive());
		LogU.add(st.getBusinessIndex().getId());
		LogU.add(st.getRegulatoryFee());
		LogU.add(st.getGrossEssential());
		LogU.add(st.getGrossNonEssential());
		LogU.add(st.getGrandTotal());
		LogU.add(st.getCapital());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to bzbilling : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static BusinessBilling updateData(BusinessBilling st){
		String sql = "UPDATE bzbilling SET "
				+ "billseries=?,"
				+ "billdate=?,"
				+ "billtype=?,"
				+ "regulatories=?,"
				+ "basictax=?,"
				+ "bnid=?,"
				+ "regulatoryFee=?,"
				+ "grossEssential=?,"
				+ "grossNonEssential=?,"
				+ "grandTotal=?,"
				+ "capital=?" 
				+ " WHERE billid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table bzbilling");
		
		ps.setString(cnt++, st.getBillSeries());
		ps.setString(cnt++, st.getBillDate());
		ps.setInt(cnt++, st.getBillType());
		ps.setString(cnt++, st.getRegulatories());
		ps.setDouble(cnt++, st.getBasicTax());
		ps.setInt(cnt++, st.getIsActive());
		ps.setDouble(cnt++, st.getRegulatoryFee());
		ps.setDouble(cnt++, st.getGrossEssential());
		ps.setDouble(cnt++, st.getGrossNonEssential());
		ps.setDouble(cnt++, st.getGrandTotal());
		ps.setDouble(cnt++, st.getCapital());
		ps.setLong(cnt++, st.getBusinessIndex().getId());
		
		LogU.add(st.getBillSeries());
		LogU.add(st.getBillDate());
		LogU.add(st.getBillType());
		LogU.add(st.getRegulatories());
		LogU.add(st.getBasicTax());
		LogU.add(st.getIsActive());
		LogU.add(st.getRegulatoryFee());
		LogU.add(st.getGrossEssential());
		LogU.add(st.getGrossNonEssential());
		LogU.add(st.getGrandTotal());
		LogU.add(st.getCapital());
		LogU.add(st.getBusinessIndex().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to bzbilling : " + s.getMessage());
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
		sql="SELECT billid FROM bzbilling ORDER BY billid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("billid");
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
		ps = conn.prepareStatement("SELECT billid FROM bzbilling WHERE billid=?");
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
		String sql = "UPDATE bzbilling set isactivebill=0 WHERE billid=?";
		
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
