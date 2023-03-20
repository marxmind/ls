package com.italia.municipality.lakesebu.controller;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.reports.ReportCompiler;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * 
 * @author mark italia
 * @since 11/12/2013
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Chequedtls {

	private long cheque_id;
	private String accntNumber;
	private String checkNo;
	private String date_disbursement;
	private String bankName;
	private String accntName;
	private double amount;
	private String payToTheOrderOf;
	private String amountInWOrds;
	private int signatory1;
	private int signatory2;
	private String processBy;
	private String date_edited;
	private String date_created;
	private int isActive;
	private int status;
	private String remarks;
	private int hasAdvice;
	private String signatoryName1;
	private String signatoryName2;
	
	private Offices office;
	private Mooe moe;
	
	private String statusName;
	private int fundTypeId;
	
	public static boolean checkIfCheckNumberExist(String checkNo) {
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			try{
			conn = BankChequeDatabaseConnect.getConnection();
			ps = conn.prepareStatement("SELECT cheque_no FROM tbl_chequedtls WHERE isactive=1 AND chkstatus=1 AND cheque_no='"+ checkNo +"'");
			
			rs = ps.executeQuery();
			
			while(rs.next()){
				System.out.println("Existing check number: " + rs.getString("cheque_no"));
				return false;
			}
			rs.close();
			ps.close();
			BankChequeDatabaseConnect.close(conn);
			}catch(SQLException sl){sl.getMessage();}
			return true;
	}
	
	public static double mooeUsed(long officeId, long mooeId, int year) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String fromDate=year+"-01-01",toDate=year+"-12-31";
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT sum(cheque_amount) as amount FROM tbl_chequedtls WHERE isactive=1 AND chkstatus=1 AND offid="+officeId + " AND moid="+ mooeId + " AND (date_disbursement>='"+fromDate+"' AND date_disbursement<='"+toDate+"')");
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			return rs.getDouble("amount");
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		return 0;
	}
	
	public static String formatAmount(String amount){
		double money = Double.valueOf(amount);
		NumberFormat format = NumberFormat.getCurrencyInstance();
		amount = format.format(money).replace("$", "");
		amount = amount.replace("Php", "");
		return amount;
	}
	
	public static Map<Integer, Double> getByMonth(int year){
		String sql = "select date_format(date_disbursement,'%m') as month, sum(cheque_amount) as amount from tbl_chequedtls where date_disbursement>='"+ year +"-01-01' and date_disbursement<='"+year+"-12-31' group by month order by month";
		Map<Integer, Double> mapData = new LinkedHashMap<Integer, Double>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		System.out.println("get by month SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			mapData.put(rs.getInt("month"), rs.getDouble("amount"));
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		
		return mapData;
	}
	/**
	 * 
	 * Revised code for fast retrieval
	 */
	public static Object[] retrieveData(String sql, String[] params){
		Object[] obj = new Object[2];
		List<Chequedtls> chks = new ArrayList<Chequedtls>();
		double totalAmount = 0d;
		String sqlTmp = "SELECT * FROM tbl_chequedtls WHERE isactive=1 ";
		
		sql = sqlTmp + sql;
		
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
		
		System.out.println("CHECK SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			if(rs.getInt("chkstatus")==1) {//record only received check
				totalAmount += rs.getDouble("cheque_amount");
			}
			Chequedtls chk = Chequedtls.builder()
					.cheque_id(rs.getLong("cheque_id"))
					.accntNumber(rs.getString("accnt_no"))
					.fundTypeId(Integer.valueOf(rs.getString("accnt_no")))
					.checkNo(rs.getString("cheque_no"))
					.accntName(rs.getString("accnt_name"))
					.bankName(rs.getString("bank_name"))
					.date_disbursement(rs.getString("date_disbursement"))
					.amount(rs.getDouble("cheque_amount"))
					.payToTheOrderOf(rs.getString("pay_to_the_order_of"))
					.amountInWOrds(rs.getString("amount_in_words"))
					.processBy(rs.getString("proc_by"))
					.date_created(rs.getString("date_created"))
					.date_edited(rs.getString("date_edited"))
					.signatory1(rs.getInt("sig1_id"))
					.signatory2(rs.getInt("sig2_id"))
					.isActive(rs.getInt("isactive"))
					.status(rs.getInt("chkstatus"))
					.remarks(rs.getString("chkremarks"))
					.hasAdvice(rs.getInt("hasadvice"))
					.office(Offices.builder().id(rs.getLong("offid")).build())
					.moe(Mooe.builder().id(rs.getLong("moid")).build())
					.build();
			chks.add(chk);
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		
		obj[0] = totalAmount;
		obj[1] = chks;
		
		return obj;
	}
	
	
	public static List<Chequedtls> retrieveCheckOnly(String sql, String[] params){
		List<Chequedtls> cList =  new ArrayList<Chequedtls>();
		
		
		String stm = "SELECT "
				+ "	d.cheque_id,d.date_disbursement,d.bank_name,d.cheque_no,d.cheque_amount,d.hasadvice,d.chkstatus,d.chkremarks,d.pay_to_the_order_of,"
				+ "	b.bank_account_name,b.bank_account_no,b.bank_id "
				+ "FROM tbl_chequedtls d,tbl_bankaccounts b "
				+ "	WHERE d.accnt_no=b.bank_account_no AND d.isactive=1 ";
		
		sql = stm + sql;
		
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
		
		System.out.println("CHECK SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			Chequedtls chk = new Chequedtls();
			try{chk.setDate_disbursement(rs.getString("date_disbursement"));}catch(NullPointerException e){}
			try{chk.setCheque_id(rs.getLong("cheque_id"));}catch(NullPointerException e){}
			try{chk.setCheckNo(rs.getString("cheque_no"));}catch(NullPointerException e){}
			try{chk.setBankName(rs.getString("bank_name"));}catch(NullPointerException e){}
			try{chk.setAmount(rs.getDouble("cheque_amount"));}catch(NullPointerException e){}
			try{chk.setPayToTheOrderOf(rs.getString("pay_to_the_order_of"));}catch(NullPointerException e){}
			try{chk.setHasAdvice(rs.getInt("hasadvice"));}catch(NullPointerException e){}
			try{chk.setStatus(rs.getInt("chkstatus"));}catch(NullPointerException e){}
			try{chk.setRemarks(rs.getString("chkremarks"));}catch(NullPointerException e){}
			try{chk.setAccntNumber(rs.getString("bank_account_no"));}catch(NullPointerException e){}
			try{chk.setAccntName(rs.getString("bank_account_name"));}catch(NullPointerException e){}
			try{chk.setStatusName(rs.getInt("chkstatus")==1? "FOR ADVICE" : "CANCELLED");}catch(NullPointerException e){}
			try{chk.setFundTypeId(rs.getInt("bank_id"));}catch(NullPointerException e){}
			chk.setOffice(Offices.builder().id(rs.getLong("offid")).build());
			chk.setMoe(Mooe.builder().id(rs.getLong("moid")).build());
			
			cList.add(chk);
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		
		return cList;
	}
	
	
	public static List<Chequedtls> retrieveChecks(String sql, String[] params){
		List<Chequedtls> cList =  new ArrayList<Chequedtls>();
		
		
		String stm = "SELECT "
				+ "	d.cheque_id,d.date_disbursement,d.bank_name,d.cheque_no,d.cheque_amount,d.hasadvice,d.chkstatus,d.chkremarks,d.pay_to_the_order_of,"
				+ "	b.bank_account_name,b.bank_account_no,b.bank_id,"
				+ "	(SELECT s.sig_name FROM tbl_signatory s WHERE s.sig_id=d.sig1_id) as sig1_id,"
				+ "	(SELECT s.sig_name FROM tbl_signatory s WHERE s.sig_id=d.sig2_id) as sig2_id "
				+ "FROM tbl_chequedtls d,tbl_bankaccounts b "
				+ "	WHERE d.accnt_no=b.bank_account_no AND d.isactive=1 ";
		
		sql = stm + sql;
		
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
		
		System.out.println("CHECK SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			Chequedtls chk = new Chequedtls();
			try{chk.setDate_disbursement(rs.getString("date_disbursement"));}catch(NullPointerException e){}
			try{chk.setCheque_id(rs.getLong("cheque_id"));}catch(NullPointerException e){}
			try{chk.setCheckNo(rs.getString("cheque_no"));}catch(NullPointerException e){}
			try{chk.setBankName(rs.getString("bank_name"));}catch(NullPointerException e){}
			try{chk.setAmount(rs.getDouble("cheque_amount"));}catch(NullPointerException e){}
			try{chk.setPayToTheOrderOf(rs.getString("pay_to_the_order_of"));}catch(NullPointerException e){}
			try{chk.setHasAdvice(rs.getInt("hasadvice"));}catch(NullPointerException e){}
			try{chk.setStatus(rs.getInt("chkstatus"));}catch(NullPointerException e){}
			try{chk.setRemarks(rs.getString("chkremarks"));}catch(NullPointerException e){}
			try{chk.setAccntNumber(rs.getString("bank_account_no"));}catch(NullPointerException e){}
			try{chk.setAccntName(rs.getString("bank_account_name"));}catch(NullPointerException e){}
			try{chk.setSignatoryName1(rs.getString("sig1_id"));}catch(NullPointerException e){}
			try{chk.setSignatoryName2(rs.getString("sig2_id"));}catch(NullPointerException e){}
			try{chk.setStatusName(rs.getInt("chkstatus")==1? "FOR ADVICE" : "CANCELLED");}catch(NullPointerException e){}
			try{chk.setFundTypeId(rs.getInt("bank_id"));}catch(NullPointerException e){}
			chk.setOffice(Offices.builder().id(rs.getLong("offid")).build());
			chk.setMoe(Mooe.builder().id(rs.getLong("moid")).build());
			cList.add(chk);
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		
		return cList;
	}
	
	public static List<Chequedtls> retrieve(String sql, String[] params){
		List<Chequedtls> cList =  new ArrayList<Chequedtls>();
		
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
		
		System.out.println("CHECK SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			Chequedtls chk = new Chequedtls();
			try{chk.setCheque_id(rs.getLong("cheque_id"));}catch(NullPointerException e){}
			try{chk.setCheckNo(rs.getString("cheque_no"));}catch(NullPointerException e){}
			try{chk.setAccntNumber(rs.getString("accnt_no"));}catch(NullPointerException e){}
			try{chk.setAccntName(rs.getString("accnt_name"));}catch(NullPointerException e){}
			try{chk.setBankName(rs.getString("bank_name"));}catch(NullPointerException e){}
			try{chk.setDate_disbursement(rs.getString("date_disbursement"));}catch(NullPointerException e){}
			try{chk.setAmount(rs.getDouble("cheque_amount"));}catch(NullPointerException e){}
			try{chk.setPayToTheOrderOf(rs.getString("pay_to_the_order_of"));}catch(NullPointerException e){}
			try{chk.setAmountInWOrds(rs.getString("amount_in_words"));}catch(NullPointerException e){}
			try{chk.setProcessBy(rs.getString("proc_by"));}catch(NullPointerException e){}
			try{chk.setDate_created(timeStamp(rs.getTimestamp("date_created")+""));}catch(NullPointerException e){}
			try{chk.setDate_edited(timeStamp(rs.getTimestamp("date_edited")+""));}catch(NullPointerException e){}
			try{chk.setIsActive(rs.getInt("isactive"));}catch(NullPointerException e){}
			try{chk.setStatus(rs.getInt("chkstatus"));}catch(NullPointerException e){}
			try{chk.setRemarks(rs.getString("chkremarks"));}catch(NullPointerException e){}
			try{chk.setHasAdvice(rs.getInt("hasadvice"));}catch(NullPointerException e){}
			int sig1=0,sig2=0;
			chk.setOffice(Offices.builder().id(rs.getLong("offid")).build());
			chk.setMoe(Mooe.builder().id(rs.getLong("moid")).build());
			
			try{
				sig1=rs.getInt("sig1_id");
				chk.setSignatory1(sig1);}catch(NullPointerException e){}
			try{
				sig2=rs.getInt("sig2_id");
				chk.setSignatory2(sig2);}catch(NullPointerException e){}
			
			sql = "SELECT * FROM tbl_signatory WHERE sig_id=?";
			params = new String[1];
			params[0] = sig1+"";
			try{chk.setSignatoryName1(Signatory.retrieve(sql, params).get(0).getSigName());}catch(Exception e){}
			params[0] = sig2+"";
			try{chk.setSignatoryName2(Signatory.retrieve(sql, params).get(0).getSigName());}catch(Exception e){}
			
			cList.add(chk);
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){sl.getMessage();}
		
		return cList;
	}
	
	/**
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static BigDecimal sum(String sql, String[] params,String fieldName){
		Connection conn = null;
		ResultSet rs = null;
		BigDecimal amnt = new BigDecimal("0.00"); 
		PreparedStatement ps = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		System.out.println("Budget SQL: " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			amnt = rs.getBigDecimal(fieldName);
		}
		
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException e){}
		
		return amnt;
	}
	
	private static String timeStamp(String timestamp){
		try{
			timestamp = timestamp.split("\\.")[0];
			//System.out.println("new timeStamp " + timestamp);
			String time = "", date="";
			date = timestamp.split(" ")[0];
			time = timestamp.split(" ")[1];
			//System.out.println("new time " + time);
			return date + " " + DateUtils.timeTo12Format(time,true);
		}catch(Exception e){}
		
		return "error";
	}
	
	
	
	
	public static List<String> retrievePayOrderOf(String sql, String[] params){
		List<Chequedtls> cList =  new ArrayList<Chequedtls>();
		List<String> results = new ArrayList<String>();
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
		
		//System.out.println("SQL " + ps.toString());
		
		
		rs = ps.executeQuery();
		
		while(rs.next()){
			results.add(rs.getString("pay_to_the_order_of"));
		}
		rs.close();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return results;
	}
	
	public static Chequedtls save(Chequedtls chk){
		if(chk!=null){
			long id = getInfo(chk.getCheque_id()==0? getLatestId()+1 : chk.getCheque_id());
			if(id==1){
				chk = Chequedtls.insertData(chk, "1");
			}else if(id==2){
				chk = Chequedtls.updateData(chk);
			}else if(id==3){
				chk = Chequedtls.insertData(chk, "3");
			}
			
		}
		return chk;
	}
	
	public void save(){
			long id = getInfo(getCheque_id()==0? getLatestId()+1 : getCheque_id());
			if(id==1){
				insertData("1");
			}else if(id==2){
				updateData();
			}else if(id==3){
				insertData("3");
			}
	}
	
	public static Chequedtls insertData(Chequedtls chk, String type){
		String sql = "INSERT INTO tbl_chequedtls ("
				+ "cheque_id,"
				+ "accnt_no,"
				+ "cheque_no,"
				+ "accnt_name,"
				+ "bank_name,"
				+ "date_disbursement,"
				+ "cheque_amount,"
				+ "pay_to_the_order_of,"
				+ "amount_in_words,"
				+ "proc_by,"
				+ "sig1_id,"
				+ "sig2_id,"
				+ "date_edited,"
				+ "isactive,"
				+ "chkstatus,"
				+ "chkremarks,"
				+ "hasadvice,"
				+ "offid,"
				+ "moid) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt=1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			chk.setCheque_id(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			chk.setCheque_id(id);
		}
		ps.setString(cnt++, chk.getAccntNumber());
		ps.setString(cnt++, chk.getCheckNo());
		ps.setString(cnt++, chk.getAccntName());
		ps.setString(cnt++, chk.getBankName());
		ps.setString(cnt++, chk.getDate_disbursement());
		ps.setDouble(cnt++, chk.getAmount());
		ps.setString(cnt++, chk.getPayToTheOrderOf());
		ps.setString(cnt++, chk.getAmountInWOrds());
		ps.setString(cnt++, chk.getProcessBy());
		ps.setInt(cnt++, chk.getSignatory1());
		ps.setInt(cnt++, chk.getSignatory2());
		ps.setTimestamp(cnt++, Timestamp.valueOf(chk.getDate_edited()));
		ps.setInt(cnt++, chk.getIsActive());
		ps.setInt(cnt++, chk.getStatus());
		ps.setString(cnt++, chk.getRemarks());
		ps.setInt(cnt++, chk.getHasAdvice());
		ps.setLong(cnt++, chk.getOffice().getId());
		ps.setLong(cnt++, chk.getMoe().getId());
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		return chk;
	}
	
	public void insertData(String type){
		String sql = "INSERT INTO tbl_chequedtls ("
				+ "cheque_id,"
				+ "accnt_no,"
				+ "cheque_no,"
				+ "accnt_name,"
				+ "bank_name,"
				+ "date_disbursement,"
				+ "cheque_amount,"
				+ "pay_to_the_order_of,"
				+ "amount_in_words,"
				+ "proc_by,"
				+ "sig1_id,"
				+ "sig2_id,"
				+ "date_edited,"
				+ "isactive,"
				+ "chkstatus,"
				+ "chkremarks,"
				+ "hasadvice,"
				+ "offid,"
				+ "moid) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		int cnt = 1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(cnt++, id);
			setCheque_id(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(cnt++, id);
			setCheque_id(id);
		}
		ps.setString(cnt++, getAccntNumber());
		ps.setString(cnt++, getCheckNo());
		ps.setString(cnt++, getAccntName());
		ps.setString(cnt++, getBankName());
		ps.setString(cnt++, getDate_disbursement());
		ps.setDouble(cnt++, getAmount());
		ps.setString(cnt++, getPayToTheOrderOf());
		ps.setString(cnt++, getAmountInWOrds());
		ps.setString(cnt++, getProcessBy());
		ps.setInt(cnt++, getSignatory1());
		ps.setInt(cnt++, getSignatory2());
		ps.setTimestamp(cnt++, Timestamp.valueOf(getDate_edited()));
		ps.setInt(cnt++, getIsActive());
		ps.setInt(cnt++, getStatus());
		ps.setString(cnt++, getRemarks());
		ps.setInt(cnt++, hasAdvice);
		ps.setLong(cnt++, getOffice().getId());
		ps.setLong(cnt++, getMoe().getId());
		ps.execute();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
	}
	
	public static Chequedtls updateData(Chequedtls chk){
		String sql = "UPDATE tbl_chequedtls SET "
				+ "accnt_no=?,"
				+ "cheque_no=?,"
				+ "accnt_name=?,"
				+ "bank_name=?,"
				+ "date_disbursement=?,"
				+ "cheque_amount=?,"
				+ "pay_to_the_order_of=?,"
				+ "amount_in_words=?,"
				+ "proc_by=?,"
				+ "sig1_id=?,"
				+ "sig2_id=?,"
				+ "chkstatus=?,"
				+ "chkremarks=?,"
				+ "hasadvice=?,"
				+ "offid=?,"
				+ "moid=? " 
				+ " WHERE cheque_id=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt=1;
		ps.setString(cnt++, chk.getAccntNumber());
		ps.setString(cnt++, chk.getCheckNo());
		ps.setString(cnt++, chk.getAccntName());
		ps.setString(cnt++, chk.getBankName());
		ps.setString(cnt++, chk.getDate_disbursement());
		ps.setDouble(cnt++, chk.getAmount());
		ps.setString(cnt++, chk.getPayToTheOrderOf());
		ps.setString(cnt++, chk.getAmountInWOrds());
		ps.setString(cnt++, chk.getProcessBy());
		ps.setInt(cnt++, chk.getSignatory1());
		ps.setInt(cnt++, chk.getSignatory2());
		ps.setInt(cnt++, chk.getStatus());
		ps.setString(cnt++, chk.getRemarks());
		ps.setInt(cnt++, chk.getHasAdvice());
		ps.setLong(cnt++, chk.getOffice().getId());
		ps.setLong(cnt++, chk.getMoe().getId());
		ps.setLong(cnt++, chk.getCheque_id());
		
		ps.executeUpdate();
		ps.close();
		BankChequeDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		return chk;
	}
	
	public void updateData(){
		String sql = "UPDATE tbl_chequedtls SET "
				+ "accnt_no=?,"
				+ "cheque_no=?,"
				+ "accnt_name=?,"
				+ "bank_name=?,"
				+ "date_disbursement=?,"
				+ "cheque_amount=?,"
				+ "pay_to_the_order_of=?,"
				+ "amount_in_words=?,"
				+ "proc_by=?,"
				+ "sig1_id=?,"
				+ "sig2_id=?,"
				+ "chkstatus=?,"
				+ "chkremarks=?,"
				+ "hasadvice=?,"
				+ "offid=?,"
				+ "moid=? "  
				+ " WHERE cheque_id=?";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		conn = BankChequeDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		int cnt=1;
		ps.setString(cnt++, getAccntNumber());
		ps.setString(cnt++, getCheckNo());
		ps.setString(cnt++, getAccntName());
		ps.setString(cnt++, getBankName());
		ps.setString(cnt++, getDate_disbursement());
		ps.setDouble(cnt++, getAmount());
		ps.setString(cnt++, getPayToTheOrderOf());
		ps.setString(cnt++, getAmountInWOrds());
		ps.setString(cnt++, getProcessBy());
		ps.setInt(cnt++, getSignatory1());
		ps.setInt(cnt++, getSignatory2());
		ps.setInt(cnt++, getStatus());
		ps.setString(cnt++, getRemarks());
		ps.setInt(cnt++, getHasAdvice());
		ps.setLong(cnt++, getOffice().getId());
		ps.setLong(cnt++, getMoe().getId());
		ps.setLong(cnt++, getCheque_id());
		
		ps.executeUpdate();
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
		sql="SELECT cheque_id FROM tbl_chequedtls  ORDER BY cheque_id DESC LIMIT 1";	
		conn = BankChequeDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("cheque_id");
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
		ps = conn.prepareStatement("SELECT cheque_id FROM tbl_chequedtls WHERE cheque_id=?");
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
		String sql = "update tbl_chequedtls set isactive=0, chkstatus=0,chkremarks='VOID' WHERE cheque_id=?";
		String params[] = new String[1];
		params[0] = getCheque_id()+"";
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
	
	public static void compileReport(Chequedtls reportFields){
		try{
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = ReadConfig.value(AppConf.CHEQUE_REPORT_NAME);
		String JRXMLFILE= ReadConfig.value(AppConf.CHEQUE_JRXML_FILE);
		
		System.out.println("CheckReport path: " + REPORT_PATH);
		HashMap paramMap = new HashMap();
		Chequedtls rpt = reportFields;
		ReportCompiler compiler = new ReportCompiler();
		System.out.println("REPORT_NAME: " +REPORT_NAME + " REPORT_PATH: " + REPORT_PATH);
		String jasperreportLocation = compiler.compileReport(JRXMLFILE, REPORT_NAME, REPORT_PATH);
		System.out.println("Check report path: " + jasperreportLocation);
		HashMap params = new HashMap();
		
		params.put("PARAM_ACCOUNT_NUMBER", rpt.getAccntNumber());
		params.put("PARAM_CHECK_NUMBER", rpt.getCheckNo());
		params.put("PARAM_DATE_DISBURSEMENT", rpt.getDate_disbursement());
		params.put("PARAM_BANK_NAME", rpt.getBankName().toUpperCase());
		params.put("PARAM_ACCOUNT_NAME", rpt.getAccntName().toUpperCase());
		params.put("PARAM_AMOUNT", Currency.formatAmount(rpt.getAmount()));
		params.put("PARAM_PAYTOORDEROF", rpt.getPayToTheOrderOf().toUpperCase());
		
		params.put("PARAM_AMOUNT_INWORDS", rpt.getAmountInWOrds().toUpperCase());
		
		String sql = "select * from tbl_signatory";
		Map<String, Signatory> sigs = Signatory.retrieveSig(sql, new String[0]);
		
		params.put("PARAM_SIGNATORY1", sigs.get(rpt.getSignatory1()+"").getSigName());
		params.put("PARAM_SIGNATORY2", sigs.get(rpt.getSignatory2()+"").getSigName());
		
		
		
		JasperPrint print = compiler.report(jasperreportLocation, params);
		File pdf = null;
		
		pdf = new File(REPORT_PATH+REPORT_NAME+".pdf");
		pdf.createNewFile();
		JasperExportManager.exportReportToPdfStream(print, new FileOutputStream(pdf));
		System.out.println("pdf successfully created...");
		System.out.println("Creating a backup copy....");
		pdf = new File(REPORT_PATH+REPORT_NAME+"_copy"+".pdf");
		pdf.createNewFile();
		JasperExportManager.exportReportToPdfStream(print, new FileOutputStream(pdf));
		
		System.out.println("Done creating a backup copy....");
		}catch(Exception e){
			e.printStackTrace();
		}
			
	}
	
	public static void backupPrint(){
		
		String REPORT_PATH = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
				AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() + AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String REPORT_NAME = AppConf.CHEQUE_REPORT_NAME.getValue();
		String REPORT_PATH_FILE = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() +  AppConf.REPORT_FOLDER.getValue() + AppConf.SEPERATOR.getValue();
		String dll = "rundll32 url.dll,FileProtocolHandler";
		String fileName = REPORT_PATH + REPORT_NAME + ".pdf"; 
		File file = new File(fileName);
		try{
			if(file.exists()){
				//Process process = Runtime.getRuntime().exec(dll + " " + fileName);
				Process process = Runtime.getRuntime().exec("cmd /c start /wait " + REPORT_PATH + "copyReport.bat");
				process = Runtime.getRuntime().exec(dll + " " + REPORT_PATH_FILE + REPORT_NAME + ".pdf");
				process.waitFor();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void close(Closeable resource) {
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
