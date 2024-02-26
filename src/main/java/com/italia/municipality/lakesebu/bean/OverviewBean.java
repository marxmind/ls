package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.Mooe;
import com.italia.municipality.lakesebu.controller.Offices;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AccessLevel;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormStatus;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

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


@Named("ovBean")
@ViewScoped
@Data
public class OverviewBean implements Serializable {

	private static final long serialVersionUID = 54578253423231L;
	private String year;
	private int todayMonth;
	private String month;
	private String startDate;
	private String dateToday;
	private String last_Date;
	private Map<Long, Offices> mapOffices;
	private Map<Long, Mooe> mapMooe;
	private Map<Integer, Collector> mapCollector;
	
	private List<Reports> rptRecentChecksIssueds;
	private List<Reports> rptMultipleCheckIssueds;
	private List<Reports> rptMonthCheckIssuedsPerAccounts;
	private List<Reports> rptHighestIssuedChecks;
	private List<Reports> rcds;
	private List<Reports> collections;
	private List<Reports> collectionPrintData;
	
	private List<Reports> collectors;
	private String grandTotalRunningCollection;
	
	private List years;
	
	private String tabSelected;
	private int tabIndex;
	
	@PostConstruct
	public void init() {
		
		rptRecentChecksIssueds = new ArrayList<Reports>();
		rptMultipleCheckIssueds = new ArrayList<Reports>();
		rptMonthCheckIssuedsPerAccounts = new ArrayList<Reports>();
		rptHighestIssuedChecks = new ArrayList<Reports>();
		rcds = new ArrayList<Reports>();
		collections = new ArrayList<Reports>();
		
		year = DateUtils.getCurrentYear()+"";
		years = new ArrayList<>();
		
		for(int y=2018; y<=DateUtils.getCurrentYear(); y++) {
			years.add(new SelectItem(y+"", y+""));
		}

		todayMonth = DateUtils.getCurrentMonth();
		month = todayMonth<10? "0"+todayMonth : todayMonth+"";
		startDate = year + "-" + month + "-01";
		dateToday = DateUtils.getCurrentDateYYYYMMDD();
		last_Date= DateUtils.getLastDayOfTheMonth("yyyy-MM-dd",dateToday, Locale.TAIWAN);
		
		//do not move these line of codes
		loadOffices();
		loadMooe();
		loadCollector();
		
		collections = new ArrayList<Reports>();
		loadCollection();
	}
	
	public void reloadTab() {
		PrimeFaces pf = PrimeFaces.current();
		
		if("Recent Issued Check".equalsIgnoreCase(getTabSelected()) || "Multiple Payee Check Issued".equalsIgnoreCase(getTabSelected())) {
			rptRecentChecksIssueds = new ArrayList<Reports>();
			rptMultipleCheckIssueds = new ArrayList<Reports>();
			loadCheckWriting();
			pf.executeScript("PF('tabSelection').select(2)");
		}else if("Monthly Check Issued".equalsIgnoreCase(getTabSelected())) {
			rptMonthCheckIssuedsPerAccounts = new ArrayList<Reports>();
			monthlyCheckIssued();
			pf.executeScript("PF('tabSelection').select(4)");
		}else if("Highest Issued Check".equalsIgnoreCase(getTabSelected())) {
			rptHighestIssuedChecks = new ArrayList<Reports>();
			loadHighestIssuedChecks();
			pf.executeScript("PF('tabSelection').select(5)");
		}else if("RCD".equalsIgnoreCase(getTabSelected())) {
			rcds = new ArrayList<Reports>();
			loadRCD();
			pf.executeScript("PF('tabSelection').select(1)");
		}else if("Running Collections".equalsIgnoreCase(getTabSelected())) {
			collections = new ArrayList<Reports>();
			loadCollection();
			pf.executeScript("PF('tabSelection').select(0)");
		}else if("Monthly Collector Collection".equalsIgnoreCase(getTabSelected())) {
			collectors = new ArrayList<Reports>();
			loadCollectorCollection();
			pf.executeScript("PF('tabSelection').select(6)");
		}	
	}
	
