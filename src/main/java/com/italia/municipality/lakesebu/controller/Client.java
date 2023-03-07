package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.ClientStatus;
import com.italia.municipality.lakesebu.enm.ClientTransactionType;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Client {

	private long id;
	private String transNo;
	private String dateTrans;
	private String firstName;
	private String middleName;
	private String lastName;
	private int civilStatus;
	private int gender;
	private String birthday;
	private String birthPlace;
	private String address;
	private String tinNo;
	private String height;
	private String weight;
	private String companyName;
	private String nationality;
	private String profession;
	private double monthlySalary;
	private int transType;
	private int status;
	private int isActive;
	private String contactNumber;
	private int collectorId;
	private String businessName;
	private String remarks;
	
	private Date dateTransTmp;
	private Date birthdayTmp;
	private String genderName;
	private String civilStatusName;
	private String transactionName;
	private String clientStatus;
	
	private String collectorName;
	
	public static String getLatestTransactionNo() {
		
		
		int mo = DateUtils.getCurrentMonth();
		String month = mo <10? "0"+mo : mo+"";
		String year = DateUtils.getCurrentYear()+"";
		int dy = DateUtils.getCurrentDay();
		String day = dy<10? "0"+dy : dy+"";
		String number = year + "-" + month + "-" + day + "-0001";
		String sql = "SELECT transno FROM client WHERE isactivec=1 AND transno like '"+ year +"-" + month +"-"+ day + "-%' ORDER BY transno DESC LIMIT 1";
		boolean found = false;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("SQL latest transaction no " + ps.toString());
		
		rs = ps.executeQuery();
		
			while(rs.next()){
				number = rs.getString("transno").split("-")[3];
				found = true;
			}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		if(found) {
			String sequence = "";
			int num = Integer.valueOf(number);
			num += 1;
			String numTmp = num+"";
			int size = numTmp.length();
			switch(size) {
			case 1 :  sequence = "000"+num; break;
			case 2 : sequence = "00"+num; break;
			case 3 : sequence = "0"+num; break;
			case 4 : sequence = ""+num; break;
			}
			number = year + "-" + month + "-" + day + "-" + sequence;
		}
		
		
		return number;
	}
	
	public static List<Client> retrieve(String sql, String[] params){
		List<Client> trans = new ArrayList<Client>();
		
		String sqlAdd = "SELECT * FROM client  WHERE  isactivec=1 ";
		
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
		System.out.println("client SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Client c = Client.builder()
					.id(rs.getLong("cid"))
					.transNo(rs.getString("transno"))
					.dateTrans(rs.getString("datetrans"))
					.firstName(rs.getString("firstname"))
					.middleName(rs.getString("middlename"))
					.lastName(rs.getString("lastname"))
					.civilStatus(rs.getInt("civilstatus"))
					.civilStatusName(CivilStatus.typeName(rs.getInt("civilstatus")))
					.gender(rs.getInt("gender"))
					.genderName(rs.getInt("gender")==1? "Male":"Female")
					.birthday(rs.getString("birthday"))
					.birthPlace(rs.getString("birthplace"))
					.address(rs.getString("address"))
					.tinNo(rs.getString("tin"))
					.height(rs.getString("height"))
					.weight(rs.getString("weight"))
					.companyName(rs.getString("company"))
					.nationality(rs.getString("nationality"))
					.profession(rs.getString("profession"))
					.monthlySalary(rs.getDouble("monthlysal"))
					.transType(rs.getInt("transtype"))
					.transactionName(ClientTransactionType.nameId(rs.getInt("transtype")))
					.status(rs.getInt("status"))
					.clientStatus(ClientStatus.nameId(rs.getInt("status")))
					.isActive(rs.getInt("isactivec"))
					.contactNumber(rs.getString("contactno"))
					.collectorId(rs.getInt("isid"))
					.businessName(rs.getString("businessname"))
					.remarks(rs.getString("remarks"))
					.build();
			
			trans.add(c);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return trans;
	}
	
	public static Client save(Client st){
		if(st!=null){
			
			long id = Client.getInfo(st.getId() ==0? Client.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = Client.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = Client.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = Client.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			Client.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			Client.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			Client.insertData(this, "3");
		}
		
 }
	
	public static Client insertData(Client st, String type){
		String sql = "INSERT INTO client ("
				+ "cid,"
				+ "transno,"
				+ "datetrans,"
				+ "firstname,"
				+ "middlename,"
				+ "lastname,"
				+ "civilstatus,"
				+ "gender,"
				+ "birthday,"
				+ "birthplace,"
				+ "address,"
				+ "tin,"
				+ "height,"
				+ "weight,"
				+ "company,"
				+ "nationality,"
				+ "profession,"
				+ "monthlysal,"
				+ "transtype,"
				+ "status,"
				+ "isactivec,"
				+ "contactno,"
				+ "isid,"
				+ "businessname,"
				+ "remarks)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table Client");
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
		
		ps.setString(cnt++, st.getTransNo());
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getFirstName());
		ps.setString(cnt++, st.getMiddleName());
		ps.setString(cnt++, st.getLastName());
		ps.setInt(cnt++, st.getCivilStatus());
		ps.setInt(cnt++, st.getGender());
		ps.setString(cnt++, st.getBirthday());
		ps.setString(cnt++, st.getBirthPlace());
		ps.setString(cnt++, st.getAddress());
		ps.setString(cnt++, st.getTinNo());
		ps.setString(cnt++, st.getHeight());
		ps.setString(cnt++, st.getWeight());
		ps.setString(cnt++, st.getCompanyName());
		ps.setString(cnt++, st.getNationality());
		ps.setString(cnt++, st.getProfession());
		ps.setDouble(cnt++, st.getMonthlySalary());
		ps.setInt(cnt++, st.getTransType());
		ps.setInt(cnt++, st.getStatus());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getContactNumber());
		ps.setInt(cnt++, st.getCollectorId());
		ps.setString(cnt++, st.getBusinessName());
		ps.setString(cnt++, st.getRemarks());
		
		LogU.add(st.getTransNo());
		LogU.add(st.getDateTrans());
		LogU.add(st.getFirstName());
		LogU.add(st.getMiddleName());
		LogU.add(st.getLastName());
		LogU.add(st.getCivilStatus());
		LogU.add(st.getGender());
		LogU.add(st.getBirthday());
		LogU.add(st.getBirthPlace());
		LogU.add(st.getAddress());
		LogU.add(st.getTinNo());
		LogU.add(st.getHeight());
		LogU.add(st.getWeight());
		LogU.add(st.getCompanyName());
		LogU.add(st.getNationality());
		LogU.add(st.getProfession());
		LogU.add(st.getMonthlySalary());
		LogU.add(st.getTransType());
		LogU.add(st.getStatus());
		LogU.add(st.getIsActive());
		LogU.add(st.getIsActive());
		LogU.add(st.getContactNumber());
		LogU.add(st.getCollectorId());
		LogU.add(st.getBusinessName());
		LogU.add(st.getRemarks());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to client : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static Client updateData(Client st){
		String sql = "UPDATE client SET "
				+ "transno=?,"
				+ "datetrans=?,"
				+ "firstname=?,"
				+ "middlename=?,"
				+ "lastname=?,"
				+ "civilstatus=?,"
				+ "gender=?,"
				+ "birthday=?,"
				+ "birthplace=?,"
				+ "address=?,"
				+ "tin=?,"
				+ "height=?,"
				+ "weight=?,"
				+ "company=?,"
				+ "nationality=?,"
				+ "profession=?,"
				+ "monthlysal=?,"
				+ "transtype=?,"
				+ "status=?,"
				+ "contactno=?,"
				+ "isid=?,"
				+ "businessname=?,"
				+ "remarks=?" 
				+ " WHERE cid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table Client");
		
		ps.setString(cnt++, st.getTransNo());
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getFirstName());
		ps.setString(cnt++, st.getMiddleName());
		ps.setString(cnt++, st.getLastName());
		ps.setInt(cnt++, st.getCivilStatus());
		ps.setInt(cnt++, st.getGender());
		ps.setString(cnt++, st.getBirthday());
		ps.setString(cnt++, st.getBirthPlace());
		ps.setString(cnt++, st.getAddress());
		ps.setString(cnt++, st.getTinNo());
		ps.setString(cnt++, st.getHeight());
		ps.setString(cnt++, st.getWeight());
		ps.setString(cnt++, st.getCompanyName());
		ps.setString(cnt++, st.getNationality());
		ps.setString(cnt++, st.getProfession());
		ps.setDouble(cnt++, st.getMonthlySalary());
		ps.setInt(cnt++, st.getTransType());
		ps.setInt(cnt++, st.getStatus());
		ps.setString(cnt++, st.getContactNumber());
		ps.setInt(cnt++, st.getCollectorId());
		ps.setString(cnt++, st.getBusinessName());
		ps.setString(cnt++, st.getRemarks());
		ps.setLong(cnt++, st.getId());
		
		
		LogU.add(st.getTransNo());
		LogU.add(st.getDateTrans());
		LogU.add(st.getFirstName());
		LogU.add(st.getMiddleName());
		LogU.add(st.getLastName());
		LogU.add(st.getCivilStatus());
		LogU.add(st.getGender());
		LogU.add(st.getBirthday());
		LogU.add(st.getBirthPlace());
		LogU.add(st.getAddress());
		LogU.add(st.getTinNo());
		LogU.add(st.getHeight());
		LogU.add(st.getWeight());
		LogU.add(st.getCompanyName());
		LogU.add(st.getNationality());
		LogU.add(st.getProfession());
		LogU.add(st.getMonthlySalary());
		LogU.add(st.getTransType());
		LogU.add(st.getStatus());
		LogU.add(st.getIsActive());
		LogU.add(st.getContactNumber());
		LogU.add(st.getCollectorId());
		LogU.add(st.getBusinessName());
		LogU.add(st.getRemarks());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to client : " + s.getMessage());
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
		sql="SELECT cid FROM client  ORDER BY cid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("cid");
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
		ps = conn.prepareStatement("SELECT cid FROM client WHERE cid=?");
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
		String sql = "UPDATE client set isactivec=0 WHERE cid=?";
		
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