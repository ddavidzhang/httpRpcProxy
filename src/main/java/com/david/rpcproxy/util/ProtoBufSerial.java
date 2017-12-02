package com.david.rpcproxy.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Created by zhangjw on 7/22/16.
 */
public class ProtoBufSerial {
    public static <T> byte[] toProto(T o) {
        Schema schema = RuntimeSchema.getSchema(o.getClass());
        return ProtobufIOUtil.toByteArray(o, schema, LinkedBuffer.allocate(256));
    }

    public static <T> T fromProto(byte[] bytes, Class<T> clz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Schema<T> schema = RuntimeSchema.getSchema(clz);
        T ret = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, ret, schema);
        return ret;
    }
}
