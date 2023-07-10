package com.italia.municipality.lakesebu.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.ORListing;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OrlistingXML;

public class JsonBuilder {

	public static void main(String[] args) {
		
		//to query in comment the session in webhisdatabaseconnect below code
		//HttpSession session = SessionBean.getSession();
		 //String val = session.getAttribute("server-local").toString();
		//uncomment below code
		//String val="true";
		
		//createBusinessJsonFile();
		createCollectionJsonFile();
		//createCheckJsonFile();
		//createORJsonFile();
	}

static void createORJsonFile() {
		
		String FILE_LOG_NAME = "orlisting";
		String FILE_LOG_TMP_NAME = "tmporlisting";
		String EXT = ".json";
		
		String logpath = ReadConfig.value(AppConf.APP_LOG_PATH);
        
        String finalFile = logpath + FILE_LOG_NAME + EXT;
        String tmpFileName = logpath + FILE_LOG_TMP_NAME + EXT;
        
        File originalFile = new File(finalFile);
        
        //check log directory
        File logdirectory = new File(ReadConfig.value(AppConf.APP_LOG_PATH));
        if(!logdirectory.isDirectory()){
        	logdirectory.mkdir();
        }
        
        File tempFile = new File(tmpFileName);
        PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(tempFile));
			
			String str = "{\n";
			str += "\t\"orlisting\" : [\n";
			Object[] objs = ORListing.getReport(" AND (o.ordatetrans>='2023-06-01' AND o.ordatetrans<='2023-12-31')");
			List<ORListing> ors = (List<ORListing>)objs[1];
			int totalSize = ors.size();
			int count = 1;
	        for(ORListing p : ors) {
	        	str +="\t\t{\n";
	        	
	        	str += "\"id\" : \"" + p.getId() + "\",\n";
	        	str += "\"dateTrans\" : \"" + DateUtils.convertDateToMonthDayYear(p.getDateTrans()) + "\",\n";
	        	str += "\"orNumber\" : \"" + p.getOrNumber().trim() + "\",\n";
	        	str += "\"customerName\" : \"" + p.getCustomer().getFullname().trim().replace("\"", "'") + "\",\n";
	        	str += "\"collectorName\" : \"" + p.getCollector().getName().trim() + "\",\n";
	        	str += "\"amount\" : \"" + Currency.formatAmount(p.getAmount()) + "\",\n";
	        	str += "\"statusName\" : \"" + p.getStatusName() + "\",\n";
	        	str += "\"formName\" : \"" + p.getFormName() + "\",\n";
	        	str += "\"formInfo\" : \"" +  "\",\n";
	        	str += "\"notes\" : \"" +  "\"\n";
	        	
	        	if(count==1 && count==totalSize) {
	        		str+="\t\t}\n";
	        	}else if(count>1 && count==totalSize) {
	        		str+="\t\t}\n";
	        	}else {
	        		str+="\t\t},\n";
	        	}
	        	
	        	
	        	count++;
	        }
	        str+="\t\t]\n";
	        str+="}\n";
	        
	        pw.println(str);	
	        pw.flush();
	        pw.close();
	        System.out.println("completed...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        

        // Rename the new file to the filename the original file had.
        if (!tempFile.renameTo(originalFile))
            System.out.println("Could not rename file");
		
	}
		
	
static void createCheckJsonFile() {
		
		String FILE_LOG_NAME = "checks";
		String FILE_LOG_TMP_NAME = "tmpchecks";
		String EXT = ".json";
		
		String logpath = ReadConfig.value(AppConf.APP_LOG_PATH);
        
        String finalFile = logpath + FILE_LOG_NAME + EXT;
        String tmpFileName = logpath + FILE_LOG_TMP_NAME + EXT;
        
        File originalFile = new File(finalFile);
        
        //check log directory
        File logdirectory = new File(ReadConfig.value(AppConf.APP_LOG_PATH));
        if(!logdirectory.isDirectory()){
        	logdirectory.mkdir();
        }
        
        File tempFile = new File(tmpFileName);
        PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(tempFile));
			
			String str = "{\n";
			str += "\t\"checks\" : [\n";
			List<Chequedtls> chks = Chequedtls.retrieveSpecAll(" AND (chk.date_disbursement>='2023-01-01' AND chk.date_disbursement<='2023-12-31')");
			int totalSize = chks.size();
			int count = 1;
	        for(Chequedtls p : chks) {
	        	str +="\t\t{\n";
	        	
	        	str += "\"id\" : \"" + p.getCheque_id() + "\",\n";
	        	str += "\"checkNumber\" : \"" + p.getCheckNo() + "\",\n";
	        	str += "\"dateTrans\" : \"" + DateUtils.convertDateToMonthDayYear(p.getDate_disbursement()) + "\",\n";
	        	str += "\"fundName\" : \"" + p.getFundName() + "\",\n";
	        	str += "\"payor\" : \"" + p.getPayToTheOrderOf().trim().replace("\"", "'") + "\",\n";
	        	str += "\"department\" : \"" + p.getOffice().getName() + "\",\n";
	        	str += "\"mooeName\" : \"" + p.getMoe().getDescription() + "\",\n";
	        	str += "\"amount\" : \"" + Currency.formatAmount(p.getAmount()) + "\"\n";
	        	
	        	if(count==1 && count==totalSize) {
	        		str+="\t\t}\n";
	        	}else if(count>1 && count==totalSize) {
	        		str+="\t\t}\n";
	        	}else {
	        		str+="\t\t},\n";
	        	}
	        	
	        	
	        	count++;
	        }
	        str+="\t\t]\n";
	        str+="}\n";
	        
