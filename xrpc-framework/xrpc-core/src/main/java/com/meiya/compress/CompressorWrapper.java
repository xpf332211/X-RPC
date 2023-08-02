package com.meiya.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaopf
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressorWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
