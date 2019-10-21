package com.alibaba.acm.shaded.com.aliyuncs.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author VK.Gao
 * @date 2018/01/04
 */
public class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
}
