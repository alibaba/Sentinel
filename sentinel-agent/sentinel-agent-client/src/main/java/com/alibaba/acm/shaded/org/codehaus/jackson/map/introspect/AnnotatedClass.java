package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.AnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.ClassIntrospector.MixInResolver;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.Annotations;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;

public final class AnnotatedClass
    extends Annotated
{
    private final static AnnotationMap[] NO_ANNOTATION_MAPS = new AnnotationMap[0];
    
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Class for which annotations apply, and that owns other
     * components (constructors, methods)
     */
    final protected Class<?> _class;

    /**
     * Ordered set of super classes and interfaces of the
     * class itself: included in order of precedence
     */
    final protected List<Class<?>> _superTypes;

    /**
     * Filter used to determine which annotations to gather; used
     * to optimize things so that unnecessary annotations are
     * ignored.
     */
    final protected AnnotationIntrospector _annotationIntrospector;

    /**
     * Object that knows mapping of mix-in classes (ones that contain
     * annotations to add) with their target classes (ones that
     * get these additional annotations "mixed in").
     */
    final protected MixInResolver _mixInResolver;

    /**
     * Primary mix-in class; one to use for the annotated class
     * itself. Can be null.
     */
    final protected Class<?> _primaryMixIn;

    /*
    /**********************************************************
    /* Gathered information
    /**********************************************************
     */

    /**
     * Combined list of Jackson annotations that the class has,
     * including inheritable ones from super classes and interfaces
     */
    protected AnnotationMap _classAnnotations;

    /**
     * Default constructor of the annotated class, if it has one.
     */
    protected AnnotatedConstructor _defaultConstructor;

    /**
     * Single argument constructors the class has, if any.
     */
    protected List<AnnotatedConstructor> _constructors;

    /**
     * Single argument static methods that might be usable
     * as factory methods
     */
    protected List<AnnotatedMethod> _creatorMethods;

    /**
     * Member methods of interest; for now ones with 0 or 1 arguments
     * (just optimization, since others won't be used now)
     */
    protected AnnotatedMethodMap  _memberMethods;

    /**
     * Member fields of interest: ones that are either public,
     * or have at least one annotation.
     */
    protected List<AnnotatedField> _fields;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * Constructor will not do any initializations, to allow for
     * configuring instances differently depending on use cases
     */
    private AnnotatedClass(Class<?> cls, List<Class<?>> superTypes,
            AnnotationIntrospector aintr, MixInResolver mir,
            AnnotationMap classAnnotations)
    {
        _class = cls;
        _superTypes = superTypes;
        _annotationIntrospector = aintr;
        _mixInResolver = mir;
        _primaryMixIn = (_mixInResolver == null) ? null
            : _mixInResolver.findMixInClassFor(_class);
        _classAnnotations = classAnnotations;
    }

    @Override
    public AnnotatedClass withAnnotations(AnnotationMap ann) {
        return new AnnotatedClass(_class, _superTypes,
                _annotationIntrospector, _mixInResolver, ann);
    }
    
    /**
     * Factory method that instantiates an instance. Returned instance
     * will only be initialized with class annotations, but not with
     * any method information.
     */
    public static AnnotatedClass construct(Class<?> cls,
            AnnotationIntrospector aintr, MixInResolver mir)
    {
        List<Class<?>> st = ClassUtil.findSuperTypes(cls, null);
        AnnotatedClass ac = new AnnotatedClass(cls, st, aintr, mir, null);
        ac.resolveClassAnnotations();
        return ac;
    }

    /**
     * Method similar to {@link #construct}, but that will NOT include
     * information from supertypes; only class itself and any direct
     * mix-ins it may have.
     */
    public static AnnotatedClass constructWithoutSuperTypes(Class<?> cls,
            AnnotationIntrospector aintr, MixInResolver mir)
    {
        List<Class<?>> empty = Collections.emptyList();
        AnnotatedClass ac = new AnnotatedClass(cls, empty, aintr, mir, null);
        ac.resolveClassAnnotations();
        return ac;
    }
    
    /*
    /**********************************************************
    /* Annotated impl 
    /**********************************************************
     */

    @Override
    public Class<?> getAnnotated() { return _class; }

    @Override
    public int getModifiers() { return _class.getModifiers(); }

    @Override
    public String getName() { return _class.getName(); }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls)
    {
        if (_classAnnotations == null) {
            return null;
        }
        return _classAnnotations.get(acls);
    }

    @Override
    public Type getGenericType() {
        return _class;
    }

    @Override
    public Class<?> getRawType() {
        return _class;
    }

    @Override
    protected AnnotationMap getAllAnnotations() {
        return _classAnnotations;
    }
    
    /*
    /**********************************************************
    /* Public API, generic accessors
    /**********************************************************
     */

    /**
     * @return 1.7
     */
    public Annotations getAnnotations() { return _classAnnotations; }
    
    public boolean hasAnnotations() { return _classAnnotations.size() > 0; }

    public AnnotatedConstructor getDefaultConstructor() { return _defaultConstructor; }

    public List<AnnotatedConstructor> getConstructors()
    {
        if (_constructors == null) {
            return Collections.emptyList();
        }
        return _constructors;
    }

    public List<AnnotatedMethod> getStaticMethods()
    {
        if (_creatorMethods == null) {
            return Collections.emptyList();
        }
        return _creatorMethods;
    }

    public Iterable<AnnotatedMethod> memberMethods()
    {
        return _memberMethods;
    }

    public int getMemberMethodCount()
    {
        return _memberMethods.size();
    }

    public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes)
    {
        return _memberMethods.find(name, paramTypes);
    }

    public int getFieldCount() {
        return (_fields == null) ? 0 : _fields.size();
    }

    public Iterable<AnnotatedField> fields()
    {
        if (_fields == null) {
            return Collections.emptyList();
        }
        return _fields;
    }

    /*
    /**********************************************************
    /* Public API, main-level resolution methods
    /**********************************************************
     */

    /**
     * Initialization method that will recursively collect Jackson
     * annotations for this class and all super classes and
     * interfaces.
     *<p>
     * Starting with 1.2, it will also apply mix-in annotations,
     * as per [JACKSON-76]
     */
    public void resolveClassAnnotations()
    {
        _classAnnotations = new AnnotationMap();
        // [JACKSON-659] Should skip processing if annotation processing disabled
        if (_annotationIntrospector == null) {
            return;
        }
        
        // add mix-in annotations first (overrides)
        if (_primaryMixIn != null) {
            _addClassMixIns(_classAnnotations, _class, _primaryMixIn);
        }
        // first, annotations from the class itself:
        for (Annotation a : _class.getDeclaredAnnotations()) {
            if (_annotationIntrospector.isHandled(a)) {
                _classAnnotations.addIfNotPresent(a);
            }
        }

        // and then from super types
        for (Class<?> cls : _superTypes) {
            // and mix mix-in annotations in-between
            _addClassMixIns(_classAnnotations, cls);
            for (Annotation a : cls.getDeclaredAnnotations()) {
                if (_annotationIntrospector.isHandled(a)) {
                    _classAnnotations.addIfNotPresent(a);
                }
            }
        }

        /* and finally... any annotations there might be for plain
         * old Object.class: separate because for all other purposes
         * it is just ignored (not included in super types)
         */
        /* 12-Jul-2009, tatu: Should this be done for interfaces too?
         *   For now, yes, seems useful for some cases, and not harmful
         *   for any?
         */
        _addClassMixIns(_classAnnotations, Object.class);
    }

    /**
     * Initialization method that will find out all constructors
     * and potential static factory methods the class has.
     *<p>
     * Starting with 1.2, it will also apply mix-in annotations,
     * as per [JACKSON-76]
     *
     * @param includeAll If true, includes all creator methods; if false,
     *   will only include the no-arguments "default" constructor
     */
    public void resolveCreators(boolean includeAll)
    {
        // Then see which constructors we have
        _constructors = null;
        Constructor<?>[] declaredCtors = _class.getDeclaredConstructors();
        for (Constructor<?> ctor : declaredCtors) {
            if (ctor.getParameterTypes().length == 0) {
                _defaultConstructor = _constructConstructor(ctor, true);
            } else {
                if (includeAll) {
                    if (_constructors == null) {
                        _constructors = new ArrayList<AnnotatedConstructor>(Math.max(10, declaredCtors.length));
                    }
                    _constructors.add(_constructConstructor(ctor, false));
                }
            }
        }
        // and if need be, augment with mix-ins
        if (_primaryMixIn != null) {
            if (_defaultConstructor != null || _constructors != null) {
                _addConstructorMixIns(_primaryMixIn);
            }
        }


        /* And then... let's remove all constructors that are deemed
         * ignorable after all annotations have been properly collapsed.
         */
        // 14-Feb-2011, tatu: AnnotationIntrospector is null if annotations not enabled; if so, can skip:
        if (_annotationIntrospector != null) {
            if (_defaultConstructor != null) {
                if (_annotationIntrospector.isIgnorableConstructor(_defaultConstructor)) {
                    _defaultConstructor = null;
                }
            }
            if (_constructors != null) {
                // count down to allow safe removal
                for (int i = _constructors.size(); --i >= 0; ) {
                    if (_annotationIntrospector.isIgnorableConstructor(_constructors.get(i))) {
                        _constructors.remove(i);
                    }
                }
            }
        }

        _creatorMethods = null;
        
        if (includeAll) {
            // Then static methods which are potential factory methods
            for (Method m : _class.getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                int argCount = m.getParameterTypes().length;
                // factory methods take at least one arg:
                if (argCount < 1) {
                    continue;
                }
                if (_creatorMethods == null) {
                    _creatorMethods = new ArrayList<AnnotatedMethod>(8);
                }
                _creatorMethods.add(_constructCreatorMethod(m));
            }
            // mix-ins to mix in?
            if (_primaryMixIn != null && _creatorMethods != null) {
                _addFactoryMixIns(_primaryMixIn);
            }
            // anything to ignore at this point?
            if (_annotationIntrospector != null) {
                if (_creatorMethods != null) {
                    // count down to allow safe removal
                    for (int i = _creatorMethods.size(); --i >= 0; ) {
                        if (_annotationIntrospector.isIgnorableMethod(_creatorMethods.get(i))) {
                            _creatorMethods.remove(i);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Method for resolving member method information: aggregating all non-static methods
     * and combining annotations (to implement method-annotation inheritance)
     * 
     * @param methodFilter Filter used to determine which methods to include
     * 
     * @since 1.9
     */
    public void resolveMemberMethods(MethodFilter methodFilter)
    {
        _memberMethods = new AnnotatedMethodMap();
        AnnotatedMethodMap mixins = new AnnotatedMethodMap();
        // first: methods from the class itself
        _addMemberMethods(_class, methodFilter, _memberMethods, _primaryMixIn, mixins);

        // and then augment these with annotations from super-types:
        for (Class<?> cls : _superTypes) {
            Class<?> mixin = (_mixInResolver == null) ? null : _mixInResolver.findMixInClassFor(cls);
            _addMemberMethods(cls, methodFilter, _memberMethods, mixin, mixins);
        }
        // Special case: mix-ins for Object.class? (to apply to ALL classes)
        if (_mixInResolver != null) {
            Class<?> mixin = _mixInResolver.findMixInClassFor(Object.class);
            if (mixin != null) {
                _addMethodMixIns(methodFilter, _memberMethods, mixin, mixins);
            }
        }

        /* Any unmatched mix-ins? Most likely error cases (not matching
         * any method); but there is one possible real use case:
         * exposing Object#hashCode (alas, Object#getClass can NOT be
         * exposed, see [JACKSON-140])
         */
        // 14-Feb-2011, tatu: AnnotationIntrospector is null if annotations not enabled; if so, can skip:
        if (_annotationIntrospector != null) {
            if (!mixins.isEmpty()) {
                Iterator<AnnotatedMethod> it = mixins.iterator();
                while (it.hasNext()) {
                    AnnotatedMethod mixIn = it.next();
                    try {
                        Method m = Object.class.getDeclaredMethod(mixIn.getName(), mixIn.getParameterClasses());
                        if (m != null) {
                            AnnotatedMethod am = _constructMethod(m);
                            _addMixOvers(mixIn.getAnnotated(), am, false);
                            _memberMethods.add(am);
                        }
                    } catch (Exception e) { }
                }
            }
        }
    }
    
    /**
     * Method that will collect all member (non-static) fields
     * that are either public, or have at least a single annotation
     * associated with them.
     * 
     * @since 1.9
     */
    public void resolveFields()
    {
        LinkedHashMap<String,AnnotatedField> foundFields = new LinkedHashMap<String,AnnotatedField>();
        _addFields(foundFields, _class);
        if (foundFields.isEmpty()) {
            _fields = Collections.emptyList();
        } else {
            _fields = new ArrayList<AnnotatedField>(foundFields.size());
            _fields.addAll(foundFields.values());
        }
    }

    /*
    /**********************************************************
    /* Deprecated methods
    /**********************************************************
     */
    
    /**
     * @since 1.9 Use version without arguments
     */
    @Deprecated
    public void resolveMemberMethods(MethodFilter methodFilter, boolean collectIgnored)
    {
        resolveMemberMethods(methodFilter);
    }
    
    /**
     * @since 1.9 Use version without arguments
     */
    @Deprecated
    public void resolveFields(boolean collectIgnored)
    {
        resolveFields();
    }
    
    /*
    /**********************************************************
    /* Helper methods for resolving class annotations
    /* (resolution consisting of inheritance, overrides,
    /* and injection of mix-ins as necessary)
    /**********************************************************
     */
    
    /**
     * Helper method for adding any mix-in annotations specified
     * class might have.
     */
    protected void _addClassMixIns(AnnotationMap annotations, Class<?> toMask)
    {
        if (_mixInResolver != null) {
            _addClassMixIns(annotations, toMask, _mixInResolver.findMixInClassFor(toMask));
        }
    }

    protected void _addClassMixIns(AnnotationMap annotations, Class<?> toMask,
                                   Class<?> mixin)
    {
        if (mixin == null) {
            return;
        }
        // Ok, first: annotations from mix-in class itself:
        for (Annotation a : mixin.getDeclaredAnnotations()) {
            if (_annotationIntrospector.isHandled(a)) {
                annotations.addIfNotPresent(a);
            }
        }
        /* And then from its supertypes, if any. But note that we will
         *  only consider super-types up until reaching the masked
         * class (if found); this because often mix-in class
         * is a sub-class (for convenience reasons). And if so, we
         * absolutely must NOT include super types of masked class,
         * as that would inverse precedence of annotations.
         */
        for (Class<?> parent : ClassUtil.findSuperTypes(mixin, toMask)) {
            for (Annotation a : parent.getDeclaredAnnotations()) {
                if (_annotationIntrospector.isHandled(a)) {
                    annotations.addIfNotPresent(a);
                }
            }
        }
    }

    /*
    /**********************************************************
    /* Helper methods for populating creator (ctor, factory) information
    /**********************************************************
     */

    protected void _addConstructorMixIns(Class<?> mixin)
    {
        MemberKey[] ctorKeys = null;
        int ctorCount = (_constructors == null) ? 0 : _constructors.size();
        for (Constructor<?> ctor : mixin.getDeclaredConstructors()) {
            if (ctor.getParameterTypes().length == 0) {
                if (_defaultConstructor != null) {
                    _addMixOvers(ctor, _defaultConstructor, false);
                }
            } else {
                if (ctorKeys == null) {
                    ctorKeys = new MemberKey[ctorCount];
                    for (int i = 0; i < ctorCount; ++i) {
                        ctorKeys[i] = new MemberKey(_constructors.get(i).getAnnotated());
                    }
                }
                MemberKey key = new MemberKey(ctor);

                for (int i = 0; i < ctorCount; ++i) {
                    if (!key.equals(ctorKeys[i])) {
                        continue;
                    }
                    _addMixOvers(ctor, _constructors.get(i), true);
                    break;
                }
            }
        }
    }

    protected void _addFactoryMixIns(Class<?> mixin)
    {
        MemberKey[] methodKeys = null;
        int methodCount = _creatorMethods.size();

        for (Method m : mixin.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            if (m.getParameterTypes().length == 0) {
                continue;
            }
            if (methodKeys == null) {
                methodKeys = new MemberKey[methodCount];
                for (int i = 0; i < methodCount; ++i) {
                    methodKeys[i] = new MemberKey(_creatorMethods.get(i).getAnnotated());
                }
            }
            MemberKey key = new MemberKey(m);
            for (int i = 0; i < methodCount; ++i) {
                if (!key.equals(methodKeys[i])) {
                    continue;
                }
                _addMixOvers(m, _creatorMethods.get(i), true);
                break;
            }
        }
    }

    /*
    /**********************************************************
    /* Helper methods for populating method information
    /**********************************************************
     */

    protected void _addMemberMethods(Class<?> cls,
            MethodFilter methodFilter, AnnotatedMethodMap methods,
            Class<?> mixInCls, AnnotatedMethodMap mixIns)
    {
        // first, mixIns, since they have higher priority then class methods
        if (mixInCls != null) {
            _addMethodMixIns(methodFilter, methods, mixInCls, mixIns);
        }

        if (cls == null) { // just so caller need not check when passing super-class
            return;
        }
        // then methods from the class itself
        for (Method m : cls.getDeclaredMethods()) {
            if (!_isIncludableMethod(m, methodFilter)) {
                continue;
            }
            AnnotatedMethod old = methods.find(m);
            if (old == null) {
                AnnotatedMethod newM = _constructMethod(m);
                methods.add(newM);
                // Ok, but is there a mix-in to connect now?
                old = mixIns.remove(m);
                if (old != null) {
                    _addMixOvers(old.getAnnotated(), newM, false);
                }
            } else {
                /* If sub-class already has the method, we only want to augment
                 * annotations with entries that are not masked by sub-class.
                 */
                _addMixUnders(m, old);

                /* 06-Jan-2010, tatu: [JACKSON-450] Except that if method we saw first is
                 *   from an interface, and we now find a non-interface definition, we should
                 *   use this method, but with combination of annotations.
                 *   This helps (or rather, is essential) with JAXB annotations and
                 *   may also result in faster method calls (interface calls are slightly
                 *   costlier than regular method calls)
                 */
                if (old.getDeclaringClass().isInterface() && !m.getDeclaringClass().isInterface()) {
                    methods.add(old.withMethod(m));
                }
            }
        }
    }

    protected void _addMethodMixIns(MethodFilter methodFilter, AnnotatedMethodMap methods,
            Class<?> mixInCls, AnnotatedMethodMap mixIns)
    {
        for (Method m : mixInCls.getDeclaredMethods()) {
            if (!_isIncludableMethod(m, methodFilter)) {
                continue;
            }
            AnnotatedMethod am = methods.find(m);
            /* Do we already have a method to augment (from sub-class
             * that will mask this mixIn)? If so, add if visible
             * without masking (no such annotation)
             */
            if (am != null) {
                _addMixUnders(m, am);
                /* Otherwise will have precedence, but must wait
                 * until we find the real method (mixIn methods are
                 * just placeholder, can't be called)
                 */
            } else {
                mixIns.add(_constructMethod(m));
            }
        }
    }

    /*
    /**********************************************************
    /* Helper methods for populating field information
    /**********************************************************
     */

    protected void _addFields(Map<String,AnnotatedField> fields, Class<?> c)
    {
        /* First, a quick test: we only care for regular classes (not
         * interfaces, primitive types etc), except for Object.class.
         * A simple check to rule out other cases is to see if there
         * is a super class or not.
         */
        Class<?> parent = c.getSuperclass();
        if (parent != null) {
            // Let's add super-class' fields first, then ours.
            /* 21-Feb-2010, tatu: Need to handle masking: as per [JACKSON-226]
             *    we otherwise get into trouble...
             */
            _addFields(fields, parent);
            for (Field f : c.getDeclaredFields()) {
                // static fields not included, nor transient
                if (!_isIncludableField(f)) {
                    continue;
                }
                /* Ok now: we can (and need) not filter out ignorable fields
                 * at this point; partly because mix-ins haven't been
                 * added, and partly because logic can be done when
                 * determining get/settability of the field.
                 */
                fields.put(f.getName(), _constructField(f));
            }
            // And then... any mix-in overrides?
            if (_mixInResolver != null) {
                Class<?> mixin = _mixInResolver.findMixInClassFor(c);
                if (mixin != null) {
                    _addFieldMixIns(mixin, fields);
                }
            }
        }
    }

    /**
     * Method called to add field mix-ins from given mix-in class (and its fields)
     * into already collected actual fields (from introspected classes and their
     * super-classes)
     */
    protected void _addFieldMixIns(Class<?> mixin, Map<String,AnnotatedField> fields)
    {
        for (Field mixinField : mixin.getDeclaredFields()) {
            /* there are some dummy things (static, synthetic); better
             * ignore
             */
            if (!_isIncludableField(mixinField)) {
                continue;
            }
            String name = mixinField.getName();
            // anything to mask? (if not, quietly ignore)
            AnnotatedField maskedField = fields.get(name);
            if (maskedField != null) {
                for (Annotation a : mixinField.getDeclaredAnnotations()) {
                    if (_annotationIntrospector.isHandled(a)) {
                        maskedField.addOrOverride(a);
                    }
                }
            }
        }
    }

    /*
    /**********************************************************
    /* Helper methods, constructing value types
    /**********************************************************
     */

    protected AnnotatedMethod _constructMethod(Method m)
    {
        /* note: parameter annotations not used for regular (getter, setter)
         * methods; only for creator methods (static factory methods)
         * -- at least not yet!
         */
        if (_annotationIntrospector == null) { // when annotation processing is disabled
            return new AnnotatedMethod(m, _emptyAnnotationMap(), null);
        }
        return new AnnotatedMethod(m, _collectRelevantAnnotations(m.getDeclaredAnnotations()), null);
    }

    protected AnnotatedConstructor _constructConstructor(Constructor<?> ctor, boolean defaultCtor)
    {
        if (_annotationIntrospector == null) { // when annotation processing is disabled
            return new AnnotatedConstructor(ctor, _emptyAnnotationMap(), _emptyAnnotationMaps(ctor.getParameterTypes().length));
        }
        if (defaultCtor) {
            return new AnnotatedConstructor(ctor, _collectRelevantAnnotations(ctor.getDeclaredAnnotations()), null);
        }
        Annotation[][] paramAnns = ctor.getParameterAnnotations();
        int paramCount = ctor.getParameterTypes().length;
        /* [JACKSON-701]: Looks like JDK has discrepancy, whereas annotations for implicit 'this'
         * (for non-static inner classes) are NOT included, but type is? Strange, sounds like
         * a bug. Alas, we can't really fix that...
         */
        /* Also: [JACKSON-767] (enum value constructors)
         */
        AnnotationMap[] resolvedAnnotations = null;
        if (paramCount != paramAnns.length) {
            // Limits of the work-around (to avoid hiding real errors):
            // first, only applicable for member classes and then either:

            Class<?> dc = ctor.getDeclaringClass();
            // (a) is enum, which have two extra hidden params (name, index)
            if (dc.isEnum() && (paramCount == paramAnns.length + 2)) {
                Annotation[][] old = paramAnns;
                paramAnns = new Annotation[old.length+2][];
                System.arraycopy(old, 0, paramAnns, 2, old.length);
                resolvedAnnotations = _collectRelevantAnnotations(paramAnns);
            } else if (dc.isMemberClass()) {
                // (b) non-static inner classes, get implicit 'this' for parameter, not  annotation
                if (paramCount == (paramAnns.length + 1)) {
                    // hack attack: prepend a null entry to make things match
                    Annotation[][] old = paramAnns;
                    paramAnns = new Annotation[old.length+1][];
                    System.arraycopy(old, 0, paramAnns, 1, old.length);
                    resolvedAnnotations = _collectRelevantAnnotations(paramAnns);
                }
            }
            if (resolvedAnnotations == null) {
                throw new IllegalStateException("Internal error: constructor for "+ctor.getDeclaringClass().getName()
                        +" has mismatch: "+paramCount+" parameters; "+paramAnns.length+" sets of annotations");
            }
        } else {
            resolvedAnnotations = _collectRelevantAnnotations(paramAnns);
        }
        return new AnnotatedConstructor(ctor, _collectRelevantAnnotations(ctor.getDeclaredAnnotations()),
                resolvedAnnotations);
    }

    protected AnnotatedMethod _constructCreatorMethod(Method m)
    {
        if (_annotationIntrospector == null) { // when annotation processing is disabled
            return new AnnotatedMethod(m, _emptyAnnotationMap(), _emptyAnnotationMaps(m.getParameterTypes().length));
        }
        return new AnnotatedMethod(m, _collectRelevantAnnotations(m.getDeclaredAnnotations()),
                                   _collectRelevantAnnotations(m.getParameterAnnotations()));
    }

    protected AnnotatedField _constructField(Field f)
    {
        if (_annotationIntrospector == null) { // when annotation processing is disabled
            return new AnnotatedField(f, _emptyAnnotationMap());
        }
        return new AnnotatedField(f, _collectRelevantAnnotations(f.getDeclaredAnnotations()));
    }

    protected AnnotationMap[] _collectRelevantAnnotations(Annotation[][] anns)
    {
        int len = anns.length;
        AnnotationMap[] result = new AnnotationMap[len];
        for (int i = 0; i < len; ++i) {
            result[i] = _collectRelevantAnnotations(anns[i]);
        }
        return result;
    }

    protected AnnotationMap _collectRelevantAnnotations(Annotation[] anns)
    {
        AnnotationMap annMap = new AnnotationMap();
        if (anns != null) {
            for (Annotation a : anns) {
                if (_annotationIntrospector.isHandled(a)) {
                    annMap.add(a);
                }
            }
        }
        return annMap;
    }
 
    private AnnotationMap _emptyAnnotationMap() {
        return new AnnotationMap();
    }

    private AnnotationMap[] _emptyAnnotationMaps(int count) {
        if (count == 0) {
            return NO_ANNOTATION_MAPS;
        }
        AnnotationMap[] maps = new AnnotationMap[count];
        for (int i = 0; i < count; ++i) {
            maps[i] = _emptyAnnotationMap();
        }
        return maps;
    }
    
    /*
    /**********************************************************
    /* Helper methods, inclusion filtering
    /**********************************************************
     */

    protected boolean _isIncludableMethod(Method m, MethodFilter filter)
    {
        if (filter != null && !filter.includeMethod(m)) {
            return false;
        }
        /* 07-Apr-2009, tatu: Looks like generics can introduce hidden
         *   bridge and/or synthetic methods. I don't think we want to
         *   consider those...
         */
        if (m.isSynthetic() || m.isBridge()) {
            return false;
        }
        return true;
    }

    private boolean _isIncludableField(Field f)
    {
        /* I'm pretty sure synthetic fields are to be skipped...
         * (methods definitely are)
         */
        if (f.isSynthetic()) {
            return false;
        }
        // Static fields are never included, nor transient
        int mods = f.getModifiers();
        if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
            return false;
        }
        return true;
    }

    /*
    /**********************************************************
    /* Helper methods, attaching annotations
    /**********************************************************
     */

    /**
     * @param addParamAnnotations Whether parameter annotations are to be
     *   added as well
     */
    protected void _addMixOvers(Constructor<?> mixin, AnnotatedConstructor target,
            boolean addParamAnnotations)
    {
        for (Annotation a : mixin.getDeclaredAnnotations()) {
            if (_annotationIntrospector.isHandled(a)) {
                target.addOrOverride(a);
            }
        }
        if (addParamAnnotations) {
            Annotation[][] pa = mixin.getParameterAnnotations();
            for (int i = 0, len = pa.length; i < len; ++i) {
                for (Annotation a : pa[i]) {
                    target.addOrOverrideParam(i, a);
                }
            }
        }
    }

    /**
     * @param addParamAnnotations Whether parameter annotations are to be
     *   added as well
     */
    protected void _addMixOvers(Method mixin, AnnotatedMethod target,
            boolean addParamAnnotations)
    {
        for (Annotation a : mixin.getDeclaredAnnotations()) {
            if (_annotationIntrospector.isHandled(a)) {
                target.addOrOverride(a);
            }
        }
        if (addParamAnnotations) {
            Annotation[][] pa = mixin.getParameterAnnotations();
            for (int i = 0, len = pa.length; i < len; ++i) {
                for (Annotation a : pa[i]) {
                    target.addOrOverrideParam(i, a);
                }
            }
        }
    }

    /**
     * Method that will add annotations from specified source method to target method,
     * but only if target does not yet have them.
     */
    protected void _addMixUnders(Method src, AnnotatedMethod target)
    {
        for (Annotation a : src.getDeclaredAnnotations()) {
            if (_annotationIntrospector.isHandled(a)) {
                target.addIfNotPresent(a);
            }
        }
    }

    /*
    /**********************************************************
    /* Other methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return "[AnnotedClass "+_class.getName()+"]";
    }
}

