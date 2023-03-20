package com.italia.municipality.lakesebu.bean;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.TabChangeEvent;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.italia.municipality.lakesebu.controller.AppSetting;
import com.italia.municipality.lakesebu.controller.Cedula;
import com.italia.municipality.lakesebu.controller.Client;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.FishcageBillingStatment;
import com.italia.municipality.lakesebu.controller.GenCollection;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.NationalIDJsonData;
import com.italia.municipality.lakesebu.controller.NumberToWords;
import com.italia.municipality.lakesebu.controller.OR51;
import com.italia.municipality.lakesebu.controller.ORListing;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.controller.PoblacionCustomer;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.ReportFields;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.TaxCodeGroup;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AccessLevel;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.ClientStatus;
import com.italia.municipality.lakesebu.enm.ClientTransactionType;
import com.italia.municipality.lakesebu.enm.FormORTypes;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.Months;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Numbers;
import com.italia.municipality.lakesebu.utils.OrlistingXML;
import jakarta.annotation.PostConstruct;
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
 * @since 04/23/2019
 * @version 1.0
 *
 *
 */
@Named
@ViewScoped
public class ORListingBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 645698875451L;
	
	@Setter @Getter private Date dateTrans;
	@Setter @Getter private String orNumber;
	@Setter @Getter private String payorName;
	@Setter @Getter private String address;
	@Setter @Getter private int formTypeId;
	@Setter @Getter private List formTypes;
	
	@Setter @Getter private List<PaymentName> namesData = new ArrayList<PaymentName>();
	@Setter @Getter private List<PaymentName> suggestedData = new ArrayList<PaymentName>();
	@Setter @Getter private List<PaymentName> namesDataSelected = new ArrayList<PaymentName>();
	@Setter @Getter private Map<Long, PaymentName> selectedPaymentNameMap = new HashMap<Long, PaymentName>();
	@Setter @Getter private String totalAmount;
	@Setter @Getter private String searchPayName;
	
	@Setter @Getter private ORListing orRecordData;
	@Setter @Getter private List<ORNameList> ornameListData;
	@Setter @Getter private List<ORListing> ors = new ArrayList<ORListing>();
	@Setter @Getter private List<ORListing> selections = new ArrayList<ORListing>();
	@Setter @Getter private String totalCollection;
	
	@Setter @Getter private String searchName;
	@Setter @Getter private int monthId;
	@Setter @Getter private List months;
	@Setter @Getter private int yearId;
	@Setter @Getter private List years;
	@Setter @Getter private int paymentId;
	@Setter @Getter private List paymentTypes;
	@Setter @Getter private int formTypeSearchId;
	@Setter @Getter private List formTypeSearch;
	
	@Setter @Getter private List<Reports>  dtls = new ArrayList<Reports>();
	@Setter @Getter private List<Reports>  rpts = new ArrayList<Reports>();
	
	@Setter @Getter private List collectors;
	@Setter @Getter private int collectorId;
	
	@Setter @Getter private List collectorsSearch;
	@Setter @Getter private int collectorSearchId;
	
	@Setter @Getter private List sorts;
	@Setter @Getter private int sortId;
	
	@Setter @Getter private int statusId;
	@Setter @Getter private List status;
	
	@Setter @Getter private int statusSearchId;
	@Setter @Getter private List statusSearch;
	
	@Setter @Getter private boolean filtered;
	
	@Setter @Getter private String sumNames;
	@Setter @Getter private String sumAmounts;
	
	@Setter @Getter private int depId;
	@Setter @Getter private List departments;
	
	@Setter @Getter private String textContent;
	@Setter @Getter private String rptSummary;
	
	@Setter @Getter List<Reports> rptsOnly = new ArrayList<Reports>();
	@Setter @Getter private String totalAmountSummaryOnly;
	
	@Setter @Getter private String limitData;
	
	@Setter @Getter private int selectOrTypeId;
	@Setter @Getter private List selectOrTypes;
	
	@Setter @Getter private boolean collectorsMode;
	@Setter @Getter private String formInfo;
	
	//for ctc individual and corporation
	@Setter @Getter private double label2;
	@Setter @Getter private double label3;
	@Setter @Getter private double label4;
	@Setter @Getter private double amount1;
	@Setter @Getter private double amount2;
	@Setter @Getter private double amount3;
	@Setter @Getter private double amount4;
	@Setter @Getter private double amount5;
	@Setter @Getter private double amount6;
	@Setter @Getter private double amount7;
	@Setter @Getter private String tinNo;
	@Setter @Getter private String hieghtDateReg;
	@Setter @Getter private String weight;
	@Setter @Getter private String customerAddress;
	//private String civilStatusOrganization;
	@Setter @Getter private String professionBusinessNature;
	@Setter @Getter private String signatory;
	@Setter @Getter private String placeOfBirth="Lake Sebu";
	@Setter @Getter private String citizenshipOrganization="FILIPINO";
	
	@Setter @Getter private int civilStatusId;
	@Setter @Getter private List civilStatus;
	
	@Setter @Getter private int genderId;
	@Setter @Getter private List genders;
	
	@Setter @Getter private Date birthdate = DateUtils.getDateToday();
	@Setter @Getter private boolean enableBirthday=true;
	
	@Setter @Getter  private String qrcodeMsg="Please place the QRCOde on the camera";
	@Setter @Getter private Customer customerDataSelected;
	@Setter @Getter private Map<String, Customer> mapCustomer;
	
	@Setter @Getter  private boolean alreadyRetrieve=false;
	@Setter @Getter private int issuedCollectorId;
	
	@Setter @Getter private String firstName;
	@Setter @Getter private String middleName;
	@Setter @Getter private String lastName;
	
	@Setter @Getter private String modeName="Collector Mode Off";
	
	@Setter @Getter private List<TaxCodeGroup> groups;
	@Setter @Getter private String searchNameAll;
	@Setter @Getter private List<ORListing> orsAll;
	@Setter @Getter private String totalCollectionAll;
	@Setter @Getter private String notes;
	
	@Setter @Getter private String fullnameQR;
	
	@Setter @Getter  private List<ORListing> selectedCollection;
	
	@Setter @Getter private String tabCurrent="first";
	
	@Setter @Getter private double totalSelectedAmount;
	
	@Setter @Getter private long orsReportsId;
	@Setter @Getter private List orsReports;
	//@Setter @Getter private long genReportId;
	
	@Setter @Getter private long reportId;
	@Setter @Getter private List reports;
	@Setter @Getter private List<ORListing> finalRpts;
	@Setter @Getter private double selectedFinalAmount;
	
	@Setter @Getter private long rptsCombId;
	@Setter @Getter private List rptsCombos;
	
	@Setter @Getter private double monthlyIncome;
	@Setter @Getter private double monthlySal;
	@Setter @Getter private double monthlyTax;
	
	@Setter @Getter private List<Client> clients;
	@Setter @Getter private String queYear;
	@Setter @Getter private String queMonth;
	@Setter @Getter private String queDay;
	@Setter @Getter private String queSequence;
	@Setter @Getter private Client clientSelected;
	@Setter @Getter private String searchParam;
	@Setter @Getter private List<ORListing> orRpts;
	@Setter @Getter private int formIdRpt;
	@Setter @Getter private List formsRpt;
	@Setter @Getter private String totalCollectionToday;
	@Setter @Getter private String checkSeriesForm;
	@Setter @Getter private List<ORListing> orsSeries;
	@Setter @Getter private List<ORListing> orsDuplicate;
	@Setter @Getter private String orNumberRedColor="";
	@Setter @Getter private List<Date> rangeDate;
	@Setter @Getter private String[] proffesionsList;
	
	public void validateSeries() {
		boolean isExist = ORListing.isExistingSeries(getCollectorId(), getOrNumber(), getFormTypeId());
		if(isExist) {
			setOrNumberRedColor("background-color: red; color: black; font-weight: bold");
			String val = getOrNumber().substring(0, getOrNumber().length()-1);
			setOrNumber(val);
			Application.addMessage(2, "Warning", "Series is existing please change");
		}else {
			setOrNumberRedColor("");
		}
	}
	
	public void loadFinalSelectedRpt() {
		PrimeFaces pf = PrimeFaces.current();
		int month = DateUtils.getCurrentMonth();
		int year = DateUtils.getCurrentYear();
		String valorsReportsId = month>9? month+"" : "0"+month; 
		valorsReportsId += "-" + year;
		List<GenCollection> cols = GenCollection.retrieve("", new String[0]);
		reports = new ArrayList<>();
		if(cols!=null && cols.size()>0) {
			for(GenCollection gen : cols){
				reports.add(new SelectItem(gen.getId(), gen.getMonthYear()));
				if(valorsReportsId.equalsIgnoreCase(gen.getMonthYear())) {
					setReportId(gen.getId());
				}
			}
			loadFinalGroupRpt();
			pf.executeScript("PF('dlgFinalRpt').show()");
		}	
	}
	
	public void loadFinalGroupRpt() {
		finalRpts = new ArrayList<ORListing>();
		
		Object[] obj = ORListing.retrieveGenReportGroup(getReportId(),null);
		double tmpAmount = (Double)obj[0];
		finalRpts = (ArrayList<ORListing>)obj[1];
		/*for(ORListing or : ORListing.retrieveGenReportGroup(getReportId())) {
			tmpAmount += or.getAmount();
			finalRpts.add(or);
		}*/
		setSelectedFinalAmount(Numbers.roundOf(tmpAmount, 2));
	}
	
	@PostConstruct
	public void init() {
		Date dateRpt = DateUtils.getDateToday();
		rangeDate = new ArrayList<Date>();
		rangeDate.add(dateRpt);
		rangeDate.add(dateRpt);
		System.out.println("range size:"+getRangeDate().size());
		
		
		int mo = DateUtils.getCurrentMonth();
		String month = mo <10? "0"+mo : mo+"";
		String year = DateUtils.getCurrentYear()+"";
		int dy = DateUtils.getCurrentDay();
		String day = dy<10? "0"+dy : dy+"";
		setQueYear(year);
		setQueMonth(month);
		setQueDay(day);
		
		if(collectorsMode==false) {
			
			String mode = AppSetting.getCollectorMode(getUser().getUserDtls());
			
			//if("ON".equalsIgnoreCase(RCDReader.readCollectorMode())){
			if("ON".equalsIgnoreCase(mode)){
				collectorsMode = true;
				setModeName("Collector Mode On");
			}
		}
		
		
		System.out.println("Initialized....");
		if(!alreadyRetrieve) {
			issuedCollectorId = Login.getUserLogin().getCollectorId();
			setCollectorSearchId(issuedCollectorId);
			alreadyRetrieve=true;
		}
		loadOtherInfo();
		//setLimitData("10");
		setLimitData("1");
		load();
		
		suggestedInfo();
		
		clearAllFlds();
		
		
		loadClient();
		proffesionsList = Words.getTagName("profession-list").split(",");
	}
	
	public void updateInfo() {
		
		orNumber = ORListing.getLatestORNumber(getFormTypeId(),getCollectorId());
		
		setLimitData("1");
		load();
		//suggestedInfo(); temporary disabled 03/04/2022 due to slow loading... when saving
		
		dateTrans = DateUtils.getDateToday();
		address = "Lake Sebu, So. Cot.";
	}
	
	private void loadOtherInfo() {
		
		//if(dateTrans==null) {
			dateTrans = DateUtils.getDateToday();
		//}
		
		
		setFormTypeId(FormType.AF_51.getId());//default form
		formTypes = new ArrayList<>();
		formTypes.add(new SelectItem(0, "Select Forms"));
		for(FormType form : FormType.values()) {
			formTypes.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
		}
		
		//if(address==null) {
			address = "Lake Sebu, So. Cot.";
		//}
		
		/////
		paymentTypes = new ArrayList<>();
		paymentTypes.add(new SelectItem(0, "All Payment Names"));
		for(PaymentName name : PaymentName.retrieve("", new String[0])) {
			paymentTypes.add(new SelectItem(name.getId(), name.getName()));
		}
		
		////month
		if(monthId==0) {
			monthId = DateUtils.getCurrentMonth();
		}
		
		months = new ArrayList<>();
		months.add(new SelectItem(0, "All Months"));
		for(int month=1; month<=12; month++) {
			months.add(new SelectItem(month, DateUtils.getMonthName(month)));
		}
		
		/////year
		if(yearId==0) {
			yearId = DateUtils.getCurrentYear();
		}
		
		years = new ArrayList<>();
		years.add(new SelectItem(0, "All years"));
		int lastYear=0;
		for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
			lastYear=year;
		}
		lastYear += 1;
		years.add(new SelectItem(lastYear, lastYear+""));
		///////////////
		
		
		if(birthdate==null) {
			birthdate = DateUtils.getDateToday();
		}
		if(civilStatusId==0) {
			civilStatusId = 1;
		}
		
		if(genderId==0) {
			genderId=1;
		}
		
		genders = new ArrayList<>();
		genders.add(new SelectItem(1, "Male"));
		genders.add(new SelectItem(2, "Female"));
		
		selectOrTypes = new ArrayList<>();
		for(FormORTypes type : FormORTypes.values()) {
			selectOrTypes.add(new SelectItem(type.getId(), type.getName()));
		}
		
		civilStatus = new ArrayList<>();
		for(CivilStatus s : CivilStatus.values()) {
			civilStatus.add(new SelectItem(s.getId(), s.getName()));
		}
		
		statusSearch = new ArrayList<>();
		statusSearch.add(new SelectItem(0, "All Status"));
		for(FormStatus s : FormStatus.values()) {
			if(s.getId()==4 || s.getId()==5) {
				statusSearch.add(new SelectItem(s.getId(), s.getName()));
			}
		}
		
		departments = new ArrayList<>();
		departments.add(new SelectItem(0, "All Department"));
		for(Department dep : Department.retrieve("SELECT * FROM department ORDER BY departmentname", new String[0])) {
			departments.add(new SelectItem(dep.getDepid(), dep.getDepartmentName()));
		}
		
		if(statusId==0) {
			statusId = 4;
		}
		
		status = new ArrayList<>();
		for(FormStatus s : FormStatus.values()) {
			if(s.getId()==4 || s.getId()==5) {
				status.add(new SelectItem(s.getId(), s.getName()));
			}
		}
		
		sorts = new ArrayList<>();
		sorts.add(new SelectItem(0, "Non Sort"));
		sorts.add(new SelectItem(1, "Order by Date"));
		sorts.add(new SelectItem(2, "Order by Collector"));
		sorts.add(new SelectItem(3, "Order by Form Name"));
		sorts.add(new SelectItem(4, "Order by OR Number"));
		sorts.add(new SelectItem(5, "Order by Payor"));
		
		/////////////////////////////////////////////
		collectorsSearch = new ArrayList<>();
		String sql = " ORDER BY cl.collectorname";
		
		if(getIssuedCollectorId()>0) {
			sql = " AND cl.isid="+ getIssuedCollectorId();
		}
		
		for(Collector c : Collector.retrieve(sql, new String[0])) {
			if(c.getId()==0) {
				collectorsSearch.add(new SelectItem(0, "All Collector"));
			}else {
				collectorsSearch.add(new SelectItem(c.getId(), c.getName()));
			}
		}
		
		////////////
		if(collectorId==0) {
			collectorId = 1;
		}
		///////
		sql = " ORDER BY cl.collectorname";
		
		if(getIssuedCollectorId()>0) {
			sql = " AND cl.isid="+ getIssuedCollectorId();
		}
		collectors = new ArrayList<>();
		for(Collector c : Collector.retrieve(sql, new String[0])) {
			if(c.getId()>0) {
				collectors.add(new SelectItem(c.getId(), c.getName()));
			}
		}
		
		formTypeSearch = new ArrayList<>();
		formsRpt = new ArrayList<>();
		formTypeSearch.add(new SelectItem(0, "All Forms"));
		formsRpt.add(new SelectItem(0, "All Forms"));
		for(FormType form : FormType.values()) {
			formTypeSearch.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
			formsRpt.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
		}
		
		rptsCombId = 0;
		rptsCombos = new ArrayList<>();
		rptsCombos.add(new SelectItem(0, "Unreported"));
		rptsCombos.add(new SelectItem(1, "Reported"));
		rptsCombos.add(new SelectItem(2, "Both"));
		//setSelectOrTypeId(FormORTypes.NEW.getId());//default OR
		//if(orNumber==null) {
		//	orNumber = ORListing.getLatestORNumber(getFormTypeId(),getCollectorId());
		//}
	}
	
	public void loadAllOrs() {
		
		String sql = "";
		String[] params = new String[0];
		double amount = 0d;
		orsAll = new ArrayList<ORListing>();
		
		if(getSearchNameAll()!=null && !getSearchNameAll().isEmpty() && getSearchNameAll().length()>4) {
			
			
		sql += " AND (cuz.fullname like '%"+ getSearchNameAll().replace("--", "") +"%' OR orl.ornumber like '%"+ getSearchNameAll().replace("--", "") +"%' OR orl.ordatetrans like '%"+ getSearchNameAll().replace("--", "") +"%' OR col.collectorname like '%"+ getSearchNameAll().replace("--", "") +"%' ) ";
			
			
		}else {
			sql += " LIMIT 10";
		}
			
		
		
		for(ORListing o : ORListing.retrieve(sql, params)) {
			if(FormStatus.CANCELLED.getId()!=o.getStatus()) {
				amount += o.getAmount();
			}	
			orsAll.add(o);
			
		}
		
		
		setTotalCollectionAll(Currency.formatAmount(amount));
	}
	
	private Login getUser() {
		return Login.getUserLogin();
	}
	
	public void modeMsg() {
		Application.addMessage(1, "Collector's Mode", "You have " + (isCollectorsMode()==true? "activated the collector's mode" : "deactivated the collector's mode"));
		//RCDReader.saveCollectorMode(isCollectorsMode()==true? "ON" : "OFF");
		setModeName(isCollectorsMode()==true? "Collector Mode On" : "Collector Mode Off");
		//setCollectorsMode(isCollectorsMode()==true? false : true);
		AppSetting.updateCollectorMode(isCollectorsMode(), getUser().getUserDtls());
	}
	
	public void reloadInit() {
		if(monthId==0) {
			monthId = DateUtils.getCurrentMonth();
		}
		System.out.println("Initialized....");
		
		setLimitData("10");
		load();
	}
	
	private void suggestedInfo() {
		suggestedData = new ArrayList<PaymentName>();//Collections.synchronizedList(new ArrayList<PaymentName>());
		Map<Long, PaymentName> mapData = new HashMap<Long, PaymentName>();//Collections.synchronizedMap(new HashMap<Long, PaymentName>());
		int cnt = 1;
		for(ORNameList or : ORNameList.retrieve(" ORDER BY nameL.timestampol DESC LIMIT 100", new String[0])) {
			if(cnt<=10) {
				long id = or.getPaymentName().getId();
				if(mapData!=null) {
					if(!mapData.containsKey(id)) {
						PaymentName name = or.getPaymentName();
						name.setAmount(or.getAmount());
						suggestedData.add(name);
						mapData.put(id, name);
					}
				}else {
					PaymentName name = or.getPaymentName();
					name.setAmount(or.getAmount());
					suggestedData.add(name);
					mapData.put(id, name);
				}
			}
			cnt++;
		}
	}
	
	public void load() {
		String sql = "";
		String[] params = new String[0];
		double amount = 0d;
		ors = new ArrayList<ORListing>();//Collections.synchronizedList(new ArrayList<ORListing>());
		String monthFrom = "";
		String monthTo = "";
		
		if(getSearchName()!=null && !getSearchName().isEmpty()) {
			
			int num = 0;
			try {
				num = Integer.valueOf(getSearchName());
				sql += " AND orl.ornumber like '%"+ getSearchName() +"%'";
			}catch(Exception e) {
				sql += " AND cuz.fullname like '%"+ getSearchName().replace("--", "") +"%'";
			}
			
		}
		
		//System.out.println("Check filtered status : " + isFiltered());
		
		if(!isFiltered()) {
		
		if(getDepId()>0) {
			sql += " AND col.departmentid=" + getDepId();
		}
			
		if(getYearId()==0) {
			params = new String[2];
			if(getMonthId()>0) {
				
				monthFrom = "0000-" + (getMonthId()<10? "0"+getMonthId() : ""+getMonthId()) + "-01";
				monthTo = DateUtils.getCurrentYear() +"-" + (getMonthId()<10? "0"+getMonthId() : ""+getMonthId()) + "-31";
				
			}else {
				
				monthFrom = "0000-01-01";
				monthFrom = DateUtils.getCurrentYear() +"-12-31";
				
			}
			sql += " AND (orl.ordatetrans>=? AND orl.ordatetrans<=?)";
			params[0] = monthFrom;
			params[1] = monthTo;
		}else {
			params = new String[2];
			if(getMonthId()>0) {
				
				monthFrom = getYearId() + "-" + (getMonthId()<10? "0"+getMonthId() : ""+getMonthId()) + "-01";
				monthTo = getYearId() + "-" + (getMonthId()<10? "0"+getMonthId() : ""+getMonthId()) + "-31";
				
			}else {
				
				monthFrom = getYearId() + "-01-01";
				monthTo = getYearId() + "-12-31";
				
			}
			sql += " AND (orl.ordatetrans>=? AND orl.ordatetrans<=?)";
			params[0] = monthFrom;
			params[1] = monthTo;
		}
		
		
		
		if(getFormTypeSearchId()>0) {
			sql += " AND orl.aform=" + getFormTypeSearchId();
		}
		
		if(getIssuedCollectorId()>0) {//specific for collector
			sql += " AND col.isid=" + getIssuedCollectorId();
		}else {
			if(getCollectorSearchId()>0) {
				sql += " AND col.isid=" + getCollectorSearchId();
			}
		}
		
		
		if(getSortId()>0) {
			sql += " ORDER By ";
			if(getSortId()==1) {
				sql += " orl.ordatetrans";
			}else if(getSortId()==2) {
				sql += " col.collectorname";
			}else if(getSortId()==3) {
				sql += " orl.aform";
			}else if(getSortId()==4) {
				sql += " orl.ornumber";
			}else if(getSortId()==5) {
				sql += " cuz.fullname";
			}
		}
		
		if(getStatusSearchId()>0) {
			sql += " AND orl.orstatus=" + getStatusSearchId();
		}
		
		if(getRptsCombId()==0) {
			sql += " AND orl.genid=0";
		}else if (getRptsCombId()==1) {
			sql += " AND orl.genid>0";
		}
		
		if(getLimitData()!=null && !getLimitData().isEmpty()) {
			String num = getLimitData();
			num = num.replace(".0", "");
			num = num.replace(".00", "");
			sql += " ORDER BY  orl.orid DESC ";
			sql += " LIMIT " + num;
		}
		
		//System.out.println("filetered >> " +sql);
		
		
		if(getPaymentId()>0) {
			for(ORListing o : ORListing.retrieve(sql, params)) {
				
					List<ORNameList> or = new ArrayList<ORNameList>();//Collections.synchronizedList(new ArrayList<ORNameList>());
					boolean hasFound = false;
					double totalAmount = 0d;
					
					for(ORNameList n : o.getOrNameList()) {
						if(getPaymentId()==n.getPaymentName().getId()) {
							or.add(n);
							if(FormStatus.CANCELLED.getId()!=o.getStatus()) {
								amount += n.getAmount();
							}
							totalAmount += n.getAmount();
							hasFound = true;
						}
					
						/*
						 * o.setAmount(totalAmount); o.setOrNameList(or); if(hasFound) { ors.add(o); }
						 */
				}
					
					  o.setAmount(totalAmount); o.setOrNameList(or); if(hasFound) { ors.add(o); }
					 
			}
		}else {
			for(ORListing o : ORListing.retrieve(sql, params)) {
				if(FormStatus.CANCELLED.getId()!=o.getStatus()) {
					amount += o.getAmount();
				}	
				ors.add(o);
				
			}
		}
		
		}else {
			
			sql += " ORDER BY orl.orid DESC ";
			if(getLimitData()!=null && !getLimitData().isEmpty()) {
				String num = getLimitData();
				num = num.replace(".0", "");
				num = num.replace(".00", "");
				sql += " LIMIT " + num;
			}
			
			for(ORListing o : ORListing.retrieve(sql, params)) {
				if(FormStatus.CANCELLED.getId()!=o.getStatus()) {
					amount += o.getAmount();
				}	
				ors.add(o);
				
			}
		}
		
		//detailsData();
		setTotalCollection(Currency.formatAmount(amount));
		
		
		if("second".equalsIgnoreCase(getTabCurrent())) {
			detailsData();
		}else if("third".equalsIgnoreCase(getTabCurrent())) {
			selectedItems();
		}
	}
	
	
	private void detailsData() {
		rpts = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		dtls = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		//init();//get the data from init
		Map<Long, PaymentName> mapData = new HashMap<Long, PaymentName>();//Collections.synchronizedMap(new HashMap<Long, PaymentName>());
		double grandTotal = 0d;
		Reports rpt = new Reports();
		double grandcancelledAmnt = 0d;
		double barangayctcamount = 0d;
		double mtoctcamount = 0d;
		double ctctotal = 0d;
		for(ORListing or : ors) {
			rpt = new Reports();
			rpt.setF8(or.getStatusName());
			rpt.setF1(or.getDateTrans());
			rpt.setF2(or.getOrNumber());
			rpt.setF3(or.getCustomer().getFullname());
			rpt.setF4(or.getFormName());
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7(or.getCollector().getName());
			dtls.add(rpt);
			rpts.add(rpt);
			
			double amount = 0d;
			double canAmount = 0d;
			boolean isCTC = false;
			//if(or.getFormType()==FormType.CTC_CORPORATION.getId() || or.getFormType()==FormType.CTC_INDIVIDUAL.getId()) {
			if(or.getFormType()==FormType.CTC_INDIVIDUAL.getId()) {
				isCTC = true;
			}
			
			//this line of code has been change. check below codes
			//boolean isbarangayCtc = or.getCollector().getDepartment().getDepid()>=62? true : false; //62-80 = barangay
			
			//revise above condition for barangay
			Department dep = or.getCollector().getDepartment();
			boolean isbarangayCtc = false;
			if(dep.getDepid()>=62 && dep.getDepid()<=80) {//barangay cedula
				isbarangayCtc = true;
			}
			
			
			for(ORNameList n : or.getOrNameList()) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("");
				rpt.setF3("");
				rpt.setF4("");
				rpt.setF5(n.getPaymentName().getName());
				rpt.setF6(Currency.formatAmount(n.getAmount()));
				rpt.setF7("");
				dtls.add(rpt);
				rpts.add(rpt);
				if(FormStatus.CANCELLED.getId()!=or.getStatus()) {
					amount += n.getAmount();
					if(isCTC) {
						ctctotal += n.getAmount();
						if(isbarangayCtc) {
							barangayctcamount += n.getAmount();
						}else {
							mtoctcamount += n.getAmount();
						}
					}
				}else {
					grandcancelledAmnt += n.getAmount();
					canAmount += n.getAmount();
				}
				
				
				//for report summary only
				//do not remove
				
				long key = n.getPaymentName().getId();	
				if(mapData!=null && mapData.containsKey(key)) {
					double amnt = mapData.get(key).getAmount();
					amnt += n.getAmount();
					PaymentName na = n.getPaymentName();
					na.setAmount(amnt);
					mapData.put(key, na);
				}else {
					PaymentName na = n.getPaymentName();
					na.setAmount(n.getAmount());
					mapData.put(key, na);
				}
				
			}
			rpt = new Reports();
			rpt.setF1("Total");
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			rpt.setF5("");
			if(FormStatus.CANCELLED.getId()!=or.getStatus()) {
				rpt.setF6(Currency.formatAmount(amount));
			}else {
				rpt.setF6("("+Currency.formatAmount(canAmount)+")");
			}
			rpt.setF7("");
			dtls.add(rpt);
			//rpts.add(rpt);
			grandTotal += amount;
		}
		rpt = new Reports();
		rpt.setF1("Grand Total");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("");
		rpt.setF6(Currency.formatAmount(grandTotal));
		dtls.add(rpt);
		rpts.add(rpt);
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("");
		rpt.setF6("");
		dtls.add(rpt);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Summary Details");
		rpt.setF6("");
		dtls.add(rpt);
		
		rptsOnly = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		
		double sumAmount = 0d;
		String sumN="";
		String sumA="";
		Reports rss = new Reports();
		for(PaymentName nem : mapData.values()) {
			rpt = new Reports();
			rpt.setF1("");
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			rpt.setF5(nem.getName());
			sumAmount += nem.getAmount();
			rpt.setF6(Currency.formatAmount(nem.getAmount()));
			dtls.add(rpt);
			sumN += rpt.getF5() + "\n";
			sumA += rpt.getF6() + "\n";
			
			rss = new Reports();
			rss.setF1(nem.getName());
			rss.setF2(Currency.formatAmount(nem.getAmount()));
			rptsOnly.add(rss);
		}
		
		sumAmount -= grandcancelledAmnt;
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Minus Cancelled OR");
		rpt.setF6("("+Currency.formatAmount(grandcancelledAmnt)+")");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Minus Cancelled OR");
		rss.setF2("("+Currency.formatAmount(grandcancelledAmnt)+")");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		double sharedAmount = barangayctcamount * 0.50;
		rpt.setF5("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Grand Total");
		rpt.setF6(Currency.formatAmount(sumAmount));
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		setSumNames(sumN);
		setSumAmounts(sumA);
		
		setTotalAmountSummaryOnly(Currency.formatAmount(sumAmount));
	}
	
	
	private void selectedItems() {
		selections = Collections.synchronizedList(new ArrayList<ORListing>());
		for(ORListing or : getOrs()) {
			if(or.getGenCollection()==0) {//only non group will be display on the list
				selections.add(or);
			}
		}
		
	}
	public void printSelected(String val) {
		
		if("DIRECT".equalsIgnoreCase(val)) {
			setSelectedCollection(getFinalRpts());
		}
		
		rpts = new ArrayList<Reports>();
		dtls = new ArrayList<Reports>();
		
		Map<Long, PaymentName> mapData = new HashMap<Long, PaymentName>();
		double grandTotal = 0d;
		Reports rpt = new Reports();
		double grandcancelledAmnt = 0d;
		double barangayctcamount = 0d;
		double mtoctcamount = 0d;
		double ctctotal = 0d;
		for(ORListing or : getSelectedCollection()) {
			rpt = new Reports();
			rpt.setF8(or.getStatusName());
			rpt.setF1(or.getDateTrans());
			rpt.setF2(or.getOrNumber());
			rpt.setF3(or.getCustomer().getFullname());
			rpt.setF4(or.getFormName());
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7(or.getCollector().getName());
			dtls.add(rpt);
			rpts.add(rpt);
			
			double amount = 0d;
			double canAmount = 0d;
			boolean isCTC = false;
			
			if(or.getFormType()==FormType.CTC_INDIVIDUAL.getId()) {
				isCTC = true;
			}
			
			
			Department dep = or.getCollector().getDepartment();
			boolean isbarangayCtc = false;
			if(dep.getDepid()>=62 && dep.getDepid()<=80) {//barangay cedula
				isbarangayCtc = true;
			}
			
			
			for(ORNameList n : or.getOrNameList()) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("");
				rpt.setF3("");
				rpt.setF4("");
				rpt.setF5(n.getPaymentName().getName());
				rpt.setF6(Currency.formatAmount(n.getAmount()));
				rpt.setF7("");
				dtls.add(rpt);
				rpts.add(rpt);
				if(FormStatus.CANCELLED.getId()!=or.getStatus()) {
					amount += n.getAmount();
					if(isCTC) {
						ctctotal += n.getAmount();
						if(isbarangayCtc) {
							barangayctcamount += n.getAmount();
						}else {
							mtoctcamount += n.getAmount();
						}
					}
				}else {
					grandcancelledAmnt += n.getAmount();
					canAmount += n.getAmount();
				}
				
				
				//for report summary only
				//do not remove
				
				long key = n.getPaymentName().getId();	
				if(mapData!=null && mapData.containsKey(key)) {
					double amnt = mapData.get(key).getAmount();
					amnt += n.getAmount();
					PaymentName na = n.getPaymentName();
					na.setAmount(amnt);
					mapData.put(key, na);
				}else {
					PaymentName na = n.getPaymentName();
					na.setAmount(n.getAmount());
					mapData.put(key, na);
				}
				
			}
			rpt = new Reports();
			rpt.setF1("Total");
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			rpt.setF5("");
			if(FormStatus.CANCELLED.getId()!=or.getStatus()) {
				rpt.setF6(Currency.formatAmount(amount));
			}else {
				rpt.setF6("("+Currency.formatAmount(canAmount)+")");
			}
			rpt.setF7("");
			dtls.add(rpt);
			//rpts.add(rpt);
			grandTotal += amount;
		}
		rpt = new Reports();
		rpt.setF1("Grand Total");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("");
		rpt.setF6(Currency.formatAmount(grandTotal));
		dtls.add(rpt);
		rpts.add(rpt);
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("");
		rpt.setF6("");
		dtls.add(rpt);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Summary Details");
		rpt.setF6("");
		dtls.add(rpt);
		
		rptsOnly = new ArrayList<Reports>();
		
		double sumAmount = 0d;
		String sumN="";
		String sumA="";
		Reports rss = new Reports();
		for(PaymentName nem : mapData.values()) {
			rpt = new Reports();
			rpt.setF1("");
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			rpt.setF5(nem.getName());
			sumAmount += nem.getAmount();
			rpt.setF6(Currency.formatAmount(nem.getAmount()));
			dtls.add(rpt);
			sumN += rpt.getF5() + "\n";
			sumA += rpt.getF6() + "\n";
			
			rss = new Reports();
			rss.setF1(nem.getName());
			rss.setF2(Currency.formatAmount(nem.getAmount()));
			rptsOnly.add(rss);
		}
		
		sumAmount -= grandcancelledAmnt;
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Minus Cancelled OR");
		rpt.setF6("("+Currency.formatAmount(grandcancelledAmnt)+")");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Minus Cancelled OR");
		rss.setF2("("+Currency.formatAmount(grandcancelledAmnt)+")");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		double sharedAmount = barangayctcamount * 0.50;
		rpt.setF5("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Grand Total");
		rpt.setF6(Currency.formatAmount(sumAmount));
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		setSumNames(sumN);
		setSumAmounts(sumA);
		
		setTotalAmountSummaryOnly(Currency.formatAmount(sumAmount));
		
		printSummaryOnly();
	}
	
	public void onChange(TabChangeEvent event) {
		
		PrimeFaces pf = PrimeFaces.current();
		
		
		if("Reports Viewing".equalsIgnoreCase(event.getTab().getTitle())) {
			setTabCurrent("first");
			init();
			pf.executeScript("hideDrop()");
			setSelectedCollection(new ArrayList<ORListing>());
		}else if("Details/Extract".equalsIgnoreCase(event.getTab().getTitle())) {
			setTabCurrent("second");
			detailsData();
			pf.executeScript("hideDrop()");
			setSelectedCollection(new ArrayList<ORListing>());
		}else if("Selection Reports".equalsIgnoreCase(event.getTab().getTitle())) {
			setTabCurrent("third");
			selectedItems();
			
			
			
			int month = DateUtils.getCurrentMonth();
			int year = DateUtils.getCurrentYear();
			String valorsReportsId = month>9? month+"" : "0"+month; 
			valorsReportsId += "-" + year;
			
			List<GenCollection> cols = GenCollection.retrieve("", new String[0]);
			orsReports = new ArrayList<>();
			boolean hasFoundLatestGroup = false;
			if(cols!=null && cols.size()>0) {
				for(GenCollection gen : cols){
					orsReports.add(new SelectItem(gen.getId(), gen.getMonthYear()));
					if(valorsReportsId.equalsIgnoreCase(gen.getMonthYear())) {
						hasFoundLatestGroup = true;
						setOrsReportsId(gen.getId());
						loadSelectedGroupRpt("PARTIAL");
					}
				}
			}else {//group for the first time
				hasFoundLatestGroup = true;
				GenCollection.saveGen(GenCollection.builder()
						.dateTrans(DateUtils.getCurrentDateYYYYMMDD())
						.monthYear(valorsReportsId)
						.isActive(1)
						.build());
				orsReports.add(new SelectItem(1, valorsReportsId));
				setOrsReportsId(1);
				loadSelectedGroupRpt("PARTIAL");
			}
			
			if(!hasFoundLatestGroup) {//adding new group if not yet on the database
				GenCollection gen = GenCollection.saveGen(GenCollection.builder()
						.dateTrans(DateUtils.getCurrentDateYYYYMMDD())
						.monthYear(valorsReportsId)
						.isActive(1)
						.build());
				orsReports.add(new SelectItem(gen.getId(), gen.getMonthYear()));
				setOrsReportsId(gen.getId());
				loadSelectedGroupRpt("PARTIAL");
			}
			//System.out.println("check listing of group : " + getSelectedCollection().size());
			pf.executeScript("showDrop()");
			/*GenCollection gen = GenCollection.retrieveSelected(valorsReportsId);
			selectedCollection = ORListing.retrieve(" AND genid="+gen.getId(), new String[0]);
			if(selectedCollection!=null && selectedCollection.size()>0) {
				setOrsReportsId(selectedCollection.get(0).getGenCollection());
				loadSelectedGroupRpt();
			}*/
		}
		
			
	}
	
	public List<String> payorNameSuggested(String query){
		List<String> result = new ArrayList<>();
		//if(!query.isBlank() && query.toLowerCase().contains("fs-")) {
			//return FishcageBillingStatment.searchControlNumber(query);
		//}
		
		
		mapCustomer = new LinkedHashMap<String, Customer>();
		if(query!=null && !query.isEmpty()) {
			int size = query.length();
			if(size>=4) {
				String sql = " AND cus.fullname like '%"+query+"%'";
				String[] params = new String[0];
				/*for(Customer cz : Customer.retrieve(sql, params)) {
					result.add(cz.getFullName());
				}*/
				
				sql = " AND (cus.cuslastname like '%"+ query +"%' OR ";
				sql += " cus.fullname like '%"+ query +"%' OR cus.cusfirstname like '%"+ query +"%' OR cus.cusmiddlename like '%"+ query +"%'";
				sql += " ) GROUP BY cus.fullname LIMIT 10";
				params = new String[0];
				
				for(com.italia.municipality.lakesebu.licensing.controller.Customer cust : com.italia.municipality.lakesebu.licensing.controller.Customer.retrieve(sql, params)) {
					String fullName = cust.getFullname();//cust.getLastname().toUpperCase() + ", " + cust.getFirstname() + " " + (cust.getMiddlename()!=null? cust.getMiddlename().substring(0, 1).toUpperCase()+"." : ".");
					result.add(fullName);
					mapCustomer.put(fullName, cust);
				}
				
			 //result = com.italia.municipality.lakesebu.licensing.controller.Customer.retrieveLFMName(query, "fullname", "10");
			}
		}
		return result;
	}
	
	public void popupData() {
		
		if(getPayorName()!=null) {
			
		}
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgCedula').show();");
	}
	
	public void loadPaymentNames() {
		
		
		namesData = new ArrayList<PaymentName>();//Collections.synchronizedList(new ArrayList<PaymentName>());
		
		String sql = " LIMIT 10";
		
		if(getSearchPayName()!=null && !getSearchPayName().isEmpty()) {
			sql = " AND pyname like '%"+ getSearchPayName().replace("--", "") +"%'";
			
			for(PaymentName name : PaymentName.retrieve(sql, new String[0])) {
				namesData.add(name);
			}
		}else {
			
			Map<Long, PaymentName> mapData = new HashMap<Long, PaymentName>();//Collections.synchronizedMap(new HashMap<Long, PaymentName>());
			int cnt = 1;
			for(ORNameList or : ORNameList.retrieve(" ORDER BY nameL.timestampol DESC LIMIT 100", new String[0])) {
				if(cnt<=10) {
					long id = or.getPaymentName().getId();
					if(mapData!=null) {
						if(!mapData.containsKey(id)) {
							PaymentName name = or.getPaymentName();
							name.setAmount(0.00);
							namesData.add(name);
							mapData.put(id, name);
						}
					}else {
						PaymentName name = or.getPaymentName();
						name.setAmount(0.00);
						namesData.add(name);
						mapData.put(id, name);
					}
				}
				cnt++;
			}
			
			
		}
		loadTaxGroup();
		
		//ADDED
		suggestedInfo();
	}
	
	public void loadTaxGroup() {
		groups = new ArrayList<TaxCodeGroup>();
		groups= TaxCodeGroup.retrieve("", new String[0]);
	}
	
	public void loadPaymentDetails(TaxCodeGroup gp) {
		namesData = new ArrayList<PaymentName>();
		Map<Long, PaymentName> mapData = new HashMap<Long, PaymentName>();
	
		for(PaymentName py : gp.getGroups()) {
			
				long id = py.getId();
				if(mapData!=null) {
					if(!mapData.containsKey(id)) {
						namesData.add(py);
						mapData.put(id, py);
						addSuggested(py);
					}
				}else {
					namesData.add(py);
					mapData.put(id, py);
					addSuggested(py);
				}
			}
		
	}
	
	public void loadLastORToSuggestForNewReceipt() {
		setOrRecordData(null);
		namesDataSelected = new ArrayList<PaymentName>();
		selectedPaymentNameMap = new HashMap<Long, PaymentName>();
		double amount = 0d;
		String sql = "";
		String[] params = new String[1];
		params[0] = getFormTypeId()+"";
		sql = " AND orl.isactiveor=1 AND orl.aform=? ORDER BY orl.orid DESC LIMIT 1";
		
		List<ORListing> ol = ORListing.retrieve(sql, params);
		if(ol!=null && ol.size()>0) {
			ORListing o = ol.get(0);
			
			params = new String[1];
			params[0] = o.getId()+"";
			sql = " AND orl.orid=? AND nameL.isactiveol=1";
			
			for(ORNameList or : ORNameList.retrieve(sql, params)) {
				PaymentName py = or.getPaymentName();
				py.setAmount(or.getAmount());
				amount += or.getAmount();
				namesDataSelected.add(py);
				getSelectedPaymentNameMap().put(py.getId(), py);
			}
			
			setFormTypeId(o.getFormType());
			if(getCollectorId()==1) {
				setCollectorId(o.getCollector().getId());
			}
			setTotalAmount(Currency.formatAmount(amount));
		}
		
	}
	
	
	private com.italia.municipality.lakesebu.licensing.controller.Customer selectedCustomer(){
		String sql = " AND cus.fullname like '%"+getPayorName()+"%'";
		String[] params = new String[0];
		for(com.italia.municipality.lakesebu.licensing.controller.Customer cz : com.italia.municipality.lakesebu.licensing.controller.Customer.retrieve(sql, params)) {
			return cz;
		}
		return null;
	}
	
	 public void onCellEdit(CellEditEvent event) {
		 try{
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        
	        System.out.println("old Value: " + oldValue);
	        System.out.println("new Value: " + newValue);
	        
	        int index = event.getRowIndex();
	        
	        PaymentName name = getNamesData().get(index);
	        
	        addNamePayment(name);
	        
		 }catch(Exception e){}  
	 }       
	
	 public void onCellEditSelected(CellEditEvent event) {
		 try{
		        Object oldValue = event.getOldValue();
		        Object newValue = event.getNewValue();
		        
		        System.out.println("old Value: " + oldValue);
		        System.out.println("new Value: " + newValue);
		        
		        int index = event.getRowIndex();
		        
		        namesDataSelected.get(index).setAmount(Double.valueOf(newValue+""));
		        long id = namesDataSelected.get(index).getId();
		        boolean reloadInit = false;
		        
		        if(getOrRecordData()!=null) {//update
		        	try {
			        	ORNameList py = ORNameList.retrieve(" AND nameL.isactiveol=1 AND orl.orid="+getOrRecordData().getId() + " AND pay.pyid=" + id, new String[0]).get(0);
			        	py.setAmount(namesDataSelected.get(index).getAmount());
			        	py.save();
			        	reloadInit = true;
		        	}catch(Exception e) {}
		        }
		        
		        double amount = 0d;
		        for(PaymentName na : namesDataSelected) {
		        	amount += na.getAmount();
		        }
		        setTotalAmount(Currency.formatAmount(amount));
		        
		        if(reloadInit) {
		        	//init();
		        	reloadInit();
		        }
		        
			 }catch(Exception e){}  
	}  
	 
	 public void addSuggested(PaymentName name) {
		 addNamePayment(name);
	 }
	 
	public void addNamePayment(PaymentName name) {
		
		if(getSelectedPaymentNameMap()!=null) {
			
			namesDataSelected = new ArrayList<PaymentName>();//Collections.synchronizedList(new ArrayList<PaymentName>());
			getSelectedPaymentNameMap().put(name.getId(), name);
			double amount = 0d;
			for(PaymentName na : getSelectedPaymentNameMap().values()) {
				namesDataSelected.add(na);
				amount += na.getAmount();
			}
			setTotalAmount(Currency.formatAmount(amount));
		}else {
			setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());
			getSelectedPaymentNameMap().put(name.getId(), name);
			getNamesDataSelected().add(name);
			setTotalAmount(Currency.formatAmount(name.getAmount()));
		}
		
	}
	
	public void calculateSelected() {
		double amount = 0d;
		for(PaymentName na : namesDataSelected) {
			amount += na.getAmount();
		}
		setTotalAmount(Currency.formatAmount(amount));
	}
	
	public void selectedOR() {
		namesDataSelected = new ArrayList<PaymentName>();
		String sql = "";
		String[] params = new String[0];
		
		clearFlds();//clearing all fields
		ctcFlds(false);
		if (getSelectOrTypeId()==0) {//new
		 //plain new
		}else if (getSelectOrTypeId()==1) {//new cedula
			setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());
			if(FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
			/**
			 * 60-CTC Individual
			 * 65-CTC Interest 
			 */
			setFormTypeId(FormType.CTC_INDIVIDUAL.getId());
			int[] ids = {60};
			double[] amounts = {0.00};
			double amount = 0d;
			
			try {
				for(int i=0; i< ids.length; i++) {
					sql = " AND pyid="+ids[i];
					PaymentName py =  PaymentName.retrieve(sql, params).get(0);
					py.setAmount(amounts[i]);
					amount += amounts[i];
					namesDataSelected.add(py);
					getSelectedPaymentNameMap().put(py.getId(), py);
				}
			}catch(Exception e) {e.printStackTrace();}
			
			
			
			if(DateUtils.getCurrentMonth()>=3) {
				try {
						sql = " AND pyid=65";
						PaymentName py =  PaymentName.retrieve(sql, params).get(0);
						py.setAmount(00.00);
						amount += 0.00;
						namesDataSelected.add(py);
						getSelectedPaymentNameMap().put(py.getId(), py);
				}catch(Exception e) {}
			}
			setTotalAmount(Currency.formatAmount(amount));
			
			ctcFlds(true);
			
			calculateCedula();
			}
			
			if(FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
				/**63-BIR CTC
				 * 65-CTC Interest 
				 */
				namesDataSelected = new ArrayList<PaymentName>();
				setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());
				setFormTypeId(FormType.CTC_CORPORATION.getId());
				int[] ids = {63};
				double[] amounts = {0.00};
				double amount = 0d;
				
				try {
					for(int i=0; i< ids.length; i++) {
						sql = " AND pyid="+ids[i];
						PaymentName py =  PaymentName.retrieve(sql, params).get(0);
						py.setAmount(amounts[i]);
						amount += amounts[i];
						namesDataSelected.add(py);
						getSelectedPaymentNameMap().put(py.getId(), py);
					}
				}catch(Exception e) {e.printStackTrace();}
				
				
				
				if(DateUtils.getCurrentMonth()>=3) {
					try {
							sql = " AND pyid=65";
							PaymentName py =  PaymentName.retrieve(sql, params).get(0);
							py.setAmount(00.00);
							amount += 0.00;
							namesDataSelected.add(py);
							getSelectedPaymentNameMap().put(py.getId(), py);
					}catch(Exception e) {}
				}
				setTotalAmount(Currency.formatAmount(amount));
				calculateCedula();
				ctcFlds(true);
			}
			
			
		}else if(getSelectOrTypeId()==2) {//new OR
			setFormTypeId(FormType.AF_51.getId());
			loadLastORToSuggestForNewReceipt();//suggest last transaction
		}else if (getSelectOrTypeId()==3) { //business
			
			/**
			 * 60-CT Individual
			 * 38-Business Plate Misc.
			 * 28-Garbage fee
			 * 18-Inspection fee
			 * 4-Mayor's Permit
			 * 37-other Misc income
			 * 48-Occupation Tax
			 * 36-Police Misc
			 * 33-Sanitary Fees
			 * 20-Secretary Fees
			 * 9-Zonal
			 */
			setFormTypeId(FormType.AF_51.getId());
			int[] ids = {60,38,28,18,4,37,48,36,33,20,9};
			double[] amounts = {0.00,350.00,1100.00,40.00,770.00,40.00,100.00,40.00,40.00,50.00,100.00};
			double amount = 0d;
			try {
				for(int i=0; i< ids.length; i++) {
					sql = " AND pyid="+ids[i];
					PaymentName py =  PaymentName.retrieve(sql, params).get(0);
					py.setAmount(amounts[i]);
					amount += amounts[i];
					namesDataSelected.add(py);
					getSelectedPaymentNameMap().put(py.getId(), py);
				}
			}catch(Exception e) {e.printStackTrace();}
			
			setTotalAmount(Currency.formatAmount(amount));
			
		}else if (getSelectOrTypeId()==4) { //fish cage
			//3-fishery rental 0.00
			/**
			 * 3-Fishery Rental
			 * 4-Mayor's Permit
			 * 20-Secretary Fees
			 * 36-Police Misc
			 * 33-Sanitary Fees
			 * 18-Inspection fee
			 * 53-DST
			 * 14-Banca Registration Fee
			 * 9-Zonal
			 * 60-CT Individual
			 */
			setFormTypeId(FormType.AF_51.getId());
			int[] ids = {3,4,20,36,33,18,53,14,9,60};
			double[] amounts = {0.00,100.00,50.00,40.00,40.00,40.00,30.00,75.00,100.00,0.00};
			double amount = 0d;
			try {
				for(int i=0; i< ids.length; i++) {
					sql = " AND pyid="+ids[i];
					PaymentName py =  PaymentName.retrieve(sql, params).get(0);
					py.setAmount(amounts[i]);
					amount += amounts[i];
					namesDataSelected.add(py);
					getSelectedPaymentNameMap().put(py.getId(), py);
				}
			}catch(Exception e) {}
			
			setTotalAmount(Currency.formatAmount(amount));
		}else if (getSelectOrTypeId()==5) { //skylab permit
			
			/**
			 * 10-Skylab Permit
			 * 11-Filling Permit
			 * 20-Secretary Fees
			 * 12-Sticker
			 * 53-DST
			 * 13-Fines and Penalty
			 */
			setFormTypeId(FormType.AF_51.getId());
			int[] ids = {10,11,20,12,53};
			double[] amounts = {320.00,180.00,50.00,50.00,30.00};
			double amount = 0d;
			try {
				for(int i=0; i< ids.length; i++) {
					sql = " AND pyid="+ids[i];
					PaymentName py =  PaymentName.retrieve(sql, params).get(0);
					py.setAmount(amounts[i]);
					amount += amounts[i];
					namesDataSelected.add(py);
					getSelectedPaymentNameMap().put(py.getId(), py);
				}
			}catch(Exception e) {}
			
			if(DateUtils.getCurrentMonth()==1 && DateUtils.getCurrentDay()>20) {
				try {
						sql = " AND pyid=13";
						PaymentName py =  PaymentName.retrieve(sql, params).get(0);
						py.setAmount(80.00);
						amount += 80.00;
						namesDataSelected.add(py);
						getSelectedPaymentNameMap().put(py.getId(), py);
				}catch(Exception e) {}
			}
			
			
			setTotalAmount(Currency.formatAmount(amount));
			
			
		}
	}
	
	
	public void clearFlds() {
		
		setMonthlyIncome(0);
		setMonthlySal(0);
		setMonthlyTax(0);
		
		setStatusId(4);
		setStatusSearchId(0);
		setCollectorId(0);
		setOrNumber(null);
		setDateTrans(null);
		setFormTypeId(1);
		setPayorName(null);
		setAddress(null);
		setTotalAmount("0.00");
		setOrRecordData(null);
		setOrnameListData(null);
		setSelectedPaymentNameMap(null);
		setSearchPayName(null);
		setFormInfo(null);
		
		setLabel2(0);
		setLabel3(0);
		setLabel4(0);
		setAmount1(0);
		setAmount2(0);
		setAmount3(0);
		setAmount4(0);
		setAmount5(0);
		setAmount6(0);
		setAmount7(0);
		setGenderId(1);
		
		setTinNo(null);
		setHieghtDateReg(null);
		setWeight(null);
		setCustomerAddress(null);
		//setCivilStatusOrganization(null);
		setProfessionBusinessNature(null);
		setSignatory(null);
		
		setBirthdate(DateUtils.getDateToday());
		setPlaceOfBirth("Lake Sebu");
		setCitizenshipOrganization("FILIPINO");
		
		namesDataSelected = new ArrayList<PaymentName>();//Collections.synchronizedList(new ArrayList<PaymentName>());
		selectedPaymentNameMap = new HashMap<Long, PaymentName>();//Collections.synchronizedMap(new HashMap<Long, PaymentName>());
	}
	
	public void saveData() {
		boolean isCashTicket=false;
		com.italia.municipality.lakesebu.licensing.controller.Customer customer = selectedCustomer();
		ORListing or = new ORListing();
		
		if(getOrRecordData()!=null) {
			or = getOrRecordData();
		}else {
			or.setIsActive(1);
		}
		UserDtls user = Login.getUserLogin().getUserDtls();
		
		boolean isOk = true;
		if(getOrNumber()==null || getOrNumber().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide official receipt");
		}
		if(getPayorName()==null || getPayorName().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide payor name");
		}
		
		if(getSelectedPaymentNameMap()==null) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide payment name");
		}
		
		if(FormType.CT_2.getId()==getFormTypeId() || FormType.CT_5.getId()==getFormTypeId()) {
			isCashTicket = true;
		}
		
		if(isOk && customer==null && !isCashTicket) {
			customer = new com.italia.municipality.lakesebu.licensing.controller.Customer();
			
			if(getMapCustomer()!=null && getMapCustomer().size()>0) {
				if(getMapCustomer().containsKey(getPayorName())) {
					customer = getMapCustomer().get(getPayorName());
				}else {
					customer.setIsactive(1);
					customer.setDateregistered(DateUtils.convertDate(getDateTrans(),"yyyy-MM-dd"));
					customer.setFirstname(getFirstName().toUpperCase());
					customer.setMiddlename(getMiddleName().toUpperCase());
					customer.setLastname(getLastName().toUpperCase());
					customer.setUserDtls(user);
					//customer.setFullname(getFirstName().toUpperCase() + " " + getLastName().toUpperCase());
					customer.setFullname(getPayorName().toUpperCase().trim());
					customer.setBirthdate(getBirthdate()==null?  DateUtils.getCurrentDateYYYYMMDD() :  DateUtils.convertDate(getBirthdate(), "yyyy-MM-dd"));
					customer.setCivilStatus(1);//default single
					customer.setGender(getGenderId()+"");
					
					
					if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId()) {
						
						customer.setCivilStatus(getCivilStatusId());
						customer.setHeight(getHieghtDateReg());
						customer.setWeight(getWeight());
						customer.setWork(getProfessionBusinessNature());
						customer.setBornplace(getPlaceOfBirth());
						
					}
					customer = Customer.save(customer);
				}
			}else {
				customer.setIsactive(1);
				customer.setDateregistered(DateUtils.convertDate(getDateTrans(),"yyyy-MM-dd")); //DateUtils.getCurrentDateMonthDayYear());
				customer.setFirstname(getFirstName().toUpperCase());
				customer.setMiddlename(getMiddleName().toUpperCase());
				customer.setLastname(getLastName().toUpperCase());
				customer.setUserDtls(user);
				//customer.setFullname(getFirstName().toUpperCase() + " " + getLastName().toUpperCase());
				customer.setFullname(getPayorName().toUpperCase().trim());
				customer.setBirthdate(getBirthdate()==null?  DateUtils.getCurrentDateYYYYMMDD() :  DateUtils.convertDate(getBirthdate(), "yyyy-MM-dd"));
				customer.setCivilStatus(1);//default single
				customer.setGender(getGenderId()+"");
				
				if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId()) {
					
					customer.setCivilStatus(getCivilStatusId());
					customer.setHeight(getHieghtDateReg());
					customer.setWeight(getWeight());
					customer.setWork(getProfessionBusinessNature());
					customer.setBornplace(getPlaceOfBirth());
					
				}
				customer = Customer.save(customer);
			}
			
		}else {
			customer.setUserDtls(user);
		}
		
		
		
		if(isOk) {
			
			if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId() || FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
				String val = "";
				
				val = getLabel2() + "<->";
				val += getLabel3() + "<->";
				val += getLabel4() + "<->";
				val += getAmount1() + "<->";
				val += getAmount2() + "<->";
				val += getAmount3() + "<->";
				val += getAmount4() + "<->";
				val += getAmount5() + "<->";
				val += getAmount6() + "<->";
				val += getAmount7() + "<->";
				val += getGenderId() + "<->";
				val += getBirthdate()==null? "0<->" : DateUtils.convertDate(getBirthdate(), "yyyy-MM-dd") + "<->";
				val += getTinNo().isEmpty()? "0<->" : getTinNo() + "<->";
				val += getHieghtDateReg().isEmpty()? "0<->" : getHieghtDateReg() + "<->";
				val += getWeight()!=null? getWeight() + "<->" : "0<->";  //   getWeight().isEmpty()? "0<->" : getWeight() + "<->";
				val += getCustomerAddress().isEmpty()? "0<->" : getCustomerAddress() + "<->";
				val += getCivilStatusId()==0? "0<->" : getCivilStatusId() + "<->";
				val += getProfessionBusinessNature().isEmpty()? "0<->" : getProfessionBusinessNature() + "<->";
				val += GlobalVar.MTO_OR_CEDULA_SIGNATORY + "<->";//signatory
				val += getPlaceOfBirth().isEmpty()? "0<->" : getPlaceOfBirth() +"<->";
				val += getCitizenshipOrganization().isEmpty()? "0" : getCitizenshipOrganization() + "<->";
				val += getAddress();
				or.setForminfo(val);
			}else {
				or.setForminfo(getAddress());//this is a backup address use for OR 51
			}
			
			//double check datetrans if null
			if(getDateTrans()==null) {
				setDateTrans(new Date());
			}
			
			or.setNotes(getNotes());//this for remarks you can add remarks except ctc and corporation
			or.setStatus(getStatusId());
			or.setDateTrans(DateUtils.convertDate(getDateTrans(), "yyyy-MM-dd"));
			or.setFormType(getFormTypeId());
			or.setCustomer(customer);
			try{or.setOrNumber(getOrNumber().trim());}catch(Exception e){}
			Collector col = new Collector();
			col.setId(getCollectorId());
			or.setCollector(col);
			
			or = ORListing.save(or);
			
			//delete first paynames attached in orlisting table
			//this will ensure for duplication of data
			ORNameList.delete("DELETE FROM ornamelist WHERE orid=" + or.getId(), new String[0]);
			
			List<ORNameList> ortemps = new ArrayList<ORNameList>();
			
			List<PaymentName> tmpName = new ArrayList<PaymentName>();
			Map<Long, PaymentName> tmpMap = new HashMap<Long, PaymentName>();
			for(PaymentName name : getSelectedPaymentNameMap().values()) {
				if(name.getAmount()>0) {
					ORNameList o = new ORNameList();
					o.setAmount(name.getAmount());
					o.setOrList(or);
					o.setCustomer(customer);
					o.setIsActive(1);
					o.setPaymentName(name);
					o.save();
					
					name.setAmount(0);
					tmpName.add(name);
					tmpMap.put(name.getId(), name);
					
					
					ortemps.add(o);
				}
			}
			
			or.setOrNameList(ortemps);
			
			String val = ReadConfig.value(AppConf.SERVER_LOCAL);
			if("true".equalsIgnoreCase(val)) {//if connection is true meaning is not connected to server, therefore create xml file to be sent to server
				System.out.println("saving to xml");
				OrlistingXML.saveForUploadXML(or);
				System.out.println("done saving to xml...");
			}
			
			Application.addMessage(1, "Success", "Successfully saved.");
			if(isCollectorsMode()) {
				if(customer!=null) {
					//setSearchName(customer.getFirstname() + " " + customer.getLastname());
					setSearchName(customer.getFullname());
				}else {
					setSearchName(getPayorName());
				}
			}
			
			namesDataSelected = new ArrayList<PaymentName>();
			selectedPaymentNameMap = new HashMap<Long, PaymentName>();
			setPayorName(null);
			setTotalAmount("0.00");
			setOrRecordData(null);
			setOrnameListData(null);
			setSelectedPaymentNameMap(null);
			setOrNumber(null);
			//setCollectorId(or.getCollector().getId());
			setNotes(null);
			//init(); //do not load data to reduce loading
			
			//System.out.println("check collector id : " + getCollectorId());
			int collectorTemp = or.getCollector().getId();//temporary assigen collector due to method on cedula clearing all fields
			//if(getSelectOrTypeId()>0) {
				//namesDataSelected = tmpName;
				//selectedPaymentNameMap = tmpMap;
			//}
			//forSaveOnly();
			 
			if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId() || FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
				setLabel2(0);
				setLabel3(0);
				setLabel4(0);
				setAmount1(0);
				setAmount2(0);
				setAmount3(0);
				setAmount4(0);
				setAmount5(0);
				setAmount6(0);
				setAmount7(0);
				setGenderId(1);
				setBirthdate(null);
				setTinNo(null);
				setHieghtDateReg(null);
				setWeight(null);
				setCustomerAddress(null);
				setCivilStatusId(1);
				setProfessionBusinessNature(null);
				setPlaceOfBirth(null);
				setCitizenshipOrganization(null);
				selectedOR();
				
				if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId()) {
					setAmount1(5.00);
					setSearchPayName("ctc");
					ctcFlds(true);
					enableBirthday=false;
					cedulaInterest();
				}else if(FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
					setAmount1(500.00);
					setSearchPayName("ctc");
					ctcFlds(true);
					enableBirthday=true;
					cedulaCorporation();
				}
				
				setBirthdate(DateUtils.getDateToday());
			}
			
			
			
			
			setCollectorId(collectorTemp);
			//setFormTypeId(FormType.AF_51.getId());
			setFormTypeId(getFormTypeId());
			
			updateInfo();//reduce data for reloading
			
			
			//updatating que if available
			if(getClientSelected()!=null) {
				Client client = getClientSelected();
				client.setStatus(ClientStatus.COMPLETED.getId());
				client.save();
			}
			setNotes(null);
		}
		
	}
	
	public void clearAllFlds() {
		setSearchName(null);
		setMonthlyIncome(0);
		setMonthlySal(0);
		setMonthlyTax(0);
		
		//dateTrans = DateUtils.getDateToday();
		namesDataSelected = new ArrayList<PaymentName>();
		selectedPaymentNameMap = new HashMap<Long, PaymentName>();
		setPayorName(null);
		setTotalAmount("0.00");
		setOrRecordData(null);
		setOrnameListData(null);
		setSelectedPaymentNameMap(null);
		
		setCollectorId(issuedCollectorId);
		//init();
		
		
		if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId() || FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
			setLabel2(0);
			setLabel3(0);
			setLabel4(0);
			setAmount1(0);
			setAmount2(0);
			setAmount3(0);
			setAmount4(0);
			setAmount5(0);
			setAmount6(0);
			setAmount7(0);
			setGenderId(1);
			setBirthdate(DateUtils.getDateToday());
			setTinNo(null);
			setHieghtDateReg(null);
			setWeight(null);
			setCustomerAddress(null);
			setCivilStatusId(1);
			setProfessionBusinessNature(null);
			setPlaceOfBirth("Lake Sebu");
			setCitizenshipOrganization("FILIPINO");
			selectedOR();
		}
		
		setSelectOrTypeId(FormORTypes.NEW.getId());
		setFormTypeId(FormType.AF_51.getId());
		setOrNumber(ORListing.getLatestORNumber(getFormTypeId(),getCollectorId()));
		dateTrans = DateUtils.getDateToday();
		address = "Lake Sebu, So. Cot.";
	}
	
	private void ctcFlds(boolean enanleCTC) {
		
		PrimeFaces pf = PrimeFaces.current();
		if(enanleCTC) {
			pf.executeScript("$('#ctid').show()");
		}else {
			pf.executeScript("$('#ctid').hide()");
		}
		
	}
	
	public void clickItem(ORListing or) {
		
		if(or.getGenCollection()==0) {
		
		setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());//empty the temporary data location
		setOrRecordData(or);
		setStatusId(or.getStatus());
		setOrnameListData(or.getOrNameList());
		setDateTrans(DateUtils.convertDateString(or.getDateTrans(), "yyyy-MM-dd"));
		setOrNumber(or.getOrNumber());
		setFormTypeId(or.getFormType());
		setNotes(or.getNotes());
		if(or.getCustomer()!=null) {
			//String fName=or.getCustomer().getFirstname();
			//String mName=or.getCustomer().getMiddlename();
			//String lName=or.getCustomer().getLastname();
			try{
				//setPayorName(lName.toUpperCase() + ", " + fName.toUpperCase() + " " + (mName==null? "." : mName.toUpperCase().substring(0,1) + "."));
			}catch(Exception e) {
				//setPayorName(lName.toUpperCase() + ", " + fName.toUpperCase() + " " + (mName==null? "." : "."));
			}
			setPayorName(or.getCustomer().getFullname().toUpperCase());
		}else {
			setPayorName("");
		}
		setAddress(or.getCustomer().getCompleteAddress());
		setCollectorId(or.getCollector().getId());
		namesDataSelected = new ArrayList<PaymentName>();//Collections.synchronizedList(new ArrayList<PaymentName>());
		double amount = 0d;
		/*for(ORNameList on : or.getOrNameList()) {
			PaymentName name = on.getPaymentName();
			name.setAmount(on.getAmount());
			namesDataSelected.add(name);
			amount += on.getAmount();
		}*/
		String sql = " AND nameL.isactiveol=1 AND nameL.orid="+or.getId();
		ctcFlds(false);
		if(FormType.CTC_INDIVIDUAL.getId()==or.getFormType() || FormType.CTC_CORPORATION.getId()==or.getFormType()) {
			
			if(or.getForminfo()!=null && !or.getForminfo().isEmpty()) {
				System.out.println(or.getForminfo());
			String[] val = or.getForminfo().split("<->");
			
			try{setLabel2(Double.valueOf(val[0]));}catch(Exception e) {setLabel2(0.00);}
			try{setLabel3(Double.valueOf(val[1]));}catch(Exception e) {setLabel3(0.00);}
			try{setLabel4(Double.valueOf(val[2]));}catch(Exception e) {setLabel4(0.00);}
			try{setAmount1(Double.valueOf(val[3]));}catch(Exception e) {setAmount1(0.00);}
			try{setAmount2(Double.valueOf(val[4]));}catch(Exception e) {setAmount2(0.00);}
			try{setAmount3(Double.valueOf(val[5]));}catch(Exception e) {setAmount3(0.00);}
			try{setAmount4(Double.valueOf(val[6]));}catch(Exception e) {setAmount4(0.00);}
			try{setAmount5(Double.valueOf(val[7]));}catch(Exception e) {setAmount5(0.00);}
			try{setAmount6(Double.valueOf(val[8]));}catch(Exception e) {setAmount6(0.00);}
			try{setAmount7(Double.valueOf(val[9]));}catch(Exception e) {setAmount7(0.00);}
			try{setGenderId(Integer.valueOf(val[10]));}catch(Exception e) {setGenderId(1);}
			try{setBirthdate(val[11].equalsIgnoreCase("0")? null : DateUtils.convertDateString(val[11], "yyyy-MM-dd"));}catch(Exception e) {setBirthdate(DateUtils.getDateToday());}
			try{setTinNo(val[12].equalsIgnoreCase("0")? "" : val[12]);}catch(Exception e) {setTinNo("");}
			try{setHieghtDateReg(val[13].equalsIgnoreCase("0")? "" : val[13]);}catch(Exception e) {setHieghtDateReg("");}
			try{setWeight(val[14].equalsIgnoreCase("0")? "" : val[14]);}catch(Exception e) {setWeight("");}
			try{setCustomerAddress(val[15].equalsIgnoreCase("0")? "" : val[15]);}catch(Exception e) {setCustomerAddress("");}
			try{setCivilStatusId(Integer.valueOf(val[16]));}catch(Exception e) {setCivilStatusId(1);}
			try{setProfessionBusinessNature(val[17].equalsIgnoreCase("0")? "" : val[17]);}catch(Exception e) {setProfessionBusinessNature("");}
			try{setSignatory(val[18].equalsIgnoreCase("0")? "" : val[18]);}catch(Exception e) {setSignatory("");}
			try{setPlaceOfBirth(val[19].equalsIgnoreCase("0")? "" : val[19]);}catch(Exception e) {setPlaceOfBirth("");}
			try{setCitizenshipOrganization(val[20].equalsIgnoreCase("0")? "" : val[20]);}catch(Exception e) {setCitizenshipOrganization("");}
			try {
				setAddress(val[21]);
			}catch(Exception e) {setAddress("");}
			ctcFlds(true);
			
			if(FormType.CTC_INDIVIDUAL.getId()==or.getFormType()) {
				setEnableBirthday(false);
			}else {
				setEnableBirthday(true);
			}
			
			}
			
		}else {
			if(or.getForminfo()!=null) {
				if(or.getForminfo().contains("<->")) {
					//setAddress();
				}else {
					setAddress(or.getForminfo());
				}
			}
			ctcFlds(false);
		}
		
		for(ORNameList on : ORNameList.retrieve(sql, new String[0])) {
			PaymentName name = on.getPaymentName();
			name.setAmount(on.getAmount());
			namesDataSelected.add(name);
			amount += on.getAmount();
			getSelectedPaymentNameMap().put(on.getPaymentName().getId(), name);
		}
		setTotalAmount(Currency.formatAmount(amount));
		
		}else {
			clearAllFlds();
			Application.addMessage(2, "Not Allowed", "Editing for this Official Receipt is no longer allowed");
		}
	}
	
	public void clickItemCopy(ORListing or) {
		or.setGenCollection(0);//reset report group
		clickItem(or);
		setOrRecordData(null);
		
		orNumber = ORListing.getLatestORNumber(getFormTypeId(),getCollectorId());
		
		dateTrans = DateUtils.getDateToday();
		address = "Lake Sebu, So. Cot.";
	}
	
	public void clickItemFishCageBill(FishcageBillingStatment st) {
		setNamesDataSelected(new ArrayList<PaymentName>());
		setSelectedPaymentNameMap(new LinkedHashMap<Long, PaymentName>());
		String fullname = st.getOwner().getOwnerName().toUpperCase();
		setPayorName(fullname);
		setFirstName(fullname);
		setMiddleName(".");
		setLastName(".");
		if(st.getRemarks()!=null) {
			setNotes("Fish cage: "+st.getRemarks());
		}
		setFormTypeId(FormType.AF_51.getId());
		//setAddress(st.getOwner().getCageArea());
		double amount = 0d;
		
		for(PaymentName name : st.getListparticulars()) {
			if(!"Total".equalsIgnoreCase(name.getName())) {
				namesDataSelected.add(name);
				amount += name.getAmount();
				getSelectedPaymentNameMap().put(name.getId(), name);
			}
		}
		
		setTotalAmount(Currency.formatAmount(amount));
	}
	
	public void editPay(PaymentName names) {
		setSearchPayName(names.getName());
		loadPaymentNames();
		if(namesData!=null && namesData.size()>0) {
			namesData.get(0).setAmount(names.getAmount());
		}
	}
	
	public void paynameDelete(PaymentName names) {
		if(getSelectedPaymentNameMap()!=null && getSelectedPaymentNameMap().size()>0) {
			getSelectedPaymentNameMap().remove(names.getId());
		}
		try{
			if(getOrnameListData()!=null && getOrnameListData().size()>0) {
				for(ORNameList or : getOrnameListData()) {
					if(names.getId()==or.getPaymentName().getId()) {
						or.delete();
					}
				}
			}
		}catch(Exception e) {e.printStackTrace();System.out.println("no data>>");}
		
		List<PaymentName> nms = new ArrayList<PaymentName>();//Collections.synchronizedList(new ArrayList<PaymentName>());
		for(PaymentName n : namesDataSelected) {
			if(names.getId()!=n.getId()) {
				nms.add(n);
			}
		}
		namesDataSelected = nms;
		double amount = 0d;
		for(PaymentName on : namesDataSelected) {
			amount += on.getAmount();
		}
		setTotalAmount(Currency.formatAmount(amount));
		init();
		Application.addMessage(1, "Sucess", "Successfully deleted.");
	}
	
	public void deleteRow(ORListing or) {
		Login in = getUser();
		if(in.getAccessLevel().getLevel()==AccessLevel.DEVELOPER.getId()) {
		or.delete();
		init();
		clearFlds();
		
		try {
			String name = or.getCollector().getName() + "-" + or.getCustomer().getFullname() + "-" + or.getFormName() + "-" + or.getDateTrans() + "-" + or.getOrNumber() + ".xml"; 
			String localpath = GlobalVar.COMMIT_XML;
			File file = new File(localpath + name);
			
			if(file.exists()) {
				file.delete();
			}
		}catch(Exception e) {}
		
			Application.addMessage(1, "Sucess", "Successfully deleted.");
		}else {
			Application.addMessage(2, "Warning", "You dont have a right to delete this transaction. Please contact admin for deletion");
		}
	}
	
	public void printOR(ORListing py) {
		
		try{
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			
			String REPORT_NAME = "OR51_" + DocumentFormatter.getTagName("printer-type");//GlobalVar.OR51_FX2175_PRINT;
			
			//String printFormat = DocumentFormatter.getTagName("printer-type");
			
			
			
			ReportCompiler compiler = new ReportCompiler();
			HashMap param = new HashMap();
	  		
	  		param.put("PARAM_DATE", DateUtils.convertDateToMonthDayYear(py.getDateTrans()));
	  		//param.put("PARAM_PAYOR", py.getCustomer().getFullname());
	  		Customer customer = py.getCustomer();
	  		String middle = ".";
	  		try {
	  			middle = customer.getMiddlename().contains("\\.")?  "" : customer.getMiddlename().substring(0, 1);
	  			middle = middle.toUpperCase()+".";
	  		}catch(Exception e) {}
	  		String name = customer.getFullname();  //customer.getLastname().toUpperCase() + ", " + customer.getFirstname().toUpperCase() + " " + middle;
	  		param.put("PARAM_PAYOR", name);
	  		com.italia.municipality.lakesebu.controller.NumberToWords numberToWords = new NumberToWords();
	  		
	  		String amnt = Currency.formatAmount(py.getAmount());
	  		amnt = amnt.replace(",", "");
	  		param.put("PARAM_WORDS", numberToWords.changeToWords(amnt).toUpperCase().replace("/", " / ") );
	  		param.put("PARAM_CASHCHECK", "CASH");
	  		param.put("PARAM_COLLECTING_OFFICER", "FERDINAND L. LOPEZ\n" + DateUtils.getCurrentDateMMDDYYYYTIME());
	  		//param.put("PARAM_COLLECTING_OFFICER", "FERDINAND L. LOPEZ");
			
	  		List<OR51> ors = new ArrayList<OR51>();
			if(FormType.CTC_INDIVIDUAL.getId()==py.getFormType() || FormType.CTC_CORPORATION.getId()==py.getFormType()) {
				REPORT_NAME = "cedula_" + DocumentFormatter.getTagName("printer-type");//GlobalVar.CEDULA_FX2175_PRINT;
				if(py.getForminfo()!=null && !py.getForminfo().isEmpty()) {
					
					String[] dt = py.getDateTrans().split("-");
					param.put("PARAM_DATE", dt[1] + " " + dt[2] + " " + dt[0]);
					
					String[] val = py.getForminfo().split("<->");
					
					
					param.put("PARAM_LABEL2", Currency.formatAmount(Double.valueOf(val[0])));
					param.put("PARAM_LABEL3", Currency.formatAmount(Double.valueOf(val[1])));
					param.put("PARAM_LABEL4", Currency.formatAmount(Double.valueOf(val[2])));
					
					param.put("PARAM_YEAR", py.getDateTrans().split("-")[0]);
					param.put("PARAM_POI", "MTO-LAKE SEBU");
					
					try {
					if(val[21]==null || val[21].isEmpty()) {
						param.put("PARAM_ADDRESS", getAddress());
					}else {
						try{param.put("PARAM_ADDRESS", val[21]);}catch(Exception e) {}
					}
					}catch(Exception e) {param.put("PARAM_ADDRESS", getAddress());}
					param.put("PARAM_TIN", val[12].equalsIgnoreCase("0")? "" : val[12]);
					param.put("PARAM_CITIZENSHIP", val[20].equalsIgnoreCase("0")? "" : val[20]);
					
					param.put("PARAM_CIVIL", CivilStatus.typeName(Integer.valueOf(val[16])));
					if(FormType.CTC_CORPORATION.getId()==py.getFormType()) {//if corporation remove civil status
						param.put("PARAM_CIVIL", "");
					}
					
					
					param.put("PARAM_PROF",val[17].equalsIgnoreCase("0")? "" : val[17]);
					param.put("PARAM_POB", val[19].equalsIgnoreCase("0")? "" : val[19]);
					param.put("PARAM_GENDER", Integer.valueOf(val[10])==1? "MALE":"FEMALE");
					param.put("PARAM_H", val[13].equalsIgnoreCase("0")? "" : val[13]);
					param.put("PARAM_W", val[14].equalsIgnoreCase("0")? "" : val[14]);
					param.put("PARAM_ONE", Currency.formatAmount(Double.valueOf(val[3])));
					param.put("PARAM_TWO", Currency.formatAmount(Double.valueOf(val[4])));
					param.put("PARAM_THREE", Currency.formatAmount(Double.valueOf(val[5])));
					param.put("PARAM_FOUR", Currency.formatAmount(Double.valueOf(val[6])));
					param.put("PARAM_FIVE", Currency.formatAmount(Double.valueOf(val[7])));
					param.put("PARAM_INTEREST", Currency.formatAmount(Double.valueOf(val[8])));
					param.put("PARAM_GRAND", Currency.formatAmount(Double.valueOf(val[9])));
					param.put("PARAM_BIRTH", val[11]);
					
		  			ors.add(new OR51());
				}else {
					ors.add(new OR51());
				}
				
			}else { //Form51
			
				if(FormType.AF_51.getId()==py.getFormType()){ 
					//param.put("PARAM_PAYOR", name + " - " + py.getForminfo());
					param.put("PARAM_PAYOR", name);
				}
				
		  		int cnt = 0;
		  		double amount = 0d;
		  		
		  		if(py.getNotes()!=null) {
			  		//add notes for except ctc and corporation for now 51
				  		OR51 or = new OR51();
				  		or.setDescription(py.getNotes());
				  		or.setCode("");
				  		or.setAmount("");
			  			ors.add(or);
			  			cnt++;
			  	}
		  		
		  		for(ORNameList na : py.getOrNameList()) {
		  			if(na.getAmount()>0) {
		  				OR51 or = new OR51();
				  		or.setDescription(na.getPaymentName().getName());
				  		or.setCode(na.getPaymentName().getAccountingCode());
				  		or.setAmount(Currency.formatAmount(na.getAmount()));
			  			ors.add(or);
			  			cnt++;
			  			amount += na.getAmount();
		  			}
		  		}
		  		
		  		
		  		
		  		if(cnt<12) {
		  			int addFldSize = 11 - cnt;
			  		for(int i=1; i<=addFldSize; i++) {
			  			OR51 or = new OR51();
				  		or.setDescription("");
				  		or.setCode("");
				  		or.setAmount("");
			  			ors.add(or);
			  		}
		  		}
		  		
		  		//total amount
	  			OR51 or = new OR51();
		  		or.setDescription("");
		  		or.setCode("");
		  		or.setAmount(Currency.formatAmount(amount));
	  			ors.add(or);
	  		
			}
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(ors);
	  		
	  		
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
	
	public void print() {
		//List<Reports> rpts = Collections.synchronizedList(new ArrayList<Reports>());
		
		try {
		compileReportList(rpts);
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = "generalcollections";
		if(getCollectorSearchId()!=0) {
			REPORT_NAME = "generalcollectionscollector";
		}
		//String REPORT_PATH_FILE = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() +  AppConf.CHEQUE_REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		
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
	                //System.out.println("printReportAll read : " + length);
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
	     
	       // backupPrintDispense();
	        
		}catch(Exception ioe){
			ioe.printStackTrace();
		}
	}
	
	public void printSummary() {
		
				List<Reports> rpts = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
				Reports rpt = new Reports();
				rpts.add(rpt);
				try {
					compileReportSummary(rpts);
				
				String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
						AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
				String REPORT_NAME = "summarycollection";
				
				
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
			                //System.out.println("printReportAll read : " + length);
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
			     
			       // backupPrintDispense();
			        
				}catch(Exception ioe){
					ioe.printStackTrace();
				}
	}
	
	private  void compileReportList(List<Reports> reportFields){
		try{
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME = "generalcollections";
			
			if(getCollectorSearchId()!=0) {
				REPORT_NAME = "generalcollectionscollector";
			}
			
			//String REPORT_PATH_FILE = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() +  AppConf.CHEQUE_REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		ArrayList<ReportFields> rpts = new ArrayList<ReportFields>();
	  		/*for(ReportFields r : reportFields){
	  			rpts.add(r);
	  		}*/
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reportFields);
	  		HashMap param = new HashMap();
	  		
	  		/*DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");//new SimpleDateFormat("MM/dd/yyyy");//new SimpleDateFormat("yyyy/MM/dd hh:mm: a");
			Date date = new Date();
			String _date = dateFormat.format(date);
			param.put("PARAM_DATE_RANGE", "From: "+ DateUtils.convertDate(getDateFrom(),"yyyy-MM-dd") + " to " + DateUtils.convertDate(getDateTo(),"yyyy-MM-dd"));
	  		param.put("PARAM_DATE", "Printed: "+_date);
	  		
	  		param.put("PARAM_PREPAREDBY", "Prepared By: "+ Login.getUserLogin().getUserDtls().getFirstname() + " " + Login.getUserLogin().getUserDtls().getLastname());*/
	  		
	  		param.put("PARAM_TITLE", "GENERAL REPORT COLLECTION");
	  		param.put("PARAM_SUM_NAME", getSumNames());
	  		param.put("PARAM_SUM_AMOUNT", getSumAmounts());
	  		
	  		if(getCollectorSearchId()!=0) {
	  			Collector col = Collector.retrieve(getCollectorSearchId());
	  			param.put("PARAM_COLLECTOR", "Collector Name: " +col.getName().toUpperCase());
	  		}
	  		
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
			param.put("PARAM_WATERMARK", off);
			}catch(Exception e){e.printStackTrace();}
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
		
		
			System.out.println("pdf successfully created...");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
	
	public void printSummaryOnly() {
		try{
			List<Reports> rss = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
			
			int size = getRptsOnly().size();
			
			if(size>35) {
				int cnt=1;
				List<Reports> r1 = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<>());
				List<Reports> r2 = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<>());
				int cntRem = 1;
				for(Reports r : getRptsOnly()) {
					if(cnt<36) {
						r1.add(r);
					}else {
						r2.add(r);
						cntRem++;
					}
					cnt++;
				}
				cntRem -=1;
				
				//this for r2 only data
				for(int i=cntRem; i<36; i++) {//supply the remaining fields
					r2.add(new Reports());
				}
				
				
				//combine the two list
				for(int i=0; i<35;i++) {
					Reports r = new Reports();
					r.setF1(r1.get(i).getF1());
					r.setF2(r1.get(i).getF2());
					
					r.setF3(r2.get(i).getF1());
					r.setF4(r2.get(i).getF2());
					
					rss.add(r);
				}
				
			}else {
				rss = getRptsOnly();
			}
			
			
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME = "summaryonly";
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rss);
	  		HashMap param = new HashMap();
	  		UserDtls user = Login.getUserLogin().getUserDtls();
	  		param.put("PARAM_TITLE", "SUMMARY OF COLLECTION FOR THE MONTH OF "+DateUtils.getMonthName(getMonthId()).toUpperCase() + " " + getYearId());
	  		param.put("PARAM_TOTAL", "Php"+getTotalAmountSummaryOnly());
	  		param.put("PARAM_PRINTED_DATE", DateUtils.getCurrentDateMMMMDDYYYY());
	  		
	  		
	  		param.put("PARAM_PREPAREDBY", user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
	  		
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
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
	  	    
	  	    
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
		                //System.out.println("printReportAll read : " + length);
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
		     
		       // backupPrintDispense();
		        
			
		
			System.out.println("pdf successfully created...");
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	private  void compileReportSummary(List<Reports> reportFields){
		try{
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME = "summarycollection";
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reportFields);
	  		HashMap param = new HashMap();
	  		UserDtls user = Login.getUserLogin().getUserDtls();
	  		param.put("PARAM_TITLE", "SUMMARY COLLECTION REPORT");
	  		String summary = getRptSummary();
	  		
	  		summary = summary.replace(", "+user.getFirstname(), ".");
	  		String preparedBy = user.getFirstname() + " " + user.getLastname();
	  		summary += "\n\n\n\n\n\n\n";
	  		summary += "Prepared By: " + preparedBy;
	  		
	  		param.put("PARAM_SUMMARY", summary);
	  		
	  		
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
			param.put("PARAM_WATERMARK", off);
			}catch(Exception e){e.printStackTrace();}
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
		
		
			System.out.println("pdf successfully created...");
			}catch(Exception e){
				e.printStackTrace();
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
	public void calculateTaxGross() {
		double yearly = getMonthlyTax() * GlobalVar.YEARLY;
		setLabel4(yearly);
		//double tax = getLabel4();
		double payable = yearly / GlobalVar.CEDULA_DIVEDEND;
		
		setAmount4(payable);
		calculateCedula();
	}
	public void calculateSalariesGross() {
		double yearly = getMonthlySal() * GlobalVar.YEARLY;
		setLabel3(yearly);
		//double salaries = getLabel3();
		double payable = yearly / GlobalVar.CEDULA_DIVEDEND;
		setAmount3(payable);
		calculateCedula();
	}
	
	public void calculateGrossRptax() {
		double yearly = getMonthlyIncome() * GlobalVar.YEARLY; 
		setLabel2(yearly);
		//double gross = getLabel2();
		double payable = yearly / GlobalVar.CEDULA_DIVEDEND;
		setAmount2(payable);
		
		calculateCedula();
	}
	
	public void calculateCedula() {
		
		double total = getAmount1() + getAmount2() + getAmount3() + getAmount4();
		setAmount5(total);
		double rate = Cedula.cedulaPenaltyRate(Months.getMonth(DateUtils.getCurrentMonth()));
		double interest = 0d;
		rate = rate/100;
		
		if(getAmount1()>0) {
			interest = rate * getAmount1();
		}
		
		if(getAmount2()>0) {
			interest = rate * getAmount2();
		}
		
		if(getAmount3()>0) {
			interest = rate * getAmount3();
		}
		
		if(getAmount4()>0) {
			interest = rate * getAmount4();
		}
		
		
		
		setAmount5(total);
		if(getAmount5()>0) {
			interest = rate * getAmount5();
		}
		
		
		setAmount6(interest);
		setAmount7(total + interest);
		setTotalAmount(Currency.formatAmount(getAmount7()));
		int index=getNamesDataSelected().size();
		
		if(index==2) {
			getNamesDataSelected().get(0).setDateTrans(DateUtils.getCurrentDateMonthDayYear());
			getNamesDataSelected().get(0).setAmount(getAmount5());
			
			getNamesDataSelected().get(1).setDateTrans(DateUtils.getCurrentDateMonthDayYear());
			getNamesDataSelected().get(1).setAmount(interest);
		}else {
			getNamesDataSelected().get(0).setDateTrans(DateUtils.getCurrentDateMonthDayYear());
			getNamesDataSelected().get(0).setAmount(getAmount5());
		}
	}
	
	public void updateORNumber() {
		//orNumber = ORListing.getLatestORNumber(getFormTypeId(),getCollectorId());
		ctcFlds(false);
		if(FormType.CT_2.getId()==getFormTypeId() || FormType.CT_5.getId()==getFormTypeId()) {
			setPayorName("N/A");
			setOrNumber("0");
			setSearchPayName("cash");
			//cedulaInterest();
		}else if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId()) {
			setAmount1(5.00);
			setSearchPayName("ctc");
			ctcFlds(true);
			enableBirthday=false;
			cedulaInterest();
		}else if(FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
			setAmount1(500.00);
			setSearchPayName("ctc");
			ctcFlds(true);
			enableBirthday=true;
			cedulaCorporation();
		}else if(FormType.AF_51.getId()==getFormTypeId()) {
			setSearchPayName("tax");
		}else if(FormType.AF_52.getId()==getFormTypeId()) {
			setSearchPayName("cattle");
		}else if(FormType.AF_58.getId()==getFormTypeId()) {
			setSearchPayName("Death/Burial Income");
		}
		orNumber = ORListing.getLatestORNumber(getFormTypeId(),getCollectorId());
		if(getSelectOrTypeId()>0 && getCollectorId()==0) {//show suggested if not NEW is selected
			loadLastORToSuggestForNewReceipt();
		}
		
	}
	
	
	private void cedulaInterest() {
		
		double rate = Cedula.cedulaPenaltyRate(Months.getMonth(DateUtils.getCurrentMonth()));
		double interest = 0d;
		rate = rate/100;
		
		/**
		 * 60-CTC Individual
		 * 65-CTC Interest 
		 */
		namesDataSelected = new ArrayList<>();
		setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());
		String sql = "";
		String[] params = new String[0];
		setFormTypeId(FormType.CTC_INDIVIDUAL.getId());
		int[] ids = {60};
		double[] amounts = {5.00};
		double amount = 0d;
		
		try {
			for(int i=0; i< ids.length; i++) {
				sql = " AND pyid="+ids[i];
				PaymentName py =  PaymentName.retrieve(sql, params).get(0);
				py.setAmount(amounts[i]);
				amount += amounts[i];
				namesDataSelected.add(py);
				getSelectedPaymentNameMap().put(py.getId(), py);
			}
		}catch(Exception e) {e.printStackTrace();}
		
		
		
		if(DateUtils.getCurrentMonth()>=3) {
			try {
					interest = rate * 5.00;
					sql = " AND pyid=65";
					PaymentName py =  PaymentName.retrieve(sql, params).get(0);
					py.setAmount(interest);
					amount += 0.00;
					namesDataSelected.add(py);
					getSelectedPaymentNameMap().put(py.getId(), py);
			}catch(Exception e) {}
		}
		setAmount5(5.00);
		setAmount6(interest);
		setAmount7(5.00 + interest);
		
		setTotalAmount(Currency.formatAmount(500 + interest));
	}
	
	private void cedulaCorporation() {
		
		double rate = Cedula.cedulaPenaltyRate(Months.getMonth(DateUtils.getCurrentMonth()));
		double interest = 0d;
		rate = rate/100;
		/**
		 * 60-CTC Individual
		 * 65-CTC Interest 
		 */
		namesDataSelected = new ArrayList<>();
		setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());
		String sql = "";
		String[] params = new String[0];
		setFormTypeId(FormType.CTC_CORPORATION.getId());
		int[] ids = {63};
		double[] amounts = {500.00};
		double amount = 0d;
		
		try {
			interest = rate * 500.00;
			for(int i=0; i< ids.length; i++) {
				sql = " AND pyid="+ids[i];
				PaymentName py =  PaymentName.retrieve(sql, params).get(0);
				py.setAmount(amounts[i]);
				amount += amounts[i];
				namesDataSelected.add(py);
				getSelectedPaymentNameMap().put(py.getId(), py);
			}
		}catch(Exception e) {e.printStackTrace();}
		
		
		
		if(DateUtils.getCurrentMonth()>=3) {
			try {
					sql = " AND pyid=62";
					PaymentName py =  PaymentName.retrieve(sql, params).get(0);
					py.setAmount(interest);
					amount += 0.00;
					namesDataSelected.add(py);
					getSelectedPaymentNameMap().put(py.getId(), py);
			}catch(Exception e) {}
		}
		setAmount5(500);
		setAmount6(interest);
		setAmount7(500 + interest);
		
		setTotalAmount(Currency.formatAmount(500 + interest));
	}

	public void loadContent() {
		setTextContent("<p><h1>Analyzing the data please wait....</h1></p>");
		Map<String, Double> names = new HashMap<String, Double>();//Collections.synchronizedMap(new HashMap<String, Double>());
		Map<Double, String> amounts = new HashMap<Double, String>();//Collections.synchronizedMap(new HashMap<Double, String>());
		
		
		UserDtls user = Login.getUserLogin().getUserDtls();
		
		String[] greets = {"Good day","Hope this update finds you well","I hope you're having a wonderful day"};
		
		String text = "<p><strong>Greetings</strong></p>";
			  text += "</br>";
			  text +=  "<p><strong>"+ greets[(int) (Math.random() * greets.length)] + ", "+ user.getFirstname() +"</strong></p>";
			  text += "</br>";
			  
			  
			  double amount = 0d;
			  String str = "";
			  for(int month=1; month<=DateUtils.getCurrentMonth(); month++) {
				  String sql = " AND orl.orstatus=4 AND (orl.ordatetrans>=? AND orl.ordatetrans<=?)";//encoded
				  String[] params = new String[2];
				  params[0] = DateUtils.getCurrentYear() + "-"+ (month<10? "0" + month : month) +"-01";
				  params[1] = DateUtils.getCurrentYear() + "-"+ (month<10? "0" + month : month) +"-31";
				  str += "<li>"+ DateUtils.getMonthName(month) +" <strong>Php ";
				  double amnt = 0d;
				  for(ORListing or : ORListing.retrieve(sql, params)) {
					  amount += or.getAmount();
					  amnt += or.getAmount();
					  
					  String name = or.getCollector().getName();
					  String dep = "";
					  try {
						  dep = Department.department(or.getCollector().getDepartment().getDepid()+"").getDepartmentName();
						  name += "("+dep+")";
					  }catch(Exception e) {}
					  if(names!=null && names.containsKey(name)) {
						  double tmp = names.get(name);
						  tmp += or.getAmount();
						  names.put(name, tmp);
					  }else {
						  names.put(name, or.getAmount());
					  }
					  
				  }
				  if(amnt==0) {
					  str += Currency.formatAmount(amnt) +" (No data has been recorded)</strong></li>";
				  }else {
					  str += Currency.formatAmount(amnt) +"</strong></li>";
				  }
			  }
			  text += "<p>As of <strong>"+ DateUtils.getCurrentDateMMMMDDYYYY() +"</strong> the total collected amount is <strong>Php"+ Currency.formatAmount(amount) +"</strong></p>";
			  text += "</br><p>Below are the details collected per month.</p>";
			  //text += "</br>";
			  text += "<ul>";
			  text += str;
			  text += "</ul>";
			  text += "</br>";
			  text += "<p>Collectors Information.</p>";
			  for(String name : names.keySet()) {
				  amounts.put(names.get(name), name);
			  }
			  Map<Double, String> sortedAmount = new TreeMap<Double, String>(amounts);
			  
			  
			  int cnt = 1;
			  str = "";
			  List<String> data = new ArrayList<String>();//Collections.synchronizedList(new ArrayList<String>());
			  for(double a : sortedAmount.keySet()) {
				  //str += "<li>"+ cnt++ +") "+ sortedAmount.get(a) +" <strong>Php" + Currency.formatAmount(a) +"</strong></li>";
				  data.add(sortedAmount.get(a)+" <strong>Php" + Currency.formatAmount(a) +"</strong>");
			  }
			  Collections.reverse(data);
			  for(String s : data) {
				  str +="<li>"+ cnt++ +") "+ s +"</strong>";
			  }
			  text += "<ul>";
			  text += str;
			  text += "</ul>";
			  
			  
		setTextContent(text);
		collectorMsgSavePath(text, "collection");
		setRptSummary(removingHtmlTag(text));
	}
	
	private String removingHtmlTag(String content) {
		
		String[] tags = {"<p>","</p>","<strong>","</strong>","</br>","<ul>","</ul>","<li>","</li>"};
		
		for(int i=0; i<tags.length; i++) {
			if(tags[i].equalsIgnoreCase("<p>")) {
				content = content.replace(tags[i], "\n");
			}else if(tags[i].equalsIgnoreCase("</br>")) {
				content = content.replace(tags[i], "\n");
			}else if(tags[i].equalsIgnoreCase("<li>")) {
				content = content.replace(tags[i], "\n");	
			}else {
				content = content.replace(tags[i], "");
			}
		}
		
		return content;
	}
	
	public void loadMsg() {
		String folderPath = System.getenv("SystemDrive");
		folderPath += File.separator + "webtis" + File.separator + "conf" + File.separator;
		setTextContent(readMsgCollectorFile(folderPath, "collection.tis"));
		
		setRptSummary(removingHtmlTag(getTextContent()));
	}
	
	public String readMsgCollectorFile(String path, String fileName) {
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path + fileName));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while(line!=null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
			
		}catch(Exception e) {}
		
		return "";
	}
	
	public void collectorMsgSavePath(String msg, String fileName) {
		
		String folderPath = System.getenv("SystemDrive");
		folderPath += File.separator + "webtis" + File.separator + "conf" + File.separator;
		//fileName += fileName;
		
		File file = new File(folderPath);
		
		if(!file.isDirectory()) {
			file.mkdir();
		}
		
		try {
		
		File email = new File(folderPath + fileName + ".tis");
		PrintWriter pw = new PrintWriter(new FileWriter(email));
		pw.println(msg);
		pw.flush();
		pw.close();
		
		
		}catch(Exception e) {}
	}
	
	public void oncapture(CaptureEvent captureEvent) {
		System.out.println("=======================================================SCANNING==================================================================");
        try {
            if (captureEvent != null) {
                Result result = null;
                BufferedImage image = null;

                InputStream in = new ByteArrayInputStream(captureEvent.getData());

                image = ImageIO.read(in);

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                result = new MultiFormatReader().decode(bitmap);
                System.out.println("Scanning the qrcode");
                if (result != null) {
                    //setResultText(result.getText());
                	System.out.println(result.getText());
                	setQrcodeMsg("QRCode has been read successfully");
                	//setQrCodeData(result.getText());
                	PrimeFaces pf = PrimeFaces.current();
                	pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
                	
                }
            }
        } catch (NotFoundException ex) {
            // fall thru, it means there is no QR code in image
        	//setResultText("Image is not readable...");
        	System.out.println("==============WARNING============ERROR=========");
        	setQrcodeMsg("Error reading the qrcode....");
        } catch (ReaderException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
	public void acceptSelection(String sel) {
		
		if(getCustomerDataSelected()!=null) {
		
		com.italia.municipality.lakesebu.licensing.controller.Customer cus = getCustomerDataSelected();	
		
		PrimeFaces pf = PrimeFaces.current();
		if("individual".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.CTC_INDIVIDUAL.getId());
		}
		if("corporation".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.CTC_CORPORATION.getId());
		}
		if("51".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.AF_51.getId());
		}
		if("52".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.AF_52.getId());
		}
		if("53".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.AF_53.getId());
		}
		if("54".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.AF_54.getId());
		}
		if("56".equalsIgnoreCase(sel)) {
			pf.executeScript("PF('dlgSelection').hide();");
			setFormTypeId(FormType.AF_56.getId());
		}
		
		//setPayorName(cus.getLastname().toUpperCase() + ", " + cus.getFirstname().toUpperCase() + " " + cus.getMiddlename().substring(0, 1).toUpperCase() + ".");
		setPayorName(cus.getFullname().toUpperCase());
		setCustomerAddress(cus.getCompleteAddress());
		
		ctcFlds(false);
		if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId() || FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
			
			
			setLabel2(0);
			setLabel3(0);
			setLabel4(0);
			setAmount1(5.00);
			setAmount2(0);
			setAmount3(0);
			setAmount4(0);
			
			setGenderId(Integer.valueOf(cus.getGender()));
			setBirthdate(DateUtils.convertDateString(cus.getBirthdate(), "yyyy-MM-dd"));
			setHieghtDateReg(cus.getHeight());
			setWeight(cus.getWeight());
			
			setCivilStatusId(cus.getCivilStatus());
			setProfessionBusinessNature(cus.getWork());
			setPlaceOfBirth(cus.getBornplace());
			setCitizenshipOrganization(cus.getCitizenship());
			
			ctcFlds(true);
			
			if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId()) {
				setEnableBirthday(false);
				cedulaInterest();
			}else {
				setAmount1(500.00);
				setEnableBirthday(true);
				cedulaCorporation();
			}
			
			
			
		}else {
			ctcFlds(false);
		}
		
		updateORNumber();
		
		}
	}
	
	public void supplyCustomerInfo() {
		if(getMapCustomer()!=null && getMapCustomer().size()>0 && getMapCustomer().get(getPayorName())!=null){
			Customer cus = getMapCustomer().get(getPayorName());
			
			//setPayorName(cus.getLastname().toUpperCase() + ", " + cus.getFirstname().toUpperCase() + " " + cus.getMiddlename().substring(0, 1).toUpperCase() + ".");
			setCustomerAddress(cus.getCompleteAddress());
			
			ctcFlds(false);
			if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId() || FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
				
				
				setLabel2(0);
				setLabel3(0);
				setLabel4(0);
				setAmount1(5.00);
				setAmount2(0);
				setAmount3(0);
				setAmount4(0);
				
				setGenderId(Integer.valueOf(cus.getGender()));
				setBirthdate(DateUtils.convertDateString(cus.getBirthdate(), "yyyy-MM-dd"));
				setHieghtDateReg(cus.getHeight());
				setWeight(cus.getWeight());
				
				setCivilStatusId(cus.getCivilStatus());
				setProfessionBusinessNature(cus.getWork());
				setPlaceOfBirth(cus.getBornplace());
				setCitizenshipOrganization(cus.getCitizenship());
				setCustomerAddress("");
				if(cus.getBornplace()==null) {
					setAddress(cus.getCompleteAddress());
				}
				ctcFlds(true);
				
				if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId()) {
					setEnableBirthday(false);
					cedulaInterest();
				}else {
					setAmount1(500.00);
					setEnableBirthday(true);
					cedulaCorporation();
				}
				
			}else {
				ctcFlds(false);
			}
			
			updateORNumber();
			
		}else {
			if(FormType.CTC_INDIVIDUAL.getId()==getFormTypeId() || FormType.CTC_CORPORATION.getId()==getFormTypeId()) {
				ctcFlds(true);
			}
			completNameOfClient(getPayorName());
			
		}
		completNameOfClient(getPayorName());
		
		
	}
	
	private void completNameOfClient(String name) {
		//System.out.println("checking completNameOfClient="+name);
		if(name!=null && !name.isEmpty()) {
			
			if(name.contains(",")) {
				//System.out.println("contain comma");
				String[] coma = name.split(",");
					setLastName(coma[0]);
					//System.out.println("setting up lastname: " + coma[0]);
				if(coma[1].contains(" ")) {
					String camma = coma[1].trim();
					//System.out.println("contain space ="+camma);
					String[] space = camma.split(" ");
					int size = space.length;
					//System.out.println("check space size:" + size);
					switch(size) {
					case 1:
						setFirstName(space[0]);
						setMiddleName(".");
						break;
					case 2:
						setFirstName(space[0]);
						setMiddleName(space[1]);
						break;
					case 3:
						setFirstName(space[0] + " " + space[1]);
						setMiddleName(space[2]);
						break;
					case 4:
						setFirstName(space[0] + " " + space[1]);
						setMiddleName(space[2] + " " + space[3]);
						break;	
						}
				}else {
					//System.out.println("adding period on lastname");
					setFirstName(coma[1].trim());
					setMiddleName(".");
				}
			}else {
				//detect company name
				//System.out.println("no last name looks like for company");
				setFirstName(name);
				setMiddleName(".");
				setLastName(".");
			}
		}else {
			//System.out.println("no cutter has been made...");
			setFirstName(getPayorName());
			setMiddleName(".");
			setLastName(".");
		}
		//System.out.println("Fullname....FirstName:"+getFirstName()+" MiddleName:"+getMiddleName()+" LastName:"+getLastName());
	}
	
	public void doneCustomerDetails() {
		boolean isOk = true;
		PrimeFaces pf = PrimeFaces.current();
		String scs = "";
		if(getFirstName()==null || getFirstName().isEmpty()) {
			isOk = false;
			scs += "$('#firstId').css('border', '3px solid red');";
		}
		if(getMiddleName()==null || getMiddleName().isEmpty()) {
			isOk = false;
			scs += "$('#middleId').css('border', '3px solid red');";
		}
		if(getLastName()==null || getLastName().isEmpty()) {
			isOk = false;
			scs += "$('#lastId').css('border', '3px solid red');";
		}
		if(isOk) {
			scs="";
			scs += "$('#firstId').css('border', '3px solid black');";
			scs += "$('#middleId').css('border', '3px solid black');";
			scs += "$('#lastId').css('border', '3px solid black');";
			scs +="PF('dlgCustomerInfo').hide(1000)";
			setPayorName(getLastName().toUpperCase() + ", " + getFirstName().toUpperCase()  +" "+ getMiddleName().toUpperCase().substring(0,1) + ". ");
			checkClientNameInDB();
		}
		pf.executeScript(scs);
	}
	
	private void checkClientNameInDB() {
		String sql = " AND cus.fullname=?";
		String[] params = new String[1];
		params[0] = getFirstName().toUpperCase() +" "+ getLastName().toUpperCase();
		List<Customer> cus = Customer.retrieve(sql, params);
		if(cus!=null && cus.size()>0) {
			Customer cust = cus.get(0);
			setMapCustomer(new LinkedHashMap<String, Customer>());
			String fullName = cust.getLastname() + ", " + cust.getFirstname() + " " + (cust.getMiddlename()!=null? cust.getMiddlename().substring(0, 1)+"." : ".");
			setPayorName(fullName);
			getMapCustomer().put(fullName,cust);
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
		if(jsonData!=null && !jsonData.isEmpty() && jsonData.contains("PLS")) {
			String sql = "";
			Customer cs = null;
			String fullname = jsonData;
			
			String cardno = "";
			String first = "";
			String middle = "";
			String last = "";
			String address = "";
			String birthdate = DateUtils.getCurrentDateYYYYMMDD();
			String gender = "1";
			String civilStatus = "1";
			
			if(jsonData.contains(":")) {
			
			String[] name = jsonData.split(":");
			
			
			try {cardno = name[0];}catch(IndexOutOfBoundsException e) {}
			try {first = name[1];}catch(IndexOutOfBoundsException e) {}
			try {middle = name[2];}catch(IndexOutOfBoundsException e) {}
			try {last = name[3];}catch(IndexOutOfBoundsException e) {}
			try {address = name[4];}catch(IndexOutOfBoundsException e) {}
			try {birthdate = name[5];}catch(IndexOutOfBoundsException e) {}
			try {gender = name[7];}catch(IndexOutOfBoundsException e) {}
			try {civilStatus = name[8];}catch(IndexOutOfBoundsException e) {}
			
			fullname = name[3] + ", " + name[1] + " " + name[2];
			
			
			System.out.println("fullname " + fullname);
			
			sql = " AND cus.cuscardno like '%"+ cardno.trim() +"%' ";
			
				
			setFullnameQR(fullname);
			
			}else {
				sql = " AND cus.cuscardno  like '%"+ jsonData.trim() +"%' ";
			}
				
			List<PoblacionCustomer> cus = PoblacionCustomer.retrieve(sql, new String[0]);
			if(cus!=null && cus.size()>0) {
				PoblacionCustomer pc = cus.get(0);
						cs = Customer.builder()
						.firstname(pc.getFirstname())
						.middlename(pc.getMiddlename())
						.lastname(pc.getLastname())
						.birthdate(pc.getBirthdate())
						.gender(pc.getGender())
						.civilStatus(pc.getCivilStatus())
						.fullname(pc.getFullname())
						.age(pc.getAge())
						.contactno(pc.getContactno())
						.cardno(pc.getCardno())
						.dateregistered(pc.getDateregistered())
						.completeAddress(pc.getCompleteAddress())
						.build();
			
				setCustomerDataSelected(cs);
				pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
			}else {
				
				
				cs = Customer.builder()
						.firstname(first)
						.middlename(middle)
						.lastname(last)
						.birthdate(birthdate)
						.gender(gender)
						.civilStatus(Integer.valueOf(civilStatus))
						.fullname(last + ", " + first + " " + middle)
						.completeAddress(address)
						.build();
				setFullnameQR(last + ", " + first + " " + middle);
				setCustomerDataSelected(cs);
				pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
			}
			
			
			
		}else {
		
			NationalIDJsonData data = new NationalIDJsonData(jsonData);
			if(data!=null && !data.getPCN().isEmpty()) {	
				System.out.println("Checking PCN: " + data.getPCN());
			/*
			String sql = " AND (cus.qrcode like '%"+ jsonData +"%' OR ";
			sql += " cus.fullname like '%"+ jsonData +"%' OR ";
			sql += " cus.nationalid like '%"+ jsonData +"%' ";
			
			sql += " )";*/
				
			String sql = " AND cus.nationalid='"+ data.getPCN().trim() +"'";	
				
			String[] params = new String[0];
			
			List<com.italia.municipality.lakesebu.licensing.controller.Customer> cust = com.italia.municipality.lakesebu.licensing.controller.Customer.retrieve(sql, params);
			
				if(cust!=null && cust.size()>0) {
					setCustomerDataSelected(cust.get(0));
					setFullnameQR(cust.get(0).getFullname());
			    	pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
				}else {
					com.italia.municipality.lakesebu.licensing.controller.Customer cus = com.italia.municipality.lakesebu.licensing.controller.Customer.builder()
							.nationalId(data.getPCN())
							.firstname(data.getFName())
							.middlename(data.getMName())
							.lastname(data.getLName())
							.completeAddress(data.getPOB())
							.bornplace(data.getPOB())
							.birthdate(data.getDOB())
							.gender(data.getSex().equalsIgnoreCase("Male")? "1" : "2")
							.fullname(data.getLName()+", " + data.getFName() + " " +data.getMName())
							.build();
					
					setFullnameQR(data.getLName() + ", " + data.getFName() + " " + data.getMName());
					setCustomerDataSelected(cus);
					pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
				}
			
			}else {
				String sql = " AND cus.qrcode='%"+ jsonData +"%'";
				String[] params = new String[0];
				
				List<com.italia.municipality.lakesebu.licensing.controller.Customer> cust = com.italia.municipality.lakesebu.licensing.controller.Customer.retrieve(sql, params);
				
					if(cust!=null && cust.size()>0) {
						setCustomerDataSelected(cust.get(0));
						setFullnameQR(cust.get(0).getFullname());
				    	pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
					}else {
						Application.addMessage(1, "Error", "This QRCode is not yet registered... Please register it first in Citizen Registration Page");
					}
			}
		}
		
	}
	
	public void runUpdate() {
		PrimeFaces pf = PrimeFaces.current();
		File folder = new File(GlobalVar.COMMIT_XML);
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles!=null && listOfFiles.length>0) {
			boolean hasFiles = false;
			for(File f : listOfFiles) {
				if(f.isFile()) {
					hasFiles = true;
				}
			}
			if(hasFiles) {
				pf.executeScript("PF('dlgSendOr').show(1000)");
				Application.addMessage(1, "Failure to update", "Please send your data to server first.");
			}
			
		}else {
			pf.executeScript("PF('dlgFetch').show(1000)");
		}
	}
	
	public void loadSelectedGroupRpt(String partial) {
		setSelectedCollection(new ArrayList<ORListing>());
		/*double tmpAmount = 0d;
		for(ORListing or : ORListing.retrieveGenReportGroup(getOrsReportsId())) {
			tmpAmount += or.getAmount();
			selectedCollection.add(or);
		}*/
		if("PARTIAL".equalsIgnoreCase(partial)) {
			partial = " LIMIT 10";
		}else {
			partial = null;
		}
		
		
		Object[] obj = ORListing.retrieveGenReportGroup(getReportId(),partial);
		double tmpAmount = (Double)obj[0];
		selectedCollection = (ArrayList<ORListing>)obj[1];
		setTotalSelectedAmount(Numbers.roundOf(tmpAmount, 2));
		
	}
	
	public void onORDrop(DragDropEvent<ORListing> ddEvent) {
		ORListing ors = ddEvent.getData();
		System.out.println("Adding OR: " + ors.getOrNumber() + " groupId: " + getOrsReportsId() + " ORID: " + ors.getId());
		if(ors.getGenCollection()==0) {
		
		double amount = ors.getAmount();
		double tmpAmount = getTotalSelectedAmount();
		setTotalSelectedAmount(Numbers.roundOf(tmpAmount + amount, 2));
		
		//ors.setGenCollection(getOrsReportsId());
		ORListing.updateGenGoup(true,ors.getId(), getOrsReportsId());
		selections.remove(ors);
		getSelectedCollection().add(ors);
        //loadSelectedGroupRpt();
        
		}else {
			Application.addMessage(2, "Warning", "Accountable form is Already on the group.");
		}
    }
	public void commandActionDelete(ORListing orsel) {
		getSelectedCollection().remove(orsel);
		ORListing.updateGenGoup(false,orsel.getId(), getOrsReportsId());
		double amount = orsel.getAmount();
		double tmpAmount = getTotalSelectedAmount();
		setTotalSelectedAmount(Numbers.roundOf(tmpAmount - amount, 2));
		//loadSelectedGroupRpt();
	}
	
	public void printFinal(String val) {
		List<ORListing> orselected = new ArrayList<ORListing>();
		if("DIRECT".equalsIgnoreCase(val)) {
			orselected = getFinalRpts();
		}else {
			orselected = getSelectedCollection();
		}

		rpts = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		dtls = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		//init();//get the data from init
		Map<Long, PaymentName> mapData = new HashMap<Long, PaymentName>();//Collections.synchronizedMap(new HashMap<Long, PaymentName>());
		double grandTotal = 0d;
		Reports rpt = new Reports();
		double grandcancelledAmnt = 0d;
		double barangayctcamount = 0d;
		double mtoctcamount = 0d;
		double ctctotal = 0d;
		for(ORListing or : orselected) {
			rpt = new Reports();
			rpt.setF8(or.getStatusName());
			rpt.setF1(or.getDateTrans());
			rpt.setF2(or.getOrNumber());
			rpt.setF3(or.getCustomer().getFullname());
			rpt.setF4(or.getFormName());
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7(or.getCollector().getName());
			dtls.add(rpt);
			rpts.add(rpt);
			
			double amount = 0d;
			double canAmount = 0d;
			boolean isCTC = false;
			//if(or.getFormType()==FormType.CTC_CORPORATION.getId() || or.getFormType()==FormType.CTC_INDIVIDUAL.getId()) {
			if(or.getFormType()==FormType.CTC_INDIVIDUAL.getId()) {
				isCTC = true;
			}
			
			//this line of code has been change. check below codes
			//boolean isbarangayCtc = or.getCollector().getDepartment().getDepid()>=62? true : false; //62-80 = barangay
			
			//revise above condition for barangay
			Department dep = or.getCollector().getDepartment();
			boolean isbarangayCtc = false;
			if(dep.getDepid()>=62 && dep.getDepid()<=80) {//barangay cedula
				isbarangayCtc = true;
			}
			
			
			for(ORNameList n : or.getOrNameList()) {
				rpt = new Reports();
				rpt.setF1("");
				rpt.setF2("");
				rpt.setF3("");
				rpt.setF4("");
				rpt.setF5(n.getPaymentName().getName());
				rpt.setF6(Currency.formatAmount(n.getAmount()));
				rpt.setF7("");
				dtls.add(rpt);
				rpts.add(rpt);
				if(FormStatus.CANCELLED.getId()!=or.getStatus()) {
					amount += n.getAmount();
					if(isCTC) {
						ctctotal += n.getAmount();
						if(isbarangayCtc) {
							barangayctcamount += n.getAmount();
						}else {
							mtoctcamount += n.getAmount();
						}
					}
				}else {
					grandcancelledAmnt += n.getAmount();
					canAmount += n.getAmount();
				}
				
				
				//for report summary only
				//do not remove
				
				long key = n.getPaymentName().getId();	
				if(mapData!=null && mapData.containsKey(key)) {
					double amnt = mapData.get(key).getAmount();
					amnt += n.getAmount();
					PaymentName na = n.getPaymentName();
					na.setAmount(amnt);
					mapData.put(key, na);
				}else {
					PaymentName na = n.getPaymentName();
					na.setAmount(n.getAmount());
					mapData.put(key, na);
				}
				
			}
			rpt = new Reports();
			rpt.setF1("Total");
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			rpt.setF5("");
			if(FormStatus.CANCELLED.getId()!=or.getStatus()) {
				rpt.setF6(Currency.formatAmount(amount));
			}else {
				rpt.setF6("("+Currency.formatAmount(canAmount)+")");
			}
			rpt.setF7("");
			dtls.add(rpt);
			//rpts.add(rpt);
			grandTotal += amount;
		}
		rpt = new Reports();
		rpt.setF1("Grand Total");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("");
		rpt.setF6(Currency.formatAmount(grandTotal));
		dtls.add(rpt);
		rpts.add(rpt);
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("");
		rpt.setF6("");
		dtls.add(rpt);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Summary Details");
		rpt.setF6("");
		dtls.add(rpt);
		
		rptsOnly = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		
		double sumAmount = 0d;
		String sumN="";
		String sumA="";
		Reports rss = new Reports();
		for(PaymentName nem : mapData.values()) {
			rpt = new Reports();
			rpt.setF1("");
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			rpt.setF5(nem.getName());
			sumAmount += nem.getAmount();
			rpt.setF6(Currency.formatAmount(nem.getAmount()));
			dtls.add(rpt);
			sumN += rpt.getF5() + "\n";
			sumA += rpt.getF6() + "\n";
			
			rss = new Reports();
			rss.setF1(nem.getName());
			rss.setF2(Currency.formatAmount(nem.getAmount()));
			rptsOnly.add(rss);
		}
		
		sumAmount -= grandcancelledAmnt;
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Minus Cancelled OR");
		rpt.setF6("("+Currency.formatAmount(grandcancelledAmnt)+")");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Minus Cancelled OR");
		rss.setF2("("+Currency.formatAmount(grandcancelledAmnt)+")");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		double sharedAmount = barangayctcamount * 0.50;
		rpt.setF5("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
		rpt.setF6("");
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		rss = new Reports();
		rss.setF1("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
		rss.setF2("");
		rptsOnly.add(rss);
		
		rpt = new Reports();
		rpt.setF1("");
		rpt.setF2("");
		rpt.setF3("");
		rpt.setF4("");
		rpt.setF5("Grand Total");
		rpt.setF6(Currency.formatAmount(sumAmount));
		dtls.add(rpt);
		sumN += rpt.getF5() + "\n";
		sumA += rpt.getF6() + "\n";
		
		setSumNames(sumN);
		setSumAmounts(sumA);
		
		setTotalAmountSummaryOnly(Currency.formatAmount(sumAmount));
		
		print();
	}
	
	public void loadClient() {
		String sql = " AND (status=" + ClientStatus.QUEUE.getId() + " OR status="+ ClientStatus.SERVING.getId()+" )";
		if(getUser().getAccessLevel().getLevel()>2) {
		 sql += " AND (isid=0 OR isid="+ issuedCollectorId +")";
		}
		clients = new ArrayList<Client>();
		
		if(getQueSequence()!=null) {
			sql += " AND transno like '%"+ getQueYear() +"-" + getQueMonth() +"-"+ getQueDay() + "-"+ getQueSequence() +"%' ORDER BY transno";
		}else {
			sql += " AND transno like '%"+ getQueYear() +"-" + getQueMonth() +"-"+ getQueDay() + "-%' ORDER BY transno";
		}
		
		clients = Client.retrieve(sql, new String[0]);
		
		/*
		 * if(clients!=null && clients.size()==1) { grabQue(clients.get(0)); PrimeFaces
		 * pf = PrimeFaces.current(); pf.executeScript("PF('panelWgr').toggle()"); }
		 */
		
	}
	
	@Setter @Getter private Client clientTmpSelected;
	public void popUpRemarks(Client c) {
		if(c.getTransType()==ClientTransactionType.CEDULA.getId()) {
			grabQue(c);
		}else {
			
			if(c.getRemarks()!=null && !c.getRemarks().isEmpty()) {
				setClientTmpSelected(c);
				PrimeFaces pf = PrimeFaces.current();
				pf.executeScript("PF('dlgPopNotes').show(1000);");
			}else {
				grabQue(c);
			}
		}
	}
	
	public void cancelRequest() {
		clientTmpSelected.setStatus(ClientStatus.CANCELLED.getId());
		clientTmpSelected.setCollectorId(issuedCollectorId);
		
		clientTmpSelected.save();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgPopNotes').hide(1000);");
		Application.addMessage(1, "Success", "Successfully cancelled.");
		
		loadClient();
	}
	
	public void proceedOR() {
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgPopNotes').hide(1000);");
		grabQue(getClientTmpSelected());
	}
	
	public void grabQue(Client c) {
		
		
		setFirstName(c.getFirstName().toUpperCase());
		try{setMiddleName(c.getMiddleName().toUpperCase());}catch(NullPointerException e) {}
		setLastName(c.getLastName().toUpperCase());
		String fullName = c.getLastName() +", " + c.getFirstName() + " " + c.getMiddleName();
		setPayorName(fullName.toUpperCase());
		setPlaceOfBirth(c.getBirthPlace());
		try{setAddress(c.getAddress().toUpperCase());}catch(NullPointerException e) {}
		//try{setCustomerAddress(c.getAddress().toUpperCase());}catch(NullPointerException e) {}//company address
		
		
		setFormTypeId(FormType.CTC_INDIVIDUAL.getId());
		ctcFlds(false);
		if(c.getTransType()==0) {
		
			
			
			String[] types = Words.getTagName("profession-list").split(",");
			for(String prof : types) {
				String[] vals = prof.split("-");
				if(c.getProfession().equalsIgnoreCase(vals[0])) {
					setProfessionBusinessNature(vals[1]);
				}
			}
			
			//setLabel2(0);
			//setLabel3(0);
			//setLabel4(0);
			setAmount1(5.00);
			//setAmount2(0);
			//setAmount3(0);
			//setAmount4(0);
			
			setGenderId(Integer.valueOf(c.getGender()));
			setBirthdate(DateUtils.convertDateString(c.getBirthday(), "yyyy-MM-dd"));
			setHieghtDateReg(c.getHeight());
			setWeight(c.getWeight());
			setTinNo(c.getTinNo());
			setCivilStatusId(c.getCivilStatus());
			
			setPlaceOfBirth(c.getBirthPlace());
			setCitizenshipOrganization(c.getNationality());
			
			ctcFlds(true);
			
			setEnableBirthday(false);
			cedulaInterest();
			
			//System.out.println("Profession: "+ getProfessionBusinessNature() +" Salary: " + c.getMonthlySalary());
			
			int type = Integer.valueOf(c.getProfession());
			if(type==0) {//senior citizen
				setAmount3(0);
				calculateCedula();
			}else if(type<=4 || type==25) {//daily income person
				setAmount3(c.getMonthlySalary());
				calculateCedula();
			}else if(type==5 || type==6) {//businessman
				//setAmount2(c.getMonthlySalary());
				setMonthlyIncome(c.getMonthlySalary());
				calculateGrossRptax();
			}else {
				setMonthlySal(c.getMonthlySalary());
				calculateSalariesGross();
			}
			
		}else {
			setFormTypeId(FormType.AF_51.getId());
			
			if(c.getTransType()==ClientTransactionType.BUSINESS_NEW.getId()) {
				setNotes(c.getBusinessName() + " (New)");
			}else if(c.getTransType()==ClientTransactionType.BUSINESS_RENEWAL.getId()) { 
				setNotes(c.getBusinessName() + " (Renew)");
			}else if (c.getTransType()==ClientTransactionType.QUARTERLY_BUSINESS_PAYMENT.getId()) {
				setNotes(c.getBusinessName() + " (Quarterly)");
			}else {
				setNotes("Request for "+ClientTransactionType.nameId(c.getTransType()));
			}
			
			
			chargingForOR(c);
		}
		
		//updateORNumber();
		
		clients.remove(c);
		
		//updating client status to serving
		c.setStatus(ClientStatus.SERVING.getId());
		c.setCollectorId(issuedCollectorId);
		c.save();
		setClientSelected(c);
	}
	
	
	
	private void chargingForOR(Client c) {
		namesDataSelected = new ArrayList<>();
		setSelectedPaymentNameMap(new HashMap<Long, PaymentName>());
		int[] ids = new int[0];
		double[] amounts = {5.00};
		
		if(c.getTransType()==ClientTransactionType.TRAFFICVIOLATION_MAJOR.getId()) {
			ids = new int[1];
			amounts = new double[1];
			ids[0] = 66;
			amounts[0] = 300.00;
		}if(c.getTransType()==ClientTransactionType.TRAFFICVIOLATION_MINOR.getId()) {
			ids = new int[1];
			amounts = new double[1];
			ids[0] = 66;
			amounts[0] = 250.00;	
		}else if(c.getTransType()==ClientTransactionType.POLICE_CLEARANCE_LOCAL.getId()) {
			ids = new int[4];
			amounts = new double[4];
			//dst
			ids[0] = 53;
			amounts[0] = 30.00;
			//sec
			ids[1] = 20;
			amounts[1] = 50.00;
			//police clearane
			ids[2] = 19;
			amounts[2] = 20.00;
			//police misc
			ids[3] = 36;
			amounts[3] = 65.00;
		}else if(c.getTransType()==ClientTransactionType.POLICE_CLEARANCE_ABROAD.getId()) {
			ids = new int[4];
			amounts = new double[4];
			//dst
			ids[0] = 53;
			amounts[0] = 30.00;
			//sec
			ids[1] = 20;
			amounts[1] = 50.00;
			//police clearane
			ids[2] = 19;
			amounts[2] = 200.00;
			//police misc
			ids[3] = 36;
			amounts[3] = 65.00;
		}else if(c.getTransType()==ClientTransactionType.TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 34;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.BAMBOO_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 122;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.ABACA_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 123;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.RATTAN_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 124;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.WOOD_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 125;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.FURNITURE_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 126;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.SWINE_TRANSPORT.getId() || c.getTransType()==ClientTransactionType.OTHER_ANIMAL_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 127;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.POULTRY_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 128;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.CARABAO_TRANSPORT.getId() || c.getTransType()==ClientTransactionType.COW_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			
			ids[0] = 129;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.LUMBER_TRANSPORT.getId()) {
			ids = new int[1];
			amounts = new double[1];
			//dst
			ids[0] = 214;
			amounts[0] = 00.00;
		}else if(c.getTransType()==ClientTransactionType.RETIREMENT_BUSINESS.getId()) {
			ids = new int[3];
			amounts = new double[3];
			//dst
			ids[0] = 53;
			amounts[0] = 30.00;
			//sec
			ids[1] = 20;
			amounts[1] = 50.00;
			//business closure certification
			ids[2] = 215;
			amounts[2] = 100.00;
		}else if(c.getTransType()==ClientTransactionType.LAND_TAX_CLEARANCE.getId()) {
			ids = new int[1];
			amounts = new double[1];
			//dst
			ids[0] = 21;
			amounts[0] = 100.00;
		}else if(c.getTransType()==ClientTransactionType.BUSINESS_NEW.getId() ||  c.getTransType()==ClientTransactionType.BUSINESS_RENEWAL.getId()) {
			ids = new int[11];
			amounts = new double[11];
			//sanitary fee
			ids[0] = 33;
			amounts[0] = 40.00;
			//mayor's permit
			ids[1] = 4;
			amounts[1] = 0.00;
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
			//other business
			ids[9] = 90;
			amounts[9] = 00.00;
			//garbage
			ids[10] = 28;
			amounts[10] = 275.00;
			
			System.out.println("Diri sa new renew nag sulod");
			
		}else if(c.getTransType()==ClientTransactionType.QUARTERLY_BUSINESS_PAYMENT.getId()){
			ids = new int[2];
			amounts = new double[2];
			
			//other business
			ids[0] = 90;
			amounts[0] = 0.00;
			
			//businesss surcharge
			ids[1] = 91;
			amounts[1] = 0.00;
			
			System.out.println("Diri sa quarterly nag sulod");
		}else if(c.getTransType()==ClientTransactionType.FISHCAGE_NEW.getId() ||  c.getTransType()==ClientTransactionType.FISHCAGE_RENEW.getId()) {
			ids = new int[9];
			amounts = new double[9];
			//sanitary fee
			ids[0] = 33;
			amounts[0] = 40.00;
			//inspection fees
			ids[1] = 18;
			amounts[1] = 40.00;
			//water rental
			ids[2] = 3;
			amounts[2] = 0.00;
			//police clearance
			ids[3] = 19;
			amounts[3] = 40.00;
			//mayor's permit
			ids[4] = 4;
			amounts[4] = 0.00;
			//secretary fee
			ids[5] = 20;
			amounts[5] = 50.00;
			//dst
			ids[6] = 53;
			amounts[6] = 30.00;
			//zonal
			ids[7] = 9;
			amounts[7] = 100.00;
			//banca registration
			ids[8] = 14;
			amounts[8] = 75.00;
			
		}else if(c.getTransType()==ClientTransactionType.OUTDOOR_ACTIVITIES.getId()) {
			ids = new int[1];
			amounts = new double[1];
			//sanitary fee
			ids[0] = 166;
			amounts[0] = 0.00;
		}else if(c.getTransType()==ClientTransactionType.OTHER_DOCUMENTS.getId()) {
			ids = new int[3];
			amounts = new double[3];
			//documentation
			ids[0] = 56;
			amounts[0] = 0.00;
			//documentation from MPDC
			ids[1] = 134;
			amounts[1] = 0.00;
			//documentation from Tourism
			ids[2] = 235;
			amounts[2] = 0.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_REG.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 16;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_CERTIFIED_TRUE_COPY.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 95;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_LIVE_CERTIFICATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 96;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_MACHINE_FORM1A.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 138;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_FILING_FEE_9255.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 140;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_CERTIFICATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 144;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_MACHINE_COPY_LIVE.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 158;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_DELAYED_REG.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 160;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.BIRTH_MACHINE_COPY.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 165;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.DEATH_CERTIFICATE.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 49;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.DEATH_MACHINE.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 137;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.DEATH_DELAYED_REG.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 154;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.CADAVER_TRANSFER.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 210;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.CADAVER_EXHUMATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 50;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARIAGE_REGISTRATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 17;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_APPLICATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 139;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_CERTIFIED_MACHINE_COPY.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 145;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_TRUE_COPY.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 148;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_NON_RESIDENT_APPLICATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 151;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_FORIEGNER_APPLICATION.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 152;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_LICENSSE.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 153;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.MARRIAGE_PRE_COUNSELING.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 208;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.COE.getId()) {
			ids = new int[2];
			amounts = new double[2];
			//documentation
			ids[0] = 116;
			amounts[0] = 0.00;
			//DST
			ids[1] = 53;
			amounts[1] = 30.00;
		}else if(c.getTransType()==ClientTransactionType.TAX_DECLARATION.getId()) {
			ids = new int[4];
			amounts = new double[4];
			//Tax Declaration
			ids[0] = 143;
			amounts[0] = 100.00;
			//Other service income
			ids[1] = 57;
			amounts[1] = 6.00;
			//DST
			ids[2] = 53;
			amounts[2] = 30.00;
			//Secretary fee
			ids[3] = 20;
			amounts[3] = 50.00;
		}
		
			
		
		double amount = 0d;
		String sql = "";
		String[] params = new String[0];
		try {
			for(int i=0; i< ids.length; i++) {
				sql = " AND pyid="+ids[i];
				PaymentName py =  PaymentName.retrieve(sql, params).get(0);
				py.setAmount(amounts[i]);
				amount += amounts[i];
				namesDataSelected.add(py);
				getSelectedPaymentNameMap().put(py.getId(), py);
			}
			setTotalAmount(Currency.formatAmount(amount));
		}catch(Exception e) {e.printStackTrace();}
	}
	public void deleteQueue(Client client) {
			client.delete();
			client.setStatus(ClientStatus.DELETED.getId());
			clients.remove(client);
			Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	public void noFileterSearch() {
		orRpts = new ArrayList<ORListing>();
		String sql = "";
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			
			if(getSearchParam().contains("-")) {
				String[] val = getSearchParam().split("-");
				sql += " AND (o.ornumber>='"+ val[0] +"' AND o.ornumber<='"+ val[1] +"' )";
			}else {
				sql += " AND (o.ornumber like '%"+ getSearchParam() +"%' OR c.fullname like '%"+ getSearchParam() +"%' )";
			}
			
			if(getFormIdRpt()!=0) {
				sql += " AND o.aform=" + getFormIdRpt();
			}
			
			Login in = getUser();
			
			if(AccessLevel.DEVELOPER.getId()== in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
				
			}else {
				sql += " AND o.isid=" + issuedCollectorId;
			}
			
			sql += " ORDER BY o.ornumber,o.ordatetrans";
			
			Object[] objs = ORListing.getReport(sql);
			double totalAmount = (Double)objs[0];
			setTotalCollectionToday(Currency.formatAmount(totalAmount));
			List<ORListing> ors = (List<ORListing>)objs[1];
			orRpts = ors;
		
		}
	}
	
	public void loadAllRpt() {
		orRpts = new ArrayList<ORListing>();
		//String dateSelected = DateUtils.convertDate(getDateRpt(), "yyyy-MM-dd");
		String sql = "";//"and o.ordatetrans='"+ dateSelected +"'";
		
		
		if(getRangeDate()!=null && getRangeDate().size()==2) {
			System.out.println("range date 2");
			sql = " AND (o.ordatetrans>='"+ DateUtils.convertDate(getRangeDate().get(0), "yyyy-MM-dd") +"' AND o.ordatetrans<='"+ DateUtils.convertDate(getRangeDate().get(1), "yyyy-MM-dd") +"')";
		}else if(getRangeDate()!=null && getRangeDate().size()==1){
			sql = " AND o.ordatetrans='"+ DateUtils.convertDate(getRangeDate().get(0),"yyyy-MM-dd") +"'";
			System.out.println("range date 1");
		}else {
			System.out.println("no range date");
			sql = " AND o.ordatetrans='"+ DateUtils.getCurrentDateYYYYMMDD() +"'";
		}
		
		if(getSearchName()!=null && !getSearchParam().isEmpty()) {
			sql += " AND (o.ornumber like '%"+ getSearchParam() +"%' OR c.fullname like '%"+ getSearchParam() +"%' )";
		}
		
		if(getFormIdRpt()!=0) {
			sql += " AND o.aform=" + getFormIdRpt();
		}
		
		Login in = getUser();
		
		if(AccessLevel.DEVELOPER.getId()== in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
			
		}else {
			sql += " AND o.isid=" + issuedCollectorId;
		}
		
		sql += " ORDER BY o.ornumber,o.ordatetrans";
		
		Object[] objs = ORListing.getReport(sql);
		double totalAmount = (Double)objs[0];
		setTotalCollectionToday(Currency.formatAmount(totalAmount));
		List<ORListing> ors = (List<ORListing>)objs[1];
		orRpts = ors;
	}
	
	public void checkSeries() {
		if(!getCheckSeriesForm().isEmpty() && getCheckSeriesForm().contains("-")) {
			String[] sers = getCheckSeriesForm().split("-");
			long startSeries=Long.valueOf(sers[0]);
			long endSeries=Long.valueOf(sers[1]);;
			int size = sers[0].length();
			Map<String, String> orMap = new LinkedHashMap<String, String>();
			int count = 0;
			for(long series=startSeries; series<=endSeries; series++) {
				String ornumber = correctSeriesNumber(series,size);
				orMap.put(ornumber, ornumber);
				count++;
			}
			String sql = " AND ( ornumber>='"+ sers[0] +"' AND ornumber<='"+ sers[1] +"') ";
			sql += "ORDER BY ornumber";
			Map<String, ORListing> ors = ORListing.retrieveORSeries(sql);
			orsSeries = new ArrayList<ORListing>();
			for(String ornumber : orMap.keySet()) {
				if(ors.containsKey(ornumber)) {
					orsSeries.add(ors.get(ornumber));
				}else {
					orsSeries.add(ORListing.builder().orNumber(ornumber).statusName("Not Recorded").build());
				}
			}
		}
	}
	private String correctSeriesNumber(long ornumber, int formSeriesLen) {
		String tmpNumber = ornumber+"";
		int size = tmpNumber.length();
		String newOrNumber="";
		if(formSeriesLen==8) {//cedula
			switch(size) {
			case 1 : newOrNumber="0000000"+ornumber; break;
			case 2 : newOrNumber="000000"+ornumber; break;
			case 3 : newOrNumber="00000"+ornumber; break;
			case 4 : newOrNumber="0000"+ornumber; break;
			case 5 : newOrNumber="000"+ornumber; break;
			case 6 : newOrNumber="00"+ornumber; break;
			case 7 : newOrNumber="0"+ornumber; break;
			case 8 : newOrNumber=""+ornumber; break;
			}
		}else {//51,56,etc
			switch(size) {
			case 1 : newOrNumber="000000"+ornumber; break;
			case 2 : newOrNumber="00000"+ornumber; break;
			case 3 : newOrNumber="0000"+ornumber; break;
			case 4 : newOrNumber="000"+ornumber; break;
			case 5 : newOrNumber="00"+ornumber; break;
			case 6 : newOrNumber="0"+ornumber; break;
			case 7 : newOrNumber=""+ornumber; break;
			}
		}
		
		return newOrNumber;
	}
	
	public void viewDuplicate() {
		orsDuplicate = ORListing.checkDuplicateOR();
		PrimeFaces pf = PrimeFaces.current();
		if(orsDuplicate!=null && orsDuplicate.size()>0) {
			pf.executeScript("PF('dlgDuplicateSeries').show(1000)");
		}else {
			Application.addMessage(1, "No Data", "No Data to be show");		}
	}
	
	public void deleteDuplicate(ORListing or) {
		Login in = getUser();
		if(in.getAccessLevel().getLevel()==AccessLevel.DEVELOPER.getId()) {
			or.delete();
			getOrsDuplicate().remove(or);
			Application.addMessage(1, "Sucess", "Successfully deleted.");
		}else {
			Application.addMessage(2, "Warning", "You dont have a right to delete this transaction. Please contact admin for deletion");
		}
		
	}
	
	public List<String> suggestedProfession(String param){
		List<String> val = new ArrayList<>();
		for(String prof : proffesionsList) {
			if(prof.trim().toLowerCase().contains(param.trim().toLowerCase())) {
				val.add(prof.split("-")[1]);
			}
		}
		
		return val;
	}
	public void calcProfession() {
		System.out.println("Calculate now....");
		String val="1";
		for(String prof : proffesionsList) {
			if(prof.trim().toLowerCase().contains(professionBusinessNature.trim().toLowerCase())) {
				val=prof.split("-")[0];
			}
		}
		int type = Integer.valueOf(val);
		if(type==0) {//senior citizen
			setAmount3(0);
			calculateCedula();
		}else if(type<=4 || type==25) {//daily income person
			if(genderId==1) {setAmount3(40);}else {setAmount3(30);}
			calculateCedula();
		}else if(type==5 || type==6) {//businessman
			/*
			 * setMonthlyIncome(c.getMonthlySalary()); calculateGrossRptax();
			 */
		}else {
			//setMonthlySal(c.getMonthlySalary());
			//calculateSalariesGross();
		}
	}
}
