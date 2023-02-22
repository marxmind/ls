package com.italia.municipality.lakesebu.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.utils.LogU;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Email {

	private long id;
	private String sendDate;
	private String readDate;
	private String title;
	private String contendId;
	private int type;
	private int isOpen;
	private int isDeleted;
	private String fromEmail;
	private String toEmail;
	private int isActive;
	private long personCopy;
	private String timestamp;
	
	private String fromNames;
	private String toNames;
	private String contentMsg;
	
	private String fromNamesCut;
	private String toNamesCut;
	private String titleCut;
	
	//--msgtype 1-inbox 2-outbox 3-send 4-draft 5-trash
	
	private static final String EMAIL_FOLDER = AppConf.PRIMARY_DRIVE.getValue() + File.separator + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + File.separator + "email" + File.separator;
	
	public static void deleteEmail(String fileName) {
		try {
			File file = new File(EMAIL_FOLDER + fileName);
			
			if(file.exists()) {
				file.delete();
			}
		}catch(Exception e) {}
		
	}
	
	public static void deleteEmailWeb(String fileName) {
		try {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String contextFileLoc = File.separator + "attachment" + File.separator;
	        String attachmentFile = externalContext.getRealPath("") + contextFileLoc;
			
	        File file = new File(attachmentFile + fileName);
	        if(file.exists()) {
				file.delete();
			}
		}catch(Exception e) {}
	}
	
	public static void saveAttachment(String fileName, String msg, String ext) {
		
		File file = new File(EMAIL_FOLDER);
		
		if(!file.isDirectory()) {
			file.mkdir();
		}
		
		try {
		
		File email = new File(EMAIL_FOLDER + fileName + "." + ext);
		PrintWriter pw = new PrintWriter(new FileWriter(email));
		pw.println(msg);
		pw.flush();
		pw.close();
		
		
		}catch(Exception e) {}
	}
	
	public static void replyEmailSavePath(String oldMsgContent, String oldFileName, String msg, String fileName) {
		
		
		File file = new File(EMAIL_FOLDER);
		
		if(!file.isDirectory()) {
			file.mkdir();
		}
		
		try {
			
		BufferedReader br = new BufferedReader(new FileReader(EMAIL_FOLDER + oldFileName + ".tis"));	
		
		File email = new File(EMAIL_FOLDER + fileName + ".tis");
		PrintWriter pw = new PrintWriter(new FileWriter(email));
		
		pw.println(msg);
		//include old email
		pw.println("<br/>");
		pw.println("<hr style=\"color: black;height:2px;\"/>");
		pw.println("<br/>");
		pw.println(oldMsgContent);
		pw.println("<br/>");
		
		String line = null;
        // Read from the original file and write to the new
        while ((line = br.readLine()) != null) {
            pw.println(line);
        }
        pw.flush();
		pw.close();
		br.close();
		
		
		}catch(Exception e) {}
	}
	
	public static void emailSavePath(String msg, String fileName) {
		
		File file = new File(EMAIL_FOLDER);
		
		if(!file.isDirectory()) {
			file.mkdir();
		}
		
		try {
		
		File email = new File(EMAIL_FOLDER + fileName + ".tis");
		PrintWriter pw = new PrintWriter(new FileWriter(email));
		pw.println(msg);
		pw.flush();
		pw.close();
		
		
		}catch(Exception e) {}
	}
	
	public static String readEmail(String fileName) {
		
		String msg="";
		try {
		 File email = new File(EMAIL_FOLDER + fileName + ".tis");
		 BufferedReader br = new BufferedReader(new FileReader(email));
		 String line = null;
	        // Read from the original file and write to the new
	        while ((line = br.readLine()) != null) {
	        	msg += line;
	        }
	     br.close();
		}catch(Exception e) {}
		return msg;
	}
	
	public static List<Email> retrieve(String sqlAdd, String[] params){
		List<Email> mails = new ArrayList<>();
		
		
		String sql = "SELECT * FROM emsg  WHERE ismsgactive=1 ";
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
			
			Email mail = new Email();
			try{mail.setId(rs.getLong("msgid"));}catch(NullPointerException e){}
			try{mail.setSendDate(rs.getString("msgsenddate"));}catch(NullPointerException e){}
			try{mail.setReadDate(rs.getString("msgreaddate"));}catch(NullPointerException e){}
			try{mail.setTitle(rs.getString("msgtitle"));}catch(NullPointerException e){}
			try{mail.setContendId(rs.getString("msgcontentid"));}catch(NullPointerException e){}
			try{mail.setType(rs.getInt("msgtype"));}catch(NullPointerException e){}
			try{mail.setIsOpen(rs.getInt("isopen"));}catch(NullPointerException e){}
			try{mail.setIsDeleted(rs.getInt("isdeleted"));}catch(NullPointerException e){}
			try{mail.setIsActive(rs.getInt("ismsgactive"));}catch(NullPointerException e){}
			try{mail.setFromEmail(rs.getString("msgfromempid"));}catch(NullPointerException e){}
			try{mail.setToEmail(rs.getString("msgtoempid"));}catch(NullPointerException e){}
			
			try{mail.setPersonCopy(rs.getLong("personcpy"));}catch(NullPointerException e){}
			
			try{ mail.setTimestamp(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a").format(rs.getTimestamp("timestampmsg")));}catch(NullPointerException e){}
			
			mails.add(mail);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return mails;
	}
	
	public static int countNewEmail(String sqlAdd, String[] params) {
		String sql = "SELECT count(*) as count FROM emsg  WHERE ismsgactive=1 ";
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
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return rs.getInt("count");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return 0;
	}
	
	public static Email save(Email mail){
		if(mail!=null){
			
			long id = Email.getInfo(mail.getId() ==0? Email.getLatestId()+1 : mail.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				mail = Email.insertData(mail, "1");
			}else if(id==2){
				LogU.add("update Data ");
				mail = Email.updateData(mail);
			}else if(id==3){
				LogU.add("added new Data ");
				mail = Email.insertData(mail, "3");
			}
			
		}
		return mail;
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
	
	public static Email insertData(Email mail, String type){
		String sql = "INSERT INTO emsg ("
				+ "msgid,"
				+ "msgsenddate,"
				+ "msgreaddate,"
				+ "msgtitle,"
				+ "msgcontentid,"
				+ "msgtype,"
				+ "isopen,"
				+ "isdeleted,"
				+ "ismsgactive,"
				+ "msgtoempid,"
				+ "msgfromempid,"
				+ "personcpy)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table email");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			mail.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			mail.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, mail.getSendDate());
		ps.setString(cnt++, mail.getReadDate());
		ps.setString(cnt++, mail.getTitle());
		ps.setString(cnt++, mail.getContendId());
		ps.setInt(cnt++, mail.getType());
		ps.setInt(cnt++, mail.getIsOpen());
		ps.setInt(cnt++, mail.getIsDeleted());
		ps.setInt(cnt++, mail.getIsActive());
		ps.setString(cnt++, mail.getToEmail());
		ps.setString(cnt++, mail.getFromEmail());
		ps.setLong(cnt++, mail.getPersonCopy());
		
		LogU.add(mail.getSendDate());
		LogU.add(mail.getReadDate());
		LogU.add(mail.getTitle());
		LogU.add(mail.getContendId());
		LogU.add(mail.getType());
		LogU.add(mail.getIsOpen());
		LogU.add(mail.getIsDeleted());
		LogU.add(mail.getIsActive());
		LogU.add(mail.getToEmail());
		LogU.add(mail.getFromEmail());
		LogU.add(mail.getPersonCopy());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to email : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return mail;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO emsg ("
				+ "msgid,"
				+ "msgsenddate,"
				+ "msgreaddate,"
				+ "msgtitle,"
				+ "msgcontentid,"
				+ "msgtype,"
				+ "isopen,"
				+ "isdeleted,"
				+ "ismsgactive,"
				+ "msgtoempid,"
				+ "msgfromempid,"
				+ "personcpy)" 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table email");
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
		
		ps.setString(cnt++, getSendDate());
		ps.setString(cnt++, getReadDate());
		ps.setString(cnt++, getTitle());
		ps.setString(cnt++, getContendId());
		ps.setInt(cnt++, getType());
		ps.setInt(cnt++, getIsOpen());
		ps.setInt(cnt++, getIsDeleted());
		ps.setInt(cnt++, getIsActive());
		ps.setString(cnt++, getToEmail());
		ps.setString(cnt++, getFromEmail());
		ps.setLong(cnt++, getPersonCopy());
		
		LogU.add(getSendDate());
		LogU.add(getReadDate());
		LogU.add(getTitle());
		LogU.add(getContendId());
		LogU.add(getType());
		LogU.add(getIsOpen());
		LogU.add(getIsDeleted());
		LogU.add(getIsActive());
		LogU.add(getToEmail());
		LogU.add(getFromEmail());
		LogU.add(getPersonCopy());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to email : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		
	}
	
	public static Email updateData(Email mail){
		String sql = "UPDATE emsg SET "
				+ "msgsenddate=?,"
				+ "msgreaddate=?,"
				+ "msgtitle=?,"
				+ "msgcontentid=?,"
				+ "msgtype=?,"
				+ "isopen=?,"
				+ "isdeleted=?,"
				+ "msgtoempid=?,"
				+ "msgfromempid=?,"
				+ "personcpy=?" 
				+ " WHERE msgid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table email");
		
		ps.setString(cnt++, mail.getSendDate());
		ps.setString(cnt++, mail.getReadDate());
		ps.setString(cnt++, mail.getTitle());
		ps.setString(cnt++, mail.getContendId());
		ps.setInt(cnt++, mail.getType());
		ps.setInt(cnt++, mail.getIsOpen());
		ps.setInt(cnt++, mail.getIsDeleted());
		ps.setString(cnt++, mail.getToEmail());
		ps.setString(cnt++, mail.getFromEmail());
		ps.setLong(cnt++, mail.getPersonCopy());
		ps.setLong(cnt++, mail.getId());
		
		LogU.add(mail.getSendDate());
		LogU.add(mail.getReadDate());
		LogU.add(mail.getTitle());
		LogU.add(mail.getContendId());
		LogU.add(mail.getType());
		LogU.add(mail.getIsOpen());
		LogU.add(mail.getIsDeleted());
		LogU.add(mail.getToEmail());
		LogU.add(mail.getFromEmail());
		LogU.add(mail.getPersonCopy());
		LogU.add(mail.getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to email : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return mail;
	}
	
	public void updateData(){
		String sql = "UPDATE emsg SET "
				+ "msgsenddate=?,"
				+ "msgreaddate=?,"
				+ "msgtitle=?,"
				+ "msgcontentid=?,"
				+ "msgtype=?,"
				+ "isopen=?,"
				+ "isdeleted=?,"
				+ "msgtoempid=?,"
				+ "msgfromempid=?,"
				+ "personcpy=?" 
				+ " WHERE msgid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table email");
		
		ps.setString(cnt++, getSendDate());
		ps.setString(cnt++, getReadDate());
		ps.setString(cnt++, getTitle());
		ps.setString(cnt++, getContendId());
		ps.setInt(cnt++, getType());
		ps.setInt(cnt++, getIsOpen());
		ps.setInt(cnt++, getIsDeleted());
		ps.setString(cnt++, getToEmail());
		ps.setString(cnt++, getFromEmail());
		ps.setLong(cnt++, getPersonCopy());
		ps.setLong(cnt++, getId());
		
		LogU.add(getSendDate());
		LogU.add(getReadDate());
		LogU.add(getTitle());
		LogU.add(getContendId());
		LogU.add(getType());
		LogU.add(getIsOpen());
		LogU.add(getIsDeleted());
		LogU.add(getToEmail());
		LogU.add(getFromEmail());
		LogU.add(getPersonCopy());
		LogU.add(getId());
		
		LogU.add("executing for saving...");
		ps.executeUpdate();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to email : " + s.getMessage());
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
		sql="SELECT msgid FROM emsg  ORDER BY msgid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("msgid");
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
		ps = conn.prepareStatement("SELECT msgid FROM emsg WHERE msgid=?");
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
		String sql = "UPDATE emsg set ismsgactive=0 WHERE msgid=?";
		
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
