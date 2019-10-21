package com.taobao.diamond.client;

import static com.taobao.diamond.client.impl.DiamondEnv.log;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.taobao.diamond.client.impl.ServerHttpAgent;
import com.taobao.diamond.common.Constants;


/**
 * Diamond�ͻ��˵�������Ϣ
 * 
 * @author aoqiong
 * 
 */
public class DiamondConfigure {

    public DiamondConfigure(ClusterType clusterType) {
        initSystemProperty();
    }
    
    private DiamondConfigure() {
        initSystemProperty();
    }

    private void initSystemProperty() {
        // ��ȡ��������, ������ѯʱ��
        try {
            String pollingIntervaStr = System.getProperty("diamond.polling.interval");
            if (pollingIntervaStr != null) {
                this.pollingIntervalTime = Integer.parseInt(pollingIntervaStr);
            }
            log.warn("diamond polling interval:" + this.pollingIntervalTime + "s");
        }
        catch (Exception e) {
            log.warn("parse system property error - diamond.polling.interval, use default:" + this.pollingIntervalTime
                    + "s," + e.getMessage());
        }

        // ��ȡ��������, ����HTTP��̨host�����������
        try {
            String httpMaxConns = System.getProperty("diamond.http.maxhostconn");
            if (httpMaxConns != null) {
                this.maxHostConnections = Integer.parseInt(httpMaxConns);
            }
            log.warn("diamond max host conn:" + this.maxHostConnections);
        }
        catch (Exception e) {
            log.warn("parse system property error - diamond.http.maxhostconn, use default:" + this.maxHostConnections
                    + "," + e.getMessage());
        }

        // ��ȡ��������, ����HTTP�ܵ����������
        try {
            String httpTotalConns = System.getProperty("diamond.http.maxtotalconn");
            if (httpTotalConns != null) {
                this.maxTotalConnections = Integer.parseInt(httpTotalConns);
            }
            log.warn("diamond max total conn:" + this.maxTotalConnections);
        }
        catch (Exception e) {
            log.warn("parse system property error - diamond.http.maxtotalconn, use default:" + this.maxTotalConnections
                    + "," + e.getMessage());
        }
    }


    /**
     * ��ȡ��ͬһ��DiamondServer�����������
     * 
     * @return
     */
    public int getMaxHostConnections() {
        return maxHostConnections;
    }


    /**
     * ���ú�ͬһ��DiamondServer�����������<br>
     * ��֧������ʱ��̬����
     * 
     * @param maxHostConnections
     */
    public void setMaxHostConnections(int maxHostConnections) {
        this.maxHostConnections = maxHostConnections;
    }


    /**
     * �Ƿ�����Գ¾ɵ�����������м�⡣<br>
     * ����⣬�����ϻ����������ǣ�����ʹ�ò��������ӵķ��յ��µ�IO Exception��Ĭ�ϼ��
     * 
     * @return
     */
    public boolean isConnectionStaleCheckingEnabled() {
        return connectionStaleCheckingEnabled;
    }


    /**
     * �����Ƿ�����Գ¾ɵ�����������м�⡣<br>
     * ��֧������ʱ��̬����
     * 
     * @param connectionStaleCheckingEnabled
     */
    public void setConnectionStaleCheckingEnabled(boolean connectionStaleCheckingEnabled) {
        this.connectionStaleCheckingEnabled = connectionStaleCheckingEnabled;
    }


