package com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.Access;

import static com.alibaba.jvm.sandbox.qatest.core.util.matcher.asserts.AccessAsserter.AccessIsEnum.*;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.junit.Assert.assertEquals;

public class AccessAsserter implements Asserter<Access> {

    public enum AccessIsEnum {

        IS_PUBLIC,
        IS_PRIVATE,
        IS_PROTECTED,
        IS_STATIC,
        IS_FINAL,
        IS_INTERFACE,
        IS_NATIVE,
        IS_ABSTRACT,
        IS_ENUM,
        IS_ANNOTATION
    }

    private final AccessIsEnum[] accessIsEnumArray;

    public AccessAsserter(final AccessIsEnum... accessIsEnumArray) {
        this.accessIsEnumArray = accessIsEnumArray;
    }

    @Override
    public void assertThat(final String message,
                           final Access access) {
        assertEquals(message + ":isPublic", access.isPublic(), contains(accessIsEnumArray, IS_PUBLIC));
        assertEquals(message + ":isPrivate", access.isPrivate(), contains(accessIsEnumArray, IS_PRIVATE));
        assertEquals(message + ":isProtected", access.isProtected(), contains(accessIsEnumArray, IS_PROTECTED));
        assertEquals(message + ":isStatic", access.isStatic(), contains(accessIsEnumArray, IS_STATIC));
        assertEquals(message + ":isFinal", access.isFinal(), contains(accessIsEnumArray, IS_FINAL));
        assertEquals(message + ":isInterface", access.isInterface(), contains(accessIsEnumArray, IS_INTERFACE));
        assertEquals(message + ":isNative", access.isNative(), contains(accessIsEnumArray, IS_NATIVE));
        assertEquals(message + ":isAbstract", access.isAbstract(), contains(accessIsEnumArray, IS_ABSTRACT));
        assertEquals(message + ":isEnum", access.isEnum(), contains(accessIsEnumArray, IS_ENUM));
        assertEquals(message + ":isAnnotation", access.isAnnotation(), contains(accessIsEnumArray, IS_ANNOTATION));
    }

}
