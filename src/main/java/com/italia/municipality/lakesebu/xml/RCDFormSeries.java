package com.italia.municipality.lakesebu.xml;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/19/2019
 */
public class RCDFormSeries {

	private String id;
	private String name;
	private String beginningQty;
	private String beginningFrom;
	private String beginningTo;
	private String receiptQty;
	private String receiptFrom;
	private String receiptTo;
	private String issuedQty;
	private String issuedFrom;
	private String issuedTo;
	private String endingQty;
	private String endingFrom;
	private String endingTo;
	private String remarks;
	private String collector;
	public String getRemarks() {
		if(remarks==null) {
			remarks = "";
		}
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getCollector() {
		if(collector==null) {
			collector = "";
		}
		return collector;
	}
	public void setCollector(String collector) {
		this.collector = collector;
	}
	public String getId() {
		if(id==null) {
			id="";
		}
		return id;
	}
	public String getName() {
		if(name==null) {
			name="";
		}
		return name;
	}
	public String getBeginningQty() {
		if(beginningQty==null) {
			beginningQty="";
		}
		return beginningQty;
	}
	public String getBeginningFrom() {
		if(beginningFrom==null) {
			beginningFrom="";
		}
		return beginningFrom;
	}
	public String getBeginningTo() {
		if(beginningTo==null) {
			beginningTo="";
		}
		return beginningTo;
	}
	public String getReceiptQty() {
		if(receiptQty==null) {
			receiptQty="";
		}
		return receiptQty;
	}
	public String getReceiptFrom() {
		if(receiptFrom==null) {
			receiptFrom="";
		}
		return receiptFrom;
	}
	public String getReceiptTo() {
		if(receiptTo==null) {
			receiptTo="";
		}
		return receiptTo;
	}
	public String getIssuedQty() {
		if(issuedQty==null) {
			issuedQty="";
		}
		return issuedQty;
	}
	public String getIssuedFrom() {
		if(issuedFrom==null) {
			issuedFrom="";
		}
		return issuedFrom;
	}
	public String getIssuedTo() {
		if(issuedTo==null) {
			issuedTo="";
		}
		return issuedTo;
	}
	public String getEndingQty() {
		if(endingQty==null) {
			endingQty="";
		}
		return endingQty;
	}
	public String getEndingFrom() {
		if(endingFrom==null) {
			endingFrom="";
		}
		return endingFrom;
	}
	public String getEndingTo() {
		if(endingTo==null) {
			endingTo="";
		}
		return endingTo;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setBeginningQty(String beginningQty) {
		this.beginningQty = beginningQty;
	}
	public void setBeginningFrom(String beginningFrom) {
		this.beginningFrom = beginningFrom;
	}
	public void setBeginningTo(String beginningTo) {
		this.beginningTo = beginningTo;
	}
	public void setReceiptQty(String receiptQty) {
		this.receiptQty = receiptQty;
	}
	public void setReceiptFrom(String receiptFrom) {
		this.receiptFrom = receiptFrom;
	}
	public void setReceiptTo(String receiptTo) {
		this.receiptTo = receiptTo;
	}
	public void setIssuedQty(String issuedQty) {
		this.issuedQty = issuedQty;
	}
	public void setIssuedFrom(String issuedFrom) {
		this.issuedFrom = issuedFrom;
	}
	public void setIssuedTo(String issuedTo) {
		this.issuedTo = issuedTo;
	}
	public void setEndingQty(String endingQty) {
		this.endingQty = endingQty;
	}
	public void setEndingFrom(String endingFrom) {
		this.endingFrom = endingFrom;
	}
	public void setEndingTo(String endingTo) {
		this.endingTo = endingTo;
	}
	
}