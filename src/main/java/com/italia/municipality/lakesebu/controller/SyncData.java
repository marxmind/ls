package com.italia.municipality.lakesebu.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.CashBookConnect;
import com.italia.municipality.lakesebu.database.Conf;
import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.CheckServerConnection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SyncData {

	private int id;
	
	public static void main(String[] args) {
		/*
		 * String val = ReadConfig.value(AppConf.SERVER_LOCAL); HttpSession session =
		 * SessionBean.getSession(); session.setAttribute("server-local", val);
		 * System.out.println("assigning local: " + val);
		 */
		//if(SyncData.checkingConnection()) {
			//SyncData.downloadDataFromServer();
		//}
		//SyncData.loadSource();
		//SyncData.loadServerData();
	}
	
	public static boolean checkingConnection() {
		String val = ReadConfig.value(AppConf.SERVER_LOCAL);
		if("true".equalsIgnoreCase(val)) {//server is local if true
			Conf conf = Conf.getInstance();
			if(CheckServerConnection.pingIp(conf.getServerDatabaseIp())) {//check if ip reachable else assigned localhost
				//check if server is accessible
				System.out.println("server ip " + conf.getServerDatabaseIp() + " is accessible...");
				return true;
			}else {
				System.out.println("server " + conf.getServerDatabaseIp() + " is not accessible...");
				return false;
			}	
			
		}
		return false;
	}
	
	public static void downloadDataFromServer() {
		
		Conf conf = Conf.getInstance();	
		String DB_PATH = conf.getDatabaseHomePath();
		String UNAME = conf.getDatabaseUserName();
		String PWORD = conf.getDatabasePassword();
		String SERVER_HOST = conf.getServerDatabaseIp();
		String WEBTIS = conf.getDatabaseMain();
		String TAXATION = conf.getDatabaseLand();
		String CHEQUE = conf.getDatabaseBank();
		String CASHBOOK = conf.getDatabaseCashBook();
		String local_path = GlobalVar.DOWNLOADED_DATA_FOLDER;
		System.out.println("check path " + local_path + "\n and db path is " + DB_PATH);
		File dir = new File(local_path);	
		
		dir.mkdir();//create directory if not present
		String[] dbName = new String[4];
		String bat = "cd " + DB_PATH + "\n";
		dbName[0] = bat + "mysqldump.exe -e -u"+UNAME+" -p"+PWORD+" -h"+ SERVER_HOST +" " +WEBTIS+" > "+local_path+ WEBTIS +".sql\n";	
		dbName[1] = bat + "mysqldump.exe -e -u"+UNAME+" -p"+PWORD+" -h"+ SERVER_HOST +" " +TAXATION+" > "+local_path+ TAXATION +".sql\n";	
		dbName[2] = bat + "mysqldump.exe -e -u"+UNAME+" -p"+PWORD+" -h"+ SERVER_HOST +" " +CHEQUE+" > "+local_path+ CHEQUE +".sql\n";	
		dbName[3] = bat + "mysqldump.exe -e -u"+UNAME+" -p"+PWORD+" -h"+ SERVER_HOST +" " +CASHBOOK+" > "+local_path+ CASHBOOK +".sql\n";	
		
	try {
		
		String[] db = {WEBTIS,TAXATION,CHEQUE,CASHBOOK};
		int i = 0;
		for(String dName : db) {
			File file_db = new File(local_path +  dName +".bat");
			PrintWriter pw = new PrintWriter(new FileWriter(file_db));
			pw.println(dbName[i++]);
			pw.flush();
			pw.close();
		    
			
			processBatFile(local_path, dName+".bat");
			
			
		}
		
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	    
	
	}
	
	
	public static void loadServerData() {
		Conf conf = Conf.getInstance();
		String DB_PATH = conf.getDatabaseHomePath();
		String UNAME = conf.getDatabaseUserName();
		String PWORD = conf.getDatabasePassword();
		String WEBTIS = conf.getDatabaseMain();
		String TAXATION = conf.getDatabaseLand();
		String CHEQUE = conf.getDatabaseBank();
		String CASHBOOK = conf.getDatabaseCashBook();
		String local_path = GlobalVar.DOWNLOADED_DATA_FOLDER;
		String local_path_copy = GlobalVar.COMPLETED_DATA_FOLDER;
		
		
		copyFileUsingStream(local_path + WEBTIS+".sql", local_path_copy + WEBTIS+".sql");
		copyFileUsingStream(local_path + TAXATION+".sql", local_path_copy + TAXATION+".sql");
		copyFileUsingStream(local_path + CHEQUE+".sql", local_path_copy + CHEQUE+".sql");
		copyFileUsingStream(local_path + CASHBOOK+".sql", local_path_copy + CASHBOOK+".sql");
		
		if(loadToDatabase(WEBTIS, UNAME, PWORD, local_path, DB_PATH)) {
			//Application.addMessage(1, "Success", "The webtis table has been successfully updated");
			System.out.println("The webtis table has been successfully updated");
			deletingFile(local_path + WEBTIS + ".sql");//delete file after uploading
			deletingFile(local_path + WEBTIS + ".bat");
			deletingFile(local_path + WEBTIS + "_uploadData.bat");
		}
		if(loadToDatabase(TAXATION, UNAME, PWORD, local_path, DB_PATH)) {
			//Application.addMessage(1, "Success", "The taxation table has been successfully updated");
			System.out.println("The taxation table has been successfully updated");
			deletingFile(local_path + TAXATION + ".sql");//delete file after uploading
			deletingFile(local_path + TAXATION + ".bat");
			deletingFile(local_path + TAXATION + "_uploadData.bat");
		}
		if(loadToDatabase(CHEQUE, UNAME, PWORD, local_path, DB_PATH)) {
			//Application.addMessage(1, "Success", "The bank_cheque table has been successfully updated");
			System.out.println("The bank_cheque table has been successfully updated");
			deletingFile(local_path + CHEQUE + ".sql");//delete file after uploading
			deletingFile(local_path + CHEQUE + ".bat");
			deletingFile(local_path + CHEQUE + "_uploadData.bat");
		}
		if(loadToDatabase(CASHBOOK, UNAME, PWORD, local_path, DB_PATH)) {
			//Application.addMessage(1, "Success", "The cashbook table has been successfully updated");
			System.out.println("The cashbook table has been successfully updated");
			deletingFile(local_path + CASHBOOK + ".sql");//delete file after uploading
			deletingFile(local_path + CASHBOOK + ".bat");
			deletingFile(local_path + CASHBOOK + "_uploadData.bat");
		}
	}
	
	private static boolean loadToDatabase(String dbName, String uname, String pword, String local_path, String db_path){
		boolean isSuccess = false;
		try{
			File dir = new File(local_path);
			dir.mkdir();
			
			File fileUp = new File(local_path + dbName + "_uploadData.bat");
			String bat = "cd " + db_path + "\n" + 
	    		"mysql -u"+uname+" -p"+pword+" -e \"use "+dbName + ";source "+ local_path.replace("\\", "/") +dbName+".sql;\"";
	    PrintWriter pw = new PrintWriter(new FileWriter(fileUp));
	    pw.println(bat);
        pw.flush();
        pw.close();
	    
        isSuccess = processBatFile(local_path, dbName + "_uploadData.bat");
        
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
		return isSuccess;
		
	}
	
	private static boolean processBatFile(String location, String batName) {
		ProcessBuilder processBuilder = new ProcessBuilder(location + batName);
		boolean isSuccess = false;
		try {

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println(output);
                //System.exit(0);
                isSuccess = true;
                //File fileOrig = new File(location + batName);
                //File fileCopy = new File(GlobalVar.COMPLETED_DATA_FOLDER + batName);
                //isSuccess = copyFileUsingStream(fileOrig, fileCopy);
            } else {
                //abnormal...
            	System.out.println("abnormal running of bat file... the file " + batName + " was not successfully processed...");
            	isSuccess = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
		return isSuccess;
		
	}
	
	private static boolean copyFileUsingStream(String source, String dest) {
		File fileOrg = new File(source);
		File fileCopy = new File(dest);
		return copyFileUsingStream(fileOrg, fileCopy);
	}
	
	private static boolean copyFileUsingStream(File source, File dest) {
		File file = new File(GlobalVar.COMPLETED_DATA_FOLDER);
		file.mkdir();
		System.out.println("copying file to new location....");
	    InputStream is = null;
	    OutputStream os = null;
	    boolean iscopied = false;
	    try {
		    try {
		        is = new FileInputStream(source);
		        os = new FileOutputStream(dest);
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = is.read(buffer)) > 0) {
		            os.write(buffer, 0, length);
		        }
		    } finally {
		        is.close();
		        os.close();
		        iscopied = true;
		        System.out.println("successfully copied the file to new location....");
		    }
	    
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	    
	    return iscopied;
	}
	
	private static boolean deletingFile(String fileCompletePathName) {
		File file = new File(fileCompletePathName);
		if(file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}
	
	@Deprecated
	public static boolean loadSource() {
		Conf conf = Conf.getInstance();
		String local_path = GlobalVar.DOWNLOADED_DATA_FOLDER;
		String WEBTIS = conf.getDatabaseMain();
		String TAXATION = conf.getDatabaseLand();
		String CHEQUE = conf.getDatabaseBank();
		String CASHBOOK = conf.getDatabaseCashBook();
		
		
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("source " + local_path + WEBTIS + ".sql");
		System.out.println("source " + local_path + WEBTIS + ".sql");
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		conn = null;
		ps = null;
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement("source " + local_path + TAXATION + ".sql");
		System.out.println("source " + local_path + TAXATION + ".sql");
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		
		conn = null;
		ps = null;
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("source " + local_path + CHEQUE + ".sql");
		System.out.println("source " + local_path + CHEQUE + ".sql");
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		
		conn = null;
		ps = null;
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement("source " + local_path + CASHBOOK + ".sql");
		System.out.println("source " + local_path + CASHBOOK + ".sql");
		ps.executeUpdate();
		ps.close();
		CashBookConnect.close(conn);
		
		return true;
		}catch(Exception e){e.getMessage();}
		return false;
	}
	
	
}
