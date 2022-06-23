---
title: Java代码解析3D模型文件，计算体积，参照three.js库
date: 2019-04-24 15:40:24
tags: 轮子,3D
---
[toc]

#  Java代码解析3D模型文件，计算体积，参照three.js库

## Obj文件
### 文件构成

首先查阅网站

[wiki]( https://en.wikipedia.org/wiki/Wavefront_.obj_file)

格式如下

```ASCII
# "#"号开头是注释行
# v（vertex）数据段: 模型顶点列表
# 顶点位置信息，是xyz三维坐标
# v开头的每一行描述一个顶点，行数等于顶点数。8个顶点所以有8行
v  1.00  -1.00  -1.00
v  1.00  1.00  1.00
......
# vt（vertex texture）数据段：模型顶点的纹理坐标列表
# 顶点的纹理坐标信息，是xy二维坐标
# vt开头的每一行描述一个纹理坐标，行数大于等于顶点数，因为一个模型顶点在贴图的UV坐标系中很可能对应多个顶点/纹理坐标。且坐标值范围是在0~1之间，这个模型中有14行。
# 关于纹理坐标看图，本文不多解释纹理坐标，可参考文献[2]或自行百度
vt  0.74  0.75
vt  0.29  0.55
......
# vn（vertex normal）数据段：顶点法线列表
# 三维法向量，xyz
# vn开头的每一行描述一个法向量，行数大于等于顶点数。 前面介绍了，法线是与面相关的概念，但是现在的面是靠顶点来描述，拿示意图中的点"1"为例，它与参与构成了三个面，所以"顶点1"对应有3条法线
# 可能你已经发现了，在这个立方体模型中，共面顶点的法向量的方向是相同的，也就是说这里面的数据会重复，所以在建模软件导出obj文件时有个优化的选项，勾选后的导出的法线列表数据中就不会有重复项，这里的例子优化后有6条法线*
vn  -1.00 0.00 0.00 
vn  1.00 0.00 0.00
vn  0.00 1.00 0.00
......
# f（face）：模型的三角面列表
# f开头的每一行描述一个面 ，关键的来了，三个点组成一个面，怎样拿到这三个点呢？通过从1开始的索引，去前面的v、vt、vn列表中去取。
# 总结一下就是：每一行定义1个面，1个面包含3个点，1个点具有“顶点/纹理坐标/法线”3个索引值，索引的是前面3个列表的信息。
f  1/1/1  2/2/1  3/3/1      # 顶点1、顶点2、顶点3 组成的面
f  2/2/1  3/3/1  4/4/1      # 顶点2、顶点3、顶点4 组成的面
f  1/1/1  5/10/1  8/14/6  # 顶点1、顶点5、顶点8 组成的面
......
```

其他字段的意义

- o 对象名

- g 组名

- s 平滑组

- usemtl 材质名

- mtllib 材质库.mtl

由于模型的面，可能是由三角面，四边面，乃至多边面构成的，因此对于体积的计算，可能需要去额外适配。

此处因为比较懒，因此只适配了三角面，四边面（实际还是当成三角面来适配）

对于多边形面的体积算法适配，请参考**多边形三角剖面算法**

[多边形三角剖面算法](https://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf)

不想研究的，可以自己把代码写死，切面，比如四边面的1234个点，切成123,234两个三角形的面

### 解析示例

[github](https://github.com/moyuking233/JavaLoad3D/blob/master/src/main/java/com/dragondb/javaLoad3D/obj/ObjDemo.java)



## Stl文件

### 文件构成
首先查阅网站

[wiki](https://en.wikipedia.org/wiki/STL_(file_format))

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

### 解析示例

[github](https://github.com/moyuking233/JavaLoad3D/blob/master/src/main/java/com/dragondb/javaLoad3D/stl/StlDemo.java)



# 代码实际业务使用情况

一般用来给3D模型网站的后台校验用户自定义上传的模型体积，在4C16G10M的服务器上表现一般，**无法处理高并发情况**。

因此建议在处理业务的时候，将用户的：

**上传模型**

**系统后台解析模型**

**用户下单**

三个业务流程分开



# 项目地址

[github](https://github.com/moyuking233/JavaLoad3D)



# 博客地址

[halo](https://dragondb.space/)

