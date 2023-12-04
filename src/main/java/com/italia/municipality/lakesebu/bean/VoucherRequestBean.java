package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.italia.municipality.lakesebu.controller.VoucherRequest;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import lombok.Data;

@Named("vrreq")
@Data
public class VoucherRequestBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1345695875354354L;
	
	private VoucherRequest vrData;
	private List<VoucherRequest> vrs;
	
	
	@PostConstruct
	public void init() {
		vrs = new ArrayList<VoucherRequest>();
	}
	
	public void save() {
		boolean isOk = true;
		VoucherRequest rq = getVrData();
		if(rq.getId()==0) {
			rq = new VoucherRequest();
			rq.setIsActive(1);
		}
		
		if(rq!=null) {
			
			if(rq.getRequestorName()==null || rq.getRequestorName().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide requestor name");
				isOk = false;
			}
			
			if(rq.getContactNo()==null || rq.getContactNo().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide contact number");
				isOk = false;
			}
			
			if(rq.getAmountRequested()==0) {
				Application.addMessage(3, "Error", "Please provide requested amount");
				isOk = false;
			}
			
		}
		
		if(isOk) {
		
			rq.setDateTrans(DateUtils.convertDate(rq.getTmpData(), "yyyy-MM-dd"));
			rq.save();
			
		}
	}
	
	public void clear() {
		
	}
	
	public void delete() {
		
	}
	
}
