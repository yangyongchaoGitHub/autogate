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

    //鉴权字段  表示通过
    public static final int AUTH_SUCCESS = 1;
    //鉴权字段  表示未通过
    public static final int AUTH_FAILT = 2;

    public static final int IMG_SYNC_NONE = 0;
    public static final int IMG_SYNC_CHANGE = 1;
    public static final int IMG_SYNC_END = 2;
    public static final int IMG_SYNC_WAIT = 3;

    public int id;
    public int expoId;
    public int pid; //数据库id
    public String name = "";
    public String company = "";
    public String position = "";
    public String cardCode = "";
    public int gender = 0;
    public String code = "";
    public String image_name = "";
    public String image_base64 = ""; //图片的base64校验值
    public String image = "";
    public Integer image_type = IMAGE_TYPE_JPG;
    public int image_sync = IMG_SYNC_NONE;
    public boolean check;
    public long ctime = 0L;
    public long updateTime = 0L;
    public String userInfo = "";
    public int auth = AUTH_SUCCESS;
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

    public User (int id, String name, String company, String position, String cardCode, int gender, String code, String image, int image_md5) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.position = position;
        this.cardCode = cardCode;
        this.gender = gender;
        this.code = code;
        this.image = image;
        this.image_sync = image_md5;
    }

    public User (String name, String company, String position, String code, String cardCode) {
        this.name = name;
        this.company = company;
        this.position = position;
        this.cardCode = cardCode;
        this.code = code;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getFaceToken() {
        if (feature != null && "".equals(faceToken)) {
            byte[] base = Base64.encode(feature, Base64.NO_WRAP);
            faceToken = new String(base);
        }
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }
}
