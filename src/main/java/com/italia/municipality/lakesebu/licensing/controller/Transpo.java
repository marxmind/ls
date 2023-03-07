package com.italia.municipality.lakesebu.licensing.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
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
 * @version 1.0
 * @since 2022-01-05
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Transpo {

	private long id;
	private String dateTrans;
	private String orNumber;
	private String controlNo;
	private String requestor;
	private String address;
	private String fromAndTo;
	private String purpose;
	private String deliveredDate;
	private String issuedDay;
	private String issuedMonth;
	private String licenseOfficer;
	private String licenseOfficerPosition;
	private String officialName;
	private String officialPosition;
	private String menroOfficer;
	private String menroPosition;
	private int isActive;
	private UserDtls userDtls;
	
	private String preparedBy;
	private String preparedByPosition;
	
	private List<TranspoItems> items;
	
	public static String getSeries() {
		String year = DateUtils.getCurrentYear()+"";
		String month = DateUtils.getCurrentMonth()<10? "0" + DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+"";
		String series = year + "-" + month + "-" + "001";
		String val=null;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT controlno FROM transpo WHERE tranisactive=1 ORDER BY tid DESC limit 1");
		rs = ps.executeQuery();
		
		while(rs.next()){
			val = rs.getString("controlno");
		}
		
		
		if(val!=null && !val.isEmpty()) {
			
			String[] spl = val.split("-");
			int newSeries = Integer.valueOf(spl[2]);
			newSeries += 1; //increase by 1
			String tmp = newSeries+"";
			int size = tmp.length();
			//System.out.println("size of series: " + size + "  new series: " + newSeries);
			if(year.equalsIgnoreCase(spl[0])) { //same year
				
				switch(size) {
				case 1 : series = year + "-" + month + "-00"+ newSeries;  break;
				case 2 : series = year + "-" + month + "-0"+ newSeries;  break;
				case 3 : series = year + "-" + month + "-"+ newSeries;  break;
				}
				
				
			}
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return series;
	}
	
	
	public static List<Transpo> retrieve(String sql, String[] params){
		List<Transpo> trans = new ArrayList<Transpo>();
		
		String tableClr = "clr";
		String tableUsr = "usr";
		String sqlAdd = "SELECT * FROM transpo "+tableClr+", userdtls "+ tableUsr + "  WHERE  "+tableClr+".tranisactive=1 AND " +
				tableClr + ".userid=" + tableUsr + ".userdtlsid ";
		
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
					.preparedBy(rs.getString("prepby"))
					.preparedByPosition(rs.getString("preppos"))
					.build();
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setRegdate(rs.getString("regdate"));;}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			
			tran.setUserDtls(user);
			
			trans.add(tran);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return trans;
	}
	
	public static Transpo save(Transpo st){
		if(st!=null){
			
			long id = Transpo.getInfo(st.getId() ==0? Transpo.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Transpo.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Transpo.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Transpo.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Transpo.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Transpo.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Transpo.insertData(this, "3");
		}
		
 }
	
	public static Transpo insertData(Transpo st, String type){
		String sql = "INSERT INTO transpo ("
				+ "tid,"
				+ "datetrans,"
				+ "controlno,"
				+ "ornumber,"
				+ "requestor,"
				+ "address,"
				+ "fromandto,"
				+ "purpose,"
				+ "delivered,"
				+ "issuedday,"
				+ "issuedmonth,"
				+ "licenseofficer,"
				+ "licenseofficerpos,"
				+ "officialName,"
				+ "officialPos,"
				+ "menroofficer,"
				+ "menropos,"
				+ "tranisactive,"
				+ "userid,"
				+ "prepby,"
				+ "preppos)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table transpo");
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
		ps.setString(cnt++, st.getOrNumber());
		ps.setString(cnt++, st.getRequestor());
		ps.setString(cnt++, st.getAddress());
		ps.setString(cnt++, st.getFromAndTo());
		ps.setString(cnt++, st.getPurpose());
		ps.setString(cnt++, st.getDeliveredDate());
		ps.setString(cnt++, st.getIssuedDay());
		ps.setString(cnt++, st.getIssuedMonth());
		ps.setString(cnt++, st.getLicenseOfficer());
		ps.setString(cnt++, st.getLicenseOfficerPosition());
		ps.setString(cnt++, st.getOfficialName());
		ps.setString(cnt++, st.getOfficialPosition());
		ps.setString(cnt++, st.getMenroOfficer());
		ps.setString(cnt++, st.getMenroPosition());
		ps.setInt(cnt++, st.getIsActive());
		ps.setLong(cnt++, st.getUserDtls().getUserdtlsid());
		ps.setString(cnt++, st.getPreparedBy());
		ps.setString(cnt++, st.getPreparedByPosition());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getControlNo());
		LogU.add(st.getOrNumber());
		LogU.add(st.getRequestor());
		LogU.add(st.getAddress());
		LogU.add(st.getFromAndTo());
		LogU.add(st.getPurpose());
		LogU.add(st.getDeliveredDate());
		LogU.add(st.getIssuedDay());
		LogU.add(st.getIssuedMonth());
		LogU.add(st.getLicenseOfficer());
		LogU.add(st.getLicenseOfficerPosition());
		LogU.add(st.getOfficialName());
		LogU.add(st.getOfficialPosition());
		LogU.add(st.getMenroOfficer());
		LogU.add(st.getMenroPosition());
		LogU.add(st.getIsActive());
		LogU.add(st.getUserDtls().getUserdtlsid());
		LogU.add(st.getPreparedBy());
		LogU.add(st.getPreparedByPosition());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to transpo : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static Transpo updateData(Transpo st){
		String sql = "UPDATE transpo SET "
				+ "datetrans=?,"
				+ "controlno=?,"
				+ "ornumber=?,"
				+ "requestor=?,"
				+ "address=?,"
				+ "fromandto=?,"
				+ "purpose=?,"
				+ "delivered=?,"
				+ "issuedday=?,"
				+ "issuedmonth=?,"
				+ "licenseofficer=?,"
				+ "licenseofficerpos=?,"
				+ "officialName=?,"
				+ "officialPos=?,"
				+ "menroofficer=?,"
				+ "menropos=?,"
				+ "userid=?,"
				+ "prepby=?,"
				+ "preppos=?" 
				+ " WHERE tid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table transpo");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getControlNo());
		ps.setString(cnt++, st.getOrNumber());
		ps.setString(cnt++, st.getRequestor());
		ps.setString(cnt++, st.getAddress());
		ps.setString(cnt++, st.getFromAndTo());
		ps.setString(cnt++, st.getPurpose());
		ps.setString(cnt++, st.getDeliveredDate());
		ps.setString(cnt++, st.getIssuedDay());
		ps.setString(cnt++, st.getIssuedMonth());
		ps.setString(cnt++, st.getLicenseOfficer());
		ps.setString(cnt++, st.getLicenseOfficerPosition());
		ps.setString(cnt++, st.getOfficialName());
		ps.setString(cnt++, st.getOfficialPosition());
		ps.setString(cnt++, st.getMenroOfficer());
		ps.setString(cnt++, st.getMenroPosition());
		ps.setLong(cnt++, st.getUserDtls().getUserdtlsid());
		ps.setString(cnt++, st.getPreparedBy());
		ps.setString(cnt++, st.getPreparedByPosition());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getControlNo());
		LogU.add(st.getOrNumber());
		LogU.add(st.getRequestor());
		LogU.add(st.getAddress());
		LogU.add(st.getFromAndTo());
		LogU.add(st.getPurpose());
		LogU.add(st.getDeliveredDate());
		LogU.add(st.getIssuedDay());
		LogU.add(st.getIssuedMonth());
		LogU.add(st.getLicenseOfficer());
		LogU.add(st.getLicenseOfficerPosition());
		LogU.add(st.getOfficialName());
		LogU.add(st.getOfficialPosition());
		LogU.add(st.getMenroOfficer());
		LogU.add(st.getMenroPosition());
		LogU.add(st.getUserDtls().getUserdtlsid());
		LogU.add(st.getPreparedBy());
		LogU.add(st.getPreparedByPosition());
		LogU.add("id: " + id);
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to transpo : " + s.getMessage());
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
		sql="SELECT tid FROM transpo  ORDER BY tid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("tid");
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
		ps = conn.prepareStatement("SELECT tid FROM transpo WHERE tid=?");
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
		String sql = "UPDATE transpo set tranisactive=0 WHERE tid=?";
		
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
