package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Department;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.Responsibility;
import com.italia.municipality.lakesebu.controller.Signatories;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.controller.VRData;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 07/31/2019
 *
 */
@Named
@ViewScoped
public class VrBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3546768978561L;
	
	private String seriesMonthId;
	private List seriesMonths;
	
	private String seriesMonthIdSearch;
	private List seriesMonthsSearch;
	
	private String voucherNo;
	
	private int accountNo;
	private List accountList;
	
	private String checkNo;
	private Date dateCreated;
	private String payee;
	
	private double grossAmount;
	private double netAmount;
	
	private int departmentId;
	private List department;
	
	private int departmentIdSearch;
	private List departmentSearch;
	
	private String natureOfPayment;
	
	private int sig1;
	private List sigs1;
	private int sig2;
	private List sigs2;
	
	private VRData vrData;
	
	private List<VRData> vrs;
	
	private String totalGross;
	private String totalNet;
	
	private String searchParam;
	
	private List<Reports> rpts;
	
	private int mondId;
	private List months;
	private int yearId;
	private List years;
	
	private long resId;
	private List responsibility;
	
	private int mondIdR;
	private List monthsR;
	private int yearIdR;
	private List yearsR;
	private List<Reports> rptsPer;
	private List<VRData> vrsPer;
	private String seriesMonthIdSearchPer;
	private List seriesMonthsSearchPer;
	
	@PostConstruct
	public void init() {
		vrs = new ArrayList<VRData>();
		String[] params = new String[0];
		String sql = "";
		int month = DateUtils.getCurrentMonth();
		String monthF = getYearId() +"-"+ (month<10? "0"+month : month) +"-01";
		String monthT = getYearId() +"-"+ (month<10? "0"+month : month) +"-31";
		sql += " AND (vr.vrdate>='"+ monthF +"'";
		sql += " AND vr.vrdate<='"+ monthT +"') ";
		sql += " ORDER BY vr.vrid DESC LIMIT 10";
		
		
		
		createReport(VRData.retrieve(sql,params));
		loadSeries();
	}
	
	private void loadSeries() {
		updateMonthSeriesSearch();
		onSeriesChange();
	}
	
	public List<String> autoCode(String query){
		List<String> result = new ArrayList<>();
		result = VRData.retrieve(query, "vrcode",10);
		return result;
	}
	public List<String> autoNature(String query){
		List<String> result = new ArrayList<>();
		result = VRData.retrieve(query, "paymentdesc",10);
		return result;
	}
	public List<String> autoPayToName(String query){
		List<String> result = new ArrayList<>();
		result = VRData.retrieve(query, "payee",10);
		return result;
	}
	
	public void loadSuggestedAmount() {
		double amount = VRData.amountSuggest(getDepartmentId(), getPayee(), getNatureOfPayment());
		setGrossAmount(amount);
		setNetAmount(amount);
	}
	
	public void load() {
		vrs = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		String[] params = new String[0];
		String sql = "";
		
		if(getSeriesMonthIdSearch()!=null && !getSeriesMonthIdSearch().isEmpty()) {
			sql += " AND vr.vrmonthgroup='" + getSeriesMonthIdSearch() +"'";
		}
		
		if(getDepartmentIdSearch()>0) {
			sql += " AND dep.departmentid=" + getDepartmentIdSearch();
		}
		
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			sql += " AND ( ";
			sql += " vr.checkno  like '%"+ getSearchParam().replace("--", "") +"%'";
			sql += " OR ";
			sql += " vr.payee  like '%"+ getSearchParam().replace("--", "") +"%'";
			sql += " OR ";
			sql += " vr.paymentdesc  like '%"+ getSearchParam().replace("--", "") +"%'";
			sql += " OR ";
			sql += " vr.vrcode like '%"+ getSearchParam().replace("--", "") +"%'";
			sql += " )";
		}
		
		if(getMondId()>0) {
			sql += " AND (vr.vrdate>='"+ getYearId() +"-"+ (getMondId()<10? "0"+getMondId() : getMondId()) +"-01'";
			sql += " AND vr.vrdate<='"+ getYearId() +"-"+ (getMondId()<10? "0"+getMondId() : getMondId()) +"-31') ";
		}
		/*
		 * if((getSearchParam()==null || getSearchParam().isEmpty()) &&
		 * getDepartmentIdSearch()==0 && getSeriesMonthIdSearch()==null) { sql +=
		 * " ORDER BY vr.vrid DESC LIMIT 10"; }
		 */
		
		List<VRData> lds = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		lds = VRData.retrieve(sql,params);
		
		int size = lds!=null? lds.size() : 0;
		
		if(size==1) {
			clickItem(lds.get(0));
		}else {
			if(getResId()==0) {
				clear();
			}
		}
		
		buildReport(lds);
		//createReport(VRData.retrieve(sql,params));
	}
	
	
	private void buildReport(List<VRData> vData) {
		
		Map<Long, List<VRData>> vrMap = new LinkedHashMap<Long, List<VRData>>();//Collections.synchronizedMap(new LinkedHashMap<Long, List<VRData>>());
		List<VRData> ldata = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		
		for(VRData v : vData) {
			
			long id = v.getResponsibility().getId();
			//long id = v.getDepartment().getDepid();
			
			//System.out.println("id:"+ id + " code "+ v.getRe +"Payee>> " + v.getPayee());
			
			if(vrMap!=null && vrMap.size()>0) {
				if(vrMap.containsKey(id)) {
					ldata = vrMap.get(id);
					ldata.add(v);
					vrMap.put(id, ldata);
				}else {
					ldata = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
					ldata.add(v);
					vrMap.put(id, ldata);
				}
			}else {
				ldata.add(v);
				vrMap.put(id, ldata);
			}
		}
		
		//Map<Long, List<VRData>> vrMapSorted = new TreeMap<Long, List<VRData>>(vrMap);
		
		rpts = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		vrs = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		
		double gross = 0d;
		double net = 0d;
		for(long key : vrMap.keySet()) {
			int cnt = 1;
			int size = vrMap.get(key).size();
			for(VRData v : vrMap.get(key)) {
				vrs.add(v);
				
				gross += v.getGross();
				net += v.getNet();
				
				Reports r = new Reports();
				if(cnt==1) {
					
					if(v.getResponsibility().getId()>0) {
						r.setF1(v.getMonthGroup());
						r.setF2(v.getDepartment().getCode());
						
						String[] date = v.getDate().split("-");
						String lastday = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", v.getDate(), Locale.TAIWAN).split("-")[2];
						String dRange = date[1] + "/1-"+ lastday +"/"+ date[0]; 
						
						r.setF3(dRange);
						r.setF4("");
						r.setF5(v.getResponsibility().getName());
						r.setF6("");
						r.setF7("no line");
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
						rpts.add(r);
						
						r = new Reports();
						r.setF1("");
						r.setF2("");
						r.setF3("");
						r.setF4(v.getVoucherSeries());
						
						r.setF5(v.getPaymentDesc());
						r.setF6(Currency.formatAmount(v.getGross()));
						
						if(size==cnt) {
							r.setF7(null);
						}else {
							r.setF7("no line");
						}
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
						rpts.add(r);
						
					}else {
						r = new Reports();
						r.setF1(v.getMonthGroup());
						r.setF2(v.getDepartment().getCode());
						r.setF3(v.getDate());
						r.setF4(v.getVoucherSeries());
						r.setF5(v.getPayee());
						r.setF6(Currency.formatAmount(v.getGross()));
						
						if(size==cnt) {
							r.setF7(null);
						}else {
							r.setF7("no line");
						}
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
						rpts.add(r);
					}
					
					
				}else {
					
					if(v.getResponsibility().getId()>0) {
						r.setF1("");
						r.setF2("");
						
						r.setF3("");
						r.setF4(v.getVoucherSeries());
						
						r.setF5(v.getPaymentDesc());
						r.setF6(Currency.formatAmount(v.getGross()));
						
						if(size==cnt) {
							r.setF7(null);
						}else {
							r.setF7("no line");
						}
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
					}else {
						/*
						 * r.setF1(""); r.setF2("");
						 * 
						 * r.setF3(v.getDate()); r.setF4(v.getVoucherSeries());
						 * 
						 * r.setF5(v.getPayee()); r.setF6(Currency.formatAmount(v.getGross()));
						 */
						r = new Reports();
						r.setF1(v.getMonthGroup());
						r.setF2(v.getDepartment().getCode());
						r.setF3(v.getDate());
						r.setF4(v.getVoucherSeries());
						r.setF5(v.getPayee());
						r.setF6(Currency.formatAmount(v.getGross()));
						r.setF7(null);
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
					}
					
					/*
					 * if(size==cnt) { r.setF7(null); }else { r.setF7("no line"); }
					 */
					rpts.add(r);
				}
				
				cnt++;
			}
		}
		setTotalGross("Php"+Currency.formatAmount(gross));
		setTotalNet("Php"+Currency.formatAmount(net));
		
	}
	
	private void createReport(List<VRData> vData) {
		
		//file name
		//center
		//code
		Map<String, Map<String, Map<String, List<VRData>>>> vrDataMap = new LinkedHashMap<String, Map<String, Map<String, List<VRData>>>>();//Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Map<String, List<VRData>>>>());
		
		//center
		Map<String, Map<String, List<VRData>>> center = new LinkedHashMap<String, Map<String, List<VRData>>>();//Collections.synchronizedMap(new LinkedHashMap<String, Map<String, List<VRData>>>());
		//code
		Map<String, List<VRData>> codes = new LinkedHashMap<String, List<VRData>>();//Collections.synchronizedMap(new LinkedHashMap<String, List<VRData>>());
		//data list
		List<VRData> vrData = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		
		for(VRData v : vData) {
			
			String group = v.getMonthGroup();
			String dep = v.getDepartment().getCode();
			String cod = v.getVoucherSeries();
			
			if(vrDataMap!=null && vrDataMap.size()>0) {
				
				if(vrDataMap.containsKey(group)) {
					
					if(vrDataMap.get(group).containsKey(dep)) {
						
						if(vrDataMap.get(group).get(dep).containsKey(cod)) {
							vrDataMap.get(group).get(dep).get(cod).add(v);
						}else {
							vrData = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
							vrData.add(v);
							vrDataMap.get(group).get(dep).put(cod, vrData);
						}
						
					}else {
						vrData = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
						codes = new LinkedHashMap<String, List<VRData>>();//Collections.synchronizedMap(new LinkedHashMap<String, List<VRData>>());
						
						vrData.add(v);
						codes.put(cod, vrData);
						vrDataMap.get(group).put(dep, codes);
					}
					
				}else {
					center = new LinkedHashMap<String, Map<String, List<VRData>>>();//Collections.synchronizedMap(new LinkedHashMap<String, Map<String, List<VRData>>>());
					codes = new LinkedHashMap<String, List<VRData>>();//Collections.synchronizedMap(new LinkedHashMap<String, List<VRData>>());
					vrData = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
					
					vrData.add(v);
					codes.put(cod, vrData);
					center.put(dep, codes);
					
					vrDataMap.put(group, center);
				}
				
			}else {
				vrData.add(v);
				codes.put(cod, vrData);
				center.put(dep, codes);
				vrDataMap.put(group, center);
			}
			
		}
		
		Map<String, Map<String, Map<String, List<VRData>>>> sortedVRDATA = new TreeMap<String, Map<String,Map<String,List<VRData>>>>(vrDataMap);
		rpts = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		double gross = 0d;
		double net = 0d;
		for(String g : sortedVRDATA.keySet()) {
			
			for(String c : sortedVRDATA.get(g).keySet()) {
				
				for(String d : sortedVRDATA.get(g).get(c).keySet()) {
					
					int cnt = 1;
					Reports r = new Reports();
					int size = sortedVRDATA.get(g).get(c).get(d).size();
					for(VRData v : sortedVRDATA.get(g).get(c).get(d)) {
						
						vrs.add(v);
						
						gross += v.getGross();
						net += v.getNet();
						
						if(cnt==1) {
							
							/*
							 * if(v.getResponsibility().getId()>0) {
							 * 
							 * r = new Reports(); r.setF1(""); r.setF2(""); r.setF3(""); r.setF4("");
							 * r.setF5(v.getResponsibility().getName()); r.setF6("");
							 * r.setF7("do not print");//this is for line use rpts.add(r); }
							 */
							
							
							r = new Reports();
							r.setF1(g);
							r.setF2(c);
							r.setF3(d);
							r.setF4(v.getDate());
							if(v.getResponsibility().getId()>0) {
								r.setF5(v.getPaymentDesc());
							}else {
								r.setF5(v.getPayee());
							}
							r.setF6(Currency.formatAmount(v.getGross()));
							if(size==cnt) {
								r.setF7(null);//this is for line use
							}else {
								r.setF7("do not print");//this is for line use
							}	
							rpts.add(r);
						}else {
							r = new Reports();
							r.setF1("");
							r.setF2("");
							r.setF3("");
							r.setF4(v.getDate());
							if(v.getResponsibility().getId()>0) {
								r.setF5(v.getPaymentDesc());
							}else {
								r.setF5(v.getPayee());
							}
							r.setF6(Currency.formatAmount(v.getGross()));
							if(size==cnt) {
								r.setF7(null);//this is for line use
							}else {
								r.setF7("do not print");//this is for line use
							}
							rpts.add(r);
						}
						cnt++;
					}
					/*
					 * r = new Reports(); r.setF1("---------------------------------");
					 * r.setF2("---------"); r.setF3("-----------------");
					 * r.setF4("----------------");
					 * r.setF5("---------------------------------------------------------------");
					 * r.setF6("-------------------------"); rpts.add(r);
					 */
				}
			}
		}
		setTotalGross("Php"+Currency.formatAmount(gross));
		setTotalNet("Php"+Currency.formatAmount(net));
	}
	
	
	public void saveData() {
		VRData data = new VRData();
		boolean isOk = true;
		if(getVrData()!=null) {
			data = getVrData();
		}else {
			data.setIsActive(1);
		}
		
		if(getVoucherNo()==null || getVoucherNo().isEmpty()) {
			isOk = false;
			Application.addMessage(2, "Error", "Please provide code no");
		}
		
		if(getCheckNo()==null || getCheckNo().isEmpty()) {
			isOk = false;
			Application.addMessage(2, "Error", "Please provide check no");
		}
		
		if(getPayee()==null || getPayee().isEmpty()) {
			isOk = false;
			Application.addMessage(2, "Error", "Please provide payee name");
		}
		
		if(getNatureOfPayment()==null || getNatureOfPayment().isEmpty()) {
			isOk = false;
			Application.addMessage(2, "Error", "Please provide nature of payment");
		}
		
		if(getGrossAmount()==0) {
			isOk = false;
			Application.addMessage(2, "Error", "Please provide gross amount");
		}
		
		if(getCheckNo()!=null && getVrData()==null && getResId()==0) {
			if(VRData.isDuplicate(getCheckNo(), getDepartmentId())) {
				isOk = false;
				Application.addMessage(2, "Error", "This information is already recorded. Duplication of data is not allowed.");
			}
		}
		
		if(isOk) {
			
			data.setDate(DateUtils.convertDate(getDateCreated(),"yyyy-MM-dd"));
			data.setMonthGroup(getSeriesMonthId());
			
			Department dep = new Department();
			dep.setDepid(getDepartmentId());
			data.setDepartment(dep);
			
			BankAccounts acc = new BankAccounts();
			acc.setBankId(getAccountNo());
			data.setAccounts(acc);
			
			data.setCheckNo(getCheckNo());
			data.setPayee(StringUtils.convertFirstCharToUpperCase(getPayee()));
			data.setPaymentDesc(StringUtils.convertFirstCharToUpperCase(getNatureOfPayment()));
			
			data.setGross(getGrossAmount());
			data.setNet(getNetAmount());
			
			data.setSignatory1(getSig1());
			data.setSignatory2(getSig2());
			
			data.setVoucherSeries(getVoucherNo());
			
			Responsibility res = new Responsibility();
			res.setId(getResId());
			data.setResponsibility(res);
			
			data.save();
			
			
			setSeriesMonthIdSearch(getSeriesMonthId());
			setDepartmentIdSearch(getDepartmentId());
			
			System.out.println("Check responsibility... " + getResId());
			
			//clear the field if responsibility is not present
			if(getResId()==0) {
				clear();
			}else {
				setVrData(null);
				setVoucherNo(null);
				setGrossAmount(0);
				setNetAmount(0);
				setNatureOfPayment(null);
			}
			
			load();
		}
		
	}
	
	public void loadNetAmount() {
		setNetAmount(getGrossAmount());
	}
	
	public void clickItem(VRData vr) {
		setVrData(vr);
		setSeriesMonthId(vr.getMonthGroup());
		setVoucherNo(vr.getVoucherSeries());
		setAccountNo(vr.getAccounts().getBankId());
		setCheckNo(vr.getCheckNo());
		setDateCreated(DateUtils.convertDateString(vr.getDate(), "yyyy-MM-dd"));
		setPayee(vr.getPayee());
		setGrossAmount(vr.getGross());
		setNetAmount(vr.getNet());
		setDepartmentId(vr.getDepartment().getDepid());
		setNatureOfPayment(vr.getPaymentDesc());
		setSig1(vr.getSignatory1());
		setSig2(vr.getSignatory2());
		setResId(vr.getResponsibility().getId());
		setDepartmentId(vr.getDepartment().getDepid());
	}
	
	public void deleteRow(VRData vr) {
		vr.delete();
		load();
		clear();
	}
	
	public void clear() {
		setVrData(null);
		//setSeriesMonthId(null);
		setVoucherNo(null);
		setAccountNo(0);
		setCheckNo(null);
		//setDateCreated(null);
		setPayee(null);
		setGrossAmount(0);
		setNetAmount(0);
		setDepartmentId(0);
		setNatureOfPayment(null);
		setSig1(0);
		setSig2(0);
		setDepartmentId(0);
		setResId(0);
		
		getResponsibility();
		getDepartment();
	}
	
	public void loadCenterFromResponsibility() {
		String sql = " AND rss.rid="+ getResId();
		String[] params = new String[0];
		Responsibility res = null;
		try{
			res = Responsibility.retrieve(sql, params).get(0);
			setDepartmentId(res.getDepartment().getDepid());
			setPayee(res.getName());
		}catch(Exception e) {}
			
	}
	
	
	public void loadDepartmentExpense() {
		
		//String text = "<p>Please see below retrive data for the month of " + DateUtils.getMonthName(getMondIdR()) + " for the year " + getYearIdR() + ".</p>";
		//text += "<ul>";
		//for(String v : VRData.retrieve(getYearIdR(), getMondIdR(), "departmentid", 0)) {
			//Department dep = Department.department(v);
			//text += "<li>" + dep.getDepartmentName() + "</li>";
		//}
		//text += "</ul>";
		//setTextContent(text);
		String[] params = new String[3];
		int year = getYearIdR();
		int month = getMondIdR();
		String sql = " AND vr.isactivevr=1 AND (vr.vrdate>=? AND vr.vrdate<=?) AND vr.vrmonthgroup=?";
		params[0] = year +"-"+ (month<10? "0"+month: month) +"-01";
		params[1] = year +"-"+ (month<10? "0"+month: month) +"-31";
		params[2] = getSeriesMonthIdSearchPer();
		
		Map<Integer, VRData> vrMap = new LinkedHashMap<Integer, VRData>();//Collections.synchronizedMap(new LinkedHashMap<Integer, VRData>());
		for(VRData vr : VRData.retrieve(sql, params)) {
			int key = vr.getDepartment().getDepid();
			if(vrMap!=null && vrMap.size()>0) {
				if(vrMap.containsKey(key)) {
					double amount = vrMap.get(key).getGross();
					amount += vr.getGross();
					vr.setGross(amount);
					vrMap.put(key, vr);
				}else {
					vrMap.put(key, vr);
				}
			}else {
				vrMap.put(key, vr);
			}
		}
		
		vrsPer = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		for(VRData v : vrMap.values()) {
			vrsPer.add(v);
		}
		
		
	}
	
	
	public void loadPer(VRData vrData) {

		String[] params = new String[0];
		String sql = "";
		
		
		sql += " AND vr.vrmonthgroup='" + vrData.getMonthGroup() +"'";
		sql += " AND dep.departmentid=" + vrData.getDepartment().getDepid();
		sql += " AND (vr.vrdate>='"+ vrData.getDate().split("-")[0] +"-"+ vrData.getDate().split("-")[1] +"-01'";
		sql += " AND vr.vrdate<='"+ vrData.getDate().split("-")[0] +"-"+ vrData.getDate().split("-")[1] +"-31') ";
		
		Map<Long, List<VRData>> vrMap = new LinkedHashMap<Long, List<VRData>>();//Collections.synchronizedMap(new LinkedHashMap<Long, List<VRData>>());
		List<VRData> ldata = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
		
		for(VRData v : VRData.retrieve(sql,params)) {
			
			long id = v.getResponsibility().getId();
			
			if(vrMap!=null && vrMap.size()>0) {
				if(vrMap.containsKey(id)) {
					ldata = vrMap.get(id);
					ldata.add(v);
					vrMap.put(id, ldata);
				}else {
					ldata = new ArrayList<VRData>();//Collections.synchronizedList(new ArrayList<VRData>());
					ldata.add(v);
					vrMap.put(id, ldata);
				}
			}else {
				ldata.add(v);
				vrMap.put(id, ldata);
			}
		}
		
		rptsPer = new ArrayList<Reports>();//Collections.synchronizedList(new ArrayList<Reports>());
		
		
		double gross = 0d;
		double net = 0d;
		for(long key : vrMap.keySet()) {
			int cnt = 1;
			int size = vrMap.get(key).size();
			for(VRData v : vrMap.get(key)) {
				
				
				gross += v.getGross();
				net += v.getNet();
				
				Reports r = new Reports();
				if(cnt==1) {
					
					if(v.getResponsibility().getId()>0) {
						r.setF1(v.getMonthGroup());
						r.setF2(v.getDepartment().getCode());
						
						String[] date = v.getDate().split("-");
						String lastday = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", v.getDate(), Locale.TAIWAN).split("-")[2];
						String dRange = date[1] + "/1-"+ lastday +"/"+ date[0]; 
						
						r.setF3(dRange);
						r.setF4("");
						r.setF5(v.getResponsibility().getName());
						r.setF6("");
						r.setF7("no line");
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
						rptsPer.add(r);
						
						r = new Reports();
						r.setF1("");
						r.setF2("");
						r.setF3("");
						r.setF4(v.getVoucherSeries());
						
						r.setF5(v.getPaymentDesc());
						r.setF6(Currency.formatAmount(v.getGross()));
						
						if(size==cnt) {
							r.setF7(null);
						}else {
							r.setF7("no line");
						}
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
						rptsPer.add(r);
						
					}else {
						r = new Reports();
						r.setF1(v.getMonthGroup());
						r.setF2(v.getDepartment().getCode());
						r.setF3(v.getDate());
						r.setF4(v.getVoucherSeries());
						r.setF5(v.getPayee());
						r.setF6(Currency.formatAmount(v.getGross()));
						
						if(size==cnt) {
							r.setF7(null);
						}else {
							r.setF7("no line");
						}
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
						rptsPer.add(r);
					}
					
					
				}else {
					
					if(v.getResponsibility().getId()>0) {
						r.setF1("");
						r.setF2("");
						
						r.setF3("");
						r.setF4(v.getVoucherSeries());
						
						r.setF5(v.getPaymentDesc());
						r.setF6(Currency.formatAmount(v.getGross()));
						
						if(size==cnt) {
							r.setF7(null);
						}else {
							r.setF7("no line");
						}
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
					}else {
						
						r = new Reports();
						r.setF1(v.getMonthGroup());
						r.setF2(v.getDepartment().getCode());
						r.setF3(v.getDate());
						r.setF4(v.getVoucherSeries());
						r.setF5(v.getPayee());
						r.setF6(Currency.formatAmount(v.getGross()));
						r.setF7(null);
						r.setF8(v.getDepartment().getDepartmentName());
						r.setF9(v.getDepartment().getDepartmentHead());
					}
					
					
					rptsPer.add(r);
				}
				
				cnt++;
			}
		}
		//setTotalGross("Php"+Currency.formatAmount(gross));
		//setTotalNet("Php"+Currency.formatAmount(net));
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = "expense";
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		String departmentName = "";
		String depHeadName = "";
		if(rptsPer!=null && rptsPer.size()>0) {
			departmentName = rptsPer.get(0).getF8();
			depHeadName = rptsPer.get(0).getF9();
		}
		System.out.println("Department Head: " + depHeadName);
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(rptsPer);
  		//HashMap param = new HashMap();
		
  		
  		Map<String, Object> param = new LinkedHashMap<String, Object>();//Collections.synchronizedMap(new LinkedHashMap<String, Object>());
  		
  		param.put("PARAM_TOTAL", Currency.formatAmount(gross));

  		param.put("PARAM_REPORT_EXPENSE_NAME", vrData.getMonthGroup());
  		UserDtls user = Login.getUserLogin().getUserDtls();
  		param.put("PARAM_PREPARED", user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
  		param.put("PARAM_DEPARTMENT", departmentName);
  		param.put("PARAM_DEPHEAD", depHeadName);
  		//logo
		/*
		 * String officialLogo = REPORT_PATH + "logo.png"; try{File file = new
		 * File(officialLogo); FileInputStream off = new FileInputStream(file);
		 * param.put("PARAM_LOGO", off); }catch(Exception e){e.printStackTrace();}
		 */
		
		//logo
		/*
		 * String officialLogotrans = REPORT_PATH + "logotrans.png"; try{File file = new
		 * File(officialLogotrans); FileInputStream off = new FileInputStream(file);
		 * param.put("PARAM_LOGO_TRANS", off); }catch(Exception e){e.printStackTrace();}
		 */
  		
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
		            input = new BufferedInputStream(new FileInputStream(file),GlobalVar.DEFAULT_BUFFER_SIZE);

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
	
	public void setSig1(int sig1){
		this.sig1 = sig1;
	}
	public int getSig1(){
		if(sig1==0){
			sig1 = 1;
		}
		return sig1;
	}
	public void setSig2(int sig2){
		this.sig2 = sig2;
	}
	public int getSig2(){
		if(sig2==0){
			sig2 = 3;
		}
		return sig2;
	}
	public int getAccountNo() {
		if(accountNo==0) {
			accountNo = 2;
		}
		return accountNo;
	}
	public void setAccountNo(int accountNo) {
		this.accountNo = accountNo;
	}
	public List getAccountList() {
		
		accountList = new ArrayList<>();
		for(BankAccounts a : BankAccounts.retrieve("SELECT * FROM tbl_bankaccounts", new String[0])){
			accountList.add(new SelectItem(a.getBankId(),a.getBankAccntNo()));
		}
		
		return accountList;
	}
	public void setAccountList(List accountList) {
		this.accountList = accountList;
	}
	
	public void loadNewCheckNo() {
		checkNo = VRData.newCheckNo(getAccountNo());
	}
	
	public String getCheckNo() {
		if(checkNo==null) {
			checkNo = VRData.newCheckNo(getAccountNo());
		}
		return checkNo;
	}
	public void setCheckNo(String checkNo) {
		this.checkNo = checkNo;
	}
	public Date getDateCreated() {
		if(dateCreated==null) {
			dateCreated = DateUtils.getDateToday();
		}
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getPayee() {
		return payee;
	}
	public void setPayee(String payee) {
		this.payee = payee;
	}
	public double getGrossAmount() {
		return grossAmount;
	}
	public void setGrossAmount(double inputAmount) {
		this.grossAmount = inputAmount;
	}
	public int getDepartmentId() {
		if(departmentId==0){
			departmentId = 49;
		}
		return departmentId;
	}
	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}
	public List getDepartment() {
		
		department = new ArrayList<>();
		for(Department dep : Department.retrieve("SELECT * FROM department order by departmentname", new String[0])){
			department.add(new SelectItem(dep.getDepid(), dep.getCode() + "-" + dep.getDepartmentName()));
		}
		
		return department;
	}
	public void setDepartment(List department) {
		this.department = department;
	}
	public String getNatureOfPayment() {
		return natureOfPayment;
	}
	public void setNatureOfPayment(String natureOfPayment) {
		this.natureOfPayment = natureOfPayment;
	}
	public List getSigs1() {
		
		sigs1 = new ArrayList<>();
		for(Signatories sig : Signatories.retrieve("SELECT * FROM signatory WHERE isofficial=0", new String[0])) {
			sigs1.add(new SelectItem(sig.getId(), sig.getName()));
		}
		
		return sigs1;
	}
	public void setSigs1(List sigs1) {
		this.sigs1 = sigs1;
	}
	public List getSigs2() {
		
		sigs2 = new ArrayList<>();
		for(Signatories sig : Signatories.retrieve("SELECT * FROM signatory WHERE isofficial=1", new String[0])) {
			sigs2.add(new SelectItem(sig.getId(), sig.getName()));
		}
		
		return sigs2;
	}
	public void setSigs2(List sigs2) {
		this.sigs2 = sigs2;
	}
	public double getNetAmount() {
		return netAmount;
	}
	public void setNetAmount(double netAmount) {
		this.netAmount = netAmount;
	}
	public String getSeriesMonthId() {
		if(seriesMonthId==null) {
			String monthTrim =DateUtils.getMonthName(DateUtils.getCurrentMonth()).toUpperCase().substring(0, 3);
			seriesMonthId = "101-200-18-" + monthTrim + "-" + DateUtils.getCurrentYear();
		}
		return seriesMonthId;
	}
	public void setSeriesMonthId(String seriesMonthId) {
		this.seriesMonthId = seriesMonthId;
	}
	public List getSeriesMonths() {
		
		/*seriesMonths = new ArrayList<>();
		for(int month=1; month<=DateUtils.getCurrentMonth(); month++) {
			
			String monthTrim = DateUtils.getMonthName(month).toUpperCase().substring(0, 3);
			
			String ser1 = "101-100-17-" + monthTrim + "-" + DateUtils.getCurrentYear();
			String ser2 = "101-200-18-" + monthTrim + "-" + DateUtils.getCurrentYear();
			String ser3 = "101-300-17-" + monthTrim + "-" + DateUtils.getCurrentYear();
			
			seriesMonths.add(new SelectItem(ser1, ser1));
			seriesMonths.add(new SelectItem(ser2, ser2));
			seriesMonths.add(new SelectItem(ser3, ser3));
			
		}*/
		
		return seriesMonths;
	}
	
public void printExpense() {
		
		//compiling report
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = "expense";
		System.out.println("Report path " + REPORT_PATH + " name " + REPORT_NAME);
		ReportCompiler compiler = new ReportCompiler();
		String jrxmlFile = compiler.compileReport(REPORT_NAME, REPORT_NAME, REPORT_PATH);
		
		String departmentName = "";
		String depHeadName = "";
		if(getRpts()!=null && getRpts().size()>0) {
			departmentName = getRpts().get(0).getF8();
			depHeadName = getRpts().get(0).getF9();
		}
		System.out.println("Department Head: " + depHeadName);
		JRBeanCollectionDataSource beanColl = new JRBeanCollectionDataSource(getRpts());
  		//HashMap param = new HashMap();
		
  		
  		Map<String, Object> param = new LinkedHashMap<String, Object>();//Collections.synchronizedMap(new LinkedHashMap<String, Object>());
  		
  		String date = "Report for the month of " + DateUtils.getMonthName(getMondId()) + " " + getYearId();
  		
  		param.put("PARAM_DATE", date);
  		param.put("PARAM_TOTAL", getTotalGross());
  		String expenseName = getSeriesMonthIdSearch()==null? "All" : (getSeriesMonthIdSearch().isEmpty()? "All" : getSeriesMonthIdSearch());
  		param.put("PARAM_REPORT_EXPENSE_NAME", expenseName);
  		UserDtls user = Login.getUserLogin().getUserDtls();
  		param.put("PARAM_PREPARED", user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
  		param.put("PARAM_DEPARTMENT", departmentName);
  		param.put("PARAM_DEPHEAD", depHeadName);
  		//logo
		/*
		 * String officialLogo = REPORT_PATH + "logo.png"; try{File file = new
		 * File(officialLogo); FileInputStream off = new FileInputStream(file);
		 * param.put("PARAM_LOGO", off); }catch(Exception e){e.printStackTrace();}
		 */
		
		//logo
		/*
		 * String officialLogotrans = REPORT_PATH + "logotrans.png"; try{File file = new
		 * File(officialLogotrans); FileInputStream off = new FileInputStream(file);
		 * param.put("PARAM_LOGO_TRANS", off); }catch(Exception e){e.printStackTrace();}
		 */
  		
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
	
	
	
	
	public void setSeriesMonths(List seriesMonths) {
		this.seriesMonths = seriesMonths;
	}

	public VRData getVrData() {
		return vrData;
	}

	public void setVrData(VRData vrData) {
		this.vrData = vrData;
	}

	public String getVoucherNo() {
		return voucherNo;
	}

	public void setVoucherNo(String voucherNo) {
		this.voucherNo = voucherNo;
	}

	public List<VRData> getVrs() {
		return vrs;
	}

	public void setVrs(List<VRData> vrs) {
		this.vrs = vrs;
	}

	public String getSeriesMonthIdSearch() {
		return seriesMonthIdSearch;
	}

	public void setSeriesMonthIdSearch(String seriesMonthIdSearch) {
		this.seriesMonthIdSearch = seriesMonthIdSearch;
	}

	public List getSeriesMonthsSearch() {
		
		return seriesMonthsSearch;
	}
	
	public void updateMonthSeriesSearch() {
		seriesMonthsSearch = new ArrayList<>();
		//for(int month=1; month<=DateUtils.getCurrentMonth(); month++) {
		//for(int month=getMondId(); month<=getMondId(); month++) {
			
			String monthTrim = DateUtils.getMonthName(getMondId()).toUpperCase().substring(0, 3);
			
			String ser1 = "101-100-17-" + monthTrim + "-" + getYearId();
			String ser2 = "101-200-18-" + monthTrim + "-" + getYearId();
			String ser3 = "101-300-17-" + monthTrim + "-" + getYearId();
			
			seriesMonthsSearch.add(new SelectItem(ser1, ser1));
			seriesMonthsSearch.add(new SelectItem(ser2, ser2));
			seriesMonthsSearch.add(new SelectItem(ser3, ser3));
			
		//}
	}
	
	public void onSeriesChange() {
		System.out.println("updating series....");
		seriesMonths = new ArrayList<>();
		String[] dateSer = DateUtils.convertDate(getDateCreated(), "yyyy-MM-dd").split("-");
		
		int year=Integer.valueOf(dateSer[0]);
		int mnth=Integer.valueOf(dateSer[1]);
		
		for(int month=mnth; month<=mnth; month++) {
			
			String monthTrim = DateUtils.getMonthName(month).toUpperCase().substring(0, 3);
			
			String ser1 = "101-100-17-" + monthTrim + "-" + year;
			String ser2 = "101-200-18-" + monthTrim + "-" + year;
			String ser3 = "101-300-17-" + monthTrim + "-" + year;
			System.out.println("series : " + ser1 + "-" + ser2 + "-" + ser3);
			seriesMonths.add(new SelectItem(ser1, ser1));
			seriesMonths.add(new SelectItem(ser2, ser2));
			seriesMonths.add(new SelectItem(ser3, ser3));
			
		}
	}
	
	public void setSeriesMonthsSearch(List seriesMonthsSearch) {
		this.seriesMonthsSearch = seriesMonthsSearch;
	}

	public String getTotalGross() {
		return totalGross;
	}

	public void setTotalGross(String totalGross) {
		this.totalGross = totalGross;
	}

	public String getTotalNet() {
		return totalNet;
	}

	public void setTotalNet(String totalNet) {
		this.totalNet = totalNet;
	}

	public int getDepartmentIdSearch() {
		return departmentIdSearch;
	}

	public void setDepartmentIdSearch(int departmentIdSearch) {
		this.departmentIdSearch = departmentIdSearch;
	}

	public List getDepartmentSearch() {
		departmentSearch = new ArrayList<>();
		for(Department dep : Department.retrieve("SELECT * FROM department order by departmentname", new String[0])){
			departmentSearch.add(new SelectItem(dep.getDepid(), dep.getCode() + "-" + dep.getDepartmentName()));
		}
		return departmentSearch;
	}

	public void setDepartmentSearch(List departmentSearch) {
		this.departmentSearch = departmentSearch;
	}

	public String getSearchParam() {
		return searchParam;
	}

	public void setSearchParam(String searchParam) {
		this.searchParam = searchParam;
	}


	public List<Reports> getRpts() {
		return rpts;
	}


	public void setRpts(List<Reports> rpts) {
		this.rpts = rpts;
	}


	public int getMondId() {
		if(mondId==0) {
			mondId = DateUtils.getCurrentMonth();
		}
		return mondId;
	}


	public void setMondId(int mondId) {
		this.mondId = mondId;
	}


	public List getMonths() {
		months = new ArrayList<>();
		months.add(new SelectItem(0, "All Months"));
		for(int month=1; month<=12; month++) {
			months.add(new SelectItem(month, DateUtils.getMonthName(month)));
		}
		return months;
	}


	public void setMonths(List months) {
		this.months = months;
	}


	public int getYearId() {
		if(yearId==0) {
			yearId = DateUtils.getCurrentYear();
		}
		return yearId;
	}


	public void setYearId(int yearId) {
		this.yearId = yearId;
	}


	public List getYears() {
		years = new ArrayList<>();
		for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
		}
		return years;
	}


	public void setYears(List years) {
		this.years = years;
	}

	public long getResId() {
		return resId;
	}

	public void setResId(long resId) {
		this.resId = resId;
	}

	public List getResponsibility() {
		responsibility = new ArrayList<>();
		
		for(Responsibility r : Responsibility.retrieve(" ORDER BY rss.rname ASC", new String[0])) {
			responsibility.add(new SelectItem(r.getId(), "("+r.getDepartment().getCode() +") "+ r.getName()));
		}
		
		return responsibility;
	}

	public void setResponsibility(List responsibility) {
		this.responsibility = responsibility;
	}
	/*
	 * public String getTextContent() { return textContent; } public void
	 * setTextContent(String textContent) { this.textContent = textContent; }
	 */
	
	public int getMondIdR() {
		if(mondIdR==0) {
			mondIdR = DateUtils.getCurrentMonth();
		}
		return mondIdR;
	}


	public void setMondIdR(int mondIdR) {
		this.mondIdR = mondIdR;
	}


	public List getMonthsR() {
		monthsR = new ArrayList<>();
		monthsR.add(new SelectItem(0, "All Months"));
		for(int month=1; month<=12; month++) {
			monthsR.add(new SelectItem(month, DateUtils.getMonthName(month)));
		}
		return monthsR;
	}


	public void setMonthsR(List monthsR) {
		this.monthsR = monthsR;
	}


	public int getYearIdR() {
		if(yearIdR==0) {
			yearIdR = DateUtils.getCurrentYear();
		}
		return yearIdR;
	}


	public void setYearIdR(int yearIdR) {
		this.yearIdR = yearIdR;
	}


	public List getYearsR() {
		yearsR = new ArrayList<>();
		for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
			yearsR.add(new SelectItem(year, year+""));
		}
		return yearsR;
	}


	public void setYearsR(List yearsR) {
		this.yearsR = yearsR;
	}
	public List<Reports> getRptsPer() {
		return rptsPer;
	}
	public List<VRData> getVrsPer() {
		return vrsPer;
	}
	public void setRptsPer(List<Reports> rptsPer) {
		this.rptsPer = rptsPer;
	}
	public void setVrsPer(List<VRData> vrsPer) {
		this.vrsPer = vrsPer;
	}
	
	public String getSeriesMonthIdSearchPer() {
		return seriesMonthIdSearchPer;
	}

	public void setSeriesMonthIdSearchPer(String seriesMonthIdSearchPer) {
		this.seriesMonthIdSearchPer = seriesMonthIdSearchPer;
	}

	public List getSeriesMonthsSearchPer() {
		
		seriesMonthsSearchPer = new ArrayList<>();
		for(int month=1; month<=DateUtils.getCurrentMonth(); month++) {
			
			String monthTrim = DateUtils.getMonthName(month).toUpperCase().substring(0, 3);
			
			String ser1 = "101-100-17-" + monthTrim + "-" + DateUtils.getCurrentYear();
			String ser2 = "101-200-18-" + monthTrim + "-" + DateUtils.getCurrentYear();
			String ser3 = "101-300-17-" + monthTrim + "-" + DateUtils.getCurrentYear();
			
			seriesMonthsSearchPer.add(new SelectItem(ser1, ser1));
			seriesMonthsSearchPer.add(new SelectItem(ser2, ser2));
			seriesMonthsSearchPer.add(new SelectItem(ser3, ser3));
			
		}
		
		return seriesMonthsSearchPer;
	}

	public void setSeriesMonthsSearchPer(List seriesMonthsSearchPer) {
		this.seriesMonthsSearchPer = seriesMonthsSearchPer;
	}
	
	

}