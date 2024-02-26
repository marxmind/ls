package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
/**
 * Transferred to OverviewBean
 */
@Named("ovrBean")
@ViewScoped
@Data
@Deprecated
public class OvBean implements Serializable{/**
	 * 
	 */
private static final long serialVersionUID = 53577956487451L;
	
	private String checkWriting;
	private String depositTrans;
	private String collectionTrans;
	
	private String summaryInfo;
	
	private String year;
	private int todayMonth;
	private String month;
	private String startDate;
	private String dateToday;
	private String last_Date;
	private StringBuilder sb;
	private Map<Long, Offices> mapOffices;
	private Map<Long, Mooe> mapMooe;
	private Map<Integer, Collector> mapCollector;
	
	private List<Reports> rptRecentChecksIssueds;
	
	@PostConstruct
	public void init() {
		sb = new StringBuilder();
		
		year = DateUtils.getCurrentYear()+"";
		todayMonth = DateUtils.getCurrentMonth();
		month = todayMonth<10? "0"+todayMonth : todayMonth+"";
		startDate = year + "-" + month + "-01";
		dateToday = DateUtils.getCurrentDateYYYYMMDD();
		last_Date= DateUtils.getLastDayOfTheMonth("yyyy-MM-dd",dateToday, Locale.TAIWAN);
		
		
		sb.append("<h1>Please see below details for today's transaction <strong style='color: red'>" +  DateUtils.convertDateToMonthDayYear(dateToday)+"</strong></h1>");
		sb.append("<br/><br/>");
		
		//do not move these line of codes
		loadOffices();
		loadMooe();
		loadCollector();
		
		rptRecentChecksIssueds = new ArrayList<Reports>();
		
		loadCheckWriting();
		loadCashCollection();
		loadCollection();
		loadPerCollectorCollection();
		
		
		setSummaryInfo(sb.toString());
	}
	
	private Login getUser() {
		return Login.getUserLogin();
	}
	
