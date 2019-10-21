package javax.xml.bind;

import java.security.PrivilegedAction;

/**
 * {@link PrivilegedAction} that gets the system property value.
 * @author Kohsuke Kawaguchi
 */
final class GetPropertyAction implements PrivilegedAction<String> {
    private final String propertyName;

    public GetPropertyAction(String propertyName) {
        this.propertyName = propertyName;
    }

    public String run() {
        return System.getProperty(propertyName);
    }
}
