package com.david.rpcproxy.config;

import lombok.Data;

/**
 * Created by zhangjw on 12/2/17.
 */
@Data
public class ServiceConfig {

    private String address;

    private boolean isGrey;

    private String betaAddress;

    private int socketTimeOut;

    private int connectionTimeOut;

    private int connectionRequestTimeOut;

}
