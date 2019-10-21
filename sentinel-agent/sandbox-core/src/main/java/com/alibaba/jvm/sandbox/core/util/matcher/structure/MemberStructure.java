package com.alibaba.jvm.sandbox.core.util.matcher.structure;

/**
 * 成员结构
 *
 * @author luanjia@taobao.com
 */
public class MemberStructure {

    private final Access access;
    private final String name;
    private final ClassStructure declaringClassStructure;

    public MemberStructure(final Access access,
                           final String name,
                           final ClassStructure declaringClassStructure) {
        this.access = access;
        this.name = name;
        this.declaringClassStructure = declaringClassStructure;
    }

    public String getName() {
        return name;
    }

    public ClassStructure getDeclaringClassStructure() {
        return declaringClassStructure;
    }

    public Access getAccess() {
        return access;
    }
}
