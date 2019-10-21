package com.taobao.diamond.manager.impl;

import java.util.Arrays;
import java.util.List;

import com.taobao.diamond.manager.BaseStoneSubManager;
import com.taobao.diamond.manager.ManagerListener;

/**
 * ��ʹ��Diamond����½ӿ�
 * @author pingwei
 *
 */
@Deprecated
public class DefaultBaseStoneSubManager extends DefaultDiamondManager implements
        BaseStoneSubManager {

    /**
     * ������ݵĹ��췽��, ָ��������
     * 
     * @param dataId ����id
     * @param group ���÷���
     * @param managerListener
     *            ������, �����н��յ���ݱ仯��ص�
     */
    public DefaultBaseStoneSubManager(String dataId, String group, ManagerListener managerListener) {
        super(group, dataId, Arrays.asList(managerListener));
    }

    /**
     * ������ݵĹ��췽��, ָ���������б�
     * 
     * @param dataId  ����id
     * @param group ���÷���
     * @param managerListenerList
     *            �������б�, �����н��յ���ݱ仯�����λص��б��е�ÿһ��������
     */
    public DefaultBaseStoneSubManager(String dataId, String group,
            List<ManagerListener> managerListenerList) {
        super(group, dataId, managerListenerList);
    }

}
