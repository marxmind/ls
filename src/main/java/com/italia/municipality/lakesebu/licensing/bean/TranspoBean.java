package com.italia.municipality.lakesebu.licensing.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.primefaces.event.CellEditEvent;
import com.italia.municipality.lakesebu.controller.Barangay;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.ClearanceRpt;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.licensing.controller.Transpo;
import com.italia.municipality.lakesebu.licensing.controller.TranspoItems;
import com.italia.municipality.lakesebu.licensing.controller.TranspoRpt;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 1-05-2022
 *
 */
@Named
@ViewScoped
@Data
public class TranspoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 435465867434341L;
	
	private Transpo transpo;
	private List<Transpo> trans;
	private Date dateCreated;
	String searchName;
	TranspoItems itemDataSelected;
	String orNumber;
	String amount;
	
	//item
	private String name;
	private String unit;
	private double qty;
	
	
	@PostConstruct
	public void init() {
		
		defaultValue();
		
		String sql = " ORDER BY clr.tid DESC LIMIT 10";
		
		trans = Transpo.retrieve(sql, new String[0]);
		
	}
	
	public void search() {
		
		String sql = "";
		
		if(getSearchName()!=null && !getSearchName().isEmpty()) {
			sql += " AND (";
			sql += " clr.controlno like '%"+ getSearchName() +"%' ";
			sql += " OR clr.ornumber like '%"+ getSearchName() +"%'";
			sql += " OR clr.requestor like '%"+ getSearchName() +"%'";
			sql += ")";
		}
		
		sql += " ORDER BY clr.tid DESC" ;
		
		trans = Transpo.retrieve(sql, new String[0]);
	}
	
	public void clickItem(Transpo trans) {
		
		setDateCreated(DateUtils.convertDateString(trans.getDateTrans(), "yyyy-MM-dd"));
		
		
		List<TranspoItems> items = new ArrayList<TranspoItems>();
		
		/*
		 * TranspoItems item = TranspoItems.builder() .cnt(0) .name("Item Name")
		 * .unit("Unit") .quantity(0) .amount(0) .isActive(1) .build();
		 * 
		 * items.add(item);
		 */
		int cnt = 1;
		for(TranspoItems i : TranspoItems.retrieveById(trans.getId())) {
			i.setCnt(cnt++);
			items.add(i);
		}
		
		if(trans.getPreparedBy()==null) {//if prepared by is empty add this default user
		trans.setPreparedBy(Words.getTagName("preparedby"));
		trans.setPreparedByPosition(Words.getTagName("preparedby-pos"));
		}
		trans.setItems(items);
		
		setTranspo(trans);
		
		if(trans.getOrNumber()!=null) {
			if(trans.getOrNumber().contains("-")) {
				
				String[] val = trans.getOrNumber().split("-");
				
				setOrNumber(val[0]);
				setAmount(val[1]);
			}else {
				setOrNumber(trans.getOrNumber());
				setAmount("0.00");
			}
		}
		
		
	}
	
	public void save() {
		if(getTranspo()!=null && getTranspo().getItems().size()>0) {
			
			Transpo trans = getTranspo();
			trans.setIsActive(1);
			trans.setDateTrans(DateUtils.convertDate(getDateCreated(), "yyyy-MM-dd"));
			trans.setUserDtls(getUser());
			
			String val = (getOrNumber()!=null? getOrNumber() : "0000000") + "-" + (getAmount()!=null? getAmount() : "0.00");
			
			trans.setOrNumber(val);
			trans = Transpo.save(trans);
			
			TranspoItems.delete(trans.getId());
			
			for(TranspoItems item : trans.getItems()) {
				if(item.getQuantity()>0) {
					item.setIsActive(1);
					item.setTranspo(trans);
					item.save();
				}
			}
			
			init();
			Application.addMessage(1, "Success", "Successfully saved");
		}else {
			Application.addMessage(3, "Error", "Please check your data");
		}
	}
	
	public UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
	public void defaultValue() {
		setDateCreated(DateUtils.getDateToday());
		/*
		 * List<TranspoItems> items = new ArrayList<TranspoItems>(); TranspoItems item =
		 * TranspoItems.builder() .name("Item Name") .unit("Unit") .quantity(0)
		 * .amount(0) .isActive(1) .build();
		 * 
		 * 
		 * items.add(item);
		 */
		
		
		transpo = Transpo.builder()
				.controlNo(Transpo.getSeries())
				.issuedDay(DateUtils.getCurrentDay()+"")
				.issuedMonth(DateUtils.getMonthName(DateUtils.getCurrentMonth()))
				.deliveredDate(DateUtils.getCurrentDateMMMMDDYYYY())
				.officialName(Words.getTagName("mayor"))
				.officialPosition(Words.getTagName("official-position"))
				.licenseOfficer(Words.getTagName("oic"))
				.licenseOfficerPosition(Words.getTagName("license-officer-pos"))
				.menroOfficer(Words.getTagName("menro"))
				.menroPosition(Words.getTagName("menro-officer-pos"))
				.preparedBy(Words.getTagName("preparedby"))
				.preparedByPosition(Words.getTagName("preparedby-pos"))
				.items(new ArrayList<TranspoItems>())
				.build();
		
	}
	
	public void clear() {
		setTranspo(null);
		setDateCreated(DateUtils.getDateToday());
		defaultValue();
		setOrNumber(null);
		setAmount(null);
		
		setName(null);
		setUnit(null);
		setQty(0);
		System.out.println("clearing....");
	}
	
	public void delete(Transpo transpo) {
		transpo.delete();
		init();
	}
	
	public void onCellEdit(CellEditEvent event) {
		 try{
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        
	        System.out.println("old Value: " + oldValue);
	        System.out.println("new Value: " + newValue);
	        
	        int index = event.getRowIndex();
	        String column =  event.getColumn().getHeaderText();
	        
	        if(getTranspo()!=null && getTranspo().getItems().size()>0) {
	        	
	        	if("Name".equalsIgnoreCase(column)) {
	        		getTranspo().getItems().get(index).setName(newValue+"");
	        	}
	        	if("Unit".equalsIgnoreCase(column)) {
	        		getTranspo().getItems().get(index).setUnit(newValue+"");
	        	}
	        	if("Qty".equalsIgnoreCase(column)) {
	        		getTranspo().getItems().get(index).setQuantity(Double.valueOf(newValue+""));
	        		System.out.println("QTY: " + getTranspo().getItems().get(index).getQuantity());
	        	}
	        	if("Amount".equalsIgnoreCase(column)) {
	        		getTranspo().getItems().get(index).setAmount(Double.valueOf(newValue+""));
	        	}
	        	
	        }
	        
		 }catch(Exception e){}  
	 }       
	
	public void addItem() {
		boolean isOk = true;
		if(getName()==null || getName().isEmpty()) { isOk=false; }
		if(getUnit()==null || getUnit().isEmpty()){ isOk=false; }
		if(getQty()==0) { isOk=false; }
		
		System.out.println("isOk:" + isOk);
		
		if(isOk) {
		TranspoItems item = TranspoItems.builder()
				.cnt(0)
				.name(getName())
				.unit(getUnit())
				.quantity(getQty())
				.amount(0)
				.isActive(1)
				.build();
		getTranspo().getItems().add(item);
		
		setName(null);
		setUnit(null);
		setQty(0);
		
		}else {
			Application.addMessage(3, "Error", "Please check your inputted data.");
		}
	}
	
	public void deleteItem(TranspoItems item) {
		getTranspo().getItems().remove(item);
		if(item.getId()>0) {item.delete();}
	}
	
	public void addRow(TranspoItems val) {
		
		boolean isOk = true;
		
		if(val.getQuantity()==0) {
			isOk = false;
		}
		//if(val.getAmount()==0) {
		//	isOk = false;
		//}
		
		if("Item Name".equalsIgnoreCase(val.getName()) || "Unit".equalsIgnoreCase(val.getName())) {
			isOk = false;
		}
		
		if(isOk) {
			TranspoItems item = TranspoItems.builder()
					.cnt(0)
					.name("Item Name")
					.unit("Unit")
					.quantity(0)
					.amount(0)
					.isActive(1)
					.build();
			
			List<TranspoItems> items = new ArrayList<TranspoItems>();
			
			//items = getTranspo().getItems();
			items.add(item);
			int cnt = 1;
			for(TranspoItems i : getTranspo().getItems()) {
				i.setCnt(cnt++);
				items.add(i);
			}
			getTranspo().setItems(items);
			
		}else {
			Application.addMessage(2, "Warning", "There is field not yet inputted. Please check your data.");
		}
	}
	
	public void print(Transpo trans) {
		String path = GlobalVar.REPORT_FOLDER;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(GlobalVar.TRANSPO, GlobalVar.TRANSPO, path);
		List<TranspoRpt> rpts = new ArrayList<TranspoRpt>();
		
		List<TranspoItems> items = TranspoItems.retrieveById(trans.getId());
		//double amount = 0d;
		for(TranspoItems item : items) {
			//amount += item.getAmount();
			rpts.add(
					TranspoRpt.builder()
					.f1(item.getName())
					.f2(item.getUnit())
					.f3(item.getQuantity()+"")
					.f4(Currency.formatAmount(item.getAmount()))
					.build()
					);
		}
		
		
		if(items==null && items.size()==0) {
			rpts.add(new TranspoRpt());
		}
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rpts);
		
		HashMap param = new HashMap();
		
		String ornumber="";
		String oramount="";
		if(trans.getOrNumber()!=null) {
			if(trans.getOrNumber().contains("-")) {
				
				String[] val = trans.getOrNumber().split("-");
				
				ornumber=val[0];
				oramount=val[1];
			}else {
				ornumber = trans.getOrNumber();
				oramount = "0.00";
			}
		}
		

		if(trans.getPreparedBy()==null) {//if prepared by is empty add this default user
			trans.setPreparedBy(Words.getTagName("preparedby"));
			trans.setPreparedByPosition(Words.getTagName("preparedby-pos"));
		}
		
		try{param.put("PARAM_CONTROLNO", "Control No: "+trans.getControlNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_ORNUMBER", "OR No.:"+ornumber + " Php" + Currency.formatAmount(oramount));}catch(NullPointerException e) {}
		try{param.put("PARAM_REQUESTOR", trans.getRequestor().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_ADDRESS", trans.getAddress().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_FROM_TO", trans.getFromAndTo().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_DATE_DELIVER", trans.getDeliveredDate());}catch(NullPointerException e) {}
		try{param.put("PARAM_PURPOSE", trans.getPurpose().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_NOTEDBY", trans.getMenroOfficer().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_NOTED_POS", trans.getMenroPosition());}catch(NullPointerException e) {}
		try{param.put("PARAM_APPROVEDBY", trans.getOfficialName().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_APPROVED_POS", trans.getOfficialPosition());}catch(NullPointerException e) {}
		try{param.put("PARAM_ISSUEDBY", trans.getLicenseOfficer().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_ISSUED_POS", trans.getLicenseOfficerPosition());}catch(NullPointerException e) {}
		try{param.put("PARAM_PREPAREDBY", trans.getPreparedBy().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_PREPARED_POS", trans.getPreparedByPosition());}catch(NullPointerException e) {}
		
		try{param.put("PARAM_ISSUED_MONTH", trans.getIssuedMonth());}catch(NullPointerException e) {}
		try{param.put("PARAM_ISSUED_DAY", trans.getIssuedDay());}catch(NullPointerException e) {}
		
		
		//logo
				String dalogo = path + "logo.png";
				try{File fil = new File(dalogo);
				FileInputStream lof = new FileInputStream(fil);
				param.put("PARAM_LOGO", lof);
				}catch(Exception e){}
		
		//logo
		String logo = path + "logo.png";
		try{File file = new File(logo);
		FileInputStream loff = new FileInputStream(file);
		param.put("PARAM_LOGO_LAKESEBU", loff);
		}catch(Exception e){}

		//logo
		String officialLogo = path + "logotrans.png";
		try{File file = new File(officialLogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_SEALTRANSPARENT", off);
		}catch(Exception e){}
		
		String backlogo = path + "documentbg-gen.png";
		try{File file = new File(backlogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_BACKGROUND", off);
		}catch(Exception e){}
		
		try{
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ GlobalVar.TRANSPO +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
		
				try{
					System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + GlobalVar.TRANSPO);
		  		 File file = new File(path, GlobalVar.TRANSPO + ".pdf");
				 FacesContext faces = FacesContext.getCurrentInstance();
				 ExternalContext context = faces.getExternalContext();
				 HttpServletResponse response = (HttpServletResponse)context.getResponse();
					
			     BufferedInputStream input = null;
			     BufferedOutputStream output = null;
			     
			     try{
			    	 
			    	 // Open file.
			            input = new BufferedInputStream(new FileInputStream(file), GlobalVar.DEFAULT_BUFFER_SIZE);

			            // Init servlet response.
			            response.reset();
			            response.setHeader("Content-Type", "application/pdf");
			            response.setHeader("Content-Length", String.valueOf(file.length()));
			            response.setHeader("Content-Disposition", "inline; filename=\"" + GlobalVar.TRANSPO + ".pdf" + "\"");
			            output = new BufferedOutputStream(response.getOutputStream(), GlobalVar.DEFAULT_BUFFER_SIZE);

			            // Write file contents to response.
			            byte[] buffer = new byte[GlobalVar.DEFAULT_BUFFER_SIZE];
			            int length;
			            while ((length = input.read(buffer)) > 0) {
			                output.write(buffer, 0, length);
			            }

			            // Finalize task.
			            output.flush();
			    	 
			     }finally{
			    	// Gently close streams.
			            close(output);
			            close(input);
			     }
			     
			     // Inform JSF that it doesn't need to handle response.
			        // This is very important, otherwise you will get the following exception in the logs:
			        // java.lang.IllegalStateException: Cannot forward after response has been committed.
			        faces.responseComplete();
			        
				}catch(Exception ioe){
					ioe.printStackTrace();
				}
		
	}
	
	
	private void close(Closeable resource) {
	    if (resource != null) {
	        try {
	            resource.close();
	        } catch (IOException e) {
	            // Do your thing with the exception. Print it, log it or mail it. It may be useful to 
	            // know that this will generally only be thrown when the client aborted the download.
	            e.printStackTrace();
	        }
	    }
	}
	
	public List<String> payorNameSuggested(String query){
		List<String> result = new ArrayList<>();
		
		if(query!=null && !query.isEmpty()) {
			int size = query.length();
			if(size>=4) {
				String sql = " AND cus.fullname like '%"+query+"%'";
				String[] params = new String[0];
				
				sql = " AND (cus.cuslastname like '%"+ query +"%' OR ";
				sql += " cus.fullname like '%"+ query +"%' OR cus.cusfirstname like '%"+ query +"%' OR cus.cusmiddlename like '%"+ query +"%'";
				sql += " ) GROUP BY cus.fullname LIMIT 10";
				params = new String[0];
				
				for(com.italia.municipality.lakesebu.licensing.controller.Customer cust : com.italia.municipality.lakesebu.licensing.controller.Customer.retrieve(sql, params)) {
					String fullName = cust.getFullname();
					result.add(fullName);
				}
				
			}
		}
		return result;
	}
}