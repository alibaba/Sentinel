package com.taobao.diamond.client.impl;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.exception.DiamondException;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.SharedManagerListenerAdapter;
import com.taobao.diamond.manager.impl.ConfigResponse;
import com.taobao.diamond.md5.MD5;


public class CacheData {
 
	public boolean isInitializing() {
		return isInitializing;
	}

	public void setInitializing(boolean isInitializing) {
		this.isInitializing = isInitializing;
	}

	public String getMd5() {
        return md5;
    }
    
	public String getTenant() {
		return tenant;
	}

    public String getContent() {
        return content;
    }
    
    public void setContent(String newContent) {
        this.content = newContent;
        this.md5 = getMd5String(content);
    }
    
    /**
     * ��Ӽ�����
     */
	public void addListener(ManagerListener listener) {
		if (null == listener) {
			throw new IllegalArgumentException("listener is null");
		}
		ManagerListenerWrap wrap = new ManagerListenerWrap(listener);
		if (listeners.addIfAbsent(wrap)) {
			log.info(env.getName(), "[add-listener] ok, tenant={}, dataId={}, group={}, cnt={}", tenant, dataId, group,
					listeners.size());
		}
	}
    
    public void removeListener(ManagerListener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("listener is null");
        }
        ManagerListenerWrap wrap = new ManagerListenerWrap(listener);
        if (listeners.remove(wrap)) {
            log.info(env.getName(), "[remove-listener] ok, dataId={}, group={}, cnt={}", dataId, group, listeners.size());
        }
    }
    
    /**
     * ���ؼ������б��ϵĵ������ֻ������֤������NULL��
     */
    public List<ManagerListener> getListeners() {
        List<ManagerListener> result = new ArrayList<ManagerListener>();
        for (ManagerListenerWrap wrap : listeners) {
            result.add(wrap.listener);
        }
        return result;
    }

    
    public long getLocalConfigInfoVersion() {
        return localConfigLastModified;
    }
    public void setLocalConfigInfoVersion(long localConfigLastModified) {
        this.localConfigLastModified = localConfigLastModified;
    }


    public boolean isUseLocalConfigInfo() {
        return isUseLocalConfig;
    }
    public void setUseLocalConfigInfo(boolean useLocalConfigInfo) {
        this.isUseLocalConfig = useLocalConfigInfo;
        if (!useLocalConfigInfo) {
            localConfigLastModified = -1;
        }
    }
    
    public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || obj.getClass() != getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        CacheData other = (CacheData) obj;
        return dataId.equals(other.dataId) && group.equals(other.group);
    }

    @Override
    public String toString() {
        return "CacheData [" + dataId + ", " + group + "]";
    }
    
    
    void checkListenerMd5() {
        for (ManagerListenerWrap wrap : listeners) {
            if (!md5.equals(wrap.lastCallMd5)) {
                safeNotifyListener(env, dataId, group, content, md5, encryptedDataKey, wrap);
            }
        }
    }
    
    static void safeNotifyListener(final DiamondEnv env, final String dataId, final String group, final String content,
            final String md5, final String encryptedDataKey, final ManagerListenerWrap listenerWrap) {
        final ManagerListener listener = listenerWrap.listener;
        
        Runnable job = new Runnable() {
            public void run() {
            	ClassLoader diamondClassLoader = Thread.currentThread().getContextClassLoader();
            	ClassLoader appClassLoader= listener.getClass().getClassLoader();
                try {
                    if(listener instanceof SharedManagerListenerAdapter){
                        SharedManagerListenerAdapter adapter = (SharedManagerListenerAdapter) listener;
                        adapter.fillContext(dataId, group);
                        log.info(env.getName(), "[notify-context] dataId={}, group={}, md5={}", dataId, group, md5);
                    }
                    // ִ�лص�֮ǰ�Ƚ��߳�classloader����Ϊ����webapp��classloader������ص������е���spi�ӿ��ǳ����쳣����ã���Ӧ�ò���Ż��и����⣩��
                    Thread.currentThread().setContextClassLoader(appClassLoader);
                    
					ConfigResponse cr = new ConfigResponse();
					cr.setDataId(dataId);
					cr.setGroup(group);
					cr.setContent(content);
                    cr.setEncryptedDataKey(encryptedDataKey);

					env.getConfigFilterChainManager().doFilter(null, cr);
					String contentTmp = cr.getContent();
					listener.receiveConfigInfo(contentTmp);
					listenerWrap.lastCallMd5 = md5;
					log.info(
							env.getName(),
							"[notify-ok] dataId={}, group={}, md5={}, listener={} ",
							dataId, group, md5, listener);
				} catch (DiamondException de) {
					log.error(env.getName(), "DIAMOND-XXXX",
							"[notify-error] dataId={}, group={}, md5={}, listener={} errCode={} errMsg={}", dataId,
							group, md5, listener, de.getErrCode(), de.getErrMsg());
				} catch (Throwable t) {
					log.error(env.getName(), "DIAMOND-XXXX",
							"[notify-error] dataId={}, group={}, md5={}, listener={} tx={}", dataId, group, md5,
							listener, t.getCause());
				}
                finally
                {
                	Thread.currentThread().setContextClassLoader(diamondClassLoader);
                }
            }
        };

        final long startNotify = System.currentTimeMillis();
        try {
            if (null != listener.getExecutor()) {
                listener.getExecutor().execute(job);
            } else {
                job.run();
            }
        } catch (Throwable t) {
			log.error(
					env.getName(),
					"DIAMOND-XXXX",
					"[notify-error] dataId={}, group={}, md5={}, listener={} throwable={}",
					dataId, group, md5, listener, t.getCause());
        }
        final long finishNotify = System.currentTimeMillis();
        log.info(env.getName(), "[notify-listener] time cost={}ms in ClientWorker, dataId={}, group={}, md5={}, listener={} ",(finishNotify - startNotify), dataId ,group, md5, listener);
    }
    
    static public String getMd5String(String config) {
        return (null == config) ? Constants.NULL : MD5.getInstance().getMD5String(config);
    }

    private String loadCacheContentFromDiskLocal(DiamondEnv env, String dataId, String group, String tenant) {
		String content = LocalConfigInfoProcessor.getFailover(env, dataId, group, tenant);
		content = (null != content) ? content //
				: LocalConfigInfoProcessor.getSnapshot(env, dataId, group, tenant);
		return content;
    }

    private String loadEncryptedDataKeyFromDiskLocal(DiamondEnv env, String dataId, String group, String tenant) {
        String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(env, dataId, group, tenant);

        if(encryptedDataKey != null){
            return encryptedDataKey;
        }

        return LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(env, dataId, group, tenant);
    }
    
    public CacheData(DiamondEnv env, String dataId, String group) {
        if (null == dataId || null == group) {
            throw new IllegalArgumentException("dataId=" + dataId + ", group=" + group);
        }
        this.env = env;
        this.dataId = dataId;
        this.group = group;
        this.tenant = TenantUtil.getUserTenant();
        listeners = new CopyOnWriteArrayList<ManagerListenerWrap>();
        this.isInitializing = true;
        this.content = loadCacheContentFromDiskLocal(env, dataId, group, tenant);
        this.md5 = getMd5String(content);
        this.encryptedDataKey = loadEncryptedDataKeyFromDiskLocal(env, dataId, group, tenant);
    }
    
    public CacheData(DiamondEnv env, String dataId, String group, String tenant) {
    	if (null == dataId || null == group) {
    		throw new IllegalArgumentException("dataId=" + dataId + ", group=" + group);
    	}
    	this.env = env;
    	this.dataId = dataId;
    	this.group = group;
    	this.tenant = tenant;
    	listeners = new CopyOnWriteArrayList<ManagerListenerWrap>();
    	this.isInitializing = true;
    	this.content = loadCacheContentFromDiskLocal(env, dataId, group, tenant);
    	this.md5 = getMd5String(content);
        this.encryptedDataKey = loadEncryptedDataKeyFromDiskLocal(env, dataId, group, tenant);
    }
    
    // ==================
    private final DiamondEnv env;

    public final String dataId;
    public final String group;
    public final String tenant;
    private final CopyOnWriteArrayList<ManagerListenerWrap> listeners;

    private volatile String md5;
    private volatile boolean isUseLocalConfig = false; // �Ƿ�ʹ�ñ����������
    private volatile long localConfigLastModified; // ����޸�ʱ��
    private volatile String content;
    private int taskId;
    private volatile boolean isInitializing = true;

    private volatile String encryptedDataKey;

    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }

    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
}

class ManagerListenerWrap {
    final ManagerListener listener;
    String lastCallMd5 = CacheData.getMd5String(null);

    ManagerListenerWrap(ManagerListener _listener) {
        listener = _listener;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || obj.getClass() != getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ManagerListenerWrap other = (ManagerListenerWrap) obj;
        return listener.equals(other.listener);
    }
}
