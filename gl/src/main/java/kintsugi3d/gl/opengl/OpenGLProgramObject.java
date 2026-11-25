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

import kintsugi3d.gl.builders.base.ProgramBuilderBase;
import kintsugi3d.gl.core.ProgramObject;
import kintsugi3d.gl.core.Shader;
import kintsugi3d.gl.core.Texture;
import kintsugi3d.gl.core.UniformBuffer;
import kintsugi3d.gl.exceptions.InvalidProgramException;
import kintsugi3d.gl.exceptions.ProgramLinkFailureException;
import kintsugi3d.gl.exceptions.UnlinkedProgramException;
import kintsugi3d.gl.util.ResourceManager;
import kintsugi3d.gl.vecmath.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static org.lwjgl.opengl.GL31.*;

final class OpenGLProgramObject implements ProgramObject<OpenGLContext>
{
    private final OpenGLContext context;
    private final Map<String, Object> defines;

    private int programId;
    private AbstractCollection<OpenGLShader> ownedShaders;
    private ResourceManager<OpenGLTexture> textureManager;
    private ResourceManager<OpenGLUniformBuffer> uniformBufferManager;
    private boolean live = true;

    static class OpenGLProgramBuilder extends ProgramBuilderBase<OpenGLContext>
    {
        OpenGLProgramBuilder(OpenGLContext context)
        {
            super(context);
        }

        @Override
        public final @NotNull OpenGLProgramObject createProgram() throws IOException
        {
            OpenGLProgramObject program = new OpenGLProgramObject(getContext(), getDefines());
            Iterable<Shader<OpenGLContext>> compiledShaders = compileShaders();
            for (Shader<OpenGLContext> shader : compiledShaders)
            {
                program.attachShader(shader);
            }
            program.link();
            return program;
        }
    }

    private OpenGLProgramObject(OpenGLContext context, Map<String, Object> defines)
    {
        this.context = context;
        this.defines = defines;
        initProgram();
    }

    private void initProgram()
    {
        programId = glCreateProgram();
        OpenGLContext.errorCheck();
        ownedShaders = new ArrayList<>(2);
        // Use one less texture unit because we don't use texture unit 0.
        textureManager = new ResourceManager<>(context.getState().getMaxCombinedTextureImageUnits() - 1);
        uniformBufferManager = new ResourceManager<>(context.getState().getMaxCombinedUniformBlocks());
    }

    private void attachShader(Shader<OpenGLContext> shader)
    {
        if (shader instanceof OpenGLShader)
        {
            OpenGLShader shaderCast = (OpenGLShader)shader;
            glAttachShader(programId, shaderCast.getShaderId());
            OpenGLContext.errorCheck();
            ownedShaders.add(shaderCast);
        }
        else
        {
            throw new IllegalArgumentException("'shader' must be of type OpenGLShader.");
        }
    }

    private boolean isLinked()
    {
        int linked = glGetProgrami(programId, GL_LINK_STATUS);
        OpenGLContext.errorCheck();
        return linked == GL_TRUE;
    }

    private void link()
    {
        glLinkProgram(programId);
        OpenGLContext.errorCheck();
        if (!isLinked())
        {
            throw new ProgramLinkFailureException(glGetProgramInfoLog(programId));
        }
    }

    @Override
    public boolean hasDefine(String key)
    {
        return defines.containsKey(key);
    }

    @Override
    public @NotNull Optional<Object> getDefine(String key)
    {
        return defines.containsKey(key) ? Optional.of(defines.get(key)) : Optional.empty();
    }

    @Override
    public @NotNull Map<String, Object> getDefines()
    {
        return Collections.unmodifiableMap(defines);
    }

    @Override
    public @NotNull OpenGLContext getContext()
    {
        return context;
    }

    private void useForUniformAssignment()
    {
        if (isLinked())
        {
            glUseProgram(programId);
            OpenGLContext.errorCheck();
        }
        else
        {
            throw new UnlinkedProgramException("An OpenGL program cannot be used if it has not been linked.");
        }
    }

    /**
     * Needed because texture slot 0 is used as a sentinel.
     */
    private static final class TextureSentinelList extends AbstractList<OpenGLTexture>
    {
        private final List<OpenGLTexture> baseTextureList;

        private TextureSentinelList(List<OpenGLTexture> baseTextureList)
        {
            this.baseTextureList = baseTextureList;
        }

        @Override
        public int size()
        {
            return baseTextureList.size() + 1;
        }

        @Override
        public @Nullable OpenGLTexture get(int index)
        {
            return index == 0 ? null : baseTextureList.get(index - 1);
        }
    }

