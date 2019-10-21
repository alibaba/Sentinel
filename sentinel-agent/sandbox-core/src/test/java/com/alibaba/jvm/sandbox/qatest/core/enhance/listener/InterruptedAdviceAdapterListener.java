package com.alibaba.jvm.sandbox.qatest.core.enhance.listener;

import com.alibaba.jvm.sandbox.api.listener.ext.AdviceAdapterListener;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.core.enhance.annotation.Interrupted;

@Interrupted
public class InterruptedAdviceAdapterListener extends AdviceAdapterListener {

    public InterruptedAdviceAdapterListener(AdviceListener adviceListener) {
        super(adviceListener);
        if(adviceListener instanceof TracingAdviceListener) {
            ((TracingAdviceListener) adviceListener).setEventListener(this);
        }
    }

}
