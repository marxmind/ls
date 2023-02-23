package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.italia.municipality.lakesebu.controller.DTR;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
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
public class DtrBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 15678432343L;

	private List<DTR> dtrs;
	
	private List years;
	private int year;
	
	private List months;
	private int monthId;
	
	private List selectedHoliday;
	private List holidays;
	
	private List selectedBz;
	private List officialBzs;
	
	private String employeeName;
	private String firstWorkingDay;
	private String lastWorkingDay;
	
	private String adminPerson="FERDINAND L. LOPEZ";
	
	@PostConstruct
	public void init() {
		
		months = new ArrayList<>();
		months.add(new SelectItem(1,"January"));
		months.add(new SelectItem(2,"February"));
		months.add(new SelectItem(3,"March"));
		months.add(new SelectItem(4,"April"));
		months.add(new SelectItem(5,"May"));
	    months.add(new SelectItem(6,"June"));
		months.add(new SelectItem(7,"July"));
		months.add(new SelectItem(8,"August"));
		months.add(new SelectItem(9,"September"));
		months.add(new SelectItem(10,"October"));
		months.add(new SelectItem(11,"November"));
		months.add(new SelectItem(12,"December"));
		
		holidays = new ArrayList<>();
		Calendar cal = new GregorianCalendar(getYear(), getMonthId()-1, 1);
		
		do {
			
			int day = cal.get(Calendar.DAY_OF_WEEK);
			 if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
				 
			 }else {
				 holidays.add(new SelectItem(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH)+""));
			 }
			 
			 cal.add(Calendar.DAY_OF_YEAR, 1);
		}  while (cal.get(Calendar.MONTH) == getMonthId()-1);
		
		years = new ArrayList<>();
		int yr = DateUtils.getCurrentYear();
		yr -=2;
		years.add(new SelectItem(yr, yr+""));
		yr +=1;
		years.add(new SelectItem(yr, yr+""));
		yr = DateUtils.getCurrentYear();
		years.add(new SelectItem(yr, yr+""));
		
		if(year==0) {
			year = DateUtils.getCurrentYear();
		}
		
		officialBzs = new ArrayList<>();
		cal = new GregorianCalendar(getYear(), getMonthId()-1, 1);
		
		do {
			
			/*int day = cal.get(Calendar.DAY_OF_WEEK);
			 if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
				 
			 }else {*/
				 officialBzs.add(new SelectItem(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH)+""));
			 //}
			 
			 cal.add(Calendar.DAY_OF_YEAR, 1);
		}  while (cal.get(Calendar.MONTH) == getMonthId()-1);
		
		generateDTR();
	}
	
	public void loadDates() {
		System.out.println("load date is loading......");
		holidays = new ArrayList<>();
		Calendar cal = new GregorianCalendar(getYear(), getMonthId()-1, 1);
		
		do {
			
			int day = cal.get(Calendar.DAY_OF_WEEK);
			 if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
				 
			 }else {
				 holidays.add(new SelectItem(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH)+""));
			 }
			 
			 cal.add(Calendar.DAY_OF_YEAR, 1);
		}  while (cal.get(Calendar.MONTH) == getMonthId()-1);
		
		
		officialBzs = new ArrayList<>();
		cal = new GregorianCalendar(getYear(), getMonthId()-1, 1);
		
		do {
			
			/*int day = cal.get(Calendar.DAY_OF_WEEK);
			 if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
				 
			 }else {*/
				 officialBzs.add(new SelectItem(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH)+""));
			 //}
			 
			 cal.add(Calendar.DAY_OF_YEAR, 1);
		}  while (cal.get(Calendar.MONTH) == getMonthId()-1);
		
	}
	
	public void generateDTR() {
		System.out.println("generateDTR() >> month " + getMonthId());
		dtrs = new ArrayList<DTR>();
		
		// create a Calendar for the 1st of the required month
		int year = getYear();//DateUtils.getCurrentYear();
		//int month = Calendar.JUNE;
		int mth = getMonthId()-1;
		//int[] holidays = {1,5,6,13,18,19,22,29};
		int size = 0;
		if(getSelectedHoliday()!=null && getSelectedHoliday().size()>0) {
			size = getSelectedHoliday().size();
		}
		
		int[] holidays = new int[size];
		
		int i=0;
		if(getSelectedHoliday()!=null && getSelectedHoliday().size()>0) {
			for(Object obj : getSelectedHoliday()) {
				int hol = Integer.valueOf(obj.toString());
				holidays[i++] = hol;
			}
		}
		
		int obSize = 0;
		if(getSelectedBz()!=null && getSelectedBz().size()>0) {
			obSize= getSelectedBz().size();
		}
		int[] obz = new int[obSize];
		int x=0;
		if(getSelectedBz()!=null && getSelectedBz().size()>0) {
					for(Object obj : getSelectedBz()) {
						int ob = Integer.valueOf(obj.toString());
						obz[x++] = ob;
					}
		}
		
		String[] timeInAM = {"07:40","07:41","07:42","07:43","07:44","07:45","07:46","07:47","07:48","07:49","07:50","07:51","07:52","07:53","07:54","07:55","07:56","07:57","07:58","07:59"};
		String[] timeOutAM = {"12:01","12:02","12:03","12:04","12:05","12:06","12:07","12:08","12:09","12:10","12:11","12:12","12:13","12:14","12:15","12:16","12:17","12:18","12:19","12:20"};
		String[] timeInPM = {"12:40","12:41","12:42","12:43","12:44","12:45","12:46","12:47","12:48","12:49","12:50","12:51","12:52","12:53","12:54","12:55","12:56","12:57","12:58","12:59"};
		String[] timeOutPM = {"05:01","05:02","05:03","05:04","05:05","05:06","05:07","05:08","05:09","05:10","05:11","05:12","05:13","05:14","05:15","05:16","05:17","05:18","05:19","05:20"};
		Calendar cal = new GregorianCalendar(year, mth, 1);
		
		boolean isfirstWorkingDaySet = false;
		do {
			
			DTR dtr = new DTR();
			
		    // get the day of the week for the current day
		    int day = cal.get(Calendar.DAY_OF_WEEK);
		    // check if it is a Saturday or Sunday
		    if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
		        // print the day - but you could add them to a list or whatever
		        
		    	boolean isOb = false;
		    	for(int o : obz) {
		    		if(o == cal.get(Calendar.DAY_OF_MONTH)) {
		    			isOb = true;
		    		}
		    	}
		    	
		    	if(isOb) {
		    		dtr.setF1(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF2("OB");
			    	dtr.setF3("OB");
			    	dtr.setF4("OB");
			    	dtr.setF5("OB");
			    	
			    	dtr.setF6(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF7("OB");
			    	dtr.setF8("OB");
			    	dtr.setF9("OB");
			    	dtr.setF10("OB");
			    	
			    	dtr.setF11(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF12("OB");
			    	dtr.setF13("OB");
			    	dtr.setF14("OB");
			    	dtr.setF15("OB");
		    	}else {
		    	
		    	
			    	dtr.setF1(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF2("S");
			    	dtr.setF3("S");
			    	dtr.setF4("S");
			    	dtr.setF5("S");
			    	
			    	dtr.setF6(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF7("S");
			    	dtr.setF8("S");
			    	dtr.setF9("S");
			    	dtr.setF10("S");
			    	
			    	dtr.setF11(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF12("S");
			    	dtr.setF13("S");
			    	dtr.setF14("S");
			    	dtr.setF15("S");
		    	}
		    }else {
		    	boolean isHoliday = false;
		    	for(int hol : holidays) {
		    		if(hol == cal.get(Calendar.DAY_OF_MONTH)) {
		    			isHoliday = true;
		    		}
		    	}
		    	if(isHoliday) {
		    		//System.out.println(cal.get(Calendar.DAY_OF_MONTH)+ " HOLIDAY\tHOLIDAY\tHOLIDAY\tHOLIDAY");
		    		dtr.setF1(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF2("***");
			    	dtr.setF3("HOLI");
			    	dtr.setF4("DAY");
			    	dtr.setF5("***");
			    	
			    	dtr.setF6(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF7("***");
			    	dtr.setF8("HOLI");
			    	dtr.setF9("DAY");
			    	dtr.setF10("***");
			    	
			    	dtr.setF11(cal.get(Calendar.DAY_OF_MONTH)+"");
			    	dtr.setF12("***");
			    	dtr.setF13("HOLI");
			    	dtr.setF14("DAY");
			    	dtr.setF15("***");
		    	}else {
		    		
		    		boolean isOb = false;
			    	for(int o : obz) {
			    		if(o == cal.get(Calendar.DAY_OF_MONTH)) {
			    			isOb = true;
			    		}
			    	}
			    	
			    	if(!isfirstWorkingDaySet) {
			    		setFirstWorkingDay(cal.get(Calendar.DAY_OF_MONTH)+"");
			    		isfirstWorkingDaySet = true;
			    	}
			    	
			    	if(isOb) {
			    		dtr.setF1(cal.get(Calendar.DAY_OF_MONTH)+"");
				    	dtr.setF2("OB");
				    	dtr.setF3("OB");
				    	dtr.setF4("OB");
				    	dtr.setF5("OB");
				    	
				    	dtr.setF6(cal.get(Calendar.DAY_OF_MONTH)+"");
				    	dtr.setF7("OB");
				    	dtr.setF8("OB");
				    	dtr.setF9("OB");
				    	dtr.setF10("OB");
				    	
				    	dtr.setF11(cal.get(Calendar.DAY_OF_MONTH)+"");
				    	dtr.setF12("OB");
				    	dtr.setF13("OB");
				    	dtr.setF14("OB");
				    	dtr.setF15("OB");
			    	}else {			    		
			    		String dy = cal.get(Calendar.DAY_OF_MONTH)+"";
			    		String amin = timeInAM[(int)(Math.random() * timeInAM.length)];
			    		String amout = timeOutAM[(int)(Math.random() * timeOutAM.length)];
			    		String pmin = timeInPM[(int)(Math.random() * timeInPM.length)];
			    		String pmout = timeOutPM[(int)(Math.random() * timeOutPM.length)];
			    		
			    		dtr.setF1(dy);
				    	dtr.setF2(amin);
				    	dtr.setF3(amout);
				    	dtr.setF4(pmin);
				    	dtr.setF5(pmout);
				    	
				    	dtr.setF6(dy);
				    	dtr.setF7(amin);
				    	dtr.setF8(amout);
				    	dtr.setF9(pmin);
				    	dtr.setF10(pmout);
				    	
				    	dtr.setF11(dy);
				    	dtr.setF12(amin);
				    	dtr.setF13(amout);
				    	dtr.setF14(pmin);
				    	dtr.setF15(pmout);
			    	}
			    	setLastWorkingDay(cal.get(Calendar.DAY_OF_MONTH)+"");
		    	}
		    }
		    dtrs.add(dtr);
		    // advance to the next day
		    cal.add(Calendar.DAY_OF_YEAR, 1);
		}  while (cal.get(Calendar.MONTH) == mth);
		// stop when we reach the start of the next month
		
	}
	
	public void printReportAll(){
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME =ReadConfig.value(AppConf.DTR_REPORT);
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(getDtrs());
  		HashMap param = new HashMap();
  		
  		param.put("PARAM_MONTH",DateUtils.getMonthName(getMonthId()) + " " + getFirstWorkingDay() +"-" + getLastWorkingDay()+ ", " + getYear());
  		param.put("PARAM_EMPLOYEE",getEmployeeName()==null? "" : getEmployeeName().toUpperCase());
  		param.put("PARAM_ADMIN",getAdminPerson()==null? "" : getAdminPerson().toUpperCase());
  		
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
