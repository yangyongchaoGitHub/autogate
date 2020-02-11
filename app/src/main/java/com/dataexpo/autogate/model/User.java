package com.dataexpo.autogate.model;

import android.util.Base64;

import static com.dataexpo.autogate.face.manager.ImportFileManager.IMPORT_FILE_EMPTY;

public class User extends BaseModel {
    //以图片的命名信息作为用户基本数据来源的命名规则
    //name-company-position-cardcode-code
    //{"name":"user1","company":"dataexpo.Ltd","position":"development","cardCode":"E0040150C714EA6A","code":"u23123","image":"test","gender":1}
    public static final int IMAGE_TYPE_JPG = 0;
    public static final int IMAGE_TYPE_PNG = 1;

    //收到数据后立即进行人脸注册
    public static final int SYNC_IMMEDIATELY = 0;
    //不在收到数据后立即注册
    public static final int SYNC_LATER = 1;

    public int id;
    public String name = "";
    public String company = "";
    public String position = "";
    public String cardCode = "";
    public int gender = 0;
    public String code = "";
    public String image_name = "";
    public String image = "";
    public Integer image_type = IMAGE_TYPE_JPG;
    public String image_md5 = "";
    public boolean check;
    public long ctime = 0L;
    public long updateTime = 0L;
    public String userInfo = "";
    public Integer bregist_face = IMPORT_FILE_EMPTY;
    private String faceToken = "";
    public Integer sync_regist = SYNC_IMMEDIATELY;

    public byte[] feature;

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

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getFaceToken() {
        if (feature != null) {
            byte[] base = Base64.encode(feature, Base64.NO_WRAP);
            faceToken = new String(base);
        }
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }
}
