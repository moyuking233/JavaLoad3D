package com.dragondb.javaLoad3D.stl;

import com.dragondb.javaLoad3D.stl.STLFile;
import com.dragondb.javaLoad3D.stl.STLFormat;
import com.dragondb.javaLoad3D.stl.STLUtils;
import com.dragondb.javaLoad3D.utils.ClasspathFileGetter;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;

public class StlDemo {
    public static void main(String[] args) {
       new StlDemo().go();
    }
    public void go(){

        File file1 = ClasspathFileGetter.getter("ball.stl");
        File file2 = ClasspathFileGetter.getter("10mm_Cube.stl");
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
