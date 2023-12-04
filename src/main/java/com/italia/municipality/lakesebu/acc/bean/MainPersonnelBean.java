package com.italia.municipality.lakesebu.acc.bean;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.acc.controller.EmployeePayroll;
import com.italia.municipality.lakesebu.controller.Card;
import com.italia.municipality.lakesebu.controller.EmployeeLoan;
import com.italia.municipality.lakesebu.controller.EmployeeMain;
import com.italia.municipality.lakesebu.controller.ID;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.CivilStatus;
import com.italia.municipality.lakesebu.enm.EmployeeType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Messages;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/21/2022
 *
 */
@Named
@ViewScoped
@Data
public class MainPersonnelBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 34254657687651L;
	private String searchParam;
	private String results;
	private List<EmployeePayroll> rolls;
	
	private String grossTotal;
	private String taxTotal;
	private String netTotal;
	private String eeTotal;
	private String erTotal;
	private String coopTotal;
	
	@PostConstruct
	public void init() {
		randomUserMsgs();
	}
	
	public UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
	public void searchFilter() {
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("$('#displayMain').show(1000);$('#displayMain2').hide(1000);");
		
		String val = getSearchParam();
		if(val.contains(":")) {
			String[] param = val.split(":");
			switch(param[0].toUpperCase()) {
			case "ID" : searchID(" AND emp.fullname like '%"+ param[1] +"%'", val);  break;
			case "PAYROLL" : 
					String month = param[1].toLowerCase();
					int year = Integer.valueOf(param[2]);
					month = month.substring(0, 1).toUpperCase() + month.substring(1);
					if(param[3].length()>=3) {
						searchPayroll(" AND py.pystatus=1  AND py.monthperiod="+ Integer.valueOf(DateUtils.getMonthNumber(month)) +" AND py.yearperiod="+ year +"  AND emp.fullname like '%"+ param[3] +"%' ", val);
					}else {
						String text = strong("There is no result found for this parameter:", "");
						text += strong(val, "rgb(0,102,204)");
						pf.executeScript("$('#displayMain').show(1000);$('#displayMain2').hide(1000);");
						text = helpMessage(text);
						setResults(text);
					}
				break;
			}
			
			
		}else {
			if(val!=null && !val.isEmpty() && val.length()>=3) {
				switch(val.toUpperCase()) {
					case "REGULAR" : loadSearch(" AND emp.employeetype="+ EmployeeType.REGULAR.getId(), val); break;
					case "CONTRACTUAL" : loadSearch(" AND emp.employeetype="+ EmployeeType.CONTRACTUAL.getId(), val); break;
					case "CASUAL" : loadSearch(" AND emp.employeetype="+ EmployeeType.CASUAL.getId(), val); break;
					case "JOB-ORDER" : loadSearch(" AND emp.employeetype="+ EmployeeType.JO.getId(), val); break;
					case "CO-TERMINOUS" : loadSearch(" AND emp.employeetype="+ EmployeeType.CO_TERMINOUS.getId(),val); break;
					default: {loadSearch("AND emp.fullname like '%"+ val +"%'",val);}	
				}
			
			}else {
				setResults(helpMessage(strong("Please search....", "rgb(0,102,204)")));
				pf.executeScript("$('#displayMain').show(1000);$('#displayMain2').hide(1000);");
			}
			
		}
	}
	
	private void searchPayroll(String sql, String val) {
		String text = " AND py.pystatus=1";
		sql += " ORDER BY emp.fullname";
		//List<EmployeePayroll> pys = EmployeePayroll.retrieve(sql, new String[0]);
		Object[] obj = EmployeePayroll.retrieveData(sql, new String[0]);
		rolls = (List<EmployeePayroll>)obj[6];
		
		if(rolls!=null && rolls.size()>0) {
			
			setTaxTotal(Currency.formatAmount(Double.valueOf(obj[0].toString())));
			setGrossTotal(Currency.formatAmount(Double.valueOf(obj[1].toString())));
			setNetTotal(Currency.formatAmount(Double.valueOf(obj[2].toString())));
			setEeTotal(Currency.formatAmount(Double.valueOf(obj[3].toString())));
			setErTotal(Currency.formatAmount(Double.valueOf(obj[4].toString())));
			setCoopTotal(Currency.formatAmount(Double.valueOf(obj[5].toString())));
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("$('#displayMain').hide(1000);$('#displayMain2').show(1000);");
			
		}else {
			text += strong("There is no result found for this parameter:", "");
			text += strong(val, "rgb(0,102,204)");
			PrimeFaces pf = PrimeFaces.current();
			pf.executeScript("$('#displayMain').show(1000);$('#displayMain2').hide(1000);");
		}
		
		text = helpMessage(text);
		
		setResults(text);
		
	}
	
	private void searchID(String sql, String val) {
		
		String pathID = GlobalVar.EMPLOYEE_ID_FOLDER;
		
		String text = "";
		int count = 1;
		
		sql += " ORDER BY emp.fullname";
		
		List<ID> ids = ID.retrieve(sql, new String[0]);
		
		if(ids!=null && ids.size()>0) {
			
			text += "<ul>";
			for(ID id : ids) {
				EmployeeMain e = id.getEmployeeMain();
				text += strong(count +") " + e.getFullName(), "");
				
				String encodeData64 = imageToBase64(GlobalVar.EMPLOYEE_ID_FOLDER, e.getFullName(),"jpg");
				if(encodeData64!=null) {
					text +="<li><img src='data:image/jpeg;base64,"+ encodeData64 +"'/></li>";
				}
				
				
				//text = composeHtmlForEmoloyee(e, text);
				text += "</li>";
				
				text += "<br/><br/>";
				
				count++;
			}
			text += "</ul>";
			
		}else {
			text += strong("There is no result found for this parameter:", "");
			text += strong(val, "rgb(0,102,204)");
		}
		
		text = helpMessage(text);
		
		setResults(text);
	}
	
	public void loadSearch(String sql, String val) {
		
			
			String text = "";
			int count = 1;
			
			sql += " ORDER BY emp.fullname";
			
			List<EmployeeMain> es = EmployeeMain.retrieve(sql, new String[0]);
			
			if(es!=null && es.size()>0) {
			
				text += "<ul>";
				for(EmployeeMain e : es) {
					
					text += strong(count +")", "");
					
					String encodeData64 = imageToBase64(GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO,e.getPhotoid(),"jpg");
					if(encodeData64!=null) {
						text +="<li><img src='data:image/jpeg;base64,"+ encodeData64 +"'/></li>";
					}
					
					
					text = composeHtmlForEmoloyee(e, text);
					text += "</li>";
					
					text += "<br/><br/>";
					
					count++;
				}
				text += "</ul>";
				
			}else {
				text += strong("There is no result found for this parameter:", "");
				text += strong(val, "rgb(0,102,204)");
			}
			
			text = helpMessage(text);
			
			setResults(text);
			
		
		
		
		
	}
	
	private String composeHtmlForEmoloyee(EmployeeMain e, String text) {
		
		text += "<ul>";
		text += "<li>"+strong("Full Name: ", "") + strong(e.getFullName(),"rgb(0,102,204)");
		text += "<li>"+strong("Recorded: ", "") + strong(e.getRegDate(),"rgb(0,102,204)");
		text += "<li>"+strong("Resigned: ", "") + strong(e.getDateResigned(),"rgb(0,102,204)");
		text += "<li>"+strong("Employee ID: ","")+ strong(e.getEmployeeId(),"rgb(0,102,204)") +"</li>";
		text += "<li>"+strong("Designation: ","")+ strong(e.getPosition(),"rgb(0,102,204)") +"</li>";
		text += "<li>"+strong("Assignement: ","")+ strong(e.getDepartment().getDepartmentName().toUpperCase(),"rgb(0,102,204)") + strong(" under the management of ","")+ strong(e.getDepartment().getDepartmentHead()!=null? e.getDepartment().getDepartmentHead().toUpperCase():"","rgb(0,102,204)") + "</li>";
		text += "<li>"+strong("Address: ","")+ strong(e.getAddress(),"rgb(0,102,204)") +"</li>";
		text += "<li>"+strong("Civil Status: ","")+ strong(CivilStatus.typeName(e.getCivilStatus()).toUpperCase(),"rgb(0,102,204)") +"</li>";
		text += "<li>"+strong("BOD: ","")+strong(e.getBirthDate(),"rgb(0,102,204)")+"</li>";
		text += "<li>"+strong("Blood Type: ","")+strong(e.getBloodType(),"rgb(0,102,204)")+"</li>";
		text += "<li>"+strong("Mobile No: ","")+strong(e.getContactNo(),"rgb(0,102,204)")+"</li>";
		text += "<li>"+strong("Salary: ", "") + strong(Currency.formatAmount(e.getRate()),"rgb(0,102,204)");
		if(e.getEmergecnyContactDtls()!=null && !e.getEmergecnyContactDtls().isEmpty()) {
		text += "<li>"+strong("In case of emergency you may contact this person: ","")+strong(e.getEmergecnyContactDtls()!=null? e.getEmergecnyContactDtls().toUpperCase():"","rgb(0,102,204)")+"</li>";
		}
		
		text = loadLoans(e,text);
		text = loadCards(e, text);
		text = loadID(e, text);
		
		text += "</ul>";
		
		
		return text;
	}
	
	private String loadLoans(EmployeeMain em, String text) {
		List<EmployeeLoan> es = EmployeeLoan.retrieve(" AND emp.eid="+em.getId(), new String[0]);
		if(es!=null && es.size()>0) {
			text += "<li>"+ strong("Deductions","")+"<li>";
			//text += "<ul>";
			for(EmployeeLoan e : es) {
				text += "<li>"+strong(e.getName() +"(" + e.getRemarks() + ") Amount: ","") + strong(Currency.formatAmount(e.getLoanAmount()),"rgb(0,102,204)") + strong(" Status: ","") + strong((e.getIsCompleted()==1? "In-Flight" : "Completed"), "rgb(0,102,204)") + "</li>";
			}
			//text += "</ul>";
			//text += "</li>";
		}
		return text;
	}
	
	private String loadCards(EmployeeMain em, String text) {
		List<Card> cards = Card.retrieve(" AND emp.eid="+em.getId(), new String[0]); 
		if(cards!=null && cards.size()>0) {
			text += "<li>"+ strong("Valid IDs","")+"<li>";
			//text += "<ul>";
			for(Card c : cards) {
				text += "<li>"+ strong(c.getName() + " No: ", "") + strong(c.getNumber(),"rgb(0,102,204)") + "</li>";
			}
			//text += "</ul>";
			//text += "</li>";
		}
		return text;
	}
	
	private String loadID(EmployeeMain em, String text) {
		List<ID> ids = ID.retrieve(" AND emp.eid="+em.getId(), new String[0]);
		if(ids!=null && ids.size()>0) {
			text += "<li>"+ strong("LGU ID Transacted","")+"</li>";
			//text += "<ul>";
			for(ID id : ids) {
				text += "<li>" + strong("Date Issued: ","") + strong(id.getIssued(),"rgb(0,102,204)") + strong(" Valid Until: ","") + strong(id.getValid(), "rgb(0,102,204)") + "</li>";
			}
			//text += "</ul>";
			//text += "</li>";
		}
		return text;
	}
	
	private String strong(String val,String colorRGB) {
		if(colorRGB.isEmpty() || colorRGB==null) {colorRGB="rgb(0,0,0)";}//default value
		if(val!=null && !val.isEmpty()) {
			return "<strong style='color: "+ colorRGB +"'>" + val + "</strong>";
		}else{
			return "";
		}
	}
	
	
	private String imageToBase64(String folderLocation, String photoId, String ext) {
		String encodeData64 = null;
		
		//ClassLoader classLoader = getClass().getClassLoader();
		//String fileImg = GlobalVar.EMPLOYEE_IMAGE_PATH_PHOTO + photoId + ".jpg";
		//File inputFile = new File(classLoader.getResource(fileImg).getFile());
		File fileImg = new File(folderLocation + photoId + "." + ext);
		try {
			byte[] fileContent = FileUtils.readFileToByteArray(fileImg);
			encodeData64 = Base64.getEncoder().encodeToString(fileContent);
			System.out.println("Base64: " + encodeData64);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return encodeData64;
	}
	
	public void randomUserMsgs() {
		int count = 1;
		int max = 7, min =1;
		Random rand = new Random();
		int index = rand.nextInt((max-min)+1) + min; 
		String message = "";
		List<EmployeeMain> es = new ArrayList<EmployeeMain>();
		switch(index) {
		case 1 : 
			message = strong(Messages.greetings(getUser().getFirstname()), "");
			break;
		case 2 :
			
			es = EmployeeMain.retrieve(" ORDER BY emp.eid DESC LIMIT 10", new String[0]);
			if(es!=null && es.size()>0) {
				message = strong("Below are the list of last transaction you have created for employee information.", "");
				message += "<ul>";
				count = 1;
				for(EmployeeMain e : es) {
					message += "<li>"+ strong(count++ + ")"+e.getFullName(), "") +"</li>";
				}
				message += "</ul>";
			}
			break;
		case 3 :
			es = EmployeeMain.retrieve(" AND emp.employeetype="+ EmployeeType.CONTRACTUAL.getId() +" ORDER BY emp.eid DESC LIMIT 10", new String[0]);
			if(es!=null && es.size()>0) {
				message = strong("Below are the list last transaction you have created for contractual employee.", "");
				message += "<ul>";
				count = 1;
				for(EmployeeMain e : es) {
					message += "<li>"+ strong(count++ + ")"+e.getFullName(), "") +"</li>";
				}
				message += "</ul>";
			}
			break;
		case 4 :
			es = EmployeeMain.retrieve(" AND emp.employeetype="+ EmployeeType.JO.getId() +" ORDER BY emp.eid DESC LIMIT 10", new String[0]);
			if(es!=null && es.size()>0) {
				message = strong("Below are the list last transaction you have created for job order employee.", "");
				message += "<ul>";
				count = 1;
				for(EmployeeMain e : es) {
					message += "<li>"+ strong(count++ + ")"+e.getFullName(), "") +"</li>";
				}
				message += "</ul>";
			}
			break;	
		case 5 :
			es = EmployeeMain.retrieve(" AND emp.employeetype="+ EmployeeType.REGULAR.getId() +" ORDER BY emp.eid DESC LIMIT 10", new String[0]);
			if(es!=null && es.size()>0) {
				message = strong("Below are the list last transaction you have created for regular employee.", "");
				message += "<ul>";
				count = 1;
				for(EmployeeMain e : es) {
					message += "<li>"+ strong(count++ + ")"+e.getFullName(), "") +"</li>";
				}
				message += "</ul>";
			}
			break;	
		case 6 :
			es = EmployeeMain.retrieve(" AND emp.employeetype="+ EmployeeType.CASUAL.getId() +" ORDER BY emp.eid DESC LIMIT 10", new String[0]);
			if(es!=null && es.size()>0) {
				message = strong("Below are the list last transaction you have created for casual employee.", "");
				message += "<ul>";
				count = 1;
				for(EmployeeMain e : es) {
					message += "<li>"+ strong(count++ + ")"+e.getFullName(), "") +"</li>";
				}
				message += "</ul>";
			}
			break;	
		case 7 :
			es = EmployeeMain.retrieve(" AND emp.employeetype="+ EmployeeType.CO_TERMINOUS.getId() +" ORDER BY emp.eid DESC LIMIT 10", new String[0]);
			if(es!=null && es.size()>0) {
				message = strong("Below are the list last transaction you have created for co-terminous employee.", "");
				message += "<ul>";
				count = 1;
				for(EmployeeMain e : es) {
					message += "<li>"+ strong(count++ + ")"+e.getFullName(), "") +"</li>";
				}
				message += "</ul>";
			}
			break;	
		}
		
		message = helpMessage(message);
		
		setResults(message); 
	}
	
	
	
	private String helpMessage(String message) {
		message += "<br/><br/><br/>";
		message += strong("Here are the list you can do here in our search engine","");
		message += "<ul>";
		message += "<li>"+strong("You can search the name of the Employee","")+"</li>";
		message += "<li>"+strong("If you want to search specific Employee just type : ","") + strong("LAST NAME, FIRST NAME MIDDLE NAME","rgb(0,102,204)")+"</li>";
		message += "<li>"+strong("To display list of specific Employee Type just type: ","") + strong("REGULAR, CONTRACTUAL, CASUAL, JOB-ORDER, CO-TERMINOUS. It will display all the employee base on your parameter.","rgb(0,102,204)")+"</li>";
		message += "<li>"+strong("To type the payroll of a specific person just type: ","") + strong("PAYROLL:MONTH NAME:YEAR:LAST NAME","rgb(0,102,204)")+"</li>";
		message += "<li>"+strong("If you want to check if the person has an ID just type: ","") + strong("ID:LAST NAME","rgb(0,102,204)")+"</li>";
		//message += "<li>"+strong("","")+"</li>";
		message += "</ul>";
		return message;
	}
	
}

