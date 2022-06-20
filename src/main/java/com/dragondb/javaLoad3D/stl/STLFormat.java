package com.dragondb.javaLoad3D.stl;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author aa12aa
 */
public class STLFormat {
    private static final Logger logger = LoggerFactory.getLogger(STLFormat.class);
    private static final int FLOAT_SIZE = 4;
    private static final int UINT16T = 2;
    private static final int FACE_SIZE = 3 * FLOAT_SIZE + 9 * FLOAT_SIZE + UINT16T;


    public static float getBinaryModelSize(File file) {
        STLFile stlFile = STLUtils.parseBinary(file);
        //35.83
        float volume = 0f;
        Vector p1;
        Vector p2;
        Vector p3;

        for (int i = 0; i < stlFile.vertices.length / 9; i++) {
            float sum;
            p1 = new Vector(stlFile.vertices[i * 9], stlFile.vertices[i * 9 + 1], stlFile.vertices[i * 9 + 2]);
            p2 = new Vector(stlFile.vertices[i * 9 + 3], stlFile.vertices[i * 9 + 4], stlFile.vertices[i * 9 + 5]);
            p3 = new Vector(stlFile.vertices[i * 9 + 6], stlFile.vertices[i * 9 + 7], stlFile.vertices[i * 9 + 8]);
            sum = SignedVolumeOfTriangle(p1, p2, p3);
            volume += sum;
        }
        return Math.abs(volume);

    }

    public static float getAscIIModelSize(File file) {
        STLFile stlFile = STLUtils.parseASCII(file);
        //35.83
        float volume = 0f;
        Vector p1;
        Vector p2;
        Vector p3;

        for (int i = 0; i < stlFile.vertices.length / 9; i++) {
            float sum;
            p1 = new Vector(stlFile.vertices[i * 9], stlFile.vertices[i * 9 + 1], stlFile.vertices[i * 9 + 2]);
            p2 = new Vector(stlFile.vertices[i * 9 + 3], stlFile.vertices[i * 9 + 4], stlFile.vertices[i * 9 + 5]);
            p3 = new Vector(stlFile.vertices[i * 9 + 6], stlFile.vertices[i * 9 + 7], stlFile.vertices[i * 9 + 8]);
            sum = SignedVolumeOfTriangle(p1, p2, p3);
            volume += sum;
        }
        return Math.abs(volume);

    }

    public static float SignedVolumeOfTriangle(Vector p1, Vector p2, Vector p3) {
        return p1.Dot(p2.Cross(p3)) / 6.0f;
    }

    public static int readFile(File file) {
        FileInputStream fis = null;
        Reader reader = null;
        BufferedReader br = null;
        BufferedInputStream bis;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            //判断文件大小
            //空的ASCII文件的最小大小是15字节
            long fileSize = file.length();//32506584
            if (fileSize >= Integer.MAX_VALUE) {//2147483647/1024/1024 = 2000M
                logger.info("文件过大");
                return -1;
            }
            int size = (int) fileSize;
            if (fileSize < 15) {
                logger.info("文件类型错误，文件长度{}一共不满足最小字节数", fileSize);
                return -1;
            }
            //二进制文件不以"solid"开头，以防万一，先检测一下
            //先寻找6个字节的text
            byte[] sixBytes = new byte[6];
            int len = 0;
            len = bis.read(sixBytes);
            String s = new String(sixBytes);
            if (s.contains("solid")) {
                String line;
                reader = new InputStreamReader(bis);
                br = new BufferedReader(reader);
                while ((line = br.readLine()) != null) {
                    if (line.contains("endsolid")) {
                        //有必要可以在这里返回ASCII类型
                        return 1;
                    }
                }
            }
            //如果不是ASCII文件，则说明是二进制文件，对字节数组进行判断
            if (fileSize < 84) {
                logger.error("文件类型错误，文件长度{}一共不满足最小字节数", fileSize);
                return -1;
            }

            //对于二进制STL文件来说 0-79是头部，三角面数的字节从第81个字节开始，也就是80
            byte[] fourBytes = new byte[4];
            file = new File(file.getAbsolutePath());
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            byte[] allBytes = new byte[size];
            bis.read(allBytes);
            fourBytes[0] = allBytes[80];
            fourBytes[1] = allBytes[81];
            fourBytes[2] = allBytes[82];
            fourBytes[3] = allBytes[83];
            long nTriangles = convertFourBytesToInt1(fourBytes[0], fourBytes[1], fourBytes[2], fourBytes[3]);
            if (fileSize == (84 + (nTriangles * FACE_SIZE))) {
                //有必要可以在这里返回Binary类型
                return 2;
            }
            return -1;
        } catch (FileNotFoundException e) {
            logger.error("找不到文件");
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            logger.error("读取文件错误");
            e.printStackTrace();
            return -1;
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.info("关闭文件流失败");
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static long convertFourBytesToInt1(byte b1, byte b2, byte b3, byte b4) {
        return (long) (((b4 & 0xFF) << 24) | ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }
}
