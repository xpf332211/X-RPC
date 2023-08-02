package com.meiya.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaopengfei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
