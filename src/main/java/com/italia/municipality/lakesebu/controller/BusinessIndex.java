package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
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
public class BusinessIndex {

	private long id;
	private String dateTrans;
	private String businessName;
	private String owner;
	private int statusId;
	private int isActive;
	private Barangay barangay;
	private Municipality municipal;
	private Province provincial;
	private Regional regional;
	private String purok;
	private BusinessMapping map;
	private int isEssential;
	private int category;
	private String natureOfBusiness;
	private String contanctNo;
	
	private Date dateTmp;
	private String address;
	private int type;
	
	public static List<String> retrieveNatureOfBusiness(String sql, String[] params){
		List<String> results = new ArrayList<String>();
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
			results.add(rs.getString("natureofbusiness"));
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return results;
	}
	
	public static List<BusinessIndex> retrieveName(String columnName, String name){
		List<BusinessIndex> bns = new ArrayList<BusinessIndex>();
		
		
		String sql = "SELECT bnid,bnname,bnowner,isessentail,category,natureofbusiness FROM businessindex WHERE isactivebn=1 AND bnstatus=0 AND  "+columnName+" like '%"+ name +"%'";
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		
		System.out.println("names SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			BusinessIndex index = BusinessIndex.builder()
					.id(rs.getLong("bnid"))
					.businessName(rs.getString("bnname"))
					.owner(rs.getString("bnowner"))
					.isEssential(rs.getInt("isessentail"))
					.category(rs.getInt("category"))
					.natureOfBusiness(rs.getString("natureofbusiness"))
					.contanctNo(rs.getString("contactno"))
					.build();
			
			bns.add(index);
			
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return bns;
	}
	
	public static List<BusinessIndex> retrieve(String sql, String[] params){
		List<BusinessIndex> bns = new ArrayList<BusinessIndex>();
		
		//String sqlTemp = "SELECT * FROM businessindex "+ bn +" WHERE "+ bn +".isactivebn=1 ";
		String bn = "bn";
		String tableBar = "bgy";
		String tableMun = "mun";
		String tableProv = "prv";
		String tableReg = "rg";
		
		String sqlTemp = "SELECT * FROM businessindex "+ bn +" ,barangay " + tableBar + ", municipality " + tableMun + ", province " + tableProv + ", regional " + tableReg +
				" WHERE " + bn + ".bgid=" + tableBar +".bgid AND "
						+ bn + ".munid=" + tableMun +".munid AND "
						+ bn + ".provid=" + tableProv+".provid AND "
						+ bn + ".regid=" + tableReg +".regid AND " + bn + ".isactivebn=1 ";
		
		
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
		System.out.println("businessindex SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			bar.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			bar.setProvince(prov);
			
			Regional reg = new Regional();
			try{reg.setId(rs.getInt("regid"));}catch(NullPointerException e){}
			try{reg.setName(rs.getString("regname"));}catch(NullPointerException e){}
			try{reg.setIsActive(rs.getInt("isactivereg"));}catch(NullPointerException e){}
			bar.setRegional(reg);
			
			
			String address = rs.getString("purok")!=null? rs.getString("purok")+", " : "";
				   address += bar.getName()+", ";
				   address += mun.getName() +", ";
				   address += prov.getName() +", ";
				   address += reg.getName();
			BusinessIndex index = BusinessIndex.builder()
					.id(rs.getLong("bnid"))
					.dateTrans(rs.getString("bndate"))
					.businessName(rs.getString("bnname"))
					.owner(rs.getString("bnowner"))
					.statusId(rs.getInt("bnstatus"))
					.isActive(rs.getInt("isactivebn"))
					.purok(rs.getString("purok"))
					.natureOfBusiness(rs.getString("natureofbusiness"))
					.barangay(bar)
					.municipal(mun)
					.provincial(prov)
					.regional(reg)
					.address(address.toUpperCase())
					.map(BusinessMapping.builder().id(rs.getLong("bzid")).build())
					.isEssential(rs.getInt("isessentail"))
					.category(rs.getInt("category"))
					.contanctNo(rs.getString("contactno"))
					.build();
			
			bns.add(index);
			
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return bns;
	}
	
	public static BusinessIndex save(BusinessIndex st){
		if(st!=null){
			
			long id = BusinessIndex.getInfo(st.getId() ==0? BusinessIndex.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = BusinessIndex.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = BusinessIndex.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = BusinessIndex.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			BusinessIndex.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			BusinessIndex.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			BusinessIndex.insertData(this, "3");
		}
		
 }
	
	public static BusinessIndex insertData(BusinessIndex st, String type){
		String sql = "INSERT INTO businessindex ("
				+ "bnid,"
				+ "bndate,"
				+ "bnname,"
				+ "bnowner,"
				+ "bnstatus,"
				+ "isactivebn,"
				+ "bgid,"
				+ "munid,"
				+ "provid,"
				+ "regid,"
				+ "purok,"
				+ "bzid,"
				+ "isessentail,"
				+ "category,"
				+ "natureofbusiness,"
				+ "contactno)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table businessindex");
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
		ps.setString(cnt++, st.getBusinessName());
		ps.setString(cnt++, st.getOwner());
		ps.setInt(cnt++, st.getStatusId());
		ps.setInt(cnt++, st.getIsActive());
		ps.setInt(cnt++, st.getBarangay().getId());
		ps.setInt(cnt++, st.getMunicipal().getId());
		ps.setInt(cnt++, st.getProvincial().getId());
		ps.setInt(cnt++, st.getRegional().getId());
		ps.setString(cnt++, st.getPurok());
		ps.setLong(cnt++, st.getMap().getId());
		ps.setInt(cnt++, st.getIsEssential());
		ps.setInt(cnt++, st.getCategory());
		ps.setString(cnt++, st.getNatureOfBusiness());
		ps.setString(cnt++, st.getContanctNo());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getBusinessName());
		LogU.add(st.getOwner());
		LogU.add(st.getStatusId());
		LogU.add(st.getIsActive());
		LogU.add(st.getBarangay().getId());
		LogU.add(st.getMunicipal().getId());
		LogU.add(st.getProvincial().getId());
		LogU.add(st.getRegional().getId());
		LogU.add(st.getPurok());
		LogU.add(st.getMap().getId());	
		LogU.add(st.getIsEssential());
		LogU.add(st.getCategory());
		LogU.add(st.getNatureOfBusiness());
		LogU.add(st.getContanctNo());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to businessindex : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static BusinessIndex updateData(BusinessIndex st){
		String sql = "UPDATE businessindex SET "
				+ "bndate=?,"
				+ "bnname=?,"
				+ "bnowner=?,"
				+ "bnstatus=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "regid=?,"
				+ "purok=?,"
				+ "bzid=?,"
				+ "isessentail=?,"
				+ "category=?,"
				+ "natureofbusiness=?,"
				+ "contactno=?" 
				+ " WHERE bnid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table businessindex");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setString(cnt++, st.getBusinessName());
		ps.setString(cnt++, st.getOwner());
		ps.setInt(cnt++, st.getStatusId());
		ps.setInt(cnt++, st.getBarangay().getId());
		ps.setInt(cnt++, st.getMunicipal().getId());
		ps.setInt(cnt++, st.getProvincial().getId());
		ps.setInt(cnt++, st.getRegional().getId());
		ps.setString(cnt++, st.getPurok());
		ps.setLong(cnt++, st.getMap().getId());
		ps.setInt(cnt++, st.getIsEssential());
		ps.setInt(cnt++, st.getCategory());
		ps.setString(cnt++, st.getNatureOfBusiness());
		ps.setString(cnt++, st.getContanctNo());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getBusinessName());
		LogU.add(st.getOwner());
		LogU.add(st.getStatusId());
		LogU.add(st.getBarangay().getId());
		LogU.add(st.getMunicipal().getId());
		LogU.add(st.getProvincial().getId());
		LogU.add(st.getRegional().getId());
		LogU.add(st.getPurok());
		LogU.add(st.getMap().getId());
		LogU.add(st.getIsEssential());
		LogU.add(st.getCategory());
		LogU.add(st.getNatureOfBusiness());
		LogU.add(st.getContanctNo());
		LogU.add(st.getId());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to businessindex : " + s.getMessage());
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
		sql="SELECT bnid FROM businessindex ORDER BY bnid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("bnid");
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
		ps = conn.prepareStatement("SELECT bnid FROM businessindex WHERE bnid=?");
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
		String sql = "UPDATE businessindex set isactivebn=0 WHERE bnid=?";
		
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
