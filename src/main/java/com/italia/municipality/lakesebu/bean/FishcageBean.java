package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;
import com.italia.municipality.lakesebu.controller.FishCage;
import com.italia.municipality.lakesebu.controller.FishCagePayment;
import com.italia.municipality.lakesebu.controller.WaterRentalsPayment;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.reports.FishCageORs;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
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

@Named
@ViewScoped
@Data
public class FishcageBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 10008089777L;
	
	
	private FishCage fishCageSelected;
	private String ownerName;
	private String ownerAddress;
	private String tenantOwner;
	private String rsbsa;
	private String waterSurveyNo;
	private String fishcageArea;
	private String totalCageArea;
	private String remarks;
	private double amountDue;
	private String searchName;
	private String location;
	private List<FishCage> cages = new ArrayList<FishCage>();
	private Date dateRegister;
	private int orderId;
	private List orders;
	private int yearApplied;
	private int motorizedBoat;
	private int nonMotorizedBoat;
	private String cellphoneNo;
	private int numberOfFishCages;
	private int noOfFunctional;
	private int noOfNonFunctional;
	private String sizeCagePerModule;
	private int noOfAnnualProduction;
	private int noOfTotalStock;
	private int foryearpaid;
	private String barangay; 
	
	//dialog payment
	private Date paymentDate;
	private String orNumber;
	private double amountPaid;
	private List<FishCagePayment> payments = new ArrayList<FishCagePayment>();
	private FishCagePayment paymentSelected;
	private String selectedOwner;
	private FishCage fishPaymentSelected;
	
	//unpaid rentals
	private List<WaterRentalsPayment> payRentals = new ArrayList<WaterRentalsPayment>();
	private String searchInUnpaidName;
	
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private static final String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
	
	private String clerkName=DocumentFormatter.getTagName("clerk-name");
	private String clerkPosition=DocumentFormatter.getTagName("clerk-position");
	private String treasurerName=DocumentFormatter.getTagName("treasurer-name");
	private String treasurerPosition=DocumentFormatter.getTagName("treasurer-position");
	private boolean displayForMto=DocumentFormatter.getTagName("mto-fishery-display-person").equalsIgnoreCase("true")? true : false;
	
	
	private int yearPaidId;
	private List yearPaid;
	
	private boolean enable2018;
	private boolean enable2019;
	private boolean enable2020;
	private boolean enable2021;
	private boolean enable2022;
	private boolean enable2023;
	private boolean enable2024;
	private boolean enable2025;
	private String totalAmountYear;
	private boolean fromsave;
	
	public void onChange(TabChangeEvent event) {
		if("Water Rentals".equalsIgnoreCase(event.getTab().getTitle())) {
			init();
		}else if("Payment Collection Summary".equalsIgnoreCase(event.getTab().getTitle())) {
			enableYear(0);
			//loadUnpaidDtls();
			loadUnpaidAll("limit");
		}
	}
	
	public void enableYear(int id) {
		
		setEnable2018(false);
		setEnable2019(false);
		setEnable2020(false);
		setEnable2021(false);
		setEnable2022(false);
		setEnable2023(false);
		setEnable2024(false);
		setEnable2025(false);
		
		switch(id) {
		case 0:
			setEnable2018(true);
			setEnable2019(true);
			setEnable2020(true);
			setEnable2021(true);
			setEnable2022(true);
			setEnable2023(true);
			setEnable2024(true);
			setEnable2025(true);
			break;
		case 2018:
			setEnable2018(true);
			break;
		case 2019:
			setEnable2019(true);
			break;
		case 2020:
			setEnable2020(true);
			break;	
		case 2021:
			setEnable2021(true);
			break;	
		case 2022:
			setEnable2022(true);
			break;	
		case 2023:
			setEnable2023(true);
			break;	
		case 2024:
			setEnable2024(true);
			break;	
		case 2025:
			setEnable2025(true);
			break;	
		}
			
	}
	
