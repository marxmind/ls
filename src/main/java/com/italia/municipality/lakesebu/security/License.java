package com.italia.municipality.lakesebu.security;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.utils.DateUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 01/24/2017
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class License {
	
	private long id;
	private String moduleName;
	private String codeName;
	private String monthExpiration;
	private String activationCode;
	private int isActive;
	private Timestamp timestamp;
	
	private static final String APPLICATION_FILE = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_LICENSE_FILE_NAME.getValue();
	
	public static List<License> retrieve(String sql, String params[]){
		List<License> lis = new ArrayList<License>();
		
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
			License li = new License();
			li.setId(rs.getLong("lid"));
			li.setModuleName(rs.getString("modulename"));
			li.setCodeName(rs.getString("codename"));
			li.setMonthExpiration(rs.getString("monthexp"));
			li.setActivationCode(rs.getString("activationcode"));
			li.setIsActive(rs.getInt("isactivated"));
			li.setTimestamp(rs.getTimestamp("timestamp"));
			lis.add(li);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){}
		
		return lis;
	}
	
	public void update(){
		
		String sql = "UPDATE license SET " +
				"codename=?," +
				"monthexp=?,"
				+ "activationcode=?,"
				+ "isactivated=?"
				+ " WHERE lid=?";
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		ps.setString(1, getCodeName());
		ps.setString(2, getMonthExpiration());
		ps.setString(3, getActivationCode());
		ps.setInt(4, getIsActive());
		ps.setLong(5, getId());
		
		ps.execute();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){
		}
		
	}
	
	
	public static boolean isActivated(AppModule module){
		
		String sql = "SELECT * FROM license where modulename=? AND isactivated=1";
		String[] params = new String[1];
		params[0] = module.getName();
		License lic = null;
		
		try{lic = License.retrieve(sql, params).get(0);}catch(Exception e){}
		
		if(lic==null){
			return false; //expired
		}
		
		return true;
	}
	
	public static boolean activateLicenseCode(AppModule module, String activationCode){
		
		String sql = "SELECT * FROM activationcode where modulename=? AND activationcode=?";
		String[] params = new String[2]; 
		params[0] = module.getName();
		params[1] = activationCode;
		
		ActivationCode code = null;
		try{code = ActivationCode.retrieve(sql, params).get(0);}catch(Exception e){}
		
		if(code!=null){
			System.out.println("Code accepted....");
			System.out.println("updating license..");
			
			sql = "SELECT * FROM license WHERE modulename=?";
			params = new String[1];
			params[0] = module.getName();
			License lic = null; 
			try{
			
			lic = License.retrieve(sql, params).get(0);
			lic.setIsActive(1);
			lic.setMonthExpiration(code.getMonthExpiration());
			lic.setCodeName(code.getCodeName());
			lic.setActivationCode(code.getActivationCode());
			lic.update();
			
			char[] month = lic.getMonthExpiration().split("-")[0].toCharArray();
			int m1 = Integer.valueOf(month[0]+"");
			int m2 = Integer.valueOf(month[1]+"");
			
			char[] day = lic.getMonthExpiration().split("-")[1].toCharArray();
			int d1 = Integer.valueOf(day[0]+"");
			int d2 = Integer.valueOf(day[1]+"");
			
			
			char[] year = lic.getMonthExpiration().split("-")[2].toCharArray();
			int y1 = Integer.valueOf(year[0]+"");
			int y2 = Integer.valueOf(year[1]+"");
			int y3 = Integer.valueOf(year[2]+"");
			int y4 = Integer.valueOf(year[3]+"");
			
			String decodedDate = months()[m1]+months()[m2] +"-"+ days()[d1]+days()[d2] +"-"+ years()[y1]+years()[y2]+years()[y3]+years()[y4]; 
			
			//update xml license file
			updateLicense(module, decodedDate);
			
			}catch(Exception e){}
			
			return true;
		}
		
		return false;
	}
	
	private static void updateLicense(AppModule moduleName, String licenseKey){
		
		try {
			File xmlFile = new File(APPLICATION_FILE);
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node node = document.selectSingleNode("/license/module/"+moduleName.getName());
			
			node.setText(licenseKey);
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileWriter(xmlFile),format);
			writer.write(document);
			writer.close();
			
			}catch(Exception e) {}
		
		
	}
	
	public static String licenseFile(AppModule module){
		String result = "";
		
		File xmlFile = new File(APPLICATION_FILE);
		if(xmlFile.exists()){
			try {
			
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node node = document.selectSingleNode("/module");
			result = node.selectSingleNode(module.getName()).getText();
			
			}catch(DocumentException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	private static String xmlLicense(AppModule module){
		return licenseFile(module);
	}
	public static String dbLicense(AppModule module){
		System.out.println("dbLicense...");
		String sql = "SELECT * FROM license WHERE modulename=? AND isactivated=1";
		String[] params = new String[1];
		params[0] = module.getName();
		License lic = null;
		
		try{
			lic = License.retrieve(sql, params).get(0);
			return lic.getMonthExpiration();
		}catch(Exception e){}
		
		return null;
	}
	
	/**
	 * 
	 * @return true if expired
	 */
	public static boolean checkLicenseExpiration(AppModule module){
		
		String dblicense = dbLicense(module);
		
		if(dblicense==null) return true;
		
		char[] month = dblicense.split("-")[0].toCharArray();
		int m1 = Integer.valueOf(month[0]+"");
		int m2 = Integer.valueOf(month[1]+"");
		
		char[] day = dblicense.split("-")[1].toCharArray();
		int d1 = Integer.valueOf(day[0]+"");
		int d2 = Integer.valueOf(day[1]+"");
		
		
		char[] year = dblicense.split("-")[2].toCharArray();
		int y1 = Integer.valueOf(year[0]+"");
		int y2 = Integer.valueOf(year[1]+"");
		int y3 = Integer.valueOf(year[2]+"");
		int y4 = Integer.valueOf(year[3]+"");
		
		String chkVal = months()[m1]+months()[m2] +"-"+ days()[d1]+days()[d2] +"-"+ years()[y1]+years()[y2]+years()[y3]+years()[y4]; 
		
		//System.out.println("xml : " + xmlLicense(module) );
		//System.out.println("dblicense : " + dblicense + " converted " + chkVal);
		
		if(xmlLicense(module).equalsIgnoreCase(chkVal)){
			//System.out.println("xml and database equal...");
			//System.out.println("checking current date...");
			return checkdate(dblicense);
			
		}else{
			System.out.println("Application expired");
		}
		
		return true;
		
	}
	
	/**
	 * 
	 * @return true if expired
	 */
	private static boolean checkdate(String dbLicense){
		
		
		String systemDate = DateUtils.getCurrentDateMMDDYYYY();
		
		SimpleDateFormat dFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		try{
		Date dbDate = dFormat.parse(dbLicense);	
		Date sysDate = dFormat.parse(systemDate);
		
		if(dbDate.compareTo(sysDate)>0){
			System.out.println("Not expired");
		}else if(dbDate.compareTo(sysDate)<0){
			System.out.println("Expired...");
			return true;
		}else if(dbDate.compareTo(sysDate)==0){
			System.out.println("Expired...");
			return true;
		}else{
			System.out.println("Expired...");
			return true;
		}
		
		}catch(ParseException pre){}
		
		
		
		return false;
	}
	
	private static String[] days(){
		char[] addChar = "markitalia".toCharArray();
		String[] days = new String[10];
		for(int i=0; i<=9;i++){
			days[i] = "0"+i+addChar[i];
		}
		return days;
	}
	
	private static String[] months(){
		char[] addChar = "mritaliamark".toCharArray();
		String[] months = new String[12];
		for(int i=0; i<12;i++){
			months[i] = "0" + (i+1) + addChar[i];
		}
		return months;
	}
	
	private static String[] years(){
		char[] addChar = "markitalia".toCharArray();
		String[] years = new String[12];
		for(int i=0; i<=9;i++){
			years[i] = "0" + i + addChar[i];
		}
		return years;
	}
	
	
}