	public void onTabChange(TabChangeEvent event) {
        
		if("Recent Issued Check".equalsIgnoreCase(event.getTab().getTitle()) || "Multiple Payee Check Issued".equalsIgnoreCase(event.getTab().getTitle())) {
			rptRecentChecksIssueds = new ArrayList<Reports>();
			rptMultipleCheckIssueds = new ArrayList<Reports>();
			loadCheckWriting();
		}else if("Monthly Check Issued".equalsIgnoreCase(event.getTab().getTitle())) {
			rptMonthCheckIssuedsPerAccounts = new ArrayList<Reports>();
			monthlyCheckIssued();
		}else if("Highest Issued Check".equalsIgnoreCase(event.getTab().getTitle())) {
			rptHighestIssuedChecks = new ArrayList<Reports>();
			loadHighestIssuedChecks();
		}else if("RCD".equalsIgnoreCase(event.getTab().getTitle())) {
			rcds = new ArrayList<Reports>();
			loadRCD();
		}else if("Running Collections".equalsIgnoreCase(event.getTab().getTitle())) {
			collections = new ArrayList<Reports>();
			loadCollection();
		}else if("Monthly Collector Collection".equalsIgnoreCase(event.getTab().getTitle())) {
			collectors = new ArrayList<Reports>();
			loadCollectorCollection();
		}	
		setTabSelected(event.getTab().getTitle());
    }
	
