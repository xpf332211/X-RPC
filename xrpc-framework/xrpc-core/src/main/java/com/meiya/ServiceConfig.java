package com.meiya;

/**
 * @author xiaopf
 */
public class ServiceConfig <T>{
    /**
     * ����ӿ�
     */
    private Class<T> interfaceServ;
    /**
     * ����ʵ����
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
