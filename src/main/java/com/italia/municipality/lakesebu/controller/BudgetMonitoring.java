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

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.BudgetType;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Mark Italia
 * @version 1.0
 * @since 06/07/2024
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class BudgetMonitoring {
	
	private long id;
	private String dateTrans;
	private double amount;
	private int type;
	private int cycle;
	private int isActive;
	private String remarks;
	private BankAccounts accounts;
	
	private String typeName;
	
	
	public static double yearBeginningBalance(String fundName, int year) {
		String sql = "SELECT b.amount as total FROM budgetmonitoring b, tbl_bankaccounts a WHERE a.bank_id=b.bankid AND b.isactiverbu=1 AND b.typebu=" + BudgetType.YEAR_BEGINNING_BALANCE.getId() ;
		sql += " AND year(b.datebu)=" + year;
		sql += " AND a.bank_account_name='"+ fundName +"'";
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		try {
			while(rs.next()) {
				return rs.getDouble("total");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static double transferFundAmount(int year, int month, int accountId) {
		double total=0d;
		
		String sql = "SELECT SUM(amount) as total FROM budgetmonitoring WHERE isactiverbu=1 AND year(datebu)=" + year  + " AND bankid=" + accountId ;
		
		if(month>0) {
			sql += " AND month(datebu)=" + month;
		}
		
		sql += " AND typebu= " + BudgetType.TRANSFER_TO_OTHER_FUND.getId();
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				return rs.getDouble("total");
			}
			rs.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return total;
	}
	
	/**
	 * 
	 * @param year - input specific year
	 * @param group by month
	 * @param accountId
	 * @return
	 */
	public static List<Reports> amountBudget(String fundName, int year, int month) {
		List<Reports> rpts = new ArrayList<Reports>();
		
		Map<Integer, Map<Integer, Double>> mapBudget = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> mapAmount = new LinkedHashMap<Integer, Double>();
		
		
		String sql = "SELECT month(b.datebu) as mnth,typebu, sum(b.amount) as total FROM budgetmonitoring b, tbl_bankaccounts a WHERE a.bank_id=b.bankid AND b.isactiverbu=1  AND year(b.datebu)=" + year + " AND a.bank_account_name='"+ fundName +"'";
		
		if(month>0) {
			sql += " AND month(b.datebu)=" + month;
		}
		
		sql += " AND b.typebu!= " + BudgetType.YEAR_BEGINNING_BALANCE.getId();
		
		sql += " GROUP BY a.bank_account_name, month(b.datebu),typebu";
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		//adding year beginning balance
		double yearBeginningBalance = BudgetMonitoring.yearBeginningBalance(fundName,year);
		try {
			while(rs.next()) {
				
				int mnt = rs.getInt("mnth");
				int typeid = rs.getInt("typebu");
				double amount = rs.getDouble("total");
				
				if(mapBudget!=null) {
					if(mapBudget.containsKey(mnt)) {
						if(mapBudget.get(mnt).containsKey(typeid)) {
							double amnt = mapBudget.get(mnt).get(typeid) + amount;
							mapBudget.get(mnt).put(typeid, amnt);
						}else {
							mapBudget.get(mnt).put(typeid, amount);
						}
					}else {
						mapAmount = new LinkedHashMap<Integer, Double>();
						mapAmount.put(typeid, amount);
						mapBudget.put(mnt, mapAmount);
					}
				}else {
					mapAmount.put(typeid, amount);
					mapBudget.put(mnt, mapAmount);
				}
				
			}
			
			double overallTotalNetNta = 0d, overallTotalNTA=0d, overallTotalLoan=0d, overDeposit = 0d, overlAll=0d, overallTransfer=0d;
			
			int fundId = 0;
			if("GENERAL FUND".equalsIgnoreCase(fundName)) {
				fundId = 1;
			}else if("MOTORPOOL".equalsIgnoreCase(fundName)) {
				fundId = 2;
			}else if("TRUST FUND".equalsIgnoreCase(fundName)) {
				fundId = 3;
			}
			
			Map<Integer, Double> mapDep = RCDDeposit.depositedCollection(year, fundId);
			
			for(int mnt : mapBudget.keySet()) {
				double nta = 0d, loan = 0d, dep = 0d, transfer = 0d;
				for(int type : mapBudget.get(mnt).keySet()) {
					if(type==BudgetType.NTA.getId()) {//nta
						nta += mapBudget.get(mnt).get(type);
					}else if(type==BudgetType.LOANS.getId()) {//loan
						loan += mapBudget.get(mnt).get(type);
					}else if(type==BudgetType.TRANSFER_TO_OTHER_FUND.getId()) {//loan
						transfer += mapBudget.get(mnt).get(type);
					}
				}
				double netNta = nta - (loan + transfer);
				
				if(mapDep!=null && mapDep.size()>0 && mapDep.containsKey(mnt)) {
					dep = mapDep.get(mnt);
				}
				double grandTotal = netNta + dep;
				
				if(mnt==1) {//if equals to January add to January the year beginning balance
					grandTotal += yearBeginningBalance;
				}
				
				Reports rpt = Reports.builder()
						.f1(DateUtils.getMonthName(mnt))
						.f2(Currency.formatAmount(nta))
						.f3(Currency.formatAmount(loan))
						.f4(Currency.formatAmount(transfer))
						.f5(Currency.formatAmount(netNta))
						.f6(Currency.formatAmount(dep))
						.f7(Currency.formatAmount(grandTotal))
						.build();
				rpts.add(rpt);
				overallTotalNTA += nta;
				overallTotalLoan += loan;
				overallTransfer += transfer;
				overallTotalNetNta += netNta;
				overDeposit += dep;
				overlAll += grandTotal;
			}
			
			Reports rpt = Reports.builder()
					.f1("Total")
					.f2(Currency.formatAmount(overallTotalNTA))
					.f3(Currency.formatAmount(overallTotalLoan))
					.f4(Currency.formatAmount(overallTransfer))
					.f5(Currency.formatAmount(overallTotalNetNta))
					.f6(Currency.formatAmount(overDeposit))
					.f7(Currency.formatAmount(overlAll))
					.build();
			rpts.add(rpt);
			
			rs.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return rpts;
	}
	
	/**
	 * 
	 * @param year - input specific year
	 * @param month - if 0 all month otherwise selected month
	 * @param accountId
	 * @return
	 */
	public static Map<String, Double> amountBudget(int year, int month) {
		Map<String, Double> mapData = new LinkedHashMap<String, Double>();
		
		Map<String, Map<Integer, Double>> mapBudget = new LinkedHashMap<String, Map<Integer, Double>>();
		Map<Integer, Double> mapAmount = new LinkedHashMap<Integer, Double>();
		
		String sql = "SELECT a.bank_account_name as account, sum(b.amount) as total,typebu FROM budgetmonitoring b, tbl_bankaccounts a WHERE a.bank_id=b.bankid AND b.isactiverbu=1  AND year(b.datebu)=" + year ;
		
		if(month>0) {
			sql += " AND month(b.datebu)=" + month;
		}
		
		sql += " AND b.typebu!= " + BudgetType.YEAR_BEGINNING_BALANCE.getId();
		
		sql += " GROUP BY a.bank_account_name,typebu";
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				String name = rs.getString("account");
				double amount = rs.getDouble("total");
				int typeid = rs.getInt("typebu");
				
				if(mapBudget!=null) {
					if(mapBudget.containsKey(name)) {
						if(mapBudget.get(name).containsKey(typeid)) {
							double amnt = mapBudget.get(name).get(typeid) + amount;
							mapBudget.get(name).put(typeid, amnt);
						}else {
							mapBudget.get(name).put(typeid, amount);
						}
					}else {
						mapAmount = new LinkedHashMap<Integer, Double>();
						mapAmount.put(typeid, amount);
						mapBudget.put(name, mapAmount);
					}
				}else {
					mapAmount.put(typeid, amount);
					mapBudget.put(name, mapAmount);
				}
				
			}
			
			for(String name : mapBudget.keySet()) {
				double nta = 0d, loan = 0d, otherfundtransfer = 0d;
				for(int type : mapBudget.get(name).keySet()) {
					if(type==BudgetType.NTA.getId()) {//nta
						nta += mapBudget.get(name).get(type);
					}else if(type==BudgetType.LOANS.getId()){//loan
						loan += mapBudget.get(name).get(type);
					}else if(type==BudgetType.TRANSFER_TO_OTHER_FUND.getId()){//loan
						otherfundtransfer += mapBudget.get(name).get(type);
					}
				}
				double netNta = nta - (loan + otherfundtransfer);
				mapData.put(name, netNta);
			}
			
			rs.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return mapData;
	}
	
	/**
	 * 
	 * @param year - input specific year
	 * @param month - if 0 all month otherwise selected month
	 * @param accountId
	 * @return
	 */
	public static double amountBudget(int year, int month, int accountId) {
		double total=0d;
		
		String sql = "SELECT SUM(amount) as total FROM budgetmonitoring WHERE isactiverbu=1 AND year(datebu)=" + year  + " AND bankid=" + accountId ;
		
		if(month>0) {
			sql += " AND month(datebu)=" + month;
		}
		
		sql += " AND typebu!= " + BudgetType.YEAR_BEGINNING_BALANCE.getId();
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				return rs.getDouble("total");
			}
			rs.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return total;
	}
	
	public static double amountBudgetOnly(int year, int month, int accountId) {
		double total=0d;
		
		String sql = "SELECT SUM(amount) as total FROM budgetmonitoring WHERE isactiverbu=1 AND year(datebu)=" + year  + " AND bankid=" + accountId ;
		
		if(month>0) {
			sql += " AND month(datebu)=" + month;
		}
		
		sql += " AND typebu= " + BudgetType.NTA.getId();
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				return rs.getDouble("total");
			}
			rs.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return total;
	}
	
	public static double loanAmount(int year, int month, int accountId) {
		double total=0d;
		
		String sql = "SELECT SUM(amount) as total FROM budgetmonitoring WHERE isactiverbu=1 AND year(datebu)=" + year  + " AND bankid=" + accountId ;
		
		if(month>0) {
			sql += " AND month(datebu)=" + month;
		}
		
		sql += " AND typebu= " + BudgetType.LOANS.getId();
		
		ResultSet rs = OpenTableAccess.query(sql, new String[0], new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				return rs.getDouble("total");
			}
			rs.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return total;
	}
	
	/**
	 * 
	 * @param year
	 * @param month if month 0 then retrieve year
	 * @param accountype if account type 0 all
	 * @return
	 */
	public static List<BudgetMonitoring> retrieve(int year, int month, int accountype){
		String sql = " AND year(mon.datebu)=" + year;
		if(month>0) {
			sql += " AND month(mon.datebu)=" + month;
		}
		if(accountype>0) {
			sql += " AND mon.bankid=" + accountype;
		}
		
		return retrieve(sql, new String[0]);
	}
	
	public static List<BudgetMonitoring> retrieve(String sql, String[] params){
		List<BudgetMonitoring> mons = new ArrayList<BudgetMonitoring>();
		String tableMon = "mon";
		String tableAcc = "acc";
		String tmpSql = "SELECT * FROM budgetmonitoring " + tableMon + ", tbl_bankaccounts " + tableAcc + " WHERE " + tableMon + ".isactiverbu=1 AND " + tableMon + ".bankid=" + tableAcc + ".bank_id ";
		
		System.out.println("Budget: " + tmpSql + sql);
		ResultSet rs = OpenTableAccess.query(tmpSql + sql, params, new BankChequeDatabaseConnect());
		
		try {
			while(rs.next()) {
				
				BankAccounts ac = BankAccounts.builder()
						.bankId(rs.getInt("bank_id"))
						.bankAccntName(rs.getString("bank_account_name"))
						.bankAccntNo(rs.getString("bank_account_no"))
						.bankAccntBranch(rs.getString("bank_branch"))
						.build();
				
				BudgetMonitoring mon = BudgetMonitoring.builder()
						.id(rs.getLong("buid"))
						.dateTrans(rs.getString("datebu"))
						.accounts(ac)
						.amount(rs.getDouble("amount"))
						.type(rs.getInt("typebu"))
						.remarks(rs.getString("remarks"))
						.cycle(rs.getInt("cyclebu"))
						.isActive(rs.getInt("isactiverbu"))
						.typeName(BudgetType.typeName(rs.getInt("typebu")))
						.build();
				
				mons.add(mon);
				
			}
			
			rs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mons;
	}
	
	public static BudgetMonitoring save(BudgetMonitoring st){
		if(st!=null){
			
			long id = BudgetMonitoring.getInfo(st.getId() ==0? BudgetMonitoring.getLatestId()+1 : st.getId());
			LogU.add("checking for new added data");
			if(id==1){
				LogU.add("insert new Data ");
				st = BudgetMonitoring.insertData(st, "1");
			}else if(id==2){
				LogU.add("update Data ");
				st = BudgetMonitoring.updateData(st);
			}else if(id==3){
				LogU.add("added new Data ");
				st = BudgetMonitoring.insertData(st, "3");
			}
			
		}
		return st;
	}
	
	public void save(){
		
		long id = getInfo(getId() ==0? getLatestId()+1 : getId());
		LogU.add("checking for new added data");
		if(id==1){
			LogU.add("insert new Data ");
			BudgetMonitoring.insertData(this, "1");
		}else if(id==2){
			LogU.add("update Data ");
			BudgetMonitoring.updateData(this);
		}else if(id==3){
			LogU.add("added new Data ");
			BudgetMonitoring.insertData(this, "3");
		}
		
	}
	
	public static BudgetMonitoring insertData(BudgetMonitoring st, String type){
		String sql = "INSERT INTO budgetmonitoring ("
				+ "buid,"
				+ "datebu,"
				+ "bankid,"
				+ "amount,"
				+ "typebu,"
				+ "remarks,"
				+ "cyclebu,"
				+ "isactiverbu)" 
				+ " VALUES(?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("inserting data into table budgetmonitoring");
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
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setInt(cnt++, st.getAccounts().getBankId());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getType());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getCycle());
		ps.setInt(cnt++, st.getIsActive());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getAccounts().getBankId());
		LogU.add(st.getAmount());
		LogU.add(st.getType());
		LogU.add(st.getRemarks());
		LogU.add(st.getCycle());
		LogU.add(st.getIsActive());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error inserting data to budgetmonitoring : " + s.getMessage());
		}
		LogU.add("===========================END=========================");
		return st;
	}
	
	public static BudgetMonitoring updateData(BudgetMonitoring st){
		String sql = "UPDATE budgetmonitoring SET "
				+ "datebu=?,"
				+ "bankid=?,"
				+ "amount=?,"
				+ "typebu=?,"
				+ "remarks=?,"
				+ "cyclebu=?" 
				+ " WHERE buid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt = 1;
		LogU.add("===========================START=========================");
		LogU.add("updating data into table budgetmonitoring");
		
		ps.setString(cnt++, st.getDateTrans());
		ps.setInt(cnt++, st.getAccounts().getBankId());
		ps.setDouble(cnt++, st.getAmount());
		ps.setInt(cnt++, st.getType());
		ps.setString(cnt++, st.getRemarks());
		ps.setInt(cnt++, st.getCycle());
		ps.setLong(cnt++, st.getId());
		
		LogU.add(st.getDateTrans());
		LogU.add(st.getAccounts().getBankId());
		LogU.add(st.getAmount());
		LogU.add(st.getType());
		LogU.add(st.getRemarks());
		LogU.add(st.getCycle());
		LogU.add(st.getId());
		
		LogU.add("executing for saving...");
		ps.execute();
		LogU.add("closing...");
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		LogU.add("data has been successfully saved...");
		}catch(SQLException s){
			LogU.add("error updating data to budgetmonitoring : " + s.getMessage());
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
		sql="SELECT buid FROM budgetmonitoring  ORDER BY buid DESC LIMIT 1";	
		conn = BankChequeDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getInt("buid");
		}
		
		rs.close();
		prep.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return id;
	}
	
	public static long getInfo(long id){
		boolean isNotNull=false;
		int result =0;
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
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT buid FROM budgetmonitoring WHERE buid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
	public void delete(){
		
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "UPDATE budgetmonitoring set isactiverbu=0 WHERE buid=?";
		
		String[] params = new String[1];
		params[0] = getId()+"";
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){}
		
	}
	
}
