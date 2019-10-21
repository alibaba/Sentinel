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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of <code>Hashtable</code> that uses <code>WeakReference</code>'s
 * to hold its keys thus allowing them to be reclaimed by the garbage collector.
 * The associated values are retained using strong references.
 * <p>
 * This class follows the semantics of <code>Hashtable</code> as closely as
 * possible. It therefore does not accept null values or keys.
 * <p>
 * <strong>Note:</strong>
 * This is <em>not</em> intended to be a general purpose hash table replacement.
 * This implementation is also tuned towards a particular purpose: for use as a replacement
 * for <code>Hashtable</code> in <code>LogFactory</code>. This application requires
 * good liveliness for <code>get</code> and <code>put</code>. Various tradeoffs
 * have been made with this in mind.
 * <p>
 * <strong>Usage:</strong> typical use case is as a drop-in replacement
 * for the <code>Hashtable</code> used in <code>LogFactory</code> for J2EE environments
 * running 1.3+ JVMs. Use of this class <i>in most cases</i> (see below) will
 * allow classloaders to be collected by the garbage collector without the need
 * to call {@link org.apache.commons.logging.LogFactory#release(ClassLoader) LogFactory.release(ClassLoader)}.
 * <p>
 * <code>org.apache.commons.logging.LogFactory</code> checks whether this class
 * can be supported by the current JVM, and if so then uses it to store
 * references to the <code>LogFactory</code> implementation it loads
 * (rather than using a standard Hashtable instance).
 * Having this class used instead of <code>Hashtable</code> solves
 * certain issues related to dynamic reloading of applications in J2EE-style
 * environments. However this class requires java 1.3 or later (due to its use
 * of <code>java.lang.ref.WeakReference</code> and associates).
 * And by the way, this extends <code>Hashtable</code> rather than <code>HashMap</code>
 * for backwards compatibility reasons. See the documentation
 * for method <code>LogFactory.createFactoryStore</code> for more details.
 * <p>
 * The reason all this is necessary is due to a issue which
 * arises during hot deploy in a J2EE-like containers.
 * Each component running in the container owns one or more classloaders; when
 * the component loads a LogFactory instance via the component classloader
 * a reference to it gets stored in the static LogFactory.factories member,
 * keyed by the component's classloader so different components don't
 * stomp on each other. When the component is later unloaded, the container
 * sets the component's classloader to null with the intent that all the
 * component's classes get garbage-collected. However there's still a
 * reference to the component's classloader from a key in the "global"
 * <code>LogFactory</code>'s factories member! If <code>LogFactory.release()</code>
 * is called whenever component is unloaded, the classloaders will be correctly
 * garbage collected; this <i>should</i> be done by any container that
 * bundles commons-logging by default. However, holding the classloader
 * references weakly ensures that the classloader will be garbage collected
 * without the container performing this step.
 * <p>
 * <strong>Limitations:</strong>
 * There is still one (unusual) scenario in which a component will not
 * be correctly unloaded without an explicit release. Though weak references
 * are used for its keys, it is necessary to use strong references for its values.
 * <p>
 * If the abstract class <code>LogFactory</code> is
 * loaded by the container classloader but a subclass of
 * <code>LogFactory</code> [LogFactory1] is loaded by the component's
 * classloader and an instance stored in the static map associated with the
 * base LogFactory class, then there is a strong reference from the LogFactory
 * class to the LogFactory1 instance (as normal) and a strong reference from
 * the LogFactory1 instance to the component classloader via
 * <code>getClass().getClassLoader()</code>. This chain of references will prevent
 * collection of the child classloader.
 * <p>
 * Such a situation occurs when the commons-logging.jar is
 * loaded by a parent classloader (e.g. a server level classloader in a
 * servlet container) and a custom <code>LogFactory</code> implementation is
 * loaded by a child classloader (e.g. a web app classloader).
 * <p>
 * To avoid this scenario, ensure
 * that any custom LogFactory subclass is loaded by the same classloader as
 * the base <code>LogFactory</code>. Creating custom LogFactory subclasses is,
 * however, rare. The standard LogFactoryImpl class should be sufficient
 * for most or all users.
 *
 * @version $Id: WeakHashtable.java 1435077 2013-01-18 10:51:35Z tn $
 * @since 1.1
 */
public final class WeakHashtable extends Hashtable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -1546036869799732453L;

    /**
     * The maximum number of times put() or remove() can be called before
     * the map will be purged of all cleared entries.
     */
    private static final int MAX_CHANGES_BEFORE_PURGE = 100;

    /**
     * The maximum number of times put() or remove() can be called before
     * the map will be purged of one cleared entry.
     */
    private static final int PARTIAL_PURGE_COUNT     = 10;

    /* ReferenceQueue we check for gc'd keys */
    private final ReferenceQueue queue = new ReferenceQueue();
    /* Counter used to control how often we purge gc'd entries */
    private int changeCount = 0;

    /**
     * Constructs a WeakHashtable with the Hashtable default
     * capacity and load factor.
     */
    public WeakHashtable() {}

    /**
     *@see Hashtable
     */
    public boolean containsKey(Object key) {
        // purge should not be required
        Referenced referenced = new Referenced(key);
        return super.containsKey(referenced);
    }

    /**
     *@see Hashtable
     */
    public Enumeration elements() {
        purge();
        return super.elements();
    }

    /**
     *@see Hashtable
     */
    public Set entrySet() {
        purge();
        Set referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (Iterator it=referencedEntries.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Referenced referencedKey = (Referenced) entry.getKey();
            Object key = referencedKey.getValue();
            Object value = entry.getValue();
            if (key != null) {
                Entry dereferencedEntry = new Entry(key, value);
                unreferencedEntries.add(dereferencedEntry);
            }
        }
        return unreferencedEntries;
    }

    /**
     *@see Hashtable
     */
    public Object get(Object key) {
        // for performance reasons, no purge
        Referenced referenceKey = new Referenced(key);
        return super.get(referenceKey);
    }

    /**
     *@see Hashtable
     */
    public Enumeration keys() {
        purge();
        final Enumeration enumer = super.keys();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return enumer.hasMoreElements();
            }
            public Object nextElement() {
                 Referenced nextReference = (Referenced) enumer.nextElement();
                 return nextReference.getValue();
            }
        };
    }

    /**
     *@see Hashtable
     */
    public Set keySet() {
        purge();
        Set referencedKeys = super.keySet();
        Set unreferencedKeys = new HashSet();
        for (Iterator it=referencedKeys.iterator(); it.hasNext();) {
            Referenced referenceKey = (Referenced) it.next();
            Object keyValue = referenceKey.getValue();
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }

    /**
     *@see Hashtable
     */
    public synchronized Object put(Object key, Object value) {
        // check for nulls, ensuring semantics match superclass
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        }
        if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        }

        // for performance reasons, only purge every
        // MAX_CHANGES_BEFORE_PURGE times
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        // do a partial purge more often
        else if (changeCount % PARTIAL_PURGE_COUNT == 0) {
            purgeOne();
        }

        Referenced keyRef = new Referenced(key, queue);
        return super.put(keyRef, value);
    }

    /**
     *@see Hashtable
     */
    public void putAll(Map t) {
        if (t != null) {
            Set entrySet = t.entrySet();
            for (Iterator it=entrySet.iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     *@see Hashtable
     */
    public Collection values() {
        purge();
        return super.values();
    }

    /**
     *@see Hashtable
     */
    public synchronized Object remove(Object key) {
        // for performance reasons, only purge every
        // MAX_CHANGES_BEFORE_PURGE times
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        // do a partial purge more often
        else if (changeCount % PARTIAL_PURGE_COUNT == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key));
    }

    /**
     *@see Hashtable
     */
    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }

    /**
     *@see Hashtable
     */
    public int size() {
        purge();
        return super.size();
    }

    /**
     *@see Hashtable
     */
    public String toString() {
        purge();
        return super.toString();
    }

    /**
     * @see Hashtable
     */
    protected void rehash() {
        // purge here to save the effort of rehashing dead entries
        purge();
        super.rehash();
    }

    /**
     * Purges all entries whose wrapped keys
     * have been garbage collected.
     */
    private void purge() {
        final List toRemove = new ArrayList();
        synchronized (queue) {
            WeakKey key;
            while ((key = (WeakKey) queue.poll()) != null) {
                toRemove.add(key.getReferenced());
            }
        }

        // LOGGING-119: do the actual removal of the keys outside the sync block
        // to prevent deadlock scenarios as purge() may be called from
        // non-synchronized methods too
        final int size = toRemove.size();
        for (int i = 0; i < size; i++) {
            super.remove(toRemove.get(i));
        }
    }

    /**
     * Purges one entry whose wrapped key
     * has been garbage collected.
     */
    private void purgeOne() {
        synchronized (queue) {
            WeakKey key = (WeakKey) queue.poll();
            if (key != null) {
                super.remove(key.getReferenced());
            }
        }
    }

    /** Entry implementation */
    private final static class Entry implements Map.Entry {

        private final Object key;
        private final Object value;

        private Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public boolean equals(Object o) {
            boolean result = false;
            if (o != null && o instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) o;
                result =    (getKey()==null ?
                                            entry.getKey() == null :
                                            getKey().equals(entry.getKey())) &&
                            (getValue()==null ?
                                            entry.getValue() == null :
                                            getValue().equals(entry.getValue()));
            }
            return result;
        }

        public int hashCode() {
            return (getKey()==null ? 0 : getKey().hashCode()) ^
                (getValue()==null ? 0 : getValue().hashCode());
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }

        public Object getValue() {
            return value;
        }

        public Object getKey() {
            return key;
        }
    }

    /** Wrapper giving correct symantics for equals and hashcode */
    private final static class Referenced {

        private final WeakReference reference;
        private final int           hashCode;

        /**
         *
         * @throws NullPointerException if referant is <code>null</code>
         */
        private Referenced(Object referant) {
            reference = new WeakReference(referant);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = referant.hashCode();
        }

        /**
         *
         * @throws NullPointerException if key is <code>null</code>
         */
        private Referenced(Object key, ReferenceQueue queue) {
            reference = new WeakKey(key, queue, this);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = key.hashCode();

        }

        public int hashCode() {
            return hashCode;
        }

        private Object getValue() {
            return reference.get();
        }

        public boolean equals(Object o) {
            boolean result = false;
            if (o instanceof Referenced) {
                Referenced otherKey = (Referenced) o;
                Object thisKeyValue = getValue();
                Object otherKeyValue = otherKey.getValue();
                if (thisKeyValue == null) {
                    result = otherKeyValue == null;

                    // Since our hashcode was calculated from the original
                    // non-null referant, the above check breaks the
                    // hashcode/equals contract, as two cleared Referenced
                    // objects could test equal but have different hashcodes.
                    // We can reduce (not eliminate) the chance of this
                    // happening by comparing hashcodes.
                    result = result && this.hashCode() == otherKey.hashCode();
                    // In any case, as our c'tor does not allow null referants
                    // and Hashtable does not do equality checks between
                    // existing keys, normal hashtable operations should never
                    // result in an equals comparison between null referants
                }
                else
                {
                    result = thisKeyValue.equals(otherKeyValue);
                }
            }
            return result;
        }
    }

    /**
     * WeakReference subclass that holds a hard reference to an
     * associated <code>value</code> and also makes accessible
     * the Referenced object holding it.
     */
    private final static class WeakKey extends WeakReference {

        private final Referenced referenced;

        private WeakKey(Object key,
                        ReferenceQueue queue,
                        Referenced referenced) {
            super(key, queue);
            this.referenced = referenced;
        }

        private Referenced getReferenced() {
            return referenced;
        }
     }
}
