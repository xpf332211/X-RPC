package com.meiya.compress.impl;

import com.meiya.compress.Compressor;
import com.meiya.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author xiaopf
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            GZIPOutputStream gos = new GZIPOutputStream(baos);
            gos.write(bytes);
            gos.finish();
            byte[] compressBytes = baos.toByteArray();
            log.info("字节数组压缩完成,长度【{}】--->【{}】", bytes.length, compressBytes.length);
            return compressBytes;
        } catch (IOException e) {
            log.error("压缩时发生异常！");
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
        ) {
            GZIPInputStream gis = new GZIPInputStream(bais);
            byte[] decompressBytes = gis.readAllBytes();
            log.info("字节数组解压缩完成,长度【{}】--->【{}】", bytes.length, decompressBytes.length);
            return decompressBytes;
        } catch (IOException e) {
            log.error("解压缩时发生异常！");
            throw new CompressException(e);
        }


    }
}
