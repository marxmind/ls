package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.utils.DateUtils;
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
 * @since 10/14/2021
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class FishcageBillingStatment {

	private long id;
	private String dateTrans;
	private String controlNo;
	private String particulars;
	private String remarks;
	private int isActive;
	private int isCompleted;
	
	private FishCage owner;
	private UserDtls userDtls;
	
	private Date date;
	private long ownerListId;
	private List owners;
	
	public List<PaymentName> listparticulars;
	
	public static List<String> searchControlNumber(String name){
		List<String> str = new ArrayList<String>();
		String sql = "SELECT fbcontrolno FROM fishcagebillingstatement WHERE fbisactive=1 AND fbcontrolno like '%"+ name.replace("--", "") +"%'";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			str.add(rs.getString("fbcontrolno"));
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return str;
	}
	
	public static List<PaymentName> getPaynames(String particulars){
		List<PaymentName> parts = new ArrayList<PaymentName>();
		
		if (particulars==null || particulars.isEmpty())
			return parts;
		
		
		double total = 0d;
		if(particulars.contains("<@>")) {
			Map<Long, Double> mapName = new LinkedHashMap<Long, Double>();
			String[] pts = particulars.split("<@>");
			
			String sql = "";
			int cnt = 1;
			sql = " AND (";
			for(String p : pts) {
				
				System.out.println("ppts>>> " + p);
				
				String[] ids = p.split(":");
				long id = Long.valueOf(ids[0]);
				
				if(cnt==1) {
					sql += " pyid=" + id;
				}else {
					sql += " OR pyid=" + id;
				}
				
				
				
				mapName.put(id, Double.valueOf(ids[1]));
				
				cnt++;
			}
			sql += " )";
			
			System.out.println("SQL Payname>>>> " + sql);
			
			for(PaymentName p : PaymentName.retrieve(sql, new String[0])) {
				double amount = mapName.get(p.getId());
				p.setAmount(amount);
				parts.add(p);
				total += amount;
			}
			
			
		}else {
			String[] pa = particulars.split(":");
			double amount = Double.valueOf(pa[1]);
			parts.add(
					PaymentName.builder()
					.id(Long.valueOf(pa[0]))
					.name(PaymentName.retrieve(Long.valueOf(pa[0])).get(0).getName())
					.amount(amount)
					.build()
					);
			total = amount;
		}
		
		parts.add(
				PaymentName.builder()
				.id(0)
				.name("Total")
				.amount(total)
				.build()
				);
		
		return parts;
	}
	
	public static String getControlNewNo() {
		
		String sql = "SELECT fbcontrolno FROM fishcagebillingstatement WHERE fbisactive=1 ORDER BY fbid DESC limit 1";
		String newControl = null;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		while(rs.next()){
			newControl = rs.getString("fbcontrolno");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		if(newControl==null) {
			newControl = "FS-" + DateUtils.getCurrentYear() + "-0001";
		}else {
			
			//check year
			String[] vals = newControl.split("-");
			String suffix = vals[0];
			String year = vals[1];
			
			int num = Integer.valueOf(vals[2]);
			
			if(!year.equalsIgnoreCase(DateUtils.getCurrentYear()+"")) {
				num = 0;//reset to zero
			}
			
			num += 1;
			String numLen = num+"";
			int len = numLen.length();
			switch(len) {
				case 1: newControl = suffix + "-" + year + "-000" + num; break;
				case 2: newControl = suffix + "-" + year + "-00" + num; break;
				case 3: newControl = suffix + "-" + year + "-0" + num; break;
				case 4: newControl = suffix + "-" + year + "-" + num; break;
			}
			
			
			
		}
		
		
		}catch(Exception e){e.getMessage();}
		
		return newControl;
	}
	
	
	public static List<FishcageBillingStatment> retrieve(String sqlAdd, String[] params){
		List<FishcageBillingStatment> sts = new ArrayList<FishcageBillingStatment>();
		
		String tableSt = "st";
		String tableOw = "ow";
		String tableUser = "su";
		String sql = "SELECT * FROM fishcagebillingstatement "+ tableSt + ", cageowner "+ tableOw +" , userdtls "+ tableUser +"  WHERE "+tableSt+".fbisactive=1 AND " +
				tableSt + ".cid=" + tableOw + ".cid AND " +
				tableSt + ".userid=" + tableUser + ".userdtlsid ";
		
				
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
			
			FishCage cg = FishCage.builder()
					.id(rs.getLong("cid"))
					.dateRegister(rs.getString("datereg"))
					.waterSurveyNo(rs.getString("watersurveyno"))
					.ownerName(rs.getString("owner"))
					.tenantOwner(rs.getString("tenantowner"))
					.cageArea(rs.getString("cagearea"))
					.totalSquareArea(rs.getString("totalsquarearea"))
					.location(rs.getString("arealocation"))
					.remarks(rs.getString("ownerremarks"))
					.amountDue(rs.getDouble("amountdue"))
					.isActive(rs.getInt("isactiveowner"))
					.yearApplied(rs.getInt("yearapplied"))
					.motorizedBoat(rs.getInt("motorizedboat"))
					.nonMotorizedBoat(rs.getInt("nonmotorizedboat"))
					.cellphoneNo(rs.getString("cellphoneno"))
					.numberOfFishCages(rs.getInt("noofcages"))
					.noOfFunctional(rs.getInt("functional"))
					.noOfNonFunctional(rs.getInt("nonfunctional"))
					.noOfAnnualProduction(rs.getInt("annprod"))
					.noOfTotalStock(rs.getInt("totalstock"))
					.sizeCagePerModule(rs.getString("sizecagemodule"))
					.build();
			
			UserDtls user = UserDtls.builder()
					.userdtlsid(rs.getLong("userdtlsid"))
					.firstname(rs.getString("firstname"))
					.middlename(rs.getString("middlename"))
					.lastname(rs.getString("lastname"))
					.build();
			
			FishcageBillingStatment st = FishcageBillingStatment.builder()
					.id(rs.getLong("fbid"))
					.dateTrans(rs.getString("fbdatebill"))
					.controlNo(rs.getString("fbcontrolno"))
					.particulars(rs.getString("fbparticulars"))
					.remarks(rs.getString("fbremarks"))
					.isActive(rs.getInt("fbisactive"))
					.owner(cg)
					.userDtls(user)
					.isCompleted(rs.getInt("iscompleted"))
					.build();
			
			sts.add(st);
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return sts;
	}
	
	public static FishcageBillingStatment save(FishcageBillingStatment st){
		if(st!=null){
			
			long id = FishcageBillingStatment.getInfo(st.getId() ==0? FishcageBillingStatment.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = FishcageBillingStatment.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = FishcageBillingStatment.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = FishcageBillingStatment.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){	
		FishcageBillingStatment.save(this);
	}
	
	public static FishcageBillingStatment insertData(FishcageBillingStatment st, String type){
		String sql = "INSERT INTO fishcagebillingstatement ("
				+ "fbid,"
				+ "fbdatebill,"
				+ "fbcontrolno,"
				+ "fbparticulars,"
				+ "fbremarks,"
				+ "fbisactive,"
				+ "cid,"
				+ "userid,"
				+ "iscompleted)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table fishcagebillingstatement");
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
		ps.setString(cnt++, st.getControlNo());
		ps.setString(cnt++, st.getParticulars());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getOwner().getId());
		ps.setLong(cnt++, st.getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, st.getIsCompleted());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getControlNo());
		LogU.add(st.getParticulars());
		LogU.add(st.getRemarks());
		LogU.add(st.getIsActive());
		LogU.add(st.getOwner().getId());
		LogU.add(st.getUserDtls().getUserdtlsid());
		LogU.add(st.getIsCompleted());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to fishcagebillingstatement : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static FishcageBillingStatment updateData(FishcageBillingStatment st){
		String sql = "UPDATE fishcagebillingstatement SET "
				+ "fbdatebill=?,"
				+ "fbcontrolno=?,"
				+ "fbparticulars=?,"
				+ "fbremarks=?,"
				+ "cid=?,"
				+ "userid=?,"
				+ "iscompleted=?" 
				+ " WHERE fbid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table fishcagebillingstatement");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getControlNo());
		ps.setString(cnt++, st.getParticulars());
		ps.setString(cnt++, st.getRemarks());
		ps.setLong(cnt++, st.getOwner().getId());
		ps.setLong(cnt++, st.getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, st.getIsCompleted());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getControlNo());
		LogU.add(st.getParticulars());
		LogU.add(st.getRemarks());
		LogU.add(st.getOwner().getId());
		LogU.add(st.getUserDtls().getUserdtlsid());
		LogU.add(st.getIsCompleted());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to fishcagebillingstatement : " + s.getMessage());
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
		sql="SELECT fbid FROM fishcagebillingstatement  ORDER BY fbid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("fbid");
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
		ps = conn.prepareStatement("SELECT fbid FROM fishcagebillingstatement WHERE fbid=?");
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
		String sql = "UPDATE fishcagebillingstatement set fbisactive=0 WHERE fbid=?";
		
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
