package com.syf.model;

public class MerReportModel {

	public String merId;
	public String key;
	
	
	
	public MerReportModel(String merId, String key) {
		super();
		this.merId = merId;
		this.key = key;
	}
	
	
	public MerReportModel() {
	}


	public String getMerId() {
		return merId;
	}
	public void setMerId(String merId) {
		this.merId = merId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}
