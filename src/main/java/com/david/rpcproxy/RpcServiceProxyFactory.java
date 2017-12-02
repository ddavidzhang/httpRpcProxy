package com.david.rpcproxy;

import com.david.rpcproxy.annonation.RpcService;
import com.david.rpcproxy.annonation.RpcUri;
import com.david.rpcproxy.exception.RpcProxyException;
import com.david.rpcproxy.config.ServiceConfig;
import com.google.common.reflect.Reflection;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangjw on 12/2/17.
 */
@Slf4j
public class RpcServiceProxyFactory {
    private final static RpcClient client = new RpcClient();
    private String configKey = "/proxy-config";

    private final static Map<String, ServiceConfig> serviceMap = new ConcurrentHashMap<>();

    public void init() {
        loadConfig();
    }

    public <T> T newFsiServiceProxy(Class<T> clazz) {
        return Reflection.newProxy(clazz, (Object proxy, Method method, Object[] args) -> {
            RpcService rpcService = method.getDeclaringClass().getDeclaredAnnotation(RpcService.class);
            RpcUri fsiUri = method.getDeclaredAnnotation(RpcUri.class);

            if (rpcService == null) {
                throw new RpcProxyException("UnsupportedInterface, Lack Annotation");
            }

            if (fsiUri == null) {
                throw new RpcProxyException("UnsupportedMethod,Lack Annotation");
            }

            Map<String, String> headers = new LinkedHashMap<>(4);
            //headers.put("Content-Type", "application/octet-stream");
            headers.put("Content-Type", "application/json");

            String serviceUrl = createServiceUrl(getServiceAddress(rpcService.value()), fsiUri.value());

            if (args == null) {
                throw new RpcProxyException("ArgsIsNull");
            }

            Object ret;
            try {
                log.debug("post serverUrl:{}", serviceUrl);
                ret = client.post(rpcService.value(), serviceUrl, headers, args[0], method.getReturnType());
            } finally {
            }
            return ret;
        });
    }

    private void loadConfig() {
        log.info("Start load service config");
        Constructor constructor = new Constructor(ServiceConfig.class);
        TypeDescription appDescription = new TypeDescription(ServiceConfig.class);
        constructor.addTypeDescription(appDescription);
        Yaml yaml = new Yaml();
        InputStream configStream = RpcServiceProxyFactory.class.getResourceAsStream(configKey);
        Map<String, Map<String, Object>> loaded = (Map<String, Map<String, Object>>) yaml.load(configStream);
        for (String key : loaded.keySet()) {
            Map<String, Object> map = loaded.get(key);
            ServiceConfig serviceConfig = new ServiceConfig();
            serviceConfig.setAddress(map.get("address") == null ? "" : (String) map.get("address"));
            serviceConfig.setGrey(map.get("grey") != null && (boolean) map.get("grey"));
            serviceConfig.setBetaAddress(map.get("address") == null ? "" : (String) map.get("betaAddress"));
            serviceConfig.setSocketTimeOut(map.get("socketTimeOut") == null ? 0 : (int) map.get("socketTimeOut"));
            serviceConfig.setConnectionTimeOut(map.get("connectionTimeOut") == null ? 0
                    : (int) map.get("connectionTimeOut"));
            serviceConfig.setConnectionRequestTimeOut(map.get("connectionRequestTimeOut") == null ? 0
                    : (int) map.get("connectionRequestTimeOut"));
            if (serviceConfig.getSocketTimeOut() != 0 || serviceConfig.getConnectionTimeOut() != 0) {
                client.createHttpClientForService(key, serviceConfig);
            }
            serviceMap.put(key, serviceConfig);
        }

        log.info("End load fsi service config, configKey:{}, config:{}", configKey, serviceMap);
    }

    protected String getServiceAddress(String serviceName) {
        ServiceConfig config = serviceMap.get(serviceName);

        if (config == null) {
            throw new RpcProxyException("ServiceConfigIsNull,serviceName=" + serviceName);
        }

        String serviceAddress = config.getAddress();

        if (serviceAddress == null) {
            throw new RpcProxyException("ServiceAddressIsNull,serviceName=" + serviceName);
        }
        return serviceAddress;
    }

    protected String createServiceUrl(String serviceAddress, String serviceUri) {
        if (serviceUri.startsWith("/")) {
            serviceUri = serviceUri.substring(1);
        }
        return String.format("http://%s/%s", serviceAddress, serviceUri);
    }
}
