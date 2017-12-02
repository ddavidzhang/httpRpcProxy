package com.david.rpcproxy.model.result;

import io.protostuff.Tag;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by zhangjw on 12/2/17.
 */
@Data
public class IncResult implements Serializable {
    @Tag(1)
    private int value;

}
