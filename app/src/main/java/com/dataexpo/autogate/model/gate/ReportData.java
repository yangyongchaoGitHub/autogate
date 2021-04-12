package com.dataexpo.autogate.model.gate;

import com.dataexpo.autogate.model.Rfid;

public class ReportData {
    private int id;
    private Rfid rfid;
    private String number = "";
    private int direction = 0;
    private String time = "";
    private String ltime = "";
    public boolean check;

    public ReportData(int id, String number, int direction, String time) {
        this.id = id;
        this.number = number;
        this.direction = direction;
        this.time = time;
    }

    public ReportData(Rfid rfid, String number, int direction, String time) {
        this.rfid = rfid;
        this.number = number;
        this.direction = direction;
        this.time = time;
    }


    public ReportData(String sData, int sDir, String sTime) {
        super();
        this.setNumber(sData);
        this.setDirection(sDir);
        this.setTime(sTime);
    }

    public String getLtime() {
        return ltime;
    }

    public void setLtime(String ltime) {
        this.ltime = ltime;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Rfid getRfid() {
        return rfid;
    }

    public void setRfid(Rfid rfid) {
        this.rfid = rfid;
    }
}
