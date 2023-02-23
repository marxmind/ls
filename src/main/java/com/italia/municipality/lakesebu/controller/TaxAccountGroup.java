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
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class TaxAccountGroup {

	private long id;
	private String name;
	private int isActive;
	private List<PaymentName> groups;
	
	public static Map<Long, TaxAccountGroup> retrieveMap(String sqlAdd, String[] params){
		Map<Long, TaxAccountGroup>  taxes = new LinkedHashMap<Long, TaxAccountGroup>();
		
		String tableTax ="st";
		String sql = "SELECT * FROM taxaccntgroup "+ tableTax + "  WHERE "+tableTax+".accisactive=1 ";  
		
				
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
		System.out.println("SQL Map " + ps.toString());
		rs = ps.executeQuery();
		while(rs.next()){
			
			TaxAccountGroup tax = TaxAccountGroup.builder()
					.id(rs.getLong("accid"))
					.name(rs.getString("accname"))
					.isActive(rs.getInt("accisactive"))
					.build();
			
			taxes.put(tax.getId(), tax);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return taxes;
	}
	
	public static List<TaxAccountGroup> retrieve(String sqlAdd, String[] params){
		List<TaxAccountGroup> taxes = new ArrayList<TaxAccountGroup>();
		
		String tableTax ="st";
		String sql = "SELECT * FROM taxaccntgroup "+ tableTax + "  WHERE "+tableTax+".accisactive=1 ";  
		
				
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
			
			TaxAccountGroup tax = TaxAccountGroup.builder()
					.id(rs.getLong("accid"))
					.name(rs.getString("accname"))
					.isActive(rs.getInt("accisactive"))
					.build();
			
			taxes.add(tax);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return taxes;
	}
	
	public static TaxAccountGroup addRemoveInGroup(boolean isAdd, TaxAccountGroup gp, PaymentName py) {
		List<PaymentName> pys = gp.getGroups();
		
		if(!isAdd) {//for removing in group
			pys.remove(py);
			py.setTaxGroupId(0);
			py.save();
		}
		
		if(isAdd) {
			py.setTaxGroupId(gp.getId());
			py.save();
		}
		
		pys.add(py);
		gp.setGroups(pys);
		
		return gp;
	}
	
	private static List<PaymentName> group(long id){
		List<PaymentName> names = new ArrayList<PaymentName>();
		
		names = PaymentName.retrieve(" AND accntgrpid=" + id + " ORDER BY pyname", new String[0]);
		
		return names;
	}
	
	public static TaxAccountGroup save(TaxAccountGroup st){
		if(st!=null){
			
			long id = TaxAccountGroup.getInfo(st.getId() ==0? TaxAccountGroup.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = TaxAccountGroup.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = TaxAccountGroup.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = TaxAccountGroup.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			TaxAccountGroup.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			TaxAccountGroup.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			TaxAccountGroup.insertData(this, "3");
		}
		
 }
	
	public static TaxAccountGroup insertData(TaxAccountGroup st, String type){
		String sql = "INSERT INTO taxaccntgroup ("
				+ "accid,"
				+ "accname,"
				+ "accisactive)" 
				+ " VALUES(?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table taxaccntgroup");
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
		
		ps.setString(cnt++, st.getName());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getName());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to taxaccntgroup : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static TaxAccountGroup updateData(TaxAccountGroup st){
		String sql = "UPDATE taxaccntgroup SET "
				+ "accname=?"
				+ " WHERE accid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table taxaccntgroup");
		
		ps.setString(cnt++, st.getName());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to taxaccntgroup : " + s.getMessage());
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
		sql="SELECT accid FROM taxaccntgroup  ORDER BY accid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("accid");
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
		ps = conn.prepareStatement("SELECT accid FROM taxaccntgroup WHERE accid=?");
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
		String sql = "UPDATE taxaccntgroup set accisactive=0 WHERE accid=?";
		
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