package com.dragondb.javaLoad3D.obj;

import com.dragondb.javaLoad3D.stl.Vector;
import com.mokiat.data.front.error.WFException;
import com.mokiat.data.front.parser.IOBJParser;
import com.mokiat.data.front.parser.OBJDataReference;
import com.mokiat.data.front.parser.OBJFace;
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
    public static double getVolume(File file) {
        double volume = 0f;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            final IOBJParser parser = new OBJParser();
            final OBJModel model = parser.parse(in);

            List<OBJVertex> vertices = model.getVertices();
            List<OBJFace> faces = model.getObjects().get(0).getMeshes().get(0).getFaces();
            int size = faces.size();
            for (int i = 0; i < size ; i++) {
                List<OBJDataReference> face = faces.get(i).getReferences();
                double sum = 0f;
                if (face.size()==3){
                   sum = SignedVolumeOfTriangle(vertices.get(face.get(0).vertexIndex), vertices.get(face.get(1).vertexIndex), vertices.get(face.get(2).vertexIndex));
                }else if (face.size() == 4){
                    sum += SignedVolumeOfTriangle(vertices.get(face.get(0).vertexIndex), vertices.get(face.get(1).vertexIndex), vertices.get(face.get(2).vertexIndex));
                    sum += SignedVolumeOfTriangle(vertices.get(face.get(1).vertexIndex), vertices.get(face.get(2).vertexIndex), vertices.get(face.get(3).vertexIndex));
                }
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

    double triangle_volume(float p1X, float p1Y, float p1Z,
                           float p2X, float p2Y, float p2Z,
                           float p3X, float p3Y, float p3Z)
    {
        double v321 = p3X*p2Y*p1Z;
        double v231 = p2X*p3Y*p1Z;
        double v312 = p3X*p1Y*p2Z;
        double v132 = p1X*p3Y*p2Z;
        double v213 = p2X*p1Y*p3Z;
        double v123 = p1X*p2Y*p3Z;
        return (double)(1.0/6.0)*(-v321 + v231 + v312 - v132 - v213 + v123);
    }


    public static double SignedVolumeOfTriangle(OBJVertex p1, OBJVertex p2, OBJVertex p3) {
        double v321 = p3.x*p2.y*p1.z;
        double v231 = p2.x*p3.y*p1.z;
        double v312 = p3.x*p1.y*p2.z;
        double v132 = p1.x*p3.y*p2.z;
        double v213 = p2.x*p1.y*p3.z;
        double v123 = p1.x*p2.y*p3.z;
        return (1.0/6.0)*(-v321 + v231 + v312 - v132 - v213 + v123);
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
