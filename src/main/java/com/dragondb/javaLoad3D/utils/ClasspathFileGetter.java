package com.dragondb.javaLoad3D.utils;

import java.io.File;

public class ClasspathFileGetter {
    public static File getter(String path){
        return new ClasspathFileGetter().go(path);
    }
    public File go(String path){
        ClassLoader classLoader = getClass().getClassLoader();
        /**
         getResource()方法会去classpath下找这个文件，获取到url resource, 得到这个资源后，调用url.getFile获取到 文件 的绝对路径
         url.getFile() 得到这个文件的绝对路径
         */
        return new File(classLoader.getResource(path).getFile());
    }
}
