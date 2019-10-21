package com.taobao.diamond.client;

import com.taobao.diamond.client.impl.*;
import com.taobao.diamond.domain.*;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.manager.ManagerListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.taobao.diamond.client.impl.DiamondEnvRepo;

/**
 * �ͻ��˹��ܣ�
 * <ul>
 * <li>����/ֱ�������
 * <li>�������
 * <li>ͬ������
 * <li>���������ļ�
 * <li>���ػ����ļ���ע��windowsϵͳ�ļ�������
 * <li>����ʱ��⻺����Ч��
 * 
 * @author jiuRen
 */
public class Diamond {

	/**
	 * ��Ӷ�dataId��group�ļ����ڷ�����޸ĸ����ú󣬿ͻ��˻�ʹ�ô����listener�ص�Ӧ�á�
	 * �Ƽ��첽���?Ӧ�ÿ���ʵ��ManagerListener�е�getExecutor�������ṩִ�е��̳߳ء����Ϊ�ṩ����ʹ�����̻߳ص���
	 * ���ܻ������������û��߱�������������
	 * @param dataId
	 * @param group
	 * @param listener
	 */
    static public void addListener(String dataId, String group, ManagerListener listener) {
		DiamondEnvRepo.getDefaultEnv().addListeners(dataId, group, Arrays.asList(listener));
    }

    /**
     * ��һ��dataId��groupͬʱ��Ӷ��Listener���������ñ�������λص�Listener
     * @param dataId
     * @param group
     * @param listeners
     */
    static public void addListeners(String dataId, String group, List<ManagerListener> listeners) {
		DiamondEnvRepo.getDefaultEnv().addListeners(dataId, group, listeners);
    }
    
    /**
     * ɾ���Ѿ���ӵ�Listenner
     * @param dataId
     * @param group
     * @param listener
     */
    static public void removeListener(String dataId, String group, ManagerListener listener) {
		DiamondEnvRepo.getDefaultEnv().removeListener(dataId, group, listener);
    }
    
    /**
     * ��ȡһ�����õ�ȫ��Listener
     * @param dataId
     * @param group
     * @return
     */
    static public List<ManagerListener> getListeners(String dataId, String group) {
        return DiamondEnvRepo.getDefaultEnv().getListeners(dataId, group);
    }

    /**
     * ���ձ������� -> server -> ���ػ�������ȼ���ȡ���á���ʱ��λ�Ǻ��롣
     */
    static public String getConfig(String dataId, String group, long timeoutMs) throws IOException {
        return DiamondEnvRepo.getDefaultEnv().getConfig(dataId, group, timeoutMs);
    }
    
    /**
     * ��ȡ��ݽӿڣ��������û�ȡ��ݵ�˳�������������ȣ���<br>
     * feature��������ѡֵ��<br>
     * Constants.GETCONFIG_LOCAL_SERVER_SNAPSHOT(�����ļ�-> ������ -> ���ػ���)<br>
     * Constants.GETCONFIG_LOCAL_SNAPSHOT_SERVER(�����ļ�-> ���ػ��� -> ������)
	 * Constants.GETCONFIG_ONLY_SERVER(�����ļ�-> ������)
     */
    static public String getConfig(String dataId, String group, int feature, long timeoutMs) throws IOException{
    	return DiamondEnvRepo.getDefaultEnv().getConfig(dataId, group, feature, timeoutMs);
    }
    
    /**
     * �ӱ���snapshot�л�ȡ���õĿ��գ�����֤���¡�
     * ���ʹ�ó������������õ���ֵ����������ֵ���������ٸ��µĻ������Կ���ʹ������������١�
     * 
     * @param dataId
     * @param group
     * @return ������ò����ڡ���ȡ�ļ������쳣���ýӿڷ���null������ļ����ڵ�������Ϊ�գ�����""����������������쳣����³��֣�Ӧ��Ҫע�⴦�?
     */
    public static String getConfigFromSnapShot(String dataId, String group){
    	return DiamondEnvRepo.getDefaultEnv().getConfigFromSnapshot(TenantUtil.getUserTenant(), dataId, group);
    }

