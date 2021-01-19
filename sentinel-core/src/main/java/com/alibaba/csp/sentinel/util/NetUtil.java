package com.alibaba.csp.sentinel.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * network tools
 *
 * @author imlzw
 */
public class NetUtil {

    public static String getLocalInetAddress() {
        List<String[]> localIPs = getLocalIPs(null);
        if (localIPs.size() > 0) {
            return localIPs.get(0)[1];
        }
        return null;
    }

    public static String getLocalInetAddress(NetConfig config) {
        List<String[]> localIPs = getLocalIPs(config);
        if (localIPs.size() > 0) {
            return localIPs.get(0)[1];
        }
        return null;
    }

    /**
     * get local ips
     *
     * @param config
     * @return
     */
    public static List<String[]> getLocalIPs(NetConfig config) {
        LinkedHashMap<String, InetAddress> localIps = getLocalHostAddresses(new ConfigInetAddressFilter(config));
        List<String[]> list = new ArrayList<>();
        if (localIps != null && !localIps.isEmpty()) {
            for (String key : localIps.keySet()) {
                list.add(new String[]{key, localIps.get(key).getHostAddress()});
            }
        }
        if (config != null) {
            String preferredNetworks = config.getPreferredNetworks();
            if (preferredNetworks != null && preferredNetworks.trim().length() > 0) {
                final String[] ipSorts = preferredNetworks.split(",");
                Collections.sort(list, new Comparator<String[]>() {
                    @Override
                    public int compare(String[] interfacess1, String[] interfacess2) {
                        int sort1 = -1;
                        int sort2 = -1;
                        for (int i = 0; i < ipSorts.length; i++) {
                            if (interfacess1[1].indexOf(ipSorts[i]) == 0) {
                                sort1 = ipSorts.length - i;
                                break;
                            }
                        }
                        for (int i = 0; i < ipSorts.length; i++) {
                            if (interfacess2[1].indexOf(ipSorts[i]) == 0) {
                                sort2 = ipSorts.length - i;
                                break;
                            }
                        }
                        return sort2 - sort1;
                    }
                });
            }
        }
        return list;
    }

    public static LinkedHashMap<String, InetAddress> getLocalHostAddresses(NameFilter nameFilter) {
        LinkedHashMap<String, InetAddress> map = new LinkedHashMap<>();
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                if (nameFilter.filter(ni.getDisplayName())) {
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(':') == -1) {
                            map.put(ni.getName() + ":" + ni.getDisplayName(), ip);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public static interface NameFilter {
        public boolean filter(String name);
    }

    /**
     * config inet address filter
     */
    public static class ConfigInetAddressFilter implements NameFilter {

        private NetConfig config;

        public ConfigInetAddressFilter(NetConfig config) {
            this.config = config;
        }

        @Override
        public boolean filter(String name) {
            String lowerCase = name.toLowerCase();
            if (config != null) {
                String ignoredInterfaces = config.getIgnoredInterfaces();
                if (ignoredInterfaces != null && ignoredInterfaces.trim().length() > 0) {
                    String[] splitInterfacess = ignoredInterfaces.split(",");
                    for (String netInterfacess : splitInterfacess) {
                        if (netInterfacess != null && lowerCase.indexOf(netInterfacess.trim().toLowerCase()) >= 0) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    public static class NetConfig implements Serializable {
        private String ignoredInterfaces;
        private String preferredNetworks;

        public String getIgnoredInterfaces() {
            return ignoredInterfaces;
        }

        public void setIgnoredInterfaces(String ignoredInterfaces) {
            this.ignoredInterfaces = ignoredInterfaces;
        }

        public String getPreferredNetworks() {
            return preferredNetworks;
        }

        public void setPreferredNetworks(String preferredNetworks) {
            this.preferredNetworks = preferredNetworks;
        }
    }
}