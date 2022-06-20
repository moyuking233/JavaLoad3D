[toc]

#  Java代码加载3D，参照three.js库

## Obj文件
### 文件构成

首先查阅网站

[wiki]: https://en.wikipedia.org/wiki/Wavefront_.obj_file



## Stl文件
### 文件构成
首先查阅网站

[wiki]: https://en.wikipedia.org/wiki/STL_(file_format)

可以得知，STL文件有两种格式，分别是ASCII格式和二进制格式

#### ASCII格式

##### 文件名

```ASCII
solid name
```
name是可选的（即便name被省略了，那么solid后面仍然需要带空格），有时用来存元数据。

##### 三角形

3D模型本质是多个三角面，本文件以任意数量的三角形来存储。每个n或者v都是一个有符号的指数形式表示的浮点数，例如2.648000e-002

```ASCII
facet normal ni nj nk
    outer loop
        vertex v1x v1y v1z
        vertex v2x v2y v2z
        vertex v3x v3y v3z
    endloop
endfacet
```

##### 结尾

```ASCII
endsolid name
```

##### 参考文件

本项目示例中的**ball.stl**文件，一旦打开就能看到类似的数据结构

```ASCII
solid object0 
...
facet normal -0.000601508 0.999698 -0.0245501 
outer loop
vertex 0.000499725 50.0005 0.000499725 
vertex 0.000499725 49.9402 -2.4529 
vertex -0.1199 49.9402 -2.44995 
endloop
endfacet
...
endsolid object0 
```

#### 二进制格式

因为 ASCII STL 文件可能非常大，所以存在 STL 的二进制版本。（注，一旦涉及到二进制文件的解析就要开始数字节了……）

##### 文件头

不应该由**solid**的二进制开头，因为这样可能会导致文件解析的时候被识别成ASCII格式（一些软件依靠这个**solid**去区分文件格式）。

80个字节组成的后部后，跟一个一个无符号的小端整数，用来记录三角形面片的数量

```
UINT8[80]    – Header                 -     80 bytes
UINT32       – Number of triangles    -      4 bytes
```

##### 文件体

每个三角形，由十二个32位浮点数描述，3个表示法线，三个表示每个顶点的X/Y/Z坐标，基本和ASCII表示的一致，不过是二进制版本的。在这之后，是2字节的属性字节数，通常为0（并不清楚什么含义，不影响解析）。

```
foreach triangle                      - 50 bytes:
    REAL32[3] – Normal vector             - 12 bytes
    REAL32[3] – Vertex 1                  - 12 bytes
    REAL32[3] – Vertex 2                  - 12 bytes
    REAL32[3] – Vertex 3                  - 12 bytes
    UINT16    – Attribute byte count      -  2 bytes
end
```

##### 示例

[github ]: https://github.com/moyuking233/JavaLoad3D/blob/master/src/main/java/com/dragondb/javaLoad3D/stl/StlDemo.java



# 代码实际使用情况

一般用来给3D模型网站的后台校验用户自定义上传的模型体积，在4C16G10M的服务器上表现一般，无法处理高并发情况。

因此建议在处理业务的时候，将用户的：

上传模型

系统后台解析模型

用户下单

三个业务流程分开。