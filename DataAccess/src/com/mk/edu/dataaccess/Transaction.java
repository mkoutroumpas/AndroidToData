package com.mk.edu.dataaccess;

/**
 * Transaction data entity 
 */
public class Transaction implements IDataEntity {
	
	private long _ID;
	public long getID() {
		return _ID;
	}
	public void setID(long _ID) {
		this._ID = _ID;
	}

	private double TransactionValue;
	public double getTransactionValue() {
		return TransactionValue;
	}
	public void setTransactionValue(double transactionValue) {
		TransactionValue = transactionValue;
	}

	private String TransactionCode;
	public String getTransactionCode() {
		return TransactionCode;
	}
	public void setTransactionCode(String transactionCode) {
		TransactionCode = transactionCode;
	}
	
}
