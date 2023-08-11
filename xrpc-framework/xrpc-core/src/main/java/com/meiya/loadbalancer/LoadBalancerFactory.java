package com.meiya.loadbalancer;

import com.meiya.config.wrapper.ObjectWrapper;
import com.meiya.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.meiya.loadbalancer.impl.RoundRobinLoadBalancer;
import com.meiya.loadbalancer.impl.ShortestResponseTimeLoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopf
 */
@Slf4j
public class LoadBalancerFactory {
    /**
     * LoadBalancerWrapper 缓存 通过type取
     */
    public static final Map<String, ObjectWrapper<?>> LOADBALANCER_CACHE_TYPE = new ConcurrentHashMap<>(4);
    /**
     * LoadBalancerWrapper 缓存 通过code取
     */
    public static final Map<Byte,ObjectWrapper<?>> LOADBALANCER_CACHE_CODE = new ConcurrentHashMap<>(4);
    static {
        ObjectWrapper<LoadBalancer> roundRobinWrapper = new ObjectWrapper<>((byte) 1, "roundRobin", new RoundRobinLoadBalancer());
        ObjectWrapper<LoadBalancer> consistentHashWrapper = new ObjectWrapper<>((byte) 2, "consistentHash", new ConsistentHashLoadBalancer());
        ObjectWrapper<LoadBalancer> shortestResponseTimeWrapper = new ObjectWrapper<>((byte) 3, "shortestResponseTime", new ShortestResponseTimeLoadBalancer());
        LOADBALANCER_CACHE_TYPE.put("roundRobin",roundRobinWrapper);
        LOADBALANCER_CACHE_TYPE.put("consistentHash",consistentHashWrapper);
        LOADBALANCER_CACHE_TYPE.put("shortestResponseTime",shortestResponseTimeWrapper);
        LOADBALANCER_CACHE_CODE.put((byte) 1,roundRobinWrapper);
        LOADBALANCER_CACHE_CODE.put((byte) 2,consistentHashWrapper);
        LOADBALANCER_CACHE_CODE.put((byte) 3,shortestResponseTimeWrapper);
    }

    /**
     * 配置自定义负载均衡器 更新负载均衡器工厂缓存
     * @param loadBalancer 负载均衡器实例
     * @param loadBalancerName 负载均衡器名称
     * @param loadBalancerNum 负载均衡器编号
     * @return 负载均衡器实例
     */
    public static LoadBalancer updateLoadBalancerFactory(LoadBalancer loadBalancer, String loadBalancerName, String loadBalancerNum) {
        if (loadBalancer == null || loadBalancerName == null || loadBalancerNum == null){
            return null;
        }
        if (!LoadBalancerFactory.LOADBALANCER_CACHE_TYPE.containsKey(loadBalancerName)
                && !LoadBalancerFactory.LOADBALANCER_CACHE_CODE.containsKey(Byte.parseByte(loadBalancerNum))){
            ObjectWrapper<LoadBalancer> loadBalancerWrapper = new ObjectWrapper<>(Byte.parseByte(loadBalancerNum), loadBalancerName, loadBalancer);
            LoadBalancerFactory.LOADBALANCER_CACHE_TYPE.put(loadBalancerName,loadBalancerWrapper);
            LoadBalancerFactory.LOADBALANCER_CACHE_CODE.put(Byte.parseByte(loadBalancerNum),loadBalancerWrapper);
            return loadBalancer;
        }else {
            log.warn("配置的负载均衡器指定的名称或号码重复！");
            return null;
        }
    }

    /**
     * 根据字符串type获取实例
     * @param loadBalanceType 字符串type
     * @return 负载均衡器实例
     */
    public static LoadBalancer getLoadBalancer(String loadBalanceType) {
        ObjectWrapper<?> wrapper = LOADBALANCER_CACHE_TYPE.get(loadBalanceType);
        wrapper = validateWrapperNotNull(wrapper);
        return (LoadBalancer) wrapper.getImpl();
    }

    /**
     * 根据byte数字获取实例
     * @param loadBalanceCode byte数字
     * @return 负载均衡器实例
     */
    public static LoadBalancer getLoadBalancer(byte loadBalanceCode){
        ObjectWrapper<?> wrapper = LOADBALANCER_CACHE_CODE.get(loadBalanceCode);
        wrapper = validateWrapperNotNull(wrapper);
        return (LoadBalancer) wrapper.getImpl();
    }

    /**
     * 根据byte数字获取对应的字符串type
     * @param loadBalanceCode byte数字
     * @return 字符串type
     */
    public static String getLoadBalanceType(byte loadBalanceCode){
        ObjectWrapper<?> wrapper = LOADBALANCER_CACHE_CODE.get(loadBalanceCode);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getType();
    }

    /**
     * 根据字符串type获取对应的byte数字
     * @param loadBalanceType 字符串type
     * @return byte数字
     */
    public static byte getLoadBalanceCode(String loadBalanceType){
        ObjectWrapper<?> wrapper = LOADBALANCER_CACHE_TYPE.get(loadBalanceType);
        wrapper = validateWrapperNotNull(wrapper);
        return wrapper.getCode();
    }

    /**
     * 判断wrapper是否为空 若是则采用默认jdk序列的wrapper
     * @param wrapper 需要判断的wrapper
     * @return wrapper
     */
    private static ObjectWrapper<?> validateWrapperNotNull(ObjectWrapper<?> wrapper){
        if (wrapper == null){
            log.info("未匹配到指定的负载均衡类型,默认采用轮询负载均衡算法");
            wrapper = LOADBALANCER_CACHE_TYPE.get("roundRobin");
        }
        return wrapper;
    }
}
