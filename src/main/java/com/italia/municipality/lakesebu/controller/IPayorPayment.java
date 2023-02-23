package com.italia.municipality.lakesebu.controller;

import java.util.ArrayList;
import java.util.List;

public interface IPayorPayment extends IBaseMethod {
	static List<PayorPayment> retrieve(String sql, String[] params){return new ArrayList<PayorPayment>();}
	static PayorPayment save(PayorPayment pay) { return pay;}
	static PayorPayment insertData(PayorPayment pay, String type) {return pay;}
	static PayorPayment updateData(PayorPayment pay) {return pay;}
	
}
