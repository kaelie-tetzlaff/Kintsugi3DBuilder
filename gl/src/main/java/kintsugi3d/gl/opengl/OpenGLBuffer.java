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

package kintsugi3d.gl.opengl;

import kintsugi3d.gl.core.ContextBound;
import kintsugi3d.gl.core.ManagedResource;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

abstract class OpenGLBuffer implements ContextBound<OpenGLContext>, ManagedResource
{
    private final OpenGLContext context;

    private final int bufferId;
    private final int usage;
    private boolean hasData = false;
    private boolean live = true;

    OpenGLBuffer(OpenGLContext context, int usage)
    {
        this.context = context;
        bufferId = glGenBuffers();
        OpenGLContext.errorCheck();
        this.usage = usage;
    }

    @Override
    public final OpenGLContext getContext()
    {
        return context;
    }

    final int getBufferId()
    {
        return bufferId;
    }

    abstract int getBufferTarget();

    final boolean hasData()
    {
        return hasData;
    }

    public final void setData(@NotNull ByteBuffer data)
    {
        if (data.remaining() > 0) // make sure there is actually data to set (lwjgl's glBufferData uses data.remaining() as the "size" parameter)
        {
            glBindBuffer(getBufferTarget(), bufferId);
            OpenGLContext.errorCheck();
            glBufferData(getBufferTarget(), data, usage);
            OpenGLContext.errorCheck();

            // fix for MacOS which segfaults if using a buffer with no data
            hasData = true;
        }
    }

    final void bind()
    {
        if (hasData) // fix for MacOS which segfaults if using a buffer with no data
        {
            glBindBuffer(getBufferTarget(), bufferId);
            OpenGLContext.errorCheck();
        }
        else
        {
            glBindBuffer(getBufferTarget(), 0);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public final void close()
    {
        if (live)
        {
            glDeleteBuffers(bufferId);
            OpenGLContext.errorCheck();
            live = false;
            hasData = false;
        }
    }
}
