package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.BankChequeDatabaseConnect;
import com.italia.municipality.lakesebu.database.TaxDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.utils.LogU;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TaxPayerReceipt implements ITaxPayerReceipt {

	private long id;
	private String location;
	private String lotBlockNo;
	private String taxdecno;
	private double assValueLand;
	private double assValueImprv;
	private double assValueTotal;
	private double taxDue;
	private String installmentRangeAndType;
	private double fullPayment;
	private double penaltyPercent;
	private double overallTotal;
	private int isActive;
	private ITaxPayor payor;
	private ITaxPayorTrans payorTrans;
	private Timestamp timestamp;
	private ILandType landType;
	private boolean isAdjust;
	private boolean isCase;
	private String remarks;
	private UserDtls userDtls;
	private String landOwnerName;
	
	private int cnt;
	private String fromYear;
	private String toYear;
	private String installmentType;
	
	private LandPayor landPayor;
	
	
	
	public static List<ITaxPayerReceipt> retrieve(String sql, String[] params){
		List<ITaxPayerReceipt> pays = new ArrayList<ITaxPayerReceipt>();
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		//System.out.println("SQL : " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			ITaxPayerReceipt rec = new TaxPayerReceipt();
			try{rec.setId(rs.getLong("recid"));}catch(NullPointerException e){}
			try{rec.setLocation(rs.getString("propertylocation"));}catch(NullPointerException e){}
			try{rec.setLotBlockNo(rs.getString("lotblockno"));}catch(NullPointerException e){}
			try{rec.setTaxDecNo(rs.getString("taxdecno"));}catch(NullPointerException e){}
			try{rec.setAssValueLand(rs.getDouble("assvalueland"));}catch(NullPointerException e){}
			try{rec.setAssValueImprv(rs.getDouble("assvalueimprv"));}catch(NullPointerException e){}
			try{rec.setAssValueTotal(rs.getDouble("assvaluetotal"));}catch(NullPointerException e){}
			try{rec.setTaxDue(rs.getDouble("taxdue"));}catch(NullPointerException e){}
			try{rec.setInstallmentRangeAndType(rs.getString("insrangeandtype"));}catch(NullPointerException e){}
			try{rec.setFullPayment(rs.getDouble("fullpayment"));}catch(NullPointerException e){}
			try{rec.setPenaltyPercent(rs.getDouble("penaltypercent"));}catch(NullPointerException e){}
			try{rec.setOverallTotal(rs.getDouble("overalltotal"));}catch(NullPointerException e){}
			try{rec.setIsActive(rs.getInt("recisactive"));}catch(NullPointerException e){}
			try{rec.setTimestamp(rs.getTimestamp("rectimestamp"));}catch(NullPointerException e){}
			try{rec.setIsAdjust(rs.getInt("isAdjust")==1? true : false);}catch(NullPointerException e){}
			try{rec.setIsCase(rs.getInt("iscase")==1? true : false);}catch(NullPointerException e){}
			try{rec.setRemarks(rs.getString("remarks"));}catch(NullPointerException e){}
			try{rec.setLandOwnerName(rs.getString("landownername"));}catch(NullPointerException e){}
			try{UserDtls user = new UserDtls();
			user.setUserdtlsid(rs.getLong("processby"));
			rec.setUserDtls(user);}catch(NullPointerException e){}
			
			ITaxPayorTrans trans = new TaxPayorTrans();
			try{trans.setId(rs.getLong("payortransid"));}catch(NullPointerException e){}
			try{trans.setTransDate(rs.getString("payortransdate"));}catch(NullPointerException e){}
			try{trans.setAmount(rs.getDouble("payortransamount"));}catch(NullPointerException e){}
			try{trans.setAmountInWords(rs.getString("payortransamountinwords"));}catch(NullPointerException e){}
			try{trans.setScNo(rs.getString("payorscno"));}catch(NullPointerException e){}
			try{trans.setAccountFormNo(rs.getInt("payoraccntform"));}catch(NullPointerException e){}
			try{trans.setIsactive(rs.getInt("paytransisactive"));}catch(NullPointerException e){}
			try{trans.setCheckNo(rs.getString("paycheckno"));}catch(NullPointerException e){}
			try{trans.setCheckDate(rs.getString("paycheckdate"));}catch(NullPointerException e){}
			try{trans.setSigned1(rs.getString("paysigned1"));}catch(NullPointerException e){}
			try{trans.setSigned2(rs.getString("paysigned2"));}catch(NullPointerException e){}
			try{trans.setTimestamp(rs.getTimestamp("payortranstimestamp"));}catch(NullPointerException e){}
			rec.setPayorTrans(trans);
			
			ITaxPayor pay = new TaxPayor();
			try{pay.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{pay.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{pay.setAddress(rs.getString("payoraddress"));}catch(NullPointerException e){}
			try{pay.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			try{pay.setTimestamp(rs.getTimestamp("paytimestamp"));}catch(NullPointerException e){}
			rec.setPayor(pay);
			
			ILandType type = new LandType();
			try{type.setId(rs.getInt("landid"));}catch(NullPointerException e){}
			try{type.setLandType(rs.getString("landtype"));}catch(NullPointerException e){}
			try{type.setTimestamp(rs.getTimestamp("landtimestamp"));}catch(NullPointerException e){}
			rec.setLandType(type);
			
			pays.add(rec);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pays;
	}
	
	public static List<TaxPayerReceipt> retrieveReceipts(String sql, String[] params){
		List<TaxPayerReceipt> pays = new ArrayList<TaxPayerReceipt>();
		
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String tblRp = "rc";
		String tblTrn = "tr";
		String tblOwn = "ow";
		String tblType = "tp";
		
		String st = "SELECT * FROM  "+ dbTax +".taxpayortransreceipt " + tblRp + ", " + dbTax + ".taxpayortrans " + tblTrn +
				", " + dbTax + ".taxpayor " + tblOwn + ", " + dbTax + ".landtype " + tblType + " WHERE " +
				tblRp + ".payortransid=" + tblTrn + ".payortransid AND " +
				tblRp + ".payorid=" + tblOwn + ".payorid AND " +
				tblRp + ".landid=" + tblType + ".landid AND " +  tblRp + ".recisactive=1 ";
		
		sql = st + sql;
		
		System.out.println("SQL >> receipt " + sql);
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		//System.out.println("SQL : " + ps.toString());
		rs = ps.executeQuery();
		
		while(rs.next()){
			TaxPayerReceipt rec = new TaxPayerReceipt();
			try{rec.setId(rs.getLong("recid"));}catch(NullPointerException e){}
			try{rec.setLocation(rs.getString("propertylocation"));}catch(NullPointerException e){}
			try{rec.setLotBlockNo(rs.getString("lotblockno"));}catch(NullPointerException e){}
			try{rec.setTaxDecNo(rs.getString("taxdecno"));}catch(NullPointerException e){}
			try{rec.setAssValueLand(rs.getDouble("assvalueland"));}catch(NullPointerException e){}
			try{rec.setAssValueImprv(rs.getDouble("assvalueimprv"));}catch(NullPointerException e){}
			try{rec.setAssValueTotal(rs.getDouble("assvaluetotal"));}catch(NullPointerException e){}
			try{rec.setTaxDue(rs.getDouble("taxdue"));}catch(NullPointerException e){}
			try{rec.setInstallmentRangeAndType(rs.getString("insrangeandtype"));}catch(NullPointerException e){}
			try{rec.setFullPayment(rs.getDouble("fullpayment"));}catch(NullPointerException e){}
			try{rec.setPenaltyPercent(rs.getDouble("penaltypercent"));}catch(NullPointerException e){}
			try{rec.setOverallTotal(rs.getDouble("overalltotal"));}catch(NullPointerException e){}
			try{rec.setIsActive(rs.getInt("recisactive"));}catch(NullPointerException e){}
			try{rec.setTimestamp(rs.getTimestamp("rectimestamp"));}catch(NullPointerException e){}
			try{rec.setIsAdjust(rs.getInt("isAdjust")==1? true : false);}catch(NullPointerException e){}
			try{rec.setIsCase(rs.getInt("iscase")==1? true : false);}catch(NullPointerException e){}
			try{rec.setRemarks(rs.getString("remarks"));}catch(NullPointerException e){}
			try{rec.setLandOwnerName(rs.getString("landownername"));}catch(NullPointerException e){}
			try{UserDtls user = new UserDtls();
			user.setUserdtlsid(rs.getLong("processby"));
			rec.setUserDtls(user);}catch(NullPointerException e){}
			
			TaxPayorTrans trans = new TaxPayorTrans();
			try{trans.setId(rs.getLong("payortransid"));}catch(NullPointerException e){}
			try{trans.setTransDate(rs.getString("payortransdate"));}catch(NullPointerException e){}
			try{trans.setAmount(rs.getDouble("payortransamount"));}catch(NullPointerException e){}
			try{trans.setAmountInWords(rs.getString("payortransamountinwords"));}catch(NullPointerException e){}
			try{trans.setScNo(rs.getString("payorscno"));}catch(NullPointerException e){}
			try{trans.setAccountFormNo(rs.getInt("payoraccntform"));}catch(NullPointerException e){}
			try{trans.setIsactive(rs.getInt("paytransisactive"));}catch(NullPointerException e){}
			try{trans.setCheckNo(rs.getString("paycheckno"));}catch(NullPointerException e){}
			try{trans.setCheckDate(rs.getString("paycheckdate"));}catch(NullPointerException e){}
			try{trans.setSigned1(rs.getString("paysigned1"));}catch(NullPointerException e){}
			try{trans.setSigned2(rs.getString("paysigned2"));}catch(NullPointerException e){}
			try{trans.setTimestamp(rs.getTimestamp("payortranstimestamp"));}catch(NullPointerException e){}
			rec.setPayorTrans(trans);
			
			TaxPayor pay = new TaxPayor();
			try{pay.setId(rs.getLong("payorid"));}catch(NullPointerException e){}
			try{pay.setFullName(rs.getString("payorname"));}catch(NullPointerException e){}
			try{pay.setAddress(rs.getString("payoraddress"));}catch(NullPointerException e){}
			try{pay.setIsactive(rs.getInt("payisactive"));}catch(NullPointerException e){}
			try{pay.setTimestamp(rs.getTimestamp("paytimestamp"));}catch(NullPointerException e){}
			rec.setPayor(pay);
			
			LandType type = new LandType();
			try{type.setId(rs.getInt("landid"));}catch(NullPointerException e){}
			try{type.setLandType(rs.getString("landtype"));}catch(NullPointerException e){}
			try{type.setTimestamp(rs.getTimestamp("landtimestamp"));}catch(NullPointerException e){}
			rec.setLandType(type);
			
			pays.add(rec);
		}
		
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
		
		return pays;
	}
	
	public static List<ITaxPayerReceipt> retrieveLand(String sql, String[] params){
		List<ITaxPayerReceipt> pays = new ArrayList<ITaxPayerReceipt>();//Collections.synchronizedList(new ArrayList<ITaxPayerReceipt>());
		
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		//System.out.println("SQL : " + ps.toString());
		rs = ps.executeQuery();
		
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		while(rs.next()){
			try{ITaxPayerReceipt rec = new TaxPayerReceipt();
			String sql1 = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid AND l.payorid=? LIMIT 1";
			String[] params1 = new String[1];
			params1[0] = rs.getLong("payorid")+"";
			LandPayor land = LandPayor.retrieve(sql1, params1).get(0); 
			rec.setLandPayor(land);
			pays.add(rec);}catch(Exception e){}
		}
		

		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException sl){}
	
		return pays;
	}	
		
	public static ITaxPayerReceipt save(ITaxPayerReceipt pay){
		if(pay!=null){
			long id = getInfo(pay.getId()==0? getLatestId()+1 : pay.getId());
			if(id==1){
				pay = TaxPayerReceipt.insertData(pay, "1");
			}else if(id==2){
				pay = TaxPayerReceipt.updateData(pay);
			}else if(id==3){
				pay = TaxPayerReceipt.insertData(pay, "3");
			}
			
		}
		return pay;
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
	
public static ITaxPayerReceipt insertData(ITaxPayerReceipt pay, String type){
		
		String sql = "INSERT INTO taxpayortransreceipt ("
				+ "recid,"
				+ "propertylocation,"
				+ "lotblockno,"
				+ "taxdecno,"
				+ "assvalueland,"
				+ "assvalueimprv,"
				+ "assvaluetotal,"
				+ "taxdue,"
				+ "insrangeandtype,"
				+ "fullpayment,"
				+ "penaltypercent,"
				+ "overalltotal,"
				+ "recisactive,"
				+ "payorid,"
				+ "payortransid,"
				+ "landid,"
				+ "isAdjust,"
				+ "remarks,"
				+ "processby,"
				+ "landownername,"
				+ "iscase) " 
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		Connection conn = null;
		try{
		LogU.add("Insert into table taxpayortransreceipt");	
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		long id =1;
		if("1".equalsIgnoreCase(type)){
			ps.setLong(1, id);
			pay.setId(id);
			LogU.add(id);
		}else if("3".equalsIgnoreCase(type)){
			id=getLatestId()+1;
			ps.setLong(1, id);
			pay.setId(id);
			LogU.add(id);
		}
		ps.setString(2, pay.getLocation());
		ps.setString(3, pay.getLotBlockNo());
		ps.setString(4, pay.getTaxDecNo());
		ps.setDouble(5, pay.getAssValueLand());
		ps.setDouble(6, pay.getAssValueImprv());
		ps.setDouble(7, pay.getAssValueTotal());
		ps.setDouble(8, pay.getTaxDue());
		ps.setString(9, pay.getInstallmentRangeAndType());
		ps.setDouble(10, pay.getFullPayment());
		ps.setDouble(11, pay.getPenaltyPercent());
		ps.setDouble(12, pay.getOverallTotal());
		ps.setInt(13, pay.getIsActive());
		ps.setLong(14, pay.getPayor()==null? 0 : pay.getPayor().getId());
		ps.setLong(15, pay.getPayorTrans()==null? 0 : pay.getPayorTrans().getId());
		ps.setLong(16, pay.getLandType()==null? 0 : pay.getLandType().getId());
		ps.setInt(17, pay.getIsAdjust()==true? 1 : 0);
		ps.setString(18, pay.getRemarks());
		ps.setLong(19, pay.getUserDtls()==null? 0 : pay.getUserDtls().getUserdtlsid());
		ps.setString(20, pay.getLandOwnerName());
		ps.setInt(21, pay.getIsCase()==true? 1 : 0);
		
		LogU.add(pay.getLocation());
		LogU.add(pay.getLotBlockNo());
		LogU.add(pay.getTaxDecNo());
		LogU.add(pay.getAssValueLand());
		LogU.add(pay.getAssValueImprv());
		LogU.add(pay.getAssValueTotal());
		LogU.add(pay.getTaxDue());
		LogU.add(pay.getInstallmentRangeAndType());
		LogU.add(pay.getFullPayment());
		LogU.add(pay.getPenaltyPercent());
		LogU.add(pay.getOverallTotal());
		LogU.add(pay.getIsActive());
		LogU.add(pay.getPayor()==null? 0 : pay.getPayor().getId());
		LogU.add(pay.getPayorTrans()==null? 0 : pay.getPayorTrans().getId());
		LogU.add(pay.getLandType()==null? 0 : pay.getLandType().getId());
		LogU.add(pay.getIsAdjust()==true? 1 : 0);
		LogU.add(pay.getRemarks());
		LogU.add(pay.getUserDtls()==null? 0 : pay.getUserDtls().getUserdtlsid());
		LogU.add(pay.getLandOwnerName());
		LogU.add(pay.getIsCase()==true? 1 : 0);
		ps.execute();
		ps.close();
		TaxDatabaseConnect.close(conn);
		LogU.add("Insert into table taxpayortransreceipt successfully.");
		}catch(SQLException s){
			s.getMessage();
			LogU.add("Error Insert into table taxpayortransreceipt.");
		}
		
		return pay;
	}
	
public void insertData(String type){
	
	String sql = "INSERT INTO taxpayortransreceipt ("
			+ "recid,"
			+ "propertylocation,"
			+ "lotblockno,"
			+ "taxdecno,"
			+ "assvalueland,"
			+ "assvalueimprv,"
			+ "assvaluetotal,"
			+ "taxdue,"
			+ "insrangeandtype,"
			+ "fullpayment,"
			+ "penaltypercent,"
			+ "overalltotal,"
			+ "recisactive,"
			+ "payorid,"
			+ "payortransid,"
			+ "landid,"
			+ "isAdjust,"
			+ "remarks,"
			+ "processby,"
			+ "landownername,"
			+ "iscase) " 
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	PreparedStatement ps = null;
	Connection conn = null;
	try{
	LogU.add("Insert into table taxpayortransreceipt");	
	conn = TaxDatabaseConnect.getConnection();
	ps = conn.prepareStatement(sql);
	long id =1;
	if("1".equalsIgnoreCase(type)){
		ps.setLong(1, id);
		setId(id);
		LogU.add(id);
	}else if("3".equalsIgnoreCase(type)){
		id=getLatestId()+1;
		ps.setLong(1, id);
		setId(id);
		LogU.add(id);
	}
	ps.setString(2, getLocation());
	ps.setString(3, getLotBlockNo());
	ps.setString(4, getTaxDecNo());
	ps.setDouble(5, getAssValueLand());
	ps.setDouble(6, getAssValueImprv());
	ps.setDouble(7, getAssValueTotal());
	ps.setDouble(8, getTaxDue());
	ps.setString(9, getInstallmentRangeAndType());
	ps.setDouble(10, getFullPayment());
	ps.setDouble(11, getPenaltyPercent());
	ps.setDouble(12, getOverallTotal());
	ps.setInt(13, getIsActive());
	ps.setLong(14, getPayor()==null? 0 : getPayor().getId());
	ps.setLong(15, getPayorTrans()==null? 0 : getPayorTrans().getId());
	ps.setLong(16, getLandType()==null? 0 : getLandType().getId());
	ps.setInt(17, getIsAdjust()==true? 1 : 0);
	ps.setString(18, getRemarks());
	ps.setLong(19, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
	ps.setString(20, getLandOwnerName());
	ps.setInt(21, getIsCase()==true? 1 : 0);
	
	LogU.add(getLocation());
	LogU.add(getLotBlockNo());
	LogU.add(getTaxDecNo());
	LogU.add(getAssValueLand());
	LogU.add(getAssValueImprv());
	LogU.add(getAssValueTotal());
	LogU.add(getTaxDue());
	LogU.add(getInstallmentRangeAndType());
	LogU.add(getFullPayment());
	LogU.add(getPenaltyPercent());
	LogU.add(getOverallTotal());
	LogU.add(getIsActive());
	LogU.add(getPayor()==null? 0 : getPayor().getId());
	LogU.add(getPayorTrans()==null? 0 : getPayorTrans().getId());
	LogU.add(getLandType()==null? 0 : getLandType().getId());
	LogU.add(getIsAdjust()==true? 1 : 0);
	LogU.add(getRemarks());
	LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
	LogU.add(getLandOwnerName());
	LogU.add(getIsCase()==true? 1 : 0);
	
	ps.execute();
	ps.close();
	TaxDatabaseConnect.close(conn);
	LogU.add("Insert into table taxpayortransreceipt successfully.");
	}catch(SQLException s){
		s.getMessage();
		LogU.add("Error Insert into table taxpayortransreceipt.");
	}

}

public static ITaxPayerReceipt updateData(ITaxPayerReceipt pay){
	
	String sql = "UPDATE taxpayortransreceipt SET "
			+ "propertylocation=?,"
			+ "lotblockno=?,"
			+ "taxdecno=?,"
			+ "assvalueland=?,"
			+ "assvalueimprv=?,"
			+ "assvaluetotal=?,"
			+ "taxdue=?,"
			+ "insrangeandtype=?,"
			+ "fullpayment=?,"
			+ "penaltypercent=?,"
			+ "overalltotal=?,"
			+ "payorid=?,"
			+ "payortransid=?,"
			+ "landid=?,"
			+ "isAdjust=?,"
			+ "remarks=?,"
			+ "processby=?,"
			+ "landownername=?,"
			+ "iscase=? " 
			+ " WHERE recid=?";
	
	PreparedStatement ps = null;
	Connection conn = null;
	try{
	LogU.add("updating into table taxpayortransreceipt");	
	conn = TaxDatabaseConnect.getConnection();
	ps = conn.prepareStatement(sql);
	
	ps.setString(1, pay.getLocation());
	ps.setString(2, pay.getLotBlockNo());
	ps.setString(3, pay.getTaxDecNo());
	ps.setDouble(4, pay.getAssValueLand());
	ps.setDouble(5, pay.getAssValueImprv());
	ps.setDouble(6, pay.getAssValueTotal());
	ps.setDouble(7, pay.getTaxDue());
	ps.setString(8, pay.getInstallmentRangeAndType());
	ps.setDouble(9, pay.getFullPayment());
	ps.setDouble(10, pay.getPenaltyPercent());
	ps.setDouble(11, pay.getOverallTotal());
	ps.setLong(12, pay.getPayor()==null? 0 : pay.getPayor().getId());
	ps.setLong(13, pay.getPayorTrans()==null? 0 : pay.getPayorTrans().getId());
	ps.setLong(14, pay.getLandType()==null? 0 : pay.getLandType().getId());
	ps.setInt(15, pay.getIsAdjust()==true? 1 : 0);
	ps.setString(16, pay.getRemarks());
	ps.setLong(17, pay.getUserDtls()==null? 0 : pay.getUserDtls().getUserdtlsid());
	ps.setString(18, pay.getLandOwnerName());
	ps.setInt(19, pay.getIsCase()==true? 1 : 0);
	ps.setLong(20, pay.getId());
	
	LogU.add(pay.getLocation());
	LogU.add(pay.getLotBlockNo());
	LogU.add(pay.getTaxDecNo());
	LogU.add(pay.getAssValueLand());
	LogU.add(pay.getAssValueImprv());
	LogU.add(pay.getAssValueTotal());
	LogU.add(pay.getTaxDue());
	LogU.add(pay.getInstallmentRangeAndType());
	LogU.add(pay.getFullPayment());
	LogU.add(pay.getPenaltyPercent());
	LogU.add(pay.getOverallTotal());
	LogU.add(pay.getPayor()==null? 0 : pay.getPayor().getId());
	LogU.add(pay.getPayorTrans()==null? 0 : pay.getPayorTrans().getId());
	LogU.add(pay.getLandType()==null? 0 : pay.getLandType().getId());
	LogU.add(pay.getIsAdjust()==true? 1 : 0);
	LogU.add(pay.getRemarks());
	LogU.add(pay.getUserDtls()==null? 0 : pay.getUserDtls().getUserdtlsid());
	LogU.add(pay.getLandOwnerName());
	LogU.add(pay.getIsCase()==true? 1 : 0);
	LogU.add(pay.getId());
	
	ps.execute();
	ps.close();
	TaxDatabaseConnect.close(conn);
	LogU.add("updating into table taxpayortransreceipt successfully.");
	}catch(SQLException s){
		s.getMessage();
		LogU.add("Error updating into table taxpayortransreceipt. " + s.getStackTrace());
	}
	
	return pay;
}

public void updateData(){
	
	String sql = "UPDATE taxpayortransreceipt SET "
			+ "propertylocation=?,"
			+ "lotblockno=?,"
			+ "taxdecno=?,"
			+ "assvalueland=?,"
			+ "assvalueimprv=?,"
			+ "assvaluetotal=?,"
			+ "taxdue=?,"
			+ "insrangeandtype=?,"
			+ "fullpayment=?,"
			+ "penaltypercent=?,"
			+ "overalltotal=?,"
			+ "payorid=?,"
			+ "payortransid=?,"
			+ "landid=?,"
			+ "isAdjust=?,"
			+ "remarks=?,"
			+ "processby=?,"
			+ "landownername=?,"
			+ "iscase=? " 
			+ " WHERE recid=?";
	
	PreparedStatement ps = null;
	Connection conn = null;
	try{
	LogU.add("updating into table taxpayortransreceipt");	
	conn = TaxDatabaseConnect.getConnection();
	ps = conn.prepareStatement(sql);
	
	ps.setString(1, getLocation());
	ps.setString(2, getLotBlockNo());
	ps.setString(3, getTaxDecNo());
	ps.setDouble(4, getAssValueLand());
	ps.setDouble(5, getAssValueImprv());
	ps.setDouble(6, getAssValueTotal());
	ps.setDouble(7, getTaxDue());
	ps.setString(8, getInstallmentRangeAndType());
	ps.setDouble(9, getFullPayment());
	ps.setDouble(10, getPenaltyPercent());
	ps.setDouble(11, getOverallTotal());
	ps.setLong(12, getPayor()==null? 0 : getPayor().getId());
	ps.setLong(13, getPayorTrans()==null? 0 : getPayorTrans().getId());
	ps.setLong(14, getLandType()==null? 0 : getLandType().getId());
	ps.setInt(15, getIsAdjust()==true? 1 : 0);
	ps.setString(16, getRemarks());
	ps.setLong(17, getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
	ps.setString(18, getLandOwnerName());
	ps.setInt(19, getIsCase()==true? 1 : 0);
	ps.setLong(20, getId());
	
	
	LogU.add(getLocation());
	LogU.add(getLotBlockNo());
	LogU.add(getTaxDecNo());
	LogU.add(getAssValueLand());
	LogU.add(getAssValueImprv());
	LogU.add(getAssValueTotal());
	LogU.add(getTaxDue());
	LogU.add(getInstallmentRangeAndType());
	LogU.add(getFullPayment());
	LogU.add(getPenaltyPercent());
	LogU.add(getOverallTotal());
	LogU.add(getPayor()==null? 0 : getPayor().getId());
	LogU.add(getPayorTrans()==null? 0 : getPayorTrans().getId());
	LogU.add(getLandType()==null? 0 : getLandType().getId());
	LogU.add(getIsAdjust()==true? 1 : 0);
	LogU.add(getRemarks());
	LogU.add(getUserDtls()==null? 0 : getUserDtls().getUserdtlsid());
	LogU.add(getLandOwnerName());
	LogU.add(getIsCase()==true? 1 : 0);
	LogU.add(getId());
	
	ps.execute();
	ps.close();
	TaxDatabaseConnect.close(conn);
	LogU.add("updating into table taxpayortransreceipt successfully.");
	}catch(SQLException s){
		s.getMessage();
		LogU.add("Error updating into table taxpayortransreceipt. " + s.getStackTrace());
	}
}

	public static long getLatestId(){
		long id =0;
		Connection conn = null;
		PreparedStatement prep = null;
		ResultSet rs = null;
		String sql = "";
		try{
		sql="SELECT recid FROM taxpayortransreceipt  ORDER BY recid DESC LIMIT 1";	
		conn = TaxDatabaseConnect.getConnection();
		prep = conn.prepareStatement(sql);	
		rs = prep.executeQuery();
		
		while(rs.next()){
			id = rs.getLong("recid");
		}
		
		rs.close();
		prep.close();
		TaxDatabaseConnect.close(conn);
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
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement("SELECT recid FROM taxpayortransreceipt WHERE recid=?");
		ps.setLong(1, id);
		rs = ps.executeQuery();
		
		if(rs.next()){
			result=true;
		}
		
		rs.close();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static void delete(String sql, String[] params){
			
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
		}catch(SQLException s){
			s.getMessage();
		}
		
	}
	
	@Override
	public void delete(){
		String sql = "update taxpayortransreceipt set recisactive=0 WHERE recid=?";
		String params[] = new String[1];
		params[0] = getId()+"";
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		LogU.add("Deleting data in taxpayortransreceipt where id="+getId());		
		conn = TaxDatabaseConnect.getConnection();
		ps = conn.prepareStatement(sql);
		
		if(params!=null && params.length>0){
			
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
			
		}
		ps.executeUpdate();
		ps.close();
		TaxDatabaseConnect.close(conn);
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
	public String getLocation() {
		// TODO Auto-generated method stub
		return location;
	}
	@Override
	public void setLocation(String location) {
		// TODO Auto-generated method stub
		this.location = location;
	}
	@Override
	public String getLotBlockNo() {
		// TODO Auto-generated method stub
		return lotBlockNo;
	}
	@Override
	public void setLotBlockNo(String lotBlockNo) {
		// TODO Auto-generated method stub
		this.lotBlockNo = lotBlockNo;
	}
	@Override
	public String getTaxDecNo() {
		// TODO Auto-generated method stub
		return taxdecno;
	}
	@Override
	public void setTaxDecNo(String taxDecNo) {
		// TODO Auto-generated method stub
		this.taxdecno = taxDecNo;
	}
	@Override
	public double getAssValueLand() {
		// TODO Auto-generated method stub
		return assValueLand;
	}
	@Override
	public void setAssValueLand(double assValueLand) {
		// TODO Auto-generated method stub
		this.assValueLand = assValueLand;
	}
	@Override
	public double getAssValueImprv() {
		// TODO Auto-generated method stub
		return assValueImprv;
	}
	@Override
	public void setAssValueImprv(double assValueImprv) {
		// TODO Auto-generated method stub
		this.assValueImprv = assValueImprv;
	}
	@Override
	public double getAssValueTotal() {
		// TODO Auto-generated method stub
		return assValueTotal;
	}
	@Override
	public void setAssValueTotal(double assValueTotal) {
		// TODO Auto-generated method stub
		this.assValueTotal = assValueTotal;
	}
	@Override
	public double getTaxDue() {
		// TODO Auto-generated method stub
		return taxDue;
	}
	@Override
	public void setTaxDue(double taxDue) {
		// TODO Auto-generated method stub
		this.taxDue = taxDue;
	}
	@Override
	public String getInstallmentRangeAndType() {
		// TODO Auto-generated method stub
		return installmentRangeAndType;
	}
	@Override
	public void setInstallmentRangeAndType(String installmentRangeAndType) {
		// TODO Auto-generated method stub
		this.installmentRangeAndType = installmentRangeAndType;
	}
	@Override
	public double getFullPayment() {
		// TODO Auto-generated method stub
		return fullPayment;
	}
	@Override
	public void setFullPayment(double fullPayment) {
		// TODO Auto-generated method stub
		this.fullPayment = fullPayment;
	}
	@Override
	public double getPenaltyPercent() {
		// TODO Auto-generated method stub
		return penaltyPercent;
	}
	@Override
	public void setPenaltyPercent(double penaltyPercent) {
		// TODO Auto-generated method stub
		this.penaltyPercent = penaltyPercent;
	}
	@Override
	public double getOverallTotal() {
		// TODO Auto-generated method stub
		return overallTotal;
	}
	@Override
	public void setOverallTotal(double overallTotal) {
		// TODO Auto-generated method stub
		this.overallTotal = overallTotal;
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
	public ITaxPayor getPayor() {
		// TODO Auto-generated method stub
		return payor;
	}
	@Override
	public void setPayor(ITaxPayor payor) {
		// TODO Auto-generated method stub
		this.payor = payor;
	}
	@Override
	public ITaxPayorTrans getPayorTrans() {
		// TODO Auto-generated method stub
		return payorTrans;
	}
	@Override
	public void setPayorTrans(ITaxPayorTrans payorTrans) {
		// TODO Auto-generated method stub
		this.payorTrans = payorTrans;
	}
	@Override
	public Timestamp getTimestamp() {
		// TODO Auto-generated method stub
		return timestamp;
	}
	@Override
	public void setTimestamp(Timestamp timestamp) {
		// TODO Auto-generated method stub
		this.timestamp = timestamp;
	}

	@Override
	public ILandType getLandType() {
		// TODO Auto-generated method stub
		return landType;
	}

	@Override
	public void setLandType(ILandType landType) {
		// TODO Auto-generated method stub
		this.landType = landType;
	}
	
	public static void main(String[] args) {
	
		ITaxPayerReceipt tax = new TaxPayerReceipt();
		tax.setLocation("lamdalag ugon");
		tax.setLotBlockNo("123 street lamdalag");
		tax.setTaxDecNo("sft 1224214");
		double  amnt = 0;//new BigDecimal("100.00");
		ILandType land = new LandType();
		land.setId(1);
		tax.setLandType(land);
		tax.setAssValueLand(amnt);
		tax.setAssValueImprv(amnt);
		tax.setAssValueTotal(amnt);
		
		tax.setTaxDue(amnt);
		tax.setInstallmentRangeAndType("2014-2016 Basic");
		tax.setFullPayment(amnt);
		tax.setPenaltyPercent(amnt);
		tax.setOverallTotal(amnt);
		tax.setIsActive(1);
		
		ITaxPayor payor = new TaxPayor();
		payor.setId(1);
		tax.setPayor(payor);
		
		ITaxPayorTrans trans = new TaxPayorTrans();
		trans.setId(1);
		tax.setPayorTrans(trans);
		tax.setId(1);
		tax.save();
		
		
		
		
	}

	@Override
	public int getCnt() {
		// TODO Auto-generated method stub
		return cnt;
	}

	@Override
	public void setCnt(int cnt) {
		// TODO Auto-generated method stub
		this.cnt = cnt;
	}

	@Override
	public String getFromYear() {
		// TODO Auto-generated method stub
		return fromYear;
	}

	@Override
	public void setFromYear(String fromYear) {
		// TODO Auto-generated method stub
		this.fromYear = fromYear;
	}

	@Override
	public String getToYear() {
		// TODO Auto-generated method stub
		return toYear;
	}

	@Override
	public void setToYear(String toYear) {
		// TODO Auto-generated method stub
		this.toYear = toYear;
	}

	@Override
	public String getInstallmentType() {
		// TODO Auto-generated method stub
		return installmentType;
	}

	@Override
	public void setInstallmentType(String installmentType) {
		// TODO Auto-generated method stub
		this.installmentType = installmentType;
	}

	@Override
	public boolean getIsAdjust() {
		// TODO Auto-generated method stub
		return isAdjust;
	}

	@Override
	public void setIsAdjust(boolean isAdjust) {
		// TODO Auto-generated method stub
		this.isAdjust = isAdjust;
	}

	@Override
	public String getRemarks() {
		// TODO Auto-generated method stub
		return remarks;
	}

	@Override
	public void setRemarks(String remarks) {
		// TODO Auto-generated method stub
		this.remarks = remarks;
	}

	@Override
	public UserDtls getUserDtls() {
		// TODO Auto-generated method stub
		return userDtls;
	}

	@Override
	public void setUserDtls(UserDtls userDtls) {
		// TODO Auto-generated method stub
		this.userDtls = userDtls;
	}

	@Override
	public String getLandOwnerName() {
		// TODO Auto-generated method stub
		return landOwnerName;
	}

	@Override
	public void setLandOwnerName(String landOwnerName) {
		// TODO Auto-generated method stub
		this.landOwnerName = landOwnerName;
	}

	@Override
	public LandPayor getLandPayor() {
		// TODO Auto-generated method stub
		return landPayor;
	}

	@Override
	public void setLandPayor(LandPayor landPayor) {
		// TODO Auto-generated method stub
		this.landPayor = landPayor;
	}

	@Override
	public boolean getIsCase() {
		// TODO Auto-generated method stub
		return isCase;
	}

	@Override
	public void setIsCase(boolean isCase) {
		// TODO Auto-generated method stub
		this.isCase = isCase;
	}
	
}
