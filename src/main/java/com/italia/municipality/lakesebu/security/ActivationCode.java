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
 * @since 01/24/2017
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ActivationCode {

	private long id;
	private String moduleName;
	private String codeName;
	private String monthExpiration;
	private String activationCode;
	private Timestamp timestamp;
	
	public static List<ActivationCode> retrieve(String sql, String params[]){
		List<ActivationCode> lis = new ArrayList<ActivationCode>();
		
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
			ActivationCode li = new ActivationCode();
			li.setId(rs.getLong("cid"));
			li.setModuleName(rs.getString("modulename"));
			li.setCodeName(rs.getString("codename"));
			li.setMonthExpiration(rs.getString("monthexp"));
			li.setActivationCode(rs.getString("activationcode"));
			li.setTimestamp(rs.getTimestamp("timestamp"));
			lis.add(li);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){}
		
		return lis;
	}
}
