package com.alibaba.csp.sentinel.demo.file.rule;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * @author dq
 * @date 2019-03-30
 * @desc 描述
 */
public class Test {

    public static void main(String[] args) throws Exception {
        String jarPath ="/Users/qing.ding/myproject/Sentinel/sentinel-demo/sentinel-demo-dynamic-file-rule/target/sentinel-demo-dynamic-file-rule-1.5.1-SNAPSHOT.jar";
        JarFile jf = new JarFile(jarPath);
//        JarFile jf = new JarFile("/Users/qing.ding/project/starwar-lottery/starwar-lottery-web/target/starwar-lottery-web.jar");

        Test test = new Test();
        System.out.println(test.getJarPath());

        String path = test.getPath();
        System.out.println("path:"+path);
//        System.out.println("FilePathInJar:"+ test.getFilePathInJar(jarPath,path));
        Enumeration enu = jf.entries();
        JarEntry entry =jf.getJarEntry("FlowRule.json");
        jf.getInputStream(entry);






        System.out.println( entry.getTime());

        while (enu.hasMoreElements()) {
            JarEntry element = (JarEntry) enu.nextElement();
            String name = element.getName();
            Long size = element.getSize();
            Long time = element.getTime();
            Long compressedSize = element.getCompressedSize();

            System.out.print(name+"/t");
            System.out.print(size+"/t");
            System.out.print(compressedSize+"/t");
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)));
        }
    }

    private String getPath() throws Exception{
        ClassLoader classLoader = getClass().getClassLoader();
        return URLDecoder.decode(classLoader.getResource("FlowRule.json").getFile(), "UTF-8");
    }

    private String getJarPath() throws Exception{
        return getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    private String getFilePathInJar(String jarPath,String resourcePath){
        int index = jarPath.length() + 7;
        return resourcePath.substring(index);
    }
}
