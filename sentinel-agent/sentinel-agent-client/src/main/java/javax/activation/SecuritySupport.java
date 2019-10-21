/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)SecuritySupport.java	1.4 07/05/14
 */

package javax.activation;

import java.security.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Security related methods that only work on J2SE 1.2 and newer.
 */
class SecuritySupport {

    private SecuritySupport() {
	// private constructor, can't create an instance
    }

    public static ClassLoader getContextClassLoader() {
	return (ClassLoader)
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		ClassLoader cl = null;
		try {
		    cl = Thread.currentThread().getContextClassLoader();
		} catch (SecurityException ex) { }
		return cl;
	    }
	});
    }

    public static InputStream getResourceAsStream(final Class c,
				final String name) throws IOException {
	try {
	    return (InputStream)
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			return c.getResourceAsStream(name);
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }

    public static URL[] getResources(final ClassLoader cl, final String name) {
	return (URL[])
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		URL[] ret = null;
		try {
		    List v = new ArrayList();
		    Enumeration e = cl.getResources(name);
		    while (e != null && e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			if (url != null)
			    v.add(url);
		    }
		    if (v.size() > 0) {
			ret = new URL[v.size()];
			ret = (URL[])v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    public static URL[] getSystemResources(final String name) {
	return (URL[])
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		URL[] ret = null;
		try {
		    List v = new ArrayList();
		    Enumeration e = ClassLoader.getSystemResources(name);
		    while (e != null && e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			if (url != null)
			    v.add(url);
		    }
		    if (v.size() > 0) {
			ret = new URL[v.size()];
			ret = (URL[])v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    public static InputStream openStream(final URL url) throws IOException {
	try {
	    return (InputStream)
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			return url.openStream();
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }
}
