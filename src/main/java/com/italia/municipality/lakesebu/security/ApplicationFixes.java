package com.italia.municipality.lakesebu.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 09/28/2016
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ApplicationFixes {

	private Long fixid;
	private String fixdesc;
	private ApplicationVersionController versionController;
	private Timestamp timestamp;
	
	public static List<ApplicationFixes> retrieve(String sql, String[] params){
		List<ApplicationFixes> data = new ArrayList<>();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn =  WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			ApplicationFixes app = new ApplicationFixes();
			try{app.setFixid(rs.getLong("fixid"));}catch(NullPointerException e){}
			try{app.setFixdesc(rs.getString("fixdesc"));}catch(NullPointerException e){}
			try{
				ApplicationVersionController versionController = new ApplicationVersionController();
				versionController.setBuildid(rs.getLong("buildid"));
				app.setVersionController(versionController);}catch(NullPointerException e){}
			try{app.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			data.add(app);
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){}
		return data;
	}
	
}
