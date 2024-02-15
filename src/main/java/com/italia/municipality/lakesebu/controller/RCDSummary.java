package com.italia.municipality.lakesebu.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RCDSummary {
	
	private long id;
	private String tableName;
	private String dateTrans;
	private String controlNumber;
	private String colIds;
	private int isActive;
	private String fundName;
	private String individualSeries;
	private double amount;
	private String collectorIDs;
	private Object objectData;
	
	public static List<RCDSummary> retrieve(String ids,String colIds){
		List<RCDSummary> sums = new ArrayList<RCDSummary>();
		RCDSummary sum = new RCDSummary();
		ResultSet rs = OpenTableAccess.query("SELECT * FROM rcdallcontroller WHERE isactiveall=1 AND fundtype=1 AND isds like '%"+ ids +"%' AND colids like '%"+ colIds +"%'", new String[0], new WebTISDatabaseConnect());
		
		if(rs!=null) {
		try {
			while(rs.next()) {
				RCDAllController obj = RCDAllController.builder()
						.id(rs.getLong("allid"))
						.dateTrans(rs.getString("alldatetrans"))
						.controlNumber(rs.getString("controlnumber"))
						.collectionIds(rs.getString("colids"))
						.isActive(rs.getInt("isactiveall"))
						.fundType(rs.getInt("fundtype"))
						.individualSeries(rs.getString("indrptseries"))
						.amount(rs.getDouble("amount"))
						.index(rs.getInt("indexid"))
						.isids(rs.getString("isds"))
						.fundName(FundType.typeName(rs.getInt("fundtype")))
						.build();
				
				sum = RCDSummary.builder()
						.id(obj.getId())
						.tableName("RCD ALL")
						.dateTrans(obj.getDateTrans())
						.controlNumber(obj.getControlNumber())
						.colIds(obj.getCollectionIds())
						.isActive(obj.getIsActive())
						.fundName(obj.getFundName())
						.individualSeries(obj.getIndividualSeries())
						.amount(obj.getAmount())
						.collectorIDs(obj.getIsids())
						.objectData(obj)
						.build();
				
				sums.add(sum);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		rs = OpenTableAccess.query("SELECT * FROM rcdsummarycontroller WHERE isactivesum=1 AND fundtype=1 AND isds like '%"+ ids +"%' AND colids like '%"+ colIds +"%'", new String[0], new WebTISDatabaseConnect());
		
		if(rs!=null) {
		try {
			while(rs.next()) {
				RCDSummaryController obj = RCDSummaryController.builder()
						.id(rs.getLong("sumid"))
						.dateTrans(rs.getString("sumdatetrans"))
						.controlNumber(rs.getString("controlnumber"))
						.collectionIds(rs.getString("colids"))
						.isActive(rs.getInt("isactivesum"))
						.fundType(rs.getInt("fundtype"))
						.individualSeries(rs.getString("indrptseries"))
						.amount(rs.getDouble("amount"))
						.isids(rs.getString("isds"))
						.fundName(FundType.typeName(rs.getInt("fundtype")))
						.build();
				
				sum = RCDSummary.builder()
						.id(obj.getId())
						.tableName("RCD SUMMARY")
						.dateTrans(obj.getDateTrans())
						.controlNumber(obj.getControlNumber())
						.colIds(obj.getCollectionIds())
						.isActive(obj.getIsActive())
						.fundName(obj.getFundName())
						.individualSeries(obj.getIndividualSeries())
						.amount(obj.getAmount())
						.collectorIDs(obj.getIsids())
						.objectData(obj)
						.build();
				
				sums.add(sum);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		
		return sums;
	}
	
}
