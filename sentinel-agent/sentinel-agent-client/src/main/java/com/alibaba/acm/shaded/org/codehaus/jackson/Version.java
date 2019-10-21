package com.alibaba.acm.shaded.org.codehaus.jackson;

/**
 * Object that encapsulates version information of a component,
 * and is return by {@link Versioned#version}.
 * 
 * @since 1.6
 */
public class Version
    implements Comparable<Version>
{
    private final static Version UNKNOWN_VERSION = new Version(0, 0, 0, null);

    protected final int _majorVersion;

    protected final int _minorVersion;

    protected final int _patchLevel;

    /**
     * Additional information for snapshot versions; null for non-snapshot
     * (release) versions.
     */
    protected final String _snapshotInfo;
    
    public Version(int major, int minor, int patchLevel,
            String snapshotInfo)
    {
        _majorVersion = major;
        _minorVersion = minor;
        _patchLevel = patchLevel;
        _snapshotInfo = snapshotInfo;
    }

    /**
     * Method returns canonical "not known" version, which is used as version
     * in cases where actual version information is not known (instead of null).
     */
    public static Version unknownVersion() { return UNKNOWN_VERSION; }

    public boolean isUknownVersion() { return (this == UNKNOWN_VERSION); }
    public boolean isSnapshot() { return (_snapshotInfo != null && _snapshotInfo.length() > 0); }
    
    public int getMajorVersion() { return _majorVersion; }
    public int getMinorVersion() { return _minorVersion; }
    public int getPatchLevel() { return _patchLevel; }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(_majorVersion).append('.');
        sb.append(_minorVersion).append('.');
        sb.append(_patchLevel);
        if (isSnapshot()) {
            sb.append('-').append(_snapshotInfo);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return _majorVersion + _minorVersion + _patchLevel;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        Version other = (Version) o;
        return (other._majorVersion == _majorVersion)
            && (other._minorVersion == _minorVersion)
            && (other._patchLevel == _patchLevel);
    }

    @Override
    public int compareTo(Version other)
    {
        int diff = _majorVersion - other._majorVersion;
        if (diff == 0) {
            diff = _minorVersion - other._minorVersion;
            if (diff == 0) {
                diff = _patchLevel - other._patchLevel;
            }
        }
        return diff;
    }
}
