package com.italia.municipality.lakesebu.bean;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.NationalIDJsonData;
import com.italia.municipality.lakesebu.controller.QRCodeCitizen;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.Relationships;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Purok;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Named
@ViewScoped
public class BusinessOwnerBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1345465766767564534L;
	private String firstname;
	private String middlename;
	private String lastname;
	private String gender;
	private int age;
	private String address; 
	private String contactno;
	private String dateregistered;
	private String cardnumber;
	private String genderId;
	private List genderList = new ArrayList<>();
	
	private List<BusinessCustomer> customers =new ArrayList<BusinessCustomer>();
	private String searchCustomer;
	private BusinessCustomer customer; 
	
	private boolean enableAdditionalButton;
	
	private int clivilId;
	private List civils;
	
	private int relationshipId;
	private List relationships;
	
	private Date birthdate;
	private String emergencyContactPersonName;
	private BusinessCustomer emergencyContactPerson;
	private String searchEmergencyPerson;
	private List<BusinessCustomer> contactPersons = new ArrayList<BusinessCustomer>();
	
	private String photoId="camera";
	
	private final String sep = File.separator;
	private final String PRIMARY_DRIVE = System.getenv("SystemDrive");
	//private final String IMAGE_PATH = PRIMARY_DRIVE + sep + "bls" + sep + "img" + sep;
	private final String IMAGE_PATH = PRIMARY_DRIVE + sep + "webtis" + sep + "license" + sep + "img" + sep;
	private List<String> shots = new ArrayList<>();
	private String keyPress;
	
	private String searchProvince;
	private String searchMunicipal;
	private String searchBarangay;
	private String searchPurok;
	
	private List<Province> provinces = new ArrayList<Province>();
	private List<Municipality> municipals = new ArrayList<Municipality>();
	private List<Barangay> barangays = new ArrayList<Barangay>();
	private List<Purok> puroks = new ArrayList<Purok>();
	
	private Province provinceSelected;
	private Municipality municipalSelected;
	private Barangay barangaySelected;
	private Purok purokSelected;
	
	
	private boolean male;
	private boolean female;
	private boolean filteredBarangay;
	
	private long purokId;
	private List purokList;
	
	private String signature;
	
	private String bornplace;
	private String weight;
	private String height;
	private String work;
	private String citizenship;
	
	//QRCOde
	private String renderMethod;
    private String text;
    private String label;
    private int mode;
    private int size;
    private String fillColor;
    private String qrCode;
    private String nationalId;
    
    private List<BusinessCustomer> selectedQRCode;
	
   
	@PostConstruct
	public void init(){
		
		
		//init qrcode
		renderMethod = "canvas";
        text = "http://www.facebook.com/marxmind";
        label = "MARXMIND";
        mode = 2;
        fillColor = "7d767d";
        size = 100;
		
		try{
		String editProfileName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editProfile");
		System.out.println("Check pass name: " + editProfileName);
		if(editProfileName!=null && !editProfileName.isEmpty() && !"null".equalsIgnoreCase(editProfileName)){
			setSearchCustomer(editProfileName);
		}
		}catch(Exception e){}
		
		
		String barangayCode = GlobalVar.BARANGAY_CODE;
		
		String sql = " AND cus.cusisactive=1 ";
		
		if(getPurokId()!=0){
			sql += " AND cus.purid=" + getPurokId();
		}
		
		if(getMale() && !getFemale()){
			sql += " AND cus.cusgender='1' ";
		}else if(!getMale() && getFemale()){
			sql += " AND cus.cusgender='2' ";
		}
		
		if(getFilteredBarangay()){
			sql += " AND bar.bgid=" + getBarangaySelected().getId();
		}
		
		if(getSearchCustomer()!=null && !getSearchCustomer().isEmpty()){
			
			String sanitize = getSearchCustomer();
			sanitize = sanitize.replace("--", "");
			sanitize = sanitize.replace("%", "");
			sanitize = sanitize.replace("/", "");
			sanitize = sanitize.replace("drop", "");
			sanitize = sanitize.replace("table", "");
			sanitize = sanitize.replace("select", "");
			sanitize = sanitize.replace(";", "");
			sanitize = sanitize.replace("'", "");
			
			int size = getSearchCustomer().length();
			if(size>=5){
				customers = Collections.synchronizedList(new ArrayList<BusinessCustomer>());
				if(getSearchCustomer().contains(barangayCode)){
					String code = getSearchCustomer().toUpperCase();
					sql += " AND cus.cuscardno like '%" + code +"%'";
					customers = BusinessCustomer.retrieve(sql, new String[0]);
				}else{
					if(sanitize!=null || !sanitize.isEmpty()) {
						sql += " AND cus.fullname like '%" + sanitize +"%'";
						customers = BusinessCustomer.retrieve(sql, new String[0]);
					}
				}
				
			}
		}else{
			customers = Collections.synchronizedList(new ArrayList<BusinessCustomer>());
			sql += " order by cus.customerid DESC limit 100;";
			customers = BusinessCustomer.retrieve(sql, new String[0]);
		}
		
		
		
		if(customers!=null && customers.size()==1){
			clickItem(customers.get(0));
		}else{
			clearFields();
		}
		
	}
	
	public void loadAll(){
		clearFields();
		setFilteredBarangay(false);
		setMale(false);
		setFemale(false);
		customers = Collections.synchronizedList(new ArrayList<BusinessCustomer>());
		
		String sql = " AND cus.cusisactive=1 ORDER BY cus.customerid ASC";
		customers = BusinessCustomer.retrieve(sql, new String[0]);
		
	}
	
	public void loadFilter(){
		clearFields();
		
		customers = Collections.synchronizedList(new ArrayList<BusinessCustomer>());
		
		String sql = " AND cus.cusisactive=1 ";
		
		if(getPurokId()!=0){
			sql += " AND cus.purid=" + getPurokId();
		}
		
		if(getMale() && !getFemale()){
			sql += " AND cus.cusgender='1' ";
		}else if(!getMale() && getFemale()){
			sql += " AND cus.cusgender='2' ";
		}
		
		if(getFilteredBarangay()){
			sql += " AND bar.bgid=" + getBarangaySelected().getId();
		}
		
		sql+=" ORDER BY cus.customerid ASC";
		customers = BusinessCustomer.retrieve(sql, new String[0]);
		
	}
	
	public void loadContactPerson(){
		contactPersons = Collections.synchronizedList(new ArrayList<BusinessCustomer>());
		String sql = " AND cus.cusisactive=1 ";
		
		if(getSearchEmergencyPerson()!=null && !getSearchEmergencyPerson().isEmpty()){
			int size = getSearchEmergencyPerson().length();
			if(size>=5){
				sql += " AND (cus.fullname like '%" + getSearchEmergencyPerson() +"%' OR cusfirstname like '%"+ 
			getSearchEmergencyPerson() +"%' OR cusmiddlename like '%"+ getSearchEmergencyPerson() +"%' OR cuslastname like '%"+ getSearchEmergencyPerson() +"%' )";
				contactPersons = BusinessCustomer.retrieve(sql, new String[0]);
			}
		}else{
			sql += " order by cus.customerid DESC limit 10;";
			contactPersons = BusinessCustomer.retrieve(sql, new String[0]);
		}
		
		
	}
	
	public void loadProvince(){
		provinces = Collections.synchronizedList(new ArrayList<Province>());
		try{
		String sql = " AND prv.provisactive=1";
		String[] params = new String[0];
		if(getSearchProvince()!=null){
			sql += " AND prv.provname like '%"+ getSearchProvince().replace("--", "") +"%'";
		}
		provinces = Province.retrieve(sql, params);
		Collections.reverse(provinces);
		}catch(Exception e){}
	}
	
	public void loadMunicipal(){
		municipals = Collections.synchronizedList(new ArrayList<Municipality>());
		try{
			String[] params = new String[1];
			String sql = " AND mun.munisactive=1 AND prv.provid=?";
			params[0] = getProvinceSelected().getId()+"";
			if(getSearchMunicipal()!=null){
				sql += " AND mun.munname like '%"+ getSearchMunicipal().replace("--", "") +"%'";
			}
			municipals = Municipality.retrieve(sql, params);
			Collections.reverse(municipals);
		}catch(Exception e){}
	}
	
	public void loadBarangay(){
		barangays = Collections.synchronizedList(new ArrayList<Barangay>());
		try{
		String[] params = new String[2];
		String sql = " AND bgy.bgisactive=1 AND prv.provid=? AND mun.munid=?";
		params[0] = getProvinceSelected().getId()+"";
		params[1] = getMunicipalSelected().getId()+"";
		if(getSearchBarangay()!=null){
			sql += " AND bgy.bgname like '%"+ getSearchBarangay().replace("--", "") +"%'";
		}
		barangays = Barangay.retrieve(sql, params);
		Collections.reverse(barangays);
		}catch(Exception e){}
	}
	
	public void loadPurok(){
		puroks = Collections.synchronizedList(new ArrayList<Purok>());
		try{
		String[] params = new String[2];
		String sql = " AND pur.isactivepurok=1 AND mun.munid=? AND bgy.bgid=? ";
		params[0] = getMunicipalSelected().getId()+"";
		params[1] = getBarangaySelected().getId()+"";
		if(getSearchPurok()!=null){
			sql += " AND pur.purokname like '%"+ getSearchPurok().replace("--", "") +"%'";
		}
		puroks = Purok.retrieve(sql, params);
		Collections.reverse(puroks);
		}catch(Exception e){}
	}
	
	public void clickItemPopup(Object obj){
		if(obj instanceof Province){
			Province prov = (Province)obj;
			setProvinceSelected(prov);
			setMunicipalSelected(null);
			setBarangaySelected(null);
			setPurokSelected(null);
		}else if(obj instanceof Municipality){
			Municipality mun = (Municipality)obj;
			setMunicipalSelected(mun);
			setBarangaySelected(null);
			setPurokSelected(null);
		}else if(obj instanceof Barangay){
			Barangay bar = (Barangay)obj;
			setBarangaySelected(bar);
			setPurokSelected(null);
		}else if(obj instanceof Purok){
			Purok pur = (Purok)obj;
			setPurokSelected(pur);
		}
		
	}
	
	public void clickContact(BusinessCustomer person){
		setEmergencyContactPerson(person);
		setEmergencyContactPersonName(person.getFullname());
	}
	
	private String getRandomImageName() {
        int i = (int) (Math.random() * 10000000);
         
        return String.valueOf(i);
    }
	
	public void selectedPhoto(String photoId){
		setPhotoId(photoId);
	}
	
	public void deleteTmpImages(){
		
		if(getShots()!=null && getShots().size()>0){
			deletingImages();
		}
	}
	
	public void oncapture(CaptureEvent captureEvent) {
        //photoId = getRandomImageName();
		
		photoId = getRandomImageName() + DateUtils.getCurrentDateMMDDYYYYTIMEPlain();
		shots.add(photoId);
    	//filename ="cam";
        System.out.println("Set picture name " + photoId);
        byte[] data = captureEvent.getData();
 
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        String driveImage =  GlobalVar.OWNER_IMG_FOLDER + photoId + ".jpg";
        String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;
        
        try{
        
        
        }catch(Exception e){}
        
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
            //capturedImagePathName = contextImageLoc + photoId + ".jpg";
           // System.out.println("capture path " + capturedImagePathName.replace("\\", "/"));
           // setCapturedImagePathName(capturedImagePathName.replace("\\", "/"));
        }
        catch(IOException e) {
            throw new FacesException("Error in writing captured image.", e);
        }
    }
	
	public void printAll(){
		
	}
	
	public String save(){
		
		if(Login.getUserLogin().checkUserStatus()){
			
			BusinessCustomer cus = new BusinessCustomer();
			if(getCustomer()!=null){
				cus = getCustomer();
			}else{
				cus.setIsactive(1);
			}
			
			boolean isOk = true;
			
			if(getFirstname()==null || getFirstname().isEmpty()){
				Application.addMessage(3, "Please provide firstname.", "");
				isOk= false;
			}
			
			if(getMiddlename()==null || getMiddlename().isEmpty()){
				Application.addMessage(3, "Please provide middlename.", "");
				isOk= false;
			}
			
			if(getLastname()==null || getLastname().isEmpty()){
				Application.addMessage(3, "Please provide lastname.", "");
				isOk= false;
			}
			
			if(getBirthdate()==null){
				Application.addMessage(3, "Please provide birthday.", "");
				isOk= false;
			}
			
			if(getAge()<0){
				Application.addMessage(3, "Please provide age.", "");
				isOk= false;
			}
			
			if(getEmergencyContactPerson()!=null && getEmergencyContactPerson().getId()!=0){
				if(getRelationshipId()==0){
					Application.addMessage(3, "Please provide relationship.", "");
					isOk= false;
				}
			}
			
			if("camera".equalsIgnoreCase(getPhotoId())){
				copyDefaultImage();
			}
			
			if(getCustomer()==null && getFirstname()!=null && getMiddlename()!=null && getLastname()!=null){
				
				boolean isExist = BusinessCustomer.validateNameEntry(getFirstname(), getMiddlename(), getLastname());
				if(isExist){
					Application.addMessage(3, getFirstname() + " " + getLastname() + " is already recorded.", "");
					isOk= false;
				}
			}
			
			isOk = addressCondition(isOk);
			
			if(isOk){
				
				if(getContactno()==null){
					cus.setContactno("0");
				}else{
					cus.setContactno(getContactno());
				}
			
			cus.setDateregistered(getDateregistered());
			//cus.setAddress(getAddress());	
			cus.setPhotoid(getPhotoId());	
			cus.setFirstname(getFirstname().trim());
			cus.setMiddlename(getMiddlename().trim());
			cus.setLastname(getLastname().trim());
			if(getLastname().equalsIgnoreCase(getMiddlename()) ) {
				cus.setFullname(getFirstname());
			}else {
			//cus.setFullname(getFirstname().trim() + " " + getLastname().trim());
			cus.setFullname(getLastname().trim() + ", " + getFirstname().trim() + " " + getMiddlename().trim());
			}
			cus.setGender(getGenderId());
			cus.setAge(getAge());
			cus.setCivilStatus(getClivilId());
			
			cus.setBirthdate(DateUtils.convertDate(getBirthdate(),"yyyy-MM-dd"));
			cus.setEmergencyContactPerson(getEmergencyContactPerson());
			cus.setRelationship(getRelationshipId());
			
			cus.setIsactive(1);
			cus.setUserDtls(Login.getUserLogin().getUserDtls());
			
			cus.setPurok(getPurokSelected());
			
			//Barangay bg = getBarMap().get(getBarangayId());
			cus.setBarangay(getBarangaySelected());
			
			//Municipality mun = getMunMap().get(getMunicipalityId());
			cus.setMunicipality(getMunicipalSelected());
			
			//Province prov = getProvMap().get(getProvinceId());
			cus.setProvince(getProvinceSelected());
			
			cus.setBornplace(getBornplace());
			cus.setWeight(getWeight());
			cus.setHeight(getHeight());
			cus.setWork(getWork());
			cus.setCitizenship(getCitizenship());
			cus.setQrcode(getQrCode());
			cus.setNationalId(getNationalId());
			
			cus = BusinessCustomer.save(cus);
			clickItem(cus);
			setCustomer(cus);
			
			//saveFinalImage();
			deletingImages();
			//clearFields();
			init();
			Application.addMessage(1, "Successfully saved.", "");
			}
		}
		
		return "save";
	}
	
	private boolean addressCondition(boolean isOk){
		
		
		
		if(getProvinceSelected()==null){
			Application.addMessage(3, "Please provide province.", "");
			isOk= false;
		}
		
		if(getProvinceSelected()!=null && getMunicipalSelected()==null && getBarangaySelected()==null && getPurokSelected()==null){
			
			Municipality mun = new Municipality();
			mun.setId(0);
			setMunicipalSelected(mun);
			
			Barangay bar = new Barangay();
			bar.setId(0);
			setBarangaySelected(bar);
			
			Purok pur = new Purok();
			pur.setId(0);
			setPurokSelected(pur);
		}
		
		if(getProvinceSelected()!=null && getMunicipalSelected()!=null && getBarangaySelected()==null && getPurokSelected()==null){
			
			Barangay bar = new Barangay();
			bar.setId(0);
			setBarangaySelected(bar);
			
			Purok pur = new Purok();
			pur.setId(0);
			setPurokSelected(pur);
		}
		
		if(getProvinceSelected()!=null && getMunicipalSelected()!=null && getBarangaySelected()!=null && getPurokSelected()==null){
			
			Purok pur = new Purok();
			pur.setId(0);
			setPurokSelected(pur);
		}
		
		
		return isOk;
	}
	
	private void copyDefaultImage(){
		
				photoId = getRandomImageName() + DateUtils.getCurrentDateMMDDYYYYTIMEPlain();
		        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		        
		        String cameralogo = IMAGE_PATH + "default.jpg";
		        String driveImage =  GlobalVar.OWNER_IMG_FOLDER + photoId + ".jpg";
		        
	            File file = new File(cameralogo);
	            try{
	    			Files.copy(file.toPath(), (new File(driveImage)).toPath(),
	    			        StandardCopyOption.REPLACE_EXISTING);
	    		}catch(IOException e){}
		        
		        
		        	String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;
		            
		            
		            
		            String pathToSave = externalContext.getRealPath("") + contextImageLoc;
		            File fileNew = new File(driveImage);
		            try{
		    			Files.copy(fileNew.toPath(), (new File(pathToSave + fileNew.getName())).toPath(),
		    			        StandardCopyOption.REPLACE_EXISTING);
		    		}catch(IOException e){}
		        
		        
		
	}
	
	public void removeEmergencyPerson(){
		setEmergencyContactPerson(null);
		setEmergencyContactPersonName(null);
		setRelationshipId(0);
		Application.addMessage(1, "Successfully removed.", "");
	}
	
	private void saveFinalImage(){
		
		if(!"camera".equalsIgnoreCase(getPhotoId())){
			String photoDestination = GlobalVar.OWNER_IMG_FOLDER + "photo" + File.separator;
			//check log directory
	        File logdirectory = new File(photoDestination);
	        if(!logdirectory.isDirectory()){
	        	logdirectory.mkdir();
	        }
			
			File file = new File(GlobalVar.OWNER_IMG_FOLDER + getPhotoId() + ".jpg");
			String pathToSave = photoDestination + getPhotoId() + ".jpg";
	         try{
	 			Files.copy(file.toPath(), (new File(pathToSave)).toPath(),
	 			        StandardCopyOption.REPLACE_EXISTING);
	 			
	 			file.delete();
	 			}catch(IOException e){}
	         
	         deletingImages();
		}
		
		 
	}
	
	/**
	 * Deleting old picture/s for the selected customer
	 */
	private void deletingImages(){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        String deleteImg = externalContext.getRealPath("") + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;// + photoId + ".jpg";
        
        try{
        
       getShots().remove(getPhotoId());	
       for(String name : shots){ 	
        	if(!getPhotoId().equalsIgnoreCase(name)){
	        File img = new File(GlobalVar.OWNER_IMG_FOLDER + name + ".jpg");
	        img.delete();
	       
	        img = new File(deleteImg + name + ".jpg");
	        img.delete();
        	}
       	}
        }catch(Exception e){}
	}
	
	public String close(){
		clearFields();
		init();
		return "close";
	}
	
	public void print(){
		
	}
	
	private FileInputStream generateQRCode(BusinessCustomer cus, String folder) throws IOException {
		
		String fileName = cus.getFullname();
		String val = "";
		
		val += "Reg:" + cus.getDateregistered()  + "|";
		val += "CardNumber:" + cus.getCardno() + "|";
		val += "FName:" + cus.getFirstname() + "|";
		val += "MName:" + cus.getMiddlename() + "|";
		val += "LName:" + cus.getLastname() + "|";
		val += "CStatus:" + cus.getCivilStatus() + "|";
		val += "BDate:" + cus.getBirthdate() + "|";
		val += "Age:" + cus.getAge() + "|";
		val += "BornPlace:" + cus.getBornplace() + "|";
		val += "Weight:" + cus.getWeight() + "|";
		val += "Height:" + cus.getHeight() + "|";
		val += "Work:" + cus.getWork() + "|";
		val += "Citizenship:" + cus.getCitizenship() + "|";
		val += "Gender:" + cus.getGender() + "|";
		val += "Province:" + cus.getProvince().getName() + "|";
		val += "Municipality:" + cus.getMunicipality().getName() + "|";
		val += "Barangay:" + cus.getBarangay().getName() + "|";
		val += "Purok:" + cus.getPurok().getPurokName() + "|";
		val += "EmergencyContactPerson:" + cus.getEmergencyContactPerson().getFullname() + "|";
		val += "ContactNo:" + cus.getContactno() + "|";
		val += "Relationship:" + cus.getRelationship();
		
		//val = cus.getFullname();
		val = cus.getQrcode()==null? cus.getFullname() : (cus.getQrcode().isEmpty()? cus.getFullname() : cus.getQrcode());
		OutputStream outputStream;
		try {
			
			outputStream = new FileOutputStream(folder + fileName + ".png");
			
			QRCode.from(val).to(ImageType.PNG).withSize(400, 400).writeTo(outputStream);
			
			FileInputStream fil = new FileInputStream(new File(folder + fileName + ".png"));
			
			outputStream.close();
			
			/*
			File file = QRCode.from(val).to(ImageType.PNG)
			        .withSize(400, 400)
			        .file();
			 
			String qrcode = folder + fileName + ".png";
			 
			Path path = Paths.get(qrcode);
			if ( Files.exists(path)){
			    Files.delete(path);
			}
			 
			Files.copy(file.toPath(), path);
			FileInputStream fil = new FileInputStream(new File(folder + fileName + ".png"));*/
			
			return fil;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public void printQRCode() {
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String qrcodeFolder = REPORT_PATH +  AppConf.SEPERATOR.getValue() + "qrcode" +  AppConf.SEPERATOR.getValue();
		try {
			if(getSelectedQRCode()!=null && getSelectedQRCode().size()>0) {
				
			List<QRCodeCitizen> rpts = new ArrayList<QRCodeCitizen>(); 
			
			int size = getSelectedQRCode().size();
			QRCodeCitizen qc = new QRCodeCitizen();
			
			if(size==1) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				rpts.add(qc);
			}
			if(size==2) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				rpts.add(qc);
			}
			if(size==3) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
			}
			if(size==4) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				rpts.add(qc);
			}
			if(size==5) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				rpts.add(qc);
			}
			if(size==6) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
			}
			
			if(size==7) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				rpts.add(qc);
			}
			
			if(size==8) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				rpts.add(qc);
			}
			if(size==9) {
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
				
				qc = new QRCodeCitizen();
				qc.setF1(generateQRCode(getSelectedQRCode().get(0),qrcodeFolder));
				qc.setF2(generateQRCode(getSelectedQRCode().get(1),qrcodeFolder));
				qc.setF3(generateQRCode(getSelectedQRCode().get(2),qrcodeFolder));
				rpts.add(qc);
			}
			
			
			String REPORT_NAME = GlobalVar.QRCODE_CITIZEN;
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rpts);
	  		HashMap param = new HashMap();
	  		
	  		
	  		
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
			}    
		        
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
	
	public void clickItem(BusinessCustomer cus){
		shots = new ArrayList<>();//clearing picture
		setCustomer(cus);
		setCardnumber(cus.getCardno());
		setDateregistered(cus.getDateregistered());
		setFirstname(cus.getFirstname());
		setMiddlename(cus.getMiddlename()==null? "Middle Name" : cus.getMiddlename());
		setLastname(cus.getLastname());
		setGenderId(cus.getGender());
		try{setAge(DateUtils.calculateAge(cus.getBirthdate()));}catch(Exception e){setAge(cus.getAge());}
		setContactno(cus.getContactno());
		setClivilId(cus.getCivilStatus());
		
		setBirthdate(DateUtils.convertDateString(cus.getBirthdate(),"yyyy-MM-dd"));
		setEmergencyContactPerson(cus.getEmergencyContactPerson());
		if(cus.getEmergencyContactPerson()!=null){
			BusinessCustomer person = BusinessCustomer.customer(cus.getEmergencyContactPerson().getId());	
		setEmergencyContactPersonName(person.getFullname());
		setRelationshipId(cus.getRelationship());
		}else{
			setRelationshipId(0);
		}
		if(cus.getPhotoid()!=null){
			copyPhoto(cus.getPhotoid()); 
			getShots().add(cus.getPhotoid());
		}
		
		setProvinceSelected(cus.getProvince());
		setMunicipalSelected(cus.getMunicipality());
		setBarangaySelected(cus.getBarangay());
		setPurokSelected(cus.getPurok());
		
		
		setBornplace(cus.getBornplace());
		setWeight(cus.getWeight());
		setHeight(cus.getHeight());
		setWork(cus.getWork());
		setCitizenship(cus.getCitizenship());
		setQrCode(cus.getQrcode());
		setNationalId(cus.getNationalId());
		
		//init qrcode
		renderMethod = "canvas";
		text = "http://www.facebook.com/marxmind";
		label = cus.getFullname().toUpperCase();
		mode = 2;
		fillColor = "7d767d";
		size = 100;
		
	}
	
	private String copyPhoto(String photoId){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String driveImage =  GlobalVar.OWNER_IMG_FOLDER + photoId + ".jpg";
        setPhotoId(photoId);
        String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;
            String pathToSave = externalContext.getRealPath("") + contextImageLoc;
            File file = new File(driveImage);
            try{
    			Files.copy(file.toPath(), (new File(pathToSave + file.getName())).toPath(),
    			        StandardCopyOption.REPLACE_EXISTING);
    			}catch(IOException e){}
            return driveImage;
	}
	
	public void calculateAge(){
		String dateBirth = DateUtils.convertDate(getBirthdate(),"yyyy-MM-dd");
		setAge(Integer.valueOf(Math.round(DateUtils.calculateAgeNow(dateBirth))+""));
	}
	
	public void closePopup(){
		setEnableAdditionalButton(true);
		clearFields();
		init();
	}
	
	public void clearFields(){
		setFirstname(null);
		setMiddlename(null);
		setLastname(null);
		setGender(null);
		setAge(0);
		setAddress(null);
		setContactno(null);
		setDateregistered(null);
		setSearchCustomer(null);
		setCardnumber(null);
		setCustomer(null);
		setGenderId(null);
		
		setClivilId(1);
		setPhotoId(null);
		shots = new ArrayList<>();
		setEmergencyContactPerson(null);
		setBirthdate(null);
		setRelationshipId(0);
		setEmergencyContactPersonName(null);
		
		setProvinceSelected(null);
		setMunicipalSelected(null);
		setBarangaySelected(null);
		setPurokSelected(null);
		
		setSearchProvince(null);
		setSearchMunicipal(null);
		setSearchBarangay(null);
		setSearchPurok(null);
		
		setEnableAdditionalButton(true);
		
		loadDefaultAddress();
		
		setBornplace(null);
		setWeight(null);
		setHeight(null);
		setWork(null);
		setCitizenship(null);
		setQrCode(null);
		setNationalId(null);
		
		//init qrcode
				renderMethod = "canvas";
				text = "http://www.facebook.com/marxmind";
				label = "MARXMIND";
				mode = 2;
				fillColor = "7d767d";
				size = 100;
	}
	
	private void loadDefaultAddress(){
		try{
		String sql = " AND prv.provisactive=1 AND prv.provname=?";
		String[] params = new String[1];
		params[0] = GlobalVar.PROVINCE;
		provinces = Province.retrieve(sql, params);
		setProvinceSelected(provinces.get(0));
		
		params = new String[2];
		sql = " AND mun.munisactive=1 AND prv.provid=? AND mun.munname=?";
		params[0] = getProvinceSelected().getId()+"";
		params[1] = GlobalVar.MUNICIPALITY;
		municipals = Municipality.retrieve(sql, params);
		setMunicipalSelected(municipals.get(0));
		
		params = new String[3];
		sql = " AND bgy.bgisactive=1 AND prv.provid=? AND mun.munid=? AND bgy.bgname=?";
		params[0] = getProvinceSelected().getId()+"";
		params[1] = getMunicipalSelected().getId()+"";
		params[2] = GlobalVar.BARANGAY;
		barangays = Barangay.retrieve(sql, params);
		setBarangaySelected(barangays.get(0));
		
		}catch(Exception e){}
	}
	
	public void deleteRow(BusinessCustomer cus){
		if(Login.getUserLogin().checkUserStatus()){
			if(cus.hasExistingTransaction()) {
				Application.addMessage(3, "Error", "Cannot erase this Customer. It has already an existing transaction in Business Registration, Official Receipt listing");
			}else {
				cus.setUserDtls(Login.getUserLogin().getUserDtls());
				cus.delete();
				init();
				Application.addMessage(1, "Successfully deleted", "");
			}
		}
	}
	
	public void fileUploadListener(FileUploadEvent event) {

        try {
            BufferedImage image = ImageIO
                    .read(event.getFile().getInputStream());
            if (image != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", outputStream);
                byte[] bytes = outputStream.toByteArray();
	            System.out.println("Bytes -> " + bytes.length);
	            writeImageToFile(image);
            } else {
                throw new IOException("FAILED TO CONVERT PICTURE");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
	
	private void writeImageToFile(BufferedImage image){
		try{
			photoId = getRandomImageName() + DateUtils.getCurrentDateMMDDYYYYTIMEPlain();
			shots.add(photoId);
			File fileImg = new File(GlobalVar.OWNER_IMG_FOLDER + photoId + ".jpg");
			System.out.println(photoId + ".jpg");
			if(image==null){
				fileImg = new File(GlobalVar.OWNER_IMG_PATH + "noimageproduct.jpg");
				 image = ImageIO.read(fileImg);
			}
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String contextImageLoc = "resources" + File.separator + "images" + File.separator + "photocam" + File.separator;
			String pathToSave = externalContext.getRealPath("") + contextImageLoc;
           // File file = new File(driveImage);
            try{
            	ImageIO.write(image, "jpg", fileImg);
    			Files.copy(fileImg.toPath(), (new File(pathToSave + fileImg.getName())).toPath(),
    			        StandardCopyOption.REPLACE_EXISTING);
    			System.out.println("writing images....." + pathToSave);
    			}catch(IOException e){}
		
		}catch(IOException e){}
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
			BusinessCustomer cs = null;
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
			
				
			setSearchCustomer(fullname);
			
			}else {
				sql = " AND cus.cuscardno  like '%"+ jsonData.trim() +"%' ";
			}
			
			/////
			if(checkBusinessCustomerIfExist(first,last)) {
				pf.executeScript("PF('dlgCam').hide()");
			}else {
				
			List<PoblacionCustomer> cus = PoblacionCustomer.retrieve(sql, new String[0]);
			if(cus!=null && cus.size()>0) {
				PoblacionCustomer pc = cus.get(0);
						cs = BusinessCustomer.builder()
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
			
						provideCustomerDataFromQRCode(cs,false);
						
				pf.executeScript("PF('dlgCam').hide();PF('dlgSelection').show();");
			}else {
				cs = BusinessCustomer.builder()
						.firstname(first)
						.middlename(middle)
						.lastname(last)
						.birthdate(birthdate)
						.gender(gender)
						.civilStatus(Integer.valueOf(civilStatus))
						.fullname(last + ", " + first + " " + middle)
						.completeAddress(address)
						.build();
				
				
				provideCustomerDataFromQRCode(cs,false);
				pf.executeScript("PF('dlgCam').hide()");
			}
			
			}
			
		}else {
				
			NationalIDJsonData data = new NationalIDJsonData(jsonData);
			if(data!=null && !data.getPCN().isEmpty()) {	
				System.out.println("Checking PCN: " + data.getPCN());
				String fullName = data.getLName() + ", " + data.getFName() + " " + data.getMName().substring(0,1);
				String sql = " AND (cus.nationalid='"+ data.getPCN().trim() +"'";	
					   sql += " OR cus.fullname like '%"+ fullName +"%' )";
				String[] params = new String[0];
				
				List<BusinessCustomer> cust = BusinessCustomer.retrieve(sql, params);
					//int size = cust.size();
					//System.out.println("counting: " + size);
					//size = size>0? size-1 : size;
					if(cust!=null && cust.size()>0) {
						BusinessCustomer customer = cust.get(0);
						
						//force change
						customer.setBornplace(data.getPOB());
						customer.setNationalId(data.getPCN());
						customer.setMiddlename(data.getMName());
						
						setCustomer(customer);
						clickItem(customer);
				    	pf.executeScript("PF('dlgCam').hide();");
					}else {
						BusinessCustomer cus = BusinessCustomer.builder()
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
						
						setDateregistered(cus.getDateregistered()==null? DateUtils.getCurrentDateMonthDayYear() : cus.getDateregistered());
						setFirstname(cus.getFirstname());
						setMiddlename(cus.getMiddlename());
						setLastname(cus.getLastname());
						setClivilId(1);
						setBirthdate(DateUtils.convertDateString(cus.getBirthdate(), "yyyy-MM-dd"));
						try{setAge(DateUtils.calculateAge(cus.getBirthdate()));}catch(Exception e){setAge(cus.getAge());}
						setGenderId(cus.getGender());
						setCitizenship("Filipino");
						setBornplace(data.getPOB());
						setNationalId(data.getPCN());
						
						defaultValueAddress();
						
						
						pf.executeScript("PF('dlgCam').hide();");
					}
				
				}else {
		
		
				String sql = " AND (cus.qrcode like '%"+ jsonData +"%' OR ";
				sql += " cus.fullname like '%"+ jsonData +"%' ";
				sql += " )";
				String[] params = new String[0];
				
				List<BusinessCustomer> cust = BusinessCustomer.retrieve(sql, params);
				
				if(cust!=null && cust.size()>0) {
					provideCustomerDataFromQRCode(cust.get(0),true);
			    	pf.executeScript("PF('dlgCam').hide();");
				}else {
					Application.addMessage(1, "Error", "This QRCode is not yet registered... Please register it first in Citizen Registration Page");
				}
		
				}
		}
		
	}
	
	private boolean checkBusinessCustomerIfExist(String first, String last) {
		
		String sql = " AND cus.cusfirstname ="+ first +"' AND cus.cuslastname = " + last;
		String[] params = new String[0];
		
		List<BusinessCustomer> cust = BusinessCustomer.retrieve(sql, params);
		
		if(cust!=null && cust.size()>0) {
			provideCustomerDataFromQRCode(cust.get(0),true);
	    	return true;
		}
		
		return false;
	}
	
	private void provideCustomerDataFromQRCode(BusinessCustomer cs, boolean isOld) {
		setCustomer(cs);
		setCardnumber(cs.getCardno()==null? BusinessCustomer.cardNumber() : cs.getCardno());
		setDateregistered(cs.getDateregistered()==null? DateUtils.getCurrentDateMonthDayYear() : cs.getDateregistered());
		setFirstname(cs.getFirstname());
		setMiddlename(cs.getMiddlename());
		setLastname(cs.getLastname());
		setClivilId(cs.getCivilStatus());
		setBirthdate(DateUtils.convertDateString(cs.getBirthdate(), "yyyy-MM-dd"));
		try{setAge(DateUtils.calculateAge(cs.getBirthdate()));}catch(Exception e){setAge(cs.getAge());}
		setGenderId(cs.getGender());
		setCitizenship(cs.getCitizenship());
		defaultValueAddress();
		
		if(isOld) {
		///////
		
		setContactno(cs.getContactno());
		
		setEmergencyContactPerson(cs.getEmergencyContactPerson());
		if(cs.getEmergencyContactPerson()!=null){
			BusinessCustomer person = BusinessCustomer.customer(cs.getEmergencyContactPerson().getId());	
		setEmergencyContactPersonName(person.getFullname());
		setRelationshipId(cs.getRelationship());
		}else{
			setRelationshipId(0);
		}
		if(cs.getPhotoid()!=null){
			copyPhoto(cs.getPhotoid()); 
			getShots().add(cs.getPhotoid());
		}
		
		setProvinceSelected(cs.getProvince());
		setMunicipalSelected(cs.getMunicipality());
		setBarangaySelected(cs.getBarangay());
		setPurokSelected(cs.getPurok());
		
		setBornplace(cs.getBornplace());
		setWeight(cs.getWeight());
		setHeight(cs.getHeight());
		setWork(cs.getWork());
		setCitizenship(cs.getCitizenship());
		setQrCode(cs.getQrcode());
		setNationalId(cs.getNationalId());
		
		}
	}
	
	private void defaultValueAddress() {
		setProvinceSelected(Province.builder().id(1).name("South Cotabato").isActive(1).build());
		setMunicipalSelected(Municipality.builder().id(1).name("Lake Sebu").isActive(1).build());
		setBarangaySelected(Barangay.builder().id(0).name("N/A").isActive(1).build());
		setPurokSelected(Purok.builder().id(0).purokName("N/A").isActive(1).build());
	}
	
	public List<String> autoFirst(String query){
		int size = query.length();
		if(size>=2){
			return BusinessCustomer.retrieve(query, "cusfirstname"," limit 10");
		}else{
			return Collections.synchronizedList(new ArrayList<String>());
		}
	}
	public List<String> autoMiddle(String query){
		int size = query.length();
		if(size>=2){
			return BusinessCustomer.retrieve(query, "cusmiddlename"," limit 10");
		}else{
			return Collections.synchronizedList(new ArrayList<String>());
		}
	}
	public List<String> autoLast(String query){
		int size = query.length();
		if(size>=2){	
			return BusinessCustomer.retrieve(query, "cuslastname"," limit 10");
		}else{
			return Collections.synchronizedList(new ArrayList<String>());
		}	
	}
	
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getMiddlename() {
		if(middlename==null || middlename.isEmpty()) {
			middlename=".";
		}
		return middlename;
	}
	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}
	public String getLastname() {
		if(lastname==null || lastname.isEmpty()) {
			lastname = ".";
		}
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getContactno() {
		return contactno;
	}
	public void setContactno(String contactno) {
		this.contactno = contactno;
	}
	public List<BusinessCustomer> getCustomers() {
		return customers;
	}
	public void setCustomers(List<BusinessCustomer> customers) {
		this.customers = customers;
	}
	public String getSearchCustomer() {
		return searchCustomer;
	}
	public void setSearchCustomer(String searchCustomer) {
		this.searchCustomer = searchCustomer;
	}
	public BusinessCustomer getCustomer() {
		return customer;
	}
	public void setCustomer(BusinessCustomer customer) {
		this.customer = customer;
	}

	public String getDateregistered() {
		if(dateregistered==null){
			dateregistered = DateUtils.getCurrentDateYYYYMMDD();
		}
		return dateregistered;
	}

	public void setDateregistered(String dateregistered) {
		this.dateregistered = dateregistered;
	}

	public String getCardnumber() {
		if(cardnumber==null){
			cardnumber = BusinessCustomer.cardNumber();
		}
		return cardnumber;
	}

	public void setCardnumber(String cardnumber) {
		this.cardnumber = cardnumber;
	}

	public String getGenderId() {
		if(genderId==null){
			genderId = "1";
		}
		return genderId;
	}

	public void setGenderId(String genderId) {
		this.genderId = genderId;
	}

	public List getGenderList() {
		
		genderList = Collections.synchronizedList(new ArrayList<>());
		genderList.add(new SelectItem("1","Male"));
		genderList.add(new SelectItem("2","Female"));
		
		return genderList;
	}

	public void setGenderList(List genderList) {
		this.genderList = genderList;
	}

	

	public int getClivilId() {
		if(clivilId==0){
			clivilId = 1;
		}
		return clivilId;
	}

	public void setClivilId(int clivilId) {
		this.clivilId = clivilId;
	}

	public List getCivils() {
		civils = new ArrayList<>();
		
		for(CivilStatus cv : CivilStatus.values()){
			civils.add(new SelectItem(cv.getId(), cv.getName()));
		}
		
		return civils;
	}

	public void setCivils(List civils) {
		this.civils = civils;
	}

	public int getRelationshipId() {
		return relationshipId;
	}

	public void setRelationshipId(int relationshipId) {
		this.relationshipId = relationshipId;
	}

	public List getRelationships() {
		relationships = new ArrayList<>();
		for(Relationships rel : Relationships.values()){
			relationships.add(new SelectItem(rel.getId(), rel.getName()));
		}
		return relationships;
	}

	public void setRelationships(List relationships) {
		this.relationships = relationships;
	}

	public Date getBirthdate() {
		if(birthdate==null){
			birthdate = DateUtils.getDateToday();
		}
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public BusinessCustomer getEmergencyContactPerson() {
		return emergencyContactPerson;
	}

	public void setEmergencyContactPerson(BusinessCustomer emergencyContactPerson) {
		this.emergencyContactPerson = emergencyContactPerson;
	}

	public String getSearchEmergencyPerson() {
		return searchEmergencyPerson;
	}

	public void setSearchEmergencyPerson(String searchEmergencyPerson) {
		this.searchEmergencyPerson = searchEmergencyPerson;
	}

	public List<BusinessCustomer> getContactPersons() {
		return contactPersons;
	}

	public String getEmergencyContactPersonName() {
		return emergencyContactPersonName;
	}

	public void setEmergencyContactPersonName(String emergencyContactPersonName) {
		this.emergencyContactPersonName = emergencyContactPersonName;
	}

	public void setContactPersons(List<BusinessCustomer> contactPersons) {
		this.contactPersons = contactPersons;
	}

	public String getPhotoId() {
		if(photoId==null){
			photoId="camera";
		}
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public List<String> getShots() {
		return shots;
	}

	public void setShots(List<String> shots) {
		this.shots = shots;
	}

	public String getKeyPress() {
		keyPress = "idFind";
		return keyPress;
	}

	public void setKeyPress(String keyPress) {
		this.keyPress = keyPress;
	}

	public String getSearchProvince() {
		return searchProvince;
	}

	public void setSearchProvince(String searchProvince) {
		this.searchProvince = searchProvince;
	}

	public String getSearchMunicipal() {
		return searchMunicipal;
	}

	public void setSearchMunicipal(String searchMunicipal) {
		this.searchMunicipal = searchMunicipal;
	}

	public String getSearchBarangay() {
		return searchBarangay;
	}

	public void setSearchBarangay(String searchBarangay) {
		this.searchBarangay = searchBarangay;
	}

	public List<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces = provinces;
	}

	public List<Municipality> getMunicipals() {
		return municipals;
	}

	public void setMunicipals(List<Municipality> municipals) {
		this.municipals = municipals;
	}

	public List<Barangay> getBarangays() {
		return barangays;
	}

	public void setBarangays(List<Barangay> barangays) {
		this.barangays = barangays;
	}

	public Province getProvinceSelected() {
		return provinceSelected;
	}

	public void setProvinceSelected(Province provinceSelected) {
		this.provinceSelected = provinceSelected;
	}

	public Municipality getMunicipalSelected() {
		return municipalSelected;
	}

	public void setMunicipalSelected(Municipality municipalSelected) {
		this.municipalSelected = municipalSelected;
	}

	public Barangay getBarangaySelected() {
		return barangaySelected;
	}

	public void setBarangaySelected(Barangay barangaySelected) {
		this.barangaySelected = barangaySelected;
	}

	public Purok getPurokSelected() {
		return purokSelected;
	}

	public void setPurokSelected(Purok purokSelected) {
		this.purokSelected = purokSelected;
	}

	public String getSearchPurok() {
		return searchPurok;
	}

	public void setSearchPurok(String searchPurok) {
		this.searchPurok = searchPurok;
	}

	public List<Purok> getPuroks() {
		return puroks;
	}

	public void setPuroks(List<Purok> puroks) {
		this.puroks = puroks;
	}

	public boolean isEnableAdditionalButton() {
		if(getCustomer()!=null){
			enableAdditionalButton = false;
		}else{
			enableAdditionalButton = true;
		}
		return enableAdditionalButton;
	}

	public void setEnableAdditionalButton(boolean enableAdditionalButton) {
		this.enableAdditionalButton = enableAdditionalButton;
	}

	public boolean getMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public boolean getFemale() {
		return female;
	}

	public void setFemale(boolean female) {
		this.female = female;
	}

	public long getPurokId() {
		return purokId;
	}

	public void setPurokId(long purokId) {
		this.purokId = purokId;
	}

	public List getPurokList() {
		//list loaded in loadDefaultAddress();
		try{
		String[] params = new String[2];
		String sql = " AND pur.isactivepurok=1 AND mun.munid=? AND bgy.bgid=? ORDER BY pur.purokname";
		params[0] = getMunicipalSelected().getId()+"";
		params[1] = getBarangaySelected().getId()+"";
		
		purokList = Collections.synchronizedList(new ArrayList<Purok>());
		purokList.add(new SelectItem(0, "All Purok"));
		for(Purok p : Purok.retrieve(sql, params)){
			purokList.add(new SelectItem(p.getId(), p.getPurokName()));
		}
		}catch(Exception e){}
		
		return purokList;
	}
	
	public void setPurokList(List purokList) {
		this.purokList = purokList;
	}

	public boolean getFilteredBarangay() {
		return filteredBarangay;
	}

	public void setFilteredBarangay(boolean filteredBarangay) {
		this.filteredBarangay = filteredBarangay;
	}

	public String getSignature() {
		
		/**
		 * <p:signature style="width:250px;height:100px" value="#{customerBean.signature}" guideline="true" guidelineOffset="15" guidelineIndent="20" guidelineColor="black" />
		 */
		
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getBornplace() {
		return bornplace;
	}

	public void setBornplace(String bornplace) {
		this.bornplace = bornplace;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getCitizenship() {
		if(citizenship==null) {
			citizenship = "Filipino";
		}
		return citizenship;
	}

	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}

	public String getRenderMethod() {
		return renderMethod;
	}

	public void setRenderMethod(String renderMethod) {
		this.renderMethod = renderMethod;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public List<BusinessCustomer> getSelectedQRCode() {
		return selectedQRCode;
	}

	public void setSelectedQRCode(List<BusinessCustomer> selectedQRCode) {
		this.selectedQRCode = selectedQRCode;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public String getNationalId() {
		return nationalId;
	}

	public void setNationalId(String nationalId) {
		this.nationalId = nationalId;
	}
}