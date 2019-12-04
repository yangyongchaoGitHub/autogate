package com.dataexpo.autogate.model;

public class User {
    public int id;
    public String name;
    public String company;
    public String position;
    public String cardCode;
    public int gender;
    public String code;

    public User() {}

    public User (int id, String name, String company, String position, String cardCode, int gender, String code) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.position = position;
        this.cardCode = cardCode;
        this.gender = gender;
        this.code = code;
    }

}
