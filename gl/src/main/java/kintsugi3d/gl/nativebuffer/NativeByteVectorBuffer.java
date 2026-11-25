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

package kintsugi3d.gl.nativebuffer;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Objects;

public class NativeByteVectorBuffer implements NativeVectorBuffer
{
    private final @NotNull ByteBuffer buffer;

    private final int dimensions;
    private final int count;

    NativeByteVectorBuffer(int dimensions, int count)
    {
        if (dimensions < 0)
        {
            throw new IllegalArgumentException("The number of vertex dimensions cannot be negative.");
        }
        if (count < 0)
        {
            throw new IllegalArgumentException("The number of vertices cannot be negative.");
        }

        this.dimensions = dimensions;
        this.count = count;
        buffer = BufferUtils.createByteBuffer(dimensions * count);
    }

    NativeByteVectorBuffer(int dimensions, int count, @NotNull ByteBuffer buffer)
    {
        if (dimensions < 0)
        {
            throw new IllegalArgumentException("The number of vertex dimensions cannot be negative.");
        }
        if (count < 0)
        {
            throw new IllegalArgumentException("The number of vertices cannot be negative.");
        }
        if (buffer.capacity() < dimensions * count)
        {
            throw new IllegalArgumentException(MessageFormat.format(
                "Insufficient buffer size - a list of {0} {1}D vertices requires a buffer with a capacity of at least {2}.",
                count, dimensions, dimensions * count));
        }
        if (!Objects.equals(buffer.order(), ByteOrder.nativeOrder()))
        {
            throw new IllegalArgumentException("Buffers used by OpenGL must be in native byte order.");
        }

        this.dimensions = dimensions;
        this.count = count;
        this.buffer = buffer;
    }

    NativeByteVectorBuffer(int dimensions, int count, byte @NotNull [] buffer)
    {
        this(dimensions, count);
        this.buffer.put(buffer);
        this.buffer.flip();
    }

    @Override
    public final int getDimensions()
    {
        return dimensions;
    }

    @Override
    public final int getCount()
    {
        return count;
    }

    @Override
    public final @NotNull Byte get(int index, int dimension)
    {
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Index cannot be negative.");
        }
        if (index >= count)
        {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than the size of the vertex list (" + count + ").");
        }
        if (dimension < 0)
        {
            throw new IndexOutOfBoundsException("Dimension cannot be negative.");
        }
        if (dimension >= dimensions)
        {
            throw new IndexOutOfBoundsException("Dimension (" + dimension + ") is greater than the dimensions of the vertex list (" + dimensions + ").");
        }
        return buffer.get(index * dimensions + dimension);
    }

    @Override
    public final void set(int index, int dimension, @NotNull Number value)
    {
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Index cannot be negative.");
        }
        if (index >= count)
        {
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than the size of the vertex list (" + count + ").");
        }
        if (dimension < 0)
        {
            throw new IndexOutOfBoundsException("Dimension cannot be negative.");
        }
        if (dimension >= dimensions)
        {
            throw new IndexOutOfBoundsException("Dimension (" + dimension + ") is greater than the dimensions of the vertex list (" + dimensions + ").");
        }
        buffer.put(index * dimensions + dimension, value.byteValue());
    }

    @Override
    public final ByteBuffer getBuffer()
    {
        return buffer;
    }

    @Override
    public final @NotNull NativeDataType getDataType()
    {
        return NativeDataType.BYTE;
    }
}
