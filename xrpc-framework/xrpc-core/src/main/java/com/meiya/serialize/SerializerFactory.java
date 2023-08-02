package com.meiya.serialize;

import com.meiya.exceptions.SerializeException;
import com.meiya.serialize.impl.JdkSerializer;
import com.meiya.serialize.impl.JsonSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopengfei
 */
public class SerializerFactory {

    /**
     * serializerWrapper 缓存 通过type取
     */
    private static final Map<String,SerializerWrapper> SERIALIZER_CACHE_TYPE = new ConcurrentHashMap<>(4);
    /**
     * serializerWrapper 缓存 通过code取
     */
    private static final Map<Byte,SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(4);
    static {
        SerializerWrapper jdkWrapper = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper jsonWrapper = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SERIALIZER_CACHE_TYPE.put("jdk",jdkWrapper);
        SERIALIZER_CACHE_TYPE.put("json",jsonWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 1,jdkWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 2,jsonWrapper);
    }
    public static Serializer getSerializer(String serializeType) {
        SerializerWrapper wrapper = SERIALIZER_CACHE_TYPE.get(serializeType);
        validateWrapperNotNull(wrapper);
        return wrapper.getSerializer();
    }
    public static Serializer getSerializer(byte code){
        SerializerWrapper wrapper = SERIALIZER_CACHE_CODE.get(code);
        validateWrapperNotNull(wrapper);
        return wrapper.getSerializer();
    }
    public static String getSerializerType(byte code){
        SerializerWrapper wrapper = SERIALIZER_CACHE_CODE.get(code);
        validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }
    public static byte getSerializerCode(String serializeType){
        SerializerWrapper wrapper = SERIALIZER_CACHE_TYPE.get(serializeType);
        validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 判断wrapper是否为空 若是则抛出异常
     * @param wrapper wrapper
     */
    private static void validateWrapperNotNull(SerializerWrapper wrapper){
        if (wrapper == null){
            throw new SerializeException("未匹配到指定的序列化方式！");
        }
    }
}
