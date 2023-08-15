package com.meiya.config.loader;

import com.meiya.compress.Compressor;
import com.meiya.compress.CompressorFactory;

import com.meiya.config.RegistryConfig;
import com.meiya.config.XrpcBootstrapConfiguration;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.LoadBalancerFactory;
import com.meiya.serialize.Serializer;
import com.meiya.serialize.SerializerFactory;

import com.meiya.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author xiaopf
 */
@Slf4j
public class XmlLoader {

    /**
     * 读取配置文件
     * dom4j框架适合 但此处不用
     * 此处用的是DocumentBuilderFactory结合Xpath
     */
    public static void loadFromXml(XrpcBootstrapConfiguration configuration){
        try {
            //1.创建一个document
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("xrpc-application.xml");
            if (inputStream == null){
                return;
            }
            Document document = documentBuilder.parse(inputStream);
            //2.获取一个xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            //3.解析所有成员变量配置
            //解析端口
            int port = portParser(document, xPath);
            if (port != -1) {
                configuration.setPort(port);
            }
            //解析应用名称
            String applicationName = applicationNameParser(document, xPath);
            if (applicationName != null) {
                configuration.setApplicationName(applicationName);
            }
            RegistryConfig registryConfig = registryConfigParser(document, xPath);
            //解析注册中心配置
            if (registryConfig != null) {
                configuration.setRegistryConfig(registryConfig);
            }
            //解析序列化类型
            String serializeType = serializeTypeParser(document, xPath);
            if (serializeType != null) {
                configuration.setSerializeType(serializeType);
            }
            //解析压缩类型
            String compressType = compressTypeParser(document, xPath);
            if (compressType != null) {
                configuration.setCompressType(compressType);
            }
            //解析负载均衡器
            LoadBalancer loadBalancer = loadBalancerParser(document, xPath);
            if (loadBalancer != null) {
                configuration.setLoadBalancer(loadBalancer);
            }
            //解析id生成器
            IdGenerator generator = idGeneratorParser(document, xPath);
            if (generator != null) {
                configuration.setIdGenerator(generator);
            }
            //配置注册中心
            configuration.setRegistry(configuration.getRegistryConfig().getRegistry());


        }catch (IllegalArgumentException e){
            log.warn(("未配置xml文件"));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.warn("解析xml文件发生异常");
        }
    }


    /**
     * 解析负载均衡器
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return 负载均衡器
     */
    private static LoadBalancer loadBalancerParser(Document document, XPath xPath) {
        String expression1 = "/configuration/loadBalanceType[@type]";
        String expression2 = "/configuration/loadBalanceType[not(@type)]";
        String expression3 = "/configuration/loadBalancer[@class]";
        String expression4 = "/configuration/loadBalancer[@name]";
        String expression5 = "/configuration/loadBalancer[@num]";
        String loadBalanceType = null;
        LoadBalancer loadBalancer = parseObject(document, xPath, expression3, null);
        String loadBalancerName = parseString(document, xPath, expression4, "name");
        String loadBalancerNum = parseString(document,xPath,expression5,"num");
        //更新简单工厂的缓存
        loadBalancer = LoadBalancerFactory.updateLoadBalancerFactory(loadBalancer,loadBalancerName,loadBalancerNum);
        if (loadBalancer != null){
            return loadBalancer;
        }
        loadBalanceType = parseString(document, xPath, expression1, "type");
        if (loadBalanceType != null) {
            return LoadBalancerFactory.getLoadBalancer(loadBalanceType);
        }
        loadBalanceType = parseString(document, xPath, expression2);
        if (loadBalanceType != null){
            return LoadBalancerFactory.getLoadBalancer(loadBalanceType);
        }
        return null;
    }

    /**
     * 解析注册中心连接配置
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return 注册中心连接配置
     */
    private static RegistryConfig registryConfigParser(Document document, XPath xPath) {
        String expression = "/configuration/registryConfig[@connect]";
        String connect = parseString(document, xPath, expression, "connect");
        if (connect == null) {
            return null;
        }
        return new RegistryConfig(connect);
    }

    /**
     * 解析压缩类型
     * 优先级 class > 带参数 > 文本
     * 先解析class 并更新简单工厂 直接返回对应的字符串
     * 然后解析带参数标签 若解析结果不为空 则直接返回；若解析结果为空 则解析文本标签
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return 压缩类型
     */
    private static String compressTypeParser(Document document, XPath xPath) {
        String expression1 = "/configuration/compressType[@type]";
        String expression2 = "/configuration/compressType[not(@type)]";
        String expression3 = "/configuration/compressor[@class]";
        String expression4 = "/configuration/compressor[@name]";
        String expression5 = "/configuration/compressor[@num]";
        String compressType = null;
        Compressor compressor = parseObject(document, xPath, expression3, null);
        String compressorName = parseString(document, xPath, expression4, "name");
        String compressorNum = parseString(document,xPath,expression5,"num");
        //更新简单工厂的缓存
        compressType = CompressorFactory.updateCompressorFactory(compressor,compressorName,compressorNum);
        if (compressType != null){
            return compressType;
        }
        compressType = parseString(document, xPath, expression1, "type");
        if (compressType != null) {
            return compressType;
        }
        compressType = parseString(document, xPath, expression2);
        return compressType;
    }




