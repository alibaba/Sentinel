package com.alibaba.jvm.sandbox.qatest.core.manager;

import com.alibaba.jvm.sandbox.api.filter.ExtFilter;
import com.alibaba.jvm.sandbox.api.filter.NameRegexFilter;
import com.alibaba.jvm.sandbox.core.manager.CoreLoadedClassDataSource;
import com.alibaba.jvm.sandbox.core.manager.impl.DefaultLoadedClassDataSource;
import com.alibaba.jvm.sandbox.qatest.core.mock.EmptyInstrumentation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

class MockLoadedClassesOnlyInstrumentation extends EmptyInstrumentation {

    final Set<Class<?>> loadedClasses = new LinkedHashSet<Class<?>>();

    void regLoadedClass(Class<?> clazz) {
        loadedClasses.add(clazz);
    }

    @Override
    public Class[] getAllLoadedClasses() {
        return loadedClasses.toArray(new Class<?>[]{});
    }

}

public class CoreLoadedClassDataSourceTestCase {

    public interface Human {
        void methodOfHuman();
    }

    public interface Man extends Human {
        void methodOfMan();
    }

    public interface Woman extends Human {
        void methodOfWoman();
    }

    public interface Worker extends Man, Woman {
        void methodOfWorker();
    }

    private static class InnerWorker implements Worker {

        @Override
        public void methodOfHuman() {

        }

        @Override
        public void methodOfMan() {

        }

        @Override
        public void methodOfWoman() {

        }

        @Override
        public void methodOfWorker() {

        }
    }

    private static final MockLoadedClassesOnlyInstrumentation mockInstrumentation
            = new MockLoadedClassesOnlyInstrumentation();

    @BeforeClass
    public static void initStaticClasses() {
        mockInstrumentation.regLoadedClass(Human.class);
        mockInstrumentation.regLoadedClass(Man.class);
        mockInstrumentation.regLoadedClass(Woman.class);
        mockInstrumentation.regLoadedClass(Worker.class);
        mockInstrumentation.regLoadedClass(InnerWorker.class);

        // 干扰项
        {
            mockInstrumentation.regLoadedClass(String.class);
            mockInstrumentation.regLoadedClass(Integer.class);
            mockInstrumentation.regLoadedClass(int.class);
            mockInstrumentation.regLoadedClass(void.class);
        }

        // init AnonymousInnerClassClasses
        {
            final Woman anonymousInnerWoman = new Woman() {
                @Override
                public void methodOfWoman() {

                }

                @Override
                public void methodOfHuman() {

                }

                {
                    mockInstrumentation.regLoadedClass(getClass());
                }
            };
            final Worker anonymousInnerWorker = new Worker() {
                @Override
                public void methodOfWorker() {

                }

                @Override
                public void methodOfWoman() {

                }

                @Override
                public void methodOfMan() {

                }

                @Override
                public void methodOfHuman() {

                }

                {
                    mockInstrumentation.regLoadedClass(getClass());
                }
            };
        }

    }

    private final CoreLoadedClassDataSource coreLoadedClassDataSource
            = new DefaultLoadedClassDataSource(mockInstrumentation, false);


