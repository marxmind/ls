package com.italia.municipality.lakesebu.bean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import com.italia.municipality.lakesebu.controller.Form11Report;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.xml.RCDFormDetails;
import com.italia.municipality.lakesebu.xml.RCDFormSeries;
import com.italia.municipality.lakesebu.xml.RCDReader;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/20/2019
 *
 */
//Former Name UploadFormRcdBean
@Named
@RequestScoped
public class RcdBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 15665768734L;
	private static final String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
	private static final String REPORT_NAME ="rcd";
	private StreamedContent tempPdfFile; 
	
	private static final  String UPLOAD_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + "upload" + AppConf.SEPERATOR.getValue(); 
			
	
	@PostConstruct
	public void init() {
		System.out.println("RCD upload init>>>>");
	}
	
	public void uploadFile(FileUploadEvent event){
		
		 try {
			 InputStream stream = event.getFile().getInputStream();
			 String file = event.getFile().getFileName();
			 
			 if(writeDocToFile(stream)){
				 Application.addMessage(1, "Success", "Data has been successfully uploaded");
			 }else{
				 Application.addMessage(3, "Error", "Error uploading the data " + file);
			 }
			 
	     } catch (Exception e) {
	     
	     }
		
	}
	
	private boolean writeDocToFile(InputStream stream){
		try{
			String filename = "uploaded-" + DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + ".xml";	
			System.out.println("writing... writeDocToFile : " + filename);
			File fileDoc = new File(UPLOAD_PATH +  filename);
			Path file = fileDoc.toPath();
			Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
			readingXML(UPLOAD_PATH +  filename);
			return true;
		}catch(IOException e) {}
		return false;
	}
	
	public void readingXML(String xmlFile) {
		
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		List<Form11Report> reports = Collections.synchronizedList(new ArrayList<Form11Report>());
		Form11Report rpt = new Form11Report();
		reports.add(rpt);
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(reports);
  		HashMap param = new HashMap();
  		
  		RCDReader xml = RCDReader.readXML(xmlFile,false);
  		
  		param.put("PARAM_FUND",xml.getFund());
  		param.put("PARAM_COLLECTOR_NAME",xml.getAccountablePerson());
  		param.put("PARAM_RPT_GROUP",xml.getSeriesReport());
  		
  		param.put("PARAM_PRINTED_DATE", xml.getDateCreated());
  		param.put("PARAM_VERIFIED_DATE", xml.getDateVerified());
  		param.put("PARAM_VERIFIED_PERSON", xml.getVerifierPerson());
  		param.put("PARAM_TREASURER", xml.getTreasurer());
  		
  		int cnt = 1;
  		for(RCDFormDetails d : xml.getRcdFormDtls()) {
  			param.put("PARAM_T"+cnt,d.getName());
	  		param.put("PARAM_FROM"+cnt,d.getSeriesFrom());
			param.put("PARAM_TO"+cnt,d.getSeriesTo());
			try{param.put("PARAM_A"+cnt,d.getAmount().replace("?", ""));}catch(NullPointerException e) {param.put("PARAM_A"+cnt,"");}
			cnt++;
  		}
		
  		cnt = 1;
		for(RCDFormSeries frm : xml.getRcdFormSeries()) {
			param.put("PARAM_F"+cnt,frm.getName());
			
	  		param.put("PARAM_BQ"+cnt,frm.getBeginningQty());
	  		param.put("PARAM_BF"+cnt,frm.getBeginningFrom());
	  		param.put("PARAM_BT"+cnt,frm.getBeginningTo());
	  		
	  		param.put("PARAM_RQ"+cnt,frm.getReceiptQty());
	  		param.put("PARAM_RF"+cnt,frm.getReceiptFrom());
	  		param.put("PARAM_RT"+cnt,frm.getReceiptTo());
	  		
	  		param.put("PARAM_IQ"+cnt,frm.getIssuedQty());
	  		param.put("PARAM_IF"+cnt,frm.getIssuedFrom());
	  		param.put("PARAM_IT"+cnt,frm.getIssuedTo());
	  		
	  		param.put("PARAM_EQ"+cnt,frm.getEndingQty());
	  		param.put("PARAM_EF"+cnt,frm.getEndingFrom());
	  		param.put("PARAM_ET"+cnt,frm.getEndingTo());
	  		cnt++;
		}
  		
  		
  		try{param.put("PARAM_TOTAL",xml.getAddAmount().replace("?", ""));}catch(NullPointerException e) {param.put("PARAM_TOTAL","");}
  		
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
	  	    
	  	    String pdfFile = REPORT_NAME + ".pdf";
			 
			 tempPdfFile = DefaultStreamedContent.builder()
					 .contentType("application/pdf")
					 .name(pdfFile)
					 .stream(()-> this.getClass().getResourceAsStream(REPORT_PATH + pdfFile))
					 .build();
			 
		  	    PrimeFaces pm = PrimeFaces.current();
		  	    pm.executeScript("displayPDF();");
	  	}catch(Exception e){e.printStackTrace();}
	}
	
	public String generateRandomIdForNotCaching() {
		return java.util.UUID.randomUUID().toString();
	}
	
	public StreamedContent getTempPdfFile() throws IOException {
		
		if(tempPdfFile==null) {
			
		    File pdfFile = new File(REPORT_PATH + REPORT_NAME + ".pdf");
		    return DefaultStreamedContent.builder()
					 .contentType("application/pdf")
					 .name(REPORT_NAME + ".pdf")
					 .stream(()-> {
						try {
							return new FileInputStream(pdfFile);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
					})
					 .build();
		}else {
			return tempPdfFile;
		}
	  }
	public void setTempPdfFile(StreamedContent tempPdfFile) {
		this.tempPdfFile = tempPdfFile;
	}
}