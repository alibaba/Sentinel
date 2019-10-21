package com.taobao.diamond.manager;

import com.taobao.diamond.utils.StringUtils;



/**
 * TDDL��������ʱTDDL������ȡ���ò���ʼ����Ȼ����Ӽ��������������յ���ʼ�ص�������TDDL��һ�γ�ʼ����
 * 
 * <p>����취�����԰�������ȡ�������ñ����ڼ���������һ���յ��ص�ʱ�����ʼ���ñȽϣ����һ����������ء�
 * 
 * <p>ʾ����룺
 * <p><blockquote><pre>
 *     String config = Diamond.getConfig(dataId, group);
 *     // initialization code
 *     
 *     SkipInitialCallbackListener bizListener = new xxx(config);
 *     Diamond.addListener(dataId, group, bizListener);
 * </pre></blockquote>
 * 
 * @author JIUREN
 */
public abstract class SkipInitialCallbackListener implements ManagerListener {

    private final String initialValue;
    private boolean hasCallbacked = false;

    /**
     * ���캯����������ȡ�����������ݡ�
     * @param initialConfig ��ʼ����
     */
    public SkipInitialCallbackListener(String initialConfig) {
        initialValue = initialConfig;
    }

    /**
     * ����������Ϣ
     * 
     * @param configInfo ����ֵ
     */
    public void receiveConfigInfo(final String configInfo) {
        if (!hasCallbacked) {
            hasCallbacked = true;

            if (StringUtils.equals(initialValue, configInfo)) {
                return;
            }
        }

        receiveConfigInfo0(configInfo);
    }
    /**
     * �ص�
     * @param configInfo ����ֵ
     */
    abstract public void receiveConfigInfo0(String configInfo);
}
