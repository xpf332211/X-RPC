package com.meiya.utils.print;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * @author xiaopf
 */
public class Error {
    public static void println(String s){
        try {
            java.lang.System.setOut(new PrintStream(java.lang.System.out, true, "UTF-8"));
            java.lang.System.err.println(s);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("错误输出时发生异常");
        }
    }
}
