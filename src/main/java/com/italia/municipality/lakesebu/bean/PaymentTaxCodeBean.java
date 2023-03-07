package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * 
 * @author Mark Italia
 * @since 09/20/2021
 * @version 1.0
 *
 */

import org.primefaces.event.CellEditEvent;
import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.CollectionAccountingGroup;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.controller.TaxAccountGroup;
import com.italia.municipality.lakesebu.controller.TaxCodeGroup;
import com.italia.municipality.lakesebu.enm.AccessLevel;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
@Named
@ViewScoped
public class PaymentTaxCodeBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1354564347544L;
	
	@Setter @Getter  private List<PaymentName> paynames;
	@Setter @Getter private String searchName;
	
	@Setter @Getter private List<TaxCodeGroup> groups;
	@Setter @Getter private Map<Long, PaymentName> mapPaynames;
	@Setter @Getter private long pyId;
	
	@Setter @Getter private List<TaxAccountGroup> groupAccs;
	@Setter @Getter private Map<Long, PaymentName> mapPaynames2;
	
	@Setter @Getter private List<CollectionAccountingGroup> collects;
	@Setter @Getter private String searchGroup;
	
	
	@Setter @Getter private List accountingParticular;
	@Setter @Getter private long accountParticularId;
	
	private List<Date> rangeDate;
	
	public void setRangeDate(List<Date> rangeDate) {
		this.rangeDate = rangeDate;
	}
	
	public List<Date> getRangeDate() {
		
		if(rangeDate==null) {
			rangeDate = new ArrayList<Date>();
			Date dateFrom = DateUtils.getDateToday();
			Date dateTo = DateUtils.getDateToday();
			rangeDate.add(dateFrom);
			rangeDate.add(dateTo);
		}
		
		return rangeDate;
	}
	
	public void loadCollections() {
		collects = new ArrayList<CollectionAccountingGroup>();
		accountingParticular = new ArrayList<>();
		accountingParticular.add(new SelectItem(0, "All Accounting Group"));
		for(TaxAccountGroup t : TaxAccountGroup.retrieve(" ORDER BY accname", new String[0])) {
			accountingParticular.add(new SelectItem(t.getId(), t.getName()));
		}
		
		
		
		String sql = "";
		
		if(getSearchGroup()!=null && !getSearchGroup().isEmpty()) {
			sql = " AND accname like'%"+ getSearchGroup() +"%'";
		}
		
		if(getAccountParticularId()>0) {
			sql += " AND accid=" + getAccountParticularId();
		}
		
		Map<Long, TaxAccountGroup> groups = TaxAccountGroup.retrieveMap(sql, new String[0]);
		
		String[] params = new String[3];
		sql = " AND (orl.ordatetrans>=? AND orl.ordatetrans<=?) AND pay.accntgrpid=?";
		
		if(getRangeDate()!=null && getRangeDate().size()==2) {
			params[0] = DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd");
			params[1] = DateUtils.convertDate(getRangeDate().get(1), "yyyy-MM-dd");
		}else {
			params[0] = DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd");
			params[1] = params[0];
		}
			
		
		for(Long  key : groups.keySet()) {
			params[2] = key+"";
			
			CollectionAccountingGroup ag = CollectionAccountingGroup.builder()
					.groupName(groups.get(key).getName())
					.build();
			
			double amount = 0d;
			
			List<ORNameList> names = ORNameList.retrieveGroupByPayName(sql, params);
			if(names!=null && names.size()>0) {
				collects.add(ag);//add if not zero	
				
				
			for(ORNameList n : names) {
				ag = CollectionAccountingGroup.builder()
						.groupName("")
						.taxName(n.getPaymentName().getName())
						.total(Currency.formatAmount(n.getPaymentName().getAmount()))
						.build();
						amount += n.getPaymentName().getAmount();
						collects.add(ag);
			}
			//total
			ag = CollectionAccountingGroup.builder()
					.groupName("Total")
					.taxName("")
					.total(Currency.formatAmount(amount))
					.build();
			collects.add(ag);
			
			}
		}
		
		/*
		for(Long  key : cols.keySet()) {
			
			if(groups!=null && groups.containsKey(key)) {
				double amount = 0d;
				CollectionAccountingGroup ag = CollectionAccountingGroup.builder()
						.groupName(groups.get(key).getName())
						.build();
				collects.add(ag);
				for(PaymentName p : cols.get(key)) {
							ag = CollectionAccountingGroup.builder()
							.groupName("")
							.taxName(p.getName())
							.total(Currency.formatAmount(p.getAmount()))
							.build();
							amount += p.getAmount();
					collects.add(ag);
				}
				
				//total
				ag = CollectionAccountingGroup.builder()
						.groupName("Total")
						.taxName("")
						.total(Currency.formatAmount(amount))
						.build();
				collects.add(ag);
				
			}
		}*/
		
	}
	
	public void loadTaxCode() {
		paynames = new ArrayList<PaymentName>();
		String sql = "";
		String[] params = new String[0];
		PaymentName py = PaymentName.builder()
				.name("Add Name")
				.dateTrans(DateUtils.getCurrentDateMonthDayYear())
				.isActive(1)
				.build();
		if(getSearchName()!=null && !getSearchName().isEmpty()) {
			sql = " AND (pyname like '%"+ getSearchName() +"%' OR pyaccntcode like '%"+ getSearchName() +"%') ORDER BY pyname DESC";
		}
		
		paynames = PaymentName.retrieve(sql, params);
		paynames.add(py);
		Collections.reverse(paynames);
	}
	
	public void loadTaxGroup() {
		mapPaynames = new LinkedHashMap<Long, PaymentName>();
		pyId = 0;
		List pyNames = new ArrayList<>();
		pyNames.add(new SelectItem(0, "Select Tax Code"));
		for(PaymentName py : PaymentName.retrieve(" ORDER BY pyname", new String[0])) {
			pyNames.add(new SelectItem(py.getId(), py.getName()));
			mapPaynames.put(py.getId(), py);
		}
		
		groups = new ArrayList<TaxCodeGroup>();
		groups.add(
				TaxCodeGroup.builder()
				.name("Add Name")
				.isActive(1)
				.build()
				);
		//List<TaxCodeGroup> group = TaxCodeGroup.retrieve("", new String[0]);
		
		for(TaxCodeGroup g : TaxCodeGroup.retrieve(" ORDER BY st.groupname", new String[0])) {
			PaymentName p = PaymentName.builder()
					.name("Add Name")
					.pyId(pyId)
					.pyNames(pyNames)
					.build();
			List<PaymentName> pys = new ArrayList<PaymentName>();
			pys.add(p);
			pys.addAll(g.getGroups());
			g.setGroups(pys);
			
			
			
			groups.add(g);
		}
		
		
		//Collections.reverse(group);
		//groups.addAll(group);
		
	}
	
	public void loadAccounts() {
		mapPaynames2 = new LinkedHashMap<Long, PaymentName>();
		List pyNames = new ArrayList<>();
		pyNames.add(new SelectItem(0, "Select Tax Code"));
		for(PaymentName py : PaymentName.retrieve(" ORDER BY pyname", new String[0])) {
			pyNames.add(new SelectItem(py.getId(), py.getName()));
			mapPaynames2.put(py.getId(), py);
		}
		
		groupAccs = new ArrayList<TaxAccountGroup>();
		groupAccs.add(
				TaxAccountGroup.builder()
				.name("Add Name")
				.isActive(1)
				.build()
				);
		
		
		for(TaxAccountGroup gp : TaxAccountGroup.retrieve(" ORDER BY st.accname", new String[0])) {
			
			PaymentName p = PaymentName.builder()
					.name("Add Name")
					.pyId(0)
					.pyNames(pyNames)
					.build();
			List<PaymentName> pys = new ArrayList<PaymentName>();
			pys.add(p);
			pys.addAll(PaymentName.retrieve(gp.getId()));
			gp.setGroups(pys);
			groupAccs.add(gp);
		}
	}
	
	public void onCellEdit(CellEditEvent event) {
		 try{
			 Login in = Login.getUserLogin();
			 if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
		        Object oldValue = event.getOldValue();
		        Object newValue = event.getNewValue();
		        
		        System.out.println("old Value: " + oldValue);
		        System.out.println("new Value: " + newValue);
		        
		        int index = event.getRowIndex();
		        
		        PaymentName py =  getPaynames().get(index);
		        if("Add Name".equalsIgnoreCase(py.getName())) {
		        	Application.addMessage(2, "Warning", "This cannot be added.");
		        }else {
			        py.save();
			        Application.addMessage(1, "Success", "Successfully updated.");
		        }
			 }else {
				 Application.addMessage(2, "Warning", "You dont have a right to modify the information");
			 }
		 }catch(Exception e){}  
	 }     
	
	public void onChange(TabChangeEvent event) {
		//Tab activeTab = event.getTab();
		//...
		if("Particulars".equalsIgnoreCase(event.getTab().getTitle())) {
			loadTaxCode();
		}else if("Accounting Code".equalsIgnoreCase(event.getTab().getTitle())){
			loadAccounts();
		}else if("Customization".equalsIgnoreCase(event.getTab().getTitle())){
			loadTaxGroup();
		}else if("Issuance".equalsIgnoreCase(event.getTab().getTitle())){
			loadCollections();
		}		
	}
	
	public void onCellEditGroup(CellEditEvent event) {
		 //try{
			 //Login in = Login.getUserLogin();
			 //if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
		        Object oldValue = event.getOldValue();
		        Object newValue = event.getNewValue();
		        
		        System.out.println("old Value: " + oldValue);
		        System.out.println("new Value: " + newValue);
		        
		        //int index = event.getRowIndex();
		        
		        //TaxCodeGroup group = getGroups().get(index);
		        //TaxCodeGroup.save(group);
		        //loadTaxGroup();
		        //Application.addMessage(1, "Success", "Successfully updated.");
			 //}else {
				// Application.addMessage(2, "Warning", "You dont have a right to modify the information");
			 //}
		 //}catch(Exception e){}  
	 }
	
	public void onCellEditAccountGroup(CellEditEvent event) {
		 //try{
			 //Login in = Login.getUserLogin();
			 //if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
		        Object oldValue = event.getOldValue();
		        Object newValue = event.getNewValue();
		        
		        System.out.println("old Value: " + oldValue);
		        System.out.println("new Value: " + newValue);
		        
		        //int index = event.getRowIndex();
		        
		        //TaxCodeGroup group = getGroups().get(index);
		        //TaxCodeGroup.save(group);
		        //loadTaxGroup();
		        //Application.addMessage(1, "Success", "Successfully updated.");
			 //}else {
				// Application.addMessage(2, "Warning", "You dont have a right to modify the information");
			 //}
		 //}catch(Exception e){}  
	 }
	
	
	public void saveGroup(TaxCodeGroup gp) {
		if(!"Add Name".equalsIgnoreCase(gp.getName())) {
			Login in = Login.getUserLogin();
			 if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
				gp.save();
				loadTaxGroup();
				Application.addMessage(1, "Success", "Successfully saved.");
			 }else {
				 Application.addMessage(2, "Warning", "You dont have a right to modify the information");
			 }
		}else {
			 Application.addMessage(2, "Warning", "This cannot be Saved.");
		}
	}
	
	public void deleteGroup(TaxCodeGroup gp) {
		Login in = Login.getUserLogin();
		 if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
			if(gp.getId()>0) {
				gp.delete();
				loadTaxGroup();
				Application.addMessage(1, "Success", "Successfully deleted.");
			}else {
				Application.addMessage(2, "Warning", "This is cannot be modified.");
			}
		 }else {
			 Application.addMessage(2, "Warning", "You dont have a right to modify the information");
		 }
	}
	
	public void deleteGroupId(TaxCodeGroup gp,PaymentName py) {
		TaxCodeGroup.addRemoveInGroup(false, gp, py).save();
		loadTaxGroup();
		Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	public void saveGroupPaymentCode(TaxCodeGroup gp, long id) {
		
			System.out.println("check pyid=" + id);
			PaymentName py = getMapPaynames().get(id);
			TaxCodeGroup.addRemoveInGroup(true, gp, py).save();
			loadTaxGroup();
			setPyId(0);
			Application.addMessage(1, "Success", "Successfully saved.");
		
	}
	
	public void saveAccountGroup(TaxAccountGroup gp) {
		if(!"Add Name".equalsIgnoreCase(gp.getName())) {
			Login in = Login.getUserLogin();
			 if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
				 	gp.save();
				 	loadAccounts();
				 	Application.addMessage(1, "Success", "Successfully saved.");
			 }else {
				 Application.addMessage(2, "Warning", "You dont have a right to modify the information");
			 }
		}else {
			 Application.addMessage(2, "Warning", "This cannot be modified.");
		}		 	
	}
	
	public void deleteAccountGroup(TaxAccountGroup gp) {
		gp.delete();
		loadAccounts();
		Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	public void deleteAccountGroupId(TaxAccountGroup gp, PaymentName py) {
		TaxAccountGroup.addRemoveInGroup(false, gp, py);
		loadAccounts();
		Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	public void saveAccountGroupPaymentCode(TaxAccountGroup gp, long id) {
		System.out.println("check pyid=" + id);
		PaymentName py = getMapPaynames2().get(id);
		TaxAccountGroup.addRemoveInGroup(true, gp, py).save();
		loadAccounts();
		Application.addMessage(1, "Success", "Successfully saved.");
	}
}