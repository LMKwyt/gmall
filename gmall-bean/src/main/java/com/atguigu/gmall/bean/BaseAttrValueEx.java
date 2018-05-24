package com.atguigu.gmall.bean;

import java.io.Serializable;

public class BaseAttrValueEx extends BaseAttrValue implements Serializable {

    String wholeName ;

    String cancelUrlParam;

    public String getWholeName() {
        return wholeName;
    }

    public void setWholeName(String wholeName) {
        this.wholeName = wholeName;
    }

    public String getCancelUrlParam() {
        return cancelUrlParam;
    }

    public void setCancelUrlParam(String cancelUrlParam) {
        this.cancelUrlParam = cancelUrlParam;
    }
}
