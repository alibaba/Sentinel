package com.alibaba.jvm.sandbox.api.listener.ext;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.AccessFlags;
import com.alibaba.jvm.sandbox.api.filter.ExtFilter;
import com.alibaba.jvm.sandbox.api.filter.Filter;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher.Progress;
import com.alibaba.jvm.sandbox.api.util.GaArrayUtils;
import com.alibaba.jvm.sandbox.api.util.GaStringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.*;
import static com.alibaba.jvm.sandbox.api.listener.ext.EventWatchBuilder.PatternType.WILDCARD;
import static com.alibaba.jvm.sandbox.api.util.GaCollectionUtils.add;
import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassName;
import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassNameArray;
import static java.util.regex.Pattern.quote;

/**
 * 事件观察者类构建器
 * <p>
 * 方便构建事件观察者，原有的{@link Filter}是一个比较原始、暴力、直接的接口，虽然很万能，但要精巧的构造门槛很高！
 * 这里设计一个Builder对是为了降低实现的门槛
 * </p>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public class EventWatchBuilder {

    /**
     * 构建类匹配器
     */
    public interface IBuildingForClass {

        /**
         * 是否包含被Bootstrap所加载的类
         * <p>
         * 类似如："java.lang.String"等，都是来自BootstrapClassLoader所加载的类。
         * 如果你需要增强他们则必须在{@code sandbox.properties}文件，将UNSAFE开关打开{@code unsafe.enable=true}
         * </p>
         *
         * @return IBuildingForClass
         */
        IBuildingForClass includeBootstrap();

        /**
         * 是否包含被Bootstrap所加载的类
         *
         * @param isIncludeBootstrap TRUE:包含Bootstrap;FALSE:不包含Bootstrap;
         * @return IBuildingForClass
         * @see #includeBootstrap()
         * @since {@code sandbox-api:1.0.15}
         */
        IBuildingForClass isIncludeBootstrap(boolean isIncludeBootstrap);

        /**
         * {@link #onClass}所指定的类，检索路径是否包含子类（实现类）
         * <ul>
         * <li>如果onClass()了一个接口，则匹配时会搜索这个接口的所有实现类</li>
         * <li>如果onClass()了一个类，则匹配时会搜索这个类的所有子类</li>
         * </ul>
         *
         * @return IBuildingForClass
         */
        IBuildingForClass includeSubClasses();

        /**
         * 是否包含被Bootstrap所加载的类
         *
         * @param isIncludeSubClasses TRUE:包含子类（实现类）;FALSE:不包含子类（实现类）;
         * @return IBuildingForClass
         * @see #includeSubClasses()
         * @since {@code sandbox-api:1.0.15}
         */
        IBuildingForClass isIncludeSubClasses(boolean isIncludeSubClasses);

        /**
         * 类修饰匹配
         *
         * @param access access flag
         * @return IBuildingForClass
         * @see AccessFlags
         */
        IBuildingForClass withAccess(int access);

        /**
         * 类是否声明实现了某一组接口
         *
         * @param classes 接口组类型数组
         * @return IBuildingForClass
         * @see #hasInterfaceTypes(String...)
         */
        IBuildingForClass hasInterfaceTypes(Class<?>... classes);

        /**
         * 类是否声明实现了某一组接口
         * <p>
         * 接口组是一个可变参数组，匹配关系为"与"。即：当前类必须同时实现接口模式匹配组的所有接口才能匹配通过
         * </p>
         *
         * @param patterns 接口组匹配模版
         * @return IBuildingForClass
         */
        IBuildingForClass hasInterfaceTypes(String... patterns);

        /**
         * 类是否拥有某一组标注
         *
         * @param classes 标注组类型数组
         * @return IBuildingForClass
         * @see #hasAnnotationTypes(String...)
         */
        IBuildingForClass hasAnnotationTypes(Class<?>... classes);

        /**
         * 类是否拥有某一组标注
         * <p>
         * 标注组是一个可变参数组，匹配关系为"与"。即：当前类必须同时满足所有标注匹配条件！
         * </p>
         *
         * @param patterns 标注组匹配模版
         * @return IBuildingForClass
         */
        IBuildingForClass hasAnnotationTypes(String... patterns);

        /**
         * 构建行为匹配器，匹配任意行为
         * <p>
         * 等同于{@code onBehavior("*")}
         * </p>
         *
         * @return IBuildingForBehavior
         */
        IBuildingForBehavior onAnyBehavior();

        /**
         * 构建行为匹配器，匹配符合模版匹配名称的行为
         *
         * @param pattern 行为名称
         * @return IBuildingForBehavior
         */
        IBuildingForBehavior onBehavior(String pattern);

    }

    /**
     * 构建方法匹配器
     */
    public interface IBuildingForBehavior {

        IBuildingForBehavior withAccess(int access);

        IBuildingForBehavior withEmptyParameterTypes();

        IBuildingForBehavior withParameterTypes(String... patterns);

        IBuildingForBehavior withParameterTypes(Class<?>... classes);

        IBuildingForBehavior hasExceptionTypes(String... patterns);

        IBuildingForBehavior hasExceptionTypes(Class<?>... classes);

        IBuildingForBehavior hasAnnotationTypes(String... patterns);

        IBuildingForBehavior hasAnnotationTypes(Class<?>... classes);

        IBuildingForBehavior onBehavior(String pattern);

        IBuildingForClass onClass(String pattern);

        IBuildingForClass onClass(Class<?> clazz);

        IBuildingForClass onAnyClass();

        IBuildingForWatching onWatching();

        EventWatcher onWatch(AdviceListener adviceListener, Event.Type... eventTypeArray);

        EventWatcher onWatch(EventListener eventListener, Event.Type... eventTypeArray);

    }

    /**
     * 构建观察构建器
     */
    public interface IBuildingForWatching {

        /**
         * 添加渲染进度监听器，可以添加多个
         * <p>
         * 用于观察{@link #onWatch(AdviceListener)}和{@link #onWatch(EventListener, Event.Type...)}的渲染进度
         * </p>
         *
         * @param progress 渲染进度监听器
         * @return IBuildingForWatching
         */
        IBuildingForWatching withProgress(Progress progress);

        /**
         * 观察行为内部的方法调用
         * 调用之后，
         * <ul>
         * <li>{@link AdviceListener#beforeCall(Advice, int, String, String, String)}</li>
         * <li>{@link AdviceListener#afterCallReturning(Advice, int, String, String, String)}</li>
         * <li>{@link AdviceListener#afterCallThrowing(Advice, int, String, String, String, String)}</li>
         * </ul>
         * <p>
         * 将会被触发
         *
         * @return IBuildingForWatching
         */
        IBuildingForWatching withCall();

        /**
         * 观察行为内部的行调用
         * 调用之后，
         * <ul>
         * <li>{@link AdviceListener#beforeLine(Advice, int)}</li>
         * </ul>
         * 将会被触发
         *
         * @return IBuildingForWatching
         */
        IBuildingForWatching withLine();

        /**
         * 使用通知监听器观察
         *
         * @param adviceListener 通知监听器
         * @return EventWatcher
         */
        EventWatcher onWatch(AdviceListener adviceListener);

        /**
         * 使用事件监听器观察
         *
         * @param eventListener  事件监听器
         * @param eventTypeArray 需要监听的事件
         * @return EventWatcher
         */
        EventWatcher onWatch(EventListener eventListener, Event.Type... eventTypeArray);

    }

    /**
     * 构建删除观察构建器
     */
    public interface IBuildingForUnWatching {

        /**
         * 添加渲染进度监听器，可以添加多个
         * <p>
         * 用于观察{@link #onUnWatched()}方法渲染类的进度
         * </p>
         *
         * @param progress 渲染进度监听器
         * @return IBuildingForWatching
         */
        IBuildingForUnWatching withProgress(Progress progress);

        /**
         * 删除观察者
         */
        void onUnWatched();

    }


    // -------------------------- 这里开始实现 --------------------------

    /**
     * 模版匹配模式
     *
     * @since {@code sandbox-api:1.1.2}
     */
    public enum PatternType {

        /**
         * 通配符表达式
         */
        WILDCARD,

        /**
         * 正则表达式
         */
        REGEX
    }

    private final ModuleEventWatcher moduleEventWatcher;
    private final PatternType patternType;
    private List<BuildingForClass> bfClasses = new ArrayList<BuildingForClass>();

    /**
     * 构造事件观察者构造器(通配符匹配模式)
     *
     * @param moduleEventWatcher 模块事件观察者
     */
    public EventWatchBuilder(final ModuleEventWatcher moduleEventWatcher) {
        this(moduleEventWatcher, WILDCARD);
    }

    /**
     * 构造事件观察者构造器
     *
     * @param moduleEventWatcher 模块事件观察者
     * @param patternType        模版匹配模式
     * @since {@code sandbox-api:1.1.2}
     */
    public EventWatchBuilder(final ModuleEventWatcher moduleEventWatcher,
                             final PatternType patternType) {
        this.moduleEventWatcher = moduleEventWatcher;
        this.patternType = patternType;
    }

    /**
     * 模式匹配
     *
     * @param string      目标字符串
     * @param pattern     模式字符串
     * @param patternType 匹配模式
     * @return TRUE:匹配成功 / FALSE:匹配失败
     */
    private static boolean patternMatching(final String string,
                                           final String pattern,
                                           final PatternType patternType) {
        switch (patternType) {
            case WILDCARD:
                return GaStringUtils.matching(string, pattern);
            case REGEX:
                return string.matches(pattern);
            default:
                return false;
        }
    }

    /**
     * 将字符串数组转换为正则表达式字符串数组
     *
     * @param stringArray 目标字符串数组
     * @return 正则表达式字符串数组
     */
    private static String[] toRegexQuoteArray(final String[] stringArray) {
        if (null == stringArray) {
            return null;
        }
        final String[] regexQuoteArray = new String[stringArray.length];
        for (int index = 0; index < stringArray.length; index++) {
            regexQuoteArray[index] = quote(stringArray[index]);
        }
        return regexQuoteArray;
    }


    /**
     * 匹配任意类
     * <p>
     * 等同于{@code onClass("*")}
     * </p>
     *
     * @return IBuildingForClass
     */
    public IBuildingForClass onAnyClass() {
        switch (patternType) {
            case REGEX:
                return onClass(".*");
            case WILDCARD:
            default:
                return onClass("*");
        }
    }

    /**
     * 匹配指定类
     * <p>
     * 等同于{@code onClass(clazz.getCanonicalName())}
     * </p>
     *
     * @param clazz 指定Class，这里的Class可以忽略ClassLoader的差异。
     *              这里主要取Class的类名
     * @return IBuildingForClass
     */
    public IBuildingForClass onClass(final Class<?> clazz) {
        switch (patternType) {
            case REGEX: {
                return onClass(quote(getJavaClassName(clazz)));
            }
            case WILDCARD:
            default:
                return onClass(getJavaClassName(clazz));
        }

    }

    /**
     * 模版匹配类名称(包含包名)
     * <p>
     * 例子：
     * <ul>
     * <li>"com.alibaba.*"</li>
     * <li>"java.util.ArrayList"</li>
     * </ul>
     *
     * @param pattern 类名匹配模版
     * @return IBuildingForClass
     */
    public IBuildingForClass onClass(final String pattern) {
        return add(bfClasses, new BuildingForClass(pattern));
    }

    /**
     * 类匹配器实现
     */
    private class BuildingForClass implements IBuildingForClass {

        private final String pattern;
        private int withAccess = 0;
        private boolean isIncludeSubClasses = false;
        private boolean isIncludeBootstrap = false;
        private final PatternGroupList hasInterfaceTypes = new PatternGroupList();
        private final PatternGroupList hasAnnotationTypes = new PatternGroupList();
        private final List<BuildingForBehavior> bfBehaviors = new ArrayList<BuildingForBehavior>();

        /**
         * 构造类构建器
         *
         * @param pattern 类名匹配模版
         */
        BuildingForClass(final String pattern) {
            this.pattern = pattern;
        }

        @Override
        public IBuildingForClass includeBootstrap() {
            this.isIncludeBootstrap = true;
            return this;
        }

        @Override
        public IBuildingForClass isIncludeBootstrap(boolean isIncludeBootstrap) {
            if (isIncludeBootstrap) {
                includeBootstrap();
            }
            return this;
        }

        @Override
        public IBuildingForClass includeSubClasses() {
            this.isIncludeSubClasses = true;
            return this;
        }

        @Override
        public IBuildingForClass isIncludeSubClasses(boolean isIncludeSubClasses) {
            if (isIncludeSubClasses) {
                includeSubClasses();
            }
            return this;
        }

        @Override
        public IBuildingForClass withAccess(final int access) {
            withAccess |= access;
            return this;
        }

        @Override
        public IBuildingForClass hasInterfaceTypes(final String... patterns) {
            hasInterfaceTypes.add(patterns);
            return this;
        }

        @Override
        public IBuildingForClass hasAnnotationTypes(final String... patterns) {
            hasAnnotationTypes.add(patterns);
            return this;
        }

        @Override
        public IBuildingForClass hasInterfaceTypes(final Class<?>... classes) {
            switch (patternType) {
                case REGEX:
                    return hasInterfaceTypes(toRegexQuoteArray(getJavaClassNameArray(classes)));
                case WILDCARD:
                default:
                    return hasInterfaceTypes(getJavaClassNameArray(classes));
            }
        }

        @Override
        public IBuildingForClass hasAnnotationTypes(final Class<?>... classes) {
            switch (patternType) {
                case REGEX:
                    return hasAnnotationTypes(toRegexQuoteArray(getJavaClassNameArray(classes)));
                case WILDCARD:
                default:
                    return hasAnnotationTypes(getJavaClassNameArray(classes));
            }
        }

        @Override
        public IBuildingForBehavior onBehavior(final String pattern) {
            return add(bfBehaviors, new BuildingForBehavior(this, pattern));
        }

        @Override
        public IBuildingForBehavior onAnyBehavior() {
            switch (patternType) {
                case REGEX:
                    return onBehavior(".*");
                case WILDCARD:
                default:
                    return onBehavior("*");
            }
        }

    }

    /**
     * 行为匹配器实现
     */
    private class BuildingForBehavior implements IBuildingForBehavior {

        private final BuildingForClass bfClass;
        private final String pattern;
        private int withAccess = 0;
        private final PatternGroupList withParameterTypes = new PatternGroupList();
        private final PatternGroupList hasExceptionTypes = new PatternGroupList();
        private final PatternGroupList hasAnnotationTypes = new PatternGroupList();

        BuildingForBehavior(final BuildingForClass bfClass,
                            final String pattern) {
            this.bfClass = bfClass;
            this.pattern = pattern;
        }

        @Override
        public IBuildingForBehavior withAccess(final int access) {
            withAccess |= access;
            return this;
        }

        @Override
        public IBuildingForBehavior withEmptyParameterTypes() {
            withParameterTypes.add();
            return this;
        }

        @Override
        public IBuildingForBehavior withParameterTypes(final String... patterns) {
            withParameterTypes.add(patterns);
            return this;
        }

        @Override
        public IBuildingForBehavior withParameterTypes(final Class<?>... classes) {
            switch (patternType) {
                case REGEX:
                    return withParameterTypes(toRegexQuoteArray(getJavaClassNameArray(classes)));
                case WILDCARD:
                default:
                    return withParameterTypes(getJavaClassNameArray(classes));
            }
        }

        @Override
        public IBuildingForBehavior hasExceptionTypes(final String... patterns) {
            hasExceptionTypes.add(patterns);
            return this;
        }

        @Override
        public IBuildingForBehavior hasExceptionTypes(final Class<?>... classes) {
            switch (patternType) {
                case REGEX:
                    return hasExceptionTypes(toRegexQuoteArray(getJavaClassNameArray(classes)));
                case WILDCARD:
                default:
                    return hasExceptionTypes(getJavaClassNameArray(classes));
            }
        }

        @Override
        public IBuildingForBehavior hasAnnotationTypes(final String... patterns) {
            hasAnnotationTypes.add(patterns);
            return this;
        }

        @Override
        public IBuildingForBehavior hasAnnotationTypes(final Class<?>... classes) {
            switch (patternType) {
                case REGEX:
                    return hasAnnotationTypes(toRegexQuoteArray(getJavaClassNameArray(classes)));
                case WILDCARD:
                default:
                    return hasAnnotationTypes(getJavaClassNameArray(classes));
            }
        }

        @Override
        public IBuildingForBehavior onBehavior(final String pattern) {
            return bfClass.onBehavior(pattern);
        }

        @Override
        public IBuildingForClass onClass(final String pattern) {
            return EventWatchBuilder.this.onClass(pattern);
        }

        @Override
        public IBuildingForClass onClass(final Class<?> clazz) {
            return EventWatchBuilder.this.onClass(clazz);
        }

        @Override
        public IBuildingForClass onAnyClass() {
            return EventWatchBuilder.this.onAnyClass();
        }

        @Override
        public IBuildingForWatching onWatching() {
            return new BuildingForWatching();
        }

        @Override
        public EventWatcher onWatch(final AdviceListener adviceListener, Event.Type... eventTypeArray) {
            if (eventTypeArray == null
                    || eventTypeArray.length == 0) {
                return build(new AdviceAdapterListener(adviceListener), null, BEFORE, RETURN, THROWS, IMMEDIATELY_RETURN, IMMEDIATELY_THROWS);
            }
            return build(new AdviceAdapterListener(adviceListener), null, eventTypeArray);
        }

        @Override
        public EventWatcher onWatch(EventListener eventListener, Event.Type... eventTypeArray) {
            return build(eventListener, null, eventTypeArray);
        }

    }

    private class BuildingForWatching implements IBuildingForWatching {

        private final Set<Event.Type> eventTypeSet = new HashSet<Event.Type>();
        private final List<Progress> progresses = new ArrayList<Progress>();

        @Override
        public IBuildingForWatching withProgress(Progress progress) {
            if (null != progress) {
                progresses.add(progress);
            }
            return this;
        }

        @Override
        public IBuildingForWatching withCall() {
            eventTypeSet.add(CALL_BEFORE);
            eventTypeSet.add(CALL_RETURN);
            eventTypeSet.add(CALL_THROWS);
            return this;
        }

        @Override
        public IBuildingForWatching withLine() {
            eventTypeSet.add(LINE);
            return this;
        }

        @Override
        public EventWatcher onWatch(AdviceListener adviceListener) {
            eventTypeSet.add(BEFORE);
            eventTypeSet.add(RETURN);
            eventTypeSet.add(THROWS);
            eventTypeSet.add(IMMEDIATELY_RETURN);
            eventTypeSet.add(IMMEDIATELY_THROWS);
            return build(
                    new AdviceAdapterListener(adviceListener),
                    toProgressGroup(progresses),
                    eventTypeSet.toArray(new Event.Type[0])
            );
        }

        @Override
        public EventWatcher onWatch(EventListener eventListener, Event.Type... eventTypeArray) {
            return build(eventListener, toProgressGroup(progresses), eventTypeArray);
        }

    }

    private EventWatchCondition toEventWatchCondition() {
        final List<Filter> filters = new ArrayList<Filter>();
        for (final BuildingForClass bfClass : bfClasses) {
            final Filter filter = new Filter() {
                @Override
                public boolean doClassFilter(final int access,
                                             final String javaClassName,
                                             final String superClassTypeJavaClassName,
                                             final String[] interfaceTypeJavaClassNameArray,
                                             final String[] annotationTypeJavaClassNameArray) {
                    return (access & bfClass.withAccess) == bfClass.withAccess
                            && patternMatching(javaClassName, bfClass.pattern, patternType)
                            && bfClass.hasInterfaceTypes.patternHas(interfaceTypeJavaClassNameArray)
                            && bfClass.hasAnnotationTypes.patternHas(annotationTypeJavaClassNameArray);
                }

                @Override
                public boolean doMethodFilter(final int access,
                                              final String javaMethodName,
                                              final String[] parameterTypeJavaClassNameArray,
                                              final String[] throwsTypeJavaClassNameArray,
                                              final String[] annotationTypeJavaClassNameArray) {
                    // nothing to matching
                    if (bfClass.bfBehaviors.isEmpty()) {
                        return false;
                    }

                    // matching any behavior
                    for (final BuildingForBehavior bfBehavior : bfClass.bfBehaviors) {
                        if ((access & bfBehavior.withAccess) == bfBehavior.withAccess
                                && patternMatching(javaMethodName, bfBehavior.pattern, patternType)
                                && bfBehavior.withParameterTypes.patternWith(parameterTypeJavaClassNameArray)
                                && bfBehavior.hasExceptionTypes.patternHas(throwsTypeJavaClassNameArray)
                                && bfBehavior.hasAnnotationTypes.patternHas(annotationTypeJavaClassNameArray)) {
                            return true;
                        }//if
                    }//for

                    // non matched
                    return false;
                }
            };//filter

            filters.add(makeExtFilter(filter, bfClass));
        }
        return new EventWatchCondition() {
            @Override
            public Filter[] getOrFilterArray() {
                return filters.toArray(new Filter[0]);
            }
        };
    }

    private Filter makeExtFilter(final Filter filter,
                                 final BuildingForClass bfClass) {
        return ExtFilter.ExtFilterFactory.make(
                filter,
                bfClass.isIncludeSubClasses,
                bfClass.isIncludeBootstrap
        );
    }

    private ProgressGroup toProgressGroup(final List<Progress> progresses) {
        if (progresses.isEmpty()) {
            return null;
        }
        return new ProgressGroup(progresses);
    }

    private EventWatcher build(final EventListener listener,
                               final Progress progress,
                               final Event.Type... eventTypes) {

        final int watchId = moduleEventWatcher.watch(
                toEventWatchCondition(),
                listener,
                progress,
                eventTypes
        );

        return new EventWatcher() {

            final List<Progress> progresses = new ArrayList<Progress>();

            @Override
            public int getWatchId() {
                return watchId;
            }

            @Override
            public IBuildingForUnWatching withProgress(Progress progress) {
                if (null != progress) {
                    progresses.add(progress);
                }
                return this;
            }

            @Override
            public void onUnWatched() {
                moduleEventWatcher.delete(watchId, toProgressGroup(progresses));
            }

        };
    }

    /**
     * 观察进度组
     */
    private static class ProgressGroup implements Progress {

        private final List<Progress> progresses;

        ProgressGroup(List<Progress> progresses) {
            this.progresses = progresses;
        }

        @Override
        public void begin(int total) {
            for (final Progress progress : progresses) {
                progress.begin(total);
            }
        }

        @Override
        public void progressOnSuccess(Class clazz, int index) {
            for (final Progress progress : progresses) {
                progress.progressOnSuccess(clazz, index);
            }
        }

        @Override
        public void progressOnFailed(Class clazz, int index, Throwable cause) {
            for (final Progress progress : progresses) {
                progress.progressOnFailed(clazz, index, cause);
            }
        }

        @Override
        public void finish(int cCnt, int mCnt) {
            for (final Progress progress : progresses) {
                progress.finish(cCnt, mCnt);
            }
        }

    }

    /**
     * 模式匹配组列表
     */
    private class PatternGroupList {

        final List<Group> groups = new ArrayList<Group>();

        /*
         * 添加模式匹配组
         */
        void add(String... patternArray) {
            groups.add(new Group(patternArray));
        }

        /*
         * 模式匹配With
         */
        boolean patternWith(final String[] stringArray) {

            // 如果模式匹配组为空，说明不参与本次匹配
            if (groups.isEmpty()) {
                return true;
            }

            for (final Group group : groups) {
                if (group.matchingWith(stringArray)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * 模式匹配Has
         */
        boolean patternHas(final String[] stringArray) {

            // 如果模式匹配组为空，说明不参与本次匹配
            if (groups.isEmpty()) {
                return true;
            }

            for (final Group group : groups) {
                if (group.matchingHas(stringArray)) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * 模式匹配组
     */
    private class Group {

        final String[] patternArray;

        Group(String[] patternArray) {
            this.patternArray = GaArrayUtils.isEmpty(patternArray)
                    ? new String[0]
                    : patternArray;
        }

        /*
         * stringArray中任意字符串能匹配上匹配模式
         */
        boolean anyMatching(final String[] stringArray,
                            final String pattern) {
            if (GaArrayUtils.isEmpty(stringArray)) {
                return false;
            }
            for (final String string : stringArray) {
                if (patternMatching(string, pattern, patternType)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * 匹配模式组中所有匹配模式都在目标中存在匹配通过的元素
         * 要求匹配组中每一个匹配项都在stringArray中存在匹配的字符串
         */
        boolean matchingHas(final String[] stringArray) {

            for (final String pattern : patternArray) {
                if (anyMatching(stringArray, pattern)) {
                    continue;
                }
                return false;
            }
            return true;
        }

        /*
         * 匹配模式组中所有匹配模式都在目标中对应数组位置存在匹配通过元素
         * 要求字符串数组每一个位对应模式匹配组的每一个模式匹配表达式
         * stringArray[0] matching wildcardArray[0]
         * stringArray[1] matching wildcardArray[1]
         * stringArray[2] matching wildcardArray[2]
         *     ...
         * stringArray[n] matching wildcardArray[n]
         */
        boolean matchingWith(final String[] stringArray) {

            // 长度不一样就不用不配了
            int length;
            if ((length = GaArrayUtils.getLength(stringArray)) != GaArrayUtils.getLength(patternArray)) {
                return false;
            }
            // 长度相同则逐个位置比较，只要有一个位置不符，则判定不通过
            for (int index = 0; index < length; index++) {
                if (!patternMatching(stringArray[index], patternArray[index], patternType)) {
                    return false;
                }
            }
            // 所有位置匹配通过，判定匹配成功
            return true;
        }

    }

}
