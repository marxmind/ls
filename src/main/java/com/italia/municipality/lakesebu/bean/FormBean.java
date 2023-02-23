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
import java.util.Locale;

import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Form11Report;
import com.italia.municipality.lakesebu.controller.IssuedForm;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.Stocks;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.enm.StockStatus;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
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
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/**
 * 
 * @author Mark Italia
 * @since 08-11-2018
 * @version 1.0
 *
 */
@Named
@ViewScoped
public class FormBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8654686645551L;
	
	
	private Date issuedDate;
	
	private int formTypeId;
	private List formTypes;
	
	private long beginningNo;
	private long endingNo;
	private int quantity;
	
	private int collectorId;
	private List collectors;
	
	private int statusId;
	private List status;
	
	private IssuedForm selectedForm;
	
	private List<IssuedForm> forms;
	
	private List<Stocks> stocks;
	private int formTypeIdSearch;
	private List formTypeSearch;
	
	private Stocks stockData;
	
	private int fundId;
	private List funds;
	
	private int collectorMapId;
	private List collectorsMap;
	private int fundSearchId;
	private List fundsSearch;
	
	private int monthId;
	private List months;
	private List<Form11Report> seriesForm;
	private String tabNow="Issued Forms";
	
	@PostConstruct
	public void init() {
		
		forms = new ArrayList<IssuedForm>();
		
		String sql = " AND frm.formstatus=1  AND frm.isactivelog=1 ";
		
		if(getCollectorMapId()>0) {
			sql +=" AND cl.isid=?";
		}else {
			sql +=" AND cl.isid!=?";
		}
		
		if(getFundSearchId()>0) {
			sql +=" AND frm.fundid=?";
		}else {
			sql +=" AND frm.fundid!=?";
		}
		String[] params = new String[2];
		params[0] = getCollectorMapId()+"";
		params[1] = getFundSearchId()+"";
		
		forms =  IssuedForm.retrieve(sql, params);
		Collections.reverse(forms);
		
	}
	
	
	public void loadData() {
		System.out.println("Check tabnow >> " + getTabNow());
		System.out.println("collector >> " + getCollectorMapId());
		if("Issued Forms".equalsIgnoreCase(getTabNow())) {
			init();
		}else if("Form Series".equalsIgnoreCase(getTabNow())){
			loadSeries();
		}
	}
	 
	
	public void onChange(TabChangeEvent event) {
		//Tab activeTab = event.getTab();
		//...
		if("Issued Forms".equalsIgnoreCase(event.getTab().getTitle())) {
			setTabNow("Issued Forms");
			init();
		}else if("Form Series".equalsIgnoreCase(event.getTab().getTitle())){
			setTabNow("Form Series");
			loadSeries();
		}
	}
	
	public void loadSeries() {
		seriesForm = new ArrayList<Form11Report>();//Collections.synchronizedList(new ArrayList<Form11Report>());
		for(FormType form : FormType.values()) {
			if(FormType.CT_2.getId()==form.getId() || FormType.CT_5.getId()==form.getId()) {
				//do nothing for now
			}else {	
			//sql = " AND frm.formstatus="+ FormStatus.HANDED.getId() +" AND frm.formtypelog="+form.getId();
			String sql = " AND frm.formtypelog=? AND frm.formstatus!=" + FormStatus.CANCELLED.getId();
			if(getFundSearchId()>0) {
				sql += " AND frm.fundid=" + getFundSearchId();
			}
			
			if(getCollectorMapId()>0) {
				sql += " AND cl.isid=" + getCollectorMapId();
			}
			String[] params = new String[1];
			params[0] = form.getId()+"";
			for(IssuedForm is : IssuedForm.retrieve(sql, params)) {
				
				sql = " AND (frm.receiveddate>=? AND frm.receiveddate<=?) AND sud.logid=?";
				
				/*if(getFundSearchId()>0) {
					sql += " AND frm.fundid=" + getFundSearchId();
				}
				
				if(getCollectorMapId()>0) {
					sql += " AND cl.isid=" + getCollectorMapId();
				}*/
				
				params = new String[3];
				params[0] = DateUtils.getCurrentYear() + "-" + (getMonthId()>=10? getMonthId() : "0"+ getMonthId()) + "-01";
				params[1] = DateUtils.getCurrentYear() + "-" + (getMonthId()>=10? getMonthId() : "0"+ getMonthId()) + "-31";
				params[2] = is.getId()+"";
				boolean noIssuance = true;
				long endingNo = FormStatus.RTS.getId()==is.getStatus()? (((is.getPcs() - (is.getEndingNo() - is.getBeginningNo()))-1)+ is.getEndingNo()) : is.getEndingNo();
				for(CollectionInfo info : CollectionInfo.retrieve(sql, params)) {
					seriesForm.add(reportCollectionInfo(info,endingNo));
					noIssuance = false;
				}
				
				if(noIssuance) {
					
					//if no issuance for current month
					//check the last issuance if available
					sql = " AND frm.receiveddate<=? AND sud.logid=? ";
					
					/*if(getFundSearchId()>0) {
						sql += " AND frm.fundid=" + getFundSearchId();
					}
					
					if(getCollectorMapId()>0) {
						sql += " AND cl.isid=" + getCollectorMapId();
					}*/
					
					sql += " ORDER BY frm.colid DESC limit 1";
					
					params = new String[2];
					int month = getMonthId() - 1;
					params[0] = DateUtils.getCurrentYear() + "-" + (month>=10? month : "0"+ month) + "-31";
					params[1] = is.getId()+"";
					System.out.println("checking previous ");
					List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
					System.out.println("end checking previous ");
					if(infos!=null && infos.size()>0) {
						Form11Report rpt = reportLastCollectionInfo(infos.get(0));
						if(rpt!=null) {
							seriesForm.add(rpt);
						}
					}else {
						seriesForm.add(reportIssued(is));
					}
					
					
				}
				
			}
		  }
		}
	}
	
	
	public void printMonthSeries() {
		if(getMonthId()==DateUtils.getCurrentMonth()) {
			buildFormData();
		}else {
			readXML();
		}
	}
	
	private void readXML() {
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();  
		String monthlyReportName = DateUtils.getMonthName(getMonthId()).toUpperCase() + "-" + DateUtils.getCurrentYear();
		
		
		File xmlFile = new File(XML_FOLDER + monthlyReportName + ".xml");
		
		if(xmlFile.isFile()) {//if file present
		setSeriesForm(new ArrayList<Form11Report>());	
		//System.out.println("File is present");
		RCDReader xml = RCDReader.readXML(XML_FOLDER + monthlyReportName + ".xml", true);
		for(RCDFormSeries s : xml.getRcdFormSeries()) {
			Form11Report sr = new Form11Report();
			
			sr.setF1(s.getName());
			
			sr.setF2(s.getBeginningQty());
			sr.setF3(s.getBeginningFrom());
			sr.setF4(s.getBeginningTo());
			
			sr.setF5(s.getReceiptQty());
			sr.setF6(s.getReceiptFrom());
			sr.setF7(s.getReceiptTo());

			sr.setF8(s.getIssuedQty());
			sr.setF9(s.getIssuedFrom());
			sr.setF10(s.getIssuedTo());
	  		
			sr.setF11(s.getEndingQty());
			sr.setF12(s.getEndingFrom());
			sr.setF13(s.getEndingTo());
			
			sr.setF14(s.getRemarks());
			sr.setF15(s.getCollector());
			
			getSeriesForm().add(sr);
		}
		printMonth();
		}else {//else create the file
			//System.out.println("File is not present... creating new file");
			buildFormData();
		}
	}
	
	private void printMonth() {
		
		//compiling report
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME =ReadConfig.value(AppConf.FORM11_REPORT);
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(getSeriesForm());
  		HashMap param = new HashMap();
		
  		param.put("PARAM_REPORT_TITLE","CONSOLIDATED REPORT OF ACCOUNTABILITY FOR ACCOUNTABLE FORMS");
  		param.put("PARAM_MUNICIPALITY","Municipality of Lake Sebu");
  		String dateMonth = DateUtils.getCurrentYear() + "-" + (getMonthId()>=10? getMonthId() : "0"+ getMonthId()) + "-01";
  		param.put("PARAM_DATE_RANGE","For the month of "+DateUtils.getMonthName(getMonthId()) + " 1-" + DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", dateMonth, Locale.TAIWAN, DateUtils.getCurrentYear(),getMonthId()).split("-")[2] +", " + DateUtils.getCurrentYear());
  		param.put("PARAM_REPORT_NO","Report No." + DateUtils.getCurrentYear() +"-" + (getMonthId()>9? getMonthId() : "0"+ getMonthId()));
  		
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
	
	private RCDReader buildFormData() {
		//String monthlyReportName = DateUtils.getMonthName(DateUtils.getCurrentMonth()).toUpperCase() + "-" + DateUtils.getCurrentYear();
		String monthlyReportName = DateUtils.getMonthName(getMonthId()).toUpperCase() + "-" + DateUtils.getCurrentYear();
		String collector = "FERDINAND L. LOPEZ";
		String[] dates = DateUtils.getCurrentDateYYYYMMDD().split("-");
		
		String virifiedDate = dates[1]+"/"+dates[2]+"/"+dates[0];
		
		String seriesRpt = "";
		
		RCDReader rcd = new RCDReader();
		rcd.setBrisFile("marxmind");
		rcd.setDateCreated(DateUtils.getCurrentDateMMMMDDYYYY());
		rcd.setFund("");
		rcd.setAccountablePerson(collector);
		rcd.setSeriesReport(seriesRpt);
		rcd.setDateVerified(virifiedDate);
		
		List<RCDFormDetails> dtls = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>());
		List<RCDFormSeries> srs = new ArrayList<RCDFormSeries>();//Collections.synchronizedList(new ArrayList<RCDFormSeries>());
		
		int cnt = 1;
		
		RCDFormDetails dt = new RCDFormDetails();
		dt.setFormId(cnt+"");
		dt.setName("");
		dt.setSeriesFrom("");
		dt.setSeriesTo("");
		dt.setAmount("0");
		dtls.add(dt);
		//System.out.println("checking series >> " + seriesForm!=null? seriesForm.size() : "null" );
		for(Form11Report frm : seriesForm) {
			
			RCDFormSeries sr = new RCDFormSeries();
			String formName = frm.getF1();
			
			sr.setId(cnt+"");
			sr.setName(formName);
			
			sr.setBeginningQty(frm.getF2());
	  		sr.setBeginningFrom(frm.getF3());
	  		sr.setBeginningTo(frm.getF4());
	  		
	  		sr.setReceiptQty(frm.getF5());
	  		sr.setReceiptFrom(frm.getF6());
	  		sr.setReceiptTo(frm.getF7());

	  		sr.setIssuedQty(frm.getF8());
	  		sr.setIssuedFrom(frm.getF9());
	  		sr.setIssuedTo(frm.getF10());
	  		
	  		sr.setEndingQty(frm.getF11());
	  		sr.setEndingFrom(frm.getF12());
	  		sr.setEndingTo(frm.getF13());
	  		
	  		sr.setRemarks(frm.getF14());
	  		sr.setCollector(frm.getF15());
	  		
	  		srs.add(sr);
			
			cnt++;
		}
		
		rcd.setRcdFormDtls(dtls);
		rcd.setRcdFormSeries(srs);
		
		rcd.setBeginningBalancesAmount("0.00");
		rcd.setAddAmount("0.00");
		rcd.setLessAmount("0.00");
		rcd.setBalanceAmount("0.00");
		
		rcd.setCertificationPerson(collector);
		rcd.setVerifierPerson("HENRY E. MAGBANUA");
		rcd.setDateVerified(dates[1]+"/"+dates[2]+"/"+dates[0]);
		rcd.setTreasurer("FERDINAND L. LOPEZ");
		
		//force order
		List<RCDFormSeries> series = rcd.getRcdFormSeries();
		List<RCDFormSeries> ss = new ArrayList<RCDFormSeries>();//Collections.synchronizedList(new ArrayList<RCDFormSeries>());
		for(FormType form : FormType.values()) {
			for(RCDFormSeries s : series) {
				if(form.getName().equalsIgnoreCase(s.getName())) {
					//System.out.println("FormType name =" + form.getName() + " == " + "series form = " + s.getName());
					ss.add(s);
				}
			}
		}
		rcd.setRcdFormSeries(ss);
		
		String XML_FOLDER = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
				"xml" + AppConf.SEPERATOR.getValue();  
		
		RCDReader.saveXML(rcd, monthlyReportName, XML_FOLDER, true);
		
		printMonth();
		
		return rcd;
	}
	
	public Form11Report reportCollectionInfo(CollectionInfo info, long endingNo){
		System.out.println("reportCollectionInfo>>>");
		System.out.println("is RTS? " + (info.getIsRts()==1? "YES" : "NO"));
		Form11Report rpt = new Form11Report();
		
		rpt.setF1(FormType.nameId(info.getFormType()));
		
		if(info.getIsRts()==1) {
			rpt = RTSData(info,rpt);
		}else {
		
		int logmonth = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[1]);
		int logDay = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[2]);
		
		String f3 = "";
		String f4 = "";
		String f6 = "";
		String f7 = "";
		String f9 = "";
		String f10 = "";
		String f12 = "";
		String f13 = "";
		
		if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {//current month and current day same
			//write in receipt
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			
			String be1= info.getBeginningNo()+"";
			int be2 = be1.length();
			
			
			
			if(info.getPrevPcs()==49) {
				rpt.setF5("50");
				f6 = be2==7? "0"+be1 : be1;
				rpt.setF6(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				
				String en1= endingNo+"";//info.getIssuedForm().getEndingNo()+"";
				int en2 = en1.length();
				//rpt.setF7(en2==7? "0"+en1 : en1);
			
				f7 = en2==7? "0"+en1 : en1;
				rpt.setF7(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));
			}else {
				/*int qty = info.getPrevPcs()+1;
				rpt.setF5(qty+"");
				
				long begNo = info.getBeginningNo();
				be1= begNo+"";
				be2 = be1.length();
				
				f6 = be2==7? "0"+be1 : be1;
				rpt.setF6(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				
				String en1= info.getIssuedForm().getEndingNo()+"";
				int en2 = en1.length();
			
				f7 = en2==7? "0"+en1 : en1;
				rpt.setF7(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));*/
				
				int qty = info.getPrevPcs()+1;
				//temporary removed if qty not equal to 50 place it to beginning
				//rpt.setF5(qty+"");
				rpt.setF2(qty+"");
				
				long begNo = info.getBeginningNo();
				be1= begNo+"";
				be2 = be1.length();
				f6 = be2==7? "0"+be1 : be1;
				
				//temporary removed if qty not equal to 50 place it to beginning
				//rpt.setF6(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f6)));
				rpt.setF6("");
				
				String en1= endingNo+"";//info.getIssuedForm().getEndingNo()+"";
				int en2 = en1.length();
				f7 = en2==7? "0"+en1 : en1;
				//temporary removed if qty not equal to 50 place it to beginning
				//rpt.setF7(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));
				rpt.setF4(DateUtils.numberResult(info.getFormType(), Long.valueOf(f7)));
				rpt.setF7("");
			}
			
			
			
			
		}else {
		//Write in beginning balance
			String be1= info.getBeginningNo()+"";
			int be2 = be1.length();
			
			if(info.getPrevPcs()==49) {
				rpt.setF2("50");
				//rpt.setF3(be2==7? "0"+be1 : be1);
				
				f3 = be2==7? "0"+be1 : be1; 
				rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
			}else {
				
				/*String sql = " AND sud.logid=?";
				String params[]= new String[1];
				params[0]= info.getIssuedForm().getId()+"";
				List<CollectionInfo> infos = CollectionInfo.retrieve(sql, params);
				
				if(infos!=null && infos.size()>1) {
					int qty = info.getPrevPcs()+1;
					rpt.setF2(qty+"");
					
					long begNo = info.getBeginningNo() - info.getPrevPcs();
					be1= begNo+"";
					be2 = be1.length();
					
					f3 = be2==7? "0"+be1 : be1; 
					rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
				}else {//correction for those who has a beginning quantity not equal to 49
					int qty = info.getPrevPcs()+1;
					rpt.setF2(qty+"");
					
					long begNo = info.getBeginningNo();
					be1= begNo+"";
					be2 = be1.length();
					
					f3 = be2==7? "0"+be1 : be1; 
					rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
				}*/
				System.out.println("Process on the new changes>>>>>");
				int qty = info.getPrevPcs()+1;
				rpt.setF2(qty+"");
				
				long begNo = info.getBeginningNo();
				be1= begNo+"";
				be2 = be1.length();
				
				f3 = be2==7? "0"+be1 : be1; 
				rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
				
				
				
				
			}
			
			
			String en1= endingNo+"";//info.getIssuedForm().getEndingNo()+"";
			int en2 = en1.length();
			//rpt.setF4(en2==7? "0"+en1 : en1);
			
			f4 = en2==7? "0"+en1 : en1;
			rpt.setF4(DateUtils.numberResult(info.getFormType(), Long.valueOf(f4)));
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
		}
		
		
		//issued
		rpt.setF8(info.getPcs()+"");
		
		String beg1= info.getBeginningNo()+"";
		int beg2 = beg1.length();
		//rpt.setF9(beg2==7? "0"+beg1 : beg1);
		
		f9 = beg2==7? "0"+beg1 : beg1;
		rpt.setF9(DateUtils.numberResult(info.getFormType(), Long.valueOf(f9)));
		
		//String en1= info.getIssuedForm().getEndingNo()+"";
		String en1= info.getEndingNo()+"";
		int en2 = en1.length();
		//rpt.setF10(en2==7? "0"+en1 : en1);
		
		f10 = en2==7? "0"+en1 : en1;
		rpt.setF10(DateUtils.numberResult(info.getFormType(), Long.valueOf(f10)));
		
		//ending balance
		
		long endingQty = info.getIssuedForm().getEndingNo() - info.getEndingNo();
		
		if(endingQty==0) {
			rpt.setF11("");
			rpt.setF12("All Issued");
			//remarks
			rpt.setF14("");
			rpt.setF13("");
			if(FormStatus.RTS.getId()==info.getIssuedForm().getStatus()) {
				endingQty = endingNo - info.getIssuedForm().getEndingNo();
				rpt.setF11(endingQty+"");
				long enNumber = info.getEndingNo() + 1;
				String enbeg1= enNumber+"";
				int enbeg2 = enbeg1.length();
				f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
				rpt.setF12(DateUtils.numberResult(info.getFormType(), Long.valueOf(f12)));
				
				String enen1= endingNo+"";
				int enen2 = enen1.length();
				f13 = enen2==7? "0"+enen1 : enen1;
				rpt.setF13(DateUtils.numberResult(info.getFormType(), Long.valueOf(f13)));
				
				rpt.setF14("Partial Issued");
			}
			
			
			
		}else {
			rpt.setF11(endingQty+"");
			long enNumber = info.getEndingNo() + 1;
			String enbeg1= enNumber+"";
			int enbeg2 = enbeg1.length();
			//rpt.setF12(enbeg2==7? "0"+enbeg1 : enbeg1);
			
			f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
			rpt.setF12(DateUtils.numberResult(info.getFormType(), Long.valueOf(f12)));
			
			String enen1= endingNo+"";//info.getIssuedForm().getEndingNo()+"";
			int enen2 = enen1.length();
			//rpt.setF13(enen2==7? "0"+enen1 : enen1);
			
			f13 = enen2==7? "0"+enen1 : enen1;
			rpt.setF13(DateUtils.numberResult(info.getFormType(), Long.valueOf(f13)));
			//remarks
			rpt.setF14("");
		}
		
		
		
		
		//change the value if the form is Cash ticket
				if(FormType.CT_2.getId()==info.getFormType() || FormType.CT_5.getId()==info.getFormType()) {
					
					rpt.setF1(FormType.nameId(info.getFormType()));
					String allIssued = info.getBeginningNo()==0? "All Issued" : "";
					double amount = 0d;
					
					if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
						
						amount = info.getBeginningNo() + info.getAmount();
						
						if(amount==info.getEndingNo()) {
							//beginning
							rpt.setF2("");
							rpt.setF3("");
							rpt.setF4("");
							 
							//Receipt
							rpt.setF5("");
							rpt.setF6(Currency.formatAmount(amount));
							rpt.setF7("");
						}else {
							//beginning
							rpt.setF2("");
							rpt.setF3(Currency.formatAmount(amount));
							rpt.setF4("");
							 
							//Receipt
							rpt.setF5("");
							rpt.setF6("");
							rpt.setF7("");
						}
						
						//issued
						rpt.setF8("");
						rpt.setF9(Currency.formatAmount(info.getAmount()));
						rpt.setF10("");
						
						//ending balance
						rpt.setF11("");
						if(info.getBeginningNo()>0) {
							rpt.setF12(Currency.formatAmount(info.getBeginningNo()));
						}else {
							rpt.setF12(allIssued);
						}
						rpt.setF13("");
					}else {
						
						amount = info.getBeginningNo() + info.getAmount();
						
						//beginning
						rpt.setF2("");
						rpt.setF3(Currency.formatAmount(amount));
						rpt.setF4("");
						
						//Receipt
						rpt.setF5("");
						rpt.setF6("");
						rpt.setF7("");
						
						//issued
						rpt.setF8("");
						rpt.setF9(Currency.formatAmount(info.getAmount()));
						rpt.setF10("");
						
						//ending balance
						rpt.setF11("");
						if(info.getBeginningNo()>0) {
							rpt.setF12(Currency.formatAmount(info.getBeginningNo()));
						}else {
							rpt.setF12(allIssued);
						}
						rpt.setF13("");
					}
						
						
						//remarks
						rpt.setF14("");
					//}
				}
				
				Collector col = Collector.retrieve(info.getCollector().getId());
				if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
					rpt.setF15(col.getName().replace("F.L Lopez-", ""));
				}else {
					rpt.setF15(col.getDepartment().getDepartmentName());
				}
		
		}		
				
		return rpt;
	}
	
	private Form11Report RTSData(CollectionInfo info,Form11Report rpt) {
		String beg="", end="";
		String be1= info.getBeginningNo()+"";
		int be2 = be1.length();
		String f3 = be2==7? "0"+be1 : be1; 
		beg = DateUtils.numberResult(info.getFormType(), Long.valueOf(f3));

		String en1= info.getEndingNo()+"";
		int en2 = en1.length();
		String f4 = en2==7? "0"+en1 : en1;
		end = DateUtils.numberResult(info.getFormType(), Long.valueOf(f4));
		
		
		
		//beginning
		rpt.setF2(info.getPcs()+"");
		rpt.setF3(beg);
		rpt.setF4(end);
		
		//receipt
		rpt.setF5("");
		rpt.setF6("");
		rpt.setF7("");
		
		//issued
		rpt.setF8("");
		rpt.setF9("");
		rpt.setF10("");
		
		//ending
		rpt.setF11(info.getPcs()+"");
		rpt.setF12(beg);
		rpt.setF13(end);
		
		rpt.setF14("***RTS***");
		
		Collector col = Collector.retrieve(info.getCollector().getId());
		if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
			rpt.setF15(col.getName().replace("F.L Lopez-", ""));
		}else {
			rpt.setF15(col.getDepartment().getDepartmentName());
		}
		
		return rpt;
	}
	
	public Form11Report reportLastCollectionInfo(CollectionInfo info){
		System.out.println("reportLastCollectionInfo>>> ");
		//
		Form11Report rpt = null;
		System.out.println("info.getIssuedForm().getEndingNo()=" + info.getIssuedForm().getEndingNo() + " - info.getEndingNo() " + info.getEndingNo());
		long endingQty = info.getIssuedForm().getEndingNo() - info.getEndingNo();
		System.out.println("endingQty>> " + endingQty);
		String f3 = "";
		String f4 = "";
		String f6 = "";
		String f7 = "";
		String f9 = "";
		String f10 = "";
		String f12 = "";
		String f13 = "";
		
		if(endingQty>0) {
		rpt = new Form11Report();	
		rpt.setF1(FormType.nameId(info.getFormType()));
		System.out.println("form type>> " + rpt.getF1());
		rpt.setF2(endingQty+"");
		
		long endTmp = info.getEndingNo() + 1;//added 1 if has previous issuance and no issuance on the next report 07/04/2019
		
		String enbeg1= endTmp+"";//info.getEndingNo()+"";
		int enbeg2 = enbeg1.length();
		
		f3 = enbeg2==7? "0"+enbeg1 : enbeg1;
		rpt.setF3(DateUtils.numberResult(info.getFormType(), Long.valueOf(f3)));
		
		String enen1= info.getIssuedForm().getEndingNo()+"";
		int enen2 = enen1.length();
		
		f4 = enen2==7? "0"+enen1 : enen1;
		rpt.setF4(DateUtils.numberResult(info.getFormType(), Long.valueOf(f4)));
		//rpt.setF4(enen2==7? "0"+enen1 : enen1);
		
		System.out.println("Beginning from >> " + rpt.getF3());
		System.out.println("Beginning to >> " + rpt.getF4());
		
		//Receipt
		rpt.setF5("");
		rpt.setF6("");
		rpt.setF7("");
		
		//issued
		rpt.setF8("");
		if(info.getIssuedForm().getIsForBarangayOR()==1) {
			rpt.setF9("All Issued");
		}else {
			rpt.setF9("No Iss.");
		}
		rpt.setF10("");
		
		
		//ending balance
		if(info.getIssuedForm().getIsForBarangayOR()==1) {
			rpt.setF11("");
			rpt.setF12("");
			rpt.setF13("");
		}else {
			rpt.setF11(endingQty+"");
			
			f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
			f13 = enen2==7? "0"+enen1 : enen1;
			rpt.setF12(DateUtils.numberResult(info.getFormType(), Long.valueOf(f12)));
			rpt.setF13(DateUtils.numberResult(info.getFormType(), Long.valueOf(f13)));
		}
		
		//remarks
		rpt.setF14("");
		
		//change the value if the form is Cash ticket
		if(FormType.CT_2.getId()==info.getFormType() || FormType.CT_5.getId()==info.getFormType()) {
			if(info.getAmount()>0) {
				int logmonth = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[1]);
				int logDay = Integer.valueOf(info.getIssuedForm().getIssuedDate().split("-")[2]);
				
				rpt.setF1(FormType.nameId(info.getFormType()));
				if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
					//beginning
					rpt.setF2("");
					rpt.setF3("");
					rpt.setF4("");
					
					//Receipt
					rpt.setF5("");
					rpt.setF6(Currency.formatAmount(info.getAmount()));
					rpt.setF7("");
				}else {
					//beginning
					rpt.setF2("");
					rpt.setF3(Currency.formatAmount(info.getAmount()));
					rpt.setF4("");
					
					//Receipt
					rpt.setF5("");
					rpt.setF6("");
					rpt.setF7("");
				}
				
				
				//issued
				rpt.setF8("");
				rpt.setF9(Currency.formatAmount(info.getAmount()));
				rpt.setF10("");
				
				String allIssued = info.getBeginningNo()==0? "All Issued" : "";
				//ending balance
				rpt.setF11("");
				rpt.setF12(allIssued);
				rpt.setF13("");
				
				//remarks
				rpt.setF14("");
			}
		}
		
		Collector col = Collector.retrieve(info.getCollector().getId());
		if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
			rpt.setF15(col.getName().replace("F.L Lopez-", ""));
		}else {
			rpt.setF15(col.getDepartment().getDepartmentName());
		}
		
		}
		
		return rpt;
	}
	
	public Form11Report reportIssued(IssuedForm isform) {
		
		System.out.println("reportIssued>>>");
		
		Form11Report rpt = new Form11Report();
		
		String f3 = "";
		String f4 = "";
		String f6 = "";
		String f7 = "";
		String f9 = "";
		String f10 = "";
		String f12 = "";
		String f13 = "";
		
		if("Stock".equalsIgnoreCase(isform.getCollector().getName())) {
			rpt.setF1(FormType.nameId(isform.getFormType()));
			
			int logmonth = Integer.valueOf(isform.getIssuedDate().split("-")[1]);
			
			if(logmonth==getMonthId()) {
				
				rpt.setF2("");
				rpt.setF3("");
				rpt.setF4("");
				
				String be1= isform.getBeginningNo()+"";
				int be2 = be1.length();
				rpt.setF5(isform.getPcs()+"");
				f6 = be2==7? "0"+be1 : be1;
				rpt.setF6(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f6)));
				//rpt.setF6(be2==7? "0"+be1 : be1);
				
				String en1= isform.getEndingNo()+"";
				int en2 = en1.length();
				f7 = en2==7? "0"+en1 : en1;
				rpt.setF7(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f7)));
				//rpt.setF7(en2==7? "0"+en1 : en1);
				
			}else {
			
				String be1= isform.getBeginningNo()+"";
				int be2 = be1.length();
				rpt.setF2(isform.getPcs()+"");
				f3 = be2==7? "0"+be1 : be1;
				rpt.setF3(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f3)));
				//rpt.setF3(be2==7? "0"+be1 : be1);
				
				String en1= isform.getEndingNo()+"";
				int en2 = en1.length();
				f4 = en2==7? "0"+en1 : en1;
				rpt.setF4(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f4)));
				//rpt.setF4(en2==7? "0"+en1 : en1);
				
				rpt.setF5("");
				rpt.setF6("");
				rpt.setF7("");
			
			}
			
			//issued
			rpt.setF8("");
			rpt.setF9("");
			rpt.setF10("");
			
			
			//ending balance
			
			
			rpt.setF11("");
			rpt.setF12("");
			rpt.setF13("");
			
			//remarks
			rpt.setF14("");
			
			Collector col = Collector.retrieve(isform.getCollector().getId());
			if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
				rpt.setF15(col.getName());
			}else {
				rpt.setF15(col.getDepartment().getDepartmentName());
			}
			
		}else {
		
		rpt.setF1(FormType.nameId(isform.getFormType()));
		
		int logmonth = Integer.valueOf(isform.getIssuedDate().split("-")[1]);
		int logDay = Integer.valueOf(isform.getIssuedDate().split("-")[2]);
		
		if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
			//write in receipt
			rpt.setF2("");
			rpt.setF3("");
			rpt.setF4("");
			
			String be1= isform.getBeginningNo()+"";
			int be2 = be1.length();
			rpt.setF5(isform.getPcs()+"");
			f6 = be2==7? "0"+be1 : be1;
			rpt.setF6(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f6)));
			//rpt.setF6(be2==7? "0"+be1 : be1);
				
			String en1= isform.getEndingNo()+"";
			int en2 = en1.length();
			f7 = en2==7? "0"+en1 : en1;
			rpt.setF7(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f7)));
			//rpt.setF7(en2==7? "0"+en1 : en1);
		
			
			
		}else {
		//Write in beginning balance
			String be1= isform.getBeginningNo()+"";
			int be2 = be1.length();
			rpt.setF2(isform.getPcs()+"");
			f3 = be2==7? "0"+be1 : be1;
			rpt.setF3(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f3)));
			//rpt.setF3(be2==7? "0"+be1 : be1);
			
			String en1= isform.getEndingNo()+"";
			int en2 = en1.length();
			f4 = en2==7? "0"+en1 : en1;
			rpt.setF4(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f4)));
			//rpt.setF4(en2==7? "0"+en1 : en1);
			
			rpt.setF5("");
			rpt.setF6("");
			rpt.setF7("");
		}
		
		//issued
		rpt.setF8("");
		if(isform.getIsForBarangayOR()==1) {
			rpt.setF9("All Issued");
		}else {
			rpt.setF9("No Iss.");
		}
		rpt.setF10("");
		
		
		//ending balance
		if(isform.getIsForBarangayOR()==1) {
			rpt.setF11("");
			rpt.setF12("");
			rpt.setF13("");
		}else {
			rpt.setF11(isform.getPcs()+"");
			
			String enbeg1= isform.getBeginningNo()+"";
			int enbeg2 = enbeg1.length();
			f12 = enbeg2==7? "0"+enbeg1 : enbeg1;
			rpt.setF12(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f12)));
			//rpt.setF12(enbeg2==7? "0"+enbeg1 : enbeg1);
			
			String enending1= isform.getEndingNo()+"";
			int enending2 = enending1.length();
			f13 = enending2==7? "0"+enending1 : enending1;
			rpt.setF13(DateUtils.numberResult(isform.getFormType(), Long.valueOf(f13)));
			//rpt.setF13(enending2==7? "0"+enending1 : enending1);
		}
		//remarks
		rpt.setF14("");
		
		Collector col = Collector.retrieve(isform.getCollector().getId());
		if(col.getDepartment().getCode().equalsIgnoreCase("1091")) {
			rpt.setF15(col.getName().replace("F.L Lopez-", ""));
		}else {
			rpt.setF15(col.getDepartment().getDepartmentName());
		}
		
		}
		
		//change the value if the form is Cash ticket
		if(FormType.CT_2.getId()==isform.getFormType() || FormType.CT_5.getId()==isform.getFormType()) {
			
			int logmonth = Integer.valueOf(isform.getIssuedDate().split("-")[1]);
			int logDay = Integer.valueOf(isform.getIssuedDate().split("-")[2]);
			
						rpt.setF1(FormType.nameId(isform.getFormType()));
						
						
						double amount = 0d;
						if(FormType.CT_2.getId()==isform.getFormType()) {
							amount = isform.getPcs() * 2;
						}else if(FormType.CT_5.getId()==isform.getFormType()) {
							amount = isform.getPcs() * 5;
						}
						
						if(logmonth==getMonthId() && logDay == DateUtils.getCurrentDay()) {
							//beginning
							rpt.setF2("");
							rpt.setF3("");
							rpt.setF4("");
							
							//Receipt
							rpt.setF5("");
							rpt.setF6(Currency.formatAmount(amount));
							rpt.setF7("");
						}else {
							//beginning
							rpt.setF2("");
							rpt.setF3(Currency.formatAmount(amount));
							rpt.setF4("");
							
							//Receipt
							rpt.setF5("");
							rpt.setF6("");
							rpt.setF7("");
						}
						
						
						//issued
						rpt.setF8("");
						if(isform.getIsForBarangayOR()==1) {
							rpt.setF9("All Issued");
						}else {
							rpt.setF9("No Iss.");
						}
						rpt.setF10("");
						
						
						//ending balance
						if(isform.getIsForBarangayOR()==1) {
							rpt.setF11("");
							rpt.setF12("");
							rpt.setF13("");
						}else {
							rpt.setF11("");
							rpt.setF12(Currency.formatAmount(amount));
							rpt.setF13("");
						}
						//remarks
						rpt.setF14("");
		}
		
		return rpt;
	
	}
	
	public void loadForms() {
		
		stocks = new ArrayList<Stocks>();//Collections.synchronizedList(new ArrayList<Stocks>());
		String sql = " AND cl.isid=0 AND st.qty>0 ";
		String[] params = new String[0];
		
		if(getFormTypeIdSearch()==0) {
			sql += " AND st.formType!=0";
		}else {
			sql += " AND st.formType="+getFormTypeIdSearch();
		}
		
		sql +=" ORDER BY st.datetrans ASC";
		
		stocks = Stocks.retrieve(sql, params);
		
	}
	
	public void stocksSelected(Stocks st) {
		setFormTypeId(st.getFormType());
		if(st.getFormType()<=8 || getFormTypeId()>=11) {
			setBeginningNo(Long.valueOf(st.getSeriesFrom()));
			setEndingNo(Long.valueOf(st.getSeriesTo()));
			setQuantity(50);
		}else {
			int qty = st.getQuantity();
			
			if(FormType.CT_2.getId()==st.getFormType()) {
				
				qty *= 2;
				
			}else if(FormType.CT_5.getId()==st.getFormType()) {
				
				qty *= 5;
				
			}
			
			setBeginningNo(qty);
			setEndingNo(qty);
			setQuantity(st.getQuantity());
		}
		setStatusId(FormStatus.HANDED.getId());
		setStockData(st);
	}
	
	public void calculateEndingNo() {
		
		if(getFormTypeId()<=8 || getFormTypeId()>=11) {
			
			long ending = (getBeginningNo()) + (getQuantity()==0? 0 : getQuantity()-1);
			System.out.println("begin: " + getBeginningNo() + " pcs: " + getQuantity());
			System.out.println("ending: " + ending);
			setEndingNo(ending);
			
		}else {
			
			int qty = getQuantity();
			
			if(FormType.CT_2.getId()==getFormTypeId()) {
				
				qty *= 2;
				
			}else if(FormType.CT_5.getId()==getFormTypeId()) {
				
				qty *= 5;
				
			}
			
			setBeginningNo(qty);
			setEndingNo(qty);
		}
		
		
	}
	
	public void createNew() {
		setStockData(null);
		setFormTypeSearch(null);
		setFormTypeIdSearch(0);
		setFundId(1);
		setFormTypeId(0);
		setIssuedDate(null);
		setBeginningNo(0);
		setEndingNo(0);
		setQuantity(0);
		setCollectorId(0);
		setStatusId(0);
		setSelectedForm(null);
		init();
	}
	
	public void saveData() {
		
		IssuedForm form = new IssuedForm();
		boolean isOk = true;
		
		if(getSelectedForm()!=null) {
			form = getSelectedForm();
		}else {
			form.setIsActive(1);
		}
		
		if(getBeginningNo()<=0 && getFormTypeId()<=8) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Serial From");
		}
		
		if(getEndingNo()<=0 && getFormTypeId()<=8) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Serial To");
		}
		
		
		if(getQuantity()<=0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Quantity");
			
			if(FormType.CT_2.getId()==getFormTypeId() || FormType.CT_2.getId()==getFormTypeId()) {
				if(getQuantity()==2000 || getQuantity()==4000 || getQuantity()==6000 || getQuantity()==8000 || getQuantity()==10000 || getQuantity()==12000) {
					
				}else {
					isOk = false;
					Application.addMessage(3, "Error", "Please provide exact quantity");
				}
			}
			
		}
		
		if(getCollectorId()<=0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please provide Collector");
		}
			
		if(isOk) {
			
			form.setFundId(getFundId());
			form.setIssuedDate(DateUtils.convertDate(getIssuedDate(), "yyyy-MM-dd"));
			form.setStatus(getStatusId());
			form.setFormType(getFormTypeId());
			form.setPcs(getQuantity());
			
			if(getFormTypeId()<=8 || getFormTypeId()>=11) {
				form.setBeginningNo(getBeginningNo());
				form.setEndingNo(getEndingNo());
			}else {
				
				/*int qty = getQuantity();
				
				if(FormType.CT_2.getId()==getFormTypeId()) {
					
					qty *= 2;
					
				}else if(FormType.CT_5.getId()==getFormTypeId()) {
					
					qty *= 5;
					
				}*/
				
				
				int quantity = getQuantity();
				if(getStockData()!=null) {
					quantity = getStockData().getQuantity();
					if(quantity>getQuantity()) {
						quantity = getQuantity();
					}
				}
				form.setBeginningNo(getBeginningNo());
				form.setEndingNo(getEndingNo());
			}
			
			if(getStockData()!=null) {
				form.setStock(getStockData());
			}
				
			Collector collector = new Collector();
			collector.setId(getCollectorId());
			form.setCollector(collector);
			
			//update stocks
			if(getStockData()!=null) {
				Stocks st = getStockData();
				
				if(getFormTypeId()<=8) {
					st.setStatus(StockStatus.ALL_ISSUED.getId());
					st.setCollector(collector);
				}else {	
					
					int quantity = getStockData().getQuantity();
					if(quantity>getQuantity()) {
						quantity -= getQuantity();
						st.setStatus(StockStatus.PARTIAL_ISSUED.getId());
					}else if(quantity==getQuantity()) {
						quantity = 0;
						st.setStatus(StockStatus.ALL_ISSUED.getId());
					}else if(quantity<getQuantity()) {
						quantity = 0;
						st.setStatus(StockStatus.ALL_ISSUED.getId());
					}
					st.setCollector(collector);
					st.setQuantity(quantity);
				}
				
				st.save();
				form.setStabNo(st.getStabNo());
			}
			
			form.save();
			
			createNew();
			setCollectorMapId(collector.getId());
			setFundSearchId(form.getFundId());
			init();
			Application.addMessage(1, "Success", "Successfully saved.");
		}
		
	}
	
	public void deleteRow(IssuedForm form) {
		
		if(IssuedForm.isExistInCollection(form.getId())) {
			Application.addMessage(2, "Warning", "Deletion is not allowed. Form already used in collection.");
		}else {
			
			Stocks st = form.getStock();
			st.setStatus(StockStatus.NOT_HANDED.getId());
			st.setCollector(null);
			st.save();
			
			form.delete();
			init();
			Application.addMessage(1, "Success", "Successfully deleted.");
		}
	}
	
	public void clickItem(IssuedForm form) {
		setStockData(null);
		setFundId(form.getFundId());
		setFormTypeId(form.getFormType());
		setIssuedDate(DateUtils.convertDateString(form.getIssuedDate(),"yyyy-MM-dd"));
		setQuantity(form.getPcs());
		
		if(getFormTypeId()<=8) {
			setBeginningNo(form.getBeginningNo());
			setEndingNo(form.getEndingNo());
		}else {
			setBeginningNo(form.getPcs());
			setEndingNo(form.getPcs());
		}
		
		setCollectorId(form.getCollector().getId());
		setStatusId(form.getStatus());
		setSelectedForm(form);
		//addedd to fix the issue in cash ticket
		calculateEndingNo();
	}
	
	
	public Date getIssuedDate() {
		if(issuedDate==null) {
			issuedDate = DateUtils.getDateToday();
		}
		return issuedDate;
	}
	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}
	public int getFormTypeId() {
		if(formTypeId==0) {
			formTypeId=1;
		}
		return formTypeId;
	}
	public void setFormTypeId(int formTypeId) {
		this.formTypeId = formTypeId;
	}
	public List getFormTypes() {
		formTypes = new ArrayList<>();
		
		for(FormType form : FormType.values()) {
			formTypes.add(new SelectItem(form.getId(), form.getName()));
		}
		
		return formTypes;
	}
	public void setFormTypes(List formTypes) {
		this.formTypes = formTypes;
	}
	public long getBeginningNo() {
		return beginningNo;
	}
	public void setBeginningNo(long beginningNo) {
		this.beginningNo = beginningNo;
	}
	public long getEndingNo() {
		return endingNo;
	}
	public void setEndingNo(long endingNo) {
		this.endingNo = endingNo;
	}

	public int getCollectorId() {
		return collectorId;
	}

	public void setCollectorId(int collectorId) {
		this.collectorId = collectorId;
	}

	public List getCollectors() {
		collectors = new ArrayList<>();
		collectors.add(new SelectItem(0, "Select Collector"));
		for(Collector col : Collector.retrieve("", new String[0])) {
			collectors.add(new SelectItem(col.getId(), col.getDepartment().getDepartmentName()+"/"+col.getName()));
		}
		
		return collectors;
	}

	public void setCollectors(List collectors) {
		this.collectors = collectors;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public List getStatus() {
		status = new ArrayList<>();
		for(FormStatus st : FormStatus.values()) {
			status.add(new SelectItem(st.getId(), st.getName()));
		}
		return status;
	}

	public void setStatus(List status) {
		this.status = status;
	}

	public IssuedForm getSelectedForm() {
		return selectedForm;
	}

	public void setSelectedForm(IssuedForm selectedForm) {
		this.selectedForm = selectedForm;
	}

	public List<IssuedForm> getForms() {
		return forms;
	}

	public void setForms(List<IssuedForm> forms) {
		this.forms = forms;
	}

	public List<Stocks> getStocks() {
		return stocks;
	}

	public void setStocks(List<Stocks> stocks) {
		this.stocks = stocks;
	}

	public int getFormTypeIdSearch() {
		return formTypeIdSearch;
	}

	public void setFormTypeIdSearch(int formTypeIdSearch) {
		this.formTypeIdSearch = formTypeIdSearch;
	}

	public List getFormTypeSearch() {
		
		formTypeSearch = new ArrayList<>();
		formTypeSearch.add(new SelectItem(0, "All Forms"));
		for(FormType form : FormType.values()) {
			formTypeSearch.add(new SelectItem(form.getId(), form.getName() + " " + form.getDescription()));
		}
		
		return formTypeSearch;
	}

	public void setFormTypeSearch(List formTypeSearch) {
		this.formTypeSearch = formTypeSearch;
	}

	public Stocks getStockData() {
		return stockData;
	}

	public void setStockData(Stocks stockData) {
		this.stockData = stockData;
	}

	public int getFundId() {
		return fundId;
	}

	public void setFundId(int fundId) {
		this.fundId = fundId;
	}

	public List getFunds() {
		funds = new ArrayList<>();
		for(FundType f : FundType.values()) {
			funds.add(new SelectItem(f.getId(), f.getName()));
		}
		return funds;
	}

	public void setFunds(List funds) {
		this.funds = funds;
	}

	public int getCollectorMapId() {
		return collectorMapId;
	}

	public void setCollectorMapId(int collectorMapId) {
		this.collectorMapId = collectorMapId;
	}

	public List getCollectorsMap() {
		collectorsMap = new ArrayList<>();
		collectorsMap.add(new SelectItem(0, "All Collectors"));
		
		for(Collector col : Collector.retrieve("", new String[0])) {
			collectorsMap.add(new SelectItem(col.getId(), col.getDepartment().getDepartmentName()+"/"+col.getName()));
		}
		return collectorsMap;
	}

	public void setCollectorsMap(List collectorsMap) {
		this.collectorsMap = collectorsMap;
	}

	public int getFundSearchId() {
		return fundSearchId;
	}

	public void setFundSearchId(int fundSearchId) {
		this.fundSearchId = fundSearchId;
	}

	public List getFundsSearch() {
		fundsSearch = new ArrayList<>();
		fundsSearch.add(new SelectItem(0, "ALL FUNDS"));
		for(FundType f : FundType.values()) {
			fundsSearch.add(new SelectItem(f.getId(), f.getName()));
		}
		return fundsSearch;
	}

	public void setFundsSearch(List fundsSearch) {
		this.fundsSearch = fundsSearch;
	}

	public List<Form11Report> getSeriesForm() {
		return seriesForm;
	}

	public void setSeriesForm(List<Form11Report> seriesForm) {
		this.seriesForm = seriesForm;
	}

	
	public int getMonthId() {
		if(monthId==0) {
			monthId = DateUtils.getCurrentMonth();
		}
		return monthId;
	}

	public void setMonthId(int monthId) {
		this.monthId = monthId;
	}

	public List getMonths() {
		months = new ArrayList<>();
		months.add(new SelectItem(0, "All Months"));
		for(int m=1; m<=12;m++) {
			months.add(new SelectItem(m, DateUtils.getMonthName(m)));
		}
		return months;
	}

	public void setMonths(List months) {
		this.months = months;
	}

	public String getTabNow() {
		if(tabNow==null) {
			tabNow = "Issued Forms";
		}
		return tabNow;
	}

	public void setTabNow(String tabNow) {
		this.tabNow = tabNow;
	}
	

}