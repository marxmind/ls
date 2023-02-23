package com.italia.municipality.lakesebu.xml;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 06/19/2019
 *
 */
public class RCDFormDetails {

	private String formId;
	private String name;
	private String seriesFrom;
	private String seriesTo;
	private String amount;
	public String getFormId() {
		if(formId==null) {
			formId="";
		}
		return formId;
	}
	public String getName() {
		if(name==null) {
			name="";
		}
		return name;
	}
	public String getSeriesFrom() {
		if(seriesFrom==null) {
			seriesFrom="";
		}
		return seriesFrom;
	}
	public String getSeriesTo() {
		if(seriesTo==null) {
			seriesTo="";
		}
		return seriesTo;
	}
	public String getAmount() {
		if(amount==null) {
			amount="";
		}
		return amount;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setSeriesFrom(String seriesFrom) {
		this.seriesFrom = seriesFrom;
	}
	public void setSeriesTo(String seriesTo) {
		this.seriesTo = seriesTo;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
}