public WaterRentalsPayment yearDtlsAmount(int id, WaterRentalsPayment w, FishCagePayment py) {
		
		switch(id) {
		
		case 2018:
			w.setF8(Currency.formatAmount(py.getAmountPaid()));
			break;
		case 2019:
			w.setF9(Currency.formatAmount(py.getAmountPaid()));
			break;
		case 2020:
			w.setF10(Currency.formatAmount(py.getAmountPaid()));
			break;	
		case 2021:
			w.setF11(Currency.formatAmount(py.getAmountPaid()));
			break;	
		case 2022:
			w.setF12(Currency.formatAmount(py.getAmountPaid()));
			break;	
		case 2023:
			w.setF13(Currency.formatAmount(py.getAmountPaid()));
			break;	
		case 2024:
			w.setF14(Currency.formatAmount(py.getAmountPaid()));
			break;	
		case 2025:
			w.setF15(Currency.formatAmount(py.getAmountPaid()));
			break;	
		}
		
		return w;
	}
	
	public void loadUnpaidAll(String limit) {
		payRentals = new ArrayList<WaterRentalsPayment>();
		String sql = "";
		if(getSearchInUnpaidName()!=null && !getSearchInUnpaidName().isBlank()) {
			sql += " AND owner like '%"+ getSearchInUnpaidName() +"%'";
		}else {
			if(!"all".equalsIgnoreCase(limit)) {
				sql += " LIMIT 10";
			}
		}
		
		List<FishCage> rentals = new ArrayList<FishCage>();
		rentals = FishCage.retrieve(sql, new String[0]);
		
		if(getYearPaidId()>0) {
			enableYear(getYearPaidId());
		}
		double amount = 0d;
		for(FishCage cage : rentals) {
			WaterRentalsPayment w = new WaterRentalsPayment();
			
			w.setF1(cage.getOwnerName());
			w.setF2(cage.getWaterSurveyNo());
			w.setF3(cage.getYearApplied()+"");
			w.setF4(cage.getCageArea());
			w.setF5(cage.getTotalSquareArea());
			w.setF6(cage.getRemarks());
			w.setF7(Currency.formatAmount(cage.getAmountDue()));
			
			sql = " AND py.cid=" + cage.getId();
			
			if(getYearPaidId()==0) {
				sql += " ORDER BY py.paymentDate DESC";
				
				for(FishCagePayment py : FishCagePayment.retrieve(sql, new String[0])) {
					switch(py.getYear()) {
						case 2018: w.setF8(Currency.formatAmount(py.getAmountPaid())); break;
						case 2019: w.setF9(Currency.formatAmount(py.getAmountPaid())); break;
						case 2020: w.setF10(Currency.formatAmount(py.getAmountPaid())); break;
						case 2021: w.setF11(Currency.formatAmount(py.getAmountPaid())); break;
						case 2022: w.setF12(Currency.formatAmount(py.getAmountPaid())); break;
						case 2023: w.setF13(Currency.formatAmount(py.getAmountPaid())); break;
						case 2024: w.setF14(Currency.formatAmount(py.getAmountPaid())); break;
						case 2025: w.setF15(Currency.formatAmount(py.getAmountPaid())); break;
					}
				}
				payRentals.add(w); 
			
			}else {
				
				sql += " AND py.yearPaid=" + getYearPaidId();
				sql += " ORDER BY py.paymentDate DESC";
				
				for(FishCagePayment py : FishCagePayment.retrieve(sql, new String[0])) {
					w = yearDtlsAmount(getYearPaidId(), w, py);
					amount += py.getAmountPaid();
				}
				payRentals.add(w); 
				
			}
			
		}
		setTotalAmountYear(Currency.formatAmount(amount));
	}
	
	public void loadUnpaidDtls() {
		payRentals = new ArrayList<WaterRentalsPayment>();
		String sql = "";
		if(getSearchInUnpaidName()!=null && !getSearchInUnpaidName().isBlank()) {
			sql += " AND owner like '%"+ getSearchInUnpaidName() +"%'";
		}else {
			//sql += " LIMIT 10";
		}
		
		List<FishCage> rentals = new ArrayList<FishCage>();
		rentals = FishCage.retrieve(sql, new String[0]);
		int counter = 1;
		for(FishCage cage : rentals) {
			
			if(counter<=10) {
			WaterRentalsPayment w = new WaterRentalsPayment();
			
			w.setF1(cage.getOwnerName());
			w.setF2(cage.getWaterSurveyNo());
			w.setF3(cage.getYearApplied()+"");
			w.setF4(cage.getCageArea());
			w.setF5(cage.getTotalSquareArea());
			w.setF6(cage.getRemarks());
			w.setF7(Currency.formatAmount(cage.getAmountDue()));
			
			
			sql = " AND py.cid=" + cage.getId();
			if(getYearPaidId()>0) {
				
				sql += " AND py.yearPaid=" + getYearPaidId();
			}
			sql += " ORDER BY py.paymentDate DESC";
			boolean noPayments = true;
			int count = 0;
			
			for(FishCagePayment py : FishCagePayment.retrieve(sql, new String[0])) {
				noPayments = false;
				switch(count) {
					case 0: w.setF8(Currency.formatAmount(py.getAmountPaid())); break;
					case 1: w.setF9(Currency.formatAmount(py.getAmountPaid())); break;
					case 2: w.setF10(Currency.formatAmount(py.getAmountPaid())); break;
					case 3: w.setF11(Currency.formatAmount(py.getAmountPaid())); break;
					case 4: w.setF12(Currency.formatAmount(py.getAmountPaid())); break;
					case 5: w.setF13(Currency.formatAmount(py.getAmountPaid())); break;
					case 6: w.setF14(Currency.formatAmount(py.getAmountPaid())); break;
					case 7: w.setF15(Currency.formatAmount(py.getAmountPaid())); break;
				}
				count++;
			}
			
			if(noPayments) {
				w.setF8("0.00");
				w.setF9("0.00");
				w.setF10("0.00");
				w.setF11("0.00");
				w.setF12("0.00");
				w.setF13("0.00");
				w.setF14("0.00");
				w.setF15("0.00");
			}
			
			if(count!=8) {//this condition is applicable only for year 2020
				payRentals.add(w); 
				counter++;
			}
			
			}
			
		}
		
	}
	
	public void deletePayment(FishCagePayment cage) {
		cage.delete();
		init();
		Application.addMessage(1, "Deleted", "Successfully deleted.");
	}
	
	public void savePayment() {
		FishCagePayment py = new FishCagePayment();
		if(getPaymentSelected()!=null) {
			py = getPaymentSelected();
		}else {
			py.setIsActive(1);
		}
		boolean isOk = true;
		
		if(getOrNumber()==null || getOrNumber().isBlank()) {
			Application.addMessage(3, "Error", "Please provide OR");
			isOk = false;
		}
		
		if(getAmountPaid()==0) {
			Application.addMessage(3, "Error", "Please provide amount");
			isOk = false;
		}
		
		if(isOk) {
			py.setPaymentPaid(DateUtils.convertDate(getPaymentDate(), "yyyy-MM-dd"));
			//py.setYear(Integer.valueOf(DateUtils.convertDate(getPaymentDate(), "yyyy-MM-dd").split("-")[0]));
			py.setYear(getForyearpaid());
			py.setOrNumber(getOrNumber());
			py.setAmountPaid(getAmountPaid());
			py.setPayableAmount(getFishPaymentSelected().getAmountDue());
			//if(getAmountPaid()==getFishPaymentSelected().getAmountDue()) {
				py.setFullpaid(1);
			//}
			py.setFishCage(getFishPaymentSelected());
			py.save();
			clearPayments();
			loadPayments(getFishPaymentSelected());
			Application.addMessage(1, "Success", "Successfully saved.");
		}
	}
	
	public void clearPayments() {
		setOrNumber(null);
		setAmountPaid(0);
		setForyearpaid(DateUtils.getCurrentYear());
	}
	
	public void loadPayments(FishCage cage) {
		System.out.println("loading payments....");
		setFishPaymentSelected(cage);
		setSelectedOwner(cage.getOwnerName());
		payments = new ArrayList<FishCagePayment>();
		String sql = " AND py.cid=" + cage.getId();
		sql += " ORDER BY py.yearPaid DESC";
		payments = FishCagePayment.retrieve(sql, new String[0]);
	}
	
	public void showSelectedPayment(FishCagePayment py) {
		setPaymentSelected(py);
		setPaymentDate(DateUtils.convertDateString(py.getPaymentPaid(), "yyyy-MM-dd"));
		setOrNumber(py.getOrNumber());
		setAmountPaid(py.getAmountPaid());
		setForyearpaid(py.getYear());
	}
	
	public void readExcel() {
		try {
		File file = new File("C:\\bls\\Water.xls");
		String sep = File.separator;
	    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
	    HSSFWorkbook wb = new HSSFWorkbook(fs);
	    HSSFSheet sheet = wb.getSheetAt(0);
	    HSSFRow row;
	    HSSFCell cell;

	    int rows; // No of rows
	    rows = sheet.getPhysicalNumberOfRows();

	    int cols = 0; // No of columns
	    int tmp = 0;
	    
	 // This trick ensures that we get the data properly even if it doesn't start from first few rows
	    for(int i = 0; i < 10 || i < rows; i++) {
	        row = sheet.getRow(i);
	        if(row != null) {
	            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
	            if(tmp > cols) cols = tmp;
	        }
	    }
	    
	    for(int r = 2; r < rows; r++) {//start from row 1 instead of row 0 (row zero is the header information on excel)
	    	row = sheet.getRow(r);
	    	FishCage cage = new FishCage();
	    	cage.setIsActive(1);
	    	cage.setDateRegister(DateUtils.getCurrentDateYYYYMMDD());
	    	FishCagePayment py = new FishCagePayment();
	    	FishCage tmpcage = new FishCage();
	    	if(row != null) {
	    		String area="";
	    		double payableAmount = 0d;
	            for(int c = 0; c < cols; c++) {
	                cell = row.getCell((short)c);
	                if(cell != null) {
	                	
	                	switch(c) {
	                	case 0 : cage.setOwnerName(cellValue(cell)); break;
	                	case 1 : cage.setWaterSurveyNo(cellValue(cell)); break;
	                	
	                	case 2 : area=cellValue(cell);break;
	                	case 3 : cage.setCageArea(area + cellValue(cell));break;
	                	
	                	case 4 : cage.setTotalSquareArea(cellValue(cell)); break;
	                	
	                	case 5 : cage.setRemarks(cellValue(cell)); break;
	                	case 6 : cage.setAmountDue(Double.valueOf(cellValue(cell).replace(",", ""))); payableAmount=cage.getAmountDue(); break;
	                	case 7 : cage=FishCage.save(cage);//save to database
	                			 tmpcage = cage;
	                			 py = new FishCagePayment();
	                			 
	                			 py.setFishCage(tmpcage);
	                			 py.setYear(2018);
	                			 try{py.setAmountPaid(Double.valueOf(cellValue(cell).replace(",", "")));}catch(Exception e) {py.setAmountPaid(0);}
	                			 py.setPayableAmount(payableAmount);
	                			 if(py.getAmountPaid()>0) {
	                				 py.setPaymentPaid("2018-01-03");
	                			 }
	                			 if(payableAmount==py.getAmountPaid()) {
	                				 py.setFullpaid(1);
	                			 }else {
	                				 py.setFullpaid(0);
	                			 }
	                			 py.setIsActive(1);
	                			 if(py.getAmountPaid()>0) {
	                				 py.save(); 
	                			 }
	                			 break;
	                	case 8 : py = new FishCagePayment();
			           			 py.setFishCage(tmpcage);
			           			try{py.setAmountPaid(Double.valueOf(cellValue(cell).replace(",", "")));}catch(Exception e) {py.setAmountPaid(0);}
			           			if(py.getAmountPaid()>0) {
	                				 py.setPaymentPaid("2019-01-03");
	                			 }
			           			py.setPayableAmount(payableAmount);
			           			if(payableAmount==py.getAmountPaid()) {
	                				 py.setFullpaid(1);
	                			 }else {
	                				 py.setFullpaid(0);
	                			 }
			           			 py.setIsActive(1);
			           			 py.setYear(2019);
			           			 if(py.getAmountPaid()>0) {
	                				 py.save(); 
	                			 } 
			           			 break;
	                	case 9 : py = new FishCagePayment();
			           			 py.setFishCage(tmpcage);
			           			try{py.setAmountPaid(Double.valueOf(cellValue(cell).replace(",", "")));}catch(Exception e) {py.setAmountPaid(0);}
			           			py.setPayableAmount(payableAmount);
			           			if(py.getAmountPaid()>0) {
	                				 py.setPaymentPaid("2020-01-03");
	                			 }
			           			if(payableAmount==py.getAmountPaid()) {
	                				 py.setFullpaid(1);
	                			 }else {
	                				 py.setFullpaid(0);
	                			 }
			           			 py.setIsActive(1);
			           			 py.setYear(2020);
			           			 if(py.getAmountPaid()>0) {
	                				 py.save(); 
	                			 } 
			           			 break;
	                	
	                	}
	                	
	                }
	                
	                
	                System.out.println(cage.getOwnerName() + " " + cage.getAmountDue() + " " + py.getOrNumber());
	                
	            }
	    	}
	    	
	    }	
	    init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String cellValue(HSSFCell cell) {
		String tmpNum ="";
		if(CellType.NUMERIC==cell.getCellTypeEnum().NUMERIC) {
			try{tmpNum = cell.getNumericCellValue()+"";}catch(IllegalStateException e) {System.out.println("illegal : " + cell.getStringCellValue()); tmpNum=cell.getStringCellValue();}
			tmpNum = tmpNum.replace(".0", "");
		}else if(CellType.STRING==cell.getCellTypeEnum().STRING) {
			tmpNum = cell.getStringCellValue().replace(".0", "");
		}else if(CellType.BLANK==cell.getCellTypeEnum().BLANK) {
			tmpNum = "";
		}else if(CellType.BOOLEAN==cell.getCellTypeEnum().BOOLEAN) {
			tmpNum = "0";
		}else if(CellType.FORMULA==cell.getCellTypeEnum().FORMULA) {
			tmpNum = "";
		}
		return tmpNum;
	}
	
	private void loadOrderList() {
		orders = new ArrayList<>();
		orders.add(new SelectItem(0, "Survey No"));
		orders.add(new SelectItem(1, "Owner Name"));
		orders.add(new SelectItem(2, "Cage Area"));
		orders.add(new SelectItem(3, "Location"));
		orders.add(new SelectItem(4, "Amount Due"));
	}
	
	private void loadYearPaid() {
		setYearPaidId(0);
		yearPaid = new ArrayList<>();
		yearPaid.add(new SelectItem(0, "Select Year"));
		for(int y=2018; y<=DateUtils.getCurrentYear(); y++) {
			yearPaid.add(new SelectItem(y, y+""));
		}
	}
	
	public void search() {
		if(getSearchName()!=null && getSearchName().length()>3) {
			init(); 	
		}
	}
	
	@PostConstruct
	public void init() {
		setForyearpaid(DateUtils.getCurrentYear());
		setPaymentDate(DateUtils.getDateToday());
		loadOrderList();
		loadYearPaid();
		
		if(getDateRegister()==null) {
			setDateRegister(DateUtils.getDateToday());
		}
		
		cages = new ArrayList<FishCage>();
		String sql=" ";
		
		
		
		if(getSearchName()!=null && !getSearchName().isBlank()) {
			sql = " AND owner like '%"+ getSearchName().replace("--", "") +"%'";
		}
		
		switch(getOrderId()) {
			case 0: sql += " ORDER BY watersurveyno ASC"; break;
			case 1: sql += " ORDER BY owner ASC"; break;
			case 2: sql += " ORDER BY cagearea ASC"; break;
			case 3: sql += " ORDER BY arealocation ASC"; break;
			case 4: sql += " ORDER BY amountdue ASC"; break;
		}
		
		if(getSearchName()!=null && !getSearchName().isBlank()) {
			sql += " LIMIT 10";
		}
		
		cages = FishCage.retrieve(sql, new String[0]);
		
		PrimeFaces pf = PrimeFaces.current();
		if(cages.size()==1) {
			loadPayments(cages.get(0));
			if(isFromsave()==false) {
				pf.executeScript("PF('paymentDlg').show()");
			}else {//this from save, set to false again
				setFromsave(false);
			}
			//pf.executeScript("$(\"#tblPayment\").show(1000);");
		}else {
			//pf.executeScript("$(\"#tblPayment\").hide(1000);");
			//System.out.println("Running on many size=" + cages.size());
		}
		
	}
	
	public void deleteOwner(FishCage cage) {
		cage.delete();
		init();
		Application.addMessage(1, "Deleted", "Successfully deleted.");
	}
	
	public void clear() {
		setYearApplied(DateUtils.getCurrentYear());
		setFishCageSelected(null);
		setOwnerName(null);
		setTenantOwner(null);
		setWaterSurveyNo(null);
		setFishcageArea(null);
		setTotalCageArea(null);
		setRemarks(null);
		setLocation(null);
		setAmountDue(0);
		
		
		setMotorizedBoat(0);
		setNonMotorizedBoat(0);
		setCellphoneNo(null);
		setNumberOfFishCages(0);
		setNoOfFunctional(0);
		setNoOfNonFunctional(0);
		setSizeCagePerModule(null);
		setNoOfAnnualProduction(0);
		setNoOfTotalStock(0);
		
		setOwnerAddress(null);
		setRsbsa(null);
		setBarangay(null);
	}
	
	public void save() {
		boolean isOk = true;
		FishCage cage = new FishCage();
		if(getFishCageSelected()!=null) {
			cage = getFishCageSelected();
		}
		
		if(getOwnerName()==null || getOwnerName().isBlank()) {
			Application.addMessage(2, "Owner field is required", "Please provide owner name");
			isOk = false;
		}
		
		if(getBarangay()==null || getBarangay().isBlank()) {
			Application.addMessage(2, "Barangay field is required", "Please provide owner name");
			isOk = false;
		}
		
		if(getOwnerAddress()==null || getOwnerAddress().isBlank()) {
			Application.addMessage(2, "Owner address field is required", "Please provide owner name");
			isOk = false;
		}
		
		if(getWaterSurveyNo()==null || getWaterSurveyNo().isBlank()) {
			Application.addMessage(2, "Survey no field is required", "Please provide water survey number");
			isOk = false;
		}
		
		if(getFishcageArea()==null || getFishcageArea().isBlank()) {
			Application.addMessage(2, "Fish cage area field is required", "Please provide fish cage area");
			isOk = false;
		}
		
		if(getTotalCageArea()==null || getTotalCageArea().isBlank()) {
			Application.addMessage(2, "Total fish cage area field is required", "Please provide total fish cage area");
			isOk = false;
		}
		
		if(getAmountDue()==0) {
			Application.addMessage(2, "Amount due field is required", "Please provide amount due");
			isOk = false;
		}
		
		if(getLocation()==null || getLocation().isBlank()) {
			Application.addMessage(2, "Location field is required", "Please provide location");
			isOk = false;
		}
		
		if(getYearApplied()==0) {
			Application.addMessage(2, "Year Applied field is required", "Please provide year applied");
			isOk = false;
		}
		
		if(isOk) {
			cage.setDateRegister(DateUtils.convertDate(getDateRegister(), "yyyy-MM-dd"));
			cage.setWaterSurveyNo(getWaterSurveyNo());
			cage.setOwnerName(getOwnerName().toUpperCase());
			cage.setTenantOwner(getTenantOwner());
			cage.setCageArea(getFishcageArea());
			cage.setTotalSquareArea(getTotalCageArea());
			cage.setRemarks(getRemarks());
			cage.setAmountDue(getAmountDue());
			cage.setLocation(getLocation());
			cage.setYearApplied(getYearApplied());
			cage.setIsActive(1);
			
			cage.setMotorizedBoat(getMotorizedBoat());
			cage.setNonMotorizedBoat(getNonMotorizedBoat());
			cage.setCellphoneNo(getCellphoneNo());
			cage.setNumberOfFishCages(getNumberOfFishCages());
			cage.setNoOfFunctional(getNoOfFunctional());
			cage.setNoOfNonFunctional(getNoOfNonFunctional());
			cage.setSizeCagePerModule(getSizeCagePerModule());
			cage.setNoOfAnnualProduction(getNoOfAnnualProduction());
			cage.setNoOfTotalStock(getNoOfTotalStock());
			
			cage.setAddress(getOwnerAddress());
			cage.setRsbsaFishR(getRsbsa());
			cage.setBarangay(getBarangay());
			cage.save();
			clear();
			
			setSearchName(cage.getOwnerName());
			setFromsave(true);
			init();
			Application.addMessage(1, "Success", "Successfully saved.");
		}else {
			Application.addMessage(3, "Error", "Something went wrong. Please check your data.");
		}
		
		
	}
	
	
	public void showSelected(FishCage fs) {
		setYearApplied(fs.getYearApplied());
		setFishCageSelected(fs);
		setOwnerName(fs.getOwnerName());
		setTenantOwner(fs.getTenantOwner());
		setWaterSurveyNo(fs.getWaterSurveyNo());
		setFishcageArea(fs.getCageArea());
		setTotalCageArea(fs.getTotalSquareArea());
		setRemarks(fs.getRemarks());
		setLocation(fs.getLocation());
		setAmountDue(fs.getAmountDue());
		//setAmountPaid(fs.getAmountDue());
		setMotorizedBoat(fs.getMotorizedBoat());
		setNonMotorizedBoat(fs.getNonMotorizedBoat());
		setCellphoneNo(fs.getCellphoneNo());
		setNumberOfFishCages(fs.getNumberOfFishCages());
		setNoOfFunctional(fs.getNoOfFunctional());
		setNoOfNonFunctional(fs.getNoOfNonFunctional());
		setSizeCagePerModule(fs.getSizeCagePerModule());
		setNoOfAnnualProduction(fs.getNoOfAnnualProduction());
		setNoOfTotalStock(fs.getNoOfTotalStock());
		setOwnerAddress(fs.getAddress());
		setRsbsa(fs.getRsbsaFishR());
		setBarangay(fs.getBarangay());
	}
	
	
	
	public void printPaymentHistory() {
		
		String path = REPORT_PATH;// + AppConf.SEPERATOR.getValue();
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT, GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT, path);
		List<FishCageORs> p = new ArrayList<FishCageORs>();
		for(FishCagePayment pay : getPayments()) {
			FishCageORs fs = FishCageORs.builder()
					.f1(pay.getPaymentPaid())
					.f2(pay.getOrNumber())
					.f3(pay.getYear()+"")
					.f4(Currency.formatAmount(pay.getAmountPaid()))
					.build();
			p.add(fs);
		}
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(p);
		
		HashMap param = new HashMap();
		FishCage py = getFishPaymentSelected();
		try{param.put("PARAM_SURVEY_NO", py.getWaterSurveyNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_OWNER_NAME", py.getOwnerName());}catch(NullPointerException e) {}
		try{param.put("PARAM_TENANT_NAME", py.getTenantOwner());}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LOCATION", py.getLocation());}catch(NullPointerException e) {}
		try{param.put("PARAM_DATE_ISSUED_CERT", "Issued on : "+DateUtils.getCurrentDateMMMMDDYYYY());}catch(NullPointerException e) {}
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
				String dalogo = path + "da_logo.png";
				try{File fil = new File(dalogo);
				FileInputStream lof = new FileInputStream(fil);
				param.put("PARAM_DA_LOGO", lof);
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
			            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);

			            // Init servlet response.
			            response.reset();
			            response.setHeader("Content-Type", "application/pdf");
			            response.setHeader("Content-Length", String.valueOf(file.length()));
			            response.setHeader("Content-Disposition", "inline; filename=\"" + GlobalVar.WATER_RENTAL_PAYMENT_HISTORY_RPT + ".pdf" + "\"");
			            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

			            // Write file contents to response.
			            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
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
	
	public void printCertificate(FishCagePayment py) {
		String path = REPORT_PATH;// + AppConf.SEPERATOR.getValue();
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(GlobalVar.WATER_RENTAL_CERTIFICATE_RPT, GlobalVar.WATER_RENTAL_CERTIFICATE_RPT, path);
		List<WaterRentalsPayment> p = new ArrayList<WaterRentalsPayment>();
		p.add(new WaterRentalsPayment());
		
		DocumentFormatter doc = new DocumentFormatter();
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(p);
		
		HashMap param = new HashMap();
		
		try{param.put("PARAM_SURVEY_NO", py.getFishCage().getWaterSurveyNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_OWNER_NAME", py.getFishCage().getOwnerName());}catch(NullPointerException e) {}
		try{param.put("PARAM_TENANT_NAME", py.getFishCage().getTenantOwner());}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LOCATION", py.getFishCage().getLocation());}catch(NullPointerException e) {}
		try{param.put("PARAM_DATE_ISSUED_CERT", "Issued on : "  + DateUtils.convertDateToMonthDayYear(py.getPaymentPaid()));}catch(NullPointerException e) {}
		try{param.put("PARAM_OR_NUMBER", py.getOrNumber());}catch(NullPointerException e) {}
		try{param.put("PARAM_TOTAL_STOCK", py.getFishCage().getNoOfTotalStock()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_ANNUAL_PROD", py.getFishCage().getNoOfAnnualProduction()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_MODULE", py.getFishCage().getSizeCagePerModule());}catch(NullPointerException e) {}
		try{param.put("PARAM_NON_FUNCTIONAL", py.getFishCage().getNoOfNonFunctional()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_FUNCTIONAL", py.getFishCage().getNoOfFunctional()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_NO_CAGES", py.getFishCage().getNumberOfFishCages()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LENTH", py.getFishCage().getCageArea());}catch(NullPointerException e) {}
		try{param.put("PARAM_WATER_AREA", py.getFishCage().getTotalSquareArea());}catch(NullPointerException e) {}
		try{param.put("PARAM_CELLPHONE_NO", py.getFishCage().getCellphoneNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_NON_MOTORIZED", py.getFishCage().getNonMotorizedBoat()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_MOTORIZED", py.getFishCage().getMotorizedBoat()+"");}catch(NullPointerException e) {}
		
		param.put("PARAM_SIGNATURE", py.getFishCage().getOwnerName());
		
		param.put("PARAM_ADDRESS", py.getFishCage().getAddress());
		param.put("PARAM_RSBSA", py.getFishCage().getRsbsaFishR());
		
		param.put("PARAM_OPERATE_YEAR", "Permit to Operate year " + py.getPaymentPaid().split("-")[0]);
		param.put("PARAM_COORDINATOR", doc.getTagName("fishery-coordinator-name").toUpperCase());
		param.put("PARAM_ZONING_OFFICER", doc.getTagName("zoning-name").toUpperCase());
		param.put("PARAM_MAYOR", doc.getTagName("mayor-name").toUpperCase());
		param.put("PARAM_DA_HEAD", doc.getTagName("agriculturist-name").toUpperCase());
		param.put("PARAM_MENRO", doc.getTagName("menro-officer").toUpperCase());
		
		//logo
				String dalogo = path + "da_logo.png";
				try{File fil = new File(dalogo);
				FileInputStream lof = new FileInputStream(fil);
				param.put("PARAM_DA_LOGO", lof);
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
	  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ GlobalVar.WATER_RENTAL_CERTIFICATE_RPT +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
		
				try{
					System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + GlobalVar.WATER_RENTAL_CERTIFICATE_RPT);
		  		 File file = new File(path, GlobalVar.WATER_RENTAL_CERTIFICATE_RPT + ".pdf");
				 FacesContext faces = FacesContext.getCurrentInstance();
				 ExternalContext context = faces.getExternalContext();
				 HttpServletResponse response = (HttpServletResponse)context.getResponse();
					
			     BufferedInputStream input = null;
			     BufferedOutputStream output = null;
			     
			     try{
			    	 
			    	 // Open file.
			            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);

			            // Init servlet response.
			            response.reset();
			            response.setHeader("Content-Type", "application/pdf");
			            response.setHeader("Content-Length", String.valueOf(file.length()));
			            response.setHeader("Content-Disposition", "inline; filename=\"" + GlobalVar.WATER_RENTAL_CERTIFICATE_RPT + ".pdf" + "\"");
			            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

			            // Write file contents to response.
			            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
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
	
	public void printCertificateReg(FishCage py) {
		String path = REPORT_PATH;// + AppConf.SEPERATOR.getValue();
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(GlobalVar.WATER_RENTAL_REG, GlobalVar.WATER_RENTAL_REG, path);
		List<WaterRentalsPayment> p = new ArrayList<WaterRentalsPayment>();
		p.add(new WaterRentalsPayment());
		
		DocumentFormatter doc = new DocumentFormatter();
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(p);
		
		HashMap param = new HashMap();
		
		try{param.put("PARAM_SURVEY_NO", py.getWaterSurveyNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_OWNER_NAME", py.getOwnerName());}catch(NullPointerException e) {}
		try{param.put("PARAM_TENANT_NAME", py.getTenantOwner());}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LOCATION", py.getLocation());}catch(NullPointerException e) {}
		try{param.put("PARAM_CAGE_LENTH", py.getCageArea());}catch(NullPointerException e) {}
		try{param.put("PARAM_WATER_AREA", py.getTotalSquareArea());}catch(NullPointerException e) {}
		try{param.put("PARAM_CELLPHONE_NO", py.getCellphoneNo());}catch(NullPointerException e) {}
		try{param.put("PARAM_NON_MOTORIZED", py.getNonMotorizedBoat()+"");}catch(NullPointerException e) {}
		try{param.put("PARAM_MOTORIZED", py.getMotorizedBoat()+"");}catch(NullPointerException e) {}
		
		try{param.put("PARAM_BARANGAY", "MUNICIPAL FISHCAGES REGISTRY BARANGAY "+py.getBarangay().toUpperCase());}catch(NullPointerException e) {}
		
		try{param.put("PARAM_ADDRESS", py.getAddress().toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_RSBSA", py.getRsbsaFishR().toUpperCase());}catch(NullPointerException e) {}
		
		try{param.put("PARAM_COORDINATOR", doc.getTagName("fishery-coordinator-name").toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_ZONING_OFFICER", doc.getTagName("zoning-name").toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_MAYOR", doc.getTagName("mayor-name").toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_DA_HEAD", doc.getTagName("agriculturist-name").toUpperCase());}catch(NullPointerException e) {}
		try{param.put("PARAM_MENRO", doc.getTagName("menro-officer").toUpperCase());}catch(NullPointerException e) {}
		
		//logo
				String dalogo = path + "da_logo.png";
				try{File fil = new File(dalogo);
				FileInputStream lof = new FileInputStream(fil);
				param.put("PARAM_DA_LOGO", lof);
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
	  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ GlobalVar.WATER_RENTAL_REG +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
		
				try{
					System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + GlobalVar.WATER_RENTAL_REG);
		  		 File file = new File(path, GlobalVar.WATER_RENTAL_REG + ".pdf");
				 FacesContext faces = FacesContext.getCurrentInstance();
				 ExternalContext context = faces.getExternalContext();
				 HttpServletResponse response = (HttpServletResponse)context.getResponse();
					
			     BufferedInputStream input = null;
			     BufferedOutputStream output = null;
			     
			     try{
			    	 
			    	 // Open file.
			            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);

			            // Init servlet response.
			            response.reset();
			            response.setHeader("Content-Type", "application/pdf");
			            response.setHeader("Content-Length", String.valueOf(file.length()));
			            response.setHeader("Content-Disposition", "inline; filename=\"" + GlobalVar.WATER_RENTAL_REG + ".pdf" + "\"");
			            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

			            // Write file contents to response.
			            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
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