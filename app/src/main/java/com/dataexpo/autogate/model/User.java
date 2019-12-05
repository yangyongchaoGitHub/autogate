package com.dataexpo.autogate.model;

public class User {
    //{"id":12,"name":"user1","company":"dataexpo.Ltd","position":"development","cardCode":"E0040150C714EA6A","code":"u23123","image":"test","gender":1}
    ///
    public int id;
    public String name;
    public String company;
    public String position;
    public String cardCode;
    public int gender;
    public String code;
    public String image;
    public String image_md5;

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

    public User (int id, String name, String company, String position, String cardCode, int gender, String code, String image, String image_md5) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.position = position;
        this.cardCode = cardCode;
        this.gender = gender;
        this.code = code;
        this.image = image;
        this.image_md5 = image_md5;
    }
}
