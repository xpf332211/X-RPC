package com.meiya.config.loader;

import com.meiya.config.RegistryConfig;
import com.meiya.config.XrpcBootstrapConfiguration;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.LoadBalancerFactory;
import com.meiya.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author xiaopengfei
 */
@Slf4j
public class YmlLoader {
    public static void loadFromYml(XrpcBootstrapConfiguration configuration) {

        try {
            //配置端口
            int port = portParser();
            if (port != -1){
                configuration.setPort(port);
            }
            //配置应用名称
            String applicationName = applicationNameParser();
            if (applicationName != null){
                configuration.setApplicationName(applicationName);
            }
            //配置注册中心信息
            RegistryConfig registryConfig = registryConfigParser();
            if (registryConfig != null){
                configuration.setRegistryConfig(registryConfig);
            }
            //配置序列化类型
            String serializeType = serializeTypeParser();
            if (serializeType != null){
                configuration.setSerializeType(serializeType);
            }
            //配置压缩类型
            String compressType = compressTypeParser();
            if (compressType != null){
                configuration.setCompressType(compressType);
            }
            //配置负载均衡器
            LoadBalancer loadBalancer = loadBalanceTypeParser();
            if (loadBalancer != null){
                configuration.setLoadBalancer(loadBalancer);
            }
            //配置id生成器
            IdGenerator idGenerator = idGeneratorParser();
            if (idGenerator != null){
                configuration.setIdGenerator(idGenerator);
            }
            //配置注册中心
            if (registryConfig != null){
                configuration.setRegistry(configuration.getRegistryConfig().getRegistry());
            }
        }catch (YAMLException e){
            log.warn("未配置yml文件");
        }
    }

    private static String applicationNameParser() {
        return stringParser("xrpc.applicationName");
    }

    private static int portParser() {
        return intParser("xrpc.port");
    }

    private static IdGenerator idGeneratorParser() {
        IdGenerator idGenerator = null;
        String expression1 = "xrpc.idGenerator.class";
        String expression2 = "xrpc.idGenerator.dataCenterId";
        String expression3 = "xrpc.idGenerator.machineId";
        String className = stringParser(expression1);
        int dataCenterId = intParser(expression2);
        int machineId = intParser(expression3);
        if (StringUtils.isEmpty(className) || dataCenterId == -1 || machineId == -1){
            log.warn("您在yml中未配置机房号和机器号,若您为服务调用方,建议进行配置,否则在分布式下会有id冲突风险！" +
                    "若您已经在其他配置方式中指定了,请无视本条信息");
            return null;
        }
        return objectParser(className, new Class[]{long.class, long.class}, dataCenterId, machineId);
    }

    private static LoadBalancer loadBalanceTypeParser() {
        String expression = "xrpc.loadBalanceType";
        String loadBalanceType = stringParser(expression);
        if (loadBalanceType == null){
            return null;
        }
        return LoadBalancerFactory.getLoadBalancer(loadBalanceType);

    }

    private static String compressTypeParser() {
        String expression = "xrpc.compressType";
        return stringParser(expression);

    }

    private static String serializeTypeParser() {
        String expression = "xrpc.serializeType";
        return stringParser(expression);
    }

    private static RegistryConfig registryConfigParser() {
        String expression = "xrpc.registryConfig";
        RegistryConfig registryConfig = null;
        String url = stringParser(expression);
        if (StringUtils.isNotEmpty(url)){
            registryConfig = new RegistryConfig(url);
        }
        return registryConfig;
    }

    private static int intParser(String expression) {
        int intValue = -1;
        Object value = read(expression);
        if (value instanceof Integer) {
            intValue = (int) value;
        }
        return intValue;
    }
    private static String stringParser(String expression){
        String stringValue = null;
        Object value = read(expression);
        if (value instanceof String){
            stringValue = (String) value;
        }
        return stringValue;
    }
    private static <T> T objectParser(String className, Class<?>[] paramType, Object... param){
        try {
            //根据类的全限定名 构造实例对象
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null) {
                instance = aClass.getConstructor().newInstance();
            } else {
                instance = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instance;
        }  catch (InvocationTargetException | InstantiationException | IllegalAccessException
                  | ClassNotFoundException | NoSuchMethodException e) {
            log.error("解析yml配置发生异常",e);
            return null;
        }
    }

    private static Object read(String expression){
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("xrpc-application.yml");
        Yaml yml = new Yaml();
        Map<String, Object> config = yml.load(inputStream);
        String[] propertyPath = expression.split("\\.");
        Object value = config;
        for (String property : propertyPath) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(property);
            } else {
                break;
            }
        }
        return value;
    }
}
