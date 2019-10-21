package com.alibaba.middleware.tls.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.alibaba.middleware.tls.log.TlsLogger;

public class FileProperties {

	private final Properties properties = new Properties();
	private final String filepath;

	
	public FileProperties(String filepath) {
		if (!new File(filepath).exists()) {
			TlsLogger.error("filepath not exits:"+filepath);
			throw new RuntimeException(new FileNotFoundException(filepath));
		}
		this.filepath = filepath;
		this.init();
	}

	public Properties getProperties() {
		return properties;
	}

	public String getFilepath() {
		return filepath;
	}

	private void init() {
		Reader r = null;
		try {
			r = new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8");
			this.properties.load(r);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			FileUtil.closeQuietly(r);
		}
	}

	public String get(String k) {
		return (String) getProperties().get(k);
	}

	/**
	 * @return the previous value of the specified key in this property
	 *             list, or {@code null} if it did not have one.
	 */
	public String set(String k, String v) {
		return (String) getProperties().setProperty(k, v);
	}

	public String remove(String k) {
		return (String) getProperties().remove(k);
	}

}
