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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.primefaces.event.FileUploadEvent;
import com.italia.municipality.lakesebu.controller.Card;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.EmployeeLoan;
import com.italia.municipality.lakesebu.controller.EmployeeMain;
import com.italia.municipality.lakesebu.controller.ID;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.EmployeeType;
import com.italia.municipality.lakesebu.enm.Gender;
import com.italia.municipality.lakesebu.enm.RateType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.LguId;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.FileConverter;
import com.italia.municipality.lakesebu.utils.QRCodeUtil;
import com.italia.municipality.lakesebu.utils.SignatureImageConverter;
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

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 03/29/2022
 *
 */
@Named
@ViewScoped
@Data
public class EmployeeMainBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 14564678754534L;

	private EmployeeMain employee;
	private List types;
	private int typeId;
	private List genders;
	private int genderId;
	private List civils;
	private int civilId;
	private List deps;
	private int depId;
	private String searchParam;
	private List<EmployeeMain> employees;
	private List rateTypes;
	private int rateId;
	private List taxables;
	private List<String> shots = new ArrayList<>();
	private String photoId="employee";
	private List<EmployeeLoan> loans;
	private EmployeeLoan loan;
	private List<Card> cards;
	private Card card;
	private List loanNames;
	private List<ID> ids;
	private ID idData;
	private Date issuedDate;
	private Date validDate;
	private String mayor;
	
	@PostConstruct
	public void init() {
		defaultValue();
		loadData();
	}
	
	private void convertSignatureToPng(EmployeeMain employee) {
		if(employee.getSignatureid()!=null && !employee.getSignatureid().isEmpty()) {
			File file = new File(GlobalVar.EMPLOYEE_IMAGE_PATH_SIG);
			file.mkdir(); //create file for directory
			try {
				OutputStream out = new FileOutputStream(GlobalVar.EMPLOYEE_IMAGE_PATH_SIG + employee.getEmployeeId() + ".png");
				SignatureImageConverter.generateSignature(employee.getSignatureid(), out, 400, 200, "png");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			File dir = new File(GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO);
			dir.mkdir();
			File fileImg = new File(GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO + photoId + ".jpg");
			System.out.println(photoId + ".jpg");
			if(image==null){
				fileImg = new File(GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO + "noimageproduct.jpg");
				 image = ImageIO.read(fileImg);
			}
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String contextImageLoc = "resources" + File.separator + "images" + File.separator + "employee" + File.separator;
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
	
	private String getRandomImageName() {
        int i = (int) (Math.random() * 10000000);
        return String.valueOf(i);
    }
	
	private String copyPhoto(String photoId){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String driveImage =  GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO + photoId + ".jpg";
        setPhotoId(photoId);
        String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "employee" + File.separator;
            String pathToSave = externalContext.getRealPath("") + contextImageLoc;
            File file = new File(driveImage);
            try{
    			Files.copy(file.toPath(), (new File(pathToSave + file.getName())).toPath(),
    			        StandardCopyOption.REPLACE_EXISTING);
    			}catch(IOException e){}
            return driveImage;
	}
	
	public void defaultValue() {
		
		taxables = new ArrayList<>();
		taxables.add(new SelectItem(0, "None"));
		taxables.add(new SelectItem(1, "Whole Year Paid"));
		
		types = new ArrayList<>();
		typeId = 1;
		for(EmployeeType t : EmployeeType.values()) {
			types.add(new SelectItem(t.getId(), t.getName()));
		}
		
		rateTypes = new ArrayList<>();
		//rateTypes.add(new SelectItem(0, "Select Type"));
		for(RateType t : RateType.values()) {
			rateTypes.add(new SelectItem(t.getId(), t.getName()));
		}
		
		genders = new ArrayList<>();
		genderId = 1;
		for(Gender g : Gender.values()) {
			genders.add(new SelectItem(g.getId(), g.getName()));
		}
		
		civils = new ArrayList<>();
		civilId = 1;
		for(CivilStatus c : CivilStatus.values()) {
			civils.add(new SelectItem(c.getId(), c.getName()));
		}
		
		deps = new ArrayList<>();
		depId = 1;
		for(Department d : Department.retrieve("SELECT * FROM department WHERE isactivedep=1 ORDER BY departmentname ASC", new String[0])) {
			deps.add(new SelectItem(d.getDepid(), d.getDepartmentName()));
		}
		
		addDefaultEmployee();
	}
	
	private void addDefaultEmployee() {
		
		setPhotoId("employee");
		setDepId(1);
		setCivilId(1);
		
		EmployeeType type = EmployeeType.value(getTypeId());
		
		EmployeeMain emp = EmployeeMain.builder()
				.employeeId(EmployeeMain.getLatestEmloyeeId(type))
				.regDate(DateUtils.getCurrentDateYYYYMMDD())
				.tempRegDate(DateUtils.getDateToday())
				.tempResDate(DateUtils.getDateToday())
				.firstName(null)
				.middleName(null)
				.lastName(null)
				.fullName("")
				.birthDate(DateUtils.getCurrentDateYYYYMMDD())
				.tempBirthDate(DateUtils.getDateToday())
				.civilStatus(1)
				.position("")
				.department(Department.department("1"))
				.cctsId("")
				.employeeType(getTypeId())
				.address("")
				.gender(1)
				.contactNo("")
				.signatureid("")
				.isResigned(0)
				.isActiveEmployee(1)
				.emergecnyContactDtls("")
				.photoid("employee")
				.build();
		
		setEmployee(emp);
		
		
		List completes = new ArrayList<>();
		completes.add(new SelectItem(0, "No Loan"));
		completes.add(new SelectItem(1, "In-Flight"));
		completes.add(new SelectItem(2, "Completed"));
		EmployeeLoan ln = EmployeeLoan.builder()
				.tempApprovedDate(new Date())
				.completes(completes)
				.build();
		setLoan(ln);
		
		card = Card.builder()
				.name(null)
				.number(null)
				.tempDateFrom(new Date())
				.tempDateTo(new Date())
				.isActive(1)
				.build();
		
		
		idData = ID.builder()
				.tempIssued(new Date())
				.tempValid(new Date())
				.mayor("HON. FLORO S. GANDAM")
				.build();
		
		loans = new ArrayList<EmployeeLoan>();
		cards = new ArrayList<Card>();
		ids = new ArrayList<ID>();
		
		loanNames = new ArrayList<>();
		loanNames.add(new SelectItem("EE", "EE"));
		loanNames.add(new SelectItem("LOAN", "LOAN"));
		loanNames.add(new SelectItem("COOP", "COOP"));
		//loanNames.add(new SelectItem("OTHERS", "OTHERS"));
	}
	
	public void loadData() {
		employees = new ArrayList<EmployeeMain>();
		String sql = "";
		String[] params = new String[0];
		
		if(getSearchParam()!=null && !getSearchParam().isBlank() && getSearchParam().length()>3) {
			sql = " AND emp.fullname like '%"+ getSearchParam() +"%'";
		}
		
		sql += " ORDER BY emp.eid DESC";
		
		employees = EmployeeMain.retrieve(sql, params);
		
		if(employees!=null && employees.size()==1) {
			clickItem(employees.get(0));
		}
	}
	
	
	public void updateEmployeeIDSeries() {
		getEmployee().setEmployeeId(EmployeeMain.getLatestEmloyeeId(EmployeeType.value(getTypeId())));
	}
	
	public void clear() {
		setPhotoId("employee");
		setEmployee(null);
		setLoan(null);
		setCard(null);
		setIdData(null);
		addDefaultEmployee();
		setTypeId(1);
	}
	
	public void clickItem(EmployeeMain em) {
		setCivilId(em.getCivilStatus());
		setDepId(em.getDepartment().getDepid());
		em.setTempRegDate(DateUtils.convertDateString(em.getRegDate(), "yyyy-MM-dd"));
		em.setTempBirthDate(DateUtils.convertDateString(em.getBirthDate(), "yyyy-MM-dd"));
		setEmployee(em);
		setTypeId(em.getEmployeeType());
		if(em.getPhotoid()!=null) {
			copyPhoto(em.getPhotoid());
		}
		
		loadLoans(em);
		loadCards(em);
		loadID(em);
	}
	
	private void loadLoans(EmployeeMain em) {
		loans = EmployeeLoan.retrieve(" AND emp.eid="+em.getId(), new String[0]);
	}
	
	private void loadCards(EmployeeMain em) {
		cards = Card.retrieve(" AND emp.eid="+em.getId(), new String[0]);
	}
	
	private void loadID(EmployeeMain em) {
		ids = ID.retrieve(" AND emp.eid="+em.getId(), new String[0]);
	}
	
	public void delete(EmployeeMain em) {
		em.delete();
		loadData();
		clear();
		Application.addMessage(1, "Success", "Successfully deleted.");
	}
	
	public void save() {
		boolean isOk = true;
		EmployeeMain em = getEmployee();
		
		if(em.getFirstName()==null | em.getFirstName().isBlank()) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in first name");
		}
		if(em.getMiddleName()==null || em.getMiddleName().isBlank()) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in middle name");
		}
		if(em.getLastName()==null || em.getLastName().isBlank()) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in last name");
		}
		if(em.getAddress()==null || em.getAddress().isBlank()) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in address");
		}
		if(em.getPosition()==null || em.getPosition().isBlank()) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in position");
		}
		if(em.getEmergecnyContactDtls()==null) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in emergency contact details");
		}
		if(em.getContactNo()==null) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in contact details");
		}
		
		if(em.getRate()==0) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in salary details");
		}
		
		if(em.getRateType()==0) {
			isOk=false;
			Application.addMessage(2, "Error", "Please fill in salary type details");
		}
		
		boolean hasLoan = false;
		if(getLoan()!=null && getLoan().getLoanAmount()>0) {
			
			if(getLoan().getMonthlyDeduction()==0) {
				isOk=false;
				Application.addMessage(2, "Error", "Pleas add montly deduction");
			}
			
			if(getLoan().getIsCompleted()==0) {
				isOk=false;
				Application.addMessage(2, "Error", "Pleas fill in flight or completed");
			}
			if(isOk) {
				hasLoan = true;
			}
		}
		
		boolean hasCard=false;
		if(getCard()!=null) {
			if(!getCard().getName().isBlank() && getCard().getNumber().isBlank()) {
				isOk=false;
				Application.addMessage(2, "Error", "Pleas fill card number in required field");
			}
			if(getCard().getName().isBlank() && !getCard().getNumber().isBlank()) {
				isOk=false;
				Application.addMessage(2, "Error", "Pleas fill card name in required field");
			}
			
			if(!getCard().getName().isBlank() && !getCard().getNumber().isBlank()) {
				
				System.out.println("getCard().getName():" + (getCard().getName().isBlank()? "blank" : "not"));
				System.out.println("getCard().getNumber():" + (getCard().getNumber().isBlank()? "blank" : "not"));
				hasCard=true;
			}
			
		}
			
		if(isOk) {	
			em.setRegDate(DateUtils.convertDate(em.getTempRegDate(), "yyyy-MM-dd"));
			em.setDateResigned(DateUtils.convertDate(em.getTempResDate(), "yyyy-MM-dd"));
			em.setEmployeeType(getTypeId());
			em.setBirthDate(DateUtils.convertDate(em.getTempBirthDate(), "yyyy-MM-dd"));
			em.setCivilStatus(getCivilId());
			em.setGender(getGenderId());
			em.setFullName(em.getLastName() + ", " + em.getFirstName() + " " + em.getMiddleName());
			em.setPhotoid(getPhotoId());
			
			Department dep = new Department();
			dep.setDepid(getDepId());
			em.setDepartment(dep);
			em = EmployeeMain.save(em);
			
			if(hasLoan) {
				loan.setEmployeeMain(em);
				loan.setApprovedDate(DateUtils.convertDate(loan.getTempApprovedDate(), "yyyy-MM-dd"));
				loan.setIsActive(1);
				loan.save();
				loadLoans(em);
			}
			
			if(hasCard) {
				card.setValidFrom(DateUtils.convertDate(card.getTempDateFrom(), "yyyy-MM-dd"));
				card.setValidTo(DateUtils.convertDate(card.getTempDateTo(), "yyyy-MM-dd"));
				card.setEmployee(em);
				card.setIsActive(1);
				card.save();
				loadCards(em);
			}
			
			
			addDefaultEmployee();
			loadData();
			Application.addMessage(1, "Success", "Successfully saved.");
			
			convertSignatureToPng(em);
		}
	}
	
	
	
	/////loan function
	public void clickLoan(EmployeeLoan ln) {
		System.out.println(ln.getIsCompleted()==1? "In-flight":"Completed");
		loan.setId(ln.getId());
		loan.setTempApprovedDate(DateUtils.convertDateString(ln.getApprovedDate(),"yyyy-MM-dd"));
		loan.setApprovedDate(ln.getApprovedDate());
		loan.setName(ln.getName());
		loan.setIsActive(1);
		loan.setIsCompleted(ln.getIsCompleted());
		loan.setLoanAmount(ln.getLoanAmount());
		loan.setMonthlyDeduction(ln.getMonthlyDeduction());
	}
	public void deleteLoanRow(EmployeeLoan ln) {
		ln.delete();
		loadLoans(ln.getEmployeeMain());
		Application.addMessage(1,"Success", "Successfully deleted");
	}
	/////card function
	public void clickCard(Card cd) {
		card = cd;
		card.setTempDateFrom(DateUtils.convertDateString(cd.getValidFrom(), "yyyy-MM-dd"));
		card.setTempDateTo(DateUtils.convertDateString(cd.getValidTo(), "yyyy-MM-dd"));
	}
	public void deleteCardRow(Card cd) {
		cd.delete();
		loadCards(cd.getEmployee());
		Application.addMessage(1,"Success", "Successfully deleted");
	}
	
	public void deleteID(ID id) {
		id.delete();
		Application.addMessage(1,"Success", "Successfully deleted");
		loadID(id.getEmployeeMain());
	}
	
	public void clickID(ID id) {
		id.setTempIssued(DateUtils.convertDateString(id.getIssued(), "yyyy-MM-dd"));
		id.setTempValid(DateUtils.convertDateString(id.getValid(), "yyyy-MM-dd"));
		setIdData(id);
	}
	
	public void clearID() {
		setIdData(ID.builder().tempIssued(new Date()).tempValid(new Date()).mayor("HON. FLORO S. GANDAM").build());
	}
	public void saveID() {
		if(getEmployee()!=null && !getEmployee().getFirstName().isEmpty()) {
			ID id = getIdData();
			   id.setIssued(DateUtils.convertDate(id.getTempIssued(), "yyyy-MM-dd"));
			   id.setValid(DateUtils.convertDate(id.getTempValid(), "yyyy-MM-dd"));
			   id.setIsActive(1);
			   id.setEmployeeMain(getEmployee());
			   id.save();
			   loadID(getEmployee());
			   saveQRCode(getEmployee());
			   Application.addMessage(1,"Success", "Successfully saved.");
		}else {
			Application.addMessage(3,"Error", "Please select employee or create employee first");
		}
	}
	
	public static void saveQRCode(EmployeeMain e) {
		String path = GlobalVar.EMPLOYEE_IMAGE_PATH_QRCODE;
		File qrcodeDir = new File(path);
		qrcodeDir.mkdir();
		try {
			//OutputStream outputStream = new FileOutputStream(path + e.getEmployeeId() + ".png");
			//QRCode.from(e.getEmployeeId()).to(ImageType.PNG).withSize(400, 400).writeTo(outputStream);
			//outputStream.close();
			
			QRCodeUtil.createQRCode(e.getEmployeeId(), 400, 400, path, e.getEmployeeId()+".png");
			
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void printId(ID idCard) {
		
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = GlobalVar.EMPLOYEE_ID;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		EmployeeMain e = idCard.getEmployeeMain();
		e.setDepartment(Department.department(e.getDepartment().getDepid()+""));
		
		String officialLogo = REPORT_PATH + "logo.png";
		String mayorSig = REPORT_PATH + "mayor.png";
		String employeePic = GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO + e.getPhotoid() + ".jpg";
		String empSig =  GlobalVar.EMPLOYEE_IMAGE_PATH_SIG + e.getEmployeeId() + ".png";
		String qrcodePic = GlobalVar.EMPLOYEE_IMAGE_PATH_QRCODE + e.getEmployeeId() + ".png";
		String bigBgPic = GlobalVar.REPORT_FOLDER + "frame-big.png";
		String provPic = GlobalVar.REPORT_FOLDER + "prov-logo.png";
		String frontPic = GlobalVar.REPORT_FOLDER + "front-card.png";
		FileInputStream logo = null;
		FileInputStream mayorSignature = null;
		FileInputStream employee = null;
		FileInputStream employeeSinature = null;
		FileInputStream qrcode = null;
		FileInputStream bgBig = null;
		FileInputStream provLogo = null;
		FileInputStream frontCard = null;
		try{
		File fileLogo = new File(officialLogo);
		logo = new FileInputStream(fileLogo);
		File fileMayorSig = new File(mayorSig);
		mayorSignature = new FileInputStream(fileMayorSig);
		employee = new FileInputStream(employeePic);
		employeeSinature = new FileInputStream(empSig);
		qrcode = new FileInputStream(qrcodePic);
		bgBig = new FileInputStream(bigBgPic);
		provLogo = new FileInputStream(provPic);
		frontCard = new FileInputStream(frontPic);
		}catch(Exception er){er.printStackTrace();}
		
		
		List<LguId> rpts = new ArrayList<LguId>();
		List<Card> cards = Card.retrieve(" AND emp.eid="+e.getId(), new String[0]);
		LguId lg =LguId.builder()
				.idNumber(e.getEmployeeId())
				.provinceName("Provine of South Cotabato")
				.municipalityName("MUNICIPALITY OF LAKE SEBU")
				.departmentName(e.getDepartment().getDepartmentName().toUpperCase())
				.employeeName(e.getFullName())
				.desiganation(e.getPosition())
				.dateIssued(DateUtils.convertDateToMonthDayYear(idCard.getIssued()))
				.validUntil(DateUtils.convertDateToMonthDayYear(idCard.getValid()))
				.lguLogo(logo)
				.mayorSignature(mayorSignature)
				.mayorName(idCard.getMayor().toUpperCase())
				.address(e.getAddress())
				.contactNo(e.getContactNo())
				.birthDate(e.getBirthDate())
				.civilStatus(CivilStatus.typeName(e.getCivilStatus()))
				.bloodType(e.getBloodType())
				//.card1("TIN No : ")
				//.card2("SSS : ")
				//.card3("GSIS No : ")
				//.card4("PAG-IBIG No : ")
				//.card5("PHILHEALTH NO : ")
				//.card6("Health Card No : ")
				.emergencyContactDtls(e.getEmergecnyContactDtls())
				.employeePicture(employee)
				.employeeSignature(employeeSinature)
				.qrcode(qrcode)
				.bgBig(bgBig)
				.provLogo(provLogo)
				.idBgFront(frontCard)
				.idBgBack(frontCard)
				.build();
		
			int cnt = 1;
			for(Card l : cards) {
				switch(cnt) {
					case 1: lg.setCard1(l.getName().toUpperCase() + " No.: " + l.getNumber()); break;
					case 2: lg.setCard2(l.getName().toUpperCase() + " No.: " + l.getNumber()); break;
					case 3: lg.setCard3(l.getName().toUpperCase() + " No.: " + l.getNumber()); break;
					case 4: lg.setCard4(l.getName().toUpperCase() + " No.: " + l.getNumber()); break;
					case 5: lg.setCard5(l.getName().toUpperCase() + " No.: " + l.getNumber()); break;
					case 6: lg.setCard6(l.getName().toUpperCase() + " No.: " + l.getNumber()); break;
				}
				cnt++;
			}
		
			
		
			rpts.add(lg);
			
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rpts);
  		HashMap param = new HashMap();
  		
  		String pathID = GlobalVar.EMPLOYEE_ID_FOLDER;
  		File idDir = new File(pathID);
  		idDir.mkdir();
  		
  		REPORT_NAME = idCard.getEmployeeMain().getFullName();//change pdf name to employee name
  		REPORT_PATH = pathID; //change pdf file location to ID location
  		
  		
  		
  		try{
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
	  	}catch(Exception es){es.printStackTrace();}
  		
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
		          //create image type
		      		FileConverter.convert(REPORT_PATH, REPORT_NAME, REPORT_PATH, "jpg");
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


