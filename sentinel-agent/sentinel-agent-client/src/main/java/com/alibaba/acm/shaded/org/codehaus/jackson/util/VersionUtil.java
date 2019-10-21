package com.alibaba.acm.shaded.org.codehaus.jackson.util;

import java.io.*;
import java.util.regex.Pattern;

import com.alibaba.acm.shaded.org.codehaus.jackson.Version;

/**
 * Functionality for supporting exposing of component {@link Version}s.
 * 
 * @since 1.6
 */
public class VersionUtil
{
    public final static String VERSION_FILE = "VERSION.txt";

    private final static Pattern VERSION_SEPARATOR = Pattern.compile("[-_./;:]");
    
    /**
     * Helper method that will try to load version information for specified
     * class. Implementation is simple: class loader that loaded specified
     * class is asked to load resource with name "VERSION" from same
     * location (package) as class itself had.
     * If no version information is found, {@link Version#unknownVersion()} is
     * returned.
     */
    public static Version versionFor(Class<?> cls)
    {
        InputStream in;
        Version version = null;
        
        try {
            in = cls.getResourceAsStream(VERSION_FILE);
            if (in != null) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    version = parseVersion(br.readLine());
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) { }
        return (version == null) ? Version.unknownVersion() : version;
    }

    public static Version parseVersion(String versionStr)
    {
        if (versionStr == null) return null;
        versionStr = versionStr.trim();
        if (versionStr.length() == 0) return null;
        String[] parts = VERSION_SEPARATOR.split(versionStr);
        // Let's not bother if there's no separate parts; otherwise use whatever we got
        if (parts.length < 2) {
            return null;
        }
        int major = parseVersionPart(parts[0]);
        int minor = parseVersionPart(parts[1]);
        int patch = (parts.length > 2) ? parseVersionPart(parts[2]) : 0;
        String snapshot = (parts.length > 3) ? parts[3] : null;
        return new Version(major, minor, patch, snapshot);
    }

    protected static int parseVersionPart(String partStr)
    {
        partStr = partStr.toString();
        int len = partStr.length();
        int number = 0;
        for (int i = 0; i < len; ++i) {
            char c = partStr.charAt(i);
            if (c > '9' || c < '0') break;
            number = (number * 10) + (c - '0');
        }
        return number;
    }
}
