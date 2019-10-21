package com.alibaba.jvm.sandbox.core.util;

import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructure;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructureFactory;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

import static com.alibaba.jvm.sandbox.core.util.SandboxStringUtils.toInternalClassName;

/**
 * ASM工具集
 *
 * @author luanjia@taobao.com
 */
public class AsmUtils {

    /**
     * just the same
     * {@code org.objectweb.asm.ClassWriter#getCommonSuperClass(String, String)}
     */
    public static String getCommonSuperClass(String type1, String type2, ClassLoader loader) {
        return getCommonSuperClassImplByAsm(type1, type2, loader);
    }

    // implements by ASM
    private static String getCommonSuperClassImplByAsm(String type1, String type2, ClassLoader targetClassLoader) {
        InputStream inputStreamOfType1 = null, inputStreamOfType2 = null;
        try {
            //targetClassLoader 为null，说明是BootStrapClassLoader，不能显式引用，故使用系统类加载器间接引用
            if(null == targetClassLoader){
                targetClassLoader = ClassLoader.getSystemClassLoader();
            }
            if(null == targetClassLoader){
                return "java/lang/Object";
            }
            inputStreamOfType1 = targetClassLoader.getResourceAsStream(type1 + ".class");
            if (null == inputStreamOfType1) {
                return "java/lang/Object";
            }
            inputStreamOfType2 = targetClassLoader.getResourceAsStream(type2 + ".class");
            if (null == inputStreamOfType2) {
                return "java/lang/Object";
            }
            final ClassStructure classStructureOfType1 = ClassStructureFactory.createClassStructure(inputStreamOfType1, targetClassLoader);
            final ClassStructure classStructureOfType2 = ClassStructureFactory.createClassStructure(inputStreamOfType2, targetClassLoader);
            if (classStructureOfType2.getFamilyTypeClassStructures().contains(classStructureOfType1)) {
                return type1;
            }
            if (classStructureOfType1.getFamilyTypeClassStructures().contains(classStructureOfType2)) {
                return type2;
            }
            if (classStructureOfType1.getAccess().isInterface()
                    || classStructureOfType2.getAccess().isInterface()) {
                return "java/lang/Object";
            }
            ClassStructure classStructure = classStructureOfType1;
            do {
                classStructure = classStructure.getSuperClassStructure();
                if (null == classStructure) {
                    return "java/lang/Object";
                }
            } while (!classStructureOfType2.getFamilyTypeClassStructures().contains(classStructure));
            return toInternalClassName(classStructure.getJavaClassName());
        } finally {
            IOUtils.closeQuietly(inputStreamOfType1);
            IOUtils.closeQuietly(inputStreamOfType2);
        }
    }

}
