package com.dataexpo.autogate.model.service;

import java.io.Serializable;
import java.util.Date;

public class RecordVo implements Serializable {
    private String eucode;
    private Date time;
    private String deviceKey;
    private Integer expoId;
    private String address;
    private String cardNum;
    private String euid;

    public RecordVo(){}

    public RecordVo(String eucode, Date time, String deviceKey, Integer expoId, String address, String cardNum, String euid) {
        this.eucode = eucode;
        this.time = time;
        this.deviceKey = deviceKey;
        this.expoId = expoId;
        this.address = address;
        this.cardNum = cardNum;
        this.euid = euid;
    }

    public String getEucode() {
        return eucode;
    }

    public void setEucode(String eucode) {
        this.eucode = eucode;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public Integer getExpoId() {
        return expoId;
    }

    public void setExpoId(Integer expoId) {
        this.expoId = expoId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getEuid() {
        return euid;
    }

    public void setEuid(String euid) {
        this.euid = euid;
    }
}
