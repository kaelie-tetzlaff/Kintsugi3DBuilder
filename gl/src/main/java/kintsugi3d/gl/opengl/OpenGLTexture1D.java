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

import kintsugi3d.gl.builders.base.ColorTextureBuilderBase;
import kintsugi3d.gl.core.ColorFormat;
import kintsugi3d.gl.core.CompressionFormat;
import kintsugi3d.gl.core.Texture1D;
import kintsugi3d.gl.core.TextureWrapMode;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL44.*;

// mipmaps

final class OpenGLTexture1D extends OpenGLTexture implements Texture1D<OpenGLContext>
{
    private final int width;

    static class OpenGLTexture1DFromBufferBuilder extends ColorTextureBuilderBase<OpenGLContext, OpenGLTexture1D>
    {
        private final int textureTarget;
        private final int width;
        private final int dimensions;
        private final int type;
        private final ByteBuffer buffer;

        OpenGLTexture1DFromBufferBuilder(OpenGLContext context, int textureTarget, int width, int dimensions, int type, ByteBuffer buffer)
        {
            super(context);
            this.textureTarget = textureTarget;
            this.width = width;
            this.dimensions = dimensions;
            this.type = type;
            this.buffer = buffer;
        }

        @Override
        public final @NotNull OpenGLTexture1D createTexture()
        {
            if (isInternalFormatCompressed())
            {
                return new OpenGLTexture1D(
                    getContext(),
                    textureTarget,
                    getInternalCompressionFormat(),
                    width,
                    bundleTextureData(dimensions, false, type, buffer),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
            else
            {
                ColorFormat internalColorFormat = getInternalColorFormat();
                assert internalColorFormat != null;

                return new OpenGLTexture1D(getContext(),
                    textureTarget,
                    internalColorFormat,
                    width,
                    bundleTextureData(dimensions, internalColorFormat.isInteger(), type, buffer),
                    isLinearFilteringEnabled(),
                    areMipmapsEnabled(),
                    getMaxMipmapLevel(),
                    getMaxAnisotropy());
            }
        }
    }

    private OpenGLTexture1D(OpenGLContext context, int textureTarget, @NotNull ColorFormat colorFormat, int width, @NotNull OpenGLTextureData textureData,
                            boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context, textureTarget, colorFormat,
            new TextureParameters(textureData.getFormat(), useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy));
        this.width = width;

        init(textureTarget, OpenGLContext.getInternalColorFormat(colorFormat), textureData, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private OpenGLTexture1D(OpenGLContext context, int textureTarget, @NotNull CompressionFormat compressionFormat, int width, @NotNull OpenGLTextureData textureData,
                            boolean useLinearFiltering, boolean useMipmaps, int maxMipmapLevel, float maxAnisotropy)
    {
        // Create an empty texture to be used as a render target for a framebuffer.
        super(context, textureTarget, compressionFormat,
            new TextureParameters(textureData.getFormat(), useLinearFiltering, useMipmaps, maxMipmapLevel, maxAnisotropy));
        this.width = width;

        init(textureTarget, OpenGLContext.getCompressionFormat(compressionFormat), textureData, useLinearFiltering, maxMipmapLevel, maxAnisotropy);
    }

    private void init(
        int textureTarget, int internalFormat, @NotNull OpenGLTextureData textureData, boolean useLinearFiltering,
        int maxMipmapLevel, float maxAnisotropy)
    {
        bind();

        int type = textureData.getType();
        int format = textureData.getFormat();

        glPixelStorei(GL_UNPACK_ALIGNMENT, getUnpackAlignment(format, type));
        OpenGLContext.errorCheck();

        glTexImage1D(textureTarget, 0, internalFormat, width, 0, format, type, textureData.getBuffer());
        OpenGLContext.errorCheck();

        calculateMipmapLevelCount(width, maxMipmapLevel);
        initFilteringAndMipmaps(useLinearFiltering, maxMipmapLevel);

        glTexParameteri(textureTarget, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        OpenGLContext.errorCheck();

        if (maxAnisotropy > 1.0f)
        {
            glTexParameterf(textureTarget, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
            OpenGLContext.errorCheck();
        }
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public void setTextureWrap(@NotNull TextureWrapMode wrap)
    {
        bind();
        switch(wrap)
        {
        case NONE:
            glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            OpenGLContext.errorCheck();
            break;
        case MIRROR_ONCE:
            glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_MIRROR_CLAMP_TO_EDGE);
            OpenGLContext.errorCheck();
            break;
        case REPEAT:
            glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_REPEAT);
            OpenGLContext.errorCheck();
            break;
        case MIRRORED_REPEAT:
            glTexParameteri(getTextureTarget(), GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
            OpenGLContext.errorCheck();
            break;
        }
    }
}
