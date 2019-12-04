package com.dataexpo.autogate.model.gate;

public class ReportData {
    private String strData = "";
    private String strDirection = "";
    private String strTime = "";

    public ReportData()
    {
        super();
    }

    public ReportData(String sData, String sDir, String sTime)
    {
        super();
        this.setStrData(sData);
        this.setStrDirection(sDir);
        this.setStrTime(sTime);
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }

    public String getStrDirection() {
        return strDirection;
    }

    public void setStrDirection(String strDirection) {
        this.strDirection = strDirection;
    }

    public String getStrTime() {
        return strTime;
    }

    public void setStrTime(String strTime) {
        this.strTime = strTime;
    }
}
