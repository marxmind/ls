package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
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
 * @since 04/23/2019
 * @version 1.0
 *
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ORNameList {

	private long id;
	private double amount;
	private int isActive;
	
	private ORListing orList;
	private Customer customer;
	private PaymentName paymentName;
	
	public static Map<Integer, Double> retrieveYear(int year){
		Map<Integer, Double> mapData = new LinkedHashMap<Integer, Double>();
			
		//select DATE_FORMAT(timestampol,'%m') as month,sum(olamount) as amount from ornamelist  where isactiveol=1 and (timestampol>='2021-01-01 00:00:00' and timestampol<='2021-12-31 23:59:59')  group by month(timestampol) DESC;
		String[] params = new String[2];
		params[0] = year + "-01-01 00:00:00";
		params[1] = year + "-12-31 23:59:59";
		String sql = "SELECT DATE_FORMAT(timestampol,'%m') as month, sum(olamount) as amount FROM ornamelist WHERE isactiveol=1 and (timestampol>=? AND timestampol<=?) GROUP BY MONTH(timestampol)";
		
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
	
	public static Map<Integer, Map<Integer, Double>> graph(List years) {
		
		Map<Integer, Map<Integer, Double>> mapData = new LinkedHashMap<Integer, Map<Integer, Double>>();
		
		for(Object year : years) {
			int yer = Integer.valueOf(year.toString());
			mapData.put(yer, ORNameList.retrieveYear(yer));
		}
		
		return mapData;
		
	}
	
	public static Map<Integer, Map<Integer, Map<Integer, Double>>> graph(List years, List collectors) {
		Map<Integer, Map<Integer, Map<Integer, Double>>> mapYear = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		
		for(Object collector : collectors) {
			int col = Integer.valueOf(collector.toString());
			for(Object year : years) {
				int yer = Integer.valueOf(year.toString());
				mapYear = ORNameList.retrieveYear(yer,col,mapYear);
			}
		}
		
		
		return mapYear;
		
	}
	
	public static List<ORNameList> retrieveGroupByPayName(String sqlAdd, String[] params) {
		
		List<ORNameList> orns = new ArrayList<ORNameList>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		String tableNameList = "nameL";
		String tableOr = "orl";
		String tableName = "pay";
		
		String sql = "SELECT nameL.olid, sum(nameL.olamount) as amount,orl.orid,orl.ordatetrans,orl.aform, orl.ornumber,pay.pyid,pay.pyname,pay.accntgrpid  FROM ornamelist " + tableNameList + ", orlisting " + tableOr + ", paymentname " + tableName + " WHERE " + tableOr + ".isactiveor=1 AND " +
				tableNameList + ".orid=" + tableOr +".orid AND " +
				tableNameList + ".pyid=" + tableName + ".pyid ";
		
		sql += sqlAdd + " Group By " + tableName + ".pyname";
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
			ORNameList orn = new ORNameList();
			try{orn.setId(rs.getLong("olid"));}catch(NullPointerException e){}
			
			ORListing or = new ORListing();
			try{or.setId(rs.getLong("orid"));}catch(NullPointerException e){}
			try{or.setDateTrans(rs.getString("ordatetrans"));}catch(NullPointerException e){}
			try{or.setOrNumber(rs.getString("ornumber"));}catch(NullPointerException e){}
			try{or.setFormType(rs.getInt("aform"));}catch(NullPointerException e){}
			orn.setOrList(or);
			
			PaymentName name = new PaymentName();
			try{name.setId(rs.getLong("pyid"));}catch(NullPointerException e){}
			try{name.setName(rs.getString("pyname"));}catch(NullPointerException e){}
			try{name.setTaxGroupId(rs.getLong("accntgrpid"));}catch(NullPointerException e){}
			try{name.setAmount(rs.getDouble("amount"));}catch(NullPointerException e){}
			orn.setPaymentName(name);
			
			orns.add(orn);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return orns;
	}
	
	public static Map<Integer, Map<Integer, Map<Integer, Double>>> retrieveYear(int year, int collector,Map<Integer, Map<Integer, Map<Integer, Double>>> mapYear){
		//Map<Integer, Map<Integer, Map<Integer, Double>>> mapYear = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<Integer, Double>> mapCollector = new LinkedHashMap<Integer, Map<Integer, Double>>();
		 Map<Integer, Double> mapMonth = new LinkedHashMap<Integer, Double>();
		String[] params = new String[3];
		params[0] = year + "-01-01";
		params[1] = year + "-12-31";
		params[2] = collector+"";
		String sql = "select DATE_FORMAT(nm.timestampol,'%Y') as year,DATE_FORMAT(nm.timestampol,'%m') as month,os.isid as collector, sum(nm.olamount) as amount from ornamelist nm, orlisting os where os.isactiveor=1 and  os.orid=nm.orid and (os.ordatetrans>=? and os.ordatetrans<=? ) and os.isid=?  GROUP BY MONTH(nm.timestampol);";
		
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
	
	public static Object[] retrieveORNames(long orId) {
		Object[] obj = new Object[2];
		List<ORNameList> orns = new ArrayList<ORNameList>();
		double amount = 0d;
		String tableNameList = "nameL";
		String tableName = "pay";
		String sql = "SELECT * FROM ornamelist "+ tableNameList +" ,paymentname "+ tableName +"  WHERE  "+tableNameList+".isactiveol=1 AND " + 
				tableNameList +".pyid=" + tableName + ".pyid AND " + 
				tableNameList + ".orid=" + orId; 
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			ORNameList orn = new ORNameList();
			try{orn.setId(rs.getLong("olid"));}catch(NullPointerException e){}
			try{orn.setAmount(rs.getDouble("olamount"));}catch(NullPointerException e){}
			try{orn.setIsActive(rs.getInt("isactiveol"));}catch(NullPointerException e){}
			
			PaymentName name = new PaymentName();
			try{name.setId(rs.getLong("pyid"));}catch(NullPointerException e){}
			try{name.setDateTrans(rs.getString("pydatetrans"));}catch(NullPointerException e){}
			try{name.setAccountingCode(rs.getString("pyaccntcode"));}catch(NullPointerException e){}
			try{name.setName(rs.getString("pyname"));}catch(NullPointerException e){}
			try{name.setAmount(rs.getDouble("pyamount"));}catch(NullPointerException e){}
			try{name.setIsActive(rs.getInt("isactivepy"));}catch(NullPointerException e){}
			orn.setPaymentName(name);
			
			orns.add(orn);
			
			amount += orn.getAmount();
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		obj[0] = amount;
		obj[1] = orns;
		
		return obj;
	}
	
	
	public static List<ORNameList> retrieve(String sqlAdd, String[] params){
		List<ORNameList> orns = new ArrayList<ORNameList>();
		
		String tableNameList = "nameL";
		String tableOr = "orl";
		String tableCus = "cuz";
		String tableName = "pay";
		String sql = "SELECT * FROM ornamelist "+ tableNameList +", orlisting "+tableOr+", customer "+ tableCus +",paymentname "+ tableName +"  WHERE  "+tableOr+".isactiveor=1 AND " + 
				tableNameList +".customerid=" + tableCus + ".customerid AND " + 
				tableNameList +".orid=" + tableOr + ".orid AND " +
				tableNameList +".pyid=" + tableName + ".pyid"; 
		
		sql += sqlAdd;
		
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
			
			ORNameList orn = new ORNameList();
			try{orn.setId(rs.getLong("olid"));}catch(NullPointerException e){}
			try{orn.setAmount(rs.getDouble("olamount"));}catch(NullPointerException e){}
			try{orn.setIsActive(rs.getInt("isactiveol"));}catch(NullPointerException e){}
			
			ORListing or = new ORListing();
			try{or.setId(rs.getLong("orid"));}catch(NullPointerException e){}
			try{or.setDateTrans(rs.getString("ordatetrans"));}catch(NullPointerException e){}
			try{or.setOrNumber(rs.getString("ornumber"));}catch(NullPointerException e){}
			try{or.setFormType(rs.getInt("aform"));}catch(NullPointerException e){}
			try{or.setIsActive(rs.getInt("isactiveor"));}catch(NullPointerException e){}
			orn.setOrList(or);
			
			Customer cus = new Customer();
			try{cus.setId(rs.getLong("customerid"));}catch(NullPointerException e){}
			try{cus.setFirstname(rs.getString("cusfirstname"));}catch(NullPointerException e){}
			try{cus.setMiddlename(rs.getString("cusmiddlename"));}catch(NullPointerException e){}
			try{cus.setLastname(rs.getString("cuslastname"));}catch(NullPointerException e){}
			try{cus.setFullname(rs.getString("fullname"));}catch(NullPointerException e){}
			try{cus.setGender(rs.getString("cusgender"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			orn.setCustomer(cus);
			
			PaymentName name = new PaymentName();
			try{name.setId(rs.getLong("pyid"));}catch(NullPointerException e){}
			try{name.setDateTrans(rs.getString("pydatetrans"));}catch(NullPointerException e){}
			try{name.setAccountingCode(rs.getString("pyaccntcode"));}catch(NullPointerException e){}
			try{name.setName(rs.getString("pyname"));}catch(NullPointerException e){}
			try{name.setAmount(rs.getDouble("pyamount"));}catch(NullPointerException e){}
			try{name.setIsActive(rs.getInt("isactivepy"));}catch(NullPointerException e){}
			orn.setPaymentName(name);
			
			orns.add(orn);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return orns;
	}
	
	public static ORNameList save(ORNameList is){
		if(is!=null){
			
			long id = ORNameList.getInfo(is.getId() ==0? ORNameList.getLatestId()+1 : is.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				is = ORNameList.insertData(is, "1");
			}else if(id==2){
				LogU.add("update Data ");
				is = ORNameList.updateData(is);
			}else if(id==3){
				LogU.add("added new Data ");
				is = ORNameList.insertData(is, "3");
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
	
	public static ORNameList insertData(ORNameList name, String type){
		String sql = "INSERT INTO ornamelist ("
				+ "olid,"
				+ "orid,"
				+ "pyid,"
				+ "customerid,"
				+ "olamount,"
				+ "isactiveol)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table ornamelist");
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
		
		ps.setLong(cnt++, name.getOrList().getId());
		ps.setLong(cnt++, name.getPaymentName().getId());
		ps.setLong(cnt++, name.getCustomer().getId());
		ps.setDouble(cnt++, name.getAmount());
		ps.setInt(cnt++, name.getIsActive());
		
		LogU.add(name.getOrList().getId());
		LogU.add(name.getPaymentName().getId());
		LogU.add(name.getCustomer().getId());
		LogU.add(name.getAmount());
		LogU.add(name.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to ornamelist : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return name;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO ornamelist ("
				+ "olid,"
				+ "orid,"
				+ "pyid,"
				+ "customerid,"
				+ "olamount,"
				+ "isactiveol)" 
				+ "values(?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table ornamelist");
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
		
		ps.setLong(cnt++, getOrList().getId());
		ps.setLong(cnt++, getPaymentName().getId());
		ps.setLong(cnt++, getCustomer().getId());
		ps.setDouble(cnt++, getAmount());
		ps.setInt(cnt++, getIsActive());
		
		LogU.add(getOrList().getId());
		LogU.add(getPaymentName().getId());
		LogU.add(getCustomer().getId());
		LogU.add(getAmount());
		LogU.add(getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to ornamelist : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
	}
	
	public static ORNameList updateData(ORNameList name){
		String sql = "UPDATE ornamelist SET "
				+ "orid=?,"
				+ "pyid=?,"
				+ "customerid=?,"
				+ "olamount=?" 
				+ " WHERE olid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table ornamelist");
		
		ps.setLong(cnt++, name.getOrList().getId());
		ps.setLong(cnt++, name.getPaymentName().getId());
		ps.setLong(cnt++, name.getCustomer().getId());
		ps.setDouble(cnt++, name.getAmount());
		ps.setLong(cnt++, name.getId());
		
		LogU.add(name.getOrList().getId());
		LogU.add(name.getPaymentName().getId());
		LogU.add(name.getCustomer().getId());
		LogU.add(name.getAmount());
		LogU.add(name.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to ornamelist : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return name;
	}
	
	public void updateData(){
		String sql = "UPDATE ornamelist SET "
				+ "orid=?,"
				+ "pyid=?,"
				+ "customerid=?,"
				+ "olamount=?" 
				+ " WHERE olid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table ornamelist");
		
		ps.setLong(cnt++, getOrList().getId());
		ps.setLong(cnt++, getPaymentName().getId());
		ps.setLong(cnt++, getCustomer().getId());
		ps.setDouble(cnt++, getAmount());
		ps.setLong(cnt++, getId());
		
		LogU.add(getOrList().getId());
		LogU.add(getPaymentName().getId());
		LogU.add(getCustomer().getId());
		LogU.add(getAmount());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to ornamelist : " + s.getMessage());
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
		sql="SELECT olid FROM ornamelist  ORDER BY olid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("olid");
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
		ps = conn.prepareStatement("SELECT olid FROM ornamelist WHERE olid=?");
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
		String sql = "UPDATE ornamelist set isactiveol=0 WHERE olid=?";
		
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