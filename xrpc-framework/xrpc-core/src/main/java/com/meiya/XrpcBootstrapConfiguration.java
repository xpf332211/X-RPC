package com.meiya;

import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.impl.RoundRobinLoadBalancer;
import com.meiya.registry.Registry;
import com.meiya.serialize.Serializer;
import com.meiya.utils.IdGenerator;
import lombok.Data;
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
 * 代码配置 --> xml配置 --> spi配置 --> 默认配置
 * @author xiaopengfei
 */
@Slf4j
@Data
public class XrpcBootstrapConfiguration {
    /**
     * 服务提供方 主机端口
     */
    private int port = 8086;
    /**
     * 服务名称
     */
    private String applicationName = "defaultAppName";
    /**
     * 序列化类型 默认为jdk
     */
    private String serializeType = "jdk";

    /**
     * 压缩类型 默认为gzip
     */
    private String compressorType = "gzip";


    /**
     * 注册中心连接地址 默认为zk连接地址
     */
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    /**
     * 注册中心实例 默认为zk 在构造器中实例化赋值
     */
    private Registry registry;

    /**
     * id生成器
     */
    private IdGenerator idGenerator = new IdGenerator(2, 10);
    /**
     * 负载均衡器
     */
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    public XrpcBootstrapConfiguration() {
        //注意：register不能在成员变量中赋值！否则会有问题
//        registry = registryConfig.getRegistry();
        loadFromXml(this);
    }

    /**
     * 读取配置文件
     * dom4j框架适合 但此处不用
     * 此处用的是DocumentBuilderFactory结合Xpath
     * @param configuration 当前实例
     */
    private void loadFromXml(XrpcBootstrapConfiguration configuration) {

        try {
            //1.创建一个document
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("application.xml");
            Document document = documentBuilder.parse(inputStream);
            //2.获取一个xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            //3.解析xml中的标签表达式 若对象构造器含参 需要传入
            String expression = null;
            expression = "/configuration/compressor";
            String compressor = parseString(document, xPath, expression);
            System.out.println(compressor);
            expression = "/configuration/serializer";
            Serializer serializer = parseObject(document, xPath,expression,null);
            System.out.println(serializer);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("解析xml配置发生异常，采用默认配置");
        }


    }

    /**
     * 解析一个xml字符串节点 返回其字符串值
     * <compressor attributeName="gzip"></compressor>
     * 和其重载方法只能二选一
     * @param document 文档对象
     * @param xPath 解析器
     * @param expression xml标签表达式
     * @param attributeName 指定参数
     * @return 字符串值
     */
    private String parseString(Document document,XPath xPath,String expression,String attributeName){
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            Node targetNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            //获取attributeName指定的值 即类的全限定名
            return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("解析xml配置发生异常，采用默认配置");
        }
        return null;
    }

    /**
     * 解析一个xml字符串节点 返回其字符串值
     * <compressor>gzip</compressor>
     * 和其重载方法只能二选一
     * @param document 文档对象
     * @param xPath 解析器
     * @param expression xml标签表达式
     * @return 字符串值
     */
    private String parseString(Document document,XPath xPath,String expression){
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            Node targetNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            //获取attributeName指定的值 即类的全限定名
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("解析xml配置发生异常，采用默认配置");
        }
        return null;
    }
    /**
     * 解析一个xml对象节点 返回一个实例
     * <serializer class="com.meiya.serialize.impl.HessianSerializer"></serializer>
     * @param document 文档对象
     * @param xPath 解析器
     * @param expression xml标签表达式
     * @param paramType 参数类型列表
     * @param param 可变参数列表
     * @return 实例对象
     */
    private <T> T parseObject(Document document, XPath xPath,String expression,Class<?>[] paramType,Object... param) {
        try {
            XPathExpression xPathExpression = xPath.compile(expression);
            Node targetNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            //获取class指定的值 即类的全限定名
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            //根据类的全限定名 构造实例对象
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null){
                instance = aClass.getConstructor().newInstance();
            }else {
                instance = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instance;
        } catch (XPathExpressionException | ClassNotFoundException
                | InvocationTargetException | InstantiationException
                | IllegalAccessException | NoSuchMethodException e) {
            log.error("解析xml配置发生异常，采用默认配置");
        }
        return null;

    }

    public static void main(String[] args) {
        XrpcBootstrapConfiguration configuration = new XrpcBootstrapConfiguration();
    }
}