    /**
     * �����Ǿۺ���ݡ���ʱʱ��Ϊ3��
     * @param dataId
     * @param group
     * @param content
     * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
     */
    static public boolean publishSingle(String dataId, String group, String content) {
        return DiamondEnvRepo.getDefaultEnv().publishSingle(dataId, group, content);
    }

	/**
	 * CAS�����Ǿۺ���ݡ���ʱʱ��Ϊ3��
	 * @param dataId
	 * @param group
	 * @param content
	 * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
	 */
	static public boolean publishSingleCas(String dataId, String group, String expect, String update) {
		return DiamondEnvRepo.getDefaultEnv().publishSingleCas(dataId, group, expect, update);
	}
    
    /**
	 * �����Ǿۺ���ݡ���ʱʱ��Ϊ3��
	 * @param dataId
	 * @param group
	 * @param appName  ���ù�����app name
	 * @param content
	 * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
	 */
	static public boolean publishSingle(String dataId, String group, String appName, String content) {
		return DiamondEnvRepo.getDefaultEnv().publishSingle(dataId, group, appName, content);
	}



    /**
     * �����ۺ���ݣ���ʱʱ��Ϊ3��
     * @param dataId
     * @param group
     * @param datumId
     * @param content
     * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
     */
    static public boolean publishAggr(String dataId, String group, String datumId, String content) {
        return DiamondEnvRepo.getDefaultEnv().publishAggr(dataId, group, datumId, content);
    }
    
    /**
     * �����ۺ���ݣ���ʱʱ��Ϊ3��
     * @param dataId
     * @param group
     * @param datumId
     * @param appName  ���ù�����app name
     * @param content
     * @return true��ʾ�����ɹ����������еĳ�����Ϊfalse�������ӳ�ʱ��������ʱ����������ȵȣ�������Բ鿴�ͻ�����־
     */
	static public boolean publishAggr(String dataId, String group, String datumId, String appName, String content) {
		return DiamondEnvRepo.getDefaultEnv().publishAggr(dataId, group, datumId, appName, content);
	}

	static public boolean remove(String dataId, String group) {
		return DiamondEnvRepo.getDefaultEnv().remove(dataId, group);
	}

    static public boolean removeAggr(String dataId, String group, String datumId) {
        return DiamondEnvRepo.getDefaultEnv().removeAggr(dataId, group, datumId);
    }

    /**
     * ͨ��IP�б���Թ����һ��ָ����Ⱥ��DiamondEnvʵ�����ͨ�����ʵ���������ݡ���Ҫʹ�ó������ڿ绷���ķ��ʡ�����ʹ��vip������ʹ������
     * ����ǿ绷����ʹ�øù��ܣ�������ϵDiamond��ͬѧ
     * @param serverIps
     * @return
     */
    static public DiamondEnv getTargetEnv(String... serverIps) {
        return DiamondEnvRepo.getTargetEnv(serverIps);
    }

    /**
     * ָ����ַ��������IP�Ͷ˿ڣ�host������IP��VIP�����������ķ������Ĺ�����ʹ�ø÷������ص�DiamondEnv����Ҫʹ��Diamond�ࡣ
     * <br>ϵͳ��Ȼ�ᴴ��һ��Ĭ�ϵ�DiamondEnv��ʹ��Diamond��Ľӿ���Ȼ��ʹ��Ĭ�ϵ�DiamondEnv��
     * @param host
     * @param port
     * @return
     */
    static public DiamondEnv getTargetEnv (String host, int port) {
        return DiamondEnvRepo.getTargetEnv(host, port);
    }
    
    /**
     * ��ȡ����ͨ��getTargetEnv������DiamondEnvʵ��������е�Diamond������
     * @return
     */
    static public List<DiamondEnv> allDiamondEnvs() {
        return DiamondEnvRepo.allDiamondEnvs();
    }

