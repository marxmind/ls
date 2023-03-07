package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.Database;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Purok;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @version 1.0
 * @since 07/05/2017
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Livelihood {

	private long id;
	private String dateRegistered;
	private String dateRetired;
	private String businessName;
	private String purokName;
	private String areaMeter;
	private String supportingDetails;
	private int typeLine;
	private int status;
	private int isActive;
	private Timestamp timestamp;
	
	private Purok purok;
	private com.italia.municipality.lakesebu.licensing.controller.Barangay barangay;
	private Municipality municipality;
	private Province province;
	private BusinessCustomer taxPayer;
	private UserDtls userDtls;
	
	private String businessLabel;
	//private static final String WEBTIS = Database.WEBTIS.getName();
	
	public static Map<String,Livelihood> collecBusinessExist() {
		Map<String, Livelihood> bizs = new LinkedHashMap<String, Livelihood>();
		String sql = "SELECT * FROM livelihood WHERE livelihoodtype>1";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			Livelihood live = new Livelihood();
			try{live.setId(rs.getLong("livelihoodid"));}catch(NullPointerException e){}
			try{live.setDateRegistered(rs.getString("livedatereg"));}catch(NullPointerException e){}
			try{live.setDateRetired(rs.getString("liveretireddate"));}catch(NullPointerException e){}
			try{live.setBusinessName(rs.getString("livename"));}catch(NullPointerException e){}
			try{live.setPurokName(rs.getString("livepurok"));}catch(NullPointerException e){}
			try{live.setAreaMeter(rs.getString("liveareameter"));}catch(NullPointerException e){}
			try{live.setSupportingDetails(rs.getString("livedetails"));}catch(NullPointerException e){}
			try{live.setTypeLine(rs.getInt("livelihoodtype"));}catch(NullPointerException e){}
			try{live.setStatus(rs.getInt("livestatus"));}catch(NullPointerException e){}
			try{live.setIsActive(rs.getInt("isactivelive"));}catch(NullPointerException e){}
			try{live.setTimestamp(rs.getTimestamp("timestamplive"));}catch(NullPointerException e){}
			bizs.put(rs.getString("livename"), live);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return bizs;
	}
	
	public static boolean isExistingBusiness(String name, long costumerId) {
		String sql = "SELECT livename FROM livelihood WHERE isactivelive=1 AND customerid="+ costumerId + " AND livename='"+ name.trim() +"'";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		System.out.println("isExistingBusiness : " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static String getNewPlateNo() {
		String sql = "SELECT liveareameter FROM livelihood WHERE isactivelive=1 ORDER BY livelihoodid DESC limit 1";
		String plateNo = "0";
		String result = "0";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			plateNo = rs.getString("liveareameter");
		}
		
		
		int no = 0;
		try{no = Integer.valueOf(plateNo) +1;}catch(Exception e) {}
		String newNo = no+"";
		int size = newNo.length();
		if(size==4) {
			result = no+"";
		}else if(size==3) {
			result = "0"+no;
		}else if(size==2) {
			result = "00"+no;
		}else if(size==1) {
			result = "000"+no;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return result;
	}
	
	public static List<Livelihood> retrieve(String sqlAdd, String[] params){
		List<Livelihood> lives = new ArrayList<>();
		
		String tableLive = "live";
		String tableCus = "cuz";
		String tableBar = "bar";
		String tableMun = "mun";
		String tableProv = "prov";
		String tableUser = "usr";
		String sql = "SELECT * FROM livelihood " + tableLive + ", businesscustomer " + tableCus +", barangay " + tableBar + ", municipality " + tableMun + ", province " + tableProv + ", userdtls " + tableUser + " WHERE " +
				tableLive + ".customerid=" + tableCus + ".customerid AND " +
				tableLive + ".bgid=" + tableBar + ".bgid AND " + 
				tableLive + ".munid=" + tableMun + ".munid AND " +
				tableLive + ".provid=" + tableProv + ".provid AND " +
				tableLive + ".userdtlsid=" + tableUser + ".userdtlsid ";
		
		sql = sql + sqlAdd;		
				
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		System.out.println("SQL " + ps.toString());
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Livelihood live = new Livelihood();
			try{live.setId(rs.getLong("livelihoodid"));}catch(NullPointerException e){}
			try{live.setDateRegistered(rs.getString("livedatereg"));}catch(NullPointerException e){}
			try{live.setDateRetired(rs.getString("liveretireddate"));}catch(NullPointerException e){}
			try{live.setBusinessName(rs.getString("livename"));}catch(NullPointerException e){}
			try{live.setPurokName(rs.getString("livepurok"));}catch(NullPointerException e){}
			try{live.setAreaMeter(rs.getString("liveareameter"));}catch(NullPointerException e){}
			try{live.setSupportingDetails(rs.getString("livedetails"));}catch(NullPointerException e){}
			try{live.setTypeLine(rs.getInt("livelihoodtype"));}catch(NullPointerException e){}
			try{live.setStatus(rs.getInt("livestatus"));}catch(NullPointerException e){}
			try{live.setIsActive(rs.getInt("isactivelive"));}catch(NullPointerException e){}
			try{live.setTimestamp(rs.getTimestamp("timestamplive"));}catch(NullPointerException e){}
			
			BusinessCustomer cus = new BusinessCustomer();
			try{cus.setId(rs.getLong("customerid"));}catch(NullPointerException e){}
			try{cus.setFirstname(rs.getString("cusfirstname"));}catch(NullPointerException e){}
			try{cus.setMiddlename(rs.getString("cusmiddlename"));}catch(NullPointerException e){}
			try{cus.setLastname(rs.getString("cuslastname"));}catch(NullPointerException e){}
			try{cus.setFullname(rs.getString("fullname"));}catch(NullPointerException e){}
			try{cus.setGender(rs.getString("cusgender"));}catch(NullPointerException e){}
			try{cus.setAge(rs.getInt("cusage"));}catch(NullPointerException e){}
			//try{cus.setAddress(rs.getString("cusaddress"));}catch(NullPointerException e){}
			try{cus.setContactno(rs.getString("cuscontactno"));}catch(NullPointerException e){}
			try{cus.setDateregistered(rs.getString("cusdateregistered"));}catch(NullPointerException e){}
			try{cus.setCardno(rs.getString("cuscardno"));}catch(NullPointerException e){}
			try{cus.setIsactive(rs.getInt("cusisactive"));}catch(NullPointerException e){}
			try{cus.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			live.setTaxPayer(cus);
			
			com.italia.municipality.lakesebu.licensing.controller.Barangay bar = new com.italia.municipality.lakesebu.licensing.controller.Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			live.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			live.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			live.setProvince(prov);
			
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
			live.setUserDtls(user);
			
			lives.add(live);
			
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return lives;
	}
	
	public static Livelihood save(Livelihood live){
		if(live!=null){
			
			long id = Livelihood.getInfo(live.getId() ==0? Livelihood.getLatestId()+1 : live.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				live = Livelihood.insertData(live, "1");
			}else if(id==2){
				LogU.add("update Data ");
				live = Livelihood.updateData(live);
			}else if(id==3){
				LogU.add("added new Data ");
				live = Livelihood.insertData(live, "3");
			}
			
		}
		return live;
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
	
	public static Livelihood insertData(Livelihood live, String type){
		String sql = "INSERT INTO livelihood ("
				+ "livelihoodid,"
				+ "livedatereg,"
				+ "liveretireddate,"
				+ "livename,"
				+ "livepurok,"
				+ "liveareameter,"
				+ "livedetails,"
				+ "livelihoodtype,"
				+ "livestatus,"
				+ "bgid,"
				+ "munid,"
				+ "provid,"
				+ "isactivelive,"
				+ "customerid,"
				+ "userdtlsid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table livelihood");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			live.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			live.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, live.getDateRegistered());
		ps.setString(cnt++, live.getDateRetired());
		ps.setString(cnt++, live.getBusinessName());
		ps.setString(cnt++, live.getPurokName());
		ps.setString(cnt++, live.getAreaMeter());
		ps.setString(cnt++, live.getSupportingDetails());
		ps.setInt(cnt++, live.getTypeLine());
		ps.setInt(cnt++, live.getStatus());
		ps.setInt(cnt++, live.getBarangay()==null? 1 : live.getBarangay().getId());
		ps.setInt(cnt++, live.getMunicipality()==null? 1 : live.getMunicipality().getId());
		ps.setInt(cnt++, live.getProvince()==null? 1 : live.getProvince().getId());
		ps.setInt(cnt++, live.getIsActive());
		ps.setLong(cnt++, live.getTaxPayer()==null? 0 : live.getTaxPayer().getId());
		ps.setLong(cnt++, live.getUserDtls()==null? 0 : live.getUserDtls().getUserdtlsid());
		
		LogU.add(live.getDateRegistered());
		LogU.add(live.getDateRetired());
		LogU.add(live.getBusinessName());
		LogU.add(live.getPurokName());
		LogU.add(live.getAreaMeter());
		LogU.add(live.getSupportingDetails());
		LogU.add(live.getTypeLine());
		LogU.add(live.getStatus());
		LogU.add(live.getBarangay()==null? 1 : live.getBarangay().getId());
		LogU.add(live.getMunicipality()==null? 1 : live.getMunicipality().getId());
		LogU.add(live.getProvince()==null? 1 : live.getProvince().getId());
		LogU.add(live.getIsActive());
		LogU.add(live.getTaxPayer()==null? 0 : live.getTaxPayer().getId());
		LogU.add(live.getUserDtls()==null? 0 : live.getUserDtls().getUserdtlsid());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to livelihood : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return live;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO livelihood ("
				+ "livelihoodid,"
				+ "livedatereg,"
				+ "liveretireddate,"
				+ "livename,"
				+ "livepurok,"
				+ "liveareameter,"
				+ "livedetails,"
				+ "livelihoodtype,"
				+ "livestatus,"
				+ "bgid,"
				+ "munid,"
				+ "provid,"
				+ "isactivelive,"
				+ "customerid,"
				+ "userdtlsid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table livelihood");
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
		
		ps.setString(cnt++, getDateRegistered());
		ps.setString(cnt++, getDateRetired());
		ps.setString(cnt++, getBusinessName());
		ps.setString(cnt++, getPurokName());
		ps.setString(cnt++, getAreaMeter());
		ps.setString(cnt++, getSupportingDetails());
		ps.setInt(cnt++, getTypeLine());
		ps.setInt(cnt++, getStatus());
		ps.setInt(cnt++, getBarangay()==null? 1 : getBarangay().getId());
		ps.setInt(cnt++, getMunicipality()==null? 1 : getMunicipality().getId());
		ps.setInt(cnt++, getProvince()==null? 1 : getProvince().getId());
		ps.setInt(cnt++, getIsActive());
		ps.setLong(cnt++, getTaxPayer()==null? 0 : getTaxPayer().getId());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		
		LogU.add(getDateRegistered());
		LogU.add(getDateRetired());
		LogU.add(getBusinessName());
		LogU.add(getPurokName());
		LogU.add(getAreaMeter());
		LogU.add(getSupportingDetails());
		LogU.add(getTypeLine());
		LogU.add(getStatus());
		LogU.add(getBarangay()==null? 1 : getBarangay().getId());
		LogU.add(getMunicipality()==null? 1 : getMunicipality().getId());
		LogU.add(getProvince()==null? 1 : getProvince().getId());
		LogU.add(getIsActive());
		LogU.add(getTaxPayer()==null? 0 : getTaxPayer().getId());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to livelihood : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Livelihood updateData(Livelihood live){
		String sql = "UPDATE livelihood SET "
				+ "livedatereg=?,"
				+ "liveretireddate=?,"
				+ "livename=?,"
				+ "livepurok=?,"
				+ "liveareameter=?,"
				+ "livedetails=?,"
				+ "livelihoodtype=?,"
				+ "livestatus=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "customerid=?,"
				+ "userdtlsid=?" 
				+ " WHERE livelihoodid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table livelihood");
		
		
		ps.setString(cnt++, live.getDateRegistered());
		ps.setString(cnt++, live.getDateRetired());
		ps.setString(cnt++, live.getBusinessName());
		ps.setString(cnt++, live.getPurokName());
		ps.setString(cnt++, live.getAreaMeter());
		ps.setString(cnt++, live.getSupportingDetails());
		ps.setInt(cnt++, live.getTypeLine());
		ps.setInt(cnt++, live.getStatus());
		ps.setInt(cnt++, live.getBarangay()==null? 1 : live.getBarangay().getId());
		ps.setInt(cnt++, live.getMunicipality()==null? 1 : live.getMunicipality().getId());
		ps.setInt(cnt++, live.getProvince()==null? 1 : live.getProvince().getId());
		ps.setLong(cnt++, live.getTaxPayer()==null? 0 : live.getTaxPayer().getId());
		ps.setLong(cnt++, live.getUserDtls()==null? 0 : live.getUserDtls().getUserdtlsid());
		ps.setLong(cnt++, live.getId());
		
		LogU.add(live.getDateRegistered());
		LogU.add(live.getDateRetired());
		LogU.add(live.getBusinessName());
		LogU.add(live.getPurokName());
		LogU.add(live.getAreaMeter());
		LogU.add(live.getSupportingDetails());
		LogU.add(live.getTypeLine());
		LogU.add(live.getStatus());
		LogU.add(live.getBarangay()==null? 1 : live.getBarangay().getId());
		LogU.add(live.getMunicipality()==null? 1 : live.getMunicipality().getId());
		LogU.add(live.getProvince()==null? 1 : live.getProvince().getId());
		LogU.add(live.getTaxPayer()==null? 0 : live.getTaxPayer().getId());
		LogU.add(live.getUserDtls()==null? 0 : live.getUserDtls().getUserdtlsid());
		LogU.add(live.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to livelihood : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return live;
	}
	
	public void updateData(){
		String sql = "UPDATE livelihood SET "
				+ "livedatereg=?,"
				+ "liveretireddate=?,"
				+ "livename=?,"
				+ "livepurok=?,"
				+ "liveareameter=?,"
				+ "livedetails=?,"
				+ "livelihoodtype=?,"
				+ "livestatus=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "customerid=?,"
				+ "userdtlsid=?" 
				+ " WHERE livelihoodid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table livelihood");
		
		
		ps.setString(cnt++, getDateRegistered());
		ps.setString(cnt++, getDateRetired());
		ps.setString(cnt++, getBusinessName());
		ps.setString(cnt++, getPurokName());
		ps.setString(cnt++, getAreaMeter());
		ps.setString(cnt++, getSupportingDetails());
		ps.setInt(cnt++, getTypeLine());
		ps.setInt(cnt++, getStatus());
		ps.setInt(cnt++, getBarangay()==null? 1 : getBarangay().getId());
		ps.setInt(cnt++, getMunicipality()==null? 1 : getMunicipality().getId());
		ps.setInt(cnt++, getProvince()==null? 1 : getProvince().getId());
		ps.setLong(cnt++, getTaxPayer()==null? 0 : getTaxPayer().getId());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateRegistered());
		LogU.add(getDateRetired());
		LogU.add(getBusinessName());
		LogU.add(getPurokName());
		LogU.add(getAreaMeter());
		LogU.add(getSupportingDetails());
		LogU.add(getTypeLine());
		LogU.add(getStatus());
		LogU.add(getBarangay()==null? 1 : getBarangay().getId());
		LogU.add(getMunicipality()==null? 1 : getMunicipality().getId());
		LogU.add(getProvince()==null? 1 : getProvince().getId());
		LogU.add(getTaxPayer()==null? 0 : getTaxPayer().getId());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to livelihood : " + s.getMessage());
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
		sql="SELECT livelihoodid FROM livelihood  ORDER BY livelihoodid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("livelihoodid");
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
		ps = conn.prepareStatement("SELECT livelihoodid FROM livelihood WHERE livelihoodid=?");
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
		String sql = "UPDATE livelihood set isactivelive=0,userdtlsid="+ getUserDtls().getUserdtlsid() +" WHERE livelihoodid=?";
		
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
