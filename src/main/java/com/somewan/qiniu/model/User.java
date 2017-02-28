package com.somewan.qiniu.model;

import java.sql.Timestamp;

/**
 * Created by wan on 2017/2/27.
 */
public class User {
    private int id;
    private String name;
    private int age;

    public Timestamp getBirth_time() {
        return birth_time;
    }

    public void setBirth_time(Timestamp birth_time) {
        this.birth_time = birth_time;
    }

    private Timestamp birth_time;

    public String getUser_gender() {
        return user_gender;
    }

    public void setUser_gender(String user_gender) {
        this.user_gender = user_gender;
    }

    private String user_gender;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
