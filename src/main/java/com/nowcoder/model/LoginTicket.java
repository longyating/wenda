package com.nowcoder.model;

import java.util.Date;

/**
 * Created by nowcoder on 2016/7/3.
 */
//保存用户登录的信息，每一个登录的用户都有一个这样的记录，
public class LoginTicket {
    private int id;
    private int userId;
    private Date expired;//过期时间
    private int status;// 0有效，1无效
    private String ticket;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