    /**
     * ��ȡ�������������������
     * 
     * @return
     */
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }


    /**
     * �����������������������<br>
     * ��֧������ʱ��̬����
     * 
     * @param maxTotalConnections
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }


    /**
     * ��ȡ��ѯ�ļ��ʱ�䡣��λ����<br>
     * �˼��ʱ������ѯ����һ��������Ϣ�ļ��ʱ�䣬����������أ������ö�һЩ��<br>
     * ��������ɱ��������Ϣ�������ó�һЩ
     * 
     * @return
     */
    public int getPollingIntervalTime() {
        return pollingIntervalTime;
    }


    /**
     * ������ѯ�ļ��ʱ�䡣��λ����<br>
     * 
     * @param pollingIntervalTime
     */
    public void setPollingIntervalTime(int pollingIntervalTime) {
        if (pollingIntervalTime < Constants.POLLING_INTERVAL_TIME) {
            return;
        }
        this.pollingIntervalTime = pollingIntervalTime;
    }


    /**
     * ��ȡ��ǰ֧�ֵ����е�DiamondServer�����б�
     * <p>������̶�Ϊ jmenv.tbsite.net��
     * 
     * @return
     */
    public List<String> getDomainNameList() {
        return Arrays.asList(ServerHttpAgent.domainName);
    }


    /**
     * ���õ�ǰ֧�ֵ����е�DiamondServer�����б�.
     * <p>�÷�����Ч����Ϊ������̶�Ϊ jmenv.tbsite.net��
     */
    @Deprecated
    public void setDomainNameList(List<String> domainNameList) {
        if (null == domainNameList) {
            throw new NullPointerException();
        }
        this.domainNameList = new LinkedList<String>(domainNameList);
    }


    /**
     * ���һ��DiamondServer����
     * <p>�÷�����Ч����Ϊ������̶�Ϊ jmenv.tbsite.net��
     */
    @Deprecated
    public void addDomainName(String domainName) {
        if (null == domainName) {
            throw new NullPointerException();
        }
        this.domainNameList.add(domainName);
    }


    /**
     * ��Ӷ��DiamondServer����
     * <p>�÷�����Ч����Ϊ������̶�Ϊ jmenv.tbsite.net��
     */
    @Deprecated
    public void addDomainNames(Collection<String> domainNameList) {
        if (null == domainNameList) {
            throw new NullPointerException();
        }
        this.domainNameList.addAll(domainNameList);
    }


    /**
     * ��ȡDiamondServer�Ķ˿ں�
     * 
     * @return
     */
    public int getPort() {
        return port;
    }


    /**
     * ����DiamondServer�Ķ˿ں�, �벻Ҫ�������, ������Ҫ���дdiamond-server���в���ʱʹ��, Ĭ�϶˿�8080<br>
     * 
     * <p>���ܸĶ˿ںš�
     */
    @Deprecated
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * ��ȡ����һ��DiamondServer���Ӧ�Ĳ�ѯһ��DataID��Ӧ��������Ϣ��Timeoutʱ��<br>
     * ��һ��HTTP����ĳ�ʱʱ��<br>
     * ��λ������<br>
     * 
     * @return
     */
    public int getOnceTimeout() {
        return onceTimeout;
    }


    /**
     * ���ö���һ��DiamondServer���Ӧ�Ĳ�ѯһ��DataID��Ӧ��������Ϣ��Timeoutʱ��<br>
     * ��λ������<br>
     * ������ϢԽ���뽫��ֵ���õ�Խ��
     * 
     * @return
     */
    public void setOnceTimeout(int onceTimeout) {
        this.onceTimeout = onceTimeout;
    }


    /**
     * ��ȡ��DiamondServer�����ӽ�����ʱʱ�䡣��λ������
     * 
     * @return
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }


    /**
     * ���ú�DiamondServer�����ӽ�����ʱʱ�䡣��λ������<br>
     * 
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }


    /**
     * ��ȡ����ʱʱ��, ��λ����
     * 
     * @return
     */
    public int getSoTimeout() {
        return soTimeout;
    }


    /**
     * ��������ʱʱ��, ��λ����
     * 
     * @param soTimeout
     */
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    /**
     * ����̽�Ȿ���ļ���·��<br>
     * <p>�����޸Ŀͻ��˻���Ŀ¼��
     */
    @Deprecated
    public void setFilePath(String filePath) {
    }
    
    /**
     * ����һ��DataID����ȴ�ʱ��<br>
     * ʵ����ȴ�ʱ��С��receiveWaitTime + min(connectionTimeout, onceTimeout)
     */
    @Deprecated
    public void setReceiveWaitTime(int receiveWaitTime) {
    }
    
    @Deprecated
    public void setTotalTimeout(long totalTimeout) {
    }
    
    // ======================
    
    static final public DiamondConfigure singleton = new DiamondConfigure();
    

    // �첽��ѯ�ļ��ʱ��, ��λΪ��
    private volatile int pollingIntervalTime = 5;

    // server��ַ�б�
    private volatile List<String> domainNameList = new LinkedList<String>();

    // ���²���֧�����к�̬����
    // �Ƿ����������
    private boolean connectionStaleCheckingEnabled = true;
    // ���ӵ�ÿ̨server�����������
    private int maxHostConnections = 20;
    // �ܵ����������
    private int maxTotalConnections = 50;

    // ����ʱʱ��, ��λΪ����
    private int soTimeout = Constants.SO_TIMEOUT;
    // ���ӳ�ʱʱ��, ��λΪ����
    private int connectionTimeout = Constants.CONN_TIMEOUT;
    // ��server��һ������ĳ�ʱʱ��, ��λΪ����
    private volatile int onceTimeout = Constants.ONCE_TIMEOUT;
    
    // ���Ӷ˿�
    private int port = Constants.DEFAULT_PORT;
    
}