    /**
     * ������ѯ�����ص�{@link BatchHttpResult}�а���{@link ConfigInfoEx}���б?����ѯʧ�ܣ����ߴ���Ӧ��ԭ
     * {@link ConfigInfoEx}����ʧ�ܣ��򷵻�ʧ�ܵ�{@link BatchHttpResult}����ѯ�ɹ�ʱ��
     * {@link ConfigInfoEx#getStatus()}
     * ��ʾ����Ƿ���ڣ�1��ʾ���ڣ�2��ʾ�����ڣ�-1��ʾ��ѯ��ݿⷢ���쳣��ʹ��������Ҫ��Ϊ�˼���ԭ�е�SDK�ӿڡ�
     * <br>�ýӿ�ֱ�Ӳ�ѯDB��ʹ��ʱ����ϵDiamond�Ŀ���ͬѧ�����Ƿ����ʹ�ô˽ӿ�
     * 
     * @param dataIds
     *            Ҫ��ѯ��dataId�б�
     * @param group
     *            ����
     * @param timeoutMs
     *            ��ʱʱ��
     * @return {@link BatchHttpResult}����֤��ΪNULL.
     */
    static public BatchHttpResult<ConfigInfoEx> batchQuery(List<String> dataIds, String group,
            long timeoutMs) {
        return DiamondEnvRepo.getDefaultEnv().batchQuery(dataIds, group, timeoutMs);
    }

    /**
     * ������getConfig�ӿڣ�ʹ��ʱ��Ҫע�⴦��ConfigInfoEx��status״̬����batchQuery�ӿڵ���𣺸ýӿڽ���ѯ����˵Ļ�����ݣ���ֱ�Ӳ�ѯDB��
     * @param dataIds
     * @param group
     * @param timeoutMs
     * @return
     */
    static public BatchHttpResult<ConfigInfoEx> batchGetConfig(List<String> dataIds, String group,
                                                           long timeoutMs) {
        return DiamondEnvRepo.getDefaultEnv().batchGetConfig(dataIds, group, timeoutMs);
    }

