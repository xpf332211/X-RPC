package com.meiya.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.meiya.exceptions.SerializeException;
import com.meiya.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author xiaopf
 * 测试xml配置
 */
@Slf4j
public class HessianSerializer2 implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ){
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] bytes = baos.toByteArray();
            log.info("hessian2序列化对象【{}】完成,序列化后的字节数为【{}】",object,bytes.length);
            return bytes;
        }catch (IOException e){
            log.error("hessian2序列化对象【{}】时发生异常！",object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
        ){
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            Object object = hessian2Input.readObject(clazz);
            log.info("hessian2反序列化类【{}】完成",clazz);
            return (T) object;
        }catch (IOException e){
            log.error("hessian2反序列化类【{}】时发生异常！",clazz);
            throw new SerializeException(e);
        }
    }
}