    /**
     * 解析序列化方式
     * 优先级 class > 带参数 > 文本
     * 先解析class 并更新简单工厂 直接返回对应的字符串
     * 然后解析带参数标签 若解析结果不为空 则直接返回；若解析结果为空 则解析文本标签
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return 序列化方式
     */
    private static String serializeTypeParser(Document document, XPath xPath) {
        String expression1 = "/configuration/serializeType[@type]";
        String expression2 = "/configuration/serializeType[not(@type)]";
        String expression3 = "/configuration/serializer[@class]";
        String expression4 = "/configuration/serializer[@name]";
        String expression5 = "/configuration/serializer[@num]";
        String serializeType = null;
        Serializer serializer = parseObject(document, xPath, expression3, null);
        String serializerName = parseString(document, xPath, expression4, "name");
        String serializerNum = parseString(document,xPath,expression5,"num");
        //更新简单工厂的缓存
        serializeType = SerializerFactory.updateSerializerFactory(serializer,serializerName,serializerNum);
        if (serializeType != null){
            return serializeType;
        }
        serializeType = parseString(document, xPath, expression1, "type");
        if (serializeType != null) {
            return serializeType;
        }
        serializeType = parseString(document, xPath, expression2);
        return serializeType;
    }




    /**
     * 解析id生成器
     * 需要提供正确的机器号和机房号 否则无法构造
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return id生成器
     */
    private static IdGenerator idGeneratorParser(Document document, XPath xPath) {
        String expression1 = "/configuration/idGenerator[@dataCenterId]";
        String expression2 = "/configuration/idGenerator[@machineId]";
        String expression3 = "/configuration/idGenerator[@class]";
        String dataCenterIdStr = parseString(document, xPath, expression1, "dataCenterId");
        String machineIdStr = parseString(document, xPath, expression2, "machineId");
        if (dataCenterIdStr == null || machineIdStr == null || dataCenterIdStr.isEmpty() || machineIdStr.isEmpty()) {
            log.warn("您在xml中未配置机房号和机器号,若您为服务调用方,建议进行配置,否则在分布式下会有id冲突风险！" +
                    "若您已经在其他配置方式中指定了,请无视本条信息");
            dataCenterIdStr = "0";
            machineIdStr = "0";
        }
        Class<?>[] classes = {long.class, long.class};
        long dataCenterId = Long.parseLong(dataCenterIdStr);
        long machineId = Long.parseLong(machineIdStr);
        return parseObject(document, xPath, expression3, classes, dataCenterId, machineId);
    }

    /**
     * 解析应用名称
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return 应用名称
     */
    private static String applicationNameParser(Document document, XPath xPath) {
        String expression = "/configuration/applicationName";
        return parseString(document, xPath, expression);
    }

    /**
     * 解析端口号
     *
     * @param document 文档对象
     * @param xPath    解析器
     * @return 端口号
     */
    private static int portParser(Document document, XPath xPath) {
        String expression = "/configuration/port";
        String port = parseString(document, xPath, expression);
        if (port == null) {
            return -1;
        }
        return Integer.parseInt(port);
    }

    /**
     * 解析一个xml字符串节点 返回其字符串值
     * <compressor attributeName="gzip"/>
     *
     * @param document      文档对象
     * @param xPath         解析器
     * @param expression    xml标签表达式
     * @param attributeName 指定参数
     * @return 字符串值
     */
    private static String parseString(Document document, XPath xPath, String expression, String attributeName) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            Node targetNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            //未读取到节点
            if (targetNode == null) {
                return null;
            }
            //获取attributeName指定的值 即类的全限定名
            return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("解析xml配置发生异常，采用默认配置", e);

        }
        return null;
    }

    /**
     * 解析一个xml字符串节点 返回其字符串值
     * <compressor>gzip</compressor>
     *
     * @param document   文档对象
     * @param xPath      解析器
     * @param expression xml标签表达式
     * @return 字符串值
     */
    private static String parseString(Document document, XPath xPath, String expression) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            Node targetNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            //未读取到节点
            if (targetNode == null) {
                return null;
            }
            //获取attributeName指定的值 即类的全限定名
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("解析xml配置发生异常，采用默认配置", e);
        }
        return null;
    }

    /**
     * 解析一个xml对象节点 返回一个实例
     * <serializer class="com.meiya.serialize.impl.HessianSerializer"/>
     *
     * @param document   文档对象
     * @param xPath      解析器
     * @param expression xml标签表达式
     * @param paramType  参数类型列表
     * @param param      可变参数列表
     * @return 实例对象
     */
    private static <T> T parseObject(Document document, XPath xPath, String expression, Class<?>[] paramType, Object... param) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            Node targetNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            if (targetNode == null) {
                return null;
            }
            //获取class指定的值 即类的全限定名
            Node node = targetNode.getAttributes().getNamedItem("class");
            //未获取到节点
            if (node == null) {
                return null;
            }
            String className = node.getNodeValue();
            //根据类的全限定名 构造实例对象
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null) {
                instance = aClass.getConstructor().newInstance();
            } else {
                instance = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instance;
        } catch (XPathExpressionException | ClassNotFoundException
                | InvocationTargetException | InstantiationException
                | IllegalAccessException | NoSuchMethodException e) {
            log.error("解析xml配置发生异常，采用默认配置", e);
        }
        return null;

    }


    public static void main(String[] args) {
        //执行前注意注释掉注册中心的配置
        XrpcBootstrapConfiguration configuration = new XrpcBootstrapConfiguration();
    }
}
