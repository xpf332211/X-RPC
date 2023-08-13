package com.meiya.channelhandler.handler;

import com.meiya.compress.Compressor;
import com.meiya.compress.CompressorFactory;
import com.meiya.enumeration.ResponseCode;
import com.meiya.serialize.Serializer;
import com.meiya.serialize.SerializerFactory;
import com.meiya.transport.message.MessageFormatConstant;
import com.meiya.transport.message.XrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * - magic 魔数	4字节 <br/>
 * - version 版本  1字节 <br/>
 * - header length 报文首部长度，长度单位为字节  2字节 <br/>
 * - full length  报文总长度，长度单位为字节  4字节 <br/>
 * - serializeType  序列化方式  1字节 <br/>
 * - compressType  压缩类型  1字节 <br/>
 * - responseCode  响应码  1字节 <br/>
 * - requestId请求id  8字节 <br/>
 * - responseBody 响应体  不定长 <br/>
 * 出站的第二个处理器，将XrpcResponse封装成报文byteBuf <br/>
 * @author xiaopf
 */
@Slf4j
public class ResponseEncodeHandler extends MessageToByteEncoder<XrpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, XrpcResponse xrpcResponse, ByteBuf byteBuf) throws Exception {
        //4个字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //2个字节的报文首部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //4个字节的报文总长度 此时还不确定报文总长度是多少，因为请求体长度还不确定
        //移动写指针 跳过4个字节的总长度字段 后续再重新处理
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_BYTES);
        //1个字节的序列化方式
        byteBuf.writeByte(xrpcResponse.getSerializeType());
        //1个字节的压缩类型
        byteBuf.writeByte(xrpcResponse.getCompressType());
        //1个字节的响应码
        byteBuf.writeByte(xrpcResponse.getResponseCode());
        //8个字节的请求id
        byteBuf.writeLong(xrpcResponse.getRequestId());

        //判断是否为普通请求的成功响应，是才需要处理响应体
        int bodyLength = 0;
        if (xrpcResponse.getResponseCode() == ResponseCode.SUCCESS.getCode()){
            Serializer serializer = SerializerFactory.getSerializer(xrpcResponse.getSerializeType());
            byte[] responseBody = serializer.serialize(xrpcResponse.getResponseBody());
            Compressor compressor = CompressorFactory.getCompressor(xrpcResponse.getCompressType());
            responseBody = compressor.compress(responseBody);
            //bodyLength个字节的响应体
            byteBuf.writeBytes(responseBody);
            bodyLength = responseBody.length;
        }


        //处理总长度字段的内容
        //1.保存当前写指针位置
        int index = byteBuf.writerIndex();
        //2.将写指针位置定位到总长度字段
        byteBuf.writerIndex(MessageFormatConstant.FULL_FIELD_LOCATION);
        //3.写入报文总长度（header+payload）
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        //4.将写指针归位
        byteBuf.writerIndex(index);
        log.info("id为【{}】的响应经过了报文封装",xrpcResponse.getRequestId());
    }
}
