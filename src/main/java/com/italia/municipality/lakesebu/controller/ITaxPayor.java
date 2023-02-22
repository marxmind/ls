package com.italia.municipality.lakesebu.controller;

import java.sql.Timestamp;
import java.util.List;

/**
 * 
 * @author mark italia
 * @since 11/17/2013
 * @version 1.0
 */
public interface ITaxPayor {

	long getId();
	void setId(long id);
	String getFullName();
	void setFullName(String fullName);
	String getAddress();
	void setAddress(String address);
	int getIsactive();
	void setIsactive(int isactive);
	Timestamp getTimestamp();
	void setTimestamp(Timestamp timestamp);
	Barangay getBarangay();
	void setBarangay(Barangay barangay);
	UserDtls getUserDtls();
	void setUserDtls(UserDtls userDtls);
	List<LandPayor> getLandPayor();
	void setLandPayor(List<LandPayor> landPayor);
	
	void save();
	static ITaxPayor save(ITaxPayor pay) {
		return pay;
	}
	static ITaxPayor insertData(ITaxPayor pay, String type){
		return pay;
	}
	static ITaxPayor updateData(ITaxPayor pay){
		return pay;
	}
	void delete();
	
}
