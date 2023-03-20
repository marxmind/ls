package com.italia.municipality.lakesebu.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.bean.SessionBean;
import com.italia.municipality.lakesebu.database.Conf;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.CheckServerConnection;
import com.italia.municipality.lakesebu.utils.StringUtils;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ChequeXML {
	
	private String date;
	private String accountNumber;
	private String checkNumber;
	private String accountName;
	private String bankName;
	private String amount;
	private String payTo;
	private String inWords;
	private String user;
	private String edited;
	private String treasurer;
	private String official;
	private String isActive;
	private String status;
	private String remarks;
	private String advice;
	private String office;
	private String mooe;
	
	public static void saveCheckXMLForUpload(Chequedtls chk) {
		//create directory if not present
		File dirxml = new File(GlobalVar.CHECK_XML_FOLDER);
		dirxml.mkdir();
		File dir = new File(GlobalVar.CHECK_XML);
		dir.mkdir();
		File file = new File(GlobalVar.CHECK_XML + chk.getAccntNumber() +"-"+ chk.getCheckNo() +".xml");
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			StringBuilder sb = new StringBuilder();
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			sb.append(xml);sb.append("\n");
			sb.append("<cheque>");sb.append("\n");
			sb.append("\t<date>"+ chk.getDate_disbursement() +"</date>");sb.append("\n");
			sb.append("\t<accountNumber>"+ chk.getAccntNumber() +"</accountNumber>");sb.append("\n");
			sb.append("\t<checkNumber>"+ chk.getCheckNo() +"</checkNumber>");sb.append("\n");
			sb.append("\t<accountName>"+ chk.getAccntName() +"</accountName>");sb.append("\n");
			sb.append("\t<bankName>"+ chk.getBankName() +"</bankName>");sb.append("\n");
			sb.append("\t<amount>"+ chk.getAmount() +"</amount>");sb.append("\n");
			sb.append("\t<payto>"+ StringUtils.convertToUTF8(chk.getPayToTheOrderOf()) +"</payto>");sb.append("\n");
			sb.append("\t<inwords>"+ chk.getAmountInWOrds() +"</inwords>");sb.append("\n");
			sb.append("\t<user>"+ chk.getProcessBy() +"</user>");sb.append("\n");
			sb.append("\t<edited>"+ chk.getDate_edited() +"</edited>");sb.append("\n");
			sb.append("\t<timestamp>"+ chk.getDate_created() +"</timestamp>");sb.append("\n");
			sb.append("\t<treasurer>"+ chk.getSignatory1() +"</treasurer>");sb.append("\n");
			sb.append("\t<official>"+ chk.getSignatory2() +"</official>");sb.append("\n");
			sb.append("\t<active>"+ chk.getIsActive() +"</active>");sb.append("\n");
			sb.append("\t<status>"+ chk.getStatus() +"</status>");sb.append("\n");
			sb.append("\t<remarks>"+ chk.getRemarks() +"</remarks>");sb.append("\n");
			sb.append("\t<advice>"+ chk.getHasAdvice() +"</advice>");sb.append("\n");
			sb.append("\t<office>"+ chk.getOffice().getId() +"</office>");sb.append("\n");
			sb.append("\t<mooe>"+ chk.getMoe().getId() +"</mooe>");sb.append("\n");
			sb.append("</cheque>");sb.append("\n");
			pw.println(sb.toString());
			pw.flush();
			pw.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
	
	public static void retrieveXMLforServerSaving() {
		File folder = new File(GlobalVar.CHECK_XML);
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	ChequeXML xml = null;
		    	try{xml = readXML(file.getName());}catch(Exception e) {e.printStackTrace();}
		    	
		    	System.out.println("Check check no:"+xml.getCheckNumber() + " pay to: " + xml.getPayTo());
		    	
		    	if(xml!=null && Chequedtls.checkIfCheckNumberExist(xml.getCheckNumber())) {
		    		
		    		Chequedtls check = convertXMLThenSave(xml);
		    		if(check!=null && check.getCheque_id()>0) {
		    			file.delete();
		    			System.out.println("file is deleted and processed");
		    		}else {
		    			System.out.println("error in saving file:"+file.getName());
		    			errorFile(file);
		    		}
		    		
		    	}else {
		    		System.out.println("unproccess file:"+file.getName());
		    		unprocessedFile(file);
		    	}
		    }
		}	    
	}	
	
	private static void unprocessedFile(File unprocessed) {
		File fileDir = new File(GlobalVar.CHECK_XML_UNPROCCESSED);
		fileDir.mkdir();
		
		File newFile = new File(GlobalVar.CHECK_XML_UNPROCCESSED + unprocessed.getName());
		if(copyFile(unprocessed, newFile)) {
			unprocessed.delete();
			System.out.println("unprocessed file has been transfered to " + fileDir.getAbsolutePath() + " file: " + unprocessed.getName());
		}
		
	}
	
	private static void errorFile(File error) {
		File fileDir = new File(GlobalVar.CHECK_XML_ERROR);
		fileDir.mkdir();
		
		File newFile = new File(GlobalVar.CHECK_XML_ERROR + error.getName());
		if(copyFile(error, newFile)) {
			error.delete();
			System.out.println("Error fi le has been transfered to " + fileDir.getAbsolutePath() + " file: " + error.getName());
		}
		
	}
	
	private static boolean copyFile(File source, File destination) {
		 InputStream inStream = null;
	     OutputStream outStream = null;
	      
	       try
	       {
	    	   
	    	   inStream = new FileInputStream(source);
	           outStream = new FileOutputStream(destination);
	           
	           byte[] buffer = new byte[1024];
	           
	           int length;
	           while ((length = inStream.read(buffer)) > 0)
	           {
	              outStream.write(buffer, 0, length);
	           }
	 
	           if (inStream != null)
	              inStream.close();
	           if (outStream != null)
	              outStream.close();
	 
	           System.out.println("File Copied..");
	    	   return true;
	       }
	       catch(IOException e)
	       {
	          e.printStackTrace();
	       }
	       return false;
	}
	
	public static Chequedtls convertXMLThenSave(ChequeXML xml) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		Chequedtls check = null;
		try {
		check = Chequedtls.builder()
				.date_disbursement(xml.getDate())
				.accntNumber(xml.getAccountNumber())
				.checkNo(xml.getCheckNumber())
				.accntName(xml.getAccountName())
				.amount(Double.valueOf(xml.getAmount().replace(",", "")))
				.payToTheOrderOf(xml.getPayTo())
				.amountInWOrds(xml.getInWords())
				.processBy(xml.getUser())
				.date_edited(dateFormat.format(date))
				.signatory1(Integer.valueOf(xml.getTreasurer()))
				.signatory2(Integer.valueOf(xml.getOfficial()))
				.isActive(Integer.valueOf(xml.getIsActive()))
				.status(Integer.valueOf(xml.getStatus()))
				.remarks(xml.getRemarks())
				.hasAdvice(Integer.valueOf(xml.getAdvice()))
				.office(Offices.builder().id(Long.valueOf(xml.getOffice())).build())
				.moe(Mooe.builder().id(Long.valueOf(xml.getMooe())).build())
				.build();
		System.out.println("Saving check");
		check = Chequedtls.save(check);
		System.out.println("successfully save check");
		}catch(Exception e) {e.printStackTrace();}
		return check;
	}
	
	private static ChequeXML readXML(String fileName) {
		System.out.println("read check xml readXML");
		ChequeXML xml = null;
		File xmlFile = new File(GlobalVar.CHECK_XML + fileName);
		if(xmlFile.exists()){
				System.out.println("reading now xml....");
				xml = new ChequeXML();
				SAXReader reader = new SAXReader();
				Document document;
				try {
					document = reader.read(xmlFile);
					Node node = document.selectSingleNode("/cheque");
					int count=0;
					try{xml.setDate(node.selectSingleNode("date").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setAccountNumber(node.selectSingleNode("accountNumber").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setCheckNumber(node.selectSingleNode("checkNumber").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setAccountName(node.selectSingleNode("accountName").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setBankName(node.selectSingleNode("bankName").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setAmount(node.selectSingleNode("amount").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					
					try{xml.setPayTo(StringUtils.convertToUTF8(node.selectSingleNode("payto").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					
					try{xml.setInWords(node.selectSingleNode("inwords").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setUser(node.selectSingleNode("user").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setEdited(node.selectSingleNode("edited").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setTreasurer(node.selectSingleNode("treasurer").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setOfficial(node.selectSingleNode("official").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setIsActive(node.selectSingleNode("active").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setStatus(node.selectSingleNode("status").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setRemarks(node.selectSingleNode("remarks").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setAdvice(node.selectSingleNode("advice").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setOffice(node.selectSingleNode("office").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{xml.setMooe(node.selectSingleNode("mooe").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					System.out.println("success reading now xml....");
				}catch(Exception e) {e.printStackTrace();}
		}
		return xml;
	}
	
	public static boolean checkingConnection() {
		String val = ReadConfig.value(AppConf.SERVER_LOCAL);
		if("true".equalsIgnoreCase(val)) {//server is local if true
			Conf conf = Conf.getInstance();
			if(CheckServerConnection.pingIp(conf.getServerDatabaseIp())) {//check if ip reachable else assigned localhost
				//check if server is accessible
				System.out.println("server ip " + conf.getServerDatabaseIp() + " is accessible...");
				activateSession(true);
				return true;
			}else {
				System.out.println("server " + conf.getServerDatabaseIp() + " is not accessible...");
				return false;
			}	
			
		}
		return false;
	}
	public static void activateSession(boolean activate) {
		HttpSession session = SessionBean.getSession();
		if(activate) {
			session.setAttribute("server-local", "false");//changing session to false to connect to remote server the database connection @see Conf (WebTISDatabaseConnection, TaxDatabaseConnection...)
		}else {
			String val = ReadConfig.value(AppConf.SERVER_LOCAL);
			session.setAttribute("server-local", val);
			System.out.println("return to normal connection " + val);
		}
	}
	
}
