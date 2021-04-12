package com.dataexpo.autogate.model;

import com.rfid.api.ADReaderInterface;

import java.io.Serializable;

public class Rfid implements Serializable {
    private int id;
    private String ip;
    private String port;
    private String name;
    private String remark;

    public ADReaderInterface m_reader = new ADReaderInterface();
    public int status = 0;

    public long lastConnect = 0;

    public Rfid(int id, String ip, String port, String name, String remark) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.remark = remark;
    }

    public Rfid(){}

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
