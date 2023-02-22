package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.CashBookConnect;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 02/01/2017
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class CashTransactions {

	private long id;
	private String dateTrans;
	private String particulars;
	private String naturePayment;
	private String voucherNo;
	private String orNumber;
	private String checkNo;
	private int isActive;
	private int transType;
	private double debitAmount;
	private double creditAmount;
	private double balances;
	private Timestamp timestamp;
	private UserDtls userDtls;
	private BankAccounts accounts;
	private Department department;
	
	private int cnt;
	private String transactionName;
	private String dAmount;
	private String cAmount;
	private String bAmount;
	private String departmentCode;
	
	public static String voucherNumber(int accountId){
		String voucherNo = "";
		
		String sql = "SELECT * FROM cashtransactions WHERE bank_id=? AND (voucherno!='' OR voucherno is not null) ORDER BY voucherno DESC LIMIT 1";
		String[] params = new String[1];
		params[0] = accountId+"";
		CashTransactions tran = null;
		String v1="",v2="",v3="",v4="";
		try{
			tran = CashTransactions.retrieve(sql, params).get(0);
			if(tran!=null){
				voucherNo = tran.getVoucherNo();
				v1 = voucherNo.split("-")[0];
				v2 = (DateUtils.getCurrentYear()+"").substring(2, 4);//voucherNo.split("-")[1];
				v3 = DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+"";//voucherNo.split("-")[2];
				v4 = voucherNo.split("-")[3];
				int vno = Integer.valueOf(v4);
				String newVN = (vno+1) + ""; 
				int len = newVN.length();
				
				if(accountId==4){
					switch(len){
					case 4: voucherNo=v1+"-"+v2+"-"+v3+"-"+newVN; break;
					case 3: voucherNo=v1+"-"+v2+"-"+v3+"-"+"0"+newVN; break;
					case 2: voucherNo=v1+"-"+v2+"-"+v3+"-"+"00"+newVN; break;
					case 1: voucherNo=v1+"-"+v2+"-"+v3+"-"+"000"+newVN; break;
					}
				}else{
				
				switch(len){
				case 3: voucherNo=v1+"-"+v2+"-"+v3+"-"+""+newVN; break;
				case 2: voucherNo=v1+"-"+v2+"-"+v3+"-"+"0"+newVN; break;
				case 1: voucherNo=v1+"-"+v2+"-"+v3+"-"+"00"+newVN; break;
				}
				
				}
				
			}else{
				
				if(accountId==1){
					v1 = "000";
				}else if(accountId==2 || accountId==4){
					v1 = "101";
				}else if(accountId==3 || accountId==5){
					v1 = "401";
				}
				
				v2 = (DateUtils.getCurrentYear()+"").substring(2, 4);
				v3 = DateUtils.getCurrentMonth()<10? "0"+DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()+"";
				if(accountId==2){
					v4 = "001";
				}else{
					v4 = "0001";
				}
				voucherNo=v1+"-"+v2+"-"+v3+"-"+v4;
			}
			
			
		}catch(Exception e){}
		return voucherNo;
	}
	
	public static String checkNumber(int accountId){
		int checkNo = 0;
		String newCheckNo = "";
		String sql = "SELECT * FROM cashtransactions WHERE bank_id=? AND (checkno!='' OR checkno is not null) ORDER BY checkno DESC LIMIT 1";
		String[] params = new String[1];
		params[0] = accountId+"";
		CashTransactions tran = null;
		try{
			tran = CashTransactions.retrieve(sql, params).get(0);
			checkNo = Integer.valueOf(tran.getCheckNo());
			if(checkNo>0){
				checkNo += 1;
				
				String tmpCheckNo = checkNo+"";
				int len = tmpCheckNo.length();
				
				switch(len){
				case 10 : newCheckNo = ""+checkNo; break;
				case 9 : newCheckNo = "0"+checkNo; break;
				case 8 : newCheckNo = "00"+checkNo; break;
				case 7 : newCheckNo = "000"+checkNo; break;
				case 6 : newCheckNo = "0000"+checkNo; break;
				case 5 : newCheckNo = "00000"+checkNo; break;
				case 4 : newCheckNo = "000000"+checkNo; break;
				case 3 : newCheckNo = "0000000"+checkNo; break;
				case 2 : newCheckNo = "00000000"+checkNo; break;
				case 1 : newCheckNo = "000000000"+checkNo; break;
				}
				
			}
			
		}catch(Exception e){}
		return newCheckNo;
	}
	
	public static List<CashTransactions> retrieve(String sql, String[] params){
		List<CashTransactions> trans = new ArrayList<CashTransactions>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		
		System.out.println("CASH SQL " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
		
			CashTransactions tran = new CashTransactions();
			tran.setId(rs.getLong("cashid"));
			tran.setDateTrans(rs.getString("cashDate"));
			tran.setParticulars(rs.getString("cashParticulars"));
			tran.setNaturePayment(rs.getString("naturepayment"));
			tran.setVoucherNo(rs.getString("voucherno"));
			tran.setOrNumber(rs.getString("orno"));
			tran.setCheckNo(rs.getString("checkno"));
			tran.setIsActive(rs.getInt("cashisactive"));
			tran.setTransType(rs.getInt("cashtranstype"));
			tran.setDebitAmount(rs.getDouble("cashDebit"));
			tran.setCreditAmount(rs.getDouble("cashCredit"));
			tran.setBalances(rs.getDouble("cashBalances"));
			tran.setTimestamp(rs.getTimestamp("cashtimestamp"));
			
			UserDtls user = new UserDtls();
			user.setUserdtlsid(rs.getLong("userdtlsid"));
			tran.setUserDtls(user);
			
			BankAccounts accounts = new BankAccounts();
			accounts.setBankId(rs.getInt("bank_id"));
			tran.setAccounts(accounts);
			
			Department dep = new Department();
			dep.setDepid(rs.getInt("departmentid"));
			tran.setDepartment(dep);
			
			trans.add(tran);
		}
		
		rs.close();
		ps.close();
		CashBookConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		return trans;
	}
	
	public static CashTransactions save(CashTransactions tran){
		if(tran!=null){
			long id = getInfo(tran.getId()==0? getLatestId()+1 : tran.getId());
			if(id==1){
				tran = CashTransactions.insertData(tran, "1");
			}else if(id==2){
				tran = CashTransactions.updateData(tran);
			}else if(id==3){
				tran = CashTransactions.insertData(tran, "3");
			}
			
		}
		return tran;
	}
	
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
	
	public static CashTransactions insertData(CashTransactions tran, String type){
		String sql = "INSERT INTO cashtransactions ("
				+ "cashid,"
				+ "cashDate,"
				+ "cashParticulars,"
				+ "voucherno,"
				+ "cashisactive,"
				+ "cashtranstype,"
				+ "cashDebit,"
				+ "cashCredit,"
				+ "cashBalances,"
				+ "userdtlsid,"
				+ "bank_id,"
				+ "orno,"
				+ "checkno,"
				+ "naturepayment,"
				+ "departmentid) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("start inserting data to cashtransactions");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			tran.setId(id);
			LogU.add(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			tran.setId(id);
			ps.setLong(1, id);
			ps.setLong(cnt++, id);
			LogU.add(id);
		}
		
		ps.setString(cnt++, tran.getDateTrans());
		ps.setString(cnt++, tran.getParticulars());
		ps.setString(cnt++, tran.getVoucherNo());
		ps.setInt(cnt++, tran.getIsActive());
		ps.setInt(cnt++, tran.getTransType());
		ps.setDouble(cnt++, tran.getDebitAmount());
		ps.setDouble(cnt++, tran.getCreditAmount());
		ps.setDouble(cnt++, tran.getBalances());
		ps.setLong(cnt++, tran.getUserDtls()==null? 0 : tran.getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, tran.getAccounts()==null? 0 : tran.getAccounts().getBankId());
		ps.setString(cnt++, tran.getOrNumber());
		ps.setString(cnt++, tran.getCheckNo());
		ps.setString(cnt++, tran.getNaturePayment());
		ps.setInt(cnt++, tran.getDepartment()==null? 0 : tran.getDepartment().getDepid());
		
		LogU.add(tran.getDateTrans());
		LogU.add(tran.getParticulars());
		LogU.add(tran.getVoucherNo());
		LogU.add(tran.getIsActive());
		LogU.add(tran.getTransType());
		LogU.add(tran.getDebitAmount());
		LogU.add(tran.getCreditAmount());
		LogU.add(tran.getBalances());
		LogU.add(tran.getUserDtls()==null? 0 : tran.getUserDtls().getUserdtlsid());
		LogU.add(tran.getAccounts()==null? 0 : tran.getAccounts().getBankId());
		LogU.add(tran.getOrNumber());
		LogU.add(tran.getCheckNo());
		LogU.add(tran.getNaturePayment());
		LogU.add(tran.getDepartment()==null? 0 : tran.getDepartment().getDepid());
		
		ps.execute();
		ps.close();
		CashBookConnect.close(conn);
		LogU.add("end inserting data to cashtransactions");
		}catch(SQLException s){
			LogU.add("error inserting data to cashtransactions : " + s.getMessage());
		}
		return tran;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO cashtransactions ("
				+ "cashid,"
				+ "cashDate,"
				+ "cashParticulars,"
				+ "voucherno,"
				+ "cashisactive,"
				+ "cashtranstype,"
				+ "cashDebit,"
				+ "cashCredit,"
				+ "cashBalances,"
				+ "userdtlsid,"
				+ "bank_id,"
				+ "orno,"
				+ "checkno,"
				+ "naturepayment,"
				+ "departmentid) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		LogU.add("start inserting data to cashtransactions");
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			setId(id);
			LogU.add(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			setId(id);
			ps.setLong(cnt++, id);
			LogU.add(id);
		}
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getParticulars());
		ps.setString(cnt++, getVoucherNo());
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getTransType());
		ps.setDouble(cnt++, getDebitAmount());
		ps.setDouble(cnt++, getCreditAmount());
		ps.setDouble(cnt++, getBalances());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, getAccounts()==null? 0 : getAccounts().getBankId());
		ps.setString(cnt++, getOrNumber());
		ps.setString(cnt++, getCheckNo());
		ps.setString(cnt++, getNaturePayment());
		ps.setInt(cnt++, getDepartment()==null? 0 : getDepartment().getDepid());
		
		LogU.add(getDateTrans());
		LogU.add(getParticulars());
		LogU.add(getVoucherNo());
		LogU.add(getIsActive());
		LogU.add(getTransType());
		LogU.add(getDebitAmount());
		LogU.add(getCreditAmount());
		LogU.add(getBalances());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getAccounts()==null? 0 : getAccounts().getBankId());
		LogU.add(getOrNumber());
		LogU.add(getCheckNo());
		LogU.add(getNaturePayment());
		LogU.add(getDepartment()==null? 0 : getDepartment().getDepid());
		
		ps.execute();
		ps.close();
		CashBookConnect.close(conn);
		LogU.add("end inserting data to cashtransactions");
		}catch(SQLException s){
			LogU.add("error inserting data to cashtransactions : " + s.getMessage());
		}
	}
	
	public static CashTransactions updateData(CashTransactions tran){
		String sql = "UPDATE cashtransactions SET "
				+ "cashDate=?,"
				+ "cashParticulars=?,"
				+ "voucherno=?,"
				+ "cashtranstype=?,"
				+ "cashDebit=?,"
				+ "cashCredit=?,"
				+ "cashBalances=?,"
				+ "userdtlsid=?,"
				+ "bank_id=?,"
				+ "orno=?,"
				+ "checkno=?,"
				+ "naturepayment=?,"
				+ "departmentid=? " 
				+ " WHERE cashid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt=1;
		LogU.add("start updating data to cashtransactions");
		
		ps.setString(cnt++, tran.getDateTrans());
		ps.setString(cnt++, tran.getParticulars());
		ps.setString(cnt++, tran.getVoucherNo());
		ps.setInt(cnt++, tran.getTransType());
		ps.setDouble(cnt++, tran.getDebitAmount());
		ps.setDouble(cnt++, tran.getCreditAmount());
		ps.setDouble(cnt++, tran.getBalances());
		ps.setLong(cnt++, tran.getUserDtls()==null? 0 : tran.getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, tran.getAccounts()==null? 0 : tran.getAccounts().getBankId());
		ps.setString(cnt++, tran.getOrNumber());
		ps.setString(cnt++, tran.getCheckNo());
		ps.setString(cnt++, tran.getNaturePayment());
		ps.setInt(cnt++, tran.getDepartment()==null? 0 : tran.getDepartment().getDepid());
		ps.setLong(cnt++, tran.getId());
		
		LogU.add(tran.getDateTrans());
		LogU.add(tran.getParticulars());
		LogU.add(tran.getVoucherNo());
		LogU.add(tran.getTransType());
		LogU.add(tran.getDebitAmount());
		LogU.add(tran.getCreditAmount());
		LogU.add(tran.getBalances());
		LogU.add(tran.getUserDtls()==null? 0 : tran.getUserDtls().getUserdtlsid());
		LogU.add(tran.getAccounts()==null? 0 : tran.getAccounts().getBankId());
		LogU.add(tran.getOrNumber());
		LogU.add(tran.getCheckNo());
		LogU.add(tran.getNaturePayment());
		LogU.add(tran.getDepartment()==null? 0 : tran.getDepartment().getDepid());
		LogU.add(tran.getId());
		
		ps.execute();
		ps.close();
		CashBookConnect.close(conn);
		LogU.add("end updating data to cashtransactions");
		}catch(SQLException s){
			LogU.add("error updating data to cashtransactions : " + s.getMessage());
		}
		return tran;
	}
	
	public void updateData(){
		String sql = "UPDATE cashtransactions SET "
				+ "cashDate=?,"
				+ "cashParticulars=?,"
				+ "voucherno=?,"
				+ "cashtranstype=?,"
				+ "cashDebit=?,"
				+ "cashCredit=?,"
				+ "cashBalances=?,"
				+ "userdtlsid=?,"
				+ "bank_id=?,"
				+ "orno=?,"
				+ "checkno=?,"
				+ "naturepayment=?,"
				+ "departmentid=? " 
				+ " WHERE cashid=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		int cnt=1;
		LogU.add("start updating data to cashtransactions");
		
		ps.setString(cnt++, getDateTrans());
		ps.setString(cnt++, getParticulars());
		ps.setString(cnt++, getVoucherNo());
		ps.setInt(cnt++, getTransType());
		ps.setDouble(cnt++, getDebitAmount());
		ps.setDouble(cnt++, getCreditAmount());
		ps.setDouble(cnt++, getBalances());
		ps.setLong(cnt++, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		ps.setInt(cnt++, getAccounts()==null? 0 : getAccounts().getBankId());
		ps.setString(cnt++, getOrNumber());
		ps.setString(cnt++, getCheckNo());
		ps.setString(cnt++, getNaturePayment());
		ps.setInt(cnt++, getDepartment()==null? 0 : getDepartment().getDepid());
		ps.setLong(cnt++, getId());
		
		LogU.add(getDateTrans());
		LogU.add(getParticulars());
		LogU.add(getVoucherNo());
		LogU.add(getTransType());
		LogU.add(getDebitAmount());
		LogU.add(getCreditAmount());
		LogU.add(getBalances());
		LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
		LogU.add(getAccounts()==null? 0 : getAccounts().getBankId());
		LogU.add(getOrNumber());
		LogU.add(getCheckNo());
		LogU.add(getNaturePayment());
		LogU.add(getDepartment()==null? 0 : getDepartment().getDepid());
		LogU.add(getId());
		
		ps.execute();
		ps.close();
		CashBookConnect.close(conn);
		LogU.add("end updating data to cashtransactions");
		}catch(SQLException s){
			LogU.add("error updating data to cashtransactions : " + s.getMessage());
		}
	}
	
	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT cashid FROM cashtransactions  ORDER BY cashid DESC LIMIT 1";	
		conn = CashBookConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("cashid");
		}
		
		rs.close();
		prep.close();
		CashBookConnect.close(conn);
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
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement("SELECT cashid FROM cashtransactions WHERE cashid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		CashBookConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
			
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		CashBookConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
	
	public void delete(){
		String sql = "update cashtransactions set cashisactive=0, userdtlsid="+ Login.getUserLogin().getUserDtls().getUserdtlsid() +" WHERE cashid=?";
		String params[] = new String[1];
		params[0] = getId()+"";
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = CashBookConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		System.out.println("cash delete id =" + getId());
		ps.close();
		CashBookConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
}
