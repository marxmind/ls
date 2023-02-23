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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @since 04/23/2019
 * @version 1.0
 *
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class PaymentName {

	private long id;
	private String dateTrans;
	private String accountingCode;
	private String name;
	private double amount;
	private int isActive;
	private String description;
	private List pyNames;
	private long pyId;
	
	private long taxGroupId;//reference to account group
	
	public static List<PaymentName> retrieve(long id){
		return retrieve(" AND accntgrpid=" + id + " ORDER BY pyname", new String[0]);
	}
	
	public static List<PaymentName> retrieve(String sqlAdd, String[] params){
		List<PaymentName> names = new ArrayList<PaymentName>();
		
		String sql = "SELECT * FROM paymentname  WHERE  isactivepy=1 " + sqlAdd;
				
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
			
			PaymentName name = new PaymentName();
			try{name.setId(rs.getLong("pyid"));}catch(NullPointerException e){}
			try{name.setDateTrans(rs.getString("pydatetrans"));}catch(NullPointerException e){}
			try{name.setAccountingCode(rs.getString("pyaccntcode"));}catch(NullPointerException e){}
			try{name.setName(rs.getString("pyname"));}catch(NullPointerException e){}
			try{name.setAmount(rs.getDouble("pyamount"));}catch(NullPointerException e){}
			try{name.setIsActive(rs.getInt("isactivepy"));}catch(NullPointerException e){}
			try{name.setDescription(rs.getString("description"));}catch(NullPointerException e){}
			try{name.setTaxGroupId(rs.getLong("accntgrpid"));}catch(NullPointerException e){}
			
			names.add(name);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return names;
	}
	
	public static PaymentName save(PaymentName is){
		if(is!=null){
			
			long id = PaymentName.getInfo(is.getId() ==0? PaymentName.getLatestId()+1 : is.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				is = PaymentName.insertData(is, "1");
			}else if(id==2){
				LogU.add("update Data ");
				is = PaymentName.updateData(is);
			}else if(id==3){
				LogU.add("added new Data ");
				is = PaymentName.insertData(is, "3");
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
	
	public static PaymentName insertData(PaymentName name, String type){
		String sql = "INSERT INTO paymentname ("
				+ "pyid,"
				+ "pydatetrans,"
				+ "pyaccntcode,"
				+ "pyname,"
				+ "pyamount,"
				+ "isactivepy,"
				+ "description,"
				+ "accntgrpid)" 
				+ "values(?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table paymentname");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			name.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			name.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, name.getDateTrans());
		ps.setString(cnt++, name.getAccountingCode());
		ps.setString(cnt++, name.getName());
		ps.setDouble(cnt++, name.getAmount());
		ps.setInt(cnt++, name.getIsActive());
		ps.setString(cnt++, name.getDescription());
		ps.setLong(cnt++, name.getTaxGroupId());
		
		LogU.add(name.getDateTrans());
		LogU.add(name.getAccountingCode());
		LogU.add(name.getName());
		LogU.add(name.getAmount());
		LogU.add(name.getIsActive());
		LogU.add(name.getDescription());
		LogU.add(name.getTaxGroupId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to paymentname : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return name;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO paymentname ("
				+ "pyid,"
				+ "pydatetrans,"
				+ "pyaccntcode,"
				+ "pyname,"
				+ "pyamount,"
				+ "isactivepy,"
				+ "description,"
				+ "accntgrpid)" 
				+ "values(?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table paymentname");
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
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getAccountingCode());
		ps.setString(cnt++, getName());
		ps.setDouble(cnt++, getAmount());
		ps.setInt(cnt++, getIsActive());
		ps.setString(cnt++, getDescription());
		ps.setLong(cnt++, getTaxGroupId());
		
		LogU.add(getDateTrans());
		LogU.add(getAccountingCode());
		LogU.add(getName());
		LogU.add(getAmount());
		LogU.add(getIsActive());
		LogU.add(getDescription());
		LogU.add(getTaxGroupId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to paymentname : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static PaymentName updateData(PaymentName name){
		String sql = "UPDATE paymentname SET "
				+ "pydatetrans=?,"
				+ "pyaccntcode=?,"
				+ "pyname=?,"
				+ "pyamount=?,"
				+ "description=?,"
				+ "accntgrpid=?" 
				+ " WHERE pyid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("update data into table paymentname");
		
		ps.setString(cnt++, name.getDateTrans());
		ps.setString(cnt++, name.getAccountingCode());
		ps.setString(cnt++, name.getName());
		ps.setDouble(cnt++, name.getAmount());
		ps.setString(cnt++, name.getDescription());
		ps.setLong(cnt++, name.getTaxGroupId());
		ps.setLong(cnt++, name.getId());
		
		LogU.add(name.getDateTrans());
		LogU.add(name.getAccountingCode());
		LogU.add(name.getName());
		LogU.add(name.getAmount());
		LogU.add(name.getDescription());
		LogU.add(name.getTaxGroupId());
		LogU.add(name.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to paymentname : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return name;
	}
	
	public void updateData(){
		String sql = "UPDATE paymentname SET "
				+ "pydatetrans=?,"
				+ "pyaccntcode=?,"
				+ "pyname=?,"
				+ "pyamount=?,"
				+ "description=?,"
				+ "accntgrpid=?" 
				+ " WHERE pyid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("update data into table paymentname");
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getAccountingCode());
		ps.setString(cnt++, getName());
		ps.setDouble(cnt++, getAmount());
		ps.setString(cnt++, getDescription());
		ps.setLong(cnt++, getTaxGroupId());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateTrans());
		LogU.add(getAccountingCode());
		LogU.add(getName());
		LogU.add(getAmount());
		LogU.add(getDescription());
		LogU.add(getTaxGroupId());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to paymentname : " + s.getMessage());
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
		sql="SELECT pyid FROM paymentname  ORDER BY pyid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("pyid");
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
		ps = conn.prepareStatement("SELECT pyid FROM paymentname WHERE pyid=?");
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
		String sql = "UPDATE paymentname set isactivepy=0 WHERE pyid=?";
		
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
