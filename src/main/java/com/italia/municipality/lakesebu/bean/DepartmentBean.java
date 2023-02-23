package com.italia.municipality.lakesebu.bean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.utils.Application;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 *@since 08/16/2019
 *
 */
@Named
@ViewScoped
public class DepartmentBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 546789876756431L;
	
	@Setter @Getter private String name;
	@Setter @Getter private String code;
	@Setter @Getter private String searchName;
	@Setter @Getter private String departmentHead;
	@Setter @Getter private Department depData;
	@Setter @Getter private String abrevation;
	@Setter @Getter private List<Department> deps;
	
	@PostConstruct
	public void init() {
		deps = new ArrayList<Department>();
		String[] params = new String[0];
		String sql = "SELECT * FROM department WHERE isactivedep=1 ";
		
		if(getSearchName()!=null) {
			sql += " AND (departmentname LIKE '%"+ getSearchName() +"%' OR ";
			sql += " code LIKE '%"+ getSearchName() +"%' OR ";
			sql += " abr LIKE '%"+ getSearchName() +"%')";
		}
		
		sql += " ORDER BY departmentid DESC LIMIT 10";
		
		deps = Department.retrieve(sql, params);
		if(deps!=null && deps.size()==1) {
			clickItem(deps.get(0));
		}
		
	}
	
	public void saveDep() {
		Department dep = new Department();
		boolean isOk = true;
		if(getDepData()!=null) {
			dep = getDepData();
		}
		
		if(getName()==null) {
			isOk=false;
			Application.addMessage(3, "Error", "Please provide name");
		}
		if(getCode()==null) {
			isOk=false;
			Application.addMessage(3, "Error", "Please provide code");
		}
		if(getDepartmentHead()==null) {
			isOk=false;
			Application.addMessage(3, "Error", "Please provide department head");
		}
		
		if(getAbrevation()==null) {
			isOk=false;
			Application.addMessage(3, "Error", "Please provide code name");
		}
		
		
		if(isOk) {
			dep.setIsActive(1);
			dep.setDepartmentName(getName());
			dep.setCode(getCode());
			dep.setDepartmentHead(getDepartmentHead());
			dep.setAbrevation(getAbrevation());
			dep.save();
			init();
			clear();
			Application.addMessage(1, "Success", "Successfully saved");
		}
		
		
	}
	
	public void clickItem(Department dep) {
		setDepData(dep);
		setName(dep.getDepartmentName());
		setCode(dep.getCode());
		setDepartmentHead(dep.getDepartmentHead());
		setAbrevation(dep.getAbrevation());
	}
	
	public void deleteRow(Department dep) {
		dep.delete();
		clear();
		init();
	}
	
	public void clear() {
		setDepData(null);
		setName(null);
		setCode(null);
		setDepartmentHead(null);
		setAbrevation(null);
	}
	
}