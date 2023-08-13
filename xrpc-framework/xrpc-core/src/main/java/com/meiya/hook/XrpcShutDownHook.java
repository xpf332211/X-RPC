package com.meiya.hook;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xiaopf
 */
@Slf4j
public class XrpcShutDownHook extends Thread{


    @Override
    public void run() {
        //1.打开挡板
        ShutDownHolder.BAFFLE.set(true);

        //2.等待处理请求
        waitForZero();

        //3.释放资源
    }

    public synchronized void waitForZero() {
        while (ShutDownHolder.REQUEST_COUNTER.sum() != 0) {
            try {
                //等待10s若请求还未处理完成，则直接强制关闭
                //未处理完的请求会等待超时，可能仍然会不断重试然后失败 不过数量较少就不做额外处理了
                wait(10 * 1000);
                if (ShutDownHolder.REQUEST_COUNTER.sum() != 0){
                    log.error("主机关闭前仍有部分请求超时未返回响应");
                    break;
                }
            } catch (InterruptedException e) {
                log.error("挡板开启后，等待处理未完成的请求时发生异常",e);
            }
        }
    }
}
