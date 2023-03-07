package com.italia.municipality.lakesebu.bean;

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
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.FishCage;
import com.italia.municipality.lakesebu.controller.FishcageBillingStatment;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.reports.FishCageORs;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @since 10/14/2021
 * @version 1.0
 *
 */
@Named
@ViewScoped
public class FishcageBillingBean extends ORListingBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 43543646565541L;
	
	@Setter @Getter private List<FishcageBillingStatment> statements;
	@Setter @Getter private String searchBill;
	@Setter @Getter private String searchPayname;
	@Setter @Getter private boolean hasbeencalled;
	@Setter @Getter private List owns;
	@Setter @Getter private Map<Long, FishCage> mapOwner;
	@Setter @Getter private List<PaymentName> paymentSelected;
	@Setter @Getter private List<PaymentName> payments;
	@Setter @Getter private Map<Long, PaymentName> mapPaymentData;
	
	@Setter @Getter private String clerkName=DocumentFormatter.getTagName("clerk-name");
	@Setter @Getter private String clerkPosition=DocumentFormatter.getTagName("clerk-position");
	@Setter @Getter private String treasurerName=DocumentFormatter.getTagName("treasurer-name");
	@Setter @Getter private String treasurerPosition=DocumentFormatter.getTagName("treasurer-position");
	private String billing_rpt=DocumentFormatter.getTagName("v7_fishcage-billing-statement");
	
	public void openParticulars(FishcageBillingStatment py) {
		loadParticulars();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgfBillPay').show(1000)");
		if(py.getParticulars()!=null) {
			loadPreviousSelected(py);
		}else {
			setPaymentSelected(new ArrayList<PaymentName>());
		}
	}
	
	public void loadParticulars() {
		payments = new ArrayList<PaymentName>();
		String sql = "";
		if(getSearchPayname()!=null && !getSearchPayname().isEmpty()) {
			sql = " AND pyname like '%"+ getSearchPayname() +"%'";
		}
		payments.addAll(PaymentName.retrieve(sql +" ORDER BY pyname", new String[0]));
	}
	
	public void loadPreviousSelected(FishcageBillingStatment py) {
		paymentSelected = py.getListparticulars();
	}
	
	public void openBilling() {	
		loadData();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgfishbill').show(1000)");
	}
	
	public void loadData() {
		statements = new ArrayList<FishcageBillingStatment>();
		
		String sql = "";
		
		if(getSearchBill()!=null && !getSearchBill().isEmpty()) {
			sql = " AND (st.fbdatebill like '%"+ getSearchBill()+"%'";
			sql += " OR st.fbcontrolno like '%"+ getSearchBill() +"%'";
			sql += " OR ow.owner like '%"+ getSearchBill() +"%'";
			sql += " OR ow.watersurveyno like '%"+ getSearchBill() +"%'";
			sql += " OR ow.arealocation like '%"+ getSearchBill() +"%')";
		}
		
		if(!hasbeencalled) {//called once only
			getOwners();
			hasbeencalled = true;
		}
		
		
		for(FishcageBillingStatment fs :  FishcageBillingStatment.retrieve(sql, new String[0])) {
			fs.setOwnerListId(fs.getOwner().getId());
			fs.setOwners(getOwns());
			fs.setListparticulars(FishcageBillingStatment.getPaynames(fs.getParticulars()));
			statements.add(fs);
		}
		
		FishcageBillingStatment st = FishcageBillingStatment.builder()
				.dateTrans(DateUtils.getCurrentDateYYYYMMDD())
				.date(DateUtils.getDateToday())
				.controlNo(FishcageBillingStatment.getControlNewNo())
				.remarks("Add Remarks")
				.particulars(null)
				.isActive(1)
				.owner(FishCage.builder().ownerName("Select Owner").build())
				.ownerListId(0)
				.owners(getOwns())
				.userDtls(getUser())
				.isCompleted(0)
				.build();
		statements.add(st);
		
		Collections.reverse(statements);
	}
	
	public void billDeleteSelected(FishcageBillingStatment fs) {
		if(fs.getParticulars()!=null) {
			fs.delete();
			Application.addMessage(1, "Success", "Data was successfully saved.");
			loadData();
		}
	}
	
	private void getOwners(){
		List own = new ArrayList<>();
		mapOwner = new LinkedHashMap<Long, FishCage>();
		own.add(new SelectItem(0, "Select Name"));
		for(FishCage cage : FishCage.retrieve(" ORDER BY owner", new String[0])) {
			own.add(new SelectItem(cage.getId(), cage.getOwnerName() + " - " + cage.getCageArea()));
			mapOwner.put(cage.getId(), cage);
		}
		setOwns(own);
	}
	
	private UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
	public void onCellEdit(CellEditEvent event) {
		   Object oldValue = event.getOldValue();
		   Object newValue = event.getNewValue();
		   int index = event.getRowIndex();
		   String column =  event.getColumn().getHeaderText();
		   
		   
		   if("Date".equalsIgnoreCase(column)) {
			   getStatements().get(index).setDateTrans(DateUtils.convertDate((Date)newValue, "yyyy-MM-dd"));
		   }else if("Fish Owner".equalsIgnoreCase(column)) {
			  long id = (Long)event.getNewValue();
			   getStatements().get(index).setOwner(getMapOwner().get(id));
			   String remarks = getMapOwner().get(id).getCageArea();
			   getStatements().get(index).setRemarks(remarks);
			   System.out.println("remarks change : " + remarks);
		   }else if("Other Details".equalsIgnoreCase(column)) {
			   getStatements().get(index).setRemarks(newValue+"");
		   }
			   
		   
	}
	
	public void addNamePayment(PaymentName py) {
		if(py.getAmount()>0) {
			paymentSelected.add(py);
		}else {
			Application.addMessage(1, "Error", "Please provide amount");
		}
	}
	
	public void onCellEditSelected(CellEditEvent event) {
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
	}
	
	public void paynameDeleteSelected(PaymentName py) {
		getPaymentSelected().remove(py);
	}
	
	public void onCellEditPay(CellEditEvent event) {
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
		int index = event.getRowIndex();
		if(getPaymentSelected()==null) {
			paymentSelected = new ArrayList<PaymentName>();
			paymentSelected.add(getPayments().get(index));
		}else {
			paymentSelected.add(getPayments().get(index));
		}
		
	}
	
	public void saveBill(FishcageBillingStatment fs) {
		if(fs.getOwner().getId()!=0 && getPaymentSelected()!=null && getPaymentSelected().size()>0) {
			
			int count = 1;
			String val = "";
			for(PaymentName py : getPaymentSelected()) {
				if(count==1) {
					val = py.getId() + ":" + py.getAmount();
				}else {
					val += "<@>"+ py.getId() + ":" + py.getAmount();
				}
				count++;
			}
			
			String rem = "Add Remarks".equalsIgnoreCase(fs.getRemarks())? "" : fs.getRemarks();
			fs.setParticulars(val);
			fs.setRemarks(rem);
			fs.save();
			loadData();
			paymentSelected = new ArrayList<PaymentName>();
			Application.addMessage(1, "Success", "Data was successfully saved.");
		}else {
			Application.addMessage(2, "Error", "Saving was not successfully please check your data.");
		}
	}
	
