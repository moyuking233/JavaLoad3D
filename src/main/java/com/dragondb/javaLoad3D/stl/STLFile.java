package com.dragondb.javaLoad3D.stl;

/**
 *
 * @author ephuizi@gmail.com
 *
 */
public class STLFile {
    int max;// 第一个三角片的z高度
    int facetNum;// 三角片数量
    float alpha;
    boolean hasColors = false;
    public float[] vertices;// 点 length=[faces * 3 * 3]
    float[] normals;// 三角面片法向量的3个分量值数据length=[faces * 3 * 3]
    float[] colors = null;// 点[r,g,b]

    public STLFile(int max, int facetNum, float alpha, boolean hasColors, float[] vertices, float[] normals,
                   float[] colors) {
        this.max = max;
        this.facetNum = facetNum;
        this.alpha = alpha;
        this.hasColors = hasColors;
        this.vertices = vertices;
        this.normals = normals;
        this.colors = colors;
    }

}
