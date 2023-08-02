package com.meiya.serialize;

/**
 * @author xiaopf
 */
public interface Serializer {
    /**
     * 序列化
     * @param object 对象
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param clazz 类型
     * @param <T> 返回对象的泛型
     * @return 对象
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
