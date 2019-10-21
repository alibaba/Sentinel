package com.taobao.middleware.logger.option;

import java.io.File;
import java.nio.charset.Charset;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

import com.taobao.middleware.logger.support.LoggerHelper;

/**
 * ActivateOption的Logback 0.9.19及后续版本的实现
 * 
 * @author zhuyong 2014年3月20日 上午10:24:58
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LogbackActivateOption extends Logback918ActivateOption {

    public LogbackActivateOption(Object logger) {
        super(logger);
    }

    protected ch.qos.logback.core.Appender getLogbackDailyRollingFileAppender(String productName, String file,
                                                                              String encoding) {
        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(LogbackLoggerContextUtil.getLoggerContext());

        appender.setName(productName + "." + file.replace(File.separatorChar, '.') + ".Appender");
        appender.setAppend(true);
        appender.setFile(LoggerHelper.getLogFile(productName, file));

        TimeBasedRollingPolicy rolling = new TimeBasedRollingPolicy();
        rolling.setParent(appender);
        rolling.setFileNamePattern(LoggerHelper.getLogFile(productName, file) + ".%d{yyyy-MM-dd}");
        rolling.setContext(LogbackLoggerContextUtil.getLoggerContext());
        rolling.start();
        appender.setRollingPolicy(rolling);

        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setPattern(LoggerHelper.getPattern(productName));
        layout.setCharset(Charset.forName(encoding));
        appender.setEncoder(layout);
        layout.setContext(LogbackLoggerContextUtil.getLoggerContext());
        layout.start();
        // 启动
        appender.start();

        return appender;
    }

    protected ch.qos.logback.core.Appender getLogbackDailyAndSizeRollingFileAppender(String productName, String file,
                                                                                     String encoding, String size) {
        return getLogbackDailyAndSizeRollingFileAppender(productName, file, encoding, size, "yyyy-MM-dd", -1);
    }

    protected ch.qos.logback.core.Appender getLogbackDailyAndSizeRollingFileAppender(String productName, String file,
                                                                                     String encoding, String size,
                                                                                     String datePattern,
                                                                                     int maxBackupIndex) {
        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(LogbackLoggerContextUtil.getLoggerContext());

        appender.setName(productName + "." + file.replace(File.separatorChar, '.') + ".Appender");
        appender.setAppend(true);
        appender.setFile(LoggerHelper.getLogFile(productName, file));

        TimeBasedRollingPolicy rolling = new TimeBasedRollingPolicy();
        rolling.setParent(appender);
        if (maxBackupIndex >= 0) {
            rolling.setMaxHistory(maxBackupIndex);
        }
        rolling.setFileNamePattern(LoggerHelper.getLogFile(productName, file) + ".%d{" + datePattern + "}.%i");
        rolling.setContext(LogbackLoggerContextUtil.getLoggerContext());

        SizeAndTimeBasedFNATP fnatp = new SizeAndTimeBasedFNATP();
        //fnatp.setMaxFileSize(size);
        setMaxFileSize(fnatp, size);
        fnatp.setTimeBasedRollingPolicy(rolling);
        rolling.setTimeBasedFileNamingAndTriggeringPolicy(fnatp);

        rolling.start();
        appender.setRollingPolicy(rolling);

        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setPattern(LoggerHelper.getPattern(productName));
        layout.setCharset(Charset.forName(encoding));
        appender.setEncoder(layout);
        layout.setContext(LogbackLoggerContextUtil.getLoggerContext());
        layout.start();

        // 启动
        appender.start();

        return appender;
    }

    protected ch.qos.logback.core.Appender getSizeRollingAppender(String productName, String file, String encoding,
                                                                  String size, int maxBackupIndex) {
        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(LogbackLoggerContextUtil.getLoggerContext());

        appender.setName(productName + "." + file.replace(File.separatorChar, '.') + ".Appender");
        appender.setAppend(true);
        appender.setFile(LoggerHelper.getLogFile(productName, file));

        SizeBasedTriggeringPolicy triggerPolicy = new SizeBasedTriggeringPolicy();
        //triggerPolicy.setMaxFileSize(size);
        setMaxFileSize(triggerPolicy, size);
        triggerPolicy.setContext(LogbackLoggerContextUtil.getLoggerContext());
        triggerPolicy.start();

        FixedWindowRollingPolicy rolling = new FixedWindowRollingPolicy();
        rolling.setContext(LogbackLoggerContextUtil.getLoggerContext());
        rolling.setParent(appender);
        rolling.setFileNamePattern(LoggerHelper.getLogFile(productName, file) + ".%i");
        rolling.setParent(appender);
        if (maxBackupIndex >= 0) {
            rolling.setMaxIndex(maxBackupIndex);
        }
        rolling.start();

        appender.setRollingPolicy(rolling);
        appender.setTriggeringPolicy(triggerPolicy);

        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setPattern(LoggerHelper.getPattern(productName));
        layout.setCharset(Charset.forName(encoding));
        appender.setEncoder(layout);
        layout.setContext(LogbackLoggerContextUtil.getLoggerContext());
        layout.start();

        // 启动
        appender.start();

        return appender;
    }
}
