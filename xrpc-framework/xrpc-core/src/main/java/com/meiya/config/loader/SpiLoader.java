package com.meiya.config.loader;

import com.meiya.compress.Compressor;
import com.meiya.compress.CompressorFactory;
import com.meiya.config.XrpcBootstrapConfiguration;
import com.meiya.config.wrapper.ObjectWrapper;
import com.meiya.exceptions.SpiException;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.LoadBalancerFactory;
import com.meiya.serialize.Serializer;
import com.meiya.serialize.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author xiaopf
 */
@Slf4j
public class SpiLoader {
    /**
     * spi文本文件存放的根路径
     */
    private static final String BASE_PATH = "META-INF/xrpc-service";

    /**
     * 缓存spi读取后的内容
     * key->接口全限名  value->实现类全限名
     */
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);

    /**
     * 缓存接口对应的实现类实例
     */
    private static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(8);

    //需要从文件中读取数据 是一个IO操作 可以在类加载后先执行 缓存Spi数据
    //加载当前工程下的classPath
    static {
        ClassLoader classLoader = SpiLoader.class.getClassLoader();
        URL url = classLoader.getResource(BASE_PATH);
        if (url != null) {
            File file = new File(url.getPath());
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    String key = childFile.getName();
                    List<String> value = getImplNames(childFile);
                    if (value != null) {
                        SPI_CONTENT.put(key, value);
                    }
                }
            }

        }
    }

    /**
     * 读取spi
     */
    public static void loadFromSpi(XrpcBootstrapConfiguration configuration) {
        //将所有的配置加入工厂
        List<ObjectWrapper<Serializer>> serializerWrapperList = getAll(Serializer.class);
        for (ObjectWrapper<Serializer> wrapper : serializerWrapperList) {
            SerializerFactory.updateSerializerFactory(wrapper.getImpl(), wrapper.getType(), String.valueOf(wrapper.getCode()));
        }
        List<ObjectWrapper<Compressor>> compressorWrapperList = getAll(Compressor.class);
        for (ObjectWrapper<Compressor> wrapper : compressorWrapperList) {
            CompressorFactory.updateCompressorFactory(wrapper.getImpl(), wrapper.getType(), String.valueOf(wrapper.getCode()));
        }
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrapperList = getAll(LoadBalancer.class);
        for (ObjectWrapper<LoadBalancer> wrapper : loadBalancerWrapperList) {
            LoadBalancerFactory.updateLoadBalancerFactory(wrapper.getImpl(),wrapper.getType(),String.valueOf(wrapper.getCode()));
        }
        //设置配置
        ObjectWrapper<Serializer> serializerWrapper = getFirst(Serializer.class);
        if (serializerWrapper != null) {
            configuration.setSerializeType(serializerWrapper.getType());
        }
        ObjectWrapper<Compressor> compressorWrapper = getFirst(Compressor.class);
        if (compressorWrapper != null) {
            configuration.setCompressType(compressorWrapper.getType());
        }
        ObjectWrapper<LoadBalancer> loadBalancerWrapper = getFirst(LoadBalancer.class);
        if (loadBalancerWrapper != null) {
            configuration.setLoadBalancer(loadBalancerWrapper.getImpl());
        }
    }

    /**
     * 通过接口类型获取到所有实现类实例
     *
     * @param clazz 接口类型
     * @return 实现类实例集合
     */
    private synchronized static <T> List<ObjectWrapper<T>> getAll(Class<T> clazz) {
        //从缓存中获取实例
        List<ObjectWrapper<?>> impls = SPI_IMPLEMENT.get(clazz);
        if (impls != null && impls.size() > 0) {
            return impls.stream().map(impl ->
                    (ObjectWrapper<T>) impl
            ).collect(Collectors.toList());

        }
        //未获取到 创建并加入缓存
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        impls = new ArrayList<>();
        for (String implName : implNames) {
            try {
                //分割
                String[] codeAndTypeAndName = implName.split("-");
                if (codeAndTypeAndName.length != 3) {
                    throw new SpiException("spi文件不合法");
                }
                //构建objectWrapper
                Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];
                Class<?> aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type, impl);
                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | SpiException e) {
                log.error("实例化【{}】的实例时发生了异常", implName, e);
            }
        }
        SPI_IMPLEMENT.put(clazz, impls);
        return impls.stream().map(impl ->
                (ObjectWrapper<T>) impl
        ).collect(Collectors.toList());
    }

    /**
     * 通过接口类型获取到一个实现类实例
     * 可能会返回空的列表
     *
     * @param clazz 接口类型
     * @return 实现类实例
     */
    private static <T> ObjectWrapper<T> getFirst(Class<T> clazz) {
        List<ObjectWrapper<T>> all = getAll(clazz);
        if (all.isEmpty()) {
            return null;
        }
        return all.get(0);
    }

    /**
     * 读取文件中的每一行实现类全限名
     *
     * @param childFile 接口全限名文件
     * @return 类全限名集合
     */
    private static List<String> getImplNames(File childFile) {
        try (
                FileReader fileReader = new FileReader(childFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            List<String> implName = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) {
                    break;
                } else {
                    implName.add(line);
                }

            }
            return implName;
        } catch (IOException e) {
            log.error("读取spi文件【{}】发生异常", BASE_PATH + "/" + childFile.getName());
            return null;
        }
    }


    public static void main(String[] args) {
        loadFromSpi(null);
        System.out.println();
    }
}
