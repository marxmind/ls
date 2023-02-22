package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @since 09/27/2016
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Job {

	private int jobid;
	private String jobname;
	private Timestamp timestamp;
	
	public static List<Job> retrieve(String sql, String[] params){
		List<Job> jobs = new ArrayList<Job>();
		
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
			Job job = Job.builder()
					.jobid(rs.getInt("jobtitleid"))
					.jobname(rs.getString("jobname"))
					.build();
			jobs.add(job);
		}
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){}
		
		return jobs;
	}
	
	public static Job job(String jobid){
		Job job = new Job();
		String sql = "SELECT * FROM jobtitle WHERE jobtitleid=?";
		String[] params = new String[1];
		params[0] = jobid;
		try{
			job = Job.retrieve(sql, params).get(0);
		}catch(Exception e){}
		return job;
	}
	
	
}