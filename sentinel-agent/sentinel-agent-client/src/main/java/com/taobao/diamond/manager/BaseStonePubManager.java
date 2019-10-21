package com.taobao.diamond.manager;

import com.taobao.diamond.client.ContentIdentityPattern;
import com.taobao.diamond.client.DiamondConfigure;


/**
 * ʹ���µ�Diamond�ӿ��滻
 * 
 */
@Deprecated
public interface BaseStonePubManager {

	/**
	 * ��������µ������, �첽��ָ������Ψһ��ʶ��pattern
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param configInfo  ����ֵ
	 * @param pattern 
	 */
    void publish(String dataId, String group, String configInfo, ContentIdentityPattern pattern);



    /**
     * ��������µ������, ͬ��. ָ������Ψһ��ʶ��pattern.  method=syncUpdateConfig
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param configInfo  ����ֵ
     * @param timeout ��ʱ
     * @param pattern
     * @return �Ƿ񷢲��ɹ�
     */
    boolean syncPublish(String dataId, String group, String configInfo, long timeout,
            ContentIdentityPattern pattern);
    
	/**
	 * �滻���е�����������Ϣ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param configInfo  ����ֵ
	 */
    void publishAll(String dataId, String group, String configInfo);
    
    /**
     * �滻���е�����������Ϣ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param configInfo  ����ֵ
     * @param timeout ��ʱ
     * @return �Ƿ񷢲��ɹ�
     */
    boolean syncPublishAll(String dataId, String group, String configInfo, long timeout);

    /**
     * ɾ�����������Ŀ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
     */
    void removeAll(String dataId, String group);

    /**
     * ɾ�����������Ŀ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param timeout ��ʱ
	 * @return �Ƿ�ɾ��ɹ�
     */
    boolean syncRemoveAll(String dataId, String group, long timeout);



    /**
     * ��ȡ������ص�����
     * 
     * @return
     */
    DiamondConfigure getDiamondConfigure();


    /**
     * ���÷�����ص�����
     * 
     * @param diamondConfigure ���ýṹ��
     */
    void setDiamondConfigure(DiamondConfigure diamondConfigure);


    /**
     * �رշ�����
     */
    void close();


	/**
	 * ��������µ��������Ŀ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param datumId  �ۺ���ݵı�ʶ, datumId��ͬ����, ��ͬ׷��
	 * @param configInfo  ����ֵ
	 * @param timeout ��ʱ
	 * @return �Ƿ񷢲��ɹ�
	 */
    boolean publish(String dataId, String group, String datumId, String configInfo, long timeout);


    /**
     * ɾ��������Ŀ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param datumId  �ۺ���ݵı�ʶ, datumId��ͬ����, ��ͬ׷��
	 * @param timeout ��ʱ
	 * @return �Ƿ�ɾ��ɹ�
     */
    boolean unPublish(String dataId, String group, String datumId, long timeout);

    /**
     * ɾ�� dataId + group �������е������Ŀ
	 * @param dataId ����id
	 * @param group  ���÷ֽM
	 * @param timeout ��ʱ
	 * @return �Ƿ�ɾ��ɹ�
     */
    boolean unPublishAll(String dataId, String group, long timeout);
}
