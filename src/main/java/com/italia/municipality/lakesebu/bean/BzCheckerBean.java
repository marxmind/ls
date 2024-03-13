package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

@Named("bzchk")
@ViewScoped
@Data
public class BzCheckerBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 43264354684751L;
	
	private List<BusinessPermit> rpts;
	private int year;
	
	@PostConstruct
	public void init() {
		year = DateUtils.getCurrentYear();
		load();
	}
	
	public void load() {
		rpts = new ArrayList<BusinessPermit>();
		Map<String, BusinessPermit> mapData = new LinkedHashMap<String, BusinessPermit>();
		int yearNow = getYear();
		int prevYear = yearNow - 1;
		
		List<BusinessPermit> ps = BusinessPermit.retrieve(" AND YEAR(bz.datetrans)="+prevYear + " ORDER BY bz.businessname", new String[0]);
		for(BusinessPermit p : ps) {
			/*if(mapData!=null && mapData.size()>0) {
				if(!mapData.containsKey(p.getBusinessName())) {
					mapData.put(p.getBusinessName(), p);
				}
			}else {*/
				mapData.put(p.getBusinessName(), p);
			//}
		}
		 ps = BusinessPermit.retrieve(" AND YEAR(bz.datetrans)="+yearNow + " ORDER BY bz.businessname", new String[0]);
		
		for(BusinessPermit s : ps) {
			if(mapData!=null && mapData.size()>0) {
				if(mapData.containsKey(s.getBusinessName())) {
					mapData.remove(s.getBusinessName());//removing renewed business
				}
			}
		}
		
		for(BusinessPermit bz : mapData.values()) {
			rpts.add(bz);
		}
		
	}
	
}
