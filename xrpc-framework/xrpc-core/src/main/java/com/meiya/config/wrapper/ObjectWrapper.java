package com.meiya.config.wrapper;

import com.meiya.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaopf
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper<T> {
    private byte code;
    private String type;
    private T impl;
}
