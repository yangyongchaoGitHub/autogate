package com.dataexpo.autogate.model;

public class FaceRecord {
    public Integer id;
    public Integer user_id;
    public String time;
    public String user_code;
    public String imageName;
    public String company;
    public String cardcode;
    public String name;
    public boolean check;

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
