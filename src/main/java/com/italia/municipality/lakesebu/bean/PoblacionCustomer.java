package com.italia.municipality.lakesebu.bean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Purok;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @For licensing customer
 * @author mark italia
 * @since 11/01/2016
 * @version 1.0
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PoblacionCustomer {

	private long id;
	private String firstname;
	private String middlename;
	private String lastname;
	private String fullname;
	private String gender;
	private int age;
	private String contactno;
	private String dateregistered;
	private String cardno; 
	private int isactive;
	private UserDtls userDtls;
	private Timestamp timestamp;
	private int civilStatus;
	private String photoid;
	
	private int relationship;
	private String birthdate;
	private PoblacionCustomer emergencyContactPerson;
	
	private Purok purok;
	private Barangay barangay;
	private Municipality municipality;
	private Province province;
	
	private String genderName;
	
	private String completeAddress;
	
	public static boolean validateNameEntry(String name){
		String sql = "SELECT fullname FROM poblacioncustomer WHERE cusisactive=1 AND fullname=?";
		String[] params = new String[1];
		params[0] = name;
		
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
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static List<String> names(String name){
		List<String> str = new ArrayList<String>();
		String sql = "SELECT fullname FROM poblacioncustomer WHERE cusisactive=1 AND fullname like '%"+ name.replace("--", "") +"%'";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			str.add(rs.getString("fullname"));
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return str;
	}
	
	public PoblacionCustomer(
			long customerid,
			String firstname,
			String middlename,
			String lastname,
			String fullname,
			String gender,
			int age,
			String address, 
			String contactno,
			String dateregistered,
			String cardno, 
			int isactive,
			UserDtls userDtls,
			Barangay barangay,
			Municipality municipality,
			Province province,
			int civilStatus,
			int relationship,
			String birthdate,
			PoblacionCustomer emergencyContactPerson,
			String photoid
			){
		
		this.id = customerid;
		this.firstname = firstname;
		this.middlename = middlename;
		this.lastname = lastname;
		this.fullname = fullname;
		this.gender = gender;
		this.age = age;
		//this.address = address;
		this.contactno = contactno;
		this.dateregistered = dateregistered;
		this.cardno = cardno;
		this.isactive = isactive;
		this.userDtls = userDtls;
		this.barangay = barangay;
		this.municipality = municipality;
		this.province = province;
		this.civilStatus = civilStatus;
		this.relationship = relationship;
		this.birthdate = birthdate;
		this.emergencyContactPerson = emergencyContactPerson;
		this.photoid = photoid;
		
	}
	
	public static String cardNumber(){
		String cardNum = "000000000000000";
		int id = 0;
		try{id = Integer.valueOf(getLatestCardNo().split(" ")[1]);}catch(Exception e){}
		String newCardNumber = (id+1) + "";
		//cardNum = (getLatestId()+1) + DateUtils.getCurrentDateMMDDYYYYPlain() + DateUtils.getCurrentDateMMDDYYYYTIMEPlain();
		String brgyCode = "MUN ";
		int num = newCardNumber.length();
		
		switch(num){
			case 1: cardNum = brgyCode+"00000"+newCardNumber;  break;
			case 2: cardNum = brgyCode+"0000"+newCardNumber;  break;
			case 3: cardNum = brgyCode+"000"+newCardNumber;  break;
			case 4: cardNum = brgyCode+"00"+newCardNumber;  break;
			case 5: cardNum = brgyCode+"0"+newCardNumber;  break;
			case 6: cardNum = brgyCode+newCardNumber;  break;
		}
		
		return cardNum;
	}
	
	public static int firstYearData(){
		String sql = "SELECT cusdateregistered FROM poblacioncustomer limit 1";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return Integer.valueOf(rs.getString("cusdateregistered").split("-")[0]);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return 0;
	}
	
	public static boolean isImageInUsed(String photoId){
		
		String sql = "SELECT photoid FROM poblacioncustomer WHERE photoid='" + photoId + "' ";
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
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
	
	public static boolean validateNameEntry(String first, String middle, String last){
		String sql = "SELECT cusfirstname,cusmiddlename,cuslastname FROM poblacioncustomer WHERE cusisactive=1 AND cusfirstname=? AND cusmiddlename=? AND cuslastname=?";
		String[] params = new String[3];
		params[0] = first;
		params[1] = middle;
		params[2] = last;
		
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
			return true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return false;
	}
	
	public static List<String> retrieveLFMName(String param, String fieldName, String limit){
		
		String sql = "SELECT cuslastname,cusfirstname,cusmiddlename " + fieldName + " FROM poblacioncustomer WHERE " + fieldName +" like '" + param + "%' " + limit;
		List<String> result = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		System.out.println("SQL.... " + ps.toString());
		while(rs.next()){
			result.add(rs.getString("cuslastname").toUpperCase() + ", " + rs.getString("cusfirstname").toUpperCase() + " " + (rs.getString("cusmiddlename")!=null? rs.getString("cusmiddlename").substring(0, 1).toUpperCase()+"." : "."));
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return result;
	}
	
	public static List<String> retrieve(String param, String fieldName, String limit){
		
		String sql = "SELECT DISTINCT " + fieldName + " FROM poblacioncustomer WHERE " + fieldName +" like '" + param + "%' " + limit;
		List<String> result = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			result.add(rs.getString(fieldName));
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return result;
	}
	
	public static String customerSQL(String tablename,PoblacionCustomer cus){
		String sql= " AND "+ tablename +".cusisactive=" + cus.getIsactive();
		if(cus!=null){
			
			if(cus.getId()!=0){
				sql += " AND "+ tablename +".customerid=" + cus.getId();
			}/*else{
				sql += " AND "+ tablename +".customerid=" + cus.getCustomerid();
			}*/
			if(cus.getDateregistered()!=null){
				sql += " AND "+ tablename +".cusdateregistered like '%" + cus.getDateregistered()+"%'";
			}
			if(cus.getFirstname()!=null){
				sql += " AND "+ tablename +".cusfirstname like '%" + cus.getFirstname()+"%'";
			}
			if(cus.getMiddlename()!=null){
				sql += " AND "+ tablename +".cusmiddlename like '%" + cus.getMiddlename()+"%'";
			}
			if(cus.getLastname()!=null){
				sql += " AND "+ tablename +".cuslastname like '%" + cus.getLastname()+"%'";
			}
			if(cus.getFullname()!=null){
				sql += " AND "+ tablename +".fullname like '%" + cus.getFullname()+"%'";
			}
			/*if(cus.getAddress()!=null){
				sql += " AND "+ tablename +".cusaddress like '%" + cus.getAddress()+"%'";
			}*/
			if(cus.getContactno()!=null){
				sql += " AND "+ tablename +".cuscontactno like'%" + cus.getContactno()+"%'";
			}
			if(cus.getAge()!=0){
				sql += " AND "+ tablename +".cusage=" + cus.getAge();
			}
			if(cus.getGender()!=null){
				sql += " AND "+ tablename +".cusgender like'%" + cus.getGender()+"%'";
			}
			
			if(cus.getCivilStatus()!=0){
				sql += " AND "+ tablename +".civilstatus=" + cus.getCivilStatus();
			}
			
			if(cus.getUserDtls()!=null){
				if(cus.getUserDtls().getUserdtlsid()!=0){
					sql += " AND "+ tablename +".userdtlsid=" + cus.getUserDtls().getUserdtlsid() ;
				}
			}
			
		}
		
		return sql;
	}
	
	public static List<PoblacionCustomer> retrieve(Object... obj){
		List<PoblacionCustomer> cuss = new ArrayList<PoblacionCustomer>();
		String supTable = "cus";
		String userTable = "usr";
		String purokTable = "pur";
		String barTable = "bar";
		String munTable = "mun";
		String provTable = "prov";
		String sql = "SELECT * FROM poblacioncustomer "+ supTable +
				                 ", userdtls "+ userTable +
				                 ", purok "+ purokTable +
				                 ", barangay "+ barTable +
				                 ", municipality "+ munTable +
				                 ", province "+ provTable + 
				                 
				" WHERE "
				+ supTable +".userdtlsid = "+ userTable +".userdtlsid AND "
				+ supTable +".purid = "+ purokTable +".purid AND "
				+ supTable +".bgid = "+ barTable +".bgid AND "
				+ supTable +".munid = "+ munTable +".munid AND "
				+ supTable +".provid = "+ provTable +".provid AND " 
				+ supTable +".purid = " + purokTable +".purid ";
		
		for(int i=0;i<obj.length;i++){
			if(obj[i] instanceof PoblacionCustomer){
				sql += customerSQL(supTable,(PoblacionCustomer)obj[i]);
			}
			if(obj[i] instanceof UserDtls){
				sql += UserDtls.userSQL(userTable,(UserDtls)obj[i]);
			}
			if(obj[i] instanceof Purok){
				sql += Purok.purokSQL(purokTable,(Purok)obj[i]);
			}
			if(obj[i] instanceof Barangay){
				sql += Barangay.barangaySQL(barTable,(Barangay)obj[i]);
			}
			if(obj[i] instanceof Municipality){
				sql += Municipality.municipalitySQL(munTable,(Municipality)obj[i]);
			}
			if(obj[i] instanceof Province){
				sql += Province.provinceSQL(provTable,(Province)obj[i]);
			}
		}
		
        System.out.println("SQL "+sql);
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			PoblacionCustomer cus = new PoblacionCustomer();
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
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			try{PoblacionCustomer emergency = new PoblacionCustomer();
			emergency.setId(rs.getLong("emeperson"));
			cus.setEmergencyContactPerson(emergency);}catch(NullPointerException e){}
			try{cus.setRelationship(rs.getInt("relid"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			cus.setPurok(pur);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			cus.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			cus.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			cus.setProvince(prov);
			
			String address="";
			try{
				address = pur.getId()==0?"" : pur.getPurokName()+", ";
				address += bar.getId()==0?"" : bar.getName()+", ";
				address += mun.getId()==0?"" : mun.getName()+", ";
				address += prov.getName();
			}catch(Exception e){}
			cus.setCompleteAddress(address);
			
			cuss.add(cus);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return cuss;
	}
	
	public static List<PoblacionCustomer> retrieve(String sqlAdd, String[] params){
		List<PoblacionCustomer> cuss = new ArrayList<PoblacionCustomer>();
		String supTable = "cus";
		String userTable = "usr";
		String purTable = "pur";
		String barTable = "bar";
		String munTable = "mun";
		String provTable = "prov";
		String sql = "SELECT * FROM poblacioncustomer "+ supTable +
				                 ", webtis.userdtls "+ userTable +
				                 ", purok "+ purTable +
				                 ", barangay "+ barTable +
				                 ", municipality "+ munTable +
				                 ", province "+ provTable + 
				                 
				" WHERE "
				+ supTable +".userdtlsid = "+ userTable +".userdtlsid AND "
				+ supTable +".purid = "+ purTable +".purid AND "
				+ supTable +".bgid = "+ barTable +".bgid AND "
				+ supTable +".munid = "+ munTable +".munid AND "
				+ supTable +".provid = "+ provTable +".provid ";
		
		sql = sql + sqlAdd;
		
		
        System.out.println("SQL Customer >> "+sql);
		
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
			
			PoblacionCustomer cus = new PoblacionCustomer();
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
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			try{PoblacionCustomer emergency = new PoblacionCustomer();
			emergency.setId(rs.getLong("emeperson"));
			cus.setEmergencyContactPerson(emergency);}catch(NullPointerException e){}
			try{cus.setRelationship(rs.getInt("relid"));}catch(NullPointerException e){}
			
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			cus.setPurok(pur);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			cus.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			cus.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			cus.setProvince(prov);
			
			String address="";
			try{
				address = pur.getId()==0?"" : pur.getPurokName()+", ";
				address += bar.getId()==0?"" : bar.getName()+", ";
				address += mun.getId()==0?"" : mun.getName()+", ";
				address += prov.getName();
			}catch(Exception e){}
			cus.setCompleteAddress(address);
			
			cuss.add(cus);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return cuss;
	}
	
	public static List<PoblacionCustomer> retrieve(String sqlAdd, String[] params, boolean isServerDatabase){
		List<PoblacionCustomer> cuss = new ArrayList<PoblacionCustomer>();
		String supTable = "cus";
		String userTable = "usr";
		String purTable = "pur";
		String barTable = "bar";
		String munTable = "mun";
		String provTable = "prov";
		String sql = "SELECT * FROM poblacioncustomer "+ supTable +
				                 ", webtis.userdtls "+ userTable +
				                 ", purok "+ purTable +
				                 ", barangay "+ barTable +
				                 ", municipality "+ munTable +
				                 ", province "+ provTable + 
				                 
				" WHERE "
				+ supTable +".userdtlsid = "+ userTable +".userdtlsid AND "
				+ supTable +".purid = "+ purTable +".purid AND "
				+ supTable +".bgid = "+ barTable +".bgid AND "
				+ supTable +".munid = "+ munTable +".munid AND "
				+ supTable +".provid = "+ provTable +".provid ";
		
		sql = sql + sqlAdd;
		
		
        System.out.println("SQL Customer >> "+sql);
		
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
			
			PoblacionCustomer cus = new PoblacionCustomer();
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
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			try{PoblacionCustomer emergency = new PoblacionCustomer();
			emergency.setId(rs.getLong("emeperson"));
			cus.setEmergencyContactPerson(emergency);}catch(NullPointerException e){}
			try{cus.setRelationship(rs.getInt("relid"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			cus.setPurok(pur);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			cus.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			cus.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			cus.setProvince(prov);
			
			String address="";
			try{
				address = pur.getId()==0?"" : pur.getPurokName()+", ";
				address += bar.getId()==0?"" : bar.getName()+", ";
				address += mun.getId()==0?"" : mun.getName()+", ";
				address += prov.getName();
			}catch(Exception e){}
			cus.setCompleteAddress(address);
			
			cuss.add(cus);
		}
		
		rs.close();
		ps.close();
		
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return cuss;
	}
	
	public static PoblacionCustomer retrieve(long customerid){
		PoblacionCustomer cus = new PoblacionCustomer();
		String supTable = "cus";
		String userTable = "usr";
		/*String sql = "SELECT * FROM customer "+ supTable +", userdtls "+ userTable +" WHERE "+ supTable +".userdtlsid = "
		+ userTable +".userdtlsid AND " + supTable + ".customerid="+customerid;
		*/
		String purTable = "pur";
		String barTable = "bar";
		String munTable = "mun";
		String provTable = "prov";
		String sql = "SELECT * FROM poblacioncustomer "+ supTable +
				                 ", webtis.userdtls "+ userTable +
				                 ", purok "+ purTable +
				                 ", barangay "+ barTable +
				                 ", municipality "+ munTable +
				                 ", province "+ provTable + 
				                 
				" WHERE "
				+ supTable +".userdtlsid = "+ userTable +".userdtlsid AND "
				+ supTable +".purid = "+ purTable +".purid AND "
				+ supTable +".bgid = "+ barTable +".bgid AND "
				+ supTable +".munid = "+ munTable +".munid AND "
				+ supTable +".provid = "+ provTable +".provid AND " +
				supTable + ".customerid="+customerid;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
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
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			try{PoblacionCustomer emergency = new PoblacionCustomer();
			emergency.setId(rs.getLong("emeperson"));
			cus.setEmergencyContactPerson(emergency);}catch(NullPointerException e){}
			try{cus.setRelationship(rs.getInt("relid"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			cus.setPurok(pur);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			cus.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			cus.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			cus.setProvince(prov);
			
			String address="";
			try{
				address = pur.getId()==0?"" : pur.getPurokName()+", ";
				address += bar.getId()==0?"" : bar.getName()+", ";
				address += mun.getId()==0?"" : mun.getName()+", ";
				address += prov.getName();
			}catch(Exception e){}
			cus.setCompleteAddress(address);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return cus;
	}
	
	public static PoblacionCustomer addUnknownCustomer(){
		PoblacionCustomer customer = new PoblacionCustomer();
		String unknowname = "unknown buyer" + DateUtils.getCurrentDateMMDDYYYYTIME();
		customer.setFirstname(unknowname);
		customer.setMiddlename(unknowname);
		customer.setLastname(unknowname);
		customer.setFullname(unknowname);
		customer.setGender("1");
		customer.setAge(0);
		//customer.setAddress("unknown address");
		customer.setContactno("00000000000");
		customer.setDateregistered(DateUtils.getCurrentDateYYYYMMDD());
		customer.setIsactive(1);
		customer.setUserDtls(Login.getUserLogin().getUserDtls());
		return customer;
	}
	
	public static PoblacionCustomer customer(long customerId){
		PoblacionCustomer cus = new PoblacionCustomer();
		String supTable = "cus";
		String userTable = "usr";
		/*String sql = "SELECT * FROM customer "+ supTable +", userdtls "+ userTable +" WHERE "+ supTable +".userdtlsid = "+ userTable +".userdtlsid AND "
				+ supTable +".customerid="+customerId;
		
        System.out.println("SQL "+sql);*/
		String purTable = "pur";
		String barTable = "bar";
		String munTable = "mun";
		String provTable = "prov";
		String sql = "SELECT * FROM poblacioncustomer "+ supTable +
				                 ", userdtls "+ userTable +
				                 ", purok "+ purTable +
				                 ", barangay "+ barTable +
				                 ", municipality "+ munTable +
				                 ", province "+ provTable + 
				                 
				" WHERE "
				+ supTable +".userdtlsid = "+ userTable +".userdtlsid AND "
				+ supTable +".purid = "+ purTable +".purid AND "
				+ supTable +".bgid = "+ barTable +".bgid AND "
				+ supTable +".munid = "+ munTable +".munid AND "
				+ supTable +".provid = "+ provTable +".provid AND " +
				supTable + ".customerid="+customerId;
		
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
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
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			try{PoblacionCustomer emergency = new PoblacionCustomer();
			emergency.setId(rs.getLong("emeperson"));
			cus.setEmergencyContactPerson(emergency);}catch(NullPointerException e){}
			try{cus.setRelationship(rs.getInt("relid"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			try{user.setFirstname(rs.getString("firstname"));}catch(NullPointerException e){}
			try{user.setMiddlename(rs.getString("middlename"));}catch(NullPointerException e){}
			try{user.setLastname(rs.getString("lastname"));}catch(NullPointerException e){}
			try{user.setAddress(rs.getString("address"));}catch(NullPointerException e){}
			try{user.setContactno(rs.getString("contactno"));}catch(NullPointerException e){}
			try{user.setAge(rs.getInt("age"));}catch(NullPointerException e){}
			try{user.setGender(rs.getInt("gender"));}catch(NullPointerException e){}
			try{user.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{user.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			try{pur.setDateTrans(rs.getString("purdate"));}catch(NullPointerException e){}
			try{pur.setPurokName(rs.getString("purokname"));}catch(NullPointerException e){}
			try{pur.setIsActive(rs.getInt("isactivepurok"));}catch(NullPointerException e){}
			cus.setPurok(pur);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			try{bar.setName(rs.getString("bgname"));}catch(NullPointerException e){}
			try{bar.setIsActive(rs.getInt("bgisactive"));}catch(NullPointerException e){}
			cus.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			try{mun.setName(rs.getString("munname"));}catch(NullPointerException e){}
			try{mun.setIsActive(rs.getInt("munisactive"));}catch(NullPointerException e){}
			cus.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			try{prov.setName(rs.getString("provname"));}catch(NullPointerException e){}
			try{prov.setIsActive(rs.getInt("provisactive"));}catch(NullPointerException e){}
			cus.setProvince(prov);
			
			String address="";
			try{
				address = pur.getId()==0?"" : pur.getPurokName()+", ";
				address += bar.getId()==0?"" : bar.getName()+", ";
				address += mun.getId()==0?"" : mun.getName()+", ";
				address += prov.getName();
			}catch(Exception e){}
			cus.setCompleteAddress(address);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return cus;
	}
	
	public static void updatePhoto(String sql, String[] params){
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		System.out.println("SQL: " + ps.toString());
		
		ps.execute();
		
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){}
		
	}
	
	public static PoblacionCustomer save(PoblacionCustomer cus){
		if(cus!=null){
			
			long id = PoblacionCustomer.getInfo(cus.getId() ==0? PoblacionCustomer.getLatestId()+1 : cus.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				cus = PoblacionCustomer.insertData(cus, "1");
			}else if(id==2){
				LogU.add("update Data ");
				cus = PoblacionCustomer.updateData(cus);
			}else if(id==3){
				LogU.add("added new Data ");
				cus = PoblacionCustomer.insertData(cus, "3");
			}
			
		}
		return cus;
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
	
	public static PoblacionCustomer insertData(PoblacionCustomer cus, String type){
		String sql = "INSERT INTO poblacioncustomer ("
				+ "customerid,"
				+ "cusfirstname,"
				+ "cusmiddlename,"
				+ "cuslastname,"
				+ "cusgender,"
				+ "cusage,"
				+ "purid,"
				+ "cuscontactno,"
				+ "cusdateregistered,"
				+ "cuscardno,"
				+ "cusisactive,"
				+ "userdtlsid,"
				+ "fullname,"
				+ "bgid,"
				+ "munid,"
				+ "provid,"
				+ "civilstatus,"
				+ "borndate,"
				+ "emeperson,"
				+ "relid,"
				+ "photoid) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table poblacioncustomer");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			cus.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			cus.setId(id);
			LogU.add("id: " + id);
		}
		ps.setString(cnt++, cus.getFirstname());
		ps.setString(cnt++, cus.getMiddlename());
		ps.setString(cnt++, cus.getLastname());
		ps.setString(cnt++, cus.getGender());
		ps.setInt(cnt++, cus.getAge());
		ps.setLong(cnt++, cus.getPurok()==null? 01 : cus.getPurok().getId());
		ps.setString(cnt++, cus.getContactno());
		ps.setString(cnt++, cus.getDateregistered());
		ps.setString(cnt++, cardNumber());
		ps.setInt(cnt++, cus.getIsactive());
		ps.setLong(cnt++, cus.getUserDtls()==null? 0 : (cus.getUserDtls().getUserdtlsid()==0? 0 : cus.getUserDtls().getUserdtlsid()));
		ps.setString(cnt++, cus.getFullname());
		ps.setInt(cnt++, cus.getBarangay()==null? 0 : cus.getBarangay().getId());
		ps.setInt(cnt++, cus.getMunicipality()==null? 0 : cus.getMunicipality().getId());
		ps.setInt(cnt++, cus.getProvince()==null? 0 : cus.getProvince().getId());
		ps.setInt(cnt++, cus.getCivilStatus());
		ps.setString(cnt++, cus.getBirthdate());
		ps.setLong(cnt++, cus.getEmergencyContactPerson()==null? 0l : cus.getEmergencyContactPerson().getId());
		ps.setInt(cnt++, cus.getRelationship());
		ps.setString(cnt++, cus.getPhotoid());
		
		
		LogU.add(cus.getFirstname());
		LogU.add(cus.getMiddlename());
		LogU.add(cus.getLastname());
		LogU.add(cus.getGender());
		LogU.add(cus.getAge());
		LogU.add(cus.getPurok()==null? 01 : cus.getPurok().getId());
		LogU.add(cus.getContactno());
		LogU.add(cus.getDateregistered());
		LogU.add(cus.getCardno());
		LogU.add(cus.getIsactive());
		LogU.add(cus.getUserDtls()==null? 0 : (cus.getUserDtls().getUserdtlsid()==0? 0 : cus.getUserDtls().getUserdtlsid()));
		LogU.add(cus.getFullname());
		LogU.add(cus.getBarangay()==null? 0 : cus.getBarangay().getId());
		LogU.add(cus.getMunicipality()==null? 0 : cus.getMunicipality().getId());
		LogU.add(cus.getProvince()==null? 0 : cus.getProvince().getId());
		LogU.add(cus.getCivilStatus());
		LogU.add(cus.getBirthdate());
		LogU.add(cus.getEmergencyContactPerson()==null? 0l : cus.getEmergencyContactPerson().getId());
		LogU.add(cus.getRelationship());
		LogU.add(cus.getPhotoid());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to poblacioncustomer : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return cus;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO poblacioncustomer ("
				+ "customerid,"
				+ "cusfirstname,"
				+ "cusmiddlename,"
				+ "cuslastname,"
				+ "cusgender,"
				+ "cusage,"
				+ "purid,"
				+ "cuscontactno,"
				+ "cusdateregistered,"
				+ "cuscardno,"
				+ "cusisactive,"
				+ "userdtlsid,"
				+ "fullname,"
				+ "bgid,"
				+ "munid,"
				+ "provid,"
				+ "civilstatus,"
				+ "borndate,"
				+ "emeperson,"
				+ "relid,"
				+ "photoid)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt =1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table poblacioncustomer");
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
		ps.setString(cnt++, getFirstname());
		ps.setString(cnt++, getMiddlename());
		ps.setString(cnt++, getLastname());
		ps.setString(cnt++, getGender());
		ps.setInt(cnt++, getAge());
		ps.setLong(cnt++, getPurok()==null? 01 : getPurok().getId());
		ps.setString(cnt++, getContactno());
		ps.setString(cnt++, getDateregistered());
		ps.setString(cnt++, cardNumber());
		ps.setInt(cnt++, getIsactive());
		ps.setLong(cnt++, getUserDtls()==null? 0 : (getUserDtls().getUserdtlsid()==0? 0 : getUserDtls().getUserdtlsid()));
		ps.setString(cnt++, getFullname());
		ps.setInt(cnt++, getBarangay()==null? 0 : getBarangay().getId());
		ps.setInt(cnt++, getMunicipality()==null? 0 : getMunicipality().getId());
		ps.setInt(cnt++, getProvince()==null? 0 : getProvince().getId());
		ps.setInt(cnt++, getCivilStatus());
		ps.setString(cnt++, getBirthdate());
		ps.setLong(cnt++, getEmergencyContactPerson()==null? 0l : getEmergencyContactPerson().getId());
		ps.setInt(cnt++, getRelationship());
		ps.setString(cnt++, getPhotoid());
		
		
		LogU.add(getFirstname());
		LogU.add(getMiddlename());
		LogU.add(getLastname());
		LogU.add(getGender());
		LogU.add(getAge());
		LogU.add(getPurok()==null? 01 : getPurok().getId());
		LogU.add(getContactno());
		LogU.add(getDateregistered());
		LogU.add(getCardno());
		LogU.add(getIsactive());
		LogU.add(getUserDtls()==null? 0 : (getUserDtls().getUserdtlsid()==0? 0 : getUserDtls().getUserdtlsid()));
		LogU.add(getFullname());
		LogU.add(getBarangay()==null? 0 : getBarangay().getId());
		LogU.add(getMunicipality()==null? 0 : getMunicipality().getId());
		LogU.add(getProvince()==null? 0 : getProvince().getId());
		LogU.add(getCivilStatus());
		LogU.add(getBirthdate());
		LogU.add(getEmergencyContactPerson()==null? 0l : getEmergencyContactPerson().getId());
		LogU.add(getRelationship());
		LogU.add(getPhotoid());
		
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to poblacioncustomer : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static PoblacionCustomer updateData(PoblacionCustomer cus){
		String sql = "UPDATE poblacioncustomer SET "
				+ "cusfirstname=?,"
				+ "cusmiddlename=?,"
				+ "cuslastname=?,"
				+ "cusgender=?,"
				+ "cusage=?,"
				+ "purid=?,"
				+ "cuscontactno=?,"
				+ "cusdateregistered=?,"
				+ "cusisactive=?,"
				+ "userdtlsid=?,"
				+ "fullname=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "civilstatus=?,"
				+ "borndate=?,"
				+ "emeperson=?,"
				+ "relid=?,"
				+ "photoid=? " 
				+ " WHERE customerid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		LogU.add("===========================START=========================");
		LogU.add("updating data into table poblacioncustomer");
		int cnt=1;
		ps.setString(cnt++, cus.getFirstname());
		ps.setString(cnt++, cus.getMiddlename());
		ps.setString(cnt++, cus.getLastname());
		ps.setString(cnt++, cus.getGender());
		ps.setInt(cnt++, cus.getAge());
		ps.setLong(cnt++, cus.getPurok()==null? 01 : cus.getPurok().getId());
		ps.setString(cnt++, cus.getContactno());
		ps.setString(cnt++, cus.getDateregistered());
		ps.setInt(cnt++, cus.getIsactive());
		ps.setLong(cnt++, cus.getUserDtls()==null? 0 : (cus.getUserDtls().getUserdtlsid()==0? 0 : cus.getUserDtls().getUserdtlsid()));
		ps.setString(cnt++, cus.getFullname());
		ps.setInt(cnt++, cus.getBarangay()==null? 0 : cus.getBarangay().getId());
		ps.setInt(cnt++, cus.getMunicipality()==null? 0 : cus.getMunicipality().getId());
		ps.setInt(cnt++, cus.getProvince()==null? 0 : cus.getProvince().getId());
		ps.setInt(cnt++, cus.getCivilStatus());
		ps.setString(cnt++, cus.getBirthdate());
		ps.setLong(cnt++, cus.getEmergencyContactPerson()==null? 0l : cus.getEmergencyContactPerson().getId());
		ps.setInt(cnt++, cus.getRelationship());
		ps.setString(cnt++, cus.getPhotoid());
		ps.setLong(cnt++, cus.getId());
		
		LogU.add(cus.getFirstname());
		LogU.add(cus.getMiddlename());
		LogU.add(cus.getLastname());
		LogU.add(cus.getGender());
		LogU.add(cus.getAge());
		LogU.add(cus.getPurok()==null? 01 : cus.getPurok().getId());
		LogU.add(cus.getContactno());
		LogU.add(cus.getDateregistered());
		LogU.add(cus.getIsactive());
		LogU.add(cus.getUserDtls()==null? 0 : (cus.getUserDtls().getUserdtlsid()==0? 0 : cus.getUserDtls().getUserdtlsid()));
		LogU.add(cus.getFullname());
		LogU.add(cus.getBarangay()==null? 0 : cus.getBarangay().getId());
		LogU.add(cus.getMunicipality()==null? 0 : cus.getMunicipality().getId());
		LogU.add(cus.getProvince()==null? 0 : cus.getProvince().getId());
		LogU.add(cus.getCivilStatus());
		LogU.add(cus.getBirthdate());
		LogU.add(cus.getEmergencyContactPerson()==null? 0l : cus.getEmergencyContactPerson().getId());
		LogU.add(cus.getRelationship());
		LogU.add(cus.getPhotoid());
		LogU.add(cus.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to poblacioncustomer : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return cus;
	}
	
	public void updateData(){
		String sql = "UPDATE poblacioncustomer SET "
				+ "cusfirstname=?,"
				+ "cusmiddlename=?,"
				+ "cuslastname=?,"
				+ "cusgender=?,"
				+ "cusage=?,"
				+ "purid=?,"
				+ "cuscontactno=?,"
				+ "cusdateregistered=?,"
				+ "cusisactive=?,"
				+ "userdtlsid=?,"
				+ "fullname=?,"
				+ "bgid=?,"
				+ "munid=?,"
				+ "provid=?,"
				+ "civilstatus=?,"
				+ "borndate=?,"
				+ "emeperson=?,"
				+ "relid=?,"
				+ "photoid=? " 
				+ " WHERE customerid=?";
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		LogU.add("===========================START=========================");
		LogU.add("updating data into table poblacioncustomer");
		int cnt = 1;
		ps.setString(cnt++, getFirstname());
		ps.setString(cnt++, getMiddlename());
		ps.setString(cnt++, getLastname());
		ps.setString(cnt++, getGender());
		ps.setInt(cnt++, getAge());
		ps.setLong(cnt++, getPurok()==null? 01 : getPurok().getId());
		ps.setString(cnt++, getContactno());
		ps.setString(cnt++, getDateregistered());
		ps.setInt(cnt++, getIsactive());
		ps.setLong(cnt++, getUserDtls()==null? 0 : (getUserDtls().getUserdtlsid()==0? 0 : getUserDtls().getUserdtlsid()));
		ps.setString(cnt++, getFullname());
		ps.setInt(cnt++, getBarangay()==null? 0 : getBarangay().getId());
		ps.setInt(cnt++, getMunicipality()==null? 0 : getMunicipality().getId());
		ps.setInt(cnt++, getProvince()==null? 0 : getProvince().getId());
		ps.setInt(cnt++, getCivilStatus());
		ps.setString(cnt++, getBirthdate());
		ps.setLong(cnt++, getEmergencyContactPerson()==null? 0l : getEmergencyContactPerson().getId());
		ps.setInt(cnt++, getRelationship());
		ps.setString(cnt++, getPhotoid());
		ps.setLong(cnt++, getId());
		
		
		LogU.add(getFirstname());
		LogU.add(getMiddlename());
		LogU.add(getLastname());
		LogU.add(getGender());
		LogU.add(getAge());
		LogU.add(getPurok()==null? 01 : getPurok().getId());
		LogU.add(getContactno());
		LogU.add(getDateregistered());
		LogU.add(getIsactive());
		LogU.add(getUserDtls()==null? 0 : (getUserDtls().getUserdtlsid()==0? 0 : getUserDtls().getUserdtlsid()));
		LogU.add(getFullname());
		LogU.add(getBarangay()==null? 0 : getBarangay().getId());
		LogU.add(getMunicipality()==null? 0 : getMunicipality().getId());
		LogU.add(getProvince()==null? 0 : getProvince().getId());
		LogU.add(getCivilStatus());
		LogU.add(getBirthdate());
		LogU.add(getEmergencyContactPerson()==null? 0l : getEmergencyContactPerson().getId());
		LogU.add(getRelationship());
		LogU.add(getPhotoid());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to poblacioncustomer : " + s.getMessage());
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
		sql="SELECT customerid FROM poblacioncustomer  ORDER BY customerid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("customerid");
		}
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static String getLatestCardNo(){
		String id ="0";
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT cuscardno FROM poblacioncustomer WHERE cusisactive=1 ORDER BY customerid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getString("cuscardno");
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
		ps = conn.prepareStatement("SELECT customerid FROM poblacioncustomer WHERE customerid=?");
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
		String sql = "UPDATE poblacioncustomer set cusisactive=0,userdtlsid="+ getUserDtls().getUserdtlsid() +" WHERE customerid=?";
		
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


