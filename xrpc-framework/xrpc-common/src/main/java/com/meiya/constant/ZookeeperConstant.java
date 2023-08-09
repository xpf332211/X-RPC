package com.meiya.constant;

/**
 * @author xiaopf
 */

public class ZookeeperConstant {
    /**
     * zk的默认连接地址
     */
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    /**
     * zk的默认连接超时时间
     */
    public static final int DEFAULT_ZK_TIMEOUT = 10 * 1000;

    /**
     * 服务提供方的根节点路径
     */
    public static final String BATH_PROVIDERS_PATH = "/xrpc-metadata/providers";

    /**
     * 服务调用方的根节点路径
     */
    public static final String BATH_CONSUMERS_PATH = "/XRPC-metadata/consumers";
}
