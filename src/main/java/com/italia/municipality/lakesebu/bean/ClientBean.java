package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;
import com.italia.municipality.lakesebu.controller.Client;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.NationalIDJsonData;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.ClientStatus;
import com.italia.municipality.lakesebu.enm.ClientTransactionType;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

@Data
@Named("client")
@ViewScoped
public class ClientBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 57665875454545441L;
	
	private Map<String, Customer> mapCustomer;
	private Map<String, Livelihood> mapCustomer2;
	private String payorName;
	private Client client;
	private List civils;
	private List genders;
	private List transTypes;
	private List status;
	private boolean include;
	private boolean skip;
	private List professions;
	private String professionName;
	private Map<String, String> mapProfession;
	private boolean businessRequest;
	private String searchPlaceholder;
	
	private String searchName;
	private List<Client> clients;
	private String queYear;
	private String queMonth;
	private String queDay;
	private String queSequence;
	
	@PostConstruct
	public void init() {
		
		int mo = DateUtils.getCurrentMonth();
		String month = mo <10? "0"+mo : mo+"";
		String year = DateUtils.getCurrentYear()+"";
		int dy = DateUtils.getCurrentDay();
		String day = dy<10? "0"+dy : dy+"";
		setQueYear(year);
		setQueMonth(month);
		setQueDay(day);
		
		setSearchPlaceholder("Search Last, First Middle Client Full Name");
		String val = ReadConfig.value(AppConf.SERVER_LOCAL);
        HttpSession session = SessionBean.getSession();
		session.setAttribute("server-local", val);
		session.setAttribute("theme", "arya");
		System.out.println("assigning local: " + val);
		loadDefault();
		loadProfession();
	}
	
	public void onIdle() {
       Application.addMessage(1, "Monitor", "Monitor is in idle");
       PrimeFaces pf = PrimeFaces.current();
       clear();
       pf.executeScript("PF('wizId').loadStep('transaction', false);displayWizard()");
    }
	
	public void loadProfession() {
		professions = new ArrayList<>();
		mapProfession = new LinkedHashMap<String, String>();
		String[] types = Words.getTagName("profession-list").split(",");
		for(String prof : types) {
			String[] vals = prof.split("-");
			professions.add(new SelectItem(vals[0], vals[1]));
			mapProfession.put(vals[0], vals[1]);
		}
	}
	
	private void loadDefault() {
		businessRequest = false;
		include = false;
		skip = false;
		payorName = null;
		mapCustomer = new LinkedHashMap<String, Customer>();
		
		client = Client.builder()
				.dateTransTmp(new Date())
				.birthdayTmp(new Date())
				.address("Lake Sebu")
				.profession("1")
				.gender(1)
				.civilStatus(1)
				.isActive(1)
				.build();
		
		civils = new ArrayList<>();
		for(CivilStatus c : CivilStatus.values()) {
			civils.add(new SelectItem(c.getId(), c.getName()));
		}
		status = new ArrayList<>();
		for(ClientStatus s : ClientStatus.values()) {
			status.add(new SelectItem(s.getId(), s.getName()));
		}
		
		genders = new ArrayList<>();
		genders.add(new SelectItem(1, "Male"));
		genders.add(new SelectItem(2, "Female"));
		
		transTypes = new ArrayList<>();
		for(ClientTransactionType c : ClientTransactionType.values()) {
			transTypes.add(new SelectItem(c.getId(), c.getName()));
		}
	}
	
	public void clear() {
		loadDefault();
	}
	
	public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String onFlowProcess(FlowEvent event) {
    	
    	setInclude(client.getTransType()==0? true : false);
    	
        if (skip) {
            skip = false; //reset in case user goes back
            supplyAdditionalDetails();
            return "confirm";
        }
        else {
        	//System.out.println("Step Old: " + event.getOldStep());
        	//System.out.println("Step New: " + event.getNewStep());
        	
        	//System.out.println("check is include: " + isInclude());
        	supplyAdditionalDetails();
            return event.getNewStep();
        }
    }
    
   
    
    public void supplyAdditionalDetails() {
    	client = getClient();
    	if(isInclude()) {
    		if(client.getBirthPlace()==null || client.getBirthPlace().isEmpty()) {
    			client.setBirthPlace("Lake Sebu");
    		}
    		if(client.getNationality()==null || client.getNationality().isEmpty()) {
    			client.setNationality("Filipino");
    		}
    		setProfessionName(getMapProfession().get(client.getProfession()));
    	}else{
    		client.setBirthPlace("");
    		client.setNationality("");
    		client.setProfession("");
    	}
    	client.setBirthday(DateUtils.convertDate(client.getBirthdayTmp(), "yyyy-MM-dd"));
        client.setTransactionName(ClientTransactionType.nameId(client.getTransType()));
        client.setGenderName(client.getGender()==1? "Male":"Female");
        client.setCivilStatusName(CivilStatus.typeName(client.getCivilStatus()));
        
    }
    
    public void checkTransaction() {
    	
    	if(client.getTransType()==ClientTransactionType.BUSINESS_NEW.getId() 
    			|| client.getTransType()==ClientTransactionType.BUSINESS_RENEWAL.getId() 
    			|| client.getTransType()==ClientTransactionType.QUARTERLY_BUSINESS_PAYMENT.getId()) {
    		setBusinessRequest(true);
    		client.setBusinessName(null);
    		setPayorName(null);
    		setSearchPlaceholder("Search Busines Name or Owner Name");
    	}else if(client.getTransType()==ClientTransactionType.FISHCAGE_NEW.getId() || client.getTransType()==ClientTransactionType.FISHCAGE_RENEW.getId()) {
    		setBusinessRequest(true);
    		client.setBusinessName(null);
    		setPayorName(null);
    		setSearchPlaceholder("Search Fish Cage Owner or Tenant");
    	}else {
    		client.setRemarks(null);
        	client.setBusinessName(null);
        	setPayorName(null);
        	setBusinessRequest(false);
        	setSearchPlaceholder("Search Last, First Middle Client Full Name");
    	}
    }
    
    public void save() {
    	if(getClient()!=null) {
    		Client c = getClient();
    		boolean isOk = true;
    		if(c.getFirstName()==null || c.getFirstName().isEmpty()) {
    			isOk = false;
    			Application.addMessage(3, "Error", "Please provide firstname");
    		}
    		if(c.getMiddleName()==null || c.getMiddleName().isEmpty()) {
    			isOk = false;
    			Application.addMessage(3, "Error", "Please provide middlename");
    		}
    		if(c.getLastName()==null || c.getLastName().isEmpty()) {
    			isOk = false;
    			Application.addMessage(3, "Error", "Please provide lastname");
    		}
    		
    		if(c.getAddress()==null || c.getAddress().isEmpty()) {
    			isOk = false;
    			Application.addMessage(3, "Error", "Please provide address");
    		}
    		//if(c.getContactNumber()==null || c.getContactNumber().isEmpty()) {
    			//isOk = false;
    			//Application.addMessage(3, "Error", "Please provide contact no");
    		//}
    		
    		if(isBusinessRequest()) {
    			if(client.getBusinessName()==null || client.getBusinessName().isEmpty()) {
    				isOk = false;
        			Application.addMessage(3, "Error", "Please provide businesss name");
    			}
    		}else {
    			if(c.getBirthday()==null || c.getBirthday().isEmpty()) {
        			isOk = false;
        			Application.addMessage(3, "Error", "Please provide birthday");
        		}
    		}
    		
    		if(c.getTransType()==0) {//individual cedula
    			if(c.getBirthPlace()==null || c.getBirthPlace().isBlank()) {
    				isOk = false;
        			Application.addMessage(3, "Error", "Please provide birth place");
    			}
    			if(!client.getProfession().equalsIgnoreCase("0")) {
	    			if(c.getMonthlySalary()==0) {
	    				isOk = false;
	        			Application.addMessage(3, "Error", "Please provide daily/monthly income or salaraly");
	    			}
    			}
    		}
    		
    		if(isOk) {
    			c.setDateTrans(DateUtils.getCurrentDateYYYYMMDD());
    			c.setTransNo(Client.getLatestTransactionNo());
    			client = Client.save(c);
    			client.setBirthday(DateUtils.convertDate(client.getBirthdayTmp(), "yyyy-MM-dd"));
    	        client.setTransactionName(ClientTransactionType.nameId(client.getTransType()));
    	        client.setGenderName(client.getGender()==1? "Male":"Female");
    	        client.setCivilStatusName(CivilStatus.typeName(client.getCivilStatus()));
    	        
    			//Application.addMessage(1, "Success", "Successfully saved");
    			PrimeFaces pf = PrimeFaces.current();
    			pf.executeScript("displayTrans();");
    		}
    		
    	}else {
    		Application.addMessage(3, "Error", "Please provide details");
    	}
    }
	
    public List<String> payorNameSuggested(String query){
		List<String> result = new ArrayList<>();
		
		mapCustomer = new LinkedHashMap<String, Customer>();
		mapCustomer2 = new LinkedHashMap<String, Livelihood>();
		//mapCustomer3 = new LinkedHashMap<String, FishCage>();
		
		if(query!=null && !query.isEmpty()) {
			int size = query.length();
			if(size>=4) {
				String sql = " AND cus.fullname like '%"+query+"%'";
				String[] params = new String[0];
				
				sql = " AND (cus.cuslastname like '%"+ query +"%' OR ";
				sql += " cus.fullname like '%"+ query +"%' OR cus.cusfirstname like '%"+ query +"%' OR cus.cusmiddlename like '%"+ query +"%'";
				sql += " ) GROUP BY cus.fullname LIMIT 10";
				params = new String[0];
				
				if(client.getTransType()==ClientTransactionType.BUSINESS_NEW.getId() || 
						client.getTransType()==ClientTransactionType.BUSINESS_RENEWAL.getId() || 
						client.getTransType()==ClientTransactionType.QUARTERLY_BUSINESS_PAYMENT.getId()) {
					
					sql = " AND   ( live.livename like '%" + query +"%' OR cuz.fullname like '%"+ query + "%') ORDER BY live.livelihoodid DESC ";
					for(Livelihood l : Livelihood.retrieve(sql, params)) {
						result.add(l.getBusinessName());
						mapCustomer2.put(l.getBusinessName(), l);
					}
					/*
					 * for(BusinessCustomer cust : BusinessCustomer.retrieve(sql, params)) { String
					 * fullName = cust.getFirstname() + " " + cust.getLastname();
					 * result.add(fullName); mapCustomer2.put(fullName, cust); }
					 */
					/*
					 * }else if(client.getTransType()==ClientTransactionType.FISHCAGE_NEW.getId() ||
					 * client.getTransType()==ClientTransactionType.FISHCAGE_RENEW.getId()) {
					 * 
					 * sql = " AND (owner like '%"+ query +"%' OR tenantowner like '%"+ query
					 * +"%')"; for(FishCage c : FishCage.retrieve(sql, new String[0])) {
					 * result.add(c.getOwnerName()); if(c.getTenantOwner()!=null) {
					 * result.add(c.getTenantOwner()); } mapCustomer3.put(c.getOwnerName(), c); }
					 */
					
				}else {
					for(com.italia.municipality.lakesebu.licensing.controller.Customer cust : com.italia.municipality.lakesebu.licensing.controller.Customer.retrieve(sql, params)) {
						String fullName = cust.getFullname();
						result.add(fullName);
						mapCustomer.put(fullName, cust);
					}
				}
			}
		}
		
		if(result.size()==0) {
			result.add("Not yet registed");
		}
		
		return result;
	}
    public void supplyCustomerInfo() {
    	
    	if(client.getTransType()==ClientTransactionType.BUSINESS_NEW.getId() || 
				client.getTransType()==ClientTransactionType.BUSINESS_RENEWAL.getId() || 
				client.getTransType()==ClientTransactionType.QUARTERLY_BUSINESS_PAYMENT.getId()) {
    		
    		if(getMapCustomer2()!=null && getMapCustomer2().size()>0 && getMapCustomer2().get(getPayorName())!=null){
    			
    			Livelihood liv = getMapCustomer2().get(getPayorName());
    			BusinessCustomer cus = liv.getTaxPayer();
    			
    			client.setFirstName(cus.getFirstname());
    			client.setMiddleName(cus.getMiddlename());
    			client.setLastName(cus.getLastname());
    			client.setBirthdayTmp(DateUtils.convertDateString(cus.getBirthdate(), "yyyy-MM-dd"));
    			client.setBirthday(cus.getBirthdate());
    			client.setGender(cus.getGender().equalsIgnoreCase("1")? 1 : 2);
    			client.setGenderName(client.getGender()==1? "Male" : "Female");
    			client.setCivilStatus(cus.getCivilStatus());
    			client.setCivilStatusName(CivilStatus.typeName(cus.getCivilStatus()));
    			client.setAddress(cus.getCompleteAddress());
    			client.setContactNumber(cus.getContactno());
    			
    			client.setBusinessName(liv.getBusinessName());
    		}
    	
    		
    	}else {
    		if(getMapCustomer()!=null && getMapCustomer().size()>0 && getMapCustomer().get(getPayorName())!=null){
    			
    			Customer cus = getMapCustomer().get(getPayorName());
    			
    			client.setFirstName(cus.getFirstname());
    			client.setMiddleName(cus.getMiddlename());
    			client.setLastName(cus.getLastname());
    			client.setBirthdayTmp(DateUtils.convertDateString(cus.getBirthdate(), "yyyy-MM-dd"));
    			client.setBirthday(cus.getBirthdate());
    			client.setGender(cus.getGender().equalsIgnoreCase("1")? 1 : 2);
    			client.setGenderName(client.getGender()==1? "Male" : "Female");
    			client.setCivilStatus(cus.getCivilStatus());
    			client.setCivilStatusName(CivilStatus.typeName(cus.getCivilStatus()));
    			client.setAddress(cus.getCompleteAddress());
    			client.setContactNumber(cus.getContactno());
    			
    			if(isInclude()) {
    				client.setBirthPlace(cus.getBornplace());
    				client.setWeight(cus.getWeight());
    				client.setHeight(cus.getHeight());
    				client.setProfession(cus.getWork());
    				client.setNationality(cus.getCitizenship());
    				client.setTinNo("");
    			}
    			
    		}
    	}
    }	
    
    public void findQRCode() {
		System.out.println("now looking the qrcode......");
		final String jsonData = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("qrcode");
		
		System.out.println("jsondata: " + jsonData);
		PrimeFaces pf = PrimeFaces.current();
		NationalIDJsonData data = new NationalIDJsonData(jsonData);
		if(data!=null && !data.getPCN().isEmpty()) {	
			
				client.setFirstName(data.getFName());
    			client.setMiddleName(data.getMName());
    			client.setLastName(data.getLName());
    			client.setBirthdayTmp(DateUtils.convertDateString(data.getDOB(), "yyyy-MM-dd"));
    			client.setBirthday(data.getDOB());
    			client.setGender(data.getSex().equalsIgnoreCase("Male")? 1 : 2);
    			client.setGenderName(data.getSex());
    			client.setCivilStatus(1);
    			client.setCivilStatusName(CivilStatus.typeName(1));
    			client.setAddress(data.getPOB());
    			client.setContactNumber("");
			
    			Application.addMessage(1, "Found", "Client has been scanned successfully. Please click next button.");
			
			pf.executeScript("PF('dlgCam').hide();");
		}else {
			Application.addMessage(1, "Error", "Cannot read QR Code");		
		}
	}	
    
    public void loadClient() {
    	String sql = "";// AND (status=" + ClientStatus.QUEUE.getId() + " OR status="+ ClientStatus.SERVING.getId()+" )";
		
		clients = new ArrayList<Client>();
		
		if(getQueSequence()!=null) {
			sql += " AND transno like '%"+ getQueYear() +"-" + getQueMonth() +"-"+ getQueDay() + "-"+ getQueSequence() +"%'";
		}else {
			sql += " AND transno like '%"+ getQueYear() +"-" + getQueMonth() +"-"+ getQueDay() + "-%'";
		}
		
		if(getSearchName()!=null) {
			 sql += " AND (firstname like '%"+ getSearchName() +"%' ";
			 sql += " OR middlename like '%"+ getSearchName() +"%' ";
			 sql += " OR lastname like '%"+ getSearchName() +"%')";
		}
		
		sql +=" ORDER BY transno";
		
		for(Client c : Client.retrieve(sql, new String[0])) {
			Collector col = Collector.retrieve(c.getCollectorId());
			System.out.println("Name>>>>>> : " + col.getName());
			c.setCollectorName(col.getName());
			clients.add(c);
		}
    }
}