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

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

@Named
@ViewScoped
@Data
public class OverviewBean implements Serializable {

	/**
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
	
	@PostConstruct
	public void init() {
		sb = new StringBuilder();
		year = DateUtils.getCurrentYear()+"";
		todayMonth = DateUtils.getCurrentMonth();
		month = todayMonth<10? "0"+todayMonth : todayMonth+"";
		startDate = year + "-" + month + "-01";
		dateToday = DateUtils.getCurrentDateYYYYMMDD();
		last_Date= DateUtils.getLastDayOfTheMonth("yyyy-MM-dd",dateToday, Locale.TAIWAN);
		
		sb.append("Good day");
		sb.append("<br/><br/>");
		sb.append("Please see below details for today's transaction <strong style='color: red'>" +  DateUtils.convertDateToMonthDayYear(dateToday)+"</strong>");
		sb.append("<br/><br/>");
		
		//do not move these line of codes
		loadOffices();
		loadMooe();
		
		loadCheckWriting();
		loadCashCollection();
		loadCollection();
		
		setSummaryInfo(sb.toString());
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
			
			
			issueds+="</ul>";
			
			sb.append("<strong>ISSUANCE OF CHECK</strong><br/><br/>");
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
			sb.append("The last transaction was issued to " + lastTransaction + " with a check no of <strong style='color:red'>" + checkNo + "</strong> dated " + DateUtils.convertDateToMonthDayYear(disBurseDate) + " amounting <strong>" + Currency.formatAmount(lastPayeeAmount) +  "</strong> for the <strong>" + remarks+" from fund: "+ mode + "/Office:" + opis + "/Charge to: " + mooe +"</strong>");
			sb.append("</li>");
			sb.append("</ul>");
			sb.append("<br/>");
			
			sb.append("<strong>Here are the 10 recent issuance of checks</strong>");
			sb.append("<br/>");
			sb.append(issueds);
			sb.append("<br/><br/>");
			
			if(mapMul!=null && mapMul.size()>0) {
				String multiple="<strong>Below are the multiple check issued for the same Payee.</strong><br/>";
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
				sb.append("<strong>Following are the Per Account Total Issued Check for " + DateUtils.getMonthName(todayMonth)+"</strong>");
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
					sb.append("<strong>Here are the 10 highest issued check</strong><br/>");
				}
				sb.append(count +") <i>"+DateUtils.convertDateToMonthDayYear(rs.getString("date_disbursement"))+"</i>/"+rs.getString("cheque_no")+"/"+rs.getString("bank_account_name")+"/"+rs.getString("payee")+"/<strong style='color: blue'>"+Currency.formatAmount(rs.getDouble("amount"))+"</strong>/"+rs.getString("chkremarks") + "/Office:" + opis + "/Charge to: " + mooe);
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
		sb.append("Following are the collection for the month of " + DateUtils.getMonthName(todayMonth));
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
			sb.append("Total Running collection for the month of "+ DateUtils.getMonthName(todayMonth) +" as of today is " + Currency.formatAmount(total));
			sb.append("<br/><br/>");
			
			sb.append("Summary report from January till today");
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
			
			sb.append("Whole year collection as of today is " + Currency.formatAmount(total));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
