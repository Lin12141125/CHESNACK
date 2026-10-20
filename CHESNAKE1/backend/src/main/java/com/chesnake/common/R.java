package com.chesnake.common;

import lombok.Data;

/**
 * 统一响应体
 */
@Data
public class R<T> {
    public static final int SUCCESS = 200;
    public static final int FAIL = 500;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return build(SUCCESS, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return build(SUCCESS, "success", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return build(SUCCESS, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return build(FAIL, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    public static <T> R<T> build(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
