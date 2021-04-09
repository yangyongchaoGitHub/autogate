package com.dataexpo.autogate.model.service;

import java.io.Serializable;
import java.util.Date;

/**
 * 传输到设备的用户实体
 * @author Administrator
 *
 */
public class UserEntityVo implements Serializable{
	//expo_info的id
	private int euId;
	//user_info的id
	private int uId;
	
	private String eucode;
	
	//姓名
	private String name;
	
	//公司
	private String company;
	
	//职务
	private String position;
	
	//绑定的卡号，理论上是eufileCode
	private String cardCode;
	
	//图像的base64校验码
	private String imageBase64;
	
	//数据创建时间
	private Date createTime;
	
	//数据修改时间
	private Date updateTime;
	
	//图像数据
	private String img;

	public int getEuId() {
		return euId;
	}

	public void setEuId(int euId) {
		this.euId = euId;
	}

	public int getuId() {
		return uId;
	}

	public void setuId(int uId) {
		this.uId = uId;
	}

	public String getEucode() {
		return eucode;
	}

	public void setEucode(String eucode) {
		this.eucode = eucode;
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

	public Date getUpdateTIme() {
		return updateTime;
	}

	public void setUpdateTIme(Date updateTIme) {
		this.updateTime = updateTIme;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
	
	
}
