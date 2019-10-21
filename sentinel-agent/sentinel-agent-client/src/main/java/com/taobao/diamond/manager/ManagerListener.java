package com.taobao.diamond.manager;

import java.util.concurrent.Executor;


/**
 * �ͻ���������DataID��Ӧ��������Ϣ����Ҫ�Լ�ʵ��һ��������
 * 
 * @author aoqiong
 * 
 */
public interface ManagerListener {

    public Executor getExecutor();


    /**
     * ����������Ϣ
     * 
     * @param configInfo ����ֵ
     */
    public void receiveConfigInfo(final String configInfo);
}
