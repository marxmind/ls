package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BusinessMapping {
	
	private int number;
	private long id;
	private String dateTrans;
	private String name;
	private String owner;
	private String lineOfBusiness;
	private String rented;
	private String sitios;
	private String barangay;
	private String hasPermit;
	private String mapLocation;
	private String latitude;
	private String longtitude;
	private String altitude;
	private String precision;
	private String pictureOfOwner;
	private String pictureOfBusiness;
	private String remarks;
	private int isActive;
	private String buildingOwnerNamer;
	private double amountRented;
	private String mapBy;
	
	private int count;
	private int countWithPermit;
	private int countNoPermit;
	private String address;
	
	public static List<BusinessMapping> retrieve(String sql, String[] params){
		List<BusinessMapping> trans = new ArrayList<BusinessMapping>();
		
		String sqlAdd = "SELECT * FROM businessmapping  WHERE  isactivemap=1 ";
		
		sql = sqlAdd + sql;
		
		
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
		System.out.println("businessmapping SQL " + ps.toString());
		rs = ps.executeQuery();
		int num = 1;
		while(rs.next()){
			
			String name = "";
			String owner = "";
			String address = "";
			try {name = rs.getString("name").toUpperCase();}catch(NullPointerException e){}
			try {owner = rs.getString("owner").toUpperCase();}catch(NullPointerException e){}
			address = rs.getString("sitios").isEmpty()? "" : rs.getString("sitios") + ", ";
			address += rs.getString("barangay");
			address = address.toUpperCase();
			
			BusinessMapping mk = BusinessMapping.builder()
					.number(num++)
					.id(rs.getLong("bzid"))
					.dateTrans(rs.getString("datereported"))
					.name(name)
					.owner(owner)
					.lineOfBusiness(rs.getString("lineofbusiness"))
					.rented(rs.getString("rented"))
					.sitios(rs.getString("sitios"))
					.barangay(rs.getString("barangay"))
					.hasPermit(rs.getString("haspermit"))
					.mapLocation(rs.getString("maplocation"))
					.latitude(rs.getString("latitude"))
					.longtitude(rs.getString("longitude"))
					.altitude(rs.getString("altitude"))
					.precision(rs.getString("prcision"))
					.pictureOfOwner(rs.getString("pictureowner"))
					.pictureOfBusiness(rs.getString("picturebusiness"))
					.remarks(rs.getString("remarks"))
					.isActive(rs.getInt("isactivemap"))
					.buildingOwnerNamer(rs.getString("buildingowner"))
					.amountRented(rs.getDouble("rentamount"))
					.mapBy(rs.getString("mapby"))
					.address(address)
					.build();
			
			trans.add(mk);
			
		}
		
		rs.close();
		ps.close();
		WebTISDatabaseConnect.close(conn);
		}catch(Exception e){e.getMessage();}
		
		return trans;
	}
}
