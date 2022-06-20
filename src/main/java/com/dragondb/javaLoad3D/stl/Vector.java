package com.dragondb.javaLoad3D.stl;
/**
 * @author Administrate
 */
public class Vector
{
    public Vector(float x, float y, float z) {
        X = x;
        Y = y;
        Z = z;
    }

    public Vector() {
    }

    private float X;
    private float Y;
    private float Z;

    public float Dot(Vector a)
    {
        return this.X * a.X + this.Y * a.Y + this.Z * a.Z;
    }

    public Vector Cross(Vector a)
    {
        return new Vector(
                this.Y * a.Z - this.Z * a.Y,
                this.Z * a.X - this.X * a.Z,
                this.X * a.Y - this.Y * a.X
        );
    }
}