	public void loadCollectorCollection() {
		String sql = "SELECT month(receiveddate) as month,sum(amount) as amount,formtypecol,isid "
				+ "FROM collectioninfo WHERE isactivecol=1 AND (receiveddate>='"+ year +"-01-01' AND receiveddate<='"+ year +"-12-31')"
						+ " GROUP BY month,formtypecol,isid ";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		//collector form month amount
		Map<Integer, Map<Integer, Map<Integer, Double>>> collector = new LinkedHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
		Map<Integer, Map<Integer, Double>> form = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> month = new LinkedHashMap<Integer, Double>();
		try {
			while(rs.next()) {
				
				int col = rs.getInt("isid");
				int frm = rs.getInt("formtypecol");
				int mnth = rs.getInt("month");
				double amount = rs.getDouble("amount");
				
				if(collector!=null && collector.size()>0) {
					if(collector.containsKey(col)) {
						if(collector.get(col).containsKey(frm)) {
							if(collector.get(col).get(frm).containsKey(mnth)){
								double newAmount = collector.get(col).get(frm).get(mnth) + amount;
								collector.get(col).get(frm).put(mnth, newAmount);	
							}else {
								collector.get(col).get(frm).put(mnth, amount);							}
						}else {
							month = new LinkedHashMap<Integer, Double>();
							month.put(mnth, amount);
							collector.get(col).put(frm, month);
						}
					}else {
						form = new LinkedHashMap<Integer, Map<Integer, Double>>();
						month = new LinkedHashMap<Integer, Double>();
						month.put(mnth, amount);
						form.put(mnth, month);
						collector.put(col, form);
					}
				}else {
					month.put(frm, amount);
					form.put(frm, month);
					collector.put(col, form);
				}
			}
			
			if(collector!=null && collector.size()>0) {
				for(int id : collector.keySet()) {
					String dep = "";
					Collector col = getMapCollector().get(id);
					try {dep = col.getDepartment().getDepartmentName();}catch(Exception e) {}
					Reports rss = Reports.builder()
							.f1(getMapCollector().get(id).getName())
							.f15(dep)
							.build();
					
					collectors.add(rss);
					
						for(int frm : collector.get(id).keySet()) {
							rss = Reports.builder()
									.f2(FormType.nameId(frm))
									.build();
							
							for(int m=1; m<=12; m++) {
								String amnt = "";
								if(collector.get(id).get(frm).containsKey(m)) {
									amnt = Currency.formatAmount(collector.get(id).get(frm).get(m));
								}
								
								//months
								switch(m) {
									case 1: rss.setF3(amnt); break;
									case 2: rss.setF4(amnt); break;
									case 3: rss.setF5(amnt); break;
									case 4: rss.setF6(amnt); break;
									case 5: rss.setF7(amnt); break;
									case 6: rss.setF8(amnt); break;
									case 7: rss.setF9(amnt); break;
									case 8: rss.setF10(amnt); break;
									case 9: rss.setF11(amnt); break;
									case 10: rss.setF12(amnt); break;
									case 11: rss.setF13(amnt); break;
									case 12: rss.setF14(amnt); break;
								}
								
								
							}
							collectors.add(rss);
						}
					
					}
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Map<Integer, String> accountingGroup(){
		Map<Integer, String> data = new LinkedHashMap<Integer, String>();
		
		ResultSet rs = OpenTableAccess.query("SELECT accid,accname FROM taxaccntgroup WHERE accisactive=1", new String[0], new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				int id = rs.getInt("accid");
				String name = rs.getString("accname");
				data.put(id, name);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	public void loadCollection() {
		
		Map<Integer, String> mapAccount =  accountingGroup();
		
		//String sql = "SELECT month(b.timestampol) as month, a.accname as name, b.olamount as amount FROM taxaccntgroup a,ornamelist b WHERE b.accid=a.accid AND b.orid IN(select o.orid from orlisting o where o.isactiveor=1 AND (o.ordatetrans>='"+ year +"-01-01' AND o.ordatetrans<='"+ year +"-12-31')) ORDER BY amount DESC";
		//o.isactiveol=1 and remove to inclued cancelled
		String sql = "SELECT o.isactiveol as stat,p.orstatus, o.olamount as amount,o.accid as id,month(p.ordatetrans) as month,p.aform,p.isid from ornamelist o, orlisting p WHERE  p.isactiveor=1 and o.orid=p.orid AND (p.ordatetrans>='"+ year +"-01-01' AND p.ordatetrans<='"+ year +"-12-31')";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
		Map<String, Map<Integer, Double>> mapColl = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapMonth = new LinkedHashMap<Integer, Double>();
		double grandTotal = 0d;
		double totalcancelled = 0d;
		double barangayctcamount = 0d;
		double mtoctcamount = 0d;
		double ctctotal = 0d;
		try {
			while(rs.next()) {
				String name =  mapAccount.get(rs.getInt("id"));
				int month = rs.getInt("month");
				int formType = rs.getInt("aform");
				int status = rs.getInt("orstatus"); //==FormStatus.CANCELLED.getId()? true : false;
				boolean isCancelledPayment = rs.getInt("stat")==0? true : false; 
				double amount = rs.getDouble("amount");
				int depId = getMapCollector().get(rs.getInt("isid")).getDepartment().getDepid();
				
				boolean isCTC = false;
				boolean isbarangayCtc = false;
				
				if(FormType.CTC_INDIVIDUAL.getId()==formType) {
					isCTC = true;
				}
				
				if(depId>1) {
					isbarangayCtc = true;
				}
				
				//if(isCancelled) {
				if(FormStatus.CANCELLED.getId()==status) {
					//isCancelledPayment) {
					if(isCancelledPayment) {
						totalcancelled += amount;
					}
				}else if(FormStatus.ENCODED.getId()==status){
					//not cancelled or
					grandTotal += amount;
				
					if(isCTC) {
						ctctotal += amount;
						if(isbarangayCtc) {
							barangayctcamount += amount;
						}else {
							mtoctcamount += amount;
						}
					}
					
				
					if(mapColl!=null && mapColl.size()>0) {
						if(mapColl.containsKey(name)) {
							if(mapColl.get(name).containsKey(month)) {
								double newAmount = mapColl.get(name).get(month) + amount;
								mapColl.get(name).put(month, newAmount);
							}else {
								mapColl.get(name).put(month, amount);
							}
						}else {
							mapMonth = new LinkedHashMap<Integer, Double>();
							mapMonth.put(month, amount);
							mapColl.put(name, mapMonth);
						}
					}else {
						mapMonth.put(month, amount);
						mapColl.put(name, mapMonth);
					}
				
				}
				
			}
			
			setGrandTotalRunningCollection(Currency.formatAmount(grandTotal));
			
			String[][] data = new String[mapColl.size()][14];
			int x = 0;
			double[] totalPerPayment = new double[12];
			//int monthNow = DateUtils.getCurrentMonth();
			for(String name : mapColl.keySet()) {
				
				data[x][0] = name;
				int month = 1;
				double total = 0d;
				for(int m=1; m<=12; m++) {
					
					//boolean isMonthBelowOrEqualNow = monthNow<=m? true : false;
					
					if(mapColl!=null && mapColl.size()>0) {
						if(mapColl.containsKey(name)) {
							if(mapColl.get(name).containsKey(m)) {
								double amount = mapColl.get(name).get(m);
								try{total += amount;
								data[x][month] = Currency.formatAmount(amount);}catch(Exception e){System.out.println("Empty: " + amount);}
								totalPerPayment[m-1] = totalPerPayment[m-1] + amount;
							}else {
								//if(isMonthBelowOrEqualNow) {data[x][month] = "0.00";}
								//totalPerPayment[m-1] = 0.00;
							}
						}else {
							//if(isMonthBelowOrEqualNow) {data[x][month] = "0.00";}
							//totalPerPayment[m-1] = 0.00;
						}
					}else {
						//if(isMonthBelowOrEqualNow) {data[x][month] = "0.00";}
						//totalPerPayment[m-1] = 0.00;
					}
					month++;
				}
				//add total
				data[x][13]=Currency.formatAmount(total);
				
				x++;
			}
			
			//add total
			Reports rss = Reports.builder()
					.f1("PRINT REPORT")
					.f2("PRINT")
					.f3("PRINT")
					.f4("PRINT")
					.f5("PRINT")
					.f6("PRINT")
					.f7("PRINT")
					.f8("PRINT")
					.f9("PRINT")
					.f10("PRINT")
					.f11("PRINT")
					.f12("PRINT")
					.f13("PRINT")
					.f14("")
					.f15("")
					.build();
			collections.add(rss);
			
			//add total
			rss = Reports.builder()
					.f1("TOTAL")
					.f2(Currency.formatAmount(totalPerPayment[0]))
					.f3(Currency.formatAmount(totalPerPayment[1]))
					.f4(Currency.formatAmount(totalPerPayment[2]))
					.f5(Currency.formatAmount(totalPerPayment[3]))
					.f6(Currency.formatAmount(totalPerPayment[4]))
					.f7(Currency.formatAmount(totalPerPayment[5]))
					.f8(Currency.formatAmount(totalPerPayment[6]))
					.f9(Currency.formatAmount(totalPerPayment[7]))
					.f10(Currency.formatAmount(totalPerPayment[8]))
					.f11(Currency.formatAmount(totalPerPayment[9]))
					.f12(Currency.formatAmount(totalPerPayment[10]))
					.f13(Currency.formatAmount(totalPerPayment[11]))
					.f14(Currency.formatAmount(grandTotal))
					.f15("font-weight: bold")
					.build();
			collections.add(rss);
			
			collectionPrintData = new ArrayList<Reports>();
			for(int b=0; b<mapColl.size(); b++) {
				Reports r = Reports.builder()
						.f1(data[b][0])
						.f2(data[b][1])
						.f3(data[b][2])
						.f4(data[b][3])
						.f5(data[b][4])
						.f6(data[b][5])
						.f7(data[b][6])
						.f8(data[b][7])
						.f9(data[b][8])
						.f10(data[b][9])
						.f11(data[b][10])
						.f12(data[b][11])
						.f13(data[b][12])
						.f14(data[b][13])
						.build();
				
				collectionPrintData.add(r);
				collections.add(r);
			}
			
			
			Reports aa = new Reports();
			aa.setF1("Total Cancelled OR");
			aa.setF2("("+Currency.formatAmount(totalcancelled)+")");
			collectionPrintData.add(aa);
			
			aa = new Reports();
			aa.setF1("CTC Collection" + "("+Currency.formatAmount(ctctotal)+")");
			aa.setF2("");
			collectionPrintData.add(aa);
			
			aa = new Reports();
			aa.setF1("MTO CTC Collection" + "("+Currency.formatAmount(mtoctcamount)+")");
			aa.setF2("");
			collectionPrintData.add(aa);
			
			aa = new Reports();
			aa.setF1("Barangay CTC Collection" + "("+Currency.formatAmount(barangayctcamount)+")");
			aa.setF2("");
			collectionPrintData.add(aa);
			
			double sharedAmount = barangayctcamount * 0.50;
			aa = new Reports();
			aa.setF1("Barangay Shared CTC Amount(50%) (" + Currency.formatAmount(sharedAmount)+")");
			aa.setF2("");
			collectionPrintData.add(aa);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void loadRCD() {
		Map<String, Map<Integer, Double>> mapBeginningBal = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapBegMonth = new LinkedHashMap<Integer, Double>();
		
		Map<String, Map<Integer, Double>> mapCollection = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapCollMonth = new LinkedHashMap<Integer, Double>();
		
		Map<String, Map<Integer, Double>> mapDeposit = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapDepMonth = new LinkedHashMap<Integer, Double>();
		
		ResultSet rs = OpenTableAccess.query("SELECT  month(datetrans) as month,fundtype, amount,remarks FROM rcddeposit WHERE isactivercd=1 AND (datetrans>='"+  year  +"-01-01' AND datetrans<='"+  year +"-12-31')", new String[0],new WebTISDatabaseConnect());
		
		try {
			while(rs.next()) {
				int month = rs.getInt("month");
				double amount = rs.getDouble("amount");
				String fund = FundType.typeName(rs.getInt("fundtype"));
				String remarks = rs.getString("remarks");
				
				
				if("BEGINNING BALANCE".equalsIgnoreCase(remarks)) {
					if(mapBeginningBal!=null && mapBeginningBal.size()>0) {
						if(mapBeginningBal.containsKey(fund)) {
							if(mapBeginningBal.get(fund).containsKey(month)) {
								double newAmount = mapBeginningBal.get(fund).get(month) + amount;
								mapBeginningBal.get(fund).put(month, newAmount);
							}else {
								mapBeginningBal.get(fund).put(month, amount);
							}
						}else {
							mapBegMonth = new LinkedHashMap<Integer, Double>();
							mapBegMonth.put(month, amount);
							mapBeginningBal.put(fund, mapBegMonth);
						}
					}else {
						mapBegMonth.put(month, amount);
						mapBeginningBal.put(fund, mapBegMonth);
					}
				}else {
					//deposit
					if(mapDeposit!=null && mapDeposit.size()>0) {
						if(mapDeposit.containsKey(fund)) {
							if(mapDeposit.get(fund).containsKey(month)) {
								double newAmount = mapDeposit.get(fund).get(month) + amount;
								mapDeposit.get(fund).put(month, newAmount);
							}else {
								mapDeposit.get(fund).put(month, amount);
							}
						}else {
							mapDepMonth = new LinkedHashMap<Integer, Double>();
							mapDepMonth.put(month, amount);
							mapDeposit.put(fund, mapDepMonth);
						}
					}else {
						mapDepMonth.put(month, amount);
						mapDeposit.put(fund, mapDepMonth);
					}
				}
				
			}
			
			rs = OpenTableAccess.query("select month(alldatetrans) as month, fundtype,amount from rcdallcontroller WHERE isactiveall=1 AND (alldatetrans>='"+  year +"-01-01' AND alldatetrans<='"+  year +"-12-31')", new String[0],new WebTISDatabaseConnect());
			
			while(rs.next()) {
				int month = rs.getInt("month");
				double amount = rs.getDouble("amount");
				String fund = FundType.typeName(rs.getInt("fundtype"));
				
				if(mapCollection!=null && mapCollection.size()>0) {
					if(mapCollection.containsKey(fund)) {
						if(mapCollection.get(fund).containsKey(month)) {
							double newAmount = mapCollection.get(fund).get(month) + amount;
							mapCollection.get(fund).put(month, newAmount);
						}else {
							mapCollection.get(fund).put(month, amount);
						}
					}else {
						mapCollMonth = new LinkedHashMap<Integer, Double>();
						mapCollMonth.put(month, amount);
						mapCollection.put(fund, mapCollMonth);
					}
				}else {
					mapCollMonth.put(month, amount);
					mapCollection.put(fund, mapCollMonth);
				}
				
			}
			int size = (FundType.values().length*FundType.values().length) + FundType.values().length;
			
			String[][] data = new String[size][14];
			/*data[0][0] = "Fund";
			data[0][1] = "Description";
			int x = 2;
			for(int m=1; m<=12; m++) {
				data[0][x] = DateUtils.getMonthName(m);
				x++;		
			}*/
			
			int xx=0;
			double beginning [] = new double[12];
			double collection [] = new double[12];
			double deposit [] = new double[12];
			
			for(FundType f : FundType.values()) {
				
				//xx += 1;
				
				data[xx][0] = f.getName();
				
				for(int i=1; i<13;i++) {
					data[xx][i] = "";
				}
				
				
				
				//beginning
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "BEGINNING BALANCE";
				int a=2;
				int index = 0;
				
				for(int m=1; m<=12; m++) {//month
					if(mapBeginningBal!=null && mapBeginningBal.size()>0) {
						if(mapBeginningBal.containsKey(f.getName())) {
							if(mapBeginningBal.get(f.getName()).containsKey(m)) {
								data[xx][a] = Currency.formatAmount(mapBeginningBal.get(f.getName()).get(m));
								beginning[index] = mapBeginningBal.get(f.getName()).get(m);
							}else {
								data[xx][a] = "";
								beginning[index] = 0.00;
							}
						}else {
							data[xx][a] = "";
							beginning[index] = 0.00;
						}
					}else {
						data[xx][a] = "";
						beginning[index] = 0.00;
					}
					a++;
					index++;
				}
				
				
				
				//collection
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "COLLECTION";
				a=2;
				index=0;
				
				for(int m=1; m<=12; m++) {//month
					if(mapCollection!=null && mapCollection.size()>0) {
						if(mapCollection.containsKey(f.getName())) {
							if(mapCollection.get(f.getName()).containsKey(m)) {
								data[xx][a] = Currency.formatAmount(mapCollection.get(f.getName()).get(m));
								collection[index] = mapCollection.get(f.getName()).get(m);
							}else {
								data[xx][a] = "";
								collection[index] = 0.00;
							}
						}else {
							data[xx][a] = "";
							collection[index] = 0.00;
						}
					}else {
						data[xx][a] = "";
						collection[index] = 0.00;
					}
					a++;
					index++;
				}
				
				
				//deposit
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "DEPOSIT";
				a=2;
				index=0;
				
				for(int m=1; m<=12; m++) {//month
					if(mapDeposit!=null && mapDeposit.size()>0) {
						if(mapDeposit.containsKey(f.getName())) {
							if(mapDeposit.get(f.getName()).containsKey(m)) {
								data[xx][a] = Currency.formatAmount(mapDeposit.get(f.getName()).get(m));
								deposit[index] = mapDeposit.get(f.getName()).get(m);
							}else {
								data[xx][a] = "";
								deposit[index] = 0.00;
							}
						}else {
							data[xx][a] = "";
							deposit[index] = 0.00;
						}
					}else {
						data[xx][a] = "";
						deposit[index] = 0.00;
					}
					a++;
					index++;
				}
				
				
				//Ending
				xx += 1;
				data[xx][0] = "";
				data[xx][1] = "ENDING BALANCE";
				a=2;
				index=0;
				
				for(int i=0; i<12; i++) {//month
					boolean isNotZero=false;
					isNotZero = collection[i]>0? true : false;
					if(!isNotZero) {
						isNotZero = deposit[i]>0? true : false;
					}
					double amount = (beginning[i] + collection[i]) - deposit[i];
					data[xx][a] = isNotZero==false? "" : Currency.formatAmount(amount);
					a++;
				}
				
				xx++;
			}
			
			for(int b=0; b<size; b++) {
				
					Reports r = Reports.builder()
							.f1(data[b][0])
							.f2(data[b][1])
							.f3(data[b][2])
							.f4(data[b][3])
							.f5(data[b][4])
							.f6(data[b][5])
							.f7(data[b][6])
							.f8(data[b][7])
							.f9(data[b][8])
							.f10(data[b][9])
							.f11(data[b][10])
							.f12(data[b][11])
							.f13(data[b][12])
							.f14(data[b][13])
							.build();
					rcds.add(r);
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
	}
	
	public void loadHighestIssuedChecks() {
		
	ResultSet	rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,c.date_disbursement,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND c.chkstatus='1' AND (c.date_disbursement>='"+  startDate +"' AND c.date_disbursement<='"+  last_Date +"') ORDER BY c.cheque_amount DESC LIMIT 10",new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				
				String mode = rs.getString("bank_account_name");
				String checkNo = rs.getString("cheque_no");
				double amnt = rs.getDouble("amount");
				String disBurseDate = rs. getString("date_disbursement");
				String mooe = mapMooe.get(rs.getLong("moid")).getDescription();
				String opis = mapOffices.get(rs.getLong("offid")).getName();
				String payee = rs.getString("payee");
				
				Reports rr = Reports.builder()
						.f1(DateUtils.convertDateToMonthDayYear(disBurseDate))
						.f2(checkNo)
						.f3(payee)
						.f4(Currency.formatAmount(amnt))
						.f5(mode)
						.f6(opis)
						.f7(mooe)
						.build();
				rptHighestIssuedChecks.add(rr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void monthlyCheckIssued() {
		
		ResultSet rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,MONTH(c.date_disbursement) as month,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND (c.date_disbursement>='"+  year +"-01-01" +"' AND c.date_disbursement<='"+  year+"-12-31" +"')", new String[0],new BankChequeDatabaseConnect());
		
		Map<String, Map<Integer, Double>> mapMonth = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapMode = new LinkedHashMap<Integer, Double>();
		try {
			while(rs.next()) {
				String mode = rs.getString("bank_account_name");
				double amount = rs.getDouble("amount");
				int month = rs.getInt("month");
				if(mapMonth!=null && mapMonth.size()>0) {
					if(mapMonth.containsKey(mode)) {
						if(mapMonth.get(mode).containsKey(month)) {
							double newAmount = mapMonth.get(mode).get(month) + amount;
							mapMonth.get(mode).put(month, newAmount);
						}else {
							mapMonth.get(mode).put(month, amount);
						}
					}else {
						mapMode = new LinkedHashMap<Integer, Double>();
						mapMode.put(month, amount);
						mapMonth.put(mode, mapMode);
					}
				}else {
					mapMode.put(month, amount);
					mapMonth.put(mode, mapMode);
				}
			}
			
			Map<String, Map<Integer, Double>> mapOrg = new TreeMap<String, Map<Integer, Double>>(mapMonth);
			//rptMonthCheckIssuedsPerAccounts
			
			
			
			int size = mapOrg.size() + 1;
			String[][] data = new String[size][13];
			
			data[0][0] = "Name";
			
			for(int m=1; m<=12; m++) {
				data[0][m] = DateUtils.getMonthName(m);
			}
			
			int sz = 1;
			for(String fund : mapOrg.keySet()) {
				data[sz][0] = fund;
				for(int month=1; month<=12; month++) {
					if(mapOrg.get(fund).containsKey(month)) {
						data[sz][month] = Currency.formatAmount(mapOrg.get(fund).get(month));
					}else {
						data[sz][month] = "";
					}
				}
				sz++;
			}
			int y=0;
			for(int x=0; x<size; x++) {
					Reports name = new Reports();
					name.setF1(data[x][0]);
					name.setF2(data[x][1]);
					name.setF3(data[x][2]);
					name.setF4(data[x][3]);
					name.setF5(data[x][4]);
					name.setF6(data[x][5]);
					name.setF7(data[x][6]);
					name.setF8(data[x][7]);
					name.setF9(data[x][8]);
					name.setF10(data[x][9]);
					name.setF11(data[x][10]);
					name.setF12(data[x][11]);
					name.setF13(data[x][12]);
					rptMonthCheckIssuedsPerAccounts.add(name);
					
				//System.out.println(data[x][0] + " = " + data[x][1] + " = " + data[x][2]);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadCheckWriting() {
		ResultSet rs = OpenTableAccess.query("SELECT c.offid,c.moid,c.cheque_no,c.cheque_amount as amount,c.date_disbursement,c.chkstatus,b.bank_account_name,pay_to_the_order_of as payee,c.chkremarks FROM tbl_chequedtls c, tbl_bankaccounts b WHERE c.accnt_no=b.bank_id AND c.isactive=1 AND (c.date_disbursement>='"+  year +"-"+ month +"-01' AND c.date_disbursement<='"+  year +"-"+ month +"-31')", new String[0],new BankChequeDatabaseConnect());
		double amountToday=0d;
		int totalCancelledCheck=0;
		double amountMonth=0d;
		String lastTransaction="";
		double lastPayeeAmount=0d;
		String payee = "";
		String disBurseDate="";
		String remarks = "";
		String mooe = "";
		String opis = "";
		String mode="";
		String checkNo="";
		
		List<Reports> rpt = new ArrayList<Reports>();
		Map<String, List<Reports>> mapMul = new LinkedHashMap<String, List<Reports>>();
		List<Reports> mul = new ArrayList<Reports>();
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
				payee = rs.getString("payee");
				
				Reports rr = Reports.builder()
						.f1(DateUtils.convertDateToMonthDayYear(disBurseDate))
						.f2(checkNo)
						.f3(payee)
						.f4(Currency.formatAmount(amnt))
						.f5(mode)
						.f6(opis)
						.f7(mooe)
						.f10(remarks)
						.build();
				
				//get the total amount today
				if("1".equalsIgnoreCase(stat)) {//issued
					
					
					
					//monthly
					amountMonth += amnt;
					
					//today
					if(dateToday.equalsIgnoreCase(disBurseDate)) {
						amountToday += amnt;
					}
					
					//last transaction
					//lastTransaction = rs.getString("payee");
					lastPayeeAmount = amnt;
					
					
					if(mapMul!=null && mapMul.size()>0) {
						if(mapMul.containsKey(payee)) {
							mul = mapMul.get(payee);
							mul.add(rr);
							mapMul.put(payee, mul);
						}else {
							mul = new ArrayList<Reports>();
							mul.add(rr);
							mapMul.put(payee, mul);
						}
					}else {
						mul.add(rr);
						mapMul.put(payee, mul);
					}
					
					
				}else {
					//get total cancelled check
					totalCancelledCheck +=1;
					rr.setF9("CANCELLED");
					
					
				}
				rpt.add(rr);
				
			}
			
			Collections.reverse(rpt);//reverse the order the last will be the first
			int cnt = 1;
			for(Reports r : rpt) {
				
				if(cnt<=10) {
					r.setF8(r.getF3());
					rptRecentChecksIssueds.add(r);
				}else if(cnt>10) {
					break;
				}
				
				//do not add 1 as it is the last transaction
				/*if(cnt>1 && cnt<12) {
					r.setF8(r.getF3());
					rptRecentChecksIssueds.add(r);
				}else if(cnt==12) {
					break;
				}*/
				cnt++;
			}
			
			
			
			//this for multiple check issued list
			if(mapMul!=null && mapMul.size()>0) {
				
				for(String name : mapMul.keySet()) {
					int size = mapMul.get(name).size();
					if(size>1) {
						Reports rv = Reports.builder().f3(name).build();
						rptMultipleCheckIssueds.add(rv);
						for(Reports v : mapMul.get(name)) {
							v.setF3("");
							rptMultipleCheckIssueds.add(v);
						}
					}
				}
				
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
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
	
	public void printCollection(int month) {
		try{
			List<Reports> rss = new ArrayList<Reports>();
			
			int size = getCollectionPrintData().size();
			
			List<Reports> data = new ArrayList<Reports>();
			
			double totalAmount = 0d;
			for(Reports r : getCollectionPrintData()) {
				Reports a = new Reports();
				double amount = 0d;
				String val = "0.00";
				switch(month) {
					case 1 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF2()); 
						try {val = r.getF2().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 2 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF3()); 
						try {val = r.getF3().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 3 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF4()); 
						try {val = r.getF4().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 4 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF5()); 
						try {val = r.getF5().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;
					case 5 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF6()); 
						try {val = r.getF6().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 6 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF7()); 
						try {val = r.getF7().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 7 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF8()); 
						try {val = r.getF8().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 8 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF9()); 
						try {val = r.getF9().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 9 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF10()); 
						try {val = r.getF10().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 10 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF11()); 
						try {val = r.getF11().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 11 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF12()); 
						try {val = r.getF12().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
					case 12 : 
						a.setF1(r.getF1()); 
						a.setF2(r.getF13()); 
						try {val = r.getF13().replace(",", ""); val=val.isEmpty()? "0.00":val;}catch(Exception e) {}
						try{amount = Double.valueOf(val);
						totalAmount += amount;}catch(NumberFormatException n) {}
						break;	
				}
				
				
				data.add(a);
			}
			
			
			
			if(size>35) {
				int cnt=1;
				List<Reports> r1 = new ArrayList<Reports>();
				List<Reports> r2 = new ArrayList<Reports>();
				int cntRem = 1;
				for(Reports r : data) {
					if(cnt<36) {
						r1.add(r);
					}else {
						r2.add(r);
						cntRem++;
					}
					cnt++;
				}
				cntRem -=1;
				
				//this for r2 only data
				for(int i=cntRem; i<36; i++) {//supply the remaining fields
					r2.add(new Reports());
				}
				
				
				//combine the two list
				//this is for the first page only
				//which is less than 35 rows
				for(int i=0; i<35;i++) {
					Reports r = new Reports();
					r.setF1(r1.get(i).getF1());
					r.setF2(r1.get(i).getF2());
					
					r.setF3(r2.get(i).getF1());
					r.setF4(r2.get(i).getF2());
					
					rss.add(r);
				}
				
				//adding for more than
				//this code is for more than one page
				if(cntRem>=36) {
					for(int i=35; i<cntRem;i++) {
						Reports r = new Reports();
						r.setF1(r2.get(i).getF1());
						r.setF2(r2.get(i).getF2());
						i+=1;
						try{r.setF3(r2.get(i).getF1());}catch(IndexOutOfBoundsException ie) {}
						try{r.setF4(r2.get(i).getF2());}catch(IndexOutOfBoundsException ie) {}
						
						rss.add(r);
					}
				}
				
			}else {
				rss = data;
			}
			
			/*for(Reports r : rss) {
				System.out.println("f1: " + r.getF1() + " f2: " + r.getF2() + " f3: " + r.getF3() + " f4: " + r.getF4());
			}*/
			
			String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
					AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
			String REPORT_NAME = "summaryonly";
			
			ReportCompiler compiler = new ReportCompiler();
			String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
	  		
	  		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rss);
	  		HashMap param = new HashMap();
	  		UserDtls user = Login.getUserLogin().getUserDtls();
	  		param.put("PARAM_TITLE", "SUMMARY OF COLLECTION FOR THE MONTH OF "+DateUtils.getMonthName(month).toUpperCase() + " " + getYear());
	  		param.put("PARAM_TOTAL", "Php"+Currency.formatAmount(totalAmount));
	  		param.put("PARAM_PRINTED_DATE", DateUtils.getCurrentDateMMMMDDYYYY());
	  		
	  		
	  		param.put("PARAM_PREPAREDBY", user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
	  		
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
	  		
	  		String jrprint = JasperFillManager.fillReportToFile(jrxmlFile, param, beanColl);
	  	    JasperExportManager.exportReportToPdfFile(jrprint,REPORT_PATH+ REPORT_NAME +".pdf");
	  	    
	  	    
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
		                //System.out.println("printReportAll read : " + length);
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
		     
		       // backupPrintDispense();
		        
			
		
			System.out.println("pdf successfully created...");
			}catch(Exception e){
				e.printStackTrace();
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
