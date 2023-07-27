package com.meiya;

/**
 * @author xiaopf
 */
public class ServiceConfig <T>{
    /**
     * 服务接口
     */
    private Class<T> interfaceServ;
    /**
     * 服务实现类
     */
    private Object ref;

    public Class<T> getInterface() {
        return interfaceServ;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceServ = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
