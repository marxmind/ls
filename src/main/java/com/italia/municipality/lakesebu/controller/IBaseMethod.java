package com.italia.municipality.lakesebu.controller;

public interface IBaseMethod {
	
	void save();
	void insertData(String type);
	void updateData();
	
	static long getLatestId() {return 0;}
	static long getInfo(long id) {return id;}
	static boolean isIdNoExist(long id) {return false;}
	
	void delete();
	static void delete(String sql, String[] params) {}
}
