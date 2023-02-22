package com.italia.municipality.lakesebu.database;

import java.sql.Connection;
import java.sql.DriverManager;

import com.italia.municipality.lakesebu.bean.SessionBean;

import jakarta.servlet.http.HttpSession;

/**
 * 
 * @author mark italia
 * @since 11/18/2016
 * @version 1.0
 *
 */
public class TaxDatabaseConnect {

	public static Connection getConnection(){
		Conf conf = Conf.getInstance();
		Connection conn = null;
		try{
			HttpSession session = SessionBean.getSession();
			 String val = session.getAttribute("server-local").toString();//if value is true meaning local server is using
			String driver = conf.getDatabaseDriver();//ReadConfig.value(AppConf.DB_DRIVER);
			       //driver = SecureChar.decode(driver);
			Class.forName(driver);
			String db_url = conf.getDatabaseUrl();// ReadConfig.value(AppConf.DB_URL);
				   //db_url = SecureChar.decode(db_url);
			if("false".equalsIgnoreCase(val)) {//meaning changing to remote server
				db_url = conf.getDatabaseUrlServer();
			}
			String port = conf.getDatabasePort(); //ReadConfig.value(AppConf.DB_PORT);
			       //port = SecureChar.decode(port);
			String dbName = conf.getDatabaseLand();//ReadConfig.value(AppConf.DB_AGRICULTURE);
				   //dbName = SecureChar.decode(dbName);
			String timezone = "";
			if(conf.getDatabaseTimeZone()!=null && !conf.getDatabaseTimeZone().isEmpty()) {
				timezone = conf.getDatabaseTimeZone() +"&";
			}
			String url = db_url + ":" + port + "/" + dbName + "?"+ timezone +  conf.getDatabaseSSL();//ReadConfig.value(AppConf.DB_SSL);
			//System.out.println("URL DATA: " + url);
			String u_name = conf.getDatabaseUserName();
			String pword = conf.getDatabasePassword();
			conn = DriverManager.getConnection(url, u_name, pword);
			return conn;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static void close(Connection conn){
		try{
			if(conn!=null){
				conn.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		BankChequeDatabaseConnect dt = new BankChequeDatabaseConnect();
		Connection s = dt.getConnection();
		
	}
}
