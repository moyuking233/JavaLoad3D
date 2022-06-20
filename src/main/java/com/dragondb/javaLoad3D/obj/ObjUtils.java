package com.dragondb.javaLoad3D.obj;

import com.dragondb.javaLoad3D.stl.Vector;
import com.mokiat.data.front.error.WFException;
import com.mokiat.data.front.parser.IOBJParser;
import com.mokiat.data.front.parser.OBJModel;
import com.mokiat.data.front.parser.OBJParser;
import com.mokiat.data.front.parser.OBJVertex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.dragondb.javaLoad3D.stl.STLFormat.SignedVolumeOfTriangle;

public class ObjUtils {
    public static float getVolume(File file) {
        float volume = 0f;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            final IOBJParser parser = new OBJParser();
            final OBJModel model = parser.parse(in);
            //35.83
            Vector p1;
            Vector p2;
            Vector p3;
            List<OBJVertex> vertices = model.getVertices();
            for (int i = 0; i < vertices.size() / 3 ; i++) {
                float sum;
                sum = SignedVolumeOfTriangle(vertices.get(i*3), vertices.get(i*3+1), vertices.get(i*3+2));
                volume += sum;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (WFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Math.abs(volume);
    }

    public static float SignedVolumeOfTriangle(OBJVertex p1, OBJVertex p2, OBJVertex p3) {
        return Dot(p1.x,p1.y,p1.z,Cross(p2.x,p2.y,p3.z,p3)) / 6.0f;
    }

    public static float Dot(float x,float y,float z,OBJVertex a)
    {
        return x * a.x + y * a.y + z * a.z;
    }

    public static OBJVertex Cross(float x,float y,float z,OBJVertex a)
    {
        return new OBJVertex(
                y * a.z - z * a.y,
                z * a.x - x * a.z,
                x * a.y - y * a.x
        );
    }
}
