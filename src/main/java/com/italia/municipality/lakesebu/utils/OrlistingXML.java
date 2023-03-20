package com.italia.municipality.lakesebu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import com.italia.municipality.lakesebu.bean.SessionBean;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.ORListing;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.PaymentName;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.Conf;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.Customer;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Purok;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrlistingXML {

	private String reg;
	private String firstName;
	private String middleName;
	private String lastName;
	private String fullName;
	private String birthDate;
	private String civilStatus;
	
	private String userId;
	
	private String dateTrans;
	private String formType;
	private String isActive;
	private String customerId;
	private String collectorId;
	private String orStatus;
	private String orNumber;
	private String formInfo;
	
	//private List<ORNameList> orlisting;
	private List<PaymentName> paynames;
	
	private static String[] special = {"&","�","�"};
	private static String[] counterSpecialName = {"ampersand","enyebig","enyesmall"};
	
	public static void main(String[] args) {
		
		String name = "FRANCISCO ESPA�OLA";
		String val = OrlistingXML.convertSpecialChar(name);
		System.out.println("Convert: " + val);
		System.out.println("Rollback: " + OrlistingXML.convertSpecialCharToRegular(val));
		
	}
	
	public static Map<String, String> mapChar(){
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("&", "ampersand");
		map.put("�", "enyebig");
		map.put("�", "enyesmall");
		
		return map;
	}
	
	public static String convertSpecialChar(String val) {
		
		String tmp = "";
		for(char c : val.toCharArray()) {
			if(mapChar().containsKey(c+"")) {
				tmp += mapChar().get(c+"");
			}else {
				tmp += c+"";
			}
		}
		
		return tmp;
	}
	
	public static String convertSpecialCharToRegular(String val) {
		
		int count = 0;
		for(String s : counterSpecialName) {
			val = val.replace(s, special[count++]); 
		}
		
		return val;
	}
	
	public static String convert(String val, boolean isConvert) {
		if(isConvert) {
			return convertSpecialChar(val);
		}else {
			return convertSpecialCharToRegular(val);
		}
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
	
	
	public static boolean checkORTransactionIfNotExist(OrlistingXML xml) {
		String sql = " AND orl.isactiveor=1 AND orl.ordatetrans=? AND orl.ornumber=? AND orl.aform=? AND cuz.fullname=?";
		String[] params = new String[4];
		params[0] = xml.getDateTrans();
		params[1] = xml.getOrNumber();
		params[2] = xml.getFormType();
		params[3] = xml.getFullName();
		
		List<ORListing> ors = ORListing.retrieve(sql, params);
		if(ors!=null && ors.size()>0) {
			return false;
		}
		
		return true;
	}
	
	public static void retrieveXMLforServerSaving() {
		File folder = new File(GlobalVar.COMMIT_XML);
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	OrlistingXML xml = null;
		    	
		    	try{xml = readXML(file.getName());}catch(Exception e) {}
//		    	System.out.println("Fullname:" + xml.getFullName() + " on file " + file.getName());
		    	
		    	if(xml!=null && checkORTransactionIfNotExist(xml)) {//return true if exist
		    	
		    	UserDtls user = new UserDtls();
		    	user.setUserdtlsid(Long.valueOf(xml.getUserId()));
		    	
		    	Customer cus = customer(xml, user);
		    	
		    	if(cus.getId()>0) {
		    		
		    		ORListing or = orlisting(xml, cus);
		    		
			    	if(or.getId()>0) {
				    	
			    		boolean hasprocessed = paymentNames(or, xml, cus)? file.delete() : false;
			    		if(hasprocessed) {
			    			System.out.println("file is deleted and processed");
			    		}else {
			    			System.out.println("failed to processed official receipt....rollback changes... removing id on orlisting id="+ or.getId());
			    			//rollback saving on orlisting table
			    			errorFile(file);
			    			or.delete();
			    		}
			    		
			    	}else {
			    		System.out.println("saving official receipt to server was not successfully processed...");
			    		unprocessedFile(file);
			    	}
		    	}
		    	
		    	}else {
		    		if(xml==null) {
		    			System.out.println("Error found for the file: " + file.getName());
		    		}else {
			    		System.out.println("data is exist...");
			    		existFile(file);
		    		}
		    	}
		    	
		    }else {
		    	System.out.println("No file to retrieve...");
		    }
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
	
	private static void unprocessedFile(File unprocessed) {
		File fileDir = new File(GlobalVar.COMMIT_XML_UNPROCESSED);
		fileDir.mkdir();
		
		File newFile = new File(GlobalVar.COMMIT_XML_UNPROCESSED + unprocessed.getName());
		if(copyFile(unprocessed, newFile)) {
			unprocessed.delete();
			System.out.println("unprocessed file has been transfered to " + fileDir.getAbsolutePath() + " file: " + unprocessed.getName());
		}
		
	}
	
	private static void errorFile(File error) {
		File fileDir = new File(GlobalVar.COMMIT_XML_ERROR);
		fileDir.mkdir();
		
		File newFile = new File(GlobalVar.COMMIT_XML_ERROR + error.getName());
		if(copyFile(error, newFile)) {
			error.delete();
			System.out.println("Error fi le has been transfered to " + fileDir.getAbsolutePath() + " file: " + error.getName());
		}
		
	}
	
	private static void existFile(File exist) {
		File fileDir = new File(GlobalVar.COMMIT_XML_EXIST);
		fileDir.mkdir();
		
		File newFile = new File(GlobalVar.COMMIT_XML_EXIST + exist.getName());
		
		if(copyFile(exist, newFile)) {
			exist.delete();
			System.out.println("exist file has been transfered to " + fileDir.getAbsolutePath() + " file: " + exist.getName());
		}
		
	}
	
	//getting info for customer
	private static Customer customer(OrlistingXML xml, UserDtls user) {
		
		String[] val = xml.getFormInfo().split("@");
		String bornplace = null;
		String weight = null;
		String heigth = null;
		String birthdate = null;
		String work = null;
		String citizenship = null;
		String gender = "1";
		if(val!=null && val.length>1) {
			try{gender = val[10];}catch(IndexOutOfBoundsException e) {}
			try{birthdate = val[11].equalsIgnoreCase("0")? null : val[11];}catch(IndexOutOfBoundsException e) {}
			try{heigth = val[13].equalsIgnoreCase("0")? "" : val[13];}catch(IndexOutOfBoundsException e) {}
			try{weight = val[14].equalsIgnoreCase("0")? "" : val[14];}catch(IndexOutOfBoundsException e) {}
			try{work = val[17].equalsIgnoreCase("0")? "" : val[17];}catch(IndexOutOfBoundsException e) {}
			try{bornplace = val[19].equalsIgnoreCase("0")? "" : val[19];}catch(IndexOutOfBoundsException e) {}
			try{citizenship = val[20].equalsIgnoreCase("0")? "" : val[20];}catch(IndexOutOfBoundsException e) {}
		}
		
		
		String sql = " AND cus.fullname=?";
    	String[] params = new String[1];
    	params[0] = xml.getFullName();
    	List<Customer> cs = Customer.retrieve(sql, params);
    	Customer cus = new Customer();
    	if(cs!=null && cs.size()>0) {//updating data instead
    		cus = cs.get(0);
    		cus.setDateregistered(xml.getDateTrans());		
    		cus.setFirstname(xml.getFirstName());
    		cus.setMiddlename(xml.getMiddleName());
    		cus.setLastname(xml.getLastName());
    		cus.setFullname(xml.getFullName());
    		cus.setCivilStatus(Integer.valueOf(xml.getCivilStatus()));
    		cus.setBirthdate(birthdate);
    		cus.setGender(gender);
    		cus.setWeight(weight);
    		cus.setHeight(heigth);
    		cus.setBornplace(bornplace);
    		cus.setWork(work);
    		cus.setCitizenship(citizenship);
    		cus.setIsactive(1);
    		cus.setUserDtls(user);
    		cus = Customer.save(cus);
    		System.out.println("Customer name is existing.....");
    	}else {//adding new data
    			cus = Customer.builder()
    			.dateregistered(xml.getDateTrans())		
    			.firstname(xml.getFirstName())
    			.middlename(xml.getMiddleName())
    			.lastname(xml.getLastName())
    			.fullname(xml.getFullName())
    			.civilStatus(Integer.valueOf(xml.getCivilStatus()))
    			.birthdate(birthdate)
    			.gender(gender)
    			.weight(weight)
    			.height(heigth)
    			.bornplace(bornplace)
    			.work(work)
    			.citizenship(citizenship)
    			.isactive(1)
    			.userDtls(user)
    			.build();
    			
    			//saving to database
    			cus = Customer.save(cus);
    			System.out.println("Customer name is adding to database.....");
    	}
		
		return cus;
	}
	
	private static ORListing orlisting(OrlistingXML xml, Customer cus) {
		Collector col = new Collector();
    	col.setId(Integer.valueOf(xml.getCollectorId()));
    	String formInfo = xml.getFormInfo().replace("@", "<->");
    	ORListing or = ORListing.builder()
    			.dateTrans(xml.getDateTrans())
    			.status(Integer.valueOf(xml.getOrStatus()))
    			.formType(Integer.valueOf(xml.getFormType()))
    			.orNumber(xml.getOrNumber())
    			.isActive(Integer.valueOf(xml.getIsActive()))
    			.forminfo(formInfo)
    			.collector(col)
    			.customer(cus)
    			.build();
    	
    			or = ORListing.save(or);
    			System.out.println("adding new official receipt");
    	return or;
	}
	
	private static boolean paymentNames(ORListing or, OrlistingXML xml, Customer cus) {
		boolean hasprocessed = false;
		for(PaymentName p : xml.getPaynames()) {
    		System.out.println("id: " + p.getId() + " amount: " + p.getAmount());
    		ORNameList o = new ORNameList();
			o.setAmount(p.getAmount());
			o.setOrList(or);
			o.setCustomer(cus);
			o.setIsActive(1);
			o.setPaymentName(p);
			o.save();
			hasprocessed = true;
			System.out.println("adding payment names...");
    	}
		return hasprocessed;
	}
	
	private static Customer getCustomerInfo(String fullname) {
		Customer cus = null;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT * FROM customer WHERE cusisactive=1 AND fullname="+fullname);
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			try{cus.setId(rs.getLong("customerid"));}catch(NullPointerException e){}
			try{cus.setFirstname(rs.getString("cusfirstname"));}catch(NullPointerException e){}
			try{cus.setMiddlename(rs.getString("cusmiddlename"));}catch(NullPointerException e){}
			try{cus.setLastname(rs.getString("cuslastname"));}catch(NullPointerException e){}
			try{cus.setFullname(rs.getString("fullname"));}catch(NullPointerException e){}
			try{cus.setGender(rs.getString("cusgender"));}catch(NullPointerException e){}
			try{cus.setAge(rs.getInt("cusage"));}catch(NullPointerException e){}
			try{cus.setContactno(rs.getString("cuscontactno"));}catch(NullPointerException e){}
			try{cus.setDateregistered(rs.getString("cusdateregistered"));}catch(NullPointerException e){}
			try{cus.setCardno(rs.getString("cuscardno"));}catch(NullPointerException e){}
			try{cus.setIsactive(rs.getInt("cusisactive"));}catch(NullPointerException e){}
			try{cus.setTimestamp(rs.getTimestamp("timestamp"));}catch(NullPointerException e){}
			try{cus.setCivilStatus(rs.getInt("civilstatus"));}catch(NullPointerException e){}
			try{cus.setPhotoid(rs.getString("photoid"));}catch(NullPointerException e){}
			try{cus.setBornplace(rs.getString("bornplace"));}catch(NullPointerException e){}
			try{cus.setWeight(rs.getString("weight"));}catch(NullPointerException e){}
			try{cus.setHeight(rs.getString("heigt"));}catch(NullPointerException e){}
			try{cus.setWork(rs.getString("work"));}catch(NullPointerException e){}
			try{cus.setCitizenship(rs.getString("citizenship"));}catch(NullPointerException e){}
			
			try{cus.setQrcode(rs.getString("qrcode"));}catch(NullPointerException e){}
			try{cus.setNationalId(rs.getString("nationalid"));}catch(NullPointerException e){}
			if("1".equalsIgnoreCase(cus.getGender())){
				cus.setGenderName("Male");
			}else{
				cus.setGenderName("Female");
			}
			
			try{cus.setBirthdate(rs.getString("borndate"));}catch(NullPointerException e){}
			
			UserDtls user = new UserDtls();
			try{user.setUserdtlsid(rs.getLong("userdtlsid"));}catch(NullPointerException e){}
			cus.setUserDtls(user);
			
			Purok pur = new Purok();
			try{pur.setId(rs.getLong("purid"));}catch(NullPointerException e){}
			cus.setPurok(pur);
			
			Barangay bar = new Barangay();
			try{bar.setId(rs.getInt("bgid"));}catch(NullPointerException e){}
			cus.setBarangay(bar);
			
			Municipality mun = new Municipality();
			try{mun.setId(rs.getInt("munid"));}catch(NullPointerException e){}
			cus.setMunicipality(mun);
			
			Province prov = new Province();
			try{prov.setId(rs.getInt("provid"));}catch(NullPointerException e){}
			cus.setProvince(prov);
			
			String address="";
			try{
				address = pur.getId()==0?"" : pur.getPurokName()+", ";
				address += bar.getId()==0?"" : bar.getName()+", ";
				address += mun.getId()==0?"" : mun.getName()+", ";
				address += prov.getName();
			}catch(Exception e){}
			cus.setCompleteAddress(address);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		
		return cus;
	}
	
	
	private static OrlistingXML readXML(String fileName) {
		OrlistingXML xml = null;//new OrlistingXML();
		File xmlFile = new File(GlobalVar.COMMIT_XML + fileName);
		if(xmlFile.exists()){
			try {
				xml = new OrlistingXML();
				SAXReader reader = new SAXReader();
				Document document = reader.read(xmlFile);
				Node node = document.selectSingleNode("/orlisting");
				int count=0;
				//System.out.println("Check count : " + count);
				try{xml.setReg(node.selectSingleNode("reg").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setFirstName(StringUtils.convertToUTF8(node.selectSingleNode("firstname").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setMiddleName(StringUtils.convertToUTF8(node.selectSingleNode("middlename").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setLastName(StringUtils.convertToUTF8(node.selectSingleNode("lastname").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setFullName(StringUtils.convertToUTF8(node.selectSingleNode("fullname").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setBirthDate(node.selectSingleNode("birthdate").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setCivilStatus(node.selectSingleNode("civilstatus").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setUserId(node.selectSingleNode("userid").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				
				try{xml.setDateTrans(node.selectSingleNode("datetrans").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setFormType(node.selectSingleNode("formtype").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setOrNumber(node.selectSingleNode("ornumber").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setOrStatus(node.selectSingleNode("status").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setCollectorId(node.selectSingleNode("collectorid").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setIsActive(node.selectSingleNode("isactive").getText());}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				try{xml.setFormInfo(StringUtils.convertToUTF8(node.selectSingleNode("forminfo").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
				
				List<PaymentName> pynames = new ArrayList<PaymentName>();
				List<Node> dtls = document.selectNodes("/orlisting/details/payname");
				for(Node n : dtls) {
					PaymentName oname = new PaymentName();
					try{oname.setId(Long.valueOf(n.selectSingleNode("pyid").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					try{oname.setAmount(Double.valueOf(n.selectSingleNode("amount").getText()));}catch(Exception e) {System.out.println("count: " + count++ + " reason: " + e.getStackTrace());}
					pynames.add(oname);
				}
				xml.setPaynames(pynames);
				//System.out.println("Check count end: " + count);
				
			}catch(DocumentException e) {
				System.out.println("Error Found for file " + fileName);
				e.printStackTrace();
				xml = null;
			}	
		}
		return xml;
	}
	
	public static void saveForUploadXML(ORListing ors) {
		
		Customer cus = ors.getCustomer();
		Collector col = Collector.retrieve(ors.getCollector().getId());
		//create directory if not present
		File dir = new File(GlobalVar.UPLOAD_XML);
		dir.mkdir();
		File dirUpload = new File(GlobalVar.COMMIT_XML);
		dirUpload.mkdir();
		//File file = new File(GlobalVar.UPLOAD_XML + col.getName() + "-" + cus.getFullname() + "-" + FormType.val(ors.getFormType()).getName() + "-" + ors.getDateTrans() + "-" + ors.getOrNumber() +".xml");
		//File fileUpload = new File(GlobalVar.COMMIT_XML + col.getName() + "-" + cus.getFullname() + "-" + FormType.val(ors.getFormType()).getName() + "-" + ors.getDateTrans() + "-" + ors.getOrNumber() + ".xml");
		
		File file = new File(GlobalVar.UPLOAD_XML + FormType.val(ors.getFormType()).getName() + "-" + ors.getOrNumber() +".xml");
		File fileUpload = new File(GlobalVar.COMMIT_XML  + FormType.val(ors.getFormType()).getName() + "-" + ors.getOrNumber() + ".xml");
		
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			PrintWriter pwUpload = new PrintWriter(new FileWriter(fileUpload));
			StringBuilder sb = new StringBuilder();
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			sb.append(xml);sb.append("\n");
			
			sb.append("<orlisting>");sb.append("\n");
			
			//customer info
			sb.append("<reg>"+ DateUtils.getCurrentDateYYYYMMDD() +"</reg>");sb.append("\n");
			sb.append("<firstname>"+ StringUtils.convertToUTF8(cus.getFirstname()) +"</firstname>");sb.append("\n");
			sb.append("<middlename>"+ StringUtils.convertToUTF8(cus.getMiddlename()) +"</middlename>");sb.append("\n");
			sb.append("<lastname>"+ StringUtils.convertToUTF8(cus.getLastname()) +"</lastname>");sb.append("\n");
			sb.append("<fullname>"+ StringUtils.convertToUTF8(cus.getFullname().trim()) +"</fullname>");sb.append("\n");
			sb.append("<birthdate>"+ cus.getBirthdate() +"</birthdate>");sb.append("\n");
			sb.append("<civilstatus>"+ cus.getCivilStatus() +"</civilstatus>");sb.append("\n");
			//user
			sb.append("<userid>"+ cus.getUserDtls().getUserdtlsid() +"</userid>");sb.append("\n");
			
			//forms details
			sb.append("<status>"+ ors.getStatus() +"</status>");sb.append("\n");
			sb.append("<datetrans>"+ ors.getDateTrans() +"</datetrans>");sb.append("\n");
			sb.append("<formtype>"+ ors.getFormType() +"</formtype>");sb.append("\n");
			sb.append("<ornumber>"+ ors.getOrNumber() +"</ornumber>");sb.append("\n");
			sb.append("<collectorid>"+ ors.getCollector().getId() +"</collectorid>");sb.append("\n");
			sb.append("<isactive>"+ ors.getIsActive() +"</isactive>");sb.append("\n");
			
			
			
			if(ors.getForminfo()!=null && !ors.getForminfo().isEmpty() && ors.getForminfo().contains("<->")) {
				sb.append("<forminfo>"+ StringUtils.convertToUTF8(ors.getForminfo().replace("<->", "@")) +"</forminfo>");sb.append("\n");
			}else {
				sb.append("<forminfo>"+ StringUtils.convertToUTF8(ors.getForminfo()) +"</forminfo>");sb.append("\n");
			}
			
			//details
			sb.append("<details>");sb.append("\n");
			
			int id=0;
			for(ORNameList name : ors.getOrNameList()) {
				//orid will be get when done uploading each Official Receipt to the server
				//same with id of customer
				sb.append("\t\t<payname id='"+ id++ +"'>");sb.append("\n");
				sb.append("\t\t\t<pyid>"+ name.getPaymentName().getId() +"</pyid>");sb.append("\n");
				sb.append("\t\t\t<amount>"+ name.getAmount() +"</amount>");sb.append("\n");
				sb.append("\t\t</payname>");sb.append("\n");
			}
			
			sb.append("</details>");sb.append("\n");
			
			
			sb.append("</orlisting>");sb.append("\n");
			
			pw.println(sb.toString());
			pw.flush();
			pw.close();
			
			pwUpload.println(sb.toString());
			pwUpload.flush();
			pwUpload.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}