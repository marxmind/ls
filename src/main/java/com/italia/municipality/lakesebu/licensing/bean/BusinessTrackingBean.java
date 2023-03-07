package com.italia.municipality.lakesebu.licensing.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.licensing.controller.BusinessTracking;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
/**
 * 
 * @author Mark Italia
 * @since 06/28/2022
 * @version 1.0
 *
 */
@Named
@ViewScoped
@Data
public class BusinessTrackingBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 32456354567751L;
	private List<BusinessTracking> tracks;
	private String searchName;
	private List<BusinessTracking> tempData;
	private int type;
	private List types;
	private Map<String, Livelihood> businessData;
	
	@PostConstruct
	public void init() {
		businessData = Livelihood.collecBusinessExist();
		types = new ArrayList<>();
		types.add(new SelectItem(0, "ALL"));
		types.add(new SelectItem(1, "RENEWED"));
		types.add(new SelectItem(2, "FOR RENEWAL"));
		types.add(new SelectItem(3, "CHANGED"));
		types.add(new SelectItem(4, "DELETED"));
		collectBusiness();
	}
	
	public void collectBusiness() {
		Map<String,Map<Integer, BusinessPermit>> permits = new LinkedHashMap<String, Map<Integer, BusinessPermit>>();
		Map<Integer, BusinessPermit> business = new LinkedHashMap<Integer, BusinessPermit>();
		

		for(BusinessPermit p : BusinessPermit.retrieve(" AND bz.isactivebusiness=1 ORDER BY bz.year, bz.businessplateno", new String[0])) {
			int year= Integer.valueOf(p.getYear().trim());
			String plateNo=p.getPlateNo().trim();
			
			if(permits!=null) {
				if(permits.containsKey(plateNo)) {
					permits.get(plateNo).put(year, p);
				}else {
					business = new LinkedHashMap<Integer, BusinessPermit>();
					business.put(year,p);
					permits.put(plateNo, business);
				}
			}else {
				business.put(year,p);
				permits.put(plateNo, business);
			}
		}
		
		int currentYear = DateUtils.getCurrentYear();
		
		tracks = new ArrayList<BusinessTracking>();
		int number = 1;
		for(String plateNo : permits.keySet()) {
			
			BusinessTracking tr = new BusinessTracking();
			tr.setPlateNo(plateNo);
			boolean foundBusinessForRenew = false;
			BusinessCustomer bz = new BusinessCustomer();
			for(BusinessPermit p : permits.get(plateNo).values()) {
				int year = Integer.valueOf(p.getYear());
				tr.setBusinessName(p.getBusinessName()); 
				tr.setOwner(p.getCustomer().getFullname());
				bz = p.getCustomer();
				int len = p.getBusinessAddress().length();
				if(len>30) {tr.setLocation(p.getBusinessAddress().substring(0,29)+"...");}else {tr.setLocation(p.getBusinessAddress());}
				
				String color="";
				
				if("NEW".equalsIgnoreCase(p.getType())) { color="color: green; font-weight: bold"; }
				
				if(currentYear==year) {foundBusinessForRenew=true;}
				
				switch(year) {
					case 2019 : tr.setYear1(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle1(color); break;
					case 2020 : tr.setYear2(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle2(color); break;
					case 2021 : tr.setYear3(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle3(color); break;
					case 2022 : tr.setYear4(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle4(color); break;
					case 2023 : tr.setYear5(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle5(color); break;
					case 2024 : tr.setYear6(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle6(color); break;
					case 2025 : tr.setYear7(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle7(color); break;
					case 2026 : tr.setYear8(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle8(color); break;
					case 2027 : tr.setYear9(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle9(color); break;
					case 2028 : tr.setYear10(p.getType() + "(" + p.getMemoType() + ")"); tr.setStyle10(color); break;
				}
				
			}
			
			if(!foundBusinessForRenew) {
				
				
				Livelihood biz = getBusinessData().get(tr.getBusinessName());
				if(biz!=null) {
					if(biz.getIsActive()==1 && biz.getDateRetired()!=null) {//business retired
						tr.setStyle("color: blue; font-weight: bold");
						tr.setStatus("RETIRED");
					}else if(biz.getIsActive()==0) {//business deleted
						tr.setStyle("color: yellow; font-weight: bold");
						tr.setStatus("DELETED");
					}else if(biz.getIsActive()==1 && biz.getDateRetired()==null) {//business exist
						tr.setStyle("color: red; font-weight: bold");
						tr.setStatus("RENEWAL");
					}
				}else {
					tr.setStyle("color: orange; font-weight: bold");//change name
					tr.setStatus("CHANGED");
				}
				
			}else {
				tr.setStatus("RENEWED");
			}
			tr.setNumber(number++);
			tracks.add(tr);
		}
		
		tempData = tracks;
		//tracks.stream().forEachOrdered( e -> System.out.println(e));

		//tracks.stream().forEach( e  ->  System.out.println(e));
		
	}

	public void search() {
		String msg = "";
		if(getSearchName()!=null && !getSearchName().isEmpty()) {
			
		tracks = new ArrayList<BusinessTracking>();
			int number = 1;
			for(BusinessTracking t : getTempData()) {
				String name = getSearchName().toUpperCase();
				if(t.getBusinessName().contains(name)  || t.getOwner().contains(name) || isExist(t,name)) {
					//t.setNumber(number++);
					//tracks.add(t);
					if(getType()==1) {//renewed
						if(isExist(t,getSearchName()) && "RENEWED".equalsIgnoreCase(t.getStatus())) {
							t.setNumber(number++);
							tracks.add(t);
							msg = "RENEWED";
						}
					}else if(getType()==2) {//not renewed
						if(!isExist(t,getSearchName()) && "RENEWAL".equalsIgnoreCase(t.getStatus())) {
							t.setNumber(number++);
							tracks.add(t);
							msg = "NOT RENEWED";
						}
					}else if(getType()==3) {//change
						if(!isExist(t,getSearchName()) && "CHANGED".equalsIgnoreCase(t.getStatus())) {
							t.setNumber(number++);
							tracks.add(t);
							msg = "CHANGE";
						}
					}else if(getType()==4) {//deleted
						if(!isExist(t,getSearchName()) && "DELETED".equalsIgnoreCase(t.getStatus())) {
							t.setNumber(number++);
							tracks.add(t);
							msg = "DELETED";
						}	
					}else {
						t.setNumber(number++);
						tracks.add(t);
					}
				}
			}
		}else {
			tracks = tempData;
		}
		
		if(getType()>0) {
			if(tracks!=null && tracks.size()>0) {Application.addMessage(1, "Info", "There are total of " + tracks.size() + " for " + msg +" business.");}
		}
	}
	
	public void searchType() {
		tracks = new ArrayList<BusinessTracking>();
		int number = 1;
		
		if(getSearchName()==null || getSearchName().isEmpty()) {Application.addMessage(3, "Error", "Please input year in the search box");}
		String msg = "";
		for(BusinessTracking t : getTempData()) {
			if(getType()==1) {//renewed
				if(isExist(t,getSearchName()) && "RENEWED".equalsIgnoreCase(t.getStatus())) {
					t.setNumber(number++);
					tracks.add(t);
					msg = "RENEWED";
				}
			}else if(getType()==2) {//not renewed
				if(!isExist(t,getSearchName()) && "RENEWAL".equalsIgnoreCase(t.getStatus())) {
					t.setNumber(number++);
					tracks.add(t);
					msg = "NOT RENEWED";
				}
			}else if(getType()==3) {//change
				if(!isExist(t,getSearchName()) && "CHANGED".equalsIgnoreCase(t.getStatus())) {
					t.setNumber(number++);
					tracks.add(t);
					msg = "CHANGE";
				}
			}else if(getType()==4) {//deleted
				if(!isExist(t,getSearchName()) && "DELETED".equalsIgnoreCase(t.getStatus())) {
					t.setNumber(number++);
					tracks.add(t);
					msg = "DELETED";
				}	
			}else {
				tracks = tempData;
			}
		}
		
		if(getType()>0) {
			if(tracks!=null && tracks.size()>0) {Application.addMessage(1, "Info", "There are total of " + tracks.size() + " for " + msg + " business.");}
		}
	}
	
	private boolean isExist(BusinessTracking t, String name) {
		
		int year = 0;
		try{year = Integer.valueOf(name);}catch(NumberFormatException num) {}
		
		switch(year) {
			case 2019 : if(t.getYear1()!=null) { return true; }  break;
			case 2020 : if(t.getYear2()!=null) { return true; }  break;
			case 2021 : if(t.getYear3()!=null) { return true; }  break;
			case 2022 : if(t.getYear4()!=null) { return true; }  break;
			case 2023 : if(t.getYear5()!=null) { return true; }  break;
			case 2024 : if(t.getYear6()!=null) { return true; }  break;
			case 2025 : if(t.getYear7()!=null) { return true; }  break;
			case 2026 : if(t.getYear8()!=null) { return true; }  break;
			case 2027 : if(t.getYear9()!=null) { return true; }  break;
			case 2028 : if(t.getYear10()!=null) { return true; }  break;
		}
		
		return false;
	}
	
}

