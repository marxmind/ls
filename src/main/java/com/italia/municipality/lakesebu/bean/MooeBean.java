package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

@Named("moe")
@ViewScoped
@Data
public class MooeBean implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 13424657687564354L;
	
	private Mooe selectedData;
	private List offices;
	private int offId;
	private Map<Integer, Offices> officesData;
	private List<Mooe> mooes;
	private String searchParam;
	
	@PostConstruct
	public void init() {
		selectedData = Mooe.builder().tmpDateTrans(new Date()).isActive(1).offices(Offices.builder().id(1).build()).build();
	}
	
	public void showMoe() {
		clear();
		load();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgMooe').show(1000)");
	}
	
	public void load() {
		mooes = new ArrayList<Mooe>();
		String sql = "";
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			sql = " AND (ofs.name like '%"+ getSearchParam() +"%' OR moe.description like '%"+ getSearchParam() +"%' )";
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
			moe.save();
			clear();
			load();
			Application.addMessage(1, "Success", "Successfully saved");
		}
	}
	
	public void delete(Mooe moe) {
		moe.delete();
		load();
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void clear() {
		
		officesData=new LinkedHashMap<Integer, Offices>();
		offId=1;
		offices = new ArrayList<>();
		 for(Offices o : Offices.retrieve(" ORDER BY name", new String[0])) {
			 offices.add(new SelectItem(o.getId(), o.getName()));
		 }
		
		selectedData = Mooe.builder().tmpDateTrans(new Date()).yearBudget(DateUtils.getCurrentYear()).isActive(1).offices(Offices.builder().id(1).build()).build();
	}

}
