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
import java.util.function.IntUnaryOperator;

/**
 * A vector of two dimensions (for linear algebra calculations) backed by 
 * 32-bit integers.
 * All arithmetic other than length() or distance() will be integer arithmetic, including division.  
 * This is an immutable object.
 * 
 * @see Vector2
 */
public class IntVector2 implements Iterable<Integer>
{
    public final int x;
    public final int y;

    public IntVector2(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a vector in two dimensions with the given values.
     * @param value Value of both dimensions.
     */
    public IntVector2(@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") int value)
    {
        this(value, value);
    }

    public final IntVector3 asVector3(int z)
    {
        return new IntVector3(this.x, this.y, z);
    }

    public final IntVector4 asVector4(int z, int w)
    {
        return new IntVector4(this.x, this.y, z, w);
    }

    public final Vector2 asFloatingPoint()
    {
        return new Vector2(x, y);
    }

    public final Vector2 asFloatingPointNormalized()
    {
        return new Vector2(x / 255.0f, y / 255.0f);
    }

    public final DoubleVector2 asDoubleFloatingPoint()
    {
        return new DoubleVector2(x, y);
    }

    public final DoubleVector2 asDoubleFloatingPointNormalized()
    {
        return new DoubleVector2(x / 255.0, y / 255.0);
    }

    @Override
    public final boolean equals(Object obj)
    {
        if (obj instanceof IntVector2)
        {
            IntVector2 other = (IntVector2) obj;
            return other.x == this.x && other.y == this.y;
        }
        else
        {
            return false;
        }
    }

    @Override
    public final int hashCode()
    {
        return 31 * (31 + x) + y;
    }

    public final IntVector2 plus(IntVector2 other)
    {
        return new IntVector2(
            this.x + other.x,
            this.y + other.y
        );
    }

    public final IntVector2 minus(IntVector2 other)
    {
        return new IntVector2(
            this.x - other.x,
            this.y - other.y
        );
    }

    public final IntVector2 negated()
    {
        return new IntVector2(-this.x, -this.y);
    }

    public final IntVector2 times(int s)
    {
        return new IntVector2(s*this.x, s*this.y);
    }

    public final IntVector2 dividedBy(int s)
    {
        return new IntVector2(this.x/s, this.y/s);
    }

    public final int dot(IntVector2 other)
    {
        return this.x * other.x + this.y * other.y;
    }

    public final double length()
    {
        return Math.sqrt(this.dot(this));
    }

    public final double distance(IntVector2 other)
    {
        return this.minus(other).length();
    }

    public final IntVector2 applyOperator(IntUnaryOperator operator)
    {
        return new IntVector2(operator.applyAsInt(x), operator.applyAsInt(y));
    }

    @Override
    public final String toString()
    {
        return String.format("IntVector2{x=%d, y=%d}", x, y);
    }

    @Override
    public final @NotNull Iterator<Integer> iterator()
    {
        return Arrays.asList(x, y).iterator();
    }
}
