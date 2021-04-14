package com.dataexpo.autogate.model.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class Permissions implements Serializable {
    private int id;
    private String names;
    private String numbers;
    private int expoId;

    @JsonIgnore
    private boolean bShow;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public int getExpoId() {
        return expoId;
    }

    public void setExpoId(int expoId) {
        this.expoId = expoId;
    }

    public boolean isbShow() {
        return bShow;
    }

    public void setbShow(boolean bShow) {
        this.bShow = bShow;
    }
}
