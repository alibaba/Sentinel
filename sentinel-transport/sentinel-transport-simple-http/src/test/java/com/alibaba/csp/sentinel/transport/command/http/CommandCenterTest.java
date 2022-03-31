package com.alibaba.csp.sentinel.transport.command.http;

import com.alibaba.csp.sentinel.command.CommandCenterProvider;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by danebrown on 2022/4/1
 * mail: tain198127@163.com
 *
 * @author danebrown
 */
public class CommandCenterTest {
    @Test
    public void stopCommandCenter(){
        CommandCenter commandCenter =  CommandCenterProvider.getCommandCenter();
        try {
            commandCenter.stop();
        } catch (Exception e) {
            Assert.fail();
        }
    }
    @Test
    public void startCommandCenter(){
        CommandCenter commandCenter =  CommandCenterProvider.getCommandCenter();
        try {
            commandCenter.start();
        } catch (Exception e) {
            Assert.fail();
        }
    }
    @Test
    public void beforeStartCommandCenter(){
        CommandCenter commandCenter =  CommandCenterProvider.getCommandCenter();
        try {
            commandCenter.beforeStart();
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
