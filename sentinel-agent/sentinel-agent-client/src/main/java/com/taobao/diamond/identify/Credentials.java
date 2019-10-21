package com.taobao.diamond.identify;



public class Credentials implements SpasCredential {
	
	private volatile String accessKey;
	
	private volatile String secretKey;
	
	public Credentials(String accessKey, String secretKey) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}
	
	public Credentials() {
		this(null, null);
	}
	
	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

    public boolean valid() {
        return accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty();
    }

    public boolean identical(Credentials other) {
        return this == other ||
               (other != null &&
                (accessKey == null && other.accessKey == null || accessKey != null && accessKey.equals(other.accessKey)) &&
                (secretKey == null && other.secretKey == null || secretKey != null && secretKey.equals(other.secretKey)));
    }
}
