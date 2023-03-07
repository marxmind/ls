package com.italia.municipality.lakesebu.licensing.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;

import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.enm.DateFormat;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.BusinessEngaged;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Purok;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Whitelist;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * 
 * @author mark italia
 * @version 1.0
 * @since 07/06/2017
 *
 */

@Named
@ViewScoped
public class BusinessBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1597696543534L;
	
	private Date dateRegistered;
	private Date dateRetired;
	private Livelihood selectedData;
	
	private String businessName = "Business";
	private String purok;
	private String areaMeter;
	private String supportingDetails;
	
	private BusinessCustomer customer;
	private String customerName;
	private List<Livelihood> businesses = new ArrayList<Livelihood>();
	
	private List types;
	private int typeId;
	
	private List status;
	private int statusId;
	
	
	private String searchName;
	
	private String searchTaxPayerName;
	private List<BusinessCustomer> customers = new ArrayList<BusinessCustomer>();
	
	private String searchProvince;
	private String searchMunicipal;
	private String searchBarangay;
	
	private List<Province> provinces = new ArrayList<Province>();
	private List<Municipality> municipals = new ArrayList<Municipality>();
	private List<Barangay> barangays = new ArrayList<Barangay>();
	
	private Province provinceSelected;
	private Municipality municipalSelected;
	private Barangay barangaySelected;
	
	
	private String BARANGAY = "Poblacion";
	private String MUNICIPALITY = "Lake Sebu";
	private String PROVINCE = "South Cotabato";
	
	private long purokId;
	private List purokList;
	private Map<Long, Purok> purokMap = new HashMap<Long, Purok>();
	
	@PostConstruct
	public void init(){
		
		
			
		businesses = new ArrayList<Livelihood>();
		String sql = " AND live.isactivelive=1 AND live.livelihoodtype!=1 AND cuz.cusisactive=1 ";
		
		try{
			String editName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editBusiness");
			System.out.println("Check pass name: " + editName);
			if(editName!=null && !editName.isEmpty() && !"null".equalsIgnoreCase(editName)){
				sql += " AND cuz.fullname='"+editName.split(":")[0]+"'";
				sql += " AND live.livedatereg='" + editName.split(":")[1] +"'";
				setSearchName(null);
			}
			}catch(Exception e){}
		
		String[] params = new String[0];
		if(getSearchName()!=null && !getSearchName().isEmpty()){
			int size = getSearchName().length();
			if(size>=4){
				sql += " AND   ( live.livename like '%" + getSearchName()+"%' OR cuz.fullname like '%"+ getSearchName() + "%') ORDER BY live.livelihoodid DESC ";
			}
		}else{
			sql += " ORDER BY live.livelihoodid DESC limit 10";
		}
		businesses = Livelihood.retrieve(sql, params);
		
		if(businesses!=null && businesses.size()==1){
			clickItem(businesses.get(0));
		}else{
			clearFields();
		}
		
		
	}
	
	public void loadAll(){
		
		
		businesses = Collections.synchronizedList(new ArrayList<Livelihood>());
		String sql = " AND live.isactivelive=1 AND live.livelihoodtype!=1";
		
		if(getPurokId()>0){
			sql += " AND live.livepurok='" + getPurokMap().get(getPurokId()).getPurokName() +"'";
		}
		
		String[] params = new String[0];
		if(getSearchName()!=null && !getSearchName().isEmpty()){
			int size = getSearchName().length();
			if(size>=4){
				sql += " AND   ( live.livename like '%" + getSearchName()+"%' OR cuz.fullname like '%"+ getSearchName() + "%') ORDER BY live.livelihoodid DESC ";
			}
		}else{
			sql += " ORDER BY live.livelihoodid DESC";
		}
		businesses = Livelihood.retrieve(sql, params);
		
		if(businesses!=null && businesses.size()==1){
			clickItem(businesses.get(0));
		}else{
			clearFields();
		}
		
		
	}
	
	private void loadDefaultAddress(){
		try{
		String sql = " AND prv.provisactive=1 AND prv.provname=?";
		String[] params = new String[1];
		params[0] = PROVINCE;
		provinces = Province.retrieve(sql, params);
		setProvinceSelected(provinces.get(0));
		
		params = new String[2];
		sql = " AND mun.munisactive=1 AND prv.provid=? AND mun.munname=?";
		params[0] = getProvinceSelected().getId()+"";
		params[1] = MUNICIPALITY;
		municipals = Municipality.retrieve(sql, params);
		setMunicipalSelected(municipals.get(0));
		
		params = new String[3];
		sql = " AND bgy.bgisactive=1 AND prv.provid=? AND mun.munid=? AND bgy.bgname=?";
		params[0] = getProvinceSelected().getId()+"";
		params[1] = getMunicipalSelected().getId()+"";
		params[2] = BARANGAY;
		barangays = Barangay.retrieve(sql, params);
		setBarangaySelected(barangays.get(0));
		}catch(Exception e){}
	}
	
	public List<String> autoPurokName(String query){
		
		
		List<String> result = new ArrayList<>();
		
		String[] params = new String[2];
		String sql = " AND pur.isactivepurok=1 AND mun.munid=? AND bgy.bgid=? ";
		params[0] = getMunicipalSelected().getId()+"";
		params[1] = getBarangaySelected().getId()+"";
		if(query!=null && !query.isEmpty()){
			sql += " AND pur.purokname like '%"+ query.replace("--", "") +"%'";
		}
		for(Purok p : Purok.retrieve(sql, params)){
			result.add(p.getPurokName());
		}
		
		return result;
	}
	
	public void taxpayerLoad(){
		System.out.println("taxpayerLoad>>> ");
		customers = new ArrayList<BusinessCustomer>();
		
		/*Customer customer = new Customer();
		customer.setIsactive(1);
		if(getSearchTaxPayerName()!=null && !getSearchTaxPayerName().isEmpty()){
			customer.setFullname(Whitelist.remove(getSearchTaxPayerName()));
		}
		customers=   Customer.retrieve(customer);*/
		
		String sql = " AND cus.cusisactive=1 ";
		String[] params = new String[0];
		
		if(getSearchTaxPayerName()!=null && !getSearchTaxPayerName().isEmpty()){
			sql += " AND cus.fullname like '%"+ Whitelist.remove(getSearchTaxPayerName()) +"%'";
		}
		customers = BusinessCustomer.retrieve(sql, params);
		Collections.reverse(customers);
		
		if(customers!=null && customers.size()==1) {
			clickItemOwner(customers.get(0));
			setSearchTaxPayerName("");
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("PF('multiDialogOwner').hide();");
		}
		
	}
	
	public void loadProvince(){
		provinces = Collections.synchronizedList(new ArrayList<Province>());
		String sql = " AND prv.provisactive=1";
		String[] params = new String[0];
		if(getSearchProvince()!=null){
			sql += " AND prv.provname like '%"+ getSearchProvince().replace("--", "") +"%'";
		}
		provinces = Province.retrieve(sql, params);
		Collections.reverse(provinces);
	}
	
	public void loadMunicipal(){
		municipals = Collections.synchronizedList(new ArrayList<Municipality>());
		String[] params = new String[1];
		String sql = " AND mun.munisactive=1 AND prv.provid=?";
		params[0] = getProvinceSelected().getId()+"";
		if(getSearchMunicipal()!=null){
			sql += " AND mun.munname like '%"+ getSearchMunicipal().replace("--", "") +"%'";
		}
		municipals = Municipality.retrieve(sql, params);
		Collections.reverse(municipals);
	}
	
	public void loadBarangay(){
		barangays = Collections.synchronizedList(new ArrayList<Barangay>());
		String[] params = new String[2];
		String sql = " AND bgy.bgisactive=1 AND prv.provid=? AND mun.munid=?";
		params[0] = getProvinceSelected().getId()+"";
		params[1] = getMunicipalSelected().getId()+"";
		if(getSearchBarangay()!=null){
			sql += " AND bgy.bgname like '%"+ getSearchBarangay().replace("--", "") +"%'";
		}
		barangays = Barangay.retrieve(sql, params);
		Collections.reverse(barangays);
	}
	
	public void clickItemPopup(Object obj){
		System.out.println("clickItemPopup");
		if(obj instanceof Province){
			Province prov = (Province)obj;
			setProvinceSelected(prov);
			setMunicipalSelected(null);
			setBarangaySelected(null);
		}else if(obj instanceof Municipality){
			System.out.println("municipality...");
			Municipality mun = (Municipality)obj;
			setMunicipalSelected(mun);
			setBarangaySelected(null);
		}else if(obj instanceof Barangay){
			Barangay bar = (Barangay)obj;
			setBarangaySelected(bar);
		}
		
	}
	
	public void clickItemOwner(BusinessCustomer customer){
		setCustomer(customer);
		setCustomerName(customer.getFullname());
	}
	
	public void saveData(){
		
			boolean isNew = false;
			Livelihood li = new Livelihood();
			if(getSelectedData()!=null){
				li = getSelectedData();
			}else{
				li.setIsActive(1);
				isNew = true;
			}
			
			boolean isOk = true;
			
			if(getCustomer()==null){
				isOk = false;
				Application.addMessage(3, "Please provide Citizen's name", "");
			}
			
			if(getStatusId()==2){
				if(getDateRetired()==null){
					isOk = false;
					Application.addMessage(3, "Please provide retired date", "");
				}
			}
			
			if(getProvinceSelected()==null){
				Application.addMessage(3, "Please provide province.", "");
				isOk= false;
			}
			
			if(getMunicipalSelected()==null){
				Application.addMessage(3, "Please provide municipality.", "");
				isOk= false;
			}
			
			if(getBarangaySelected()==null){
				Application.addMessage(3, "Please provide barangay.", "");
				isOk= false;
			}
			
			if(isNew && Livelihood.isExistingBusiness(getBusinessName().toUpperCase(), getCustomer().getId())) {
				Application.addMessage(3, "Error", "Business name is already exist");
				isOk= false;
			}
			
			if(isOk){
				if(getTypeId()==1){
					li.setBusinessName("Fish Cage");
				}else{
					li.setBusinessName(getBusinessName().toUpperCase().trim());
					li.setAreaMeter(getAreaMeter());
				}

			li.setDateRegistered(DateUtils.convertDate(getDateRegistered(), DateFormat.YYYY_MM_DD()));	
			li.setPurokName(getPurok());
			li.setSupportingDetails(getSupportingDetails());
			li.setTaxPayer(getCustomer());
			li.setStatus(getStatusId());
			li.setTypeLine(getTypeId());
			
			if(getStatusId()==2){
				li.setDateRetired(DateUtils.convertDate(getDateRetired(),DateFormat.YYYY_MM_DD()));
			}
			
			li.setBarangay(getBarangaySelected());
			li.setMunicipality(getMunicipalSelected());
			li.setProvince(getProvinceSelected());
			
			li.setUserDtls(Login.getUserLogin().getUserDtls());
			
			li = Livelihood.save(li);
			
			setSelectedData(li);
			init();
			Application.addMessage(1, "Successfully saved.", "");
			}
			
		
		
	}
	
	public void clickItem(Livelihood li){
		setSelectedData(li);
		setDateRegistered(DateUtils.convertDateString(li.getDateRegistered(), DateFormat.YYYY_MM_DD()));
		setDateRetired(DateUtils.convertDateString(li.getDateRetired(), DateFormat.YYYY_MM_DD()));
		setBusinessName(li.getBusinessName());
		setPurok(li.getPurokName());
		setAreaMeter(li.getAreaMeter());
		setSupportingDetails(li.getSupportingDetails());
		setTypeId(li.getTypeLine());
		setStatusId(li.getStatus());
		
		/*setBarangayId(li.getBarangay().getId());
		setMunicipalityId(li.getMunicipality().getId());
		setProvinceId(li.getProvince().getId());*/
		setProvinceSelected(li.getProvince());
		setMunicipalSelected(li.getMunicipality());
		setBarangaySelected(li.getBarangay());
		
		setCustomer(li.getTaxPayer());
		setCustomerName(li.getTaxPayer().getFullname());
	}
	
	public void deleteRow(Livelihood li){
		if(Login.checkUserStatus()){
			li.delete();
			clearFields();
			init();
			Application.addMessage(1, "successfully removed business.","");
		}
	}
	
	public void clearFields(){
		setSearchName(null);
		setCustomerName(null);
		setCustomer(null);
		setDateRegistered(null);
		setDateRetired(null);
		setSelectedData(null);
		setTypeId(2);
		setStatusId(1);
		
		setBusinessName(null);
		setPurok(null);
		setAreaMeter(Livelihood.getNewPlateNo());
		setSupportingDetails(null);
		
		setProvinceSelected(null);
		setMunicipalSelected(null);
		setBarangaySelected(null);
		
		setSearchProvince(null);
		setSearchMunicipal(null);
		setSearchBarangay(null);
		
		loadDefaultAddress();
		
	}
	
	public Date getDateRegistered() {
		if(dateRegistered==null){
			dateRegistered = DateUtils.getDateToday();
		}
		return dateRegistered;
	}
	public void setDateRegistered(Date dateRegistered) {
		this.dateRegistered = dateRegistered;
	}
	public Date getDateRetired() {
		return dateRetired;
	}
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getPurok() {
		return purok;
	}
	public void setPurok(String purok) {
		this.purok = purok;
	}
	public String getAreaMeter() {
		
		if(areaMeter==null) {
			areaMeter = Livelihood.getNewPlateNo();
		}
		
		return areaMeter;
	}
	public void setAreaMeter(String areaMeter) {
		this.areaMeter = areaMeter;
	}
	public String getSupportingDetails() {
		return supportingDetails;
	}
	public void setSupportingDetails(String supportingDetails) {
		this.supportingDetails = supportingDetails;
	}
	public BusinessCustomer getCustomer() {
		return customer;
	}
	public void setCustomer(BusinessCustomer customer) {
		this.customer = customer;
	}
	public List getTypes() {
		types = new ArrayList<>();
		
		for(BusinessEngaged line : BusinessEngaged.readBusinessEngagedXML()){
			types.add(new SelectItem(line.getId(), line.getName()));
		}
		
		
		return types;
	}
	public void setTypes(List types) {
		this.types = types;
	}
	public int getTypeId() {
		if(typeId==0){
			typeId = 2;
		}
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public List getStatus() {
		status = new ArrayList<>();
		status.add(new SelectItem(1, "ACTIVE"));
		status.add(new SelectItem(2, "CLOSED"));
		return status;
	}
	public void setStatus(List status) {
		this.status = status;
	}
	public int getStatusId() {
		if(statusId==0){
			statusId = 1;
		}
		return statusId;
	}
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
	
	public String getSearchName() {
		return searchName;
	}
	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}
	
	public Livelihood getSelectedData() {
		return selectedData;
	}
	public void setSelectedData(Livelihood selectedData) {
		this.selectedData = selectedData;
	}


	public List<Livelihood> getBusinesses() {
		return businesses;
	}


	public void setBusinesses(List<Livelihood> businesses) {
		this.businesses = businesses;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getSearchTaxPayerName() {
		return searchTaxPayerName;
	}

	public void setSearchTaxPayerName(String searchTaxPayerName) {
		this.searchTaxPayerName = searchTaxPayerName;
	}

	public List<BusinessCustomer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<BusinessCustomer> customers) {
		this.customers = customers;
	}

	public String getSearchProvince() {
		return searchProvince;
	}

	public void setSearchProvince(String searchProvince) {
		this.searchProvince = searchProvince;
	}

	public String getSearchMunicipal() {
		return searchMunicipal;
	}

	public void setSearchMunicipal(String searchMunicipal) {
		this.searchMunicipal = searchMunicipal;
	}

	public String getSearchBarangay() {
		return searchBarangay;
	}

	public void setSearchBarangay(String searchBarangay) {
		this.searchBarangay = searchBarangay;
	}

	public List<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces = provinces;
	}

	public List<Municipality> getMunicipals() {
		return municipals;
	}

	public void setMunicipals(List<Municipality> municipals) {
		this.municipals = municipals;
	}

	public List<Barangay> getBarangays() {
		return barangays;
	}

	public void setBarangays(List<Barangay> barangays) {
		this.barangays = barangays;
	}

	public Province getProvinceSelected() {
		return provinceSelected;
	}

	public void setProvinceSelected(Province provinceSelected) {
		this.provinceSelected = provinceSelected;
	}

	public Municipality getMunicipalSelected() {
		return municipalSelected;
	}

	public void setMunicipalSelected(Municipality municipalSelected) {
		this.municipalSelected = municipalSelected;
	}

	public Barangay getBarangaySelected() {
		return barangaySelected;
	}

	public void setBarangaySelected(Barangay barangaySelected) {
		this.barangaySelected = barangaySelected;
	}

	public long getPurokId() {
		return purokId;
	}

	public void setPurokId(long purokId) {
		this.purokId = purokId;
	}

	public List getPurokList() {
		
		String[] params = new String[2];
		String sql = " AND pur.isactivepurok=1 AND mun.munid=? AND bgy.bgid=? ";
		params[0] = getMunicipalSelected().getId()+"";
		params[1] = getBarangaySelected().getId()+"";
		purokMap = Collections.synchronizedMap(new HashMap<Long, Purok>());
		purokList = Collections.synchronizedList(new ArrayList<Purok>());
		purokList.add(new SelectItem(0, "All Purok"));
		for(Purok p : Purok.retrieve(sql, params)){
			purokList.add(new SelectItem(p.getId(), p.getPurokName()));
			purokMap.put(p.getId(), p);
		}
		
		return purokList;
	}

	public void setPurokList(List purokList) {
		this.purokList = purokList;
	}

	public Map<Long, Purok> getPurokMap() {
		return purokMap;
	}

	public void setPurokMap(Map<Long, Purok> purokMap) {
		this.purokMap = purokMap;
	}
	
}