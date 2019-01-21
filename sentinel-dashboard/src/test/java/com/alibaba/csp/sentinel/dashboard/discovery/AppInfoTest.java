package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.ConcurrentModificationException;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppInfoTest {
    @Test
    public void testConcurrentGetMachines() throws Exception {
        AppInfo appInfo = new AppInfo("testApp");
        appInfo.addMachine(genMachineInfo("hostName1", "10.18.129.91"));
        appInfo.addMachine(genMachineInfo("hostName2", "10.18.129.92"));
        Set<MachineInfo> machines = appInfo.getMachines();
        new Thread(() -> {
            try {
                for (MachineInfo m : machines) {
                    System.out.println(m);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
                assertTrue(false);
            }

        }).start();
        Thread.sleep(100);
        try {
            appInfo.addMachine(genMachineInfo("hostName3", "10.18.129.93"));
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        Thread.sleep(1000);
    }

    private MachineInfo genMachineInfo(String hostName, String ip) {
        MachineInfo machine = new MachineInfo();
        machine.setApp("testApp");
        machine.setHostname(hostName);
        machine.setIp(ip);
        machine.setPort(8719);
        machine.setVersion(String.valueOf(System.currentTimeMillis()));
        return machine;
    }

}