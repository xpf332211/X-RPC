package com.meiya.serialize.impl;

import com.meiya.serialize.Serializer;

/**
 * @author xiaopengfei
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
