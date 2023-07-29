package com.meiya.utils.print;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * @author xiaopf
 */
public class Out {

    public static void println(String s){
        try {
            java.lang.System.setOut(new PrintStream(java.lang.System.out, true, "UTF-8"));
            java.lang.System.out.println(s);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("标准输出时发生异常");
        }
    }
}
