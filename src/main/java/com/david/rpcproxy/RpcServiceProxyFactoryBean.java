package com.david.rpcproxy;

import org.springframework.beans.factory.FactoryBean;

/**
 * Created by zhangjw on 12/2/17.
 */
public class RpcServiceProxyFactoryBean<T> implements FactoryBean<T> {
    private RpcServiceProxyFactory factory;
    private Class<T> type;
    @Override
    public T getObject() throws Exception {
        return factory.newFsiServiceProxy(type);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void setFactory(RpcServiceProxyFactory factory) {
        this.factory = factory;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }
}
