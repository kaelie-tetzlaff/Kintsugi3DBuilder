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

import kintsugi3d.gl.core.DrawableResourceBase;
import kintsugi3d.gl.core.Framebuffer;
import kintsugi3d.gl.core.PrimitiveMode;
import kintsugi3d.gl.core.ReadonlyVertexBuffer;
import kintsugi3d.gl.exceptions.UnrecognizedPrimitiveModeException;
import kintsugi3d.gl.vecmath.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

import static org.lwjgl.opengl.GL32.*;

class OpenGLDrawableResource extends DrawableResourceBase<OpenGLContext>
{
    @FunctionalInterface
    private interface VertexAttributeSetting
    {
        void set();
    }

    private final OpenGLContext context;

    private final OpenGLProgramObject program;
    private final @NotNull OpenGLVertexArray vao;
    private final @NotNull Map<Integer, VertexAttributeSetting> settings;

    OpenGLDrawableResource(OpenGLContext context, OpenGLProgramObject program)
    {
        this.context = context;
        this.program = program;
        vao = new OpenGLVertexArray(context);
        settings = new TreeMap<>();
    }

    @Override
    public final OpenGLContext getContext()
    {
        return context;
    }

    @Override
    public final void close()
    {
        vao.close();
    }

    @Override
    public final OpenGLProgramObject program()
    {
        return program;
    }

    private static int getPrimitiveModeConst(@NotNull PrimitiveMode primitiveMode)
    {
        switch(primitiveMode)
        {
        case LINES: return GL_LINES;
        case LINES_ADJACENCY: return GL_LINES_ADJACENCY;
        case LINE_LOOP: return GL_LINE_LOOP;
        case LINE_STRIP: return GL_LINE_STRIP;
        case LINE_STRIP_ADJACENCY: return GL_LINE_STRIP_ADJACENCY;
        case POINTS: return GL_POINTS;
        case TRIANGLES: return GL_TRIANGLES;
        case TRIANGLES_ADJACENCY: return GL_TRIANGLES_ADJACENCY;
        case TRIANGLE_FAN: return GL_TRIANGLE_FAN;
        case TRIANGLE_STRIP: return GL_TRIANGLE_STRIP;
        case TRIANGLE_STRIP_ADJACENCY: return GL_TRIANGLE_STRIP_ADJACENCY;
        default: throw new UnrecognizedPrimitiveModeException("Unrecognized primitive mode: " + primitiveMode);
        }
    }

    @Override
    public final void draw(@NotNull PrimitiveMode primitiveMode, @NotNull Framebuffer<OpenGLContext> framebuffer)
    {
        framebuffer.getDrawContents().bindForDraw();
        program.use(vao);
        for (VertexAttributeSetting s : settings.values())
        {
            s.set();
        }
        vao.draw(getPrimitiveModeConst(primitiveMode));
    }

    @Override
    public final void draw(@NotNull PrimitiveMode primitiveMode, @NotNull Framebuffer<OpenGLContext> framebuffer, int x, int y, int width, int height)
    {
        framebuffer.getDrawContents().bindViewportForDraw(x, y, width, height);
        program.use(vao);
        for (VertexAttributeSetting s : settings.values())
        {
            s.set();
        }
        vao.draw(getPrimitiveModeConst(primitiveMode));
    }

    @Override
    public final void draw(@NotNull PrimitiveMode primitiveMode, @NotNull Framebuffer<OpenGLContext> framebuffer, int width, int height)
    {
        draw(primitiveMode, framebuffer, 0, 0, width, height);
    }

    @Override
    public final void draw(@NotNull PrimitiveMode primitiveMode)
    {
        draw(primitiveMode, context.getDefaultFramebuffer());
    }

    @Override
    public final void addVertexBuffer(int location, ReadonlyVertexBuffer<OpenGLContext> buffer)
    {
        if (buffer instanceof OpenGLVertexBuffer)
        {
            if (location >= 0)
            {
                vao.addVertexBuffer(location, buffer);
            }
        }
        else
        {
            throw new IllegalArgumentException("'buffer' must be of type OpenGLVertexBuffer.");
        }
    }

    @Override
    public final void addVertexBuffer(@NotNull String name, ReadonlyVertexBuffer<OpenGLContext> buffer)
    {
        addVertexBuffer(program.getVertexAttribLocation(name), buffer);
    }

    @Override
    public final void setVertexAttrib(int location, int value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttribI1i(location, value);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull IntVector2 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttribI2i(location, value.x, value.y);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull IntVector3 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttribI3i(location, value.x, value.y, value.z);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull IntVector4 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttribI4i(location, value.x, value.y, value.z, value.w);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, float value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib1f(location, value);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull Vector2 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib2f(location, value.x, value.y);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull Vector3 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib3f(location, value.x, value.y, value.z);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull Vector4 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib4f(location, value.x, value.y, value.z, value.w);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, double value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib1d(location, value);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull DoubleVector2 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib2d(location, value.x, value.y);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull DoubleVector3 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib3d(location, value.x, value.y, value.z);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(int location, @NotNull DoubleVector4 value)
    {
        if (location >= 0)
        {
            settings.put(location, () ->
            {
                glVertexAttrib4d(location, value.x, value.y, value.z, value.w);
                OpenGLContext.errorCheck();
            });
        }
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, int value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull IntVector2 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull IntVector3 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull IntVector4 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, float value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull Vector2 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull Vector3 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull Vector4 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, double value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull DoubleVector2 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull DoubleVector3 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }

    @Override
    public final void setVertexAttrib(@NotNull String name, @NotNull DoubleVector4 value)
    {
        int location = program.getVertexAttribLocation(name);
        setVertexAttrib(location, value);
    }
}
