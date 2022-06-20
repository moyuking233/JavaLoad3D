package com.dragondb.javaLoad3D.stl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注意java byte范围-128~127
 *
 * @author ephuizi@gmail.com
 *
 */
public class STLUtils {
    // final private static Pattern ASCII_PATTERN_FACET =
    // Pattern.compile("facet([\\s\\S]*?)endfacet");
    final private static Pattern ASCII_PATTERN_NORMAL = Pattern
            .compile("normal[\\s]+([\\-+]?[0-9]+\\.?[0-9]*([eE][\\-+]?[0-9]+)?)+[\\s]+([\\-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?)+[\\s]+([\\-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?)+");
    final private static Pattern ASCII_PATTERN_VERTEX = Pattern
            .compile("vertex[\\s]+([\\-+]?[0-9]+\\.?[0-9]*([eE][\\-+]?[0-9]+)?)+[\\s]+([\\-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?)+[\\s]+([\\-+]?[0-9]*\\.?[0-9]+([eE][\\-+]?[0-9]+)?)+");

    /**
     * 判断是否stl格式
     *
     * @param stlPath
     * @return true binary false ascii
     */
    public static boolean isBinary(String stlPath) {
        long expect = 0;// 以binnary方式计算的文件大小;
        int face_size = (32 / 8 * 3) + ((32 / 8 * 3) * 3) + (16 / 8);// 一个三角片大小
        int n_facetNum = 0;// 三角片数量
        RandomAccessFile stl = null;
        try {
            stl = new RandomAccessFile(stlPath, "r");

            stl.seek(80);
            byte[] arr = { 0, 0, 0, 0 };
            stl.read(arr);
            n_facetNum = STLFaceNum(arr);

            expect = 80 + (32 / 8) + (n_facetNum * face_size);
            if (expect == stl.length()) {
                stl.close();
                return true;
            }

            // some binary files will have different size from expected,
            // checking characters lower than ASCII to confirm is binary
            long fileLength = stl.length();
            stl.seek(0);
            for (long index = 0; index < fileLength; index++) {
                if (stl.readByte() < 0) {
                    stl.close();
                    return true;
                }
            }

            stl.close();
            return false;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 用于正数,因为负数存储是补码 第81~84四个字节
     *
     * @param arr
     * @return
     */
    public static int STLFaceNum(byte[] arr) {
        if (arr != null && arr.length == 4) {
            int a = arr[0] & (0xFF);// 防止低位转二进制后是变成负数
            int b = (arr[1] << 8) & (0xFFFF);
            int c = (arr[2] << 16) & (0xFFFFFF);
            int d = (arr[3] << 24) & (0xFFFFFFFF);
            return a + b + c + d;
        }
        return -1;
    }

    /**
     * resolve binary stl file
     *
     * @param stlFile
     * @return
     */
    public static STLFile parseBinary(File stlFile) {

        RandomAccessFile stl = null;
        try {
            stl = new RandomAccessFile(stlFile, "r");

            stl.seek(80);
            byte[] arr = { 0, 0, 0, 0 };
            stl.read(arr);
            int facetNum = STLFaceNum(arr);

            float r = 0, g = 0, b = 0;
            boolean hasColors = false;
            float[] colors = null;
            float defaultR = 0, defaultG = 0, defaultB = 0, alpha = 0;
            // process STL header
            // check for default color in header ("COLOR=rgba" sequence).

            for (int index = 0; index < 80 - 10; index++) {
                stl.seek(index);
                // 6字节("COLOR=")
                if (stl.readInt() == 0x434F4C4F /* COLO */&& (stl.readByte() == 0x52 /* 'R' */)
                        && (stl.readByte() == 0x3D /* '=' */)) {
                    hasColors = true;

                    colors = new float[facetNum * 3 * 3];// 一个面三个点每个点(r,b,g)

                    defaultR = STLUtils.toFloat(stl.readByte()) / 255;// 6
                    defaultG = STLUtils.toFloat(stl.readByte()) / 255;// 7
                    defaultB = STLUtils.toFloat(stl.readByte()) / 255;// 8
                    alpha = STLUtils.toFloat(stl.readByte()) / 255;// 9
                    break;
                }
            }

            int dataOffset = 84;
            int offset = 0;

            float[] vertices = new float[facetNum * 3 * 3];
            float[] normals = new float[facetNum * 3 * 3];// 三角面片法向量的3个分量值数据

            byte temp[] = { 0, 0, 0, 0 };

            int max = 0;// 第一个三角片z轴高度
            boolean isBegin = true;

            stl.seek(dataOffset);
            for (int face = 0; face < facetNum; face++) {
                // 法向量12个字节
                stl.read(temp);
                float normalX = STLUtils.toFloat(temp);// 4
                stl.read(temp);
                float normalY = STLUtils.toFloat(temp);// 4
                stl.read(temp);
                float normalZ = STLUtils.toFloat(temp);// 4

                // 顶点坐标36字节
                for (int i = 1; i <= 3; i++) {
                    stl.read(temp);
                    vertices[offset] = STLUtils.toFloat(temp);
                    stl.read(temp);
                    vertices[offset + 1] = STLUtils.toFloat(temp);
                    stl.read(temp);
                    vertices[offset + 2] = STLUtils.toFloat(temp);
                    if (isBegin) {
                        isBegin = false;
                        max = (int) (vertices[offset + 2]);
                    }

                    normals[offset] = normalX;
                    normals[offset + 1] = normalY;
                    normals[offset + 2] = normalZ;

                    offset += 3;// 增加位移
                }
                // color2字节
                if (hasColors) {

                    int packedColor = STLUtils.toInt(stl.readByte()) | STLUtils.toInt(stl.readByte()) << 8 & 0xFFFF;

                    if ((packedColor & 0x8000) == 0) { // facet has its own
                                                        // unique color

                        r = (packedColor & 0x1F) / 31;
                        g = ((packedColor >> 5) & 0x1F) / 31;
                        b = ((packedColor >> 10) & 0x1F) / 31;
                    } else {

                        r = defaultR;
                        g = defaultG;
                        b = defaultB;
                    }
                } else {
                    // 无颜色 丢弃2字节
                    stl.readByte();
                    stl.readByte();
                }

                // 补充颜色
                if (hasColors) {
                    colors[face * 9 + 0] = r;
                    colors[face * 9 + 1] = g;
                    colors[face * 9 + 2] = b;
                    colors[face * 9 + 3] = r;
                    colors[face * 9 + 4] = g;
                    colors[face * 9 + 5] = b;
                    colors[face * 9 + 6] = r;
                    colors[face * 9 + 7] = g;
                    colors[face * 9 + 8] = b;
                }

            }
            stl.close();
            return new STLFile(max, facetNum, alpha, hasColors, vertices, normals, colors);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static STLFile parseASCII(File stlPath) {
        int facetNum = asciiFacetNum(stlPath);

        RandomAccessFile stl = null;
        try {
            stl = new RandomAccessFile(stlPath, "r");
            float[] vertices = new float[facetNum * 3 * 3];
            float[] normals = new float[facetNum * 3 * 3];// 三角面片法向量的3个分量值数据

            final String FACET_END = "endfacet";
            StringBuffer bf = new StringBuffer();// record one-facet

            int facetIndex = 0;
            String line = null;
            while ((line = stl.readLine()) != null) {
                if (line.contains("endsolid")||line.contains("solid"))
                    continue;
                bf.append(line);
                if (line.contains(FACET_END)) {
                    // one facet
                    String oneFacet = bf.toString();
                    Matcher nMatcher = ASCII_PATTERN_NORMAL.matcher(oneFacet);
                    if (!nMatcher.find()) {
                        continue;
                    }
                    String normal = nMatcher.group();

                    Matcher mV = ASCII_PATTERN_VERTEX.matcher(oneFacet);
                    if (!mV.find()) {
                        continue;
                    }
                    String v1 = mV.group();// 第一个顶点
                    if (!mV.find()) {
                        continue;
                    }
                    String v2 = mV.group();// 第二个顶点
                    if (!mV.find()) {
                        continue;
                    }
                    String v3 = mV.group();// 第三个顶点

                    // 解析法向量
                    String GAP = " ";

                    int nfIndex = facetIndex * 9;
                    String[] n_f_arr = normal.split(GAP);
                    normals[nfIndex + 6] = normals[nfIndex + 3] = normals[nfIndex] = Float.parseFloat(n_f_arr[1]);
                    normals[nfIndex + 1 + 6] = normals[nfIndex + 1 + 3] = normals[nfIndex + 1] = Float
                            .parseFloat(n_f_arr[2]);
                    normals[nfIndex + 2 + 6] = normals[nfIndex + 2 + 3] = normals[nfIndex + 2] = Float
                            .parseFloat(n_f_arr[3]);
                    // 解析顶点
                    String[] v1_f_arr = v1.split(GAP);
                    vertices[nfIndex + 0] = Float.parseFloat(v1_f_arr[1]);// x
                    vertices[nfIndex + 1] = Float.parseFloat(v1_f_arr[2]);// y
                    vertices[nfIndex + 2] = Float.parseFloat(v1_f_arr[3]);// z

                    String[] v2_f_arr = v2.split(GAP);
                    vertices[nfIndex + 3] = Float.parseFloat(v2_f_arr[1]);
                    vertices[nfIndex + 4] = Float.parseFloat(v2_f_arr[2]);
                    vertices[nfIndex + 5] = Float.parseFloat(v2_f_arr[3]);

                    String[] v3_f_arr = v3.split(GAP);
                    vertices[nfIndex + 6] = Float.parseFloat(v3_f_arr[1]);
                    vertices[nfIndex + 7] = Float.parseFloat(v3_f_arr[2]);
                    vertices[nfIndex + 8] = Float.parseFloat(v3_f_arr[3]);

                    // set bf count=0
                    facetIndex++;
                    bf.setLength(0);
                }
            }

            stl.close();
            int max = (int) vertices[2];
            return new STLFile(max, facetNum, 0, false, vertices, normals, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算ascii-stl-file三角片数量
     *
     * @param stlPath
     * @return
     */
    public static final int asciiFacetNum(File stlPath) {

        RandomAccessFile stl = null;
        int facetNum = 0;
        try {
            stl = new RandomAccessFile(stlPath, "r");

            int lineNum = 0;

            int c = 0;
            while (c != -1) {
                switch (c = stl.read()) {
                case -1:
                case '\n':
                    lineNum++;
                    break;
                case '\r':
                    stl.read();// to skip '\n'
                    lineNum++;
                    break;
                default:
                    break;
                }
            }

            facetNum = lineNum / 7;
            stl.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return facetNum;
    }

    /**
     * -1 原码1000,0001 反码1111,1110 补码1111,1111 所以无符号值 255
     *
     * @param b
     * @return
     */
    public static int toInt(byte b) {
        return (int) (b & 0xFF);
    }

    /**
     * -1 原码1000,0001 反码1111,1110 补码1111,1111 所以无符号值 255 带符号位
     *
     * @param b
     * @return
     */
    public static float toFloat(byte b) {
        return (float) (b & 0xFF);
    }

    /**
     * 字节转换为浮点
     *
     * @param b
     *            字节（至少4个字节）
     * @return
     */
    public static float toFloat(byte[] b) {
        int l;
        l = b[0];
        l &= 0xff;
        l |= ((int) b[1] << 8);
        l &= 0xffff;
        l |= ((int) b[2] << 16);
        l &= 0xffffff;
        l |= ((int) b[3] << 24);
        return Float.intBitsToFloat(l);
    }

    /**
     * 浮点转换为字节
     *
     * @param f
     * @return
     */
    public static byte[] toByteArr(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }
}
