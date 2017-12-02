package com.david.rpcproxy.service;

import com.david.rpcproxy.annonation.RpcService;
import com.david.rpcproxy.annonation.RpcUri;
import com.david.rpcproxy.model.arg.IncArg;
import com.david.rpcproxy.model.result.IncResult;

/**
 * Created by zhangjw on 12/2/17.
 */
@RpcService("Hello")
public interface HelloService {
    @RpcUri("/inc")
    IncResult inc(IncArg arg);
}
