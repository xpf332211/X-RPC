package com.meiya.netty.demo3;

import com.meiya.utils.print.Out;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author xiaopf
 */
public class MyCompletableFuture {
    public static void main(String[] args) {
        /**
         * 可以获取子线程中的结果，并在主线程中阻塞等待获取
         */
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i = 10;
            completableFuture.complete(i);
        }).start();

        try {
            Integer num = completableFuture.get(1, TimeUnit.SECONDS);
            if (num != null){
                Out.println(num.toString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
