package com.italia.municipality.lakesebu.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.DateUtils;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/19/2019
 *
 */
public class RCDReader {

	public static String REPORT_PATH_XML = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue() + "xml" + AppConf.SEPERATOR.getValue();
	
	private String brisFile="";
	private String dateCreated="";
	private String fund="";
	private String accountablePerson="";
	private String seriesReport="";
	private List<RCDFormDetails> rcdFormDtls = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>()); 
	private List<RCDFormSeries> rcdFormSeries = new ArrayList<RCDFormSeries>();//Collections.synchronizedList(new ArrayList<RCDFormSeries>());
	private String beginningBalancesAmount="";
	private String addAmount="";
	private String lessAmount="";
	private String balanceAmount="";
	private String certificationPerson="";
	private String verifierPerson="";
	private String dateVerified="";
	private String treasurer="";
	
	private static String xmlFolder = AppConf.PRIMARY_DRIVE.getValue() + File.separator + AppConf.APP_CONFIG_FOLDER_NAME.getValue() +File.separator + AppConf.REPORT_FOLDER.getValue() + File.separator + "xml" + File.separator;
	
	public static void main(String[] args) {
		
		/*RCDReader rcd = new RCDReader();
		rcd.setBrisFile("marxmind");
		rcd.setDateCreated("June 20, 2019");
		rcd.setFund("GENERAL FUND");
		rcd.setAccountablePerson("MERVER VERGARA");
		rcd.setSeriesReport("2019-06-001");
		rcd.setBeginningBalancesAmount("100,000.00");
		rcd.setAddAmount("100,000.00");
		rcd.setLessAmount("0.00");
		rcd.setBalanceAmount("0.00");
		rcd.setCertificationPerson("MERVER VERGARA");
		rcd.setVerifierPerson("HENRY E. MAGBANUA");
		rcd.setDateVerified("6/20/2019");
		rcd.setTreasurer("FERDINAND L. LOPEZ");
		
		List<RCDFormDetails> fs = Collections.synchronizedList(new ArrayList<RCDFormDetails>());
			for(int i=0; i<10; i++) {
				RCDFormDetails f = new RCDFormDetails();
				f.setFormId(i+"");
				f.setName("CTC IND.");
				f.setSeriesFrom("00000001");
				f.setSeriesTo("00000050");
				f.setAmount("150,000.00");
				fs.add(f);
			}
		rcd.setRcdFormDtls(fs);
		
		List<RCDFormSeries> sr = Collections.synchronizedList(new ArrayList<RCDFormSeries>());
		for(int i=0; i<10; i++) {
			RCDFormSeries s = new RCDFormSeries();
			s.setId(i+"");
			s.setName("CTC IND.");
			
			s.setBeginningQty("50");
			s.setBeginningFrom("00000001");
			s.setBeginningTo("00000050");
			
			s.setReceiptQty("50");
			s.setReceiptFrom("00000001");
			s.setReceiptTo("00000050");
			
			s.setIssuedQty("50");
			s.setIssuedFrom("00000001");
			s.setIssuedTo("00000050");
			
			s.setEndingQty("50");
			s.setEndingFrom("00000001");
			s.setEndingTo("00000050");
			sr.add(s);
		}
		rcd.setRcdFormSeries(sr);
		RCDReader.saveXML(rcd, "mark", REPORT_PATH_XML);*/
		//RCDReader.saveXML(RCDReader.readXML(REPORT_PATH_XML +"test.xml"), "mark", REPORT_PATH_XML);
		List<RCDFormDetails> rs = RCDReader.readCashTicker("C:\\webtis\\reports\\xml\\Jessie Handatu-2019-06-046_TRUST FUND_CT.xml");
		for(RCDFormDetails s : rs) {
			System.out.println(s.getName() +" Php "+ s.getAmount());
		}
		
		RCDReader.saveCashTicket(rs, "Jessie Handatu-2019-06-047_TRUST FUND_CT", "C:\\webtis\\reports\\xml\\");
		
	}
	

	public static void saveCollectorMode(String value) {
		try {
		File dir = new File(GlobalVar.APP_CONF_DIR);
		dir.mkdir();
		String counterMsg = "collector-mode="+value;
		
		File counterFile = new File(GlobalVar.APP_CONF_DIR + GlobalVar.COLLECTOR_MODE_FILE);
		PrintWriter pw = new PrintWriter(new FileWriter(counterFile));
		pw.println(counterMsg);
		pw.flush();
		pw.close();
		}catch(Exception e) {}
	}
	
	public static String readCollectorMode() {
		try {
		Properties prop = new Properties();
		File dirProp = new File(GlobalVar.APP_CONF_DIR + GlobalVar.COLLECTOR_MODE_FILE);
		if(dirProp.exists()) {
			prop.load(new FileInputStream(dirProp));
			return prop.getProperty("collector-mode");
		}else {
			String counter = DateUtils.getCurrentYear() + "-" + (DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()) + "-#001";
			saveSummaryCounterSeries(counter);
			dirProp = new File(GlobalVar.APP_CONF_DIR + GlobalVar.COLLECTOR_MODE_FILE);
			prop.load(new FileInputStream(dirProp));
			return prop.getProperty("collector-mode");
		}
			
		}catch(Exception e) {}
		
		return "ON";
	}
	
	public static void saveSummaryCounterSeries(String value) {
		try {
		File dir = new File(xmlFolder);
		
		//if(!dir.isDirectory()) {
			dir.mkdir();
		//}
		String counterMsg = "counter="+value;
		
		//System.out.println("saveSummaryCounterSeries "+ xmlFolder + GlobalVar.COUNTER_REPORT_FILE); //"counter_report.tis");
		File counterFile = new File(xmlFolder + GlobalVar.COUNTER_REPORT_FILE);
		PrintWriter pw = new PrintWriter(new FileWriter(counterFile));
		pw.println(counterMsg);
		pw.flush();
		pw.close();
		}catch(Exception e) {}
	}
	public static String readCounterReportSeries() {
		try {
		Properties prop = new Properties();
		File dirProp = new File(xmlFolder + GlobalVar.COUNTER_REPORT_FILE);
		//System.out.println("readCounterReportSeries "+ xmlFolder + GlobalVar.COUNTER_REPORT_FILE);//"counter_report.tis");
		if(dirProp.exists()) {
			prop.load(new FileInputStream(dirProp));
			return prop.getProperty("counter");
		}else {
			String counter = DateUtils.getCurrentYear() + "-" + (DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()) + "-#001";
			saveSummaryCounterSeries(counter);
			dirProp = new File(xmlFolder + GlobalVar.COUNTER_REPORT_FILE);
			prop.load(new FileInputStream(dirProp));
			return prop.getProperty("counter");
		}
			
		}catch(Exception e) {}
		
		return "#001";
	}
	
	public static void saveCashTicket(List<RCDFormDetails> rs, String fileName, String fileSaveLocation) {
		System.out.println("saving cash ticket xml");
		try {
		
			File dir = new File(fileSaveLocation);
			if(!dir.isDirectory()) { dir.mkdir(); }
			
			File file = new File(fileSaveLocation + fileName + ".xml");
			org.dom4j.Document document = DocumentHelper.createDocument();
			org.dom4j.Element rcd = document.addElement("rcd");
			org.dom4j.Element frmDtls = rcd.addElement("form-details");
			int id=0;
			for(RCDFormDetails f : rs) {
				org.dom4j.Element node = frmDtls.addElement("form").addAttribute("id", id+"");
					    node.addElement("name").addText(f.getName());
					    node.addElement("amount").addText(f.getAmount());
					    id++;
			}
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileWriter(file),format);
			writer.write(document);
			writer.close();
		}catch(IOException e) {}
		/*
		 * 
		 * String tag="";
		 * 
		 * 
		 * try {
		 * 
		 * 
		 * if(!file.isDirectory()) { file.mkdir(); }
		 * 
		 * String tags="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		 * 
		 * tags += "<rcd>" +"\n"; tags += "\t<form-details>" +"\n";
		 * 
		 * int id=0; for(RCDFormDetails f : rs) { tags += "\t\t<form id=\""+ id
		 * +"\">\n"; tags +="\t\t\t<name>"+f.getName()+"</name>\n"; tags
		 * +="\t\t\t<amount>"+f.getAmount()+"</amount>\n"; tags +="\t\t</form>\n"; id++;
		 * } tags += "\t</form-details>" +"\n"; tags += "</rcd>" +"\n";
		 * 
		 * 
		 * 
		 * File xml = new File(fileSaveLocation + fileName + ".xml"); PrintWriter pw =
		 * new PrintWriter(new FileWriter(xml)); pw.println(tags); pw.flush();
		 * pw.close(); System.out.println("completed saving xml");
		 * 
		 * return tags; }catch(Exception e) {
		 * 
		 * }
		 */
	}
	
	
	public static void saveXML(RCDReader rcd, String fileName, String fileSaveLocation, boolean isForMonthly) {
		System.out.println("saving xml");
		File dir = new File(fileSaveLocation);
		
		if(!dir.isDirectory()) {
			dir.mkdir();
		}
		
		File xmlFile = new File(fileSaveLocation + fileName + ".xml");
		
			try {
				Document document = DocumentHelper.createDocument();
				Element root = document.addElement("rcd");
				root.addElement("bris-file").addText(rcd.getBrisFile());
				root.addElement("date-created").addText(rcd.getDateCreated());
				root.addElement("fund").addText(rcd.getFund());
				root.addElement("accountable-person").addText(rcd.getAccountablePerson());
				root.addElement("series-report").addText(rcd.getSeriesReport());
				root.addElement("beginning-balances").addText(rcd.getBeginningBalancesAmount());
				root.addElement("add").addText(rcd.getAddAmount());
				root.addElement("less").addText(rcd.getLessAmount());
				root.addElement("balance").addText(rcd.getBalanceAmount());
				root.addElement("certification-person").addText(rcd.getCertificationPerson());
				root.addElement("verification-person").addText(rcd.getVerifierPerson());
				root.addElement("date-verified").addText(rcd.getDateVerified());
				root.addElement("treasurer").addText(rcd.getTreasurer());
				
				Element frmDtls = root.addElement("form-details");
				int id=0;
				for(RCDFormDetails f : rcd.getRcdFormDtls()) {
					Element node = frmDtls.addElement("form").addAttribute("id", id+"");
						    node.addElement("name").addText(f.getName());
						    node.addElement("series-from").addText(f.getSeriesFrom());
						    node.addElement("series-to").addText(f.getSeriesTo());
						    node.addElement("amount").addText(f.getAmount());
						    id++;
				}
				
				Element seriesDtls = root.addElement("form-series");
				id=0;
				for(RCDFormSeries s : rcd.getRcdFormSeries()) {
					Element node = seriesDtls.addElement("line").addAttribute("id", id+"");
						    node.addElement("name").addText(s.getName());
						    
						    node.addElement("beginning-qty").addText(s.getBeginningQty());
						    node.addElement("beginning-from").addText(s.getBeginningFrom());
						    node.addElement("beginning-to").addText(s.getBeginningTo());
						    
						    node.addElement("receipt-qty").addText(s.getReceiptQty());
						    node.addElement("receipt-from").addText(s.getReceiptFrom());
						    node.addElement("receipt-to").addText(s.getReceiptTo());
						    
						    node.addElement("issued-qty").addText(s.getIssuedQty());
						    node.addElement("issued-from").addText(s.getIssuedFrom());
						    node.addElement("issued-to").addText(s.getIssuedTo());
						    
						    node.addElement("ending-qty").addText(s.getEndingQty());
						    node.addElement("ending-from").addText(s.getEndingFrom());
						    node.addElement("ending-to").addText(s.getEndingTo());
						    
						    if(isForMonthly) {
						    	node.addElement("remarks").addText(s.getRemarks());
						    	node.addElement("collector").addText(s.getCollector());
							}
						    
						    id++;
				}
				
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(new FileWriter(xmlFile),format);
				writer.write(document);
				writer.close();
				
			}catch(Exception e) {}
		
	}
	
	/*
	 * public static String saveXML(RCDReader rcd, String fileName, String
	 * fileSaveLocation, boolean isForMonthly) { System.out.println("saving xml");
	 * try { File file = new File(fileSaveLocation);
	 * 
	 * if(!file.isDirectory()) { file.mkdir(); }
	 * 
	 * String tags="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	 * 
	 * tags += "<rcd>" +"\n"; tags += "\t<bris-file>"+rcd.getBrisFile()
	 * +"</bris-file>\n"; tags +=
	 * "\t<date-created>"+rcd.getDateCreated()+"</date-created>\n"; tags +=
	 * "\t<fund>"+rcd.getFund()+"</fund>\n"; tags +=
	 * "\t<accountable-person>"+rcd.getAccountablePerson()+
	 * "</accountable-person>\n"; tags +=
	 * "\t<series-report>"+rcd.getSeriesReport()+"</series-report>\n"; tags +=
	 * "\t<beginning-balances>"+rcd.getBeginningBalancesAmount()+
	 * "</beginning-balances>\n"; tags += "\t<add>"+rcd.getAddAmount()+"</add>\n";
	 * tags += "\t<less>"+rcd.getLessAmount()+"</less>\n"; tags +=
	 * "\t<balance>"+rcd.getBalanceAmount()+"</balance>\n"; tags +=
	 * "\t<certification-person>"+rcd.getCertificationPerson()+
	 * "</certification-person>\n"; tags +=
	 * "\t<verification-person>"+rcd.getVerifierPerson()+"</verification-person>\n";
	 * tags += "\t<date-verified>"+rcd.getDateVerified()+"</date-verified>\n"; tags
	 * += "\t<treasurer>"+rcd.getTreasurer()+"</treasurer>\n";
	 * 
	 * tags += "\t<form-details>" +"\n"; int id=0; for(RCDFormDetails f :
	 * rcd.getRcdFormDtls()) { tags += "\t\t<form id=\""+ id +"\">\n"; tags
	 * +="\t\t\t<name>"+f.getName()+"</name>\n"; tags
	 * +="\t\t\t<series-from>"+f.getSeriesFrom()+"</series-from>\n"; tags
	 * +="\t\t\t<series-to>"+f.getSeriesTo()+"</series-to>\n"; tags
	 * +="\t\t\t<amount>"+f.getAmount()+"</amount>\n"; tags +="\t\t</form>\n"; id++;
	 * } tags += "\t</form-details>" +"\n";
	 * 
	 * tags += "\t<form-series>" +"\n"; id=0; for(RCDFormSeries s :
	 * rcd.getRcdFormSeries()) { tags += "\t\t<line id=\""+ id +"\">\n"; tags
	 * +="\t\t\t<name>"+ s.getName()+"</name>\n";
	 * 
	 * tags +="\t\t\t<beginning-qty>"+ s.getBeginningQty()+"</beginning-qty>\n";
	 * tags +="\t\t\t<beginning-from>"+ s.getBeginningFrom()+"</beginning-from>\n";
	 * tags +="\t\t\t<beginning-to>"+ s.getBeginningTo()+"</beginning-to>\n";
	 * 
	 * tags +="\t\t\t<receipt-qty>"+ s.getReceiptQty()+"</receipt-qty>\n"; tags
	 * +="\t\t\t<receipt-from>"+ s.getReceiptFrom()+"</receipt-from>\n"; tags
	 * +="\t\t\t<receipt-to>"+ s.getReceiptTo()+"</receipt-to>\n";
	 * 
	 * tags +="\t\t\t<issued-qty>"+ s.getIssuedQty()+"</issued-qty>\n"; tags
	 * +="\t\t\t<issued-from>"+ s.getIssuedFrom()+"</issued-from>\n"; tags
	 * +="\t\t\t<issued-to>"+ s.getIssuedTo()+"</issued-to>\n";
	 * 
	 * tags +="\t\t\t<ending-qty>"+ s.getEndingQty()+"</ending-qty>\n"; tags
	 * +="\t\t\t<ending-from>"+ s.getEndingFrom()+"</ending-from>\n"; tags
	 * +="\t\t\t<ending-to>"+ s.getEndingTo()+"</ending-to>\n";
	 * 
	 * if(isForMonthly) { tags +="\t\t\t<remarks>"+ s.getRemarks()+"</remarks>\n";
	 * tags +="\t\t\t<collector>"+ s.getCollector()+"</collector>\n"; } tags
	 * +="\t\t</line>\n"; id++; } tags += "\t</form-series>" +"\n"; tags += "</rcd>"
	 * +"\n";
	 * 
	 * 
	 * 
	 * File xml = new File(fileSaveLocation + fileName + ".xml"); PrintWriter pw =
	 * new PrintWriter(new FileWriter(xml)); pw.println(tags); pw.flush();
	 * pw.close(); System.out.println("completed saving xml");
	 * 
	 * return tags; }catch(Exception e) { e.printStackTrace(); } return null; }
	 */
	
	public static RCDReader readXML(String xml, boolean isForMonthly) {
				
				System.out.println("reading xml at >> " + xml);
				
				RCDReader rcd = new RCDReader();
				
				File xmlFile = new File(xml);
				if(xmlFile.exists()){
					try {
					SAXReader reader = new SAXReader();
					Document document = reader.read(xmlFile);
					
					Node node = document.selectSingleNode("/rcd");
					rcd.setBrisFile(node.selectSingleNode("bris-file").getText());
					rcd.setDateCreated(node.selectSingleNode("date-created").getText());
					rcd.setFund(node.selectSingleNode("fund").getText());
					rcd.setAccountablePerson(node.selectSingleNode("accountable-person").getText());
					rcd.setSeriesReport(node.selectSingleNode("series-report").getText());
					rcd.setBeginningBalancesAmount(node.selectSingleNode("beginning-balances").getText());
					rcd.setAddAmount(node.selectSingleNode("add").getText());
					rcd.setLessAmount(node.selectSingleNode("less").getText());
					rcd.setBalanceAmount(node.selectSingleNode("balance").getText());
					rcd.setCertificationPerson(node.selectSingleNode("certification-person").getText());
					rcd.setVerifierPerson(node.selectSingleNode("verification-person").getText());
					rcd.setDateVerified(node.selectSingleNode("date-verified").getText());
					rcd.setTreasurer(node.selectSingleNode("treasurer").getText());
					
					////////////////////FORM DETAILS/////////////////////////
					List<RCDFormDetails> frmDtls = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>());
					List<Node> dtls = document.selectNodes("/rcd/form-details/form");
					for(Node n : dtls) {
						RCDFormDetails d = new RCDFormDetails();
		            	d.setFormId(n.valueOf("@id"));
		            	d.setName(n.selectSingleNode("name").getText());
		            	d.setSeriesFrom(n.selectSingleNode("series-from").getText());
		            	d.setSeriesTo(n.selectSingleNode("series-to").getText());
		            	d.setAmount(n.selectSingleNode("amount").getText());
		            	frmDtls.add(d);
					}
					rcd.setRcdFormDtls(frmDtls);
					
					///////////////////SERIES////////////////////////////////
					List<RCDFormSeries> srsDtls = new ArrayList<RCDFormSeries>();//Collections.synchronizedList(new ArrayList<RCDFormSeries>());
					List<Node> series = document.selectNodes("/rcd/form-series/line");
					
					for(Node n : series) {
						RCDFormSeries d = new RCDFormSeries();
						
		            	d.setId(n.valueOf("@id"));
		            	d.setName(n.selectSingleNode("name").getText());
		            	
		            	d.setBeginningQty(n.selectSingleNode("beginning-qty").getText());
		            	d.setBeginningFrom(n.selectSingleNode("beginning-from").getText());
		            	d.setBeginningTo(n.selectSingleNode("beginning-to").getText());
		            	
		            	d.setReceiptQty(n.selectSingleNode("receipt-qty").getText());
		            	d.setReceiptFrom(n.selectSingleNode("receipt-from").getText());
		            	d.setReceiptTo(n.selectSingleNode("receipt-to").getText());
		            	
		            	d.setIssuedQty(n.selectSingleNode("issued-qty").getText());
		            	d.setIssuedFrom(n.selectSingleNode("issued-from").getText());
		            	d.setIssuedTo(n.selectSingleNode("issued-to").getText());
		            	
		            	d.setEndingQty(n.selectSingleNode("ending-qty").getText());
		            	d.setEndingFrom(n.selectSingleNode("ending-from").getText());
		            	d.setEndingTo(n.selectSingleNode("ending-to").getText());
		            	
		            	if(isForMonthly) {
			            	d.setRemarks(n.selectSingleNode("remarks").getText());
			            	d.setCollector(n.selectSingleNode("collector").getText());
		            	}
		            	srsDtls.add(d);
					}
					rcd.setRcdFormSeries(srsDtls);
					
					}catch(DocumentException e) {}
				}
				
				return rcd;
	}	
	
	/*
	 * public static RCDReader readXML(String xml, boolean isForMonthly) {
	 * 
	 * System.out.println("reading xml at >> " + xml); try { RCDReader rcd = new
	 * RCDReader();
	 * 
	 * File fXmlFile = new File(xml); DocumentBuilderFactory dbFactory =
	 * DocumentBuilderFactory.newInstance(); DocumentBuilder dBuilder =
	 * dbFactory.newDocumentBuilder(); Document doc = dBuilder.parse(fXmlFile);
	 * 
	 * NodeList ls = doc.getElementsByTagName("rcd"); int size=ls.getLength();
	 * for(int i=0; i<size; i++){ Node n = ls.item(i);
	 * 
	 * if(n.getNodeType() == Node.ELEMENT_NODE){ Element tag = (Element)n;
	 * 
	 * rcd.setBrisFile(tag.getElementsByTagName("bris-file").item(0).getTextContent(
	 * )); rcd.setDateCreated(tag.getElementsByTagName("date-created").item(0).
	 * getTextContent());
	 * rcd.setFund(tag.getElementsByTagName("fund").item(0).getTextContent());
	 * rcd.setAccountablePerson(tag.getElementsByTagName("accountable-person").item(
	 * 0).getTextContent());
	 * rcd.setSeriesReport(tag.getElementsByTagName("series-report").item(0).
	 * getTextContent());
	 * 
	 * rcd.setBeginningBalancesAmount(tag.getElementsByTagName("beginning-balances")
	 * .item(0).getTextContent());
	 * rcd.setAddAmount(tag.getElementsByTagName("add").item(0).getTextContent());
	 * rcd.setLessAmount(tag.getElementsByTagName("less").item(0).getTextContent());
	 * rcd.setBalanceAmount(tag.getElementsByTagName("balance").item(0).
	 * getTextContent());
	 * rcd.setCertificationPerson(tag.getElementsByTagName("certification-person").
	 * item(0).getTextContent());
	 * rcd.setVerifierPerson(tag.getElementsByTagName("verification-person").item(0)
	 * .getTextContent());
	 * rcd.setDateVerified(tag.getElementsByTagName("date-verified").item(0).
	 * getTextContent());
	 * rcd.setTreasurer(tag.getElementsByTagName("treasurer").item(0).getTextContent
	 * ());
	 * 
	 * ////////////////////FORM DETAILS/////////////////////////
	 * List<RCDFormDetails> frmDtls = Collections.synchronizedList(new
	 * ArrayList<RCDFormDetails>());
	 * 
	 * NodeList dtls = tag.getElementsByTagName("form");
	 * 
	 * for (int temp = 0; temp < dtls.getLength(); temp++) {
	 * //System.out.println("details lenght = " + dtls.getLength()); Node nNode =
	 * dtls.item(temp); if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 * 
	 * Element eDtls = (Element) nNode;
	 * 
	 * RCDFormDetails d = new RCDFormDetails();
	 * d.setFormId(eDtls.getAttribute("id"));
	 * d.setName(eDtls.getElementsByTagName("name").item(0).getTextContent());
	 * d.setSeriesFrom(eDtls.getElementsByTagName("series-from").item(0).
	 * getTextContent());
	 * d.setSeriesTo(eDtls.getElementsByTagName("series-to").item(0).getTextContent(
	 * ));
	 * d.setAmount(eDtls.getElementsByTagName("amount").item(0).getTextContent());
	 * frmDtls.add(d); } } rcd.setRcdFormDtls(frmDtls);
	 * 
	 * 
	 * ///////////////////SERIES//////////////////////////////// List<RCDFormSeries>
	 * srsDtls = Collections.synchronizedList(new ArrayList<RCDFormSeries>());
	 * 
	 * NodeList series = tag.getElementsByTagName("line");
	 * 
	 * for (int temp = 0; temp < series.getLength(); temp++) {
	 * //System.out.println("details lenght = " + series.getLength()); Node nNode =
	 * series.item(temp); if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 * 
	 * Element eDtls = (Element) nNode;
	 * 
	 * RCDFormSeries d = new RCDFormSeries(); d.setId(eDtls.getAttribute("id"));
	 * d.setName(eDtls.getElementsByTagName("name").item(0).getTextContent());
	 * 
	 * d.setBeginningQty(eDtls.getElementsByTagName("beginning-qty").item(0).
	 * getTextContent());
	 * d.setBeginningFrom(eDtls.getElementsByTagName("beginning-from").item(0).
	 * getTextContent());
	 * d.setBeginningTo(eDtls.getElementsByTagName("beginning-to").item(0).
	 * getTextContent());
	 * 
	 * d.setReceiptQty(eDtls.getElementsByTagName("receipt-qty").item(0).
	 * getTextContent());
	 * d.setReceiptFrom(eDtls.getElementsByTagName("receipt-from").item(0).
	 * getTextContent());
	 * d.setReceiptTo(eDtls.getElementsByTagName("receipt-to").item(0).
	 * getTextContent());
	 * 
	 * d.setIssuedQty(eDtls.getElementsByTagName("issued-qty").item(0).
	 * getTextContent());
	 * d.setIssuedFrom(eDtls.getElementsByTagName("issued-from").item(0).
	 * getTextContent());
	 * d.setIssuedTo(eDtls.getElementsByTagName("issued-to").item(0).getTextContent(
	 * ));
	 * 
	 * d.setEndingQty(eDtls.getElementsByTagName("ending-qty").item(0).
	 * getTextContent());
	 * d.setEndingFrom(eDtls.getElementsByTagName("ending-from").item(0).
	 * getTextContent());
	 * d.setEndingTo(eDtls.getElementsByTagName("ending-to").item(0).getTextContent(
	 * ));
	 * 
	 * if(isForMonthly) {
	 * d.setRemarks(eDtls.getElementsByTagName("remarks").item(0).getTextContent());
	 * d.setCollector(eDtls.getElementsByTagName("collector").item(0).getTextContent
	 * ()); } srsDtls.add(d); } } rcd.setRcdFormSeries(srsDtls); }
	 * 
	 * }
	 * 
	 * return rcd; }catch(Exception e) {}
	 * 
	 * return null; }
	 */
	
