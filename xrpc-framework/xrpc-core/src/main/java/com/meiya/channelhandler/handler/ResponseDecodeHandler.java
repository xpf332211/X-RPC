package com.meiya.channelhandler.handler;

import com.meiya.compress.Compressor;
import com.meiya.compress.CompressorFactory;
import com.meiya.enumeration.ResponseCode;
import com.meiya.serialize.Serializer;
import com.meiya.serialize.SerializerFactory;
import com.meiya.transport.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 入站处理器 解析响应报文
 * @author xiaopf
 */
@Slf4j
public class ResponseDecodeHandler extends LengthFieldBasedFrameDecoder {
    public ResponseDecodeHandler() {
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
                throw new RuntimeException("获得的响应不合法！");
            }
        }
        //解析版本
        byte version = byteBuf.readByte();
        if (version < MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得的响应版本不被支持！");
        }
        //解析首部长度
        short headerLength = byteBuf.readShort();
        //解析总长度
        int fullLength = byteBuf.readInt();
        //解析序列化方式
        byte serializeType = byteBuf.readByte();
        //解析压缩类型
        byte compressType = byteBuf.readByte();
        //解析响应码
        byte responseCode = byteBuf.readByte();
        //解析请求id
        long requestId = byteBuf.readLong();
        //封装响应类 (缺少responseBody)
        XrpcResponse xrpcResponse = XrpcResponse.builder()
                .serializeType(serializeType)
                .compressType(compressType)
                .responseCode(responseCode)
                .requestId(requestId)
                .build();

        //判断是否为普通请求的成功响应，只有普通请求的成功响应才需要解析响应体
        if (xrpcResponse.getResponseCode() == ResponseCode.SUCCESS.getCode()){
            //解析响应体
            int bodyLength = fullLength - headerLength;
            byte[] body = new byte[bodyLength];
            byteBuf.readBytes(body);
            Compressor compressor = CompressorFactory.getCompressor(compressType);
            body = compressor.decompress(body);
            Serializer serializer = SerializerFactory.getSerializer(serializeType);
            ResponseBody responseBody = serializer.deserialize(body, ResponseBody.class);
            xrpcResponse.setResponseBody(responseBody);
            log.info("id为【{}】的响应经过了报文解析",xrpcResponse.getRequestId());
        }

        return xrpcResponse;
    }
}

