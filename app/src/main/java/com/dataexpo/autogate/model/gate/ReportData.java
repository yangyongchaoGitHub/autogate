package com.dataexpo.autogate.model.gate;

public class ReportData {
    private int id;
    private String number = "";
    private String direction = "";
    private String time = "";

    public ReportData()
    {
        super();
    }

    public ReportData(int id, String number, String direction, String time) {
        this.id = id;
        this.number = number;
        this.direction = direction;
        this.time = time;
    }

    public ReportData(String sData, String sDir, String sTime) {
        super();
        this.setNumber(sData);
        this.setDirection(sDir);
        this.setTime(sTime);
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
