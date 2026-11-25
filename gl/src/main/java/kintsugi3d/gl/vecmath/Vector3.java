/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch,
 *                           Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.gl.vecmath;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.DoubleUnaryOperator;

/**
 * A vector of three dimensions (for linear algebra calculations) backed by 
 * 32-bit floats.  This is an immutable object.
 * 
 */
public class Vector3 implements Iterable<Float>
{
    /**
     * The first dimension
     */
    public final float x;
    /**
     * The second dimension
     */
    public final float y;
    /**
     * The third dimension
     */
    public final float z;

    public static final Vector3 ZERO = new Vector3(0.0f);

    /**
     * Construct a vector in three dimensions with the given values.
     * @param x Value of the first dimension.
     * @param y Value of the second dimension.
     * @param z Value of the third dimension.
     */
    public Vector3(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct a vector in three dimensions with the given values.
     * @param value Value of all three dimensions.
     */
    public Vector3(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") float value)
    {
        this(value, value, value);
    }

    public final @NotNull Vector4 asVector4(float w)
    {
        return new Vector4(x, y, z, w);
    }

    public final @NotNull Vector4 asDirection()
    {
        return new Vector4(x, y, z, 0.0f);
    }

    public final @NotNull Vector4 asPosition()
    {
        return new Vector4(x, y, z, 1.0f);
    }

    public final @NotNull Vector2 getXY()
    {
        return new Vector2(x, y);
    }

    public final @NotNull IntVector3 rounded()
    {
        return new IntVector3(Math.round(x), Math.round(y), Math.round(z));
    }

    public final @NotNull IntVector3 truncated()
    {
        return new IntVector3((int) x, (int) y, (int) z);
    }

    public final @NotNull DoubleVector3 asDoublePrecision()
    {
        return new DoubleVector3(x, y, z);
    }

    @Override
    @SuppressWarnings("FloatingPointEquality")
    public final boolean equals(Object obj)
    {
        if (obj instanceof Vector3)
        {
            Vector3 other = (Vector3) obj;
            return other.x == x && other.y == y && other.z == z;
        }
        else
        {
            return false;
        }
    }

    @Override
    public final int hashCode()
    {
        if (equals(ZERO))
        {
            return 0;
        }
        else
        {
            int result = Float.floatToIntBits(x);
            result = 31 * result + Float.floatToIntBits(y);
            return 31 * result + Float.floatToIntBits(z);
        }
    }

    /**
     * Construct a new vector as the sum of this one and the given parameter.
     * @param other The vector to add.
     * @return A new vector that is the mathematical (componentwise) sum of this and 'other'.
     */
    public final @NotNull Vector3 plus(@NotNull Vector3 other)
    {
        return new Vector3(
            x + other.x,
            y + other.y,
            z + other.z
        );
    }

    /**
     * Construct a new vector as the subtraction of the given parameter from this.
     * @param other The vector to add.
     * @return A new vector that is the mathematical (componentwise) subtraction of 'other' from this.
     */
    public final @NotNull Vector3 minus(@NotNull Vector3 other)
    {
        return new Vector3(
            x - other.x,
            y - other.y,
            z - other.z
        );
    }

    /**
     * Construct a new vector that is the negation of this.
     * @return A new vector with the values (-x, -y, -z)
     */
    public final @NotNull Vector3 negated()
    {
        return new Vector3(-x, -y, -z);
    }

    /**
     * Construct a new vector that is the product of this and a given scaler.
     * @param s The scaler to multiply by.
     * @return A new vector equal to (s*x, s*y, s*z)
     */
    public final @NotNull Vector3 times(float s)
    {
        return new Vector3(s* x, s* y, s* z);
    }

    /**
     * Construct a new vector that is the quotient of this and a given scaler.
     * @param s The scaler to divide by.
     * @return A new vector equal to (x/s, y/s, z/s)
     */
    public final @NotNull Vector3 dividedBy(float s)
    {
        return new Vector3(x/s, y/s, z/s);
    }

    public final @NotNull Vector3 times(@NotNull Vector3 other)
    {
        return new Vector3(x * other.x, y * other.y, z * other.z);
    }

    public final @NotNull Vector3 dividedBy(@NotNull Vector3 other)
    {
        return new Vector3(x / other.x, y / other.y, z / other.z);
    }

    /**
     * Compute the dot product (scalar product) of this vector and another given vector.
     * @param other The vector to use when computing the dot product.
     * @return A scalar value equal to the sum of x1*x2, y1*y2 and z1*z2.
     */
    public final float dot(@NotNull Vector3 other)
    {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Compute the outer product of this vector and another given vector.
     * @param other The vector to use when computing the outer product.
     * @return The matrix that is the outer product of the vectors.
     */
    public final @NotNull Matrix3 outerProduct(@NotNull Vector3 other)
    {
        return Matrix3.fromColumns(
            new Vector3(x * other.x, y * other.x, z * other.x),
            new Vector3(x * other.y, y * other.y, z * other.y),
            new Vector3(x * other.z, y * other.z, z * other.z)
        );
    }

    /**
     * Compute the cross product (vector product) of this vector and another given vector.
     * @param other The vector to use when computing the dot product.
     * @return A new 3d vector with the values (y1*z2-z1*y2, z1*x2-x1*z2, x1*y2-y1*x2)
     */
    public final @NotNull Vector3 cross(@NotNull Vector3 other)
    {
        return new Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }

    /**
     * Compute a scaler value representing the length/magnitude of this vector.
     * @return A scaler value equal to square root of the sum of squares of the components.
     */
    public final float length()
    {
        return (float)Math.sqrt(dot(this));
    }

    /**
     * Calculate the distance between this and another given vector.
     * @param other The vector to compute the distance between.
     * @return A scaler value equal to the length of the different vector.
     */
    public final float distance(@NotNull Vector3 other)
    {
        return minus(other).length();
    }

    /**
     * Create a new vector with the same direction as this one but with unit
     * magnitude (a length of 1.0).  CAUTION!  May cause divide by zero error.
     * Do not attempt to normalize a zero-length vector.
     * @return A new vector equal to this vector divided by it's length.
     */
    public final @NotNull Vector3 normalized()
    {
        return times(1.0f / length());
    }

    /**
     * Create a new vector with the same direction as this one but with unit
     * magnitude (a length of 1.0).  If this vector has zero length, it will return itself.
     * @return A new vector equal to this vector divided by it's length except in cases of a zero-length vector.
     */
    public final @NotNull Vector3 normalizedSafe()
    {
        float l = length();
        if (l == 0.0f)
        {
            return this;
        }
        else
        {
            return times(1.0f / l);
        }
    }

    public final @NotNull Vector3 applyOperator(@NotNull DoubleUnaryOperator operator)
    {
        return new Vector3((float)operator.applyAsDouble(x), (float)operator.applyAsDouble(y), (float)operator.applyAsDouble(z));
    }

    @Override
    public final @NotNull String toString()
    {
        return String.format("Vector3{x=%s, y=%s, z=%s}", x, y, z);
    }

    @Override
    public final @NotNull Iterator<Float> iterator()
    {
        return Arrays.asList(x, y, z).iterator();
    }
}
