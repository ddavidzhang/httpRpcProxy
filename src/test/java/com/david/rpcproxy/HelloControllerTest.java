package com.david.rpcproxy;

import com.david.rpcproxy.controller.HelloController;
import com.david.rpcproxy.model.arg.IncArg;
import com.david.rpcproxy.model.result.IncResult;
import com.david.rpcproxy.service.HelloService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Created by zhangjw on 12/2/17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class HelloControllerTest {
    private MockMvc mockMvc;
    private RpcServiceProxyFactory factory;
    @Before
    public void setUp()throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
        factory = new RpcServiceProxyFactory();
        factory.init();
    }
    @Test
    public void hello()throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/hello").accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void inc()throws  Exception {
        RpcServiceProxyFactoryBean<HelloService> factoryBean = new RpcServiceProxyFactoryBean<>();
        factoryBean.setFactory(factory);
        factoryBean.setType(HelloService.class);
        HelloService helloService = factoryBean.getObject();
        IncArg arg = new IncArg();
        {
            arg.setNum(1);
        }
        IncResult result = helloService.inc(arg);
        System.out.println(result);
    }
}
