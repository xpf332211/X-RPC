package com.meiya.compress;

import com.meiya.compress.impl.GzipCompressor;
import com.meiya.exceptions.SerializeException;
import com.meiya.serialize.SerializerWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopf
 */
public class CompressorFactory {
    private static final Map<String,CompressorWrapper> COMPRESSOR_CACHE_TYPE = new ConcurrentHashMap<>(4);
    private static final Map<Byte,CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(4);
    static {
        CompressorWrapper gzip = new CompressorWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE_TYPE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte)1,gzip);
    }

    /**
     * 根据字符串type获取实例
     * @param compressorType 字符串type
     * @return 压缩器实例
     */
    public static Compressor getCompressor(String compressorType){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_TYPE.get(compressorType);
        validateWrapperNotNull(wrapper);
        return wrapper.getCompressor();
    }

    /**
     * 根据字节数字code获取实例
     * @param compressorCode 字节数字code
     * @return 压缩器实例
     */
    public static Compressor getCompressor(byte compressorCode){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        validateWrapperNotNull(wrapper);
        return wrapper.getCompressor();
    }

    /**
     * 根据字符串type获取字节数字code
     * @param compressorType 字符串type
     * @return 字节数字code
     */
    public static byte getCompressorCode(String compressorType){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_TYPE.get(compressorType);
        validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 根据字节数字code获取字符串type
     * @param compressorCode 字节数字code
     * @return 字符串type
     */
    public static String getCompressorType(byte compressorCode){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }
    /**
     * 判断wrapper是否为空 若是则抛出异常
     * @param wrapper wrapper
     */
    private static void validateWrapperNotNull(CompressorWrapper wrapper){
        if (wrapper == null){
            throw new SerializeException("未匹配到指定的压缩类型！");
        }
    }
}
