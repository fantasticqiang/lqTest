package com.syf.util;

import java.util.HashMap;
import java.util.Map;

public class HttpMsg {

	private String reqMsg;
	private Map<String, String> reqMap;

	private String reqBusiMsg;
	private Map<String, String> reqBusiMap;

	private String resMsg;
	private Map<String, String> resMap;

	private String resBusiMsg;
	private Map<String, String> resBusiMap;

	private int status;
	private boolean verify;

	private String sourceFile;
	private String targetFile;

	public HttpMsg() {
		this.reqBusiMap = new HashMap<String, String>();
		this.resMap = new HashMap<String, String>();
		this.resBusiMap = new HashMap<String, String>();
	}

	public String getReqBusiMsg() {
		return reqBusiMsg;
	}

	public void setReqBusiMsg(String reqBusiMsg) {
		this.reqBusiMsg = reqBusiMsg;
	}

	public Map<String, String> getReqBusiMap() {
		return reqBusiMap;
	}

	public void setReqBusiMap(Map<String, String> reqBusiMap) {
		this.reqBusiMap = reqBusiMap;
	}

	public String getResMsg() {
		return resMsg;
	}

	public void setResMsg(String resMsg) {
		this.resMsg = resMsg;
	}

	public Map<String, String> getResMap() {
		return resMap;
	}

	public void setResMap(Map<String, String> resMap) {
		this.resMap = resMap;
	}

	public String getResBusiMsg() {
		return resBusiMsg;
	}

	public void setResBusiMsg(String resBusiMsg) {
		this.resBusiMsg = resBusiMsg;
	}

	public Map<String, String> getResBusiMap() {
		return resBusiMap;
	}

	public void setResBusiMap(Map<String, String> resBusiMap) {
		this.resBusiMap = resBusiMap;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isVerify() {
		return verify;
	}

	public void setVerify(boolean verify) {
		this.verify = verify;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public String getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}

	public String getReqMsg() {
		return reqMsg;
	}

	public void setReqMsg(String reqMsg) {
		this.reqMsg = reqMsg;
	}

	public Map<String, String> getReqMap() {
		return reqMap;
	}

	public void setReqMap(Map<String, String> reqMap) {
		this.reqMap = reqMap;
	}

}
