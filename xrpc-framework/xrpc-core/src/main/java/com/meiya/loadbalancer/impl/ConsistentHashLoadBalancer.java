package com.meiya.loadbalancer.impl;

import com.meiya.XrpcBootstrap;
import com.meiya.loadbalancer.AbstractLoadBalancer;
import com.meiya.loadbalancer.Selector;
import com.meiya.transport.message.XrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author xiaopf
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    private static class ConsistentHashSelector implements Selector {

        private final SortedMap<Integer, InetSocketAddress> hashCircle = new TreeMap<>();
        private int virtualNodeNum;

        /**
         * 带选取的服务列表
         */
        private final List<InetSocketAddress> serviceList;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodeNum) {
            this.serviceList = serviceList;
            this.virtualNodeNum = virtualNodeNum;
            for (InetSocketAddress serviceNode : serviceList) {
                addNodeToCircle(serviceNode);
            }
        }


        //一致性Hash
        @Override
        public InetSocketAddress loadBalance() {
            XrpcRequest xrpcRequest = XrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = Long.toString(xrpcRequest.getRequestId());
            int hashCode = getHash(requestId);
            if (hashCircle.isEmpty()) {
                return null;
            }
            //获取比该hashCode大的所有子服务节点
            //即顺时针寻找比服务调用方hashCode大的所有服务提供方主机
            SortedMap<Integer, InetSocketAddress> tailMap = hashCircle.tailMap(hashCode);
            //若获取的所有服务节点为空，则返回Hash环的第一个服务节点 否则返回子服务节点的第一个
            //即在这些服务提供方主机中寻找到第一个
            int firstNodeCode = tailMap.isEmpty() ? hashCircle.firstKey() : tailMap.firstKey();
            return hashCircle.get(firstNodeCode);
        }

        /**
         * 将服务节点转化成虚拟节点 并挂载到Hash环上
         * @param serviceNode 服务节点
         */
        private void addNodeToCircle(InetSocketAddress serviceNode) {
            for (int i = 0; i < virtualNodeNum; i++) {
                String virtualServiceNode = serviceNode.toString() + "-" + i;
                int hashCode = getHash(virtualServiceNode);
                hashCircle.put(hashCode, serviceNode);
                if (log.isDebugEnabled()) {
                    log.debug("虚拟节点【{}】已经挂载到Hash环上,其hash值为【{}】", virtualServiceNode, hashCode);
                }
            }
        }

        /**
         * 将服务节点对应的虚拟节点从Hash环上移除
         * @param serviceNode 服务节点
         */
        private void removeNodeToCircle(InetSocketAddress serviceNode) {
            for (int i = 0; i < virtualNodeNum; i++) {
                int hashCode = getHash(serviceNode.toString() + "-" + i);
                hashCircle.remove(hashCode, serviceNode);
            }
        }

        /**
         * 根据虚拟服务节点 获取hash值
         * 该方法来自gpt
         * @param key 虚拟服务节点
         * @return hash值
         */
        private int getHash(String key) {
//             使用MD5哈希函数计算哈希值
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                // 如果不支持MD5算法，可以使用其他哈希算法
                throw new RuntimeException("Hash algorithm not supported.", e);
            }

            byte[] keyBytes = key.getBytes();
            md.update(keyBytes);
            byte[] digest = md.digest();

            // 将哈希字节数组转换为整数哈希值(取前四字节)
            int hash = 0;
            for (int i = 0; i < 4; i++) {
                //将结果左移八位
                hash <<= 8;
                //与字节进行与操作 若字节为负数 会以补码形式转成int
                hash |= (digest[i] & 0xFF);
            }
            return hash;
        }
    }

}
