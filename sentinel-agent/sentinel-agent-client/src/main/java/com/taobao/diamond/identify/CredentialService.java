package com.taobao.diamond.identify;

import com.taobao.diamond.client.impl.LogUtils;
import com.taobao.diamond.utils.StringUtils;
import com.taobao.middleware.logger.Logger;

import java.util.concurrent.ConcurrentHashMap;

public final class CredentialService implements SpasCredentialLoader {
	static final public Logger log = LogUtils.logger(CredentialService.class);
    private static ConcurrentHashMap<String, CredentialService> instances = new ConcurrentHashMap<String, CredentialService>();

    private String appName;
	private Credentials credentials = new Credentials();
    private CredentialWatcher watcher;
    private CredentialListener listener;
	
	private CredentialService(String appName) {
        if (appName == null) {
        	String value = System.getProperty("project.name");
        	if (StringUtils.isNotEmpty(value)) {
				appName = value;
			}
        }
        this.appName = appName;
        watcher = new CredentialWatcher(appName, this);
	}
	
	
	public static CredentialService getInstance() {
        return getInstance(null);
	}

    public static CredentialService getInstance(String appName) {
        String key = appName != null ? appName : Constants.NO_APP_NAME;
        CredentialService instance = instances.get(key);
        if (instance == null) {
            instance = new CredentialService(appName);
            CredentialService previous = instances.putIfAbsent(key, instance);
            if (previous != null) {
                instance = previous;
            }
        }
        return instance;
    }

    public static CredentialService freeInstance() {
        return freeInstance(null);
    }

    public static CredentialService freeInstance(String appName) {
        String key = appName != null ? appName : Constants.NO_APP_NAME;
        CredentialService instance = instances.remove(key);
        if (instance != null) {
            instance.free();
        }
        return instance;
    }

    public void free() {
        if (watcher != null) {
            watcher.stop();
        }
        log.info(appName, this.getClass().getSimpleName() + " is freed");
    }

	public Credentials getCredential() {
        Credentials localCredential = credentials;
        if (localCredential.valid()) {
            return localCredential;
        }
		return credentials;
	}

    public void setCredential(Credentials credential) {
        boolean changed = !(credentials == credential || (credentials != null && credentials.identical(credential)));
        credentials = credential;
        if (changed && listener != null) {
            listener.onUpdateCredential();
        }
    }

    public void setStaticCredential(Credentials credential) {
        if (watcher != null) {
            watcher.stop();
        }
        setCredential(credential);
    }

    public void registerCredentialListener(CredentialListener listener) {
        this.listener = listener;
    }

    @Deprecated
	public void setAccessKey(String accessKey) {
		credentials.setAccessKey(accessKey);
	}

    @Deprecated
	public void setSecretKey(String secretKey) {
		credentials.setSecretKey(secretKey);
	}

    @Deprecated
	public String getAccessKey() {
		return credentials.getAccessKey();
	}

    @Deprecated
	public String getSecretKey() {
		return credentials.getSecretKey();
	}

}
