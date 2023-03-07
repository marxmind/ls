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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.DateFormat;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.BusinessORTransaction;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.licensing.controller.ClearanceRpt;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.licensing.controller.DocumentPrinting;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
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
 * @since 11/04/2021
 * @version 1.0
 *
 *
 */
@Named
@ViewScoped
public class BizLookupBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 546476576875641L;
	@Setter @Getter private List<BusinessPermit> pmts;
	@Setter @Getter private Date calendarFrom;
	@Setter @Getter private Date calendarTo;
	@Setter @Getter private String searchName;
	private String BARANGAY = "Poblacion";
	private String MUNICIPALITY = "Lake Sebu";
	private String PROVINCE = "South Cotabato";
	private static final String REPORT_PATH = ReadConfig.value(AppConf.REPORT_FOLDER);
	private static final String BUSINESS_REPORT_PERMIT = DocumentFormatter.getTagName("v6_business-permit");
	
	public void openLookup() {
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgBizLookup').show(1000);");
	}
	
	public void loadSearch() {
		pmts = new ArrayList<BusinessPermit>();
		String sql = " AND bz.isactivebusiness=1";
		//String sql = " AND bz.isactivebusiness=1 AND (bz.datetrans>=? AND bz.datetrans<=?) ";
		String[] params = new String[0];
		//params[0] = DateUtils.convertDate(getCalendarFrom(), DateFormat.YYYY_MM_DD());
		//params[1] = DateUtils.convertDate(getCalendarTo(), DateFormat.YYYY_MM_DD());
		
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
		
		Collections.reverse(pmts);
	}
	
	public void printPermit(BusinessPermit permit) {
		
		System.out.println("Business: " + permit.getBusinessName());
	
		String REPORT_NAME = BUSINESS_REPORT_PERMIT;
		
		HashMap param = new HashMap();
		String path = REPORT_PATH;
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, path);
		
		List<ClearanceRpt> reports = Collections.synchronizedList(new ArrayList<ClearanceRpt>());
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
				name = permit.getCustomer().getFirstname().replace(".", "") + " " + permit.getCustomer().getLastname().replace(".", "");
			}else {
				name = permit.getCustomer().getFirstname() + " " + middleName.substring(0, 1) + ". " + permit.getCustomer().getLastname();
			}
			
		}catch(Exception e) {}
		
		param.put("PARAM_REQUESTOR_NAME", name);
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
		//Map<Double, ORTransaction> unSort = Collections.synchronizedMap(new HashMap<Double, ORTransaction>());
		Map<String, BusinessORTransaction> unSort = Collections.synchronizedMap(new HashMap<String, BusinessORTransaction>());
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
		List<String> lData = Collections.synchronizedList(new ArrayList<String>());
		boolean hasFire=false;
		for(BusinessORTransaction or : sortedOR.values()) {
			String fire = or.getPurpose().toLowerCase().trim();
			System.out.println("Start Check " + or.getPurpose() + " php " + or.getAmount());
			if(!"fire".equalsIgnoreCase(fire.trim())) {//do not include fire
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
		}
		
		//Collections.reverse(lData);
		String eng = "";
		int size = lData.size();
		int cnt = 1;
		for(String d : lData) {
			if(!"brp".equalsIgnoreCase(d.trim())) {
				if(cnt==1) {
					eng = d;
					System.out.println("1 " + eng);
				}else {
				
					if(cnt==size) {
						eng += " & " + d;
						System.out.println("cnt == size " + size + "=" + eng);
					}else {
						eng += ", " + d;
						System.out.println("cnt != size " + size + "=" + eng);
					}
					
				}
				cnt++;
			}
		}
		int countlent = eng!=null? eng.length() : 0;
		//do not change location // this code is after OR
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
