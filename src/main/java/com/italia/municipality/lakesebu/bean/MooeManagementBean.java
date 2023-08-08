package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.PrimeFaces;

import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.reports.OfficeBudget;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

@Named("budbdgt")
@ViewScoped
@Data
public class MooeManagementBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 143435465756454L;
	
	
	private long officeHisId;
	private List officesHis;
	private int yearHisId;
	private List yearsHIs;
	private List<OfficeBudget> budgetsHis;
	
	
	///mooe
	private Mooe selectedData;
	private List offices;
	private Map<Long, Offices> officesData;
	private List<Mooe> mooes;
	private String searchParam;
	
	@PostConstruct
	public void init() {
		
		selectedData = Mooe.builder().tmpDateTrans(new Date()).isActive(1).offices(Offices.builder().id(1).build()).build();
		
		officeHisId=0;
		int year=DateUtils.getCurrentYear();//current year
		yearHisId=year;
		yearsHIs = new ArrayList<>();
		
		for(int y=2016; y<=year; y++) {
			yearsHIs.add(new SelectItem(y, y+""));
		}
		
		clear();
		load();
	}
	
	/////////////////////////////////////////////////MOOE//////////////////////////////
	
	public void load() {
		mooes = new ArrayList<Mooe>();
		String sql = "";
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			sql = " AND (ofs.name like '%"+ getSearchParam() +"%' OR moe.description like '%"+ getSearchParam() +"%' OR moe.code like '%"+ getSearchParam() +"%' )";
		}else {
			sql = " AND moe.yearbudget= " + DateUtils.getCurrentYear();
		}
		mooes = Mooe.retrieve(sql, new String[0]);
	}
	
	public void clickItem(Mooe moe) {
		moe.setTmpDateTrans(DateUtils.convertDateString(moe.getDateTrans(), "yyyy-MM-dd"));
		setSelectedData(moe);
	}
	
	public void save() {
		boolean isOk = true;
		
		if(getSelectedData()!=null && getSelectedData().getCode().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide code");
		}
		if(getSelectedData()!=null && getSelectedData().getDescription().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide description");
		}
		
		if(isOk) {
			Mooe moe = getSelectedData();
			moe.setDateTrans(DateUtils.convertDate(moe.getTmpDateTrans(), "yyyy-MM-dd"));
			Offices office = getOfficesData().get(moe.getOffices().getId());
			moe =  Mooe.save(moe);
			selectedData = Mooe.builder().tmpDateTrans(new Date()).yearBudget(DateUtils.getCurrentYear()).isActive(1).offices(office).build();
			//load();
			if(mooes!=null && mooes.size()==1) {
				mooes=new ArrayList<Mooe>();
			}	
			moe.setOffices(office);
			mooes.add(moe);
			Collections.reverse(mooes);
			
			Application.addMessage(1, "Success", "Successfully saved");
		}
	}
	
	public void delete(Mooe moe) {
		moe.delete();
		load();
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void clear() {
		
		officesData=new LinkedHashMap<Long, Offices>();
		officesHis = new ArrayList<>();
		offices = new ArrayList<>();
		 for(Offices o : Offices.retrieve(" ORDER BY name", new String[0])) {
			 offices.add(new SelectItem(o.getId(), o.getName()));
			 officesHis.add(new SelectItem(o.getId(), o.getCode() + "-" + o.getName()));
			 officesData.put(o.getId(), o);
		 }
		
		selectedData = Mooe.builder().tmpDateTrans(new Date()).yearBudget(DateUtils.getCurrentYear()).isActive(1).offices(Offices.builder().id(1).build()).build();
	}
	
	
	////////////////////////////////////////END MOOE//////////////////////////////////
	
	
	public void loadMooeForOfficeHis() {
		budgetsHis = new ArrayList<OfficeBudget>();
		List<Mooe> moes = Mooe.retrieve(" AND ofs.offid="+getOfficeHisId() + " AND moe.yearbudget=" + getYearHisId(), new String[0]);
		for(Mooe mo : moes) {
			double usedAmount = Chequedtls.mooeUsed(mo.getOffices().getId(), mo.getId(), getYearHisId());
			String budget=Currency.formatAmount(mo.getBudgetAmount());
			String usedAm=Currency.formatAmount(usedAmount);
			double remaining = mo.getBudgetAmount() - usedAmount;
			OfficeBudget bud = OfficeBudget.builder()
					.mooeCode(mo.getCode())
					.officeName(mo.getOffices().getName())
					.mooeName(mo.getDescription())
					.mooeBudget(budget)
					.usedBudget(usedAm)
					.remainingBudget(Currency.formatAmount(remaining))
					.build();
			budgetsHis.add(bud);
		}
		
	}
	
}
