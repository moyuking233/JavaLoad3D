package com.dragondb.javaLoad3D.stl;

import com.dragondb.javaLoad3D.stl.STLFile;
import com.dragondb.javaLoad3D.stl.STLFormat;
import com.dragondb.javaLoad3D.stl.STLUtils;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;

public class StlDemo {
    public static void main(String[] args) {
       new StlDemo().go();
    }
    public void go(){
        ClassLoader classLoader = getClass().getClassLoader();
        /**
         getResource()方法会去classpath下找这个文件，获取到url resource, 得到这个资源后，调用url.getFile获取到 文件 的绝对路径
         url.getFile() 得到这个文件的绝对路径
         */
        File file1 = new File(classLoader.getResource("ball.stl").getFile());
        File file2 = new File(classLoader.getResource("10mm_Cube.stl").getFile());
        int type = STLFormat.readFile(file1);
        float modelSize = 0f;
        //判断类型 计算体积
        if (type == 1) {
            modelSize = STLFormat.getAscIIModelSize(file1);
        } else {
            modelSize = STLFormat.getBinaryModelSize(file2);
        }
        System.out.println(modelSize);



        type = STLFormat.readFile(file2);
        //判断类型 计算体积
        if (type == 1) {
            modelSize = STLFormat.getAscIIModelSize(file1);
        } else {
            modelSize = STLFormat.getBinaryModelSize(file2);
        }
        System.out.println(modelSize);
    }

}
