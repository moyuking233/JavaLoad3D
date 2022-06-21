package com.dragondb.javaLoad3D.obj;

import com.dragondb.javaLoad3D.utils.ClasspathFileGetter;
import com.mokiat.data.front.parser.IOBJParser;
import com.mokiat.data.front.parser.OBJModel;
import com.mokiat.data.front.parser.OBJParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

public class ObjDemo {
    public static void main(String[] args) {
        File file = ClasspathFileGetter.getter("PM3D_Helix3D1.obj");
        try (InputStream in = new FileInputStream(file)) {
            final IOBJParser parser = new OBJParser();
            final OBJModel model = parser.parse(in);
            // 算体积就单独算，上面两句就不需要了，如果还需要额外参数可以直接上面自己存
            System.out.println(MessageFormat.format(
                    "OBJ model has {0} vertices, {1} normals, {2} texture coordinates, and {3} objects.",
                    model.getVertices().size(),
                    model.getNormals().size(),
                    model.getTexCoords().size(),
                    model.getObjects().size()));
            System.out.println(ObjUtils.getVolume(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
