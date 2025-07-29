package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.ClassType;
import com.italia.municipality.lakesebu.enm.TenantStatus;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @since 05/27/2025
 * @version 1.0
 * @author Mark Italia
 * 
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class TenantBilling {

	private long id;
	private String date;
	private String billNo;
	private double billAmount;
	private double interest;
	private double surcharge;
	private int isPaid;
	private String officialRefNumber;
	private double amountPaid;
	private int isActive;
	private String remarks;
	private int year;
	private int month;
	private double total;
	private double interestRate;
	private double unpaidPrincipal;
	
	private Tenant tenant;
	private TenantContract contract;
	
	private Date dateTrans;
	
	
	public static List<Tenant> getTenantBillLatest(int year, int month, String name){
		System.out.println("getTenantBill....");
		List<Tenant> tns = new ArrayList<Tenant>();
		String tblTenant = "ten";
		String tblCon = "con";
		String tblBill = "bill";
		String sql = "SELECT sum(bill.total) as billAmnt,ten.tenantno, ten.tid,ten.tdate,ten.fullnamet,ten.contractnot,ten.addresst,ten.isactivet FROM tenantbilling "+ tblBill +", tenantcontract "+ tblCon +", tenant "+ tblTenant +" WHERE "+ tblBill +".isactiveb=1 AND " +
		tblBill + ".tid=" + tblTenant + ".tid AND " +
				tblBill + ".cid=" + tblCon + ".cid AND " + tblBill + ".ispaid=0 ";
		
		sql += " AND bill.yer=" + year + " AND bill.mnt=" + month;
		if(name!=null && !name.isEmpty()) {
			sql += " AND ten.fullnamet like '%"+ name +"%'";
		}
		sql += " GROUP BY ten.tid";
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
			conn = WebTISDatabaseConnect.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			System.out.println("Query tenant billing: " + sql);
			
			while(rs.next()){
				Tenant tn = Tenant.builder()
						.id(rs.getLong("tid"))
						.tenantNo(rs.getString("tenantno"))
						.date(rs.getString("tdate"))
						.fullName(rs.getString("fullnamet"))
						.contactNumber(rs.getString("contractnot"))
						.address(rs.getString("addresst"))
						.isActive(rs.getInt("isactivet"))
						.totalBill(Currency.formatAmount(rs.getDouble("billAmnt")))
						.build();
				tns.add(tn);
			}
			
			rs.close();
			ps.close();
			WebTISDatabaseConnect.close(conn);
	
		}catch(Exception e){e.getMessage();}
		System.out.println("getTenantBill.... sql: " + ps.toString());
		return tns;
	}
	/**
	 * 
	 * Check first if bill already created
	 */
	public static boolean createBill(int year, int month) {
		int currentYear = DateUtils.getCurrentYear();
		int currentMonth = DateUtils.getCurrentMonth();
		int currentDay = DateUtils.getCurrentDay();
		
		boolean isOk = true;
		//Contract Id, Blling
		Map<Long, TenantBilling> billed = new LinkedHashMap<Long, TenantBilling>();
		String sql = "SELECT * FROM tenantbilling WHERE isactiveb=1 AND yer=" + year + " AND mnt=" + month;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		System.out.println("Query tenant billing: " + sql);
		while(rs.next()){
			TenantBilling bill = builder()
					.id(rs.getLong("bid"))
					.date(rs.getString("bdate"))
					.billNo(rs.getString("billno"))
					.billAmount(rs.getDouble("billamnt"))
					.surcharge(rs.getDouble("surcharge"))
					.interest(rs.getDouble("interest"))
					.isPaid(rs.getInt("ispaid"))
					.officialRefNumber(rs.getString("orreferenceno"))
					.amountPaid(rs.getDouble("amountpaid"))
					.isActive(rs.getInt("isactiveb"))
					.remarks(rs.getString("remarksb"))
					.tenant(Tenant.builder().id(rs.getLong("tid")).build())
					.contract(TenantContract.builder().id(rs.getLong("cid")).build())
					.year(rs.getInt("yer"))
					.month(rs.getInt("mnt"))
					.total(rs.getDouble("total"))
					.interestRate(rs.getDouble("interestrate"))
					.unpaidPrincipal(rs.getDouble("unpaidprincipal"))
					.build();
			
			billed.put(bill.getContract().getId(), bill);
			
		}
		
		 rs = null;
		 ps = null;
		//retrieve all contract to create bill
		 sql = "SELECT * FROM tenantcontract WHERE isactivec=1 AND statusc=" + TenantStatus.ACTIVE.getId();
		Map<Long, TenantContract> contractData = new LinkedHashMap<Long, TenantContract>();
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		System.out.println("Query contract: " + sql);
		while(rs.next()){
			TenantContract con = TenantContract.builder()
					.id(rs.getLong("cid"))
					.accountNo(rs.getString("accountno"))
					.date(rs.getString("cdate"))
					.contractStart(rs.getString("contractstart"))
					.contractEnd(rs.getString("contractend"))
					.amount(rs.getDouble("amountc"))
					.locationNumber(rs.getString("locationno"))
					.classType(rs.getInt("classtype"))
					.classTypeName(ClassType.val(rs.getInt("classtype")).getName())
					.status(rs.getInt("statusc"))
					.statusName(TenantStatus.val(rs.getInt("statusc")).getName())
					.isActive(rs.getInt("isactivec"))
					.tenant(Tenant.builder().id(rs.getLong("tid")).build())
					.build();
			
			contractData.put(con.getId(), con);
		}
		
		//removing already create billing
		if(billed!=null && billed.size()>0) {
			for(long contractId : billed.keySet()) {
				contractData.remove(contractId);
			}
		}
		
		//Check recent Month
		Map<Long, TenantBilling> unpaidBillLastMonth = new LinkedHashMap<Long, TenantBilling>();
		//if(currentYear==year) {
			month = month - 1;
			if(month<0) {
				month = 12;
				year -= 1; 
			}
		//}
		rs = null;
		ps = null;	
		sql = "SELECT * FROM tenantbilling WHERE isactiveb=1 AND yer=" + year + " AND mnt=" + month + " AND ispaid=0";
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		System.out.println("Query previous bill: " + sql);
		while(rs.next()){
			TenantBilling bill = builder()
					.id(rs.getLong("bid"))
					.date(rs.getString("bdate"))
					.billNo(rs.getString("billno"))
					.billAmount(rs.getDouble("billamnt"))
					.surcharge(rs.getDouble("surcharge"))
					.interest(rs.getDouble("interest"))
					.isPaid(rs.getInt("ispaid"))
					.officialRefNumber(rs.getString("orreferenceno"))
					.amountPaid(rs.getDouble("amountpaid"))
					.isActive(rs.getInt("isactiveb"))
					.remarks(rs.getString("remarksb"))
					.tenant(Tenant.builder().id(rs.getLong("tid")).build())
					.contract(TenantContract.builder().id(rs.getLong("cid")).build())
					.year(rs.getInt("yer"))
					.month(rs.getInt("mnt"))
					.total(rs.getDouble("total"))
					.interestRate(rs.getDouble("interestrate"))
					.unpaidPrincipal(rs.getDouble("unpaidprincipal"))
					.build();
			
			unpaidBillLastMonth.put(bill.getContract().getId(), bill);
		}
		
		//Check last last Month
				Map<Long, TenantBilling> unpaidBeforeLastMonth = new LinkedHashMap<Long, TenantBilling>();
				//if(currentYear==year) {
					month = month - 1;
					if(month<0) {
						month = 12;
						year -= 1; 
					}
				//}
				rs = null;
				ps = null;	
				sql = "SELECT * FROM tenantbilling WHERE isactiveb=1 AND yer=" + year + " AND mnt=" + month + " AND ispaid=0";
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				System.out.println("Query previous bill: " + sql);
				while(rs.next()){
					TenantBilling bill = builder()
							.id(rs.getLong("bid"))
							.date(rs.getString("bdate"))
							.billNo(rs.getString("billno"))
							.billAmount(rs.getDouble("billamnt"))
							.surcharge(rs.getDouble("surcharge"))
							.interest(rs.getDouble("interest"))
							.isPaid(rs.getInt("ispaid"))
							.officialRefNumber(rs.getString("orreferenceno"))
							.amountPaid(rs.getDouble("amountpaid"))
							.isActive(rs.getInt("isactiveb"))
							.remarks(rs.getString("remarksb"))
							.tenant(Tenant.builder().id(rs.getLong("tid")).build())
							.contract(TenantContract.builder().id(rs.getLong("cid")).build())
							.year(rs.getInt("yer"))
							.month(rs.getInt("mnt"))
							.total(rs.getDouble("total"))
							.interestRate(rs.getDouble("interestrate"))
							.unpaidPrincipal(rs.getDouble("unpaidprincipal"))
							.build();
					
					unpaidBeforeLastMonth.put(bill.getContract().getId(), bill);
				}
		
		//create billing
		for(long contractId : contractData.keySet()) {
			if(unpaidBillLastMonth!=null && unpaidBillLastMonth.containsKey(contractId)) {
				double principal = contractData.get(contractId).getAmount();  //unpaidBillLastMonth.get(contractId).getBillAmount();
				double previnterestRate = unpaidBillLastMonth.get(contractId).getInterestRate();
				double surcharge = unpaidBillLastMonth.get(contractId).getSurcharge();
				//double oldTotal = unpaidBillLastMonth.get(contractId).getTotal();
				double unpaid = unpaidBillLastMonth.get(contractId).getUnpaidPrincipal();
				//double prepInterest = unpaidBillLastMonth.get(contractId).getInterest();
				double newSurcharge = 0;
				double total = 0;
				double newInterest = 0;
				double newInterestRate = 0;
				double newUnpaid = 0d;
				//double baseInterest = (principal + (principal * 0.25)) * 0.02;
				
				
				//adding surcharge and interest if above 20 of the day
				if(currentDay>20) {
					newSurcharge =   (principal + unpaid) * 0.25 ;//+ surcharge;
					
					newInterestRate = previnterestRate + 0.02;
					
					//check if recent month penalty rate is same with before recent month if so increase penalty rate adding 0.02
					if(unpaidBeforeLastMonth!=null) {
						if(unpaidBeforeLastMonth.containsKey(contractId) && unpaidBillLastMonth.containsKey(contractId)) {
							if(previnterestRate==0) {
								double beforeLastMonthBill = unpaidBeforeLastMonth.get(contractId).billAmount;
								newInterestRate = 0.04;
								newSurcharge =   (principal + unpaid + beforeLastMonthBill) * 0.25 ;
								newInterest = (principal +  newSurcharge) * newInterestRate; //0.02;
								total = unpaid + principal + beforeLastMonthBill + newSurcharge + newInterest;
								newUnpaid = unpaid + principal + beforeLastMonthBill;
							}
						}else {
							newInterest = (principal +  newSurcharge) * newInterestRate; //0.02;
							total = unpaid + principal + newSurcharge + newInterest;
							newUnpaid = unpaid + principal;
						}
					}else {
					
					newInterest = (principal +  newSurcharge) * newInterestRate; //0.02;
					total = unpaid + principal + newSurcharge + newInterest;
					newUnpaid = unpaid + principal;
					
					}
				}else {
					
					newInterestRate = previnterestRate;
					
					//check if recent month penalty rate is same with before recent month if so increase penalty rate adding 0.02
					if(unpaidBeforeLastMonth!=null) {
						if(unpaidBeforeLastMonth.containsKey(contractId) && unpaidBillLastMonth.containsKey(contractId)) {
							/*if(unpaidBeforeLastMonth.get(contractId).getInterestRate()==unpaidBillLastMonth.get(contractId).getInterestRate()) {
								newInterestRate = previnterestRate + 0.02;
							}else {
								
							}*/
							newInterestRate = previnterestRate + 0.02;
						}
					}
					
					if(unpaid==0) {
						unpaid = principal;
						newUnpaid = principal;
					}else {
						unpaid += principal;
						newUnpaid = unpaid;
					}
					newSurcharge = unpaid * 0.25;
					newInterest = (unpaid + newSurcharge) * newInterestRate;
					
					total = unpaid + principal + newSurcharge + newInterest;
					
					
				}
				
				//double total = oldTotal + principal + newSurcharge + newInterest;
				
				
				TenantBilling
				.builder()
				.id(0)
				.date(DateUtils.getCurrentDateYYYYMMDD())
				.billNo(TenantBilling.getNewBillNumber())
				.billAmount(principal)
				.surcharge(newSurcharge)
				.interest(newInterest)
				.interestRate(newInterestRate)
				.isPaid(0)
				.amountPaid(0)
				.remarks("Auto bill creation")
				.isActive(1)
				.month(currentMonth)
				.year(currentYear)
				.tenant(unpaidBillLastMonth.get(contractId).getTenant())
				.contract(unpaidBillLastMonth.get(contractId).getContract())
				.total(total)
				.unpaidPrincipal(newUnpaid)
				.build()
				.save();
			}else {
				double principal = contractData.get(contractId).getAmount(); 
				double newInterest = 0;
				
				if(currentDay>20) {
					newInterest = principal  * 0.02;
				}
				
				
				TenantBilling
				.builder()
				.id(0)
				.date(DateUtils.getCurrentDateYYYYMMDD())
				.billNo(TenantBilling.getNewBillNumber())
				.billAmount(contractData.get(contractId).getAmount())
				.surcharge(0)
				.interest(newInterest)
				.isPaid(0)
				.amountPaid(0)
				.remarks("Auto bill creation")
				.isActive(1)
				.month(currentMonth)
				.year(currentYear)
				.tenant(contractData.get(contractId).getTenant())
				.contract(contractData.get(contractId))
				.total(contractData.get(contractId).getAmount())
				.interestRate(0)
				.unpaidPrincipal(0)
				.build()
				.save();
			}
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		isOk=true;
		}catch(Exception e){e.getMessage(); isOk=false;}
		
		
		return isOk;
	}
	
	public static String getNewBillNumber() {
		String series = null;
		String date = DateUtils.getCurrentDateYYYYMMDD();
		String yearNow = date.split("-")[0];
		String yearLast = "";
		int number = 0;
		int size = 0;
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT billno FROM tenantbilling ORDER BY bid DESC LIMIT 1");
		rs = ps.executeQuery();
		
		while(rs.next()){
			String[] val = rs.getString("billno").split("-");
 			yearLast = val[0];
 			number = Integer.valueOf(val[1]);
		}
		
		number += 1;
		String len = number + "";
		size = len.length();
		
		if(yearNow.equalsIgnoreCase(yearLast)) {
			switch(size) {
			case 1 : series = yearLast + "-000000000" + number; break;
			case 2 : series = yearLast + "-00000000" + number; break;
			case 3 : series = yearLast + "-0000000" + number; break;
			case 4 : series = yearLast + "-000000" + number; break;
			case 5 : series = yearLast + "-00000" + number; break;
			case 6 : series = yearLast + "-0000" + number; break;
			case 7 : series = yearLast + "-000" + number; break;
			case 8 : series = yearLast + "-00" + number; break;
			case 9 : series = yearLast + "-0" + number; break;
			case 10 : series = yearLast + "-" + number; break;
			}
			
			
		}else {
			series = null; //reset series
		}
		
		
		if(series==null) {
			series = yearNow + "-0000000001";
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return series;
	} 
	
	
	public static List<TenantBilling> retrieve(String sql, String[] params){
		List<TenantBilling> tns = new ArrayList<TenantBilling>();
		
		String tblTenant = "ten";
		String tblCon = "con";
		String tblBill = "bill";
		String sqlTemp = "SELECT * FROM tenantbilling "+ tblBill +", tenantcontract "+ tblCon +", tenant "+ tblTenant +" WHERE "+ tblBill +".isactiveb=1 AND " +
		tblBill + ".tid=" + tblTenant + ".tid AND " +
				tblBill + ".cid=" + tblCon + ".cid ";
		
		sql = sqlTemp + sql;
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
		
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		System.out.println("tenant billing SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			Tenant tn = Tenant.builder()
					.id(rs.getLong("tid"))
					.tenantNo(rs.getString("tenantno"))
					.date(rs.getString("tdate"))
					.fullName(rs.getString("fullnamet"))
					.contactNumber(rs.getString("contractnot"))
					.address(rs.getString("addresst"))
					.isActive(rs.getInt("isactivet"))
					.build();
			
			TenantContract con = TenantContract.builder()
					.id(rs.getLong("cid"))
					.accountNo(rs.getString("accountno"))
					.date(rs.getString("cdate"))
					.contractStart(rs.getString("contractstart"))
					.contractEnd(rs.getString("contractend"))
					.amount(rs.getDouble("amountc"))
					.locationNumber(rs.getString("locationno"))
					.classType(rs.getInt("classtype"))
					.classTypeName(ClassType.val(rs.getInt("classtype")).getName())
					.status(rs.getInt("statusc"))
					.isActive(rs.getInt("isactivec"))
					.tenant(tn)
					.build();
			
			TenantBilling bill = builder()
					.id(rs.getLong("bid"))
					.date(rs.getString("bdate"))
					.billNo(rs.getString("billno"))
					.billAmount(rs.getDouble("billamnt"))
					.surcharge(rs.getDouble("surcharge"))
					.interest(rs.getDouble("interest"))
					.isPaid(rs.getInt("ispaid"))
					.officialRefNumber(rs.getString("orreferenceno"))
					.amountPaid(rs.getDouble("amountpaid"))
					.isActive(rs.getInt("isactiveb"))
					.remarks(rs.getString("remarksb"))
					.tenant(tn)
					.contract(con)
					.year(rs.getInt("yer"))
					.month(rs.getInt("mnt"))
					.total(rs.getDouble("total"))
					.interestRate(rs.getDouble("interestrate"))
					.unpaidPrincipal(rs.getDouble("unpaidprincipal"))
					.build();
			
			
			tns.add(bill);
			
		}
		
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		
		}catch(Exception e){e.getMessage();}
		
		return tns;
	}
	
	public static TenantBilling save(TenantBilling st){
		if(st!=null){
			
			long id = TenantBilling.getInfo(st.getId() ==0? TenantBilling.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = TenantBilling.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = TenantBilling.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = TenantBilling.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save() {
		TenantBilling.save(this);
	}
	
	public static TenantBilling insertData(TenantBilling st, String type){
		String sql = "INSERT INTO tenantbilling ("
				+ "bid,"
				+ "bdate,"
				+ "billno,"
				+ "billamnt,"
				+ "surcharge,"
				+ "interest,"
				+ "ispaid,"
				+ "orreferenceno,"
				+ "amountpaid,"
				+ "isactiveb,"
				+ "remarksb,"
				+ "tid,"
				+ "cid,"
				+ "yer,"
				+ "mnt,"
				+ "total,"
				+ "interestrate,"
				+ "unpaidprincipal)" 
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table tenantbilling");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			st.setId(id);
			LogU.add("id: 1");
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			st.setId(id);
			LogU.add("id: " + id);
		}
		
		ps.setString(cnt++, st.getDate());
		ps.setString(cnt++, st.getBillNo());
		ps.setDouble(cnt++, st.getBillAmount());
		ps.setDouble(cnt++, st.getSurcharge());
		ps.setDouble(cnt++, st.getInterest());
		ps.setInt(cnt++, st.getIsPaid());
		ps.setString(cnt++, st.getOfficialRefNumber());
		ps.setDouble(cnt++, st.getAmountPaid());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getRemarks());
		ps.setLong(cnt++, st.getTenant().getId());
		ps.setLong(cnt++, st.getContract().getId());
		ps.setInt(cnt++, st.getYear());
		ps.setInt(cnt++, st.getMonth());
		ps.setDouble(cnt++, st.getTotal());
		ps.setDouble(cnt++, st.getInterestRate());;
		ps.setDouble(cnt++, st.getUnpaidPrincipal());
		
		LogU.add(st.getDate());
		LogU.add(st.getBillNo());
		LogU.add(st.getBillAmount());
		LogU.add(st.getSurcharge());
		LogU.add(st.getInterest());
		LogU.add(st.getIsPaid());
		LogU.add(st.getOfficialRefNumber());
		LogU.add(st.getAmountPaid());
		LogU.add(st.getIsActive());
		LogU.add(st.getRemarks());
		LogU.add(st.getTenant().getId());
		LogU.add(st.getContract().getId());
		LogU.add(st.getYear());
		LogU.add(st.getMonth());
		LogU.add(st.getTotal());
		LogU.add(st.getInterestRate());;
		LogU.add(st.getUnpaidPrincipal());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to tenantbilling : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static TenantBilling updateData(TenantBilling st){
		String sql = "UPDATE tenantbilling SET "
				+ "bdate=?,"
				+ "billno=?,"
				+ "billamnt=?,"
				+ "surcharge=?,"
				+ "interest=?,"
				+ "ispaid=?,"
				+ "orreferenceno=?,"
				+ "amountpaid=?,"
				+ "isactiveb=?,"
				+ "remarksb=?,"
				+ "tid=?,"
				+ "cid=?,"
				+ "yer=?,"
				+ "mnt=?,"
				+ "total=?,"
				+ "interestrate=?,"
				+ "unpaidprincipal=?" 
				+ " WHERE bid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table tenantbilling");
		
		ps.setString(cnt++, st.getDate());
		ps.setString(cnt++, st.getBillNo());
		ps.setDouble(cnt++, st.getBillAmount());
		ps.setDouble(cnt++, st.getSurcharge());
		ps.setDouble(cnt++, st.getInterest());
		ps.setInt(cnt++, st.getIsPaid());
		ps.setString(cnt++, st.getOfficialRefNumber());
		ps.setDouble(cnt++, st.getAmountPaid());
		ps.setInt(cnt++, st.getIsActive());
		ps.setString(cnt++, st.getRemarks());
		ps.setLong(cnt++, st.getTenant().getId());
		ps.setLong(cnt++, st.getContract().getId());
		ps.setInt(cnt++, st.getYear());
		ps.setInt(cnt++, st.getMonth());
		ps.setDouble(cnt++, st.getTotal());
		ps.setDouble(cnt++, st.getInterestRate());;
		ps.setDouble(cnt++, st.getUnpaidPrincipal());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDate());
		LogU.add(st.getBillNo());
		LogU.add(st.getBillAmount());
		LogU.add(st.getSurcharge());
		LogU.add(st.getInterest());
		LogU.add(st.getIsPaid());
		LogU.add(st.getOfficialRefNumber());
		LogU.add(st.getAmountPaid());
		LogU.add(st.getIsActive());
		LogU.add(st.getRemarks());
		LogU.add(st.getTenant().getId());
		LogU.add(st.getContract().getId());
		LogU.add(st.getYear());
		LogU.add(st.getMonth());
		LogU.add(st.getTotal());
		LogU.add(st.getInterestRate());
		LogU.add(st.getUnpaidPrincipal());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		WebTISDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to tenantbilling : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT bid FROM tenantbilling ORDER BY bid DESC LIMIT 1";	
		conn = WebTISDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("bid");
		}
		
		rs.close();
		prep.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static long getInfo(long id){
		boolean isNotNull=false;
		long result =0;
		//id no data retrieve.
		//application will insert a default no which 1 for the first data
		long val = getLatestId();	
		if(val==0){
			isNotNull=true;
			result= 1; // add first data
			System.out.println("First data");
		}
		
		//check if empId is existing in table
		if(!isNotNull){
			isNotNull = isIdNoExist(id);
			if(isNotNull){
				result = 2; // update existing data
				System.out.println("update data");
			}else{
				result = 3; // add new data
				System.out.println("add new data");
			}
		}
		
		
		return result;
	}
	
	public static boolean isIdNoExist(long id){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT bid FROM tenantbilling WHERE bid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		System.out.println("Delete SQL : " + ps.toString());
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public  void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE tenantbilling set isactiveb=0 WHERE bid=?";
		
		String[] params = new String[1];
		params[0] = getId()+"";
		try{
		conn = WebTISDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
}
