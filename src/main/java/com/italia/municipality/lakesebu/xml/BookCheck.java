package com.italia.municipality.lakesebu.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.CashTransactions;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;

/**
* 
* @author mark italia
* @since 02/08/2017
* @version 1.0
*
*/
public class BookCheck {

	private static final String PATH = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue();
	private static final String APP_FOLDER = AppConf.APP_CONFIG_FOLDER_NAME.getValue();
	private static final String CHECK_NO_FOLDER = PATH +  APP_FOLDER + AppConf.SEPERATOR.getValue() + AppConf.CHECKNOXMLS.getValue() + AppConf.SEPERATOR.getValue(); 		
	private static final String CHECK_NO_LOAD_XML = PATH +  APP_FOLDER + AppConf.SEPERATOR.getValue() + AppConf.LOADXML.getValue() + AppConf.SEPERATOR.getValue();
	
	public static void main(String[] args) {
		BookCheck.loadXML();
	}
	
	public static void loadXML(){
		File folder = new File(CHECK_NO_LOAD_XML);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	CheckXML xml = readXML(file.getName());
		    	CashTransactions tran = null;
		    	
		    	try{tran = CashTransactions.retrieve("SELECT * FROM cashtransactions WHERE checkno="+xml.getCheckNo(), new String[0]).get(0);}catch(Exception e){}
		    	
		    	if(tran==null){
		    		tran = new CashTransactions();
		    		tran.setVoucherNo(tran.voucherNumber(xml.getAccounts().getBankId()));
		    		tran.setOrNumber(xml.getOrNumber());
		    	}else{
		    		tran.setVoucherNo(tran.getVoucherNo());
		    		tran.setOrNumber(tran.getOrNumber());
		    	}
		    	
		    	tran.setDateTrans(xml.getDateTrans());
		    	tran.setCheckNo(xml.getCheckNo());
		    	tran.setParticulars(xml.getPayee());
		    	tran.setNaturePayment(xml.getNaturePayment());
		    	tran.setIsActive(xml.getIsActive());
		    	tran.setTransType(xml.getTransactionType());
		    	tran.setCreditAmount(xml.getCreditAmount());
		    	tran.setDepartment(xml.getDepartment());
		    	tran.setAccounts(xml.getAccounts());
		    	tran.setUserDtls(xml.getUserDtls());
		    	tran.save();
		    }
		}
	}
	
	public static void createXML(CheckXML xml, String fileName){
		
		 try {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("checkno");
		doc.appendChild(rootElement);
		
		// firstname elements
				Element cashDate = doc.createElement("cashDate");
				cashDate.appendChild(doc.createTextNode(xml.getDateTrans()));
				rootElement.appendChild(cashDate);

				Element departmentid = doc.createElement("departmentid");
				departmentid.appendChild(doc.createTextNode(xml.getDepartment().getDepid()+""));
				rootElement.appendChild(departmentid);
				
				Element orno = doc.createElement("orno");
				orno.appendChild(doc.createTextNode(xml.getOrNumber()));
				rootElement.appendChild(orno);
		
				Element checkno = doc.createElement("checkno");
				checkno.appendChild(doc.createTextNode(xml.getCheckNo()));
				rootElement.appendChild(checkno);
				
				Element voucherno = doc.createElement("voucherno");
				voucherno.appendChild(doc.createTextNode(xml.getVoucherNo()));
				rootElement.appendChild(voucherno);
				
				Element cashParticulars = doc.createElement("cashParticulars");
				cashParticulars.appendChild(doc.createTextNode(xml.getPayee()));
				rootElement.appendChild(cashParticulars);
				
				Element naturepayment = doc.createElement("naturepayment");
				naturepayment.appendChild(doc.createTextNode(xml.getNaturePayment()));
				rootElement.appendChild(naturepayment);
				
				Element cashisactive = doc.createElement("cashisactive");
				cashisactive.appendChild(doc.createTextNode(xml.getIsActive()+""));
				rootElement.appendChild(cashisactive);
				
				Element cashtranstype = doc.createElement("cashtranstype");
				cashtranstype.appendChild(doc.createTextNode(xml.getTransactionType()+""));
				rootElement.appendChild(cashtranstype);
				
				Element bankid = doc.createElement("bankid");
				bankid.appendChild(doc.createTextNode(xml.getAccounts().getBankId()+""));
				rootElement.appendChild(bankid);
				
				Element cashCredit = doc.createElement("cashCredit");
				cashCredit.appendChild(doc.createTextNode(xml.getCreditAmount()+""));
				rootElement.appendChild(cashCredit);
				
				Element userdtlsid = doc.createElement("userdtlsid");
				userdtlsid.appendChild(doc.createTextNode(xml.getUserDtls().getUserdtlsid()+""));
				rootElement.appendChild(userdtlsid);
				
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				
				File folder = new File(CHECK_NO_FOLDER);
				if(folder.isDirectory()){
					//do nothing
				}else{
					folder.mkdirs();
				}
				
				File folder2 = new File(CHECK_NO_LOAD_XML);
				if(folder2.isDirectory()){
					//do nothing
				}else{
					folder2.mkdirs();
				}
				
				StreamResult result = new StreamResult(new File(CHECK_NO_FOLDER + fileName));
				StreamResult result2 = new StreamResult(new File(CHECK_NO_LOAD_XML + fileName));
				// Output to console for testing
				//StreamResult result = new StreamResult(System.out);
				//System.out.println(result);
				transformer.transform(source, result);
				transformer.transform(source, result2);
				
				
				

				System.out.println("File saved!");
				
		 } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
		} catch (TransformerException tfe) {
				tfe.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 * @Read and load xml data to database
	 */
	public static CheckXML readXML(String fileName){
		CheckXML xml = new CheckXML();
		
		File xmlFile = new File(CHECK_NO_LOAD_XML + fileName);
		System.out.println("file load " + xmlFile.getAbsolutePath());
		if(xmlFile.exists()){
			try{
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			Document doc = documentBuilder.parse(xmlFile);
			
			/////////////normalize
			doc.getDocumentElement().normalize();
			//System.out.println("Reading conf......");
			
			NodeList ls = doc.getElementsByTagName("checkno");
			int size=ls.getLength();
			
			for(int i=0; i<size; i++){
				Node n = ls.item(i);
				//System.out.println("Current Node: "+ n.getNodeName());
				
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element e = (Element)n;
					
					xml.setDateTrans(e.getElementsByTagName(Check.DATE.getTagName()).item(0).getTextContent());
					xml.setVoucherNo(e.getElementsByTagName(Check.VOUCHER_NO.getTagName()).item(0).getTextContent());
					xml.setOrNumber(e.getElementsByTagName(Check.OR_NO.getTagName()).item(0).getTextContent());
					xml.setCheckNo(e.getElementsByTagName(Check.CHECK_NO.getTagName()).item(0).getTextContent());
					xml.setPayee(e.getElementsByTagName(Check.PAYEE.getTagName()).item(0).getTextContent());
					xml.setNaturePayment(e.getElementsByTagName(Check.NATURE_PAYMENT.getTagName()).item(0).getTextContent());
					xml.setTransactionType(Integer.valueOf(e.getElementsByTagName(Check.TRANSACTION_TYPE.getTagName()).item(0).getTextContent()));
					xml.setDebitAmount(0);
					xml.setCreditAmount(Double.valueOf(e.getElementsByTagName(Check.AMOUNT.getTagName()).item(0).getTextContent()));
					
					Department dep = new Department();
					dep.setDepid(Integer.valueOf(e.getElementsByTagName(Check.DEPARTMENT_ID.getTagName()).item(0).getTextContent()));
					xml.setDepartment(dep);
					
					BankAccounts account = new  BankAccounts();
					account.setBankId(Integer.valueOf(e.getElementsByTagName(Check.ACCOUNT_TYPE.getTagName()).item(0).getTextContent()));
					xml.setAccounts(account);
					
					UserDtls user = new UserDtls();
					user.setUserdtlsid(Long.valueOf(e.getElementsByTagName(Check.USER.getTagName()).item(0).getTextContent()));
					xml.setUserDtls(user);
					
					xml.setIsActive(Integer.valueOf(e.getElementsByTagName(Check.IS_ACTIVE.getTagName()).item(0).getTextContent()));
					
				}
				
			}
			
			
			}catch(Exception e){
				
			}
			
			//delete the file after use
			xmlFile.delete();
			System.out.println("File successfully deleted...");
		}else{
			System.out.println("File is not exist");
		}
		return xml;
	}
	
}
