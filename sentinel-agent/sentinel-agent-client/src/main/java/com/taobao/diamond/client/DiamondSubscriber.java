package com.taobao.diamond.client;

import java.util.List;
import java.util.Set;


/**
 * DiamondSubscriber���ڶ��ĳ־õ��ı�������Ϣ��<br>
 * ��ʹ��Diamond��
 * 
 * @author aoqiong
 * 
 */
@Deprecated
public interface DiamondSubscriber extends DiamondClientSub {

    /**
     * ��ȡһ�ݿ��õ�������Ϣ������<strong>�����ļ�->diamond������</strong>
     * ��˳���ȡһ����Ч��������Ϣ, �������;�����޷���ȡһ����Ч������Ϣ, �򷵻�null
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    String getAvailableConfigureInfomation(String dataId, String group, long timeout);
    
    /**
     * ͬgetAvailableConfigureInfomation��
     */
    String getConfigureInfomation(String dataId, String group, long timeout);

    /**
     * ��ȡ֧�ֵ����е�DataID
     */
    Set<String> getDataIds();
    
    List<String> getServerList();
    
}
