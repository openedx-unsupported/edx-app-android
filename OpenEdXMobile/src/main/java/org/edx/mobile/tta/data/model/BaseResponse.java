package org.edx.mobile.tta.data.model;

import java.io.Serializable;


/**
 * Created by Arjun on 2018/9/18.
 */

public class BaseResponse<E> implements Serializable {
    private String code;
    private String msg;
    private E data;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public E getData() {
        return data;
    }
}
