package com.meiya;

import com.meiya.utils.NetUtils;
import com.meiya.utils.ZookeeperUtils;
import com.meiya.utils.zk.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;


/**
 * @author xiaopf
 */
@Slf4j
public class XrpcBootstrap {


    private String applicationName;
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private ZooKeeper zooKeeper;
    private int port = 8080;


    //XrpcBootstrap是单例 采用饿汉式方法创建
    private static final XrpcBootstrap xrpcBootstrap = new XrpcBootstrap();

    private XrpcBootstrap(){
        //初始化
    }

    public static XrpcBootstrap getInstance(){
        return xrpcBootstrap;
    }


    /**
     * 定义当前应用的名称
     * @param applicationName   应用名称
     * @return  当前实例
     */
    public XrpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig 注册中心的封装
     * @return 当前实例
     */
    public XrpcBootstrap registry(RegistryConfig registryConfig) {
        //维护一个zk实例
        zooKeeper = ZookeeperUtils.createZookeeper();
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 配置协议
     * @param protocolConfig 协议的封装
     * @return 当前实例
     */
    public XrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //----------------------------------provider相关的api--------------------------------------

    /**
     * 服务发布
     * @param serviceConfig 需要发布的服务的封装
     * @return 当前实例
     */
    public XrpcBootstrap publish(ServiceConfig<?> serviceConfig) {
        if (serviceConfig.getInterface() == null){
            throw new NullPointerException("请配置需要发布的服务接口");
        }
        if (serviceConfig.getRef() == null){
            throw new NullPointerException("请配置需要发布的服务实现类");
        }
        //创建服务对应的根节点 为持久节点
        String serviceName = serviceConfig.getInterface().getName();
        String providersPath = Constant.BATH_PROVIDERS_PATH + '/' + serviceName;
        if (!ZookeeperUtils.exists(zooKeeper,providersPath,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(providersPath,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,null);
        }
        //创建服务对应的子节点 为临时节点 名称为ip:port
        //服务提供方的端口先直接定义好 还需要一个获取ip的方法
        String childServiceName = providersPath + '/' + NetUtils.getIp() + ':' + port;
        if (!ZookeeperUtils.exists(zooKeeper,childServiceName,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(childServiceName,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted){
                    log.info("节点【{}】已经被删除",childServiceName);
                }
            },null, CreateMode.EPHEMERAL);
        }
        return this;
    }

    /**
     * 批量 服务发布
     * @param serviceConfigList 需要发布的服务的封装的集合
     * @return 当前实例
     */
    public XrpcBootstrap publish(List<ServiceConfig> serviceConfigList){
        return this;
    }


    //----------------------------------consumer相关的api--------------------------------------


    /**
     * 代理对象配置
     * @param referenceConfig 代理对象配置
     * @return 当前实例
     */
    public XrpcBootstrap reference(ReferenceConfig<?> referenceConfig) {

        return this;
    }
}
