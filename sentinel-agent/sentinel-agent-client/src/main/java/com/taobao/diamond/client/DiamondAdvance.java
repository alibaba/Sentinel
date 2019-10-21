package com.taobao.diamond.client;


import java.util.Arrays;
import java.util.List;

import com.taobao.diamond.client.impl.DiamondEnvRepo;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.manager.ManagerListener;

/**
 * Diamond advanced IMPL
 * 
 * @author Diamond
 *
 */
public class DiamondAdvance extends Diamond {

	/**
	 * ��Ӷ�tenant��dataId��group�ļ����ڷ�����޸ĸ����ú󣬿ͻ��˻�ʹ�ô����listener�ص�Ӧ�á�
	 * �Ƽ��첽���?Ӧ�ÿ���ʵ��ManagerListener�е�getExecutor�������ṩִ�е��̳߳ء����Ϊ�ṩ����ʹ�����̻߳ص���
	 * ���ܻ������������û��߱�������������
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param listener
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 * 
	 */
	static public void addListener(String tenant, String dataId, String group, ManagerListener listener)
			throws DiamondException {
		DiamondEnvRepo.getDefaultEnv().addListeners(tenant, dataId, group, Arrays.asList(listener));
	}

	/**
	 * ��һ��tenant��dataId��groupͬʱ��Ӷ��Listener���������ñ�������λص�Listener
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param listeners
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 * 
	 */
	static public void addListeners(String tenant, String dataId, String group, List<ManagerListener> listeners)
			throws DiamondException {
		DiamondEnvRepo.getDefaultEnv().addListeners(tenant, dataId, group, listeners);
	}

	/**
	 * ɾ���Ѿ���ӵ�Listenner
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param listener
	 * @throws DiamondException
	 * 
	 */
	static public void removeListener(String tenant, String dataId, String group, ManagerListener listener)
			throws DiamondException {
		DiamondEnvRepo.getDefaultEnv().removeListener(tenant, dataId, group, listener);
	}

	/**
	 * ��ȡһ�����õ�ȫ��Listener
	 * 
	 * @param dataId
	 * @param group
	 * @param tenant
	 * @return
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public List<ManagerListener> getListeners(String tenant, String dataId, String group)
			throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().getListeners(tenant, dataId, group);
	}

	/**
	 * ��ȡ����
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param timeoutMs
	 * @return
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public String getConfig(String tenant, String dataId, String group, long timeoutMs) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().getConfig(tenant, dataId, group, timeoutMs);
	}

	/**
	 * �������á���ʱʱ��Ϊ3��
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param appName
	 *            ���ù�����app name
	 * @param content
	 * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public boolean publishSingle(String tenant, String dataId, String group, String appName, String content)
			throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().publishSingle(tenant, dataId, group, appName, content);
	}

	/**
	 * ɾ������
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @return
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public boolean remove(String tenant, String dataId, String group) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().remove(tenant, dataId, group);
	}

	/**
	 * ������getConfig�ӿڣ�ʹ��ʱ��Ҫע�⴦��ConfigInfoEx��status״̬����batchQuery�ӿڵ���𣺸ýӿڽ���ѯ����˵Ļ�����ݣ���ֱ�Ӳ�ѯDB��
	 * 
	 * @param tenant
	 * @param dataIds
	 * @param group
	 * @param timeoutMs
	 * @return
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public BatchHttpResult<ConfigInfoEx> batchGetConfig(String tenant, List<String> dataIds, String group,
			long timeoutMs) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().batchGetConfig(tenant, dataIds, group, timeoutMs);
	}

	/**
	 * �����ۺ���ݣ���ʱʱ��Ϊ3��
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param datumId
	 * @param appName
	 *            ���ù�����app name
	 * @param content
	 * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public boolean publishAggr(String tenant, String dataId, String group, String datumId, String appName,
			String content) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().publishAggr(tenant, dataId, group, datumId, appName, content);
	}

	/**
	 * ɾ��ۺ����
	 * 
	 * @param tenant
	 * @param dataId
	 * @param group
	 * @param datumId
	 * @return
	 * @throws DiamondException
	 *             ��client�˲����쳣��server�˼�Ȩʧ���쳣
	 */
	static public boolean removeAggr(String tenant, String dataId, String group, String datumId)
			throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().removeAggr(tenant, dataId, group, datumId);
	}

}
