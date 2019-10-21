package com.taobao.diamond.manager.impl;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taobao.diamond.manager.ManagerListenerAdapter;



public abstract class DelayLoadListener extends ManagerListenerAdapter {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static Pattern pattern = Pattern.compile("diamond-config-effective-time\\s*=\\s*\"(.*?)\"");

    private ScheduledExecutorService scheduled;
    private SimpleDateFormat format;
    private volatile ScheduledFuture<?> previousTask;


	public DelayLoadListener() {
		scheduled = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("com.taobao.diamond.manager.impl.DelayLoadListener");
				t.setDaemon(true);
				return t;
			}
		});
		format = new SimpleDateFormat(DATE_FORMAT);
	}


    public synchronized void receiveConfigInfo(String configInfo) {
        if (configInfo == null || configInfo.length() == 0) {
            return;
        }

        long delay = getDelay(configInfo);
        delayReceive(configInfo, delay);

    }


    private void delayReceive(final String configInfo, long delay) {
        // ����ȡ��ǰһ�εĶ�ʱ����
        if (previousTask != null) {
            previousTask.cancel(false);
        }

        if (delay > 0) {
            previousTask = scheduled.schedule(new Runnable() {

                public void run() {
                    innerReceive(configInfo);

                }
            }, delay, TimeUnit.MILLISECONDS);
        }
        else {
            innerReceive(configInfo);
        }

    }


    public abstract void innerReceive(String configInfo);


    /**
     * <pre>
     * ������������������ diamond-config-effective-time="2010-07-10 10:29:01"���ַ�
     * ȡ�������ʱ����Ϊ���õ���Чʱ�䣬��ȥ��ǰʱ������ӳ�ʱ��
     * </pre>
     * 
     * @param configInfo
     * @return delay time
     */
    private long getDelay(String configInfo) {

        long delay = 0;
        try {
            delay = getEffectiveTime(configInfo) - System.currentTimeMillis();
        }
        catch (Exception e) {

        }

        if (delay < 0) {
            delay = 0;
        }
        return delay;
    }


    private long getEffectiveTime(String configInfo) {
        long time = 0;
        Matcher matcher = pattern.matcher(configInfo);
        String data = null;
        if (matcher.find()) {
            data = matcher.group(1);
        }
        if (data != null) {
            try {
                time = format.parse(data.trim()).getTime();
            }
            catch (Exception e) {
            }
        }
        return time;
    }

}
