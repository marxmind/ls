package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.security.ApplicationFixes;
import com.italia.municipality.lakesebu.security.ApplicationVersionController;
import com.italia.municipality.lakesebu.security.Copyright;
import com.italia.municipality.lakesebu.security.License;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author mark italia
 * @since 11/16/2016
 * @version 1.0
 */

@Named
@ViewScoped
@Data
public class VersionBean implements Serializable{

private static final long serialVersionUID = 1394801825228386363L;
	
	private ApplicationVersionController versionController;
	private ApplicationFixes applicationFixes;
	private List<ApplicationFixes> fixes = new ArrayList<ApplicationFixes>();
	private Copyright copyright;
	private List<License> licenses;
	
	@PostConstruct
	public void init(){
		 
		String sql = "SELECT * FROM app_version_control ORDER BY timestamp DESC LIMIT 1";
		String[] params = new String[0];
		versionController = ApplicationVersionController.retrieve(sql, params).get(0);
		
		sql = "SELECT * FROM copyright ORDER BY id desc limit 1";
		params = new String[0];
		copyright = Copyright.retrieve(sql, params).get(0);
		
		try{fixes = new ArrayList<ApplicationFixes>();}catch(Exception e){}
		sql = "SELECT * FROM buildfixes WHERE buildid=?";
		params = new String[1];
		params[0] = versionController.getBuildid()+"";
		try{fixes = ApplicationFixes.retrieve(sql, params);}catch(Exception e){}
		
		sql = "SELECT * FROM license";
		licenses = new ArrayList<License>();
		licenses = License.retrieve(sql, new String[0]);
	}
}
