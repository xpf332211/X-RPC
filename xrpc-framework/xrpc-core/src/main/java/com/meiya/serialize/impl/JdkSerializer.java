package com.meiya.serialize.impl;

import com.meiya.exceptions.SerializeException;
import com.meiya.serialize.Serializer;
import com.meiya.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author xiaopf
 */
@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null){
            throw new NullPointerException("序列化目标对象为空！");
        }
        //序列化
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
        ) {
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化对象【{}】时发生异常！",object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            return (T) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            log.error("反序列化对象【{}】时发生异常",clazz);
            throw new SerializeException(e);
        }


    }
}
