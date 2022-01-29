package com.junling.registry.common.result;

import lombok.Data;

public class R {

    public static final Integer SUCCESS_CODE = 200;
    public static final Integer FAIL_CODE = 500;

    private Integer code;
    private String msg;
    private Object data;

    public R (Object data) {
        this.code = SUCCESS_CODE;
        this.msg = "success";
        this.data = data;
    }

    public R (Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static final R SUCCESS(){
        return new R(SUCCESS_CODE, "success");
    }

    public static final R FAIL(){
        return new R(FAIL_CODE, "fail");
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
