package com.meiya.compress;

import com.meiya.compress.impl.GzipCompressor;
import com.meiya.config.wrapper.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopf
 */
@Slf4j
public class CompressorFactory {
    public static final Map<String, ObjectWrapper<?>> COMPRESSOR_CACHE_TYPE = new ConcurrentHashMap<>(4);
    public static final Map<Byte,ObjectWrapper<?>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(4);
    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE_TYPE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte)1,gzip);
    }


    /**
     * 配置自定义压缩类 更新压缩类工厂缓存
     * @param compressor 压缩类实例
     * @param compressorName 压缩类名称
     * @param compressorNum 压缩类号码
     * @return 压缩类名称
     */
    public static String updateCompressorFactory(Compressor compressor, String compressorName, String compressorNum) {
        if (compressor == null || compressorName == null || compressorNum == null){
            return null;
        }
        if (!CompressorFactory.COMPRESSOR_CACHE_TYPE.containsKey(compressorName)
                && !CompressorFactory.COMPRESSOR_CACHE_CODE.containsKey(Byte.parseByte(compressorNum))) {
            ObjectWrapper<Compressor> compressorWrapper = new ObjectWrapper<>(Byte.parseByte(compressorNum), compressorName, compressor);
            CompressorFactory.COMPRESSOR_CACHE_TYPE.put(compressorName,compressorWrapper);
            CompressorFactory.COMPRESSOR_CACHE_CODE.put(Byte.parseByte(compressorNum),compressorWrapper);
            return compressorName;
        }else {
            log.warn("配置的压缩类指定的名称或号码重复！");
            return null;
        }
    }

    /**
     * 根据字符串type获取实例
     * @param compressorType 字符串type
     * @return 压缩器实例
     */
    public static Compressor getCompressor(String compressorType){
        ObjectWrapper<?> wrapper = COMPRESSOR_CACHE_TYPE.get(compressorType);
        wrapper = validateWrapperNotNull(wrapper);
        return (Compressor) wrapper.getImpl();
    }

    /**
     * 根据字节数字code获取实例
     * @param compressorCode 字节数字code
     * @return 压缩器实例
     */
    public static Compressor getCompressor(byte compressorCode){
        ObjectWrapper<?> wrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        wrapper = validateWrapperNotNull(wrapper);
        return (Compressor) wrapper.getImpl();
    }

    /**
     * 根据字符串type获取字节数字code
     * @param compressorType 字符串type
     * @return 字节数字code
     */
    public static byte getCompressorCode(String compressorType){
        ObjectWrapper<?> wrapper = COMPRESSOR_CACHE_TYPE.get(compressorType);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 根据字节数字code获取字符串type
     * @param compressorCode 字节数字code
     * @return 字符串type
     */
    public static String getCompressorType(byte compressorCode){
        ObjectWrapper<?> wrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }

    /**
     * 判断wrapper是否为空 若是则采用默认gzip压缩的wrapper
     * @param wrapper 需要判断的wrapper
     * @return wrapper
     */
    private static ObjectWrapper<?> validateWrapperNotNull(ObjectWrapper<?> wrapper){
        if (wrapper == null){
            log.info("未匹配到指定的压缩类型,默认采用gzip压缩");
            wrapper = COMPRESSOR_CACHE_TYPE.get("gzip");
        }
        return wrapper;
    }
}
