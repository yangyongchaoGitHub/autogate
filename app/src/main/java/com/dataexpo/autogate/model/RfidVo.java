package com.dataexpo.autogate.model;

import com.rfid.api.ADReaderInterface;

import java.io.Serializable;

public class RfidVo implements Serializable {
    private int id;
    private String ip;
    private String port;
    private String name;
    private String remark;

    public int status = 0;

    public RfidVo(int id, String ip, String port, String name, String remark, int status) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.remark = remark;
        this.status = status;
    }

    public RfidVo(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