public static List<RCDFormDetails> readCashTicker(String xml) {
		
		System.out.println("reading casticket xml at >> " + xml);
		try {
			////////////////////FORM DETAILS/////////////////////////
			List<RCDFormDetails> frmDtls = new ArrayList<RCDFormDetails>();//Collections.synchronizedList(new ArrayList<RCDFormDetails>());
			
			
			File xmlFile = new File(xml);
			if(xmlFile.exists()){
				try {
				SAXReader reader = new SAXReader();
				Document document = reader.read(xmlFile);
				
				List<Node> nodes = document.selectNodes("/rcd/form-details/form");
				for(Node n : nodes) {
					RCDFormDetails d = new RCDFormDetails();
	            	d.setFormId(n.valueOf("@id"));
	            	d.setName(n.selectSingleNode("name").getText());
	            	d.setAmount(n.selectSingleNode("amount").getText());
	            	frmDtls.add(d);
				}
				
				
				}catch(DocumentException e) {}	
			}
			
			return frmDtls;
		}catch(Exception e) {}
		
		return null;
	}
	
	public String getBrisFile() {
		if(brisFile==null) {
			brisFile="";
		}
		return brisFile;
	}

	public void setBrisFile(String brisFile) {
		this.brisFile = brisFile;
	}

	public String getDateCreated() {
		if(dateCreated==null) {
			dateCreated="";
		}
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getFund() {
		if(fund==null) {
			fund = "";
		}
		return fund;
	}

	public void setFund(String fund) {
		this.fund = fund;
	}

	public String getAccountablePerson() {
		if(accountablePerson==null) {
			accountablePerson="";
		}
		return accountablePerson;
	}

	public void setAccountablePerson(String accountablePerson) {
		this.accountablePerson = accountablePerson;
	}

	public String getSeriesReport() {
		if(seriesReport==null) {
			seriesReport="";
		}
		return seriesReport;
	}

	public void setSeriesReport(String seriesReport) {
		this.seriesReport = seriesReport;
	}

	public List<RCDFormDetails> getRcdFormDtls() {
		return rcdFormDtls;
	}

	public void setRcdFormDtls(List<RCDFormDetails> rcdFormDtls) {
		this.rcdFormDtls = rcdFormDtls;
	}

	public List<RCDFormSeries> getRcdFormSeries() {
		return rcdFormSeries;
	}

	public void setRcdFormSeries(List<RCDFormSeries> rcdFormSeries) {
		this.rcdFormSeries = rcdFormSeries;
	}

	public String getBeginningBalancesAmount() {
		if(beginningBalancesAmount==null) {
			beginningBalancesAmount="";
		}
		return beginningBalancesAmount;
	}

	public void setBeginningBalancesAmount(String beginningBalancesAmount) {
		this.beginningBalancesAmount = beginningBalancesAmount;
	}

	public String getAddAmount() {
		if(addAmount==null) {
			addAmount="";
		}
		return addAmount;
	}

	public void setAddAmount(String addAmount) {
		this.addAmount = addAmount;
	}

	public String getLessAmount() {
		if(lessAmount==null) {
			lessAmount="";
		}
		return lessAmount;
	}

	public void setLessAmount(String lessAmount) {
		this.lessAmount = lessAmount;
	}

	public String getBalanceAmount() {
		if(balanceAmount==null) {
			balanceAmount="";
		}
		return balanceAmount;
	}

	public void setBalanceAmount(String balanceAmount) {
		this.balanceAmount = balanceAmount;
	}

	public String getCertificationPerson() {
		if(certificationPerson==null) {
			certificationPerson="";
		}
		return certificationPerson;
	}

	public void setCertificationPerson(String certificationPerson) {
		this.certificationPerson = certificationPerson;
	}

	public String getVerifierPerson() {
		if(verifierPerson==null) {
			verifierPerson="";
		}
		return verifierPerson;
	}

	public void setVerifierPerson(String verifierPerson) {
		this.verifierPerson = verifierPerson;
	}

	public String getDateVerified() {
		if(dateVerified==null) {
			dateVerified="";
		}
		return dateVerified;
	}

	public void setDateVerified(String dateVerified) {
		this.dateVerified = dateVerified;
	}

	public String getTreasurer() {
		if(treasurer==null) {
			treasurer="";
		}
		return treasurer;
	}

	public void setTreasurer(String treasurer) {
		this.treasurer = treasurer;
	}

}