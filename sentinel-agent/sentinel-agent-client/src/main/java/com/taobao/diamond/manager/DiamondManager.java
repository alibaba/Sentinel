package com.taobao.diamond.manager;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.taobao.diamond.client.DiamondConfigure;


/**
 * ʹ��Diamond�ӿ�
 * 
 */
@Deprecated
public interface DiamondManager {

	/**
	 * ����µ�ҵ����ݼ�����
	 * @param newListeners ������
	 */
    void addListeners(List<ManagerListener> newListeners);
    
    /**
     * ɾ���Լ��ļ�������
     */
    void clearSelfListener();
    
    
    /**
     * ����ҵ����ݼ������� �����µļ������滻ԭ�е�ҵ����ݼ�������
     * @param managerListener ������
     */
    @Deprecated
    public void setManagerListener(ManagerListener managerListener);

    /**
     * ����ҵ����ݼ������� �����µļ������滻ԭ�е�ҵ����ݼ�������
     * @param managerListenerList �������б�
     */
    @Deprecated
    public void setManagerListeners(List<ManagerListener> managerListenerList);


    /**
     * ���ظ�DiamondManager���õ�listener�б�
     */
    public List<ManagerListener> getManagerListeners();


    /**
     * ͬ����ȡ������Ϣ,���ȼ�����������Ŀ¼ -> server
     * 
     * @param timeoutMs
     *            �������ȡ������Ϣ�ĳ�ʱ����λ����
     * @return
     */
    public String getConfigureInfomation(long timeoutMs);


    /**
     * ͬ����ȡһ����Ч��������Ϣ�����ȼ�����������Ŀ¼ -> server -> ����snapshot��
     * �����Щ;������Ч���򷵻�null
     * @param timeoutMs
     *            �������ȡ������Ϣ�ĳ�ʱ����λ����
     * @return
     */
    public String     getAvailableConfigureInfomation(long timeoutMs);
    
    /**
     * ͬ����ȡһ����Ч��������Ϣ�����ȼ�����������Ŀ¼ -> server -> ����snapshot��
     * �����Щ;������Ч���򷵻�null
     * @param timeoutMs
     *            �������ȡ������Ϣ�ĳ�ʱ����λ����
     * @return
     */
    public Properties getAvailablePropertiesConfigureInfomation(long timeoutMs);
    
    /**
     * ��������
     * @param diamondConfigure ����
     */
    public void setDiamondConfigure(DiamondConfigure diamondConfigure);
    public DiamondConfigure getDiamondConfigure();


    /**
     * �ر����DiamondManager
     */
    public void close();


    /**
     * ��ȡ��ǰ����ʹ�õķ������б�
     */
    public List<String> getServerAddress();


    /**
     * ��ȡע����������dataId
     */
    public Set<String> getAllDataId();

}
