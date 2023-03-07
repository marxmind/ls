package com.italia.municipality.lakesebu.licensing.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.stream.FileImageOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;

import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.BFilter;
import com.italia.municipality.lakesebu.enm.DateFormat;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.BusinessEngaged;
import com.italia.municipality.lakesebu.licensing.controller.BusinessFilter;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.licensing.controller.BusinessRpt;
import com.italia.municipality.lakesebu.licensing.controller.ClearanceRpt;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.licensing.controller.DocumentPrinting;
import com.italia.municipality.lakesebu.licensing.controller.BusinessORTransaction;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @since 02/08/2019
 * @version 1.0
 *
 */

@Named
@ViewScoped
@Data
public class BusinessPermitBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1543678734345454L;

	private List<BusinessPermit> pmts = new ArrayList<BusinessPermit>();
	
	private Date issuedDate;
	private Date calendarFrom;
	private Date calendarTo;
	
	private Date calendarFromCon;
	private Date calendarToCon;
	private boolean checkBoxSeries; 
	
	private BusinessCustomer taxPayer;
	private String customerName;
	private String searchTaxpayer;
	private List<BusinessCustomer> taxpayers = new ArrayList<BusinessCustomer>();
	private String photoId="camera";
	private List<String> shots = new ArrayList<>();
	
	private String capturedImagePathName;
	private final static String IMAGE_PATH = ReadConfig.value(AppConf.LICENSING_IMG) ;
	private String searchName;
	
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private static final String REPORT_PATH = ReadConfig.value(AppConf.REPORT_FOLDER);
	
	private String BARANGAY = "Poblacion";
	private String MUNICIPALITY = "Lake Sebu";
	private String PROVINCE = "South Cotabato";
	private static final String BUSINESS_REPORT_PERMIT = DocumentFormatter.getTagName("v6_business-permit");
	private static final String BUSINESS_REPORT = "businesslist";
	private List<BusinessPermit> seriesData;
	
	private  String DOC_PATH = AppConf.PRIMARY_DRIVE.getValue() + 
			AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + 
			AppConf.SEPERATOR.getValue() + "upload" + AppConf.SEPERATOR.getValue();
	
	private BusinessPermit businessData;
	
	private String businessName;
	private String businessEngage;
	private String businessAddress;
	private String barangay;
	private String plateNo;
	private String validUntil;
	private String issuedOn;
	private String year;
	private String memoType;
	private String oic;
	private String mayor;
	private String controlNo;
	private String typeOf;
	
	private List<BusinessORTransaction> ors = new ArrayList<BusinessORTransaction>();
	private List<BusinessORTransaction> orsSelected = new ArrayList<BusinessORTransaction>();
	
	private String searchBusinessName;
	private List<Livelihood> business = new ArrayList<Livelihood>();
	private List<Livelihood> selectedBusiness = new ArrayList<Livelihood>();
	private List<Livelihood> ownerBusiness = new ArrayList<Livelihood>();
	
	private Map<Integer, BusinessEngaged> EngagedData = new HashMap<Integer, BusinessEngaged>();
	
	private double grossAmount;
	private String employeeDtls;
	
	private String memoTypeId;
	private List memos;
	
	private String typeId;
	private List types;
	
	private List businessStatus;
	private String businessStatusId;
	
	@PostConstruct
	public void init() {
		
		calendarFromCon = DateUtils.getDateToday();
		calendarToCon = DateUtils.getDateToday();
		System.out.println("init start...");
		setBusinessStatusId("All");
		loadLineOfBusiness();
		loadTypes();
		loadMemos();
		loadSearch();
		System.out.println("end init...");
	}
	
	public void loadTypes() {
		types = new ArrayList<>();
		businessStatus = new ArrayList<>();
		String[] typs = Words.getTagName("types").split(",");
		businessStatus.add(new SelectItem("All", "All"));
		for(String t : typs) {
			types.add(new SelectItem(t, t));
			businessStatus.add(new SelectItem(t, t));
		}
	}
	
	public void loadMemos() {
		memos = new ArrayList<>();
		String[] types = Words.getTagName("memotype").split(",");
		for(String t : types) {
				memos.add(new SelectItem(t, t));
		}
	}
	
	public void loadSearch() {
		pmts = new ArrayList<BusinessPermit>();
		
		String sql = " AND bz.isactivebusiness=1 AND (bz.datetrans>=? AND bz.datetrans<=?) ";
		String[] params = new String[2];
		params[0] = DateUtils.convertDate(getCalendarFrom(), DateFormat.YYYY_MM_DD());
		params[1] = DateUtils.convertDate(getCalendarTo(), DateFormat.YYYY_MM_DD());
		
		if(!"All".equalsIgnoreCase(getBusinessStatusId())) {
			sql += " AND  bz.typeof='"+ getBusinessStatusId() +"' ";
		}
		
		if(getSearchName()!=null && !getSearchName().isBlank()) {
			int size = getSearchName().length();
			
			if(size>=4) {
				sql +=" AND (";
				sql += " cuz.fullname like '%"+ getSearchName().replace("--", "") +"%'";
				sql += " OR bz.businessname like '%"+ getSearchName().replace("--", "") +"%'";
				sql +=" )";
				pmts = BusinessPermit.retrieve(sql, params);
			}
			
		}else {
			pmts = BusinessPermit.retrieve(sql, params);
		}
		
		//sort
		Map<String, BusinessPermit> rptsUnsort = new HashMap<String, BusinessPermit>();
		for(BusinessPermit p : getPmts()) {
			rptsUnsort.put(p.getControlNo(), p);
		}
		Map<String, BusinessPermit> rptsSort = new TreeMap<String, BusinessPermit>(rptsUnsort);
		pmts = new ArrayList<>();
		pmts.addAll(rptsSort.values());
		//end sort
		
		
		Collections.reverse(pmts);
	}
	
	public void clickItemOwner(BusinessCustomer cuz){
		
		clearFields();
		
		setTaxPayer(cuz);
		setCustomerName(cuz.getFirstname() + " " + cuz.getMiddlename().substring(0, 1) + ". " + cuz.getLastname());
		setPhotoId(cuz.getPhotoid());
		shots = new ArrayList<>();
		shots.add(cuz.getPhotoid());
		String year = DateUtils.getCurrentYear()+"";
		String sql = " AND orl.oractive=1 AND cuz.customerid=? AND (orl.ordate>=? AND orl.ordate<=?) ORDER BY orl.orid DESC";
		String[] params = new String[3];
		params[0] = cuz.getId()+"";
		params[1] = year + "-01-01";
		params[2] = year + "-12-31";
		ors = BusinessORTransaction.retrieve(sql, params);
		
		loadBusiness();
	}
	
	public void clickItem(BusinessPermit permit) {
		BusinessCustomer cuz = permit.getCustomer();
		setTaxPayer(cuz);
		setCustomerName(cuz.getFullname());
		setPhotoId(cuz.getPhotoid());
		shots = new ArrayList<>();
		shots.add(cuz.getPhotoid());
		setBusinessData(permit);
		String sql = "";
		String[] params = new String[0];
		orsSelected = new ArrayList<BusinessORTransaction>();
		String year = permit.getDateTrans().split("-")[0];
		sql = " AND orl.oractive=1 AND cuz.customerid=? AND (orl.ordate>=? AND orl.ordate<=? ) ORDER BY orl.orid DESC";
		params = new String[3];
		params[0] = cuz.getId()+"";
		params[1] = year + "-01-01";
		params[2] = year + "-12-31";	
		ors = BusinessORTransaction.retrieve(sql, params);
		
		if(permit.getOrs().contains("/")) {
			String[] ors = permit.getOrs().split("/");
			
			for(String or : ors) {
				
				sql = " AND orl.oractive=1 AND cuz.customerid=? AND orl.orno=?   ORDER BY orl.orid DESC";
				params = new String[2];
				params[0] = cuz.getId()+"";
				params[1] = or;	
				BusinessORTransaction o = BusinessORTransaction.retrieve(sql, params).get(0);
				orsSelected.add(o);
			}
		}else {
			sql = " AND orl.oractive=1 AND cuz.customerid=? AND orl.orno=? ORDER BY orl.orid DESC";
			params = new String[2];
			params[0] = cuz.getId()+"";
			params[1] = permit.getOrs();
			BusinessORTransaction o = BusinessORTransaction.retrieve(sql, params).get(0);
			orsSelected.add(o);
		}
		loadBusiness();
		setBusinessName(permit.getBusinessName());
		setBusinessEngage(permit.getBusinessEngage());
		setBusinessAddress(permit.getBusinessAddress());
		setBarangay(permit.getBarangay());
		setPlateNo(permit.getPlateNo());
		setValidUntil(permit.getValidUntil());
		setIssuedOn(permit.getIssuedOn());
		setYear(permit.getYear());
		setMemoType(permit.getMemoType());
		setMemoTypeId(permit.getMemoType());
		setOic(permit.getOic());
		setMayor(permit.getMayor());
		setControlNo(permit.getControlNo());
		setTypeOf(permit.getType());
		setTypeId(permit.getType());
		setEmployeeDtls(permit.getEmpdtls());
		setGrossAmount(permit.getGrossAmount());
		setIssuedDate(DateUtils.convertDateString(permit.getDateTrans(), "yyyy-MM-dd"));
		//loadBusiness();
	}
	
	public void loadLineOfBusiness() {
		EngagedData = Collections.synchronizedMap(new HashMap<Integer, BusinessEngaged>());
		for(BusinessEngaged line : BusinessEngaged.readBusinessEngagedXML()){
			getEngagedData().put(line.getId(), line);
		}
	}
	
	public void deleteRow(BusinessPermit permit) {
		permit.delete();
		//init();
		loadSearch();
		clearFields();
	}
	
	public void clickItemBusiness(Livelihood bz) {
		
		String address = "";
		boolean isPurok=false;
		try{
			if(bz.getPurokName()!=null && !bz.getPurokName().isBlank()) {
				address = bz.getPurokName();
				isPurok=true;
			}
		}catch(Exception e) {}
		try{
			if(isPurok) {
				address +=", "+ bz.getBarangay().getName();
			}else {
				address = bz.getBarangay().getName();
			}
		}catch(Exception e) {}
		try{
			address +=", "+ bz.getMunicipality().getName(); 
		}catch(Exception e) {}
		
		try{
			address +=", "+ bz.getProvince().getName();
		}catch(Exception e) {}
		/*try{
			address +=", "+ bz.getProvince().getName(); 
		}catch(Exception e) {}*/
		
		setBusinessName(bz.getBusinessName());
		setBusinessEngage(getEngagedData().get(bz.getTypeLine()).getName());
		setBusinessAddress(address);
		setBarangay(bz.getBarangay().getName());
		setPlateNo(bz.getAreaMeter());
		try{setGrossAmount(Double.valueOf(bz.getSupportingDetails()));}catch(Exception e) {}
	}
	
	public void selectedPhoto(String photoId){
		setPhotoId(photoId);
	}
	
	public void deleteTmpImages(){
		
		if(getShots()!=null && getShots().size()>0){
			deletingImages();
		}
	}
	
	private String getRandomImageName() {
        int i = (int) (Math.random() * 10000000);
         
        return String.valueOf(i);
    }
	
	public void oncapture(CaptureEvent captureEvent) {
        photoId = getRandomImageName() + DateUtils.getCurrentDateMMDDYYYYTIMEPlain();
    	//filename ="cam";
        shots.add(photoId);
        
        System.out.println("Set picture name " + photoId);
        byte[] data = captureEvent.getData();
 
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        String driveImage =  IMAGE_PATH + photoId + ".jpg";
        String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;
        
        FileImageOutputStream imageOutput;
        try {
            imageOutput = new FileImageOutputStream(new File(driveImage));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();    
            
            
            String pathToSave = externalContext.getRealPath("") + contextImageLoc;
            File file = new File(driveImage);
            try{
    			Files.copy(file.toPath(), (new File(pathToSave + file.getName())).toPath(),
    			        StandardCopyOption.REPLACE_EXISTING);
    			}catch(IOException e){}
            capturedImagePathName = contextImageLoc + photoId + ".jpg";
            System.out.println("capture path " + capturedImagePathName.replace("\\", "/"));
            setCapturedImagePathName(capturedImagePathName.replace("\\", "/"));
        }
        catch(IOException e) {
            throw new FacesException("Error in writing captured image.", e);
        }
    }
	
	private void deletingImages(){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        String deleteImg = externalContext.getRealPath("") + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;// + photoId + ".jpg";
        
        try{
        System.out.println("Check before deleting.... " + getPhotoId());
       getShots().remove(getPhotoId());	
       for(String name : shots){ 	
        	if(!getPhotoId().equalsIgnoreCase(name)){
		        File img = new File(IMAGE_PATH +  name + ".jpg");
		        img.delete();
		       
		        img = new File(deleteImg + name + ".jpg");
		        img.delete();
		        System.out.println("photo deleting "+name);
        	}else{
        		System.out.println("Existing "+name);
        	}
       	}
        }catch(Exception e){}
	}
	
	public void loadTaxpayer(){
		
		taxpayers = new ArrayList<BusinessCustomer>();
		
		//String sql = " AND cus.cusisactive=1 ";
		
		//these lines of code have been revised.
		/*if(getSearchTaxpayer()!=null && !getSearchTaxpayer().isEmpty()){
			int size = getSearchTaxpayer().length();
			if(size>=5){
				sql += " AND (cus.fullname like '%" + getSearchTaxpayer().replace("--", "") +"%' OR cus.cuscardno like '%"+ getSearchTaxpayer().replace("--", "") +"%')";
				taxpayers =  Customer.retrieve(sql, new String[0]);
				
				if(taxpayers!=null && taxpayers.size()==1) {
					clickItemOwner(taxpayers.get(0));
					PrimeFaces pf = PrimeFaces.current();
					pf.executeScript("PF('multiDialogOwner').hide();");
					setSearchTaxpayer("");
				}
				
			}
		}else{
			sql += " order by cus.customerid DESC limit 10;";
			taxpayers =  Customer.retrieve(sql, new String[0]);
		}
		*/
		String sql = " AND live.isactivelive=1 AND cuz.cusisactive=1 ";
		if(getSearchTaxpayer()!=null && !getSearchTaxpayer().isEmpty()){
			int size = getSearchTaxpayer().length();
			if(size>=5){
				sql += " AND (cuz.fullname like '%" + getSearchTaxpayer().replace("--", "") +"%' OR cuz.cuscardno like '%"+ getSearchTaxpayer().replace("--", "") +"%')";
				List<Livelihood> lvs = Livelihood.retrieve(sql, new String[0]);
				if(lvs!=null && lvs.size()==1) {
					clickItemOwner(lvs.get(0).getTaxPayer());
					PrimeFaces pf = PrimeFaces.current();
					pf.executeScript("PF('multiDialogOwner').hide();");
					setSearchTaxpayer("");
				}else {
					for(Livelihood lv : lvs) {
						taxpayers.add(lv.getTaxPayer());
					}
				}
			}
		}else {
			sql += " ORDER BY live.livelihoodid DESC LIMIT 10";
			for(Livelihood lv : Livelihood.retrieve(sql, new String[0])){
				taxpayers.add(lv.getTaxPayer());
			}
		}
		
	}
	
	public void clearNew() {
		clearFields();
	}
	
	public void clearFields(){
		setTypeId(null);
		setMemoTypeId(null);
		setIssuedDate(null);
		setPhotoId("camera");
		setTaxPayer(null);
		setCustomerName(null);
		shots = new ArrayList<>();
		
		setCalendarFrom(null);
		setCalendarTo(null);
		
		setBusinessName(null);
		setBusinessEngage(null);
		setBusinessAddress(null);
		setBarangay(null);
		setPlateNo(null);
		setValidUntil(null);
		setIssuedOn(null);
		setYear(null);
		setMemoType(null);
		setOic(null);
		setMayor(null);
		setControlNo(null);
		setTypeOf(null);
		setEmployeeDtls(null);
		setGrossAmount(0.00);
		
		ors = Collections.synchronizedList(new ArrayList<BusinessORTransaction>());
		orsSelected = Collections.synchronizedList(new ArrayList<BusinessORTransaction>());
		business = Collections.synchronizedList(new ArrayList<Livelihood>());
		selectedBusiness = Collections.synchronizedList(new ArrayList<Livelihood>());
		
	}
	
	public void saveData() {
		
		BusinessPermit permit = new BusinessPermit();
		boolean isNew = false;
		if(getBusinessData()!=null) {
			permit = getBusinessData();
		}else {
			isNew = true;
			permit.setIsActive(1);
		}
		
		boolean isOk = true;
		
		if(getTaxPayer()==null) {
			Application.addMessage(3, "Error", "Please Select Owner Name");
			isOk = false;
		}
		
		if(getOrsSelected()==null || getOrsSelected().size()==0) {
			Application.addMessage(3, "Error", "Please select OR's");
			isOk = false;
		}
		
		if(BusinessPermit.isExistControlNumber(getControlNo(), getTaxPayer().getId())) {
			Application.addMessage(3, "Error", "Controll Number already in use");
			isOk = false;
		}
		
		if(isNew && BusinessPermit.isCertificateAlreadyCreated(getControlNo(), getPlateNo() ,getTaxPayer().getId())) {
			Application.addMessage(3, "Error", "This certification is already created. Check your previous transaction");
			isOk = false;
		}
		
		if(isOk) {
			
		try {
			String control = getControlNo().split("-")[2];	
			 permit.setIsSecond(1);
		}catch(Exception e) {}
			
		permit.setCustomer(getTaxPayer());
		String det = DateUtils.convertDate(getIssuedDate(), "yyyy-MM-dd");
		String year = det.split("-")[0];
		permit.setYear(year);	
		permit.setDateTrans(det);
		permit.setControlNo(getControlNo().trim());
		permit.setType(getTypeId());
		permit.setBusinessName(getBusinessName().trim());
		permit.setBusinessEngage(getBusinessEngage());
		permit.setBusinessAddress(getBusinessAddress());
		permit.setBarangay(getBarangay());
		permit.setPlateNo(getPlateNo().trim());
		permit.setValidUntil(getValidUntil());
		permit.setIssuedOn(getIssuedOn());
		permit.setMemoType(getMemoTypeId());
		permit.setOic(getOic());
		permit.setMayor(getMayor());
		permit.setEmpdtls(getEmployeeDtls());
		permit.setGrossAmount(getGrossAmount());
		
		String ors = "";
		int i=1;
		for(BusinessORTransaction o : getOrsSelected()) {
			
			if(i==1) {
				ors = o.getOrNumber();
			}else if(i>1) {
				ors +="/"+ o.getOrNumber();
			}
			
			i++;
		}
		permit.setOrs(ors);
		
		permit.save();
		//init();
		
		//set specific permit
		setSearchName(permit.getCustomer().getFullname());
		setCalendarFrom(getIssuedDate());
		setCalendarTo(getIssuedDate());
		
		loadSearch();
		clearFields();
		Application.addMessage(1, "Success", "Successfully saved.");
		}
	}
	
	public List<String> autoTypes(String query){
			
			String[] types = Words.getTagName("types").split(",");
			List<String> rs = Collections.synchronizedList(new ArrayList<String>());
			for(String t : types) {
					rs.add(t);
			}
			return rs;
		
	}
	
	public List<String> autoMemos(String query){
					
			String[] types = Words.getTagName("memotype").split(",");
			List<String> rs = Collections.synchronizedList(new ArrayList<String>());
			for(String t : types) {
					rs.add(t);
			}
			return rs;
	}
	
	public List<String> autoBarangay(String query){
		
		int size = query.length();
		if(size>=2){
			return Barangay.retrieve(query, "bgname"," limit 10");
		}else{
			return Collections.synchronizedList(new ArrayList<String>());
		}
	}
	
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	
	public Date getIssuedDate() {
		if(issuedDate==null){
			issuedDate =  DateUtils.getDateToday();//DateUtils.getCurrentDateYYYYMMDD();
		}
		return issuedDate;
	}
	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}
	
	public List<BusinessPermit> getPmts() {
		return pmts;
	}

	public void setPmts(List<BusinessPermit> pmts) {
		this.pmts = pmts;
	}
	
	public Date getCalendarFrom() {
		if(calendarFrom==null){
			calendarFrom = DateUtils.getDateToday();
		}
		return calendarFrom;
	}

	public void setCalendarFrom(Date calendarFrom) {
		this.calendarFrom = calendarFrom;
	}

	public Date getCalendarTo() {
		if(calendarTo==null){
			calendarTo = DateUtils.getDateToday();
		}
		return calendarTo;
	}

	public void setCalendarTo(Date calendarTo) {
		this.calendarTo = calendarTo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public BusinessCustomer getTaxPayer() {
		return taxPayer;
	}

	public void setTaxPayer(BusinessCustomer taxPayer) {
		this.taxPayer = taxPayer;
	}

	public String getSearchTaxpayer() {
		return searchTaxpayer;
	}

	public void setSearchTaxpayer(String searchTaxpayer) {
		this.searchTaxpayer = searchTaxpayer;
	}

	public List<BusinessCustomer> getTaxpayers() {
		return taxpayers;
	}

	public void setTaxpayers(List<BusinessCustomer> taxpayers) {
		this.taxpayers = taxpayers;
	}

	public List<String> getShots() {
		return shots;
	}

	public void setShots(List<String> shots) {
		this.shots = shots;
	}
	
	public String getCapturedImagePathName() {
		return capturedImagePathName;
	}

	public void setCapturedImagePathName(String capturedImagePathName) {
		this.capturedImagePathName = capturedImagePathName;
	}

	public String getSearchName() {
		return searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}
	
	public void uploadData(FileUploadEvent event){
		
		 try {
			 InputStream stream = event.getFile().getInputStream();
			 String file = event.getFile().getFileName();
			 
			 if(writeDocToFile(event)){
				 Application.addMessage(1, "Success", "Data has been successfully uploaded");
				 init();
			 }else{
				 Application.addMessage(3, "Error", "Error uploading the data " + file);
			 }
			 
	     } catch (Exception e) {
	     
	     }
		
	}
	
	private boolean writeDocToFile(FileUploadEvent event){
		try{
		InputStream stream = event.getFile().getInputStream();
		String fileExt = event.getFile().getFileName().split("\\.")[1];
		String filename = "permitData-" + DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "."+fileExt.toLowerCase();
		
		System.out.println("writing... writeDocToFile : " + filename);
		File fileDoc = new File(DOC_PATH +  filename);
		Path file = fileDoc.toPath();
		Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
		//return loadToDatabase(fileDoc);
		readingExcelFile(filename);
		return true;
		}catch(IOException e){return false;}
		
	}
	
	public static void main(String[] args) {
		BusinessPermitBean bean = new BusinessPermitBean();
		
		File file = new File("C:\\bris\\BLS-Data.xls");
		//bean.loadFile(file, 0);
		//bean.loadFile(file, 1);
		
	}
	
	private void readingExcelFile(String fileName) {
		File file = new File(DOC_PATH + fileName);
		loadFile(file);
		//loadFile(file,1);
	}
	
	public void loadFile(File file) {
		System.out.println(file.getName());
		String ext = file.getName().split("\\.")[1];
		UserDtls user = getUser();
		BusinessPermit business = new BusinessPermit();
		if("xls".equalsIgnoreCase(ext)) {
			//if(sheetNo==0) {
				business = readXLSFile(file,0);
				business.setDateTrans(DateUtils.getCurrentDateYYYYMMDD());
				business.setCustomer(getTaxPayer());
				business.setIsActive(1);
				business.setYear(DateUtils.getCurrentYear()+"");
				
			//}else {
				List<BusinessORTransaction> names = readXLSFileOR(file,1);
				int size = names.size();
				String ors = "";
				for(int i=0; i<size; i++) {
					names.get(i).setPurpose("MUNICIPAL PERMIT");
					names.get(i).setAddress("Lake Sebu, South Cotabato");
					names.get(i).setCustomer(getTaxPayer());
					names.get(i).setUserDtls(user);
					names.get(i).setIsActive(1);
					names.get(i).setStatus(1);
					names.get(i).save();
					if(i>0) {
						ors += "/" + names.get(i).getOrNumber();
					}else {
						ors = names.get(i).getOrNumber();
					}
				}
				//save business permit details
				business.setOrs(ors);
				business = BusinessPermit.save(business);
				setBusinessData(business);
				clickItem(business);
			//}
		}
	}
	
	public UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
private static BusinessPermit readXLSFile(File file,int sheetNo) {
		
		try {
			FileInputStream fin = new FileInputStream(file);
			POIFSFileSystem fs = new POIFSFileSystem(fin); 
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(sheetNo);
			HSSFRow row;
			HSSFCell cell;
				
				BusinessPermit att = new BusinessPermit();
				Iterator rows = sheet.rowIterator();
				int startRow=1;
			    while (rows.hasNext()){
		            row=(HSSFRow) rows.next();
		            Iterator cells = row.cellIterator();
		            int countRow = 1;
		            String value="";
		            if(startRow<=13) {
		            	
			            while (cells.hasNext()){
			            		
			            		
					                cell=(HSSFCell) cells.next();
					                
					                
					                if(cell.getCellTypeEnum()==CellType.STRING) {
					                	value = cell.getStringCellValue();
					                }else if(cell.getCellTypeEnum()==CellType.NUMERIC) {
					                	value = cell.getNumericCellValue()+"";
					                }else {
					                	//U Can Handel Boolean, Formula, Errors
					                	//System.out.println("\t");
					                }
					                //System.out.println("value " + value);
				            		
				               countRow++;
			            }
			            
		            }
		            
		            switch(startRow) {
		            case 1 : att.setType(value);break;
		            case 2 : att.setControlNo(value);
		            case 3 : att.setBusinessName(value);break;
		            case 4 : att.setBusinessEngage(value);break;
		            case 5 : att.setBusinessAddress(value);break;
		            case 6 : att.setBarangay(value);break;
		            case 7 : try{att.setPlateNo(value.split(".0")[0]);}catch(NullPointerException e) {}break;
		            case 8 : att.setValidUntil(value);break;
		            case 9 : att.setIssuedOn(value);break;
		            case 10 : try{att.setYear(value.split(".0")[0]);}catch(NullPointerException e) {att.setYear(value);}break;
		            case 11 : att.setMemoType(value);break;
		            case 12 : att.setOic(value);break;
		            case 13 : att.setMayor(value);break;
		            }
		            
		            startRow++;
			    }   
	    
			   
			    fin.close();
				
			    //System.out.println(att.getBusinessName() + " / " + att.getBusinessEngage() + " / " + att.getBusinessAddress() + " / " + att.getBarangay() + " / " + att.getPlateNo());
			    
			    return att;
			    } catch(Exception e) {}	
		return null;
			
	}
	
private static List<BusinessORTransaction> readXLSFileOR(File file,int sheetNo) {
	
	try {
		FileInputStream fin = new FileInputStream(file);
		POIFSFileSystem fs = new POIFSFileSystem(fin); 
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(sheetNo);
		HSSFRow row;
		HSSFCell cell;
			
			List<BusinessORTransaction> atts = Collections.synchronizedList(new ArrayList<BusinessORTransaction>());
			Iterator rows = sheet.rowIterator();
			int startRow=1;
		    while (rows.hasNext()){
	            row=(HSSFRow) rows.next();
	            Iterator cells = row.cellIterator();
	            int countRow = 1;
	            BusinessORTransaction att = new BusinessORTransaction();
	            if(startRow>1) {
	            	
		            while (cells.hasNext()){
		            		
		            		
				                cell=(HSSFCell) cells.next();
				                String value="";
				                
				                if(cell.getCellTypeEnum()==CellType.STRING) {
				                	value = cell.getStringCellValue();
				                }else if(cell.getCellTypeEnum()==CellType.NUMERIC) {
				                	value = cell.getNumericCellValue()+"";
				                }else {
				                	//U Can Handel Boolean, Formula, Errors
				                	//System.out.println("\t");
				                }
				                //System.out.println("value " + value);
			            		
				                switch(countRow) {
				                case 1: try{att.setOrNumber(value.split(".0")[0]);}catch(NullPointerException e) {}break;
				                case 2: try{att.setDateTrans(value);}catch(NullPointerException e) {}break;
				                case 3: try{att.setAmount(Double.valueOf(value));}catch(NumberFormatException e) {att.setAmount(0.00);}break;
				                }
				                
			               countRow++;
		            }
		            if(att.getAmount()!=0) {
		            	atts.add(att);
		            }
	            }
	            startRow++;
		    }
		    
    	   for(BusinessORTransaction or : atts) {
			   //if(or.getAmount()!=0) {
				   System.out.println("OR" + or.getOrNumber() + " Date: " + or.getDateTrans() + " amount " + or.getAmount());
			   //}
			   
		   }
		    
		    fin.close();
			
		    return atts;
		    } catch(Exception e) {
		    	e.printStackTrace();
		    }	
	return null;
		
}



public void printPermit(BusinessPermit permit) {
		
		//System.out.println("Business: " + permit.getBusinessName());
	
		String REPORT_NAME = BUSINESS_REPORT_PERMIT;
		
		HashMap param = new HashMap();
		String path = REPORT_PATH;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, path);
		
		List<ClearanceRpt> reports = new ArrayList<ClearanceRpt>();
		ClearanceRpt rpt = new ClearanceRpt();
		reports.add(rpt);
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
		
		param.put("PARAM_PROVINCE", Words.getTagName("province-line").replace("<province>", PROVINCE));
		param.put("PARAM_MUNICIPALITY", Words.getTagName("municipality-line").replace("<municipality>", MUNICIPALITY));
		
		param.put("PARAM_YEAR", permit.getYear());
		param.put("PARAM_TYPE", permit.getType());
		param.put("PARAM_BARANGAY", permit.getBarangay().toUpperCase());
		
		
		
		String name = permit.getCustomer().getFirstname() + " " + permit.getCustomer().getLastname();
		try {
			String middleName = permit.getCustomer().getMiddlename().trim();
			if(middleName.contains(".")) {
				name = permit.getCustomer().getFirstname() + " " + permit.getCustomer().getLastname();//permit.getCustomer().getLastname().replace(".", "");
			}else {
				name = permit.getCustomer().getFirstname() + " " + middleName.substring(0, 1) + ". " + permit.getCustomer().getLastname();
			}
			
		}catch(Exception e) {}
		
		param.put("PARAM_REQUESTOR_NAME", name.toUpperCase());
		param.put("PARAM_BUSINESS_NAME", permit.getBusinessName().toUpperCase());
		param.put("PARAM_BUSINESS_ADDRESS", permit.getBusinessAddress().toUpperCase());
		param.put("PARAM_BUSINESS_MUNICIPALITY_ADDRESS", "Lake Sebu, South Cotabato to operate the following Business");
		
		
		param.put("PARAM_CONTROLNO", "Control No: "+permit.getControlNo());
		param.put("PARAM_PLATENO", permit.getPlateNo());
		param.put("PARAM_MEMO", permit.getMemoType());
		param.put("PARAM_VALIDTO", permit.getValidUntil());
		param.put("PARAM_ISSUEDON", permit.getIssuedOn());
		
		String sql = " AND orl.oractive=1 AND orl.orno=? AND cuz.customerid=? ";
		String[] params = new String[0];
		
		BusinessORTransaction ors = null;
		
		String[] sp = permit.getOrs().split("/");
		Map<String, BusinessORTransaction> unSort = new HashMap<String, BusinessORTransaction>();
		for(int i=0; i<sp.length; i++) {
			sql = " AND orl.oractive=1 AND orl.orno=? AND cuz.customerid=? ";
			params = new String[2];
			params[0] = sp[i];
			params[1] = permit.getCustomer().getId()+"";
			try{ors = BusinessORTransaction.retrieve(sql, params).get(0);}catch(Exception e) {}
			
			if(ors!=null) {
				//unSort.put(ors.getAmount()+"-"+ors.getPurpose(), ors);
				unSort.put(""+i, ors);
				System.out.println("Check add to map>> "+ors.getPurpose() + " php " + ors.getAmount());
			}
		}
		//Map<Double, ORTransaction> sortedOR = new TreeMap<Double, ORTransaction>(unSort);
		Map<String, BusinessORTransaction> sortedOR = new TreeMap<String, BusinessORTransaction>(unSort);
		//int i=sortedOR.size();
		int i = 1;
		int count = sortedOR.size();
		
		BusinessORTransaction fireData = new BusinessORTransaction();
		List<String> lData = new ArrayList<String>();
		boolean hasFire=false;
		String[] filters = BusinessFilter.filter(BFilter.EXCEPTION);
		for(BusinessORTransaction or : sortedOR.values()) {
			String fire = or.getPurpose().toLowerCase().trim();
			System.out.println("Start Check " + or.getPurpose() + " php " + or.getAmount());
			
			if(!BusinessFilter.checkFilter(fire, filters)) {
			//if(!"fire".equalsIgnoreCase(fire.trim())) {//do not include fire
				lData.add(or.getPurpose());
				System.out.println("Not fire " + or.getPurpose() + " php " + or.getAmount());
				if(i==1) {
					param.put("PARAM_1", or.getOrNumber());
					param.put("PARAM_2", or.getDateTrans());
					param.put("PARAM_3", Currency.formatAmount(or.getAmount()));
				}
				
				if(i==2) {
					param.put("PARAM_4", or.getOrNumber());
					param.put("PARAM_5", or.getDateTrans());
					param.put("PARAM_6", Currency.formatAmount(or.getAmount()));
				}
				if(i==3) {
					param.put("PARAM_7", or.getOrNumber());
					param.put("PARAM_8", or.getDateTrans());
					param.put("PARAM_9", Currency.formatAmount(or.getAmount()));
				}
				if(i==4) {
					param.put("PARAM_10", or.getOrNumber());
					param.put("PARAM_11", or.getDateTrans());
					param.put("PARAM_12", Currency.formatAmount(or.getAmount()));
				}
				if(i==5) {
					param.put("PARAM_13", or.getOrNumber());
					param.put("PARAM_14", or.getDateTrans());
					param.put("PARAM_15", Currency.formatAmount(or.getAmount()));
				}
				if(i==6) {
					param.put("PARAM_16", or.getOrNumber());
					param.put("PARAM_17", or.getDateTrans());
					param.put("PARAM_18", Currency.formatAmount(or.getAmount()));
				}
				if(i==7) {
					param.put("PARAM_19", or.getOrNumber());
					param.put("PARAM_20", or.getDateTrans());
					param.put("PARAM_21", Currency.formatAmount(or.getAmount()));
				}
				if(i==8) {
					param.put("PARAM_22", or.getOrNumber());
					param.put("PARAM_23", or.getDateTrans());
					param.put("PARAM_24", Currency.formatAmount(or.getAmount()));
				}
				if(i==9) {
					param.put("PARAM_25", or.getOrNumber());
					param.put("PARAM_26", or.getDateTrans());
					param.put("PARAM_27", Currency.formatAmount(or.getAmount()));
				}
				if(i==10) {
					param.put("PARAM_28", or.getOrNumber());
					param.put("PARAM_29", or.getDateTrans());
					param.put("PARAM_30", Currency.formatAmount(or.getAmount()));
				}
				if(i==11) {
					param.put("PARAM_31", or.getOrNumber());
					param.put("PARAM_32", or.getDateTrans());
					param.put("PARAM_33", Currency.formatAmount(or.getAmount()));
				}
				
				//i--;
				i++;
				
			}else {
				fireData = or;
				hasFire=true;
			}
			
			
			
		}
		
		//THIS IS FOR FIRE
		if(hasFire) {
			if(count==1) {
				param.put("PARAM_1", fireData.getOrNumber());
				param.put("PARAM_2", fireData.getDateTrans());
				param.put("PARAM_3", Currency.formatAmount(fireData.getAmount()));
			}
			
			if(count==2) {
				param.put("PARAM_4", fireData.getOrNumber());
				param.put("PARAM_5", fireData.getDateTrans());
				param.put("PARAM_6", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==3) {
				param.put("PARAM_7", fireData.getOrNumber());
				param.put("PARAM_8", fireData.getDateTrans());
				param.put("PARAM_9", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==4) {
				param.put("PARAM_10", fireData.getOrNumber());
				param.put("PARAM_11", fireData.getDateTrans());
				param.put("PARAM_12", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==5) {
				param.put("PARAM_13", fireData.getOrNumber());
				param.put("PARAM_14", fireData.getDateTrans());
				param.put("PARAM_15", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==6) {
				param.put("PARAM_16", fireData.getOrNumber());
				param.put("PARAM_17", fireData.getDateTrans());
				param.put("PARAM_18", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==7) {
				param.put("PARAM_19", fireData.getOrNumber());
				param.put("PARAM_20", fireData.getDateTrans());
				param.put("PARAM_21", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==8) {
				param.put("PARAM_22", fireData.getOrNumber());
				param.put("PARAM_23", fireData.getDateTrans());
				param.put("PARAM_24", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==9) {
				param.put("PARAM_25", fireData.getOrNumber());
				param.put("PARAM_26", fireData.getDateTrans());
				param.put("PARAM_27", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==10) {
				param.put("PARAM_28", fireData.getOrNumber());
				param.put("PARAM_29", fireData.getDateTrans());
				param.put("PARAM_30", Currency.formatAmount(fireData.getAmount()));
			}
			if(count==11) {
				param.put("PARAM_31", fireData.getOrNumber());
				param.put("PARAM_32", fireData.getDateTrans());
				param.put("PARAM_33", Currency.formatAmount(fireData.getAmount()));
			}
		}
		
		filters = BusinessFilter.filter(BFilter.ENGAGEMENT);
		//Collections.reverse(lData);
		String eng = "";
		int size = lData.size();
		int cnt = 1;
		for(String d : lData) {
			if(!BusinessFilter.checkFilter(d, filters)) {
			//if(!"brp".equalsIgnoreCase(d.trim())) {
				if(cnt==1) {
					eng = d;
					//System.out.println("1 " + eng);
				}else {
				
					if(cnt==size) {
						eng += " and " + d;
						//System.out.println("cnt == size " + size + "=" + eng);
					}else {
						eng += ", " + d;
						//System.out.println("cnt != size " + size + "=" + eng);
					}
					
				}
				cnt++;
			}
		}
		int countlent = eng!=null? eng.length() : 0;
		//do not change location // this code is after OR
		
		//System.out.println("engagement: " + eng);
		
		if(eng!=null && !eng.isEmpty()) {
			//eng = eng.trim();
			if(eng.contains(", and") || eng.contains(" and ")) {
				eng = eng.replace(",  and", "");
			//}
				//System.out.println("pasok ..... " + eng);
				
				String[] xx = eng.split(" and");
				try {
					if(xx[1].trim().isEmpty()) {
						eng = eng.replace(" and ", "");
					}
				}catch(Exception e) {}
			}	
			
			if(eng.contains(",")) {
				//System.out.println("has comma " + eng);
				
				String[] splitComma = eng.split(",");
				try {
					if(!splitComma[0].isEmpty()) {
						System.out.println(" is not empty 0:"+splitComma[0]);
					}
					if(!splitComma[1].trim().isEmpty()) {
						System.out.println("is not empty 1"+splitComma[1]);
					}else {
						eng = splitComma[0];
					}
				}catch(Exception e) {}
				
			}
			
		}
		
		if(countlent<=45) {
			param.put("PARAM_BUSINESS_TYPE", eng.toUpperCase());
		}else {
			param.put("PARAM_BUSINESS_TYPE_2", eng.toUpperCase());
		}
		param.put("PARAM_OIC", permit.getOic());
		param.put("PARAM_MAYOR", permit.getMayor());
		
		
		
		//certificate
		String certificate = path + "businesspermit.png";
		try{File file1 = new File(certificate);
		FileInputStream off1 = new FileInputStream(file1);
		param.put("PARAM_CERTIFICATE", off1);
		}catch(Exception e){}
		
					
			if(permit.getCustomer().getPhotoid()!=null && !permit.getCustomer().getPhotoid().isEmpty()){
				String picture = DocumentPrinting.copyPhoto(permit.getCustomer().getPhotoid()).replace("\\", "/");
				System.out.println("Images: " + picture);
				//InputStream img = this.getClass().getResourceAsStream("/"+clr.getPhotoId()+".jpg");
				File file = new File(picture);
				if(file.exists()){
					try{
					FileInputStream st = new FileInputStream(file);
					param.put("PARAM_PICTURE", st);
					}catch(Exception e){}
				}else{
					picture = DocumentPrinting.copyPhoto(permit.getCustomer().getPhotoid()).replace("\\", "/");
					file = new File(picture);
					try{
						FileInputStream st = new FileInputStream(file);
						param.put("PARAM_PICTURE", st);
					}catch(Exception e){}
				}
			}
		
		
		
		
		String documentNote="";
		documentNote += "Municipal Document\n";
		documentNote += "Series of " + permit.getYear()+"\n";
		try{param.put("PARAM_DOCUMENT_NOTE", documentNote);}catch(NullPointerException e) {}
		try{param.put("PARAM_TAGLINE", Words.getTagName("tagline"));}catch(NullPointerException e) {}
		//background
		String backlogo = path + "documentbg-gen.png";
		try{File file = new File(backlogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_BACKGROUND", off);
		}catch(Exception e){}
		
		
		//logo
		String officialLogo = path + "logo.png";
		try{File file = new File(officialLogo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_LOGO", off);
		}catch(Exception e){}
		
		//officialseallakesebu
		String lakesebuofficialseal = path + "municipalseal.png";
		try{File file1 = new File(lakesebuofficialseal);
		FileInputStream off2 = new FileInputStream(file1);
		param.put("PARAM_LOGO_LAKESEBU", off2);
		}catch(Exception e){}
		
		//logo
		String logo = path + "logotrans.png";
		try{File file = new File(logo);
		FileInputStream off = new FileInputStream(file);
		param.put("PARAM_SEALTRANSPARENT", off);
		}catch(Exception e){}
		
		
		
		try{
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ REPORT_NAME +".pdf");
	  		}catch(Exception e){e.printStackTrace();}
		
				try{
					System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + REPORT_NAME);
		  		 File file = new File(path, REPORT_NAME + ".pdf");
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
			            response.setHeader("Content-Disposition", "inline; filename=\"" + REPORT_NAME + ".pdf" + "\"");
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
	
	public void checkControlSeries() {
		seriesData = new ArrayList<BusinessPermit>();
		String dateFrom = DateUtils.convertDate(getCalendarFromCon(), DateFormat.YYYY_MM_DD());
		String dateTo = DateUtils.convertDate(getCalendarToCon(), DateFormat.YYYY_MM_DD());
		seriesData = BusinessPermit.getAscendingOrderOfControlNumber(dateFrom, dateTo, isCheckBoxSeries());
	}
	
	public void printAll() {
				
		String REPORT_NAME = BUSINESS_REPORT;
		double capital_total=0d, gross_total=0d;
		int number_of_employee=0;
		HashMap param = new HashMap();
		String path = REPORT_PATH;// + ReadConfig.value(Bris.BARANGAY_NAME).replace(" ", "") + Bris.SEPERATOR.getName();
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, path);
		
		List<BusinessRpt> reports = new ArrayList<BusinessRpt>();
		
		int cnt = 1;
		double total = 0d;
		//int size = getPmts().size();
		
		/*//remove since already sorted in loadSeach() method
		Map<String, BusinessPermit> rptsUnsort = new HashMap<String, BusinessPermit>();
		for(BusinessPermit p : getPmts()) {
			rptsUnsort.put(p.getControlNo(), p);
		}
		Map<String, BusinessPermit> rptsSort = new TreeMap<String, BusinessPermit>(rptsUnsort);
		*/
		//added to reverse
		Collections.reverse(getPmts());
		
		String[] filters = BusinessFilter.filter(BFilter.EXCEPTION);
		for(BusinessPermit p : getPmts()) {
		//for(BusinessPermit p : rptsSort.values()) {
			
			//get number of employee
			try { number_of_employee += Integer.valueOf(p.getEmpdtls().trim());}catch(Exception e) {}
			
			BusinessRpt r = new BusinessRpt();
			r.setF1(p.getControlNo().split("-")[1]);
			r.setF2(p.getCustomer().getFullname());
			//r.setF3(p.getBusinessEngage());
			r.setF4(p.getBusinessName());
			r.setF5(p.getBusinessAddress());
			
			String[] sp = p.getOrs().split("/");
			Map<String, BusinessORTransaction> unSort = new HashMap<String, BusinessORTransaction>();
			BusinessORTransaction ors = null;
			for(int i=0; i<sp.length; i++) {
				String sql = " AND orl.oractive=1 AND orl.orno=? AND cuz.customerid=? ";
				String[]params = new String[2];
				params[0] = sp[i];
				params[1] = p.getCustomer().getId()+"";
				try{ors = BusinessORTransaction.retrieve(sql, params).get(0);}catch(Exception e) {}
				
				if(ors!=null) {
					//unSort.put(ors.getAmount()+"-"+ors.getPurpose(), ors);
					unSort.put(""+i, ors);
				}
			}
			Map<String, BusinessORTransaction> sortedOR = new TreeMap<String, BusinessORTransaction>(unSort);
			int i = 1;
			
			String type = p.getType().substring(0, 1);
			//String mode = p.getMemoType().substring(0, 1);
			
			/*
			if("N".equalsIgnoreCase(type)) {
				newA +=1;
			}else if("R".equalsIgnoreCase(type)) {
				renewA +=1;
			}else {
				addA +=1;
			}*/
			
			/**
			if("Q".equalsIgnoreCase(mode)) {
				quarterly +=1;
			}else if("S".equalsIgnoreCase(mode)) {
				semi_annual +=1;
			}else {
				annually +=1;
			}*/
			
			BusinessORTransaction forFire = null;
			for(BusinessORTransaction or : sortedOR.values()) {
				
				String fire = or.getPurpose().toLowerCase().trim();
				if(!BusinessFilter.checkFilter(fire, filters)) {
				//if(!"fire".equalsIgnoreCase(fire)) {
					if(i==1) {
						
							r.setF3(or.getPurpose());
							r.setF6(or.getOrNumber());
							r.setF7(Currency.formatAmount(or.getAmount()));
							total += or.getAmount();
							r.setF8(type);
							r.setF9(p.getDateTrans());
							r.setF10(p.getDateTrans());
							
							r.setF11(p.getCapital());
							r.setF12(p.getGross());
							r.setF13(p.getEmpdtls());
							r.setF14(p.getMemoType());
							
							reports.add(r);
							
							try{ capital_total += Double.valueOf(p.getCapital().trim().replace(",", ""));}catch(Exception e) {}
							try{ gross_total += Double.valueOf(p.getGross().trim().replace(",", ""));}catch(Exception e) {}
							
					}else {
							r = new BusinessRpt();
							r.setF1("");
							r.setF2("");
							r.setF3(or.getPurpose());
							r.setF4("");
							r.setF5("");
							r.setF6(or.getOrNumber());
							r.setF7(Currency.formatAmount(or.getAmount()));
							total += or.getAmount();
							r.setF8("");
							r.setF9(or.getDateTrans());
							r.setF10(or.getDateTrans());
							
							if(or.getIscapital()==1) {
								r.setF11(Currency.formatAmount(or.getGrossAmount()));//capital
								r.setF12("");//gross sale
								capital_total += or.getGrossAmount();
							} else {
								r.setF11("");//capital
								r.setF12(Currency.formatAmount(or.getGrossAmount()));//gross sale
								gross_total += or.getGrossAmount();
							}
							
							//provide gross sale value if has value inputted
							/*
							r.setF11("");
							if(or.getGrossAmount()>0) {
								r.setF12(Currency.formatAmount(or.getGrossAmount()));//gross sale
							}else {
								r.setF12("");
							}*/
							
							r.setF13("");
							r.setF14("");
							
							reports.add(r);
					}
					i++;
				}else {
					forFire = or;
				}
			}
			
			
			if(forFire!=null) {
				r = new BusinessRpt();
				r.setF1("");
				r.setF2("");
				r.setF3(forFire.getPurpose());
				r.setF4("");
				r.setF5("");
				r.setF6(forFire.getOrNumber());
				r.setF7(Currency.formatAmount(forFire.getAmount()));
				
				r.setF8("");
				r.setF9(forFire.getDateTrans());
				r.setF10(forFire.getDateTrans());
				
				r.setF11("");
				r.setF12("");
				r.setF13("");
				r.setF14("");
				r.setLine("line");
				reports.add(r);
			}else {
				reports.add(BusinessRpt.builder().line("line").build());
			}
			
			
			/*change with setLine("line"); check on fire
			if(cnt!=size) {
				r = new BusinessRpt();
				
				r.setF1("------");//no
				r.setF2("-----------------------------");//name
				r.setF3("--------------------------------------------");//enganged
				r.setF4("---------------------------------------");//trade
				r.setF5("-----------------------------------------------------------");//address
				r.setF6("----------------");//or
				r.setF7("----------------");//amount
				r.setF8("---");//code
				r.setF9("----------------");//applied
				r.setF10("----------------");//issued
				r.setF11("-------------------");//capital
				r.setF12("----------------------");//gross
				r.setF13("---------------");//emp
				r.setF14("--------------------");//mode
				
				reports.add(r);
			}*/
			
			cnt++;
		}
		
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
		
		String yearFrom = DateUtils.convertDate(getCalendarFrom(), DateFormat.YYYY_MM_DD());
			   yearFrom = yearFrom.split("-")[0];
		String yearTo = DateUtils.convertDate(getCalendarTo(), DateFormat.YYYY_MM_DD());
			   yearTo = yearTo.split("-")[0];
		String cy = DateUtils.getCurrentYear()+"";
		
			   if(yearFrom.equalsIgnoreCase(yearTo)) {
				   cy = yearFrom;
			   }else {
				   cy = yearFrom + " to " + yearTo;
			   }
			   
		param.put("PARAM_CY", "LIST OF BUSINESS ESTABLISHMENTS CY. " + cy);
		param.put("PARAM_GRAND_TOTAL", "Php"+ Currency.formatAmount(total));
		
		//String yearFromDate = DateUtils.convertDate(getCalendarFrom(), "yyyy-MM-dd").split("-")[0];
		//String yearToDate = DateUtils.convertDate(getCalendarTo(), "yyyy-MM-dd").split("-")[0];
		String fromDate = DateUtils.convertDate(getCalendarFrom(), "yyyy-MM-dd");
		String toDate = DateUtils.convertDate(getCalendarTo(), "yyyy-MM-dd");
		
		Map<String, Integer> mapType = BusinessPermit.countType(fromDate, toDate);
		
		//try{param.put("PARAM_NEW", mapType.get("NEW")+"");}catch(Exception e) {param.put("PARAM_NEW", "0");}
		//try{param.put("PARAM_RENEW", mapType.get("RENEW")+"");}catch(Exception e) {param.put("PARAM_RENEW", "0");}
		//try{param.put("PARAM_ADDITIONAL", mapType.get("ADDITIONAL")+"");}catch(Exception e) {param.put("PARAM_ADDITIONAL", "0");}
		if(mapType.containsKey("NEW")) {
			param.put("PARAM_NEW", mapType.get("NEW")+"");
		}
		if(mapType.containsKey("RENEW")) {
			param.put("PARAM_RENEW", mapType.get("RENEW")+"");
		}
		if(mapType.containsKey("ADDITIONAL")) {
			if(mapType.get("ADDITIONAL")!=null) {param.put("PARAM_ADDITIONAL", mapType.get("ADDITIONAL")+"");}else {param.put("PARAM_ADDITIONAL", "0");}
		}
		
		Map<String, Integer> mapMemo = BusinessPermit.countMemoType(fromDate, toDate);
		
		int qr=0,sem=0;
		try{qr += mapMemo.get("1st QUARTER");}catch(Exception e) {}
		try{qr += mapMemo.get("2nd QUARTER");}catch(Exception e) {}
		try{qr += mapMemo.get("3rd QUARTER");}catch(Exception e) {}
		try{qr += mapMemo.get("4th QUARTER");}catch(Exception e) {}
		
		try{sem += mapMemo.get("1st SEMI-ANNUAL");}catch(Exception e) {}
		try{sem += mapMemo.get("2nd SEMI-ANNUAL");}catch(Exception e) {}
		
		try{param.put("PARAM_QUARTERLY", qr+"");}catch(Exception e) {param.put("PARAM_QUARTERLY", "0");}
		try{param.put("PARAM_SEMI_ANNUALLY", sem+"");}catch(Exception e) {param.put("PARAM_SEMI_ANNUALLY", "0");}
		try{param.put("PARAM_ANNUALLY", mapMemo.get("ANNUALLY")+"");}catch(Exception e) {param.put("PARAM_ANNUALLY", "0");}
		
		param.put("PARAM_CAPITAL", Currency.formatAmount(capital_total));
		param.put("PARAM_GROSS", Currency.formatAmount(gross_total));
		param.put("PARAM_EMPLOYEE_TOTAL", number_of_employee+"");
		
		//background
				String backlogo = path + "businessrpt.png";
				try{File file = new File(backlogo);
				FileInputStream off = new FileInputStream(file);
				//param.put("PARAM_BACKGROUND", off);
				}catch(Exception e){}
				
				
				//logo
				String officialLogo = path + "logo.png";
				try{File file = new File(officialLogo);
				FileInputStream off = new FileInputStream(file);
				param.put("PARAM_LOGO", off);
				}catch(Exception e){}
				
				//logo
				String logo = path + "logotrans.png";
				try{File file = new File(logo);
				FileInputStream off = new FileInputStream(file);
				param.put("PARAM_LOGO_WATERMARK", off);
				}catch(Exception e){}
				
				
				
				try{
			  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
			  	    JasperExportManager.exportReportToPdfFile(jrprint,path+ REPORT_NAME +".pdf");
			  		}catch(Exception e){e.printStackTrace();}
				
						try{
							System.out.println("REPORT_PATH:" + path + "REPORT_NAME: " + REPORT_NAME);
				  		 File file = new File(path, REPORT_NAME + ".pdf");
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
					            response.setHeader("Content-Disposition", "inline; filename=\"" + REPORT_NAME + ".pdf" + "\"");
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
	
	public BusinessPermit getBusinessData() {
		return businessData;
	}

	public void setBusinessData(BusinessPermit businessData) {
		this.businessData = businessData;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getBusinessEngage() {
		return businessEngage;
	}

	public void setBusinessEngage(String businessEngage) {
		this.businessEngage = businessEngage;
	}

	public String getBusinessAddress() {
		return businessAddress;
	}

	public void setBusinessAddress(String businessAddress) {
		this.businessAddress = businessAddress;
	}

	public String getBarangay() {
		return barangay;
	}

	public void setBarangay(String barangay) {
		this.barangay = barangay;
	}

	public String getPlateNo() {
		if(plateNo==null) {
			plateNo = BusinessPermit.getNewPlateNo();
		}
		return plateNo;
	}

	public void setPlateNo(String plateNo) {
		this.plateNo = plateNo;
	}

	public String getValidUntil() {
		if(validUntil==null) {
			String date = DateUtils.getCurrentYear()+"-12-31";
			validUntil = DateUtils.convertDateToMonthDayYear(date);
		}
		return validUntil;
	}

	public void setValidUntil(String validUntil) {
		this.validUntil = validUntil;
	}

	public String getIssuedOn() {
		if(issuedOn==null) {
			issuedOn = DateUtils.convertDateToMonthDayYear(DateUtils.getCurrentDateYYYYMMDD());
		}
		return issuedOn;
	}

	public void setIssuedOn(String issuedOn) {
		this.issuedOn = issuedOn;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMemoType() {
		if(memoType==null) {
			memoType = "ANNUALLY";
		}
		return memoType;
	}

	public void setMemoType(String memoType) {
		this.memoType = memoType;
	}

	public String getOic() {
		if(oic==null) {
			oic = Words.getTagName("oic");
		}
		return oic;
	}

	public void setOic(String oic) {
		this.oic = oic;
	}

	public String getMayor() {
		if(mayor==null) {
			mayor = Words.getTagName("mayor");
		}
		return mayor;
	}

	public void setMayor(String mayor) {
		this.mayor = mayor;
	}

	public String getControlNo() {
		if(controlNo==null) {
			controlNo = BusinessPermit.getNewControlNo();
		}
		return controlNo;
	}

	public void setControlNo(String controlNo) {
		this.controlNo = controlNo;
	}

	public String getTypeOf() {
		if(typeOf==null) {
			typeOf = "RENEW";
		}
		return typeOf;
	}

	public void setTypeOf(String typeOf) {
		this.typeOf = typeOf;
	}

	public List<BusinessORTransaction> getOrs() {
		return ors;
	}

	public void setOrs(List<BusinessORTransaction> ors) {
		this.ors = ors;
	}

	public List<BusinessORTransaction> getOrsSelected() {
		return orsSelected;
	}

	public void setOrsSelected(List<BusinessORTransaction> orsSelected) {
		this.orsSelected = orsSelected;
	}
	
	public void loadBusiness(){
		business = Collections.synchronizedList(new ArrayList<Livelihood>());
		String[] params = new String[0];
		String sql = " AND live.isactivelive=1";
		
		if(getTaxPayer()!=null) {
			params = new String[1];
			
			sql += " AND live.livelihoodtype!=1 AND cuz.customerid=?";
			params[0] = getTaxPayer().getId()+"";
			
			List<Livelihood> lvs = Livelihood.retrieve(sql, params); 
			if(lvs!=null && lvs.size()>1) {
				for(Livelihood lv : lvs){
					lv.setBusinessLabel(lv.getBusinessName());
					business.add(lv);
				}
			}else {
				clickItemBusiness(lvs.get(0));
				business.add(lvs.get(0));
			}
		}	
		
	}
}
