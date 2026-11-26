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
 * A vector of two dimensions (for linear algebra calculations) backed by 
 * 64-bit floats.  This is an immutable object.
 * 
 * @see Vector2
 */
public class DoubleVector2 implements Iterable<Double>
{
    public static final DoubleVector2 ZERO = new DoubleVector2(0.0f);

    /**
     * The first dimension
     */
    public final double x;

    /**
     * The second dimension
     */
    public final double y;

    /**
     * Construct a vector in two dimensions with the given values.
     * @param x Value of the first dimension.
     * @param y Value of the second dimension.
     */
    public DoubleVector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a vector in two dimensions with the given values.
     * @param value Value of both dimensions.
     */
    public DoubleVector2(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") double value)
    {
        this(value, value);
    }

    public final @NotNull DoubleVector3 asVector3()
    {
        return asVector3(0.0f);
    }

    public final @NotNull DoubleVector3 asVector3(double z)
    {
        return new DoubleVector3(x, y, z);
    }

    public final @NotNull DoubleVector4 asPosition()
    {
        return asVector4(0.0f, 1.0f);
    }

    public final @NotNull DoubleVector4 asDirection()
    {
        return asVector4(0.0f, 0.0f);
    }

    public final @NotNull DoubleVector4 asVector4(double z, double w)
    {
        return new DoubleVector4(x, y, z, w);
    }

    public final @NotNull IntVector2 rounded()
    {
        return new IntVector2((int)Math.round(x), (int)Math.round(y));
    }

    public final @NotNull IntVector2 truncated()
    {
        return new IntVector2((int) x, (int) y);
    }

    public final @NotNull Vector2 asSinglePrecision()
    {
        return new Vector2((float)x, (float)y);
    }

    public final @NotNull DoubleVector2 plus(@NotNull DoubleVector2 other)
    {
        return new DoubleVector2(
            x + other.x,
            y + other.y
        );
    }

    public final @NotNull DoubleVector2 minus(@NotNull DoubleVector2 other)
    {
        return new DoubleVector2(
            x - other.x,
            y - other.y
        );
    }

    public final @NotNull DoubleVector2 negated()
    {
        return new DoubleVector2(-x, -y);
    }

    public final @NotNull DoubleVector2 times(double s)
    {
        return new DoubleVector2(s* x, s* y);
    }

    public final @NotNull DoubleVector2 dividedBy(double s)
    {
        return new DoubleVector2(x/s, y/s);
    }

    public final @NotNull DoubleVector2 times(@NotNull DoubleVector2 other)
    {
        return new DoubleVector2(x * other.x, y * other.y);
    }

    public final @NotNull DoubleVector2 dividedBy(@NotNull DoubleVector2 other)
    {
        return new DoubleVector2(x / other.x, y / other.y);
    }

    public final double dot(@NotNull DoubleVector2 other)
    {
        return x * other.x + y * other.y;
    }

    public final double length()
    {
        return Math.sqrt(dot(this));
    }

    public final double distance(@NotNull DoubleVector2 other)
    {
        return minus(other).length();
    }

    public final @NotNull DoubleVector2 normalized()
    {
        return times(1.0 / length());
    }

    /**
     * Create a new vector with the same direction as this one but with unit
     * magnitude (a length of 1.0).  If this vector has zero length, it will return itself.
     * @return A new vector equal to this vector divided by it's length except in cases of a zero-length vector.
     */
    public final @NotNull DoubleVector2 normalizedSafe()
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

    public final @NotNull DoubleVector2 applyOperator(@NotNull DoubleUnaryOperator operator)
    {
        return new DoubleVector2(operator.applyAsDouble(x), operator.applyAsDouble(y));
    }

    @Override
    public final @NotNull String toString()
    {
        return String.format("DoubleVector2{x=%s, y=%s}", x, y);
    }

    @Override
    public final @NotNull Iterator<Double> iterator()
    {
        return Arrays.asList(x, y).iterator();
    }
}
