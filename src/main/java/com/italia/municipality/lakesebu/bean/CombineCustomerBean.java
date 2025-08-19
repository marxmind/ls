package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.ORListing;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Named("comCustomer")
@ViewScoped
public class CombineCustomerBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 36546856456741L;
	private String first;
	private String middle;
	private String last;
	private List<Customer> customers;
	private List<Customer> selectedCustomer;
	
	private List<ORListing> orlist;
	private long newCustomerId;
	private long oldCustomerId;
	
	@PostConstruct
	public void init() {
		
	}
	
	public void search() {
		String sql = "";
		
		customers = new ArrayList<Customer>();
		
		if(getFirst()!=null) {
			sql += " AND a.cusfirstname like '%"+ getFirst().trim() +"%'";
		}
		if(getMiddle()!=null) {
			sql += " AND a.cusmiddlename like '%"+ getMiddle().trim() +"%'";
		}
		if(getLast()!=null) {
			sql += " AND a.cuslastname like '%"+ getLast().trim() +"%'";
		}
		
		sql = "SELECT a.customerid,a.cusdateregistered,a.cusfirstname,a.cusmiddlename,a.cuslastname,a.fullname, (select count(*) from orlisting s where s.customerid=a.customerid) as total FROM customer a WHERE a.cusisactive=1 " + sql;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			
			//String val = orlistDtls(rs.getLong("customerid"));
			
			Customer cus = Customer.builder()
					.id(rs.getLong("customerid"))
					.dateregistered(rs.getString("cusdateregistered"))
					.firstname(rs.getString("cusfirstname"))
					.middlename(rs.getString("cusmiddlename"))
					.lastname(rs.getString("cuslastname"))
					.fullname(rs.getString("fullname"))
					.gender(rs.getString("total"))
					.build();
			customers.add(cus);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		
	}
	
	public static String orlistDtls(long id) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "SELECT count(*) as total FROM orlisting WHERE isactiveor=1 AND customerid=" + id;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return rs.getString("total");
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		return "0";
	}
	
	public void clickOrlisting(long customerId) {
		
		if(getSelectedCustomer()!=null && getSelectedCustomer().size()==1) {
			setNewCustomerId(getSelectedCustomer().get(0).getId());
			setOldCustomerId(customerId);
		}
		
		String sql = "SELECT *, (select sum(s.olamount) as amount from ornamelist s where s.orid=o.orid AND s.customerid=o.customerid) as amount FROM orlisting o where o.isactiveor=1 AND o.customerid=" + customerId;
		orlist = new ArrayList<ORListing>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		System.out.println("clickOrlisting: " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			ORListing or = ORListing.builder()
					.id(rs.getLong("orid"))
					.dateTrans(rs.getString("ordatetrans"))
					.orNumber(rs.getString("ornumber"))
					.formType(rs.getInt("aform"))
					.formName(FormType.nameId(rs.getInt("aform")))
					.status(rs.getInt("orstatus"))
					.statusName(FormStatus.nameId(rs.getInt("orstatus")))
					.amount(rs.getDouble("amount"))
					.fundId(rs.getInt("fundid"))
					.build();
			
			orlist.add(or);
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		
		PrimeFaces pf = PrimeFaces.current();
		if(orlist!=null && orlist.size()>0 && (getSelectedCustomer()!=null && getSelectedCustomer().size()==1)) {
			pf.executeScript("PF('dlgOrs').show(1000)");
		}else {
			Application.addMessage(3, "Error", "Please check one on the list of customer to merge the selected customer official receipt");
		}
		
	}
	
	public void mergeOfficialReceiptToNewCustomerId() {
		long customerId = getOldCustomerId();
		
		
		if(customerId>0) {
			Connection conn = null;
			PreparedStatement ps = null;
			try{
			conn = WebTISDatabaseConnect.getConnection();
			
			String sql = "UPDATE ornamelist SET customerid=" + getNewCustomerId() + " WHERE customerid=" + customerId;
			ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			
			sql = "UPDATE ORLISTING SET customerid=" + getNewCustomerId() + " WHERE customerid=" + customerId;
			ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			
			//hide the old customer regeren
			sql = "UPDATE customer SET cusisactive=0 WHERE customerid=" + customerId;
			ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			
			ps.close();
			
			WebTISDatabaseConnect.close(conn);
			}catch(Exception e){e.getMessage();}
			
			Application.addMessage(1, "Success", "Successfully merged to new customer id");
			setOldCustomerId(0);
			search();
		}
	}
	
	public void deleteCustomer(Customer customer) {
		if("0".equalsIgnoreCase(customer.getGender())) {
			Customer.delete("UPDATE customer set cusisactive=0 WHERE customerid=" + customer.getId(), new String[0]);
			Application.addMessage(1, "Success", "Successfully deleted customer");
			search();
		}
	}
	
	
	public void onCellEditSelected(CellEditEvent event) {
		 try{
		        Object oldValue = event.getOldValue();
		        Object newValue = event.getNewValue();
		        
		        System.out.println("old Value: " + oldValue);
		        System.out.println("new Value: " + newValue);
		        if(newValue!=null && !newValue.toString().isEmpty()) {
		        int index = event.getRowIndex();
		        Customer customer = customers.get(index);
		        if("First".equalsIgnoreCase(event.getColumn().getHeaderText())) {
		        	customer.setFirstname(event.getNewValue().toString().toUpperCase());
		        }else if("Middle".equalsIgnoreCase(event.getColumn().getHeaderText())) {
		        	customer.setMiddlename(event.getNewValue().toString().toUpperCase());
		        }else if("Last".equalsIgnoreCase(event.getColumn().getHeaderText())) {
		        	customer.setLastname(event.getNewValue().toString().toUpperCase());
		        }else if("Fullname".equalsIgnoreCase(event.getColumn().getHeaderText())) {
		        	customer.setFullname(event.getNewValue().toString().toUpperCase());
		        }
		       String fullname = customer.getLastname().trim() + ", " + customer.getFirstname().trim() + " " + customer.getMiddlename().trim();
		       customer.setFullname(fullname);
		       customer.setIsactive(1);
		       customer.save();
		        }
		        
			 }catch(Exception e){}  
	}  
	
}
