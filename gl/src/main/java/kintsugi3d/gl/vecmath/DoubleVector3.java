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
 * 64-bit floats.  This is an immutable object.
 * 
 * @see Vector3
 */
public class DoubleVector3 implements Iterable<Double>
{
    /**
     * The first dimension
     */
    public final double x;

    /**
     * The second dimension
     */
    public final double y;

    /**
     * The third dimension
     */
    public final double z;

    public static final DoubleVector3 ZERO = new DoubleVector3(0.0);

    /**
     * Construct a vector in three dimensions with the given values.
     * @param x Value of the first dimension.
     * @param y Value of the second dimension.
     * @param z Value of the third dimension.
     */
    public DoubleVector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct a vector in three dimensions with the given values.
     * @param value Value of all three dimensions.
     */
    public DoubleVector3(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") double value)
    {
        this(value, value, value);
    }

    public final @NotNull DoubleVector4 asVector4(double w)
    {
        return new DoubleVector4(x, y, z, w);
    }

    public final @NotNull DoubleVector4 asDirection()
    {
        return new DoubleVector4(x, y, z, 0.0);
    }

    public final @NotNull DoubleVector4 asPosition()
    {
        return new DoubleVector4(x, y, z, 1.0);
    }

    public final @NotNull DoubleVector2 getXY()
    {
        return new DoubleVector2(x, y);
    }

    public final @NotNull IntVector3 rounded()
    {
        return new IntVector3((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
    }

    public final @NotNull IntVector3 truncated()
    {
        return new IntVector3((int) x, (int) y, (int) z);
    }

    public final @NotNull Vector3 asSinglePrecision()
    {
        return new Vector3((float)x, (float)y, (float)z);
    }

    public final @NotNull DoubleVector3 plus(@NotNull DoubleVector3 other)
    {
        return new DoubleVector3(
            x + other.x,
            y + other.y,
            z + other.z
        );
    }

    public final @NotNull DoubleVector3 minus(@NotNull DoubleVector3 other)
    {
        return new DoubleVector3(
            x - other.x,
            y - other.y,
            z - other.z
        );
    }

    public final @NotNull DoubleVector3 negated()
    {
        return new DoubleVector3(-x, -y, -z);
    }

    public final @NotNull DoubleVector3 times(double s)
    {
        return new DoubleVector3(s* x, s* y, s* z);
    }

    public final @NotNull DoubleVector3 dividedBy(double s)
    {
        return new DoubleVector3(x/s, y/s, z/s);
    }

    public final @NotNull DoubleVector3 times(@NotNull DoubleVector3 other)
    {
        return new DoubleVector3(x * other.x, y * other.y, z * other.z);
    }

    public final @NotNull DoubleVector3 dividedBy(@NotNull DoubleVector3 other)
    {
        return new DoubleVector3(x / other.x, y / other.y, z / other.z);
    }

    public final double dot(@NotNull DoubleVector3 other)
    {
        return x * other.x + y * other.y + z * other.z;
    }

    public final @NotNull DoubleVector3 cross(@NotNull DoubleVector3 other)
    {
        return new DoubleVector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    public final @NotNull DoubleMatrix3 outerProduct(@NotNull DoubleVector3 other)
    {
        return DoubleMatrix3.fromColumns(
            new DoubleVector3(x * other.x, y * other.x, z * other.x),
            new DoubleVector3(x * other.y, y * other.y, z * other.y),
            new DoubleVector3(x * other.z, y * other.z, z * other.z)
        );
    }

    public final double length()
    {
        return Math.sqrt(dot(this));
    }

    public final double distance(@NotNull DoubleVector3 other)
    {
        return minus(other).length();
    }

    public final @NotNull DoubleVector3 normalized()
    {
        return times(1.0 / length());
    }
    /**
     * Create a new vector with the same direction as this one but with unit
     * magnitude (a length of 1.0).  If this vector has zero length, it will return itself.
     * @return A new vector equal to this vector divided by it's length except in cases of a zero-length vector.
     */
    public final @NotNull DoubleVector3 normalizedSafe()
    {
        double l = length();
        if (l == 0.0f)
        {
            return this;
        }
        else
        {
            return times(1.0f / l);
        }
    }

    public final @NotNull DoubleVector3 applyOperator(@NotNull DoubleUnaryOperator operator)
    {
        return new DoubleVector3(operator.applyAsDouble(x), operator.applyAsDouble(y), operator.applyAsDouble(z));
    }

    @Override
    public final @NotNull String toString()
    {
        return String.format("DoubleVector3{x=%s, y=%s, z=%s}", x, y, z);
    }

    @Override
    public final @NotNull Iterator<Double> iterator()
    {
        return Arrays.asList(x, y, z).iterator();
    }
}
