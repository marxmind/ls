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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.BusinessBilling;
import com.italia.municipality.lakesebu.controller.BusinessBillingRpt;
import com.italia.municipality.lakesebu.controller.BusinessIndex;
import com.italia.municipality.lakesebu.controller.BusinessIndexTrans;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.NumberToWords;
import com.italia.municipality.lakesebu.controller.OR51;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.BusinessCategory;
import com.italia.municipality.lakesebu.enm.BusinessQtrType;
import com.italia.municipality.lakesebu.enm.BusinessType;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Numbers;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @since 5/25/2023
 * @version 1.0
 *
 */
@Named("bnreg")
@ViewScoped
@Data
public class BusinessRegBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 156679756756435454L;
	
	private String businessName;
	private String ownerName;
	private int typeId;
	private List types;
	private int essentialId;
	private List essentials;
	private int businessTypeId;
	private List businessTypes;
	private String labelAmountCapitalGross;
	private double amountCapitalGross;
	private String dtls;
	
	private String businessNameTmp;
	private String ownerNameTmp;
	
	//private String billSeries;
	private List<BusinessIndex> business;
	private BusinessIndex selectedBusinessData;
	
	private final int CURRENT_MONTH = DateUtils.getCurrentMonth(); 
	
	private String tmpCalculated;
	private List<PaymentName> payments;
	
	private Map<Long, PaymentName> mapPaynames;
	private long idName;
	private List names;
	private double amountNew;
	
	private List<String> regulatories;
	private double basicTax;
	private List<BusinessBilling> billings;
	private String searchBilling;
	private double grandTotal;
	private double regulatoryFee;
	
	public void saveBilling() {
		if(getSelectedBusinessData()!=null && getPayments().size()>0) {
			String data = BusinessBilling.regulatoriesCollection(getPayments());
			BusinessBilling bill = BusinessBilling.builder()
					.billDate(DateUtils.getCurrentDateYYYYMMDD())
					.billSeries(BusinessBilling.getLatestBillSeries())
					.billType(getTypeId())
					.basicTax(getBasicTax())
					.isActive(1)
					.regulatories(data)
					.regulatoryFee(getRegulatoryFee())
					.capital(getAmountCapitalGross())
					.grossEssential(getEssentialId()==1? getAmountCapitalGross() : 0)
					.grossNonEssential(getEssentialId()==0? getAmountCapitalGross() : 0)
					.grandTotal(getGrandTotal())
					.businessIndex(getSelectedBusinessData())
					.build();
			
			bill.save();
			Application.addMessage(1, "Success", "Successfully saved");
			//setDtls("Your billing was successfully saved!!!!");
			clearFields();
			init();
		}
	}
	
	public void selectedBilling(BusinessBilling bill) {
		try{
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			
			String REPORT_NAME = GlobalVar.BUSINESS_BILLING;
			
			BusinessIndex bz = bill.getBusinessIndex();
					
				try{//the purpose of this is to supply null values	
					bz = BusinessIndex.retrieve(" AND bn.bnid="+ bill.getBusinessIndex().getId(), new String[0]).get(0);
					bill.setBusinessIndex(bz);
				}catch(IndexOutOfBoundsException io) {}
			
			ReportCompiler compiler = new ReportCompiler();
			HashMap param = new HashMap();
	  		param.put("PARAM_BILL_DATE", DateUtils.convertDateToMonthDayYear(bill.getBillDate()));
	  		param.put("PARAM_CONTROL_NO", bill.getBillSeries());
	  		param.put("PARAM_TAXPAYER", bill.getBusinessIndex().getOwner());
	  		param.put("PARAM_TRADENAME", bill.getBusinessIndex().getBusinessName());
	  		param.put("PARAM_REPRESENTATIVE", bill.getBusinessIndex().getOwner());
	  		
	  		String address = bill.getBusinessIndex().getAddress().replace("N/A,", "").trim();
	  		address = address.replace(" ,", "");
	  		param.put("PARAM_BUSINESS_ADDRESS", address);
	  		try{param.put("PARAM_BARANGAY", bill.getBusinessIndex().getBarangay().getName().toUpperCase());}catch(Exception e) {}
	  		param.put("PARAM_HOME_ADDRESS", address);
	  		
	  		if(bill.getBillType()==0) {//NEW
	  			param.put("PARAM_GROSS_ESSENTIAL", "");
		  		param.put("PARAM_GROSS_NON_ESSENTIAL", "");
		  		param.put("PARAM_CAPITAL", Currency.formatAmount(bill.getCapital()));
	  		}else {//RENEW
	  			param.put("PARAM_GROSS_ESSENTIAL", Currency.formatAmount(bill.getGrossEssential()));
		  		param.put("PARAM_GROSS_NON_ESSENTIAL", Currency.formatAmount(bill.getGrossNonEssential()));
		  		param.put("PARAM_CAPITAL", "");
	  		}
	  		
	  		param.put("PARAM_NATURE_OF_BUSINESS", bill.getBusinessIndex().getNatureOfBusiness());
	  		param.put("PARAM_PIN", "");
	  		param.put("PARAM_PERMIT_TYPE", BusinessType.val(bill.getBillType()).getName());
	  		param.put("PARAM_CATEGORY", BusinessCategory.val(bill.getBusinessIndex().getCategory()).getName());
	  		param.put("PARAM_MEMO", "");
	  		param.put("PARAM_PRIV_PERMIT", "");
	  		double perQtr = bill.getBasicTax() / 4;
	  		param.put("PARAM_1QTR", Currency.formatAmount(perQtr));
	  		param.put("PARAM_2QTR", Currency.formatAmount(perQtr));
	  		param.put("PARAM_3QTR", Currency.formatAmount(perQtr));
	  		param.put("PARAM_4QTR", Currency.formatAmount(perQtr));
	  		
	  		
	  		param.put("PARAM_BASIC_TAX", Currency.formatAmount(bill.getBasicTax()));
	  		param.put("PARAM_DEFICIENCY", "");
	  		param.put("PARAM_DELINQUENCY", "");
	  		
	  		param.put("PARAM_AD_PAYMENT", "");
	  		param.put("PARAM_TAX_CREDIT", "");
	  		param.put("PARAM_PROMISORY", "");
	  		
	  		UserDtls user = getUser().getUserDtls();
	  		
	  		param.put("PARAM_GRAND_TOTAL", Currency.formatAmount(bill.getGrandTotal()));
	  		param.put("PARAM_PREPARED_BY", user.getFirstname() + " " + user.getLastname());
	  		param.put("PARAM_VERIFIED_BY", "Henry E. Magbanua");
	  		
	  		
	  		
	  		List<OR51> rpts = new ArrayList<OR51>();
	  		for(String s : BusinessBilling.regulatoriesData(bill.getRegulatories())) {
	  			String[] vals = s.split("-");
	  			String amount = vals[1].replace("<*>", "");
	  			amount = amount.replace("<*", "");
	  			long id = Long.valueOf(vals[0]);
	  			String name = getMapPaynames().get(id).getName();
	  			
	  			OR51 or = OR51.builder()
	  					.code("")
	  					.description(name)
	  					.amount(amount)
	  					.build();
	  			
	  			if(225==id) {//business interest 
	  				param.put("PARAM_INTEREST", amount);
	  			}else if( 91==id) {//business surcharge
	  				param.put("PARAM_SURCHARGE", amount);
	  			}else if( 1==id || 87==id || 219==id) {//retailer, wholesaler and business tax
	  				//retailer
	  			}else {
	  				rpts.add(or);
	  			}
	  			
	  		}
	  		
	  		param.put("PARAM_REGULATORY_FEE", Currency.formatAmount(bill.getRegulatoryFee()));
	  		
	  		
	  		
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rpts);
	  		
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
	  	    
	  	    File file = new File(REPORT_PATH, REPORT_NAME + ".pdf");
			 FacesContext faces = FacesContext.getCurrentInstance();
			 ExternalContext context = faces.getExternalContext();
			 HttpServletResponse response = (HttpServletResponse)context.getResponse();
				
		     BufferedInputStream input = null;
		     BufferedOutputStream output = null;
	  	    try {
		  // Open file.
	            input = new BufferedInputStream(new FileInputStream(file), GlobalVar.DEFAULT_BUFFER_SIZE);

	            // Init servlet response.
	            response.reset();
	            response.setHeader("Content-Type", "application/pdf");
	            response.setHeader("Content-Length", String.valueOf(file.length()));
	            response.setHeader("Content-Disposition", "inline; filename=\"" + REPORT_NAME + ".pdf" + "\"");
	            output = new BufferedOutputStream(response.getOutputStream(), GlobalVar.DEFAULT_BUFFER_SIZE);

	            // Write file contents to response.
	            byte[] buffer = new byte[GlobalVar.DEFAULT_BUFFER_SIZE];
	            int length;
	            while ((length = input.read(buffer)) > 0) {
	                output.write(buffer, 0, length);
	                //System.out.println("printReportAll read : " + length);
	            }

	            // Finalize task.
	            output.flush();
		    }finally {
		    	 close(output);
		         close(input);
		    }	
	            
			
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	private Login getUser() {
		return Login.getUserLogin();
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
	
	public void loadBilling() {
		billings = new ArrayList<BusinessBilling>();
		String sql = "";
		if(getSearchBilling()!=null && !getSearchBilling().isEmpty() && getSearchBilling().length()>0) {
			sql = " AND (bl.billseries like '%"+ getSearchBilling().trim() +"%' OR bn.bnname like '%"+ getSearchBilling().trim()+"%' OR bn.bnowner like '%"+ getSearchBilling().trim() +"%')";
			sql += " ORDER BY bl.billid DESC";
		}else {
			sql = " ORDER BY bl.billid DESC LIMIT 10";
		}
		
		
		billings = BusinessBilling.retrieve(sql, null);
		//Collections.reverse(billings);
	}
	
	public void clearFields() {
		setBusinessName(null);
		setOwnerName(null);
	}
	
	public List getNames() {
		mapPaynames = new HashMap<Long, PaymentName>();
		names = new ArrayList<>();
		for(PaymentName name : PaymentName.retrieve("", new String[0])) {
			mapPaynames.put(name.getId(), name);
			names.add(new SelectItem(name.getId(),name.getName()));
		}
		return names;
	}
	
	private String getSeries() {
		return BusinessBilling.getLatestBillSeries();
	}
	
	@PostConstruct
	public void init() {
		payments = new ArrayList<PaymentName>();
		payments.add(PaymentName.builder().id(0).name("Add here").build());//add default
		
		billings = new ArrayList<BusinessBilling>();
		
		//setBillSeries("BILL-" + DateUtils.getCurrentYear()+"-00001");
		
		
		String val = ReadConfig.value(AppConf.SERVER_LOCAL);
        HttpSession session = SessionBean.getSession();
		session.setAttribute("server-local", val);
		session.setAttribute("theme", "saga");
		
		labelAmountCapitalGross="Please Provide Capital";
		types = new ArrayList<>();
		for(BusinessType type : BusinessType.values()) {
			types.add(new SelectItem(type.getId(), type.getName()));
		}
		essentials = new ArrayList<>();
		essentials.add(new SelectItem(0, "Non-Essential"));
		essentials.add(new SelectItem(1, "Essential"));
		
		businessTypes = new ArrayList<>();
		for(BusinessCategory cat : BusinessCategory.values()) {
			businessTypes.add(new SelectItem(cat.getId(), cat.getName()));
		}
		
		setDtls("<h2>Requriements for processing a business permit</h2>"+otherRequirements());
	}
	
	public void updateData() {
		System.out.println(getTypeId());
		if(getTypeId()==0) {
			setLabelAmountCapitalGross("Please Provide Capital");
		}else {
			setLabelAmountCapitalGross("Please Provide Gross");
		}
		reCalculateBusiness();
	}
	
	public void selectedBusiness(BusinessIndex in) {
		
		String text = "";
		in.setType(BusinessType.RENEW.getId());
		setTypeId(in.getType());
		setSelectedBusinessData(in);
		setBusinessName(in.getBusinessName());
		setOwnerName(in.getOwner());
		setLabelAmountCapitalGross("Please Provide Gross");
		setBusinessTypeId(in.getCategory());
		setEssentialId(in.getIsEssential());
		
		int year = DateUtils.getCurrentYear() - 1;
		List<BusinessIndexTrans> trans = BusinessIndexTrans.retrieve("AND bnt.bnyear="+year+" AND bn.bnid=" + in.getId() + " ORDER BY bnt.bntid", new String[0]);
		if(trans.size()>0) {
			text += "<p>Below are the previous payment paid for this business.</p>";
			for(BusinessIndexTrans tr : trans) {
				text += "<p>OR Number: " + tr.getOrnumber()+"</p>";
				text += "<p>Quarter: " + BusinessQtrType.typeName(tr.getQtrPayment())+"</p>";
				text += findORNumber(tr);
			}
			text += calculatingSurchargeAndInterest(trans);
		}else {
			text += "<h2>This business has no pending payment.</h2>";
			text += "<br/>";
			text += retrieveLatestBill();
		}
		setTmpCalculated(text);
		calculateBusiness();
		
		//setDtls(text);
	}
	
	private String retrieveLatestBill() {
		String text = "";
		double prevGrossAmount = 0d;
		if(getSelectedBusinessData()!=null) {
			BusinessIndex in = getSelectedBusinessData();
			int year = DateUtils.getCurrentYear();
			List<BusinessIndexTrans> trans = BusinessIndexTrans.retrieve("AND bnt.bnyear="+year+" AND bn.bnid=" + in.getId() + " ORDER BY bnt.bntid", new String[0]);
			if(trans.size()>0) {
				text += "<p>Below are the previous payment paid for this business.</p>";
				int count = 1;
				for(BusinessIndexTrans tr : trans) {
					if(count == 1) {
						if(BusinessQtrType.ANNUAL.getId()==tr.getQtrPayment()
								|| BusinessQtrType.FIRST_QTR.getId()==tr.getQtrPayment()
								|| BusinessQtrType.FIRST_SEMI_ANNUAL.getId()==tr.getQtrPayment()
								|| BusinessQtrType.FIRST_TO_THIRD.getId()==tr.getQtrPayment()) {
							double amount = tr.getCapital()>0? tr.getCapital() : tr.getGross();
							prevGrossAmount = amount;
							double markup = amount * 0.10;
							setAmountCapitalGross(amount+markup);
						}
					}
					count++;
					
					text += "<p>Issued: <b>" + DateUtils.convertDateToMonthDayYear(tr.getDateTrans()) +"</b></p>";
					text += "<p>OR Number: <b style='color: red'>" + tr.getOrnumber()+"</b></p>";
					text += "<p>Quarter: <b>" + BusinessQtrType.typeName(tr.getQtrPayment())+"</b></p>";
					text += findORNumber(tr);
				}
			}
		}
		
		text += "<h2>Marking up 10% from previous " + (getTypeId()>0? "Gross" : "Capital") + " of " + Currency.formatAmount(prevGrossAmount) + ". The current suggested gross is " + Currency.formatAmount(getAmountCapitalGross())+"</h2>";
		text += "<p>----------------------------------------------------------------------------------------------------------------------------------------------------------------------</p>";
		text += "<p>----------------------------------------------------------------------------------------------------------------------------------------------------------------------</p>";
		text += "<p>----------------------------------------------------------------------------------------------------------------------------------------------------------------------</p>";
		return text;
	}
	
	
	public String calculatingSurchargeAndInterest(List<BusinessIndexTrans> trans) {
		
		String text = "";
		double businessTaxLastYear = 0d;
		//int currentMonth = 1;
		int qtrmonths = 0;
		double qtrAmount = 0d;
		boolean qtrFound = false;
		
		int semin = 0;
		double semeAmount = 0d;
		boolean semeFound = false;
		
		int firstToThird = 0;
		double firstToThirdAmount = 0d;
		boolean firstToThirdFound = false;
		int quarters=0;
		for(BusinessIndexTrans tran : trans) {
				if(BusinessQtrType.FIRST_QTR.getId()==tran.getQtrPayment()) {
					qtrAmount = tran.getBasicTax();
					businessTaxLastYear = tran.getBasicTax();
					qtrmonths += 3;
					qtrFound = true;
					quarters +=3;
				}else if(BusinessQtrType.SECOND_QTR.getId()==tran.getQtrPayment()) {
						qtrmonths += 3;	
						quarters +=3;
				}else if(BusinessQtrType.THIRD_QTR.getId()==tran.getQtrPayment()) {
					qtrmonths += 3;	
					quarters +=3;
				}else if(BusinessQtrType.FOURTH_QTR.getId()==tran.getQtrPayment()) {
					qtrmonths += 3;
					quarters +=3;
				}else if(BusinessQtrType.FIRST_SEMI_ANNUAL.getId()==tran.getQtrPayment()) {
					semeAmount = tran.getBasicTax();
					businessTaxLastYear = tran.getBasicTax();
					semin +=6;
					semeFound = true;
					quarters +=6;
				}else if(BusinessQtrType.SECOND_SEMI_ANNUAL.getId()==tran.getQtrPayment()) {
					semin +=6;	
					quarters +=6;
				}else if(BusinessQtrType.FIRST_TO_THIRD.getId()==tran.getQtrPayment()) {
					firstToThirdAmount = tran.getBasicTax();
					businessTaxLastYear = tran.getBasicTax();
					firstToThird += 9;
					firstToThirdFound = true;
					quarters +=9;
				}
		}
		double amount = 0d;
		double finalSurcharge = 0d;
		double finalInterest = 0d;
		
		double unpaidQuarters = 0d;
		if(qtrFound) {
			int remMonth = 12 - quarters;//qtrmonths;
			double perMonthAmount = qtrAmount/12;
			double surcharge = perMonthAmount *  remMonth;
			unpaidQuarters = surcharge;
				   surcharge = surcharge * 0.25;
			double interest = (perMonthAmount *  (remMonth>0? (remMonth +CURRENT_MONTH) : 0)) * 0.02;
			       interest = interest * remMonth;
			       finalSurcharge = surcharge;
			       finalInterest = interest;
			amount =  surcharge + interest + unpaidQuarters;
		}
		
		if(semeFound) {
			int remMonth = 12 - quarters;//semin;
			double perMonthAmount = semeAmount/12;
			double surcharge = perMonthAmount *  remMonth;
			   unpaidQuarters = surcharge;
			   surcharge = surcharge * 0.25;
			double interest = (perMonthAmount *  (remMonth>0? (remMonth +CURRENT_MONTH) : 0)) * 0.02;
		       interest = interest * remMonth;
		       finalSurcharge += surcharge;
		       finalInterest += interest;
		       amount +=  surcharge + interest + unpaidQuarters;
		}
		
		if(firstToThirdFound) {
			int remMonth = 12 - quarters;//firstToThird;
			double perMonthAmount = firstToThirdAmount/12;
			double surcharge = perMonthAmount *  remMonth;
			   unpaidQuarters = surcharge;
			   surcharge = surcharge * 0.25;
			double interest = (perMonthAmount *  (remMonth>0? (remMonth +CURRENT_MONTH) : 0)) * 0.02;
		       interest = interest * remMonth;
		       finalSurcharge += surcharge;
		       finalInterest += interest;
		       amount +=  surcharge + interest + unpaidQuarters;
		}
		
		if(amount>0) {
			text += "<h2>Business Tax Last Year: "+ Currency.formatAmount(businessTaxLastYear) +"</h2>";
			text += "<h3>Unpaid Quarter/s: "+ Currency.formatAmount(unpaidQuarters) +"</h3>";
			text += "<h3>Business Surcharge: "+ Currency.formatAmount(finalSurcharge) +"</h3>";
			text += "<h3>Interest as of "+ DateUtils.getCurrentDateMMMMDDYYYY() +": "+ Currency.formatAmount(finalInterest) +"</h3>";
			text += "<h1>Total Unpaid Tax: "+ Currency.formatAmount(amount) +"</h1>";
			text += "<br/>";
		}else {
			text += "<h1>This business has no previous balances...</h1>";
		}
		text += "<br/>";
		text += "<h1>Please see below calculation for Surcharge, Interest and Total Unpaid Tax</h1>";
		text += "<h2>Surcahrge: Unpaid quarters * 25% ="+Currency.formatAmount(finalSurcharge)+" </h2>";
		text += "<h2>Interest: 2% per month ="+Currency.formatAmount(finalInterest)+" </h2>";
		text += "<h2>Unpaid Tax: Surcahrge + Interest + Unpaid Quarters ="+Currency.formatAmount(amount)+" </h2>";
		return text;
		
	}
	
	public String findORNumber(BusinessIndexTrans tran) {
		String sql = " AND orl.ornumber='"+ tran.getOrnumber() +"'";
		String text="";
		
		text += "<pi>Payment Details</pi>";
		text += "<br/>";
		text += "<ul>";
		double amount = 0d;
		String date="";
		String payor="";
		int count = 1;
		for(ORNameList on : ORNameList.retrieve(sql, new String[0])) {
			text += "<li>" + on.getPaymentName().getName() + "\t" + Currency.formatAmount(on.getAmount()) + "</li>";
			amount += on.getAmount();
			if(count==1) {
				date = on.getOrList().getDateTrans();
				payor=on.getCustomer().getFullname();
			}
			count++;
		}
		text += "</ul>";
		text += "<p>Total: <b>"+ Currency.formatAmount(amount) +"</b></p>";
		text += "<br/>";
		return text;
	}
	
	public void provideBusinessDtls() {
		String text = "";
		if(getBusinessName()!=null && !getBusinessName().isEmpty()) {
		List<BusinessIndex> bzs = BusinessIndex.retrieveName("bnname", getBusinessName());
		if(bzs!=null && bzs.size()>0) {
			PrimeFaces pf = PrimeFaces.current();
			if(bzs.size()>1) {
				business = new ArrayList<BusinessIndex>();
				business = bzs;
				pf.executeScript("showBusiness()");
				text = "<h2>Please select business found above</h2>";
				setDtls(text);
			}else {
				pf.executeScript("hideBusiness()");
				business = new ArrayList<BusinessIndex>();
				selectedBusiness(bzs.get(0));
			}
		}else {
			text = "<h2>This business name "+ getBusinessName().toUpperCase() +" is not yet recorded in the system.</h2>";
			text = guidingInfo();
			setDtls(text);
		}
		
		}
		
	}
	
	private String guidingInfo() {
		String text = "";
			text += "<h3>Please provide the following information.</h3>";
			text += "<h1>Fill-in:</h1>";
			text += "<ul>";
			text += "<li>Business Name</li>";
			text += "<li>Owner Name</li>";
			text += "<li>Business Type: <b>NEW</b></li>";
			text += "<li>Capital: <b>Provide capital more than 30,000.00</b></li>";
			text += "<li><h1>Non-Essential</h1>Entertainment & hospitality, including but not limited to strip clubs and brothels, casinos, concert venues, arenas, auditoriums, stadiums, large conference rooms, meeting halls, and cafeterias, Recreation and athletic facilities, including but not limited to community and recreation centers, gyms, health clubs, fitness centers, yoga, barre and spin facilities, Beauty and personal care services and facilities, including but not limited to barber shops, beauty, tanning, waxing hair salons, and nail salons and spas, Retail facilities, including shopping malls except for pharmacy or other health care facilities within retail operations. Retailers are encouraged to continue online operations with pickup and delivery.</li>";
			text += "<li><h1>Essential</h1>Fire services, law enforcement agencies, emergency medical services & public safety agencies, Healthcare services, Businesses or organizations that provide food, shelter, or critical social services for disadvantaged populations, Home maintenance/repair services, Auto repair services & trucking service centers, Grocery stores, supermarkets, hardware stores, convenience & discount stores, Pharmacies, healthcare operations, & biomedical facilities, Post offices & shipping outlets, Gas stations & truck stops, Banks & financial institutions, Veterinary services & pet stores, Laundromats & dry cleaners, Food processing, Agriculture, livestock & feed mills, Logistics & Supply Chain Operations: Warehousing, storage, distribution, and supply-chain related operations, Public transportation, Essential stays in hotels, commercial lodging, dormitories, shelters, and homeless encampments</li>";
			text += "<li><h1>Category</h1>Retailer, Wholesaler, Amusement, Contractor, Cottages, Eating Places, Financial Institution, Fishery Rental, Institution, Manufacturer, Other Retailer, Sylab Permit, Swimming Pool, and Trader</li>";
			text += "</ul>";
		return text;
	}
	
	public void provideOwnerDtls() {
		List<String> lst = BusinessCustomer.names(getOwnerName());
		String text = "";
		int size = lst.size();
		if(size>0) {
			text = "<h2>There "+ (size>1? "are " : "is ") + size +" matched for the owner name >>> "+ getOwnerName() +"</h2>";
			text += "</br>";
			//text += "<ul>";
			String str="";
			int cnt = 0;
			for(String bn : lst){
				text += "<p style='color: blue'>"+ bn +"</p>";
				str=bn;
				cnt++;
			}
			//text += "</ul>";
			if(cnt==1 && getOwnerName().equalsIgnoreCase(str)) {
				text="";
			}
		}
		setOwnerNameTmp(text);
		updatingDtls();
	}
	
	public void updatingDtls() {
		String text ="";//otherRequirements();
		text += getBusinessNameTmp()==null? "" : getBusinessNameTmp();
		text += "</br>";
		text += getOwnerNameTmp()==null? "" : getOwnerNameTmp();
		
		setDtls(text);
	}
	
	private String otherRequirements() {
		String text = "";
		text += "</br>";
		text += "<p>1) BARANGAY CERTIFICATE</p>";
		text += "<p>2) CEDULA OF OWNER OR CORPORATION</p>";
		text += "<p>3) ZONING CLEARANCE</p>";
		text += "<p>4) MENRO CERTIFICATE</p>";
		text += "<p>5) DTI/SEC. REGISTRATION</p>";
		text += "<p>6) SANITARY PERMIT</p>";
		text += "<p>7) FIRE CERTIFICATE</p>";
		text += "</br>";
		//text += guidingInfo();
		return text;
	}
	
	public void calculateBusiness() {
		String text = "";
		if(getTmpCalculated()!=null) {text += getTmpCalculated(); text +="<br/>";}
		text += "<h2>TAXDUE WORKSHEET</h2>";
		text += "<p>Bill No: <b>"+ getSeries() +"</b></p>";
		text += "<p>Bill Date: <b>"+ DateUtils.getCurrentDateMMMMDDYYYY() +"</b></p>";
		text += "<p>Business Name: <b>"+ getBusinessName().toUpperCase() +"</b></p>";
		text += "<p>Owner Name: <b>"+getOwnerName().toUpperCase()+"</b></p>";
		text += "<p>"+ (getTypeId()>0? "Gross" : "Capital") +": "+"<b>"+Currency.formatAmount(getAmountCapitalGross())+"</b></p>";
		text += "<p>Type: <b>"+BusinessType.val(getTypeId()).getName()+"</b></p>";
		text += "<p>Category: <b>"+BusinessCategory.val(getBusinessTypeId()).getName()+"</b></p>";
		text += "<p>For the : <b>"+(getEssentialId()==0? "Non-Essential":"Essential")+"</b></p>";
		text += "<br/>";
		
		if(getAmountCapitalGross()>=30000) {
			text += calc();
			text += "<br/><br/>";
		}else {
			text += "<p>Remarks: "+ (getTypeId()>0? "Gross amount below 30,000.00 is not qualified for renewing for Business Permit." : "Capital less than 30,000.00 is not qualified for Municipal Business Permit.") +"</p>";
		}
		
		text += "<br/><br/>";
		text +="<p><b>Additional requirements. Please disregard if already settled.</b></p>";
		text += "<br/><br/>";
		text += otherRequirements();
		
		setDtls(text);
	}
	
	public void reCalculateBusiness() {
		String text = "";
		if(getTmpCalculated()!=null) {text += getTmpCalculated(); text +="<br/>";}
		text += "<h2>TAXDUE WORKSHEET</h2>";
		text += "<p>Bill No: <b>"+ getSeries() +"</b></p>";
		text += "<p>Bill Date: <b>"+ DateUtils.getCurrentDateMMMMDDYYYY() +"</b></p>";
		text += "<p>Business Name: <b>"+ getBusinessName().toUpperCase() +"</b></p>";
		text += "<p>Owner Name: <b>"+getOwnerName().toUpperCase()+"</b></p>";
		text += "<p>"+ (getTypeId()>0? "Gross" : "Capital") +": "+"<b>"+Currency.formatAmount(getAmountCapitalGross())+"</b></p>";
		text += "<p>Type: <b>"+BusinessType.val(getTypeId()).getName()+"</b></p>";
		text += "<p>Category: <b>"+BusinessCategory.val(getBusinessTypeId()).getName()+"</b></p>";
		text += "<p>For the : <b>"+(getEssentialId()==0? "Non-Essential":"Essential")+"</b></p>";
		text += "<br/>";
		
		if(getAmountCapitalGross()>=30000) {
			//text += calculatePayment();
			text += reCalculatePayment();
			text += "<br/><br/>";
		}else {
			text += "<p>Remarks: "+ (getTypeId()>0? "Gross amount below 30,000.00 is not qualified for renewing for Business Permit." : "Capital less than 30,000.00 is not qualified for Municipal Business Permit.") +"</p>";
		}
		
		text += "<br/><br/>";
		text +="<p><b>Additional requirements. Please disregard if already settled.</b></p>";
		text += "<br/><br/>";
		text += otherRequirements();
		
		setDtls(text);
	}
	
	private String calc() {
		payments = new ArrayList<PaymentName>();
		String text = "";
		int[] ids = new int[0];
		double[] amounts = {5.00};
		int index = 0;
		if(getBusinessTypeId()==BusinessCategory.RETAILER.getId() 
				|| getBusinessTypeId()==BusinessCategory.FINANCIAL_INSTITUTION.getId()
				|| getBusinessTypeId()==BusinessCategory.AMUSEMENT.getId()
				|| getBusinessTypeId()==BusinessCategory.CONTRACTOR.getId()
				|| getBusinessTypeId()==BusinessCategory.EATING_PLACES.getId()
				|| getBusinessTypeId()==BusinessCategory.COTTAGES.getId()
				|| getBusinessTypeId()==BusinessCategory.INSTITUTION.getId()
				|| getBusinessTypeId()==BusinessCategory.MANUFACTURER.getId()
				|| getBusinessTypeId()==BusinessCategory.OTHER_RETAILER.getId()
				|| getBusinessTypeId()==BusinessCategory.OTHER.getId()
				|| getBusinessTypeId()==BusinessCategory.TRADER.getId()
				|| getBusinessTypeId()==BusinessCategory.SALON.getId()
				|| getBusinessTypeId()==BusinessCategory.RESORT.getId()
				|| getBusinessTypeId()==BusinessCategory.MINING.getId()
				|| getBusinessTypeId()==BusinessCategory.MOTORCYCLE_SHOP.getId()) {
			ids = new int[15];
			amounts = new double[15];
			//sanitary fee
			ids[index] = 33;
			amounts[index++] = 40.00;
			//mayor's permit
			ids[index] = 4;
			amounts[index++] = 0.00;
			//police clearance
			ids[index] = 19;
			amounts[index++] = 40.00;
			//other misc income
			ids[index] = 37;
			amounts[index++] = 40.00;
			//business plate fee
			ids[index] = 38;
			amounts[index++] = 350.00;
			//zonal
			ids[index] = 9;
			amounts[index++] = 100.00;
			//occupation tax fee
			ids[index] = 48;
			amounts[index++] = 100.00;
			//inspection fees
			ids[index] = 18;
			amounts[index++] = 40.00;
			//secretary fee
			ids[index] = 20;
			amounts[index++] = 50.00;
			//retailer business
			ids[index] = 1;
			amounts[index++] = 00.00;
			//wholesaler business
			ids[index] = 87;
			amounts[index++] = 00.00;
			//business tax
			//ids[index] = 219;
			//amounts[index++] = 00.00;
			//garbage
			ids[index] = 28;
			amounts[index++] = 275.00;
			//MENRO
			ids[index] = 24;
			amounts[index++] = 50.00;
			//Interest
			ids[index] = 225;
			amounts[index++] = 0.00;
			ids[index] = 91;
			amounts[index++] = 0.00;
		
		}else if(BusinessCategory.FISHERY_RENTAL.getId()==getBusinessTypeId()) {
			ids = new int[9];
			amounts = new double[9];
			//sanitary fee
			ids[index] = 33;
			amounts[index++] = 40.00;
			//inspection fees
			ids[index] = 18;
			amounts[index++] = 40.00;
			//water rental
			ids[index] = 3;
			amounts[index++] = 0.00;
			//police clearance
			ids[index] = 19;
			amounts[index++] = 40.00;
			//mayor's permit
			ids[index] = 4;
			amounts[index++] = 0.00;
			//secretary fee
			ids[index] = 20;
			amounts[index++] = 50.00;
			//dst
			ids[index] = 53;
			amounts[index++] = 30.00;
			//zonal
			ids[index] = 9;
			amounts[index++] = 100.00;
			//banca registration
			ids[index] = 14;
			amounts[index++] = 75.00;
		}else if(BusinessCategory.SKYLAB_PERMIT.getId()==getBusinessTypeId()) {
			ids = new int[8];
			amounts = new double[8];
			//Skylab Registration Permit Fee
			ids[index] = 10;
			amounts[index++] = 50.00;
			//Skylab Filling Permit Fees
			ids[index] = 11;
			amounts[index++] = 180.00;
			//Skylab Sticker
			ids[index] = 12;
			amounts[index++] = 50.00;
			//Fines and Penalty (Permit and License)
			ids[index] = 13;
			amounts[index++] = 0.00;
			//Secretary Fee
			ids[index] = 20;
			amounts[index++] = 50.00;
			//DST
			ids[index] = 53;
			amounts[index++] = 30.00;
			//Skylab ID
			ids[index] = 81;
			amounts[index++] = 50.00;
			//LRF /  MFHOR
			ids[index] = 92;
			amounts[index++] = 0.00;
		}
		
		
		double tarifAnnual = 0d;
		double firstQuarter = 0d;
		double perQuarter = 0d;
		double threeQuarter = 0d;
		//basic tax
		if(getTypeId()==1) {//RENEW
			tarifAnnual = tarifa();
			perQuarter = tarifAnnual/4;
			threeQuarter = perQuarter * 3;
			firstQuarter = tarifAnnual - threeQuarter;// only first quarter is charge
		}
		
		double amount = 0d;
		String sql = "";
		String[] params = new String[0];
		double amountDeductionForRegulatory=0d;
		try {
			text += "Regulatory Fees";
			text += "<ul>";
			for(int i=0; i< ids.length; i++) {
				sql = " AND pyid="+ids[i];
				PaymentName py =  PaymentName.retrieve(sql, params).get(0);
				
				//only renew will have a businesstax
				if(py.getId()==1 && BusinessType.RENEW.getId()==getTypeId()) {//Retailer
					if(getBusinessTypeId()==BusinessCategory.RETAILER.getId() 
							|| getBusinessTypeId()==BusinessCategory.FINANCIAL_INSTITUTION.getId()
							|| getBusinessTypeId()==BusinessCategory.AMUSEMENT.getId()
							|| getBusinessTypeId()==BusinessCategory.CONTRACTOR.getId()
							|| getBusinessTypeId()==BusinessCategory.EATING_PLACES.getId()
							|| getBusinessTypeId()==BusinessCategory.COTTAGES.getId()
							|| getBusinessTypeId()==BusinessCategory.INSTITUTION.getId()
							|| getBusinessTypeId()==BusinessCategory.MANUFACTURER.getId()
							|| getBusinessTypeId()==BusinessCategory.OTHER_RETAILER.getId()
							|| getBusinessTypeId()==BusinessCategory.OTHER.getId()
							|| getBusinessTypeId()==BusinessCategory.TRADER.getId()
							|| getBusinessTypeId()==BusinessCategory.SALON.getId()
							
							|| getBusinessTypeId()==BusinessCategory.RESORT.getId()
							|| getBusinessTypeId()==BusinessCategory.MINING.getId()
							|| getBusinessTypeId()==BusinessCategory.MOTORCYCLE_SHOP.getId()) {
						text += "<li><b>"+py.getName()+ "\t" + Currency.formatAmount(firstQuarter) + " (first quarter) </b></li>";
						amount += firstQuarter;
						py.setAmount(Numbers.formatDouble(firstQuarter));
						payments.add(py);
						amountDeductionForRegulatory += firstQuarter;
					}
				}else if(py.getId()==87 && BusinessType.RENEW.getId()==getTypeId()) {//wholesaler
					if(getBusinessTypeId()==BusinessCategory.WHOLESALER.getId()) {
						text += "<li><b>"+py.getName()+ "\t" + Currency.formatAmount(firstQuarter) + " (first quarter) </b></li>";
						amount += firstQuarter;
						py.setAmount(Numbers.formatDouble(firstQuarter));
						payments.add(py);
						amountDeductionForRegulatory += firstQuarter;
					}
				}else if(py.getId()==219 && BusinessType.RENEW.getId()==getTypeId()) {//Business Tax
					if(getBusinessTypeId()!=BusinessCategory.RETAILER.getId() || getBusinessTypeId()!=BusinessCategory.WHOLESALER.getId()) {
						text += "<li><b>"+py.getName()+ "\t" + Currency.formatAmount(firstQuarter) + " (first quarter) </b></li>";
						amount += firstQuarter;
						py.setAmount(Numbers.formatDouble(firstQuarter));
						payments.add(py);
						amountDeductionForRegulatory += firstQuarter;
					}
				}else if(py.getId()==4) {//mayor permit
					double mayor = mayorPermitTrarif(getAmountCapitalGross());
					text += "<li>"+py.getName()+ "\t" + Currency.formatAmount(mayor) + "</li>";
					amount += mayor;
					py.setAmount(Numbers.formatDouble(mayor));
					payments.add(py);	
				}else {
					text += "<li>"+py.getName()+ "\t" + Currency.formatAmount(amounts[i]) + "</li>";
					amount += amounts[i];
					py.setAmount(Numbers.formatDouble(amounts[i]));
					payments.add(py);
					
					if(91==py.getId() || 225==py.getId()) {//business surcharge and business interest
						amountDeductionForRegulatory += amounts[i];
					}
					
				}
				
			}
			text += "</ul>";
			
		}catch(Exception e) {e.printStackTrace();}
		
		if(getTypeId()==1) {//RENEW
		text += "<br/><br/>";
		
		text += "<h2>Basic Tax : "+ Currency.formatAmount(tarifAnnual) +"</h2>";
		setBasicTax(tarifAnnual);
		//double quarter = tarif/4;
		text += "<p><b>1ST QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		text += "<p><b>2ND QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		text += "<p><b>3RD QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		text += "<p><b>4TH QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		
		text += "<br/><br/>";
		text += "<p>Regulatory fee + first quarter business tax</p>";
		text += "<h2>Total: " + Currency.formatAmount(amount)+"</h2>";
		text += "<p>Regulatory fee + two(2) quarters business tax</p>";
		text += "<h2>Total : Php"+ Currency.formatAmount(firstQuarter+amount) +"</h2>";
		text += "<p/>Regulatory fee + three(3) quarters business tax<p/>";
		text += "<h2>Total : Php"+ Currency.formatAmount((firstQuarter*2)+amount) +"</h2>";
		text += "<p/>Regulatory fee + four(4) quarters business tax<p/>";
		text += "<h2>Total : Php"+ Currency.formatAmount(threeQuarter+amount) +"</h2>";
		}
		//add for new payment
		payments.add(PaymentName.builder().id(0).name("Add here").build());
		
		//business interest
		//business surcharge
		//retailer or wholesaler (busines tax)
		
		setRegulatoryFee(amount-amountDeductionForRegulatory);
		setGrandTotal(threeQuarter+amount);
		return text;
	}
	
	@Deprecated
	private String calculatePayment() {
		
		String text = "";
		
		double tarifAnnual = 0d;
		double firstQuarter = 0d;
		double perQuarter = 0d;
		double threeQuarter = 0d;
		//basic tax
		if(getTypeId()==1) {//RENEW
			tarifAnnual = tarifa();
			perQuarter = tarifAnnual/4;
			threeQuarter = perQuarter * 3;
			firstQuarter = tarifAnnual - threeQuarter;// only first quarter is charge
		}
		
		double amount = 0d;
		
		try {
			text += "Regulatory Fees";
			text += "<ul>";
			List<PaymentName> pays = new ArrayList<PaymentName>();
			for(PaymentName py : getPayments()) {
				//amount += py.getAmount();
				//text += "<li>"+py.getName()+ "\t" + Currency.formatAmount(py.getAmount()) + "</li>";
				
				//only renew has a basic tax
				if(py.getId()==1 && BusinessType.RENEW.getId()==getTypeId()) {//Retailer
					if(getBusinessTypeId()==BusinessCategory.RETAILER.getId() 
							|| getBusinessTypeId()==BusinessCategory.FINANCIAL_INSTITUTION.getId()
							|| getBusinessTypeId()==BusinessCategory.AMUSEMENT.getId()
							|| getBusinessTypeId()==BusinessCategory.CONTRACTOR.getId()
							|| getBusinessTypeId()==BusinessCategory.EATING_PLACES.getId()
							|| getBusinessTypeId()==BusinessCategory.COTTAGES.getId()
							|| getBusinessTypeId()==BusinessCategory.INSTITUTION.getId()
							|| getBusinessTypeId()==BusinessCategory.MANUFACTURER.getId()
							|| getBusinessTypeId()==BusinessCategory.OTHER_RETAILER.getId()
							|| getBusinessTypeId()==BusinessCategory.OTHER.getId()
							|| getBusinessTypeId()==BusinessCategory.TRADER.getId()
							|| getBusinessTypeId()==BusinessCategory.SALON.getId()
							
							|| getBusinessTypeId()==BusinessCategory.RESORT.getId()
							|| getBusinessTypeId()==BusinessCategory.MINING.getId()
							|| getBusinessTypeId()==BusinessCategory.MOTORCYCLE_SHOP.getId()) {
						text += "<li><b>"+py.getName()+ "\t" + Currency.formatAmount(firstQuarter) + " (first quarter) </b></li>";
						amount += firstQuarter;
						py.setAmount(Numbers.formatDouble(firstQuarter));
					}
				}else if(py.getId()==87 && BusinessType.RENEW.getId()==getTypeId()) {//wholesaler
					if(getBusinessTypeId()==BusinessCategory.WHOLESALER.getId()) {
						text += "<li><b>"+py.getName()+ "\t" + Currency.formatAmount(firstQuarter) + " (first quarter) </b></li>";
						amount += firstQuarter;
						py.setAmount(Numbers.formatDouble(firstQuarter));
					}
				}else if(py.getId()==219 && BusinessType.RENEW.getId()==getTypeId()) {//Business Tax
					if(getBusinessTypeId()!=BusinessCategory.RETAILER.getId() || getBusinessTypeId()!=BusinessCategory.WHOLESALER.getId()) {
						text += "<li><b>"+py.getName()+ "\t" + Currency.formatAmount(firstQuarter) + " (first quarter) </b></li>";
						amount += firstQuarter;
						py.setAmount(Numbers.formatDouble(firstQuarter));
					}
				}else if(py.getId()==4) {//mayor permit
					double mayor = mayorPermitTrarif(getAmountCapitalGross());
					text += "<li>"+py.getName()+ "\t" + Currency.formatAmount(mayor) + "</li>";
					amount += mayor;
					py.setAmount(Numbers.formatDouble(mayor));
				}else if(py.getId()==0) {//added
					
				}else {
					if(py.getAmount()>0) {
					text += "<li>"+py.getName()+ "\t" + Currency.formatAmount(py.getAmount()) + "</li>";
					amount += py.getAmount();
					}
				}
				pays.add(py);
				
			}	
			setPayments(pays);//return to original list
			text += "</ul>";
			
		}catch(Exception e) {e.printStackTrace();}
		
		if(getTypeId()==1) {//RENEW
		text += "<br/><br/>";
		
		text += "<h2>Basic Tax : "+ Currency.formatAmount(tarifAnnual) +"</h2>";
		//double quarter = tarif/4;
		text += "<p><b>1ST QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		text += "<p><b>2ND QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		text += "<p><b>3RD QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		text += "<p><b>4TH QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		
		text += "<br/><br/>";
		text += "<p>Regulatory fee + first quarter business tax</p>";
		text += "<h2>Total: " + Currency.formatAmount(amount)+"</h2>";
		text += "<p>Regulatory fee + two(2) quarters business tax</p>";
		text += "<h2>Total : Php"+ Currency.formatAmount(firstQuarter+amount) +"</h2>";
		text += "<p/>Regulatory fee + three(3) quarters business tax<p/>";
		text += "<h2>Total : Php"+ Currency.formatAmount((firstQuarter*2)+amount) +"</h2>";
		text += "<p/>Regulatory fee + four(4) quarters business tax<p/>";
		text += "<h2>Total : Php"+ Currency.formatAmount(threeQuarter+amount) +"</h2>";
		}
		return text;
	}
	
private String reCalculatePayment() {
		
		String text = "";
		
		double tarifAnnual = 0d;
		double firstQuarter = 0d;
		double perQuarter = 0d;
		double threeQuarter = 0d;
		//basic tax
		if(getTypeId()==1) {//RENEW
			tarifAnnual = tarifa();
			perQuarter = tarifAnnual/4;
			threeQuarter = perQuarter * 3;
			firstQuarter = tarifAnnual - threeQuarter;// only first quarter is charge
		}
		
		double amount = 0d;
		double amountDeductionForRegulatory = 0d;
		double basicTax = 0d;
		try {
			text += "Regulatory Fees";
			text += "<ul>";
			List<PaymentName> pays = new ArrayList<PaymentName>();
			for(PaymentName py : getPayments()) {
				if(py.getId()>0) {
					text += "<li>"+py.getName()+ "\t" + Currency.formatAmount(py.getAmount()) + "</li>";
					amount += py.getAmount();
					
					if(91==py.getId() || 225==py.getId()) {//business surcharge and business interest
						amountDeductionForRegulatory += py.getAmount();
					}
					
					if(1==py.getId() || 87==py.getId() || 219==py.getId()) {//retailer, wholesaler and business tax
						amountDeductionForRegulatory += py.getAmount();
						basicTax = py.getAmount();
					}
					
					//else if(91==py.getId() || 225==py.getId()) {//business surcharge and business interest
						
					//}
					
					
				}
				pays.add(py);
			}	
			setPayments(pays);//return to original list
			text += "</ul>";
			
		}catch(Exception e) {e.printStackTrace();}
		
		double tmpAnnual = basicTax * 4;
		//if annual suggested basic tax override by user
		//system will change base on the user input basic tax
		if(tarifAnnual!=tmpAnnual) {
			tarifAnnual = tmpAnnual;
			perQuarter = tarifAnnual/4;
			threeQuarter = perQuarter * 3;
			firstQuarter = tarifAnnual - threeQuarter;// only first quarter is charge
			setBasicTax(tarifAnnual);
		}
		
		if(getTypeId()==1) {//RENEW
			text += "<br/><br/>";
			text += "<h2>Basic Tax : "+ Currency.formatAmount(tarifAnnual) +"</h2>";
			text += "<p><b>1ST QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
			text += "<p><b>2ND QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
			text += "<p><b>3RD QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
			text += "<p><b>4TH QUARTER : "+Currency.formatAmount(perQuarter)+"</b></p>";
		}
		
		text += "<br/><br/>";
		text += "<h2>First Quarter Payable Total : Php"+ Currency.formatAmount(amount) +"</h2>";
		text += "<h2>First and Second Quarter Payable Total : Php"+ Currency.formatAmount(amount+perQuarter) +"</h2>";
		text += "<h2>First to Third Quarter Payable Total : Php"+ Currency.formatAmount(amount+perQuarter+perQuarter) +"</h2>";
		text += "<h2>First to Fourth Quarter Payable Total : Php"+ Currency.formatAmount(amount+perQuarter+perQuarter+perQuarter) +"</h2>";
		
		
		setRegulatoryFee(amount-amountDeductionForRegulatory);
		setGrandTotal(amount+perQuarter+perQuarter+perQuarter);
		
		return text;
	}
	
	public void toggledNewBusiness() {
		if(BusinessType.NEW.getId()==getTypeId()) {
		payments = new ArrayList<PaymentName>();
		long[] ids = new long[11];
		double[] amounts = new double[11];
		//sanitary fee
		ids[0] = 33;
		amounts[0] = 40.00;
		//mayor's permit
		ids[1] = 4;
		amounts[1] = mayorPermitTrarif(getAmountCapitalGross());
		//police clearance
		ids[2] = 19;
		amounts[2] = 40.00;
		//other misc income
		ids[3] = 37;
		amounts[3] = 40.00;
		//business plate fee
		ids[4] = 38;
		amounts[4] = 350.00;
		//zonal
		ids[5] = 9;
		amounts[5] = 100.00;
		//occupation tax fee
		ids[6] = 48;
		amounts[6] = 100.00;
		//inspection fees
		ids[7] = 18;
		amounts[7] = 40.00;
		//secretary fee
		ids[8] = 20;
		amounts[8] = 50.00;
		//garbage
		ids[9] = 28;
		amounts[9] = 275.00;
		//MENRO
		ids[10] = 24;
		amounts[10] = 50.00;
		Map<Long, PaymentName> pays = PaymentName.retrieveAllInMap();
		for(int i=0; i<ids.length; i++) {
			PaymentName py = pays.get(ids[i]);
			py.setAmount(amounts[i]);
			payments.add(py);
		}
		//add for new payment
		payments.add(PaymentName.builder().id(0).name("Add here").build());
		}
		reCalculateBusiness();
	}
	
	private double tarifa() {
		double amount = 0d;
		double tarifAmount = 400000;
		double defaultAmountAdd = 8000;//this the 2% of tarifAmount
		
		if(getBusinessTypeId()==BusinessCategory.RETAILER.getId() 
				|| getBusinessTypeId()==BusinessCategory.FINANCIAL_INSTITUTION.getId()
				|| getBusinessTypeId()==BusinessCategory.AMUSEMENT.getId()
				|| getBusinessTypeId()==BusinessCategory.CONTRACTOR.getId()
				|| getBusinessTypeId()==BusinessCategory.EATING_PLACES.getId()
				|| getBusinessTypeId()==BusinessCategory.COTTAGES.getId()
				|| getBusinessTypeId()==BusinessCategory.INSTITUTION.getId()
				|| getBusinessTypeId()==BusinessCategory.MANUFACTURER.getId()
				|| getBusinessTypeId()==BusinessCategory.OTHER_RETAILER.getId()
				|| getBusinessTypeId()==BusinessCategory.OTHER.getId()
				|| getBusinessTypeId()==BusinessCategory.TRADER.getId()
				|| getBusinessTypeId()==BusinessCategory.SALON.getId()
				|| getBusinessTypeId()==BusinessCategory.WHOLESALER.getId()
				|| getBusinessTypeId()==BusinessCategory.MOTORCYCLE_SHOP.getId()) {	
			
			if(getAmountCapitalGross()<=tarifAmount) {
				if(getEssentialId()==0) {//non-essential
					//amount = getAmountCapitalGross() * 0.01;
					amount = getAmountCapitalGross() * 0.02;
				}else{ //essential
					 //amount = getAmountCapitalGross() * 0.01;
					amount = getAmountCapitalGross()/2;
				}
			//more than 400,000.00	
			}else if(getAmountCapitalGross()>tarifAmount) {
				//calculation for non-essential
				//ex gross 8,000,000.00
				//8,000,000.00
				//-400,000.00
				//============
				//7,600,000.00 x 0.01=76,000.00
				if(getEssentialId()==0) {
					amount = getAmountCapitalGross() - tarifAmount;
					amount = amount * 0.01;
					amount += defaultAmountAdd;
				//calculation for essential	
				}else{ 
					amount = getAmountCapitalGross() /2;
					//amount = amount - tarifAmount;
					//amount = amount * 0.01;
					//amount += defaultAmountAdd;
				}
			}
		}else if(getBusinessTypeId()==BusinessCategory.RESORT.getId()) {
			amount = getAmountCapitalGross() * 0.02;
		}
		
		return amount;
	}
	
	private double mayorPermitTrarif(double capitalGross) {
		double amount = 0d;
		if(capitalGross>=30000 && capitalGross<35000) {
			amount = 55.00;
		}else if (capitalGross>=35000 && capitalGross<40000) {
			amount = 82.50;
		}else if (capitalGross>=40000 && capitalGross<45000) {
			amount = 110.00;
		}else if (capitalGross>=45000 && capitalGross<50000) {
			amount = 165.00;
		}else if (capitalGross>=50000 && capitalGross<55000) {
			amount = 220.00;
		}else if (capitalGross>=55000 && capitalGross<60000) {
			amount = 275.00;
		}else if (capitalGross>=60000 && capitalGross<65000) {
			amount = 330.00;
		}else if (capitalGross>=65000 && capitalGross<70000) {
			amount = 385.00;
		}else if (capitalGross>=70000 && capitalGross<75000) {
			amount = 440.00;
		}else if (capitalGross>=75000 && capitalGross<80000) {
			amount = 495.00;
		}else if (capitalGross>=80000 && capitalGross<85000) {
			amount = 550.00;
		}else if (capitalGross>=85000 && capitalGross<80000) {
			amount = 605.00;
		}else if (capitalGross>=90000 && capitalGross<95000) {
			amount = 715.00;
		}else if (capitalGross>=95000 && capitalGross<100000) {
			amount = 725.00;///
		}else if (capitalGross>=100000 && capitalGross<200000) {
			amount = 770.00;
		}else if (capitalGross>=200000 && capitalGross<300000) {
			amount = 825.00;
		}else if (capitalGross>=300000 && capitalGross<400000) {
			amount = 880.00;
		}else if (capitalGross>=400000 && capitalGross<500000) {
			amount = 935.00;
		}else if (capitalGross>=500000 && capitalGross<750000) {
			amount = 990.00;
		}else if (capitalGross>=750000 && capitalGross<=1000000) {
			amount = 1100.00;
		}else if (capitalGross>=1000001 && capitalGross<=2000000) {
			amount = 1500.00;
		}else if (capitalGross>=2000001 && capitalGross<=3000000) {
			amount = 2500.00;
		}else if (capitalGross>=3000001 && capitalGross<=4000000) {
			amount = 3000.00;
		}else if (capitalGross>=4000001 && capitalGross<=5000000) {
			amount = 3500.00;
		}else if (capitalGross>=5000001 && capitalGross<=7500000) {
			amount = 4000.00;
		}else if (capitalGross>=7500001 && capitalGross<=10000000) {
			amount = 4500.00;
		}else if (capitalGross>=10000001 && capitalGross<=15000000) {
			amount = 5000.00;
		}else if (capitalGross>=15000001) {
			amount = 7500.00;
		}
		
		
		return amount;
	}
	
	public void addPayment() {
		PaymentName name = getMapPaynames().get(getIdName());
		name.setAmount(getAmountNew());
		payments.add(name);
		reCalculateBusiness();
		setAmountNew(0);
	}
	
	public void onCellPayEdit(CellEditEvent event) {
		int index = event.getRowIndex();
		if(payments.get(index).getId()==0) {
			payments.get(index).setAmount(getAmountNew());
		}
		reCalculateBusiness();
	}
	
	public void deletePay(PaymentName py) {
		payments.remove(py);
		reCalculateBusiness();
	}
}
