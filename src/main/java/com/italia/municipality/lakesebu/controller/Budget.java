package com.italia.municipality.lakesebu.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

public class Budget implements IBudget{

	private long id;
	private String budgetDate;
	private BigDecimal amount;
	private double limitAmount;
	private String processBy;
	private int isActive;
	private IBudgetType budgetType;
	private BankAccounts accounts;
	private int cycleDate;
	
	
	private String remainingAmount;
	private String usedAmount;
	private String budgetAmount;
	private BigDecimal addedAmount;
	private boolean isActivated;
	
	private String addedAmountTmp;
	private String limitAmountTmp;
	
	public Budget(){}
	
	public Budget(
			long id,
			String budgetDate,
			BigDecimal amount,
			double limitAmount,
			String processBy,
			int isActive,
			IBudgetType budgetType,
			BankAccounts accounts,
			boolean isActivated,
			int cycleDate
			){
		this.id = id;
		this.budgetDate = budgetDate;
		this.amount = amount;
		this.limitAmount = limitAmount;
		this.processBy = processBy;
		this.isActive = isActive;
		this.budgetType = budgetType;
		this.accounts = accounts;
		this.isActivated = isActivated;
		this.cycleDate = cycleDate;
	}
	
	public static List<IBudget> retrieve(String sql, String[] params){
		List<IBudget> types =  new ArrayList<IBudget>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		System.out.println("SQL : " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			IBudget bud = new Budget();
			try{bud.setId(rs.getInt("budid"));}catch(NullPointerException e){}
			try{bud.setBudgetDate(rs.getString("buddate"));}catch(NullPointerException e){}
			try{bud.setAmount(rs.getBigDecimal("budamount"));}catch(NullPointerException e){}
			try{bud.setProcessBy(rs.getString("budproc"));}catch(NullPointerException e){}
			try{bud.setIsActive(rs.getInt("budisactive"));}catch(NullPointerException e){}
			try{bud.setLimitAmount(rs.getDouble("limitamount"));}catch(NullPointerException e){}
			try{bud.setLimitAmountTmp(Currency.formatAmount(bud.getLimitAmount()));}catch(NullPointerException e){bud.setLimitAmountTmp("0.00");}
			try{bud.setIsActivated(rs.getInt("limitisactivated")==1? true : false);}catch(NullPointerException e){}
			try{bud.setCycleDate(rs.getInt("cycledate"));}catch(NullPointerException e){}
			
			
			IBudgetType type = new BudgetType();
			try{type.setId(rs.getInt("budtypeid"));}catch(NullPointerException e){}
			try{type.setName(rs.getString("typename"));}catch(NullPointerException e){}
			try{type.setIsActive(rs.getInt("typeisactive"));}catch(NullPointerException e){}
			try{bud.setBudgetType(type);}catch(NullPointerException e){}
			
			BankAccounts ac = new BankAccounts();
			try{ac.setBankId(rs.getInt("bank_id"));}catch(NullPointerException e){}
			try{ac.setBankAccntName(rs.getString("bank_account_name"));}catch(NullPointerException e){}
			try{ac.setBankAccntNo(rs.getString("bank_account_no"));}catch(NullPointerException e){}
			try{ac.setBankAccntBranch(rs.getString("bank_branch"));}catch(NullPointerException e){}
			bud.setAccounts(ac);
			
			types.add(bud);
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return types;
	}
	
	public static List<IBudget> retrieveBudget(String sql, String[] params){
		List<IBudget> types =  new ArrayList<IBudget>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		System.out.println("SQL : " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			
			IBudget bud = new Budget();
			try{bud.setId(rs.getInt("budid"));}catch(NullPointerException e){}
			try{bud.setBudgetDate(rs.getString("buddate"));}catch(NullPointerException e){}
			try{bud.setAmount(rs.getBigDecimal("budamount"));}catch(NullPointerException e){}
			try{bud.setProcessBy(rs.getString("budproc"));}catch(NullPointerException e){}
			try{bud.setIsActive(rs.getInt("budisactive"));}catch(NullPointerException e){}
			try{bud.setLimitAmount(rs.getDouble("limitamount"));}catch(NullPointerException e){}
			try{bud.setLimitAmountTmp(Currency.formatAmount(bud.getLimitAmount()));}catch(NullPointerException e){bud.setLimitAmountTmp("0.00");}
			try{bud.setIsActivated(rs.getInt("limitisactivated")==1? true : false);}catch(NullPointerException e){}
			try{bud.setCycleDate(rs.getInt("cycledate"));}catch(NullPointerException e){}
			
			IBudgetType type = new BudgetType();
			try{type.setId(rs.getInt("budtypeid"));}catch(NullPointerException e){}
			try{bud.setBudgetType(type);}catch(NullPointerException e){}
			
			BankAccounts ac = new BankAccounts();
			try{ac.setBankId(rs.getInt("bank_id"));}catch(NullPointerException e){}
			bud.setAccounts(ac);
			
			types.add(bud);
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return types;
	}
	
	
	public static IBudget save(IBudget bud){
		if(bud!=null){
			long id = getInfo(bud.getId()==0? getLatestId()+1 : bud.getId());
			if(id==1){
				bud = Budget.insertData(bud, "1");
			}else if(id==2){
				bud = Budget.updateData(bud);
			}else if(id==3){
				bud = Budget.insertData(bud, "3");
			}
			
		}
		return bud;
	}
	@Override
	public void save(){
		long id = getInfo(getId()==0? getLatestId()+1 : getId());
		if(id==1){
			insertData("1");
		}else if(id==2){
			updateData();
		}else if(id==3){
			insertData("3");
		}
	}
	
	public static IBudget insertData(IBudget bud, String type){
		String sql = "INSERT INTO budget ("
				+ "budid,"
				+ "buddate,"
				+ "budamount,"
				+ "budproc,"
				+ "budisactive,"
				+ "budtypeid,"
				+ "bank_id,"
				+ "limitamount,"
				+ "limitisactivated,"
				+ "cycledate) " 
				+ "values(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			bud.setId(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			bud.setId(id);
		}
		ps.setString(2, bud.getBudgetDate());
		ps.setBigDecimal(3, bud.getAmount());
		ps.setString(4, bud.getProcessBy());
		ps.setInt(5, bud.getIsActive());
		ps.setInt(6, bud.getBudgetType()==null? 0 : bud.getBudgetType().getId());
		ps.setInt(7, bud.getAccounts()==null? 0 : bud.getAccounts().getBankId());
		ps.setDouble(8, bud.getLimitAmount());
		ps.setInt(9, bud.getIsActivated()==true? 1 : 0);
		ps.setInt(10, bud.getCycleDate());
		
		
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		return bud;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO budget ("
				+ "budid,"
				+ "buddate,"
				+ "budamount,"
				+ "budproc,"
				+ "budisactive,"
				+ "budtypeid,"
				+ "bank_id,"
				+ "limitamount,"
				+ "limitisactivated,"
				+ "cycledate) " 
				+ "values(?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			setId(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			setId(id);
		}
		ps.setString(2, getBudgetDate());
		ps.setBigDecimal(3, getAmount());
		ps.setString(4, getProcessBy());
		ps.setInt(5, getIsActive());
		ps.setInt(6, getBudgetType()==null? 0 : getBudgetType().getId());
		ps.setInt(7, getAccounts()==null? 0 : getAccounts().getBankId());
		ps.setDouble(8, getLimitAmount());
		ps.setInt(9, getIsActivated()==true? 1 : 0);
		ps.setInt(10, getCycleDate());
		
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
	}
	
	public static IBudget updateData(IBudget bud){
		String sql = "UPDATE budget SET "
				+ "buddate=?,"
				+ "budamount=?,"
				+ "budproc=?,"
				+ "budtypeid=?,"
				+ "bank_id=?,"
				+ "limitamount=?,"
				+ "limitisactivated=?,"
				+ "cycledate=? " 
				+ " WHERE budid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		ps.setString(1, bud.getBudgetDate());
		ps.setBigDecimal(2, bud.getAmount());
		ps.setString(3, bud.getProcessBy());
		ps.setInt(4, bud.getBudgetType()==null? 0 : bud.getBudgetType().getId());
		ps.setInt(5, bud.getAccounts()==null? 0 : bud.getAccounts().getBankId());
		ps.setDouble(6, bud.getLimitAmount());
		ps.setInt(7, bud.getIsActivated()==true? 1 : 0);
		ps.setInt(8, bud.getCycleDate());
		ps.setLong(9, bud.getId());
		
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		return bud;
	}
	
	public void updateData(){
		String sql = "UPDATE budget SET "
				+ "buddate=?,"
				+ "budamount=?,"
				+ "budproc=?,"
				+ "budtypeid=?,"
				+ "bank_id=?,"
				+ "limitamount=?,"
				+ "limitisactivated=?,"
				+ "cycledate=? " 
				+ " WHERE budid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		ps.setString(1, getBudgetDate());
		ps.setBigDecimal(2, getAmount());
		ps.setString(3, getProcessBy());
		ps.setInt(4, getBudgetType()==null? 0 : getBudgetType().getId());
		ps.setInt(5, getAccounts()==null? 0 : getAccounts().getBankId());
		ps.setDouble(6, getLimitAmount());
		ps.setInt(7, getIsActivated()==true? 1 : 0);
		ps.setInt(8, getCycleDate());
		ps.setLong(9, getId());
		
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT budid FROM budget  ORDER BY budid DESC LIMIT 1";	
		conn = BankChequeDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("budid");
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
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT budid FROM budget WHERE budid=?");
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
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
	
	public void delete(){
		String sql = "update budget set budisactive=0 WHERE budid=?";
		String params[] = new String[1];
		params[0] = getId()+"";
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
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
	
	
	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return id;
	}
	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub
		this.id = id;
	}
	@Override
	public String getBudgetDate() {
		// TODO Auto-generated method stub
		return budgetDate;
	}
	@Override
	public void setBudgetDate(String budgetDate) {
		// TODO Auto-generated method stub
		this.budgetDate = budgetDate;
	}
	@Override
	public BigDecimal getAmount() {
		// TODO Auto-generated method stub
		return amount;
	}
	@Override
	public void setAmount(BigDecimal amount) {
		// TODO Auto-generated method stub
		this.amount = amount;
	}
	@Override
	public String getProcessBy() {
		// TODO Auto-generated method stub
		return processBy;
	}
	@Override
	public void setProcessBy(String processBy) {
		// TODO Auto-generated method stub
		this.processBy = processBy;
	}
	@Override
	public int getIsActive() {
		// TODO Auto-generated method stub
		return isActive;
	}
	@Override
	public void setIsActive(int isActive) {
		// TODO Auto-generated method stub
		this.isActive = isActive;
	}
	@Override
	public IBudgetType getBudgetType() {
		// TODO Auto-generated method stub
		return budgetType;
	}
	@Override
	public void setBudgetType(IBudgetType budgetType) {
		// TODO Auto-generated method stub
		this.budgetType = budgetType;
	}
	@Override
	public BankAccounts getAccounts() {
		// TODO Auto-generated method stub
		return accounts;
	}
	@Override
	public void setAccounts(BankAccounts accounts) {
		// TODO Auto-generated method stub
		this.accounts = accounts;
	}
	
	public static void main(String[] args) {
		
		IBudget bud = new Budget();
		try{bud.setId(7);}catch(NullPointerException e){}
		try{bud.setBudgetDate(DateUtils.getCurrentDateYYYYMMDD());}catch(NullPointerException e){}
		try{bud.setAmount(new BigDecimal("200.00"));}catch(NullPointerException e){}
		try{bud.setProcessBy("mark");}catch(NullPointerException e){}
		try{bud.setIsActive(1);}catch(NullPointerException e){}
		
		IBudgetType type = new BudgetType();
		try{type.setId(1);}catch(NullPointerException e){}
		bud.setBudgetType(type);
		
		BankAccounts ac = new BankAccounts();
		try{ac.setBankId(1);}catch(NullPointerException e){}
		bud.setAccounts(ac);
		Budget.save(bud);
		
	}

	@Override
	public String getRemainingAmount() {
		// TODO Auto-generated method stub
		return remainingAmount;
	}

	@Override
	public void setRemainingAmount(String remainingAmount) {
		// TODO Auto-generated method stub
		this.remainingAmount = remainingAmount;
	}

	@Override
	public String getUsedAmount() {
		// TODO Auto-generated method stub
		return usedAmount;
	}

	@Override
	public void setUsedAmount(String usedAmount) {
		// TODO Auto-generated method stub
		this.usedAmount = usedAmount;
	}

	@Override
	public String getBudgetAmount() {
		// TODO Auto-generated method stub
		return budgetAmount;
	}

	@Override
	public void setBudgetAmount(String budgetAmount) {
		// TODO Auto-generated method stub
		this.budgetAmount = budgetAmount;
	}

	@Override
	public BigDecimal getAddedAmount() {
		// TODO Auto-generated method stub
		return addedAmount;
	}

	@Override
	public void setAddedAmount(BigDecimal addedAmount) {
		// TODO Auto-generated method stub
		this.addedAmount = addedAmount;
	}

	@Override
	public double getLimitAmount() {
		return limitAmount;
	}

	@Override
	public void setLimitAmount(double limitAmount) {
		this.limitAmount = limitAmount;
	}

	@Override
	public String getAddedAmountTmp() {
		return addedAmountTmp;
	}

	@Override
	public void setAddedAmountTmp(String addedAmountTmp) {
		this.addedAmountTmp = addedAmountTmp;
	}

	@Override
	public String getLimitAmountTmp() {
		return limitAmountTmp;
	}

	@Override
	public void setLimitAmountTmp(String limitAmountTmp) {
		this.limitAmountTmp = limitAmountTmp;
	}
	@Override
	public boolean getIsActivated() {
		return isActivated;
	}
	@Override
	public void setIsActivated(boolean isActivated) {
		this.isActivated = isActivated;
	}
	@Override
	public int getCycleDate() {
		return cycleDate;
	}
	@Override
	public void setCycleDate(int cycleDate) {
		this.cycleDate = cycleDate;
	}
	
}