    void use(@NotNull OpenGLVertexArray vao)
    {
        if (isLinked())
        {
            context.updateTextureBindings(new TextureSentinelList(textureManager.asReadonlyList()));
            context.updateUniformBufferBindings(uniformBufferManager.asReadonlyList());

            vao.bind(); // needs to be bound to validate program

            glValidateProgram(programId);
            OpenGLContext.errorCheck();
            if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE)
            {
                OpenGLContext.errorCheck();

                String programInfoLog = glGetProgramInfoLog(programId);
                OpenGLContext.errorCheck();

                throw new InvalidProgramException(programInfoLog);
            }
            else
            {
                OpenGLContext.errorCheck();
            }

            glUseProgram(programId);
            OpenGLContext.errorCheck();
        }
        else
        {
            throw new UnlinkedProgramException("An OpenGL program cannot be used if it has not been linked.");
        }
    }

    @Override
    public void setTexture(int location, Texture<OpenGLContext> texture)
    {
        if (texture instanceof OpenGLTexture)
        {
            // We don't use texture unit 0, so add one to the resource ID.
            int textureUnit = 1 + textureManager.assignResourceByKey(location, (OpenGLTexture)texture);
            setUniform(location, textureUnit);
        }
        else
        {
            throw new IllegalArgumentException("'texture' must be of type OpenGLTexture.");
        }
    }

    @Override
    public void setTexture(@NotNull String name, Texture<OpenGLContext> texture)
    {
        setTexture(getUniformLocation(name), texture);
    }

    @Override
    public void setUniformBuffer(int index, UniformBuffer<OpenGLContext> buffer)
    {
        if (buffer instanceof OpenGLUniformBuffer)
        {
            if (index >= 0)
            {
                int bindingPoint = uniformBufferManager.assignResourceByKey(index, (OpenGLUniformBuffer)buffer);

                glUniformBlockBinding(programId, index, bindingPoint);
                OpenGLContext.errorCheck();
            }
        }
        else
        {
            throw new IllegalArgumentException("'buffer' must be of type OpenGLUniformBuffer.");
        }
    }

    @Override
    public void setUniformBuffer(@NotNull String name, UniformBuffer<OpenGLContext> buffer)
    {
        setUniformBuffer(getUniformBlockIndex(name), buffer);
    }

    @Override
    public int getUniformBlockIndex(@NotNull String name)
    {
        int index = glGetUniformBlockIndex(programId, name);
        OpenGLContext.errorCheck();
        return index;
    }

    @Override
    public void close()
    {
        if (live)
        {
            glDeleteProgram(programId);
            OpenGLContext.errorCheck();
            for (OpenGLShader shader : ownedShaders)
            {
                shader.close();
            }
            live = false;
        }
    }

    @Override
    public int getUniformLocation(@NotNull String name)
    {
        int location = glGetUniformLocation(programId, name);
        OpenGLContext.errorCheck();
        return location;
    }

    @Override
    public void setUniform(int location, int value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform1i(location, value);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, @NotNull IntVector2 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform2i(location, value.x, value.y);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, @NotNull IntVector3 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform3i(location, value.x, value.y, value.z);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, @NotNull IntVector4 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform4i(location, value.x, value.y, value.z, value.w);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, float value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform1f(location, value);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, @NotNull Vector2 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform2f(location, value.x, value.y);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, @NotNull Vector3 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform3f(location, value.x, value.y, value.z);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, @NotNull Vector4 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniform4f(location, value.x, value.y, value.z, value.w);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(int location, boolean value)
    {
        setUniform(location, value ? GL_TRUE : GL_FALSE);
    }

    @Override
    public void setUniform(@NotNull String name, boolean value)
    {
        setUniform(getUniformLocation(name), value ? GL_TRUE : GL_FALSE);
    }

    @Override
    public void setUniform(@NotNull String name, int value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull IntVector2 value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull IntVector3 value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull IntVector4 value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, float value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Vector2 value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Vector3 value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Vector4 value)
    {
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public int getVertexAttribLocation(@NotNull String name)
    {
        int location = glGetAttribLocation(programId, name);
        OpenGLContext.errorCheck();
        return location;
    }

    @Override
    public void setUniform(int location, @NotNull Matrix4 value)
    {
        if (location >= 0)
        {
            useForUniformAssignment();

            glUniformMatrix4fv(location, false, value.asFloatBuffer());
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public void setUniform(@NotNull String name, @NotNull Matrix4 value)
    {
        setUniform(getUniformLocation(name), value);
    }
}
