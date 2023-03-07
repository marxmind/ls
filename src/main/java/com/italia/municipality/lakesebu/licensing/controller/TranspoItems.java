package com.italia.municipality.lakesebu.licensing.controller;


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
 * @version 1.0
 * @since 2022-01-05
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class TranspoItems {
	
	private int cnt;
	private long id;
	private String name;
	private String unit;
	private double quantity;
	private double amount;
	private int isActive;
	private Transpo transpo;
	
	public static List<TranspoItems> retrieveById(long id){
		return  TranspoItems.retrieve(" AND itm.tid="+ id, new String[0]);
	}
	
	public static List<TranspoItems> retrieve(String sql, String[] params){
		List<TranspoItems> items = new ArrayList<TranspoItems>();
		
		String tableTrans = "tran";
		String tableItem = "itm";
		String sqlAdd = "SELECT * FROM  transpoitems "+tableItem+", transpo "+ tableTrans + "  WHERE  "+tableItem+".itemisactive=1 AND " +
				tableItem + ".tid=" + tableTrans + ".tid ";
		
		sql = sqlAdd + sql;
		
		
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
		System.out.println("ORLISTING SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Transpo tran = Transpo.builder()
					.id(rs.getLong("tid"))
					.dateTrans(rs.getString("datetrans"))
					.controlNo(rs.getString("controlno"))
					.orNumber(rs.getString("ornumber"))
					.requestor(rs.getString("requestor"))
					.address(rs.getString("address"))
					.fromAndTo(rs.getString("fromandto"))
					.purpose(rs.getString("purpose"))
					.deliveredDate(rs.getString("delivered"))
					.issuedDay(rs.getString("issuedday"))
					.issuedMonth(rs.getString("issuedmonth"))
					.licenseOfficer(rs.getString("licenseofficer"))
					.licenseOfficerPosition(rs.getString("licenseofficerpos"))
					.officialName(rs.getString("officialName"))
					.officialPosition(rs.getString("officialPos"))
					.menroOfficer(rs.getString("menroofficer"))
					.menroPosition(rs.getString("menropos"))
					.isActive(rs.getInt("tranisactive"))
					.build();
			
			TranspoItems item = TranspoItems.builder()
					.id(rs.getLong("iid"))
					.name(rs.getString("itemname"))
					.unit(rs.getString("itemunit"))
					.quantity(rs.getDouble("qty"))
					.amount(rs.getDouble("amount"))
					.isActive(rs.getInt("itemisactive"))
					.transpo(tran)
					.build();
			
			
			
			
			items.add(item);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return items;
	}
	
	public static TranspoItems save(TranspoItems st){
		if(st!=null){
			
			long id = TranspoItems.getInfo(st.getId() ==0? TranspoItems.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = TranspoItems.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = TranspoItems.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = TranspoItems.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			TranspoItems.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			TranspoItems.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			TranspoItems.insertData(this, "3");
		}
		
 }
	
	public static TranspoItems insertData(TranspoItems st, String type){
		String sql = "INSERT INTO transpoitems ("
				+ "iid,"
				+ "itemname,"
				+ "itemunit,"
				+ "qty,"
				+ "amount,"
				+ "itemisactive,"
				+ "tid)" 
				+ " VALUES(?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table transpoitems");
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
		ps.setString(cnt++, st.getUnit());
		ps.setDouble(cnt++, st.getQuantity());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getTranspo().getId());
		
		LogU.add(st.getName());
		LogU.add(st.getUnit());
		LogU.add(st.getQuantity());
		LogU.add(st.getAmount());
		LogU.add(st.getIsActive());
		LogU.add(st.getTranspo().getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to transpoitems : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static TranspoItems updateData(TranspoItems st){
		String sql = "UPDATE transpoitems SET "
				+ "itemname=?,"
				+ "itemunit=?,"
				+ "qty=?,"
				+ "amount=?,"
				+ "tid=?" 
				+ " WHERE iid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table transpoitems");
		
		
		ps.setString(cnt++, st.getName());
		ps.setString(cnt++, st.getUnit());
		ps.setDouble(cnt++, st.getQuantity());
		ps.setDouble(cnt++, st.getAmount());
		ps.setLong(cnt++, st.getTranspo().getId());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getName());
		LogU.add(st.getUnit());
		LogU.add(st.getQuantity());
		LogU.add(st.getAmount());
		LogU.add(st.getTranspo().getId());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to transpoitems : " + s.getMessage());
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
		sql="SELECT iid FROM transpoitems  ORDER BY iid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("iid");
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
		ps = conn.prepareStatement("SELECT iid FROM transpoitems WHERE iid=?");
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
	
	public static void delete(long id) {
		delete("DELETE FROM transpoitems WHERE tid="+ id, new String[0]);
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
		String sql = "UPDATE transpoitems set itemisactive=0 WHERE iid=?";
		
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