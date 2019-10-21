package com.taobao.diamond.client.impl;

import com.taobao.diamond.client.SnapShotSwitch;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

/**
 * ���������Կ��EncryptedDataKey���ı��ؿ��ա�����Ŀ¼��ء�
 */
public class LocalEncryptedDataKeyProcessor extends LocalConfigInfoProcessor {

    /**
     * ��ȡ�������õ� EncryptedDataKey��NULL��ʾû�б����ļ����׳��쳣��
     */
    public static String getEncryptDataKeyFailover(DiamondEnv env, String dataId, String group, String tenant) {
        File file = getEncryptDataKeyFailoverFile(env, dataId, group, tenant);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            return readFile(file);
        } catch (IOException ioe) {
            log.error(env.getName(), "DIAMOND-0009", "get failover error, " + file + ioe.toString());
            return null;
        }
    }

    /**
     * ��ȡ���ػ����ļ��� EncryptedDataKey��NULL��ʾû�б����ļ����׳��쳣��
     */
    public static String getEncryptDataKeySnapshot(DiamondEnv env, String dataId, String group, String tenant) {
        if (!SnapShotSwitch.getIsSnapShot()) {
            return null;
        }

        File file = getEncryptDataKeySnapshotFile(env, dataId, group, tenant);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            return readFile(file);
        } catch (IOException ioe) {
            log.error(env.getName(), "DIAMOND-0009", "get snapshot error, " + file + ", " + ioe.toString());
            return null;
        }
    }

    public static void batchSaveEncryptDataKeySnapshot(DiamondEnv env, List<ConfigInfoEx> configInfos) {
        for (ConfigInfoEx config : configInfos) {
            if (config.getStatus() == 2) {
                continue;
            }
            saveEncryptDataKeySnapshot(env, config.getDataId(), config.getGroup(), config.getTenant(),
                config.getEncryptedDataKey());
        }
    }

    /**
     * ���� encryptDataKey ��snapshot���������ΪNULL����ɾ��snapshot��
     */
    public static void saveEncryptDataKeySnapshot(DiamondEnv env, String dataId, String group, String tenant,
                                                  String encryptDataKey) {
        String envName = env.serverMgr.name;

        if (!SnapShotSwitch.getIsSnapShot()) {
            return;
        }

        File file = getEncryptDataKeySnapshotFile(envName, dataId, group, tenant);
        write(envName, encryptDataKey, file);
    }

    private static File getEncryptDataKeyFailoverFile(DiamondEnv env, String dataId, String group, String tenant) {
        File tmp = new File(localSnapShotPath, env.serverMgr.name + "_diamond");
        tmp = new File(tmp, "encrypted-data-key");

        if (StringUtils.isBlank(tenant)) {
            tmp = new File(tmp, "failover");
        } else {
            tmp = new File(tmp, "failover-tenant");
            tmp = new File(tmp, tenant);
        }

        return new File(new File(tmp, group), dataId);
    }

    private static File getEncryptDataKeySnapshotFile(DiamondEnv env, String dataId, String group, String tenant) {
        return getEncryptDataKeySnapshotFile(env.serverMgr.name, dataId, group, tenant);
    }

    private static File getEncryptDataKeySnapshotFile(String envName, String dataId, String group, String tenant) {
        File tmp = new File(localSnapShotPath, envName + "_diamond");
        tmp = new File(tmp, "encrypted-data-key");

        if (StringUtils.isBlank(tenant)) {
            tmp = new File(tmp, "snapshot");
        } else {
            tmp = new File(tmp, "snapshot-tenant");
            tmp = new File(tmp, tenant);
        }

        return new File(new File(tmp, group), dataId);
    }

}