	        pw.println(str);	
	        pw.flush();
	        pw.close();
	        System.out.println("completed...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        

        // Rename the new file to the filename the original file had.
        if (!tempFile.renameTo(originalFile))
            System.out.println("Could not rename file");
		
	}
	
	static void createBusinessJsonFile() {
		
		String FILE_LOG_NAME = "business";
		String FILE_LOG_TMP_NAME = "tmpbusiness";
		String EXT = ".json";
		
		String logpath = ReadConfig.value(AppConf.APP_LOG_PATH);
        
        String finalFile = logpath + FILE_LOG_NAME + EXT;
        String tmpFileName = logpath + FILE_LOG_TMP_NAME + EXT;
        
        File originalFile = new File(finalFile);
        
        //check log directory
        File logdirectory = new File(ReadConfig.value(AppConf.APP_LOG_PATH));
        if(!logdirectory.isDirectory()){
        	logdirectory.mkdir();
        }
        
        File tempFile = new File(tmpFileName);
        PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(tempFile));
			
			String str = "{\n";
			str += "\t\"business\" : [\n";
			List<BusinessPermit> bls = BusinessPermit.retrieve(" AND bz.isactivebusiness=1 ORDER BY bz.businessname", new String[0]);
			int totalSize = bls.size();
			int count = 1;
	        for(BusinessPermit p : bls) {
	        	str +="\t\t{\n";
	        	
	        	str += "\"id\" : \"" + p.getId() + "\",\n";
	        	str += "\"dateTrans\" : \"" + DateUtils.convertDateToMonthDayYear(p.getDateTrans()) + "\",\n";
	        	str += "\"controlNumber\" : \"" + p.getControlNo() + "\",\n";
	        	str += "\"type\" : \"" + p.getType() + "\",\n";
	        	str += "\"businessName\" : \"" + p.getBusinessName().trim().replace("\"", "'")+ "\",\n";
	        	str += "\"owner\" : \"" + p.getCustomer().getFullname().trim().replace("\"", "'") + "\",\n";
	        	str += "\"location\" : \"" + p.getBusinessAddress() + "\",\n";
	        	str += "\"category\" : \"" + p.getBusinessEngage() + "\",\n";
	        	str += "\"remarks\" : \"" + p.getMemoType() + "\"\n";
	        	
	        	if(count==1 && count==totalSize) {
	        		str+="\t\t}\n";
	        	}else if(count>1 && count==totalSize) {
	        		str+="\t\t}\n";
	        	}else {
	        		str+="\t\t},\n";
	        	}
	        	
	        	
	        	count++;
	        }
	        str+="\t\t]\n";
	        str+="}\n";
	        
	        pw.println(str);	
	        pw.flush();
	        pw.close();
	        System.out.println("completed...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        

        // Rename the new file to the filename the original file had.
        if (!tempFile.renameTo(originalFile))
            System.out.println("Could not rename file");
		
	}
	
static void createCollectionJsonFile() {
		
	
			Map<Integer, Map<Integer, Double>> mapYear = new LinkedHashMap<Integer, Map<Integer, Double>>();
			
			for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
				mapYear = CollectionInfo.retrieveYear(year,0,mapYear);
			}
			
			String str = "{\n";
			str += "\t\"collection\" : [\n";
			int count = 1;
			for(int year : mapYear.keySet()) {
				for(int form : mapYear.get(year).keySet()) {
					str +="\t\t{\n";
					str += "\"id\" : \"" + count + "\",\n";
					str += "\"formName\" : \"" + FormType.nameId(form) + "\",\n";
					str += "\"amount\" : \"" + Currency.formatAmount(mapYear.get(year).get(form)) + "\",\n";
					str += "\"month\" : \"" + "\",\n";
					str += "\"year\" : \"" + year + "\"\n";
					
					if(count>=1) {
						str +="\t\t},\n";
					}else {
						str +="\t\t}\n";
					}
					
					count++;
				}
			}
			str+="\t\t]\n";
	        str+="}\n";
	
	
		String FILE_LOG_NAME = "collection";
		String FILE_LOG_TMP_NAME = "tmpcollection";
		String EXT = ".json";
		
		String logpath = ReadConfig.value(AppConf.APP_LOG_PATH);
        
        String finalFile = logpath + FILE_LOG_NAME + EXT;
        String tmpFileName = logpath + FILE_LOG_TMP_NAME + EXT;
        
        File originalFile = new File(finalFile);
        
        //check log directory
        File logdirectory = new File(ReadConfig.value(AppConf.APP_LOG_PATH));
        if(!logdirectory.isDirectory()){
        	logdirectory.mkdir();
        }
        
        File tempFile = new File(tmpFileName);
        PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(tempFile));
			
	        pw.println(str);	
	        pw.flush();
	        pw.close();
	        System.out.println("completed...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        

        // Rename the new file to the filename the original file had.
        if (!tempFile.renameTo(originalFile))
            System.out.println("Could not rename file");
		
	}
	
}
