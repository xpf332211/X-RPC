package com.meiya.netty;

import com.meiya.netty.utils.BufLog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Test;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class nettyTest {
    @Test
    public void testByteBuf(){
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{1,2,3,4});
        buf.writeInt(5);
        BufLog.log(buf);
        buf.readByte();
        BufLog.log(buf);
    }

    @Test
    /**
     * Header
     *  Magic Number（4字节） 魔数，用于识别该协议
     *  Version（1字节）    版本协议号
     *  MessageType（1字节）    消息类型    请求/响应
     *  Serialization（1字节）  序列化类型
     *  RequestID（8字节）  请求ID
     *  Body Length（4字节）
     *  Head Length（4字节）
     *
     *  Body
     *   Request
     *      Service Name    被调用方的服务名称
     *      Method Name     被调用方法的名称
     *      Method Arguments Types   被调用方法的参数类型列表
     *      Method Argument 被调用方法的参数名称列表
     *   Response
     *      Status Code     响应状态码
     *      Error Message   错误信息
     *      Success Value   成功返回值
     */
    public void testMessage() throws IOException {
        ByteBuf message = ByteBufAllocator.DEFAULT.buffer(50);
        //4个字节 魔数 78727063
        message.writeBytes("xrpc".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
        // 用对象流转化为字节数据
        MessageBody body = new MessageBody();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(body);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);
        BufLog.log(message);
    }
     static class MessageBody implements Serializable {

    }

    @Test
    /**
     * 简单压缩
     */
    public void testCompress() throws IOException {
        byte[] bytes = new byte[]{24,54,87,43,67,89,65,
                24,54,87,43,67,89,65,24,54,87,43,67,89,65,
                24,54,87,43,67,89,65,24,54,87,43,67,89,65,
                24,54,87,43,67,89,65,24,54,87,43,67,89,65,
                24,54,87,43,67,89,65,24,54,87,43,67,89,65};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(baos);
        gos.write(bytes);
        gos.finish();
        byte[] zipBytes = baos.toByteArray();
        System.out.println(bytes.length + "===>" + zipBytes.length);
        System.out.println(Arrays.toString(zipBytes));
    }

    @Test
    /**
     * 简单解压
     */
    public void testDeCompress() throws IOException {
        byte[] zipBytes = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -109, 48, 11,
                -41, 118, -114, 116, -108, 32, -109, 2, 0, -63, -113, 85, 54, 63, 0, 0, 0};
        ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
        GZIPInputStream gis = new GZIPInputStream(bais);
        byte[] bytes = gis.readAllBytes();
        System.out.println(zipBytes.length + "===>" + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void testOut() throws UnsupportedEncodingException {
//        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        System.out.println("输出");
    }
}