public void printPaymentHistory(FishcageBillingStatment bill) {
		HashMap param = new HashMap();
		FishCage py = bill.getOwner();
		String path = GlobalVar.REPORT_FOLDER;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(billing_rpt, billing_rpt, path);
		List<FishCageORs> p = new ArrayList<FishCageORs>();
		for(PaymentName pay : bill.getListparticulars()) {
			if("Total".equalsIgnoreCase(pay.getName())) {
				param.put("PARAM_TOTAL", Currency.formatAmount(pay.getAmount()));
			}else {
				FishCageORs fs = FishCageORs.builder()
						.f1(pay.getName())
						.f2(Currency.formatAmount(pay.getAmount()))
						.build();
				p.add(fs);
			}
		}
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(p);
		
		
		
		try{param.put("PARAM_DATE_ISSUED_BILL", DateUtils.convertDateToMonthDayYear(bill.getDateTrans()));}catch(NullPointerException e) {}
		try{param.put("PARAM_BILL_NO", bill.getControlNo());}catch(NullPointerException e) {}
		
		
		try{param.put("PARAM_SURVEY_NO", py.getWaterSurveyNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_OWNER_NAME", py.getOwnerName());}catch(NullPointerException e) {}
		try{param.put("PARAM_TENANT_NAME", py.getTenantOwner());}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LOCATION", py.getLocation());}catch(NullPointerException e) {}
		
		try{param.put("PARAM_TOTAL_STOCK", py.getNoOfTotalStock()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_ANNUAL_PROD", py.getNoOfAnnualProduction()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_MODULE", py.getSizeCagePerModule());}catch(NullPointerException e) {}
		try{param.put("PARAM_NON_FUNCTIONAL", py.getNoOfNonFunctional()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_FUNCTIONAL", py.getNoOfFunctional()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_NO_CAGES", py.getNumberOfFishCages()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LENTH", py.getCageArea());}catch(NullPointerException e) {}
		try{param.put("PARAM_WATER_AREA", py.getTotalSquareArea());}catch(NullPointerException e) {}
		try{param.put("PARAM_CELLPHONE_NO", py.getCellphoneNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_NON_MOTORIZED", py.getNonMotorizedBoat()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_MOTORIZED", py.getMotorizedBoat()+"");}catch(NullPointerException e) {}
		param.put("PARAM_BIRTH_DATE", "");
		param.put("PARAM_SIGNATURE", py.getOwnerName());
		
		
		param.put("PARAM_VERIFIEDBY", getClerkName().toUpperCase());
		param.put("PARAM_TREASURER", getTreasurerName().toUpperCase());
		
		//logo
				//String dalogo = path + "da_logo.png";
				//try{File fil = new File(dalogo);
				//FileInputStream lof = new FileInputStream(fil);
				//param.put("PARAM_DA_LOGO", lof);
				//}catch(Exception e){}
		
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
	  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
		
				try{
					System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT);
		  		 File file = new File(path, GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT + ".pdf");
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
			            response.setHeader("Content-Disposition", "inline; filename=\"" + GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT + ".pdf" + "\"");
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
	
}
