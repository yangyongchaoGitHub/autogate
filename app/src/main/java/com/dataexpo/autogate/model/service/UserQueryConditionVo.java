package com.dataexpo.autogate.model.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;

public class UserQueryConditionVo implements Serializable {
	public static final int STATUS_INIT = 0;
	public static final int STATUS_REQUESTING = 1;
	public static final int STATUS_RESPONSE = 2;
	public static final int STATUS_FAIL = 3;
	public static final int STATUS_TIMEOUT = 4;

	//查询起始euid
	private Integer startEuId;

	private Integer expoId;
	
	private String name;
	
	private String company;
	
	private String position;
	
	private String eucode;
	
	private String cardCode;
	
	private String imageBase64;
	
	private Date createTime;
	
	private Integer pageNo;
	
	private Integer pageSize;
	
	//数据修改时间
	private Date updateTime;

	//当前请求的序号
	@JsonIgnore
	private int requestId = 0;

	//请求状态
	@JsonIgnore
	private int requestStatus = STATUS_INIT;

	//当前线程
	@JsonIgnore
	private String threadName;

	public Integer getStartEuId() {
		return startEuId;
	}

	public void setStartEuId(Integer startEuId) {
		this.startEuId = startEuId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getEucode() {
		return eucode;
	}

	public void setEucode(String eucode) {
		this.eucode = eucode;
	}

	public String getCardCode() {
		return cardCode;
	}

	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(int requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public Integer getExpoId() {
		return expoId;
	}

	public void setExpoId(Integer expoId) {
		this.expoId = expoId;
	}
}
