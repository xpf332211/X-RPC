package com.meiya.serialize;

import com.meiya.exceptions.SerializeException;
import com.meiya.serialize.impl.HessianSerializer;
import com.meiya.serialize.impl.JdkSerializer;
import com.meiya.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopengfei
 */
@Slf4j
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
        SerializerWrapper hessianWrapper = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE_TYPE.put("jdk",jdkWrapper);
        SERIALIZER_CACHE_TYPE.put("json",jsonWrapper);
        SERIALIZER_CACHE_TYPE.put("hessian",hessianWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 1,jdkWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 2,jsonWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 3,hessianWrapper);
    }

    /**
     * 根据字符串type获取实例
     * @param serializeType 字符串type
     * @return 序列化器实例
     */
    public static Serializer getSerializer(String serializeType) {
        SerializerWrapper wrapper = SERIALIZER_CACHE_TYPE.get(serializeType);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getSerializer();
    }

    /**
     * 根据byte数字获取实例
     * @param serializeCode byte数字
     * @return 序列化器实例
     */
    public static Serializer getSerializer(byte serializeCode){
        SerializerWrapper wrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getSerializer();
    }

    /**
     * 根据byte数字获取对应的字符串type
     * @param serializeCode byte数字
     * @return 字符串type
     */
    public static String getSerializerType(byte serializeCode){
        SerializerWrapper wrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }

    /**
     * 根据字符串type获取对应的byte数字
     * @param serializeType 字符串type
     * @return byte数字
     */
    public static byte getSerializerCode(String serializeType){
        SerializerWrapper wrapper = SERIALIZER_CACHE_TYPE.get(serializeType);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 判断wrapper是否为空 若是则采用默认jdk序列的wrapper
     * @param wrapper 需要判断的wrapper
     * @return wrapper
     */
    private static SerializerWrapper validateWrapperNotNull(SerializerWrapper wrapper){
        if (wrapper == null){
            log.info("未匹配到指定的序列化类型,默认采用jdk序列化");
            wrapper = SERIALIZER_CACHE_TYPE.get("jdk");
        }
        return wrapper;
    }
}
