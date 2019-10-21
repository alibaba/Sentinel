package com.taobao.diamond.client;

/**
 * Diamond��һ��֧�ֳ־õĿɿ����ı�������Ϣ�������ģ������ı�������Ϣ�Ķ��ġ�<br>
 * DiamondĿǰ��ʹ��ops�����־�������Ϣ��<br>
 * Diamond����ʹ�ü��е���ݿⱣ��־õ�������Ϣ��������Ϣʮ�ְ�ȫ�����ң�Diamond�ܹ�ʹ�ͻ���Զ��ȡ���µĶ�����Ϣ��<br>
 * ĿǰDiamond�ͻ���ӵ�����¼��ַ�ʽ�� <br>
 * 1.������ȡ<br>
 * 2.��ʱ��ȡ<br>
 * Diamond�ͻ��˻�֧�����Զ��������Ϣ���ԣ����ȼ���ߵı�������������Ϣ�Ļ�ȡ
 * 
 * @author aoqiong
 * 
 */
public interface DiamondClientSub {

    /**
     * ����diamond���ö���
     * 
     * @param diamondConfigure
     */
    public void setDiamondConfigure(DiamondConfigure diamondConfigure);


    /**
     * ��ȡdiamond���ö���
     * 
     * @return
     */
    public DiamondConfigure getDiamondConfigure();


    /**
     * ����diamond
     */
    public void start();


    /**
     * �ر�diamond
     */
    public void close();
}
