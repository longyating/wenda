package com.nowcoder.model;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class Feed {
    private int id;
    private int type;  //关注；评论等不同的类型，用不同的渲染
    private int userId; //由谁产生的
    private Date createdDate;
    private String data;  //JSON格式 不同的新鲜事有着不同的具体内容，采用JSON。
    private JSONObject dataJSON = null; //为了可以快速得到data数据 设置一个辅助变量，

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        dataJSON = JSONObject.parseObject(data);
    }

    //和viewObject 思路一致
    public String get(String key){
        return dataJSON==null ? null : dataJSON.getString(key);
    }
}
