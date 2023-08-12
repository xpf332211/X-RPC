package com.meiya.watcher;

import com.meiya.bootstrap.NettyBootstrap;
import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.exceptions.DiscoveryException;
import com.meiya.registry.Registry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiaopf
 */
@Slf4j
public class OnlineAndOfflineWatcher implements Watcher {
    @SneakyThrows
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("感知到服务【{}】下有节点上下线,将重新拉取服务", event.getPath());
            }
            String serviceName = getServiceName(event.getPath());
            Registry registry = XrpcBootstrap.getInstance().getConfiguration().getRegistry();
            //获取感知服务下的所有子节点的地址
            List<InetSocketAddress> addressList = new ArrayList<>();
            try {
                 addressList = registry.seekServiceList(serviceName);
            }catch (DiscoveryException e){
                //服务的所有主机均下线 清空缓存
                log.error("感知到服务下已经没有可用主机了");
                XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST.clear();
                XrpcBootstrap.CHANNEL_CACHE.clear();
            }


            //可能增加了节点 此时ALL_SERVICE_ADDRESS_LIST和CHANNEL_CACHE中都没有该节点
            for (InetSocketAddress address : addressList) {
                if (!XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST.contains(address)) {
                    XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST.add(address);
                }
                if (!XrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Bootstrap bootstrap = NettyBootstrap.getBootstrap();
                    Channel channel = bootstrap.connect(address).sync().channel();
                    XrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            }
            //可能下线了节点 此时ALL_SERVICE_ADDRESS_LIST和CHANNEL_CACHE可能还存在该节点 可能已经心跳检测剔除了
            //不过addressList中一定没有该节点
            //如果ALL_SERVICE_ADDRESS_LIST和CHANNEL_CACHE中有的节点 在addressList中没有 说明该节点应该被剔除
            for (InetSocketAddress address : XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST) {
                if (!addressList.contains(address)){
                    XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST.remove(address);
                }
            }
            for (Map.Entry<InetSocketAddress,Channel> entry : XrpcBootstrap.CHANNEL_CACHE.entrySet()){
                if (!addressList.contains(entry.getKey())){
                    XrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }
            //负载均衡缓存的主机列表未更新 需要重新负载均衡根据拉取的最新的服务列表 构建新的selector并加入缓存
            //若服务的主机均下线，则加入空的addressList
            XrpcBootstrap.getInstance().getConfiguration().getLoadBalancer().reLoadBalance(serviceName,addressList);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
