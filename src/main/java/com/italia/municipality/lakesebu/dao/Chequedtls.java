package com.italia.municipality.lakesebu.dao;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 
 * @author ark
 *		cheque_id int NOT NULL,
		accnt_no varchar(225),
		cheque_no varchar(225),
		accnt_name varchar(225),
		bank_name varchar(225),
		date_disbursement varchar(225),
		cheque_amount varchar(225),
		pay_to_the_order_of varchar(225),
		amount_in_words varchar(225),
		proc_by varchar(255),
		date_created TIMESTAMP,
		date_edited TIMESTAMP,
		sig1_id int,
		sig2_id int
		this form is not in use
 */
@Deprecated
public class Chequedtls implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long chequeId;
	private String accntNo;
	private String chequeNo;
	private String accntName;
	private String bankName;
	private String dateDisbursement;
	private String chequeAmount;
	private String payToTheOrderOf;
	private String amountInWords;
	private String procBy;
	private Timestamp dateCreated;
	private Timestamp dateEdited;
	private int sig1Id;
	private int sig2Id;
	
	public Chequedtls(){}
	
	public Long getChequeId() {
		return chequeId;
	}
	public void setChequeId(Long chequeId) {
		this.chequeId = chequeId;
	}
	public String getAccntNo() {
		return accntNo;
	}
	public void setAccntNo(String accntNo) {
		this.accntNo = accntNo;
	}
	public String getChequeNo() {
		return chequeNo;
	}
	public void setChequeNo(String chequeNo) {
		this.chequeNo = chequeNo;
	}
	public String getAccntName() {
		return accntName;
	}
	public void setAccntName(String accntName) {
		this.accntName = accntName;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getDateDisbursement() {
		return dateDisbursement;
	}
	public void setDateDisbursement(String dateDisbursement) {
		this.dateDisbursement = dateDisbursement;
	}
	public String getChequeAmount() {
		return chequeAmount;
	}
	public void setChequeAmount(String chequeAmount) {
		this.chequeAmount = chequeAmount;
	}
	public String getPayToTheOrderOf() {
		return payToTheOrderOf;
	}
	public void setPayToTheOrderOf(String payToTheOrderOf) {
		this.payToTheOrderOf = payToTheOrderOf;
	}
	public String getAmountInWords() {
		return amountInWords;
	}
	public void setAmountInWords(String amountInWords) {
		this.amountInWords = amountInWords;
	}
	public String getProcBy() {
		return procBy;
	}
	public void setProcBy(String procBy) {
		this.procBy = procBy;
	}
	public Timestamp getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Timestamp getDateEdited() {
		return dateEdited;
	}
	public void setDateEdited(Timestamp dateEdited) {
		this.dateEdited = dateEdited;
	}
	public int getSig1Id() {
		return sig1Id;
	}
	public void setSig1Id(int sig1Id) {
		this.sig1Id = sig1Id;
	}
	public int getSig2Id() {
		return sig2Id;
	}
	public void setSig2Id(int sig2Id) {
		this.sig2Id = sig2Id;
	}
	
	// Object overrides ---------------------------------------------------------------------------

    /**
     * The cheque ID is unique for each User. So this should compare User by ID only.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof Chequedtls) && (chequeId != null)
             ? chequeId.equals(((Chequedtls) other).chequeId)
             : (other == this);
    }

    /**
     * The user ID is unique for each User. So User with same ID should return same hashcode.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (chequeId != null) 
             ? (this.getClass().hashCode() + chequeId.hashCode()) 
             : super.hashCode();
    }

    /**
     * Returns the String representation of this User. Not required, it just pleases reading logs.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
        		"Chequedtls[chequeId=%d,accntNo=%s,chequeNo=%s,accntName=%s,bankName=%s,dateDisbursement=%s,chequeAmount=%s,payToTheOrderOf=%s,amountInWords=%s,procBy=%s,Timestamp dateCreated=%s,dateEdited=%s,sig1Id=%s,sig2Id=%s]",
        		chequeId,
        		accntNo,
        		chequeNo,
        		accntName,
        		bankName,
        		dateDisbursement,
        		chequeAmount,
        		payToTheOrderOf,
        		amountInWords,
        		procBy,
        		dateCreated,
        		dateEdited,
        		sig1Id,
        		sig2Id);
    }
	
	public static void main(String[] args) {
		
		Chequedtls id = new Chequedtls();
		System.out.println(id.getChequeId()+"");
		
	}
}
