package com.meiya.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.meiya.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaopengfei
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        byte[] bytes = JSON.toJSONBytes(object);
        log.info("json【{}】完成,序列化后的字节数为【{}】",object,bytes.length);
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        T t = JSON.parseObject(bytes, clazz, JSONReader.Feature.SupportClassForName);
        log.info("json【{}】完成",clazz);
        return t;
    }
}
