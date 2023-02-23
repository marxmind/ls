package com.italia.municipality.lakesebu.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.primefaces.event.FileUploadEvent;

import com.italia.municipality.lakesebu.controller.Barangay;
import com.italia.municipality.lakesebu.controller.ITaxPayor;
import com.italia.municipality.lakesebu.controller.LandPayor;
import com.italia.municipality.lakesebu.controller.LandType;
import com.italia.municipality.lakesebu.controller.PayorPayment;
import com.italia.municipality.lakesebu.controller.TaxPayor;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 01/23/2019
 *
 */
//former name ReadExtractPayorBean
@Named
@ViewScoped
public class ExtractBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7775454343431L;
	private static final String DOC_PATH = AppConf.PRIMARY_DRIVE.getValue() + File.separator + AppConf.APP_CONFIG_FOLDER_NAME.getValue() + File.separator +"rptsExtractFiles" + File.separator;
	private static final String DOC_FILTER = AppConf.PRIMARY_DRIVE.getValue() + File.separator + AppConf.APP_CONFIG_FOLDER_NAME.getValue() + File.separator +AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + File.separator + "extractFilter.max";
	private static Map<String, Barangay> bars;
	@Getter @Setter private List<PayorPayment> pays;
	@Getter @Setter private List<PayorPayment> holdpays;
	@Getter @Setter private String searchParama;
	
	
	public String[] loadFilter() {
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(DOC_FILTER));
			String[] fls = prop.getProperty("filter").split(",");
			
			String[] filters = new String[fls.length];
			int i=0;
			for(String f : fls) {
				filters[i++] = f;
			}
			return filters;
		}catch(Exception e) {}
		return null;
	}
	
	public void saveTransaction(PayorPayment pay) {
		ITaxPayor payor = null;
		try {
			payor = TaxPayor.retrievePayor(pay.getOwner().trim());
		}catch(Exception e) {}
		
		if(payor!=null) {
			
			LandPayor land = null;
			
			try{land = LandPayor.retrieveLand(pay.getTdNo().trim(), pay.getOwner().trim());}catch(NullPointerException e) {}
			
			if(land==null) {
				land = new LandPayor();
				land.setTaxDeclarionNo(pay.getTdNo());
				land.setPayor(payor);
				land.setAddress(pay.getAddress());
				land.setIsActive(1);
				
				LandType type = new LandType();
				type.setId(1);//agricultural
				
				land.setLandType(type);
				
				UserDtls user = new UserDtls();
				user.setUserdtlsid(1);
				land.setUserDtls(user);
				
				land.save();
			}
			
			
		}else {
			
			
			payor.setFullName(pay.getOwner());
			payor.setAddress(pay.getAddress());
			payor.setIsactive(1);
			
			Barangay bar = new Barangay();
			bar.setId(Integer.valueOf(pay.getBarangay()));
			payor.setBarangay(bar);
			
			payor = TaxPayor.save(payor);
			
			LandPayor land = new LandPayor();
			land.setTaxDeclarionNo(pay.getTdNo());
			land.setPayor(payor);
			land.setAddress(pay.getAddress());
			land.setIsActive(1);
			
			LandType type = new LandType();
			type.setId(1);//agricultural
			
			land.setLandType(type);
			
			UserDtls user = new UserDtls();
			user.setUserdtlsid(1);
			land.setUserDtls(user);
			
			land.save();
			
			
			
		}
		
		
		
	}
	
	public void saveData() {
		
		if(getPays().size()>0) {
			for(PayorPayment pay : getPays()) {
				if(!pay.getTdNo().isBlank() && !pay.getOwner().isBlank()) {
					saveTransaction(pay);
				}
			}
		}
		
	}
	
	public void searchExtract() {
		if(getHoldpays()!=null && getHoldpays().size()>0) {
			if(getSearchParama()!=null && !getSearchParama().isEmpty()) {
				pays = new ArrayList<PayorPayment>();
				for(PayorPayment p : getHoldpays()) {
					if(p.getOwner().contains(getSearchParama().toUpperCase()) || p.getTdNo().contains(getSearchParama())) {
						pays.add(p);
					}
				}
			}
		}
		if(getSearchParama()==null || getSearchParama().isEmpty()) {
			pays = getHoldpays();
		}
	}
	
	public void uploadData(FileUploadEvent event){
		
		 try {
			 InputStream stream = event.getFile().getInputStream();
			 String file = event.getFile().getFileName();
			 loadFilter();
			 if(writeDocToFile(event)){
				 Application.addMessage(1, "Success", "Data has been successfully uploaded");
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
		String filename = "rpts-extract-" + DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "."+fileExt.toLowerCase();
		
		System.out.println("writing... writeDocToFile : " + filename);
		
		File dir = new File(DOC_PATH);
		dir.mkdirs();//create dir if not present
		
		File fileDoc = new File(DOC_PATH +  filename);
		Path file = fileDoc.toPath();
		Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
		//return loadToDatabase(fileDoc);
		readExcel(fileDoc);
		return true;
		}catch(IOException e){return false;}
		
	}
	
	private Map<String, Barangay> loadBarangay() {
		
		for(Barangay b : Barangay.retrieve("SELECT * FROM barangay", new String[0])) {
			bars.put(b.getName().toUpperCase(), b);
			System.out.println("BAR: " + b.getName() + " id " + b.getId());
		}
		
		return bars;
	}
	
	
	public List<PayorPayment> readExcel(File file) {
		loadBarangay();//initialize value of Barangay
		try {
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
		    
		    Map<String, PayorPayment> payorMap = new LinkedHashMap<String, PayorPayment>();
		    String[] filters = loadFilter();
		    for(int r = 1; r < rows; r++) {//start from row 1 instead of row 0 (row zero is the header information on excel)
		        row = sheet.getRow(r);
		        PayorPayment ro = new PayorPayment();
		        if(row != null) {
		            for(int c = 0; c < cols; c++) {
		                cell = row.getCell((short)c);
		                if(cell != null) {
		                    
		                	switch(c) {
		                	case 2 : ro.setTdNo(cell.getStringCellValue());break;
		                	case 7 : ro.setOwner(cell.getStringCellValue());break;
		                	case 8 : ro.setBarangay(cell.getStringCellValue());break;
		                	case 11 : try{ro.setAssessedValue(cell.getNumericCellValue());}catch(Exception e) {}break;
		                	}
		                	
		                }
		            }
		            //pays.add(ro);
		            ro = splitOwnerLot(ro,filters); // add lot number if available
		            payorMap.put(ro.getTdNo(), ro);
		        }
		        
		    }
		    
		    for(PayorPayment pa : payorMap.values()) {
		    	pays.add(pa);
		    	holdpays.add(pa);
		    }
		    
		    return pays;
		} catch(Exception ioe) {
		    ioe.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private PayorPayment splitOwnerLot(PayorPayment pay, String[] filters) {
		
		try {
			String[] val = new String[2];
			
			for(String fil : filters) {
				if(pay.getOwner().contains(fil)){
					val = pay.getOwner().split(fil);
					pay.setOwner(val[0].trim());
					try{
						pay.setLotNo(val[1]);}catch(IndexOutOfBoundsException ie) {
						pay.setLotNo(fil);
					}
				}	
			}
			
			
			String key = pay.getBarangay().toUpperCase();
			if(getBars().containsKey(key)) {
				pay.setAddress(key + ", LAKE SEBU, SO. COT.");
				pay.setBarangay(getBars().get(key).getId()+"");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return pay;
	}
	public Map<String, Barangay> getBars() {
		return bars;
	}

	public void setBars(Map<String, Barangay> bars) {
		this.bars = bars;
	}
}
