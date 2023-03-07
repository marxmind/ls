package com.italia.municipality.lakesebu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;

public class AgricultureConnect {
	public static String getDBName() {
		return ReadConfig.value(AppConf.DB_AGRICULTURE);
	}
	
	public static Connection getConnection(){
		Conf conf = Conf.getInstance();
		Connection conn = null;
		try{
			
			String driver = conf.getDatabaseDriver();//ReadConfig.value(AppConf.DB_DRIVER);
			       //driver = SecureChar.decode(driver);
			Class.forName(driver);
			String db_url = conf.getDatabaseUrl();// ReadConfig.value(AppConf.DB_URL);
				   //db_url = SecureChar.decode(db_url);
			String port = conf.getDatabasePort(); //ReadConfig.value(AppConf.DB_PORT);
			       //port = SecureChar.decode(port);
			String dbName = conf.getDatabaseAgriculture();//ReadConfig.value(AppConf.DB_AGRICULTURE);
				   //dbName = SecureChar.decode(dbName);
			String timezone = "";
			if(conf.getDatabaseTimeZone()!=null && !conf.getDatabaseTimeZone().isEmpty()) {
				timezone = conf.getDatabaseTimeZone() +"&";
			}
			String url = db_url + ":" + port + "/" + dbName + "?"+ timezone +  conf.getDatabaseSSL();//ReadConfig.value(AppConf.DB_SSL);
			System.out.println("URL DATA: " + url);
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
}
