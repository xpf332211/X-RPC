package com.meiya.channelHandler.handler;

import com.meiya.enumeration.RequestType;
import com.meiya.transport.message.MessageFormatConstant;
import com.meiya.transport.message.RequestPayload;
import com.meiya.transport.message.XrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * 入站处理器 解析请求报文
 *
 * @author xiaopf
 */
@Slf4j
public class RequestDecodeHandler extends LengthFieldBasedFrameDecoder {

    public RequestDecodeHandler() {
        //截取报文
        super(
                //最大帧长度 超过会直接丢弃报文
                MessageFormatConstant.MAX_FRAME_LENGTH,
                //长度字段偏移量（报文总长度字段的起始位置）
                MessageFormatConstant.FULL_FIELD_LOCATION,
                //长度字段占用的字节数
                MessageFormatConstant.FULL_FIELD_BYTES,
                //负载的适配长度（总长度字段的末尾位置）
                -(MessageFormatConstant.FULL_FIELD_LOCATION + MessageFormatConstant.FULL_FIELD_BYTES),
                0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //解析成字节数组
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("获得的请求不合法！");
            }
        }
        //解析版本
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得的请求版本不被支持！");
        }
        //解析首部长度
        short headerLength = byteBuf.readShort();
        //解析总长度
        int fullLength = byteBuf.readInt();
        //解析序列化方式
        byte serializeType = byteBuf.readByte();
        //解析压缩类型
        byte compressType = byteBuf.readByte();
        //解析请求类型
        byte requestType = byteBuf.readByte();
        //解析请求id
        long requestId = byteBuf.readLong();
        //封装请求类 （缺少requestPayload）
        XrpcRequest xrpcRequest = XrpcRequest.builder()
                .serializeType(serializeType)
                .compressType(compressType)
                .requestType(requestType)
                .requestId(requestId)
                .build();
        //判断是否为心跳检测请求 若是则不需要解析请求体
        if (xrpcRequest.getRequestType() == RequestType.HEART_BETA.getId()){
            return xrpcRequest;
        }
        //解析请求体
        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(payload);
                ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
                RequestPayload requestPayload = (RequestPayload) ois.readObject();
                xrpcRequest.setRequestPayload(requestPayload);
        }catch (IOException | ClassNotFoundException e){
            log.error("请求【{}】反序列化时发生异常",requestId);
            throw new RuntimeException(e);
        }
        return xrpcRequest;

    }

}
