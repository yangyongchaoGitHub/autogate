package com.dataexpo.autogate.model.service;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {
	private Integer pageNo;
	
	private Integer pageSize;
	
	private int count; // 总记录数
	
	private List<T> objectList;

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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<T> getObjectList() {
		return objectList;
	}

	public void setObjectList(List<T> objectList) {
		this.objectList = objectList;
	}
	
	
}
