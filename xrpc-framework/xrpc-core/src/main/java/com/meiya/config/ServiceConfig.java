package com.meiya.config;

/**
 * @author xiaopf
 */
public class ServiceConfig <T>{
    /**
     * 服务接口
     */
    private Class<?> interfaceServ;
    /**
     * 服务实现类
     */
    private Object ref;

    public Class<?> getInterface() {
        return interfaceServ;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceServ = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