	private void loadOffices() {
		mapOffices = new LinkedHashMap<Long, Offices>();
		ResultSet rs = OpenTableAccess.query("SELECT * FROM offices WHERE isactiveoff=1",new String[0], new WebTISDatabaseConnect());
		try {
			while(rs.next()) {
				Offices off = Offices.builder()
						.id(rs.getInt("offid"))
						.name(rs.getString("name"))
						.code(rs.getString("code"))
						.headOfOffice(rs.getString("headname"))
						.abr(rs.getString("abrname"))
						.isActive(rs.getInt("isactiveoff"))
						.departmentId(rs.getInt("departmentid"))
						.build();
				mapOffices.put(off.getId(), off);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadMooe() {
		mapMooe = new LinkedHashMap<Long, Mooe>();
		ResultSet rs = OpenTableAccess.query("SELECT * FROM mooe WHERE isactivem=1",new String[0], new WebTISDatabaseConnect());
		try {
			while(rs.next()) {
				Mooe moe = Mooe.builder()
						.id(rs.getLong("moid"))
						.code(rs.getString("code"))
						.dateTrans(rs.getString("encodedate"))
						.description(rs.getString("description"))
						.budgetAmount(rs.getDouble("budgetamount"))
						.isActive(rs.getInt("isactivem"))
						.yearBudget(rs.getInt("yearbudget"))
						.offices(Offices.builder().id(rs.getLong("offid")).build())
						.build();
				mapMooe.put(moe.getId(), moe);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadCollector() {
		mapCollector = new LinkedHashMap<Integer, Collector>();
		for(Collector c : Collector.retrieve("", new String[0])) {
			mapCollector.put(c.getId(), c);
		}
	}
	
	private void loadCheckWriting() {
		
		
		
		ResultSet rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,c.date_disbursement,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND (c.date_disbursement>='"+  startDate +"' AND c.date_disbursement<='"+  last_Date +"')", new String[0],new BankChequeDatabaseConnect());
		double amountToday=0d;
		double amountMonth=0d;
		String lastTransaction="";
		double lastPayeeAmount=0d;
		String disBurseDate="";
		String remarks = "";
		String mooe = "";
		String opis = "";
		String mode="";
		String checkNo="";
		String issueds="<ul>";
		Map<String, Double> mapMode = new LinkedHashMap<String, Double>();
		List<String> vals = new ArrayList<String>();
		Map<String, List<String>> mapMul = new LinkedHashMap<String, List<String>>();
		List<String> mul = new ArrayList<String>();
		try {
			
			while(rs.next()) {
				mode = rs.getString("bank_account_name");
				checkNo = rs.getString("cheque_no");
				double amnt = rs.getDouble("amount");
				String stat = rs. getString("chkstatus");
				disBurseDate = rs. getString("date_disbursement");
				remarks = rs.getString("chkremarks");
				mooe = mapMooe.get(rs.getLong("moid")).getDescription();
				opis = mapOffices.get(rs.getLong("offid")).getName();
				
				if("1".equalsIgnoreCase(stat)) {//issued
					if(dateToday.equalsIgnoreCase(disBurseDate)) {
						amountToday += amnt;
					}else {
						amountMonth += amnt;
					}
					
					
					//last transaction
					lastTransaction = rs.getString("payee");
					lastPayeeAmount = amnt;
					
					vals.add("<li>"+ DateUtils.convertDateToMonthDayYear(disBurseDate)+"/"+ checkNo+ "/" + rs.getString("payee") +" --> "+ Currency.formatAmount(amnt) + " for the "+ remarks +" from fund "+ mode + "/Office:" + opis + "/Charge to: " + mooe +"</li>");
					
					if(mapMode!=null) {
						if(mapMode.containsKey(mode)){
							double newAmount = mapMode.get(mode) + amnt;
							mapMode.put(mode, newAmount);
						}else {
							mapMode.put(mode, amnt);
						}
					}else {
						mapMode.put(mode, amnt);
					}
				}else {
					//cancelled
				}
				
				
				if(mapMul!=null && mapMul.size()>0) {
					if(mapMul.containsKey(lastTransaction)) {
						mul = mapMul.get(lastTransaction);
						mul.add("<li>"+DateUtils.convertDateToMonthDayYear(disBurseDate)+"/"+ checkNo+"/"+ remarks+ "/"+Currency.formatAmount(amnt)+"/"+ mode + "/Office:" + opis + "/Charge to: " + mooe +"</li>");
						mapMul.put(lastTransaction, mul);
					}else {
						mul = new ArrayList<String>();
						mul.add("<li>"+DateUtils.convertDateToMonthDayYear(disBurseDate)+"/"+ checkNo+"/"+ remarks+ "/"+Currency.formatAmount(amnt)+"/"+ mode + "/Office:" + opis + "/Charge to: " + mooe +"</li>");
						mapMul.put(lastTransaction, mul);
					}
				}else {
					mul.add("<li>"+DateUtils.convertDateToMonthDayYear(disBurseDate)+"/"+ checkNo+"/"+ remarks+ "/"+Currency.formatAmount(amnt)+"/"+ mode + "/Office:" + opis + "/Charge to: " + mooe +"</li>");
					mapMul.put(lastTransaction, mul);
				}
				
			}
			
			Collections.reverse(vals);
			int count = 1;
			for(String v : vals) {
				if(count==1) {
					//do not add as this is the last transaction
				}else if(count>1 && count<=11) {
					issueds+=v;
				}else if(count==12) {
					break;
				}
				count++;
			}
			
			if(count>1) {
				issueds+="</ul>";
			}
			
			sb.append("<h2>ISSUANCE OF CHECK</h2><br/><br/>");
			sb.append("<ul>");
			if(amountToday==0) {
				sb.append("<li>");
				sb.append("There is no transaction yet for today");
				sb.append("</li>");
			}else {
				sb.append("<li>");
				sb.append("The total issued CHECK for the date <strong style='color: red'>" + DateUtils.convertDateToMonthDayYear(dateToday) +"</strong> is total of <strong>" + Currency.formatAmount(amountToday)+"</strong>");
				sb.append("</li>");
			}
			if(amountMonth==0) {
				sb.append("<li>");
				sb.append("There is no transaction yet for "+ DateUtils.getMonthName(todayMonth));
				sb.append("</li>");
			}else {	
				sb.append("<li>");
				sb.append("The total issued for the month of " + DateUtils.getMonthName(todayMonth) + " is <strong>" + Currency.formatAmount(amountMonth)+"</strong>");
				sb.append("</li>");
			}
			sb.append("<li>");
			sb.append("The last transaction was issued to <strong>" + lastTransaction + "</strong> with a check no of <strong style='color:red'>" + checkNo + "</strong> dated " + DateUtils.convertDateToMonthDayYear(disBurseDate) + " amounting <strong>" + Currency.formatAmount(lastPayeeAmount) +  "</strong> for the <strong>" + remarks+" from fund: "+ mode + "/Office:" + opis + "/Charge to: " + mooe +"</strong>");
			sb.append("</li>");
			sb.append("</ul>");
			sb.append("<br/>");
			
			sb.append("<h2>Here are the 10 recent issuance of checks</h2>");
			sb.append("<br/>");
			sb.append(issueds);
			sb.append("<br/><br/>");
			
			if(mapMul!=null && mapMul.size()>0) {
				String multiple="<h2>Below are the multiple check issued for the same Payee.</h2><br/>";
				boolean hasFound=false;
				int num = 1;
				for(String name : mapMul.keySet()) {
					int size = mapMul.get(name).size();
					if(size>1) {
						hasFound=true;
						multiple+= num + ") <strong>"+ name +"</strong><br/>";
						multiple+="<ul>";
						for(String v : mapMul.get(name)) {
							multiple+=v;
						}
						multiple+="</ul>";
						num++;
					}
				}
				if(hasFound) {
					sb.append(multiple);
				}
			}
			
			sb.append("<br/><br/>");
			if(mapMode!=null && mapMode.size()>0) {
				sb.append("<h2>Following are the Per Account Total Issued Check for " + DateUtils.getMonthName(todayMonth)+"</h2>");
				sb.append("<br/><br/>");
				sb.append("<ul>");
				for(String mod : mapMode.keySet()) {
					sb.append("<li>");
					sb.append(mod + " " + Currency.formatAmount(mapMode.get(mod)));
					sb.append("</li>");
				}
				sb.append("</ul>");
			}
			
			rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,c.date_disbursement,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND (c.date_disbursement>='"+  startDate +"' AND c.date_disbursement<='"+  last_Date +"') ORDER BY c.cheque_amount DESC LIMIT 10",new String[0], new BankChequeDatabaseConnect());
			
			count = 1;
			while(rs.next()) {
				
				mooe = mapMooe.get(rs.getLong("moid")).getDescription();
				opis = mapOffices.get(rs.getLong("offid")).getName();
				
				if(count==1) {
					sb.append("<br/><br/>");
					sb.append("<h2>Here are the 10 highest issued check</h2><br/>");
				}
				sb.append(count +") <i>"+DateUtils.convertDateToMonthDayYear(rs.getString("date_disbursement"))+"</i>/"+rs.getString("cheque_no")+"/"+rs.getString("bank_account_name")+"/<strong>"+rs.getString("payee")+"</strong>/<strong style='color: blue'>"+Currency.formatAmount(rs.getDouble("amount"))+"</strong>/"+rs.getString("chkremarks") + "/Office:" + opis + "/Charge to: " + mooe);
				sb.append("<br/>");
				count++;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadCashCollection() {
		sb.append("<br/><br/>");
		sb.append("<h2>CASH COLLECTION</h2>");
		sb.append("<br/>");
		sb.append("Following are the collection for " + DateUtils.getMonthName(todayMonth));
		ResultSet rs = OpenTableAccess.query("SELECT fundtype, SUM(amount) as amount,remarks FROM rcddeposit WHERE isactivercd=1 AND remarks!='BEGINNING BALANCE' AND (datetrans>='"+  startDate +"' AND datetrans<='"+  last_Date +"') GROUP BY fundtype", new String[0],new WebTISDatabaseConnect());
		Map<String, Double> mapDeposit = new LinkedHashMap<String, Double>();
		Map<String, Double> mapBeginning = new LinkedHashMap<String, Double>();
		Map<String, Double> mapCollection = new LinkedHashMap<String, Double>();
		try {
			
			while(rs.next()) {
				mapDeposit.put(FundType.typeName(rs.getInt("fundtype")), rs.getDouble("amount"));
			}
			
			rs = OpenTableAccess.query("SELECT fundtype, amount,remarks FROM rcddeposit WHERE isactivercd=1 AND remarks='BEGINNING BALANCE' AND (datetrans>='"+  startDate +"' AND datetrans<='"+  last_Date +"')", new String[0],new WebTISDatabaseConnect());
			while(rs.next()) {
				mapBeginning.put(FundType.typeName(rs.getInt("fundtype")), rs.getDouble("amount"));
			}
			
			rs = OpenTableAccess.query("select fundtype,sum(amount) as amount from rcdallcontroller WHERE isactiveall=1 AND (alldatetrans>='"+  startDate +"' AND alldatetrans<='"+  last_Date +"') GROUP BY fundtype", new String[0],new WebTISDatabaseConnect());
			while(rs.next()) {
				mapCollection.put(FundType.typeName(rs.getInt("fundtype")), rs.getDouble("amount"));
			}
			
			sb.append("<br/>");
			for(FundType v : FundType.values()) {
				double amountBeg=0d;
				double amountCol=0d;
				double amountDep=0d;
				double balance=0d;
				sb.append("<b>");
				sb.append(v.getName());
				sb.append("</b>");
				if(mapBeginning!=null && mapBeginning.containsKey(v.getName())) {
					sb.append("<br/>");
					amountBeg = mapBeginning.get(v.getName());
					sb.append("<strong>Beginning Balance:</strong> " + Currency.formatAmount(amountBeg));
				}else {
					sb.append("<br/>");
					sb.append("<strong>Beginning Balance:</strong> 0.00");
				}
				
				if(mapCollection!=null && mapCollection.containsKey(v.getName())) {
					sb.append("<br/>");
					amountCol = mapCollection.get(v.getName());
					sb.append("<strong>Collection:</strong> " + Currency.formatAmount(amountCol));
				}else {
					sb.append("<br/>");
					sb.append("<strong>Collection:</strong> 0.00");
				}
				
				if(mapDeposit!=null && mapDeposit.containsKey(v.getName())) {
					sb.append("<br/>");
					amountDep = mapDeposit.get(v.getName());
					sb.append("<strong>Deposit:</strong> " + Currency.formatAmount(amountDep));
				}else {
					sb.append("<br/>");
					sb.append("<strong>Deposit:</strong> 0.00");
				}
				
				balance = (amountBeg + amountCol) - amountDep;
				
				sb.append("<br/>");
				sb.append("<strong>Ending Balance:</strong> " + Currency.formatAmount(balance));
				sb.append("<br/><br/>");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
			
	}
	
	private void loadCollection() {
		String tmp = sb.toString();
		sb = new StringBuilder();
		sb.append("Ordered from highest to lowest collection");
		String sql = "SELECT a.accname as name,sum(b.olamount) as amount FROM taxaccntgroup a,ornamelist b WHERE b.accid=a.accid AND b.orid IN(select o.orid from orlisting o where o.isactiveor=1 AND (o.ordatetrans>='"+ startDate +"' AND o.ordatetrans<='"+ last_Date +"')) GROUP BY b.accid ORDER BY amount DESC;";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		sb.append("<br/>");
		try {
			int count = 1;
			sb.append("<ul>");
			double total = 0d;
			while(rs.next()) {
				double amount = rs.getDouble("amount");
				total += amount;
				sb.append("<li>");
				sb.append(count+") " + rs.getString("name") + ": " + Currency.formatAmount(amount));
				sb.append("</li>");
				count++;
			}
			sb.append("</ul>");
			sb.append("<br/><br/>");
			
			tmp += "<h2>Total Running collection for the month of "+ DateUtils.getMonthName(todayMonth) +" as of today is <strong style='red'>" + Currency.formatAmount(total)+"</strong></h2>";
			tmp += sb.toString();
			sb = new StringBuilder();
			sb.append(tmp);
			
			sb.append("<br/><br/>");
			tmp = sb.toString();
			sb = new StringBuilder();
			sb.append("Summary report - Ordered from highest to lowest");
			sb.append("<br/><br/>");
			sql = "SELECT a.accname as name,sum(b.olamount) as amount FROM taxaccntgroup a,ornamelist b WHERE b.accid=a.accid AND b.orid IN(select o.orid from orlisting o where o.isactiveor=1 AND (o.ordatetrans>='"+ year +"-01-01' AND o.ordatetrans<='"+ year +"-12-31')) GROUP BY b.accid ORDER BY amount DESC;";
			rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
			total = 0;
			count = 1;
			sb.append("<ul>");
			while(rs.next()) {
				double amount = rs.getDouble("amount");
				total += amount;
				sb.append("<li>");
				sb.append(count+") " + rs.getString("name") + ": " + Currency.formatAmount(amount));
				sb.append("</li>");
				count++;
			}
			sb.append("</ul>");
			sb.append("<br/><br/>");
			
			tmp += "<h2>"+ year +" collection as of today is <strong style='red'>" + Currency.formatAmount(total)+"</strong></h2>";
			
			tmp += sb.toString();
			sb = new StringBuilder();
			sb.append(tmp);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		sql = "select o.olamount, a.isid, a.aform from orlisting a , ornamelist o WHERE a.isactiveor=1 AND a.orstatus=4 AND a.orid=o.orid AND (a.ordatetrans>='"+ dateToday +"' and a.ordatetrans<='"+ dateToday +"')";
		rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		try {
			Map<Integer, Map<Integer, Double>> mapColToday = new LinkedHashMap<Integer, Map<Integer, Double>>();
			Map<Integer, Double> mapFormToday = new LinkedHashMap<Integer, Double>();
			while(rs.next()) {
				int id = rs.getInt("isid");
				int form = rs.getInt("aform");
				double amount = rs.getDouble("olamount");
				
				if(mapColToday!=null && mapColToday.size()>0) {
					if(mapColToday.containsKey(id)) {
						if(mapColToday.get(id).containsKey(form)) {
							double newAmount = mapColToday.get(id).get(form) + amount;
							mapColToday.get(id).put(form, newAmount);
						}else {
							mapColToday.get(id).put(form, amount);
						}
					}else {
						mapFormToday = new LinkedHashMap<Integer, Double>();
						mapFormToday.put(form, amount);
						mapColToday.put(id, mapFormToday);
					}
				}else {
					mapFormToday.put(form, amount);
					mapColToday.put(id, mapFormToday);
				}
				
			}
			
			tmp = sb.toString();
			sb = new StringBuilder();
			if(mapColToday!=null && mapColToday.size()>0) {
				int num = 1;
				double total = 0d;
				for(int id : mapColToday.keySet()) {
					Collector col = getMapCollector().get(id);
					String dep = "";
					try {dep = col.getDepartment().getDepartmentName();}catch(Exception e) {}
					sb.append(num + " ) <strong>"+ col.getName() + "</strong> - " + dep);
					
					sb.append("<ul>");
					for(int form : mapColToday.get(id).keySet()) {
						double amount = mapColToday.get(id).get(form);
						total += amount;
						sb.append("<li>");
						sb.append(FormType.nameId(form) + " : " + Currency.formatAmount(amount));
						sb.append("</li>");
					}
					sb.append("</ul>");
					
					num++;
				}
				
				tmp += "<h2>Running collection for today is <strong>"+ Currency.formatAmount(total) +"</strong></h2>";
				tmp += sb.toString();
				sb = new StringBuilder();
				sb.append(tmp);
				
			}else {
				sb.append(tmp);
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void loadPerCollectorCollection() {
		String sql = "SELECT receiveddate,amount,formtypecol,isid FROM collectioninfo WHERE isactivecol=1 AND (receiveddate>='"+ startDate +"' AND receiveddate<='"+ last_Date +"')";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		Map<Integer, Map<Integer, Double>> mapData = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> mapForm = new LinkedHashMap<Integer, Double>();
		
		Map<Integer, Map<Integer, Double>> mapDataToday = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> mapFormToday = new LinkedHashMap<Integer, Double>();
		
		try {
			while(rs.next()) {
				int form = rs.getInt("formtypecol");
				double amount = rs.getDouble("amount");
				int id = rs.getInt("isid");
				String date = rs.getString("receiveddate");
				
				if(dateToday.equalsIgnoreCase(date)) {
					if(mapDataToday!=null && mapDataToday.size()>0) {
						if(mapDataToday.containsKey(id)) {
							if(mapDataToday.get(id).containsKey(form)) {
								double newAmount = mapDataToday.get(id).get(form) + amount;
								mapDataToday.get(id).put(form, newAmount);
							}else {
								mapDataToday.get(id).put(form, amount);
							}
						}else {
							mapFormToday = new LinkedHashMap<Integer, Double>();
							mapFormToday.put(form, amount);
							mapDataToday.put(id, mapFormToday);
						}
					}else {
						mapFormToday.put(form, amount);
						mapDataToday.put(id, mapFormToday);
					}
				}
				
				if(mapData!=null && mapData.size()>0) {
					if(mapData.containsKey(id)) {
						if(mapData.get(id).containsKey(form)) {
							double newAmount = mapData.get(id).get(form) + amount;
							mapData.get(id).put(form, newAmount);
						}else {
							mapData.get(id).put(form, amount);
						}
					}else {
						mapForm = new LinkedHashMap<Integer, Double>();
						mapForm.put(form, amount);
						mapData.put(id, mapForm);
					}
				}else {
					mapForm.put(form, amount);
					mapData.put(id, mapForm);
				}
			}
			
			sb.append("<h2>Below are the Reported Collector's Collection for " + DateUtils.getMonthName(todayMonth) + "</h2>");
			sb.append("<br/><br/>");
			int num = 1;
			for(int id : mapData.keySet()) {
				
				Collector col = getMapCollector().get(id);
				String dep = "";
				try {dep = col.getDepartment().getDepartmentName();}catch(Exception e) {}
				sb.append(num + " ) <strong>"+ col.getName() + "</strong> - " + dep);
				
				sb.append("<ul>");
				for(int form : mapData.get(id).keySet()) {
					sb.append("<li>");
					sb.append(FormType.nameId(form) + " : " + Currency.formatAmount(mapData.get(id).get(form)));
					sb.append("</li>");
				}
				sb.append("</ul>");
				
				num++;
			}
			
			if(mapDataToday!=null && mapDataToday.size()>0) {
				sb.append("<br/><br/>");
				String tmp = sb.toString();
				sb = new StringBuilder();
				sb.append("<br/>");
				
				 num = 1;
				 double total = 0d;
				for(int id : mapDataToday.keySet()) {
					
					Collector col = getMapCollector().get(id);
					String dep = "";
					try {dep = col.getDepartment().getDepartmentName();}catch(Exception e) {}
					sb.append(num + " ) <strong>"+ col.getName() + "</strong> - " + dep);
					
					sb.append("<ul>");
					for(int form : mapDataToday.get(id).keySet()) {
						double amount = mapDataToday.get(id).get(form);
						total += amount;
						sb.append("<li>");
						sb.append(FormType.nameId(form) + " : " + Currency.formatAmount(amount));
						sb.append("</li>");
					}
					sb.append("</ul>");
					
					num++;
				}
				tmp += "<h2>Reported Collection for Today "+ DateUtils.convertDateToMonthDayYear(dateToday) + " to LO is total of " + Currency.formatAmount(total) +"</h2>";
				tmp += sb.toString();
				sb = new StringBuilder();
				sb.append(tmp);
				
			}
			
			sb.append("<br/><br/>");
			sb.append("<br/><br/>");
			sb.append("<br/><br/>");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