    @Test
    public void test$$CoreLoadedClassDataSource$$list() {
        final Set<Class<?>> foundClasses = coreLoadedClassDataSource.list();
        Assert.assertEquals(11, foundClasses.size());

        final Set<String> uniqueClassNameSet = new LinkedHashSet<String>();
        for(final Class<?> foundClass : foundClasses) {
            uniqueClassNameSet.add(foundClass.getName());
        }

        Assert.assertTrue(uniqueClassNameSet.contains(String.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Integer.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(int.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(void.class.getName()));

        Assert.assertTrue(uniqueClassNameSet.contains(Human.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Man.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Woman.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Worker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(InnerWorker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$1"));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$2"));

    }

    @Test
    public void test$$CoreLoadedClassDataSource$$findSingleByRegex() {
        final Set<Class<?>> foundClasses = coreLoadedClassDataSource.find(new NameRegexFilter(
                "com\\.alibaba\\.jvm\\.sandbox\\.qatest\\.core\\.manager\\.CoreLoadedClassDataSourceTestCase\\$Human",
                ".*"
        ));
        Assert.assertEquals(1, foundClasses.size());
        Assert.assertEquals(
                Human.class.getName(),
                foundClasses.iterator().next().getName()
        );
    }

    @Test
    public void test$$CoreLoadedClassDataSource$$findMultiByRegex() {
        final Set<Class<?>> foundClasses = coreLoadedClassDataSource.find(new NameRegexFilter(
                "com\\.alibaba\\.jvm\\.sandbox\\.qatest\\.core\\.manager\\.CoreLoadedClassDataSourceTestCase\\$.*[M|m]an",
                ".*"
        ));
        Assert.assertEquals(3, foundClasses.size());
        Assert.assertTrue(foundClasses.contains(Human.class));
        Assert.assertTrue(foundClasses.contains(Man.class));
        Assert.assertTrue(foundClasses.contains(Woman.class));
    }


    static class NameRegexWithSubClassesExtFilter extends NameRegexFilter implements ExtFilter {

        /**
         * 构造名称正则表达式过滤器
         *
         * @param javaNameRegex   类名正则表达式
         * @param javaMethodRegex 方法名正则表达式
         */
        public NameRegexWithSubClassesExtFilter(String javaNameRegex, String javaMethodRegex) {
            super(javaNameRegex, javaMethodRegex);
        }

        @Override
        public boolean isIncludeSubClasses() {
            return true;
        }

        @Override
        public boolean isIncludeBootstrap() {
            return false;
        }

    }

    @Test
    public void test$$CoreLoadedClassDataSource$$findSubClasses$$Human() {
        final Set<Class<?>> foundClasses = coreLoadedClassDataSource.find(
                new NameRegexWithSubClassesExtFilter(
                        "com\\.alibaba\\.jvm\\.sandbox\\.qatest\\.core\\.manager\\.CoreLoadedClassDataSourceTestCase\\$Human",
                        ".*"
                )
        );
        Assert.assertEquals(7, foundClasses.size());

        final Set<String> uniqueClassNameSet = new LinkedHashSet<String>();
        for(final Class<?> foundClass : foundClasses) {
            uniqueClassNameSet.add(foundClass.getName());
        }

        Assert.assertTrue(uniqueClassNameSet.contains(Human.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Man.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Woman.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Worker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(InnerWorker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$1"));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$2"));

    }

    @Test
    public void test$$CoreLoadedClassDataSource$$findSubClasses$$Woman() {
        final Set<Class<?>> foundClasses = coreLoadedClassDataSource.find(
                new NameRegexWithSubClassesExtFilter(
                        "com\\.alibaba\\.jvm\\.sandbox\\.qatest\\.core\\.manager\\.CoreLoadedClassDataSourceTestCase\\$Woman",
                        ".*"
                )
        );
        Assert.assertEquals(5, foundClasses.size());

        final Set<String> uniqueClassNameSet = new LinkedHashSet<String>();
        for(final Class<?> foundClass : foundClasses) {
            uniqueClassNameSet.add(foundClass.getName());
        }

        Assert.assertTrue(uniqueClassNameSet.contains(Woman.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Worker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(InnerWorker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$1"));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$2"));

    }

    @Test
    public void test$$CoreLoadedClassDataSource$$findSubClasses$$Man() {
        final Set<Class<?>> foundClasses = coreLoadedClassDataSource.find(
                new NameRegexWithSubClassesExtFilter(
                        "com\\.alibaba\\.jvm\\.sandbox\\.qatest\\.core\\.manager\\.CoreLoadedClassDataSourceTestCase\\$Man",
                        ".*"
                )
        );
        Assert.assertEquals(4, foundClasses.size());

        final Set<String> uniqueClassNameSet = new LinkedHashSet<String>();
        for(final Class<?> foundClass : foundClasses) {
            uniqueClassNameSet.add(foundClass.getName());
        }

        Assert.assertTrue(uniqueClassNameSet.contains(Man.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(Worker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains(InnerWorker.class.getName()));
        Assert.assertTrue(uniqueClassNameSet.contains("com.alibaba.jvm.sandbox.qatest.core.manager.CoreLoadedClassDataSourceTestCase$2"));

    }

}
