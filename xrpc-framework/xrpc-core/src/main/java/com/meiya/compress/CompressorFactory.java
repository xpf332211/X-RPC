package com.meiya.compress;

import com.meiya.compress.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopf
 */
@Slf4j
public class CompressorFactory {
    public static final Map<String,CompressorWrapper> COMPRESSOR_CACHE_TYPE = new ConcurrentHashMap<>(4);
    public static final Map<Byte,CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(4);
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
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCompressor();
    }

    /**
     * 根据字节数字code获取实例
     * @param compressorCode 字节数字code
     * @return 压缩器实例
     */
    public static Compressor getCompressor(byte compressorCode){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCompressor();
    }

    /**
     * 根据字符串type获取字节数字code
     * @param compressorType 字符串type
     * @return 字节数字code
     */
    public static byte getCompressorCode(String compressorType){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_TYPE.get(compressorType);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 根据字节数字code获取字符串type
     * @param compressorCode 字节数字code
     * @return 字符串type
     */
    public static String getCompressorType(byte compressorCode){
        CompressorWrapper wrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }

    /**
     * 判断wrapper是否为空 若是则采用默认gzip压缩的wrapper
     * @param wrapper 需要判断的wrapper
     * @return wrapper
     */
    private static CompressorWrapper validateWrapperNotNull(CompressorWrapper wrapper){
        if (wrapper == null){
            log.info("未匹配到指定的压缩类型,默认采用gzip压缩");
            wrapper = COMPRESSOR_CACHE_TYPE.get("gzip");
        }
        return wrapper;
    }
}
