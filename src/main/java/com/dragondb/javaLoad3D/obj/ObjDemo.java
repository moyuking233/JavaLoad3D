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
        File file = ClasspathFileGetter.getter("Dog.obj");
        // Open a stream to your OBJ resource
        try (InputStream in = new FileInputStream(file)) {
            // Create an OBJParser and parse the resource
            final IOBJParser parser = new OBJParser();
            final OBJModel model = parser.parse(in);
            // Use the model representation to get some basic info
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
