package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.LogU;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @since 09/21/2021
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class TaxCodeGroup {

	private long id;
	private String name;
	private String ids;
	private int isActive;
	
	private List<PaymentName> groups;
	
	
	public static List<TaxCodeGroup> retrieve(String sqlAdd, String[] params){
		List<TaxCodeGroup> taxes = new ArrayList<TaxCodeGroup>();
		
		String tableTax ="st";
		String sql = "SELECT * FROM taxcodegroup "+ tableTax + "  WHERE "+tableTax+".groupisactive=1 ";  
		
				
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
		int count = 1;
		while(rs.next()){
			
			TaxCodeGroup tax = TaxCodeGroup.builder()
					.id(rs.getLong("taxid"))
					.name(rs.getString("groupname"))
					.ids(rs.getString("groupids"))
					.isActive(rs.getInt("groupisactive"))
					.groups(group(rs.getString("groupids")))
					.build();
			
			taxes.add(tax);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return taxes;
	}
	
	public static TaxCodeGroup addRemoveInGroup(boolean isAdd, TaxCodeGroup gp, PaymentName py) {
		List<PaymentName> pys = gp.getGroups();
		
		if(!isAdd) {//for removing in group
			pys.remove(py);
		}
		
		String ids="";
		int i = 1;
		if(pys!=null && pys.size()>0) {
			for(PaymentName p : pys) {
				if(i==1) {
					ids = p.getId()+"";
				}else {
					ids +="<@>" + p.getId();
				}
				i++;
			}
		}
		
		if(isAdd) {
			ids += "<@>" + py.getId();
		}
		gp.setIds(ids);
		pys.add(py);
		gp.setGroups(pys);
		System.out.println("saving ids=" + ids);
		return gp;
	}
	
	private static List<PaymentName> group(String ids){
		List<PaymentName> names = new ArrayList<PaymentName>();
		
		String[] id =  new String[0];
		
		if(ids!=null && ids.length()>1 && ids.contains("<@>")) {
			id = ids.split("<@>");
			String val = " AND (";
			
			int count = 1;
			for(String i : id) {
				if(count==1) {
					val += " pyid=" + i;
				}else {
					val += " OR pyid=" + i;
				}
				count++;
			}
			val += ")";
			
			names = PaymentName.retrieve(val, new String[0]);
			
		}else if(ids!=null && ids.length()==1){
			names = PaymentName.retrieve(" AND pyid=" + ids, new String[0]);
		}
		
		
		return names;
	}
	
	public static TaxCodeGroup save(TaxCodeGroup st){
		if(st!=null){
			
			long id = TaxCodeGroup.getInfo(st.getId() ==0? TaxCodeGroup.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = TaxCodeGroup.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = TaxCodeGroup.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = TaxCodeGroup.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			TaxCodeGroup.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			TaxCodeGroup.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			TaxCodeGroup.insertData(this, "3");
		}
		
 }
	
	public static TaxCodeGroup insertData(TaxCodeGroup st, String type){
		String sql = "INSERT INTO taxcodegroup ("
				+ "taxid,"
				+ "groupname,"
				+ "groupids,"
				+ "groupisactive)" 
				+ " VALUES(?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table taxcodegroup");
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
		ps.setString(cnt++, st.getIds());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getName());
		LogU.add(st.getIds());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to taxcodegroup : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static TaxCodeGroup updateData(TaxCodeGroup st){
		String sql = "UPDATE taxcodegroup SET "
				+ "groupname=?,"
				+ "groupids=?"
				+ " WHERE taxid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table taxcodegroup");
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getIds());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getIds());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to taxcodegroup : " + s.getMessage());
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
		sql="SELECT taxid FROM taxcodegroup  ORDER BY taxid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("taxid");
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
		ps = conn.prepareStatement("SELECT taxid FROM taxcodegroup WHERE taxid=?");
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
		String sql = "UPDATE taxcodegroup set groupisactive=0 WHERE taxid=?";
		
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