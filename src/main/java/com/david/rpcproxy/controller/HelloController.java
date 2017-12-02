package com.david.rpcproxy.controller;

import com.david.rpcproxy.model.arg.IncArg;
import com.david.rpcproxy.model.result.IncResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhangjw on 12/2/17.
 */
@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, world";
    }
    @RequestMapping("/inc")
    public IncResult hello(@RequestBody IncArg arg) {
        IncResult result = new IncResult();
        result.setValue(arg.getNum() + 1);
        return result;
    }
}
