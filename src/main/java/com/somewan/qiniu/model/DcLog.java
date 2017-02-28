package com.somewan.qiniu.model;

import java.sql.Timestamp;

/**
 * Created by wan on 2017/2/27.
 */
public class DcLog {
    private int id;
    private Timestamp time;
    private long time_stamp;
    private int method;
    private String url;
    private String request_key;
    private Integer request_from;
    private Integer request_to;
    private int length;
    private int response_code;
    private String message;
    private int take;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequest_key() {
        return request_key;
    }

    public void setRequest_key(String request_key) {
        this.request_key = request_key;
    }

    public Integer getRequest_from() {
        return request_from;
    }

    public void setRequest_from(Integer request_from) {
        this.request_from = request_from;
    }

    public Integer getRequest_to() {
        return request_to;
    }

    public void setRequest_to(Integer request_to) {
        this.request_to = request_to;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getResponse_code() {
        return response_code;
    }

    public void setResponse_code(int response_code) {
        this.response_code = response_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTake() {
        return take;
    }

    public void setTake(int take) {
        this.take = take;
    }
}
