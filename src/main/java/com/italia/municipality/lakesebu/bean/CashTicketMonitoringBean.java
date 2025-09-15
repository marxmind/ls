package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.CashTicketMovement;
import com.italia.municipality.lakesebu.controller.CashTicketMovementRpt;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Form11Report;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.CashTicketMovementType;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.enm.Months;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;
import com.italia.municipality.lakesebu.xml.RCDFormDetails;
import com.italia.municipality.lakesebu.xml.RCDFormSeries;
import com.italia.municipality.lakesebu.xml.RCDReader;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named("ckBean")
@ViewScoped
@Data
public class CashTicketMonitoringBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 143543565795764353L;
	private List<CashTicketMovement> moves;
	
	private List collectors;
	private int collectorId;
	private int yearId;
	private List years;
	private int  monthId;
	private List months;
	private int generateTypeId;
	private List generateTypes;
	private boolean rendered;
	
	private List<CashTicketMovementRpt> movements;
	private List collectorMvs;
	private int collectorMvId;
	private int yearMvId;
	private List yearMvs;
	private int  monthMvId;
	private List monthMvs;
	private Map<Integer, String> collectorNames;
	
	@PostConstruct
	public void init() {
		collectorNames = new LinkedHashMap<Integer, String>();
		setRendered(false);
		generateTypes = new ArrayList<>();
		generateTypeId=0;
		generateTypes.add(new SelectItem(0, "Monthly Generation"));
		generateTypes.add(new SelectItem(1, "Yearly Generation"));
		
		moves = new ArrayList<CashTicketMovement>();
		movements = new ArrayList<CashTicketMovementRpt>();
		loadCollector();
		loadYear();
		loadMonths();
	}
	
	/**
	 * //get the beginning balance of the selected month
		
		//get received form
		
		//get issued form
		
		//calculate unremitted balance
	 */
	public void generateMovement() {
		movements = new ArrayList<CashTicketMovementRpt>();
		
		//get the beginning balance of the selected month
		double balance = CashTicketMovement.getBalance(CashTicketMovementType.BEGINNING.getId(), getCollectorMvId(), getMonthMvId(), getYearMvId());
		
		//get received form
		List<String> receivedForms = CashTicketMovement.getReceivedForms(getCollectorMvId(), getMonthMvId(), getYearMvId());
		
		//get issued form
		List<String> issuedForms = CashTicketMovement.getIssuedForms(getCollectorMvId(), getMonthMvId(), getYearMvId());
		
		//formulate report
		int month = getMonthMvId()==1? 12 : getMonthMvId() - 1;
		CashTicketMovementRpt rpt = CashTicketMovementRpt.builder().f2(DateUtils.getMonthName(month)).f4(Currency.formatAmount(balance)).build();
		movements.add(rpt);//beginning balance
		
		double totalRecievedForms = 0d;
		for(String val : receivedForms) {
			String[] v = val.split("<*>");
			double amount = Double.valueOf(v[2].replace(",", ""));
			rpt = CashTicketMovementRpt.builder().f2(v[0].replace("<*", "")).f3(v[1].replace("<*", "")).f4(Currency.formatAmount(amount)).build();
			totalRecievedForms += amount;
			movements.add(rpt);//add received forms
		}
		//total of received plus beginning balance
		rpt = CashTicketMovementRpt.builder().f2("Total").f4(Currency.formatAmount(totalRecievedForms + balance)).build();
		movements.add(rpt);
		
		double totalIssuedForms = 0d;
		int count = 1;
		for(String val : issuedForms) {
			String[] v = val.split("<*>");
			double amount = Double.valueOf(v[1].replace(",", ""));
			
			int size = movements.size();
			if(count<=size) {
				totalIssuedForms += amount;
				movements.get(count-1).setF5(v[0].replace("<*", ""));
				movements.get(count-1).setF6(v[1]);
			}else {
				rpt = CashTicketMovementRpt.builder().f5(v[0].replace("<*", "")).f6(Currency.formatAmount(amount)).build();
				movements.add(rpt);
				totalIssuedForms += amount;
			}
			
			count++;
		}
		//Total issued
		rpt = CashTicketMovementRpt.builder().f5("Total").f6(Currency.formatAmount(totalIssuedForms)).build();
		movements.add(rpt);
		
		//add balance
		double endingBalance = (balance + totalRecievedForms) - totalIssuedForms;
		rpt = CashTicketMovementRpt.builder().f7(Currency.formatAmount(endingBalance)).build();
		movements.add(rpt);
		
		for(int i=0; i<movements.size(); i++) {
			int num = i+1;
			movements.get(i).setF1(num+"");
		}
		
		
	}
	
	public void changeAmount(CellEditEvent event) {
		Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        
        System.out.println("Old Value   "+ event.getOldValue()); 
        System.out.println("New Value   "+ event.getNewValue()); 
        System.out.println("Row Index   "+ event.getRowIndex());
        
        getMoves().get(index).save();
        Application.addMessage(1, "Success", "Successfully saved");
	}  
	
	public void deleteItem(CashTicketMovement ct) {
		ct.delete();
		Application.addMessage(1, "Success", "Successfully deleted");
		loadCashTicket();
	}
	
	public void updateFilter() {
		
		if(getGenerateTypeId()==1) {
			setRendered(true);
		}else {
			setRendered(false);
		}
		System.out.println("update filter: " + isRendered());
	}
	
	private void loadMonths() {
		monthId = DateUtils.getCurrentMonth();
		monthMvId = monthId;
		months = new ArrayList<>();
		monthMvs = new ArrayList<>();
		for(Months month : Months.values()) {
			if(month.getId()>=1 && month.getId()<=12) {
				months.add(new SelectItem(month.getId(), month.getName()));
				monthMvs.add(new SelectItem(month.getId(), month.getName()));
			}
		}
	}
	
	private void loadYear() {
		int startYear=DateUtils.getCurrentYear();
		int yearNow = DateUtils.getCurrentYear();
		years = new ArrayList<>();
		yearMvs = new ArrayList<>();
		ResultSet rs = OpenTableAccess.query("SELECT year(datetrans) as year FROM stockreceipt WHERE isactivestock=1 AND statusstock=2 LIMIT 1", new String[0], new WebTISDatabaseConnect());
		try {
			while(rs.next()) {
				startYear = rs.getInt("year");
			}
			
			yearId = DateUtils.getCurrentYear();//default year
			yearMvId = yearId;
			for(int y=startYear; y<=yearNow; y++) {
				years.add(new SelectItem(y, y+""));
				yearMvs.add(new SelectItem(y, y+""));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadCollector() {
		collectors = new ArrayList<>();
		collectorMvs = new ArrayList<>();
		collectors.add(new SelectItem(0, "Select Collector"));
		collectorMvs.add(new SelectItem(0, "Select Collector"));
		String sql = "SELECT distinct isid FROM logissuedform WHERE isactivelog=1 AND (formtypelog="+ FormType.CT_20.getId() +" OR formtypelog="+ FormType.CT_5.getId() +") order by isid";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		int cnt = 1;
		
		try {
			sql = " AND (";
			while(rs.next()) {
				if(cnt==1) {
					sql += " cl.isid=" + rs.getInt("isid");
				}else {
					sql += " OR cl.isid=" + rs.getInt("isid");
				}
				cnt++;
			}
			sql += " ) ";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sql += " ORDER By cl.collectorname";
		for(Collector c : Collector.retrieve(sql, new String[0])) {
			collectors.add(new SelectItem(c.getId(), c.getName()));
			collectorMvs.add(new SelectItem(c.getId(), c.getName()));
			collectorNames.put(c.getId(), c.getName());
		}
	}
	
	public void generateCT() {
		if(getGenerateTypeId()==0) {//monthly generation
			createBeginningBalance(
					getYearId(), 
					getMonthId(), 
					CashTicketMovementType.BEGINNING.getId(), getCollectorId());
		}else {//yearly generation
		
			int monthToday = DateUtils.getCurrentMonth();
			int yearNow = DateUtils.getCurrentYear();
			for(int year=getYearId(); year<=yearNow; year++) {
				for(int month=1; month<=12; month++) {
					if(year<yearNow) {
						createBeginningBalance(
								year, 
								month, 
								CashTicketMovementType.BEGINNING.getId(), getCollectorId());
						
					}else {
						if(month<=monthToday) {//only generate month this month and previous months
						createBeginningBalance(
								year, 
								month, 
								CashTicketMovementType.BEGINNING.getId(), getCollectorId());
						}
					}
				}
			}
		}
		loadCashTicket();
	}
	
	public void loadCashTicket() {
		
		moves = CashTicketMovement.retrieve(" AND ct.isid="+ getCollectorId() +" ORDER BY ct.movid,ct.monthm", new String[0]);
		
	}
	
	/**
	 * 
	 * @param fundid
	 * @param year=current year
	 * @param month=current month
	 * @param balanceType=CashTicketMovementType.ENDING.getId()
	 * @return create balance
	 */
	private static void createBeginningBalance(int year, int month, int balanceType, int collectorId) {
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1");
		if(month==1) {//if current month is january change year to last year and month to december
			year = year - 1;
			month = 12;
			//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 6");
			Object[] obj = isExist(year, month, balanceType, collectorId);
			boolean isPrevPresent = (boolean)obj[0];
			double prevBalance = (double)obj[1];
			if(isPrevPresent) {
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 7");
				createBeginning(year+1, 1, balanceType, collectorId, prevBalance);
			}else {
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 8");
				//if not present the previous ending balance create the new one
				double endingBalanceTotal = createEndingBalance(year, month, collectorId);
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 9 ending balance: " + endingBalanceTotal);
				//then create the current month beginning balance
				createBeginning(year+1, 1, balanceType, collectorId, endingBalanceTotal);
				
			}
			
		}else {
			//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2");
			Object[] obj = isExist(year, month, balanceType, collectorId);
			boolean isPrevPresent = (boolean)obj[0];
			double prevBalance = (double)obj[1];
			if(isPrevPresent) {
				createBeginning(year, month, balanceType, collectorId, prevBalance);
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 3");
			}else {
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 4");
				//if not present the previous ending balance create the new one
				double endingBalanceTotal = createEndingBalance(year, month-1, collectorId);
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 5 ending balance: " + endingBalanceTotal);
				//then create the current month beginning balance
				createBeginning(year, month, balanceType, collectorId, endingBalanceTotal);
				
			}
		}
			
	}
	
	private static void createBeginning(int year, int month, int balanceType, int collectorId, double prevBalance) {
		Object[] objCurrent = isExist(year, month, balanceType, collectorId);
		boolean isPresent = (boolean)objCurrent[0];
		double currentBalance = (double)objCurrent[1];
		long id = (long)objCurrent[2];
		
		if(isPresent) {
			CashTicketMovement.builder()
			.id(id)
			.year(year)
			.month(month)
			.balanceType(balanceType)
			.balance(currentBalance)
			.collector(Collector.builder().id(collectorId).build())
			.isActive(1)
			.build().save();
		}else {
			CashTicketMovement.builder()
			.balanceType(CashTicketMovementType.BEGINNING.getId())
			.year(year)
			.month(month)
			.balance(prevBalance)//for beginning balance of the current month
			.collector(Collector.builder().id(collectorId).build())
			.isActive(1)
			.balance(prevBalance).build().save();
		}
	}
	
	//get beginning balance
	//get acquired cash ticket
	//get all issued cash ticket
	private static double createEndingBalance(int year, int month, int collectorId) {
		String sql = "SELECT balance FROM cashticketmovement WHERE isactivemov=1 AND "
				+ "yearm="+year + " AND monthm=" 
				+ month + " AND balancetype="+ CashTicketMovementType.BEGINNING.getId() 
				+ " AND isid=" + collectorId;
		double beginningBalance = 0d;
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		try {
			while (rs.next()) {
				beginningBalance = rs.getDouble("balance");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("Beginning balance: " + beginningBalance);
		//get all issued cash ticket
		//System.out.println("get all cash ticket for the month of " + DateUtils.getMonthName(month));
		sql = "SELECT logpcs,formtypelog FROM logissuedform WHERE isactivelog=1 AND isid="+ 
		collectorId + " AND month(issueddate)=" + month + " AND year(issueddate)=" + year + 
		" AND (formtypelog=" + FormType.CT_20.getId() + " OR formtypelog=" +FormType.CT_5.getId()+")" ;
		rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		double amount_2 = 0d, amount_5 = 0d;
		double totalStabAmount = 0d;
		try {
			while (rs.next()) {
				if(FormType.CT_20.getId()==rs.getInt("formtypelog")) {
					amount_2 += rs.getInt("logpcs") * 20;
				}else if(FormType.CT_5.getId()==rs.getInt("formtypelog")) {
					amount_5 += rs.getInt("logpcs") * 5;
				}	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		totalStabAmount = amount_2 + amount_5;
		//System.out.println("amount_2: " + amount_2 + " amount_5: " + amount_5 + " total received cash ticket: "+ totalStabAmount);
		double amountIssued=0d;
		sql = "SELECT sum(amount) as totalissued FROM collectioninfo WHERE isactivecol=1 AND isid="+collectorId + 
				" AND (formtypecol="+ FormType.CT_20.getId() +" OR formtypecol="+FormType.CT_5.getId() +") AND month(receiveddate)=" + month + " AND year(receiveddate)=" + year;
		//System.out.println("Checking sql for issued cash ticket : " + sql);
		rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		try {
			while (rs.next()) {
				amountIssued = rs.getDouble("totalissued");	
				System.out.println("amountIssued: " + rs.getDouble("totalissued") + " total :" + amountIssued);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("total issued cash ticket: " + amountIssued);
		double totalB = beginningBalance + totalStabAmount;
		double totalEndingBalance = totalB==0? 0 : totalB - amountIssued;
		//System.out.println("total ending balance: " + totalEndingBalance);
		//create data in database
		CashTicketMovement.builder()
		.year(year)
		.month(month)
		.collector(Collector.builder().id(collectorId).build())
		.balanceType(CashTicketMovementType.ENDING.getId())
		.balance(totalEndingBalance)
		.isActive(1)
		.build()
		.save();
		
		return totalEndingBalance;
	}

	
	private static Object[] isExist(int year, int month, int balanceType, int collectorId) {
		Object[] obj = new Object[3];
		obj[0]= false;
		obj[1] = 0.00;
		obj[2] = 0l;
		String sql = "SELECT balance,movid FROM cashticketmovement WHERE isactivemov=1 AND "
				+ "yearm="+year + " AND monthm=" 
				+ month + " AND balancetype="+ balanceType 
				+ " AND isid=" + collectorId;
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		try {
			while (rs.next()) {
				obj[0]= true;
				obj[1] = rs.getDouble("balance");
				obj[2] = rs.getLong("movid");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public void print() {
		
			
			String REPORT_PATH = GlobalVar.REPORT_FOLDER;
			String REPORT_NAME = GlobalVar.CASH_TICKET_MOVEMENT_RPT;
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
			
			List<CashTicketMovementRpt> reports = new ArrayList<CashTicketMovementRpt>();
			//CashTicketMovementRpt rpt = new CashTicketMovementRpt();
			for(CashTicketMovementRpt rpt : getMovements()) {
				reports.add(rpt);
			}
			JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
	  		HashMap param = new HashMap();
	  		
	  		param.put("PARAM_COLLECTOR",getCollectorNames().get(getCollectorMvId()));
	  		
	  		
	  		//logo
			String officialLogo = REPORT_PATH + "logo.png";
			try{File file = new File(officialLogo);
			FileInputStream off = new FileInputStream(file);
			param.put("PARAM_LOGO", off);
			}catch(Exception e){e.printStackTrace();}
			
			//logo
			String officialLogotrans = REPORT_PATH + "logotrans.png";
			try{File file = new File(officialLogotrans);
			FileInputStream off = new FileInputStream(file);
			param.put("PARAM_LOGO_TRANS", off);
			}catch(Exception e){e.printStackTrace();}
	  		
	  		try{
		  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
		  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
		  		}catch(Exception e){e.printStackTrace();}
				
		  		try{
		  		File file = new File(REPORT_PATH, REPORT_NAME + ".pdf");
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
			            response.setHeader("Content-Disposition", "inline; filename=\"" + REPORT_NAME + ".pdf" + "\"");
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
