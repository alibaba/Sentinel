package com.alibaba.jvm.sandbox.module.debug;

import com.alibaba.jvm.sandbox.api.http.printer.Printer;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import org.apache.commons.lang3.StringUtils;

/**
 * 进度输出器
 * Created by luanjia@taobao.com on 2017/2/24.
 */
public class ProgressPrinter implements ModuleEventWatcher.Progress {

    private final String prefix;
    private final int width;
    private final Printer printer;
    private int total;

    public ProgressPrinter(Printer printer) {
        this("", 50, printer);
    }

    public ProgressPrinter(String prefix, int width, Printer printer) {
        this.prefix = prefix;
        this.width = width;
        this.printer = printer;
    }

    @Override
    public void begin(int total) {
        this.total = total;
        printer.print("%s[");
    }

    @Override
    public void progressOnSuccess(Class clazz, int index) {
        progress(index);
    }

    @Override
    public void progressOnFailed(Class clazz, int index, Throwable cause) {
        progress(index);
    }

    private void progress(int index) {
        if (printer.isBroken()) {
            return;
        }
        final int rate = computeRate(index);
        printer.print(String.format("\r%s[%-" + width + "s]", prefix, StringUtils.repeat('#', rate)));
        printer.flush();
    }

    private int computeRate(int index) {
        return (int) (index * width * 1f / total);
    }

    @Override
    public void finish(int cCnt, int mCnt) {
        printer.println(String.format("FINISH(cCnt=%d,mCnt=%d)", cCnt, mCnt));
    }

}
