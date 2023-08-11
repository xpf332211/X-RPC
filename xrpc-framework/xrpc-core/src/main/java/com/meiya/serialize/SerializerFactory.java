package com.meiya.serialize;

import com.meiya.config.wrapper.ObjectWrapper;
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
    public static final Map<String, ObjectWrapper<?>> SERIALIZER_CACHE_TYPE = new ConcurrentHashMap<>(4);
    /**
     * serializerWrapper 缓存 通过code取
     */
    public static final Map<Byte,ObjectWrapper<?>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(4);
    static {
        ObjectWrapper<Serializer> jdkWrapper = new ObjectWrapper<>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> jsonWrapper = new ObjectWrapper<>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessianWrapper = new ObjectWrapper<>((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE_TYPE.put("jdk",jdkWrapper);
        SERIALIZER_CACHE_TYPE.put("json",jsonWrapper);
        SERIALIZER_CACHE_TYPE.put("hessian",hessianWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 1,jdkWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 2,jsonWrapper);
        SERIALIZER_CACHE_CODE.put((byte) 3,hessianWrapper);
    }

    /**
     * 配置自定义序列化类 更新序列化类工厂缓存
     * @param serializer 序列化类实例
     * @param serializerName 序列化类名称
     * @param serializerNum 序列化类编号
     * @return 序列化类名称
     */
    public static String updateSerializerFactory(Serializer serializer, String serializerName, String serializerNum) {
        if (serializer == null || serializerName == null || serializerNum == null){
            return null;
        }
        if (!SerializerFactory.SERIALIZER_CACHE_TYPE.containsKey(serializerName)
                && !SerializerFactory.SERIALIZER_CACHE_CODE.containsKey(Byte.parseByte(serializerNum))){
            ObjectWrapper<Serializer> serializerWrapper = new ObjectWrapper<>(Byte.parseByte(serializerNum), serializerName, serializer);
            SerializerFactory.SERIALIZER_CACHE_TYPE.put(serializerName,serializerWrapper);
            SerializerFactory.SERIALIZER_CACHE_CODE.put(Byte.parseByte(serializerNum),serializerWrapper);
            return serializerName;
        }else {
            log.warn("配置的序列化类指定的名称或号码重复！");
            return null;
        }
    }

    /**
     * 根据字符串type获取实例
     * @param serializeType 字符串type
     * @return 序列化器实例
     */
    public static Serializer getSerializer(String serializeType) {
        ObjectWrapper<?> wrapper = SERIALIZER_CACHE_TYPE.get(serializeType);
        wrapper = validateWrapperNotNull(wrapper);
        return (Serializer) wrapper.getImpl();
    }

    /**
     * 根据byte数字获取实例
     * @param serializeCode byte数字
     * @return 序列化器实例
     */
    public static Serializer getSerializer(byte serializeCode){
        ObjectWrapper<?> wrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        wrapper = validateWrapperNotNull(wrapper);
        return (Serializer) wrapper.getImpl();
    }

    /**
     * 根据byte数字获取对应的字符串type
     * @param serializeCode byte数字
     * @return 字符串type
     */
    public static String getSerializerType(byte serializeCode){
        ObjectWrapper<?> wrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }

    /**
     * 根据字符串type获取对应的byte数字
     * @param serializeType 字符串type
     * @return byte数字
     */
    public static byte getSerializerCode(String serializeType){
        ObjectWrapper<?> wrapper = SERIALIZER_CACHE_TYPE.get(serializeType);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 判断wrapper是否为空 若是则采用默认jdk序列的wrapper
     * @param wrapper 需要判断的wrapper
     * @return wrapper
     */
    private static ObjectWrapper<?> validateWrapperNotNull(ObjectWrapper<?> wrapper){
        if (wrapper == null){
            log.info("未匹配到指定的序列化类型,默认采用jdk序列化");
            wrapper = SERIALIZER_CACHE_TYPE.get("jdk");
        }
        return wrapper;
    }
}
