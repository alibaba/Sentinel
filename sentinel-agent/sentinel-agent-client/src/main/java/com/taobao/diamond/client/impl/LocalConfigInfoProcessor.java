package com.taobao.diamond.client.impl;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.client.SnapShotSwitch;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.utils.StringUtils;

import java.io.*;
import java.util.List;

import static com.taobao.diamond.client.impl.DiamondEnv.log;


/**
 * ��������Ŀ¼��ء�
 */
public class LocalConfigInfoProcessor {

    /**
     * ��ȡ�����������ݡ�NULL��ʾû�б����ļ����׳��쳣��
     */
    static public String getFailover(DiamondEnv env, String dataId, String group, String tenant) {
        File localPath = getFailoverFile(env, dataId, group, tenant);
        if (!localPath.exists() || !localPath.isFile()) {
            return null;
        }

        try {
            return readFile(localPath);
        } catch (IOException ioe) {
            log.error(env.getName(), "DIAMOND-XXXX","get failover error, " + localPath + ioe.toString());
            return null;
        }
    }
    
    /**
     * ��ȡ���ػ����ļ����ݡ�NULL��ʾû�б����ļ����׳��쳣��
     */
    static public String getSnapshot(DiamondEnv env, String dataId, String group, String tenant) {
		if (!SnapShotSwitch.getIsSnapShot()) {
			return null;
		}
        File file = getSnapshotFile(env, dataId, group, tenant);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            return readFile(file);
        } catch (IOException ioe) {
            log.error(env.getName(), "DIAMOND-XXXX","get snapshot error, " + file + ", " + ioe.toString());
            return null;
        }
    }
    
	static String readFile(File file) throws IOException {
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		if (JVMUtil.isMultiInstance()) {
			return ConcurrentDiskUtil.getFileContent(file, Constants.ENCODE);
		} else {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				return IOUtils.toString(is, Constants.ENCODE);
			} finally {
				try {
					if (null != is) {
						is.close();
					}
				} catch (IOException ioe) {
				}
			}
		}
	}

    
    static public void batchSaveSnapshot(DiamondEnv env, List<ConfigInfoEx> configInfos){
    	for(ConfigInfoEx config: configInfos) {
    		if(config.getStatus()==2)
    			continue;
    		saveSnapshot(env, config.getDataId(), config.getGroup(), config.getTenant(), config.getContent());
    	}
    }
    
    /**
     * ����snapshot���������ΪNULL����ɾ��snapshot��
     */
    static public void saveSnapshot(DiamondEnv env, String dataId, String group, String tenant, String config) {
        saveSnapshot(env.serverMgr.name, dataId, group, tenant, config);
    }
    
    static public void saveSnapshot(String envName, String dataId, String group, String tenant, String config) {
		if (!SnapShotSwitch.getIsSnapShot()) {
			return;
		}
        File file = getSnapshotFile(envName, dataId, group, tenant);
	    write(envName, config, file);
    }

    static void write(String envName, String content, File file) {
		if (null == content) {
            try {
                IOUtils.delete(file);
            } catch (IOException ioe) {
                log.error(envName, "DIAMOND-XXXX","delete snapshot error, " + file + ", " + ioe.toString());
            }
        } else {
            try {
                file.getParentFile().mkdirs();
//                file.createNewFile();//���ﲻ��Ҫ����
                if (JVMUtil.isMultiInstance()) {
                    ConcurrentDiskUtil.writeFileContent(file, content,
                            Constants.ENCODE);
                } else {
                    IOUtils.writeStringToFile(file, content, Constants.ENCODE);
                }
            } catch (IOException ioe) {
                log.error(envName, "DIAMOND-XXXX","save snapshot error, " + file + ", " + ioe.toString());
            }
        }
	}

	/**
     * ���snapshotĿ¼�����л����ļ���
     */
    static public void cleanAllSnapshot() {
        try {
        	File rootFile = new File(localSnapShotPath);
        	File[] files = rootFile.listFiles();
			if (files == null || files.length == 0) {
				return;
			}
        	for(File file : files){
        		if(file.getName().endsWith("_diamond")){
        			IOUtils.cleanDirectory(file);
        		}
        	}
        } catch (IOException ioe) {
            log.error("DIAMOND-XXXX","clean all snapshot error, " + ioe.toString(), ioe);
        }
    }
    
    static public void cleanEnvSnapshot(String envName){
    	File tmp = new File(localSnapShotPath, envName + "_diamond");
    	tmp = new File(tmp, "snapshot");
    	try {
			IOUtils.cleanDirectory(tmp);
			log.info("success dlelet " + envName + "-snapshot");
		} catch (IOException e) {
			log.info("fail dlelet " + envName + "-snapshot, " + e.toString());
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) throws IOException {
        //LocalConfigInfoProcessor.readServerlist(DiamondEnvRepo.defaultEnv);

        Diamond.getConfig("test","test",2000l);
    }

    static File getFailoverFile(DiamondEnv env, String dataId, String group, String tenant) {
    	File tmp = new File(localSnapShotPath, env.serverMgr.name + "_diamond");
    	tmp = new File(tmp, "data");
    	if (StringUtils.isBlank(tenant)) {
    		tmp = new File(tmp, "config-data");
		} else
		{
			tmp = new File(tmp, "config-data-tenant");
			tmp = new File(tmp, tenant);
		}
        return new File(new File(tmp, group), dataId);
    }
    
    static File getSnapshotFile(DiamondEnv env, String dataId, String group, String tenant) {
    	return getSnapshotFile(env.serverMgr.name, dataId, group, tenant);
    }
    
    static File getSnapshotFile(String envName, String dataId, String group, String tenant) {
		File tmp = new File(localSnapShotPath, envName + "_diamond");
		if (StringUtils.isBlank(tenant)) {
			tmp = new File(tmp, "snapshot");
		} else {
			tmp = new File(tmp, "snapshot-tenant");
			tmp = new File(tmp, tenant);
		}
    	
        return new File(new File(tmp, group), dataId);
    }
    
    
    
//    static public boolean hasLocalFile(String dataId, String group) {
//        File file = getFilePath(dataId, group);
//        return file.exists() && file.isFile();
//    }
    
    // =================

    static public final LocalConfigInfoProcessor singleton = new LocalConfigInfoProcessor();

//    static final File failoverRoot;
//    static final File snapshotRoot;
    public static final String localFileRootPath;
    public static final String localSnapShotPath;
    static {
    	localFileRootPath = System.getProperty("JM.LOG.PATH", System.getProperty("user.home")) + File.separator + "diamond";
    	localSnapShotPath =  System.getProperty("JM.SNAPSHOT.PATH", System.getProperty("user.home")) + File.separator + "diamond";
		log.warn("localSnapShotPath:{}", localSnapShotPath);
    }

}