	/**
	 * ��ѯ�⻧�µ����е�����
	 * @param timeoutMs
	 * @return
	 */
	static public List<ConfigKey> getAllTenantConfig(long timeoutMs) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().getAllTenantConfig(timeoutMs);
	}

    /**
     * ����ɾ��ָ����datumList��һ��������1000����ݣ������ܾ����󡣸ò���Ϊԭ�Ӳ�������DBѹ���ϴ��벻ҪƵ��ʹ��
     * @param dataId
     * @param group
     * @param datumIdList
     * @param timeoutMs
     * @return
     * @throws IOException
     */
	public static boolean batchRemoveAggr(String dataId, String group, List<String> datumIdList, long timeoutMs) throws IOException {
		return DiamondEnvRepo.getDefaultEnv().batchRemoveAggr(dataId, group, datumIdList, timeoutMs);
	}
	
	/**
	 * �����������߸��¾ۺ���ݣ����datum���������¼ӣ�����������¡�һ��������1000����ݣ������ܾ����󡣸ò���Ϊԭ�Ӳ�������DBѹ���ϴ��벻ҪƵ��ʹ��
	 * @param dataId
	 * @param group
	 * @param datumMap
	 * @param timeoutMs
	 * @return
	 * @throws IOException
	 */
	public static boolean batchPublishAggr(String dataId, String group, Map<String, String> datumMap, long timeoutMs) throws IOException{
		return DiamondEnvRepo.getDefaultEnv().batchPublishAggr(dataId, group, datumMap, timeoutMs);
	}
	
	/**
	 * �����������߸��¾ۺ���ݣ����datum���������¼ӣ�����������¡�һ��������1000����ݣ������ܾ����󡣸ò���Ϊԭ�Ӳ�������DBѹ���ϴ��벻ҪƵ��ʹ��
	 * @param dataId
	 * @param group
	 * @param datumMap
	 * @param appName  ���ù�����app name
	 * @param timeoutMs
	 * @return
	 * @throws IOException
	 */
	public static boolean batchPublishAggr(String dataId, String group, Map<String, String> datumMap, String appName, long timeoutMs) throws IOException{
		return DiamondEnvRepo.getDefaultEnv().batchPublishAggr(dataId, group, datumMap, appName, timeoutMs);
	}
	
	/**
	 * �ø��datumMap����滻dataId+group��������ݣ��������ʷ���������ӡ�һ��������1000����ݣ������ܾ����󡣸ò���Ϊԭ�Ӳ�������DBѹ���ϴ��벻ҪƵ��ʹ��
	 * @param dataId
	 * @param group
	 * @param datumMap
	 * @param timeoutMs
	 * @return
	 * @throws IOException
	 */
	public static boolean replaceAggr(String dataId, String group, Map<String, String> datumMap, long timeoutMs) throws IOException{
		return DiamondEnvRepo.getDefaultEnv().replaceAggr(dataId, group, datumMap, timeoutMs);
	}
	
	/**
	 * �ø��datumMap����滻dataId+group��������ݣ��������ʷ���������ӡ�һ��������1000����ݣ������ܾ����󡣸ò���Ϊԭ�Ӳ�������DBѹ���ϴ��벻ҪƵ��ʹ��
	 * @param dataId
	 * @param group
	 * @param datumMap
	 * @param appName  ���ù�����app name
	 * @param timeoutMs
	 * @return
	 * @throws IOException
	 */
	public static boolean replaceAggr(String dataId, String group, Map<String, String> datumMap, String appName, long timeoutMs) throws IOException{
		return DiamondEnvRepo.getDefaultEnv().replaceAggr(dataId, group, datumMap, appName, timeoutMs);
	}
	
	/**
	 * beta publish
	 * 
	 * @param dataId
	 *            data key
	 * @param group
	 *            data group
	 * @param appName
	 *            app name
	 * @param betaIps
	 *            beta ips,e.g. ip1,ip2
	 * @param content
	 *            value
	 * @return whether publish ok
	 * @throws DiamondException
	 *             exception
	 */
	public static boolean publishBeta(String dataId, String group, String appName, String betaIps, String content)
			throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().publishBeta(dataId, group, appName, betaIps, content);
	}

	/**
	 * beta publish
	 * 
	 * @param dataId
	 *            data key
	 * @param group
	 *            data group
	 * @param betaIps
	 *            beta ips,e.g. ip1,ip2
	 * @param content
	 *            value
	 * @return whether publish ok
	 * @throws DiamondException
	 *             exception
	 */
	public static boolean publishBeta(String dataId, String group, String betaIps, String content) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().publishBeta(dataId, group, betaIps, content);
	}

	/**
	 * get beta value
	 * 
	 * @param dataId
	 *            data key
	 * @param group
	 *            data group
	 * @return ConfigInfo4Beta beta value
	 * @throws DiamondException
	 *             exception
	 */
	public static ConfigInfo4Beta getBeta(String dataId, String group) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().getBeta(dataId, group);
	}

	/**
	 * stop beta
	 * 
	 * @param dataId
	 *            data key
	 * @param group
	 *            data group
	 * @return whether stop ok
	 * @throws DiamondException
	 *             exception
	 */
	public static boolean stopBeta(String dataId, String group) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().stopBeta(dataId, group);
	}

	/**
	 * ��������tagֵ
	 * @param dataId data key
	 * @param group data group
	 * @param tag ��
	 * @param content ����
	 * @return �Ƿ����ͳɹ�
	 * @throws DiamondException
	 */
	public static boolean publishSingleTag(String dataId, String group, String tag, String content)
			throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().publishSingleTag(dataId, group, tag, content);
	}

	/**
	 * ɾ������tagֵ
	 * @param dataId data key
	 * @param group data group
	 * @param tag ��
	 * @return �Ƿ�ɾ��ɹ�
	 * @throws DiamondException
	 */
	public static boolean removeTag(String dataId, String group, String tag) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().removeTag(dataId, group, tag);
	}

	/**
	 * ��������tagֵ
	 * @param dataId data key
	 * @param group data group
	 * @param tag ��
	 * @param timeoutMs ��ȡ��ʱ 
	 * @return ����
	 * @throws DiamondException
	 */
	public static String getConfigTag(String dataId, String group, String tag, long timeoutMs) throws DiamondException {
		return DiamondEnvRepo.getDefaultEnv().getConfigTag(dataId, group, tag, timeoutMs);
	}

}
