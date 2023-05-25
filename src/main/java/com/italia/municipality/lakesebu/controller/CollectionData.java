package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class CollectionData {
	
	private static Connection conn;
	
	public static void main(String[] args)  {
		loadDBConnection();inquire("2023-03-01","2023-03-31");
	}
	
	public static void loadDBConnection() {
		try {
			conn = null;
			String driver = "org.mariadb.jdbc.Driver";
			Class.forName(driver);
			String url = "jdbc:mariadb://localhost:3306/webtis?serverTimezone=UTC&useSSL=false";
			String u_name = "root";
			String pword = "october181986*";
			conn = DriverManager.getConnection(url, u_name, pword);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void inquire(String fromDate, String toDate) {
		
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		Map<Integer, Collector> mapCollector = new LinkedHashMap<Integer, Collector>();
		
		try {
		/*	
		ps = conn.prepareStatement("select * from issuedcollector");
		rs = ps.executeQuery();
		
		while(rs.next()) {
			Collector col = Collector.builder()
					.id(rs.getInt("isid"))
					.name(rs.getString("collectorname"))
					.department(Department.builder().depid(rs.getInt("departmentid")).build())
					.isActive(rs.getInt("isactivecollector"))
					.build();
			mapCollector.put(col.getId(), col);
		}
		
		
		
		ps = conn.prepareStatement("SELECT * FROM taxaccntgroup WHERE accisactive=1");
		rs = ps.executeQuery();
		Map<Long, TaxAccountGroup> mapAccountGroup = new LinkedHashMap<Long, TaxAccountGroup>();
		while(rs.next()) {
			TaxAccountGroup tax = TaxAccountGroup.builder()
					.id(rs.getLong("accid"))
					.name(rs.getString("accname"))
					.isActive(rs.getInt("accisactive"))
					.build();
			mapAccountGroup.put(tax.getId(), tax);
		}
		*/
		ps = conn.prepareStatement("SELECT * FROM paymentname  WHERE  isactivepy=1");
		rs = ps.executeQuery();
		Map<Long, PaymentName> mapPay = new LinkedHashMap<Long, PaymentName>();
		while(rs.next()) {
			PaymentName name = PaymentName.builder()
					.id(rs.getLong("pyid"))
					.dateTrans(rs.getString("pydatetrans"))
					.accountingCode(rs.getString("pyaccntcode"))
					.name(rs.getString("pyname"))
					.amount(rs.getDouble("pyamount"))
					.isActive(rs.getInt("isactivepy"))
					.description(rs.getString("description"))
					.taxGroupId(rs.getLong("accntgrpid"))
					.build();
			
			mapPay.put(name.getId(), name);
		}
		
		ps = conn.prepareStatement("SELECT pyid,sum(olamount) as amount FROM ornamelist WHERE isactiveol=1 AND (DATE(timestampol)>='"+ fromDate +"' AND DATE(timestampol)<='"+ toDate +"' ) group by pyid");
		
		rs = ps.executeQuery();
		Map<Long, ORNameList> mapOR = new LinkedHashMap<Long, ORNameList>();
		while(rs.next()) {
			String name = "";
			long id = rs.getLong("pyid");
			if(mapPay.containsKey(id)) {name=mapPay.get(id).getName();}
			ORNameList or = ORNameList.builder()
					.id(id)
					.paymentName(PaymentName.builder().id(id).name(name).build())
					.amount(rs.getDouble("amount"))
					.build();
			
			mapOR.put(or.getId(), or);
		}
		
		rs.close();
		ps.close();
		conn.close();
		
		
		/*
		for(Collector c : mapCollector.values()) {
			System.out.println("Name: " + c.getName());
		}
		for(TaxAccountGroup g : mapAccountGroup.values()) {
			System.out.println("Group: " + g.getName());
		}
		for(PaymentName p : mapPay.values()) {
			System.out.println("Payname: " + p.getName());
		}*/
		double total = 0d;
		for(ORNameList o : mapOR.values()) {
			System.out.println(o.getPaymentName().getName() + "\t\t" + o.getAmount());
			total += o.getAmount();
		}
		System.out.println("Total: " + total);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
