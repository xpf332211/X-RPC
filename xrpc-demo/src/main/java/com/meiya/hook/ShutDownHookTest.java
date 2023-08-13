package com.meiya.hook;

import com.meiya.utils.print.Out;

/**
 * @author xiaopf
 */
public class ShutDownHookTest {
    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Out.println("jvm即将被被关闭");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Out.println("请求处理完毕，jvm关闭");
        }));

        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Out.println("处理请求。。");
        }

    }




}
