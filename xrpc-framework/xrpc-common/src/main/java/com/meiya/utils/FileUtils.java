package com.meiya.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaopengfei
 */
public class FileUtils {

    /**
     * 根据包名称获取其下的 类全限定名集合
     * @param packageName 包名称
     * @return 类全限定名集合
     */
    public static List<String> getAllClassNamesByPackageName(String packageName){
        //1.根据包名获取绝对路径 com.meiya ----> D://xxx/yyy/.../com/meiya/xxx

        //1.1 com.meiya --->  com\meiya
        String bathPath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(bathPath);
        if (url == null){
            throw new RuntimeException("包扫描时，发现路径不存在");
        }
        //1.2 --->   /D:/xpf/RPC/X-RPC/xrpc-framework/xrpc-core/target/classes/com/meiya
        String absolutePath = url.getPath();

        //2.递归获取绝对路径下的类全限定名集合
        List<String> classNameList = new ArrayList<>();
        classNameList = recursionFile(absolutePath,classNameList,bathPath);
        return classNameList;
    }



    /**
     * 递归 通过文件夹的绝对路径 递归获取其下的类全限定名集合
     * @param absolutePath 文件夹的绝对路径
     * @param classNameList 类全限定名集合
     * @param bathPath 基础路径 com\meiya 用于分割
     * @return 类全限定名集合
     */
    private static List<String> recursionFile(String absolutePath, List<String> classNameList,String bathPath) {
        File file = new File(absolutePath);
        if (file.isDirectory()){
            //判断是文件夹，拿到文件夹下的所有[满足条件：是文件夹 或者 包含.class]的子文件
            File[] childFiles = file.listFiles(f -> f.isDirectory() || f.getPath().contains(".class"));
            //文件夹内没有文件了 结束递归
            if (childFiles == null || childFiles.length == 0){
                return classNameList;
            }
            //遍历文件夹内的满足条件的文件
            for (File childFile : childFiles) {
                if (childFile.isDirectory()){
                    //子文件是文件夹 递归调用
                    recursionFile(childFile.getAbsolutePath(), classNameList,bathPath);
                }else {
                    //子文件是文件
                    //打印效果 D:\xpf\RPC\X-RPC\xrpc-framework\xrpc-core\target\classes\com\meiya\channelhandler\handler\RequestDecodeHandler.class
                    String childFileAbsolutePath = childFile.getAbsolutePath();
                    //通过子文件的绝对路径获取文件名 转换成com\meiya\channelhandler\handler\RequestDecodeHandler
                    String className = getClassNameByAbsolutePath(childFileAbsolutePath,bathPath);
                    classNameList.add(className);
                }
            }

        }else {
            //判断是文件
            // 打印效果 D:\xpf\RPC\X-RPC\xrpc-framework\xrpc-core\target\classes\com\meiya\channelhandler\ConsumerChannelInitializer.class
            // ↓转换成 com\meiya\channelhandler\ConsumerChannelInitializer
            String className = getClassNameByAbsolutePath(absolutePath,bathPath);
            classNameList.add(className);
        }
        return classNameList;
    }

    /**
     * 将绝对路径转换成类的全限定名
     * @param childFileAbsolutePath 绝对路径
     * @param bathPath 基础路径 com\meiya 用于分割
     * @return 类的全限定名
     */
    private static String getClassNameByAbsolutePath(String childFileAbsolutePath,String bathPath) {
        //D:\xpf\RPC\X-RPC\xrpc-framework\xrpc-core\target\classes\com\meiya\channelhandler\handler\RequestDecodeHandler.class
        // ↓ 转换成
        // com\meiya\channelhandler\handler\RequestDecodeHandler.class
        // ↓ 转换成 fileName
        // com.meiya.channelhandler.handler.RequestDecodeHandler.class
        // ↓ 转换成 className
        // com.meiya.channelhandler.handler.RequestDecodeHandler
        String fileName = childFileAbsolutePath
                .substring(childFileAbsolutePath.indexOf(bathPath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");
        return fileName.substring(0,fileName.indexOf(".class"));



    }
}
