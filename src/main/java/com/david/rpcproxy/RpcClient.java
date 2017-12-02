package com.david.rpcproxy;

import com.david.rpcproxy.config.ServiceConfig;
import com.david.rpcproxy.exception.RpcClientException;
import com.david.rpcproxy.util.GsonSerial;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhangjw on 12/2/17.
 */
@Slf4j
public class RpcClient {
    protected static final int DEFAULT_MAX_CONNECTION = 512;
    protected static final int DEFAULT_MAX_PER_ROUTE_CONNECTION = 50;

    protected static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    protected static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
    protected static final int DEFAULT_GET_POOL_TIMEOUT = 500;

    private static final String RPC_STATUS = "RPC-STATUS";
    private static final String RPC_CONTENT = "RPC-Content";
    private static final String RPC_CODE = "RPC-FailCode";
    private static final String RPC_REASON = "RPC-Reason";


    public enum RpcStatus {
        success, error, failure,
    }


    private CloseableHttpClient defaultHttpClient;

    private Map<String, CloseableHttpClient> httpClientMap = Maps.newConcurrentMap();

    public RpcClient() {
        try {
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(DEFAULT_MAX_CONNECTION);
            connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE_CONNECTION);

            SocketConfig.Builder sb = SocketConfig.custom();
            sb.setSoKeepAlive(true);
            sb.setTcpNoDelay(true);
            connectionManager.setDefaultSocketConfig(sb.build());

            HttpClientBuilder hb = HttpClientBuilder.create();
            hb.setConnectionManager(connectionManager);

            RequestConfig.Builder rb = RequestConfig.custom();
            rb.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
            rb.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            rb.setConnectionRequestTimeout(DEFAULT_GET_POOL_TIMEOUT);

            hb.setDefaultRequestConfig(rb.build());

            defaultHttpClient = hb.build();
        } catch (Throwable t) {
            log.error("RPCClient init error", t);
            throw t;
        }
    }

    public void createHttpClientForService(String serviceKey, ServiceConfig serviceConfig) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(50);

        SocketConfig.Builder sb = SocketConfig.custom();
        sb.setSoKeepAlive(true);
        sb.setTcpNoDelay(true);
        connectionManager.setDefaultSocketConfig(sb.build());

        HttpClientBuilder hb = HttpClientBuilder.create();
        hb.setConnectionManager(connectionManager);

        RequestConfig.Builder rb = RequestConfig.custom();
        rb.setSocketTimeout(
                serviceConfig.getSocketTimeOut() == 0 ? DEFAULT_SOCKET_TIMEOUT : serviceConfig.getSocketTimeOut());
        rb.setConnectTimeout(
                serviceConfig.getConnectionTimeOut() == 0 ? DEFAULT_CONNECTION_TIMEOUT : serviceConfig.getConnectionTimeOut());
        rb.setConnectionRequestTimeout(serviceConfig.getConnectionRequestTimeOut() == 0 ?
                DEFAULT_GET_POOL_TIMEOUT :
                serviceConfig.getConnectionRequestTimeOut());
        hb.setDefaultRequestConfig(rb.build());

        try {
            CloseableHttpClient oldClient = httpClientMap.get(serviceKey);
            if (oldClient != null) {
                oldClient.close();
            }
        } catch (Exception e) {
            log.error("Close httpClient error,serviceKey:{}", serviceKey, e);
        }

        httpClientMap.put(serviceKey, hb.build());
    }


    public <A, R> R post(String serviceKey, String uri, Map<String, String> headers, A arg, Class<R> clazz) {
        DefaultRpcResponseHandler<R> handler = new DefaultRpcResponseHandler<>(uri, clazz);
        try {
            return post(serviceKey, uri, headers, arg, handler);
        } catch (IOException e) {
            throw new RpcClientException("Error post uri=" + uri, e);
        }
    }

    public <A, R> R post(String serviceKey,
                         String uri,
                         Map<String, String> headers,
                         A arg,
                         ResponseHandler<R> handler) throws IOException {

        RequestBuilder requestBuilder = RequestBuilder.post();
        requestBuilder.setUri(uri);

        for (Map.Entry<String, String> e : headers.entrySet()) {
            requestBuilder.addHeader(e.getKey(), e.getValue());
        }

        BasicHttpEntity entity = new BasicHttpEntity();

        byte[] bytes = GsonSerial.entityToJson(arg).getBytes(Charsets.UTF_8);

        entity.setContentLength(bytes.length);
        entity.setContent(new ByteArrayInputStream(bytes));
        requestBuilder.setEntity(entity);

        CloseableHttpClient serviceClient = httpClientMap.get(serviceKey);
        if (serviceClient == null) {
            serviceClient = defaultHttpClient;
        }
        try (CloseableHttpResponse response = serviceClient.execute(requestBuilder.build())) {
            return handler.handleResponse(response);
        }
    }

    public static final class DefaultRpcResponseHandler<T> implements ResponseHandler<T> {

        private Class<T> clazz;
        private String uri;

        public DefaultRpcResponseHandler(String uri, Class<T> clazz) {
            this.uri = uri;
            this.clazz = clazz;
        }

        @Override
        public T handleResponse(HttpResponse response) throws IOException {
            int statusCode = response.getStatusLine().getStatusCode();
            Header rpcStatus = response.getFirstHeader(RPC_STATUS);

            // process the grey status
            Header firstHeader = response.getFirstHeader(RPC_REASON);
            if (statusCode == HttpStatus.SC_FORBIDDEN && Objects.nonNull(firstHeader) &&
                    firstHeader.getValue().equals("EnterpriseBlocked")) {
                throw new RpcClientException("Service Uri:" + uri + " Greyed");
            }

            //process no ok
            if (statusCode != HttpStatus.SC_OK) {
                String statusValue = rpcStatus != null ? rpcStatus.getValue() : "";
                throw new RpcClientException("StatusCode:" + statusCode + " RpcStatus:" + statusValue + ",Message:" +
                        EntityUtils.toString(response.getEntity()) + ",url:" + this.uri);
            }

            // process rpc status
            if (rpcStatus == null || rpcStatus.getValue() == null) {
                //throw new RpcClientException("RpcStatusNotFound!" + ",url:" + this.uri);
            }
            RpcStatus status=RpcStatus.success;
            try {
                //status = RpcStatus.valueOf(rpcStatus.getValue().toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new RpcClientException("UnknownRpcStatus:" + rpcStatus.getValue() + ",url:" + this.uri);
            }

            switch (status) {
                case success:
                    return GsonSerial.jsonToEntity(EntityUtils.toString(response.getEntity(),Charsets.UTF_8), clazz);
                case failure:
                    int code = Integer.parseInt(response.getFirstHeader(RPC_CODE).getValue());
                    String message = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
                    throw new RpcClientException(code, "RpcFailure,Code:" + code + ",Message:" + message + ",url:" + this.uri);
                case error:
                    String error = response.getFirstHeader(RPC_CONTENT).getValue();
                    throw new RpcClientException(
                            "RpcError:" + error + ",response:" + EntityUtils.toString(response.getEntity()) + ",url:" + this.uri);
                default:
                    throw new RpcClientException("RpcStatus:" + status + ",url:" + this.uri);
            }
        }
    }
}
