/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.LogFactory;

/**
 * This class is capable of receiving notifications about the undeployment of
 * a webapp, and responds by ensuring that commons-logging releases all
 * memory associated with the undeployed webapp.
 * <p>
 * In general, the WeakHashtable support added in commons-logging release 1.1
 * ensures that logging classes do not hold references that prevent an
 * undeployed webapp's memory from being garbage-collected even when multiple
 * copies of commons-logging are deployed via multiple classloaders (a
 * situation that earlier versions had problems with). However there are
 * some rare cases where the WeakHashtable approach does not work; in these
 * situations specifying this class as a listener for the web application will
 * ensure that all references held by commons-logging are fully released.
 * <p>
 * To use this class, configure the webapp deployment descriptor to call
 * this class on webapp undeploy; the contextDestroyed method will tell
 * every accessible LogFactory class that the entry in its map for the
 * current webapp's context classloader should be cleared.
 *
 * @version $Id: ServletContextCleaner.java 1432580 2013-01-13 10:41:05Z tn $
 * @since 1.1
 */
public class ServletContextCleaner implements ServletContextListener {

    private static final Class[] RELEASE_SIGNATURE = {ClassLoader.class};

    /**
     * Invoked when a webapp is undeployed, this tells the LogFactory
     * class to release any logging information related to the current
     * contextClassloader.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        Object[] params = new Object[1];
        params[0] = tccl;

        // Walk up the tree of classloaders, finding all the available
        // LogFactory classes and releasing any objects associated with
        // the tccl (ie the webapp).
        //
        // When there is only one LogFactory in the classpath, and it
        // is within the webapp being undeployed then there is no problem;
        // garbage collection works fine.
        //
        // When there are multiple LogFactory classes in the classpath but
        // parent-first classloading is used everywhere, this loop is really
        // short. The first instance of LogFactory found will
        // be the highest in the classpath, and then no more will be found.
        // This is ok, as with this setup this will be the only LogFactory
        // holding any data associated with the tccl being released.
        //
        // When there are multiple LogFactory classes in the classpath and
        // child-first classloading is used in any classloader, then multiple
        // LogFactory instances may hold info about this TCCL; whenever the
        // webapp makes a call into a class loaded via an ancestor classloader
        // and that class calls LogFactory the tccl gets registered in
        // the LogFactory instance that is visible from the ancestor
        // classloader. However the concrete logging library it points
        // to is expected to have been loaded via the TCCL, so the
        // underlying logging lib is only initialised/configured once.
        // These references from ancestor LogFactory classes down to
        // TCCL classloaders are held via weak references and so should
        // be released but there are circumstances where they may not.
        // Walking up the classloader ancestry ladder releasing
        // the current tccl at each level tree, though, will definitely
        // clear any problem references.
        ClassLoader loader = tccl;
        while (loader != null) {
            // Load via the current loader. Note that if the class is not accessible
            // via this loader, but is accessible via some ancestor then that class
            // will be returned.
            try {
                Class logFactoryClass = loader.loadClass("org.apache.commons.logging.LogFactory");
                Method releaseMethod = logFactoryClass.getMethod("release", RELEASE_SIGNATURE);
                releaseMethod.invoke(null, params);
                loader = logFactoryClass.getClassLoader().getParent();
            } catch(ClassNotFoundException ex) {
                // Neither the current classloader nor any of its ancestors could find
                // the LogFactory class, so we can stop now.
                loader = null;
            } catch(NoSuchMethodException ex) {
                // This is not expected; every version of JCL has this method
                System.err.println("LogFactory instance found which does not support release method!");
                loader = null;
            } catch(IllegalAccessException ex) {
                // This is not expected; every ancestor class should be accessible
                System.err.println("LogFactory instance found which is not accessable!");
                loader = null;
            } catch(InvocationTargetException ex) {
                // This is not expected
                System.err.println("LogFactory instance release method failed!");
                loader = null;
            }
        }

        // Just to be sure, invoke release on the LogFactory that is visible from
        // this ServletContextCleaner class too. This should already have been caught
        // by the above loop but just in case...
        LogFactory.release(tccl);
    }

    /**
     * Invoked when a webapp is deployed. Nothing needs to be done here.
     */
    public void contextInitialized(ServletContextEvent sce) {
        // do nothing
    }
}
