package com.meiya.registry;

import com.meiya.ServiceConfig;

/**
 * @author xiaopf
 */
public interface Registry {
    /**
     * ע�����ĵ�ע��ʵ������
     * @param serviceConfig ������Ϣ����
     */
    void register(ServiceConfig<?> serviceConfig);
}
