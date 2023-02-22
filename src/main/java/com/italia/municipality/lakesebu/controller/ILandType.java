package com.italia.municipality.lakesebu.controller;

import java.sql.Timestamp;

public interface ILandType {
	int getId();
	void setId(int id);
	String getLandType();
	void setLandType(String landType);
	Timestamp getTimestamp();
	void setTimestamp(Timestamp timestamp);
}
